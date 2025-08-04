package com.example.coloredapp.floatingButtons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.coloredapp.CameraActivity;
import com.example.coloredapp.FloatingWidgetService;
import com.example.coloredapp.PermissionActivity;
import com.example.coloredapp.ProjectionHolder;
import com.example.coloredapp.R;
import com.example.coloredapp.SettingsActivity;
import com.example.coloredapp.filter.FilterRenderer;
import com.example.coloredapp.filter.GLFilterView;

public class FloatingTouchButtons {
    private static boolean clicked = false;
    // Condição de segmentação do botão flutuante (long press) para verificar se foi considerado como segurando botão
    public static void holdingCondition(long pressDuration, long LONG_PRESS_THRESHOLD, AtomicBoolean isHolding, View floatingButton, View deleteButton) {
        Log.d("FloatingWidgetService", "Duração do tempo segurado: " + pressDuration);

        if (pressDuration >= LONG_PRESS_THRESHOLD && !isHolding.get()) {
            Log.d("FloatingWidgetService", "Botão segurado, prosseguindo abertura do botão de deletar.");

            isHolding.set(true); // Marca como segurando

            FloatingAnimations.scaleUp(floatingButton);
            FloatingAnimations.fadeIn(deleteButton);
        }
        else Log.i("FloatingWidgetService", "Botão não segurado, não prosseguindo abertura do botão de deletar.");
    }
    public static boolean movedCondition(long pressDuration, long LONG_PRESS_THRESHOLD) {
        return pressDuration >= LONG_PRESS_THRESHOLD;
    }
    public static void cancelCondition(long pressDuration, long LONG_PRESS_THRESHOLD, View darkBackground, View toolMenu, View filterTab, View toolMenuRoot, ImageView floatingBtnIcon, View floatingBtnMain){
        if(pressDuration < LONG_PRESS_THRESHOLD && !clicked){
            Log.d("FloatingWidgetService","Executando abrir as ferramentas");
            clicked = true;
            // Executar a função de abrir os botões de ferramentas;
            floatingBtnIcon.setImageResource(R.drawable.close_btn);
            toolMenuRoot.setVisibility(View.VISIBLE);
            darkBackground.setVisibility(View.VISIBLE);
            FloatingAnimations.scaleUp(floatingBtnMain);
            FloatingAnimations.fadeBackground(darkBackground);
            FloatingAnimations.fadeBackground(toolMenu);
        }
        else if(pressDuration < LONG_PRESS_THRESHOLD){
            Log.d("FloatingWidgetService","Executando fechar as ferramentas");
            clicked = false;
            // Executar a função de fechar os botões de ferramentas;
            floatingBtnIcon.setImageResource(R.drawable.logo);
            FloatingAnimations.scaleDown(floatingBtnMain);
            FloatingAnimations.fadeOut(toolMenu);
            FloatingAnimations.fadeOut(darkBackground);
            FloatingAnimations.fadeOut(filterTab);
            toolMenuRoot.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void darkBackgroundTouch(View darkBackground, View toolMenu,View filterTab,View toolMenuRoot){
        Log.d("FloatingTouchButton","Função de toque no fundo escuro habilitado.");
        darkBackground.setOnTouchListener((v, event) -> {
            Log.d("FloatingTouchButton","Fundo clicado!");
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                FloatingAnimations.fadeOut(v);
                FloatingAnimations.fadeOut(toolMenu);
                FloatingAnimations.fadeOut(filterTab);
                toolMenuRoot.setVisibility(View.GONE);
                Log.d("FloatingTouchButton", "Toque no fundo escuro detectado. Fechando menu.");
                return true;
            }
            return false;
        });
    }

    @SuppressLint({"ClickableViewAcessibility", "ClickableViewAccessibility"})
    public static void toolBtnTouchRedirect(Context context, View floatingButtonCamera, View floatingButtonMenu){
        floatingButtonCamera.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                context.stopService(new Intent(context, FloatingWidgetService.class));
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent cameraIntent = new Intent(context, CameraActivity.class);
                    cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(cameraIntent);
                });
                return true;
            }
            return false;
        });
        floatingButtonMenu.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                context.stopService(new Intent(context, FloatingWidgetService.class));
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent menuIntent = new Intent(context, SettingsActivity.class);
                    menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(menuIntent);
                });
                return true;
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void colorPickerTouch(Context context, View floatingButtonFilter, View circleBright, View toolmenu, View toolMenuRoot, WindowManager windowManager, WindowManager.LayoutParams circleBrightParams, View floatingConfirmProjection,View darkBackground, View floatingBtnMain, ImageView floatingBtnIcon, View toolMenu) {
        floatingButtonFilter.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Verificando se há uma projeção sendo executada.
                if (ProjectionHolder.getMediaProjection() != null) {
                    ProjectionHolder.getMediaProjection().stop();
                    ProjectionHolder.setMediaProjection(null);
                    Log.d("FloatingTouchButtons", "Projeção encerrada com sucesso.");
                }
                FloatingAnimations.fadeIn(floatingConfirmProjection);
                FloatingAnimations.fadeOut(darkBackground);
                clicked = false;
                floatingBtnIcon.setImageResource(R.drawable.logo);
                FloatingAnimations.scaleDown(floatingBtnMain);
                FloatingAnimations.fadeOut(toolMenu);
                toolMenuRoot.setVisibility(View.GONE);
                Button confirm = floatingConfirmProjection.findViewById(R.id.confirm_button);
                confirm.setOnClickListener(c -> {
                    Log.d("FloatingTouchButtons","Permissão concedida");
                    floatingConfirmProjection.setVisibility(View.GONE);

                    Intent permissionIntent = new Intent(context, PermissionActivity.class);
                    permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(permissionIntent);

                    // VERIFICAÇÃO DE PERMISSÃO
                    if (Settings.canDrawOverlays(context)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            circleBrightParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        } else {
                            circleBrightParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                        }

                        WindowLayoutParamsPosition.circuleBrightPosition(circleBrightParams);

                        try {
                            windowManager.addView(circleBright, circleBrightParams);
                            Log.d("FloatingTouchButtons", "Overlay adicionado com sucesso.");
                            circleBright.setVisibility(View.VISIBLE);
                            toolMenuRoot.setVisibility(View.GONE);
                            toolmenu.setVisibility(View.GONE);
                        } catch (WindowManager.BadTokenException e) {
                            Log.e("FloatingTouchButtons", "Erro ao adicionar overlay: " + e.getMessage());
                        }
                    } else {
                        Log.e("FloatingTouchButtons", "Permissão SYSTEM_ALERT_WINDOW não concedida.");
                        Toast.makeText(context, "Permissão de sobreposição não concedida.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void filterTouch(View floatingButtonFilter, View toolmenu, View filterTab) {
        floatingButtonFilter.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("FloatingTouchButtons", "Toque no botão de leitura detectado.");
                FloatingAnimations.fadeOut(toolmenu);
                FloatingAnimations.fadeBackground(filterTab);
                return true;
            }
            return false;
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    public static void filterBtnTouch(View btnProtanopia, View btnDeuteranopia, View btnTritanopia, View btnNone, View filterScreen, MediaProjection mediaProjection,View floatingButtonFilter,Context context) {
        GLFilterView glFilterView = filterScreen.findViewById(R.id.gl_filter_view);

        btnProtanopia.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Protanopia Clicado, iniciar captura");
            verificationMediaProjection(mediaProjection,floatingButtonFilter,context);
            filterScreen.setVisibility(View.VISIBLE);
            glFilterView.startCapture(mediaProjection);
            glFilterView.setFilter(FilterRenderer.FilterType.PROTANOPIA);
            glFilterView.requestRender();
        });

        btnDeuteranopia.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Deuteranopia Clicado, iniciar captura");
            verificationMediaProjection(mediaProjection,floatingButtonFilter,context);
            glFilterView.startCapture(mediaProjection);
            glFilterView.setFilter(FilterRenderer.FilterType.DEUTERANOPIA);
            glFilterView.requestRender();
        });

        btnTritanopia.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Tritanopia Clicado, iniciar captura");
            verificationMediaProjection(mediaProjection,floatingButtonFilter,context);
            glFilterView.startCapture(mediaProjection);
            glFilterView.setFilter(FilterRenderer.FilterType.TRITANOPIA);
            glFilterView.requestRender();
        });

        btnNone.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Nada Clicado, desabilitar filtro");
            glFilterView.setFilter(FilterRenderer.FilterType.NONE);
            glFilterView.stopCapture();
            glFilterView.requestRender();
        });
    }

    public static void verificationMediaProjection(MediaProjection mediaProjection,View floatingConfirmProjection,Context context){
        if(ProjectionHolder.getMediaProjection() != null){
            ProjectionHolder.getMediaProjection().stop();
            ProjectionHolder.setMediaProjection(null);

            FloatingAnimations.fadeIn(floatingConfirmProjection);

            Button confirm = floatingConfirmProjection.findViewById(R.id.confirm_button);
            confirm.setOnClickListener(c -> {
                Log.d("FloatingTouchButtons","Permissão concedida");
                floatingConfirmProjection.setVisibility(View.GONE);

                Intent permissionIntent = new Intent(context, PermissionActivity.class);
                permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(permissionIntent);
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void closeResultColor(View closeButtonColor, View resultColor){
        closeButtonColor.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("FloatingWidget", "Toque no botão de fechar detectado.");
                FloatingAnimations.fadeOut(resultColor);
                return true;
            }
            return false;
        });
    }
}
