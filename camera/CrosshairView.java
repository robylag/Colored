package com.example.coloredapp.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CrosshairView extends View {

    private Paint paint;

    public CrosshairView(Context context) {
        super(context);
        init();
    }

    public CrosshairView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xFFFFFFFF); // Branco
        paint.setStrokeWidth(4f);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float lineLength = 300f; // tamanho da linha da mira

        // Linha horizontal
        canvas.drawLine(centerX - lineLength, centerY, centerX + lineLength, centerY, paint);
        // Linha vertical
        canvas.drawLine(centerX, centerY - lineLength, centerX, centerY + lineLength, paint);
    }
}
