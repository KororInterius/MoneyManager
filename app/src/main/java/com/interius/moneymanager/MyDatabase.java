package com.interius.moneymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Koror on 28.05.2017.
 */

public class MyDatabase {

    public static final String DB_NAME = "MMDB"; // money manager database
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "MONEYMANAGER";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_MONEY = "MONEY";
    public static final String COLUMN_DATE = "DATA";

    private Context context;

    public MyDatabase(Context context)
    {
       this.context = context;
    }
    private MySQLiteOpenHelper mySQlitOH;
    private SQLiteDatabase myDatabase;


    public void open(){
        mySQlitOH = new MySQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
        myDatabase = mySQlitOH.getWritableDatabase();
    }

    public  void close()
    {
        if(mySQlitOH != null) mySQlitOH.close();
    }

    //получить все базу
    public Cursor getAllData()
    {
        return myDatabase.query(TABLE_NAME,null, null, null, null, null, null);
    }
    //добавляет запись в бд
    public void addRec(String name, int money, String date)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_MONEY, money);
        cv.put(COLUMN_DATE, date);
        myDatabase.insert(TABLE_NAME, null, cv);
    }

    public void delAll()
    {
        myDatabase.delete(TABLE_NAME,null,null);
    }

    //находит строку и передает курсор со значение денег
    public Cursor findRec(int pos)
    {
        Cursor c ;
        String[] columns = new String[]{COLUMN_MONEY,COLUMN_NAME};
        c = myDatabase.query(TABLE_NAME,columns,null,null,null,null,null,null);
        c.moveToFirst();
        c.moveToPosition(pos);
        return c;
    }
    //удаляет строку по id
    public void delRec(long id)
    {
        myDatabase.delete(TABLE_NAME, COLUMN_ID + " = " + id, null);
    }

    public void update(long id, String name, int money, String date)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_MONEY, money);
        cv.put(COLUMN_DATE, date);
        // обновляем по id
        myDatabase.update(TABLE_NAME, cv, "_id="+id, null);
    }



    private class MySQLiteOpenHelper extends SQLiteOpenHelper {

        public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_MONEY + " TEXT, " +
                    COLUMN_DATE + " TEXT" + ");");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }
}
