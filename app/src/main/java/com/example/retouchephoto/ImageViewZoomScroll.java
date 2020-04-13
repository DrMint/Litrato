package com.example.retouchephoto;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
    private final Point topLeft = new Point(0,0);

    /**
     * How large can zoom be.
     */
    private float maxZoom = 5f;
    private float minZoom;

    private int bmpWidth = 0;
    private int bmpHeight = 0;

    private int viewWidth = 0;
    private int viewHeight = 0;

    boolean horizontalScroll = false;
    boolean verticalScroll = false;


    /**
     * The imageView on which the image will be drawn.
     */
    private final ImageView imageView;


    @SuppressLint("ClickableViewAccessibility")
    void setOnTouchListener(View.OnTouchListener myTouchListener) {
        imageView.setOnTouchListener(myTouchListener);
    }

    ImageViewZoomScroll(ImageView imageView) {
        this.imageView = imageView;
    }


    private void setX(int x) {
        if (horizontalScroll) {
            topLeft.x = x;
            if (topLeft.x < 0 ) {
                topLeft.x = 0;
            } else {
                int maxAcceptableX = (int) -(viewWidth / zoom - bmpWidth);
                if (topLeft.x > maxAcceptableX) topLeft.x = maxAcceptableX;
            }
        } else {
            topLeft.x = 0;
        }
        refresh();
    }

    private void setY(int y) {
        if (verticalScroll) {
            topLeft.y = y;
            if (topLeft.y < 0 ) {
                topLeft.y = 0;
            } else {
                int maxAcceptableY = (int) -(viewHeight / zoom - bmpHeight);
                if (topLeft.y > maxAcceptableY) topLeft.y = maxAcceptableY;
            }
        } else {
            topLeft.y = 0;
        }
        refresh();
    }

    void setTopLeft(int x, int y) {
        setX(x);
        setY(y);
    }

    void setTopLeft(Point p) {
        setTopLeft(p.x, p.y);
    }

    void setCenter(Point p) {
        p.x -= bmpWidth / zoom / 2;
        p.y -= bmpHeight / zoom / 2;
        setTopLeft(p);
    }

    void setZoom(float zoom) {
        float currentZoom = this.zoom;
        this.zoom = zoom;
        if (zoom > maxZoom * minZoom) {
            this.zoom = maxZoom * minZoom;
        } else if (zoom < minZoom) {
            this.zoom = minZoom;
        } else {

            //float diff = zoom - currentZoom;

            //translate((int) (diff * (bmpWidth / zoom) / 2), (int) (diff * (bmpHeight / zoom) / 2));
        }
        horizontalScroll = bmpWidth * this.zoom > viewWidth;
        verticalScroll = bmpHeight * this.zoom > viewHeight;
        refresh();
    }

    private void refresh() {
        Matrix test = new Matrix();
        if (horizontalScroll) {
            test.setTranslate(-topLeft.x, -topLeft.y);
        } else {
            //Log.wtf("Test", zoom + " " + bmpWidth);
            test.setTranslate(0, -topLeft.y);
        }

        test.postScale(zoom, zoom);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(test);
    }

    void reset() {
        setZoom(minZoom);
        setX(0);
        setY(0);
        refresh();
    }

    /**
     * Translates to center by x on the X axis, and y on the Y axis
     * @param x the shift in X axis.
     * @param y the shift in Y axis.
     */
    void translate(int x, int y) {
        setX(topLeft.x + x);
        setY(topLeft.y + y);
    }

    void sanitizeBmpCoordinates(Point p) {
        if (p.x < 0) p.x = 0;
        if (p.y < 0) p.y = 0;
        if (p.x > bmpWidth - 1) p.x = bmpWidth - 1;
        if (p.y > bmpHeight - 1) p.y = bmpHeight - 1;
    }

    void setImageBitmap(Bitmap newBmp) {

        int newBmpWidth = newBmp.getWidth();
        int newBmpHeight = newBmp.getHeight();

        if (newBmpWidth != bmpWidth || newBmpHeight != bmpHeight) {
            bmpWidth = newBmpWidth;
            bmpHeight = newBmpHeight;
            setInternalValues();
        }

        imageView.setImageBitmap(newBmp);
        refresh();
    }

    void setInternalValues() {
        viewWidth = imageView.getMeasuredWidth();
        viewHeight = imageView.getMeasuredHeight();

        horizontalScroll = bmpWidth * zoom > viewWidth;
        verticalScroll = bmpHeight * zoom > viewHeight;

        minZoom = Math.min((float) viewWidth / bmpWidth, (float) viewHeight / bmpHeight);
        reset();
    }

    @SuppressWarnings("SameParameterValue")
    void setMaxZoom(float maxZoom) {
        if (maxZoom < 1f) maxZoom = 1f;
        this.maxZoom = maxZoom;
    }

    float getZoom() {return zoom;}

    Point imageViewTouchPointToBmpCoordinates(Point p) {
        p.x = (int) (topLeft.x + (p.x / zoom));
        p.y = (int) (topLeft.y + (p.y / zoom));
        return p;
    }

    int getPixelAt(Point p) {
        p = imageViewTouchPointToBmpCoordinates(p);
        return ((BitmapDrawable)imageView.getDrawable()).getBitmap().getPixel(p.x, p.y);
    }
}
