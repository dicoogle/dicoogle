package pt.ua.dicoogle.sdk.mlprovider;

/**
 * Object to map training tasks.
 * Training tasks have an associated life cycle and unique identifier.
 * Additional information such as current epoch and iteration, as well as training details can be mapped in this object too.
 */
public class MLTrainTask {

    public enum TRAINING_STATUS {
        SUBMITTED,
        RUNNING,
        CANCELLED,
        BUSY,
        REJECTED
    }

    public MLTrainTask(String taskID, TRAINING_STATUS status) {
        this.taskID = taskID;
        this.status = status;
    }

    private String taskID;

    private TRAINING_STATUS status;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public TRAINING_STATUS getStatus() {
        return status;
    }

    public void setStatus(TRAINING_STATUS status) {
        this.status = status;
    }
}
