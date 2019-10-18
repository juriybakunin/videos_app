package com.jbak.videos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.youtube.player.YouTubePlayer
import com.jbak.setTrueFullscreen
import com.jbak.setVisGone
import com.jbak.setVisInvis
import com.jbak.videos.playback.*
import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SerialList
import com.jbak.videos.view.ItemListView.OnItemClick
import com.jbak.videos.view.OnSerialLoaded
import kotlinx.android.synthetic.main.controller_layout.*
import kotlinx.android.synthetic.main.playlist_view.view.*
import kotlinx.coroutines.*
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Listeners
import tenet.lib.base.utils.Utils
import tenet.lib.tv.MediaEventListener
import java.util.concurrent.TimeUnit

val ACTIVITY_EVENT_PAUSE = 1
val ACTIVITY_EVENT_RESUME = 2
val ACTIVITY_EVENT_DESTROY = 3

class ControllerDialog(
    context: Context,
    videoProvider: Factory.VideoProvider
) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen),
    View.OnClickListener,
    OnItemClick,
    MediaEventListener,
    IPlayer,
    INextPrevious,
    OnSerialLoaded


{

    private var mUrl: String? = null
    private var startOnResume: Boolean = false
    var activityPaused = false
    private var mCurItem: IItem? = null
    var isVideoLoaded = false
    var iPlayback : IPlayback
    var isHidden = true;
    var mUrlLoaderJob : Job? = null
    private var topView : ViewGroup
    private var itemsList: IItem.IItemList? = null
    private val mInterceptor : VideoUrlInterceptor
    private var mProvider : Factory.VideoProvider
    private var isItemStarted = false
    private val mInitContext : Context
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
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        topView = LayoutInflater.from(context).inflate(R.layout.controller_layout,null,false) as ViewGroup
        setContentView(topView)
        iPlayback = mPlayer
        mVideoSeek.iPlayback = iPlayback
        mInterceptor = VideoUrlInterceptor(this)
        topView.setOnClickListener(this)
        mPlayPause.setOnClickListener(this)
        mNext.setOnClickListener(this)
        mPrevious.setOnClickListener(this)
        mChangeMargins.setOnClickListener(this)
        mPlayerMenu.setOnClickListener(this)

        mMargins = App.prefs().getMargins()
        mProvider = videoProvider
        mPlayer.addMediaListener(this, true)
        mPlaylistView.mListView.onItemClick = this
        mPlaylistView.mSerialView.serialListeners.registerListener(this)
        mPlaylistView.setOnSeriesClick(object : OnItemClick{
            override fun onItemClick(iItem: IItem, view: View) {
                playItem(iItem,itemsList)
            }

        })
        setOnShowListener {
            setOrientation()
            window.decorView.setOnSystemUiVisibilityChangeListener{
                if(!isHidden)
                    onSystemUiVisibilityChange(it)
            }
        }


    }

    fun setOrientation(portrait: Boolean = App.res().configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        if(!isHidden)
            window?.setTrueFullscreen(!portrait)
    }

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
            sendEmptyMessageDelayed(Const.MSG_AUTOHIDE, Const.AUTOHIDE_MILLIS)
        }


    }

    fun cancelDownload() {
        mUrlLoaderJob?.cancel()
        mInterceptor.cancel()
    }

    override fun onBackPressed() {
        PlaybackService.stop()
        App.PLAYER = null
        iPlayback.clear()
        dlgHandler.onClose()
        cancelDownload()
        isHidden = true
        cancel()
        mBufferingText.setVisInvis(false)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun show() {
        App.PLAYER = this
        isHidden = false
        iPlayback.setMargins(mMargins)
        super.show()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    fun onSystemUiVisibilityChange(visibility: Int) {
        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            dlgHandler.restoreScreen()
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

    override fun onClick(v: View?) {
        if(mPlayPause == v){
            iPlayback?.run {
                if(isPlaying()){
                    pause()
                } else {
                    play()
                }
            }
        }
        if(topView == v){
            hidePlayback()
        } else if(mPrevious == v) {
            playNextPreviousMedia(false)
        }  else if(mNext == v) {
            playNextPreviousMedia(true)
        } else if(mPlayerMenu == v){
            showMenu()
        }

        if(mChangeMargins == v){
            mMargins = !mMargins
            iPlayback?.setMargins(mMargins)
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
            item = Utils.getNextPreviousItem(next,mCurItem!!.id,itemsList as List<IItem>)
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
            mPlayer.clear()
            mLoadView.setError(error)
        } else {
            mWebView.post {
                setError(error)
            }
        }
    }
    fun playUrl(url: String) {
        if(isHidden) {
            return
        }
        if(Utils.isUIThread()) {
            mUrl = url
            MyLog.log("play video: "+url)
            mWebView.setVisInvis(false)
            mPlayerContainer.setVisInvis(true)
            mPlayer.playUrl(url)
        } else {
            mWebView.post {
                playUrl(url)
            }
        }

    }

    fun playUrlLoader(iUrlLoader : IItem.IVideoUrlLoader) {
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
                    playUrl(url)
                } else {
                    onVideoEvent(MediaEventListener.EVENT_MEDIA_ERROR,null,42,312);
                }
                mUrlLoaderJob = null;

            }
        }
    }


    fun playItem(item: IItem, items: IItem.IItemList?) {
        mCurItem = item
        mProvider = Factory.getItemProvider(item)
        if(isHidden)
            show()
        for (listener in changeItemListeners.list){
            listener.onCurItemChange(item)
        }
        iPlayback.clear()
        cancelDownload()
        isItemStarted = false
        isVideoLoaded = false
        MyLog.log("=== START LOAD ${item.id} ${item.name}")
        mVideoTitle.text = item.name
        this.itemsList = items

        val intercept = item is  IItem.IResourceIntercept
        mPlayerContainer.setVisInvis(false)
        mPlaybackView.setVisInvis(false)


        mWebView.setVisInvis(intercept)
        mLoadView.setItem(item)
        mLoadView.setLoad(true)
        val url = UrlCache.get().getVideoUrl(item);
        mCacheLoad = url != null
        if(url != null) {
            MyLog.log("Play cached url $url")
            playUrl(url)
        }
        else if(item is IItem.IVideoUrlLoader) {
            playUrlLoader(item)
        } else if(item is  IItem.IResourceIntercept) {
            mInterceptor.loadUrlItem(item)
        }
        mPlaylistView.openItem(item, itemsList)
    }


    fun playAlternativeUrl() : Boolean {
        if(!isVideoLoaded && mCurItem is IItem.INextUrl ){
            val nextUrl = (mCurItem as IItem.INextUrl).getNextUrl(mPlayer.mUrl)
            if(!TextUtils.isEmpty(nextUrl)) {
                MyLog.log("Play alternate url")
                playUrl(nextUrl!!)
                return true
            }
        }
        return false;
    }

    override fun onVideoEvent(event: Int, player: Any?, param1: Any?, param2: Any?) {
        if(event == MediaEventListener.EVENT_MEDIA_ERROR){
            if(mCacheLoad && mCurItem != null) {
                UrlCache.get().setVideoUrl(mCurItem!!, null)
            }
            else if(playAlternativeUrl()) {

            } else {
                val err = App.str(R.string.err_general)+" Err $param1 : $param2"
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
            if(!playNextPreviousMedia(true)) {
                onBackPressed()
            }
        }
        if(event == MediaEventListener.EVENT_MEDIA_STARTED) {
            if(!isVideoLoaded) {
                UrlCache.get().setVideoUrl(mCurItem!!,mPlayer.mUrl)
                isVideoLoaded = true
                mPlayerContainer.setVisInvis(true)
                mLoadView.setLoad(false)
                showPlayback()
            }
        }
        updateController()
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        if(isHidden)
            return
        setOrientation()
        mPlayerContainer.post {
            iPlayback?.setMargins(mMargins)
        }
    }

    fun showMenu(){
        val info = """
            Size: ${mPlayer.getMediaWidth()}x${mPlayer.getMediaHeight()}
            Id: ${mCurItem?.id}
            Name: ${mCurItem?.name}
            Url: ${mPlayer.mUrl}
        """.trimIndent()

        AlertDialog.Builder(mInitContext)
            .setTitle(R.string.player_info)
            .setMessage(info)
            .show()

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
            ACTIVITY_EVENT_PAUSE->{
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

            ACTIVITY_EVENT_RESUME->{
                if(startOnResume) {
                    iPlayback.play()
                }
                PlaybackService.stop()
                startOnResume = false
                activityPaused = false
            }

            ACTIVITY_EVENT_DESTROY->{
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