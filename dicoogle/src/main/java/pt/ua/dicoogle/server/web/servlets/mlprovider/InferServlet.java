package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.server.web.dicom.WSISopDescriptor;
import pt.ua.dicoogle.sdk.mlprovider.MLInference;
import pt.ua.dicoogle.sdk.mlprovider.MLInferenceRequest;
import pt.ua.dicoogle.sdk.task.Task;
import pt.ua.dicoogle.server.web.dicom.ROIExtractor;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class InferServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(InferServlet.class);

    private final ROIExtractor roiExtractor;

    private final WSICache wsiCache;

    /**
     * Creates ROI servlet servlet.
     *
     */
    public InferServlet() {
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
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "DIM level provided was invalid");
            return;
        }

        if(!body.has("uid")){
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "DIM UID provided was invalid");
            return;
        }

        if(!body.has("provider")){
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider provided was invalid");
            return;
        }

        if(!body.has("modelID")){
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Model identifier was invalid");
            return;
        }

        String provider = body.get("provider").asText();
        String modelID = body.get("modelID").asText();
        boolean wsi = body.has("wsi") && body.get("wsi").asBoolean();
        DimLevel level = DimLevel.valueOf(body.get("level").asText().toUpperCase());
        String dimUID = body.get("uid").asText();

        Task<MLInference> task;

        if(wsi){

            if(!body.has("points") || !body.has("type") || !body.has("baseUID")){
                ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Insufficient data to build request");
                return;
            }

            if(level != DimLevel.INSTANCE){
                ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Only Instance level is supported with WSI");
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
            ResponseUtil.sendError(response, Status.SERVER_ERROR_INTERNAL.getCode(), "Could not build prediction request");
            return;
        }

        task.run();
    }

    private Task<MLInference> sendWSIRequest(String provider, String modelID, String baseSopInstanceUID, String uid,
                                             BulkAnnotation.AnnotationType roiType, List<Point2D> roi, HttpServletResponse response){

        ObjectMapper mapper = new ObjectMapper();
        MLInferenceRequest predictionRequest = new MLInferenceRequest(true, DimLevel.INSTANCE, uid, modelID);
        BulkAnnotation annotation = new BulkAnnotation();
        annotation.setPoints(roi);
        annotation.setAnnotationType(roiType);

        try {
            DicomMetaData dicomMetaData = this.getDicomMetadata(uid);
            BufferedImage bi = roiExtractor.extractROI(dicomMetaData, annotation);
            predictionRequest.setRoi(bi);
            Task<MLInference> task = PluginController.getInstance().infer(provider, predictionRequest);
            if(task != null){
                task.onCompletion(() -> {
                    try {
                        MLInference prediction = task.get();

                        if(prediction == null){
                            log.error("Provider returned null prediction");
                            ResponseUtil.sendError(response, Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
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
                            ResponseUtil.sendError(response, Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
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

    private Task<MLInference> sendRequest(String provider, String modelID, DimLevel level, String dimUID, HttpServletResponse response){
        ObjectMapper mapper = new ObjectMapper();
        MLInferenceRequest predictionRequest = new MLInferenceRequest(false, level, dimUID, modelID);
        Task<MLInference> task = PluginController.getInstance().infer(provider, predictionRequest);
        if(task != null){
            task.onCompletion(() -> {
                try {
                    MLInference prediction = task.get();

                    if(prediction == null){
                        log.error("Provider returned null prediction");
                        ResponseUtil.sendError(response, Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
                        return;
                    }

                    if((prediction.getDicomSEG() != null) && !prediction.hasResults()){
                        response.setContentType("application/dicom");
                        ServletOutputStream out = response.getOutputStream();
                        try (InputStream fi = Files.newInputStream(prediction.getDicomSEG())) {
                            IOUtils.copy(fi, out);
                            out.flush();
                        }
                    } else if((prediction.getDicomSEG() != null) && prediction.hasResults()){
                        // We have a file to send, got to build a multi part response
                        String boundary = UUID.randomUUID().toString();
                        response.setContentType("multipart/form-data; boundary=" + boundary);

                        ServletOutputStream out = response.getOutputStream();

                        out.print("--" + boundary);
                        out.println();
                        out.print("Content-Disposition: form-data; name=\"params\"");
                        out.println();
                        out.print("Content-Type: application/json");
                        out.println(); out.println();
                        out.print(mapper.writeValueAsString(prediction));
                        out.println();
                        out.print("--" + boundary);
                        out.println();
                        out.print("Content-Disposition: form-data; name=\"dicomseg\"; filename=\"dicomseg.dcm\"");
                        out.println();
                        out.print("Content-Type: application/dicom");
                        out.println(); out.println();

                        try (InputStream fi = Files.newInputStream(prediction.getDicomSEG())) {
                            IOUtils.copy(fi, out);
                            out.flush();
                        }

                        out.println();
                        out.print("--" + boundary + "--");
                        out.flush();
                        out.close();

                    } else {
                        response.setContentType("application/json");
                        PrintWriter out = response.getWriter();
                        mapper.writeValue(out, prediction);
                        out.close();
                        out.flush();
                    }

                    try{
                        if(!StringUtils.isBlank(prediction.getResourcesFolder())){
                            FileUtils.deleteDirectory(new File(prediction.getResourcesFolder()));
                        }
                    } catch (IOException e){
                        log.warn("Could not delete temporary file", e);
                    }

                } catch (InterruptedException | ExecutionException e) {
                    log.error("Could not make prediction", e);
                    try {
                        ResponseUtil.sendError(response, Status.SERVER_ERROR_INTERNAL.getCode(), "Could not make prediction");
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
    private void convertCoordinates(MLInference prediction, Point2D tl, double scale){
        for(BulkAnnotation ann : prediction.getAnnotations()){
            for(Point2D p : ann.getPoints()){
                p.setX((p.getX() + tl.getX())/scale);
                p.setY((p.getY() + tl.getY())/scale);
            }
        }
    }
}
