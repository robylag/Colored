package com.example.coloredapp.floatingButtons;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;// Animação de FadeIn do botão excluir.
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.example.coloredapp.R;

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

    public static void closeToolFloating(View darkBackground, View toolMenu, View floatingBtnMain, View toolMenuRoot, ImageView floatingBtnIcon) {
        FloatingAnimations.fadeOut(darkBackground);
        FloatingAnimations.fadeOut(toolMenu);
        FloatingAnimations.scaleDown(floatingBtnMain);
        floatingBtnIcon.setImageResource(R.drawable.logo);
        toolMenuRoot.setVisibility(View.GONE);
    }

    public static void plateChange(ImageView plate, int imageResId) {
        // Garante que a view está visível e com opacidade total
        plate.setAlpha(1f);

        // Fade-out
        plate.animate()
                .alpha(0f)
                .setDuration(100) // tempo do fade-out
                .withEndAction(() -> {
                    // Troca a imagem SOMENTE depois do fade-out terminar
                    plate.setImageResource(imageResId);

                    // Fade-in
                    plate.animate()
                            .alpha(1f)
                            .setDuration(300) // tempo do fade-in
                            .start();
                })
                .start();
    }

    public static void captureFade(View btn, int duration) {
        btn.setVisibility(View.VISIBLE);
        btn.setAlpha(0f);

        // Fade In (0 → 1)
        btn.animate()
                .alpha(1f)
                .setDuration(duration)
                .withEndAction(() -> {
                    // Depois do fade in, inicia o fade out
                    btn.animate()
                            .alpha(0f)
                            .setDuration(duration)
                            .withEndAction(() -> btn.setVisibility(View.GONE))
                            .start();
                })
                .start();
    }
}