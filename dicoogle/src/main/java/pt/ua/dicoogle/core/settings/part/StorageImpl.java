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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;

import java.util.Objects;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class StorageImpl implements ServerSettings.DicomServices.Storage {
    @JacksonXmlProperty(isAttribute = true)
    private boolean autostart;
    @JacksonXmlProperty(isAttribute = true)
    private int port;
    @JacksonXmlProperty(isAttribute = true)
    private String hostname;

    @JsonProperty(value = "save-ae-titles", defaultValue = "true")
    private boolean saveAETitles = true;

    @JsonCreator
    public StorageImpl(@JacksonXmlProperty(localName = "autostart") boolean autostart,
            @JacksonXmlProperty(localName = "port") int port,
            @JacksonXmlProperty(localName = "hostname") String hostname) {
        this.autostart = autostart;
        this.port = port;
        this.hostname = hostname;
    }

    public static StorageImpl createDefault() {
        return new StorageImpl(true, 6666, null);
    }

    @Override
    public boolean isAutostart() {
        return autostart;
    }

    @Override
    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
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

    @Override
    public boolean isSaveAETitles() {
        return this.saveAETitles;
    }

    @Override
    public void setSaveAETitles(boolean saveAETitles) {
        this.saveAETitles = saveAETitles;
    }

    @Override
    public String toString() {
        return "StorageImpl{" + "autostart=" + autostart + ", port=" + port +
                ", hostname=" + hostname + ", saveAETitles=" + saveAETitles +'}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StorageImpl storage = (StorageImpl) o;
        return autostart == storage.autostart && port == storage.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autostart, port);
    }
}
