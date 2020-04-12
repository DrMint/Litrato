package com.example.retouchephoto;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/**
 * This class implements a way to zoom and scroll.
 * It creates a rectangle that represent the part of the image that should be displayed by a imageView,
 * depending to a zoom level and center of zoom (a point).
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-31-01
 */
class ImageViewZoomScroll {

    /**
     * How much to image is zoomed in (i.e if zoomFactor = 2.0f, only 1/2 of the original is shown).
     */
    private float zoom = 1.f;

    /**
     * This is the center of zoom. Its coordinates are in pixels.
     * Minimum coordinates are 0, 0.
     * Maximum coordinate on the x axis is bitmap width - newWidth.
     */
    private final Point center = new Point(0,0);

    /**
     * How large can zoom be.
     */
    private float maxZoom = 5f;

    private Bitmap bmp;

    /**
     * The imageView on which the image will be drawn.
     */
    private final ImageView imageView;

    /**
     * The width of the rectangle.
     */
    private int newWidth;

    /**
     * The height of the rectangle.
     */
    private int newHeight;


    @SuppressLint("ClickableViewAccessibility")
    void setOnTouchListener(View.OnTouchListener myTouchListener) {
        imageView.setOnTouchListener(myTouchListener);
    }

    @SuppressWarnings("WeakerAccess")
    ImageViewZoomScroll(ImageView imageView, Bitmap bmp) {
        this.imageView = imageView;
        this.bmp = bmp;
    }

    ImageViewZoomScroll(ImageView imageView) {
        // Generate a empty image
        this(imageView, ImageTools.bitmapCreate(100,100));
    }

    private void refresh() {
        calculateNewBmpSize();
        if (newWidth == 0 || newHeight == 0) {
            imageView.setImageBitmap(bmp);
        } else {
            if (center.y + newHeight <= bmp.getHeight() && center.x + newWidth <= bmp.getWidth()) {
                imageView.setImageBitmap(createBitmap(bmp, center.x, center.y, newWidth, newHeight));
            }
        }
    }

    void reset() {
        setZoom(1.f);
        setX(newWidth / 2);
        setY(newHeight / 2);
        refresh();
    }

    /**
     * Translates to center by x on the X axis, and y on the Y axis
     * @param x the shift in X axis.
     * @param y the shift in Y axis.
     */
    void translate(int x, int y) {
        setX(center.x + x);
        setY(center.y + y);
    }

    void sanitizeBmpCoordinates(Point p) {
        if (p.x < 0) p.x = 0;
        if (p.y < 0) p.y = 0;
        if (p.x > bmp.getWidth() - 1) p.x = bmp.getWidth() - 1;
        if (p.y > bmp.getHeight() - 1) p.y = bmp.getHeight() - 1;
    }

    void setImageBitmap(Bitmap newBmp) {
        if (newBmp.getWidth() != bmp.getWidth() || newBmp.getHeight() != bmp.getHeight()) {
            reset();
        }
        this.bmp = newBmp;
        refresh();
    }

    void setZoom(float zoom) {
        this.zoom = zoom;
        if (zoom > maxZoom) this.zoom = maxZoom;
        if (this.zoom < 1f) {
            reset();
            return;
        }

        int tmpWidth = newWidth;
        int tmpHeight = newHeight;
        calculateNewBmpSize();
        translate((tmpWidth - newWidth) / 2, (tmpHeight - newHeight) / 2);
    }

    private void setX(int x) {
        center.x = x;
        if (center.x < 0) center.x = 0;
        if (center.x > bmp.getWidth()) center.x = bmp.getWidth();
        if (center.x + newWidth > bmp.getWidth()) center.x = bmp.getWidth() - newWidth;
    }

    private void setY(int y) {
        center.y = y;
        if (center.y < 0) center.y = 0;
        if (center.y > bmp.getHeight()) center.y = bmp.getHeight();
        if (center.y + newHeight > bmp.getHeight()) center.y = bmp.getHeight() - newHeight;
    }

    @SuppressWarnings("WeakerAccess")
    void setCenter(int x, int y) {
        setX(x - newWidth / 2);
        setY(y - newHeight / 2);
    }

    void setCenter(Point p) {
        setCenter(p.x, p.y);
    }

    @SuppressWarnings("SameParameterValue")
    void setMaxZoom(float maxZoom) {
        if (maxZoom < 1f) maxZoom = 1f;
        this.maxZoom = maxZoom;
    }

    float getZoom() {return zoom;}

    /**
     * Returns the top left point of the image.
     * The image is scaled by the imageView to fit the imageView area without changing the aspect ratio.
     * It makes it unintuitive to get this important points.
     * @return the top left point of the image.
     */
    private Point getDisplayedImageTopLeft() {

        int iViewWidth = imageView.getMeasuredWidth();
        int iViewHeight = imageView.getMeasuredHeight();

        float displayedImageRatio = (float) newWidth / newHeight;
        float imageViewRatio = (float) iViewWidth / iViewHeight;

        int x;
        int y;

        // If the displayed image's ratio is close enough to its imageView's ratio
        if (Math.abs(displayedImageRatio - imageViewRatio) < Settings.IMAGE_RATIO_PRECISION) {
            x = 0;
            y = 0;

            // If the displayed image's ratio is wider than its imageView's ratio
        }  else if (displayedImageRatio > imageViewRatio) {
            x = 0;
            y = (int) (iViewHeight - iViewWidth / displayedImageRatio) / 2;

            // If the displayed image's ratio is taller than its imageView's ratio
        } else {
            x = (int) (iViewWidth - iViewHeight * displayedImageRatio) / 2;
            y = 0;
        }
        return new Point(x, y);
    }

    /**
     * Returns the down right point of the image.
     * The image is scaled by the imageView to fit the imageView area without changing the aspect ratio.
     * It makes it unintuitive to get this important points.
     * @return the down right point of the image.
     */
    private Point getDisplayedImageDownRight() {
        Point result = getDisplayedImageTopLeft();
        result.x = imageView.getMeasuredWidth() - result.x;
        result.y = imageView.getMeasuredHeight() - result.y;
        return result;
    }

    Point imageViewTouchPointToBmpCoordinates(Point touch) {
        Point a = getDisplayedImageTopLeft();
        Point b = getDisplayedImageDownRight();
        Point result = new Point();
        result.x = (touch.x - a.x) * newWidth / (b.x - a.x) + center.x;
        result.y = (touch.y - a.y) * newHeight / (b.y - a.y) + center.y;
        return result;
    }

    private void selectCorrectScaleType() {
        Point p = getDisplayedImageTopLeft();
        if (p.isEquals(0,0)) {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private void calculateNewBmpSize() {

        int iViewWidth = imageView.getMeasuredWidth();
        int iViewHeight = imageView.getMeasuredHeight();

        // If iView.getMeasuredWidth returns a null value, it means the UI is still not ready. Aborts.
        if (iViewWidth == 0 || iViewHeight == 0) {
            newWidth = bmp.getWidth();
            newHeight = bmp.getHeight();
            return;
        }

        // If the image ratio is taller than the imageView ratio
        if ((float) bmp.getWidth() / bmp.getHeight() > (float) (iViewWidth) / iViewHeight) {

            newWidth = (int) (1 / zoom * bmp.getWidth());

            // If the image ratio (after zooming in) is still taller than the imageView ratio
            if ((float) (newWidth) / bmp.getHeight() > (float) (iViewWidth) / iViewHeight) {
                newHeight = bmp.getHeight();
            } else {
                newHeight = newWidth * iViewHeight / iViewWidth;
            }

            // If the image ratio is wider than the imageView ratio
        } else {

            newHeight = (int) (1 / zoom * bmp.getHeight());

            // If the image ratio (after zooming in) is still wider than the imageView ratio
            if ((float) (bmp.getWidth()) / newHeight < (float) (iViewWidth) / iViewHeight) {
                newWidth = bmp.getWidth();
            } else {
                newWidth = newHeight * iViewWidth / iViewHeight;
            }
        }

        // Let's sanitize the values on last time before calling createBitmap.
        if (newWidth > bmp.getWidth()) newWidth = bmp.getWidth();
        if (newHeight > bmp.getHeight()) newHeight = bmp.getHeight();
        selectCorrectScaleType();
    }

    int getPixelAt(Point p) {
        sanitizeBmpCoordinates(p);
        return bmp.getPixel(p.x, p.y);
    }
}
