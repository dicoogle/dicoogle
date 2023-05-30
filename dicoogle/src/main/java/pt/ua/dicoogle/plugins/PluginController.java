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
package pt.ua.dicoogle.plugins;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.core.mlprovider.PrepareDatasetTask;
import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.plugins.webui.WebUIPlugin;
import pt.ua.dicoogle.plugins.webui.WebUIPluginManager;
import pt.ua.dicoogle.sdk.*;
import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.datastructs.UnindexReport;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.datastructs.dim.DimLevel;
import pt.ua.dicoogle.sdk.datastructs.dim.ImageROI;
import pt.ua.dicoogle.sdk.mlprovider.MLDataset;
import pt.ua.dicoogle.sdk.mlprovider.MLPrediction;
import pt.ua.dicoogle.sdk.mlprovider.MLProviderInterface;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;
import pt.ua.dicoogle.sdk.task.JointQueryTask;
import pt.ua.dicoogle.sdk.task.Task;
import pt.ua.dicoogle.server.ControlServices;
import pt.ua.dicoogle.server.PluginRestletApplication;
import pt.ua.dicoogle.server.web.DicoogleWeb;
import pt.ua.dicoogle.core.mlprovider.CreateDatasetRequest;
import pt.ua.dicoogle.taskManager.RunningIndexTasks;
import pt.ua.dicoogle.taskManager.TaskManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

/**
 *
 * PluginController is the core of the Plugins architecture.
 *
 * <p>
 * It loads the plugins, takes care of the list of active plugins and control
 * the tasks that are exchanged between plugins and core plugins
 *
 * @author Carlos Ferreira
 * @author Frederico Valente
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Tiago Marques Godinho
 * @author Eduardo Pinho
 */
public class PluginController {

    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);
    private static PluginController instance;

    public synchronized static PluginController getInstance() {
        if (instance == null) {
            instance = new PluginController(new File("Plugins"));
        }
        return instance;
    }

    private final Collection<PluginSet> pluginSets;
    private final Collection<DeadPlugin> deadPluginSets;
    private File pluginFolder;
    private final PluginPreparer preparer;

    private PluginSet remoteQueryPlugins = null;
    private final WebUIPluginManager webUI;
    private final DicooglePlatformProxy proxy;
    // Task manager for index processes
    private TaskManager taskManager =
            new TaskManager(Integer.parseInt(System.getProperty("dicoogle.taskManager.nThreads", "4")));
    // Task Managers for Queries
    private TaskManager taskManagerQueries =
            new TaskManager(Integer.parseInt(System.getProperty("dicoogle.taskManager.nQueryThreads", "4")));

    private final TaskManager taskManagerML =
            new TaskManager(Integer.parseInt(System.getProperty("dicoogle.taskManager.nMLThreads", "1")));

    /** Whether to shut down Dicoogle when a plugin is marked as dead */
    private static boolean DEAD_PLUGIN_KILL_SWITCH =
            System.getProperty("dicoogle.deadPluginKillSwitch", "false").equalsIgnoreCase("true");

    public PluginController(File pathToPluginDirectory) {
        logger.info("Creating PluginController Instance");
        pluginFolder = pathToPluginDirectory;

        // the plugin directory does not exist. lets create it
        if (!pathToPluginDirectory.exists()) {
            logger.info("Creating new Plugin Folder");
            pathToPluginDirectory.mkdirs();
        }

        this.deadPluginSets = new ArrayList<>(4);

        // loads the plugins
        pluginSets = PluginFactory.getPlugins(pathToPluginDirectory);
        // load web UI plugins (they are not Java, so the process is delegated to another entity)
        this.webUI = new WebUIPluginManager();
        this.loadWebUIPlugins();

        logger.info("Loaded Local Plugins");

        this.configurePlugins();

        pluginSets.add(new DefaultFileStoragePlugin());
        logger.info("Added default storage plugin");

        this.proxy = new DicooglePlatformProxy(this);
        this.preparer = new PluginPreparer(this.proxy);

        initializePlugins(pluginSets);
        initRestInterface(pluginSets);
        initJettyInterface(pluginSets);
        logger.info("Initialized plugins");
    }

    private void loadWebUIPlugins() {
        // loadByPluginName all at "WebPlugins"
        this.webUI.loadAll(new File("WebPlugins"));

        // go through each jar'd plugin and fetch their WebPlugins
        for (File j : FileUtils.listFiles(pluginFolder, new String[] {"jar", "zip"}, false)) {
            try {
                this.webUI.loadAllFromZip(new ZipFile(j));
            } catch (IOException ex) {
                // ignore
                logger.warn("Failed to load web UI plugins from {}: {}", j.getName(), ex.getMessage());
            }
        }
    }

    private void configurePlugins() {
        // loads plugins' settings and passes them to the plugin
        File settingsFolder = new File(pluginFolder.getPath() + "/settings/");
        if (!settingsFolder.exists()) {
            logger.info("Creating Local Settings Folder");
            settingsFolder.mkdir();
        }

        for (Iterator<PluginSet> it = pluginSets.iterator(); it.hasNext();) {
            PluginSet plugin = it.next();
            try {
                final String name = plugin.getName();
                logger.info("Loading plugin: {}", name);
                File pluginSettingsFile = new File(settingsFolder + "/" + name.replace('/', '-') + ".xml");
                ConfigurationHolder holder = new ConfigurationHolder(pluginSettingsFile);
                if (plugin.getName().equals("RemotePluginSet")) {
                    this.remoteQueryPlugins = plugin;
                    // holder.getConfiguration().setProperty("NodeName", ServerSettingsManager.getSettings().getNodeName());
                    // holder.getConfiguration().setProperty("TemporaryPath", ServerSettingsManager.getSettings().getPath());

                    logger.info("Started Remote Communications Manager");
                }
                applySettings(plugin, holder);
            } catch (ConfigurationException e) {
                logger.error("Failed to create configuration holder", e);
            } catch (RuntimeException e) {
                String name;
                try {
                    name = plugin.getName();
                } catch (Exception ex2) {
                    logger.warn("Plugin set name cannot be retrieved: {}", ex2.getMessage());
                    name = "UNKNOWN";
                }
                if (DEAD_PLUGIN_KILL_SWITCH) {
                    logger.error("Unexpected error while loading plugin set {}. Dicoogle will shut down.", name, e);
                    System.exit(-4);
                } else {
                    logger.error("Unexpected error while loading plugin set {}. Plugin set marked as dead.", name, e);
                    this.deadPluginSets.add(new DeadPlugin(name, e));
                    it.remove();
                }
            }
        }
        logger.debug("Settings pushed to plugins");
        webUI.loadSettings(settingsFolder);
        logger.debug("Settings pushed to web UI plugins");
    }

    private void initializePlugins(Collection<PluginSet> plugins) {
        for (PluginSet set : plugins) {
            logger.debug("SetPlugins: {}", set);

            // provide platform to each plugin interface
            final Collection<Collection<?>> all = Arrays.asList(set.getStoragePlugins(), set.getIndexPlugins(),
                    set.getQueryPlugins(), set.getJettyPlugins(), set.getRestPlugins());
            for (Collection<?> interfaces : all) {
                if (interfaces == null) {
                    logger.debug("Plugin set {} provided a null collection!");
                    continue;
                }
                for (Object o : interfaces) {
                    this.preparer.injectPlatform(o);
                }
            }

            // and to the set itself
            this.preparer.setup(set);
        }
    }

    private void applySettings(PluginSet set, ConfigurationHolder holder) {
        // provide platform to each plugin interface
        final Collection<Collection<? extends DicooglePlugin>> all = Arrays.asList(set.getStoragePlugins(),
                set.getIndexPlugins(), set.getQueryPlugins(), set.getJettyPlugins());
        for (Collection<? extends DicooglePlugin> interfaces : all) {
            if (interfaces == null)
                continue;
            for (DicooglePlugin p : interfaces) {
                p.setSettings(holder);
            }
        }
        set.setSettings(holder);

    }

    /**
     * Each pluginSet provides a collection of barebone rest interfaces Here we
     * check which interfaces are present and create a restlet component to
     * handle them. also we export them using common settings and security
     * profiles
     */
    private void initRestInterface(Collection<PluginSet> plugins) {
        logger.info("Initializing plugin rest interfaces");

        ArrayList<ServerResource> restInterfaces = new ArrayList<>();
        for (PluginSet set : plugins) {
            Collection<? extends ServerResource> restInterface = set.getRestPlugins();
            if (restInterface == null) {
                continue;
            }
            restInterfaces.addAll(restInterface);
        }

        for (ServerResource resource : restInterfaces) {
            PluginRestletApplication.attachRestPlugin(resource);
        }
        logger.info("Finished initializing rest interfaces");
    }

    private void initJettyInterface(Collection<PluginSet> plugins) {
        logger.info("Initializing jetty interface");

        ArrayList<JettyPluginInterface> jettyInterfaces = new ArrayList<>();
        for (PluginSet set : plugins) {
            Collection<? extends JettyPluginInterface> jettyInterface = set.getJettyPlugins();
            if (jettyInterface == null)
                continue;
            jettyInterfaces.addAll(jettyInterface);
        }

        DicoogleWeb jettyServer = ControlServices.getInstance().getWebServicePlatform();
        for (JettyPluginInterface resource : jettyInterfaces) {
            jettyServer.addContextHandlers(resource.getJettyHandlers());
        }
    }

    /**
     * Stops the plugins and saves the settings
     */
    public void shutdown() {
        for (PluginSet plugin : pluginSets) {
            // TODO(#605) Consider auto-saving settings on shutdown:
            /*
            Settings settings = plugin.getSettings();
            if (settings != null) {
                settings.save();
            }
            */

            // let the plugin know we are shutting down
            try {
                logger.debug("Plugin set {} is shutting down", plugin.getName());
                plugin.shutdown();
            } catch (Exception ex) {
                logger.error("Plugin set {} did not shutdown gracefully", plugin.getName(), ex);
            }
        }
    }

    /**
     * stops a pluginset. this could be more efficient, however this is hardly a
     * bottleneck TODO: needs more granularity, we should be able to stop only
     * the indexers or the queryers
     *
     * @param pluginName
     */
    public void stopPlugin(String pluginName) {
        for (PluginSet pluginSet : pluginSets) {
            if (pluginSet.getName().compareTo(pluginName) == 0) {
                // pluginSet.stop();
                return;
            }
        }
    }

    public void startPlugin(String pluginName) {
        for (PluginSet pluginSet : pluginSets) {
            if (pluginSet.getName().compareTo(pluginName) == 0) {
                // pluginSet.stop();
                return;
            }
        }
    }

    public Collection<IndexerInterface> getIndexingPlugins(boolean onlyEnabled) {
        ArrayList<IndexerInterface> indexers = new ArrayList<>();
        for (PluginSet pSet : pluginSets) {
            for (IndexerInterface index : pSet.getIndexPlugins()) {
                if (!index.isEnabled() && onlyEnabled) {
                    continue;
                }
                indexers.add(index);
            }
        }
        return indexers;
    }

    public Collection<StorageInterface> getStoragePlugins(boolean onlyEnabled) {
        ArrayList<StorageInterface> storagePlugins = new ArrayList<>();
        for (PluginSet pSet : pluginSets) {
            for (StorageInterface store : pSet.getStoragePlugins()) {
                if (!store.isEnabled() && onlyEnabled) {
                    continue;
                }
                storagePlugins.add(store);
            }
        }
        return storagePlugins;
    }

    public Collection<JettyPluginInterface> getServletPlugins(boolean onlyEnabled) {
        List<JettyPluginInterface> plugins = new ArrayList<>();
        for (PluginSet pSet : pluginSets) {
            for (JettyPluginInterface p : pSet.getJettyPlugins()) {
                if (!p.isEnabled() && onlyEnabled) {
                    continue;
                }
                plugins.add(p);
            }
        }
        return plugins;
    }

    public Collection<JettyPluginInterface> getServletPlugins() {
        return this.getServletPlugins(true);
    }

    public Collection<MLProviderInterface> getMLPlugins(boolean onlyEnabled) {
        List<MLProviderInterface> plugins = new ArrayList<>();
        for (PluginSet pSet : pluginSets) {
            for (MLProviderInterface ml : pSet.getMLPlugins()) {
                if (!ml.isEnabled() && onlyEnabled) {
                    continue;
                }
                plugins.add(ml);
            }
        }
        return plugins;
    }

    public Collection<String> getPluginSetNames() {
        Collection<String> l = new ArrayList<>();
        for (PluginSet s : this.pluginSets) {
            l.add(s.getName());
        }
        return l;
    }

    public Collection<DeadPlugin> getDeadPluginSets() {
        return new ArrayList<>(this.deadPluginSets);
    }

    /**
     * Resolve a URI to a DicomInputStream
     * @param location
     * @param args
     * @return 
     */
    public Iterable<StorageInputStream> resolveURI(URI location, Object... args) {
        Collection<StorageInterface> storages = getStoragePlugins(true);

        for (StorageInterface store : storages) {
            if (store.handles(location)) {
                logger.debug("Resolving URI: {} Storage: {}", location, store.getName());
                return store.at(location, args);
            }
        }

        logger.error("Could not resolve uri: {}", location);
        return Collections.emptyList();
    }

    /** Retrieve a storage interface capable of handling files on a given location.
     * 
     * TODO: this can be heavily improved if we keep a map of scheme->indexer
     * However we are not supposed to call this every other cycle.
     *
     * TODO: we should return a proxy storage that always returns error
     * 
     * @todo "schema" is a typo, should read "scheme"
     * 
     * @param location a URI of the location, only the scheme matters
     * @return a storage interface capable of handling the location, null if no suitable plugin is found
     */
    public StorageInterface getStorageForSchema(URI location) {
        if (location == null) {
            logger.warn("URI for retrieving storage interface is null, ignoring");
            return null;
        }
        Collection<StorageInterface> storages = getStoragePlugins(false);

        for (StorageInterface store : storages) {
            if (store.handles(location)) {
                logger.debug("Retrieved storage for scheme: {}", location);
                return store;
            }
        }
        logger.warn("Could not get storage for scheme: {}", location);
        return null;
    }

    /** Retrieve a storage interface capable of handling files with the given scheme.
     * 
     * TODO: this can be heavily improved if we keep a map of scheme->indexer
     * However we are not supposed to call this every other cycle.
     *
     * TODO: we should return a proxy storage that always returns error
     * 
     * @param scheme a URI of the location, only the scheme matters
     * @return a storage interface capable of handling the location, null if no suitable plugin is found
     */
    public StorageInterface getStorageForSchema(String scheme) {
        URI uri = URI.create(scheme + ":/");
        return getStorageForSchema(uri);
    }

    public Collection<QueryInterface> getQueryPlugins(boolean onlyEnabled) {
        ArrayList<QueryInterface> queriers = new ArrayList<>();
        for (PluginSet pSet : pluginSets) {
            for (QueryInterface querier : pSet.getQueryPlugins()) {
                if (!querier.isEnabled() && onlyEnabled) {
                    continue;
                }
                queriers.add(querier);
            }
        }
        return queriers;
    }

    public List<String> getQueryProvidersName(boolean enabled) {
        Collection<QueryInterface> plugins = getQueryPlugins(enabled);
        List<String> names = new ArrayList<>(plugins.size());
        for (QueryInterface p : plugins) {
            names.add(p.getName());
        }
        return names;
    }

    public QueryInterface getQueryProviderByName(String name, boolean onlyEnabled) {
        Collection<QueryInterface> plugins = getQueryPlugins(onlyEnabled);
        for (QueryInterface p : plugins) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        logger.debug("Could not retrieve query provider {} for onlyEnabled = {}", name, onlyEnabled);
        return null;
    }

    /**
     * Filter a list of query providers, returning the ones that are DICOM and active.
     * If the list to filter is empty, return all the active DICOM query providers.
     * If there are no DICOM query providers specified in the settings, filter across all the active query providers.
     *
     * @param providers a list of query providers
     * @return the filtered list of active DICOM query providers
     */
    public List<String> filterDicomQueryProviders(List<String> providers) {
        List<String> baseProviders = ServerSettingsManager.getSettings().getArchiveSettings().getDIMProviders();

        if (baseProviders == null || baseProviders.isEmpty()) {
            baseProviders = PluginController.getInstance().getQueryProvidersName(true);
        }

        return baseProviders.stream().filter(p -> providers.isEmpty() || providers.contains(p))
                .collect(Collectors.toList());
    }

    // TODO: CONVENIENCE METHOD
    public IndexerInterface getIndexerByName(String name, boolean onlyEnabled) {
        Collection<IndexerInterface> plugins = getIndexingPlugins(onlyEnabled);
        for (IndexerInterface p : plugins) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        logger.debug("No indexer matching name {} for onlyEnabled = {}", name, onlyEnabled);
        return null;
    }


    public JettyPluginInterface getServletByName(String name, boolean onlyEnabled) {
        Collection<JettyPluginInterface> plugins = getServletPlugins(onlyEnabled);
        for (JettyPluginInterface p : plugins) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        logger.debug("No indexer matching name {} for onlyEnabled = {}", name, onlyEnabled);
        return null;
    }

    public StorageInterface getStorageByName(String name, boolean onlyEnabled) {
        Collection<StorageInterface> plugins = getStoragePlugins(onlyEnabled);
        for (StorageInterface p : plugins) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        logger.debug("No indexer matching name {} for onlyEnabled = {}", name, onlyEnabled);
        return null;
    }

    public MLProviderInterface getMachineLearningProviderByName(String name, boolean onlyEnabled) {
        Collection<MLProviderInterface> plugins = getMLPlugins(onlyEnabled);
        for (MLProviderInterface p : plugins) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        logger.debug("No machine learning provider matching name {} for onlyEnabled = {}", name, onlyEnabled);
        return null;
    }

    public JointQueryTask queryAll(JointQueryTask holder, final String query, final Object... parameters) {
        List<String> providers = this.getQueryProvidersName(true);
        return query(holder, providers, query, parameters);
    }

    public JointQueryTask queryAll(JointQueryTask holder, final String query, final DimLevel level,
            final Object... parameters) {
        List<String> providers = this.getQueryProvidersName(true);
        return query(holder, providers, query, level, parameters);
    }

    public Task<Iterable<SearchResult>> query(String querySource, final String query, final Object... parameters) {
        Task<Iterable<SearchResult>> t = getTaskForQuery(querySource, query, parameters);
        taskManagerQueries.dispatch(t);
        return t;

    }

    public Task<Iterable<SearchResult>> query(String querySource, final String query, final DimLevel level,
            final Object... parameters) {
        Task<Iterable<SearchResult>> t = getTaskForQueryDim(querySource, query, level, parameters);
        taskManagerQueries.dispatch(t);

        return t;// returns the handler to obtain the computation results
    }

    public JointQueryTask query(JointQueryTask holder, List<String> querySources, final String query,
            final Object... parameters) {
        if (holder == null)
            return null;

        List<Task<Iterable<SearchResult>>> tasks = new ArrayList<>();
        for (String p : querySources) {
            Task<Iterable<SearchResult>> task = getTaskForQuery(p, query, parameters);
            tasks.add(task);
            holder.addTask(task);
        }

        // and executes said task asynchronously
        for (Task<?> t : tasks)
            taskManagerQueries.dispatch(t);

        return holder;// returns the handler to obtain the computation results
    }

    public JointQueryTask query(JointQueryTask holder, List<String> querySources, final String query,
            final DimLevel level, final Object... parameters) {
        if (holder == null)
            return null;

        List<Task<Iterable<SearchResult>>> tasks = new ArrayList<>();
        for (String p : querySources) {
            Task<Iterable<SearchResult>> task = getTaskForQueryDim(p, query, level, parameters);
            tasks.add(task);
            holder.addTask(task);
        }

        // and executes said task asynchronously
        for (Task<?> t : tasks)
            taskManagerQueries.dispatch(t);

        return holder;// returns the handler to obtain the computation results
    }


    private Task<Iterable<SearchResult>> getTaskForQuery(final String querySource, final String query,
            final Object... parameters) {

        final QueryInterface queryEngine = getQueryProviderByName(querySource, true);
        // returns a tasks that runs the query from the selected query engine
        String uid = UUID.randomUUID().toString();
        Task<Iterable<SearchResult>> queryTask = new Task<>(uid, querySource, new Callable<Iterable<SearchResult>>() {
            @Override
            public Iterable<SearchResult> call() throws Exception {
                if (queryEngine == null)
                    return Collections.emptyList();
                try {
                    return queryEngine.query(query, parameters);
                } catch (RuntimeException ex) {
                    logger.warn("Query plugin {} failed unexpectedly", querySource, ex);
                    return Collections.emptyList();
                }

            }
        });
        return queryTask;
    }


    private Task<Iterable<SearchResult>> getTaskForQueryDim(final String querySource, final String query,
            final DimLevel level, final Object... parameters) {

        final QueryDimInterface queryEngine;
        QueryInterface tmpQueryDimInterface = getQueryProviderByName(querySource, true);
        if (tmpQueryDimInterface instanceof QueryDimInterface) {
            queryEngine = (QueryDimInterface) tmpQueryDimInterface;
        } else {
            logger.warn("Query plugin {} is not a DIM query provider", querySource);
            queryEngine = null;
        }

        // returns a tasks that runs the query from the selected query engine
        String uid = UUID.randomUUID().toString();
        Task<Iterable<SearchResult>> queryTask = new Task<>(uid, querySource, new Callable<Iterable<SearchResult>>() {
            @Override
            public Iterable<SearchResult> call() {
                if (queryEngine == null) {
                    logger.warn("Could not query provider {} as a DIM source", querySource);

                    return Collections.emptyList();
                }
                try {
                    return queryEngine.query(query, level, parameters);
                } catch (RuntimeException ex) {
                    logger.warn("Query plugin {} failed unexpectedly", querySource, ex);
                    return Collections.emptyList();
                }

            }
        });
        return queryTask;
    }



    /*
     * Given an URI (which may be a path to a dir or file, a web resource or whatever)
     * this method creates a task that
     * calls the appropriate indexers and instructs them to index the data pointed to by the URI
     * it is up to the caller to run the task asynchronously by feeding it to an executor
     * or in a blocking way by calling the get() method of the task
     */
    public List<Task<Report>> index(URI path) {
        logger.info("Starting Indexing procedure for {}", path.toString());
        StorageInterface store = getStorageForSchema(path);

        if (store == null) {
            logger.error("No storage plugin detected");
            return Collections.emptyList();
        }

        Collection<IndexerInterface> indexers = getIndexingPlugins(true);
        // Collection<IndexerInterface> indexers = getIndexingPluginsByMimeType(path);
        ArrayList<Task<Report>> rettasks = new ArrayList<>();
        final String pathF = path.toString();
        for (IndexerInterface indexer : indexers) {
            try {
                Task<Report> task = indexer.index(store.at(path));
                if (task == null)
                    continue;
                final String taskUniqueID = UUID.randomUUID().toString();
                task.setName(String.format("[%s]index %s", indexer.getName(), path));
                task.onCompletion(() -> {
                    logger.info("Task [{}] complete on {}", taskUniqueID, pathF);
                });

                taskManager.dispatch(task);
                rettasks.add(task);
                RunningIndexTasks.getInstance().addTask(task);
            } catch (RuntimeException ex) {
                logger.warn("Indexer {} failed unexpectedly", indexer.getName(), ex);
            }
        }
        logger.debug("Finished firing all indexing plugins for {}", path);

        return rettasks;
    }

    public List<Task<Report>> index(String pluginName, URI path) {
        logger.info("Starting Indexing procedure for {}", path);
        StorageInterface store = getStorageForSchema(path);

        if (store == null) {
            logger.error("No storage plugin detected");
            return Collections.emptyList();
        }

        final String taskUniqueID = UUID.randomUUID().toString();

        IndexerInterface indexer = getIndexerByName(pluginName, true);
        ArrayList<Task<Report>> rettasks = new ArrayList<>();
        final String pathF = path.toString();
        try {
            Task<Report> task = indexer.index(store.at(path));
            if (task != null) {
                task.setName(String.format("[%s]index %s", pluginName, path));
                task.onCompletion(new Runnable() {

                    @Override
                    public void run() {
                        logger.info("Task [{}] complete on {}", taskUniqueID, pathF);
                    }
                });

                taskManager.dispatch(task);

                rettasks.add(task);
                logger.debug("Fired indexer {} for URI {}", pluginName, path.toString());
                RunningIndexTasks.getInstance().addTask(task);
            }
        } catch (RuntimeException ex) {
            logger.warn("Indexer {} failed unexpectedly", indexer.getName(), ex);
        }

        return rettasks;
    }

    public List<Task<Report>> index(Collection<URI> paths) {
        logger.info("Starting indexing procedure for {} items", paths.size());

        List<StorageInputStream> objectsToStore = new ArrayList<>();
        ArrayList<Task<Report>> rettasks = new ArrayList<>();
        Collection<IndexerInterface> indexers = getIndexingPlugins(true);

        for (URI path : paths) {
            StorageInterface store = getStorageForSchema(path);

            if (store == null) {
                logger.error("Could not get storage schema for {}", path);
                continue;
            }

            store.at(path).forEach(objectsToStore::add);
        }

        for (IndexerInterface indexer : indexers) {
            try {
                Task<Report> task = indexer.index(objectsToStore);
                if (task == null)
                    continue;
                final String taskUniqueID = UUID.randomUUID().toString();
                task.setName(String.format("[%s]index %d items", indexer.getName(), objectsToStore.size()));
                task.onCompletion(() -> {
                    logger.info("Task [{}] complete on {} items", taskUniqueID, objectsToStore.size());
                });

                taskManager.dispatch(task);
                rettasks.add(task);
                RunningIndexTasks.getInstance().addTask(task);
            } catch (RuntimeException ex) {
                logger.warn("Indexer {} failed unexpectedly", indexer.getName(), ex);
            }

        }

        return rettasks;
    }

    public void unindex(URI path) {
        logger.info("Starting unindexing procedure for {}", path.toString());
        this.doUnindex(path, this.getIndexingPlugins(true));
    }

    /** Issue the removal of indexed entries in a path from the given indexers.
     *
     * @param path the URI of the directory or file to unindex
     * @param indexProviders a collection of providers
     */
    public void unindex(URI path, Collection<String> indexProviders) {
        logger.info("Starting unindexing procedure for {}", path);

        if (indexProviders != null) {
            List<IndexerInterface> indexers = new ArrayList<>();
            for (String provider : indexProviders) {
                indexers.add(this.getIndexerByName(provider, true));
            }
            this.doUnindex(path, indexers);
        } else {
            this.doUnindex(path, this.getIndexingPlugins(true));
        }
    }

    /** Issue the removal of indexed entries in bulk.
     *
     * @param paths a collections of item identifiers to unindex
     * @param progressCallback an optional function (can be `null`),
     *        called for every batch of items successfully unindexed
     *        to indicate early progress
     *        and inform consumers that
     *        it is safe to remove or exclude the unindexed item
     * @return an asynchronous task object returning
     *         a report containing which files were not unindexed,
     *         and whether some of them were not found in the database
     */
    public List<Task<UnindexReport>> unindex(Collection<URI> paths, Consumer<Collection<URI>> progressCallback) {
        List<Task<UnindexReport>> tasks = new ArrayList<>();
        for (IndexerInterface indexer : this.getIndexingPlugins(true))
            tasks.add(createUnindexTask(paths, progressCallback, indexer));

        return tasks;
    }

    /** Issue the removal of indexed entries in bulk.
     *
     * @param indexProvider the name of the indexer
     * @param paths a collections of item identifiers to unindex
     * @param progressCallback an optional function (can be `null`),
     *        called for every batch of items successfully unindexed
     *        to indicate early progress
     *        and inform consumers that
     *        it is safe to remove or exclude the unindexed item
     * @return an asynchronous task object returning
     *         a report containing which files were not unindexed,
     *         and whether some of them were not found in the database
     */
    public Task<UnindexReport> unindex(String indexProvider, Collection<URI> paths,
            Consumer<Collection<URI>> progressCallback) {

        IndexerInterface indexer = null;
        if (indexProvider != null) {
            indexer = this.getIndexerByName(indexProvider, true);
        }
        if (indexer == null) {
            indexer = this.getIndexingPlugins(true).iterator().next();
        }
        return createUnindexTask(paths, progressCallback, indexer);
    }

    /** Issue an unindexing procedure to the given indexers.
     *
     * @param path the URI of the directory or file to unindex
     * @param indexers a collection of providers
     */
    private void doUnindex(URI path, Collection<IndexerInterface> indexers) {
        for (IndexerInterface indexer : indexers) {
            indexer.unindex(path);
        }
        logger.info("Finished unindexing {}", path);
    }

    public void remove(URI uri) {
        StorageInterface si = getStorageForSchema(uri);
        if (si != null)
            doRemove(uri, si);
        else
            logger.error("Could not find storage plugin to handle URI: {}", uri);
    }

    public void doRemove(URI uri, StorageInterface si) {
        if (Objects.equals(uri.getScheme(), si.getScheme())) {
            si.remove(uri);
        } else {
            logger.warn("Storage Plugin does not handle URI: {},{}", uri, si);
        }
        logger.info("Finished removing {}", uri);
    }

    /*
     * Convinience method that calls index(URI) and runs the returned
     * tasks on the executing thread 
     */
    public List<Report> indexBlocking(URI path) {
        logger.info("Starting indexing blocking procedure for {}", path);
        List<Task<Report>> ret = index(path);

        ArrayList<Report> reports = new ArrayList<>(ret.size());
        for (Task<Report> t : ret) {
            try {
                reports.add(t.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("Finished indexing {}", path);

        return reports;
    }

    /**
     * Generate and dispatch an unindex task.
     * @param items Collection of URIs to unindex
     * @param progressCallback Callback raised when URIs are declared
     * @param indexer
     * @return
     */
    private Task<UnindexReport> createUnindexTask(Collection<URI> items, Consumer<Collection<URI>> progressCallback,
            IndexerInterface indexer) {
        logger.info("[{}] Starting unindexing procedure for {} items", indexer.getName(), items.size());

        Task<UnindexReport> task = null;
        try {
            task = indexer.unindex(items, progressCallback);
        } catch (IOException e) {
            logger.error("IO exception ocurred while creating unindex task", e);
        }

        if (task != null) {
            final String taskUniqueID = UUID.randomUUID().toString();
            task.setName(String.format("[%s]unindex", indexer.getName()));
            task.onCompletion(() -> {
                logger.info("Unindexing task [{}] complete", taskUniqueID);
            });
            taskManager.dispatch(task);
        }
        return task;
    }

    /**
     * This method orders a prediction on the selected image, using the selected provider.
     * @param bos image to classify.
     * @param provider provider to use.
     * @param modelID the model identifier of the provider to use.
     * @return the created task
     */
    public Task<MLPrediction> makePredictionOverImage(final ByteArrayOutputStream bos, final String provider, final String modelID) {
        MLProviderInterface providerInterface = this.getMachineLearningProviderByName(provider, true);
        if(providerInterface == null)
            return null;

        String taskName = "MLPredictionTask" + UUID.randomUUID();
        Task<MLPrediction> result = providerInterface.makePredictionOverImage(bos, modelID);
        result.setName(taskName);
        return result;
    }

    /**
     * This method creates a {@link PrepareDatasetTask}.
     * The task is responsible for creating a directory where the processed dataset will be placed.
     * After the task is finished, the chosen mlProvider will be invoked to upload the dataset.
     * @param datasetRequest the dataset to upload.
     * @return the created task
     */
    public Task<MLDataset> prepareMLDataset(final CreateDatasetRequest datasetRequest) {
        String uuid = UUID.randomUUID().toString();
        Task<MLDataset> prepareTask =
                new Task<>("MLPrepareDatasetTask" + uuid, new PrepareDatasetTask(this, datasetRequest));
        prepareTask.onCompletion(() -> {
            MLProviderInterface mlInterface = getMachineLearningProviderByName(datasetRequest.getProviderName(), true);
            if (mlInterface == null) {
                logger.error("MLProvider with name {} not found", prepareTask.getName());
            } else {
                try {
                    mlInterface.createDataset(prepareTask.get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Task {} failed execution", prepareTask.getName(), e);
                }
            }
        });
        logger.debug("Fired prepare dataset task with uuid {}", uuid);
        taskManagerML.dispatch(prepareTask);
        return prepareTask;
    }

    // Methods for Web UI

    /** Retrieve all web UI plugin descriptors for the given slot id.
     *
     * @param ids the slot id's for the plugin ("query", "result", "menu", ...), empty or null for any slot
     * @return a collection of web UI plugins.
     */
    public Collection<WebUIPlugin> getWebUIPlugins(String... ids) {
        logger.debug("getWebUIPlugins(slot ids: {})", ids != null ? Arrays.asList(ids) : "<any>");
        List<WebUIPlugin> plugins = new ArrayList<>();
        Set<String> idSet = Collections.EMPTY_SET;
        if (ids != null) {
            idSet = new HashSet<>(Arrays.asList(ids));
        }
        for (WebUIPlugin plugin : webUI.pluginSet()) {
            if (!plugin.isEnabled()) {
                continue;
            }
            if (idSet.isEmpty() || idSet.contains(plugin.getSlotId())) {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    /** Retrieve the web UI plugin descriptor of the plugin with the given name.
     *
     * @param name the unique name of the plugin
     * @return a web UI plugin descriptor object, or null if no such plugin exists or is inactive
     */
    public WebUIPlugin getWebUIPlugin(String name) {
        logger.debug("getWebUIPlugin(name: {})", name);
        WebUIPlugin plugin = webUI.get(name);
        return plugin == null ? null : plugin.isEnabled() ? plugin : null;
    }

    /** Retrieve the web UI plugin descriptor package.json.
     *
     * @param name the unique name of the plugin
     * @return the full contents of the package.json, null if the plugin is not available
     */
    public String getWebUIPackageJSON(String name) {
        logger.debug("getWebUIPackageJSON(name: {})", name);
        try {
            Object o = webUI.retrieveJSON(name);
            return (o != null) ? o.toString() : null;
        } catch (IOException ex) {
            logger.error("Failed to retrieve package JSON", ex);
            return null;
        }
    }

    /** Retrieve the web UI plugin module code.
     *
     * @param name the unique name of the plugin
     * @return the full contents of the module file, null if the plugin is not available
     */
    public String getWebUIModuleJS(String name) {
        logger.debug("getWebUIModuleJS(name: {})", name);
        try {
            return webUI.retrieveModuleJS(name);
        } catch (IOException ex) {
            logger.error("Failed to retrieve module", ex);
            return null;
        }
    }
}
