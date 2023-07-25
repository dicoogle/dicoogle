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

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * An ML dataset of image objects.
 * Optionally an array of labels can be given to create a labelled dataset.
 */
public class MLImageDataset extends MLDataset {

    private HashMap<URI, List<MLlabel>> dataset;

    private boolean multiClass;

    public HashMap<URI, List<MLlabel>> getDataset() {
        return dataset;
    }

    public void setDataset(HashMap<URI, List<MLlabel>> dataset) {
        this.dataset = dataset;
    }

    public boolean isMultiClass() {
        return multiClass;
    }

    public void setMultiClass(boolean multiClass) {
        this.multiClass = multiClass;
    }
}
