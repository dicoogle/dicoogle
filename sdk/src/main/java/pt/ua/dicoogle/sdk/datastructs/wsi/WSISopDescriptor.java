package pt.ua.dicoogle.sdk.datastructs.wsi;

import org.dcm4che2.data.Tag;
import org.dcm4che3.data.Attributes;
/**
 * Utility class to describe a specific resolution level of a WSI pyramid
 */
public class WSISopDescriptor {

    private int tileWidth;
    private int tileHeight;
    private int totalPixelMatrixColumns;
    private int totalPixelMatrixRows;

    public WSISopDescriptor(int tileWidth, int tileHeight, int totalPixelMatrixColumns, int totalPixelMatrixRows) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.totalPixelMatrixColumns = totalPixelMatrixColumns;
        this.totalPixelMatrixRows = totalPixelMatrixRows;
    }

    public WSISopDescriptor() {
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getTotalPixelMatrixColumns() {
        return totalPixelMatrixColumns;
    }

    public void setTotalPixelMatrixColumns(int totalPixelMatrixColumns) {
        this.totalPixelMatrixColumns = totalPixelMatrixColumns;
    }

    public int getTotalPixelMatrixRows() {
        return totalPixelMatrixRows;
    }

    public void setTotalPixelMatrixRows(int totalPixelMatrixRows) {
        this.totalPixelMatrixRows = totalPixelMatrixRows;
    }

    /**
     * Given a search result, extract if possible the information that describes the resolution level contained within.
     * @param attrs
     */
    public void extractData(Attributes attrs){
        String strRows = attrs.getString(Tag.Rows);
        String strColumns = attrs.getString(Tag.Columns);
        String strTotalPixelMatrixColumns = attrs.getString(Tag.TotalPixelMatrixColumns);
        String strTotalPixelMatrixRows = attrs.getString(Tag.TotalPixelMatrixRows);

        this.totalPixelMatrixColumns = Integer.parseInt(strTotalPixelMatrixColumns);
        this.totalPixelMatrixRows = Integer.parseInt(strTotalPixelMatrixRows);
        this.tileHeight = Integer.parseInt(strRows.split("\\.")[0]);
        this.tileWidth = Integer.parseInt(strColumns.split("\\.")[0]);
    }

}

