package com.jbak.videos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.*
import com.google.android.youtube.player.YouTubePlayer
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideosList
import com.jbak.videos.view.ItemListView
import com.jbak.videos.view.ItemView
import kotlinx.android.synthetic.main.controller_layout.*
import kotlinx.android.synthetic.main.controller_layout.view.*
import tenet.lib.base.Err
import tenet.lib.base.utils.Utils
import java.util.concurrent.TimeUnit

class ControllerDialog : Dialog, DataLoader.OnItemsLoaded,
    View.OnClickListener,YouTubePlayer.PlaybackEventListener,ItemListView.OnItemClick


{

    companion object {
        val MSG_AUTOHIDE = 10;
        val AUTOHIDE_MILLIS = TimeUnit.SECONDS.toMillis(8)
    }
    var iPlayback : IPlayback? = null
        set(value) {
            field = value
            mVideoSeek.iPlayback = value
        }
    var isHidden = true;
    private var topView : ViewGroup
    private var itemsList: IItem.IItemList? = null
    private var previousItemView : ItemView
    private var nextItemView : ItemView

    @SuppressLint("HandlerLeak")
    private val dlgHandler = object : Handler(){
        var autoHideDlg = false;
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == MSG_AUTOHIDE){
                hide()
                autoHideDlg = false
            }
        }

        fun autoHide(){
            autoHideDlg = true
            removeMessages(MSG_AUTOHIDE)
            sendEmptyMessageDelayed(MSG_AUTOHIDE, AUTOHIDE_MILLIS)
        }
    }

    constructor(context: Context) : super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen){
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        topView = LayoutInflater.from(context).inflate(R.layout.controller_layout,null,false) as ViewGroup
        setContentView(topView)

        mListView.setType(ItemListView.Type.RELATED)
        mListView.onItemClick = this
        mCtrlPause.setOnClickListener(this)
        topView.setOnClickListener(this)
        previousItemView = topView.mPrevious
        nextItemView = topView.next
        previousItemView.setOnClickListener(this)
        nextItemView.setOnClickListener(this)

    }

    override fun onItemsLoaded(err: Err, videosList: VideosList) {
        if(err.isOk) {
            mListView.getItemAdapter().setList(videosList)

        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(!isHidden && dlgHandler.autoHideDlg)
            dlgHandler.autoHide()
        return super.dispatchTouchEvent(ev)

    }

    fun updateController() {
        iPlayback?.run {
            if(isPlaying())
                mCtrlPause.setImageResource(android.R.drawable.ic_media_pause)
            else
                mCtrlPause.setImageResource(android.R.drawable.ic_media_play)
        }
    }
    
    override fun onClick(v: View?) {
        if(mCtrlPause == v){
            iPlayback?.run {
                if(isPlaying()){
                    pause()
                } else {
                    play()
                }
            }
        }
        if(topView == v){
            hide()
        }
        if(previousItemView == v) {
            if(previousItemView.videoItem != null)
                playVideo(previousItemView.videoItem!!,itemsList)
        }

        if(nextItemView == v) {
            if(nextItemView.videoItem != null)
                playVideo(nextItemView.videoItem!!,itemsList)
        }

    }

    fun playVideo(videoItem: IItem, videosList: IItem.IItemList?) {
        onOpenVideo(videoItem,videosList)
        hide()
        iPlayback?.playVideo(videoItem)
    }

    override fun onSeekTo(sec: Int) {

    }

    override fun onBuffering(p0: Boolean) {
    }

    override fun onPlaying() {
        updateController()
    }

    override fun onStopped() {
        updateController()
    }

    override fun onPaused() {
        updateController()
    }

    override fun onItemClick(iItem: IItem, view: View) {
        onOpenVideo(iItem,mListView.getItemAdapter().items)
        iPlayback?.playVideo(iItem)
    }

    fun show(autoHide: Boolean) {
        super.show()
        mVideoSeek.iPlayback = iPlayback
        mListView.setLayout()
        isHidden = false
        if(autoHide)
            dlgHandler.autoHide()
    }
    override fun show() {
        show(true)
    }

    override fun hide() {
        super.hide()
        mVideoSeek.iPlayback = null
        isHidden = true
    }

    fun onVideoLoaded(videoId: String) {
        var show = itemsList != null
        itemsList?.let {
            val list = it as List<IItem>
            val prev = Utils.getNextPreviousItem(false,videoId, list)
            val next = Utils.getNextPreviousItem(true,videoId, list)
            show = prev != null && next != null
            if (show) {
                previousItemView.setItem(prev)
                nextItemView.setItem(next)
            }
        }
        if(show) {
            previousItemView.visibility = View.VISIBLE
            nextItemView.visibility = View.VISIBLE
        } else{
            previousItemView.visibility = View.GONE
            nextItemView.visibility = View.GONE
        }

    }

    fun onOpenVideo(iItem: IItem, videosList: IItem.IItemList?) {
        mListView.clear()
        mVideoTitle.text = iItem.name
        this.itemsList = videosList
    }
}