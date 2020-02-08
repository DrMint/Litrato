package com.example.retouchephoto;

/**
 * This class is where all the constant are store. It gives easy access to some settings.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-31-01
 */
public class Settings {

    /**
     * The maximum size of a loaded image.
     * The image will not be bigger than MAXSIZE * MAXSIZE(the image ratio will be conserved).
     */
    static final int IMPORTED_BMP_SIZE = 1000;

    /**
     * How much the user can zoom on the image.
     * For example: 5f means display 1/5 a the image.
     */
    static final float MAX_ZOOM_LEVEL = 5f;

    /**
     * How much the user can zoom on the image.
     * For example: 5f means display 1/5 a the image.
     */
    static final float DOUBLE_TAP_ZOOM = 3f;

    /**
     * ImageViewZoomScroll is using this value when comparing the imageView ratio
     * and the image ratio. Because those two values will never be exactly the same,
     * this value is how far off is still considered equal.
     */
    static final float IMAGE_RATIO_PRECISION = 0.01f;
}
