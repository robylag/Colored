package com.example.coloredapp.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class CameraScanner extends View {
    private float centerX = 550f;
    private float centerY = 900f;

    public float getCenterX() {
        return centerX;
    }
    public float getCenterY() {
        return centerY;
    }

    private final Paint darkPaint = new Paint();
    private final Paint glowPaint = new Paint();
    private final Paint clearPaint = new Paint();
    private final Paint crosshairPaint = new Paint();

    private float radius = 200f;

    public CameraScanner(Context context) {
        super(context);
        init();
    }

    public CameraScanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Fundo escuro
        darkPaint.setColor(0x99000000); // 60% opaco
        darkPaint.setStyle(Paint.Style.FILL);

        // Clareamento central
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Pintura do glow
        glowPaint.setAntiAlias(true);

        // Mira "+"
        crosshairPaint.setColor(0xFFFFFFFF);
        crosshairPaint.setStrokeWidth(4f);
        crosshairPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Gradiente radial para efeito brilhante
        RadialGradient gradient = new RadialGradient(
                centerX,
                centerY,
                radius + 30f, // brilho levemente maior que o buraco
                new int[]{0x80FFFFFF, 0x00FFFFFF},
                new float[]{0.3f, 1f},
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Escurece toda a tela
        canvas.drawRect(0, 0, getWidth(), getHeight(), darkPaint);

        // Adiciona brilho por trás
        canvas.drawCircle(centerX, centerY, radius + 20f, glowPaint);

        // Clareia o círculo
        canvas.drawCircle(centerX, centerY, radius, clearPaint);

        // Desenha a mira "+"
        float lineLength = 200;
        canvas.drawLine(centerX - lineLength, centerY, centerX + lineLength, centerY, crosshairPaint);
        canvas.drawLine(centerX, centerY - lineLength, centerX, centerY + lineLength, crosshairPaint);
    }
}
