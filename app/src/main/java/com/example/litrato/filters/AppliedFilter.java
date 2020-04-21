package com.example.litrato.filters;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.litrato.tools.ImageTools;
import com.example.litrato.tools.Point;
import com.example.litrato.tools.PointPercentage;

/** An AppliedFilter contains a filter and all the necessary information to apply this specific filter to an image.
 * Needed to create an historic of the applied filters.
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since 2020-19-04
 */
public class AppliedFilter {
    private final Filter filter;
    private Bitmap maskBmp;
    private final int colorSeekHue;
    private final float seekBar;
    private final float seekBar2;
    private final boolean switch1;
    private final PointPercentage touchDown;
    private final PointPercentage touchUp;

    /**
     * Constructor
     * @param filter the filter
     * @param maskBmp the mask
     * @param colorSeekHue value of color seekbar
     * @param seekBar value of seekbar1
     * @param seekBar2 value of seekbar2
     * @param switch1 state of switch
     * @param touchDown point
     * @param touchUp point
     */
    public AppliedFilter(Filter filter, Bitmap maskBmp, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, PointPercentage touchDown, PointPercentage touchUp) {
        this.filter = filter;
        this.maskBmp = maskBmp;
        this.colorSeekHue = colorSeekHue;
        this.seekBar = seekBar;
        this.seekBar2 = seekBar2;
        this.switch1 = switch1;
        this.touchDown = touchDown;
        this.touchUp = touchUp;
    }

    /**
     * Constructor when no parameters are required to apply the correct filter
     * @param filter the filter
     */
    public AppliedFilter(Filter filter){
        this(filter, null, 0, filter.seekBar1Set, filter.seekBar2Set, filter.switch1Default,  new PointPercentage(0,0), new PointPercentage(0,0));
    }

    /**
     * getter
     * @return name of the filter
     */
    public String getName() {return filter.getName();}

    /**
     * Apply the filter
     * @param bmp the image
     * @param context the context
     * @return the modified image
     */
    public Bitmap apply(Bitmap bmp, Context context) {

        Point a = null;
        Point b = null;
        if (touchDown != null) {
            a = new Point(touchDown, bmp);
            b = new Point(touchUp, bmp);
        }

        // This only works because we never use a maskBmp and return the filtered image in filter.apply
        if (maskBmp == null) {

            return filter.apply(bmp, null, context, colorSeekHue, seekBar, seekBar2, switch1, a, b);

        } else {

            // If we apply the mask was create an a smaller image than bmp (when we apply the history to the original image)
            if (maskBmp.getWidth() != bmp.getWidth() || maskBmp.getHeight() != bmp.getHeight()) {
                maskBmp = ImageTools.scale(maskBmp, bmp.getWidth(), bmp.getHeight());
            }

            Bitmap invertedMaskBmp = ImageTools.bitmapClone(maskBmp);
            FilterFunction.invert(invertedMaskBmp);

            Bitmap originalImageMasked = ImageTools.bitmapClone(bmp);
            FilterFunction.applyTexture(originalImageMasked, invertedMaskBmp, BlendType.MULTIPLY);

            filter.apply(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, a, b);

            FilterFunction.applyTexture(bmp, maskBmp,BlendType.MULTIPLY);
            FilterFunction.applyTexture(bmp, originalImageMasked, BlendType.ADD);
            return null;
        }
    }
}
