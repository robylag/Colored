package com.example.coloredapp.floatingButtons;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Context;


import com.example.coloredapp.ProjectionHolder;

public class FloatingRemovingCondition {

    public static void checkCollisionAndAnimate(final View floatingButton, final View deleteButton, final WindowManager windowManager, final WindowManager.LayoutParams layoutParams, final boolean moved,Context context) {
        if(moved){
            deleteButton.post(() -> {
                // Garantir que o deleteButton tem as dimensões corretas após ser desenhado
                int delBtnWidth = deleteButton.getWidth();
                int delBtnHeight = deleteButton.getHeight();
                int[] deleteButtonLocation = new int[2];
                deleteButton.getLocationOnScreen(deleteButtonLocation);

                // Obter as coordenadas do deleteButton
                int delBtn_x = deleteButtonLocation[0];
                int delBtn_y = deleteButtonLocation[1];

                // Obter as coordenadas e tamanho do floatingButton
                int Btn_x = layoutParams.x;
                int Btn_y = layoutParams.y;
                int Btn_w = floatingButton.getWidth(); // Usando getWidth() para largura
                int Btn_h = floatingButton.getHeight(); // Usando getHeight() para altura

                // Verificação se o floatingButton está sobre o deleteButton
                if (Btn_x + Btn_w > delBtn_x && Btn_x < delBtn_x + delBtnWidth &&
                        Btn_y + Btn_h > delBtn_y && Btn_y < delBtn_y + delBtnHeight) {
                    // Remover o floatingButton se estiver sobre o deleteButton
                    windowManager.removeView(floatingButton);
                    Toast.makeText(context, "Encerrando COLORED, obrigado por utilizar!", Toast.LENGTH_SHORT).show();
                    Log.d("FloatingWidgetService", "excluído!");
                    Log.d("FloatingWidgetService", "Btn " + Btn_x + " " + Btn_y + " " + Btn_h + " " + Btn_w);
                    Log.d("FloatingWidgetService", "delBtn " + delBtn_x + " " + delBtn_y + " " + delBtnHeight + " " + delBtnWidth);
                    Log.d("FloatingWidgetService", "Pop-up fechado, botão delete acionado.");
                    android.os.Process.killProcess(android.os.Process.myPid());
                    ProjectionHolder.getMediaProjection().stop();
                    System.exit(1);

                }
                else {
                    Log.d("FloatingWidgetService", "Não excluído!");
                    Log.d("FloatingWidgetService", "Btn " + Btn_x + " " + Btn_y + " " + Btn_h + " " + Btn_w);
                    Log.d("FloatingWidgetService", "delBtn " + delBtn_x + " " + delBtn_y + " " + delBtnHeight + " " + delBtnWidth);
                }

                // Aplicando animações
                FloatingAnimations.scaleDown(floatingButton);
                FloatingAnimations.fadeOutMove(deleteButton);

                // Verificar a visibilidade do deleteButton
                if (deleteButton.getVisibility() == View.GONE) {
                    Log.d("Teste de Visibilidade", "Botão Desativado");
                } else {
                    Log.d("Teste de Visibilidade", "Botão não Desativado");
                }
            });
        }
    }
}
