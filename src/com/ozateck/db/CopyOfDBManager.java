package com.ozateck.db;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CopyOfDBManager{
	
	private static final String TAG = "myTag";
	
	private String dbName    = "ranking.db";
	private int    dbVersion = 1;
	private String tableName = "mytable";
	private String columns[] = {"myind", "myname", "myscore"};
	
	private DBHelper       dbHelper;
	private SQLiteDatabase db;
	
	public CopyOfDBManager(Context context){
		//DBオブジェクト取得
		dbHelper = new DBHelper(context, dbName, dbVersion, tableName);
		db       = dbHelper.getWritableDatabase();
	}
	
	//DBから読込(name)
	public String readName(int myind) throws Exception{
		Cursor cursor = db.query(tableName, columns,
						"myind = " + myind, null, null, null, null);
		String str = "";
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			str = cursor.getString(1);
			cursor.close();
		}else{
			throw new Exception();
		}
		return str;
	}
	
	//DBから読込(List)
	public List<String[]> getList(){
		List<String[]> list = new ArrayList<String[]>();
		Cursor cursor = db.query(tableName, columns, 
								 null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			String[] strs = new String[columns.length];
			for(int i=0; i<columns.length; i++){
				strs[i] = cursor.getString(i);
			}
			list.add(strs);
			cursor.moveToNext();
		}
		return list;
	}
	
	//DBから読込(List)
	public List<String[]> getList(int limit){
		List<String[]> list = new ArrayList<String[]>();
		String sql = "SELECT * FROM " + tableName  +  " ORDER BY myscore DESC LIMIT " + limit + ";";
		Log.d(TAG, sql);
		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			String[] strs = new String[columns.length];
			for(int i=0; i<columns.length; i++){
				strs[i] = cursor.getString(i);
			}
			list.add(strs);
			cursor.moveToNext();
		}
		return list;
	}
	
	//追加
	public void insert(String myname, int myscore) throws Exception{
		//ContentValues
		ContentValues values = new ContentValues();
		//values.put("myind", "");//追加の場合、primary key なのでここは抜いておく
		values.put("myname", myname);
		values.put("myscore", new Integer(myscore));
		
		//db
		db.insert(tableName, "", values);
	}
	
	//更新
	public void update(int myind, String myname, int myscore) throws Exception{
		//ContentValues
		ContentValues values = new ContentValues();
		values.put("myind",  new Integer(myind));
		values.put("myname", myname);
		values.put("myscore", new Integer(myscore));
		
		//db
		db.update(tableName, values, "myind=" + myind, null);
	}
	
	//削除
	public void delete(int myind) throws Exception{
		//db
		db.delete(tableName, "myind=" + myind, null);
	}
	
	//総件数の取得
	public int getTotalCount(){
		int count;
		Cursor cursor = db.query(tableName, columns, 
								 null, null, null, null, null);
		count = cursor.getCount();
		cursor.close();
		return count;
	}
}