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

import org.dcm4che2.data.DicomObject;

import java.net.URI;
import java.util.Objects;

/**
 * This object is used in {@see MLImageDataset} to map the image objects.
 * Each entry is defined by a physical file and a set of DICOM metadata containing information such as
 * the transfer syntax of the image, and other relevant information to process this image.
 */
public class ImageEntry {

    private DicomObject object;

    private URI file;

    public ImageEntry(DicomObject object, URI file) {
        this.object = object;
        this.file = file;
    }

    public DicomObject getObject() {
        return object;
    }

    public void setObject(DicomObject object) {
        this.object = object;
    }

    public URI getFile() {
        return file;
    }

    public void setFile(URI file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ImageEntry that = (ImageEntry) o;
        return object.equals(that.object) && file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
