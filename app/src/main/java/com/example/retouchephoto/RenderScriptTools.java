package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Script;
import androidx.renderscript.ScriptIntrinsicConvolve3x3;
import androidx.renderscript.ScriptIntrinsicBlend;

import com.android.retouchephoto.ScriptC_convolution;

/**
 * This class implements useful tools for functions that use RS.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */
class RenderScriptTools {

    /**
     * Does all the job of setting up the convolution, renderscript stuff and then clean the mess.
     * @param bmp the image to load
     * @param context the app context
     * @param kernel the kernel to use for convolution
     */
    static void applyConvolution3x3RIntrinsic(final Bitmap bmp, final Context context, final float[] kernel) {

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicConvolve3x3 script = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        script.setInput(input);
        script.setCoefficients(kernel);
        script.forEach(output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }


    static void BlendIntrinsic(final Bitmap bmp1, final Bitmap bmp2, final Context context) {

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlend script = ScriptIntrinsicBlend.create(rs, Element.U8_4(rs));

        Allocation input = Allocation.createFromBitmap(rs, bmp1);
        Allocation output = Allocation.createFromBitmap(rs, bmp2);

        script.forEachMultiply(input, output);

        cleanRenderScript(script, rs, input, output);
    }




    /**
     * Does all the job of setting up the convolution, RenderScript stuff and then clean the mess.
     * @param bmp the image.
     * @param context the app context
     * @param kernelWidth the width of the kernel
     * @param kernelHeight the height of the kernel
     * @param kernel the kernel to use for convolution
     */
    static void applyConvolution(final Bitmap bmp, final Context context, final int kernelWidth, final int kernelHeight, final float[] kernel) {

        RenderScript rs = RenderScript.create(context);
        ScriptC_convolution script = new ScriptC_convolution(rs);

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation pixels = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs,input.getType());

        script.set_pixels(pixels);

        script.set_width(bmp.getWidth());
        script.set_height(bmp.getHeight());

        script.set_kernelWidth((kernelWidth - 1) / 2);
        script.set_kernelHeight((kernelHeight - 1) / 2);

        float kernelWeight = 0f;

        // If no kernel was provided then let's considered it as a kernel containing just a bunch of ones
        if (kernel.length == 0) {
            kernelWeight = kernelWidth * kernelHeight;
            script.set_kernelWeight(kernelWeight);
            script.forEach_toConvolutionUniform(input, output);
        } else {
            for (float v : kernel) {
                kernelWeight += v;
            }
            if (kernelWeight == 0) kernelWeight++;
            script.set_kernelWeight(kernelWeight);

            Allocation kernelAlloc = Allocation.createSized(rs, Element.F32(rs), kernel.length);
            kernelAlloc.copyFrom(kernel);
            script.bind_kernel(kernelAlloc);

            script.forEach_toConvolution(input, output);
        }

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }



    /**
     * Does all the job of setting up the convolution, RenderScript stuff and then clean the mess.
     * Without a kernel, the kernel weights will be uniform.
     * @param bmp the image.
     * @param context the app context
     * @param kernelWidth the width of the kernel
     * @param kernelHeight the height of the kernel
     */
    static void applyConvolution(final Bitmap bmp, final Context context, final int kernelWidth, final int kernelHeight) {
        final float[] kernel = {};
        applyConvolution(bmp, context, kernelWidth, kernelHeight, kernel);
    }



    /**
     * Can be called after any RS function to destroy the different object used.
     * @param script the Script object to destroy.
     * @param rs the RenderScript object to destroy.
     * @param allocations an array of all the allocations to destroy.
     */
    static void cleanRenderScript(final Script script, final RenderScript rs, final Allocation[] allocations) {
        script.destroy();
        rs.destroy();
        for (Allocation allocation : allocations) allocation.destroy();
    }

    /**
     * Can be called after any RS function to destroy the different object used.
     * @param script the Script object to destroy.
     * @param rs the RenderScript object to destroy.
     * @param input the Allocations to destroy.
     * @param output the Allocations to destroy.
     */
    static void cleanRenderScript(final Script script, final RenderScript rs, Allocation input, Allocation output) {
        Allocation[] allocations = {input, output};
        cleanRenderScript(script, rs, allocations);
    }
}
