package com.example.coloredapp.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.coloredapp.camera.CameraRenderer.FilterType;


public class GLTextureView extends GLSurfaceView {

    // Renderizador responsável por aplicar os filtros usando OpenGL
    private CameraRenderer renderer;

    // Construtor usado quando a view é instanciada via XML
    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Inicializa a GLSurfaceView com a versão do OpenGL e o renderizador.
     * Deve ser chamado após a criação da view.
     */
    public void init() {
        // Define o uso do OpenGL ES 2.0
        setEGLContextClientVersion(2);

        // Cria e configura o renderizador da câmera
        renderer = new CameraRenderer(getContext(), this);
        setRenderer(renderer);

        // Define o modo de renderização para contínuo (frames são renderizados constantemente)
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    /**
     * Permite alterar o filtro aplicado em tempo real.
     * @param type Tipo de filtro a ser aplicado (ex: PROTANOPIA, DEUTERANOPIA, etc.)
     */
    public void setFilter(FilterType type) {
        if (renderer != null) {
            renderer.setFilterType(type);
        }
    }

    /**
     * Retorna a SurfaceTexture usada para capturar o feed da câmera.
     * Pode ser usada para configurar o CameraHelper.
     * @return SurfaceTexture da câmera ou null se o renderer ainda não foi inicializado.
     */
    public SurfaceTexture getSurfaceTexture() {
        return renderer != null ? renderer.getSurfaceTexture() : null;
    }
}
