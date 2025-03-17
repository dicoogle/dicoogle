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

import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.mlprovider.MLDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private MLDataType dataType;

    private DimLevel dimLevel;

    private ArrayList<String> uids;

    /**
     * The dataset to upload.
     * Each key should be a SOPInstanceUID and optionally the value should be a list of annotations.
     */
    private Map<String, List<BulkAnnotation>> dataset;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public MLDataType getDataType() {
        return dataType;
    }

    public void setDataType(MLDataType dataType) {
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

    public Map<String, List<BulkAnnotation>> getDataset() {
        return dataset;
    }

    public void setDataset(Map<String, List<BulkAnnotation>> dataset) {
        this.dataset = dataset;
    }

    /**
     * Check if this request has enough information to be processed.
     * For example, if it is of DICOM type, it must specify the dim level and the dim uid.
     * @return true if the request can processed, false otherwise.
     */
    public boolean validate() {
        if (provider == null || provider.isEmpty())
            return false;

        switch (dataType) {
            case DICOM:
                return this.dimLevel != null && this.uids != null && !this.uids.isEmpty();
            case IMAGE:
                return this.dataset != null && !this.dataset.isEmpty();
            default:
                return false;
        }
    }
}
