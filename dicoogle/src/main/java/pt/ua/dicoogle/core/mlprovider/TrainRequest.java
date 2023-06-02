package pt.ua.dicoogle.core.mlprovider;

/**
 * Object to map train requests.
 * A train request intends to send a train or re-train request to a model at a provider.
 */
public class TrainRequest {

    private String provider;
    private String modelID;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }
}
