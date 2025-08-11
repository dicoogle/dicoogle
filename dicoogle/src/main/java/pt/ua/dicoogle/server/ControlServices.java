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
package pt.ua.dicoogle.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;
import pt.ua.dicoogle.server.queryretrieve.QueryRetrieve;

import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.server.web.DicoogleWeb;

/**
 *
 * @author Luís Bastião Silva <bastiao@bmd-softwre.com>
 * @author Samuel Campos <samuelcampos@ua.pt>
 */
public class ControlServices {
    private static final Logger logger = LoggerFactory.getLogger(ControlServices.class);

    private static ControlServices instance = null;
    // Services vars
    private DicomStorage storage = null;
    private boolean webServerRunning = false;
    private QueryRetrieve retrieve = null;

    private DicoogleWeb webServices;

    private ControlServices() {
        autoStartServices();
    }

    public static synchronized ControlServices getInstance() {
        // sem.acquire();
        if (instance == null) {
            instance = new ControlServices();
        }
        // sem.release();
        return instance;
    }


    /* Starts the initial services based on server settings. */
    private void autoStartServices() {
        ServerSettings settings = ServerSettingsManager.getSettings();

        ServerSettings.DicomServices dicomSettings = settings.getDicomServicesSettings();

        if (!this.storageIsRunning() && dicomSettings.getStorageSettings().isAutostart()) {
            try {
                startStorage();
            } catch (IOException ex) {
                logger.error("Failed to start the DICOM storage service", ex);
            }
        }

        if (!this.queryRetrieveIsRunning() && dicomSettings.getQueryRetrieveSettings().isAutostart()) {
            startQueryRetrieve();
        }

        if (!this.webServerIsRunning() && settings.getWebServerSettings().isAutostart()) {
            startWebServer();
        }
    }

    /* Stop all services that are running */
    public boolean stopAllServices() {
        try {
            // TODO: DELETED
            // PluginController.getSettings().stopAll();
            // stopP2P();
            stopStorage();
            stopQueryRetrieve();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    /**
     * @return `true` if everything is fine and the service was started,
     *         `false` if the service was already running
     * @throws IOException if the storage service could not start
     */
    public boolean startStorage() throws IOException {
        if (storage == null) {
            ServerSettings settings = ServerSettingsManager.getSettings();

            SOPList list = SOPList.getInstance();
            list.readFromSettings(settings);

            List<String> l = list.getKeys();
            String[] keys = new String[l.size()];

            int i;
            for (i = 0; i < l.size(); i++) {
                keys[i] = l.get(i);
            }
            storage = new DicomStorage(keys, list);
            storage.start();

            logger.info("Starting DICOM Storage SCP");

            return true;
        }

        return false;
    }

    public void stopStorage() {
        if (storage != null) {
            storage.stop();
            storage = null;
            logger.info("Stopping DICOM Storage SCP");
        }
    }

    public boolean storageIsRunning() {
        return storage != null;
    }

    public void startQueryRetrieve() {
        if (retrieve == null) {
            retrieve = new QueryRetrieve();
            retrieve.startListening();
            // DebugManager.getInstance().debug("Starting DICOM QueryRetrive");
            logger.info("Starting DICOM QueryRetrieve");
        }
    }

    public void stopQueryRetrieve() {
        if (retrieve != null) {
            retrieve.stopListening();
            retrieve = null;
            logger.info("Stopping DICOM QueryRetrieve");
        }
    }

    public boolean queryRetrieveIsRunning() {
        return retrieve != null;
    }

    public boolean webServerIsRunning() {
        return webServerRunning;
    }

    public void startWebServer() {
        logger.info("Starting WebServer");

        try {
            if (webServices == null) {
                int port = ServerSettingsManager.getSettings().getWebServerSettings().getPort();
                String hostname = ServerSettingsManager.getSettings().getWebServerSettings().getHostname();
                InetSocketAddress bindAddr;
                if (hostname == null) {
                    bindAddr = new InetSocketAddress(port);
                } else {
                    bindAddr = new InetSocketAddress(hostname, port);
                }
                logger.info("Starting Dicoogle Web");
                webServices = new DicoogleWeb(bindAddr);
                webServerRunning = true;
            }
        } catch (Exception ex) {
            logger.error("Failed to launch the web server", ex);
        }

    }

    public void stopWebServer() {
        logger.info("Stopping Web Server");

        if (webServices != null) {
            try {
                webServerRunning = false;

                webServices.stop();

                webServices = null;
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        logger.info("Stopping Dicoogle Web");
    }

    public DicoogleWeb getWebServicePlatform() {
        return webServices;
    }

}
