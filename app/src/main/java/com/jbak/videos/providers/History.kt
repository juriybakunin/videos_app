package com.jbak.videos.providers

import com.jbak.videos.DataLoader
import com.jbak.videos.Db
import com.jbak.videos.SerialLoader
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideoItem
import com.jbak.videos.types.VideosList
import tenet.lib.base.Err

class HistoryItem : VideoItem() {

}

class History : Factory.BaseVideoProvider(){
    override fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                val cursor = Db.get().getHistoryCursor();
                if(cursor.moveToFirst()) {
                    do {
                        val item = Db.getItemFromCursor(cursor)
                        videosList.add(item)
                    } while (cursor.moveToNext())
                }
                videosList.nextPageToken = null
                return Err.OK
            }
        }
    }

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader? {
        return null
    }

    override fun createSerialLoader(parentItem: IItem, onItemsLoaded: DataLoader.OnItemsLoaded): SerialLoader? {
        return null
    }

    override fun getType(): Factory.Type {
        return Factory.Type.HISTORY
    }

    override fun getItemClass(): Class<out VideoItem> {
        return HistoryItem::class.java
    }

}