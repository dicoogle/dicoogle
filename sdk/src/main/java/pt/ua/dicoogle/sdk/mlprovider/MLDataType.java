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
 * This enum maps the supported data types used in the MLProviderInterface.
 * Data in this context always refers to data objects, labelled or unlabelled, used throughout the ML pipeline,
 * for example in training or inference jobs.
 * The data types listed here are not exhaustive, meaning future releases and iterations might add or remove new data types.
 */
public enum MLDataType {
    /**
     * CSV data objects refers to data that can be mapped in a tabular format.
     */
    CSV,
    /**
     * IMAGE data objects refer explicitly to pixel data objects.
     */
    IMAGE,
    /**
     * DICOM data objects refer to one: Study, Series or Instance.
     */
    DICOM
}
