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

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.sdk.mlprovider.*;
import pt.ua.dicoogle.server.web.dicom.ROIExtractor;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

        if (!request.validate())
            return null;

        switch (request.getDataType()) {
            case DICOM:
                return new MLDicomDataset(request.getDimLevel(), request.getUids());
            case IMAGE:
                // throw new UnsupportedOperationException("Datastore requests for image objects is not supported");

                HashMap<String, String> extraFields = new HashMap<>();

                String path = this.ensureAndCreatePath();

                extraFields.put("SOPInstanceUID", "SOPInstanceUID");
                extraFields.put("SharedFunctionalGroupsSequence_PixelMeasuresSequence_PixelSpacing",
                        "SharedFunctionalGroupsSequence_PixelMeasuresSequence_PixelSpacing");
                extraFields.put("Rows", "Rows");
                extraFields.put("Columns", "Columns");
                extraFields.put("NumberOfFrames", "NumberOfFrames");
                extraFields.put("TotalPixelMatrixColumns", "TotalPixelMatrixColumns");
                extraFields.put("TotalPixelMatrixRows", "TotalPixelMatrixRows");
                extraFields.put("ImageType", "ImageType");

                MLImageDataset mlDataset = new MLImageDataset();
                HashMap<ImageEntry, MLlabel> dataset = new HashMap<>();
                Set<String> classes = new HashSet<>();

                this.request.getDataset().entrySet().forEach((entry -> {
                    try {

                        // Query this image using the first available query provider
                        Iterable<SearchResult> results = controller.query(controller.getQueryProvidersName(true).get(0),
                                "SOPInstanceUID:" + entry.getKey(), extraFields).get();

                        int c = 0;
                        for (SearchResult image : results) {

                            BasicDicomObject dcm = new BasicDicomObject();
                            dcm.putString(Tag.TransferSyntaxUID, VR.CS, "1.2.840.10008.1.2.4.50");

                            for (BulkAnnotation annotation : entry.getValue()) {
                                for (List<Point2D> points : annotation.getAnnotations()) {
                                    BufferedImage roi = roiExtractor.extractROI(image.get("SOPInstanceUID").toString(),
                                            annotation.getAnnotationType(), points);
                                    String roiFileName = annotation.getLabel().getName() + c++;
                                    classes.add(annotation.getLabel().getName());
                                    File output = new File(path + File.separator + roiFileName + ".jpeg");
                                    ImageIO.write(roi, "jpeg", output);
                                    dataset.put(new ImageEntry(dcm, output.toURI()), annotation.getLabel());
                                }
                            }
                        }

                    } catch (IOException | InterruptedException | ExecutionException e) {
                        logger.error("Error preparing datastore task", e);
                    }
                }));

                mlDataset.setMultiClass(classes.size() > 2);
                mlDataset.setDataset(dataset);

                return mlDataset;
            default:
                return null;
        }
    }

    private String ensureAndCreatePath() {
        Path datasetsFolder = Paths.get("datasets");
        // Check if the folder exists
        if (!Files.exists(datasetsFolder)) {
            try {
                Files.createDirectories(datasetsFolder);
                System.out.println("Datasets folder didn't exist, creating one.");
            } catch (Exception e) {
                System.err.println("Failed to create datasets folder: " + e.getMessage());
            }
        }

        Path datasetFolder = Paths.get("datasets" + File.separator + System.currentTimeMillis());

        if (!Files.exists(datasetFolder)) {
            try {
                Files.createDirectories(datasetFolder);
            } catch (Exception e) {
                System.err.println("Failed to create dataset folder: " + e.getMessage());
            }
        }

        return datasetFolder.toString();
    }

}
