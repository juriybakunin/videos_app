package com.jbak.videos.view

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.jbak.videos.R
import com.jbak.videos.types.IItem
import kotlinx.android.synthetic.main.video_player_view.view.*
import kotlinx.coroutines.*
import tenet.lib.base.MyLog

class VideoPlayerView (context: Context, attributeSet: AttributeSet?):
    FrameLayout(context, attributeSet),
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {

    var mJob : Job? = null
    var mPrepared = false

    constructor(context: Context) : this(context, null)
    init {
        LayoutInflater.from(context).inflate(R.layout.video_player_view,this, true)
        setLoad(false)
        mVideo.setOnInfoListener(this)
        mVideo.setOnErrorListener(this)
        mVideo.setOnPreparedListener(this)
    }

    fun playVideo(iUrlLoader : IItem.IVideoUrlLoader) {
        mJob?.cancel()
        setLoad(true)
        val uiScope = CoroutineScope(Dispatchers.Main)
        mJob = uiScope.launch(Dispatchers.IO) {
            var url : String? = null
            try {
                url = iUrlLoader.loadVideoUrlSync()
            } catch (t : Throwable){
                MyLog.err(t)
            }
            withContext(Dispatchers.Main){
                onUrlLoaded(url)
                mJob = null;

            }
        }
    }

    fun stop() {
        mJob?.cancel()
        if(mPrepared)
            mVideo.stopPlayback()
    }

    fun onUrlLoaded(url:String?) {
        url?.let {
            MyLog.log("Url loaded: "+it)
            mVideo.setVideoURI(Uri.parse(it))
            mVideo.start()
        }
    }

    fun setLoad(load: Boolean){
        mLoad.visibility = if(load) VISIBLE else INVISIBLE
    }
    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        MyLog.log("On info: $what, $extra")
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        setLoad(false)
        mPrepared = true
        mp.start()
        MyLog.log("Video prepared")

    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        MyLog.log("Error: $what, $extra")
        mPrepared = false
        setLoad(false)
        return false
    }

}