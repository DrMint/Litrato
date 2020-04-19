package com.example.litrato.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.litrato.tools.ImageTools;
import com.example.litrato.tools.Point;

/**
 * A instance of this class has many properties such as what kind of inputs (colorSeekBar and seekBars) should be available to the user.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */

public class Filter {

    /**
     * The name displayed in the spinner.
     */
    private final String name;

    /**
     * Does this filter utilize the colorSeekBar.
     */
    public boolean colorSeekBar = false;
    public boolean colorSeekBarAutoRefresh = true;

    /**
     * Does this filter utilize the first seekBar.
     */
    public boolean seekBar1 = false;
    public boolean seekBar1AutoRefresh = true;
    public int seekBar1Min;
    public int seekBar1Set;
    public int seekBar1Max;
    public String seekBar1Unit;

    /**
     * Does this filter utilize the second seekBar.
     */
    public boolean seekBar2 = false;
    public boolean seekBar2AutoRefresh = true;
    public int seekBar2Min;
    public int seekBar2Set;
    public int seekBar2Max;
    public String seekBar2Unit;

    /**
     * Does this filter utilize the first switch.
     */
    public boolean switch1 = false;
    public boolean switch1AutoRefresh = true;
    public boolean switch1Default;
    public String switch1UnitFalse;
    public String switch1UnitTrue;

    /**
     * Only for generate Tools Button dynamically
     */
    private Bitmap icon;

    private FilterApplyInterface myApplyInterface;
    private FilterPreviewInterface myPreviewInterface;

    private Category category;
    public boolean needFilterActivity = true;
    public boolean allowMasking = true;
    public boolean allowHistogram = true;
    public boolean allowScrollZoom = true;
    public boolean allowFilterMenu = true;

    public Filter(String name) {
        this.name = name;
    }

    //Getters and Setters

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

    public void setSwitch1(boolean switch1Default, String switch1UnitFalse, String switch1UnitTrue) {
        this.switch1 = true;
        this.switch1Default = switch1Default;
        this.switch1UnitFalse = switch1UnitFalse;
        this.switch1UnitTrue = switch1UnitTrue;
    }

    public void setColorSeekBar() {this.colorSeekBar = true;}

    public void setFilterCategory(Category category) {this.category = category; if (category == Category.PRESET) needFilterActivity = false;}
    public void setFilterApplyFunction(final FilterApplyInterface newInterface) {this.myApplyInterface = newInterface;}
    public void setFilterPreviewFunction(final FilterPreviewInterface newInterface) {this.myPreviewInterface = newInterface;}
    public void setIcon(Bitmap bmp){this.icon = bmp;}

    public String getName() {return this.name;}
    public Bitmap getIcon(){return icon;}
    public Category getFilterCategory() {return category;}


    /**
     *  Start the correct filter function for that specific filter instance.
     *  Because RenderScript uses Bitmap as input and other filters use an array of pixel, we have to
     *  create both
     *  @param bmp the image the filter will be apply to.
     *  @param colorSeekHue the value of colorSeekBar.
     *  @param seekBar the value of seekBar1.
     *  @param seekBar2 the value of seeBar2.
     */
    public Bitmap apply(final Bitmap bmp, final Bitmap maskBmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        if (myApplyInterface != null) return myApplyInterface.apply(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        return preview(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
    }

    public Bitmap apply(final Bitmap bmp, Bitmap maskBmp, final Context context) {
        return apply(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    public Bitmap apply(final Bitmap bmp, final Context context) {
        final Bitmap maskBmp = ImageTools.bitmapClone(bmp);
        ImageTools.fillWithColor(maskBmp, Color.WHITE);
        return apply(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    public Bitmap preview(final Bitmap bmp, final Bitmap maskBmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        if (myPreviewInterface != null) return myPreviewInterface.preview(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        return null;
    }

    public Bitmap preview(final Bitmap bmp, final Bitmap maskBmp, final Context context) {
        return preview(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    public Bitmap preview(final Bitmap bmp, final Context context) {
        final Bitmap maskBmp = ImageTools.bitmapClone(bmp);
        ImageTools.fillWithColor(maskBmp, Color.WHITE);
        return preview(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }
}

