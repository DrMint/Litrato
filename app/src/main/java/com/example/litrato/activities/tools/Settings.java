package com.example.litrato.activities.tools;

import android.content.Context;
import android.os.Environment;

import com.example.litrato.activities.ui.ViewTools;

/**
 * This class is where most constant are store.
 * It gives easy access to some settings.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
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
     * ImageViewZoomScrollDeprecated is using this value when comparing the imageView ratio
     * and the image ratio. Because those two values will never be exactly the same,
     * this value is how far off is still considered equal.
     */
    @Deprecated public static final float IMAGE_RATIO_PRECISION = 0.01f;

    /**
     * The quality of the saved image.
     * 100 means no compression, the lower you go, the higher the compression.
     */
    public static final int OUTPUT_JPG_QUALITY = 90;

    /**
     * The path to Litrato's folder.
     * Photo are not saved there if using the Android MediaStore.
     */
    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Litrato" + "/";

    /**
     * The path to the subfolder of Litrato where the captured image are saved.
     */
    public static final String SAVE_PATH_ORIGINAL = SAVE_PATH + "Original" + "/";


    public static final int ITEMS_MARGIN_IN_MENU = 20;
    public static final int PADDING_BETWEEN_MINIATURE_AND_LABEL = 25;
    public static final int MINIATURE_AND_TOOL_TEXT_SIZE_SP = 12;
    private static final int MINIATURE_DISPLAYED_SIZE_DP = 80;
    private static final int TOOL_DISPLAYED_SIZE_DP = 45;

    public static int MINIATURE_AND_TOOL_TEXT_SIZE;
    public static int MINIATURE_DISPLAYED_SIZE;
    public static int TOOL_DISPLAYED_SIZE;

    /**
     * Convert the SP and DP values into Pixels.
     * I.e. MINIATURE_DISPLAYED_SIZE_DP's pixel value is stored in MINIATURE_DISPLAYED_SIZE.
     * @param context
     */
    public static void setDPValuesInPixel(Context context) {
        MINIATURE_DISPLAYED_SIZE = ViewTools.convertDpToPixel(MINIATURE_DISPLAYED_SIZE_DP, context);
        TOOL_DISPLAYED_SIZE = ViewTools.convertDpToPixel(TOOL_DISPLAYED_SIZE_DP, context);
        MINIATURE_AND_TOOL_TEXT_SIZE = ViewTools.convertSpToPixel(MINIATURE_AND_TOOL_TEXT_SIZE_SP , context);
    }

    public static final int HISTOGRAM_BACKGROUND_TRANSPARENCY = 100;
    public static final int HISTOGRAM_FOREGROUND_TRANSPARENCY = 200;

    /**
     * The opacity of the rectangle drawn when using the Crop tool.
     * This value must be between 0 and 255.
     */
    public static final int CROP_OPACITY = 100;

    /**
     * The thickness rectangle's border drawn when using the Crop tool.
     */
    public static final int CROP_BORDER_SIZE = 5;

    /**
     * Because some filter are used in the code, they must have a peculiar name.
     * To ensure this name is the same throughout the code, they are stored there.
     */
    public static final String FILTER_MASK_NAME = "Create mask";
    public static final String FILTER_ROTATION = "Rotation";

    /**
     * When adding extras to a StartActivity's Intent, we must used a string key to transfer
     * information. To ensure this key is the same throughout the code, it is stored there.
     */
    public static final String ACTIVITY_EXTRA_CALLER = "CallerActivity";

}
