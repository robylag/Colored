package com.example.coloredapp.floatingButtons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class ScannerScopeBright extends View {
    private float centerX = 500f;
    private float centerY = 800f;
    public float getCenterX() {
        return centerX;
    }
    public float getCenterY() {
        return centerY;
    }


    private final Paint darkPaint = new Paint();
    private final Paint clearPaint = new Paint();
    private final Paint crosshairPaint = new Paint();

    public ScannerScopeBright(Context context) {
        super(context);
        init();
    }
    public ScannerScopeBright(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // Habilita interações
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        // Pintura escura
        darkPaint.setColor(0x99000000); // 60% opaco
        darkPaint.setStyle(Paint.Style.FILL);
        // Clareamento no círculo
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        // Estilo da mira "+"
        crosshairPaint.setColor(0xFFFFFFFF); // branco
        crosshairPaint.setStrokeWidth(4f);
        crosshairPaint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // Escurece toda a tela
        canvas.drawRect(0, 0, getWidth(), getHeight(), darkPaint);
        // Clareia o círculo
        float radius = 200f;
        canvas.drawCircle(centerX, centerY, radius, clearPaint);
        // Desenha a mira "+" no centro do círculo
        float lineLength = 200;
        // Linha horizontal
        canvas.drawLine(centerX - lineLength, centerY, centerX + lineLength, centerY, crosshairPaint);
        // Linha vertical
        canvas.drawLine(centerX, centerY - lineLength, centerX, centerY + lineLength, crosshairPaint);
    }

    public void setHighlightCenter(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        invalidate();
    }
}
