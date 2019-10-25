package com.jbak.videos

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.jbak.lib.data.DbUtils
import com.jbak.lib.data.DbUtils.StrConst.*
import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideoItem
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Listeners
import tenet.lib.base.utils.Utils

const val DB_VERSION = 4
const val DB_FILENAME = "videos"

const val TBL_SEARCH = "tblSearch"
const val TBL_POS = "tblPos"
const val TBL_SERIAL_POS = "tblSerialPos"
const val TBL_HISTORY = "tblHistory"

const val PROVIDER = "provider"
const val QUERY = "query"
const val DATE = "date"
const val POS = "pos"
private const val VALUE = "value"
private const val SERIAL_ID = "serialId"
private const val VIDEO_ID = "videoId"

interface DbListener{
    fun onDbEvent(event: Int, id: String = "")
}

class Db : SQLiteOpenHelper (App.get(), DB_FILENAME,null, DB_VERSION), DbUtils.StrConst{
    val listeners = Listeners<DbListener>()
    companion object {
        val DB_DELETE_HISTORY = 1

        private var INST = Db()

        fun get() : Db {
            return INST
        }
        fun getItemFromCursor(cursor: Cursor): VideoItem{
            val data = cursor.getString(cursor.getColumnIndex(VALUE))
            val split = data.split(VideoItem.DELIM)
            val typeId = cursor.getString(cursor.getColumnIndex(PROVIDER))
            val type = Utils.itemById(typeId, Factory.Type.values())
            val item = Factory.getProvider(type).createItem()
            item.setStringData(data)
            return item
        }

    }

    class SearchList : IItem.IItemList{
        private var cursor: Cursor? = null
        fun setCursor(cursor: Cursor?) : SearchList{
            this.cursor = cursor
            return this
        }

        override fun getItem(pos: Int): IItem {
            val item = VideoItem()
            cursor?.let {
                it.moveToPosition(pos)
                val id = it.getLong(it.getColumnIndex(_ID)).toString()
                val name = it.getString(it.getColumnIndex(QUERY)).toString()
                item.setIdAndName(id,name)
            }
            return item
        }

        override fun getCount(): Int {
            if(cursor == null)
                return 0
            else
                return cursor!!.count
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        DbUtils.CreateTable(TBL_SEARCH,true)
            .addIdRow()
            .addRowCollateNoCase(QUERY)
            .addRow(VALUE, TEXT)
            .addRow(DATE, INTEGER)
            .create(db)

        DbUtils.CreateTable(TBL_POS, true)
            .addIdRow()
            .addRow(VIDEO_ID, TEXT)
            .addRow(POS, INTEGER)
            .create(db)

        DbUtils.CreateTable(TBL_SERIAL_POS, true)
            .addIdRow()
            .addRow(SERIAL_ID, TEXT)
            .addRow(VIDEO_ID, TEXT)
            .create(db)

        DbUtils.CreateTable(TBL_HISTORY, true)
            .addIdRow()
            .addRow(PROVIDER, TEXT)
            .addRow(VIDEO_ID, TEXT)
            .addRow(DATE, INTEGER)
            .addRow(VALUE, TEXT)
            .create(db)

    }



    fun getAllSearches() : Cursor? {
        return DbUtils.Select(TBL_SEARCH)
            .orderBy(DATE,false).selectOpt(readableDatabase)
    }

    fun getVideoPos(id: String) : Int{
        val cursor = DbUtils.Select(TBL_POS)
            .where().eq(VIDEO_ID, id).selectOrNull(readableDatabase);
        val posStr: String? = DbUtils.getStringFromCursorAndClose(cursor, POS)
        posStr?.let {
            return it.toInt()
        }
        return 0
    }

    fun hasInHistory(id:String): Boolean {
        return DbUtils.Select(TBL_HISTORY)
            .where().eq(VIDEO_ID, id)
            .hasOne(readableDatabase)
    }

    private fun notifyListeners(event: Int, id: String = ""){
        for (l in listeners.list)
            l.onDbEvent(event, id)
    }

    fun deleteFromHistory(id:String) {
        val del = DbUtils.Select(TBL_HISTORY)
            .where().eq(VIDEO_ID, id)
            .delete(writableDatabase)
        if(del > 0)
            notifyListeners(DB_DELETE_HISTORY, id)
    }

    fun addToHistory(videoItem: VideoItem){
        val value = videoItem.getStringData()
        val cv = DbUtils.makeContentValues(
            VIDEO_ID,videoItem.id,
            PROVIDER, videoItem.providerType.id,
            DATE, System.currentTimeMillis(),
            VALUE, value);
        DbUtils.Select(TBL_HISTORY)
            .where().eq(VIDEO_ID, videoItem.id)
            .limit(100)
            .insertOrUpdate(writableDatabase,cv)
    }

    fun getHistoryCursor(): Cursor {
        return DbUtils.Select(TBL_HISTORY)
            .orderBy(DATE,false)
            .select(readableDatabase)
    }

    fun setVideoPos(id: String, pos: Int){
        val cv = DbUtils.makeContentValues(POS,pos, VIDEO_ID, id);
        val upd = DbUtils.Select(TBL_POS)
            .where().eq(VIDEO_ID, id)
            .update(writableDatabase,cv)
        if(upd < 1)
            writableDatabase.insert(TBL_POS, QUERY,cv)

    }
    fun getSerialSeriesId(parentId: String) : String?{
        return DbUtils.getStringFromCursorAndClose(
            DbUtils.Select(TBL_SERIAL_POS)
                .where().eq(SERIAL_ID, parentId).selectOrNull(readableDatabase),
            VIDEO_ID
        )
    }

    fun setSerialSeriesId(parentId: String, seriesId: String){
        val cv = DbUtils.makeContentValues(SERIAL_ID,parentId, VIDEO_ID, seriesId);
        val upd = DbUtils.Select(TBL_SERIAL_POS)
            .where().eq(SERIAL_ID, parentId)
            .update(writableDatabase,cv)
        if(upd < 1)
            writableDatabase.insert(TBL_SERIAL_POS, QUERY,cv)

    }

    fun getSearches(query: String) : Cursor? {
        if(TextUtils.isEmpty(query))
            return getAllSearches()
        return DbUtils.Select(TBL_SEARCH)
            .where().glob(QUERY,"*"+DbUtils.getGlobNoCase(query)+"*")
            .orderBy(QUERY,true).selectOpt(readableDatabase)
    }

    fun saveSearch(query : String)  {
        val cv = DbUtils.makeContentValues(QUERY,query, DATE, System.currentTimeMillis());
        val upd = DbUtils.Select(TBL_SEARCH)
            .where().eq(QUERY,query)
            .update(writableDatabase,cv)
        if(upd < 1)
            writableDatabase.insert(TBL_SEARCH, QUERY,cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    fun deleteSearch(name: String) {
        val del = DbUtils.select(TBL_SEARCH).where().eq(QUERY,name).delete(writableDatabase)
        MyLog.log("Delete items: $del search : $name")
    }

    fun clearTable(table:String){
        DbUtils.Select(table).delete(writableDatabase)
    }

}