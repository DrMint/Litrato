package com.example.retouchephoto;

import android.graphics.Bitmap;
import android.graphics.Color;

import static android.graphics.Bitmap.createBitmap;

class ImageTools {

    static Bitmap resizeAsContainInARectangle(Bitmap bmp, int maxSize) {
        if (bmp.getHeight() >  bmp.getWidth()) {
            return Bitmap.createScaledBitmap(bmp, bmp.getWidth() * Settings.IMPORTED_BMP_SIZE / bmp.getHeight() , Settings.IMPORTED_BMP_SIZE, true);
        } else {
            return Bitmap.createScaledBitmap(bmp, Settings.IMPORTED_BMP_SIZE, bmp.getHeight() * Settings.IMPORTED_BMP_SIZE / bmp.getWidth(), true);
        }
    }

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


}
