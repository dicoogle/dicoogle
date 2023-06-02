package pt.ua.dicoogle.sdk.mlprovider;

import java.util.*;

public class MLModel {

    private String name;

    private String id;

    private String type;

    private String description;

    private Date creationDate;

    private ML_DATA_TYPE dataType;

    private Set<MLlabel> labels;

    public MLModel(String name, String id) {
        this.name = name;
        this.id = id;
        this.type = "";
        labels = new TreeSet<>();
        dataType = ML_DATA_TYPE.IMAGE;
        creationDate = new Date();
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

    public ML_DATA_TYPE getDataType() {
        return dataType;
    }

    public void setDataType(ML_DATA_TYPE dataType) {
        this.dataType = dataType;
    }

    public Set<MLlabel> getLabels() {
        return labels;
    }

    public void setLabels(Set<MLlabel> labels) {
        this.labels = labels;
    }

    public void removeLabel(MLlabel label){
        this.labels.remove(label);
    }

    public void addLabel(MLlabel label){
        this.labels.add(label);
    }
}
