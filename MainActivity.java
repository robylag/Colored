package com.example.coloredapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coloredapp.db.DatabaseCopy;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity-Colored", "Iniciando a MainActivity");

        // 1. Verifica a permissão de overlay antes de qualquer coisa
        if (!Settings.canDrawOverlays(this)) {
            setContentView(R.layout.overlay_permission);

            Button confirmButton = findViewById(R.id.btn_confirm);
            confirmButton.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent); // NÃO usa finish aqui
            });
            return; // Impede que o resto do onCreate continue sem permissão
        }

        // 2. Se já tem permissão, continua normalmente
        setContentView(R.layout.activity_main);

        DatabaseCopy.copyDatabase(this);

        Intent serviceIntent = new Intent(this, FloatingWidgetService1.class);
        startService(serviceIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.canDrawOverlays(this)) {
            Intent serviceIntent = new Intent(this, FloatingWidgetService1.class);
            startService(serviceIntent);
        }
    }
}
