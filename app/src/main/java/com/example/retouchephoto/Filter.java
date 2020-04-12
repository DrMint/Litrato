package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

/**
 * A instance of this class has many properties such as what kind of inputs (colorSeekBar and seekBars) should be available to the user.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */

class Filter {

    /**
     * The name displayed in the spinner.
     */
    private final String name;

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

    /**
     * Does this filter utilize the first switch.
     */
    boolean switch1;
    boolean switch1Default;
    String switch1UnitFalse;
    String switch1UnitTrue;

    /**
     * Only for generate Tools Button dynamically
     */
    private Bitmap icon;

    private FilterApplyInterface myApplyInterface;
    private FilterPreviewInterface myPreviewInterface;
    private FilterInitInterface myInitInterface;

    private FilterCategory category;
    boolean needFilterActivity = true;
    boolean allowMasking = true;
    boolean allowScrollZoom = true;

    Filter(String name) {
        this.name = name;
    }

    //Getters and Setters

    void setSeekBar1(int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Unit) {
        this.seekBar1 = true;
        this.seekBar1Min = seekBar1Min;
        this.seekBar1Set = seekBar1Set;
        this.seekBar1Max = seekBar1Max;
        this.seekBar1Unit = seekBar1Unit;
    }

    void setSeekBar2(int seekBar2Min, int seekBar2Set, int seekBar2Max, String seekBar2Unit) {
        this.seekBar2 = true;
        this.seekBar2Min = seekBar2Min;
        this.seekBar2Set = seekBar2Set;
        this.seekBar2Max = seekBar2Max;
        this.seekBar2Unit = seekBar2Unit;
    }

    void setSwitch1(boolean switch1Default, String switch1UnitFalse, String switch1UnitTrue) {
        this.switch1 = true;
        this.switch1Default = switch1Default;
        this.switch1UnitFalse = switch1UnitFalse;
        this.switch1UnitTrue = switch1UnitTrue;
    }

    void setFilterCategory(FilterCategory category) {this.category = category; if (category == FilterCategory.PRESET) needFilterActivity = false;}
    void setColorSeekBar() {this.colorSeekBar = true;}
    void setFilterApplyFunction(final FilterApplyInterface newInterface) {this.myApplyInterface = newInterface;}
    void setFilterPreviewFunction(final FilterPreviewInterface newInterface) {this.myPreviewInterface = newInterface;}
    void setFilterInitFunction(final FilterInitInterface newInterface) {this.myInitInterface = newInterface;}
    void setIcon(Bitmap bmp){this.icon = bmp;}

    String getName() {return this.name;}
    Bitmap getIcon(){return icon;}
    FilterCategory getFilterCategory() {return category;}


    /**
     *  Start the correct filter function for that specific filter instance.
     *  Because RenderScript uses Bitmap as input and other filters use an array of pixel, we have to
     *  create both
     *  @param bmp the image the filter will be apply to.
     *  @param colorSeekHue the value of colorSeekBar.
     *  @param seekBar the value of seekBar1.
     *  @param seekBar2 the value of seeBar2.
     */
    Bitmap apply(final Bitmap bmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        if (myApplyInterface != null) return myApplyInterface.apply(bmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        return myPreviewInterface.preview(bmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
    }

    Bitmap apply(final Bitmap bmp, final Context context) {
        return apply(bmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    Bitmap preview(final Bitmap bmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        if (myPreviewInterface != null) return myPreviewInterface.preview(bmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        return null;
    }

    Bitmap preview(final Bitmap bmp, final Context context) {
        return preview(bmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    void init() {
        if (myInitInterface != null) myInitInterface.init();
    }
}

