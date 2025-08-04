package com.example.coloredapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.coloredapp.camera.AspectRatioFrameLayout;
import com.example.coloredapp.camera.CameraRenderer;
import com.example.coloredapp.camera.GLCameraView;
import com.example.coloredapp.floatingButtons.FloatingAnimations;

import java.util.Objects;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 2000;
    // Referência para a view que exibe a câmera com OpenGL
    private GLCameraView glCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CameraActivity", "Executando CameraActivity");
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Mostra o botão de voltar
        getSupportActionBar().setTitle("Filtro de Câmera"); // Título opcional


        // Verifica se a permissão de câmera foi concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Solicita permissão
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        // Define o layout XML como interface da atividade
        setContentView(R.layout.camera_layout);

        // Recupera a GLCameraView (view que renderiza a câmera com OpenGL)
        glCameraView = findViewById(R.id.glCameraView);

        // Configura o container da câmera para manter proporção 4:3 (letterbox)
        AspectRatioFrameLayout aspectContainer = findViewById(R.id.aspectContainer);
        if (aspectContainer != null) {
            aspectContainer.setAspectRatio(9f / 16f);
        }

        // Referencia os botões de filtro no layout
        //Button menuFilters = findViewById(R.id.menuFilters);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) View btnMenu = findViewById(R.id.btnMenuFilters);
        View menuFilters = findViewById(R.id.overlayMenu);
        Button btnProtanopia = findViewById(R.id.btnProtanopia);
        Button btnDeuteranopia = findViewById(R.id.btnDeuteranopia);
        Button btnTritanopia = findViewById(R.id.btnTritanopia);
        Button btnNone = findViewById(R.id.btnNone); // Botão para remover o filtro
        ImageButton exitButton = findViewById(R.id.exitButton); // Botão para fechar o menu

        // Abre o menu de filtros ao clicar no botão
        btnMenu.setOnClickListener(v ->
                FloatingAnimations.fadeBackground(menuFilters));

        exitButton.setOnClickListener(v ->
                FloatingAnimations.fadeOut(menuFilters));

        // Aplica o filtro de protanopia ao clicar
        btnProtanopia.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.PROTANOPIA));

        // Aplica o filtro de deuteranopia ao clicar
        btnDeuteranopia.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.DEUTERANOPIA));

        // Aplica o filtro de tritanopia ao clicar
        btnTritanopia.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.TRITANOPIA));

        // Remove qualquer filtro aplicado
        btnNone.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.NONE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa a renderização da câmera para liberar recursos
        if (glCameraView != null) glCameraView.onPause();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retoma a renderização da câmera ao voltar para a atividade
        if (glCameraView != null) glCameraView.onResume();
    }
}