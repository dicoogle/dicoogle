package pt.ua.dicoogle.sdk.imageworker;

import java.io.File;
import java.net.URI;
import java.util.Objects;

/**
 * This object defines an Image ROI.
 * It has a physical location defined by an URI.
 * It is possible to write the image contents to a file defined by the URI.
 * The ROI has an x and y position that identify its origin in the source image.
 * The SOPInstanceUID defines where this ROI was extracted from.
 */
public class ImageROI {

    public enum FileType {
        JPEG (".jpg", "image/jpeg"),
        PNG (".png", "image/png");

        private final String extension;
        private final String mimeType;

        private FileType(String s, String mimeType) {
            this.extension = s;
            this.mimeType = mimeType;
        }

        public String getExtension() { return extension; }

        public String getMimeType() {return mimeType; }
    }

    private double x;

    private double y;

    private int width;

    private int height;

    private String sopInstanceUID;

    private URI uriROI;

    private FileType fileType;

    private ImageROI(URI uriROI){
        File f = new File(uriROI);
        if(!f.exists())
            throw new IllegalArgumentException(String.format("URI %s does not exist", uriROI.getPath()));
        this.x = 0;
        this.y = 0;
    }

    public ImageROI(String sopInstanceUID, int width, int height, URI uriROI, FileType fileType) {
        this(uriROI);
        this.width = width;
        this.height = height;
        this.uriROI = uriROI;
        this.fileType = fileType;
    }

    public ImageROI(String sopInstanceUID, int x, int y, int width, int height, URI uriROI, FileType fileType) {
        this(uriROI);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.uriROI = uriROI;
        this.fileType = fileType;
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

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public URI getUriROI() {
        return uriROI;
    }

    public void setUriROI(URI uriROI) {
        this.uriROI = uriROI;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageROI imageROI = (ImageROI) o;
        return Double.compare(imageROI.x, x) == 0 && Double.compare(imageROI.y, y) == 0 && width == imageROI.width && height == imageROI.height && Objects.equals(sopInstanceUID, imageROI.sopInstanceUID) && Objects.equals(uriROI, imageROI.uriROI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height, sopInstanceUID, uriROI);
    }
}
