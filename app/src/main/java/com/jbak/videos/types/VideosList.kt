package com.jbak.videos.types

import android.text.TextUtils
import com.jbak.videos.providers.HDRezkaItem
import tenet.lib.base.utils.Utils


open class VideosList() : IItem.ItemList() {

    companion object {
        val FIRST_PAGE = "FIRSTPAGETOKEN";
    }

    init {
        reset()
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

open class Season(name:String, id:String) : VideosList(),IItem {
    var name : String = ""
    var mId = IItem.ID
    init {
        this.mId = id
        this.name = name
    }

    override fun getImageUrl(): String {
        return ""
    }

    override fun getName(): CharSequence {
        return name
    }

    override fun getId(): String {
        return mId
    }

    override fun getShortDescText(): String? {
        return ""
    }

}
open class SerialList() : VideosList() {
    lateinit var parentItem : IItem
    val firstSeries : IItem?
        get() {
            val seas = firstOrNull() as? Season
            if(seas != null) {
                return seas.firstOrNull()
            }
            return null
        }

    fun isFirstSeries(id: String): Boolean {
        val fs = firstSeries
        if(fs != null) {
            return fs.id.equals(id)
        }
        return false
    }

    fun getNextPreviousSeries(iItem: IItem, next: Boolean) : IItem? {
        var item = iItem
        var season:Season? = null
        if(item.equals(parentItem) && size > 0){
            season = get(0) as? Season
            if(season != null) {
                item = season[0]
            }
        }
        if(season == null)
            season = getSeason(item)
        if (season == null) {
            return null
        }
        var nextItem:IItem? = Utils.getNextPreviousItem(next,item.id,season,false)
        if(nextItem == null) {
            season = getNextSeason(next, season)
            if(season != null) {
                nextItem = if(next) season.firstOrNull() else season.lastOrNull()
            }
        }
        return nextItem
    }

    fun hasItem(item:IItem) : Boolean {
        return getItemById(item.id) != null
    }

    fun getNextSeason(next: Boolean, season: Season) : Season? {
        return Utils.getNextPreviousItem(next,season.id,this,false) as? Season
    }
    fun getItemById(id: String) : IItem? {
        if(parentItem.id.equals(id))
            return parentItem
        for (s in this){
            val season = s as? Season
            if(season != null){
                val serie = Utils.itemById(id, season)
                if(serie != null)
                    return serie;
            }
        }
        return null
    }

    fun getSeason(item:IItem) : Season? {
        for (s in this){
            val season = s as? Season
            if(season != null){
                val serie = Utils.itemById(item.id, season)
                if(serie != null)
                    return season;
            }
        }
        return null
    }

}