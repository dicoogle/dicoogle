/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
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
                throw new UnsupportedOperationException("Datastore requests for image objects is not supported");
                /*
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
                */
            default:
                return null;
        }
    }
}
