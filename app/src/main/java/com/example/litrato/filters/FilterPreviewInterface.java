package com.example.litrato.filters;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.litrato.tools.Point;

/**
 * This interface is used to dynamically change the Filter instances apply method.
 * Each filter will call a different FilterFunction static method and using this
 * interface, it is possible for MainActivity to change which one to use.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */
interface FilterPreviewInterface {

    /**
     * Calls the correct function to apply the filter
     * @param bmp the image
     * @param context the app context
     * @param colorSeekHue the state of colorSeekBar
     * @param seekBar the state of seekBar1
     * @param seekBar2 the state of seekBar2
     */
    Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp);

}



