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
