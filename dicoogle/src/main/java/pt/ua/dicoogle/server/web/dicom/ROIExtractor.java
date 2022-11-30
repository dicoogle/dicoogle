package pt.ua.dicoogle.server.web.dicom;

import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.sdk.datastructs.wsi.WSIFrame;
import pt.ua.dicoogle.sdk.datastructs.wsi.WSISopDescriptor;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ROIExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ROIExtractor.class);
    private final WSICache wsiCache;

    public ROIExtractor() {
        this.wsiCache = WSICache.getInstance();
    }

    public BufferedImage extractROI(String sopInstanceUID, BulkAnnotation bulkAnnotation) {
        DicomMetaData metaData;
        try {
            metaData = getDicomMetadata(sopInstanceUID);
        } catch (IOException e) {
            logger.error("Could not extract metadata", e);
            return null;
        }
        return extractROI(metaData, bulkAnnotation);
    }

    public BufferedImage extractROI(DicomMetaData dicomMetaData, BulkAnnotation bulkAnnotation) {

        ImageReader imageReader = getImageReader();

        if(imageReader == null)
            return null;

        if(dicomMetaData == null){
            return null;
        }

        DicomImageReadParam param;
        try{
            imageReader.setInput(dicomMetaData);
            param = (DicomImageReadParam) imageReader.getDefaultReadParam();
        } catch (Exception e){
            logger.error("Error setting image reader", e);
            return null;
        }

        WSISopDescriptor descriptor = new WSISopDescriptor();
        descriptor.extractData(dicomMetaData.getAttributes());

        try {
            return getROIFromAnnotation(bulkAnnotation, descriptor, imageReader, param);
        } catch (IllegalArgumentException e) {
            logger.error("Error writing ROI", e);
        }

        return null;
    }

    private static ImageReader getImageReader() {
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
        while (iter.hasNext()) {
            ImageReader reader = iter.next();
            if (reader instanceof DicomImageReader)
                return reader;
        }
        return null;
    }

    /**
     * Given an annotation and the details of the image, this method returns the frames that intersect with the annotation.
     * This method is meant for WSI images that are split into frames.
     * @param descriptor the WSI descriptor
     * @param annotation The annotation to intersect
     * @return a 2D matrix of frames that intersect this annotation.
     */
    private List<List<WSIFrame>> getFrameMatrixFromAnnotation(WSISopDescriptor descriptor, BulkAnnotation annotation) {

        //Number of tiles along the x direction, number of columns in the frame matrix
        int nx_tiles = (int) Math.ceil((descriptor.getTotalPixelMatrixColumns() * 1.0) / descriptor.getTileWidth());

        //Number of tiles along the y direction, number of rows in the frame matrix
        int ny_tiles = (int) Math.ceil((descriptor.getTotalPixelMatrixRows() * 1.0) / descriptor.getTileHeight());

        List<List<WSIFrame>> matrix = new ArrayList<>();

        switch (annotation.getAnnotationType()) {
            case RECTANGLE:
                // Calculate the starting position of the annotation in frame coordinates
                int x_c = (int) (annotation.getPoints().get(0).getX() / descriptor.getTileWidth());
                int y_c = (int) (annotation.getPoints().get(0).getY() / descriptor.getTileHeight());

                //Annotation is completely out of bounds, no intersection possible
                if(x_c > nx_tiles || y_c > ny_tiles)
                    return matrix;

                // Calculate the ending position of the annotation in frame coordinates
                int x_e = (int) (annotation.getPoints().get(3).getX() / descriptor.getTileWidth());
                int y_e = (int) (annotation.getPoints().get(3).getY() / descriptor.getTileHeight());

                //Annotation might be out of bonds, adjust that
                if(x_e > (nx_tiles - 1))
                    x_e = nx_tiles - 1;

                if(y_e > (ny_tiles - 1))
                    y_e = ny_tiles - 1;

                for (int i = y_c; i <= y_e ; i++) {
                    matrix.add(new ArrayList<>());
                    for (int j = x_c; j <= x_e; j++) {
                        WSIFrame frame = new WSIFrame(descriptor.getTileWidth(), descriptor.getTileHeight(), j, i, i * nx_tiles + j);
                        matrix.get(i).add(frame);
                    }
                }
                break;
        }

        return matrix;
    }

    /**
     * Given an annotation and an image, return the section of the image the annotation intersects.
     * It only works with rectangle type annotations.
     * @param annotation the annotation to intersect
     * @param descriptor descriptor of the WSI pyramid, contains information about the dimmensions of the image.
     * @param imageReader
     * @param param
     * @return the intersection of the annotation on the image.
     * @throws IllegalArgumentException when the annotation is not one of the supported types.
     */
    private BufferedImage getROIFromAnnotation(BulkAnnotation annotation, WSISopDescriptor descriptor, ImageReader imageReader, DicomImageReadParam param) throws IllegalArgumentException {
        if(annotation.getAnnotationType() != BulkAnnotation.AnnotationType.RECTANGLE){
            throw new IllegalArgumentException("Trying to build a ROI without a rectangle annotation");
        }

        Point2D annotationPoint1 = annotation.getPoints().get(0);
        Point2D annotationPoint2 = annotation.getPoints().get(3);

        int clipX = (int) Math.max(annotationPoint2.getX() - descriptor.getTotalPixelMatrixColumns(), 0);
        int clipY = (int) Math.max(annotationPoint2.getY() - descriptor.getTotalPixelMatrixRows(), 0);

        int annotationWidth = (int) annotation.getPoints().get(0).distance(annotation.getPoints().get(1)) - clipX;
        int annotationHeight = (int) annotation.getPoints().get(0).distance(annotation.getPoints().get(2)) - clipY;

        List<List<WSIFrame>> frameMatrix = getFrameMatrixFromAnnotation(descriptor, annotation);
        BufferedImage combined = new BufferedImage(annotationWidth, annotationHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.getGraphics();

        for (List<WSIFrame> matrix : frameMatrix) {
            for (WSIFrame frame : matrix) {
                BufferedImage bb;
                try {
                    bb = imageReader.read(frame.getFrameIndex(), param);
                } catch (IOException e) {
                    logger.error("Error building ROI, skipping entry", e);
                    continue;
                }

                //Calculate intersections between ROI and frame
                Point2D framePoint1 = new Point2D(frame.getX() * descriptor.getTileWidth(), frame.getY() * descriptor.getTileHeight());
                Point2D framePoint2 = new Point2D(framePoint1.getX() + bb.getWidth(), framePoint1.getY() + bb.getHeight());
                Point2D intersectionPoint1 = new Point2D(Math.max(annotationPoint1.getX(), framePoint1.getX()), Math.max(annotationPoint1.getY(), framePoint1.getY()));
                Point2D intersectionPoint2 = new Point2D(Math.min(annotationPoint2.getX(), framePoint2.getX()), Math.min(annotationPoint2.getY(), framePoint2.getY()));

                int startX = (int) (intersectionPoint1.getX() - annotationPoint1.getX());
                int startY = (int) (intersectionPoint1.getY() - annotationPoint1.getY());

                int endX = (int) (intersectionPoint2.getX() - annotationPoint1.getX());
                int endY = (int) (intersectionPoint2.getY() - annotationPoint1.getY());

                int frameStartX = (int) (intersectionPoint1.getX() - framePoint1.getX());
                int frameStartY = (int) (intersectionPoint1.getY() - framePoint1.getY());

                int frameEndX = (int) (intersectionPoint2.getX() - framePoint1.getX());
                int frameEndY = (int) (intersectionPoint2.getY() - framePoint1.getY());

                int deltaX = frameEndX - frameStartX;
                int deltaY = frameEndY - frameStartY;

                //This means that the frame is smaller than the intersection area
                //It can happen when we are on the edge of the image and the tiles do not have the dimensions stated in the DICOM file
                if (deltaX > bb.getWidth()) {
                    endX = frameEndX - bb.getWidth();
                    frameEndX = bb.getWidth();
                }

                if (deltaY > bb.getHeight()) {
                    endY = frameEndY - bb.getHeight();
                    frameEndY = bb.getHeight();
                }

                g.drawImage(bb, startX, startY,
                        endX, endY, frameStartX, frameStartY, frameEndX, frameEndY, null);

            }
        }

        g.dispose();
        return combined;
    }

    private DicomMetaData getDicomMetadata(String sop) throws IOException{
        return wsiCache.get(sop);
    }
}
