package pt.ua.dicoogle.sdk.mlprovider;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class MLImageDataset extends MLDataset {

    private HashMap<URI, List<MLlabel>> dataset;

    private boolean multiClass;

    public HashMap<URI, List<MLlabel>> getDataset() {
        return dataset;
    }

    public void setDataset(HashMap<URI, List<MLlabel>> dataset) {
        this.dataset = dataset;
    }

    public boolean isMultiClass() {
        return multiClass;
    }

    public void setMultiClass(boolean multiClass) {
        this.multiClass = multiClass;
    }
}
