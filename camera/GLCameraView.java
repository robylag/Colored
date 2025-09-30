package com.example.coloredapp.camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

import android.provider.MediaStore;

import android.opengl.GLSurfaceView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class GLCameraView extends GLSurfaceView {

    private static final String TAG = "GLCameraView";

    private CameraRenderer renderer;
    private CameraHelper cameraHelper;
    private boolean isCameraReady = false;

    // Listener para avisar a Activity que a câmera está pronta
    public interface OnCameraReadyListener {
        void onReady();
    }

    private OnCameraReadyListener cameraReadyListener;

    public void setOnCameraReadyListener(OnCameraReadyListener listener) {
        this.cameraReadyListener = listener;
    }

    // Construtor programático
    public GLCameraView(Context context) {
        super(context);
        init(context);
    }

    // Construtor via XML
    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);

        renderer = new CameraRenderer(context, this);

        // Configura listener para SurfaceTexture pronto
        renderer.setSurfaceTextureListener(new CameraRenderer.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
                if (cameraHelper == null) {
                    cameraHelper = new CameraHelper(context, surfaceTexture);

                    cameraHelper.setCameraReadyCallback(() -> {
                        isCameraReady = true;
                        if (cameraReadyListener != null) {
                            post(() -> cameraReadyListener.onReady());
                        }
                    });

                    cameraHelper.openCamera();
                }
            }
        });

        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void startCamera() {
        // A câmera inicia via listener do SurfaceTexture
    }

    public void stopCamera() {
        isCameraReady = false;
        if (cameraHelper != null) {
            cameraHelper.stopCamera();
            cameraHelper = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Câmera abre no momento certo via listener do SurfaceTexture
    }

    public void setFilter(CameraRenderer.FilterType filterType) {
        renderer.setFilterType(filterType);
        requestRender();
    }

    // Capture a foto com filtro, salvando o bitmap renderizado pelo OpenGL
    public void capturePhoto() {
        if (!isCameraReady) {
            Log.w(TAG, "Camera not ready for capture");
            return;
        }
        queueEvent(() -> {
            Bitmap filteredBitmap = renderer.getCurrentFrameBitmap();
            if (filteredBitmap != null) {
                new Thread(() -> {
                    saveBitmapToGallery(filteredBitmap);
                    filteredBitmap.recycle();
                }).start();
            } else {
                Log.e(TAG, "Failed to capture bitmap with filter");
            }
        });
    }

    public void captureImage(OnImageCapturedListener listener) {
        queueEvent(() -> {
            Bitmap bitmap = renderer.getCurrentFrameBitmap();
            if (bitmap != null && listener != null) {
                post(() -> listener.onCaptured(bitmap));
            }
        });
    }

    public interface OnImageCapturedListener {
        void onCaptured(Bitmap bitmap);
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        try {
            OutputStream out;
            Context context = getContext();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Colored");

                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Log.e(TAG, "Failed to create new MediaStore record.");
                    return;
                }
                out = context.getContentResolver().openOutputStream(uri);
            } else {
                File picturesDir = null;
                picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File coloredDir = new File(picturesDir, "Colored");
                if (!coloredDir.exists()) coloredDir.mkdirs();
                File file = new File(coloredDir, "IMG_" + System.currentTimeMillis() + ".jpg");
                out = new FileOutputStream(file);

                // Notifica galeria para escanear o arquivo
                context.sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }

            if (out != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                Log.d(TAG, "Imagem com filtro salva na galeria");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar imagem com filtro", e);
        }
    }
}