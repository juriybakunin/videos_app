package com.jbak.videos

import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
import com.jbak.videos.types.Media
import com.jbak.videos.types.SerialList
import tenet.lib.base.MyLog
import tenet.lib.base.utils.TimeUtils

class SerialCacher : HashMap<String, SerialList>(){
    companion object {
        val INST: SerialCacher = SerialCacher()
    }
    fun putSerial(list: SerialList){
        put(list.parentItem.id, list)
    }
    fun getSerial(iItem: IItem): SerialList?{
        var s = get(iItem.id)
        if(s == null) {
            for (e in entries) {
                if(e.value.getSeason(iItem) != null){
                    s = e.value
                    break
                }
            }
        }
        if(s == null) {
            MyLog.log("Serial not found for: ${iItem.name}")
        } else {
            MyLog.log("Serial found for: ${iItem.name}")
        }
        return s;
    }

}

class UrlInfo(val media: Media) {
    var expireTime = 0L
}


class UrlCache : java.util.HashMap<String, UrlInfo>() {

    fun getVideoUrl(item: IItem): Media? {
        val key = getItemKey(item)
        val info = get(key)
        if (info != null) {
            if (info.expireTime > System.currentTimeMillis() - 5000L) {
                return info.media
            }
            remove(key)
        }
        return null
    }

    fun setVideoMedia(item: IItem, media: Media?) {
        val key = getItemKey(item)
        if (media == null) {
            remove(key)
            return
        }
        val info = UrlInfo(media)
        val provider = Factory.getItemProvider(item)
        info.expireTime = provider.getUrlExpireTime(item, media.videoUri.toString())
        MyLog.log("Put cache, valid to " + TimeUtils.getDateText(TimeUtils.calendar(info.expireTime)))
        put(key, info)

    }

    fun reset() {
        clear()
    }


    companion object {
        private val INST = UrlCache()
        fun get(): UrlCache {
            return INST
        }

        fun getItemKey(item: IItem): String {
            val id = Factory.getItemType(item).id
            return id + "_" + item.id
        }
    }
}

class PosCache {
    companion object{
        val INST = PosCache()
    }
    fun setVideoPos(iItem: IItem, pos: Int){
        Db.get().setVideoPos(iItem.id, pos)
    }
    fun getVideoPos(iItem: IItem): Int {
        return Db.get().getVideoPos(iItem.id)
    }
}
