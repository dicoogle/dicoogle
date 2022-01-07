package pt.ua.dicoogle.sdk.mlprovider;

public class MLlabel {

    private String label;
    private double confidence;

    public MLlabel(String label) {
        this.label = label;
        this.confidence = 1;
    }

    public MLlabel(String label, double confidence) {
        this.label = label;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
