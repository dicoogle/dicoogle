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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ua.dicoogle.sdk.datastructs.AdditionalSOPClass;
import pt.ua.dicoogle.sdk.datastructs.AdditionalTransferSyntax;
import pt.ua.dicoogle.sdk.datastructs.MoveDestination;
import pt.ua.dicoogle.sdk.datastructs.SOPClass;

import java.util.Collection;
import java.util.List;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public interface ServerSettings extends ServerSettingsReader {

    /** Base interface for reading and writing service settings */
    interface ServiceBase extends ServerSettingsReader.ServiceBase {
        void setAutostart(boolean autostart);

        void setPort(int port);

        void setHostname(String hostname);
    }

    @Override
    @JsonGetter("web-server")
    WebServer getWebServerSettings();

    interface WebServer extends ServiceBase, ServerSettingsReader.WebServer {
        @JsonProperty("allowed-origins")
        void setAllowedOrigins(String allowedOrigins);
    }

    @Override
    @JsonGetter("archive")
    Archive getArchiveSettings();

    interface Archive extends ServerSettingsReader.Archive {
        void setSaveThumbnails(boolean saveThumbnails);

        void setThumbnailSize(int thumbnailSize);

        void setMainDirectory(String directory);

        void setIndexerEffort(int effort);

        void setWatchDirectory(String dir);

        void setDirectoryWatcherEnabled(boolean watch);

        void setUseIIORegistry(boolean useIIORegistry);

        void setEncryptUsersFile(boolean encrypt);

        void setDIMProviders(List<String> providers);

        void setDefaultStorage(List<String> storages);

        void setNodeName(String nodeName);

        void setCallShutdown(boolean shutdown);
    }

    @JsonGetter("dicom-services")
    DicomServices getDicomServicesSettings();

    interface DicomServices extends ServerSettingsReader.DicomServices {
        void setAETitle(String aetitle);

        void setDeviceDescription(String description);

        void setAllowedAETitles(Collection<String> AETitles);

        void setPriorityAETitles(Collection<String> AETitles);

        void setMoveDestinations(List<MoveDestination> destinations);

        void addMoveDestination(MoveDestination destination);

        boolean removeMoveDestination(String aetitle);

        void setAllowedLocalInterfaces(Collection<String> localInterfaces);

        void setAllowedHostnames(Collection<String> hostnames);

        void setSOPClasses(Collection<SOPClass> sopClasses);

        void setAdditionalSOPClasses(Collection<AdditionalSOPClass> additionalSOPClasses);

        void setAdditionalTransferSyntaxes(Collection<AdditionalTransferSyntax> additionalTransferSyntaxes);

        interface Storage extends ServiceBase, ServerSettingsReader.DicomServices.Storage {
            void setSaveAETitles(boolean saveAETitles);
        }

        @JsonGetter("storage")
        Storage getStorageSettings();

        interface QueryRetrieve extends ServiceBase, ServerSettingsReader.DicomServices.QueryRetrieve {
            void setSOPClass(Collection<String> sopClasses);

            void setTransferCapabilities(Collection<String> tcap);

            void setRspDelay(int timeout);

            void setIdleTimeout(int timeout);

            void setAcceptTimeout(int timeout);

            void setConnectionTimeout(int timeout);

            public void setDIMSERspTimeout(int timeout);

            public void setMaxClientAssoc(int max);

            public void setMaxPDULengthReceive(int pdu);

            public void setMaxPDULengthSend(int pdu);
        }

        @JsonGetter("query-retrieve")
        QueryRetrieve getQueryRetrieveSettings();
    }
}
