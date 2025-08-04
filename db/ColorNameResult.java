package com.example.coloredapp.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ColorNameResult {
    public static String getName(int r, int g, int b, SQLiteDatabase db) {
        String name = "Desconhecido";
        String query;
        Cursor cursor;

        // Comando que pega a cor mais próxima da cor selecionada
        query = "SELECT nome_cor, " +
                "((red - ?) * (red - ?) + (green - ?) * (green - ?) + (blue - ?) * (blue - ?)) AS distance " +
                "FROM Cor " +
                "ORDER BY distance ASC " +
                "LIMIT 1";

        // Inserindo os valores de RGB na query.
        String[] args = { String.valueOf(r), String.valueOf(r),
                String.valueOf(g), String.valueOf(g),
                String.valueOf(b), String.valueOf(b) };

        cursor = db.rawQuery(query, args);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow("nome_cor"));
            }
            cursor.close();
        }

        return name;
    }
}
