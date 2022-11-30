package pt.ua.dicoogle.sdk.mlprovider;

import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;

import java.util.HashMap;
import java.util.List;

/**
 * This object maps predictions done by the AI algorithms.
 * It can contain a set of metrics and a list of annotations.
 */
public class MLPrediction {

    private HashMap<String, String> metrics;
    private String version;

    private List<BulkAnnotation> annotations;

    public HashMap<String, String> getMetrics() {
        return metrics;
    }

    public void setMetrics(HashMap<String, String> metrics) {
        this.metrics = metrics;
    }

    public List<BulkAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<BulkAnnotation> annotations) {
        this.annotations = annotations;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
