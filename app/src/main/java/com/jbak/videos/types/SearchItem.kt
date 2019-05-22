package com.jbak.videos.types

enum class TypeSearchItem{
    WEB, DB
}
class SearchItem (query: String,val type : TypeSearchItem = TypeSearchItem.WEB): SimpleItem(){
    init {
        setIdAndName(query,query)
    }

    fun canDelete() : Boolean {
        return type == TypeSearchItem.DB
    }
}