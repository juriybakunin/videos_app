package com.jbak.videos.providers

import android.annotation.SuppressLint
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.R
import com.jbak.videos.SerialLoader
import com.jbak.videos.model.rutube.RutubeVideo
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideoItem
import tenet.lib.base.MyLog
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

    enum class Type(id: Int, val strId : Int, var showInMenu: Boolean = true) : IItem{
        YOTUBE(1, R.string.youtube_provider),
        RUTUBE(2, R.string.rutube_provider),
        HDREZKA(3, R.string.hdrezka_provider, false),
        KINOKRAD(4, R.string.kinokrad_provider),
        KINOGO(5, R.string.kinogo_provider),
        HISTORY(6, R.string.history),
        NONE(100000, R.string.none, false);
        val mId:String

        init {
            this.mId = id.toString()
        }

        fun createProvider(): BaseVideoProvider {
            return when(this){
                KINOGO -> Kinogo()
                YOTUBE -> YouTube()
                RUTUBE -> RuTube()
                HDREZKA -> HdRezka()
                KINOKRAD -> Kinokrad()
                NONE -> throw IllegalArgumentException("Can't create provider none ")
                HISTORY -> History()
            }

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
        fun getItemClass(): Class<out VideoItem>
    }
    
    abstract class BaseVideoProvider : VideoProvider {
        override fun getUrlExpireTime(iItem: IItem, url: String): Long {
            return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(4)
        }
        fun createItem() : VideoItem {
            val cls = getItemClass()
            try {
                val constr = cls.getConstructor()
                constr.isAccessible = true
                val item = constr.newInstance()
                return item
            } catch (e : Throwable){
                MyLog.err(e)
            }
            return VideoItem()
        }

    }

    companion object {
        var LAST_LOAD_ERROR = "";
        @SuppressLint("UseSparseArrays")
        private val providersMap = HashMap<String,BaseVideoProvider>();

        fun getItemType(iItem: IItem) : Type {
            if (iItem is Type) {
                return iItem
            }
            for(t in Type.values()) {
                val p = getProvider(t)
                if(p.getItemClass() == iItem.javaClass) {
                    return t
                }
            }
            return Type.NONE
        }

        public fun getItemProvider(iItem: IItem) : BaseVideoProvider {
            return getProvider(getItemType(iItem))
        }

        fun getProvider(type: Type) : BaseVideoProvider {
            var p = providersMap[type.id]
            if(p == null) {
                p = type.createProvider()
                providersMap.put(type.id, p)
            }
            return p
        }

    }
}