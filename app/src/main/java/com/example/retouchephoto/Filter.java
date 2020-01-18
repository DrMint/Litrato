package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;


/**
 * A instance of this class has many properties such as what kind of inputs (colorSeekBar and seekBars) should be available to the user.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */

/*  The Android Studio seems to think my function aren't use in other classes inside
    this package. It would like me to add "protected" to my function which would
    prevent MainActivity to use those filter...
    I will disable this warning for now.
 */
@SuppressWarnings("WeakerAccess")

class Filter {

    /**
     * The name displayed in the spinner.
     */
    private String name;

    /**
     * Does this filter utilize the colorSeekBar.
     */
    boolean colorSeekBar;

    /**
     * Does this filter utilize the first seekBar.
     */
    boolean seekBar1;
    int seekBar1Min;
    int seekBar1Set;
    int seekBar1Max;
    String seekBar1Unit;

    /**
     * Does this filter utilize the second seekBar.
     */
    boolean seekBar2;
    int seekBar2Min;
    int seekBar2Set;
    int seekBar2Max;
    String seekBar2Unit;

    private FilterInterface myInterface;

    public Filter(String name) {
        this.name = name;
    }

    //Getters and Setters
    public String getName() {return this.name;}

    public void setSeekBar1(int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Unit) {
        this.seekBar1 = true;
        this.seekBar1Min = seekBar1Min;
        this.seekBar1Set = seekBar1Set;
        this.seekBar1Max = seekBar1Max;
        this.seekBar1Unit = seekBar1Unit;
    }

    public void setSeekBar2(int seekBar2Min, int seekBar2Set, int seekBar2Max, String seekBar2Unit) {
        this.seekBar2 = true;
        this.seekBar2Min = seekBar2Min;
        this.seekBar2Set = seekBar2Set;
        this.seekBar2Max = seekBar2Max;
        this.seekBar2Unit = seekBar2Unit;
    }

    public void setColorSeekBar() {
        this.colorSeekBar = true;
    }

    public void setFilterFunction(final FilterInterface newInterface) {
        this.myInterface = newInterface;
    }

    /**
     *  Start the correct filter function for that specific filter instance.
     *  Because RenderScript uses Bitmap as input and other filters use an array of pixel, we have to
     *  create both
     *  @param bmp the image the filter will be apply to.
     *  @param colorSeekHue the value of colorSeekBar.
     *  @param seekBar the value of seekBar1.
     *  @param seekBar2 the value of seeBar2.
     */
    public void apply(final Bitmap bmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2) {
        if (myInterface != null) myInterface.apply(bmp, context, colorSeekHue, seekBar, seekBar2);
    }
}

