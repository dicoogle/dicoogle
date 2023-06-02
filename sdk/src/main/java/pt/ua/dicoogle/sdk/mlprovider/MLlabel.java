package pt.ua.dicoogle.sdk.mlprovider;

import java.util.Objects;

/**
 * A label object that belongs to a model.
 * Labels must be unique within a model.
 */
public class MLlabel implements Comparable<MLlabel>{

    private String label;
    private String description;

    public MLlabel(String label) {
        this.label = label;
        this.description = "";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MLlabel mLlabel = (MLlabel) o;
        return label.equals(mLlabel.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    @Override
    public int compareTo(MLlabel o) {
        return o.getLabel().compareTo(this.getLabel());
    }
}
