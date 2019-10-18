package com.jbak.videos.view

import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.VideoView
import com.jbak.videos.playback.IPlayback
import com.jbak.videos.playback.PlayerUtils
import tenet.lib.base.MyLog
import tenet.lib.tv.MediaEventListener

class VideoPlayerView (context: Context, attributeSet: AttributeSet?):
    VideoView(context, attributeSet),

    IPlayback {

    var mUrl: String = ""
    var Listener: MediaEventListener? = null
    var mPrepared = false
    var mMargins = true
    var videoWidth = 0;
    var videoHeight = 0;

    constructor(context: Context) : this(context, null)
    init {

        setOnInfoListener{
            mediaPlayer: MediaPlayer, i: Int, i1: Int ->
            sendEvent(MediaEventListener.EVENT_MEDIA_INFO, mediaPlayer, i, i1)
        }
        setOnErrorListener{
            mediaPlayer: MediaPlayer, i: Int, i1: Int ->
            sendEvent(MediaEventListener.EVENT_MEDIA_ERROR, mediaPlayer, i, i1)
        }
        setOnPreparedListener{
            mPrepared = true
            sendEvent(MediaEventListener.EVENT_MEDIA_PREPARE, it)

        }
        setOnCompletionListener {
            sendEvent(MediaEventListener.EVENT_MEDIA_COMPLETED, it)
        }

    }


    override fun clear() {
        stopPlayback()
    }

    override fun pause() {
        super.pause()
        if(mPrepared) {
            sendEvent(MediaEventListener.EVENT_MEDIA_PAUSED,null)
        }
    }



    fun sendEvent(event: Int, player: MediaPlayer?, param1: Any? = null, param2: Any? = null): Boolean {
        MediaEventListener.Func.logMediaEvent(event,param1,param2)
        Listener?.onVideoEvent(event,player,param1,param2)
        if(mPrepared && player != null){
            videoWidth = player.videoWidth
            videoHeight = player.videoHeight;
            if(event == MediaEventListener.EVENT_MEDIA_PREPARE){
                setSize(videoWidth,videoHeight)
            }
        }

        return true
    }

    override fun start() {
        super.start()
        if (mPrepared) {
            sendEvent(MediaEventListener.EVENT_MEDIA_STARTED, null)
        }

    }

    override fun play() {
        start()
    }
    override fun playUrl(url: String) {
        mUrl = url;
        mPrepared = false;
        MyLog.log("Play url: "+url)
        setVideoURI(Uri.parse(url))
        start()
    }


    override fun currentMillis(): Int {
        return currentPosition
    }

    override fun durationMillis(): Int {
        return duration
    }

    override fun seekToMillis(millis: Long) {
        return seekTo(millis.toInt())
    }

    override fun setMargins(margins: Boolean) {
        mMargins = margins
        setSizeFromPlayer()
    }

    private fun setSizeFromPlayer() {
        if(mPrepared){
            setSize(videoWidth, videoHeight)
        }

    }

    fun setSize(videoWidth:Int, videoHeight:Int){
        val fl = parent as? FrameLayout
        if(fl == null) {
            MyLog.log("No parent in setSize, return")
            return
        }
        if(!mPrepared){
            MyLog.log("Not prepared in setSize, return")
            return
        }
        val flp = PlayerUtils.getProportionalLayoutParams(this,mMargins, videoWidth.toDouble(),videoHeight.toDouble());
        layoutParams = flp
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        setSizeFromPlayer();
    }


    override fun addMediaListener(mediaEventListener: MediaEventListener,add: Boolean) {
        if(add)
            Listener = mediaEventListener
        else if(Listener == mediaEventListener)
            Listener = null
    }

}