package com.example.coloredapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.coloredapp.floatingButtons.FloatingTouchButtons;

public class PermissionActivity extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 100;
    private MediaProjectionManager projectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                MediaProjection projection = projectionManager.getMediaProjection(resultCode, data);
                ProjectionHolder.setMediaProjection(projection);

                // registra o callback corretamente
                projection.registerCallback(new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        Log.d("MediaProjection", "Captura finalizada");
                    }
                }, new Handler(Looper.getMainLooper()));
                FloatingTouchButtons.returnProjection();
                ProjectionHolder.setMediaProjection(projection);

                Toast.makeText(this, "MediaProjection atualizada!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada!", Toast.LENGTH_SHORT).show();
            }
            finish(); // fecha essa Activity
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
