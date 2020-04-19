package com.example.litrato.activities.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.example.litrato.tools.Point;

/**
 * This class implements a way to zoom and scroll.
 * It creates a rectangle that represent the part of the image that should be displayed by a imageView,
 * depending to a zoom level and center of zoom (a point).
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-31-01
 */
public class ImageViewZoomScroll {

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

    private boolean horizontalScroll = false;
    private boolean verticalScroll = false;


    /**
     * The imageView on which the image will be drawn.
     */
    private final ImageView imageView;


    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchListener(View.OnTouchListener myTouchListener) {
        imageView.setOnTouchListener(myTouchListener);
    }

    public ImageViewZoomScroll(ImageView imageView) {
        this.imageView = imageView;
    }

    @SuppressWarnings("WeakerAccess")
    public void setTopLeft(int x, int y) {
        setX(x);
        setY(y);
    }

    @SuppressWarnings("WeakerAccess")
    public void setTopLeft(Point p) {
        setTopLeft(p.x, p.y);
    }

    public void setCenter(Point p) {
        p.x -= bmpWidth / zoom / 2;
        p.y -= bmpHeight / zoom / 2;
        setTopLeft(p);
    }

    public void setZoom(float zoom) {
        if (zoom > maxZoom * minZoom) {
            this.zoom = maxZoom * minZoom;
        } else if (zoom < minZoom) {
            this.zoom = minZoom;
        } else {
            // We calculate the position of the center point
            Point p = new Point(viewWidth, viewHeight);
            Point p2 = p.copy();
            imageViewTouchPointToBmpCoordinates(p);
            this.zoom = zoom;
            // We calculate the new position of the center point
            imageViewTouchPointToBmpCoordinates(p2);
            // We divide this difference by two to keep the center at the center.
            translate((p.x - p2.x) / 2, (p.y - p2.y) / 2);
        }
        horizontalScroll = bmpWidth * this.zoom > viewWidth;
        verticalScroll = bmpHeight * this.zoom > viewHeight;

        refresh();
    }


    public void reset() {
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
    public void translate(int x, int y) {
        setX(topLeft.x + x);
        setY(topLeft.y + y);
    }

    public void sanitizeBmpCoordinates(Point p) {
        if (p.x < 0) p.x = 0;
        if (p.y < 0) p.y = 0;
        if (p.x > bmpWidth - 1) p.x = bmpWidth - 1;
        if (p.y > bmpHeight - 1) p.y = bmpHeight - 1;
    }

    public void setImageBitmap(Bitmap newBmp) {

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

    public void setInternalValues() {
        viewWidth = imageView.getMeasuredWidth();
        viewHeight = imageView.getMeasuredHeight();

        horizontalScroll = bmpWidth * zoom > viewWidth;
        verticalScroll = bmpHeight * zoom > viewHeight;

        minZoom = Math.min((float) viewWidth / bmpWidth, (float) viewHeight / bmpHeight);
        reset();
    }

    @SuppressWarnings("SameParameterValue")
    public void setMaxZoom(float maxZoom) {
        if (maxZoom < 1f) maxZoom = 1f;
        this.maxZoom = maxZoom;
    }

    public float getZoom() {return zoom;}

    public Point imageViewTouchPointToBmpCoordinates(Point p) {
        p.x = (int) (topLeft.x + (p.x / zoom));
        p.y = (int) (topLeft.y + (p.y / zoom));
        return p;
    }

    public int getPixelAt(Point p) {
        p = imageViewTouchPointToBmpCoordinates(p);
        sanitizeBmpCoordinates(p);
        return ((BitmapDrawable)imageView.getDrawable()).getBitmap().getPixel(p.x, p.y);
    }

    public boolean hasHorizontalScroll() {return horizontalScroll;}
    public boolean hasVerticalScroll() {return verticalScroll;}

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
            topLeft.x = (int) -(((viewWidth - bmpWidth * zoom) / 2) / zoom);
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

    private void refresh() {
        Matrix test = new Matrix();
        test.setTranslate(-topLeft.x, -topLeft.y);
        test.postScale(zoom, zoom);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(test);
    }


}
