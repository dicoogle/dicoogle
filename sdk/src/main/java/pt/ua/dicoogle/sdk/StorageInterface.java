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
package pt.ua.dicoogle.sdk;

import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.util.Objects;
import java.util.stream.Stream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/** Storage plugin interface. These types of plugins provide an abstraction to reading and writing from
 * files or data blobs.
 * 
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Frederico Valente
 */
public interface StorageInterface extends DicooglePlugin {

    /**
     * Gets the scheme URI of this storage plugin.
     *
     * @see URI
     * @return a string denoting the scheme that this plugin associates to
     */
    public String getScheme();

    /**
     * Checks whether the file in the given path can be handled by this storage plugin.
     *
     * The default implementation checks that the given location URI
     * has the exact same scheme as the scheme returned by {@link #getScheme()}.
     *
     * @param location a URI containing a scheme to be verified
     * @return true if this storage plugin is in charge of URIs in the given form 
     */
    public default boolean handles(URI location) {
        return Objects.equals(this.getScheme(), location.getScheme());
    }

    /**
     * Provides a means of iteration over all existing objects at a specified location,
     * including those in sub-directories.
     * This method is particularly nice for use in for-each loops.
     * 
     * The provided scheme is not relevant at this point, but the developer must avoid calling this method
     * with a path of a different scheme.
     *
     * <pre>
     * URI uri = URI.create("file://dataset/");
     * for (StorageInputStream dicomObj: storagePlugin.at(uri)) {
     *     System.err.println(dicomObj.getURI());
     * }
     * </pre>
     * 
     * @param location the location to read
     * @param parameters a variable list of extra retrieval parameters
     * @return an iterable of storage input streams
     * @see StorageInputStream
     */
    public Iterable<StorageInputStream> at(URI location, Object... parameters);

    /**
     * Obtains an item stored at the exact location specified.
     *
     * The provided scheme is not relevant at this point,
     * but the developer must avoid calling this method
     * with a path of a different scheme.
     *
     * <pre>
     * URI uri = URI.create("file://dataset/CT/001.dcm");
     * StorageInputStream item = storagePlugin.get(uri);
     * if (item != null) {
     *      System.err.println("Item at " + dicomObj.getURI() + " is available");
     * }
     * </pre>
     *
     * The default implementation calls {@linkplain #at}
     * and returns the first item if its URI matches the location requested.
     * Implementations may wish to override this method for performance reasons.
     *
     * @param location the URI of the item to retrieve
     * @param parameters a variable list of extra retrieval parameters
     * @return a storage item if it was found, <code>null</code> otherwise
     * @see StorageInputStream
     */
    default public StorageInputStream get(URI location, Object... parameters) {
        for (StorageInputStream sis : this.at(location, parameters)) {
            if (Objects.equals(sis.getURI(), location)) {
                return sis;
            }
            // don't try any further
            break;
        }
        return null;
    }

    /**
     * Stores a DICOM object into the storage.
     *
     * @param dicomObject DICOM object to be stored
     * @param parameters a variable list of extra parameters for the retrieve
     * @return The URI of the object just stored
     * @throws IOException if an I/O error occurs during the store operation
     */
    public URI store(DicomObject dicomObject, Object... parameters) throws IOException;

    /**
     * Stores a new item into the storage.
     *
     * @param inputStream an input stream with the contents to be stored
     * @param parameters a variable list of extra parameters for the retrieve
     * @return The URI of the DICOM data just stored
     * @throws IOException if an I/O error occurs while fetching or storing DICOM data
     */
    public URI store(DicomInputStream inputStream, Object... parameters) throws IOException;

    /** Removes an item at the given URI.
     * 
     * @param location the URI of the stored data
     */
    public void remove(URI location);

    /** Lists the items at the given location in the storage's file tree.
     * Unlike {@link StorageInterface#at}, this method is not recursive and
     * can yield intermediate URIs representing other directories rather than
     * objects.
     * 
     * Directories can be distinguished from regular files
     * by the presence of a trailing forward slash in the URI.
     * 
     * The provided scheme is not relevant at this point, but the developer
     * must avoid calling this method with a path of a different scheme.
     * 
     * @param location the base storage location to list
     * 
     * @return a standard stream of URIs representing entries in the given base
     * location
     * @throws UnsupportedOperationException if this storage does not support
     * listing directories
     * @throws NoSuchFileException if the given location does not exist in the
     * storage
     * @throws NotDirectoryException if the given location does not refer to a
     * listable entry (a directory)
     * @throws IOException if some other I/O error occurs
     */
    public default Stream<URI> list(URI location) throws IOException {
        throw new UnsupportedOperationException(
                String.format("Storage %s does not support directory listing", this.getName()));
    }
}
