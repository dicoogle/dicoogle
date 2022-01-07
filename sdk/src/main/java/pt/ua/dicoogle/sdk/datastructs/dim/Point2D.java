package pt.ua.dicoogle.sdk.datastructs.dim;

public class Point2D {

    private int x;

    private int y;

    public Point2D() {
        x = 0;
        y = 0;
    }

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double distance(Point2D otherPoint) {
        return Math.sqrt(Math.pow(this.x - otherPoint.getX(), 2) + Math.pow(this.y - otherPoint.getY(), 2));
    }
}
