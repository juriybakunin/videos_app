package com.jbak.videos.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.jbak.setVisGone
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.R
import com.jbak.videos.SerialCacher
import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
import com.jbak.videos.types.Season
import com.jbak.videos.types.SerialList
import com.jbak.videos.types.VideosList
import com.jbak.videos.view.ItemListView.OnItemClick
import kotlinx.android.synthetic.main.item_view_text.view.*
import kotlinx.android.synthetic.main.serial_view.view.*
import tenet.lib.base.Err
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Listeners
import tenet.lib.base.utils.Utils
import tenet.lib.base.utils.ViewUtils

class SerialView(context: Context, attributeSet: AttributeSet):
    LinearLayout(context,attributeSet) {
    private lateinit var curItem: IItem
    var serial : SerialList? = null
    val serialListeners = Listeners<OnSerialLoaded>()

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.serial_view,this,true)

        mListSeasons.onItemClick = object : OnItemClick{
            override fun onItemClick(iItem: IItem, view: View) {
                onSeasonSelected(iItem, view)
            }

        }
        mCurSeason.setOnClickListener {
            if(mCurSeason.videoItem != null) {
                onCurSeasonClick(mCurSeason.videoItem!!,it)
            }
        }
        setSerialList(null)
    }

    private fun onCurSeasonClick(item: IItem, it: View) {
        mListSeasons.setVisGone(true)
    }

    fun setSerialList(serialList: SerialList?){
        serial = serialList
        if(serial != null){
            SerialCacher.INST.putSerial(serial!!)
        }
        for (l in serialListeners.list){
            l.onSerialLoaded(serial)
        }
        if(serialList != null && !serialList.isEmpty()) {
            val seas = serialList.getSeason(curItem)
            val curSeason = if(seas != null) seas else serialList[0] as Season
            val ser = Utils.itemById(curItem.id,curSeason)
            val curSeries =  if(ser != null) ser else curSeason[0]


            setCurSeason(curSeason)
            mListSeasons.setItems(serialList,curSeason.id,true)
            mListSeries.setItems(curSeason,curSeries.id, true)
            mCurSeason.setVisGone(serialList.size > 1)
            setVisGone(true)
        } else {
            mListSeasons.clear()
            mListSeries.clear()
            setVisGone(false)

        }
    }

    fun setCurSeason(item: IItem){
        mCurSeason.setItem(item)
        mCurSeason.text?.setTextColor(App.res().getColor(R.color.orange))
    }

    fun onSeasonSelected(iItem: IItem, view: View) {
        mListSeasons.itemAdapter.currentItemId = iItem.id
        ViewUtils.updateVisibleItems(mListSeasons)
        mListSeasons.setVisGone(false)
        mCurSeason.setItem(iItem)
        val season = iItem as Season
        mListSeries.setItems(season)
        if(season.size > 0)
            mListSeries.scrollToPosition(0)
    }

    fun openItem(item:IItem){
        this.curItem =  item
        startSerialLoader()
    }

    fun startSerialLoader(){
        val provider = Factory.getItemProvider(curItem)
        serial?.let {
            if(it.hasItem(curItem)) {
                MyLog.log("Same serial ")
                setSerialList(it)
                return
            }
        }
        val cached = SerialCacher.INST.getSerial(curItem)
        cached?.let {
            setSerialList(it)
            return
        }
        val loader = provider.createSerialLoader(curItem, object : DataLoader.OnItemsLoaded{
            override fun onItemsLoaded(err: Err, videosList: VideosList) {
                MyLog.log("Serial loaded")
                setSerialList(videosList as SerialList)
            }
        })
        if(loader != null) {
            loader.loadSearch(curItem.id)
        } else {
            MyLog.log("Not a serial ")
            setSerialList(null)
        }
    }

    fun setOnSeriesClick(onItemClick: OnItemClick){
        mListSeries.onItemClick = onItemClick
    }
}

interface OnSerialLoaded {
    fun onSerialLoaded(list : SerialList?)
}