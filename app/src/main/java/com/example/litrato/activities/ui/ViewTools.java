package com.example.litrato.activities.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

/**
 * Tools useful with views and other layout related stuff.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class ViewTools {

    /**
     * Returns true if the View is VISIBLE, false otherwise.
     * @param view the view to check
     * @return true if the View is VISIBLE, false otherwise.
     */
    public static boolean isVisible(View view) {
        return (view.getVisibility() == View.VISIBLE);
    }

    /**
     * Convert DP into Pixels.
     * DP stands for Density-independent Pixels. It means that the measure doesn't depend on
     * the screen resolution, similar to a vw and vh in HTML.
     * @param dp the value in DP
     * @param context the context
     * @return the value in pixels
     */
    public static int convertDpToPixel(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Convert SP into Pixels.
     * SP stands for Scaled Pixels. It means that the measure will depend on the System Global UI
     * size, selected in the phone settings.
     * @param sp the value in sp
     * @param context the context
     * @return the value in pixels
     */
    public static int convertSpToPixel(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }


}
