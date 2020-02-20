package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static com.example.retouchephoto.ConvolutionTools.*;
import static com.example.retouchephoto.ColorTools.*;
import static com.example.retouchephoto.RenderScriptTools.*;
import static java.lang.Math.abs;

import androidx.renderscript.Allocation;

import com.android.retouchephoto.ScriptC_addNoise;
import com.android.retouchephoto.ScriptC_gray;
import com.android.retouchephoto.ScriptC_invert;
import com.android.retouchephoto.ScriptC_posterizing;
import com.android.retouchephoto.ScriptC_rgbWeights;
import com.android.retouchephoto.ScriptC_saturation;
import com.android.retouchephoto.ScriptC_brightness;
import com.android.retouchephoto.ScriptC_threshold;
import com.android.retouchephoto.ScriptC_colorize;
import com.android.retouchephoto.ScriptC_keepAColor;
import com.android.retouchephoto.ScriptC_histogram;
import com.android.retouchephoto.ScriptC_constrastExtension;
import com.android.retouchephoto.ScriptC_convolution;

/**
 * This class implements all the filter function.
 * All filters should have the following signature:
 * static void FilterName(final Bitmap bmp, final Context context, ... other parameters that can influence the result)
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-02-08
 */
class FilterFunction {

    /**
     *  A filter that convert the image to grayscale, but keeps a shade of color intact.
     *  @param bmp the image
     *  @param deg the hue that must be kept (must be between 0 and 360)
     *  @param colorMargin how large the range of color will be (must be between 0 and 360)
     *  @param keepColor indicates if the color must be kept or removed
     */
    static void keepOrRemoveAColor(final Bitmap bmp, int deg, int colorMargin, final boolean keepColor) {
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
    static void colorize(final Bitmap bmp, final int deg, final float saturation) {
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
    static void changeHue(final Bitmap bmp, final int deg) {
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
    static void linearContrastStretching(final Bitmap bmp, final float targetMinLuminosity, final float targetMaxLuminosity) {
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
    static void histogramEqualization(final Bitmap bmp) {
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

    /**
     *  Shift all the hue of all pixels by a certain value.
     *  @param bmp the image
     *  @param shift the value to shift the hue with.
     */
    static void hueShift(final Bitmap bmp, final int shift) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        float[] hsv = new float[3];
        int pixelsLength = pixels.length;

        int[] lut = new int[360];

        for (int i = 0; i < lut.length; i++) {
            lut[i] = i + shift;
            if (lut[i] < 0) {
                lut[i] += 360;
            } else if (lut[i] >= 360) {
                lut[i] -= 360;
            }
        }

        for (int i = 0; i < pixelsLength; i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[0] = lut[(int) hsv[0]];
            pixels[i] = hsv2rgb(hsv);
        }

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }

    /**
     *  Each pixel becomes the average of size * size pixels around it.
     *  @param bmp the image
     *  @param size size of the kernel
     */
    static void averageBlur(final Bitmap bmp, final int size) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        final int newSize = size + 1 + size;

        // Convert all RGB values into luminosity
        for (int i = 0; i < bmp.getWidth() * bmp.getHeight(); i++) {
            pixels[i] = (pixels[i]) & 0x000000FF;
        }

        convolution2DUniform(pixels, bmp.getWidth(), bmp.getHeight(), newSize, newSize);
        convertGreyToColor(pixels);

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }

    /**
     *  Apply a gaussian blur filter. This takes advantage of the Gaussian blur’s separable property by dividing the process into two passes.
     *  In the first pass, a one-dimensional kernel is used to blur the image in only the horizontal or vertical direction.
     *  In the second pass, the same one-dimensional kernel is used to blur in the remaining direction.
     *  The resulting effect is the same as convolving with a two-dimensional kernel in a single pass, but requires fewer calculations.
     *  (Text taken from the Wikipedia article Gaussian blur: https://en.wikipedia.org/wiki/Gaussian_blur)
     *  @param bmp the pixels of the image
     *  @param size size of the kernel
     *  @param correctBorders corrects the borders if true, otherwise doesn't
     *  @deprecated
     */
    static void gaussianBlur(final Bitmap bmp, final int size, final boolean correctBorders) {

        if (size < 1) return;

        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        // Let's calculate the gaussian kernel
        final double sigma = size / 3.0;
        final double tmp = Math.exp(-(size * size / (2 * sigma * sigma)));
        final int floatToIntCoef = (int) (1 / tmp);

        final int[] gaussianKernel = new int[size + 1 + size];
        for (int i = -size; i <= size ; i++) {
            gaussianKernel[i + size] = (int) (Math.exp(-(i * i / (2 * sigma * sigma))) * floatToIntCoef);
        }

        // Convert all RGB values into luminosity
        for (int i = 0; i < bmp.getWidth() * bmp.getWidth(); i++) {
            // equivalent to pixels[i] = Red(pixels[i]);
            pixels[i] = (pixels[i]) & 0x000000FF;
        }

        // Apply the gaussian kernel to the image, the first time horizontally, then vertically
        convolution1D(pixels, bmp.getWidth(), bmp.getWidth(), gaussianKernel, true, correctBorders);
        convolution1D(pixels, bmp.getWidth(), bmp.getWidth(), gaussianKernel, false, correctBorders);
        convertGreyToColor(pixels);

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }

    /**
     *  A filter that change the saturation of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param saturation the amount of saturation (must be between 0 and +inf)
     */
    static void saturationRS(final Bitmap bmp, final Context context, final float saturation) {

        RenderScript rs = RenderScript.create(context);
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
    private static void toGrayRS(final Bitmap bmp, final Context context) {

        RenderScript rs = RenderScript.create(context);
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
    static void invertRS(final Bitmap bmp, final Context context) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_invert script = new ScriptC_invert(rs);
        script.forEach_invert(input, output) ;

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  A filter that change the luminosity of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param exposure the exposure to use (should be between -inf and 255)
     */
    static void brightnessRS(final Bitmap bmp, final Context context, final float exposure) {

        RenderScript rs = RenderScript.create(context);
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
    static void posterizeRS(final Bitmap bmp, final Context context, final int steps, boolean toGray) {

        if (toGray) toGrayRS(bmp, context);

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_posterizing script = new ScriptC_posterizing(rs);

        script.invoke_setSteps((short) steps);
        script.forEach_posterize(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }

    /**
     *  Reduces the number of discrete luminance values.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param level numbers of luminance values.
     */
    static void thresholdRS(final Bitmap bmp, final Context context, final float level) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_threshold script = new ScriptC_threshold(rs);

        script.invoke_setLevel(level);
        script.forEach_threshold(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  Makes the image warmer or colder.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param level how powerful is the effect.
     */
    static void temperatureRS(final Bitmap bmp, final Context context, final float level) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_rgbWeights script = new ScriptC_rgbWeights(rs);

        // Slightly different weights for warming and colling filter.
        if (level >= 0) {
            script.invoke_setWeights(4 * level, 1 * level, -5 * level);
        } else {
            script.invoke_setWeights(2 * level, 1 * level, -3 * level);
        }

        script.forEach_applyWeights(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  Change the tint of the image.
     *  The tint is a slight green or magenta coloration.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param level how much tint to apply.
     */
    static void tintRS(final Bitmap bmp, final Context context, final float level) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_rgbWeights script = new ScriptC_rgbWeights(rs);

        script.invoke_setWeights(2 * level, -4 * level, 2 * level);
        script.forEach_applyWeights(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  Adds noise to the image.
     *  Noise is created by adding to each channel a random number between [-level; level].
     *  If colorNoise is true, different random number are used for each channel.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param level numbers of luminance values.
     *  @param colorNoise turns the noise colored.
     */
    static void noiseRS(final Bitmap bmp, final Context context, final int level, final boolean colorNoise) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_addNoise script = new ScriptC_addNoise(rs);

        script.invoke_setIntensity(level);

        if (colorNoise) {
            script.forEach_applyNoise(input, output);
        } else {
            script.forEach_applyNoiseBW(input, output);
        }


        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }


    static void colorizeRS(final Bitmap bmp, final Context context, final int deg, final float saturation, boolean changeSaturation) {
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_colorize colorizeScript = new ScriptC_colorize(rs);
        colorizeScript.set_t(deg);
        if (changeSaturation) {
            colorizeScript.set_saturation(deg);
        }
        colorizeScript.forEach_colorize(input, output);
        output.copyTo(bmp);
        cleanRenderScript(colorizeScript, rs, input, output);
    }


    static void keepAColorRS(final Bitmap bmp, final Context context, final int deg) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_keepAColor script = new ScriptC_keepAColor(rs);
        script.set_keep(true);
        script.set_choosedColor((float) deg);
        script.forEach_keepAColor(input, output);
        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }
    static void removeAColorRS(final Bitmap bmp, final Context context, final int deg) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_keepAColor script = new ScriptC_keepAColor(rs);
        script.set_keep(false);
        script.set_choosedColor((float) deg);
        script.forEach_keepAColor(input, output);
        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }

    static void histogramEqualizationRS(final Bitmap bmp, final Context context){
        Bitmap res = bmp.copy(bmp.getConfig(), true);
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, res);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_histogram script = new ScriptC_histogram(rs);
        script.set_size(bmp.getWidth() * bmp.getHeight());
        script.forEach_root(input, output);
        script.invoke_createRemapArray();
        script.forEach_remaptoRGB(output, input);
        input.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }


    static void toExtDyn(final Bitmap bmp, final Context context, final int targetMinLuminosity, final int targetMaxLuminosity){
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createFromBitmap(rs, bmp);
        ScriptC_constrastExtension script = new ScriptC_constrastExtension(rs);
        script.forEach_compute_histogramr(input);
        script.forEach_compute_histogramg(input);
        script.forEach_compute_histogramb(input);
        script.invoke_maxArrayB();
        script.invoke_maxArrayG();
        script.invoke_minArrayR();
        script.invoke_minArrayB();
        script.invoke_maxArrayR();
        script.invoke_minArrayG();
        script.invoke_createRemapArray();
        script.forEach_apply_histogram(input, output);
        output.copyTo(bmp);
        Allocation[] allocations = {input, output};
        cleanRenderScript(script, rs, allocations);
    }


    /**
     *  Apply a gaussian blur filter on one axis. You can choose which one by changing "vertical".
     *  @param bmp the pixels of the image
     *  @param context the context
     *  @param size size of the kernel
     *  @param vertical applies the kernel vertically, horizontally otherwise.
     */
    static void directionalBlur(final Bitmap bmp, final Context context, int size, boolean vertical) {

        if (size < 1) return;

        // Let's calculate the gaussian kernel
        final double sigma = size / 3.0;
        final double tmp = Math.exp(-(size * size / (2 * sigma * sigma)));
        final int floatToIntCoef = (int) (1 / tmp);
        int kernelWeight = 0;

        final float[] gaussianKernel = new float[size + 1 + size];
        for (int i = -size; i <= size ; i++) {
            gaussianKernel[i + size] = (int) (Math.exp(-(i * i / (2 * sigma * sigma))) * floatToIntCoef);
            kernelWeight += gaussianKernel[i + size];
        }

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs,input.getType());
        ScriptC_convolution script = new ScriptC_convolution(rs);
        script.bind_pixels(input);
        Allocation fGauss = Allocation.createSized(rs, Element.F32(rs), size + 1 + size);
        script.bind_kernel(fGauss);
        fGauss.copyFrom(gaussianKernel);
        script.set_kernelWidth(size);
        script.set_kernelHeight(size);
        script.set_kernelWeight(kernelWeight);
        script.set_width(bmp.getWidth());
        script.set_height(bmp.getHeight());
        if (vertical) {
            script.forEach_toConvolutionVertical(input, output);
        } else {
            script.forEach_toConvolutionHorizontal(input, output);
        }
        output.copyTo(bmp);

        Allocation[] allocations = {input, output, fGauss};
        cleanRenderScript(script, rs, allocations);
    }

    /**
     *  Apply a gaussian blur filter. This takes advantage of the Gaussian blur’s separable property by dividing the process into two passes.
     *  In the first pass, a one-dimensional kernel is used to blur the image in only the horizontal or vertical direction.
     *  In the second pass, the same one-dimensional kernel is used to blur in the remaining direction.
     *  The resulting effect is the same as convolving with a two-dimensional kernel in a single pass, but requires fewer calculations.
     *  (Text taken from the Wikipedia article Gaussian blur: https://en.wikipedia.org/wiki/Gaussian_blur)
     *  @param bmp the pixels of the image
     *  @param context the context
     *  @param size size of the kernel
     */
    static void gaussianRS(final Bitmap bmp, final Context context, int size) {
        directionalBlur(bmp, context, size, false);
        directionalBlur(bmp, context, size, true);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    static void laplacianRS(final Bitmap bmp, final Context context, final float amount) {
        if (amount > 0) gaussianRS(bmp, context, (int) amount);
        float v = amount + 1;
        float[] kernel = {
                v, v, v,
                v, -8 * v, v,
                v, v, v
        };
        applyConvolution(bmp, context, 3, 3, kernel);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    static void sharpenRS(final Bitmap bmp, final Context context, final float amount) {
        float[] kernel = {
                0f, -amount, 0f,
                -amount, 1f + 4f * amount, -amount,
                0f, -amount, 0f
        };
        applyConvolution(bmp, context, 3, 3, kernel);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    static void sobelRS(final Bitmap bmp, final Context context, final float amount, boolean vertical) {

        if (amount > 0) gaussianRS(bmp, context, (int) amount);

        float v = amount + 1;

        float[] kernelVertical = {
                -v, 0, v,
                -2 * v, 0, 2 * v,
                -v, 0, v
        };

        float[] kernelHorizontal = {
                -v, -2 * v, -v,
                0, 0, 0,
                v, 2 * v, v
        };
        float[] kernel = (vertical) ? kernelHorizontal : kernelVertical;
        applyConvolution(bmp, context, 3, 3, kernel);
    }

    /**
     *  Each pixel becomes the average of size * size pixels around it.
     *  @param bmp the image
     *  @param size size of the kernel
     */
    static void averageBlurRS(final Bitmap bmp, final Context context, final int size) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs,input.getType());
        ScriptC_convolution script = new ScriptC_convolution(rs);
        script.bind_pixels(input);
        script.set_kernelWidth(size);
        script.set_kernelHeight(size);
        script.set_kernelWeight((size + 1 + size) * (size + 1 + size));
        script.set_width(bmp.getWidth());
        script.set_height(bmp.getHeight());
        script.forEach_toConvolutionUniform(input, output);
        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

}
