package com.jbak.videos.view

import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import com.jbak.videos.App
import com.jbak.videos.playback.IPlayback
import com.jbak.videos.playback.PlayerUtils
import com.jbak.videos.types.Media
import tenet.lib.base.MyLog
import tenet.lib.tv.MediaEventListener
import tenet.lib.tv.MediaEventListener.*

class MediaPlayerView(context: Context, attributeSet: AttributeSet?)
    : SurfaceView(context,attributeSet),
    IPlayback,MediaEventListener
{

    constructor(context: Context) : this(context, null)
    private var mStartPos: Int = 0
    var mUrl: String? = null
    val mediaPlayer : MediaPlayer = MediaPlayer()
    var prepared = false
    var mMargins =  false
    val mediaHandler = MediaPlayerListeners()
    init {
        mediaPlayer.audioSessionId = App.get().audioSession
        mediaHandler.setToMediaPlayer(mediaPlayer)
        mediaHandler.registerListener(this)
        holder.addCallback(object: SurfaceHolder.Callback{
            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mediaPlayer.setDisplay(null)
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mediaPlayer.setDisplay(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                mediaPlayer.setDisplay(holder)

            }

        })
    }


    private fun setSizeFromPlayer() {
        if(prepared){
            val videoWidth = mediaPlayer.videoWidth
            val videoHeight = mediaPlayer.videoHeight
            setSize(videoWidth, videoHeight)
        }

    }
    override fun addMediaListener(mediaEventListener: MediaEventListener, add: Boolean) {
        if(add)
            mediaHandler.registerListener(mediaEventListener)
        else
            mediaHandler.unregisterListener(mediaEventListener)

    }

    override fun setMargins(margins: Boolean) {
        mMargins = margins
        setSizeFromPlayer()
    }

    override fun pause() {
        if(prepared && isPlaying()) {
            mediaPlayer.pause()
            mediaHandler.notifyListeners(EVENT_MEDIA_PAUSED,mediaPlayer, null, null)
        }
    }

    override fun play() {
        if(prepared && !isPlaying()){
            mediaPlayer.start()
            mediaHandler.notifyListeners(EVENT_MEDIA_STARTED,mediaPlayer, null, null)
        }
    }

    override fun isPlaying(): Boolean {
        return prepared && mediaPlayer.isPlaying
    }

    override fun currentMillis(): Int {
        if(prepared) {
            return mediaPlayer.currentPosition
        }
        return 0
    }

    override fun getVideoSize(width: Boolean) : Int{
        if(prepared) {
            return  if(width) mediaPlayer.videoWidth else mediaPlayer.videoHeight
        }
        return 0
    }

    override fun durationMillis(): Int {
        if(prepared) {
            return mediaPlayer.duration
        }
        return 0
    }

    override fun seekToMillis(millis: Long) {
        if(prepared) {
            mediaPlayer.seekTo(millis.toInt())
        }
    }

    override fun clear() {
        mediaPlayer.reset()
        prepared = false
    }


    override fun playMedia(media: Media, startPos: Int) {
        mStartPos = startPos;
        prepared = false
        mUrl = media.videoUri.toString()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(media.videoUri.toString())
        mediaPlayer.prepareAsync()
    }


    override fun onVideoEvent(event: Int, player: Any?, param1: Any?, param2: Any?) {
        Func.logMediaEvent(event, param1, param2)
        if(event == EVENT_MEDIA_ERROR){
            prepared = false
        }
        else if(event == EVENT_MEDIA_PREPARE){
            prepared = true
            if(mStartPos > 0){
                mediaPlayer.seekTo(mStartPos)
            } else {
                mediaPlayer.start()
            }
        } else if(event == EVENT_MEDIA_COMPLETED){

        } else if(event == EVENT_MEDIA_SIZE_CHANGED){
            setSize(param1 as Int, param2 as Int);
        } else if(event == EVENT_MEDIA_SEEK_COMPLETED) {
            if(mStartPos > 0) {
                mStartPos = 0
                mediaPlayer.start()
            }
        }

    }

    fun setSize(videoWidth:Int, videoHeight:Int){
        val fl = parent as? FrameLayout
        if(fl == null) {
            MyLog.log("No parent in setSize, return")
            return
        }
        if(!prepared){
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
}