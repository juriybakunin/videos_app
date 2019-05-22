package com.jbak.videos

import android.content.pm.ApplicationInfo
import android.content.res.Resources
import tenet.lib.base.MyLog
import tenet.lib.base.TenetApp

class App : TenetApp() {
    private lateinit var prefs: Prefs
    override fun onCreate() {
        INST = this
        super.onCreate()
        MyLog.setDefTag("Videos")
        prefs = Prefs(getSharedPreferences("prefs",0));
    }




    companion object {

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