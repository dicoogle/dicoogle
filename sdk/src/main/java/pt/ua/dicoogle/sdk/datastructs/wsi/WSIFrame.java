package pt.ua.dicoogle.sdk.datastructs.wsi;

public class WSIFrame {

    private int width;
    private int height;
    private int x;
    private int y;
    private int frameIndex;

    /**
     * Construct a WSI frame.
     * @param width the width of the frame
     * @param height the height of the frame
     * @param x the x coordinate of this frame in the frame matrix
     * @param y the y coordinate of this frame in the frame matrix
     * @param frameIndex the index of the frame that uniquely identifies this frame in the WSI
     */
    public WSIFrame(int width, int height, int x, int y, int frameIndex) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.frameIndex = frameIndex;
    }

    public WSIFrame(int width, int height) {
        this.width = width;
        this.height = height;
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

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }
}
