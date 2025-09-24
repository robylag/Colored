package com.example.coloredapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
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
                Log.e("NotificationColored", "Permissão de MediaProjection não concedida.");
                stopSelf();
                // Retorna para que o sistema não tente recriar o serviço automaticamente
                return START_NOT_STICKY;
            }
            // Inicia o serviço em primeiro plano com uma notificação, necessário no Android 10+ (Q)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d("NotificationColored","Iniciando NotificationService");
                startForegroundService();
            }

            // Cria uma instância de MediaProjectionManager para obter a MediaProjection
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            // Cria a MediaProjection com os dados recebidos da permissão do usuário
            MediaProjection projection = projectionManager.getMediaProjection(resultCode, data);
            // Armazena a projeção em uma classe auxiliar estática para ser usada em outros lugares do app
            ProjectionHolder.setMediaProjection(projection);
        }
        // Retorna START_STICKY para que o sistema tente recriar o serviço se ele for encerrado
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
}
