package com.jbak.videos

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.jbak.videos.view.MyYouTubePlayerView
import tenet.lib.base.utils.Utils

class MyYouTubeFragment : YouTubePlayerFragment(),YouTubePlayer.OnInitializedListener {


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(Const.YOUTUBE_KEY,this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {

    }

    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, player: YouTubePlayer?, p2: Boolean) {
        val frag = view?.parent as MyYouTubePlayerView
        player?.run {
            frag.setYouTubePlayer(player,this@MyYouTubeFragment)
        }

    }

}
