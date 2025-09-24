package com.example.coloredapp.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

// FrameLayout personalizado que mantém uma proporção fixa (ex: 4:3) para seus filhos
public class AspectRatioFrameLayout extends FrameLayout {

    // Proporção desejada (largura / altura)
    private float aspectRatio = 0f;

    // Construtor para uso programático
    public AspectRatioFrameLayout(Context context) {
        super(context);
    }

    // Construtor para uso via XML
    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Define a proporção desejada (por exemplo, 4f / 3f)
    public void setAspectRatio(float ratio) {
        if (ratio <= 0) return;
        this.aspectRatio = ratio;
        requestLayout(); // Força o layout a ser recalculado com a nova proporção
    }

    // Sobrescreve o metodo de medição do layout para manter a proporção desejada
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Obtém as dimensões disponíveis
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Se a proporção ainda não foi definida, usa o comportamento padrão
        if (aspectRatio == 0f) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // Calcula a proporção atual da view
        float viewRatio = (float) width / height;

        if (viewRatio > aspectRatio) {
            // A view está mais larga do que deveria — ajusta a largura para manter a proporção
            width = (int) (height * aspectRatio);
        } else {
            // A view está mais alta do que deveria — ajusta a altura
            height = (int) (width / aspectRatio);
        }

        // Cria novas medidas exatas com os valores ajustados
        int newWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int newHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        // Aplica as novas dimensões ao layout
        super.onMeasure(newWidthSpec, newHeightSpec);
    }
}
