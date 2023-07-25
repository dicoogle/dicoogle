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

import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object maps inferences done by the AI algorithms.
 * It can contain a set of metrics, annotations and a DICOM SEG file.
 */
public class MLInference {

    private Map<String, String> metrics;

    private String version;

    private List<BulkAnnotation> annotations;

    @JsonIgnore
    private String resourcesFolder;

    @JsonIgnore
    private Path dicomSEG;

    public MLInference(){
        this.metrics = new HashMap<>();
        this.annotations = new ArrayList<>();
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }

    public List<BulkAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<BulkAnnotation> annotations) {
        this.annotations = annotations;
    }

    public String getResourcesFolder() {
        return resourcesFolder;
    }

    public void setResourcesFolder(String resourcesFolder) {
        this.resourcesFolder = resourcesFolder;
    }

    public Path getDicomSEG() {
        return dicomSEG;
    }

    public void setDicomSEG(Path dicomSEG) {
        this.dicomSEG = dicomSEG;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasResults(){
        return metrics != null || annotations != null;
    }
}
