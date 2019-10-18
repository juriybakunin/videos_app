package com.jbak.videos.view


import androidx.appcompat.app.AlertDialog
import com.jbak.videos.R
import com.jbak.videos.UrlCache
import com.jbak.videos.activity.MainActivity

class TechMenu {
    private var mActivity: MainActivity? = null
    private val items = arrayOf<CharSequence>("Clear videos cache")
    private fun onMenuClick(which: Int) {
        when (which) {
            0 -> UrlCache.get().clear()
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
