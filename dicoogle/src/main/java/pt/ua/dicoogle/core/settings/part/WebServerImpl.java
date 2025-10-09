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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;

import java.util.Objects;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class WebServerImpl implements ServerSettings.WebServer {
    @JacksonXmlProperty(isAttribute = true, localName = "autostart")
    private boolean autostart;
    @JacksonXmlProperty(isAttribute = true, localName = "port")
    private int port;
    @JacksonXmlProperty(isAttribute = true, localName = "hostname")
    private String hostname;

    public WebServerImpl() {}

    public WebServerImpl(boolean autostart, int port, String hostname, String allowedOrigins) {
        this.autostart = autostart;
        this.port = port;
        this.hostname = hostname;
        this.allowedOrigins = allowedOrigins;
    }

    public static WebServerImpl createDefault() {
        return new WebServerImpl(true, 8080, null, null);
    }

    @JsonProperty("allowed-origins")
    private String allowedOrigins;

    @JsonProperty("allow-unauthorized")
    private boolean allowUnauthorized = false;

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
        return this.port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String getHostname() {
        return this.hostname;
    }

    @Override
    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public String getAllowedOrigins() {
        return this.allowedOrigins;
    }

    @Override
    public boolean isAllowUnauthorized() {
        return allowUnauthorized;
    }

    @Override
    public void setAllowUnauthorized(boolean allowUnauthorized) {
        this.allowUnauthorized = allowUnauthorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WebServerImpl webServer = (WebServerImpl) o;
        return autostart == webServer.autostart && port == webServer.port
                && Objects.equals(allowedOrigins, webServer.allowedOrigins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autostart, port, allowedOrigins);
    }

    @Override
    public String toString() {
        return "WebServerImpl{" + "autostart=" + autostart + ", port=" + port + ", allowedOrigins='" + allowedOrigins
                + '\'' + '}';
    }
}
