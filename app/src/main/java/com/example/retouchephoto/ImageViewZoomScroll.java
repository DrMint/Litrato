package com.example.retouchephoto;

import android.widget.ImageView;

import java.util.Set;

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
    private Point center = new Point(0,0);

    /**
     * How large can zoom be.
     */
    private float maxZoom = 5f;

    /**
     * Width of the source bitmap image.
     */
    private int bmpWidth;

    /**
     * Height of the source bitmap image.
     */
    private int bmpHeight;

    /**
     * The imageView on which the image will be drawn.
     */
    private ImageView iView;

    /**
     * The width of the rectangle.
     */
    private int newWidth;

    /**
     * The height of the rectangle.
     */
    private int newHeight;

    ImageViewZoomScroll(ImageView iView) {
        this.iView = iView;
        reset();
    }

    float getZoom() {return zoom;}
    int getX() {return center.x;}
    int getY() {return center.y;}
    int getNewWidth() {return newWidth;}
    int getNewHeight() {return newHeight;}

    void setBmp(int bmpWidth, int bmpHeight) {
        this.bmpWidth = bmpWidth;
        this.bmpHeight = bmpHeight;
        newWidth = bmpWidth;
        newHeight = bmpHeight;
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

    void setX(int x) {
        center.x = x;
        if (center.x < 0) center.x = 0;
        if (center.x > bmpWidth) center.x = bmpWidth;
        if (center.x + newWidth > bmpWidth) center.x = bmpWidth - newWidth;
    }

    void setY(int y) {
        center.y = y;
        if (center.y < 0) center.y = 0;
        if (center.y > bmpHeight) center.y = bmpHeight;
        if (center.y + newHeight > bmpHeight) center.y = bmpHeight - newHeight;
    }

    void setCenter(int x, int y) {
        setX(x - newWidth / 2);
        setY(y - newHeight / 2);
    }

    void setCenter(Point p) {
        setCenter(p.x, p.y);
    }

    void setMaxZoom(float maxZoom) {
        if (maxZoom < 1f) maxZoom = 1f;
        this.maxZoom = maxZoom;
    }

    void reset() {
        setZoom(1.f);
        setX(newWidth / 2);
        setY(newHeight / 2);
    }

    /**
     * Makes sure that newWidth and newHeight are up-to-date.
     */
    void refresh() {
        calculateNewBmpSize();
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

    /**
     * Returns the top left point of the image.
     * The image is scaled by the imageView to fit the imageView area without changing the aspect ratio.
     * It makes it unintuitive to get this important points.
     * @return the top left point of the image.
     */
    Point getDisplayedImageTopLeft() {

        int iViewWidth = iView.getMeasuredWidth();
        int iViewHeight = iView.getMeasuredHeight();

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
    Point getDisplayedImageDownRight() {
        Point result = getDisplayedImageTopLeft();
        result.x = iView.getMeasuredWidth() - result.x;
        result.y = iView.getMeasuredHeight() - result.y;
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
            iView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private void calculateNewBmpSize() {

        int iViewWidth = iView.getMeasuredWidth();
        int iViewHeight = iView.getMeasuredHeight();

        // If iView.getMeasuredWidth returns a null value, it means the UI is still not ready. Aborts.
        if (iViewWidth == 0 || iViewHeight == 0) return;

        // TODO: Pretty sure the "taller" and "wider" are mix up.
        // If the image ratio is taller than the imageView ratio
        if ((float) bmpWidth / bmpHeight > (float) (iViewWidth) / iViewHeight) {

            newWidth = (int) (1 / zoom * bmpWidth);

            // If the image ratio (after zooming in) is still taller than the imageView ratio
            if ((float) (newWidth) / bmpHeight > (float) (iViewWidth) / iViewHeight) {
                newHeight = bmpHeight;
            } else {
                newHeight = newWidth * iViewHeight / iViewWidth;
            }

            // If the image ratio is wider than the imageView ratio
        } else {

            newHeight = (int) (1 / zoom * bmpHeight);

            // If the image ratio (after zooming in) is still wider than the imageView ratio
            if ((float) (bmpWidth) / newHeight < (float) (iViewWidth) / iViewHeight) {
                newWidth = bmpWidth;
            } else {
                newWidth = newHeight * iViewWidth / iViewHeight;
            }
        }

        // Let's sanitize the values on last time before calling createBitmap.
        if (newWidth > bmpWidth) newWidth = bmpWidth;
        if (newHeight > bmpHeight) newHeight = bmpHeight;
        selectCorrectScaleType();
    }

}
