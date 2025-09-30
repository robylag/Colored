package com.example.coloredapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;

public class DatabaseHelper {
    private static final String DB_NAME = "ColoredAPP.db";
    private final Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    public SQLiteDatabase openDatabase() {
        File dbFile = context.getDatabasePath(DB_NAME);
        String dbPath = dbFile.getPath();

        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
}
