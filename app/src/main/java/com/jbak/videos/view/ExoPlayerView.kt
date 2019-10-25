package com.jbak.videos.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.jbak.isExtension
import com.jbak.videos.playback.IPlayback
import com.jbak.videos.types.Media
import tenet.lib.base.MyLog
import tenet.lib.tv.MediaEventListener
import tenet.lib.tv.MediaEventListener.*


class ExoPlayerView(context: Context, attributeSet: AttributeSet?)
    : PlayerView(context, attributeSet)
        , IPlayback, Player.EventListener

    {

        private var mSeekStart: Int = 0
        private var isBuffering: Boolean = false
        var isPrepared = false
        constructor(context: Context) : this(context, null)
        val mediaHandler = MediaPlayerListeners()
        val exoPlayer : SimpleExoPlayer
        var userAgent = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30"
            set(value) {
                field = value
                dataSourceFactory = DefaultHttpDataSourceFactory(value)
            }
        var dataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        init {
             exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
            player = exoPlayer
            useController = false
            player.addListener(this)
        }

        override fun pause() {
            exoPlayer.playWhenReady = false
        }

        override fun play() {
            exoPlayer.playWhenReady = true
        }

        override fun isPlaying(): Boolean {
            return player.isPlaying
        }

        override fun currentMillis(): Int {
            return player.currentPosition.toInt()
        }

        override fun durationMillis(): Int {
            val d = player.duration
            if(d == C.TIME_UNSET)
                return 0
            return d.toInt()
        }

        override fun seekToMillis(millis: Long) {
            exoPlayer.seekTo(millis)
        }

        override fun clear() {
            player.stop(true)
        }

        override fun playMedia(media: Media, startPos: Int) {
            mSeekStart = startPos
            isPrepared = false
            val uri = media.videoUri;
            var source : BaseMediaSource
            if(uri.isExtension(".m3u8") || uri.isExtension(".m3u"))
                source = hlsMediaSource(uri)
            else
                source = extractMediaSource(uri)
            if(media.audioUri != null){
                val audioSource = extractMediaSource(media.audioUri)
                source = MergingMediaSource(source,audioSource)
            }
            exoPlayer.playWhenReady = true
            exoPlayer.prepare(source)
        }

        private fun hlsMediaSource(uri: Uri): HlsMediaSource {
            val hlsMediaSource = HlsMediaSource.
                Factory(dataSourceFactory)
                .createMediaSource(uri)
            return hlsMediaSource
        }

        private fun extractMediaSource(uri: Uri): BaseMediaSource {
            val source = ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(uri)
            return source
        }


        override fun setMargins(margins: Boolean) {
            resizeMode = if(margins) RESIZE_MODE_FIT else RESIZE_MODE_ZOOM
//            if(margins) {
//                s
//            }
        }

        override fun addMediaListener(mediaEventListener: MediaEventListener, add: Boolean) {
            if(add) {
                mediaHandler.registerListener(mediaEventListener)
            } else {
                mediaHandler.unregisterListener(mediaEventListener)
            }
        }
        fun sendEvent(event: Int, p1 : Any? = null, p2: Any? = null){
            mediaHandler.notifyListeners(event, player,p1,p2)
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            isPrepared = false
            sendEvent(EVENT_MEDIA_ERROR, error.toString(), 10)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when(playbackState){
                Player.STATE_READY -> {
                    if(!isPrepared) {
                        isPrepared = true
                        sendEvent(EVENT_MEDIA_PREPARE)
                        sendEvent(EVENT_MEDIA_STARTED)
                        if(mSeekStart > 0) {
                            seekToMillis(mSeekStart.toLong())
                            mSeekStart = 0
                        }
                    }
                    if(isBuffering) {
                        sendEvent(EVENT_MEDIA_BUFFERING_END)
                        isBuffering = false

                    }
                }
                Player.STATE_ENDED -> {
                    isPrepared = false
                    sendEvent(EVENT_MEDIA_COMPLETED)
                }
                Player.STATE_BUFFERING -> {
                    sendEvent(EVENT_MEDIA_BUFFERING_START)
                    isBuffering = true
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            sendEvent(if(isPlaying) EVENT_MEDIA_PAUSED else EVENT_MEDIA_STARTED)
        }
        override fun getVideoSize(width: Boolean): Int {
            return if(width) exoPlayer.videoFormat?.width ?:0 else exoPlayer.videoFormat?.height ?:0
        }

    }