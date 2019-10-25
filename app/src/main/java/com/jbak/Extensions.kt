package com.jbak

import android.net.Uri
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import at.huber.youtubeExtractor.YtFile
import com.jbak.videos.types.IItem
import tenet.lib.base.utils.TimeUtils
import tenet.lib.base.utils.Utils

fun Uri.isExtension(ext : String) : Boolean{
    return this.lastPathSegment?.endsWith(ext, true) == true
}

fun Uri.isFilename(last : String) : Boolean{
    if(this.lastPathSegment != null){
        return this.lastPathSegment.equals(last, true)
    }
    return false
}

fun Uri.removeParam(paramName: String): Uri{
    val builder = buildUpon().clearQuery()
    for (key in queryParameterNames) {
        val v = getQueryParameter(key)
        if(key == null|| v ==null || paramName.equals(key))
            continue
        builder.appendQueryParameter(key, v)
    }
    return builder.build()
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

fun Window.setTrueFullscreen(set: Boolean, fsHideNavigation: Boolean = true, fsCutout: Boolean = false) {
    var systemVis = 0
    var fsFlags = FLAG_FULLSCREEN
    if (set) {
        systemVis = (systemVis or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        if(fsHideNavigation) {
            systemVis = (systemVis or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }
    if(set) {
        addFlags(fsFlags)
    } else {
        clearFlags(fsFlags)
    }
    if(Build.VERSION.SDK_INT >= 28) {
        val useCutout = set && fsCutout
        val attr = if(useCutout) LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES  else LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        if(attributes.layoutInDisplayCutoutMode != attr) {
            if(useCutout) {
                addFlags(FLAG_TRANSLUCENT_STATUS)
            } else {
                clearFlags(FLAG_TRANSLUCENT_STATUS)
            }
            attributes = attributes

        }
    }

    decorView.systemUiVisibility = systemVis
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

fun IItem.IItemList.getItemIndex(id: String) : Int {
    for (i in 0 until count) {
        if(getItem(i).id == id)
            return i
    }
    return -1
}
fun IItem.IItemList.getNextPreviousItem(next: Boolean, id: String, circleMove: Boolean) : IItem? {
    var index = getItemIndex(id)
    if(index < 0)
        return null
    index = Utils.getNextPreviousIndex(next,index,count)
    return if(index >= 0) getItem(index) else null
}

fun IItem.isId(iItem: IItem?): Boolean {
    return Utils.isId(iItem, id)
}