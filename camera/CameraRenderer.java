package com.example.coloredapp.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.coloredapp.CameraActivity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraRenderer";

    public enum FilterType {
        NONE,
        PROTANOPIA,
        DEUTERANOPIA,
        TRITANOPIA
    }

    public interface SurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture);
    }

    private SurfaceTextureListener surfaceTextureListener;

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        this.surfaceTextureListener = listener;
    }

    private final Context context;
    private static int[] rgb;
    private final GLSurfaceView glSurfaceView;

    private int oesTextureId;
    private SurfaceTexture surfaceTexture;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;

    private final float[] transformMatrix = new float[16];

    private FilterType currentFilter = FilterType.NONE;
    private volatile FilterType pendingFilterType = null;
    private int shaderProgram;

    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    public CameraRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        oesTextureId = OpenGLUtils.createOESTexture();
        if (oesTextureId == 0) {
            Log.e(TAG, "Failed to create external texture.");
            return;
        }

        surfaceTexture = new SurfaceTexture(oesTextureId);
        surfaceTexture.setOnFrameAvailableListener(surface -> glSurfaceView.requestRender());

        GLES20.glClearColor(0f, 0f, 0f, 1f);
        Matrix.setIdentityM(transformMatrix, 0);

        vertexBuffer = OpenGLUtils.createVertexBuffer();
        texCoordBuffer = OpenGLUtils.createTexCoordBuffer();

        shaderProgram = OpenGLUtils.createCameraShaderProgram(currentFilter);

        // Avisa que o SurfaceTexture está pronto para uso
        if (surfaceTextureListener != null) {
            surfaceTextureListener.onSurfaceTextureAvailable(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture == null) return;

        if (pendingFilterType != null && pendingFilterType != currentFilter) {
            if (shaderProgram != 0) {
                GLES20.glDeleteProgram(shaderProgram);
            }
            shaderProgram = OpenGLUtils.createCameraShaderProgram(pendingFilterType);
            currentFilter = pendingFilterType;
            pendingFilterType = null;
        }

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(transformMatrix);

        rgb = readCenterPixelRGB();
        Log.d("ColorReader", "Pixel central original: R=" + rgb[0] + " G=" + rgb[1] + " B=" + rgb[2]);
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        float nr = (float) ((((r / 255.0) - 0.5) * 2f) + 0.5) * 255;
        float ng = (float) ((((g / 255.0) - 0.5) * 2f) + 0.5) * 255;
        float nb = (float) ((((b / 255.0) - 0.5) * 2f) + 0.5) * 255;

        // ---- SATURAÇÃO ----
        float gray = (0.3f * nr + 0.59f * ng + 0.11f * nb);
        nr = gray + (nr - gray) * 2f;
        ng = gray + (ng - gray) * 2f;
        nb = gray + (nb - gray) * 2f;

        // ---- CLAMP ----
        int fr = Math.min(255, Math.max(0, Math.round(nr)));
        int fg = Math.min(255, Math.max(0, Math.round(ng)));
        int fb = Math.min(255, Math.max(0, Math.round(nb)));

        Log.d("ColorReader", "Pixel ajustado contraste: R=" + nr + " G=" + ng + " B=" + nb);

        CameraActivity.getR(fr);
        CameraActivity.getG(fg);
        CameraActivity.getB(fb);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        OpenGLUtils.drawFrame(shaderProgram, oesTextureId, transformMatrix, vertexBuffer, texCoordBuffer);
    }

    public void setFilterType(FilterType filterType) {
        this.pendingFilterType = filterType;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void release() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    public Bitmap getCurrentFrameBitmap() {
        if (surfaceWidth == 0 || surfaceHeight == 0) return null;

        int width = surfaceWidth;
        int height = surfaceHeight;
        int size = width * height;
        IntBuffer buffer = IntBuffer.allocate(size);
        int[] pixels = new int[size];

        GLES20.glReadPixels(0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        int[] data = buffer.array();

        // Inverter linhas (OpenGL salva de baixo para cima)
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pix = data[i * width + j];

                int alpha = (pix >> 24) & 0xff;
                int red   = (pix) & 0xff;
                int green = (pix >> 8) & 0xff;
                int blue  = (pix >> 16) & 0xff;

                pixels[(height - i - 1) * width + j] =
                        (alpha << 24) | (red << 16) | (green << 8) | blue;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private int[] readCenterPixelRGB() {
        // 1️⃣ Criar FBO temporário
        int[] fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);

        // 2️⃣ Criar textura RGBA para armazenar a imagem
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                surfaceWidth, surfaceHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        );
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 3️⃣ Associar FBO à textura
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                tex[0], 0
        );

        // 4️⃣ Renderizar oesTextureId (câmera crua) no FBO usando shader neutro
        int neutralShader = OpenGLUtils.createCameraShaderProgram(FilterType.NONE);
        OpenGLUtils.drawFrame(neutralShader, oesTextureId, transformMatrix, vertexBuffer, texCoordBuffer);
        GLES20.glDeleteProgram(neutralShader);

        // 5️⃣ Ler pixel central
        int centerX = surfaceWidth / 2;
        int centerY = surfaceHeight / 2;
        IntBuffer pixelBuffer = IntBuffer.allocate(1);
        GLES20.glReadPixels(centerX, centerY, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        int pixel = pixelBuffer.get(0);

        // 6️⃣ Extrair cores
        int r = (pixel) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = (pixel >> 16) & 0xFF;

        // 7️⃣ Restaurar framebuffer padrão
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // 8️⃣ Limpar FBO e textura temporária
        GLES20.glDeleteTextures(1, tex, 0);
        GLES20.glDeleteFramebuffers(1, fbo, 0);

        return new int[]{r, g, b};
    }
}