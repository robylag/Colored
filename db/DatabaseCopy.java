package com.example.coloredapp.db;

import android.content.Context;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class DatabaseCopy {
    private static final String DB_NAME = "ColoredAPP.db";

    public static void copyDatabase(Context context) {
        File dbFile = context.getDatabasePath(DB_NAME);

        if (!dbFile.exists()) {
            // Cria a pasta databases se não existir
            Objects.requireNonNull(dbFile.getParentFile()).mkdirs();

            try (InputStream is = context.getAssets().open(DB_NAME);
                 OutputStream os = new FileOutputStream(dbFile)) {

                byte[] buffer = new byte[1024];
                int length;

                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                Log.d("DBCopy", "Banco copiado com sucesso");
            } catch (IOException e) {
                Log.e("DBCopy", "Erro ao copiar o banco", e);
            }
        } else {
            Log.d("DBCopy", "Banco já existe, não copiando");
        }
    }
}
