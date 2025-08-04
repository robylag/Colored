package com.example.coloredapp.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

// Classe auxiliar para abrir, configurar e controlar a câmera usando Camera2 API
public class CameraHelper {
    private static final String TAG = "CameraHelper";

    private final Context context;
    private final SurfaceTexture surfaceTexture;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    public CameraHelper(Context context, SurfaceTexture surfaceTexture) {
        this.context = context;
        this.surfaceTexture = surfaceTexture;
    }

    // Abre a câmera e inicia o fluxo de captura
    public void openCamera() {
        // Verifica permissão de câmera
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Camera permission not granted.");
            return;
        }

        startBackgroundThread(); // Inicia thread de fundo para operações da câmera

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            // Obtém o ID da primeira câmera disponível (normalmente a traseira)
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            // Obtém as resoluções de preview suportadas
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                Log.e(TAG, "StreamConfigurationMap is null.");
                return;
            }

            // Escolhe a melhor resolução com proporção 4:3
            Size previewSize = chooseBestSize(map.getOutputSizes(SurfaceTexture.class));
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(surfaceTexture); // Conecta o SurfaceTexture à câmera

            // Abre a câmera de forma assíncrona
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    Log.d(TAG, "Camera opened.");
                    cameraDevice = camera;
                    createCaptureSession(camera, surface); // Cria sessão de captura
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    Log.w(TAG, "Camera disconnected.");
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    camera.close();
                    cameraDevice = null;
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException: ", e);
        }
    }

    // Cria a sessão de captura para exibir o preview da câmera
    private void createCaptureSession(CameraDevice camera, Surface surface) {
        try {
            camera.createCaptureSession(
                    Collections.singletonList(surface), // Apenas um destino: o preview
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;

                            captureSession = session;

                            try {
                                // Cria requisição de captura contínua (preview)
                                CaptureRequest.Builder builder =
                                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(surface);

                                // Define modos de foco automático e exposição automática
                                builder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                builder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON);

                                // Inicia o preview contínuo
                                session.setRepeatingRequest(builder.build(), null, backgroundHandler);
                                Log.d(TAG, "Capture session configured.");
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Failed to start preview.", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Capture session configuration failed.");
                        }
                    },
                    backgroundHandler
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create capture session.", e);
        }
    }

    // Escolhe a melhor resolução disponível com proporção 16:9
    private Size chooseBestSize(Size[] sizes) {
        Size bestSize = null;
        for (Size size : sizes) {
            float aspect = (float) size.getWidth() / size.getHeight();
            if (Math.abs(aspect - (16f / 9f)) < 0.01f) {
                if (bestSize == null || size.getWidth() > bestSize.getWidth()) {
                    bestSize = size;
                }
            }
        }

        // Se não encontrar 16:9, pega a maior resolução disponível
        if (bestSize == null && sizes.length > 0) {
            bestSize = Collections.max(Arrays.asList(sizes),
                    Comparator.comparingInt(s -> s.getWidth() * s.getHeight()));
        }

        Log.d(TAG, "Selected preview size: " + bestSize.getWidth() + "x" + bestSize.getHeight());
        return bestSize;
    }

    // Inicia uma thread separada para operações da câmera (evita travar UI)
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    // Encerra a câmera e libera recursos
    public void stopCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        stopBackgroundThread();
    }

    // Encerra a thread de fundo com segurança
    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Background thread interrupted.", e);
            }
        }
    }
}