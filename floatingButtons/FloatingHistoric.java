package com.example.coloredapp.floatingButtons;

import android.view.View;

public class FloatingHistoric {
    static View FloatingButtonMain;
    public static void saveFloatingButtonMain(View view) {
        FloatingButtonMain = view;
    }
    public static void closeFloating() {
        FloatingAnimations.fadeOut(FloatingButtonMain);
    }
    public static void openFloating(){
        FloatingAnimations.fadeIn(FloatingButtonMain);
    }
}
