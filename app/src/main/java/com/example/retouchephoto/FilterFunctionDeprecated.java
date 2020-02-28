package com.example.retouchephoto;

import android.graphics.Bitmap;

import static com.example.retouchephoto.ColorTools.hsv2rgb;
import static com.example.retouchephoto.ColorTools.rgb2hsv;
import static com.example.retouchephoto.ColorTools.rgb2s;
import static com.example.retouchephoto.ColorTools.rgb2v;

/**
 * This class contains java version of the filters
 */
class FilterFunctionDeprecated {
    /**
     *  A filter that convert the image to grayscale, but keeps a shade of color intact.
     *  @param bmp the image
     *  @param deg the hue that must be kept (must be between 0 and 360)
     *  @param colorMargin how large the range of color will be (must be between 0 and 360)
     *  @param keepColor indicates if the color must be kept or removed
     */
   @Deprecated static void keepOrRemoveAColor(final Bitmap bmp, int deg, int colorMargin, final boolean keepColor) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        // Makes sure the values are in acceptable ranges.
        // Note that deg = 0 cause some problem.
        if (deg < 0) deg = 0;
        if (deg >= 360) deg = 0;

        if (colorMargin < 0) colorMargin = 0;
        if (colorMargin > 360) colorMargin = 360;


        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        // Create a look up table for the distance in degrees between each possible hue and deg
        int[] lut = new int[360];
        int increment;

        if (deg > 180) {
            lut[0] = 360 - deg;
            increment = 1;
        } else if (deg == 0) {
            lut[0] = 0;
            increment = 1;
        } else {
            lut[0] = deg;
            increment = -1;
        }

        for (int i = 1; i < 360; i++) {
            lut[i] = lut[i - 1] + increment;
            if (lut[i] == 180 || lut[i] == 0) {
                increment = -increment;
            }
        }

        // For each pixel, change the saturation depending on the distance of the hue with deg.
        if (keepColor) {
            for (int i = 0; i < pixelsLength; i++) {
                rgb2hsv(pixels[i], hsv);
                hsv[1] = Math.min(hsv[1], 1f - 1f / colorMargin * lut[(int) hsv[0]]);
                pixels[i] = hsv2rgb(hsv);
            }
        } else {
            for (int i = 0; i < pixelsLength; i++) {
                rgb2hsv(pixels[i], hsv);
                hsv[1] = Math.min(hsv[1], 1f / colorMargin * lut[(int) hsv[0]]);
                pixels[i] = hsv2rgb(hsv);
            }
        }

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }


    /**
     *  Colorizes the image with a certain hue given in parameters
     *  @param bmp the image
     *  @param deg the hue that must will be apply (must be between 0 and 360)
     */
    @Deprecated static void colorize(final Bitmap bmp, final int deg, final float saturation) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < pixels.length; i++) pixels[i] = hsv2rgb(deg, saturation, rgb2v(pixels[i]));
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }


    /**
     *  Change the hue of every pixels with a certain hue given in parameters
     *  @param bmp the image
     *  @param deg the hue that must will be apply (must be between 0 and 360)
     */
    @Deprecated static void changeHue(final Bitmap bmp, final int deg) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < pixels.length; i++) pixels[i] = hsv2rgb(deg, rgb2s(pixels[i]), rgb2v(pixels[i]));
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }


    /**
     *  A filter that stretch or compress the current range of luminosity to a new target range.
     *  @param bmp the image
     *  @param targetMinLuminosity the luminosity of the darkest pixel after linear stretching (must be between 0f and 1f)
     *  @param targetMaxLuminosity the luminosity of the brightest pixel after linear stretching (must be between 0f and 1f)
     */
    @Deprecated static void linearContrastStretching(final Bitmap bmp, final float targetMinLuminosity, final float targetMaxLuminosity) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        float minLuminosity = 1;
        float maxLuminosity = 0;

        int pixelsLength = pixels.length;

        for (int pixel : pixels) {
            minLuminosity = Math.min(minLuminosity, rgb2v(pixel));
            maxLuminosity = Math.max(maxLuminosity, rgb2v(pixel));
        }

        float stretching = (targetMaxLuminosity - targetMinLuminosity);
        if (maxLuminosity == minLuminosity) {
            stretching /= 255;
        } else {
            stretching /= (maxLuminosity - minLuminosity);
        }

        float[] lut = new float[256];
        for(int i = 0; i < 256; i++){
            lut[i] = ((float) 1 / 256 * i - minLuminosity) * stretching + targetMinLuminosity;
        }

        float[] hsv = new float[3];
        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[2] = lut[(int) (hsv[2] * 255)];
            pixels[i] = hsv2rgb(hsv);
        }

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }


    /**
     *  A filter that increase the contrast by evenly distributing
     *  the intensities on the histogram.
     *  @param bmp the image
     */
    @Deprecated static void histogramEqualization(final Bitmap bmp) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        int pixelsLength = pixels.length;
        int[] histogram = new int[256];

        for (int pixel : pixels) {
            histogram[(int) (rgb2v(pixel) * 255)]++;
        }

        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for(int i = 1; i < 256; i++){
            cdf[i] = cdf[i-1] + histogram[i];
        }

        float[] lut = new float[256];
        for(int i = 0; i < 256; i++){
            lut[i] = cdf[i] / (float) pixelsLength;
        }

        float[] hsv = new float[3];
        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[2] = lut[(int) (hsv[2] * 255)];
            pixels[i] = hsv2rgb(hsv);
        }

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }

}
