package com.jbak.videos

import android.content.SharedPreferences
import com.jbak.videos.providers.Factory

class Prefs(private var prefs: SharedPreferences) {
    fun saveSearch(query : String){
        prefs.edit().putString("searchQuery",query).apply()
    }

    fun getSearch() : String{
        return prefs.getString("searchQuery","")?:""
    }

    fun setProviderType(type: Factory.Type){
        prefs.edit().putInt("providerId",type.id).apply()
    }

    fun getProviderType() : Factory.Type{
        val id = prefs.getInt("providerId", Factory.Type.YOTUBE.id)
        for (t in Factory.Type.values()){
            if(t.id == id)
                return t
        }
        return Factory.Type.YOTUBE
    }
}
