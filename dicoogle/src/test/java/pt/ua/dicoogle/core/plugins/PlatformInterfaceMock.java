/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.core.plugins;

import pt.ua.dicoogle.sdk.IndexerInterface;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.core.DicooglePlatformInterface;
import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.datastructs.UnindexReport;
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.settings.server.ServerSettingsReader;
import pt.ua.dicoogle.sdk.task.JointQueryTask;
import pt.ua.dicoogle.sdk.task.Task;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PlatformInterfaceMock implements DicooglePlatformInterface {


    @Override
    public IndexerInterface requestIndexPlugin(String name) {
        return null;
    }

    @Override
    public QueryInterface requestQueryPlugin(String name) {
        return null;
    }

    @Override
    public Collection<IndexerInterface> getAllIndexPlugins() {
        return null;
    }

    @Override
    public Collection<QueryInterface> getAllQueryPlugins() {
        return null;
    }

    @Override
    public StorageInterface getStoragePluginForSchema(String scheme) {
        return null;
    }

    @Override
    public Collection<StorageInterface> getStoragePlugins(boolean onlyEnabled) {
        return null;
    }

    @Override
    public Collection<QueryInterface> getQueryPlugins(boolean onlyEnabled) {
        return null;
    }

    @Override
    public List<String> getQueryProvidersName(boolean enabled) {
        return null;
    }

    @Override
    public QueryInterface getQueryProviderByName(String name, boolean onlyEnabled) {
        return null;
    }

    @Override
    public JointQueryTask queryAll(JointQueryTask holder, String query, Object... parameters) {
        return null;
    }

    @Override
    public JointQueryTask queryAll(JointQueryTask holder, String query, DimLevel level, Object... parameters) {
        return null;
    }


    @Override
    public JointQueryTask query(JointQueryTask holder, List<String> querySources, DimLevel level, String query,
            Object... parameters) {
        return null;
    }

    @Override
    public Task<Iterable<SearchResult>> query(String querySource, String query, Object... parameters) {
        return null;
    }


    @Override
    public Task<Iterable<SearchResult>> query(String querySource, DimLevel level, String query, Object... parameters) {
        return null;
    }

    @Override
    public JointQueryTask query(JointQueryTask holder, List<String> querySources, String query, Object... parameters) {
        return null;
    }

    @Override
    public List<Task<Report>> index(URI path) {
        return null;
    }

    @Override
    public List<Task<Report>> index(Collection<URI> paths) {
        return null;
    }

    @Override
    public void unindex(URI path) {}

    @Override
    public List<Task<UnindexReport>> unindex(Collection<URI> paths) {
        return null;
    }

    @Override
    public List<Task<UnindexReport>> unindex(Collection<URI> paths, Consumer<Collection<URI>> progressCallback) {
        return null;
    }

    @Override
    public List<Report> indexBlocking(URI path) {
        return null;
    }

    @Override
    public ServerSettingsReader getSettings() {
        return null;
    }

    @Override
    public StorageInterface getStorageForSchema(URI location) {
        return null;
    }

    @Override
    public Iterable<StorageInputStream> resolveURI(URI location, Object... args) {
        return null;
    }

    @Override
    public StorageInterface getStorageForSchema(String scheme) {
        return null;
    }
}
