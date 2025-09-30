package com.example.coloredapp.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.coloredapp.R;
import com.example.coloredapp.floatingButtons.FloatingAnimations;
import com.example.coloredapp.sound.SoundEffects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class CaptureScreenShot {

    private static final String TAG = "CaptureScreenShot";
    @SuppressLint("StaticFieldLeak")
    private static View screenshotBright;

    public static void getScreenshotBright(View btn) {
        screenshotBright = btn;
    }

    public static void CaptureFrame(Context context, int width, int height, int screenDensity,
                                    MediaProjection mediaProjection, int filterType,
                                    View screenshotView, View darkBackground, View floatingMain, View filterScreen) {

        Log.d(TAG, "CaptureFrame chamado");
        Log.d(TAG, "width=" + width + " height=" + height + " density=" + screenDensity);
        Log.d(TAG, "mediaProjection = " + mediaProjection);

        if (mediaProjection == null) {
            Log.e(TAG, "ERRO: mediaProjection está NULL. Não é possível criar VirtualDisplay!");
            return;
        }

        // Cria ImageReader
        final ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        // Registra callback obrigatório
        mediaProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                Log.d(TAG, "MediaProjection parou");
                try {
                    imageReader.close();
                } catch (Exception ignored) {}
            }
        }, new Handler(Looper.getMainLooper()));

        // Cria VirtualDisplay
        final VirtualDisplay virtualDisplay;
        try {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    width,
                    height,
                    screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null,
                    new Handler(Looper.getMainLooper())
            );
            Log.d(TAG, "VirtualDisplay criado: " + virtualDisplay);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar VirtualDisplay", e);
            try { imageReader.close(); } catch (Exception ignored) {}
            return;
        }

        // Listener de captura
        imageReader.setOnImageAvailableListener(reader -> {
            try (Image image = reader.acquireLatestImage()) {
                if (image == null) return;

                Log.d(TAG, "Imagem capturada: " + image.getWidth() + "x" + image.getHeight());

                // Feedback visual e sonoro
                FloatingAnimations.captureFade(screenshotBright, 300);
                SoundEffects.playScreenshotShutter(context);

                // Converte Image -> Bitmap
                Bitmap original = imageToBitmap(image);
                Bitmap filtered = applyDaltonismFilter(original, filterType);

                // Salva bitmap
                String filename = "print_daltonismo.png";
                saveBitmap(context, filtered, filename);

                // Atualiza UI na main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        File folder = new File(context.getFilesDir(), "ColoredScreenshots");
                        File file = new File(folder, filename);
                        if (file.exists()) {
                            Bitmap savedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            ImageView previewImageView = screenshotView.findViewById(R.id.screenshot);
                            if (previewImageView != null) previewImageView.setImageBitmap(savedBitmap);

                            FloatingAnimations.fadeIn(screenshotView);
                            FloatingAnimations.fadeBackground(darkBackground);
                            floatingMain.setVisibility(View.VISIBLE);
                            filterScreen.setVisibility(View.VISIBLE);
                        } else {
                            Log.e(TAG, "Arquivo não encontrado: " + file.getAbsolutePath());
                        }
                    } catch (Exception uiEx) {
                        Log.e(TAG, "Erro ao atualizar UI", uiEx);
                    }
                });

                // Libera bitmaps
                original.recycle();
                filtered.recycle();
                Log.d(TAG, "Bitmaps liberados");

            } catch (Exception e) {
                Log.e(TAG, "Erro em onImageAvailable", e);
            } finally {
                // Limpeza
                try { reader.setOnImageAvailableListener(null, null); } catch (Exception ignored) {}
                try { reader.close(); } catch (Exception ignored) {}
                try { if (virtualDisplay != null) virtualDisplay.release(); } catch (Exception ignored) {}
            }
        }, new Handler(Looper.getMainLooper()));
    }

    // Converte Image -> Bitmap
    private static Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth() + rowPadding / pixelStride,
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer);

        return Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
    }

    // Aplica filtro de daltonismo
    private static Bitmap applyDaltonismFilter(Bitmap src, int filterType) {
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Objects.requireNonNull(src.getConfig()));
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();

        ColorMatrix cm;
        switch (filterType) {
            case 0: // Protanopia
                cm = new ColorMatrix(new float[]{
                        0.567f, 0.433f, 0, 0, 0,
                        0.558f, 0.442f, 0, 0, 0,
                        0, 0.242f, 0.758f, 0, 0,
                        0, 0, 0, 1, 0
                });
                break;
            case 1: // Deuteranopia
                cm = new ColorMatrix(new float[]{
                        0.625f, 0.375f, 0, 0, 0,
                        0.7f, 0.3f, 0, 0, 0,
                        0, 0.3f, 0.7f, 0, 0,
                        0, 0, 0, 1, 0
                });
                break;
            case 2: // Tritanopia
                cm = new ColorMatrix(new float[]{
                        0.95f, 0.05f, 0, 0, 0,
                        0, 0.433f, 0.567f, 0, 0,
                        0, 0.475f, 0.525f, 0, 0,
                        0, 0, 0, 1, 0
                });
                break;
            default: // Sem filtro
                cm = new ColorMatrix();
                break;
        }

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return output;
    }

    // Salva bitmap em pasta interna
    private static void saveBitmap(Context context, Bitmap bitmap, String filename) {
        File folder = new File(context.getFilesDir(), "ColoredScreenshots");
        if(!folder.exists()) folder.mkdirs();

        File file = new File(folder, filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar bitmap", e);
        }
    }
}
