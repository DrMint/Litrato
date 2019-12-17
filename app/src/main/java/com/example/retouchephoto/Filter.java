package com.example.retouchephoto;

import android.graphics.Bitmap;
import androidx.renderscript.RenderScript;
import android.graphics.Color;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import androidx.renderscript.Allocation;

import com.android.retouchephoto.ScriptC_gray;
import com.android.retouchephoto.ScriptC_invert;

public class Filter {

    /**
     *  A filter that convert the image to grayscale.
     *  This filter use RenderScript.
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     */
    static public void toGrayRS(int[] pixels, int imageWidth, int imageHeight) {

        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_gray grayScript = new ScriptC_gray(rs);
        grayScript.forEach_gray(input, output) ;

        output.copyTo(bmp);

        input.destroy(); output.destroy();
        grayScript.destroy() ; rs.destroy();

        bmp.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

    }


    /**
     *  A filter that invert the luminosity of the image.
     *  This filter use RenderScript.
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     */
    static public void invertRS(int[] pixels, int imageWidth, int imageHeight) {

        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_invert invertScript = new ScriptC_invert(rs);
        invertScript.forEach_invert(input, output) ;

        output.copyTo(bmp);

        input.destroy(); output.destroy();
        invertScript.destroy() ; rs.destroy();

        bmp.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

    }


    /**
     *  A filter that convert the image to grayscale, but keeps a shade of color intact.
     *  @param pixels the pixels of the image
     *  @param deg the hue that must be kept (must be between 0 and 360)
     *  @param colorMargin how large the range of color will be (must be between 0 and 360)
     */
    static public void keepAColor(int[] pixels, int deg, int colorMargin) {

        deg = 180 - deg;
        int diff;
        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            diff = (int) Math.abs(((hsv[0] + deg) % 360) - 180);
            hsv[1] = Math.min(hsv[1], 1f - 1f / colorMargin * diff);
            pixels[i] = hsv2rgb(hsv);
        }
    }


    /**
     *  A filter that convert the image to grayscale, but keeps a shade of color intact.
     *  @param pixels the pixels of the image
     *  @param deg the hue that must be kept (must be between 0 and 360)
     */
    static public void keepAColor(int[] pixels, int deg) {
        // I used 50 because it seems to be a good default value for the colorMargin.
        keepAColor(pixels, deg, 50);
    }


    /**
     *  Colorizes the image with a certain hue given in parameters
     *  @param pixels the pixels of the image
     *  @param deg the hue that must will be apply (must be between 0 and 360)
     */
    static public void colorize(int[] pixels, int deg) {

        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[0] = deg;
            hsv[1] = 1;
            pixels[i] = hsv2rgb(hsv);
        }
    }


    /**
     *  Change the hue of every pixels with a certain hue given in parameters
     *  @param pixels the pixels of the image
     *  @param deg the hue that must will be apply (must be between 0 and 360)
     */
    static public void changeHue(int[] pixels, int deg) {

        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[0] = deg;
            pixels[i] = hsv2rgb(hsv);
        }
    }


    /**
     *  A filter that invert the luminosity of the image, but keeps the hue intact.
     *  This filter use RenderScript.
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     */
    static public void invertLuminosity(int[] pixels, int imageWidth, int imageHeight) {
        // First, invert the image
        invertRS(pixels, imageWidth, imageHeight);

        //Then, for each pixel, apply a 180deg turn to the hue.
        float[] hsv = new float[3];
        int pixelsLength = pixels.length;
        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[0] = (180 + hsv[0]) % 360;
            pixels[i] = hsv2rgb(hsv);
        }
    }


    /**
     *  A filter that increase the contrast by making sure the intensities
     *  of the pixels are spread in between 0 and 255.
     *  @param pixels the pixels of the image
     */
    static public void linearContrastStretching(int[] pixels) {

        float minLuminosity = 1;
        float maxLuminosity = 0;
        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        for (int pixel : pixels) {
            rgb2hsv(pixel, hsv);
            minLuminosity = Math.min(minLuminosity, hsv[2]);
            maxLuminosity = Math.max(maxLuminosity, hsv[2]);
        }

        float diff;
        if (maxLuminosity == minLuminosity) {
            diff = 1;
        } else {
            diff = 1 / (maxLuminosity - minLuminosity);
        }

        float[] lut = new float[256];
        for(int i = 0; i < 256; i++){
            lut[i] = ((float) 1 / 256 * i - minLuminosity) * diff;
        }


        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[2] = lut[(int) (hsv[2] * 255)];
            pixels[i] = hsv2rgb(hsv);
        }
    }


    /**
     *  A filter that increase the contrast by evenly distributing
     *  the intensities on the histogram.
     *  @param pixels the pixels of the image
     */
    static public void histogramEqualization(int[] pixels) {
        int pixelsLength = pixels.length;
        int[] histogram = new int[256];
        float[] hsv = new float[3];

        for (int pixel : pixels) {
            rgb2hsv(pixel, hsv);
            histogram[(int) (hsv[2] * 255)]++;
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

        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[2] = lut[(int) (hsv[2] * 255)];
            pixels[i] = hsv2rgb(hsv);
        }

    }


    /**
     *  A filter used in debugging to test the rgb2hsv and hsv2rgb functions.
     */
    static public void testHSVRGB(int[] pixels) {
        float[] hsv = new float[3];
        int pixelsLength = pixels.length;
        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            pixels[i] = hsv2rgb(hsv);
        }
    }


    /**
     *  Converts an HSV color into a RGB color.
     * @param color the HSV color to be converted
     * @return the color in RGB
     */
    static public int hsv2rgb (float[] color) {
        float H = color[0];
        float S = color[1];
        float V = color[2];

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
    static public void rgb2hsv (int color, float[] hsv) {


        float R = (float) red(color) / 255;
        float G = (float) green(color) / 255;
        float B = (float) blue(color) / 255;

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

}
