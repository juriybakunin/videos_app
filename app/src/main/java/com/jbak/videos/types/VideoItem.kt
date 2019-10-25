package com.jbak.videos.types

import com.google.gson.Gson
import com.jbak.videos.providers.Factory
import tenet.lib.base.types.BaseIdNamed

open class VideoItem() : BaseIdNamed<VideoItem>(), IItem {

    companion object {
        val GSON = Gson()
        val DELIM = "\n-=-\n"
    }

    val providerType: Factory.Type
        get() = Factory.getItemType(this)

    var image : String = ""
    var dur : String = ""
    var extra: String = ""


    override fun getShortDescText(): String? {
        return dur
    }

    open fun getItemUrl(): String? {
        return id
    }

    override fun getImageUrl(): String {
        return image
    }


    fun setData(id : String, name : String, imageUrl : String, dur : String) {
        setIdAndName(id,name)
        image = imageUrl
        this.dur = dur
    }


    fun getStringData(): String {
        return "$id$DELIM$name$DELIM$image$DELIM$dur$DELIM$extra"
    }

    fun setStringData(data: String) {
        setStringData(data.split(DELIM))
    }

    open fun setStringData(items: List<String>) : Int {
        var index = 0
        id = items[index++]
        name = items[index++]
        image = items[index++]
        dur = items[index++]
        extra = items[index++]
        return index;
    }

}