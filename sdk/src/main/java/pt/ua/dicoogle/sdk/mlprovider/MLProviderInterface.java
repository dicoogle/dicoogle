package pt.ua.dicoogle.sdk.mlprovider;

import pt.ua.dicoogle.sdk.DicooglePlugin;

import java.util.List;
import java.util.Set;

/**
 * Interface to define Machine Learning providers.
 * A machine learning provider can be a remote service, hosted on the cloud, or a simple remote/local server
 * that has installed machine learning algorithms for problem solving.
 * Machine learning providers can work with either image or csv datasets.
 * The purpose of these providers is to provide a way to develop plugins to integrate with services such as Google's Vertex API or Amazon's SageMaker API.
 *
 * @author Rui Jesus
 */
public abstract class MLProviderInterface implements DicooglePlugin {

    protected Set<ML_DATA_TYPE> acceptedDataTypes;

    /**
     * This method creates and uploads a dataset to the machine learning provider.
     * A dataset is defined as a set of labelled images or a labelled CSV file with one column used to label the entries.
     */
    public abstract void createDataset(MLDataset dataset);

    /**
     * This method creates a model using a specific dataset
     */
    public abstract MLModel createModel();

    /**
     * This method creates a model using a specific dataset
     */
    public abstract void createEndpoint();

    /**
     * This method creates a model using a specific dataset
     */
    public abstract List<MLEndpoint> listEndpoints();

    /**
     * This method creates a model using a specific dataset
     */
    public abstract void deleteEndpoint();

    /**
     * This method deploys a model
     */
    public abstract void deployModel();

    /**
     * This method lists the models created on this provider.
     */
    public abstract List<MLModel> listModels();

    /**
     * This method deletes a model
     */
    public abstract void deleteModel();

    /**
     * This method makes a prediction about an item using the selected model
     */
    public abstract MLPrediction makePrediction();

    /**
     * This method makes a bulk prediction using the selected model
     */
    public abstract void makeBulkPrediction();

    public Set<ML_DATA_TYPE> getAcceptedDataTypes() {
        return acceptedDataTypes;
    }

    public void setAcceptedDataTypes(Set<ML_DATA_TYPE> acceptedDataTypes) {
        this.acceptedDataTypes = acceptedDataTypes;
    }
}
