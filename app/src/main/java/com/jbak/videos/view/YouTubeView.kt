package com.jbak.videos.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.youtube.player.YouTubePlayerView

class YouTubeView : FrameLayout {
    lateinit var youTubePlayer : YouTubePlayerView
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    private fun init(){
        youTubePlayer = YouTubePlayerView(context)
        addView(youTubePlayer)
    }
}

