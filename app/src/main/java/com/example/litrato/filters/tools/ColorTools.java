package com.example.litrato.filters.tools;

import android.graphics.Color;


/**
 * This class implements all the functions necessary for conversions between RGB and HSV, among others.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2019-01-08
 */
@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
@Deprecated public class ColorTools {

    /**
     * Converts an HSV color into a RGB color.
     * Calls the hsv2rgb(int H, float S, float V) function.
     * @param color the HSV color to be converted
     * @return the color in RGB.
     */
    @Deprecated public static int hsv2rgb (final float[] color) {
        return hsv2rgb((int) color[0], color[1], color[2]);
    }

    /**
     * Converts an HSV color into a RGB color.
     * @param H the hue of the image (between 0 and 360)
     * @param S the saturation (between 0 and 1)
     * @param V the luminosity (between 0 and 1)
     * @return the color in RGB
     */
    @Deprecated public static int hsv2rgb (int H, float S, float V) {

        if (S < 0) {S = 0;}
        if (S > 1) {S = 1;}
        if (V < 0) {V = 0;}
        if (V > 1) {V = 1;}

        float C = V * S;
        float X = C * (1 - Math.abs((H / 60f) % 2 - 1));
        float m = V - C;

        float R = 0;
        float G = 0;
        float B = 0;

        int tmp = (int) (H / 60f);


        switch (tmp) {
            case 0: R = C; G = X; B = 0; break;
            case 1: R = X; G = C; B = 0; break;
            case 2: R = 0; G = C; B = X; break;
            case 3: R = 0; G = X; B = C; break;
            case 4: R = X; G = 0; B = C; break;
            case 5: R = C; G = 0; B = X; break;
        }

        int r = (int) ((R + m) * 255);
        int g = (int) ((G + m) * 255);
        int b = (int) ((B + m) * 255);

        return Color.rgb(r,g,b);
    }


    /**
     *  Converts an RGB color into a HSV color.
     * @param color the RGB color to be converted
     * @param hsv the float[] in which to store the result
     */
    @Deprecated public static void rgb2hsv (final int color, final float[] hsv) {
        float R = ((color >> 16) & 0x000000FF) / 255f;
        float G = ((color >>8 ) & 0x000000FF) / 255f;
        float B = ((color) & 0x000000FF) / 255f;

        float minRGB = Math.min(R, Math.min(G, B));
        float maxRGB = Math.max(R, Math.max(G, B));

        float deltaRGB = maxRGB - minRGB;

        float H;
        float S;
        float V;

        if (deltaRGB == 0) {
            H = 0;
        } else if (maxRGB == R) {
            H = 60 * ((G - B) / deltaRGB % 6);
        } else if (maxRGB == G) {
            H = 60 * ((B - R) / deltaRGB + 2);
        } else {
            H = 60 * ((R - G) / deltaRGB + 4);
        }

        if (maxRGB == 0) {
            S = 0;
        } else {
            S = deltaRGB / maxRGB;
        }

        V = maxRGB;

        if (H < 0) {
            H += 360;
        }

        hsv[0] = H;
        hsv[1] = S;
        hsv[2] = V;
    }


    /**
     *  Converts an RGB color into a HSV color.
     * @param color the RGB color to be converted
     * @return the hue between 0 and 359
     */
    @Deprecated public static int rgb2h (final int color) {
        float R = ((color >> 16) & 0x000000FF) / 255f;
        float G = ((color >>8 ) & 0x000000FF) / 255f;
        float B = ((color) & 0x000000FF) / 255f;

        float minRGB = Math.min(R, Math.min(G, B));
        float maxRGB = Math.max(R, Math.max(G, B));

        float deltaRGB = maxRGB - minRGB;

        float H;

        if (deltaRGB == 0) {
            H = 0;
        } else if (maxRGB == R) {
            H = 60 * ((G - B) / deltaRGB % 6);
        } else if (maxRGB == G) {
            H = 60 * ((B - R) / deltaRGB + 2);
        } else {
            H = 60 * ((R - G) / deltaRGB + 4);
        }

        if (H < 0) H += 360;

        return (int) H;
    }

    /**
     *  Converts an RGB color into a HSV color.
     * @param color the RGB color to be converted
     * @return the saturation between 0 and 1
     */
    @Deprecated public static float rgb2s (final int color) {
        int R = (color >> 16) & 0x000000FF;
        int G = (color >>8 ) & 0x000000FF;
        int B = (color) & 0x000000FF;

        int minRGB = Math.min(R, Math.min(G, B));
        int maxRGB = Math.max(R, Math.max(G, B));

        int deltaRGB = maxRGB - minRGB;

        return (maxRGB == 0) ? 0 : (float) deltaRGB / maxRGB;
    }

    /**
     *  Converts an RGB color into a HSV color.
     * @param color the RGB color to be converted
     * @return the luminosity between 0 and 1
     */
    @Deprecated public static float rgb2v (final int color) {
        int R = (color >> 16) & 0x000000FF;
        int G = (color >>8 ) & 0x000000FF;
        int B = (color) & 0x000000FF;
        return (float) Math.max(R, Math.max(G, B)) / 255;
    }

}
