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

import java.io.Serializable;
import java.util.*;

public class MLModel implements Serializable {

    private String name;

    private String id;

    private String type;

    private String description;

    private Date creationDate;

    private MLDataType dataType;

    private Set<MLlabel> labels;

    private List<MLModelParameter> parameters;

    /**
     * A number between 1 and n that specifies the magnification level of the images this model supports.
     * It is only useful in pathology where algorithms might be trained on lower resolution levels of the pyramid.
     */
    private int processMagnification;

    public MLModel(String name, String id) {
        this.name = name;
        this.id = id;
        this.type = "";
        labels = new TreeSet<>();
        parameters = new ArrayList<>();
        dataType = MLDataType.IMAGE;
        creationDate = new Date();
        processMagnification = 0;
    }

    public MLModel(String name, String id, List<MLModelParameter> parameters){
        this(name, id);
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public MLDataType getDataType() {
        return dataType;
    }

    public void setDataType(MLDataType dataType) {
        this.dataType = dataType;
    }

    public Set<MLlabel> getLabels() {
        return labels;
    }

    public void setLabels(Set<MLlabel> labels) {
        this.labels = labels;
    }

    public List<MLModelParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MLModelParameter> parameters) {
        this.parameters = parameters;
    }

    public void removeLabel(MLlabel label){
        this.labels.remove(label);
    }

    public void addLabel(MLlabel label){
        this.labels.add(label);
    }

    public int getProcessMagnification() {
        return processMagnification;
    }

    public void setProcessMagnification(int processMagnification) {
        this.processMagnification = processMagnification;
    }
}
