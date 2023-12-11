/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle-sdk.
 *
 * Dicoogle/dicoogle-sdk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle-sdk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.sdk.mlprovider;

import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * This object is used in the infer servlet to serialize the incoming JSON objects.
 */
public class MLInferenceRequest {

    private boolean isWsi;

    private DimLevel level;

    private String dimID;

    private BufferedImage roi;

    private String modelID;

    private Map<String, Object> parameters;

    public MLInferenceRequest(boolean isWsi, DimLevel level, String dimID, String modelID) {
        this.isWsi = isWsi;
        this.level = level;
        this.dimID = dimID;
        this.modelID = modelID;
        parameters = new HashMap<>();
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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

}
