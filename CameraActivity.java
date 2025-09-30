package com.example.coloredapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.coloredapp.sound.SoundEffects;
import com.example.coloredapp.db.DatabaseHelper;
import com.example.coloredapp.floatingButtons.FloatingAnimations;
import com.example.coloredapp.floatingButtons.FloatingHistoric;
import com.example.coloredapp.camera.AspectRatioFrameLayout;
import com.example.coloredapp.camera.CameraRenderer;
import com.example.coloredapp.camera.GLCameraView;
import com.example.coloredapp.db.ColorNameResult;

public class CameraActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private final Handler handler = new Handler(Looper.getMainLooper());
    static int r;
    static int g;
    static int b;

    private GLCameraView glCameraView;
    private Button btnCapture;

    @SuppressLint({"MissingInflatedId","LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("COLORED");
        setContentView(R.layout.camera_layout); // Mudar aqui
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.openDatabase();

        glCameraView = findViewById(R.id.glCameraView);
        btnCapture = findViewById(R.id.captureButton);

        AspectRatioFrameLayout aspectContainer = findViewById(R.id.aspectContainer);
        if (aspectContainer != null) {
            aspectContainer.setAspectRatio(3f / 4f);
        }
        View btnMenu = findViewById(R.id.btnMenuFilters);
        View menuFilters = findViewById(R.id.overlayMenu);
        View colorPickerView = findViewById(R.id.crosshairOverlay);
        Button btnProtanopia = findViewById(R.id.btnProtanopia);
        Button btnDeuteranopia = findViewById(R.id.btnDeuteranopia);
        Button btnTritanopia = findViewById(R.id.btnTritanopia);
        Button btnNone = findViewById(R.id.btnNone);
        ImageView btnCloseColor = findViewById(R.id.closeColorPicker);
        View btnLeitura = findViewById(R.id.btnLeitura);
        ImageButton exitButton = findViewById(R.id.exitButton); // Botão para fechar o menu
        View colorPickerResult = findViewById(R.id.colorPickerResult);
        TextView colorCategory = findViewById(R.id.colorCategory);
        TextView colorName = findViewById(R.id.colorName);
        View captureCameraBright = findViewById(R.id.captrePhotoBright);

        final Runnable rgbUpdater = new Runnable() {
            @Override
            public void run() {
                if (colorPickerResult.getVisibility() == View.VISIBLE) {
                    Log.d("ColorPicker","ColorPicker Aberto!");
                    runOnUiThread(() -> {
                        Log.d("ColorPicker", "R=" + r + " G=" + g + " B=" + b);
                        String name = ColorNameResult.getName(r, g, b, db);
                        String category = ColorNameResult.getNameCategory(db);
                        colorName.setText(name);
                        colorCategory.setText(category);
                    });
                    handler.postDelayed(this, 500);
                }
                else{
                    Log.d("ColorPicker","ColorPicker Fechado ou não executando!");
                }
            }
        };

        btnCapture.setEnabled(false); // Desabilita até câmera pronta
        // Abre o menu de filtros ao clicar no botão
        btnMenu.setOnClickListener(v ->
                FloatingAnimations.fadeBackground(menuFilters));
        exitButton.setOnClickListener(v ->
                FloatingAnimations.fadeOut(menuFilters));
        btnProtanopia.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.PROTANOPIA));
        btnDeuteranopia.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.DEUTERANOPIA));
        btnTritanopia.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.TRITANOPIA));
        btnNone.setOnClickListener(v ->
                glCameraView.setFilter(CameraRenderer.FilterType.NONE));
        btnLeitura.setOnClickListener(v->{
            FloatingAnimations.fadeBackground(colorPickerView);
            FloatingAnimations.fadeBackground(btnCloseColor);
            FloatingAnimations.fadeBackground(colorPickerResult);
            handler.post(rgbUpdater);
        });
        btnCloseColor.setOnClickListener(v->{
            FloatingAnimations.fadeOut(colorPickerView);
            FloatingAnimations.fadeOut(btnCloseColor);
            FloatingAnimations.fadeOut(colorPickerResult);
        });
        btnCapture.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                glCameraView.capturePhoto();
                FloatingAnimations.captureFade(captureCameraBright,200);
                SoundEffects.playCameraShutter(this);
                Toast.makeText(this,"Foto salva na galeria!",Toast.LENGTH_SHORT).show();
            }
        });

        glCameraView.setOnCameraReadyListener(() -> runOnUiThread(() -> btnCapture.setEnabled(true)));

        // Inicia câmera somente se permissões estiverem garantidas
        if (checkAndRequestPermissions()) {
            glCameraView.startCamera();
        }
    }

    private boolean checkAndRequestPermissions() {
        boolean cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = true;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        if (!cameraGranted || !storageGranted) {
            String[] permissions;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else {
                permissions = new String[]{Manifest.permission.CAMERA};
            }
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted && glCameraView != null) {
                glCameraView.startCamera();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa a renderização da câmera para liberar recursos
        if (glCameraView != null) glCameraView.onPause();
        FloatingHistoric.openFloating();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatingHistoric.closeFloating();
        // Retoma a renderização da câmera ao voltar para a atividade
        if (glCameraView != null) glCameraView.onResume();
    }

    public static void getR(int red){
        r = red;
    }
    public static void getG(int green){
        g = green;
    }
    public static void getB(int blue) {
        b = blue;
    }

}