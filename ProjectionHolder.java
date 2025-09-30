package com.example.coloredapp;

import android.media.projection.MediaProjection;

public class ProjectionHolder {
    private static MediaProjection mediaProjection;
    public static void setMediaProjection(MediaProjection mp) {
        mediaProjection = mp;
    }

    public static MediaProjection getMediaProjection() {
        return mediaProjection;
    }
}
