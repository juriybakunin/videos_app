package com.jbak.videos

import com.google.android.youtube.player.YouTubePlayer
import com.jbak.videos.types.IItem

interface IPlayback {
    fun pause()
    fun play()
    fun isPlaying() : Boolean
    fun playVideo(iItem: IItem)
    fun currentMillis() : Int
    fun durationMillis() : Int
    fun seekToMillis(millis: Long)


    companion object {
        fun createYoutubeCallback(player: YouTubePlayer) : IPlayback {
            return YouTubePlayback(player)
        }
        class YouTubePlayback(val player: YouTubePlayer) : IPlayback{
            override fun seekToMillis(millis: Long) {
                player.seekToMillis(millis.toInt())
            }

            override fun currentMillis(): Int {
                return player.currentTimeMillis
            }

            override fun durationMillis(): Int {
                return player.durationMillis
            }

            override fun playVideo(iItem: IItem) {
                player.loadVideo(iItem.id)
            }

            override fun isPlaying(): Boolean {
                return player.isPlaying
            }

            override fun pause() {
                player.pause()
            }

            override fun play() {
                player.play()
            }
        }
    }
}