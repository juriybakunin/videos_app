package com.jbak.videos.playback

import com.google.android.youtube.player.YouTubePlayer
import com.jbak.videos.types.IItem
import tenet.lib.tv.MediaEventListener

interface IChangeItem {
    fun onCurItemChange(iItem: IItem)
}

interface INextPrevious{
    fun getNextPreviousMedia(next :Boolean) : IItem?
    fun playNextPreviousMedia(next: Boolean) : Boolean
    fun getCurrentItem() : IItem?
    fun addChangeItemListener(iChangeItem: IChangeItem, add: Boolean)
}

interface IPlayback {
    fun pause()
    fun play()
    fun isPlaying() : Boolean
    fun currentMillis() : Int
    fun durationMillis() : Int
    fun seekToMillis(millis: Long)
    fun clear()
    fun playUrl(url : String)
    fun setMargins(margins: Boolean)
    fun addMediaListener(mediaEventListener: MediaEventListener, add: Boolean)



    companion object {
        fun createYoutubeCallback(player: YouTubePlayer) : IPlayback {
            return YouTubePlayback(player)
        }
        class YouTubePlayback(val player: YouTubePlayer) : IPlayback {
            override fun addMediaListener(mediaEventListener: MediaEventListener, add: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setMargins(margins: Boolean) {

            }

            override fun playUrl(url: String) {

            }

            override fun clear() {
                player.pause()
            }

            override fun seekToMillis(millis: Long) {
                player.seekToMillis(millis.toInt())
            }

            override fun currentMillis(): Int {
                return player.currentTimeMillis
            }

            override fun durationMillis(): Int {
                return player.durationMillis
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

interface IPlayer{
    fun getNextPrevious() : INextPrevious
    fun getPlayback() : IPlayback
}
