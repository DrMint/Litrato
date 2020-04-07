package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;

import static com.example.retouchephoto.RenderScriptTools.*;

import androidx.renderscript.Allocation;
import androidx.renderscript.ScriptIntrinsicBlur;


/**
 * This class implements all the filter function.
 * This class uses Intrinsic functions such as ScriptIntrinsicConvolve3x3.
 * All filters should have the following signature:
 * static void FilterName(final Bitmap bmp, final Context context, ... other parameters that can influence the result)
 * If a filter function has a Intrinsic equivalent, then the name of the function should be the same in both classes.
 * That way, it is easy to switch between them by simply changing the class when calling the function.
 * This is also true for FilterFunctionDeprecated.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-02-08
 */
class FilterFunctionIntrinsic {

    /**
     * Make the image blurry.
     * This filter uses RenderScript.
     * @param bmp the image.
     * @param context the context.
     * @param radius how blurry the image should be (the size of the kernel).
     *               Cannot be more than 25. because of Intrinsic's implementation limitations.
     */
    private static void gaussianBlur(final Bitmap bmp, final Context context, final float radius) {

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        script.setInput(input);
        script.setRadius(radius);
        script.forEach(output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter uses RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    @SuppressWarnings("unused")
    static void sobel(final Bitmap bmp, final Context context, final float amount, boolean vertical) {

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
        applyConvolution3x3RIntrinsic(bmp, context, kernel);

        removeAlpha(bmp);
    }

    /**
     *  Highlights the contour of an image.
     *  This filter uses RenderScript.
     *  @param bmp the image
     *  @param amount size of the blur (must be between 0 and 25)
     */
    @SuppressWarnings("unused")
    static void laplacian(final Bitmap bmp, final Context context, final float amount) {

        if (amount > 0) gaussianBlur(bmp, context, amount);

        float v = amount + 1;
        float[] kernel = {
                v,      v,          v,
                v,      -8 * v,     v,
                v,      v,          v
        };
        applyConvolution3x3RIntrinsic(bmp, context, kernel);
        removeAlpha(bmp);
    }

    /**
     *  Enhanced the image sharpness.
     *  If a negative number is used for amount, blurs the image slightly.
     *  This filter uses RenderScript.
     *  @param bmp the image
     *  @param amount amount of sharpness.
     */
    @SuppressWarnings("unused")
    static void sharpen(final Bitmap bmp, final Context context, final float amount) {
        float[] kernel = {
                0f,         -amount,            0f,
                -amount,    1f + 4f * amount,   -amount,
                0f,         -amount,            0f
        };
        applyConvolution3x3RIntrinsic(bmp, context, kernel);
    }

    /**
     * For all pixels, set the alpha value to 255.
     * @param bmp the image to modify.
     */
    private static void removeAlpha (final Bitmap bmp) {

        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        for (int i = 0; i < bmp.getWidth() * bmp.getWidth(); i++) {
            // Set alpha value to 255;
            pixels[i] = (pixels[i] | 0xFF000000);
        }

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }

}
