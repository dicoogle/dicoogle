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
package pt.ua.dicoogle.server.web.servlets.management;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;

import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;
import pt.ua.dicoogle.sdk.settings.server.ServerSettingsReader;
import pt.ua.dicoogle.server.ControlServices;

/** Servlet for reading and writing DICOM service configurations.
 * Modifying the "running" setting will trigger a start or a stop on the actual service.
 *
 * @author Frederico Silva <fredericosilva@ua.pt>
 */
public class ServicesServlet extends HttpServlet {

    /** The type of DICOM service */
    public enum ServiceType {
        /** DICOM storage */
        STORAGE,
        /** DICOM query/retrieve */
        QUERY
    }

    private final ServiceType mType;

    public ServicesServlet(ServiceType type) {
        mType = type;
    }

    /** Get the current status of a DICOM service */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final ControlServices controlServices = ControlServices.getInstance();
        final ServerSettings serverSettings = ServerSettingsManager.getSettings();

        boolean isRunning;
        ServerSettingsReader.ServiceBase base;
        switch (mType) {
            case STORAGE:
                base = serverSettings.getDicomServicesSettings().getStorageSettings();
                isRunning = controlServices.storageIsRunning();
                break;
            case QUERY:
                base = serverSettings.getDicomServicesSettings().getQueryRetrieveSettings();
                isRunning = controlServices.queryRetrieveIsRunning();
                break;
            default:
                throw new IllegalStateException("Unexpected service type " + mType);
        }
        int port = base.getPort();
        boolean autostart = base.isAutostart();
        String hostname = base.getHostname();

        JSONObject obj = new JSONObject();
        obj.element("isRunning", isRunning);
        obj.element("port", port);
        obj.element("autostart", autostart);
        if (hostname != null) {
            obj.element("hostname", hostname);
        }

        resp.setContentType("application/json");
        resp.getWriter().print(obj.toString());
    }

    /** Start/stop a DICOM service or set whether the DICOM service should auto-start */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JSONObject obj = new JSONObject();

        // gather settings to update
        boolean updateAutostart = false, updatePort = false, updateRunning = false, updateHostname = false;
        boolean autostart = false, running = false;
        int port = 0;
        String hostname = null;

        String paramPort = req.getParameter("port");
        if (paramPort != null) {
            try {
                port = Integer.parseInt(paramPort);
                if (port <= 0 || port > 65535) {
                    throw new NumberFormatException();
                }
                updatePort = true;
            } catch (NumberFormatException ex) {
                obj.element("success", false);
                obj.element("error", "Bad service port: must be a valid port number");
                reply(resp, 400, obj);
                return;
            }
        }

        String paramAutostart = req.getParameter("autostart");
        if (paramAutostart != null) {
            autostart = Boolean.parseBoolean(paramAutostart);
            updateAutostart = true;
        }

        String paramRunning = req.getParameter("running");
        if (paramRunning != null) {
            running = Boolean.parseBoolean(paramRunning);
            updateRunning = true;
        }

        String paramHostname = req.getParameter("hostname");
        if (paramHostname != null) {
            hostname = paramHostname.trim();
            // remove hostname if empty
            if (hostname.isEmpty()) {
                hostname = null;
            }
            updateHostname = true;
        }

        final ControlServices controlServices = ControlServices.getInstance();
        final ServerSettings serverSettings = ServerSettingsManager.getSettings();

        // update auto-start
        if (updateAutostart) {
            switch (mType) {
                case STORAGE:
                    serverSettings.getDicomServicesSettings().getStorageSettings().setAutostart(autostart);
                    obj.element("autostart", autostart);
                    break;
                case QUERY:
                    serverSettings.getDicomServicesSettings().getQueryRetrieveSettings().setAutostart(autostart);
                    obj.element("autostart", autostart);
                    break;
            }
        }

        // update port
        if (updatePort) {
            switch (mType) {
                case STORAGE:
                    serverSettings.getDicomServicesSettings().getStorageSettings().setPort(port);
                    obj.element("port", port);
                    break;
                case QUERY:
                    serverSettings.getDicomServicesSettings().getQueryRetrieveSettings().setPort(port);
                    obj.element("port", port);
                    break;
            }
        }

        // update hostname
        if (updateHostname) {
            switch (mType) {
                case STORAGE:
                    serverSettings.getDicomServicesSettings().getStorageSettings().setHostname(hostname);
                    obj.element("hostname", hostname);
                    break;
                case QUERY:
                    serverSettings.getDicomServicesSettings().getQueryRetrieveSettings().setHostname(hostname);
                    obj.element("hostname", hostname);
                    break;
            }
        }

        // update running
        if (updateRunning) {
            try {
                switch (mType) {
                    case STORAGE:
                        if (running) {
                            boolean out = controlServices.startStorage();
                            if (!out) {
                                resp.addHeader("Warning", "Service was already running");
                                obj.element("warning", "Service was already running");
                            }
                            obj.element("running", true);
                        } else {
                            controlServices.stopStorage();
                            obj.element("running", false);
                        }
                        break;

                    case QUERY:
                        if (running) {
                            controlServices.startQueryRetrieve();
                            obj.element("running", true);

                        } else {
                            controlServices.stopQueryRetrieve();
                            obj.element("running", false);
                        }
                        break;

                    default:
                        break;
                }
            } catch (Exception ex) {
                obj.element("success", false);
                obj.element("error", ex.getMessage());
                reply(resp, 500, obj);
                return;
            }
        }

        // finalize
        ServerSettingsManager.saveSettings();
        obj.element("success", true);
        reply(resp, 200, obj);
    }

    private static void reply(HttpServletResponse resp, int code, JSONObject object) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(code);
        resp.getWriter().print(object.toString());
    }

}
