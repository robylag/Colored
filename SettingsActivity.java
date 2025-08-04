package com.example.coloredapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Configurações do COLORED");
        setContentView(R.layout.settings_activity);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(this, CameraActivity.class);
            this.startActivity(cameraIntent);
        });
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button openTest = findViewById(R.id.openTest);
        openTest.setOnClickListener(v -> {
            Intent testIntent = new Intent(this, testActivity.class);
            this.startActivity(testIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reabre o serviço flutuante ao sair da tela
        Intent intent = new Intent(this, FloatingWidgetService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }


}