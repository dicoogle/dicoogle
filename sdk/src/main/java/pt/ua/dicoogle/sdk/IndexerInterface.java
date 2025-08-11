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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.datastructs.UnindexReport;
import pt.ua.dicoogle.sdk.datastructs.UnindexReport.FailedUnindex;
import pt.ua.dicoogle.sdk.task.Task;

/**
 * Indexing plugin interface.
 * 
 * Indexers analyze and record documents for future retrieval.
 * They are primarily designed to index DICOM meta-data,
 * which in that case they are accompanied by a query plugin,
 * and both plugins are called <em>DIM providers</em>.
 * However, indexers are not restricted to processing DICOM files,
 * or to retrieving and indexing meta-data.
 *
 * @author Luís A. Bastião Silva <bastiao@bmd-software.com>
 * @author Frederico Valente <fmvalente@ua.pt>
 */
public interface IndexerInterface extends DicooglePlugin {

    /**
     * Indexes the file path to the database. Indexing procedures are asynchronous, and will return
     * immediately after the call. The outcome is a report that can be retrieved from the given task
     * as a future.
     *
     * @param file directory or file to index
     * @return a representation of the asynchronous indexing task
     */
    public Task<Report> index(StorageInputStream file, Object... parameters);

    /**
     * Indexes multiple file paths to the database. Indexing procedures are asynchronous, and will return
     * immediately after the call. The outcomes are aggregated into a single report and can be retrieved from
     * the given task as a future.
     *
     * @param files a collection of directories and/or files to index
     * @return a representation of the asynchronous indexing task
     */
    public Task<Report> index(Iterable<StorageInputStream> files, Object... parameters);

    /**
     * Checks whether the file in the given path can be indexed by this indexer.
     * 
     * The method should return <code>false</code> <em>if and only if</em>
     * it is sure that the file cannot be indexed,
     * by observation of its URI.
     * This method exists in order to filter out files
     * that are obviously incompatible for the indexer.
     * However, there are situations where this is not reliable,
     * since the storage is free to establish its own file naming rules,
     * and that can affect the file extension.
     * In case of doubt, it is recommended to leave the default implementation,
     * which returns true unconditionally.
     * Attempts to read invalid files can instead
     * be handled gracefully by the indexer by capturing exceptions.
     * 
     * @param path a URI to the file to check
     * @return whether the item at the given URI path can be fed to this indexer
     */
    public default boolean handles(URI path) {
        return true;
    }

    /**
     * Removes the indexed file at the given path from the database.
     * 
     * Unlike the other indexing tasks,
     * this operation is synchronous
     * and will only return when the operation is done.
     * 
     * @param path the URI of the document
     * @return whether it was successfully deleted from the database
     */
    public boolean unindex(URI path);

    /**
     * Removes indexed files from the database in bulk.
     *
     * The default implementation unindexes each item one by one
     * in a non-specified order via {@linkplain #unindex(URI)},
     * but indexers may implement this as
     * one or more individual operations in batch,
     * thus becoming faster than unindexing each item individually.
     * 
     * Like {@linkplain #index(Iterable, Object...)},
     * this operation is asynchronous.
     * One can keep track of the unindexing task's progress
     * by passing a callback function as the second parameter.
     *
     * @param uris the URIs of the items to unindex
     * @param progressCallback an optional function (can be `null`),
     *        called for every batch of items successfully unindexed
     *        to indicate early progress
     *        and inform consumers that
     *        it is safe to remove or exclude the unindexed item
     * @return an asynchronous task object returning
     *         a report containing which files were not unindexed,
     *         and whether some of them were not found in the database
     * @throws IOException if an error occurred
     *         before the unindexing operation could start,
     *         such as when failing to access or open the database
     */
    public default Task<UnindexReport> unindex(Collection<URI> uris, Consumer<Collection<URI>> progressCallback)
            throws IOException {
        Objects.requireNonNull(uris);
        return new Task<>(() -> {
            List<FailedUnindex> failures = new ArrayList<>();
            for (URI uri : uris) {
                try {
                    if (unindex(uri)) {
                        // unindexed successfully
                        if (progressCallback != null) {
                            progressCallback.accept(Collections.singleton(uri));
                        }
                    } else {
                        // failed to unindex, reason unknown
                        failures.add(new FailedUnindex(Collections.singleton(uri), null));
                    }
                } catch (Exception ex) {
                    failures.add(new FailedUnindex(Collections.singleton(uri), ex));
                }
            }
            return UnindexReport.withFailures(failures);
        });
    }
}
