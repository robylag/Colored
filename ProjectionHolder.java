package com.example.coloredapp;

import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;

public class ProjectionHolder {
    private static MediaProjection mediaProjection;
    private static VirtualDisplay virtualDisplay;

    public static void setMediaProjection(MediaProjection mp) {
        mediaProjection = mp;
    }

    public static MediaProjection getMediaProjection() {
        return mediaProjection;
    }

    public static void setVirtualDisplay(VirtualDisplay display) {
        virtualDisplay = display;
    }

    public static VirtualDisplay getVirtualDisplay() {
        return virtualDisplay;
    }
}
