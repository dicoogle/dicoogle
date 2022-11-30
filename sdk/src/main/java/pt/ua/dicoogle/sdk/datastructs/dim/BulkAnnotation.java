package pt.ua.dicoogle.sdk.datastructs.dim;

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
}
