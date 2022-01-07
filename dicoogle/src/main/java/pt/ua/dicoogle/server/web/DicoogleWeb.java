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
package pt.ua.dicoogle.server.web;

import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.plugins.webui.WebUIPlugin;
import pt.ua.dicoogle.sdk.utils.TagsStruct;
import pt.ua.dicoogle.server.web.rest.VersionResource;
import pt.ua.dicoogle.server.web.servlets.*;
import pt.ua.dicoogle.server.web.servlets.plugins.PluginsServlet;
import pt.ua.dicoogle.server.web.servlets.management.*;
import pt.ua.dicoogle.server.web.servlets.search.*;
import pt.ua.dicoogle.server.web.servlets.search.ExportServlet.ExportType;
import pt.ua.dicoogle.server.web.servlets.search.SearchServlet.SearchType;
import pt.ua.dicoogle.server.web.servlets.accounts.LoginServlet;
import pt.ua.dicoogle.server.web.servlets.accounts.UserServlet;

//import pt.ua.dicoogle.core.ServerSettings;

import pt.ua.dicoogle.server.web.servlets.management.AETitleServlet;
import pt.ua.dicoogle.server.web.servlets.management.DicomQuerySettingsServlet;
import pt.ua.dicoogle.server.web.servlets.management.ForceIndexing;
import pt.ua.dicoogle.server.web.servlets.management.IndexerSettingsServlet;
import pt.ua.dicoogle.server.web.servlets.management.LoggerServlet;
import pt.ua.dicoogle.server.web.servlets.management.RemoveServlet;
import pt.ua.dicoogle.server.web.servlets.management.RunningTasksServlet;
import pt.ua.dicoogle.server.web.servlets.management.ServerStorageServlet;
import pt.ua.dicoogle.server.web.servlets.management.ServicesServlet;
import pt.ua.dicoogle.server.web.servlets.management.TransferOptionsServlet;

import pt.ua.dicoogle.server.web.servlets.mlprovider.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.EnumSet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import pt.ua.dicoogle.server.LegacyRestletApplication;
import pt.ua.dicoogle.server.web.servlets.accounts.LogoutServlet;
import pt.ua.dicoogle.server.web.servlets.search.DumpServlet;
import pt.ua.dicoogle.server.web.servlets.management.UnindexServlet;
import pt.ua.dicoogle.server.web.servlets.webui.WebUIModuleServlet;
import pt.ua.dicoogle.server.web.servlets.webui.WebUIServlet;
import pt.ua.dicoogle.server.web.utils.LocalImageCache;
import pt.ua.dicoogle.server.PluginRestletApplication;
import pt.ua.dicoogle.server.web.utils.SimpleImageRetriever;

/**
 * @author António Novo <antonio.novo@ua.pt>
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Frederico Valente
 * @author Frederico Silva <fredericosilva@ua.pt>
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class DicoogleWeb {

    private static final Logger logger = LoggerFactory.getLogger(DicoogleWeb.class);
    /**
     * Sets the path where the web-pages/scripts or .war are.
     */
    public static final String WEBAPPDIR = "webapp";

    /**
     * Sets the context path used to serve the contents.
     */
    public static final String CONTEXTPATH = "/";
    private LocalImageCache cache = null;
    private Server server = null;

    private final ContextHandlerCollection contextHandlers;
    private ServletContextHandler pluginHandler = null;
    private PluginRestletApplication pluginApp = null;
    private ServletContextHandler legacyHandler = null;
    private LegacyRestletApplication legacyApp = null;

    /**
     * Initializes and starts the Dicoogle Web service.
     * @param socketAddr the server binding socket address
     * @throws java.lang.Exception
     */
    public DicoogleWeb(InetSocketAddress socketAddr) throws Exception {
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
        // System.setProperty("org.mortbay.jetty.webapp.parentLoaderPriority", "true");
        // System.setProperty("production.mode", "true");

        // "build" the input location, based on the www directory/.war chosen
        final URL warUrl = Thread.currentThread().getContextClassLoader().getResource(WEBAPPDIR);
        final String warUrlString = warUrl.toExternalForm();

        // setup the DICOM to PNG image servlet, with a local cache
        cache = new LocalImageCache("dic2png", 300, 900, new SimpleImageRetriever()); // pooling rate of 12/hr and max un-used cache age of 15 minutes
        final ServletContextHandler dic2png = createServletHandler(new ImageServlet(cache), "/dic2png");
        cache.start(); // start the caching system

        // setup the ROI extractor
        final ServletContextHandler roiExtractor = createServletHandler(new ROIServlet(), "/roi");

        // setup the DICOM to PNG image servlet
        final ServletContextHandler dictags = createServletHandler(new TagsServlet(), "/dictags");

        // setup the Export to CSV Servlet
        final ServletContextHandler csvServletHolder = createServletHandler(new ExportToCSVServlet(), "/export");
        File tempDir = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
        csvServletHolder.addServlet(new ServletHolder(new ExportCSVToFILEServlet(tempDir)), "/exportFile");

        // setup the search (DIMSE-service-user C-FIND ?!?) servlet
        // final ServletContextHandler search = new ServletContextHandler(ServletContextHandler.SESSIONS); // servlet with session support enabled
        // search.setContextPath(CONTEXTPATH);
        // search.addServlet(new ServletHolder(new SearchServlet()), "/search");

        /*hooks = getRegisteredHookActions();
         plugin.addServlet(new ServletHolder(new PluginsServlet(hooks)), "/plugin/*");
         */

        // setup the web pages/scripts app
        final WebAppContext webpages = new WebAppContext(warUrlString, CONTEXTPATH);
        webpages.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false"); // disables directory listing
        webpages.setInitParameter("useFileMappedBuffer", "false");
        webpages.setInitParameter("cacheControl", "public, max-age=2592000"); // cache for 30 days
        webpages.setInitParameter("etags", "true"); // generate and handle weak entity validation tags
        webpages.setDisplayName("webapp");
        webpages.setWelcomeFiles(new String[] {"index.html"});
        webpages.addServlet(new ServletHolder(new SearchHolderServlet()), "/search/holders");
        webpages.addFilter(GzipFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        this.pluginApp = new PluginRestletApplication();
        this.pluginHandler = createServletHandler(new RestletHttpServlet(this.pluginApp), "/ext/*");

        this.legacyApp = new LegacyRestletApplication();
        this.legacyHandler = createServletHandler(new RestletHttpServlet(this.legacyApp), "/legacy/*");

        // Add Static RESTlet Plugins
        PluginRestletApplication.attachRestPlugin(new VersionResource());

        // list the all the handlers mounted above
        Handler[] handlers = new Handler[] {pluginHandler, legacyHandler, dic2png, roiExtractor, dictags,
                createServletHandler(new IndexerServlet(), "/indexer"), // DEPRECATED
                createServletHandler(new SettingsServlet(), "/settings"), csvServletHolder,
                createServletHandler(new LoginServlet(), "/login"),
                createServletHandler(new LogoutServlet(), "/logout"),
                createServletHandler(new UserServlet(), "/user/*"),
                createServletHandler(new SearchServlet(), "/search"),
                createServletHandler(new SearchServlet(SearchType.PATIENT), "/searchDIM"),
                createServletHandler(new DumpServlet(), "/dump"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.path),
                        "/management/settings/index/path"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.zip),
                        "/management/settings/index/zip"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.effort),
                        "/management/settings/index/effort"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.thumbnail),
                        "/management/settings/index/thumbnail"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.watcher),
                        "/management/settings/index/watcher"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.thumbnailSize),
                        "/management/settings/index/thumbnail/size"),
                createServletHandler(new IndexerSettingsServlet(IndexerSettingsServlet.SettingsType.all),
                        "/management/settings/index"),
                createServletHandler(new TransferOptionsServlet(), "/management/settings/transfer"),
                createServletHandler(new WadoServlet(), "/wado"),
                createServletHandler(new ProvidersServlet(), "/providers"),
                createServletHandler(new DicomQuerySettingsServlet(), "/management/settings/dicom/query"),
                createServletHandler(new DimTagsServlet(TagsStruct.getInstance()), "/management/settings/dicom/tags"),
                createServletHandler(new ForceIndexing(), "/management/tasks/index"),
                createServletHandler(new UnindexServlet(), "/management/tasks/unindex"),
                createServletHandler(new RemoveServlet(), "/management/tasks/remove"),
                createServletHandler(new ServicesServlet(ServicesServlet.ServiceType.STORAGE), "/management/dicom/storage"),
                createServletHandler(new ServicesServlet(ServicesServlet.ServiceType.QUERY), "/management/dicom/query"),
                createServletHandler(new AETitleServlet(), "/management/settings/dicom"),
                createServletHandler(new PluginsServlet(), "/plugins/*"),
                createServletHandler(new PresetsServlet(), "/presets/*"),
                createServletHandler(new WebUIServlet(), "/webui"), createWebUIModuleServletHandler(),
                createServletHandler(new LoggerServlet(), "/logger"),
                createServletHandler(new RunningTasksServlet(), "/index/task"),
                createServletHandler(new ExportServlet(ExportType.EXPORT_CVS), "/export/cvs"),
                createServletHandler(new ExportServlet(ExportType.LIST), "/export/list"),
                createServletHandler(new ServerStorageServlet(), "/management/settings/storage/dicom"),

                // ml provider servlets
                createServletHandler(new CreateDatasetServlet(), "/ml/createDataset"),
                createServletHandler(new MakePredictionServlet(), "/ml/makePrediction"),
                createServletHandler(new MakeBulkPredictionServlet(), "/ml/makeBulkPrediction"), webpages};

        // setup the server
        server = new Server(socketAddr);
        // register the handlers on the server
        this.contextHandlers = new ContextHandlerCollection();
        this.contextHandlers.setHandlers(handlers);
        server.setHandler(this.contextHandlers);

        server.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                logger.info("Dicoogle Web Services available at {}",
                        URI.create("http://" + socketAddr.getHostString() + ":" + socketAddr.getPort()));
            }

            @Override
            public void lifeCycleStopped(LifeCycle event) {
                logger.info("Dicoogle Web Services stopped");
            }

            @Override
            public void lifeCycleFailure(LifeCycle event, Throwable cause) {
                logger.error("Dicoogle Web Services failed to start", cause);
            }

            // remove the methods below once Jetty is updated to 9.4

            @Override
            public void lifeCycleStarting(LifeCycle event) {
                // no-op
            }

            @Override
            public void lifeCycleStopping(LifeCycle event) {
                // no-op
            }
        });

        // Increase maxFormContentSize to avoid issues with big forms
        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", 3325000);

        // and then start the server
        server.start();
    }

    private ServletContextHandler createWebUIModuleServletHandler() {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS); // servlet with session support enabled
        handler.setContextPath(CONTEXTPATH);

        HttpServlet servletModule = new WebUIModuleServlet();
        // CORS support
        this.addCORSFilter(handler);
        // Caching!
        FilterHolder cacheHolder = new FilterHolder(new AbstractCacheFilter() {
            @Override
            protected String etag(HttpServletRequest req) {
                String name = req.getRequestURI().substring("/webui/module/".length());
                WebUIPlugin plugin = PluginController.getInstance().getWebUIPlugin(name);
                if (plugin == null)
                    return null;
                if (WebUIModuleServlet.isPrerelease(plugin.getVersion())) {
                    // pre-release, use hash (to facilitate development)
                    String fingerprint = PluginController.getInstance().getWebUIModuleJS(name);
                    return '"' + Hashing.murmur3_32().hashString(fingerprint, StandardCharsets.UTF_8).toString() + '"';
                } else {
                    // normal release, use weak ETag
                    String pProcess = req.getParameter("process");
                    boolean process = pProcess == null || Boolean.parseBoolean(pProcess);
                    if (process) {
                        return "W/\"" + plugin.getName() + '@' + plugin.getVersion() + '"';
                    } else {
                        return "W/\"" + plugin.getName() + '@' + plugin.getVersion() + ";raw\"";
                    }
                }
            }
        });
        cacheHolder.setInitParameter(AbstractCacheFilter.CACHE_CONTROL_PARAM, "private, max-age=2592000"); // cache for 30 days
        handler.addFilter(cacheHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addServlet(new ServletHolder(servletModule), "/webui/module/*");
        return handler;
    }

    private ServletContextHandler createServletHandler(HttpServlet servlet, String path) {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS); // servlet with session support enabled
        handler.setContextPath(CONTEXTPATH);

        // CORS support
        this.addCORSFilter(handler);

        handler.addServlet(new ServletHolder(servlet), path);
        return handler;
    }

    private void addCORSFilter(ServletContextHandler handler) {
        String origins = ServerSettingsManager.getSettings().getWebServerSettings().getAllowedOrigins();
        if (origins != null) {
            handler.setDisplayName("cross-origin");
            FilterHolder corsHolder = new FilterHolder(CORSFilter.class);
            corsHolder.setInitParameter(CORSFilter.ALLOWED_ORIGINS_PARAM, origins);
            corsHolder.setInitParameter(CORSFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,PUT,DELETE");
            corsHolder.setInitParameter(CORSFilter.ALLOWED_HEADERS_PARAM,
                    "X-Requested-With,Content-Type,Accept,Origin,Authorization,Content-Length");
            handler.addFilter(corsHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
        }
    }

    private void addCORSFilter(Handler handler) {
        String origins = ServerSettingsManager.getSettings().getWebServerSettings().getAllowedOrigins();
        if (origins == null) {
            return;
        }

        logger.debug("Applying CORS filter to {}", handler);
        if (handler instanceof ServletContextHandler) {
            ServletContextHandler svHandler = (ServletContextHandler) handler;
            this.addCORSFilter(svHandler);
            logger.debug("Applied CORS filter to {}!", svHandler);
        } else if (handler instanceof HandlerWrapper) {
            for (Handler h : ((HandlerWrapper) handler).getHandlers()) {
                addCORSFilter(h);
            }
        } else if (handler instanceof HandlerCollection) {
            for (Handler h : ((HandlerCollection) handler).getHandlers()) {
                addCORSFilter(h);
            }
        }
    }

    /**
     * Stops the Dicoogle Web service.
     * @throws java.lang.Exception if a problem occurs when stopping the server
     */
    public void stop() throws Exception {
        // abort if the server is not running
        if (server == null) {
            return;
        }

        try {
            // stop the server
            server.stop();

            // voiding its value
            server = null;
        } finally {

            // and remove the local cache, if any
            if (cache != null) {
                cache.terminate();
                cache = null;
            }
        }

        this.pluginHandler = null;
    }

    public void addContextHandlers(HandlerList handler) {
        this.contextHandlers.addHandler(handler);

        this.addCORSFilter(handler);
        // this.server.setHandler(this.contextHandlers);
    }

    public void stopPluginWebServices() {
        if (this.pluginHandler != null) {
            this.contextHandlers.removeHandler(this.pluginHandler);
        }
    }

    public void stopLegacyWebServices() {
        if (this.legacyHandler != null) {
            this.contextHandlers.removeHandler(this.legacyHandler);
        }
    }

}
