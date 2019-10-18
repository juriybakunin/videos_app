package com.jbak.videos.providers

import android.annotation.SuppressLint
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.R
import com.jbak.videos.SerialLoader
import com.jbak.videos.model.rutube.RutubeVideo
import com.jbak.videos.types.IItem
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class Factory {
    enum class Quality(id: Int, val strId : Int) : IItem{
        LOW(1,  R.string.quality_low),
        MEDIUM(2, R.string.quality_medium),
        HIGH(3, R.string.quality_high);

        val mId:String
        init {
            this.mId = id.toString()
        }

        override fun getName() : String{
            return App.str(strId)
        }

        override fun getImageUrl(): String? {
            return null
        }

        override fun getId(): String {
            return mId;
        }

        override fun getShortDescText(): String? {
            return "";
        }

    }

    enum class Type(id: Int, val strId : Int) : IItem{
        YOTUBE(1, R.string.youtube_provider),
        RUTUBE(2, R.string.rutube_provider),
        HDREZKA(3, R.string.hdrezka_provider),
        KINOKRAD(4, R.string.kinokrad_provider),
        NONE(100000, R.string.none);
        val mId:String

        init {
            this.mId = id.toString()
        }

        override fun getName() : String{
            return App.str(strId)
        }

        override fun getImageUrl(): String? {
            return null
        }

        override fun getId(): String {
            return mId;
        }

        override fun getShortDescText(): String? {
            return "";
        }
    }

    interface VideoProvider {
        fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded) : DataLoader
        fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded) : DataLoader?
        fun createSerialLoader( parentItem: IItem, onItemsLoaded: DataLoader.OnItemsLoaded ): SerialLoader?
        fun getType() : Type
        fun getUrlExpireTime(iItem: IItem, url:String) : Long
    }
    
    abstract class BaseVideoProvider : VideoProvider {
        override fun getUrlExpireTime(iItem: IItem, url: String): Long {
            return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(4)
        }
    }

    companion object {
        var LAST_LOAD_ERROR = "";
        @SuppressLint("UseSparseArrays")
        private val providersMap = HashMap<String,VideoProvider>();

        fun getItemType(iItem: IItem) : Type {
            if (iItem is Type) {
                return iItem
            }
            if(iItem is KinokradItem) {
                return Type.KINOKRAD
            } else if(iItem is HDRezkaItem) {
                 return Type.HDREZKA
            } else if(iItem is YouTubeItem) {
                 return Type.YOTUBE
            } else if(iItem is RutubeVideo) {
                 return Type.RUTUBE
            }
            return Type.NONE
        }

        public fun getItemProvider(iItem: IItem) : VideoProvider {
            return getProvider(getItemType(iItem))
        }

        fun getProvider(type: Type) : VideoProvider {
            var p = providersMap[type.id]
            if(p == null) {
                p = createProvider(type)
                providersMap.put(type.id, p)
            }
            return p
        }

        private fun createProvider(type: Type) : VideoProvider{
            return when(type){
                Type.YOTUBE -> YouTube()
                Type.RUTUBE -> RuTube()
                Type.HDREZKA -> HdRezka()
                Type.KINOKRAD -> Kinokrad()
                Type.NONE -> throw IllegalArgumentException("Can't create provider none ")
            }
        }
    }
}