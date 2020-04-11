package com.example.retouchephoto;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static android.graphics.Bitmap.createBitmap;

class ImageTools {


    @SuppressWarnings("SameParameterValue")
    static Bitmap resizeAsContainInARectangle(Bitmap bmp, int maxSize) {
        if (bmp.getHeight() >  bmp.getWidth()) {
            return Bitmap.createScaledBitmap(bmp, bmp.getWidth() * maxSize / bmp.getHeight() , maxSize, true);
        } else {
            return Bitmap.createScaledBitmap(bmp, maxSize, bmp.getHeight() * maxSize / bmp.getWidth(), true);
        }
    }

    @SuppressWarnings({"SuspiciousNameCombination", "SameParameterValue"})
    static Bitmap toSquare(Bitmap bmp, int newSize) {

        int currentWidth = bmp.getWidth();
        int currentHeight =  bmp.getHeight();

        int newWidth = currentWidth;
        int newHeight = currentHeight;

        int newX = 0;
        int newY = 0;

        if (currentWidth > currentHeight) {
            newWidth = currentHeight;
            newX = (currentWidth - currentHeight) / 2;
        } else {
            newHeight = currentWidth;
            newY = (currentHeight - currentWidth) / 2;
        }

        bmp = Bitmap.createBitmap(bmp, newX, newY, newWidth, newHeight);

        // L'image est maintenant un carrée

        return Bitmap.createScaledBitmap(bmp, newSize, newSize, true);

    }

    /**
     *  Generates the histogram and displays it in "histogram"
     */
    static Bitmap generateHistogram(Bitmap bmp) {

        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        int[] Rvalues = new int[256];
        int[] Gvalues = new int[256];
        int[] Bvalues = new int[256];

        // Stores how many occurrences of all color intensities, for each channel.
        for (int pixel : pixels) {
            Rvalues[(pixel >> 16) & 0x000000FF] += 1;
            Gvalues[(pixel >>8 ) & 0x000000FF] += 1;
            Bvalues[(pixel) & 0x000000FF] += 1;
        }

        int max = 0;

        // Finds the intensity (in all three channels) with the maximum number of occurrences.
        for (int i = 0; i < 256; i++) {
            max = Math.max(max, Rvalues[i]);
            max = Math.max(max, Gvalues[i]);
            max = Math.max(max, Bvalues[i]);
        }

        Bitmap hist = createBitmap(256, 200, Bitmap.Config.ARGB_8888);
        int histHeight = hist.getHeight() - 1;
        int histWidth = hist.getWidth();

        int[] histPixels = new int[hist.getHeight() * hist.getWidth()];

        // If the image is blank, return with a black histogram.
        if (max == 0) {
            for (int x = 0; x < histWidth; x++) {
                for (int y = 0; y < histHeight; y++) {
                    histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(0, 0, 0);
                }
            }

        } else {

            int colorR;
            int colorG;
            int colorB;

            for (int x = 0; x < histWidth; x++) {
                for (int y = 0; y < histHeight; y++) {

                    colorR = 0;
                    colorG = 0;
                    colorB = 0;

                    if (Math.sqrt(Rvalues[x] * histHeight / max) * 14 >= y) {colorR = 255;}
                    if (Math.sqrt(Gvalues[x] * histHeight / max) * 14 >= y) {colorG = 255;}
                    if (Math.sqrt(Bvalues[x] * histHeight / max) * 14 >= y) {colorB = 255;}

                    histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(colorR, colorG, colorB);

                }
            }
        }

        hist.setPixels(histPixels, 0, hist.getWidth(), 0, 0, hist.getWidth(), hist.getHeight());
        return hist;
    }


    static void drawRectangle(final Bitmap bmp, Point a, Point b, int color) {
        drawRectangle(bmp, a, b, color, -1);
    }


    static void drawRectangle(final Bitmap bmp, Point a, Point b, int color, int thickness) {
        if (!a.isEquals(b)) {
            Canvas myCanvas = new Canvas(bmp);

            Paint paint = new Paint();
            if (thickness > 0) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Settings.CROP_BORDER_SIZE);
            } else {
                paint.setStyle(Paint.Style.FILL);
            }
            paint.setColor(color);
            myCanvas.drawRect(a.x, a.y, b.x, b.y, paint);
        }
    }

    @SuppressWarnings("unused")
    static void fillWithColor(final Bitmap bmp, int color) {
        drawRectangle(bmp, new Point(0,0), new Point(bmp.getWidth(), bmp.getHeight()), color);
    }

    static Bitmap bitmapClone(Bitmap source) {
        return source.copy(source.getConfig(), true);
    }


}
