package com.jbak.videos

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.jbak.lib.data.DbUtils
import com.jbak.lib.data.DbUtils.StrConst.*
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SimpleItem
import tenet.lib.base.MyLog

private const val DB_VERSION = 1
private const val DB_FILENAME = "videos"
private const val TBL_SEARCH = "tblSearch"
const val QUERY = "query"
const val DATE = "date"
private const val VALUE = "value"

class Db : SQLiteOpenHelper (App.get(), DB_FILENAME,null, DB_VERSION), DbUtils.StrConst{
    companion object {
        private var INST = Db()

        fun get() : Db {
            return INST
        }


    }

    class SearchList : IItem.IItemList{
        private var cursor: Cursor? = null
        fun setCursor(cursor: Cursor?) : SearchList{
            this.cursor = cursor
            return this
        }

        override fun getItem(pos: Int): IItem {
            val item = SimpleItem()
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
    }

    fun getAllSearches() : Cursor? {
        return DbUtils.Select(TBL_SEARCH)
            .orderBy(DATE,false).selectOpt(readableDatabase)
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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun deleteSearch(name: String) {
        val del = DbUtils.select(TBL_SEARCH).where().eq(QUERY,name).delete(writableDatabase)
        MyLog.log("Delete items: $del search : $name")
    }


}