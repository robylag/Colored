package com.example.coloredapp.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Classe responsável por renderizar o preview da câmera com filtros em OpenGL ES
public class CameraRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraRenderer";

    // Enum que define os tipos de filtro disponíveis
    public enum FilterType {
        NONE,
        PROTANOPIA,
        DEUTERANOPIA,
        TRITANOPIA
    }

    private final Context context;
    private final GLSurfaceView glSurfaceView;

    private int oesTextureId; // ID da textura externa (OES) usada para receber frames da câmera
    private SurfaceTexture surfaceTexture; // Objeto que conecta a câmera ao OpenGL
    private CameraHelper cameraHelper; // Classe auxiliar para controlar a câmera

    private FloatBuffer vertexBuffer;    // Buffer de coordenadas de vértices
    private FloatBuffer texCoordBuffer;  // Buffer de coordenadas de textura

    private final float[] transformMatrix = new float[16]; // Matriz de transformação dos frames

    private FilterType currentFilter = FilterType.NONE; // Filtro atualmente aplicado
    private volatile FilterType pendingFilterType = null; // Filtro a ser aplicado (pendente)
    private int shaderProgram; // Programa de shader usado para desenhar a imagem da câmera

    // Construtor recebe contexto e GLSurfaceView
    public CameraRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
    }

    // Chamado quando a surface OpenGL é criada
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        // Cria uma textura externa (OES) para os frames da câmera
        oesTextureId = OpenGLUtils.createOESTexture();
        if (oesTextureId == 0) {
            Log.e(TAG, "Falha ao criar textura externa.");
            return;
        }

        // Inicializa o SurfaceTexture e escuta por novos frames para renderizar
        surfaceTexture = new SurfaceTexture(oesTextureId);
        surfaceTexture.setOnFrameAvailableListener(surface -> glSurfaceView.requestRender());

        // Define a cor de fundo como preto
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        Matrix.setIdentityM(transformMatrix, 0);

        // Inicializa a câmera com o SurfaceTexture
        cameraHelper = new CameraHelper(context, surfaceTexture);
        cameraHelper.openCamera();

        // Cria os buffers de vértices e coordenadas de textura
        vertexBuffer = OpenGLUtils.createVertexBuffer();
        texCoordBuffer = OpenGLUtils.createTexCoordBuffer();

        // Cria o shader inicial (sem filtro)
        shaderProgram = OpenGLUtils.createCameraShaderProgram(currentFilter);
    }

    // Chamado quando a superfície muda de tamanho (ex: rotação de tela)
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height); // Define a área de renderização
    }

    // Chamado a cada frame para desenhar a imagem da câmera
    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture == null) return;

        // Aplica filtro novo, se necessário
        if (pendingFilterType != null && pendingFilterType != currentFilter) {
            // Libera o shader antigo
            if (shaderProgram != 0) {
                GLES20.glDeleteProgram(shaderProgram);
            }
            // Cria shader com o novo filtro
            shaderProgram = OpenGLUtils.createCameraShaderProgram(pendingFilterType);
            currentFilter = pendingFilterType;
            pendingFilterType = null;
        }

        // Atualiza o frame da câmera e aplica a matriz de transformação
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(transformMatrix);

        // Limpa o framebuffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Desenha a imagem da câmera na tela usando OpenGL
        OpenGLUtils.drawFrame(shaderProgram, oesTextureId, transformMatrix, vertexBuffer, texCoordBuffer);
    }

    // Define o tipo de filtro a ser aplicado (de forma segura entre threads)
    public void setFilterType(FilterType filterType) {
        this.pendingFilterType = filterType;
    }

    // Retorna o SurfaceTexture para a GLTextureView
    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    // Libera recursos usados (câmera e SurfaceTexture)
    public void release() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (cameraHelper != null) {
            cameraHelper.stopCamera();
        }
    }
}