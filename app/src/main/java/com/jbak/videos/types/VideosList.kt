package com.jbak.videos.types

import android.text.TextUtils
import tenet.lib.base.utils.Utils


class VideosList : ArrayList<IItem>, IItem.IItemList {

    constructor() : super(){
        reset()
    }

    companion object {
        val FIRST_PAGE = "FIRSTPAGETOKEN";
    }

    var nextPageToken:String? = "FIRSTPAGETOKEN"
    lateinit var query:String

    override fun getCount(): Int {
        return size
    }

    override fun getItem(pos: Int): IItem {
        return get(pos)
    }

    fun setQuery(query: String): VideosList {
        this.query = query;
        return this;
    }

    fun isFirstPage():Boolean{
        return  nextPageToken == FIRST_PAGE
    }

    fun hasNextPage() : Boolean{
        return nextPageToken != null
    }

    fun reset() : VideosList {
        query = ""
        nextPageToken = FIRST_PAGE
        clear()
        return this
    }

    fun addCheckId(iItem: IItem) : Boolean{
        if(TextUtils.isEmpty(iItem.id ))
            return false
        if(Utils.indexById(iItem.id,this) > -1)
            return false
        add(iItem)
        return true
    }

}