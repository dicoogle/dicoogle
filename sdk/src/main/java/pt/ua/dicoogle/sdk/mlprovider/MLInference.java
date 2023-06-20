package pt.ua.dicoogle.sdk.mlprovider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dcm4che2.io.DicomInputStream;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * This object maps predictions done by the AI algorithms.
 * It can contain a set of metrics, annotations and a DICOM SEG file.
 */
public class MLInference {

    private HashMap<String, String> metrics;

    private String version;

    private List<BulkAnnotation> annotations;

    @JsonIgnore
    private String resourcesFolder;

    @JsonIgnore
    private Path dicomSEG;

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

    public String getResourcesFolder() {
        return resourcesFolder;
    }

    public void setResourcesFolder(String resourcesFolder) {
        this.resourcesFolder = resourcesFolder;
    }

    public Path getDicomSEG() {
        return dicomSEG;
    }

    public void setDicomSEG(Path dicomSEG) {
        this.dicomSEG = dicomSEG;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasResults(){
        return metrics != null || annotations != null;
    }
}
