package com.jbak.videos.model

import java.util.ArrayList

class GoogleCompletions : ArrayList<Any>(){
    fun getStrings() : List<String>{
        val list = ArrayList<String>()
        if(size < 2)
            return list
        val o = get(1)
        if(!(o is List<*>))
            return list
        for (a in o) {
            list.add(a.toString())
        }
        return list
    }
}
