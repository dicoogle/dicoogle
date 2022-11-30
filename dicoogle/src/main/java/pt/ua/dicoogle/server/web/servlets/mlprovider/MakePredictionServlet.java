package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.sdk.datastructs.wsi.WSISopDescriptor;
import pt.ua.dicoogle.sdk.mlprovider.MLPrediction;
import pt.ua.dicoogle.sdk.task.Task;
import pt.ua.dicoogle.server.web.dicom.ROIExtractor;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MakePredictionServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MakePredictionServlet.class);

    private final ROIExtractor roiExtractor;

    private final WSICache wsiCache;

    /**
     * Creates ROI servlet servlet.
     *
     */
    public MakePredictionServlet() {
        this.wsiCache = WSICache.getInstance();
        this.roiExtractor = new ROIExtractor();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String sopInstanceUID = request.getParameter("uid");
        String x = request.getParameter("x");
        String y = request.getParameter("y");
        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String provider = request.getParameter("provider");
        String wsi = request.getParameter("wsi");
        final String baseSopInstanceUID = request.getParameter("baseUID");

        if(sopInstanceUID == null || sopInstanceUID.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "SOPInstanceUID provided was invalid");
            return;
        }

        if(provider == null || provider.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider provided was invalid");
            return;
        }

        if(x == null || x.isEmpty() || y == null || y.isEmpty() || width == null || width.isEmpty() || height == null || height.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "ROI provided was invalid");
            return;
        }

        if(wsi != null && wsi.equals("true")){
            if(baseSopInstanceUID == null || baseSopInstanceUID.isEmpty()){
                response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "WSI was selected but a valid base SopInstanceUID was not provided");
                return;
            }
        }

        int nX, nY, nWidth, nHeight;

        try{
            nX = Integer.parseInt(x);
            nY = Integer.parseInt(y);
            nWidth = Integer.parseInt(width);
            nHeight = Integer.parseInt(height);
        } catch (NumberFormatException e){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "ROI provided was invalid");
            return;
        }

        BulkAnnotation annotation = new BulkAnnotation();
        Point2D tl = new Point2D(nX, nY);
        Point2D tr = new Point2D(nX + nWidth, nY);
        Point2D bl = new Point2D(nX, nY + nHeight);
        Point2D br = new Point2D(nX + nWidth, nY + nHeight);
        List<Point2D> points = new ArrayList<>();
        points.add(tl); points.add(tr); points.add(bl); points.add(br);
        annotation.setPoints(points);
        annotation.setAnnotationType(BulkAnnotation.AnnotationType.RECTANGLE);

        DicomMetaData dicomMetaData = this.getDicomMetadata(sopInstanceUID);

        BufferedImage bi = roiExtractor.extractROI(dicomMetaData, annotation);

        // mount the resulting memory stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ImageIO.write(bi, "jpg", bos);

        Task<MLPrediction> task = PluginController.getInstance().makePredictionOverImage(bos, provider);
        if(task == null){
            response.sendError(Status.SERVER_ERROR_INTERNAL.getCode(), "Could not create prediction task");
            return;
        }

        task.onCompletion(() -> {
            try {
                MLPrediction prediction = task.get();

                // Coordinates need to be converted if we're working with WSI
                if(wsi.equals("true")){
                    WSISopDescriptor descriptor = new WSISopDescriptor();
                    descriptor.extractData(dicomMetaData.getAttributes());
                    DicomMetaData base = this.getDicomMetadata(baseSopInstanceUID);
                    WSISopDescriptor baseDescriptor = new WSISopDescriptor();
                    baseDescriptor.extractData(base.getAttributes());
                    double scale = (descriptor.getTotalPixelMatrixRows() * 1.0) / baseDescriptor.getTotalPixelMatrixRows();
                    if(scale != 1){ // scale will be 1 if the levels match, no conversion needed
                        convertCoordinates(prediction, nX, nY, scale);
                    }
                }

                ObjectMapper mapper = new ObjectMapper();
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                mapper.writeValue(out, prediction);
                out.close();
                out.flush();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Could not make prediction", e);
                try {
                    response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Could not make prediction");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        task.run();
    }

    private DicomMetaData getDicomMetadata(String sop) throws IOException{
        return wsiCache.get(sop);
    }

    /**
     * When working with WSI, it is convenient to have coordinates relative to the base of the pyramid.
     * This method takes care of that.
     * @param prediction
     * @param scale to transform coordinates
     * @return the ml prediction with the converted coordinates.
     */
    private void convertCoordinates(MLPrediction prediction, int x, int y, double scale){
        for(BulkAnnotation ann : prediction.getAnnotations()){
            for(Point2D p : ann.getPoints()){
                p.setX((p.getX() + x)/scale);
                p.setY((p.getY() + y)/scale);
            }
        }
    }
}
