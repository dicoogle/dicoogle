package pt.ua.dicoogle.server.web.dicom;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.sdk.datastructs.wsi.WSIFrame;
import pt.ua.dicoogle.sdk.datastructs.wsi.WSISopDescriptor;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ROIExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ROIExtractor.class);
    private static final String EXTENSION_GZIP = ".gz";
    private static final int BUFFER_SIZE = 8192;

    public BufferedImage extractROI(StorageInputStream sis, BulkAnnotation bulkAnnotation) {

        ImageReader imageReader = getImageReader();

        DicomMetaData dicomMetaData;
        try {
            dicomMetaData = getDicomMetadata(sis);
        } catch (IOException e) {
            logger.error("Error reading DICOM file", e);
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
        int nx_tiles = (int) Math.ceil(descriptor.getTotalPixelMatrixColumns() / descriptor.getTileWidth());

        //Number of tiles along the y direction, number of rows in the frame matrix
        int ny_tiles = (int) Math.ceil(descriptor.getTotalPixelMatrixRows() / descriptor.getTileHeight());

        List<List<WSIFrame>> matrix = new ArrayList<>();

        switch (annotation.getAnnotationType()) {
            case RECTANGLE:
                // Calculate the starting position of the annotation in frame coordinates
                int x_c = (int) Math.floor(annotation.getPoints().get(0).getX() / descriptor.getTileWidth());
                int y_c = (int) Math.floor(annotation.getPoints().get(0).getY() / descriptor.getTileHeight());

                //Annotation is completely out of bounds, no intersection possible
                if(x_c > nx_tiles || y_c > ny_tiles)
                    return matrix;

                // Calculate the ending position of the annotation in frame coordinates
                int x_e = (int) Math.floor(annotation.getPoints().get(3).getX() / descriptor.getTileWidth());
                int y_e = (int) Math.floor(annotation.getPoints().get(3).getY() / descriptor.getTileHeight());

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
     * @param annotation
     * @param descriptor
     * @param imageReader
     * @param param
     * @return
     * @throws IllegalArgumentException
     */
    private BufferedImage getROIFromAnnotation(BulkAnnotation annotation, WSISopDescriptor descriptor, ImageReader imageReader, DicomImageReadParam param) throws IllegalArgumentException {
        if(annotation.getAnnotationType() != BulkAnnotation.AnnotationType.RECTANGLE){
            throw new IllegalArgumentException("Trying to build a ROI without a rectangle annotation");
        }

        Point2D annotationPoint1 = annotation.getPoints().get(0);
        Point2D annotationPoint2 = annotation.getPoints().get(3);

        int clipX = Math.max(annotationPoint2.getX() - descriptor.getTotalPixelMatrixColumns(), 0);
        int clipY = Math.max(annotationPoint2.getY() - descriptor.getTotalPixelMatrixRows(), 0);

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

                int startX = intersectionPoint1.getX() - annotationPoint1.getX();
                int startY = intersectionPoint1.getY() - annotationPoint1.getY();

                int endX = intersectionPoint2.getX() - annotationPoint1.getX();
                int endY = intersectionPoint2.getY() - annotationPoint1.getY();

                int frameStartX = intersectionPoint1.getX() - framePoint1.getX();
                int frameStartY = intersectionPoint1.getY() - framePoint1.getY();

                int frameEndX = intersectionPoint2.getX() - framePoint1.getX();
                int frameEndY = intersectionPoint2.getY() - framePoint1.getY();

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

    private DicomMetaData getDicomMetadata(StorageInputStream sis) throws IOException{
        Attributes fmi;
        Attributes dataset;
        DicomInputStream dis;

        if(sis == null){
            logger.info("Storage == null");
            throw new InvalidParameterException("Could not find the desired URI");
        }

        String filePath = sis.getURI().getPath();
        if (filePath.endsWith(EXTENSION_GZIP)){
            InputStream inStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE));
            dis = new DicomInputStream(inStream);
        }
        else {
            dis = new DicomInputStream(new File(filePath));
        }
        dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
        dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
        fmi = dis.readFileMetaInformation();
        dataset = dis.readDataset(-1, -1);
        return new DicomMetaData(fmi, dataset);
    }

}
