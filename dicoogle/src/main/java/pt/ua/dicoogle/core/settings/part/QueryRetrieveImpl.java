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
package pt.ua.dicoogle.core.settings.part;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.dcm4che2.data.UID;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class QueryRetrieveImpl implements ServerSettings.DicomServices.QueryRetrieve {
    @JacksonXmlProperty(isAttribute = true)
    private boolean autostart;
    @JacksonXmlProperty(isAttribute = true)
    private int port;
    @JacksonXmlProperty(isAttribute = true)
    private String hostname;

    public QueryRetrieveImpl() {}

    public static QueryRetrieveImpl createDefault() {
        QueryRetrieveImpl qr = new QueryRetrieveImpl();

        qr.autostart = true;
        qr.port = 1045;
        qr.rspDelay = 0;
        qr.dimseRspTimeout = 60;
        qr.idleTimeout = 60;
        qr.acceptTimeout = 60;
        qr.connectionTimeout = 60;
        qr.transferCapabilities =
                Arrays.asList(UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian);
        qr.sopClasses = Arrays.asList(UID.StudyRootQueryRetrieveInformationModelFIND,
                UID.PatientRootQueryRetrieveInformationModelFIND);
        qr.maxClientAssocs = 20;
        qr.maxPduLengthReceive = 16364;
        qr.maxPduLengthSend = 16364;

        return qr;
    }

    @JsonProperty("rsp-delay")
    private int rspDelay;

    @JsonProperty("dimse-rsp-timeout")
    private int dimseRspTimeout;

    @JsonProperty("idle-timeout")
    private int idleTimeout;

    @JsonProperty("accept-timeout")
    private int acceptTimeout;

    @JsonProperty("connection-timeout")
    private int connectionTimeout;

    @JacksonXmlElementWrapper(useWrapping = false, localName = "transfer-capabilities")
    @JacksonXmlProperty(localName = "transfer-capabilities")
    private Collection<String> transferCapabilities;

    @JacksonXmlElementWrapper(useWrapping = false, localName = "sop-class")
    @JacksonXmlProperty(localName = "sop-class")
    private Collection<String> sopClasses;

    @JsonProperty("max-client-assocs")
    private int maxClientAssocs;

    @JsonProperty("max-pdu-length-receive")
    private int maxPduLengthReceive;

    @JsonProperty("max-pdu-length-send")
    private int maxPduLengthSend;

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getRspDelay() {
        return rspDelay;
    }

    public void setRspDelay(int rspDelay) {
        this.rspDelay = rspDelay;
    }

    public int getDIMSERspTimeout() {
        return dimseRspTimeout;
    }

    public void setDIMSERspTimeout(int dimseRspTimeout) {
        this.dimseRspTimeout = dimseRspTimeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getAcceptTimeout() {
        return acceptTimeout;
    }

    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Collection<String> getTransferCapabilities() {
        return transferCapabilities;
    }

    public void setTransferCapabilities(Collection<String> transferCapabilities) {
        this.transferCapabilities = transferCapabilities;
    }

    public Collection<String> getSOPClass() {
        return sopClasses;
    }

    public void setSOPClass(Collection<String> sopClasses) {
        this.sopClasses = sopClasses;
    }

    public int getMaxClientAssoc() {
        return maxClientAssocs;
    }

    public void setMaxClientAssoc(int maxClientAssocs) {
        this.maxClientAssocs = maxClientAssocs;
    }

    public int getMaxPDULengthReceive() {
        return maxPduLengthReceive;
    }

    public void setMaxPDULengthReceive(int maxPduLengthReceive) {
        this.maxPduLengthReceive = maxPduLengthReceive;
    }

    public int getMaxPDULengthSend() {
        return maxPduLengthSend;
    }

    public void setMaxPDULengthSend(int maxPduLengthSend) {
        this.maxPduLengthSend = maxPduLengthSend;
    }

    @Override
    public String toString() {
        return "QueryRetrieveImpl{" + "autostart=" + autostart + ", port=" + port + ", rspDelay=" + rspDelay
                + ", dimseRspTimeout=" + dimseRspTimeout + ", idleTimeout=" + idleTimeout + ", acceptTimeout="
                + acceptTimeout + ", connectionTimeout=" + connectionTimeout + ", transferCapabilities="
                + transferCapabilities + ", sopClasses=" + sopClasses + ", maxClientAssocs=" + maxClientAssocs
                + ", maxPduLengthReceive=" + maxPduLengthReceive + ", maxPduLengthSend=" + maxPduLengthSend + '}';
    }
}
