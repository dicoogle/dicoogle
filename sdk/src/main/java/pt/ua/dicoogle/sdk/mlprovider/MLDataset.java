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

/**
 * ML dataset objects map a collection of labelled data, to be used in the training and generation of models.
 * ML datasets have a type, defined by {@see MLDataType} and an identifier, to be used by the providers to internally manage this dataset.
 * This data object is used in datastore requests to construct annotated datasets.
 */
public abstract class MLDataset {

    protected String name;
    protected MLDataType dataType;

    public MLDataset(String name, MLDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MLDataType getDataType() {
        return dataType;
    }

    public void setDataType(MLDataType dataType) {
        this.dataType = dataType;
    }
}
