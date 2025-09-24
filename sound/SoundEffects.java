package com.example.coloredapp.sound;

import android.content.Context;
import android.media.MediaPlayer;
import com.example.coloredapp.R;

public class SoundEffects {
    public static void playCameraShutter(Context context) {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.shutter);
        if (mp != null) {
            mp.setVolume(0.2f, 0.2f); // ajusta o volume (esquerda, direita)
            mp.setOnCompletionListener(MediaPlayer::release); // libera memória
            mp.start();
        }
    }
    public static void playScreenshotShutter(Context context){
        MediaPlayer mp = MediaPlayer.create(context, R.raw.shutter);
        if(mp != null){
            mp.setVolume(0.2f, 0.2f);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }
    }
}
