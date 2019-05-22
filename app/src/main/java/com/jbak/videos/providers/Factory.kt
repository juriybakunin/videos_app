package com.jbak.videos.providers

import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.R

class Factory {

    enum class Type(val id: Int, val strId : Int) {
        YOTUBE(1, R.string.youtube_provider),
        RUTUBE(2, R.string.rutube_provider),
        HDREZKA(3, R.string.hdrezka_provider);

        fun getName() : String{
            return App.str(strId)
        }
    }

    interface VideoProvider {
        fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded) : DataLoader
        fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded) : DataLoader
        fun getType() : Type
    }

    companion object {
        fun createProvider(type: Type) : VideoProvider{
            return when(type){
                Type.YOTUBE -> YouTube()
                Type.RUTUBE -> RuTube()
                Type.HDREZKA -> HdRezka()
            }
        }
    }
}