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
package pt.ua.dicoogle.core.settings;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.UID;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.core.XMLSupport;
import pt.ua.dicoogle.sdk.datastructs.AdditionalSOPClass;
import pt.ua.dicoogle.sdk.datastructs.AdditionalTransferSyntax;
import pt.ua.dicoogle.sdk.datastructs.MoveDestination;
import pt.ua.dicoogle.sdk.datastructs.SOPClass;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;
import pt.ua.dicoogle.server.SOPList;
import pt.ua.dicoogle.server.web.utils.types.DataTable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Legacy implementation of server settings.
 *
 * @author Marco Pereira
 * @author Luís A. Bastião Silva <bastiao@bmd-software.com>
 * @author António Novo <antonio.novo@ua.pt>
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 * @see XMLSupport
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonRootName("config")
public class LegacyServerSettings implements ServerSettings {
    private String AETitle;

    // Access List Settings
    private String[] CAETitle;
    private boolean permitAllAETitles;

    private String Path;
    private String ID;
    private int storagePort;

    @Deprecated
    private int rGUIPort;
    @Deprecated
    private String RGUIExternalIP;

    // Dicoogle Settings
    private String dicoogleDir;
    private boolean fullContentIndex;
    private boolean saveThumbnails;
    private int thumbnailsMatrix;
    // private boolean P2P;

    private boolean storage;
    private boolean queryRetrieve;
    private boolean encryptUsersFile;

    /**
     * Indicates, for each plugin, if it is to start at server init or not.
     */
    private final ConcurrentHashMap<String, Boolean> autoStartPlugin; // NOTE the concurrent hash map is used to prevent having to synchronize the methods that use it

    /**
     * The name of the Remote GUI setting that indicates the External IP address.
     */
    public static final String RGUI_SETTING_EXTERNAL_IP = "External IP";

    /**
     * The name of the Query Retrieve setting that indicates the Maximum number of Client Associations.
     */
    public static final String QUERYRETRIEVE_MAX_ASSOCIATIONS = "Max. Associations";
    /**
     * The name of the Query Retrieve setting that indicates the Maximum PDU Length Receive.
     */
    public static final String QUERYRETRIEVE_MAX_PDU_RECEIVE = "Max. PDU Receive";
    /**
     * The name of the Query Retrieve setting that indicates the Maximum PDU Length Send.
     */
    public static final String QUERYRETRIEVE_MAX_PDU_SEND = "Max. PDU Send";
    /**
     * The name of the Query Retrieve setting that indicates the Idle Timeout.
     */
    public static final String QUERYRETRIEVE_IDLE_TIMEOUT = "Idle Timeout";
    /**
     * The name of the Query Retrieve setting that indicates the Accpet Timeout.
     */
    public static final String QUERYRETRIEVE_ACCEPT_TIMEOUT = "Accept Timeout";
    /**
     * The name of the Query Retrieve setting that indicates the Response Timeout.
     */
    public static final String QUERYRETRIEVE_RESPONSE_TIMEOUT = "Response Timeout";
    /**
     * The name of the Query Retrieve setting that indicates the Connection Timeout.
     */
    public static final String QUERYRETRIEVE_CONNECTION_TIMEOUT = "Connection Timeout";

    /**
     * The name of the Storage setting that indicates the Servers Destinations.
     */
    public static final String STORAGE_SETTING_SERVERS_DESTINATIONS = "Storage Servers Destinations";
    /**
     * The name of the Storage setting that indicates the Storage Path.
     */
    public static final String STORAGE_SETTING_PATH = "Storage Path";

    /**
     * The help for the Remote GUI External IP setting.
     */
    public static final String RGUI_SETTING_EXTERNAL_IP_HELP =
            "If your Dicoogle GUI Server is running behind a router, you need to provide your external IP address to have access from outside of the router.\nBesides that, you need to configure your router to open the Remote GUI Port!";

    /**
     * QueryRetrieve Server
     */

    private boolean wlsOn = false;

    /* DEFAULT Brief class description */
    private String deviceDescription;
    /* DEFAULT Process Worklist Server AE Title */
    private String localAETName;

    /* DEFAULT ("any"->null)Permited local interfaces to incomming connection
     * ('null'->any interface; 'ethx'->only this interface |->separator */
    private String permitedLocalInterfaces;

    /* DEFAULT ("any"->null)Permited remote host name connections
     * ('null'->any can connect; 'www.x.com'->only this can connect |->separator */
    private String permitedRemoteHostnames;

    /* DEFAULT Dimse response timeout (in sec) */
    private int DIMSERspTimeout;
    // Connection settings

    /* DEFAULT Listening TCP port */
    private int wlsPort;
    /* DEFAULT Response delay (in miliseconds) */
    private int rspDelay;
    /* DEFAULT Idle timeout (in sec) */
    private int idleTimeout;
    /* DEFAULT Accept timeout (in sec) */
    private int acceptTimeout;
    /* DEFAULT Connection timeout (in sec) */
    private int connectionTimeout;

    private int maxMessages = 2000;
    private String sopClass;
    private String transfCAP;

    /* DEFAULT Max Client Associations */
    private int maxClientAssocs;

    private int maxPDULengthReceive;
    private int maxPDULengthSend;


    HashMap<String, String> modalityFind = new HashMap<>();

    ArrayList<MoveDestination> dest = new ArrayList<>();

    private Set<String> priorityAETitles = new HashSet<>();

    private boolean indexAnonymous = false;

    private boolean indexZIPFiles = true;

    private boolean monitorWatcher = false;

    /**
     * P2P
     */

    private String p2pLibrary = "JGroups";
    private String nodeName = "Dicoogle";
    private boolean nodeNameDefined = false;

    private String networkInterfaceName = "";

    /** Indexer */
    private String indexer = "lucene2.2";
    private int indexerEffort = 0;
    private HashSet<String> extensionsAllowed = new HashSet<>();

    private boolean gzipStorage = false;
    private final String aclxmlFileName = "aetitleFilter.xml";

    /**
     * @return the web
     */
    @Override
    public Web getWebServerSettings() {
        return web;
    }

    /**
     * @param web the web to set
     */
    public void setWeb(Web web) {
        this.web = web;
    }

    /**
     * @return the p2pLibrary
     */
    public String getP2pLibrary() {
        return p2pLibrary;
    }

    /**
     * @param p2pLibrary the p2pLibrary to set
     */
    public void setP2pLibrary(String p2pLibrary) {
        this.p2pLibrary = p2pLibrary;
    }

    /**
     * @return the indexer
     */
    public String getIndexer() {
        return indexer;
    }

    /**
     * @param indexer the indexer to set
     */
    public void setIndexer(String indexer) {
        this.indexer = indexer;
    }

    /**
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @return the nodeNameDefined
     */
    public boolean isNodeNameDefined() {
        return nodeNameDefined;
    }

    /**
     * @param nodeNameDefined the nodeNameDefined to set
     */
    public void setNodeNameDefined(boolean nodeNameDefined) {
        this.nodeNameDefined = nodeNameDefined;
    }

    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    public void setNetworkInterfaceName(String interfaceName) {
        this.networkInterfaceName = interfaceName;
    }


    /**
     * @return the indexerEffort
     */
    public int getIndexerEffort() {
        return indexerEffort;
    }

    /**
     * @param indexerEffort the indexerEffort to set
     */
    public void setIndexerEffort(int indexerEffort) {
        this.indexerEffort = indexerEffort;
    }

    /**
     * @return the encryptUsersFile
     */
    public boolean isEncryptUsersFile() {
        return encryptUsersFile;
    }

    /**
     * @param encryptUsersFile the encryptUsersFile to set
     */
    public void setEncryptUsersFile(boolean encryptUsersFile) {
        this.encryptUsersFile = encryptUsersFile;
    }

    public boolean isIndexZIPFiles() {
        return indexZIPFiles;
    }

    public void setIndexZIPFiles(boolean indexZIPFiles) {
        this.indexZIPFiles = indexZIPFiles;
    }

    @Deprecated
    public String getRGUIExternalIP() {
        return RGUIExternalIP;
    }

    @Deprecated
    public void setRGUIExternalIP(String RGUIExternalIP) {
        this.RGUIExternalIP = RGUIExternalIP;
    }

    public boolean isDirectoryWatcherEnabled() {
        return monitorWatcher;
    }

    public void setDirectoryWatcherEnabled(boolean monitorWatcher) {
        this.monitorWatcher = monitorWatcher;
    }

    public boolean isIndexAnonymous() {
        return indexAnonymous;
    }

    public void setIndexAnonymous(boolean indexAnonymous) {
        this.indexAnonymous = indexAnonymous;
    }

    public boolean isGzipStorage() {
        return gzipStorage;
    }

    public void setGzipStorage(boolean gzipStorage) {
        this.gzipStorage = gzipStorage;
    }

    public String getAccessListFileName() {
        return this.aclxmlFileName;
    }

    /**
     * Web (including web server, webservices, etc)
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
    public class Web implements ServerSettings.WebServer {
        private boolean webServer = true;
        private int serverPort = 8080;
        private String accessControlAllowOrigins = "*";

        @Deprecated
        @JsonIgnore
        private boolean webServices = false;
        @Deprecated
        @JsonIgnore
        private int servicePort = 6060;

        public Web() {}

        /**
         * @return the webServer
         */
        @Override
        public boolean isAutostart() {
            return webServer;
        }

        /**
         * @param webServer the webServer to set
         */
        @Override
        public void setAutostart(boolean webServer) {
            this.webServer = webServer;
        }

        /**
         * @return the serverPort
         */
        @Override
        public int getPort() {
            return serverPort;
        }

        /**
         * @param serverPort the serverPort to set
         */
        @Override
        public void setPort(int serverPort) {
            this.serverPort = serverPort;
        }

        @Override
        public void setHostname(String hostname) {
            // no-op, not supported by legacy settings
        }

        @Override
        public String getHostname() {
            return null;
        }

        @Override
        public String getAllowedOrigins() {
            return this.accessControlAllowOrigins;
        }

        @Override
        public void setAllowedOrigins(String origins) {
            this.accessControlAllowOrigins = origins;
        }

        @Deprecated
        public void setWebServices(boolean webServices) {
            this.webServices = webServices;
        }

        @Deprecated
        public void setServicePort(int servicePort) {
            this.servicePort = servicePort;
        }

        @Deprecated
        public boolean isWebServices() {
            return webServices;
        }

        @Deprecated
        public int getServicePort() {
            return servicePort;
        }
    }

    @JsonProperty("web-server")
    private Web web = new Web();

    private boolean wanmode;

    public LegacyServerSettings() {
        rGUIPort = 9014;
        storagePort = 104;
        AETitle = "DICOOGLE";
        CAETitle = new String[0];
        permitAllAETitles = true;
        Path = "";
        dicoogleDir = "";
        fullContentIndex = false;
        saveThumbnails = false;
        thumbnailsMatrix = 64;

        encryptUsersFile = false;

        /**
         * Set default values of QueryRetrieve Server
         */

        this.deviceDescription = "Dicoogle - Server SCP";
        this.localAETName = "Dicoogle";
        this.permitedLocalInterfaces = "any";
        this.permitedRemoteHostnames = "any";
        this.wlsPort = 1045; // default: 104
        this.idleTimeout = 60;
        this.acceptTimeout = 60;
        this.rspDelay = 0;
        this.DIMSERspTimeout = 60;
        this.connectionTimeout = 60;

        this.transfCAP = UID.ImplicitVRLittleEndian + "|" + UID.ExplicitVRBigEndian + "|" + UID.ExplicitVRLittleEndian;

        this.sopClass =
                UID.StudyRootQueryRetrieveInformationModelFIND + "|" + UID.PatientRootQueryRetrieveInformationModelFIND;

        fillModalityFindDefault();
        this.maxClientAssocs = 20;
        this.maxPDULengthReceive = 16364;
        this.maxPDULengthSend = 16364;

        autoStartPlugin = new ConcurrentHashMap<>();
    }

    // Nasty bug fix; no thumbnails references here = null pointers
    public void setDefaultSettings() {
        rGUIPort = 9014;
        storagePort = 6666;
        AETitle = "DICOOGLE-STORAGE";
        Path = System.getProperty("java.io.tmpdir");
        CAETitle = new String[0];
        permitAllAETitles = true;
        dicoogleDir = System.getProperty("java.io.tmpdir");
        fullContentIndex = false;
        saveThumbnails = false;
        thumbnailsMatrix = 64;
        autoStartPlugin.clear();

        setEncryptUsersFile(false);
    }

    public void setAETitle(String AE) {
        AETitle = AE;
    }

    public String getAETitle() {
        return AETitle;
    }

    public void setID(String I) {
        ID = I;
    }

    public String getID() {
        return ID;
    }

    public void setAllowedAETitles(Collection<String> CAET) {
        CAETitle = CAET.toArray(CAETitle);
    }

    public List<String> getAllowedAETitles() {
        return Arrays.asList(this.CAETitle);
    }

    public String[] getCAET() {
        return CAETitle;
    }

    public void setPermitAllAETitles(boolean value) {
        permitAllAETitles = value;
    }

    public boolean getPermitAllAETitles() {
        return permitAllAETitles;
    }

    public void setStoragePort(int p) {
        storagePort = p;
    }

    public void setPath(String p) {
        Path = p;
    }

    public String getWatchDirectory() {
        return Path;
    }

    public int getStoragePort() {
        return storagePort;
    }

    @Deprecated
    public void setRemoteGUIPort(int port) {
        rGUIPort = port;
    }

    @Deprecated
    public int getRemoteGUIPort() {
        return rGUIPort;
    }

    public String getDicoogleDir() {
        return dicoogleDir;
    }

    public String getMainDirectory() {
        return dicoogleDir;
    }

    public void setMainDirectory(String directory) {
        this.dicoogleDir = directory;
    }

    public void setWatchDirectory(String watchDirectory) {
        this.Path = watchDirectory;
    }

    public boolean getFullContentIndex() {
        return fullContentIndex;
    }

    public void setFullContentIndex(boolean fullContentIndex) {
        this.fullContentIndex = fullContentIndex;
    }

    public boolean getSaveThumbnails() {
        return saveThumbnails;
    }

    public void setSaveThumbnails(boolean saveThumbnails) {
        this.saveThumbnails = saveThumbnails;
    }

    public int getThumbnailSize() {
        return thumbnailsMatrix;
    }

    public void setThumbnailSize(int thumbnailsMatrix) {
        this.thumbnailsMatrix = thumbnailsMatrix;
    }

    /*
     * Query Retrieve Server
     */

    public void setWlsPort(int port) {
        this.wlsPort = port;
    }

    public int getWlsPort() {
        return this.wlsPort;
    }

    public void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout;
    }

    public int getIdleTimeout() {
        return this.idleTimeout;
    }

    public void setRspDelay(int delay) {
        this.rspDelay = delay;
    }

    public int getRspDelay() {
        return this.rspDelay;
    }

    public void setAcceptTimeout(int timeout) {
        this.acceptTimeout = timeout;
    }

    public int getAcceptTimeout() {
        return this.acceptTimeout;
    }

    public void setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setSOPClass(String SOPClass) {
        this.sopClass = SOPClass;
    }

    public List<String> getQRSOPClass() {
        return Arrays.asList(UID.StudyRootQueryRetrieveInformationModelFIND,
                UID.PatientRootQueryRetrieveInformationModelFIND);
    }

    public void setSOPClasses(Collection<SOPClass> classes) {
        Objects.requireNonNull(classes);
        LoggerFactory.getLogger(LegacyServerSettings.class).warn("Configuring DICOM SOP classes is unsupported "
                + " in legacy configuration file \"config.xml\". Please upgrade your server to use the latest format.");
    }

    public void setAdditionalSOPClasses(Collection<AdditionalSOPClass> classes) {
        // No-op
    }

    public void setAdditionalTransferSyntaxes(Collection<AdditionalTransferSyntax> syntaxes) {
        // No-op
    }

    public List<SOPClass> getSOPClasses() {
        return SOPList.getInstance().asSOPClassList();
    }

    public List<AdditionalSOPClass> getAdditionalSOPClass() {
        // No-op
        return Collections.emptyList();
    }

    public List<AdditionalTransferSyntax> getAdditionalTransferSyntax() {
        // No-op
        return Collections.emptyList();
    }

    public void setDIMSERspTimeout(int timeout) {
        this.DIMSERspTimeout = timeout;
    }

    public int getDIMSERspTimeout() {
        return this.DIMSERspTimeout;
    }

    public void setDeviceDescription(String desc) {
        this.deviceDescription = desc;
    }

    public String getDeviceDescription() {
        return this.deviceDescription;
    }

    public void setTransfCap(String transfCap) {
        this.transfCAP = transfCap;
    }

    public List<String> getTransferCapabilities() {
        return Arrays.asList(this.transfCAP.split("\\|"));
    }

    public void setMaxClientAssoc(int maxClients) {
        this.maxClientAssocs = maxClients;
    }

    public int getMaxClientAssoc() {
        return this.maxClientAssocs;
    }

    public void setMaxPDULengthReceive(int len) {
        this.maxPDULengthReceive = len;
    }

    public int getMaxPDULengthReceive() {
        return this.maxPDULengthReceive;
    }

    public void setMaxPDULengthSend(int len) {
        this.maxPDULengthSend = len;
    }

    public int getMaxPDULenghtSend() // FIXME typo
    {
        return this.maxPDULengthSend;
    }

    public void setLocalAETName(String name) {
        this.localAETName = name;
    }

    public String getLocalAETName() {
        return this.localAETName;
    }

    public void setPermitedLocalInterfaces(String localInterfaces) {
        this.permitedLocalInterfaces = localInterfaces;
    }

    public void setAllowedLocalInterfaces(Collection<String> localInterfaces) {
        this.permitedLocalInterfaces = StringUtils.join(localInterfaces, '|');
    }

    public List<String> getAllowedLocalInterfaces() {
        return Arrays.asList(this.permitedLocalInterfaces.split("\\|"));
    }

    public void setPermitedRemoteHostnames(String remoteHostnames) {
        this.permitedRemoteHostnames = remoteHostnames;
    }

    public void setAllowedHostnames(Collection<String> hostnames) {
        this.permitedRemoteHostnames = StringUtils.join(hostnames, '|');
    }

    public List<String> getAllowedHostnames() {
        return Arrays.asList(this.permitedRemoteHostnames.split("\\|"));
    }

    /**
     * @return the P2P
     */
    /* public boolean isP2P() {
        return P2P;
    }*/

    /** Whether the storage service auto-starts. */
    public boolean isStorageAutostart() {
        return storage;
    }

    /** Whether the query/retrieve service auto-starts. */
    public boolean isQueryRetrieveAutostart() {
        return queryRetrieve;
    }

    public void addMoveDestination(MoveDestination m) {
        this.dest.add(m);
    }

    public boolean removeMoveDestination(String AETitle) {
        boolean removed = false;
        Iterator<MoveDestination> it = dest.iterator();
        while (it.hasNext()) {
            MoveDestination mv = it.next();
            if (mv.getAETitle().equals(AETitle)) {
                it.remove();
                removed = true;
            }

        }
        return removed;
    }

    public boolean contains(MoveDestination m) {
        return this.dest.contains(m);
    }

    public ArrayList<MoveDestination> getMoveDestinations() {
        return this.dest;
    }

    public Set<String> getPriorityAETitles() {
        return priorityAETitles;
    }

    public void setPriorityAETitles(Collection<String> aeTitles) {
        this.priorityAETitles = new HashSet<>(aeTitles);
    }

    public void addPriorityAETitle(String aet) {
        this.priorityAETitles.add(aet);
    }

    public void removePriorityAETitle(String aet) {
        this.priorityAETitles.remove(aet);
    }

    public void setMoveDestinations(List<MoveDestination> moves) {
        if (moves != null) {
            this.dest = new ArrayList<>(moves);
        }
    }

    private void fillModalityFindDefault() {
        addModalityFind("1.2.840.10008.5.1.4.1.2.2.1", "Study Root Query/Retrieve Information Model");

        addModalityFind("1.2.840.10008.5.1.4.1.2.1.1", "Patient Root Query/Retrieve Information Model");

    }

    /**
     * Set default values
     */
    public void setDefaultsValues() {
        this.fillModalityFindDefault();
    }

    /**
     * Add a modality
     * @param sop Number like 1.2.3.5.6.7.32.1
     * @param description Description like "Modality Worklist Model"
     */
    public void addModalityFind(String sop, String description) {
        this.modalityFind.put(sop, description);
    }

    /**
     *
     * @return HashMap with Modalitys FIND
     */
    public HashMap<String, String> getModalityFind() {
        return this.modalityFind;
    }


    /**
     * Sets the plugin start on server init value.
     * <b>NOTE</b>: this method is strictly for plugins, not embedded services.
     *
     * @param name the name of the plugin.
     * @param value true to auto start the plugin on server init.
     */
    public void setAutoStartPlugin(String name, boolean value) {
        // remove the previous setting, if there is one
        autoStartPlugin.remove(name);

        // insert the new setting
        autoStartPlugin.put(name, value);
    }

    /**
     * Returns if the plugin is to be auto started on server init or not.
     * The default value (without previous configuration) is to start the plugin.
     * <b>NOTE</b>: this method is strictly for plugins, not embedded services.
     *
     * @param name the name of the plugin.
     * @return true if the plugin is to be auto started on server init or false if it is not.
     */
    public boolean getAutoStartPlugin(String name) {
        Boolean result = autoStartPlugin.get(name);

        // if there is not such setting return the default value
        if (result == null)
            return true; // by default start the plugin
        return result.booleanValue();
    }

    /**
     * Returns the current settings for plugin auto start on server init.
     *
     * @return the current settings for plugin auto start on server init.
     */
    public ConcurrentHashMap<String, Boolean> getAutoStartPluginsSettings() {
        return autoStartPlugin;
    }

    /**
     * Returns the Remote GUI list of settings (name, value/type pairs).
     *
     * @return and HashMap containing the Remote GUI list of settings (name, value/type pairs).
     */
    @Deprecated
    public HashMap<String, Object> getRGUISettings() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put(RGUI_SETTING_EXTERNAL_IP, getRGUIExternalIP());

        return result;
    }

    /**
     * Validates the new settings for the Remote GUI service.
     *
     * @param settings a HashMap containing the new setting values.
     * @return true if all the values are valid and can be applied, false otherwise.
     */
    @Deprecated
    public boolean tryRGUISettings(HashMap<String, Object> settings) {
        // TODO
        return true;
    }

    /**
     * Tries to apply the new settings for the Remote GUI service.
     *
     * @param settings a HashMap containing the new setting values.
     * @return true if all the values are valid and were applied successfully, false otherwise.
     */
    @Deprecated
    public boolean setRGUISettings(HashMap<String, Object> settings) {
        if (!tryRGUISettings(settings))
            return false;

        setRGUIExternalIP((String) settings.get(RGUI_SETTING_EXTERNAL_IP));

        return true;
    }

    /**
     * Returns the Remote GUI list of settings help (name,help pairs).
     *
     * @return and HashMap containing the Remote GUI list of settings help (name, help).
     */
    public HashMap<String, String> getRGUISettingsHelp() {
        HashMap<String, String> result = new HashMap<String, String>();

        result.put(RGUI_SETTING_EXTERNAL_IP, RGUI_SETTING_EXTERNAL_IP_HELP);

        return result;
    }

    /**
     * Returns the Query Retrieve list of settings (name, value/type pairs).
     *
     * @return and HashMap containing the Query Retrieve list of settings (name, value/type pairs).
     */
    public HashMap<String, Object> getQueryRetrieveSettings_xml() {
        HashMap<String, Object> result = new HashMap<>();

        result.put(QUERYRETRIEVE_MAX_ASSOCIATIONS, getMaxClientAssoc());
        result.put(QUERYRETRIEVE_MAX_PDU_RECEIVE, getMaxPDULengthReceive());
        result.put(QUERYRETRIEVE_MAX_PDU_SEND, getMaxPDULenghtSend());
        result.put(QUERYRETRIEVE_IDLE_TIMEOUT, getIdleTimeout());
        result.put(QUERYRETRIEVE_ACCEPT_TIMEOUT, getAcceptTimeout());
        result.put(QUERYRETRIEVE_RESPONSE_TIMEOUT, getRspDelay());
        result.put(QUERYRETRIEVE_CONNECTION_TIMEOUT, getConnectionTimeout());

        return result;
    }

    /**
     * Validates the new settings for the Query Retrieve service.
     *
     * @param settings a HashMap containing the new setting values.
     * @return true if all the values are valid and can be applied, false otherwise.
     */
    public boolean tryQueryRetrieveSettings(HashMap<String, Object> settings) {
        // TODO
        return true;
    }

    /**
     * Tries to apply the new settings for the Query Retrieve service.
     *
     * @param settings a HashMap containing the new setting values.
     * @return true if all the values are valid and were applied successfully, false otherwise.
     */
    public boolean setQueryRetrieveSettings(HashMap<String, Object> settings) {
        if (!tryQueryRetrieveSettings(settings))
            return false;

        setMaxClientAssoc(((Integer) settings.get(QUERYRETRIEVE_MAX_ASSOCIATIONS)).intValue());
        setMaxPDULengthReceive(((Integer) settings.get(QUERYRETRIEVE_MAX_PDU_RECEIVE)).intValue());
        setMaxPDULengthSend(((Integer) settings.get(QUERYRETRIEVE_MAX_PDU_SEND)).intValue());
        setIdleTimeout(((Integer) settings.get(QUERYRETRIEVE_IDLE_TIMEOUT)).intValue());
        setAcceptTimeout(((Integer) settings.get(QUERYRETRIEVE_ACCEPT_TIMEOUT)).intValue());
        setRspDelay(((Integer) settings.get(QUERYRETRIEVE_RESPONSE_TIMEOUT)).intValue());
        setConnectionTimeout(((Integer) settings.get(QUERYRETRIEVE_CONNECTION_TIMEOUT)).intValue());

        return true;
    }

    /**
     * Returns the Query Retrieve list of settings help (name,help pairs).
     *
     * @return and HashMap containing the Query Retrieve list of settings help (name, help).
     */
    public HashMap<String, String> getQueryRetrieveSettingsHelp() {
        return null; // no help available
    }

    /**
     * Returns the Storage list of settings (name, value/type pairs).
     *
     * @return and HashMap containing the Storage list of settings (name, value/type pairs).
     */
    public HashMap<String, Object> getStorageSettings_xml() {
        HashMap<String, Object> result = new HashMap<>();

        // result.put(STORAGE_SETTING_PATH, new ServerDirectoryPath(getWatchDirectory()));
        // TODO move some of these new classes onto the SDK, so that plugins can also process option types/fields
        int destCount = dest.size();
        DataTable storageServers = new DataTable(3, destCount);
        storageServers.setColumnName(0, "AETitle");
        storageServers.setColumnName(1, "IP");
        storageServers.setColumnName(2, "Port");
        // if there are no rows, then add an empty one (for reference)
        if (destCount < 1) {
            storageServers.addRow();
            storageServers.setCellData(0, 0, "");
            storageServers.setCellData(0, 1, "");
            storageServers.setCellData(0, 2, "");
        } else
            for (int i = 0; i < destCount; i++) {
                MoveDestination aDest = dest.get(i);
                storageServers.setCellData(i, 0, aDest.getAETitle());
                storageServers.setCellData(i, 1, aDest.getIpAddrs());
                storageServers.setCellData(i, 2, "" + aDest.getPort());
            }
        result.put(STORAGE_SETTING_SERVERS_DESTINATIONS, storageServers);

        return result;
    }

    /**
     * Validates the new settings for the Storage service.
     *
     * @param settings a HashMap containing the new setting values.
     * @return true if all the values are valid and can be applied, false otherwise.
     */
    public boolean tryStorageSettings(HashMap<String, Object> settings) {
        // TODO
        return true;
    }

    /**
     * Tries to apply the new settings for the Storage service.
     *
     * @param settings a HashMap containing the new setting values.
     * @return true if all the values are valid and were applied successfully, false otherwise.
     */
    public boolean setStorageSettings(HashMap<String, Object> settings) {
        if (!tryStorageSettings(settings))
            return false;

        // setPath(((ServerDirectoryPath) settings.get(STORAGE_SETTING_PATH)).getWatchDirectory());
        // TODO set the query retrieve options

        return true;
    }

    /**
     * Returns the Storage list of settings help (name,help pairs).
     *
     * @return and HashMap containing the Storage list of settings help (name, help).
     */
    public HashMap<String, String> getStorageSettingsHelp() {
        return null; // no help available
    }

    public void setStorageAutostart(boolean storage) {
        this.storage = storage;
    }

    public void setQueryRetrieveAutostart(boolean queryRetrieve) {
        this.queryRetrieve = queryRetrieve;
    }

    public ArrayList<String> getNetworkInterfacesNames() {
        ArrayList<String> interfaces = new ArrayList<String>();
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }


        for (NetworkInterface netint : Collections.list(nets)) {
            try {
                if (!netint.isLoopback()) {
                    Enumeration<InetAddress> addresses = netint.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        if (Inet4Address.class.isInstance(addresses.nextElement())) {
                            interfaces.add(netint.getDisplayName());
                        }
                    }
                }
            } catch (SocketException ex) {
                LoggerFactory.getLogger(LegacyServerSettings.class).error(ex.getMessage(), ex);
            }
        }
        return interfaces;
    }

    public String getNetworkInterfaceAddress() {
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

        for (NetworkInterface netint : Collections.list(nets)) {
            if (netint.getDisplayName().compareTo(this.networkInterfaceName) == 0) {
                Enumeration<InetAddress> addresses = netint.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (Inet4Address.class.isInstance(address)) {
                        return address.getHostAddress();
                    }
                }
                return null;
            }
        }
        return null;
    }

    public HashSet<String> getExtensionsAllowed() {
        return extensionsAllowed;
    }

    /**
     * @return the maxMessages
     */
    public int getMaxMessages() {
        return maxMessages;
    }

    /**
     * @param maxMessages the maxMessages to set
     */
    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public boolean isWANModeEnabled() {
        return wanmode;
    }

    public void setWanmode(boolean wanmode) {
        this.wanmode = wanmode;
    }


    // --------- additional fields methods for compatibility with main API (will not persist though) -----------

    @JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.ANY)
    protected class StubArchive implements Archive {
        @Override
        public void setSaveThumbnails(boolean saveThumbnails) {
            LegacyServerSettings.this.setSaveThumbnails(saveThumbnails);
        }

        @Override
        public void setThumbnailSize(int thumbnailSize) {
            LegacyServerSettings.this.setThumbnailSize(thumbnailSize);
        }

        @Override
        public void setMainDirectory(String directory) {
            LegacyServerSettings.this.setMainDirectory(directory);
        }

        @Override
        public void setIndexerEffort(int effort) {
            LegacyServerSettings.this.setIndexerEffort(effort);
        }

        @Override
        public void setWatchDirectory(String dir) {
            LegacyServerSettings.this.setWatchDirectory(dir);
        }

        @Override
        public void setUseIIORegistry(boolean useIIORegistry) {}

        @Override
        public void setDirectoryWatcherEnabled(boolean watch) {
            LegacyServerSettings.this.setDirectoryWatcherEnabled(watch);
        }

        @Override
        public void setDIMProviders(List<String> providers) {
            LegacyServerSettings.this.setDIMProviders(providers);
        }

        @Override
        public void setDefaultStorage(List<String> storages) {
            LegacyServerSettings.this.setDefaultStorage(storages);
        }

        @Override
        public void setNodeName(String nodeName) {
            LegacyServerSettings.this.setNodeName(nodeName);
        }

        @Override
        public void setEncryptUsersFile(boolean encrypt) {
            LegacyServerSettings.this.setEncryptUsersFile(encrypt);
        }

        @JsonGetter("save-thumbnails")
        @Override
        public boolean getSaveThumbnails() {
            return LegacyServerSettings.this.getSaveThumbnails();
        }

        @JsonGetter("thumbnail-size")
        @Override
        public int getThumbnailSize() {
            return LegacyServerSettings.this.getThumbnailSize();
        }

        @JsonGetter("indexer-effort")
        @Override
        public int getIndexerEffort() {
            return LegacyServerSettings.this.getIndexerEffort();
        }

        @JsonGetter("main-directory")
        @Override
        public String getMainDirectory() {
            return LegacyServerSettings.this.getMainDirectory();
        }

        @JsonGetter("enable-watch-directory")
        @Override
        public boolean isDirectoryWatcherEnabled() {
            return LegacyServerSettings.this.isDirectoryWatcherEnabled();
        }

        @JsonGetter("watch-directory")
        @Override
        public String getWatchDirectory() {
            return LegacyServerSettings.this.getWatchDirectory();
        }

        @JsonGetter("use-iio-registry")
        @Override
        public boolean isUseIIORegistry() {
            return false;
        }

        @JsonGetter("dim-provider")
        @Override
        public List<String> getDIMProviders() {
            return LegacyServerSettings.this.getDIMProviders();
        }

        @JsonGetter("default-storage")
        @Override
        public List<String> getDefaultStorage() {
            return LegacyServerSettings.this.getDefaultStorage();
        }

        @JsonGetter("node-name")
        @Override
        public String getNodeName() {
            return LegacyServerSettings.this.getNodeName();
        }

        @JsonGetter("encrypt-users-file")
        @Override
        public boolean isEncryptUsersFile() {
            return LegacyServerSettings.this.isEncryptUsersFile();
        }

        @JsonGetter("call-shutdown")
        @Override
        public boolean isCallShutdown() {
            return false;
        }

        @Override
        public void setCallShutdown(boolean shutdown) {
            // no-op, not supported
        }
    }

    @JsonGetter("archive")
    @Override
    public Archive getArchiveSettings() {
        return new StubArchive();
    }

    @JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
    protected class StubDicomServices implements DicomServices {

        @Override
        public void setAETitle(String aetitle) {
            LegacyServerSettings.this.setAETitle(aetitle);
        }

        @Override
        public void setDeviceDescription(String description) {
            LegacyServerSettings.this.setDeviceDescription(description);
        }

        @Override
        public void setAllowedAETitles(Collection<String> AETitles) {
            LegacyServerSettings.this.setAllowedAETitles(AETitles);
        }

        @Override
        public void setPriorityAETitles(Collection<String> AETitles) {
            LegacyServerSettings.this.setPriorityAETitles(AETitles);
        }

        @Override
        public void setMoveDestinations(List<MoveDestination> destinations) {
            LegacyServerSettings.this.setMoveDestinations(destinations);
        }

        @Override
        public void addMoveDestination(MoveDestination destination) {
            LegacyServerSettings.this.addMoveDestination(destination);
        }

        @Override
        public boolean removeMoveDestination(String aetitle) {
            return LegacyServerSettings.this.removeMoveDestination(aetitle);
        }

        @Override
        public void setAllowedLocalInterfaces(Collection<String> localInterfaces) {
            LegacyServerSettings.this.setAllowedLocalInterfaces(localInterfaces);
        }

        @Override
        public void setAllowedHostnames(Collection<String> hostnames) {
            LegacyServerSettings.this.setAllowedHostnames(hostnames);
        }

        @Override
        public void setSOPClasses(Collection<SOPClass> sopClasses) {
            LegacyServerSettings.this.setSOPClasses(sopClasses);
        }

        @Override
        public void setAdditionalSOPClasses(Collection<AdditionalSOPClass> additionalSOPClasses) {
            LegacyServerSettings.this.setAdditionalSOPClasses(additionalSOPClasses);
        }

        @Override
        public void setAdditionalTransferSyntaxes(Collection<AdditionalTransferSyntax> additionalTransferSyntaxes) {
            LegacyServerSettings.this.setAdditionalTransferSyntaxes(additionalTransferSyntaxes);
        }

        @Override
        public ServiceBase getStorageSettings() {
            return LegacyServerSettings.this.getStorageSettings();
        }

        @Override
        public QueryRetrieve getQueryRetrieveSettings() {
            return LegacyServerSettings.this.getQueryRetrieveSettings();
        }

        @Override
        public String getAETitle() {
            return LegacyServerSettings.this.getAETitle();
        }

        @Override
        public String getDeviceDescription() {
            return LegacyServerSettings.this.getDeviceDescription();
        }

        @Override
        public Collection<String> getAllowedAETitles() {
            return LegacyServerSettings.this.getAllowedAETitles();
        }

        @Override
        public Collection<String> getPriorityAETitles() {
            return LegacyServerSettings.this.getPriorityAETitles();
        }

        @Override
        public Collection<String> getAllowedLocalInterfaces() {
            return LegacyServerSettings.this.getAllowedLocalInterfaces();
        }

        @Override
        public Collection<String> getAllowedHostnames() {
            return LegacyServerSettings.this.getAllowedHostnames();
        }

        @Override
        public Collection<SOPClass> getSOPClasses() {
            return LegacyServerSettings.this.getSOPClasses();
        }

        @Override
        public Collection<AdditionalSOPClass> getAdditionalSOPClasses() {
            return LegacyServerSettings.this.getAdditionalSOPClass();
        }

        @Override
        public Collection<AdditionalTransferSyntax> getAdditionalTransferSyntaxes() {
            return LegacyServerSettings.this.getAdditionalTransferSyntax();
        }

        @Override
        public List<MoveDestination> getMoveDestinations() {
            return LegacyServerSettings.this.getMoveDestinations();
        }
    }

    @JsonGetter("dicom-services")
    @Override
    public DicomServices getDicomServicesSettings() {
        return new StubDicomServices();
    }

    private List<String> ds = Collections.EMPTY_LIST;
    private List<String> dp = Collections.EMPTY_LIST;

    public List<String> getDefaultStorage() {
        return ds;
    }

    public List<String> getDIMProviders() {
        return dp;
    }

    public void setDefaultStorage(List<String> storages) {
        ds = storages;
    }

    public void setDIMProviders(List<String> providers) {
        dp = providers;
    }

    public ServiceBase getStorageSettings() {
        return new ServiceBase() {
            @Override
            public void setAutostart(boolean b) {
                setStorageAutostart(b);
            }

            @Override
            public void setPort(int i) {
                setStoragePort(i);
            }

            @Override
            public void setHostname(String s) {
                // no-op, not supported by legacy settings
            }

            @Override
            public boolean isAutostart() {
                return isStorageAutostart();
            }

            @Override
            public int getPort() {
                return getStoragePort();
            }

            @Override
            public String getHostname() {
                return null;
            }
        };
    }

    public ServerSettings.DicomServices.QueryRetrieve getQueryRetrieveSettings() {
        return new DicomServices.QueryRetrieve() {
            @Override
            public void setSOPClass(Collection<String> collection) {
                LegacyServerSettings.this.setSOPClass(StringUtils.join(collection, '|'));
            }

            @Override
            public void setTransferCapabilities(Collection<String> collection) {
                LegacyServerSettings.this.setTransfCap(StringUtils.join(collection, '|'));
            }

            @Override
            public void setAutostart(boolean b) {
                LegacyServerSettings.this.setQueryRetrieveAutostart(b);
            }

            @Override
            public void setPort(int i) {
                LegacyServerSettings.this.setWlsPort(i);
            }

            @Override
            public void setHostname(String hostname) {
                // no-op, not supported by legacy settings
            }

            @Override
            public void setRspDelay(int i) {
                LegacyServerSettings.this.setRspDelay(i);
            }

            @Override
            public void setIdleTimeout(int i) {
                LegacyServerSettings.this.setIdleTimeout(i);
            }

            @Override
            public void setAcceptTimeout(int i) {
                LegacyServerSettings.this.setAcceptTimeout(i);
            }

            @Override
            public void setConnectionTimeout(int i) {
                LegacyServerSettings.this.setConnectionTimeout(i);
            }

            @Override
            public void setDIMSERspTimeout(int i) {
                LegacyServerSettings.this.setDIMSERspTimeout(i);
            }

            @Override
            public void setMaxClientAssoc(int i) {
                LegacyServerSettings.this.setMaxClientAssoc(i);
            }

            @Override
            public void setMaxPDULengthReceive(int i) {
                LegacyServerSettings.this.setMaxPDULengthReceive(i);
            }

            @Override
            public void setMaxPDULengthSend(int i) {
                LegacyServerSettings.this.setMaxPDULengthReceive(i);
            }

            @Override
            public Collection<String> getSOPClass() {
                return LegacyServerSettings.this.getQRSOPClass();
            }

            @Override
            public Collection<String> getTransferCapabilities() {
                return LegacyServerSettings.this.getTransferCapabilities();
            }

            @Override
            public int getRspDelay() {
                return LegacyServerSettings.this.getRspDelay();
            }

            @Override
            public int getIdleTimeout() {
                return LegacyServerSettings.this.getIdleTimeout();
            }

            @Override
            public int getAcceptTimeout() {
                return LegacyServerSettings.this.getAcceptTimeout();
            }

            @Override
            public int getConnectionTimeout() {
                return LegacyServerSettings.this.getConnectionTimeout();
            }

            @Override
            public int getDIMSERspTimeout() {
                return LegacyServerSettings.this.getDIMSERspTimeout();
            }

            @Override
            public int getMaxClientAssoc() {
                return LegacyServerSettings.this.getMaxClientAssoc();
            }

            @Override
            public int getMaxPDULengthReceive() {
                return LegacyServerSettings.this.getMaxPDULengthReceive();
            }

            @Override
            public int getMaxPDULengthSend() {
                return LegacyServerSettings.this.getMaxPDULenghtSend();
            }

            @Override
            public boolean isAutostart() {
                return isQueryRetrieveAutostart();
            }

            @Override
            public int getPort() {
                return getWlsPort();
            }

            @Override
            public String getHostname() {
                return null;
            }
        };
    }
}
