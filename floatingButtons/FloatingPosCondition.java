package com.example.coloredapp.floatingButtons;

import android.util.DisplayMetrics;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


public class FloatingPosCondition {
    public static void checkPos(int x, Context context, View floatingButtonCamera, View floatingButtonFilter, View floatingButtonScan, View floatingButtonMenu, View floatingBtnTools, WindowManager.LayoutParams toolLayoutParams, View floatingBtnMain, WindowManager windowManager){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        if(x>width/2){
            Log.d("CheckPos","Mudando a posição!");
            floatingButtonCamera.setTranslationX(130f);
            floatingButtonFilter.setTranslationX(0f);
            floatingButtonScan.setTranslationX(0f);
            floatingButtonMenu.setTranslationX(80f);
            WindowLayoutParamsPosition.toolMenuPositionDir(windowManager, floatingBtnTools, toolLayoutParams, floatingBtnMain);
        }
        else{
            floatingButtonCamera.setTranslationX(0f);
            floatingButtonFilter.setTranslationX(150f);
            floatingButtonScan.setTranslationX(150f);
            floatingButtonMenu.setTranslationX(0f);
            WindowLayoutParamsPosition.toolMenuPositionEsq(windowManager, floatingBtnTools, toolLayoutParams, floatingBtnMain);
        }
    }
}
