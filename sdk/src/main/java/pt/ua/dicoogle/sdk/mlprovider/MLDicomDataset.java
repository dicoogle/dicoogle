package pt.ua.dicoogle.sdk.mlprovider;

import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * An ML dataset of DICOM objects.
 */
public class MLDicomDataset extends MLDataset {

    private DimLevel level;
    private List<String> dimUIDs;

    public MLDicomDataset(DimLevel level){
        this.level = level;
        dimUIDs = new ArrayList<>();
    }

    public MLDicomDataset(DimLevel level, List<String> dimUIDs){
        this.level = level;
        this.dimUIDs = dimUIDs;
    }

    public DimLevel getLevel() {
        return level;
    }

    public void setLevel(DimLevel level) {
        this.level = level;
    }

    public List<String> getDimUIDs() {
        return dimUIDs;
    }

    public void setDimUIDs(List<String> dimUIDs) {
        this.dimUIDs = dimUIDs;
    }
}
