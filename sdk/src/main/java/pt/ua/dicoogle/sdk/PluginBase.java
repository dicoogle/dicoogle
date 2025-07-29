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

import pt.ua.dicoogle.sdk.mlprovider.MLProviderInterface;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.resource.ServerResource;

import pt.ua.dicoogle.sdk.core.DicooglePlatformInterface;
import pt.ua.dicoogle.sdk.core.PlatformCommunicatorInterface;

/**
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Luís S. Ribeiro
 */
public abstract class PluginBase implements PluginSet, PlatformCommunicatorInterface {

    protected List<IndexerInterface> indexPlugins = new ArrayList<>();
    protected List<QueryInterface> queryPlugins = new ArrayList<>();
    protected List<JettyPluginInterface> jettyPlugins = new ArrayList<>();
    protected List<StorageInterface> storagePlugins = new ArrayList<>();
    protected List<MLProviderInterface> mlPlugins = new ArrayList<>();
    protected List<ServerResource> services = new ArrayList<>();
    protected ConfigurationHolder settings = null;

    protected DicooglePlatformInterface platform;

    @Override
    public List<IndexerInterface> getIndexPlugins() {
        return indexPlugins;
    }

    @Override
    public List<QueryInterface> getQueryPlugins() {
        return queryPlugins;
    }

    @Override
    public List<ServerResource> getRestPlugins() {
        return services;
    }

    @Override
    public List<JettyPluginInterface> getJettyPlugins() {
        return jettyPlugins;
    }

    @Override
    public abstract String getName();

    @Override
    public ConfigurationHolder getSettings() {
        return settings;
    }

    @Override
    public void setSettings(ConfigurationHolder xmlSettings) {
        settings = xmlSettings;
    }

    @Override
    public Collection<StorageInterface> getStoragePlugins() {
        return storagePlugins;
    }

    @Override
    public Collection<MLProviderInterface> getMLPlugins() {
        return mlPlugins;
    }

    @Override
    public void setPlatformProxy(DicooglePlatformInterface core) {
        this.platform = core;
    }
}
