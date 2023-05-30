package pt.ua.dicoogle.sdk.mlprovider;

import java.util.ArrayList;
import java.util.List;

/**
 * Data object to model MLPlugin instances.
 */
public class MLProvider {

    public MLProvider(String name) {
        this.name = name;
        this.models = new ArrayList<>();
        this.datasets = new ArrayList<>();
    }

    private List<MLModel> models;
    private List<MLDataset> datasets;
    private String name;

    public List<MLModel> getModels() {
        return models;
    }

    public void setModels(List<MLModel> models) {
        this.models = models;
    }

    public List<MLDataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<MLDataset> datasets) {
        this.datasets = datasets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
