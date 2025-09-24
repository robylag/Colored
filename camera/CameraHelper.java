package com.example.coloredapp.camera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class CameraHelper {
    private static final String TAG = "CameraHelper";

    private final Context context;
    private final SurfaceTexture surfaceTexture;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private ImageReader imageReader;

    private Size previewSize;
    private Size captureSize;

    // Callback para avisar quando câmera estiver pronta para captura
    public interface CameraReadyCallback {
        void onCameraReady();
    }

    private CameraReadyCallback cameraReadyCallback;

    public void setCameraReadyCallback(CameraReadyCallback callback) {
        this.cameraReadyCallback = callback;
    }

    public CameraHelper(Context context, SurfaceTexture surfaceTexture) {
        this.context = context;
        this.surfaceTexture = surfaceTexture;
    }

    public void openCamera() {
        Log.d(TAG, "openCamera() chamado.");

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permissão de câmera não concedida.");
            return;
        }

        if (cameraDevice != null) {
            Log.w(TAG, "Câmera já aberta.");
            return;
        }

        if (surfaceTexture == null) {
            Log.e(TAG, "SurfaceTexture null.");
            return;
        }

        startBackgroundThread();

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length == 0) {
                Log.e(TAG, "Nenhuma câmera encontrada.");
                return;
            }

            // Usar câmera traseira preferencialmente
            String cameraId = null;
            for (String id : cameraIds) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }
            if (cameraId == null) {
                // fallback para a primeira disponível
                cameraId = cameraIds[0];
            }
            Log.d(TAG, "Usando câmera ID: " + cameraId);

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                Log.e(TAG, "StreamConfigurationMap null.");
                return;
            }

            // Escolhe tamanho preview 4:3 para SurfaceTexture
            previewSize = chooseBestSize(map.getOutputSizes(SurfaceTexture.class));
            if (previewSize == null) {
                Log.e(TAG, "Preview size null.");
                return;
            }

            // Escolhe tamanho para captura em JPEG (maior possível 4:3)
            captureSize = chooseBestSize(map.getOutputSizes(ImageFormat.JPEG));
            if (captureSize == null) {
                Log.e(TAG, "Capture size null.");
                return;
            }

            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            // Configura ImageReader para captura JPEG
            imageReader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(),
                    ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(this::onImageAvailable, backgroundHandler);

            // Abrir câmera
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    Log.d(TAG, "Câmera aberta.");
                    cameraDevice = camera;
                    createCaptureSession(cameraDevice, previewSurface, imageReader.getSurface());
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    Log.w(TAG, "Câmera desconectada.");
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.e(TAG, "Erro câmera: " + error);
                    camera.close();
                    cameraDevice = null;
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException", e);
        }
    }

    private void createCaptureSession(CameraDevice camera, Surface previewSurface, Surface captureSurface) {
        try {
            List<Surface> surfaces = Arrays.asList(previewSurface, captureSurface);

            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    if (cameraDevice == null) return;

                    captureSession = session;

                    try {
                        previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        previewRequestBuilder.addTarget(previewSurface);
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON);

                        captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler);
                        Log.d(TAG, "Preview iniciado.");

                        // Notifica que a câmera está pronta para capturar
                        if (cameraReadyCallback != null) {
                            cameraReadyCallback.onCameraReady();
                        }

                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Falha ao iniciar preview.", e);
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "Falha ao configurar sessão de captura.");
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Erro ao criar sessão de captura.", e);
        }
    }

    private int getJpegOrientation(CameraCharacteristics c, int deviceRotation) {
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        int rotationComp;
        switch (deviceRotation) {
            case Surface.ROTATION_0: rotationComp = 0; break;
            case Surface.ROTATION_90: rotationComp = 90; break;
            case Surface.ROTATION_180: rotationComp = 180; break;
            case Surface.ROTATION_270: rotationComp = 270; break;
            default: rotationComp = 0; break;
        }

        return (sensorOrientation + rotationComp + 360) % 360;
    }

    public void takePicture() {
        if (cameraDevice == null || captureSession == null) {
            Log.e(TAG, "Câmera ou sessão não prontos para capturar.");
            return;
        }
        try {
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON);

            int deviceRotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getRotation();

            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());

            int jpegOrientation = getJpegOrientation(characteristics, deviceRotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            captureSession.stopRepeating();
            captureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request,
                                               TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    try {
                        captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Erro ao reiniciar preview após captura.", e);
                    }
                    Log.d(TAG, "Foto capturada com sucesso.");
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Erro ao capturar foto.", e);
        }
    }

    private void onImageAvailable(ImageReader reader) {
        Log.d(TAG, "onImageAvailable chamado");
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            if (image != null) {
                saveImageToGallery(image);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao adquirir imagem.", e);
        } finally {
            if (image != null) image.close();
        }
    }

    private void saveImageToGallery(Image image) {
        Log.d(TAG, "saveImageToGallery chamado.");
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Colored");

                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    Log.d(TAG, "URI para salvar criada: " + uri.toString());
                    try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                        if (out != null) {
                            out.write(bytes);
                            out.flush();
                            Log.d(TAG, "Bytes escritos no OutputStream com sucesso.");
                        } else {
                            Log.e(TAG, "OutputStream retornou null.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao escrever bytes no OutputStream.", e);
                    }
                    Log.d(TAG, "Imagem salva na galeria: " + uri.toString());
                } else {
                    Log.e(TAG, "Falha ao criar URI para salvar imagem.");
                }
            } else {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File coloredDir = new File(picturesDir, "Colored");
                if (!coloredDir.exists()) {
                    boolean dirCreated = coloredDir.mkdirs();
                    Log.d(TAG, "Diretório Colored criado? " + dirCreated);
                } else {
                    Log.d(TAG, "Diretório Colored já existe.");
                }

                String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
                File file = new File(coloredDir, filename);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    fos.flush();
                    Log.d(TAG, "Imagem salva em arquivo: " + file.getAbsolutePath());

                    context.sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    Log.d(TAG, "Broadcast enviado para atualizar galeria.");
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao salvar arquivo JPEG.", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar imagem.", e);
        }
    }

    private Size chooseBestSize(Size[] sizes) {
        Size bestSize = null;
        for (Size size : sizes) {
            float aspect = (float) size.getWidth() / size.getHeight();
            if (Math.abs(aspect - (4f / 3f)) < 0.01f) {
                if (bestSize == null || size.getWidth() > bestSize.getWidth()) {
                    bestSize = size;
                }
            }
        }
        if (bestSize == null && sizes.length > 0) {
            bestSize = Collections.max(Arrays.asList(sizes),
                    Comparator.comparingInt(s -> s.getWidth() * s.getHeight()));
        }
        return bestSize;
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public void stopCamera() {
        Log.d(TAG, "Parando câmera...");
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        stopBackgroundThread();
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread de fundo interrompida.", e);
            } finally {
                backgroundThread = null;
                backgroundHandler = null;
            }
        }
    }
}