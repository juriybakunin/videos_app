package com.jbak.videos.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.text.TextUtils
import com.jbak.videos.App
import com.jbak.videos.R
import tenet.lib.base.MyLog
import android.os.SystemClock
import android.media.session.PlaybackState
import android.os.Handler
import android.util.Log
import android.view.KeyEvent


class PlayerReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context, intent: Intent) {
        val act = intent.action
        if(!TextUtils.isEmpty(act)){
            MyLog.log("RECEIVED BROADCAST: "+ act)
            when(act){
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> onActionNoisy()
                else -> onPlayerAction(act!!)
            }
        }
    }

    private fun onPlayerAction(action: String) {
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



        private var mediaButtonsReceiver = PlayerReceiver()
        private var noisyReceiver = PlayerReceiver()
        private var mediaSession:MediaSession? = null
        private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val mediaButtonsFilter = IntentFilter(Intent.ACTION_MEDIA_BUTTON)
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

        fun registerAudioReceivers(register: Boolean) {
            registerNoisyReceiver(register)
            registerMediaKeysReceiver(register)
        }

        fun createMediaSession() : MediaSession {
            val audioSession = MediaSession(App.get(), "TAG")
            audioSession.setCallback(object : MediaSession.Callback() {

                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                    if(App.PLAYER == null)
                        return false
                    val intentAction = mediaButtonIntent.action
                    if (Intent.ACTION_MEDIA_BUTTON == intentAction) {
                        val event = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                        if(event.action == KeyEvent.ACTION_DOWN) {
                            when(event.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_PLAY-> mediaButtonsReceiver.onPlayerAction(ACT_PLAY)
                                KeyEvent.KEYCODE_MEDIA_PAUSE-> mediaButtonsReceiver.onPlayerAction(ACT_PLAY)
                                KeyEvent.KEYCODE_MEDIA_NEXT-> mediaButtonsReceiver.onPlayerAction(ACT_NEXT)
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS-> mediaButtonsReceiver.onPlayerAction(ACT_PREV)
                                else->return false
                            }
                        }

                    }
                    return true
                }


            })

            val state = PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, 0, 0f, 0)
                .build()
            audioSession.setPlaybackState(state)

            audioSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            return audioSession
        }

        private fun registerMediaKeysReceiver(register: Boolean) {
            if(register) {
                if(mediaSession == null)
                    mediaSession = createMediaSession()
                mediaSession!!.isActive = true

            } else {
                if(mediaSession != null){
                    mediaSession!!.isActive = false
                }
            }
        }
    }
}