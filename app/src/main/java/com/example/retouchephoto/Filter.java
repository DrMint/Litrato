package com.example.retouchephoto;

import android.graphics.Bitmap;

import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import android.graphics.Color;

import static android.graphics.Color.red;
import static com.example.retouchephoto.Convolution.*;
import static com.example.retouchephoto.ColorTools.*;
import static com.example.retouchephoto.RenderScriptTools.*;

import androidx.renderscript.Allocation;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.android.retouchephoto.ScriptC_gray;
import com.android.retouchephoto.ScriptC_invert;
import com.android.retouchephoto.ScriptC_posterize;
import com.android.retouchephoto.ScriptC_saturation;
import com.android.retouchephoto.ScriptC_brightness;
import com.android.retouchephoto.ScriptC_threshold;

/*  The Android Studio seems to think my function aren't use in other classes inside
    this package. It would like me to add "protected" to my function which would
    prevent MainActivity to use those filter...
    I will disable this warning for now.
 */
@SuppressWarnings("WeakerAccess")


class Filter {

    int id;
    String name;
    int redirect;
    boolean useRS;

    boolean colorSeekBar;

    boolean seekBar1;
    int seekBar1Min;
    int seekBar1Set;
    int seekBar1Max;
    String seekBar1Unit;

    boolean seekBar2;
    int seekBar2Min;
    int seekBar2Set;
    int seekBar2Max;
    String seekBar2Unit;


    public Filter(int id, String name, int redirect, boolean useRS, boolean colorSeekBar, boolean seekBar1, int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Unit, boolean seekBar2, int seekBar2Min, int seekBar2Set, int seekBar2Max, String seekBar2Unit) {
        this.id = id;
        this.name = name;
        this.redirect = redirect;
        this.useRS = useRS;
        this.colorSeekBar = colorSeekBar;
        this.seekBar1 = seekBar1;
        this.seekBar1Min = seekBar1Min;
        this.seekBar1Set = seekBar1Set;
        this.seekBar1Max = seekBar1Max;
        this.seekBar1Unit = seekBar1Unit;
        this.seekBar2 = seekBar2;
        this.seekBar2Min = seekBar2Min;
        this.seekBar2Set = seekBar2Set;
        this.seekBar2Max = seekBar2Max;
        this.seekBar2Unit = seekBar2Unit;
    }

    public Filter(int id, String name, boolean useRS, boolean colorSeekBar, boolean seekBar1, int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Unit, boolean seekBar2, int seekBar2Min, int seekBar2Set, int seekBar2Max, String seekBar2Unit) {
        this(id, name, 0, useRS, colorSeekBar, seekBar1, seekBar1Min, seekBar1Set, seekBar1Max, seekBar1Unit, seekBar2, seekBar2Min, seekBar2Set, seekBar2Max, seekBar2Unit);
    }

    public Filter(int id, String name, int redirect, boolean useRS, boolean colorSeekBar, boolean seekBar1, int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Unit) {
        this(id, name, redirect, useRS, colorSeekBar, seekBar1, seekBar1Min, seekBar1Set, seekBar1Max, seekBar1Unit, false, 0, 0, 0, "");
    }

    public Filter(int id, String name, boolean useRS, boolean colorSeekBar, boolean seekBar1, int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Unit) {
        this(id, name, 0, useRS, colorSeekBar, seekBar1, seekBar1Min, seekBar1Set, seekBar1Max, seekBar1Unit, false, 0, 0, 0, "");
    }

    public Filter(int id, String name, int redirect, boolean useRS, boolean colorSeekBar) {
        this(id, name, redirect, useRS, colorSeekBar, false, 0, 0, 0, "");
    }

    public Filter(int id, String name, boolean useRS, boolean colorSeekBar) {
        this(id, name, 0, useRS, colorSeekBar, false, 0, 0, 0, "");
    }

    public Filter(int id, String name, int redirect, boolean useRS) {
        this(id, name, redirect, useRS, false);
    }

    public Filter(int id, String name, boolean useRS) {
        this(id, name, 0, useRS);
    }

    public Filter(int id, String name) {
        this(id, name, 0, false);
    }


    /**
     *  A filter that convert the image to grayscale, but keeps a shade of color intact.
     *  @param pixels the pixels of the image
     *  @param deg the hue that must be kept (must be between 0 and 360)
     *  @param colorMargin how large the range of color will be (must be between 0 and 360)
     */
    static void keepOrRemoveAColor(final int[] pixels, int deg, int colorMargin, final boolean keepColor) {

        // Makes sure the values are in acceptable ranges.
        // Note that deg = 0 cause some problem.
        if (deg < 1) deg = 1;
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
    }


    /**
     *  Colorizes the image with a certain hue given in parameters
     *  @param pixels the pixels of the image
     *  @param deg the hue that must will be apply (must be between 0 and 360)
     */
    static void colorize(final int[] pixels, final int deg, final float saturation) {
        for (int i = 0; i < pixels.length; i++) pixels[i] = hsv2rgb(deg, saturation, rgb2v(pixels[i]));
    }


    /**
     *  Change the hue of every pixels with a certain hue given in parameters
     *  @param pixels the pixels of the image
     *  @param deg the hue that must will be apply (must be between 0 and 360)
     */
    static void changeHue(final int[] pixels, final int deg) {
        for (int i = 0; i < pixels.length; i++) pixels[i] = hsv2rgb(deg, rgb2s(pixels[i]), rgb2v(pixels[i]));
    }


    /**
     *  A filter that stretch or compress the current range of luminosity to a new target range.
     *  @param pixels the pixels of the image
     *  @param targetMinLuminosity the luminosity of the darkest pixel after linear stretching (must be between 0f and 1f)
     *  @param targetMaxLuminosity the luminosity of the brightest pixel after linear stretching (must be between 0f and 1f)
     */
    static void linearContrastStretching(final int[] pixels, final float targetMinLuminosity, final float targetMaxLuminosity) {
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
    }


    /**
     *  A filter that increase the contrast by evenly distributing
     *  the intensities on the histogram.
     *  @param pixels the pixels of the image
     */
    static void histogramEqualization(final int[] pixels) {
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
     *  Shift all the hue of all pixels by a certain value.
     *  @param pixels the pixels of the image
     *  @param shift the value to shift the hue with.
     */
    static void hueShift(final int[] pixels, final int shift) {

        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[0] += shift;
            if (hsv[0] < 0) {
                hsv[0] += 360;
            } else if (hsv[0] >= 360) {
                hsv[0] -= 360;
            }
            pixels[i] = hsv2rgb(hsv);
        }
    }

    /**
     *  Apply gaussian then Laplacian filter.
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     */
    static void laplacienEdgeDetection(final int[] pixels, final int imageWidth, final int imageHeight, final int blur) {

        //toGrayRS(pixels, imageWidth, imageHeight);
        gaussianBlur(pixels, imageWidth, imageHeight, blur, true);

        // Convert all RGB values into luminosity
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            pixels[i] = red(pixels[i]);
        }

        int[] kernel = {
                1, 1, 1,
                1, -8, 1,
                1, 1, 1
        };

        int kernelSize = 3;

        convulution2D(pixels, imageWidth, imageHeight, kernel, kernelSize, kernelSize);

        // Saves the new values as colors
        int outputGrey;
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            outputGrey = pixels[i];
            pixels[i] = Color.rgb(outputGrey, outputGrey, outputGrey);
        }
        histogramEqualization(pixels);
    }

    /**
     *  Each pixel becomes the average of size * size pixels around it.
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     *  @param size size of the kernel
     */
    static void averageBlur(final int[] pixels, final int imageWidth, final int imageHeight, final int size) {

        //toGrayRS(pixels, imageWidth, imageHeight);

        final int newSize = size + 1 + size;

        // Convert all RGB values into luminosity
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            pixels[i] = red(pixels[i]);
        }

        convulution2DUniform(pixels, imageWidth, imageHeight, newSize, newSize);

        // Saves the new values as colors
        int outputGrey;
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            outputGrey = pixels[i];
            pixels[i] = Color.rgb(outputGrey, outputGrey, outputGrey);
        }
    }

    /**
     *  Apply a gaussian blur filter. This takes advantage of the Gaussian blur’s separable property by dividing the process into two passes.
     *  In the first pass, a one-dimensional kernel is used to blur the image in only the horizontal or vertical direction.
     *  In the second pass, the same one-dimensional kernel is used to blur in the remaining direction.
     *  The resulting effect is the same as convolving with a two-dimensional kernel in a single pass, but requires fewer calculations.
     *  (Text taken from the Wikipedia article Gaussian blur: https://en.wikipedia.org/wiki/Gaussian_blur)
     *  @param pixels the pixels of the image
     *  @param imageWidth the image's width
     *  @param imageHeight the image's height
     *  @param size size of the kernel
     */
    static void gaussianBlur(final int[] pixels, final int imageWidth, final int imageHeight, final int size, final boolean correctBorders) {

        // Let's calculate the gaussian kernel
        final double sigma = size / 3.0;
        final double tmp = Math.exp(-(size * size / (2 * sigma * sigma)));
        final int floatToIntCoef = (int) (1 / tmp);

        final int[] gaussianKernel = new int[size + 1 + size];
        for (int i = -size; i <= size ; i++) {
            gaussianKernel[i + size] = (int) (Math.exp(-(i * i / (2 * sigma * sigma))) * floatToIntCoef);
        }

        // Convert all RGB values into luminosity
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            pixels[i] = red(pixels[i]);
        }

        // Apply the gaussian kernel to the image, the first time horizontally, then vertically
        convulution1D(pixels, imageWidth, imageHeight, gaussianKernel, true, correctBorders);
        convulution1D(pixels, imageWidth, imageHeight, gaussianKernel, false, correctBorders);

        // Saves the new values as colors
        int outputGrey;
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            outputGrey = pixels[i];
            pixels[i] = Color.rgb(outputGrey, outputGrey, outputGrey);
        }
    }


    /**
     *  A filter that change the saturation of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param saturation the amount of saturation (must be between 0 and +inf)
     */
    static void saturationRS(final Bitmap bmp, final float saturation) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_saturation script = new ScriptC_saturation(rs);

        // Set global variable in RS
        script.set_saturationValue(saturation);

        script.forEach_saturation(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  A filter that convert the image to grayscale.
     *  This filter use RenderScript.
     *  @param bmp the image
     */
    static void toGrayRS(final Bitmap bmp) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_gray script = new ScriptC_gray(rs);
        script.forEach_grayscale(input, output) ;

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }


    /**
     *  A filter that invert the luminosity of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     */
    static void invertRS(final Bitmap bmp) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_invert script = new ScriptC_invert(rs);
        script.forEach_invert(input, output) ;

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  A filter that invert the luminosity of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param exposure the exposure to use (should be between -inf and 255)
     */
    static void brightnessRS(final Bitmap bmp, final float exposure) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_brightness script = new ScriptC_brightness(rs);

        script.invoke_setBright(exposure);
        script.forEach_brightness(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  Reduces the number of discrete luminance values.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param steps numbers of luminance values.
     *  @param toGray if true, also turns the image gray.
     */
    static void posterizeRS(final Bitmap bmp, final int steps, boolean toGray) {

        if (toGray) toGrayRS(bmp);

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_posterize script = new ScriptC_posterize(rs);

        script.invoke_setSteps((short) steps);
        script.forEach_posterize(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }

    /**
     *  A filter that add a gaussian blur to the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param radius size of the blur (must be between 0 and 25)
     */
    static void gaussianRS(final Bitmap bmp, final float radius) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        script.setInput(input);
        script.setRadius(radius);
        script.forEach(output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    protected static void laplacianRS(final Bitmap bmp, final float amount) {

        if (amount > 0) gaussianRS(bmp, amount);

        float v = amount + 1;
        float[] kernel = {
                -v, -v, -v,
                -v, 8 * v, -v,
                -v, -v, -v
        };
        applyConvolution3x3RS(bmp, kernel);
    }


    static void sharpenRS(final Bitmap bmp, final float amount) {
        float[] kernel = {
                0f, -amount, 0f,
                -amount, 1 + 4 * amount, -amount,
                0f, -amount, 0f
        };
        applyConvolution3x3RS(bmp, kernel);
    }

    /**
     *  Reduces the number of discrete luminance values.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param level numbers of luminance values.
     */
    static void thresholdRS(final Bitmap bmp, final float level) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_threshold script = new ScriptC_threshold(rs);

        script.invoke_setLevel(level);
        script.forEach_threshold(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }

}
