package com.jbak

import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.view.View.INVISIBLE
import android.view.Window
import android.view.WindowManager
import at.huber.youtubeExtractor.YtFile
import tenet.lib.base.utils.TimeUtils

fun Uri.isExtension(ext : String) : Boolean{
    return this.lastPathSegment?.endsWith(ext, true) == true
}

fun Uri.isFilename(last : String) : Boolean{
    if(this.lastPathSegment != null){
        return this.lastPathSegment.equals(last, true)
    }
    return false
}

fun Uri.isExtensions(vararg extensions : String) : Boolean{
    for (ext in extensions) {
        if(isExtension(ext))
            return true
    }
    return false
}

fun Uri.hasInPath(segment : String) : Boolean{
    for (ext in pathSegments) {
        if(segment.equals(ext,true))
            return true
    }
    return false
}

fun Int.formatDuration() : String{
    return if(this>0) TimeUtils.getTimeRangeText(this, true, null).toString() else ""
}

fun Window.setTrueFullscreen(set: Boolean) {
    var flags = 0
    if (set) {
        flags = (flags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    decorView.systemUiVisibility = flags
    if(set) {
        addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    } else {
        clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}

fun View.setVisInvis(visible: Boolean) {
    visibility = if(visible) View.VISIBLE else View.INVISIBLE
}

fun View.setVisGone(visible: Boolean) {
    visibility = if(visible) View.VISIBLE else View.GONE
}

fun YtFile.getDescription(): String {
    return "${format.height}p, itag=${format.itag} video=${format.hasVideo()} audio=${format.hasAudio()}"
}
