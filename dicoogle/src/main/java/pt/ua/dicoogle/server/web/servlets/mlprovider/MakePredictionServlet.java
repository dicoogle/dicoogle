package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String jsonString = IOUtils.toString(request.getReader());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode body = mapper.readTree(jsonString);

        if(!body.has("uid")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "SOPInstanceUID provided was invalid");
            return;
        }

        if(!body.has("provider")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider provided was invalid");
            return;
        }

        if(!body.has("modelID")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Model identifier was invalid");
            return;
        }

        if(!body.has("type")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Annotation type must be provided");
            return;
        }

        String sopInstanceUID = body.get("uid").asText();
        String baseSopInstanceUID = body.get("baseUID").asText();
        String provider = body.get("provider").asText();
        String modelID = body.get("modelID").asText();
        String type = body.get("type").asText();
        boolean wsi = body.get("wsi").asBoolean();
        List<Point2D> points = mapper.readValue(body.get("points").toString(), new TypeReference<List<Point2D>>(){});

        BulkAnnotation annotation = new BulkAnnotation();
        annotation.setPoints(points);
        annotation.setAnnotationType(BulkAnnotation.AnnotationType.valueOf(type));

        DicomMetaData dicomMetaData = this.getDicomMetadata(sopInstanceUID);

        BufferedImage bi = roiExtractor.extractROI(dicomMetaData, annotation);

        // mount the resulting memory stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ImageIO.write(bi, "jpg", bos);

        Task<MLPrediction> task = PluginController.getInstance().makePredictionOverImage(bos, provider, modelID);
        if(task == null){
            response.sendError(Status.SERVER_ERROR_INTERNAL.getCode(), "Could not create prediction task");
            return;
        }

        task.onCompletion(() -> {
            try {
                MLPrediction prediction = task.get();

                if(prediction == null){
                    log.error("Provider returned null prediction");
                    response.sendError(Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
                    return;
                }

                // Coordinates need to be converted if we're working with WSI
                if(wsi && !prediction.getAnnotations().isEmpty()){
                    WSISopDescriptor descriptor = new WSISopDescriptor();
                    descriptor.extractData(dicomMetaData.getAttributes());
                    DicomMetaData base = this.getDicomMetadata(baseSopInstanceUID);
                    WSISopDescriptor baseDescriptor = new WSISopDescriptor();
                    baseDescriptor.extractData(base.getAttributes());
                    double scale = (descriptor.getTotalPixelMatrixRows() * 1.0) / baseDescriptor.getTotalPixelMatrixRows();
                    Point2D tl = annotation.getBoundingBox().get(0);
                    convertCoordinates(prediction, tl, scale);
                }

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
     * @param tl top left corner of the bounding box of the annotation
     * @param scale to transform coordinates
     * @return the ml prediction with the converted coordinates.
     */
    private void convertCoordinates(MLPrediction prediction, Point2D tl, double scale){
        for(BulkAnnotation ann : prediction.getAnnotations()){
            for(Point2D p : ann.getPoints()){
                p.setX((p.getX() + tl.getX())/scale);
                p.setY((p.getY() + tl.getY())/scale);
            }
        }
    }
}
