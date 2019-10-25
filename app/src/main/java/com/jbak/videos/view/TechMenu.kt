package com.jbak.videos.view


import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.jbak.videos.activity.MainActivity
import com.jbak.videos.activity.WebViewActivity
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideoItem
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.jbak.videos.*
import tenet.lib.base.MyLog


class TechMenu {
    private var mActivity: MainActivity? = null
    private val items = arrayOf<CharSequence>(
        "Clear videos cache",
        "Clear history"
    )
    private fun onMenuClick(which: Int) {
        when (which) {
            0 -> UrlCache.get().clear()
            1 -> Db.get().clearTable(TBL_HISTORY)
        }
    }

    fun showMain(activity: MainActivity) {
        mActivity = activity
        AlertDialog.Builder(activity)
            .setTitle(R.string.tech_menu)
            .setIcon(R.drawable.ic_tech_menu)
            .setItems(items) { dialog, which -> this.onMenuClick(which) }
            .show()

    }

}

class MenuItem(val strId: Int) : IItem{
    override fun getImageUrl(): String? {
        return null
    }

    override fun getName(): CharSequence {
        return App.str(strId)
    }

    override fun getId(): String {
        return strId.toString()
    }

    override fun getShortDescText(): String? {
        return ""
    }

}

private fun stringItems(items: List<MenuItem>) : Array<CharSequence> {
    val ar = Array<CharSequence>(items.size){""}
    for (i in items.indices){
        ar[i] = items[i].name
    }
    return ar
}

class VideoMenu(val context: Context, val videoItem: VideoItem, val items: ArrayList<MenuItem> = ArrayList()) {

    private var onCancel: DialogInterface.OnCancelListener? = null
    var videoPlayer: VideoPlayer? = null
    fun showMenu(){

        val url = videoItem.getItemUrl()
        videoPlayer?.let {
            items.add(MenuItem(R.string.seek_video_start))
        }
        url?.let {
            items.add(MenuItem(R.string.share))
            items.add(MenuItem(R.string.open_browser))
            items.add(MenuItem(R.string.open_page))
        }
        if(Db.get().hasInHistory(videoItem.id)){
            items.add(MenuItem(R.string.delete_history_item))
        }
        videoPlayer?.let {
            items.add(MenuItem(R.string.video_info))
        }
        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.video_menu)
            .setItems(stringItems(items)){
                d,pos->
                if(pos >= 0)
                    onMenuClick(items[pos], d)

            }
        onCancel?.let { builder.setOnCancelListener(it) }
        builder.show()
    }

    fun onMenuClick(item: MenuItem, dialogInterface: DialogInterface){
        when(item.strId) {
            R.string.video_info -> showInfo()
            R.string.share -> shareVideo()
            R.string.open_browser->openBrowser()
            R.string.open_page -> openPage()
            R.string.delete_history_item->deleteHistory()
            R.string.seek_video_start->videoPlayer?.iPlayback?.seekToMillis(0L)
        }
        onCancel?.onCancel(dialogInterface)
    }

    private fun deleteHistory() {
        Db.get().deleteFromHistory(videoItem.id)
    }

    private fun openBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoItem.getItemUrl()))
        try {
            context.startActivity(intent)

        } catch (e: Throwable){
            MyLog.err(e)
        }
    }

    private fun openPage() {
        WebViewActivity.start(context, videoItem.getItemUrl()!!)
    }

    private fun shareVideo() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        if(Build.VERSION.SDK_INT >= 24) {
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        } else {
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }
        share.putExtra(Intent.EXTRA_SUBJECT, videoItem.name)
        share.putExtra(Intent.EXTRA_TEXT, videoItem.getItemUrl())
        context.startActivity(Intent.createChooser(share, "Share link!"))
    }

    fun showInfo(){
        if(videoPlayer == null)
            return
        var info = ""
        videoPlayer?.run{
            info = """
             Size: ${iPlayback.getVideoSize(true)}x${iPlayback.getVideoSize(false)}
            Id: ${mCurItem?.id}
            Name: ${mCurItem?.name}
            Url: ${mMedia}
        """.trimIndent()
        }

        val builder = AlertDialog.Builder(videoPlayer!!.mInitContext)
            .setTitle(R.string.player_info)
            .setMessage(info)
        onCancel?.let { builder.setOnCancelListener(it) }
        builder.show()

    }

    fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener): VideoMenu {
        this.onCancel = onCancelListener
        return this
    }
}
