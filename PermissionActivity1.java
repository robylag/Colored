package com.example.coloredapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.coloredapp.floatingButtons.FloatingTouchButtons;

public class PermissionActivity1 extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null){
                Intent serviceIntent = new Intent(this, NotificationService.class);
                serviceIntent.putExtra("resultCode", resultCode);
                serviceIntent.putExtra("data", data);
                Log.d("MainActivity-Colored","Iniciando o serviço");
                FloatingTouchButtons.returnProjection();
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
