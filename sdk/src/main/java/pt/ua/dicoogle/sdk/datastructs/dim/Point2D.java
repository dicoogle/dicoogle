package pt.ua.dicoogle.sdk.datastructs.dim;

/**
 * A point in pixel coordinates, to be used to identify coordinates in pixel space.
 */
public class Point2D {

    private double x;
    private double y;

    public Point2D() {
        x = 0;
        y = 0;
    }

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double distance(Point2D otherPoint) {
        return Math.sqrt(Math.pow(this.x - otherPoint.getX(), 2) + Math.pow(this.y - otherPoint.getY(), 2));
    }
}
