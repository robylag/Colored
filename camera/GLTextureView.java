package com.example.coloredapp.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLTextureView extends GLSurfaceView {

    // Renderizador responsável por aplicar os filtros usando OpenGL

    // Construtor usado quando a view é instanciada via XML
    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
