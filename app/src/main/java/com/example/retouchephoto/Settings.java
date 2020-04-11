package com.example.retouchephoto;

import android.graphics.Color;
import android.os.Environment;

/**
 * This class is where all the constant are store. It gives easy access to some settings.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-31-01
 */
class Settings {

    /**
     * The maximum size of a loaded image.
     * If the image is rectangular, the longest dimension will be resized to IMPORTED_BMP_SIZE and the other will be smaller than IMPORTED_BMP_SIZE.
     */
    static final int IMPORTED_BMP_SIZE = 1000;

    /**
     * How much the user can zoom on the image.
     * For example: 5f means display 1/5 a the image.
     */
    static final float MAX_ZOOM_LEVEL = 5f;

    /**
     * How much it zooms on the image when double tapping it.
     * For example: 5f means display 1/5 a the image.
     */
    static final float DOUBLE_TAP_ZOOM = 3f;

    /**
     * ImageViewZoomScroll is using this value when comparing the imageView ratio
     * and the image ratio. Because those two values will never be exactly the same,
     * this value is how far off is still considered equal.
     */
    static final float IMAGE_RATIO_PRECISION = 0.01f;

    static final int CROP_OPACITY = 100;

    static final int CROP_BORDER_SIZE = 5;

    // 100 means no compression, the lower you go, the stronger the compression
    static final int OUTPUT_JPG_QUALITY = 90;

    static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Litrato" + "/";

    static final String SAVE_PATH_ORIGINAL = SAVE_PATH + "Original" + "/";

    static final int ITEMS_MARGIN_IN_MENU = 20;

    static final int MINIATURE_BMP_SIZE = 100;

    static final int MINIATURE_DISPLAYED_SIZE = 250;

    static final int COLOR_BACKGROUND = Color.rgb(0,0,0);
    static final int COLOR_GREY = Color.rgb(40,40,40);
    static final int COLOR_SELECTED = Color.rgb(70,70,70);

}
