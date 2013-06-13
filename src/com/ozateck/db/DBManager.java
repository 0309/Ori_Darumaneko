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

public class DBManager{
	
	private static final String TAG = "myTag";
	
	private String dbName    = "ranking.db";
	private int    dbVersion = 1;
	private String tableName = "mytable";
	private String columns[] = {"myind", "myname", "myscore"};
	
	private DBHelper       dbHelper;
	private SQLiteDatabase db;
	
	public DBManager(Context context){
		//DBオブジェクト取得
		dbHelper = new DBHelper(context, dbName, dbVersion, tableName);
		db       = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		db.close();
	}
	
	//DBの初期化(初期値無し)
	public void resetAll(){
		dbHelper.resetAll(db);
	}
	
	//DBの初期化(初期値設定)
	public void initialize(){
		resetAll();
		try{
			insert("abc", "500");
			insert("def", "400");
			insert("ghi", "300");
			insert("jkl", "200");
			insert("mno", "100");
		}catch(Exception e){
			Log.d(TAG, "initialize error.");
		}
	}

	//追加
	public void insert(String myname, String myscore){
		//ContentValues
		ContentValues values = new ContentValues();
		//values.put("myind", "");//追加の場合、primary key なのでここは抜いておく
		values.put("myname", myname);
		values.put("myscore", Integer.valueOf(myscore));
		
		//db
		db.insert(tableName, "", values);
	}

	//更新
	public void update(int myind, String myname, String myscore) throws Exception{
		//ContentValues
		ContentValues values = new ContentValues();
		values.put("myind",  new Integer(myind));
		values.put("myname", myname);
		values.put("myscore", Integer.valueOf(myscore));
		
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
	
	//ランキングに入っているかどうか
	public boolean isRankin(int newScore, int limit){
		boolean rankin = false;//ランクインフラグ
		Cursor cursor = db.query(tableName, columns, 
				null, null, null, null, "myscore DESC", ""+limit);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			int myInd = cursor.getInt(0);
			String myName = cursor.getString(1);
			int myScore = cursor.getInt(2);
			if(newScore >= myScore){
				rankin = true;
			}
			cursor.moveToNext();
		}
		cursor.close();
		return rankin;
	}
	
	//ランキングのリストを取得
	public List<RankData> getRankList(int limit){
		List<RankData> list = new ArrayList<RankData>();
		Cursor cursor = db.query(tableName, columns, 
				null, null, null, null, "myscore DESC", ""+limit);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			int myInd = cursor.getInt(0);
			String myName = cursor.getString(1);
			int myScore = cursor.getInt(2);
			list.add(new RankData(myInd, myName, myScore, false));
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	//ランキングのリストを取得(チェック済みのリスト)
	public List<RankData> getRankList(String newName, int newScore, int limit){
		List<RankData> list = new ArrayList<RankData>();
		boolean rankin = false;//ランクインフラグ
		Cursor cursor = db.query(tableName, columns, 
				null, null, null, null, "myscore DESC", ""+limit);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			int myInd = cursor.getInt(0);
			String myName = cursor.getString(1);
			int myScore = cursor.getInt(2);
			//ランクインしている場合はリストに追加
			if(newScore >= myScore && rankin == false){
				list.add(new RankData(myInd, newName, newScore, true));
				rankin = true;
			}
			//limitの件数に達していなければ追加
			if(list.size() < limit){
				if(rankin){
					list.add(new RankData(myInd+1, myName, myScore, false));
				}else{
					list.add(new RankData(myInd, myName, myScore, false));
				}
			}
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}
}