package pt.ua.dicoogle.core.mlprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.mlprovider.MLDataset;
import pt.ua.dicoogle.sdk.mlprovider.MLDicomDataset;
import pt.ua.dicoogle.sdk.mlprovider.MLImageDataset;
import pt.ua.dicoogle.server.web.dicom.ROIExtractor;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * This task processes Datastore Requests, and builds the appropriate ML Dataset.
 */
public class PrepareDatastoreTask implements Callable<MLDataset> {

    private static final Logger logger = LoggerFactory.getLogger(PrepareDatastoreTask.class);
    private final DatastoreRequest request;
    private final PluginController controller;
    private String dataset;

    private final ROIExtractor roiExtractor;

    private final WSICache wsiCache;

    public PrepareDatastoreTask(PluginController controller, DatastoreRequest request) {
        this.controller = controller;
        this.request = request;
        this.dataset = UUID.randomUUID().toString();

        this.wsiCache = WSICache.getInstance();
        this.roiExtractor = new ROIExtractor();
    }

    @Override
    public MLDataset call() throws Exception {

        if(!request.validate())
            return null;

        switch (request.getDataType()){
            case DICOM:
                return new MLDicomDataset(request.getDimLevel(), request.getUids());
            case IMAGE: // Not operational
                HashMap<String, String> extraFields = new HashMap<String, String>();

                extraFields.put("SOPInstanceUID", "SOPInstanceUID");
                extraFields.put("SharedFunctionalGroupsSequence_PixelMeasuresSequence_PixelSpacing", "SharedFunctionalGroupsSequence_PixelMeasuresSequence_PixelSpacing");
                extraFields.put("Rows", "Rows");
                extraFields.put("Columns", "Columns");
                extraFields.put("NumberOfFrames", "NumberOfFrames");
                extraFields.put("TotalPixelMatrixColumns", "TotalPixelMatrixColumns");
                extraFields.put("TotalPixelMatrixRows", "TotalPixelMatrixRows");
                extraFields.put("ImageType", "ImageType");

                MLImageDataset mlDataset = new MLImageDataset();

                this.request.getDataset().entrySet().forEach((entry -> {
                    try {

                        // Query this image using the first available query provider
                        Iterable<SearchResult> results = controller
                                .query(controller.getQueryProvidersName(true).get(0), "SOPInstanceUID:" + entry.getKey(), extraFields).get();

                        for (SearchResult image : results) {
                            //List<ImageROI> rois = (List<ImageROI>) roiExtractor.extractROI();
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("Error preparing datastore task", e);
                    }
                }));

                return mlDataset;
            default:
                return null;
        }
    }
}
