package com.example.coloredapp.filter;

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

public class CaptureScreenShot {

    private static final String TAG = "CaptureScreenShot";
    private static View screenshotBright;
    public static void getScreenshotBright(View btn){
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

        final ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        final VirtualDisplay virtualDisplay;
        try {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture", width, height, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(), null, null
            );
            Log.d(TAG, "VirtualDisplay criado com sucesso: " + virtualDisplay);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar VirtualDisplay", e);
            // garante que liberamos reader se algo falhar
            try { imageReader.close(); } catch (Exception ex) { /* ignore */ }
            return;
        }

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            boolean captured = false; // flag para evitar captura múltipla

            @Override
            public void onImageAvailable(ImageReader reader) {
                if (captured) return;
                captured = true;

                Log.d(TAG, "onImageAvailable chamado");
                try (Image image = reader.acquireLatestImage()) {
                    if (image != null) {
                        Log.d(TAG, "Imagem capturada: " + image.getWidth() + "x" + image.getHeight());

                        FloatingAnimations.captureFade(screenshotBright,300);
                        SoundEffects.playScreenshotShutter(context);

                        // Converte o frame para Bitmap
                        Bitmap original = imageToBitmap(image);
                        Log.d(TAG, "Bitmap original criado");

                        // Aplica o filtro de daltonismo
                        Bitmap filtered = applyDaltonismFilter(original, filterType);
                        Log.d(TAG, "Filtro aplicado: tipo=" + filterType);

                        // Salva a imagem processada na pasta interna
                        String filename = "print_daltonismo.png";
                        saveBitmap(context, filtered, filename);

                        // Carregamos o bitmap salvo e atualizamos a UI *na main thread*
                        File folder = new File(context.getFilesDir(), "ColoredScreenshots");
                        File file = new File(folder, filename);

                        if (file.exists()) {
                            Bitmap savedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                            // Atualiza UI na main thread
                            new Handler(Looper.getMainLooper()).post(() -> {
                                try {
                                    ImageView previewImageView = screenshotView.findViewById(R.id.screenshot);
                                    if (previewImageView != null) {
                                        previewImageView.setImageBitmap(savedBitmap);
                                    } else {
                                        Log.w(TAG, "previewImageView (R.id.screenshot) é null");
                                    }

                                    // mostra overlays (ou qualquer comportamento UI que desejar)
                                    FloatingAnimations.fadeIn(screenshotView);
                                    FloatingAnimations.fadeBackground(darkBackground);
                                    floatingMain.setVisibility(View.VISIBLE);
                                    filterScreen.setVisibility(View.VISIBLE);

                                    Log.d(TAG, "Imagem carregada e mostrada no ImageView (UI thread)");
                                } catch (Exception uiEx) {
                                    Log.e(TAG, "Erro ao atualizar UI com a imagem", uiEx);
                                }
                            });
                        } else {
                            Log.e(TAG, "Arquivo não encontrado: " + file.getAbsolutePath());
                        }

                        // libera bitmaps temporários
                        original.recycle();
                        filtered.recycle();
                        Log.d(TAG, "Bitmaps temporários liberados");
                    } else {
                        Log.w(TAG, "Nenhuma imagem disponível no ImageReader");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro em onImageAvailable", e);
                } finally {
                    // Limpeza: desregistra listener, fecha ImageReader e VirtualDisplay
                    try {
                        reader.setOnImageAvailableListener(null, null);
                        reader.close();
                    } catch (Exception ex) {
                        Log.w(TAG, "Erro fechando ImageReader", ex);
                    }

                    try {
                        if (virtualDisplay != null) {
                            virtualDisplay.release();
                            Log.d(TAG, "VirtualDisplay liberado");
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "Erro liberando VirtualDisplay", ex);
                    }

                    // opcional: se você quiser parar a MediaProjection quando terminar,
                    // faça: mediaProjection.stop(); ProjectionHolder.setMediaProjection(null);
                    // mas cuidado: só faça isso se não precisar mais da projeção.
                }
            }
        }, null);
    }

    // Converte Image -> Bitmap
    private static Bitmap imageToBitmap(Image image) {
        Log.d(TAG, "Convertendo Image para Bitmap");
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
        Log.d(TAG, "Bitmap criado a partir do buffer");

        return Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
    }

    // Aplica filtro de daltonismo
    private static Bitmap applyDaltonismFilter(Bitmap src, int filterType) {
        Log.d(TAG, "Aplicando filtro tipo=" + filterType);
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();

        ColorMatrix cm;
        switch (filterType) {
            case 0: // Protanopia
                cm = new ColorMatrix(new float[]{
                        0.567f, 0.433f, 0.0f, 0, 0,
                        0.558f, 0.442f, 0.0f, 0, 0,
                        0.0f, 0.242f, 0.758f, 0, 0,
                        0, 0, 0, 1, 0
                });
                break;
            case 1: // Deuteranopia
                cm = new ColorMatrix(new float[]{
                        0.625f, 0.375f, 0.0f, 0, 0,
                        0.7f, 0.3f, 0.0f, 0, 0,
                        0.0f, 0.3f, 0.7f, 0, 0,
                        0, 0, 0, 1, 0
                });
                break;
            case 2: // Tritanopia
                cm = new ColorMatrix(new float[]{
                        0.95f, 0.05f, 0.0f, 0, 0,
                        0.0f, 0.433f, 0.567f, 0, 0,
                        0.0f, 0.475f, 0.525f, 0, 0,
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

    // Salvar em pasta interna do app
    private static void saveBitmap(Context context, Bitmap bitmap, String filename) {
        File folder = new File(context.getFilesDir(), "ColoredScreenshots");
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            Log.d(TAG, "Pasta criada? " + created + " -> " + folder.getAbsolutePath());
        }

        File file = new File(folder, filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            boolean ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d(TAG, "Compressão e salvamento OK? " + ok + " -> " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar bitmap", e);
        }
    }
}
