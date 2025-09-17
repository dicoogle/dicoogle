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

package pt.ua.dicoogle.sdk.settings.server;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import pt.ua.dicoogle.sdk.datastructs.AdditionalSOPClass;
import pt.ua.dicoogle.sdk.datastructs.AdditionalTransferSyntax;
import pt.ua.dicoogle.sdk.datastructs.MoveDestination;
import pt.ua.dicoogle.sdk.datastructs.SOPClass;

import java.util.Collection;
import java.util.List;

/** A read-only interface for accessing server settings.
 *
 * When requesting the server's settings from a plugin, an instance with this API is returned.
 * Plugins should not implement this interface.
 *
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public interface ServerSettingsReader {

    /** Base interface for reading service settings */
    interface ServiceBase {
        /** Whether the service starts automatically when booting Dicoogle */
        @JacksonXmlProperty(isAttribute = true, localName = "autostart")
        boolean isAutostart();

        /** The TCP port to bind the service to */
        @JacksonXmlProperty(isAttribute = true, localName = "port")
        int getPort();

        /** Optional: The hostname or IP address to bind the service to.
         *
         * If not specified, the service will bind to all available interfaces.
         */
        @JacksonXmlProperty(isAttribute = true, localName = "hostname")
        String getHostname();
    }

    interface WebServer extends ServiceBase {

        @JsonGetter("allowed-origins")
        String getAllowedOrigins();

        /** If true, authorization in the Dicoogle web API is disabled,
         * which allows access to all services without logging in to Dicoogle.
         *
         * Use with caution, as this effectively disables all safeguards.
         */
        @JsonProperty("allow-unauthorized")
        boolean isAllowUnauthorized();
    }

    @JsonGetter("web-server")
    WebServer getWebServerSettings();

    interface Archive {

        @JsonGetter("save-thumbnails")
        boolean getSaveThumbnails();

        @JsonGetter("thumbnail-size")
        int getThumbnailSize();

        @JsonGetter("indexer-effort")
        int getIndexerEffort();

        @JsonGetter("main-directory")
        String getMainDirectory();

        @JsonGetter("encrypt-users-file")
        boolean isEncryptUsersFile();

        @JsonGetter("enable-watch-directory")
        boolean isDirectoryWatcherEnabled();

        @JsonGetter("watch-directory")
        String getWatchDirectory();

        /** Whether to use the Image I/O registry
         * for DICOM image readers and writers.
         *
         * This is particularly useful for WSI support.
         */
        @JsonGetter("use-iio-registry")
        @JsonAlias({ "support-wsi"})
        boolean isUseIIORegistry();

        @JsonGetter("dim-provider")
        @JacksonXmlElementWrapper(localName = "dim-providers")
        List<String> getDIMProviders();

        @JsonGetter("default-storage")
        @JacksonXmlElementWrapper(localName = "default-storages")
        List<String> getDefaultStorage();

        @JsonGetter("node-name")
        String getNodeName();

        /**
         * Whether to call the shutdown routine for installed plugin sets.
         */
        @JsonGetter("call-shutdown")
        boolean isCallShutdown();
    }

    @JsonGetter("archive")
    Archive getArchiveSettings();

    interface DicomServices {

        @JsonGetter("aetitle")
        String getAETitle();

        @JsonGetter("device-description")
        String getDeviceDescription();

        @JacksonXmlElementWrapper(useWrapping = false, localName = "allowed-aetitles")
        @JsonGetter("allowed-aetitles")
        Collection<String> getAllowedAETitles();

        @JacksonXmlElementWrapper(useWrapping = false, localName = "priority-aetitles")
        @JsonGetter("priority-aetitles")
        Collection<String> getPriorityAETitles();

        @JacksonXmlElementWrapper(useWrapping = false, localName = "allowed-local-interfaces")
        @JsonGetter("allowed-local-interfaces")
        Collection<String> getAllowedLocalInterfaces();

        @JacksonXmlElementWrapper(useWrapping = false, localName = "allowed-hostnames")
        @JsonGetter("allowed-hostnames")
        Collection<String> getAllowedHostnames();

        @JsonGetter("sop-classes")
        Collection<SOPClass> getSOPClasses();

        @JsonGetter("additional-sop-classes")
        Collection<AdditionalSOPClass> getAdditionalSOPClasses();

        @JsonGetter("additional-transfer-syntaxes")
        Collection<AdditionalTransferSyntax> getAdditionalTransferSyntaxes();

        @JsonGetter("move-destinations")
        List<MoveDestination> getMoveDestinations();

        @JsonGetter("storage")
        ServiceBase getStorageSettings();

        interface QueryRetrieve extends ServiceBase {
            @JacksonXmlElementWrapper(useWrapping = false, localName = "sop-class")
            @JacksonXmlProperty(localName = "sop-class")
            Collection<String> getSOPClass();

            @JacksonXmlElementWrapper(useWrapping = false, localName = "transfer-capabilities")
            @JacksonXmlProperty(localName = "transfer-capabilities")
            Collection<String> getTransferCapabilities();

            @JsonGetter("rsp-delay")
            int getRspDelay();

            @JsonGetter("idle-timeout")
            int getIdleTimeout();

            @JsonGetter("accept-timeout")
            int getAcceptTimeout();

            @JsonGetter("connection-timeout")
            int getConnectionTimeout();

            @JsonGetter("dimse-rsp-timeout")
            int getDIMSERspTimeout();

            @JsonGetter("max-client-assocs")
            int getMaxClientAssoc();

            @JsonGetter("max-pdu-length-receive")
            int getMaxPDULengthReceive();

            @JsonGetter("max-pdu-length-send")
            int getMaxPDULengthSend();
        }

        @JsonGetter("query-retrieve")
        QueryRetrieve getQueryRetrieveSettings();
    }

    @JsonGetter("dicom-services")
    DicomServices getDicomServicesSettings();
}

