package com.jbak.videos.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.jbak.*
import com.jbak.videos.*
import com.jbak.videos.playback.*
import com.jbak.videos.types.IItem
import com.jbak.videos.types.Media
import com.jbak.videos.types.SerialList
import com.jbak.videos.types.VideoItem
import com.jbak.videos.view.ItemListView.OnItemClick
import kotlinx.android.synthetic.main.video_player_layout.view.*
import kotlinx.android.synthetic.main.playlist_view.view.*
import kotlinx.coroutines.*
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Listeners
import tenet.lib.base.utils.Utils
import tenet.lib.tv.MediaEventListener
import java.lang.Runnable
import java.util.concurrent.TimeUnit

const val ACTIVITY_EVENT_PAUSE = 1
const val ACTIVITY_EVENT_RESUME = 2
const val ACTIVITY_EVENT_DESTROY = 3

enum class PlayerType(val id: Int) {
    MEDIA_PLAYER(1), EXO_PLAYER(2)
}

class VideoPlayer(
    context: Context, attributeSet: AttributeSet
) : FrameLayout(context, attributeSet),
    View.OnClickListener,
    OnItemClick,
    MediaEventListener,
    IPlayer,
    INextPrevious,
    OnSerialLoaded

{

    internal var mMedia: Media? = null
    private var startOnResume: Boolean = false
    var activityPaused = false
    var mCurItem: IItem? = null
    var isVideoLoaded = false
    lateinit var iPlayback : IPlayback
    var isHidden = true;
    var mUrlLoaderJob : Job? = null
    private var itemsList: IItem.IItemList? = null
    private val mInterceptor : VideoUrlInterceptor
    private var isItemStarted = false
    var onClosePlayer : Runnable? = null

    val mInitContext : Context
    val window : Window?
        get() {
            if (context is Activity) {
                return (context as Activity).window
            }
            return null
        }
    private val changeItemListeners = Listeners<IChangeItem>()
    private var mMargins = true
        set(value) {
            field = value
            App.prefs().setMargins(value)
            mChangeMargins.setImageResource(if (value) R.drawable.ic_margins_yes else R.drawable.ic_margins_no)
        }

    private var mCacheLoad = false;

    init {
        mInitContext = context
        LayoutInflater.from(context).inflate(R.layout.video_player_layout,this,true) as ViewGroup
        mInterceptor = VideoUrlInterceptor(this)
        setPlayerType(App.prefs().getPlayerType(),true)
        setOnClickListener(this)
        mPlayPause.setOnClickListener(this)
        mNext.setOnClickListener(this)
        mPrevious.setOnClickListener(this)
        mChangeMargins.setOnClickListener(this)
        mPlayerMenu.setOnClickListener(this)
        mClose.setOnClickListener(this)

        mMargins = App.prefs().getMargins()
        mPlaylistView.videoPlayer = this
        mPlaylistView.mSerialView.serialListeners.registerListener(this)
        mPlaylistView.setOnSeriesClick(object : OnItemClick{
            override fun onItemClick(iItem: IItem, view: View) {
                playItem(iItem,itemsList,false)
            }
        })
    }

    fun setPlayerType(playerType: PlayerType, init: Boolean = false){
        if(!init) {
            iPlayback.addMediaListener(this, false)
        }
        val v: View
        if(playerType == PlayerType.EXO_PLAYER) {
            val exo = ExoPlayerView(context)
            exo.userAgent = mInterceptor.defaultUserAgent
            v = exo
        } else {
            v = MediaPlayerView(context)
        }
        if(mPlayerContainer.childCount > 1)
            mPlayerContainer.removeViewAt(0)
        mPlayerContainer.addView(v)
        iPlayback = v as IPlayback
        mVideoSeek.iPlayback = iPlayback
        iPlayback.addMediaListener(this, true)
    }

    fun setOrientation(portrait: Boolean = App.res().configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {

//        if(!isHidden)
//            window?.setTrueFullscreen(!portrait)
        mClose.setVisInvis(!portrait)
    }

//    fun setOrientation(portrait: Boolean = App.res().configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//        if(!isHidden)
//            window?.setTrueFullscreen(!portrait)
//    }

    @SuppressLint("HandlerLeak")
    private val dlgHandler = object : Handler(){
        var autoHideDlg = false;
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == Const.MSG_AUTOHIDE){
                hidePlayback()
                autoHideDlg = false
            } else if (msg.what == Const.MSG_RESTORE_SCREEN){
                setOrientation()
            }

        }

        fun onClose() {
            removeMessages(Const.MSG_AUTOHIDE)
            removeMessages(Const.MSG_RESTORE_SCREEN)
        }

        fun restoreScreen() {
            removeMessages(Const.MSG_RESTORE_SCREEN)
            sendEmptyMessageDelayed(Const.MSG_RESTORE_SCREEN,TimeUnit.SECONDS.toMillis(5))
        }

        fun autoHide(){
            autoHideDlg = true
            removeMessages(Const.MSG_AUTOHIDE)
            sendEmptyMessageDelayed(
                Const.MSG_AUTOHIDE,
                Const.AUTOHIDE_MILLIS
            )
        }


    }

    fun cancelDownload() {
        mUrlLoaderJob?.cancel()
        mInterceptor.cancel()
    }

    fun saveSerialPosition() {
        mCurItem?.let {
            val s = mPlaylistView.mSerialView.serial
            if (s != null) {
                Db.get().setSerialSeriesId(s.parentItem.id, it.id)
            }
        }
    }
    fun savePosition(zero: Boolean) {
        mCurItem?.let {
            val pos = if(zero) 0 else iPlayback.currentMillis();
            PosCache.INST.setVideoPos(it, pos)
            MyLog.log("Save position: ${it.name} $pos")
            val s = mPlaylistView.mSerialView.serial
            if(s != null) {
                val firstSeries = s.isFirstSeries(it.id)
                if(firstSeries)
                    PosCache.INST.setVideoPos(s.parentItem,pos)
                saveSerialPosition()
            }
        }

    }

    fun onBackPressed() {
        PlayerReceiver.useAudioFocus(false)
        savePosition(false)
        PlaybackService.stop()
        App.PLAYER = null
        iPlayback.clear()
        dlgHandler.onClose()
        cancelDownload()
        isHidden = true
        mBufferingText.setVisInvis(false)
        mCurItem = null
        visibility = View.INVISIBLE
    }

    fun show() {
        visibility = View.VISIBLE
        App.PLAYER = this
        isHidden = false
        iPlayback.setMargins(mMargins)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if(isVisible) {
            setOrientation()
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onClosePlayer?.run()
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(isVideoLoaded && ev.action == MotionEvent.ACTION_DOWN ) {
            val intercept = !mPlaybackView.isVisible;
            showPlayback()
            if(intercept)
                 return true
        }
        return super.dispatchTouchEvent(ev)
    }

    fun updateController() {
        iPlayback.run {
            if(isPlaying())
                mPlayPause.setImageResource(R.drawable.ic_pause)
            else
                mPlayPause.setImageResource(R.drawable.ic_play)
        }
        mNext.setVisGone(getNextPreviousMedia(true) != null)
        mPrevious.setVisGone(getNextPreviousMedia(false) != null)

    }

    override fun playNextPreviousMedia(next: Boolean) : Boolean {
        val prev = getNextPreviousMedia(next)
        if(prev != null) {
            playItem(prev, itemsList)
            return true
        }
        return false
    }

    override fun onClick(v: View) {
        when(v) {
            mClose -> onBackPressed()
            this -> hidePlayback()
            mPrevious -> playNextPreviousMedia(false)
            mNext -> playNextPreviousMedia(true)
            mPlayerMenu -> showMenu()
            mChangeMargins -> {
                mMargins = !mMargins
                iPlayback.setMargins(mMargins)
                }
            mPlayPause->
                if(iPlayback.isPlaying()){
                    iPlayback.pause()
                } else {
                    iPlayback.play()
                }

        }
    }

    override fun getNextPreviousMedia(next: Boolean) : IItem? {
        if(mCurItem == null)
            return  null;
        var item : IItem? = null
        mPlaylistView.mSerialView.serial?.let {
            item = it.getNextPreviousSeries(mCurItem!!, next)
        }
        if(item == null && itemsList != null) {
            item = itemsList?.getNextPreviousItem(next, mCurItem!!.id, true)
        }
        return item
    }

    private fun hidePlayback() {
        mPlaybackView.setVisInvis(false)
    }

    override fun onItemClick(iItem: IItem, view: View) {
        playItem(iItem,mPlaylistView.getPlaylistItems())
    }

    fun showPlayback(autoHide: Boolean = true) {
        mVideoSeek.iPlayback = iPlayback
        isHidden = false
        if(autoHide)
            dlgHandler.autoHide()
        mPlaybackView.setVisInvis(true)
    }



    fun setError(error: String) {
        if(Utils.isUIThread()) {
            MyLog.log("Error: "+error)
            iPlayback.clear()
            mLoadView.setError(error)
        } else {
            mWebView.post {
                setError(error)
            }
        }
    }

    fun playMedia(media: Media) {
        if(isHidden) {
            return
        }
        if(Utils.isUIThread()) {
            mLoadView.setLoadText(App.str(R.string.load_media))
            mMedia = media
            MyLog.log("play video: $media")
            mWebView.setVisInvis(false)
            mPlayerContainer.setVisInvis(true)
            val pos = PosCache.INST.getVideoPos(mCurItem!!)
            iPlayback.playMedia(mMedia!!,pos)
        } else {
            mWebView.post {
                playMedia(media)
            }
        }

    }

    fun startUrlLoader(iUrlLoader : IItem.IVideoUrlLoader) {
        mUrlLoaderJob?.cancel()
        val uiScope = CoroutineScope(Dispatchers.Main)
        mUrlLoaderJob = uiScope.launch(Dispatchers.IO) {
            var url : String? = null
            try {
                url = iUrlLoader.loadVideoUrlSync()
            } catch (t : Throwable){
                MyLog.err(t)
            }
            withContext(Dispatchers.Main){
                if(url != null) {
                    playMedia(Media(url))
                } else {
                    onVideoEvent(MediaEventListener.EVENT_MEDIA_ERROR,null,42,312);
                }
                mUrlLoaderJob = null;

            }
        }
    }

    fun getItemFromSerial(item: IItem) : IItem{
        val list = SerialCacher.INST.getSerial(item) ?: return item
        val serId = Db.get().getSerialSeriesId(list.parentItem.id) ?: return item
        val ret = list.getItemById(serId) ?: return item
        return ret
    }


    fun playItem(item: IItem, items: IItem.IItemList?, checkSerialSeries: Boolean = true) {
        if(item.isId(mCurItem)) {
            savePosition(true)
        } else if(isVideoLoaded && mCurItem != null) {
            savePosition(false)
        }

        mCurItem = item; //getItemFromSerial(item)
        if(isHidden) {
            show()
        }
        if(checkSerialSeries)
            mCurItem = getItemFromSerial(item)
        for (listener in changeItemListeners.list){
            listener.onCurItemChange(mCurItem!!)
        }
        iPlayback.clear()
        cancelDownload()
        isItemStarted = false
        isVideoLoaded = false
        MyLog.log("=== START LOAD ${mCurItem!!.id} ${mCurItem!!.name}")
        mVideoTitle.text = mCurItem!!.name
        this.itemsList = items

        val intercept = mCurItem!! is  IItem.IResourceIntercept
        mPlayerContainer.setVisInvis(false)
        mPlaybackView.setVisInvis(false)


        mWebView.setVisInvis(intercept)
        mLoadView.setItem(mCurItem!!)
        mLoadView.setLoad(true)
        val media = UrlCache.get().getVideoUrl(mCurItem!!);
        mCacheLoad = media != null
        if(!mCacheLoad) {
            mLoadView.setLoadText(App.str(R.string.load_media_url))
        }
        if(media != null) {
            MyLog.log("Play cached: $media")
            playMedia(media)
        }
        else if(mCurItem is IItem.IVideoUrlLoader) {
            startUrlLoader(mCurItem as IItem.IVideoUrlLoader)
        } else if(mCurItem is  IItem.IResourceIntercept) {
            mInterceptor.loadUrlItem(mCurItem as IItem.IResourceIntercept)
        }
        mPlaylistView.openItem(mCurItem!!, itemsList)
    }


    fun playAlternativeUrl() : Boolean {
        if(!isVideoLoaded && mCurItem is IItem.INextMedia ){
            val nextMedia = (mCurItem as IItem.INextMedia).getNextMedia(mMedia)
            if(nextMedia != null) {
                MyLog.log("Play alternate url")
                playMedia(nextMedia!!)
                return true
            }
        }
        return false;
    }

    override fun onVideoEvent(event: Int, player: Any?, param1: Any?, param2: Any?) {
        if(event == MediaEventListener.EVENT_MEDIA_ERROR){
            if(mCacheLoad && mCurItem != null) {
                UrlCache.get().setVideoMedia(mCurItem!!, null)
            }
            else if(playAlternativeUrl()) {

            } else {
                val err = App.str(R.string.err_general) +" Err $param1 : $param2"
                mLoadView.setError(err)
            }
        }

        if(event == MediaEventListener.EVENT_MEDIA_BUFFERING_START) {
            var text = App.str(R.string.buffering);
            if(param1 is Int) {
                text = "$text $param1%"
            }
            mBufferingText.text = text
            mBufferingText.setVisInvis(true)
            return
        }
        if(event == MediaEventListener.EVENT_MEDIA_BUFFERING_END) {
            mBufferingText.setVisInvis(false)
            return
        }
        if(event == MediaEventListener.EVENT_MEDIA_COMPLETED) {
            savePosition(true)
            PlayerReceiver.useAudioFocus(false)
            if(!playNextPreviousMedia(true)) {
                onBackPressed()
            }
        }
        if(event == MediaEventListener.EVENT_MEDIA_STARTED) {
            if(!isVideoLoaded) {
                onVideoLoaded()
            }
            PlayerReceiver.useAudioFocus(true)

        }
        updateController()
    }

    private fun onVideoLoaded() {
        UrlCache.get().setVideoMedia(mCurItem!!,mMedia)
        isVideoLoaded = true
        mPlayerContainer.setVisInvis(true)
        mLoadView.setLoad(false)
        Db.get().addToHistory(mCurItem as VideoItem)
        showPlayback()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if(isHidden)
            return
        setOrientation()
        mPlayerContainer.post {
            iPlayback.setMargins(mMargins)
        }
    }

    fun showMenu(){
        val menu = VideoMenu(mInitContext, mCurItem as VideoItem)
        menu.videoPlayer = this
        menu.setOnCancelListener(object : DialogInterface.OnCancelListener{
            override fun onCancel(dialog: DialogInterface?) {
                setOrientation()
            }

        }).showMenu()
    }

    override fun getNextPrevious(): INextPrevious {
        return this
    }

    override fun getPlayback(): IPlayback {
        return iPlayback
    }

    override fun getCurrentItem(): IItem? {
        return mCurItem
    }
    override fun addChangeItemListener(iChangeItem: IChangeItem, add: Boolean) {
        if(add){
            changeItemListeners.registerListener(iChangeItem)
        } else {
            changeItemListeners.unregisterListener(iChangeItem)
        }
    }

    override fun onSerialLoaded(list: SerialList?) {
        updateController()
    }

    fun onActivityEvent(event:Int) {
        when(event){
            ACTIVITY_EVENT_PAUSE ->{
                activityPaused = true
                if(App.prefs().getPlayInBackground()) {
                    if(!isHidden)
                        PlaybackService.start(App.get())
                } else {
                    startOnResume = iPlayback.isPlaying()
                    if(startOnResume) {
                        iPlayback.pause()
                    }

                }
            }

            ACTIVITY_EVENT_RESUME ->{
                if(startOnResume) {
                    iPlayback.play()
                }
                PlaybackService.stop()
                startOnResume = false
                activityPaused = false
            }

            ACTIVITY_EVENT_DESTROY ->{
                iPlayback.clear()
                PlaybackService.stop()
            }
        }
    }

    fun setSerial(serial: SerialList) {
        if(Utils.isUIThread()){
            mPlaylistView.mSerialView.setSerialList(serial)
        } else {
            mWebView.post {
                setSerial(serial)
            }
        }
    }

}