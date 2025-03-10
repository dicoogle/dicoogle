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
 * Object to map training tasks.
 * Training tasks have an associated life cycle and unique identifier.
 * Additional information such as current epoch and iteration, as well as training details can be mapped in this object too.
 */
public class MLTrainTask {

    /**
     * Status of a training task request/job.
     * It is used to identify the status of training tasks in execution but also training requests.
     */
    public enum TrainingStatus {
        /**
         * A training task was successfully submitted.
         * A corresponding identifier should be generated to track the progress.
         */
        SUBMITTED,
        /**
         * A training job identified by a ID is currently running
         */
        RUNNING,
        /**
         * A training job identified by an ID was cancelled.
         */
        CANCELLED,
        /**
         * A training request was not processed because the service cannot process more training requests.
         */
        BUSY,
        /**
         * A training request was not processed because the request was malformed or some other error occured.
         */
        REJECTED
    }

    public MLTrainTask(String taskID, TrainingStatus status) {
        this.taskID = taskID;
        this.status = status;
    }

    private String taskID;

    private TrainingStatus status;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public TrainingStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingStatus status) {
        this.status = status;
    }
}
