package com.example.coloredapp.filter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FilterRenderer implements GLSurfaceView.Renderer {

    public enum FilterType { NONE, PROTANOPIA, DEUTERANOPIA, TRITANOPIA }

    private static final String TAG = "FilterRenderer";

    private final Context context;
    private final GLSurfaceView glSurfaceView;

    private SurfaceTexture screenTexture;
    private int screenTextureId;
    private Surface inputSurface;

    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;

    private boolean isCapturing = false;

    private int screenWidth;
    private int screenHeight;

    private FilterType currentFilter = FilterType.NONE;

    public FilterRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
    }

    public void setMediaProjection(MediaProjection projection) {
        this.projection = projection;
    }

    public void setFilter(FilterType filter) {
        this.currentFilter = filter != null ? filter : FilterType.NONE;
    }

    private final MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            Log.i(TAG, "MediaProjection stopped");
            stopCapture();
        }
    };

    private final VirtualDisplay.Callback virtualDisplayCallback = new VirtualDisplay.Callback() {
        @Override
        public void onPaused() {
            Log.d(TAG, "Virtual display paused");
        }
        @Override
        public void onResumed() {
            Log.d(TAG, "Virtual display resumed");
        }
        @Override
        public void onStopped() {
            Log.d(TAG, "Virtual display stopped");
        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("FilterRenderer", "onSurfaceCreated() chamado");
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLUtils.init();

        screenTextureId = GLUtils.createOESTexture();
        Log.d("FilterRenderer", "screenTextureId criado: " + screenTextureId);

        screenTexture = new SurfaceTexture(screenTextureId);
        Log.d("FilterRenderer", "SurfaceTexture criada");

        inputSurface = new Surface(screenTexture);
        Log.d("FilterRenderer", "InputSurface criada a partir da SurfaceTexture");

        screenTexture.setOnFrameAvailableListener(surfaceTexture -> {
            Log.d("FilterRenderer", "Frame disponível na SurfaceTexture");
            glSurfaceView.queueEvent(() -> {
                Log.d("FilterRenderer", "Solicitando renderização via glSurfaceView.requestRender()");
                glSurfaceView.requestRender();
            });
        });
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        screenWidth = width;
        screenHeight = height;
        GLUtils.resizeFBO(width, height);

        if (projection != null && !isCapturing) {
            createVirtualDisplay();
            isCapturing = true;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!isCapturing || screenTexture == null) {
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            return;
        }

        // Atualiza a imagem capturada do SurfaceTexture
        screenTexture.updateTexImage();

        // Obtém a matriz de transformação do SurfaceTexture (STMatrix)
        float[] stMatrix = new float[16];
        screenTexture.getTransformMatrix(stMatrix);

        // Limpa o framebuffer
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Define o tamanho da área de visualização
        GLES20.glViewport(0, 0, screenWidth, screenHeight);

        // Renderiza a textura com o filtro e a matriz de textura correta
        GLUtils.drawTextureWithFilter(screenTextureId, currentFilter.ordinal(), stMatrix);
    }





    private void createVirtualDisplay() {
        if (inputSurface == null || projection == null) return;

        screenTexture.setDefaultBufferSize(screenWidth, screenHeight);

        Handler handler = new Handler(Looper.getMainLooper());

        projection.registerCallback(projectionCallback, handler);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int densityDpi = metrics.densityDpi;

        virtualDisplay = projection.createVirtualDisplay(
                "GLCapture",
                screenWidth,
                screenHeight,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                inputSurface,
                virtualDisplayCallback,
                handler
        );
    }

    public void startCapture(MediaProjection projection) {
        setMediaProjection(projection);
        if (!isCapturing && screenWidth > 0 && screenHeight > 0) {
            createVirtualDisplay();
            isCapturing = true;
        }
    }

    public void stopCapture() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (projection != null) {
            projection.unregisterCallback(projectionCallback);
            projection.stop();
            projection = null;
        }
        if (screenTexture != null) {
            screenTexture.release();
            screenTexture = null;
        }
        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }
        isCapturing = false;
    }
}
