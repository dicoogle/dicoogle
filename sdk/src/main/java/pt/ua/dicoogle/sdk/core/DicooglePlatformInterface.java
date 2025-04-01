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
package pt.ua.dicoogle.sdk.core;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import pt.ua.dicoogle.sdk.IndexerInterface;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.datastructs.UnindexReport;
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.settings.server.ServerSettingsReader;
import pt.ua.dicoogle.sdk.task.JointQueryTask;
import pt.ua.dicoogle.sdk.task.Task;

/**
 * Interface for accessing external functionalities of the platform, including other plugins' interfaces.
 * <p>
 * For instance, if a graphical plugin needs to query, it will able to access query providers
 * with this platform. </p>
 * 
 * @author: Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Carlos Ferreira
 * @author Frederico Valente
 * @see QueryInterface
 * @see IndexerInterface
 * @see StorageInterface
 */
public interface DicooglePlatformInterface {

    /**
     * Gets an installed indexer plugin by name.
     * @param name the name of the plugin 
     * @return the plugin that implements this interface
     */
    IndexerInterface requestIndexPlugin(String name);

    /**
     * Gets an installed query plugin by name.
     * @param name the name of the plugin 
     * @return the plugin that implements this interface
     */
    QueryInterface requestQueryPlugin(String name);

    /**
     * Obtains a collection of all active index plugins.
     * @return an unmodifiable collection of active instances of {@link IndexerInterface}
     */
    Collection<IndexerInterface> getAllIndexPlugins();

    /**
     * Obtains a collection of all active query plugins.
     * @return an unmodifiable collection of active instances of {@link QueryInterface}
     */
    Collection<QueryInterface> getAllQueryPlugins();

    /** Obtains the storage interface for handling storage of the given scheme.
     * 
     * @param scheme the storage scheme
     * @return the storage interface capable of handling that content, or {@code null} if no storage plugin
     * installed can.
     */
    StorageInterface getStoragePluginForSchema(String scheme);

    /** Obtains the storage interface for handling content in the given location.
     * 
     * @param location the storage location to check
     * @return the storage interface capable of handling that content, or {@code null} if no storage plugin
     * installed can
     */
    StorageInterface getStorageForSchema(URI location);

    /** Quickly obtains all storage elements at the given location.
     * 
     * @param location the location to retrieve
     * @param args a variable list of extra parameters for the retrieve
     * @return an iterable of storage input streams
     */
    Iterable<StorageInputStream> resolveURI(URI location, Object... args);

    /** Obtains all installed storage plugins.
     * 
     * @param onlyEnabled whether only enabled plugins should be retrieved (all are retrieved if {@code false})
     * @return a collection of storage plugin interfaces
     */
    Collection<StorageInterface> getStoragePlugins(boolean onlyEnabled);

    /** Obtains the storage interface for handling storage of the given scheme.
     * Same as {@link #getStoragePluginForSchema(java.lang.String)}.
     * 
     * @param scheme the storage scheme
     * @return the storage interface capable of handling that content, or {@code null} if no storage plugin
     * installed can
     */
    StorageInterface getStorageForSchema(String scheme);

    /** Obtains all installed query plugins.
     * 
     * @param onlyEnabled whether only enabled plugins should be retrieved (all are retrieved if {@code false})
     * @return a collection of query plugin interfaces
     */
    Collection<QueryInterface> getQueryPlugins(boolean onlyEnabled);

    /** Gets the names of all query providers.
     * 
     * @param enabled whether only enabled plugins should be retrieved (all are retrieved if {@code false})
     * @return a collection of unique query provider names
     */
    List<String> getQueryProvidersName(boolean enabled);

    /** Gets the query provider with the given name.
     * 
     * @param name the unique name of the provider
     * @param onlyEnabled whether only enabled plugins should be retrieved (all are retrieved if {@code false})
     * @return a collection of unique query provider names
     */
    QueryInterface getQueryProviderByName(String name, boolean onlyEnabled);

    /**
     * Easily performs a query over all query providers. This operation is asynchronous and returns immediately.
     * @param holder a query task holder containing callback methods
     * @param query a string describing the query
     * @param parameters a variable list of extra parameters for the query
     * @return a join query task
     */
    JointQueryTask queryAll(JointQueryTask holder, String query, Object... parameters);

    /**
     * Easily performs a query over all query providers. This operation is asynchronous and returns immediately.
     * @param holder a query task holder containing callback methods
     * @param query a string describing the query
     * @param level level of the query
     * @param parameters a variable list of extra parameters for the query
     * @return a join query task
     */
    JointQueryTask queryAll(JointQueryTask holder, String query, DimLevel level, Object... parameters);

    /** Easily performs a query over a specific provider. This operation is asynchronous and returns immediately.
     * 
     * @param querySource the name of the query provider to issue
     * @param query a string describing the query
     * @param parameters a variable list of extra parameters for the query
     * @return an asynchronous task containing the results
     */
    Task<Iterable<SearchResult>> query(String querySource, String query, Object... parameters);

    /** Easily performs a query over a specific provider. This operation is asynchronous and returns immediately.
     *
     * @param querySource the name of the query provider to issue
     * @param query a string describing the query
     * @param level level of the query
     * @param parameters a variable list of extra parameters for the query
     * @return an asynchronous task containing the results
     */
    Task<Iterable<SearchResult>> query(String querySource, DimLevel level, String query, Object... parameters);

    /** Easily performs a query over multiple providers. This operation is asynchronous and returns immediately.
     * 
     * @param holder a query task holder containing callback methods
     * @param querySources a list of names of query providers to issue
     * @param query a string describing the query
     * @param parameters a variable list of extra parameters for the query
     * @return an asynchronous task containing the results
     */
    JointQueryTask query(JointQueryTask holder, List<String> querySources, String query, Object... parameters);

    /** Easily performs a query over multiple providers. This operation is asynchronous and returns immediately.
     *
     * @param holder a query task holder containing callback methods
     * @param querySources a list of names of query providers to issue
     * @param level level of the query
     * @param query a string describing the query
     * @param parameters a variable list of extra parameters for the query
     * @return an asynchronous task containing the results
     */
    JointQueryTask query(JointQueryTask holder, List<String> querySources, DimLevel level, String query,
            Object... parameters);


    /** Easily performs an indexation procedure over all active indexers. This operation is asynchronous
     * and returns immediately.
     *
     * @param path the path to index
     * @return a list of asynchronous tasks, one for each provider
     */
    List<Task<Report>> index(URI path);


    /** Easily performs indexing procedures over all active indexers. This operation is asynchronous
     * and returns immediately.
     *
     * @param paths the paths to index
     * @return a list of asynchronous tasks, one for each provider
     */
    List<Task<Report>> index(Collection<URI> paths);

    /** Easily performs an unindex procedure over all active indexers. This operation is synchronous.
     *
     * @param path the path to index
     */
    void unindex(URI path);


    /** Easily performs an unindex procedure for several paths over all active indexers. This operation is asynchronous
     * and returns immediately.
     *
     * @param paths the path to index
     * @return a list of asynchronous tasks from each provider
     */
    List<Task<UnindexReport>> unindex(Collection<URI> paths);

    /** Easily performs an unindex procedure for several paths over all active indexers. This operation is asynchronous
     * and returns immediately.
     *
     * @param paths the path to index
     * @return a list of asynchronous tasks from each provider
     */
    List<Task<UnindexReport>> unindex(Collection<URI> paths, Consumer<Collection<URI>> progressCallback);


    /** Easily performs an indexation procedure over all active indexers. This operation is synchronous
     * and will wait until all providers have finished indexing.
     * 
     * @param path the path to index
     * @return a list of reports, one for each provider
     * @deprecated Call {@linkplain #index} and get the result instead
     */
    @Deprecated
    List<Report> indexBlocking(URI path);

    /** Obtain access to the server's settings.
     * @return an object for read-only access to the settings
     */
    ServerSettingsReader getSettings();
}
