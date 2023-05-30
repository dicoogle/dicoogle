package pt.ua.dicoogle.sdk.mlprovider;

import java.util.Date;

public class MLModel {

    private String name;

    private String id;

    private String description;

    private Date creationDate;

    private ML_DATA_TYPE dataType;

    public MLModel(String name, String id) {
        this.name = name;
        this.id = id;
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
}
