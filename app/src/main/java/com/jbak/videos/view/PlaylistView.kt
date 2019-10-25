package com.jbak.videos.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.jbak.getItemIndex
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.R
import com.jbak.videos.playback.IPlayback
import com.jbak.videos.providers.Factory
import com.jbak.videos.providers.Factory.VideoProvider
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SerialList
import com.jbak.videos.types.VideoItem
import com.jbak.videos.types.VideosList
import com.jbak.videos.view.ItemDesign.*
import com.jbak.videos.view.ItemListView.Type.RELATED
import com.jbak.videos.view.ItemListView.Type.TEXT
import kotlinx.android.synthetic.main.playlist_view.view.*
import tenet.lib.base.Err
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Utils

enum class PlaylistType(val strRes: Int) : IItem{
    PLAYLIST(R.string.playlist),
    RELATED(R.string.related),
    MOVEMENT(R.string.movement);

    override fun getImageUrl(): String? {
        return null
    }

    override fun getName(): CharSequence {
        return App.str(strRes)
    }

    override fun getId(): String {
        return strRes.toString()
    }

    override fun getShortDescText(): String? {
        return null
    }
}

enum class Movement(val strRes: Int) : IItem{
    TO_START(R.string.seek_video_start),
    MINUS_1_MINUTE(R.string.seek_minus_1_minute),
    MINUS_10_SEC(R.string.seek_minus_10),
    MINUS_5_SEC(R.string.seek_minus_5),
    PLUS_5_SEC(R.string.seek_plus5),
    PLUS_10_SEC(R.string.seek_plus_10),
    PLUS_1_MINUTE(R.string.seek_plus_1_minute);
    override fun getImageUrl(): String? {
        return null
    }

    override fun getName(): CharSequence {
        return App.str(strRes)
    }

    override fun getId(): String {
        return strRes.toString()
    }

    override fun getShortDescText(): String? {
        return null
    }
    fun getInterval() : Long {
        return when(this){
            TO_START -> 0L
            MINUS_1_MINUTE -> -60*1000L
            MINUS_10_SEC -> -10*1000L
            MINUS_5_SEC -> -5*1000L
            PLUS_5_SEC -> 5*1000L
            PLUS_10_SEC -> 10*1000L
            PLUS_1_MINUTE -> 60*1000L
        }
    }

    fun move(iPlayback: IPlayback) {
        iPlayback.seekToMillis(getMoveTime(iPlayback.currentMillis().toLong()))
    }

    fun getMoveTime(curMillis: Long) : Long {
        if(this == TO_START)
            return 0L
        var time = curMillis
        time = time + getInterval()
        if(time < 0)
            time = 0L
        return time
    }

}

private val MOVE_DESIGN = ItemDesign().set(TEXT_SIZE, 20).set(TEXT_PADDING, Utils.dpToPx(8))
class PlaylistView(context: Context, attributeSet: AttributeSet?)
    : LinearLayout(context,attributeSet), ItemListView.OnItemClick{

    var curItem : IItem? = null
    val playlists = HashMap<PlaylistType,IItem.IItemList?>()
    var curType : PlaylistType = PlaylistType.PLAYLIST
    private var dataLoader: DataLoader? = null
    lateinit var videoPlayer : VideoPlayer



    constructor(context: Context) : this(context, null)
    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.playlist_view, this, true)
        mListView.onItemClick = this
        playlists[PlaylistType.MOVEMENT] = IItem.ItemList(Movement.values())
        mPlaylistTabs.setItems(IItem.ItemList(PlaylistType.values()))
        mPlaylistTabs.onItemClick = object : ItemListView.OnItemClick{
            override fun onItemClick(iItem: IItem, view: View) {
                setType(iItem as PlaylistType)
            }

        }
    }


    fun openItem(item:IItem, playlist: IItem.IItemList?){
        this.curItem =  item
        playlists.put(PlaylistType.PLAYLIST,playlist)
        playlists.put(PlaylistType.RELATED, null)
        setType(curType)
        mSerialView.openItem(item)
    }


    fun setType(playlistType: PlaylistType) {
        this.curType = playlistType
        val playlist = playlists[playlistType]
        mPlaylistTabs.setCurrent(playlistType.id, true)
        val itemType : ItemListView.Type = when(playlistType){
            PlaylistType.MOVEMENT ->  TEXT
            else -> RELATED
        }
        val itemDesign : ItemDesign? = when(playlistType){
            PlaylistType.MOVEMENT ->  MOVE_DESIGN
            else -> null
        }
        mListView.setDesign(itemDesign)
        mListView.setType(itemType)
        if(playlist == null){
            mListView.clear()
            if(curType == PlaylistType.RELATED) {
                startRelatedLoader()
            }
        } else {
            var curId:String? = null
            if(curType == PlaylistType.PLAYLIST && curItem != null)
                curId =  curItem!!.id;

            mListView.setItems(playlist,curId,true)
            mListView.setPageLoader(playlist as? DataLoader)

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
            setType(curType)
        }
    }

    fun getPlaylistItems(): IItem.IItemList? {
        return mListView.itemAdapter.items
    }

    fun setOnSeriesClick(onItemClick: ItemListView.OnItemClick) {
        mSerialView.setOnSeriesClick(onItemClick)
    }


    override fun onItemClick(iItem: IItem, view: View) {
        if(iItem is VideoItem) {
            val playlist = playlists[curType]
            videoPlayer.playItem(iItem, playlist)
        } else if(iItem is Movement) {
            iItem.move(videoPlayer.iPlayback)
        }
    }

}