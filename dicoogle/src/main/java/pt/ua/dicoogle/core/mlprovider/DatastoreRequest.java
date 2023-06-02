package pt.ua.dicoogle.core.mlprovider;

import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.mlprovider.ML_DATA_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Java object to represent datastore requests.
 * Datastore requests can be for image ROIs or DICOM objects.
 * In case of DICOM objects, the DIM level and DIM UID must be provided.
 * @author Rui Jesus
 */
public class DatastoreRequest {

    /**
     * The name of the ML Provider plugin to update the dataset to.
     */
    private String provider;

    /**
     * The type of dataset to upload
     */
    private ML_DATA_TYPE dataType;

    private DimLevel dimLevel;

    private ArrayList<String> uids;

    /**
     * The dataset to upload.
     * Each key should be a SOPInstanceUID and optionally the value should be a list of annotations.
     */
    private HashMap<String, List<BulkAnnotation>> dataset;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ML_DATA_TYPE getDataType() {
        return dataType;
    }

    public void setDataType(ML_DATA_TYPE dataType) {
        this.dataType = dataType;
    }

    public DimLevel getDimLevel() {
        return dimLevel;
    }

    public void setDimLevel(DimLevel dimLevel) {
        this.dimLevel = dimLevel;
    }

    public ArrayList<String> getUids() {
        return uids;
    }

    public void setUids(ArrayList<String> uids) {
        this.uids = uids;
    }

    public HashMap<String, List<BulkAnnotation>> getDataset() {
        return dataset;
    }

    public void setDataset(HashMap<String, List<BulkAnnotation>> dataset) {
        this.dataset = dataset;
    }

    public boolean validate(){
        if(provider == null || provider.isEmpty())
            return false;

        switch (dataType){
            case DICOM:
                if(this.dimLevel == null || this.uids == null || this.uids.isEmpty())
                    return false;
            case IMAGE:
                return this.dataset == null;
            default:
                return false;
        }
    }
}
