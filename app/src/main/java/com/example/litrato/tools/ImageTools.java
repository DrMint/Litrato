package com.example.litrato.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.filters.FilterFunction;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/**
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since 2020-19-04
 */

public class ImageTools {

    /**
     * A function to rescale a bitmap and make it fit into a square of side maxsize but keeping its ratio.
     * @param bmp the image
     * @param maxSize the maxsize of the width or the height
     * @return the resized image
     */
    @SuppressWarnings("SameParameterValue")
    public static Bitmap scaleToBeContainedInSquare(Bitmap bmp, int maxSize) {
        if (bmp.getHeight() >  bmp.getWidth()) {
            return Bitmap.createScaledBitmap(bmp, bmp.getWidth() * maxSize / bmp.getHeight() , maxSize, true);
        } else {
            return Bitmap.createScaledBitmap(bmp, maxSize, bmp.getHeight() * maxSize / bmp.getWidth(), true);
        }
    }

    /**
     * A function to scale a bitmap to a desired size.
     * @param bmp the image
     * @param width wanted width
     * @param height wanted height
     * @return new bitmap
     */
    public static Bitmap scale(Bitmap bmp, int width, int height) {
        return Bitmap.createScaledBitmap(bmp, width, height, true);
    }

    /**
     * A function to make a square bitmap.
     * @param bmp the image
     * @param newSize wanted size
     * @return a square bitmap
     */
    @SuppressWarnings({"SuspiciousNameCombination", "SameParameterValue"})
    public static Bitmap toSquare(Bitmap bmp, int newSize) {
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

        return Bitmap.createScaledBitmap(bmp, newSize, newSize, true);
    }

    /**
     * Generates the histogram and displays it in "histogram"
     * @param bmp the image
     * @return its histogram
     */
    public static Bitmap generateHistogram(Bitmap bmp) {

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

        //ImageTools.fillWithColor(hist, Color.argb(Settings.HISTOGRAM_BACKGROUND_TRANSPARENCY, 0, 0, 0));

        // If the image is blank, return with a black histogram.
        if (max != 0) {

            int[] histPixels = new int[hist.getHeight() * hist.getWidth()];

            /*
            int rPos;
            int gPos;
            int bPos;
            for (int x = 0; x < 256; x++) {

                rPos = (int) Math.sqrt(Rvalues[x] * histHeight / max) * 14;
                gPos = (int) Math.sqrt(Gvalues[x] * histHeight / max) * 14;
                bPos = (int) Math.sqrt(Bvalues[x] * histHeight / max) * 14;

                for (int shift = - 5; shift <= 0; shift++) {
                    if (rPos + shift >= 0) histPixels[x + ((histHeight - rPos + shift) * histWidth)] = Color.rgb(255, 0, 0);
                    if (gPos + shift >= 0) histPixels[x + ((histHeight - gPos + shift) * histWidth)] = Color.rgb(0, 255, 0);
                    if (bPos + shift >= 0) histPixels[x + ((histHeight - bPos + shift) * histWidth)] = Color.rgb(0, 0, 255);
                }
            }

             */

            float ratio = (float) histHeight / max;

            for (int x = 0; x < histWidth; x++) {
                for (int y = 1; y < histHeight; y++) {

                    int colorR = 0;
                    int colorG = 0;
                    int colorB = 0;
                    int colorA = Settings.HISTOGRAM_BACKGROUND_TRANSPARENCY;

                    if (Math.sqrt(Rvalues[x] * ratio) * 14 >= y) {colorR = 255; colorA = Settings.HISTOGRAM_FOREGROUND_TRANSPARENCY;}
                    if (Math.sqrt(Gvalues[x] * ratio) * 14 >= y) {colorG = 255; colorA = Settings.HISTOGRAM_FOREGROUND_TRANSPARENCY;}
                    if (Math.sqrt(Bvalues[x] * ratio) * 14 >= y) {colorB = 255; colorA = Settings.HISTOGRAM_FOREGROUND_TRANSPARENCY;}

                    histPixels[x + ((histHeight - y) * histWidth)] = Color.argb(colorA, colorR, colorG, colorB);
                }
            }
            hist.setPixels(histPixels, 0, hist.getWidth(), 0, 0, hist.getWidth(), hist.getHeight());
        }
        return hist;
    }

    /**
     * Draw a filled rectangle on a bitmap.
     * @param bmp the image
     * @param a position of a corner of the rectangle
     * @param b position of the opposite corner
     * @param color the color of the rectangle
     */
    public static void drawRectangle(final Bitmap bmp, Point a, Point b, int color) {
        drawRectangle(bmp, a, b, color, -1);
    }

    /**
     * Draw a rectangle on a bitmap.
     * @param bmp the image
     * @param a position of a corner of the rectangle
     * @param b position of the opposite corner
     * @param color the color of the rectangle
     * @param thickness thickness of the rectangle border
     */
    public static void drawRectangle(final Bitmap bmp, Point a, Point b, int color, int thickness) {
        if (a != null && b!= null) {
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
    }

    @SuppressWarnings("unused")
    public static void fillWithColor(final Bitmap bmp, int color) {
        drawRectangle(bmp, new Point(0,0), new Point(bmp.getWidth(), bmp.getHeight()), color);
    }

    /**
     * Return a clone of a bitmap.
     * @param source the image to be copied
     * @return the copied bitmap
     */
    public static Bitmap bitmapClone(Bitmap source) {
        if (source == null) return null;
        return source.copy(source.getConfig(), true);
    }

    /**
     * Create a bitmap with the wanted width and height.
     * @param width wanted width
     * @param height wanted height
     * @return the bitmap
     */
    public static Bitmap bitmapCreate(int width, int height) {return Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);}

    /**
     * Draw a circle on a bitmap
     * @param bmp the image
     * @param center position of the center of the circle
     * @param radius radius of the circle
     * @param color color of the circle
     */
    public static void drawCircle(final Bitmap bmp, Point center, int radius, int color) {
        if (center != null && radius > 0) {
            final Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            Canvas selection = new Canvas(bmp);
            selection.setBitmap(bmp);
            selection.drawCircle(center.x, center.y, radius, paint);
        }
    }

    //TODO: Not working well yet
    public static void forceRectangleRatio(Bitmap bmp, Point a, Point b) {

        // Negative values are not acceptable.
        if (a == null || b == null) return;
        if (a.x < 0 || a.y < 0 ||b.x < 0 ||b.y < 0) return;

        int rectangleWidth = Math.abs(a.x - b.x);
        int rectangleHeight = Math.abs(a.y - b.y);

        float rectangleRatio = (float) rectangleWidth / rectangleHeight;
        float bmpRatio = (float) bmp.getWidth() / bmp.getHeight();

        boolean rectangleWiderThenBmp = rectangleRatio > bmpRatio;

        if (rectangleWiderThenBmp) {
            if (b.x > a.x) {
                b.x =  a.x + (int) Math.abs((b.y - a.y) * bmpRatio);
            } else {
                b.x =  a.x + (int) - Math.abs((b.y - a.y) * bmpRatio);
            }
        } else {
            if (b.y > a.y) {
                b.y =  a.y + (int) Math.abs((b.x - a.x) / bmpRatio);
            } else {
                b.y =  a.y + (int) - Math.abs((b.x - a.x) / bmpRatio);
            }
        }

    }

    /**
     * Give the hue value from a color.
     * @param color color we want the hue
     * @return its hue
     */
    public static int getHueFromColor(int color){
        float[] hsv = new float[3];
        Color.RGBToHSV(red(color), green(color), blue(color), hsv);
        return (int) hsv[0];
    }

    /**
     * Return the icon at the wanted index.
     * @param context the context
     * @param index the index
     * @return the bitmap
     */
    public static Drawable getThemedIcon(Context context, int index){
        Bitmap bmp = FileInputOutput.getBitmap(context.getResources(), index);
        return getThemedIcon(context, bmp);
    }

    public static Drawable getThemedIcon(Context context, Bitmap icon){

        //TODO: Use invert instead but invert has to deal with transparent images.
        if (!PreferenceManager.getBoolean(context, Preference.DARK_MODE)) {
            FilterFunction.brightness(icon, -2000);
        }

        Drawable drawable = new BitmapDrawable(context.getResources(), icon);
        drawable.setBounds(0, 0, Settings.TOOL_DISPLAYED_SIZE, Settings.TOOL_DISPLAYED_SIZE);
        return drawable;
    }



}
