package com.example.retouchephoto;

import android.graphics.Bitmap;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Script;
import androidx.renderscript.ScriptIntrinsicConvolve3x3;

class RenderScriptTools {

    /**
     * Does all the job of setting the convolution, renderscript stuff and then clean the mess.
     * @param bmp the image to load
     * @param kernel the kernel to use for convolution
     */
    static void applyConvolution3x3RS(final Bitmap bmp, final float[] kernel) {

        RenderScript rs = RenderScript.create(MainActivity.getAppContext());
        ScriptIntrinsicConvolve3x3 script = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        script.setInput(input);
        script.setCoefficients(kernel);
        script.forEach(output);

        output.copyTo(bmp);
        cleanRenderScript(script, rs, input, output);
    }

    static void cleanRenderScript(Script script, RenderScript rs, Allocation input, Allocation output) {
        script.destroy();
        input.destroy();
        output.destroy();
        rs.destroy();
    }
}