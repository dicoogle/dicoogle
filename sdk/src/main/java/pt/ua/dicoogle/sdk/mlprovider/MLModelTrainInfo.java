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
