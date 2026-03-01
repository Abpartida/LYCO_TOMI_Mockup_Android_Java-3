package com.lyco.tomi.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lyco.tomi.ui.PestItem;

import java.util.ArrayList;
import java.util.List;

public class PestDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pests.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_PESTS = "pests";

    public PestDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PESTS + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "count INTEGER NOT NULL," +
            "detected_at TEXT NOT NULL," +
            "trap_location TEXT NOT NULL" +
            ")");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PESTS);
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        insert(db, "Whitefly", 12, "2025-10-06 13:20", "Trap 1");
        insert(db, "Aphid", 7, "2025-10-05 11:02", "Trap 2");
        insert(db, "Thrips", 3, "2025-10-04 16:45", "Trap 3");
    }

    private void insert(SQLiteDatabase db, String name, int count, String time, String location) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("count", count);
        values.put("detected_at", time);
        values.put("trap_location", location);
        db.insert(TABLE_PESTS, null, values);
    }

    public List<PestItem> getAllPests() {
        List<PestItem> pests = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_PESTS,
                new String[]{"name", "count", "detected_at", "trap_location"},
                null,
                null,
                null,
                null,
                "datetime(detected_at) DESC")) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                int count = cursor.getInt(1);
                String time = cursor.getString(2);
                String location = cursor.getString(3);
                pests.add(new PestItem(name, count, time, location));
            }
        }
        return pests;
    }
}
