package com.jbak.videos

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.media.AudioManager
import com.google.android.exoplayer2.Player
import com.jbak.videos.playback.PlayerReceiver
import com.jbak.videos.playback.IPlayer
import com.jbak.videos.playback.PlayerUtils
import com.jbak.videos.providers.YouTube
import tenet.lib.base.MyLog
import tenet.lib.base.TenetApp

class App : TenetApp() {
    private lateinit var prefs: Prefs
    lateinit var audioManager: AudioManager
    var audioSession: Int = 0
    override fun onCreate() {
        INST = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioSession = audioManager.generateAudioSessionId()
        super.onCreate()
        MyLog.setDefTag("Videos")
        prefs = Prefs(getSharedPreferences("prefs",0));
        YouTube.USE_INTERCEPTOR = prefs.getYoutubeInterceptor()
        PlayerUtils.disableSSLCertificateChecking()
    }



    companion object {
        var PLAYER: IPlayer? = null
            set(value) {
                field = value
                PlayerReceiver.registerAudioReceivers(value != null)
//                if(value!=null)
//                    PlayerReceiver.createMediaSession()
            }
        fun dpToPx(dp: Int): Int {
            val density = res().displayMetrics.density
            return Math.round(dp.toFloat() * density)
        }

        lateinit var INST:App
        /** Возвращает строку id из ресурсов  */
        fun str(id: Int): String {
            return INST.getString(id)
        }

        /** Возвращает true, если приложение дебажное  */
        fun isDebug(): Boolean {
            return INST.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE > 0
        }


        /** Возвращает ресурсы приложения  */
        fun res(): Resources {
            return INST.resources
        }
        fun get() : App {
            return INST as App
        }

        fun prefs() : Prefs{
            return get().prefs;
        }
    }
}