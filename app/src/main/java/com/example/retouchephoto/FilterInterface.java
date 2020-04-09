package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * This interface is used to dynamically change the Filter instances apply method.
 * Each filter will call a different FilterFunction static method and using this
 * interface, it is possible for MainActivity to change which one to use.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */
interface FilterApplyInterface {

    /**
     * Calls the correct function to apply the filter
     * @param bmp the image
     * @param context the app context
     * @param colorSeekHue the state of colorSeekBar
     * @param seekBar the state of seekBar1
     * @param seekBar2 the state of seekBar2
     */
    @SuppressWarnings("unused")
    Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1);

}

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
    Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1);

}

interface FilterInitInterface {

    /**
     * Calls this function when the filter is becomes selected
     */
    void init();

}


