package com.example.litrato.activities.tools;

import android.graphics.Color;
import android.os.Environment;

/**
 * This class is where all the constant are store. It gives easy access to some settings.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-31-01
 */
public class Settings {



    /**
     * How much the user can zoom on the image.
     * For example: 5f means display 1/5 a the image.
     */
    public static final float MAX_ZOOM_LEVEL = 5f;

    /**
     * How much it zooms on the image when double tapping it.
     * For example: 5f means display 1/5 a the image.
     */
    public static final float DOUBLE_TAP_ZOOM = 3f;

    /**
     * ImageViewZoomScroll is using this value when comparing the imageView ratio
     * and the image ratio. Because those two values will never be exactly the same,
     * this value is how far off is still considered equal.
     */
    public static final float IMAGE_RATIO_PRECISION = 0.01f;

    // 100 means no compression, the lower you go, the stronger the compression
    public static final int OUTPUT_JPG_QUALITY = 90;

    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Litrato" + "/";

    public static final String SAVE_PATH_ORIGINAL = SAVE_PATH + "Original" + "/";

    public static final int ITEMS_MARGIN_IN_MENU = 20;

    public static final int MINIATURE_DISPLAYED_SIZE = 250;
    public static final int TOOL_DISPLAYED_SIZE = 120;

    public static final int HISTOGRAM_BACKGROUND_TRANSPARENCY = 100;
    public static final int HISTOGRAM_FOREGROUND_TRANSPARENCY = 200;

    public static final int CROP_OPACITY = 100;
    public static final int CROP_BORDER_SIZE = 5;

    public static final String FILTER_MASK_NAME = "Create mask";
    public static final String FILTER_ROTATION = "Rotation";



    // COLOR

    public static void setColorTheme(boolean darkMode) {
        if (darkMode) {
            COLOR_BACKGROUND = Color.rgb(0,0,0);
            COLOR_GREY = Color.rgb(40,40,40);
            COLOR_SELECTED = Color.rgb(70,70,70);
            COLOR_TEXT = Color.rgb(255,255,255);
        } else {
            COLOR_BACKGROUND = Color.rgb(255,255,255);
            COLOR_GREY = Color.rgb(215,215,215);
            COLOR_SELECTED = Color.rgb(190,190,190);
            COLOR_TEXT = Color.rgb(0,0,0);
        }
    }

    public static int COLOR_BACKGROUND;
    public static int COLOR_GREY;
    public static int COLOR_SELECTED;
    public static int COLOR_TEXT;


    public static final String ACTIVITY_EXTRA_CALLER = "CallerActivity";

}
