package com.jbak.videos.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.jbak.videos.playback.IPlayback
import com.jbak.videos.R
import kotlinx.android.synthetic.main.seek_bar_layout.view.*
import tenet.lib.base.utils.TimeUtils

class VideoSeek : ConstraintLayout, SeekBar.OnSeekBarChangeListener{

    var iPlayback : IPlayback? = null
        set(value) {
            field = value
            if(value == null)
                seekHandler.stopUpdate()
            else {
                seekHandler.startUpdate()
                updateSeekBar()
            }
        }

    @SuppressLint("HandlerLeak")
    private val seekHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 1){
                updateSeekBar()
                sendEmptyMessageDelayed(1,500)
            }
        }

        fun stopUpdate() {
            removeMessages(1)
        }

        fun startUpdate() {
            stopUpdate()
            sendEmptyMessageDelayed(1,500)
        }
    }

    constructor(context: Context, attrs: AttributeSet? = null):super(context,attrs){
        LayoutInflater.from(context).inflate(R.layout.seek_bar_layout,this,true)
        mSeekBar.max = 1
        mSeekBar.progress = 1
        mSeekBar.setOnSeekBarChangeListener(this)
    }

    fun updateSeekBar(){
        iPlayback?.let {
            val pos = it.currentMillis()/1000
            val total = it.durationMillis()/1000
            if(mSeekBar.max != total)
                mSeekBar.max = total
            if(mSeekBar.progress != pos)
                mSeekBar.progress = pos

            mTimeCur.text = TimeUtils.getTimeRangeText(pos,null)
            mTimeTotal.text = TimeUtils.getTimeRangeText(total,null)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        iPlayback = null
        seekHandler.stopUpdate()

    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if(fromUser) {
            mTimeCur.text = TimeUtils.getTimeRangeText(progress,null)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        seekHandler.stopUpdate()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        iPlayback?.seekToMillis(seekBar.progress.toLong() * 1000L)
        updateSeekBar()
        if(iPlayback != null)
            seekHandler.startUpdate()
    }

}