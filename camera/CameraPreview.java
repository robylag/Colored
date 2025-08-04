package com.example.coloredapp.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.Surface;

import java.util.Arrays;

/**
 * Classe responsável por exibir o preview da câmera usando TextureView.
 */
public class CameraPreview extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraPreview";

    // Sessão de captura da câmera
    private CameraCaptureSession cameraCaptureSession;

    // Instância da câmera em uso
    private CameraDevice cameraDevice;

    // ID da câmera (por padrão, usa a traseira)
    private String cameraId;

    // Gerenciador de câmeras
    private CameraManager cameraManager;

    // Builder para requisições de captura
    private CaptureRequest.Builder previewRequestBuilder;

    // Requisição de captura em si
    private CaptureRequest previewRequest;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Obtém o serviço de câmera do sistema
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        // Define o listener para eventos do SurfaceTexture
        setSurfaceTextureListener(this);
    }

    // Chamado quando o SurfaceTexture está pronto para uso
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera(); // Abre a câmera assim que a textura estiver disponível
    }

    // Não é necessário tratar mudanças de tamanho neste caso
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    // Fecha a câmera quando o SurfaceTexture é destruído
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        closeCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    /**
     * Abre a câmera traseira (ID 0) se a permissão foi concedida.
     */
    private void openCamera() {
        try {
            cameraId = cameraManager.getCameraIdList()[0]; // Seleciona a câmera traseira

            // Verifica permissão antes de abrir a câmera
            if (getContext().checkSelfPermission(android.Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, null);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to open camera: " + e.getMessage());
        }
    }

    // Callback chamado ao abrir, desconectar ou ocorrer erro com a câmera
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            startPreview(); // Inicia o preview após abrir a câmera
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    /**
     * Inicia o preview da câmera configurando o SurfaceTexture como destino.
     */
    private void startPreview() {
        try {
            SurfaceTexture texture = getSurfaceTexture();

            // Define o tamanho do buffer do SurfaceTexture para o tamanho da view
            texture.setDefaultBufferSize(getWidth(), getHeight());

            Surface surface = new Surface(texture);

            // Cria a requisição de captura para preview
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // Cria a sessão de captura
            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            cameraCaptureSession = session;

                            // Constrói a requisição final e inicia preview contínuo
                            previewRequest = previewRequestBuilder.build();
                            try {
                                cameraCaptureSession.setRepeatingRequest(previewRequest, null, null);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Failed to start camera preview: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.e(TAG, "Camera preview configuration failed");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting preview: " + e.getMessage());
        }
    }

    /**
     * Fecha a câmera e libera os recursos da sessão de captura.
     */
    private void closeCamera() {
        if (cameraCaptureSession != null) {
            try {
                cameraCaptureSession.stopRepeating(); // Para as requisições contínuas
                cameraCaptureSession.abortCaptures();  // Aborta capturas pendentes
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error stopping camera session: " + e.getMessage());
            }
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}