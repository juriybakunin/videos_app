package com.jbak.videos.playback

import android.app.PendingIntent
import android.content.*
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.session.MediaSession
import android.text.TextUtils
import com.jbak.videos.App
import com.jbak.videos.R
import tenet.lib.base.MyLog
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.view.KeyEvent.*
import androidx.media.session.MediaButtonReceiver
import tenet.lib.tv.MediaEventListener


class PlayerReceiver : BroadcastReceiver(),AudioManager.OnAudioFocusChangeListener  {
    override fun onReceive(context: Context, intent: Intent) {
        onIntent(intent)
    }

    private var mFocRequest: Any? = null

    fun onIntent(intent: Intent): Boolean {
        val act = intent.action
        if(TextUtils.isEmpty(act))
            return false
        MyLog.log("RECEIVED BROADCAST: "+ act)
            when(act){
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> onActionNoisy()
                Intent.ACTION_MEDIA_BUTTON -> return onMediaButton(intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT))
                else -> onPlayerAction(act!!)
            }
        return true
    }

    override fun onAudioFocusChange(focusChange: Int) {
        MyLog.log("Focus change: $focusChange")
        when(focusChange) {
            AUDIOFOCUS_GAIN -> {

            }
            AUDIOFOCUS_LOSS -> {
                App.PLAYER?.getPlayback()?.pause()
            }
        }
    }

    public fun useFocus(use: Boolean) {
        if(use) {
            val result: Int
            if (Build.VERSION.SDK_INT >= 26) {
                val req = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this).build()
                result = App.get().audioManager.requestAudioFocus(req)
                mFocRequest = req
            } else {
                result = App.get().audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AUDIOFOCUS_GAIN)
            }
            val ok = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            MyLog.log("Focus granted: $ok")
        } else {
            if(Build.VERSION.SDK_INT >= 26) {
                if(mFocRequest is AudioFocusRequest) {
                    MyLog.log("Focus cancelled")
                    App.get().audioManager.abandonAudioFocusRequest(mFocRequest as AudioFocusRequest)
                }
            } else {
                App.get().audioManager.abandonAudioFocus(this)
            }
        }

    }


    fun onMediaButton(event: KeyEvent?): Boolean {
        if(event == null)
            return false
        MyLog.log("Media key: $event")
        if(event.action == KeyEvent.ACTION_DOWN) {
            when(event.keyCode) {
                KEYCODE_HEADSETHOOK, KEYCODE_MEDIA_PLAY,KEYCODE_MEDIA_PAUSE ->
                    mediaButtonsReceiver.onPlayerAction(ACT_PLAY)
                KEYCODE_MEDIA_NEXT -> mediaButtonsReceiver.onPlayerAction(ACT_NEXT)
                KEYCODE_MEDIA_PREVIOUS -> mediaButtonsReceiver.onPlayerAction(ACT_PREV)
                else->return false
            }
        }
        return true

    }

    fun onPlayerAction(action: String) {
       App.PLAYER?.let{
           when(action){
               ACT_PLAY-> {
                   if(it.getPlayback().isPlaying()) {
                       it.getPlayback().pause()
                   } else {
                       it.getPlayback().play()
                   }

               }
               ACT_NEXT-> it.getNextPrevious().playNextPreviousMedia(true)
               ACT_PREV-> it.getNextPrevious().playNextPreviousMedia(false)
               else -> MyLog.log("No player action: $action")
           }
       }
    }

    private fun onActionNoisy() {
        App.PLAYER?.getPlayback()?.pause()
    }

    companion object {
        val ACT_PLAY = App.str(R.string.act_receiver_play)
        val ACT_NEXT = App.str(R.string.act_receiver_next)
        val ACT_PREV = App.str(R.string.act_receiver_previous)


        private val focusReceiver = PlayerReceiver()
        private var mediaButtonsReceiver = PlayerReceiver()
        private var noisyReceiver = PlayerReceiver()
        private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val mediaSesionCallback: MediaSesionCallback = MediaSesionCallback()


        fun getIntent(action: String): Intent {
            return Intent(App.get(), PlayerReceiver::class.java)
                .setAction(action)
        }

        fun pendingIntent(action:String): PendingIntent {
            val pi = PendingIntent.getBroadcast(App.get(),
                action.hashCode(),
                getIntent(action),
                PendingIntent.FLAG_UPDATE_CURRENT)
            return pi
        }

        fun registerNoisyReceiver(register: Boolean) {
            if(register){
                App.get().registerReceiver(
                    noisyReceiver,
                    noisyFilter
                )

            } else {
                App.get()
                    .unregisterReceiver(noisyReceiver)
            }
        }
        fun useAudioFocus(use : Boolean) {
            focusReceiver.useFocus(true)
            mediaSesionCallback.register(use)
        }
        fun registerAudioReceivers(register: Boolean) {
            registerNoisyReceiver(register)
        }

    }
}

class MediaSesionCallback(
    val mediaSession: MediaSession = MediaSession(App.get(),"VideosApp"),
    val playerReceiver: PlayerReceiver = PlayerReceiver()
)
    : MediaSession.Callback() {
    init {
        mediaSession.setCallback(this)
        val state = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY_PAUSE
                        or PlaybackState.ACTION_SKIP_TO_NEXT
                        or PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(PlaybackState.STATE_PLAYING, 0, 0f, 0)
            .build()
        mediaSession.setPlaybackState(state)

    }

    fun register(register: Boolean) {
        mediaSession.isActive = register
    }

    override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, extras, cb)
        MyLog.log("Command: $command")
    }

    override fun onPause() {
        playerReceiver.onPlayerAction(PlayerReceiver.ACT_PLAY)
    }

    override fun onSkipToPrevious() {
        playerReceiver.onPlayerAction(PlayerReceiver.ACT_PREV)
    }

    override fun onSkipToNext() {
        playerReceiver.onPlayerAction(PlayerReceiver.ACT_NEXT)
    }

}