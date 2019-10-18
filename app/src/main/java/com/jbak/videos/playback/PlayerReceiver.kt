package com.jbak.videos.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.text.TextUtils
import com.jbak.videos.App
import com.jbak.videos.R
import tenet.lib.base.MyLog

class PlayerReceiver : BroadcastReceiver() {
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

        private var noisyReceiver = PlayerReceiver()
        private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
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
    }
}