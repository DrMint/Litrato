package com.example.litrato.tools;

import android.graphics.Bitmap;

/**
 * A point is an object with two integers.
 * It is possible to create a point, copy a point, translate it, and test if two points are equals.
 * It is used by ImageViewZoomScroll.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class PointPercentage {

    /**
     * Coordinates on the x axis in %.
     */
    public float x = 0;

    /**
     * Coordinates on the y axis in %.
     */
    public float y = 0;

    /**
     * Constructor using floating coordinates.
     * @param x the coordinates on the x axis in %.
     * @param y the coordinates on the y axis in %.
     */
    public PointPercentage(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor using floating coordinates.
     *
     */
    public PointPercentage(Point p, Bitmap bmp) {
        this.x = (float) p.x / bmp.getWidth();
        this.y = (float) p.y / bmp.getHeight();
    }

    /**
     * Default constructor. If no coordinates are given, the point is (0, 0)
     */
    public PointPercentage() {}

    /**
     * Translates this point coordinates according to the param x and y.
     * @param x translation on the X axis
     * @param y translation on the y axis
     */
    @SuppressWarnings("unused")
    public void translate(int x, int y) {
        this.x += x;
        this.y += y;
    }

    /**
     * Change the coordinates of the point
     * @param x coordinates on the X axis
     * @param y coordinates on the y axis
     */
    @SuppressWarnings("WeakerAccess")
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy the coordinates of a point to this point
     * @param p to point to be copied
     */
    @SuppressWarnings("unused")
    public void set(Point p) {
        set(p.x, p.y);
    }

    /**
     * Returns a copy of the point.
     * @return a copy of the point.
     */
    @SuppressWarnings("unused")
    public Point copy() {
        return new Point(x, y);
    }

    /**
     * Returns true if both points are equal, false otherwise.
     * @param other the other point.
     * @return true if both points are equal, false otherwise.
     */
    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
    public boolean isEquals(PointPercentage other) {
        return isEquals(other.x, other.y);
    }

    /**
     * Returns true if both points are equal, false otherwise.
     * @param x the coordinates on the x axis.
     * @param y the coordinates on the y axis.
     * @return true if both points are equal, false otherwise.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isEquals(float x, float y) {
        return this.x == x && this.y == y;
    }

}
