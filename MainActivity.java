package com.example.coloredapp;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.coloredapp.db.DatabaseCopy;
import com.example.coloredapp.db.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager projectionManager;

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

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.openDatabase();

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startMediaProjectionRequest();
    }



    private void startMediaProjectionRequest() {
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null){
                Intent serviceIntent = new Intent(this, FloatingWidgetService.class);
                serviceIntent.putExtra("resultCode", resultCode);
                serviceIntent.putExtra("data", data);
                Log.d("MainActivity-Colored","Iniciando o serviço");

                ContextCompat.startForegroundService(this, serviceIntent);

                finish();
            } else {
                Toast.makeText(this, "Permissão de captura de tela negada", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }
}

