package org.appducegep.mameteo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

import java.util.Date;

// https://developer.android.com/training/data-storage/sqlite.html

public class MeteoDAO extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Meteo.db";

    public MeteoDAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATION_TABLE = "create table meteo(id INTEGER PRIMARY KEY, ville TEXT, soleilOuNuage TEXT, date TEXT)";
        db.execSQL(SQL_CREATION_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void ajouterMeteo(String soleilOuNuage)
    {
        //Date aujourdhui = new Date();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues meteoDuJour = new ContentValues();
        meteoDuJour.put("ville", "Matane");
        meteoDuJour.put("soleilOuNuage", soleilOuNuage);
        meteoDuJour.put("date", DateFormat.format("MMMM d, yyyy ", (new Date()).getTime()).toString());
        long newRowId = db.insert("meteo", null, meteoDuJour);

    }
}
