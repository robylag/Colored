package com.example.coloredapp.floatingButtons;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;// Animação de FadeIn do botão excluir.
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

public class FloatingAnimations {
    public static void scaleUp(View btn){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 1.2f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);

        scaleX.start();
        scaleY.start();
    }


    public static void fadeIn(View btn){
        btn.setVisibility(View.VISIBLE);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(btn, "alpha", 0f, 1f);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(btn, "translationY", 500f,0f);
        moveY.setDuration(300);
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());
        moveY.start();
        fadeIn.setDuration(500);
        fadeIn.start();
    }

    public static void fadeBackground(View btn){
        btn.setVisibility(View.VISIBLE);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(btn, "alpha", 0f, 1f);
        fadeIn.setDuration(200);
        fadeIn.start();
    }
    public static void fadeOut(View btn){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(btn,"alpha",1f, 0f);
        fadeOut.setDuration(200);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                btn.setVisibility(View.GONE);
            }
        });
        fadeOut.start();
    }

    public static void scaleDown(View btn){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn,"scaleX",1.2f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn,"scaleY",1.2f,1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();
    }

    public static void fadeOutMove(View btn){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(btn,"alpha",1f, 0f);
        fadeOut.setDuration(500);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(btn, "translationY", 0f,500f);
        moveY.setDuration(300);
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());
        moveY.start();

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                btn.setVisibility(View.GONE);
            }
        });
        fadeOut.start();
    }

    public static void fadeOutScope(View btn, WindowManager windowManager){
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(btn,"alpha",1f, 0f);
        fadeOut.setDuration(500);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    if (btn.isAttachedToWindow()) {
                        windowManager.removeView(btn);
                    }
                } catch (Exception ignored) {}
            }
        });
        fadeOut.start();
    }
}