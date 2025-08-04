package com.example.coloredapp.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ColorNameResult {
    static Cursor cursor;
    static String query;
    static String name;
    public static String getName(int r, int g, int b, SQLiteDatabase db) {
        name = "Desconhecido";

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
    public static String getCategory(String name, SQLiteDatabase db) {
        String category = "Desconhecida";
        query = "SELECT id_cor FROM Cor WHERE nome_cor = ?";
        String[] args = { name };

        cursor = db.rawQuery(query, args);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                category = cursor.getString(cursor.getColumnIndexOrThrow("id_cor"));
            }
            cursor.close();
        }

        query = "SELECT nome_cor from Categoria WHERE id_categoria = ?";
        String[] args2 = { category };
        cursor = db.rawQuery(query, args2);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                category = cursor.getString(cursor.getColumnIndexOrThrow("nome_cor"));
            }
            cursor.close();
        }
        return category;
    }

}
