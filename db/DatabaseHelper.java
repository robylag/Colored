package com.example.coloredapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;

public class DatabaseHelper {
    private static final String DB_NAME = "ColoredAPP.db";
    private final Context context;
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    public SQLiteDatabase openDatabase() {
        File dbFile = context.getDatabasePath(DB_NAME);
        String dbPath = dbFile.getPath();

        database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        return database;
    }

    public void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}
