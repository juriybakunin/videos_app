package com.jbak.videos.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.R
import com.jbak.videos.providers.Factory
import com.jbak.videos.providers.Factory.VideoProvider
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SerialList
import com.jbak.videos.types.VideosList
import kotlinx.android.synthetic.main.playlist_view.view.*
import tenet.lib.base.Err
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Utils

enum class PlaylistType {
    PLAYLIST, RELATED
}
class PlaylistView(context: Context, attributeSet: AttributeSet?)
    : LinearLayout(context,attributeSet){

    var curItem : IItem? = null
    val playlists = HashMap<PlaylistType,IItem.IItemList?>()
    var curType : PlaylistType = PlaylistType.PLAYLIST
    private var dataLoader: DataLoader? = null



    constructor(context: Context) : this(context, null)
    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.playlist_view, this, true)
        mListView.setType(ItemListView.Type.RELATED)
        mButtonPlaylist.setOnClickListener {
            setType(PlaylistType.PLAYLIST)
        }
        mButtonRelativeVideos.setOnClickListener {
            setType(PlaylistType.RELATED)
        }

    }

    fun openItem(item:IItem, playlist: IItem.IItemList?){
        this.curItem =  item
        playlists.put(PlaylistType.PLAYLIST,playlist)
        playlists.put(PlaylistType.RELATED, null)
        setType(curType)
        mSerialView.openItem(item)
    }

    fun setButtonSelected(button: TextView, selected: Boolean){
        button.textSize = if(selected) 14f else 12f
        val color = if(selected) App.res().getColor(R.color.colorPrimary) else Color.WHITE
        button.setTextColor(color)
    }
    fun selectButton(button:View){
        for( i in 0..mMenu.childCount){
            val v = mMenu.getChildAt(i)
            if(v is TextView) {
                setButtonSelected(v,v == button)
            }
        }
    }

    fun setType(playlistType: PlaylistType) {
        this.curType = playlistType
        val playlist = playlists[playlistType]
        mListView.setLayout()
        when(playlistType) {
            PlaylistType.RELATED -> selectButton(mButtonRelativeVideos)
            PlaylistType.PLAYLIST -> selectButton(mButtonPlaylist)
        }
        if(playlist == null){
            if(curType == PlaylistType.RELATED) {
                startRelatedLoader()
            }
        } else {
            var curId:String? = null
            if(curType == PlaylistType.PLAYLIST && curItem != null)
                curId =  curItem!!.id;

            mListView.itemAdapter.currentItemId = curId
            mListView.setItems(playlist)
            if(curId != null) {
                val index = Utils.indexById(curId, IItem.ItemIterator(playlist))
                if(index > -1) {
                    mListView.scrollToPosition(index)
                }
            }
        }
    }

    fun startRelatedLoader(){
        if (curItem == null)
            return
        dataLoader?.cancelJob()
        dataLoader = Factory.getItemProvider(curItem!!).createRelatedLoader(object : DataLoader.OnItemsLoaded {
            override fun onItemsLoaded(err: Err, videosList: VideosList) {
                onRelatedItemsLoaded(err, videosList)
            }

        })
        dataLoader!!.loadSearch(curItem!!.id)

    }

    fun onRelatedItemsLoaded(err: Err, videosList: VideosList?) {
        playlists[PlaylistType.RELATED] = videosList
        if(videosList != null && curType == PlaylistType.RELATED) {
            mListView.setItems(videosList)
        }
    }

    fun getPlaylistItems(): IItem.IItemList? {
        return mListView.itemAdapter.items
    }

    fun setOnSeriesClick(onItemClick: ItemListView.OnItemClick) {
        mSerialView.setOnSeriesClick(onItemClick)
    }


}