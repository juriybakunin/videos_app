package com.jbak.videos.types

import com.google.gson.Gson
import tenet.lib.base.types.BaseIdNamed

open class SimpleItem() : BaseIdNamed<SimpleItem>(), IItem {

    companion object {
        val GSON = Gson()
        val DELIM = "\n- # -\n"
    }

    var image : String = ""
    var dur : Int = 0

    override fun getDuration(): Int {
        return dur
    }

    override fun getImageUrl(): String {
        return image
    }


    fun setData(id : String, name : String, imageUrl : String, dur : Int) {
        setIdAndName(id,name)
        image = imageUrl
        this.dur = dur
    }

    fun getStringData(): String {
        return "$id$DELIM$name$DELIM$image$DELIM$dur"
    }

    fun setStringData(data: String) {
        setStringData(data.split(DELIM))
    }

    open fun setStringData(items: List<String>) : Int {
        var index = 0
        id = items[index++]
        name = items[index++]
        image = items[index++]
        dur = items[index++].toInt()
        return index;
    }

}