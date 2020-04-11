package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;

import com.android.retouchephoto.ScriptC_addNoise;
import com.android.retouchephoto.ScriptC_brightness;
import com.android.retouchephoto.ScriptC_burn;
import com.android.retouchephoto.ScriptC_colorize;
import com.android.retouchephoto.ScriptC_constrastExtension;
import com.android.retouchephoto.ScriptC_convolution;
import com.android.retouchephoto.ScriptC_gamma;
import com.android.retouchephoto.ScriptC_gray;
import com.android.retouchephoto.ScriptC_histogram;
import com.android.retouchephoto.ScriptC_hueshift;
import com.android.retouchephoto.ScriptC_invert;
import com.android.retouchephoto.ScriptC_keepAColor;
import com.android.retouchephoto.ScriptC_mirror;
import com.android.retouchephoto.ScriptC_mix;
import com.android.retouchephoto.ScriptC_posterizing;
import com.android.retouchephoto.ScriptC_rgbWeights;
import com.android.retouchephoto.ScriptC_saturation;
import com.android.retouchephoto.ScriptC_threshold;

import static com.example.retouchephoto.RenderScriptTools.applyConvolution;
import static com.example.retouchephoto.RenderScriptTools.cleanRenderScript;

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
     *  A filter that convert the image to grayscale.
     *  This filter use RenderScript.
     *  @param bmp the image
     */
    private static void grayscale(final Bitmap bmp, final Context context) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_gray script = new ScriptC_gray(rs);
        script.forEach_grayscale(input, output) ;

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }


    /**
     *  Shift all the hue of all pixels by a certain value.
     *  @param bmp the image
     * @param context the context
     *  @param shift the value to shift the hue with.
     */
    static void hueShift(final Bitmap bmp, final Context context, final float shift) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_hueshift script = new ScriptC_hueshift(rs);

        script.set_shift(shift);
        script.invoke_calculateLUT();
        script.forEach_hueshift(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  A filter that change the saturation of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param saturation the amount of saturation (must be between 0 and +inf)
     */
    static void saturation(final Bitmap bmp, final Context context, final float saturation) {

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
     *  A filter that invert the luminosity of the image.
     *  This filter use RenderScript.
     *  @param bmp the image
     */
    static void invert(final Bitmap bmp, final Context context) {

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
    static void brightness(final Bitmap bmp, final Context context, final float exposure) {

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
    static void posterize(final Bitmap bmp, final Context context, final int steps, boolean toGray) {

        if (toGray) grayscale(bmp, context);

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
    static void threshold(final Bitmap bmp, final Context context, final float level) {

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
    static void temperature(final Bitmap bmp, final Context context, final float level) {

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
    static void tint(final Bitmap bmp, final Context context, final float level) {

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
    static void noise(final Bitmap bmp, final Context context, final int level, final boolean colorNoise) {

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

    /**
     * Colorize an image with a picked hue and a picked saturation (which modifies the intensity of the color).
     * @param bmp the image
     * @param context the context
     * @param hue the new hue for all pixels
     * @param saturation the new saturation for all pixels
     * @param changeSaturation if we change the saturation or not
     */

    static void colorize(final Bitmap bmp, final Context context, final int hue, final float saturation, boolean changeSaturation) {
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_colorize colorizeScript = new ScriptC_colorize(rs);
        colorizeScript.set_t(hue);
        if (changeSaturation) {
            colorizeScript.set_saturation(saturation);
        }
        colorizeScript.forEach_colorize(input, output);
        output.copyTo(bmp);
        cleanRenderScript(colorizeScript, rs, input, output);
    }

    /**
     * A filter that convert the image to grayscale, but keeps a shade of color intact.
     * @param bmp the image
     * @param context the context
     * @param deg the color we want to keep (hue between 0 and 360)
     */
    static void keepAColor(final Bitmap bmp, final Context context, final int deg, final int colorMargin) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_keepAColor script = new ScriptC_keepAColor(rs);
        script.set_keep(true);
        script.set_choosedColor(deg);
        script.set_margin((float)colorMargin);
        script.invoke_calculateLUT();
        script.forEach_keepAColor(input, output);
        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }

    /**
     * A filter that turn to gray a picked color
     * @param bmp the image
     * @param context the context
     * @param deg the color we want to remove (hue between 0 and 360)
     */
    static void removeAColor(final Bitmap bmp, final Context context, final int deg, final int colorMargin) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_keepAColor script = new ScriptC_keepAColor(rs);
        script.set_keep(false);
        script.set_choosedColor(deg);
        script.set_margin((float)colorMargin);
        script.invoke_calculateLUT();
        script.forEach_keepAColor(input, output);
        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }


    /**
     *  A filter that increase the contrast by evenly distributing
     *  the intensities on the histogram.
     *  @param bmp the image
     *  @param context the context
     */
    static void histogramEqualization(final Bitmap bmp, final Context context){
        Bitmap res = ImageTools.bitmapClone(bmp);
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

    /**
     *  A filter that stretch or compress the current range of luminosity to a new target range.
     * @param bmp the image
     * @param context the context
     *  @param targetMinLuminosity the luminosity of the darkest pixel after linear stretching (must be between 0f and 1f)
     *  @param targetMaxLuminosity the luminosity of the brightest pixel after linear stretching (must be between 0f and 1f)
     */
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
        script.set_targetMin(targetMinLuminosity);
        script.set_targetMax(targetMaxLuminosity);
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
    static void directionalBlur(final Bitmap bmp, final Context context, final int size, final boolean vertical) {

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
        script.set_pixels(input);
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
    static void gaussianBlur(final Bitmap bmp, final Context context, final int size) {
        directionalBlur(bmp, context, size, false);
        directionalBlur(bmp, context, size, true);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    static void laplacian(final Bitmap bmp, final Context context, final float amount) {
        if (amount > 0) gaussianBlur(bmp, context, (int) amount);
        float v = amount + 1;
        float[] kernel = {
                v,      v,          v,
                v,      -8 * v,     v,
                v,      v,          v
        };
        applyConvolution(bmp, context, 3, 3, kernel);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    static void sharpen(final Bitmap bmp, final Context context, final float amount) {
        float[] kernel = {
                0f,         -amount,            0f,
                -amount,    1f + 4f * amount,   -amount,
                0f,         -amount,            0f
        };
        applyConvolution(bmp, context, 3, 3, kernel);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter use RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    static void sobel(final Bitmap bmp, final Context context, final float amount, final boolean vertical) {
        if (amount > 0) gaussianBlur(bmp, context, (int) amount);
        float v = amount + 1;

        float[] kernelVertical = {
                -v,         0,      v,
                -2 * v,     0,      2 * v,
                -v,         0,      v
        };

        float[] kernelHorizontal = {
                -v,     -2 * v,     -v,
                0,      0,          0,
                v,      2 * v,      v
        };

        float[] kernel = (vertical) ? kernelHorizontal : kernelVertical;
        applyConvolution(bmp, context, 3, 3, kernel);
    }

    /**
     *  Each pixel becomes the average of size * size pixels around it.
     *  @param bmp the image
     *  @param size size of the kernel
     */
    static void averageBlur(final Bitmap bmp, final Context context, final int size) {
        applyConvolution(bmp, context, size * 2 + 1, size * 2 + 1);
    }

    static Bitmap rotate(final Bitmap bmp, final float degrees){
        if (degrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }
        return bmp;
    }

    /**
     * Takes a image and keeps only what's between point a and b
     * @param bmp the image to crop
     * @param a a corner defining the rectangle
     * @param b the opposite corner of the rectangle
     */
    static Bitmap crop (final Bitmap bmp, final Point a, final Point b){

        if (!a.isEquals(b)) {
            int width = Math.abs(a.x - b.x);
            int height = Math.abs(a.y - b.y);

            int startX = Math.min(a.x, b.x);
            int startY = Math.min(a.y, b.y);

            return Bitmap.createBitmap(bmp, startX, startY, width, height);
        }
        return bmp;
    }


    static void cartoon(final Bitmap bmp, final Context context, final int contour, final int posterize) {

        Bitmap bmpCopy = ImageTools.bitmapClone(bmp);


        // First layer
        laplacian(bmp, context, 4);
        invert(bmp, context);
        threshold(bmp, context, 0.8f);

        // Second layer
        posterize(bmpCopy, context, posterize, false);
        toExtDyn(bmpCopy, context, contour, 255);

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation pixels = Allocation.createFromBitmap(rs, bmpCopy);
        Allocation output = Allocation.createTyped(rs,input.getType());

        // Multiply layer 1 and 2
        ScriptC_mix script = new ScriptC_mix(rs);
        script.set_pixels(pixels);
        script.forEach_multiply(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }


    static void sketch(final Bitmap bmp, final Context context, final int contour, final float saturation) {

        Bitmap bmpCopy = ImageTools.bitmapClone(bmp);

        // First layer
        laplacian(bmp, context, contour);
        invert(bmp, context);

        // Using layer 1's luminosity and apply it to layer 2
        applyTexture(bmp, bmpCopy, context, BlendType.LUMINOSITY, saturation);
    }


    static void applyTexture(final Bitmap bmp, final Bitmap texture, final Context context, final BlendType typeOfBlend, final float parameter) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation pixels = Allocation.createFromBitmap(rs, texture);
        Allocation output = Allocation.createTyped(rs,input.getType());

        // Multiply layer 1 and 2
        ScriptC_mix script = new ScriptC_mix(rs);
        script.set_pixels(pixels);

        switch (typeOfBlend) {
            case MULTIPLY:
                script.forEach_multiply(input, output);
                break;

            case ADD:
                script.forEach_add(input, output);
                break;

            case LUMINOSITY:
                script.set_luminositySaturation(parameter);
                script.forEach_luminosity(input, output);
                break;

            case OVERLAY:
                script.set_overlayTransparency(parameter);
                script.forEach_overlay(input, output);
        }


        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    static void gamma(final Bitmap bmp, final Context context, final float gamma) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_gamma script = new ScriptC_gamma(rs);

        script.invoke_setGamma(gamma);
        script.forEach_gamma(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);

    }

    static void applyTexture(final Bitmap bmp, final Bitmap texture, final Context context, final BlendType typeOfBlend) {
        applyTexture(bmp, texture, context, typeOfBlend,0);
    }

    static void applyTexture(final Bitmap bmp, final Bitmap texture, final Context context) {
        applyTexture(bmp, texture, context, BlendType.MULTIPLY);
    }

    static void mirror(final Bitmap bmp, final Context context) {

        Bitmap bmpCopy = ImageTools.bitmapClone(bmp);

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation pixels = Allocation.createFromBitmap(rs, bmpCopy);
        Allocation output = Allocation.createTyped(rs,input.getType());

        // Multiply layer 1 and 2
        ScriptC_mirror script = new ScriptC_mirror(rs);
        script.set_width(bmp.getWidth());
        script.set_pixels(pixels);
        script.forEach_mirror(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    static void burnValues(final Bitmap bmp, final Context context, final float level) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_burn script = new ScriptC_burn(rs);

        if (level < 0) {
            script.invoke_setBurnBlackIntensity(-level);
        } else {
            script.invoke_setBurnWhiteIntensity(level);
        }

        script.forEach_burn(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    static void constrastBurn(final Bitmap bmp, final Context context, float level) {

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_burn script = new ScriptC_burn(rs);

        if (level < 0 ) {
            level /= 2f;
        } else {
            level *= 2f;
        }

        script.invoke_setBurnIntensity(level);

        script.forEach_burn(input, output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }


}
