package com.example.coloredapp.floatingButtons;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

public class WindowLayoutParamsPosition {
    // Layout para o botão flutuante
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static WindowManager.LayoutParams layoutparams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
    }
    // Posição do botão flutuante na tela (canto superior esquerdo)
    @SuppressLint("RtlHardcoded")
    public static void layoutPosition(WindowManager.LayoutParams buttonLayoutParams) {
        buttonLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        buttonLayoutParams.x = 0;
        buttonLayoutParams.y = 200;
    }
    // Layout para o botão de deletar
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static WindowManager.LayoutParams delete_layoutparams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
    }
    // Posição do botão de deletar na tela (Centro inferior)
    public static void deletePosition(WindowManager.LayoutParams deleteLayoutParams) {
        deleteLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        deleteLayoutParams.y = 200;
    }
    // Layout para o layout raiz do menu de ferramentas e fundo escuro (darkBackground)
    @SuppressLint("RtlHardcoded")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static WindowManager.LayoutParams toolMenuRootParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                650,  // largura em pixels
                650,  // altura em pixels
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );


        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }
    // Posição do layout raiz do menu de ferramentas
    public static void toolMenuPosition(WindowManager windowManager, View toolMenu, WindowManager.LayoutParams params, View floatingButton) {
        floatingButton.post(() -> {
            int[] buttonLocation = new int[2];
            floatingButton.getLocationOnScreen(buttonLocation);
            int x = buttonLocation[0] + floatingButton.getWidth() - 100;
            int y = buttonLocation[1] + floatingButton.getHeight() - 450;
            params.x = x;
            params.y = y;
            windowManager.updateViewLayout(toolMenu, params);
        });
    }

    // Posição da tela de filtro
    @SuppressLint("RtlHardcoded")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static WindowManager.LayoutParams filterTabParams(){
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static WindowManager.LayoutParams circleBrightParams(){
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // Permite toque
                PixelFormat.TRANSLUCENT
        );
    }

    public static void circuleBrightPosition(WindowManager.LayoutParams circulareBrightParams){
        circulareBrightParams.gravity = Gravity.TOP | Gravity.START;
        circulareBrightParams.x = 100;
        circulareBrightParams.y = 200;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static WindowManager.LayoutParams resultColorParams(WindowManager.LayoutParams circleBrightParams) {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
    }

    public static void resultColorPosition(WindowManager.LayoutParams circleBrightParams) {
        circleBrightParams.gravity = Gravity.CENTER;
        circleBrightParams.x = 0;
        circleBrightParams.y = 0;
    }

    public static WindowManager.LayoutParams filterScreenParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                PixelFormat.TRANSLUCENT
        );
    }

    public static WindowManager.LayoutParams darkBackgroundParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                PixelFormat.TRANSLUCENT
        );
    }
    public static void applyImmersiveMode(View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        });
    }
    public static WindowManager.LayoutParams confirmParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                PixelFormat.TRANSLUCENT
        );
    }
}
