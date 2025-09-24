package com.example.coloredapp.floatingButtons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
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
import com.example.coloredapp.filter.CaptureScreenShot;
import com.example.coloredapp.TestDaltActivity;

public class FloatingTouchButtons {
    private static boolean clicked = false;
    public static boolean isProjectionActive=false;
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
    public static void darkBackgroundTouch(View darkBackground, View toolMenu,View filterTab,View toolMenuRoot,ImageView floatingBtnIcon, View floatingBtnMain){
        Log.d("FloatingTouchButton","Função de toque no fundo escuro habilitado.");
        darkBackground.setOnTouchListener((v, event) -> {
            Log.d("FloatingTouchButton","Fundo clicado!");
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                FloatingAnimations.fadeOut(v);
                FloatingAnimations.fadeOut(toolMenu);
                FloatingAnimations.fadeOut(filterTab);
                FloatingAnimations.scaleDown(floatingBtnMain);
                floatingBtnIcon.setImageResource(R.drawable.logo);
                toolMenuRoot.setVisibility(View.GONE);
                clicked = false;
                Log.d("FloatingTouchButton", "Toque no fundo escuro detectado. Fechando menu.");
                return true;
            }
            return false;
        });
    }

    @SuppressLint({"ClickableViewAcessibility", "ClickableViewAccessibility"})
    public static void toolBtnTouchRedirect(Context context, View floatingButtonCamera, View floatingButtonMenu, View toolMenu, View filterTab, View toolMenuRoot, ImageView floatingBtnIcon, View floatingBtnMain, View darkBackground){
        floatingButtonCamera.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("FloatingTouchButtons", "Toque no botão de câmera detectado.");
                FloatingAnimations.closeToolFloating(darkBackground,toolMenu,floatingBtnMain,toolMenuRoot,floatingBtnIcon);
                FloatingAnimations.fadeOut(filterTab);
                clicked = false;
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
                FloatingAnimations.closeToolFloating(darkBackground,toolMenu,floatingBtnMain,toolMenuRoot,floatingBtnIcon);
                clicked = false;
                context.stopService(new Intent(context, FloatingWidgetService.class));
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent menuIntent = new Intent(context, TestDaltActivity.class);
                    menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(menuIntent);
                });
                return true;
            }
            return false;
        });
    }

    // BOTÃO QUE ABRE A LEITURA DE COR EM TELA
    @SuppressLint("ClickableViewAccessibility")
    public static void colorPickerTouch(Context context, View floatingButtonFilter, View circleBright, View toolmenu, View toolMenuRoot, WindowManager windowManager, WindowManager.LayoutParams circleBrightParams, View floatingConfirmProjection,View darkBackground, View floatingBtnMain, ImageView floatingBtnIcon, View toolMenu) {
        floatingButtonFilter.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // VERIFICANDO SE HÁ UMA PROJEÇÃO ATIVA NO MOMENTO
                if(ProjectionHolder.getMediaProjection() != null){
                    ProjectionHolder.getMediaProjection().stop();
                    ProjectionHolder.setMediaProjection(null);
                }
                // EXECUTANDO AS ANIMAÇÕES DE ABERTURA E FECHADURA
                Log.d("FloatingTouchButtons", "Projeção encerrada com sucesso.");
                FloatingAnimations.fadeIn(floatingConfirmProjection);
                FloatingAnimations.closeToolFloating(darkBackground,toolMenu,floatingBtnMain,toolMenuRoot,floatingBtnIcon);
                clicked = false;
                // ABRINDO A TELA DE CONFIRMAÇÃO DE PROJEÇÃO
                Button confirm = floatingConfirmProjection.findViewById(R.id.confirm_button);
                confirm.setOnClickListener(c -> {
                    Log.d("FloatingTouchButtons","Permissão concedida");
                    floatingConfirmProjection.setVisibility(View.GONE);

                    // SOLICITANDO A PERMISSÃO DE TRANSMISSÃO DE TELA
                    Intent permissionIntent = new Intent(context, PermissionActivity.class);
                    permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(permissionIntent);

                    /*SISTEMA DE SEGURANÇA PARA ASSEGURAR DE QUE A FUNCIONALIDADE NÃO SEJA EXECUTADA ENQUANTO A PERMISSÃO
                    DE TRANSMISSÃO NÃO FOR CONCEDIDA */
                    Handler handler = new Handler(Looper.getMainLooper());
                    Runnable check = new Runnable() {
                        @Override
                        public void run() {
                            Log.d("FloatingTouchButtons", "Verificando se a permissão foi concedida");
                            Log.d("FloatingTouchButtons", "IsProjectionActive = " + isProjectionActive);
                            // SE A PROJEÇÃO ESTIVER OFICIALMENTE HABILITADA, EXECUTAR A FUNCIONALIDADE
                            if (isProjectionActive) {
                                isProjectionActive = false;
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
                                }
                                else {
                                    Log.e("FloatingTouchButtons", "Permissão SYSTEM_ALERT_WINDOW não concedida.");
                                    Toast.makeText(context, "Permissão de sobreposição não concedida.", Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                handler.postDelayed(this, 100);
                            }
                        }
                    };
                    handler.post(check);
                });

            }
            return false;
        });
    }

    // BOTÕES QUE ABRE A CAPTURA DE TELA COM FILTRO
    @SuppressLint("ClickableViewAccessibility")
    public static void ScreenshotFilterTab(View floatingButtonFilter, View toolmenu, View filterTab) {
        floatingButtonFilter.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("FloatingTouchButtons", "Toque no botão de captura de tela com filtro");
                // EXECUTANDO AS ANIMAÇÕES DE ABERTURA E FECHADURA
                FloatingAnimations.fadeOut(toolmenu);
                FloatingAnimations.fadeBackground(filterTab);
                return true;
            }
            return false;
        });
    }
    // BOTÕES DA CAPTURA DE TELA COM FILTRO
    @SuppressLint("ClickableViewAccessibility")
    public static void filterBtnTouch(View btnProtanopia, View btnDeuteranopia, View btnTritanopia, View filterScreen,View darkBackground,View floatingMain,View floatingButtonFilter,Context context, View screenshotFilter) {
        // ELEMENTO DO RESULTADO DA CAPTURA DE TELA
        View ScreenView = screenshotFilter.findViewById(R.id.resultScreenshot);
        // CAPTURA DE TELA COM FILTRO PARA PROTANOPIA
        btnProtanopia.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Protanopia Clicado, iniciar captura");
            verificationMediaProjection(floatingButtonFilter, context,filterScreen,darkBackground,floatingMain,ScreenView,0);
        });
        // CAPTURA DE TELA COM FILTRO PARA DEUTERANOPIA
        btnDeuteranopia.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Deuteranopia Clicado, iniciar captura");
            verificationMediaProjection(floatingButtonFilter, context,filterScreen,darkBackground,floatingMain,ScreenView,1);
        });
        // CAPTURA DE TELA COM FILTRO PARA TRITANOPIA
        btnTritanopia.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Tritanopia Clicado, iniciar captura");
            verificationMediaProjection(floatingButtonFilter, context,filterScreen,darkBackground,floatingMain,ScreenView,2);
        });
    }

    public static void verificationMediaProjection(View floatingConfirmProjection, Context context, View filterScreen, View darkBackground, View floatingMain, View screenshotFilter, int filtertype) {
        // NÃO resetar ProjectionHolder aqui (evita invalidar a projeção que a PermissionActivity criará)
        FloatingAnimations.fadeIn(floatingConfirmProjection);
        Button confirm = floatingConfirmProjection.findViewById(R.id.confirm_button);
        confirm.setOnClickListener(c -> {

            // Oculta a overlay de confirmação
            floatingConfirmProjection.setVisibility(View.GONE);

            // Inicia a PermissionActivity que solicita a permissão de captura
            Intent permissionIntent = new Intent(context, PermissionActivity.class);
            permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(permissionIntent);

            Handler handler = new Handler(Looper.getMainLooper());
            Runnable check = new Runnable() {
                @Override
                public void run() {
                    Log.d("FloatingTouchButtons", "Verificando se a permissão foi concedida");
                    Log.d("FloatingTouchButtons", "IsProjectionActive = " + isProjectionActive);

                    if (isProjectionActive) {
                        isProjectionActive = false;
                        // Obtém a MediaProjection criada e guardada pela PermissionActivity
                        MediaProjection projection = ProjectionHolder.getMediaProjection();
                        if (projection == null) {
                            Log.e("FloatingTouchButtons", "ProjectionHolder.getMediaProjection() ainda é null mesmo com isProjectionActive=true");
                            return;
                        }

                        // Ajusta UI (esconder overlays) — já feito antes também, mas mantemos para garantir
                        filterScreen.setVisibility(View.GONE);
                        darkBackground.setVisibility(View.GONE);
                        floatingMain.setVisibility(View.GONE);

                        // Pega dimensões e chama a captura
                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int width = metrics.widthPixels;
                        int height = metrics.heightPixels;
                        int density = metrics.densityDpi;

                        // Chama CaptureFrame (ele vai liberar o VirtualDisplay internamente)
                        CaptureScreenShot.CaptureFrame(context, width, height, density, projection,
                                filtertype, screenshotFilter, darkBackground, floatingMain, filterScreen);

                        // Não repostamos o handler: paramos o loop
                    } else{
                        // ainda não autorizado, tenta de novo em 100ms
                        handler.postDelayed(this, 100);
                    }
                }
            };
            handler.post(check);
        });
    }


    public static void returnProjection() {
        Log.d("FloatingTouchButtons", "Permissão autorizada na PermissionActivity pelo usuário");
        isProjectionActive = true;
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

    public static void screenBtnTouch(View saveCaptureFilter, View closeButtonScreenshot, View screenshotFilter,Context context) {
        View ScreenView = screenshotFilter.findViewById(R.id.resultScreenshot);
        saveCaptureFilter.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Protanopia Clicado, iniciar captura");
            ImageView image = ScreenView.findViewById(R.id.screenshot);

            Drawable drawable = image.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            // Salva no armazenamento
            try {
                // Gera um nome único para a imagem
                String fileName = "colored_screenshot_" + System.currentTimeMillis() + ".jpg";

                // Cria o arquivo na pasta Pictures
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), fileName);

                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                // Atualiza a galeria
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                context.sendBroadcast(mediaScanIntent);

                Toast.makeText(context, "Imagem salva em " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e("FloatingTouchButtons","Erro ao salvar imagem");
            }


        });
        closeButtonScreenshot.setOnClickListener(v -> {
            Log.d("FloatingTouchButtons", "Fechando Screenshot");
            ScreenView.setVisibility(View.GONE);
        });
    }
}
