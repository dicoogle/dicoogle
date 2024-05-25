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

import pt.ua.dicoogle.sdk.DicooglePlugin;
import pt.ua.dicoogle.sdk.task.Task;

import java.util.List;
import java.util.Set;

/**
 * Interface to define Machine Learning providers.
 * A machine learning provider can be a remote service, hosted on the cloud, or a simple remote/local server
 * that has machine learning algorithms installed for problem solving.
 * Machine learning providers can work with either image or csv datasets.
 * The purpose of this interface is to provide a way
 * to integrate with services such as Google's Vertex API or Amazon's SageMaker API.
 *
 * @author Rui Jesus
 */
public abstract class MLProviderInterface implements DicooglePlugin {

    protected Set<MLDataType> acceptedDataTypes;

    /**
     * This method uploads data to the provider.
     * The API assumes three kinds of data:
     *   - CSV files, identified by a URI
     *   - Image files, identified by a URI
     *   - DICOM files, identified by their respective UIDs.
     * This method can be used to upload labelled or un-labelled datasets to the provider.
     */
    public abstract void dataStore(MLDataset dataset);

    /**
     * This method is similar to dataStore in that is also used to upload data to a provider.
     * The main difference is that this method should be used to cache DICOM objects on the provider,
     * so that the provider can for example run the inference tasks locally.
     * Only DICOM objects can be cached.
     * @param dataset a DICOM dataset to cache.
     * @return a task to the cache operation. Returns true if the dataset was cached.
     */
    public abstract Task<Boolean> cache(MLDicomDataset dataset);
    
    /**
     * This method creates a model using a specific dataset
     */
    public abstract MLModel createModel();

    /**
     * This method lists the models created on this provider.
     */
    public abstract List<MLModel> listModels();

    /**
     * This method lists the models created on this provider.
     * @param modelID model identifier
     */
    public abstract MLModelTrainInfo modelInfo(String modelID);

    /**
     * This method orders the training of a model.
     * @param modelID the unique model identifier within the provider.
     */
    public abstract MLTrainTask trainModel(String modelID);

    /**
     * This method orders the training of a model.
     * @param trainingTaskID the unique training task identifier.
     */
    public abstract boolean stopTraining(String trainingTaskID);

    /**
     * This method deletes a model
     */
    public abstract void deleteModel();

    /**
     * Order a prediction over a single object.
     * The object can be a series instance, a sop instance or a 2D/3D ROI.
     *
     * @param inferRequest object that defines this inference request
     */
    public abstract Task<MLInference> infer(MLInferenceRequest inferRequest);

    /**
     * This method makes a bulk inference request using the selected model
     */
    public abstract void batchInfer();

    /**
     * This method indicates if the service is available.
     * @return true if the provider is ready to be used, false otherwise.
     */
    public abstract boolean isAvailable();

    /**
     * This method can be used to determine which of the methods of this interface are implemented.
     * @return a list of the methods implemented.
     */
    public abstract Set<MLMethod> getImplementedMethods();

    public Set<MLDataType> getAcceptedDataTypes() {
        return acceptedDataTypes;
    }
}
