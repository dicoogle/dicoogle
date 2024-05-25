package pt.ua.dicoogle.sdk.mlprovider;

/**
 * This enum lists the available methods of the MLProvider interface.
 * This is used when requesting the available methods of a provider.
 * It is a ENUM instead for example of a String to restrict the possible values.
 */
public enum MLMethod {
    INFER,
    BULK_INFER,
    DATASTORE,
    CACHE,
    LIST_MODELS,
    CREATE_MODEL,
    MODEL_INFO,
    TRAIN_MODEL,
    STOP_TRAINING,
    DELETE_MODEL,
}
