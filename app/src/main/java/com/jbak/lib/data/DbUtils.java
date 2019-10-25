package com.jbak.lib.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import tenet.lib.base.MyLog;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DbUtils {
    public static final Select select(String tableName){
        return new Select(tableName);
    }
	public interface StrConst
	{
         String _RANDOM ="RANDOM()";
		 String TEXT ="TEXT";
		 String TINYINT ="tinyint";
		 String INSERT ="INSERT";
		 String INTO ="INTO";
		 String VALUES ="VALUES";
		 String INTEGER ="INTEGER";
		 String LENGTH ="length";
		 String BLOB ="BLOB";
		 String COLLATE ="COLLATE";
		 String NOCASE ="NOCASE";
		 String _ID = "_id";
		 String ASC = "asc";
		 String DESC = "desc";
         String _SPACE = " ";
		 String _COMMA = ",";
		 String AND = "and";
		 String OR = "or";
		 String IF_NOT_EXISTS= "IF NOT EXISTS";
		 String IF_EXISTS= "IF EXISTS";
		 String SQLITE_MASTER = "sqlite_master";
		 String SQL= "sql";
		 String NAME= "pkgName";
		 String TYPE= "type";
		 String TABLE= "table";
		 String PRIMARY_KEY = "PRIMARY KEY";
		 String _AUTOINCREMENT = PRIMARY_KEY+_SPACE+"AUTOINCREMENT NOT NULL";
	}
	/** Курсор, возвращающий значения в случайном порядке */
	public static class ShuffledCursor extends CursorWrapper
	{
		/** Перемешанные индексы */
		private ArrayList<Integer> mShuffle;
		/** Оригинальная позиция курсора  */
		int mOrigPos = -1;
		
		/** Конструктор 
		 * @param cursor Курсор для перемешивания
		 */
		public ShuffledCursor(Cursor cursor) {
			super(cursor);
			int sz = cursor.getCount();
			makeShuffle(sz);
		}
		/** Конструктор 
		 * @param cursor Курсор для перемешивания
		 * @param shuffle Перемешанные индексы 
		 */
		public ShuffledCursor(Cursor cursor,ArrayList<Integer>shuffle) {
			super(cursor);
			int sz = cursor.getCount();
			if(shuffle.size()==sz)
				mShuffle = shuffle;
			else
				makeShuffle(sz);
		}
		/** 
		 * Выполняет перемешивание элементов
		 * @param sz Размер курсора 
		 */
		void makeShuffle(int sz)
		{
			mShuffle= new ArrayList<Integer>(sz);
			for(int i=0;i<sz;i++)
				mShuffle.add(Integer.valueOf(i));
			Collections.shuffle(mShuffle,new Random());
		}
		@Override
		public boolean moveToFirst() {
			return moveToPosition(mShuffle.get(0));
		}
		@Override
		public boolean move(int offset) {
			return moveToPosition(mOrigPos+offset);
		}
		@Override
		public final boolean moveToNext() {
			if(mOrigPos+1>=mShuffle.size())
				return false;
			return moveToPosition(mOrigPos+1);
		}
		@Override
		public boolean moveToPosition(int position) {
			boolean ret = super.moveToPosition(mShuffle.get(position));
			if(ret)
				mOrigPos = position;
			return ret;
		}
		/** Возвращает текущий порядок перемешивания */
		public final ArrayList<Integer> getShuffle()
		{
			return mShuffle;
		}
	}
	public static String getGlobNoCase(String text){
		if (TextUtils.isEmpty(text))
			return text;
		StringBuilder sb = new StringBuilder();
		String lc = text.toLowerCase();
		int len = text.length();
		boolean brackets = false;
		for (int i=0;i<len;i++){
			char c = lc.charAt(i);
			if(c=='[')
				brackets = true;
			else if(c==']')
				brackets = false;
			char other = Character.isLetter(c)?Character.toUpperCase(c):c;
			if(other!=c) {
				if(!brackets)
					sb.append('[');
				sb.append(c).append(other);
				if(!brackets)
					sb.append(']');
			}
			else
				sb.append(c);
		}
		return sb.toString();
	}
	public static ContentValues makeContentValues(Object ... items)
	{
		ContentValues cv = new ContentValues();
		for(int i=0;i<items.length;i+=2)
		{
			String column = items[i].toString();
			Object o = items[i+1];
			if(o instanceof byte[])
				cv.put(column,(byte[])o);
			else if (o instanceof Integer)
				cv.put(column, (Integer)o);
			else if (o instanceof Long)
				cv.put(column, (Long) o);
			else
				cv.put(column, o.toString());
		}
		return cv;
	}
	public static class CreateTable implements StrConst
	{
		public static final String CREATE_TABLE = "CREATE TABLE ";
		String mResult;
		boolean firstField = true;
		public CreateTable(String tableName,boolean ifNotExists) {
			mResult = CREATE_TABLE;
			if(ifNotExists)
				mResult+=_SPACE+IF_NOT_EXISTS+_SPACE;
			mResult+=tableName+"(";
		}
		void nextParam()
		{
			if(!firstField)
				mResult+=_COMMA;
			mResult+='\n';
			firstField = false;
		}
		public CreateTable addRow(String colName, String colType) {
			nextParam();
			mResult+=colName+" "+colType;
			return this;
		}
		public CreateTable addIdRow() {
			addRow(_ID, INTEGER, _AUTOINCREMENT);
			return this;
		}
		public CreateTable addRow(String colName, String colType, String params) {
			nextParam();
			mResult += colName+_SPACE+colType+_SPACE+params;
			return this;
		}
		public CreateTable addRowCollateNoCase(String colName) {
			return addRow(colName, TEXT, COLLATE+_SPACE+NOCASE);
		}
		public void create(SQLiteDatabase db)
		{
			mResult+=")";
			db.execSQL(toString());
		}
		@Override
		public String toString() {
			return mResult;
		}
	}
/** Класс для считывания всех строк курсора с последующим закрытием */
	public static abstract class CursorReader
	{
		/** Абстрактная функция считывания строки
		 * @param row Позиция строки
		 * @param cursor Курсор
		 * @return Если функция возвращает false - считывание прекращается
		 */
		public abstract boolean readRow(int row, Cursor cursor);
		public boolean read(Cursor cursor)
		{
			if(cursor==null)
				return false;
			try {
				if(cursor.moveToFirst())
				{
					int row = 0;
					do{
						if(!readRow(row, cursor))
							break;
						++row;
					}
					while (cursor.moveToNext());
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			if(!cursor.isClosed())
				cursor.close();
			return true;
		}
	}
	/** Класс для выполнения запросов к БД */
	public static class Select implements StrConst
	{
		static final String ERR_NO_WHERE = "Do you forget call method where() ?";
		String mTable;
		String mWhere;
		String mOrderBy;
		String mLimit;
		boolean mDistinct = false;
		ArrayList<String> mSelects;
		String[] mColumns;

		/**  Конструктор
		 * @param table название таблицы
		 */
		public Select(String table)
		{
			mTable = table;
		}
		public Select()
		{
			mTable = "";
		}
		public Select(Select sel)
		{
			mTable = sel.mTable;
			mWhere = sel.mWhere;
			mOrderBy = sel.mOrderBy;
			mLimit = sel.mLimit;
			if(sel.mColumns!=null)
			{
				mColumns = new String[sel.mColumns.length];
				System.arraycopy(sel.mColumns, 0, mColumns, 0, sel.mColumns.length);
			}
			if(sel.mSelects!=null)
			{
				mSelects = new ArrayList<String>(sel.mSelects);
			}
		}
		/** Начинает Where */
		public Select where()
		{
			mWhere = "";
			mSelects=null;
			return this;
		}
		public Select setLimit(int limit)
		{
			mLimit = String.valueOf(limit);
			return this;
		}
		public Select setWhere(String where,String selects[])
		{
			mWhere = where;
			if(selects!=null)
			{
			mSelects = new ArrayList<String>();
			for(String s:selects)
				mSelects.add(s);
			}
			return this;
		}
		public String getTable()
		{
			return mTable;
		}
		public boolean hasWhere()
		{
			return mWhere!=null;
		}
		private void check()
		{
			if(mWhere==null)
				throw new IllegalStateException(ERR_NO_WHERE);
		}
		public Select distinct(boolean distinct)
		{
			mDistinct = distinct;
			return this;
		}
		private Select addToSelect(Object sel)
		{
			if(mSelects==null)
				mSelects = new ArrayList<String>();
			mSelects.add(sel.toString());
			return this;
		}
		/** Добавляет в where условие col = value
		 * @param col Название столбца
		 * @param value Требуемое значение. В БД ищется значение value.toString()
		 * @return
		 */
		public Select eq(String col,Object value)
		{
			return compare(col, "=", value);
		}
		public Select whereRaw(String value)
		{
			mWhere+=value;
			return this;
		}
		public Select checkLength(String col,String operand,int size)
		{
			whereRaw(LENGTH+'(' + col + ')'+operand+_SPACE+size+_SPACE);
			return this;
		}
		private Select compare(String col,String operand,Object value)
		{
			check();
			if(mWhere.length()>0)
				mWhere+=_SPACE;
			mWhere+=col+_SPACE+operand+_SPACE+'?'+_SPACE;
			return addToSelect(value);
		}
		public Select like(String col,String like)
		{
			return compare(col, "LIKE", like);
		}
		public Select globNoCase(String col,String like){
			return glob(col,getGlobNoCase(like));
		}
		public Select glob(String col,String like)
		{
			return compare(col, "GLOB", like);
		}
		public Select less(String col,Object val)
		{
			return compare(col, "<", val);
		}
		public Select lessOrEqual(String col,Object val)
		{
			return compare(col, "<=", val);
		}
		public Select moreOrEqual(String col,Object val)
		{
			return compare(col, ">=", val);
		}
		public Select more(String col,Object val)
		{
			return compare(col, ">", val);
		}
		public Select notEqual(String col,Object val)
		{
			return compare(col, "!=", val);
		}
		/** Добавляет к Where условие and */
		public Select and()
		{
			check();
			mWhere+=_SPACE+AND+_SPACE;
			return this;
		}
		/** Добавляет к Where условие or */
		public Select or()
		{
			check();
			mWhere+=_SPACE+OR+_SPACE;
			return this;
		}
		public Select startAndCount(int start, int count){
			this.mLimit=""+start+", "+count;
			return this;
		}
		/** Выставляет лимит количества выбираемых значений
		 * @param limit Лимит выборки */
		public Select limit(int limit)
		{
			this.mLimit=""+limit;
			return this;
		}
		/** Сортировка выборки 
		 * @param col Столбец сортировки
		 * @param asc true - по возрастанию, false - по убыванию
		 * @return
		 */
		public Select orderBy(String col,boolean asc)
		{
			mOrderBy=col+_SPACE+(asc?ASC:DESC);
			return this;
		}
		public Select orderBy(String order)
		{
			mOrderBy=order;
			return this;
		}
		/** Задает столбцы таблицы для выборки 
		 * @param cols Список столбцов 
		 */
		public Select columns(String ...cols)
		{
			mColumns = cols;
			return this;
		}
		public boolean selectAndRead(ContentResolver cr,Uri selectUri,CursorReader reader)
		{
			Cursor cursor = selectOpt(cr,selectUri);
			return reader.read(cursor);
		}
		public Cursor select(ContentResolver cr,Uri selectUri)
		{
			return  cr.query(selectUri, mColumns, mWhere, getSelectArgs(), mOrderBy);//db.query(mTable, mColumns, mWhere, getSelectArgs(), null, null, mOrderBy,mLimit);
		}
		public int deleteOpt(ContentResolver cr,Uri selectUri)
		{
			try{
				return cr.delete(selectUri, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				
			}
			return -1;
		}
		public Cursor selectOpt(ContentResolver cr,Uri selectUri)
		{
			Cursor c = null;
			try{
				c = select(cr, selectUri);
			}
			catch(Throwable e)
			{
				MyLog.err(e);
			}
			return  c;
		}

		private String [] getSelectArgs()
		{
			if(mSelects!=null)
				return mSelects.toArray(new String[mSelects.size()]);
			return null;
		}
		@SuppressWarnings("deprecation")
		@Override
		public String toString() {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			String str = builder.buildQuery(mColumns, mWhere, getSelectArgs(), null, null, mOrderBy, mLimit);
			if(mSelects!=null)
				str+=" whereArgs:"+mSelects.toString();
			return str;
		}
		/** Выполняет выборку в базе db . В случае неудачи может бросить SQLException */
		public Cursor select(SQLiteDatabase db)
		{
			return db.query(mDistinct,mTable,mColumns,mWhere,getSelectArgs(),null,null,mOrderBy,mLimit);
			//return db.query(mTable, mColumns, mWhere, getSelectArgs(), null, null, mOrderBy,mLimit);
		}
		public int update(ContentResolver cr,Uri contentUri,ContentValues cv)
		{
			try{
				return cr.update(contentUri, cv, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				MyLog.err(e);
			}
			return -1;
		}
		public int insertOrUpdate(SQLiteDatabase db,ContentValues cv){
			int upd = update(db,cv);
			if(upd > 0)
				return 1;
			if(db.insert(mTable,StrConst.BLOB,cv) <0)
				return  -1;
			return 1;

		}
		public int update(SQLiteDatabase db,ContentValues cv)
		{
			try{
				return db.update(mTable, cv, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				MyLog.err(e);
			}
			return -1;
		}
/** Возвращает количество элементов, соответствующих текущей выборке
 * @param db База данных для выполнения запроса
 * @return Количество жлементов или -1, если произошла ошибка при выборке
 */
		public int getCount(SQLiteDatabase db)
		{
			Cursor c = selectOrNull(db);
			if(c==null)
				return -1;
			int ret = c.getCount();
			c.close();
			return ret;
		}
		/** Возвращает true, если есть хоть один элемент, соответствующий текущей выборке */
		public boolean hasOne(SQLiteDatabase db)
		{
			return getCount(db)>0;
		}
		public final boolean selectAndRead(SQLiteDatabase db,CursorReader reader)
		{
			return reader.read(selectOrNull(db));
		}
		public final Cursor selectOpt(SQLiteDatabase db)
		{
			return selectOrNull(db);
		}
		/** Возвращает курсор выборки или null в случае ошибки */
		public Cursor selectOrNull(SQLiteDatabase db)
		{
			try{
			return select(db);
			}
			catch(Throwable e)
			{
				MyLog.err(e);
			}
			return null;
		}
		
		public int delete(SQLiteDatabase db)
		{
			return db.delete(mTable, mWhere, getSelectArgs());
		}
		public int deleteOpt(SQLiteDatabase db)
		{
			try{
				return db.delete(mTable, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				MyLog.err(e);
			}
			return -1;
		}
	}
	/** Запуск транзакции */
	public static abstract class Transaction
	{
		SQLiteDatabase db;
		boolean ok = true;
		/** Конструктор 
		 * @param db База данных
		 */
		public Transaction(SQLiteDatabase db) {
			this.db = db;
		}
		/** Абстрактная функция для вызова в теле транзакции 
		 * @param db База данных
		 * @throws Throwable Если функция бросила exception - транзакция считается неуспешной и откатывается
		 */
		public abstract void call(SQLiteDatabase db) throws Throwable;
		/** Старт транзакции
		 * @return true в случае успеха, false в случае неудачи
		 */
		public boolean execute()
		{
			try{
				db.beginTransaction();
				call(db);
				db.setTransactionSuccessful();
			}
			catch(Throwable e)
			{
				ok = false;
			}
			finally
			{
				db.endTransaction();
			}
			return ok;
		}
	}
	public static int getCountAndClose(Cursor c)
	{
		int count = 0;
		if(c==null)
			return count;
		if(c.isBeforeFirst())
		{
			if(c.moveToFirst())
				count = c.getCount();
		}
		count = c.getCount();
		c.close();
		return count;
			
	}
	/** Делает курсору moveToFirst. Берет строку из столбца fieldName и закрывает курсор.
	 * @param c курсор
	 * @param fieldName Столбец, из которого нужно взять строку
	 * @return Возвращает строку или null в случае ошибки
	 */
	public static String getStringFromCursorAndClose(Cursor c,String fieldName)
	{
		String ret = null;
		if(c==null)
			return ret;
		if(c.moveToFirst())
			ret = c.getString(c.getColumnIndex(fieldName));
		c.close();
		return ret;
	}
	/** Возвращает строку order by
	 * 
	 * @param column Столбец, для которого нужен order by
	 * @param asc true - по возрастанию, false - по убыванию
	 * @return Возвращает строку условия Order by [column] [DESC] 
	 */
	public static String getOrder(String column,boolean asc)
	{
		if(asc)
			return column;
		return column+' '+StrConst.DESC;
	}
	
	public static boolean tableExists(SQLiteDatabase db,String tableName)
	{
		try{
			Cursor c = new Select(StrConst.SQLITE_MASTER).columns(StrConst.NAME).where().eq("type", StrConst.TABLE).and().eq(StrConst.NAME, tableName).selectOrNull(db);
			return getStringFromCursorAndClose(c, StrConst.NAME)!=null;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/** Возвращает sql, с которым была создана таблица tableName
	 * 
	 * @param db База данных
	 * @param tableName Таблица, чей sql нужно узнать
	 * @return Строка sql или null
	 */
	public static String getCreateStatementFromTable(SQLiteDatabase db,String tableName)
	{
		try{
			String sql;
			Cursor c = new Select(StrConst.SQLITE_MASTER).columns(StrConst.SQL,StrConst.NAME).where().eq(StrConst.TYPE, StrConst.TABLE).and().eq(StrConst.NAME, tableName).selectOrNull(db);
			sql = getStringFromCursorAndClose(c, StrConst.SQL);
			return sql;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/** Удаляет таблицу tableToDrop
	 * @param db База данных
	 * @param tableToDrop таблица для удаления
	 * @return Возвращает true в случае успешного удаления (и если таблицы не существует). Иначе - false
	 */
	public static boolean dropTable(SQLiteDatabase db, String tableToDrop)
	{
		try{
			String sql = new StringBuffer().append( "DROP TABLE "+StrConst.IF_EXISTS).append(' ').append(tableToDrop).toString();
			db.execSQL(sql);
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
	/** Переименовывает таблицу from в to
	 * 
	 * @param db База данных
	 * @param from Исходная таблица
	 * @param to Во что переименовываем
	 * @return true - удалось переименовать, false - ошибка
	 */
	public static boolean renameTable(SQLiteDatabase db, String from,String to)
	{
		try{
			if(!dropTable(db, to))
				return false;
			String sql = new StringBuffer().append( "ALTER TABLE ").append(from).append(" RENAME TO ").append(to).toString();
			db.execSQL(sql);
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
