package com.example.coloredapp.db;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coloredapp.ProjectionHolder;
import com.example.coloredapp.R;
import com.example.coloredapp.floatingButtons.FloatingAnimations;

import java.nio.ByteBuffer;

public class ColorReader {
    public static int[] readColorAt(MediaProjection mediaProjection, DisplayMetrics metrics, int x, int y) {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        Log.d("DisplayMetric", "Width: " + width + ", Height: " + height + ", Density: " + density);

        if (mediaProjection != null) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    Log.d("ColorReader", "MediaProjection stopped.");
                }
            }, mainHandler);
        }

        ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        assert mediaProjection != null;
        VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width,
                height,
                density,
                0,
                imageReader.getSurface(),
                null,
                null
        );

        try {
            Thread.sleep(150); // tempo para garantir que a imagem foi capturada
        } catch (InterruptedException e) {
            Log.e("ColorReader","Erro ao esperar a captura da imagem");
        }

        Image image = imageReader.acquireLatestImage();
        if (image != null) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();

            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            Bitmap bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
            );
            bitmap.copyPixelsFromBuffer(buffer);

            Log.d("DisplayMetric", "Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            Log.d("DisplayMetric", "Requested pixel at: x=" + x + ", y=" + y);

            // Garante que os valores estão dentro dos limites do bitmap
            x = Math.max(0, Math.min(x, bitmap.getWidth() - 1));
            y = Math.max(0, Math.min(y, bitmap.getHeight() - 1));

            int pixel = bitmap.getPixel(x, y);

            image.close();
            bitmap.recycle();
            virtualDisplay.release();

            return new int[] {
                    Color.red(pixel),
                    Color.green(pixel),
                    Color.blue(pixel)
            };
        }
        return null;
    }

    public static void ColorResult(View resultColor, int r, int g, int b, SQLiteDatabase db) {
        ProjectionHolder.getMediaProjection().stop();
        ProjectionHolder.setMediaProjection(null);
        Log.d("ColorDebug", "ColorResult called with r=" + r + ", g=" + g + ", b=" + b);
        String name = ColorNameResult.getName(r,g,b,db);
        String nameCategory = ColorNameResult.getNameCategory(db);
        Log.d("ColorReader", "Color name: " + name + " RGB: "+r+","+g+","+b);

        FloatingAnimations.fadeBackground(resultColor);

        // Atualiza o círculo de cor
        View circle = resultColor.findViewById(R.id.colorCircle);
        circle.getBackground().setTint(Color.rgb(r, g, b));


        TextView nome = resultColor.findViewById(R.id.colorName);
        TextView categoria = resultColor.findViewById(R.id.colorCategory);
        nome.setText(name);
        categoria.setText(nameCategory);

        // Valor HEX
        TextView hexValue = resultColor.findViewById(R.id.hexValue);
        String hex = String.format("#%02X%02X%02X", r, g, b);
        hexValue.setText(hex);

        // Copiar RGB
        Button copyRgb = resultColor.findViewById(R.id.copyRgbButton);
        String rgb = "RGB(" + r + ", " + g + ", " + b + ")";
        copyRgb.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) resultColor.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("RGB Color", rgb);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(resultColor.getContext(), "RGB copiado!", Toast.LENGTH_SHORT).show();
        });
    }


}
