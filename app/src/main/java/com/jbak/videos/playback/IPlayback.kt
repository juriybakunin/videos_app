package com.jbak.videos.playback

import com.jbak.videos.types.IItem
import com.jbak.videos.types.Media
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
    fun playMedia(media: Media, startPos: Int)
    fun setMargins(margins: Boolean)
    fun addMediaListener(mediaEventListener: MediaEventListener, add: Boolean)
    fun getVideoSize(width: Boolean) : Int

}

interface IPlayer{
    fun getNextPrevious() : INextPrevious
    fun getPlayback() : IPlayback
}
