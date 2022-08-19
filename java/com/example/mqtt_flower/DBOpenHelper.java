package com.example.mqtt_flower;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据库sql语句并执行
        String sql1="create table device1(" +
                "id integer primary key autoincrement," +
                "date timestamp NOT NULL default (datetime('now','localtime'))," +
                "longitude TEXT NOT NULL," +
                "latitude TEXT NOT NULL," +
                "CurrentTemperature TEXT NOT NULL," +
                "Humidity TEXT NOT NULL," +
                "water_bump TEXT NOT NULL," +
                "air_bump TEXT NOT NULL," +
                "WindLevel TEXT NOT NULL," +
                "shield TEXT NOT NULL" +
                ")"
                ;

        db.execSQL(sql1);


        for (int i=0;i<10;i++){
            ContentValues values = new ContentValues();

            values.put("longitude","114.43979");
            values.put("latitude","30.46922");
            values.put("CurrentTemperature",20);
            values.put("Humidity",20);
            values.put("water_bump",0);
            values.put("air_bump",0);
            values.put("WindLevel",0);
            values.put("shield",0);

            db.insert("device1", null, values);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
