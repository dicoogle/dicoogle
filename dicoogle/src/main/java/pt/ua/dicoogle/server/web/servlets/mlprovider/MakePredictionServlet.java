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
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.server.web.dicom.WSISopDescriptor;
import pt.ua.dicoogle.sdk.mlprovider.MLPrediction;
import pt.ua.dicoogle.sdk.mlprovider.MLPredictionRequest;
import pt.ua.dicoogle.sdk.task.Task;
import pt.ua.dicoogle.server.web.dicom.ROIExtractor;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
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

        // Validate the common attributes between WSI and non-WSI requests

        if(!body.has("level")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "DIM level provided was invalid");
            return;
        }

        if(!body.has("uid")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "DIM UID provided was invalid");
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

        String provider = body.get("provider").asText();
        String modelID = body.get("modelID").asText();
        boolean wsi = body.get("wsi").asBoolean();
        DimLevel level = DimLevel.valueOf(body.get("level").asText().toUpperCase());
        String dimUID = body.get("uid").asText();

        Task<MLPrediction> task;

        if(wsi){

            if(!body.has("points") || !body.has("type") || !body.has("baseUID")){
                response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Insufficient data to build request");
                return;
            }

            if(level != DimLevel.INSTANCE){
                response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Only Instance level is supported with WSI");
                return;
            }

            String baseSopInstanceUID = body.get("baseUID").asText();

            BulkAnnotation.AnnotationType type = BulkAnnotation.AnnotationType.valueOf(body.get("type").asText());
            List<Point2D> points = mapper.readValue(body.get("points").toString(), new TypeReference<List<Point2D>>(){});
            task = sendWSIRequest(provider, modelID, baseSopInstanceUID, dimUID, type, points, response);

        } else {
            task = sendRequest(provider, modelID, level, dimUID, response);
        }

        if(task == null){
            response.sendError(Status.SERVER_ERROR_INTERNAL.getCode(), "Could not build prediction request");
            return;
        }

        task.run();
    }

    private Task<MLPrediction> sendWSIRequest(String provider, String modelID, String baseSopInstanceUID, String uid,
                                              BulkAnnotation.AnnotationType roiType, List<Point2D> roi, HttpServletResponse response){

        ObjectMapper mapper = new ObjectMapper();
        MLPredictionRequest predictionRequest = new MLPredictionRequest(true, DimLevel.INSTANCE, uid, modelID);
        BulkAnnotation annotation = new BulkAnnotation();
        annotation.setPoints(roi);
        annotation.setAnnotationType(roiType);

        try {
            DicomMetaData dicomMetaData = this.getDicomMetadata(uid);
            BufferedImage bi = roiExtractor.extractROI(dicomMetaData, annotation);
            predictionRequest.setRoi(bi);
            Task<MLPrediction> task = PluginController.getInstance().makePredictionOverImage(provider, predictionRequest);
            if(task != null){
                task.onCompletion(() -> {
                    try {
                        MLPrediction prediction = task.get();

                        if(prediction == null){
                            log.error("Provider returned null prediction");
                            response.sendError(Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
                            return;
                        }

                        // Coordinates need to be converted if we're working with WSI
                        if(!prediction.getAnnotations().isEmpty()){
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
            }
            return task;
        } catch (IOException e) {
            return null;
        }
    }

    private Task<MLPrediction> sendRequest(String provider, String modelID, DimLevel level, String dimUID, HttpServletResponse response){
        ObjectMapper mapper = new ObjectMapper();
        MLPredictionRequest predictionRequest = new MLPredictionRequest(true, level, dimUID, modelID);
        Task<MLPrediction> task = PluginController.getInstance().makePredictionOverImage(provider, predictionRequest);
        if(task != null){
            task.onCompletion(() -> {
                try {
                    MLPrediction prediction = task.get();

                    if(prediction == null){
                        log.error("Provider returned null prediction");
                        response.sendError(Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
                        return;
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
        }

        return task;
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
