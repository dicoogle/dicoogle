package pt.ua.dicoogle.sdk.datastructs.dim;

import java.util.Arrays;
import java.util.List;

public class BulkAnnotation {

    public enum PixelOrigin {
        FRAME, // Coordinates of this annotation are related to the frame (image section)
        VOLUME // Coordinates of this annotation are related to the Frame Matrix (whole image)
    }

    public enum AnnotationType {
        RECTANGLE, ELLIPSE, POLYGON, POLYLINE, POINT
    }

    public enum CoordinateType {
        TWO_DIMENSIONAL, //for image relative coordinates
        THREE_DIMENSIONAL //for coordinates in a Cartesian system defined by a frame of reference
    }

    private PixelOrigin pixelOrigin;

    private CoordinateType coordinateType;

    private AnnotationType annotationType;

    private String label;

    private List<Point2D> points;

    private double confidence;

    public PixelOrigin getPixelOrigin() {
        return pixelOrigin;
    }

    public void setPixelOrigin(PixelOrigin pixelOrigin) {
        this.pixelOrigin = pixelOrigin;
    }

    public CoordinateType getCoordinateType() {
        return coordinateType;
    }

    public void setCoordinateType(CoordinateType coordinateType) {
        this.coordinateType = coordinateType;
    }

    public AnnotationType getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(AnnotationType annotationType) {
        this.annotationType = annotationType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public void setPoints(List<Point2D> points) {
        this.points = points;
    }


    /**
     * Calculate the bounding box of this annotation.
     * @return a list of 4 points, representing a rectangle that contains the provided annotation.
     */
    public List<Point2D> getBoundingBox(){

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        switch (annotationType){
            case RECTANGLE:
                return this.getPoints();
            case POLYGON:
            case POLYLINE:
                for(Point2D p : this.getPoints()){
                    if(p.getX() > maxX)
                        maxX = p.getX();
                    if(p.getX() < minX)
                        minX = p.getX();

                    if(p.getY() > maxY)
                        maxY = p.getY();
                    if(p.getY() < minY)
                        minY = p.getY();
                }
                break;
            case ELLIPSE:
                minX = this.getPoints().get(0).getX();
                maxX = this.getPoints().get(1).getX();
                minY = this.getPoints().get(2).getY();
                maxY = this.getPoints().get(3).getY();
                break;
        }

        Point2D tl = new Point2D(minX, minY);
        Point2D tr = new Point2D(maxX, minY);
        Point2D bl = new Point2D(minX, maxY);
        Point2D br = new Point2D(maxX, maxY);

        return Arrays.asList(tl, tr, bl, br);
    }

}
