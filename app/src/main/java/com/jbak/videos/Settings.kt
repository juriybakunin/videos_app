package com.jbak.videos

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.jbak.videos.activity.MainActivity
import com.jbak.videos.providers.Factory
import com.jbak.videos.providers.YouTube
import com.jbak.videos.types.IItem
import com.jbak.videos.view.ItemListView
import com.jbak.videos.view.PlayerType
import kotlinx.android.synthetic.main.activity_main.*
import tenet.lib.base.utils.Utils
import kotlin.collections.ArrayList


private val LIST_YES_NO = mutableListOf(App.str(R.string.yes), App.str(R.string.no))

abstract class ItemSetting : IItem{

    override fun getId(): String {
        return javaClass.simpleName
    }
    override fun getImageUrl(): String? {
        return null
    }

    abstract fun getMenuItems() : List<String>
    abstract fun onMenuItemSelected(pos: Int, context: Context)
}

class SetYouTubeItem : ItemSetting() {
    val items = mutableListOf("Extractor", "Interceptor")
    override fun getMenuItems(): List<String> {
        return items
    }

    override fun onMenuItemSelected(pos: Int, context: Context) {
        App.prefs().setYoutubeInterceptor(pos == 1)
        if(context is MainActivity) {
            context.refresh()
        }
        UrlCache.get().reset()
    }

    override fun getName(): CharSequence {
        return "You tube item"
    }

    override fun getShortDescText(): String? {
        return if(YouTube.USE_INTERCEPTOR) items[1] else items[0]
    }



}

class SetPlayerType : ItemSetting() {
    val items = mutableListOf("Media", "Exo")
    override fun getMenuItems(): List<String> {
        return items
    }

    override fun onMenuItemSelected(pos: Int, context: Context) {
        val t = if(pos==0) PlayerType.MEDIA_PLAYER else PlayerType.EXO_PLAYER
        App.prefs().setPlayerType(t)
        if(context is MainActivity) {
            context.mVideoPlayer.setPlayerType(t)
        }
    }

    override fun getName(): CharSequence {
        return "Player type"
    }

    override fun getShortDescText(): String? {
        return if(App.prefs().getPlayerType() == PlayerType.MEDIA_PLAYER) items[0] else items[1]
    }

}
class SetQuality : ItemSetting() {
    val menu : List<String>
    init {
        menu = ArrayList<String>()
        for(q in Factory.Quality.values()){
            menu.add(q.getName())
        }
    }
    override fun getMenuItems(): List<String> {
        return menu
    }

    override fun onMenuItemSelected(pos: Int, context: Context) {
        val q = Factory.Quality.values()[pos]
        if(q != App.prefs().getQuality()) {
            UrlCache.get().reset()
            App.prefs().setQuality(q)
        }
    }

    override fun getName(): CharSequence {
        return App.str(R.string.set_quality)
    }

    override fun getShortDescText(): String? {
        return App.prefs().getQuality().getName()
    }
}

class SetPlayInBackground : ItemSetting(){
    override fun onMenuItemSelected(pos: Int, context: Context) {
        App.prefs().setPlayInBackground(pos == 0)
    }

    override fun getName(): CharSequence {
        return App.str(R.string.set_play_background)
    }

    override fun getShortDescText(): String? {
        when(App.prefs().getPlayInBackground()) {
            true -> return LIST_YES_NO[0]
            false -> return LIST_YES_NO[1]
        }
    }

    override fun getMenuItems(): List<String> {
        return LIST_YES_NO
    }

}

class SettingDlg(
    var title:String = App.str(R.string.settings),
    val settings:IItem.ItemList = IItem.ItemList()
        ) : ItemListView.OnItemClick {
    lateinit var list : ItemListView
    override fun onItemClick(iItem: IItem, view: View) {
        val set = iItem as? ItemSetting
        if(set != null) {
            val items = set.getMenuItems();
            AlertDialog.Builder(list.context)
                .setItems(items.toTypedArray()) { dlg,pos ->
                    if(pos >= 0) {
                        set.onMenuItemSelected(pos,list.context)
                        list.itemAdapter.notifyDataSetChanged()
                    }
                }
                .setNegativeButton(android.R.string.cancel){d,p->}
                .setTitle(set.name)
                .show()

        }
    }

    fun setGeneral() : SettingDlg{
        settings.add(SetPlayInBackground())
        settings.add(SetPlayerType())
        settings.add(SetYouTubeItem())
        settings.add(SetQuality())
        return this
    }

    fun show(context: Context){
        list = ItemListView(context,null, ItemListView.Type.SETTING)
        val pad = Utils.dpToPx(8)
        list.setPadding(pad,pad,pad,pad)
        list.onItemClick = this
        list.setItems(settings)
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(list)
            .show()
    }
}