package com.example.coloredapp.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ColorNameResult {
    static Cursor cursor;
    static String query;
    static String name;
    static String category;
    public static String getName(int r, int g, int b, SQLiteDatabase db) {
        name = "Desconhecido";

        // Comando que pega a cor mais próxima da cor selecionada
        String query = "SELECT *, " +
                "(((red - ?) * (red - ?)) + ((green - ?) * (green - ?)) + ((blue - ?) * (blue - ?))) AS distance " +
                "FROM Cor " +
                "ORDER BY distance ASC " +
                "LIMIT 1";


        // Inserindo os valores de RGB na query.
        String[] args = { String.valueOf(r), String.valueOf(r),
                String.valueOf(g), String.valueOf(g),
                String.valueOf(b), String.valueOf(b) };

        cursor = db.rawQuery(query, args);

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("nome_cor"));
            category = cursor.getString(cursor.getColumnIndexOrThrow("id_cor"));
        }
        cursor.close();

        return name;
    }

    public static String getNameCategory(SQLiteDatabase db) {
        String nameCategory = "Desconhecida";
        query = "SELECT nome_cor FROM Categoria WHERE id_categoria = ?";
        String[] args = { category };
        cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            nameCategory = cursor.getString(cursor.getColumnIndexOrThrow("nome_cor"));
        }
        cursor.close();
        return nameCategory;
    }

}
