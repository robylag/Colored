package com.example.coloredapp.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

// View personalizada que usa OpenGL ES 2.0 para exibir o preview da câmera com filtros
public class GLCameraView extends GLSurfaceView {
    private CameraRenderer renderer;

    // Construtor usado quando a view é criada programaticamente
    public GLCameraView(Context context) {
        super(context);
        init(context);
    }

    // Construtor usado quando a view é definida em XML
    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Inicialização da GLSurfaceView e do renderer
    private void init(Context context) {
        // Define a versão do OpenGL ES (2.0)
        setEGLContextClientVersion(2);

        // Cria e associa o renderer responsável por desenhar a câmera e aplicar filtros
        renderer = new CameraRenderer(context, this);
        setRenderer(renderer);

        // Define que o OpenGL só vai renderizar quando for explicitamente solicitado
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    // Método público para trocar o filtro de cor
    public void setFilter(CameraRenderer.FilterType filterType) {
        renderer.setFilterType(filterType); // Define o novo filtro
        requestRender(); // Solicita que o frame seja redesenhado
    }
}