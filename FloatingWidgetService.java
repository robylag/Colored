// Pacote principal do app
package com.example.coloredapp;

// Importações de bibliotecas Android e do projeto
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.database.sqlite.SQLiteDatabase;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.coloredapp.db.ColorReader;
import com.example.coloredapp.db.DatabaseHelper;
import com.example.coloredapp.filter.CaptureScreenShot;
import com.example.coloredapp.floatingButtons.*;

import java.util.concurrent.atomic.AtomicBoolean;

// Classe de serviço responsável por exibir os elementos de sobreposição e processar a captura de tela
public class FloatingWidgetService extends Service {
    private WindowManager windowManager;
    private long pressStartTime;
    private static final long LONG_PRESS_THRESHOLD = 300; // Tempo para detectar toque longo
    private final AtomicBoolean isHolding = new AtomicBoolean(false);
    private long pressDuration;
    private boolean moved = false;
    private MediaProjection mediaProjection;

    // Método chamado quando o serviço é iniciado
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Verifica se há uma Intent válida (evita NullPointerException)
        if (intent != null) {
            // Extrai o código de resultado da permissão de captura de tela
            int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
            // Extrai os dados da Intent de captura de tela (usados para criar a MediaProjection)
            Intent data = intent.getParcelableExtra("data");

            // Verifica se a permissão foi negada ou se os dados estão ausentes
            if (resultCode != Activity.RESULT_OK || data == null) {
                Log.e("FloatingWidgetService", "Permissão de MediaProjection não concedida.");
                stopSelf();
                // Retorna para que o sistema não tente recriar o serviço automaticamente
                return START_NOT_STICKY;
            }
            // Inicia o serviço em primeiro plano com uma notificação, necessário no Android 10+ (Q)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForegroundService();
            }

            // Cria uma instância de MediaProjectionManager para obter a MediaProjection
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            // Cria a MediaProjection com os dados recebidos da permissão do usuário
            MediaProjection projection = projectionManager.getMediaProjection(resultCode, data);
            // Armazena a projeção em uma classe auxiliar estática para ser usada em outros lugares do app
            ProjectionHolder.setMediaProjection(projection);
            // Inicializa a interface de usuário flutuante, se a versão do Android for compatível
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                initializeFloatingUI();
            }
        }
        // Retorna START_STICKY para que o sistema tente recriar o serviço se ele for encerrado
        return START_STICKY;
    }


    // Cria um canal de notificação para Android O ou superior
    private void createNotification() {
        // Verifica se a versão do Android suporta canais de notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Cria um canal de notificação para notificações em primeiro plano
            NotificationChannel channel = new NotificationChannel(
                    "media_projection_channel",
                    "Media Projection",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal usado para o serviço de leitura de cor e filtro de tela.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Inicializa o serviço em primeiro plano com uma notificação ativa
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void startForegroundService() {
        createNotification();
        Notification notification = new NotificationCompat.Builder(this, "media_projection_channel")
                .setContentTitle("Recursos de Leitura e Filtro Habilitado")
                .setContentText("O aplicativo COLORED está utlizando do recursos para o funcionamento da leitura de cor e da aplicação de tela, para desabilitar, arraste o botão para a exlusão.")
                .setSmallIcon(R.drawable.app_logo)
                .build();

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
    }

    // Serviço não permite bind
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // FUNÇÃO RESPONSÁVEL PELA INICIALIZAÇÃO DA INTERFACE PRINCIPAL DO APLICATIVO
    @SuppressLint({"RtlHardcoded", "InflateParams", "ClickableViewAccessibility", "ResourceType"})
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initializeFloatingUI() {
        // INSTANCIA DO BANCO DE DADOS PARA LEITURA DE COR
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.openDatabase();

        // INSTANCIA PARA A FINALIZAÇÃO DA PROJEÇÃO
        ProjectionHolder.getMediaProjection().stop();
        ProjectionHolder.setMediaProjection(null);

        // INSTANCIA PARA TODOS OS LAYOUT
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // INFLANDO TODAS AS VIEWS

        // INTERFACE PRINCIPAL E PAINÉIS DE FUNCIONALIDADES
        View floatingBtnDelete = inflater.inflate(R.layout.delete_button_layout, null);                 // Pegando o layout do botão de deletar
        View floatingBtnMain = inflater.inflate(R.layout.main_button_layout, null);                     // Pegando o layout do botão principal
        View filterTab = inflater.inflate(R.layout.filter_tab_layout, null);                            // Pegando o layout da aba de filtro
        View darkBackground = inflater.inflate(R.layout.black_background, null);                        // Pegando o layout do fundo escuro
        View floatingConfirmProjection = inflater.inflate(R.layout.confirm_projection_layout, null);    // Pegando o layout do botão de confirmação de projeção
        View resultColor = inflater.inflate(R.layout.result_color_layout, null);                        // Pegando o layout do resultado da cor

        // LAYOUTS DOS BOTÕES PRINCIPAIS
        ImageView floatingIcon = floatingBtnMain.findViewById(R.id.icon);                                    // Pegando a imagem do botão principal
        View toolMenuRoot = inflater.inflate(R.layout.tool_buttons_layout, null);                       // Pegando o layout do menu de ferramentas
        View floatingBtnTools = toolMenuRoot.findViewById(R.id.toolMenu);                                    // Pegando o layout do menu de ferramentas
        View floatingButtonCamera = toolMenuRoot.findViewById(R.id.floatingButtonCamera);                    // Pegando o layout do botão de câmera
        View floatingButtonFilter = toolMenuRoot.findViewById(R.id.floatingButtonFilter);                    // Pegando o layout do botão de filtro
        View floatingButtonScan = toolMenuRoot.findViewById(R.id.floatingButtonColorPicker);                 // Pegando o layout do botão de leitura de cor
        View floatingButtonMenu = toolMenuRoot.findViewById(R.id.floatingButtonTest);                        // Pegando o layout do botão de menu

        // LAYOUTS DA LEITURA DE COR
        View closeButtonColor = resultColor.findViewById(R.id.closeButton);                                  // Pegando o layout do botão de fechar a cor
        ScannerScopeBright circleBright = new ScannerScopeBright(this);                              // Pegando o layout da mira de leitura de cor

        // LAYOUTS DA PAINEL DE FILTRO DE TELA
        View btnProtanopia = filterTab.findViewById(R.id.filterProtanopia);                                 // Pegando o botão de protanopia
        View btnDeuteranopia = filterTab.findViewById(R.id.filterDeuteranopia);                             // Pegando o botão de Deuteranopia
        View btnTritanopia = filterTab.findViewById(R.id.filterTritanopia);                                 // Pegando o botão de Tritanopia
        View screenshotFilter = inflater.inflate(R.layout.result_screenshot, null);                    // Pegando o layout do resultado da screenshot
        View saveCaptureFilter = screenshotFilter.findViewById(R.id.saveCapture);                           // Pegando o botão de salvar a screenshot
        View closeButtonScreenshot = screenshotFilter.findViewById(R.id.closeCapture);                      // Pegando o botão de fechar a screenshot
        View screenshotBright = inflater.inflate(R.layout.screenshot_bright, null);                        // Pegando o layout da screenshot bright)

        // DEFINIÇÃO DE PARAMETROS DE CADA LAYOUT
        WindowManager.LayoutParams buttonLayoutParams = WindowLayoutParamsPosition.layoutparams();                          // Parametro do botão principal
        WindowManager.LayoutParams deleteLayoutParams = WindowLayoutParamsPosition.delete_layoutparams();                   // Parametro do botão de deletar
        WindowManager.LayoutParams toolLayoutParams = WindowLayoutParamsPosition.toolMenuRootParams();                      // Parametro do menu de ferramentas
        WindowManager.LayoutParams filterTabParams = WindowLayoutParamsPosition.filterTabParams();                          // Parametro da aba de filtro
        WindowManager.LayoutParams circleBrightParams = WindowLayoutParamsPosition.circleBrightParams();                    // Parametro da mira de leitura de cor
        WindowManager.LayoutParams resultColorParams = WindowLayoutParamsPosition.resultColorParams(circleBrightParams);    // Parametro do resultado da cor
        WindowManager.LayoutParams darkBackgroundParams = WindowLayoutParamsPosition.darkBackgroundParams();                // Parametro do fundo escuro
        WindowManager.LayoutParams confirmParams = WindowLayoutParamsPosition.confirmParams();                              // Parametro do botão de confirmação de projeção
        WindowManager.LayoutParams resultScreenshotParams = WindowLayoutParamsPosition.resultScreenshotparams();            // Parametro do resultado da screenshot;
        WindowLayoutParamsPosition.toolMenuPositionEsq(windowManager, floatingBtnTools, toolLayoutParams, floatingBtnMain); // Posição do menu de ferramentas à esquerda
        WindowLayoutParamsPosition.layoutPosition(buttonLayoutParams);                                                      // Posição do botão principal
        WindowLayoutParamsPosition.deletePosition(deleteLayoutParams);                                                      // Posição do botão de deletar
        WindowLayoutParamsPosition.resultColorPosition(circleBrightParams);                                                 // Posição da mira de leitura de cor// Posição da aba de filtro

        // ADIÇÃO DAS VIEWS/LAYOUT A TELA DO USUÁRIO
        windowManager.addView(darkBackground, darkBackgroundParams);                                        // Adiciona o fundo escuro a tela
        WindowLayoutParamsPosition.applyImmersiveMode(darkBackground);
        windowManager.addView(toolMenuRoot, toolLayoutParams);                                              // Adiciona o menu de ferramentas a tela
        windowManager.addView(floatingBtnDelete, deleteLayoutParams);                                       // Adiciona o botão de deletar a tela
        windowManager.addView(floatingBtnMain, buttonLayoutParams);                                         // Adiciona o botão principal a tela
        windowManager.updateViewLayout(floatingBtnDelete, deleteLayoutParams);                              // Atualiza a posição do botão de deletar
        windowManager.addView(resultColor, resultColorParams);                                              // Adiciona o resultado da cor a tela
        windowManager.addView(filterTab, filterTabParams);                                                  // Adiciona a aba de filtro a tela
        windowManager.addView(floatingConfirmProjection, confirmParams);                                    // Adiciona o botão de confirmação de projeção a tela
        windowManager.addView(screenshotFilter, resultScreenshotParams);                                    // Adiciona o resultado da screenshot a tela
        windowManager.addView(screenshotBright, resultScreenshotParams);                                        // Adiciona a mira de leitura de cor a tela

        // LISTENERS DE TOQUE DAS FUNCIONALIDADES PRINCIPAIS

        // TOQUE AO FUNDO ESCURO
        FloatingTouchButtons.darkBackgroundTouch(darkBackground, floatingBtnTools, filterTab, toolMenuRoot, floatingIcon, floatingBtnMain);
        // TOQUE DE COLETA DE COR
        FloatingTouchButtons.colorPickerTouch(this, floatingButtonScan, circleBright, floatingBtnTools, toolMenuRoot, windowManager, circleBrightParams, floatingConfirmProjection, darkBackground, floatingBtnMain, floatingIcon, floatingBtnTools);
        // TOQUE DE FILTRO DE TELA
        FloatingTouchButtons.ScreenshotFilterTab(floatingButtonFilter, floatingBtnTools, filterTab);
        // TOQUE DE SELEÇÃO DO TIPO DE DALTONISMO NO FILTRO DE TELA
        FloatingTouchButtons.filterBtnTouch(btnProtanopia, btnDeuteranopia, btnTritanopia,filterTab,darkBackground,floatingBtnMain,floatingConfirmProjection, this,screenshotFilter);
        // TOQUE DE FECHAR A COR
        FloatingTouchButtons.closeResultColor(closeButtonColor, resultColor);
        // TOQUE DE BOTÕES PRINCIPAIS
        FloatingTouchButtons.toolBtnTouchRedirect(this, floatingButtonCamera, floatingButtonMenu, floatingBtnTools, filterTab, toolMenuRoot, floatingIcon, floatingBtnMain, darkBackground);
        // SALVA O BOTÃO PRINCIPAL
        FloatingHistoric.saveFloatingButtonMain(floatingBtnMain);
        // BOTOES DO SCREENSHOT
        FloatingTouchButtons.screenBtnTouch(saveCaptureFilter,closeButtonScreenshot,screenshotFilter,this);
        CaptureScreenShot.getScreenshotBright(screenshotBright);

        // Move e ativa o botão principal
        floatingBtnMain.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                // Eventos de toque
                case MotionEvent.ACTION_DOWN:
                    // Coleta o tempo pressionado ao botão principal
                    pressStartTime = System.currentTimeMillis();
                    isHolding.set(false);

                    // Coleta a posição do botão principal
                    int xOffset = (int) event.getRawX() - buttonLayoutParams.x;
                    int yOffset = (int) event.getRawY() - buttonLayoutParams.y;
                    v.setTag(new int[]{xOffset, yOffset});
                    break;
                case MotionEvent.ACTION_MOVE:
                    int[] offsets = (int[]) v.getTag();
                    pressDuration = System.currentTimeMillis() - pressStartTime;
                    FloatingTouchButtons.holdingCondition(pressDuration, LONG_PRESS_THRESHOLD, isHolding, floatingBtnMain, floatingBtnDelete);
                    buttonLayoutParams.x = (int) event.getRawX() - offsets[0];
                    buttonLayoutParams.y = (int) event.getRawY() - offsets[1];
                    FloatingPosCondition.checkPos(buttonLayoutParams.x,this,floatingButtonCamera,floatingButtonFilter,floatingButtonScan,floatingButtonMenu,floatingBtnTools,toolLayoutParams,floatingBtnMain,windowManager);
                    windowManager.updateViewLayout(floatingBtnMain, buttonLayoutParams);
                    moved = FloatingTouchButtons.movedCondition(pressDuration, LONG_PRESS_THRESHOLD);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    pressDuration = System.currentTimeMillis() - pressStartTime;
                    FloatingTouchButtons.cancelCondition(pressDuration, LONG_PRESS_THRESHOLD, darkBackground, floatingBtnTools, filterTab, toolMenuRoot, floatingIcon, floatingBtnMain);
                    FloatingRemovingCondition.checkCollisionAndAnimate(floatingBtnMain, floatingBtnDelete, windowManager, buttonLayoutParams, moved,this);
                    break;
            }
            return false;
        });

        // Listener de toque para a mira de leitura de cor
        circleBright.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    circleBright.setHighlightCenter(event.getX(), event.getY());
                    windowManager.updateViewLayout(circleBright, circleBrightParams);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    DisplayMetrics metrics = this.getResources().getDisplayMetrics();
                    int width = metrics.widthPixels;
                    int height = metrics.heightPixels;
                    int density = metrics.densityDpi;
                    Log.d("DisplayMetric", "Width: " + width + ", Height: " + height + ", Density: " + density);
                    circleBright.setVisibility(View.GONE);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        int centerX = (int) circleBright.getCenterX();
                        int centerY = (int) circleBright.getCenterY() + 80;
                        Log.d("DisplayMetric","X: "+centerX+", Y: "+centerY);
                        new Thread(() -> {
                            mediaProjection = ProjectionHolder.getMediaProjection();
                            int[] rgb = ColorReader.readColorAt(mediaProjection, getResources().getDisplayMetrics(), centerX, centerY);
                            if (rgb != null) {
                                new Handler(Looper.getMainLooper()).post(() -> ColorReader.ColorResult(resultColor, rgb[0], rgb[1], rgb[2], db));
                                circleBright.resetPosition();
                            } else {
                                Log.e("ColorReader", "Falha ao obter a cor na posição: (" + centerX + ", " + centerY + ")");
                            }
                        }).start();
                    }, 300);
                    try {
                        windowManager.removeView(circleBright);
                    } catch (Exception ignored) {}
                    break;
            }
            return true;
        });
    }
}
