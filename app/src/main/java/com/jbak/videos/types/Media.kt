package com.jbak.videos.types

import android.net.Uri

open class Media(val videoUri: Uri, val audioUri: Uri? = null){
    constructor(url: String) : this(Uri.parse(url))
    constructor(video: String, audio: String) : this(Uri.parse(video), Uri.parse(audio))
    val separateAudio: Boolean
        get() = audioUri != null

    override fun toString(): String {
        var str = "[sepAudio: $separateAudio video: $videoUri"
        if(audioUri != null) {
            str = "$str audio: $audioUri"
        }
        return "$str]"
    }

}