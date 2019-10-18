package com.jbak.videos

import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
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

class UrlInfo(val url: String) {
    var expireTime = 0L
}


class UrlCache : java.util.HashMap<String, UrlInfo>() {

    fun getVideoUrl(item: IItem): String? {
        val key = getItemKey(item)
        val info = get(key)
        if (info != null) {
            if (info.expireTime > System.currentTimeMillis() - 5000L) {
                return info.url
            }
            remove(key)
        }
        return null
    }

    fun setVideoUrl(item: IItem, url: String?) {
        val key = getItemKey(item)
        if (url == null) {
            remove(key)
            return
        }
        val info = UrlInfo(url)
        val provider = Factory.getItemProvider(item)
        info.expireTime = provider.getUrlExpireTime(item, url)
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
