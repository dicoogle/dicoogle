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
