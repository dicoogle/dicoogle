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

import java.util.Date;
import java.util.Map;

/**
 * This object stores model information
 */
public class MLModelTrainInfo {

    private Date modifiedTime;

    private Date startTime;

    private int currentEpoch;

    private int totalEpochs;

    private double bestMetric;

    private String bestMetricKey;

    private Map<String, Double> metrics;

    public MLModelTrainInfo(Date modifiedTime, Date startTime) {
        this.modifiedTime = modifiedTime;
        this.startTime = startTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getCurrentEpoch() {
        return currentEpoch;
    }

    public void setCurrentEpoch(int currentEpoch) {
        this.currentEpoch = currentEpoch;
    }

    public int getTotalEpochs() {
        return totalEpochs;
    }

    public void setTotalEpochs(int totalEpochs) {
        this.totalEpochs = totalEpochs;
    }

    public double getBestMetric() {
        return bestMetric;
    }

    public void setBestMetric(double bestMetric) {
        this.bestMetric = bestMetric;
    }

    public String getBestMetricKey() {
        return bestMetricKey;
    }

    public void setBestMetricKey(String bestMetricKey) {
        this.bestMetricKey = bestMetricKey;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Double> metrics) {
        this.metrics = metrics;
    }
}
