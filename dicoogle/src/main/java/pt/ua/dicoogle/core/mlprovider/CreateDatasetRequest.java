package pt.ua.dicoogle.core.mlprovider;

import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;

import java.util.HashMap;
import java.util.List;

/**
 * Java object to represent create dataset requests
 * @author Rui Jesus
 */
public class CreateDatasetRequest {

    /**
     * The name of the ML Provider plugin to update the dataset to.
     */
    private String providerName;

    /**
     * The dataset to upload.
     * Each key should be a SOPInstanceUID and optionally the value should be a list of annotations.
     */
    private HashMap<String, List<BulkAnnotation>> dataset;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public HashMap<String, List<BulkAnnotation>> getDataset() {
        return dataset;
    }

    public void setDataset(HashMap<String, List<BulkAnnotation>> dataset) {
        this.dataset = dataset;
    }
}
