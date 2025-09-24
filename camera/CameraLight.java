package com.example.coloredapp.camera;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class CameraLight {

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    public CameraLight(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            // Pega o ID da câmera traseira com flash
            for (String id : cameraManager.getCameraIdList()) {
                if (Boolean.TRUE.equals(cameraManager.getCameraCharacteristics(id)
                        .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE))) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void toggleFlashlight(Context context) {
        try {
            if (cameraManager == null) return;

            if (cameraId == null) {
                // Tenta encontrar a câmera correta
                for (String id : cameraManager.getCameraIdList()) {
                    Boolean hasFlash = cameraManager.getCameraCharacteristics(id)
                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    if (Boolean.TRUE.equals(hasFlash)) {
                        cameraId = id;
                        break;
                    }
                }
            }

            if (cameraId == null) {
                Toast.makeText(context, "Nenhuma câmera com flash encontrada", Toast.LENGTH_SHORT).show();
                return;
            }

            // Permissão runtime
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show();
                return;
            }

            isFlashOn = !isFlashOn;
            cameraManager.setTorchMode(cameraId, isFlashOn);
            Toast.makeText(context, "Flash " + (isFlashOn ? "ligado" : "desligado"), Toast.LENGTH_SHORT).show();

        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(context, "Não foi possível acessar a lanterna", Toast.LENGTH_SHORT).show();
        }
    }

}

