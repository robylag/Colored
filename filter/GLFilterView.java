package com.example.coloredapp.filter;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

public class GLFilterView extends GLSurfaceView {
    private FilterRenderer renderer;
    public GLFilterView(Context context) {
        super(context);
        init(context);
    }
    public GLFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        setEGLContextClientVersion(2);

        // Configura o EGL para pedir buffer com canal alfa de 8 bits (transparência)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // Permite que a view fique por cima e com fundo transparente
        setZOrderOnTop(true);
        getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);

        renderer = new FilterRenderer(context, this);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
    public void startCapture(MediaProjection projection) {
        if (projection == null) {
            Log.e("FilterRenderer", "❌ MediaProjection recebido é null!");
            return;
        } else {
            Log.d("FilterRenderer", "✅ MediaProjection recebido é válido!");
        }
        renderer.startCapture(projection);
    }
    public void stopCapture() {
        renderer.stopCapture();
    }
    public void setFilter(FilterRenderer.FilterType type) {
        renderer.setFilter(type);
    }
}
