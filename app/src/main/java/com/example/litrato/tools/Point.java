package com.example.litrato.tools;

/**
 * A point is an object with two integers.
 * It is possible to create a point, copy a point, translate it, and test if two points are equals.
 * It is used by ImageViewZoomScroll.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2020-31-01
 */
public class Point {

    /**
     * Coordinates on the x axis.
     */
    public int x = 0;

    /**
     * Coordinates on the y axis.
     */
    public int y = 0;

    /**
     * Constructor using two coordinates.
     * @param x the coordinates on the x axis.
     * @param y the coordinates on the y axis.
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor using floating coordinates. They are converted to int.
     * @param x the coordinates on the x axis.
     * @param y the coordinates on the y axis.
     */
    public Point(float x, float y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    /**
     * Default constructor. If no coordinates are given, the point is (0, 0)
     */
    public Point() {}

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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEquals(Point other) {
        return isEquals(other.x, other.y);
    }

    /**
     * Returns true if both points are equal, false otherwise.
     * @param x the coordinates on the x axis.
     * @param y the coordinates on the y axis.
     * @return true if both points are equal, false otherwise.
     */
    public boolean isEquals(int x, int y) {
        return this.x == x && this.y == y;
    }

    public int distance(Point y){
        return (int)Math.sqrt((this.x+y.x)*(this.x+y.x)+(this.y+y.y)*(this.y+y.y));
    }
}
