package com.example.interviewsimulator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context c) { super(c, "interview.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, pass TEXT)");
        db.execSQL("CREATE TABLE results(id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, topic TEXT, " +
                "correct INTEGER, total INTEGER, happy INTEGER, neutral INTEGER, nervous INTEGER, date TEXT DEFAULT CURRENT_TIMESTAMP)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS users"); db.execSQL("DROP TABLE IF EXISTS results"); onCreate(db);
    }

    static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return s; }
    }

    public boolean register(String name, String email, String pass) {
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("email", email); v.put("pass", hash(pass));
        return getWritableDatabase().insert("users", null, v) != -1;
    }
    public boolean login(String email, String pass) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM users WHERE email=? AND pass=?", new String[]{email, hash(pass)});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }
    public void saveResult(String email, String topic, int correct, int total, int happy, int neutral, int nervous) {
        ContentValues v = new ContentValues();
        v.put("email", email); v.put("topic", topic); v.put("correct", correct); v.put("total", total);
        v.put("happy", happy); v.put("neutral", neutral); v.put("nervous", nervous);
        getWritableDatabase().insert("results", null, v);
    }
}
