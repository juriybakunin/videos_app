package com.jbak.videos

import android.content.SharedPreferences
import com.jbak.videos.providers.Factory
import com.jbak.videos.providers.YouTube
import com.jbak.videos.view.PlayerType
import tenet.lib.base.utils.Utils

class Prefs(private var prefs: SharedPreferences) {
    fun saveSearch(query : String){
        prefs.edit().putString("searchQuery",query).apply()
    }

    fun getSearch() : String{
        return prefs.getString("searchQuery","")?:""
    }

    fun setYoutubeInterceptor(interceptor: Boolean){
        YouTube.USE_INTERCEPTOR = interceptor
        prefs.edit().putBoolean("useYoutubeInterceptor", interceptor).apply()
    }
    fun getYoutubeInterceptor() : Boolean{
        return prefs.getBoolean("useYoutubeInterceptor",false)
    }

    fun setProviderType(type: Factory.Type){
        prefs.edit().putString("curProviderId",type.id).apply()
    }
    fun setMargins(margins: Boolean){
        prefs.edit().putBoolean("margins",margins).apply()
    }
    fun getMargins() : Boolean{
        return prefs.getBoolean("margins",true)
    }

    fun getProviderType() : Factory.Type{
        val id = prefs.getString("curProviderId", Factory.Type.YOTUBE.id)
        for (t in Factory.Type.values()){
            if(t.id.equals(id))
                return t
        }
        return Factory.Type.YOTUBE
    }

    fun getPlayInBackground(): Boolean {
        return prefs.getBoolean("playInBackground", false)
    }

    fun setPlayInBackground(playInBackground: Boolean){
        prefs.edit().putBoolean("playInBackground", playInBackground).apply()
    }

    fun setQuality(quality: Factory.Quality) {
        prefs.edit().putString("videoQuality",quality.id).apply()
    }

    fun getQuality() : Factory.Quality {
        val id = prefs.getString("videoQuality", Factory.Quality.HIGH.id)
        return Utils.itemById(id, Factory.Quality.values())
    }

    fun getPlayerType(): PlayerType {
        val id = prefs.getInt("playerType", PlayerType.MEDIA_PLAYER.id)
        return if(id == PlayerType.MEDIA_PLAYER.id) PlayerType.MEDIA_PLAYER else PlayerType.EXO_PLAYER
    }

    fun setPlayerType(playerType: PlayerType) {
        prefs.edit().putInt("playerType", playerType.id).apply()
    }
}
