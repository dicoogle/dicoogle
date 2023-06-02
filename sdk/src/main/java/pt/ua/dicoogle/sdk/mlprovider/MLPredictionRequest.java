package pt.ua.dicoogle.sdk.mlprovider;

import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;

import java.awt.image.BufferedImage;

public class MLPredictionRequest {

    private boolean isWsi;

    private DimLevel level;

    private String dimID;

    private BufferedImage roi;

    private String modelID;

    public MLPredictionRequest(boolean isWsi, DimLevel level, String dimID, String modelID) {
        this.isWsi = isWsi;
        this.level = level;
        this.dimID = dimID;
        this.modelID = modelID;
    }

    public boolean isWsi() {
        return isWsi;
    }

    public void setWsi(boolean wsi) {
        isWsi = wsi;
    }

    public DimLevel getLevel() {
        return level;
    }

    public void setLevel(DimLevel level) {
        this.level = level;
    }

    public String getDimID() {
        return dimID;
    }

    public void setDimID(String dimID) {
        this.dimID = dimID;
    }

    public BufferedImage getRoi() {
        return roi;
    }

    public void setRoi(BufferedImage roi) {
        this.roi = roi;
    }

    public boolean hasROI(){
        return this.roi != null;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }
}
