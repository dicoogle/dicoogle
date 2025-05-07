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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;
import pt.ua.dicoogle.server.web.utils.ResponseUtil.Pair;

/** Servlet to retrieve and update archive settings pertaining to the Dicoogle node */
public class ArchiveSettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Pair<?>> mpairs = new ArrayList<>();
        mpairs.add(new ResponseUtil.Pair<>("nodeName",
                ServerSettingsManager.getSettings().getArchiveSettings().getNodeName()));
        mpairs.add(new ResponseUtil.Pair<>("dimProviders",
                ServerSettingsManager.getSettings().getArchiveSettings().getDIMProviders()));
        mpairs.add(new ResponseUtil.Pair<>("defaultStorage",
                ServerSettingsManager.getSettings().getArchiveSettings().getDefaultStorage()));

        ResponseUtil.objectResponse(resp, mpairs);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ServerSettings.Archive archiveSettings = ServerSettingsManager.getSettings().getArchiveSettings();

        // update nodeName
        String nodeName = req.getParameter("nodeName");

        boolean changed = false;
        if (nodeName != null) {
            nodeName = nodeName.trim();
            if (!nodeName.isEmpty()) {
                archiveSettings.setNodeName(nodeName);
                changed = true;
            }
        }

        // update dimProviders (comma separated)
        String dimProviders = req.getParameter("dimProviders");
        if (dimProviders != null) {
            dimProviders = dimProviders.trim();
            if (!dimProviders.isEmpty()) {
                String[] providers = dimProviders.split(",");
                archiveSettings.setDIMProviders(Arrays.asList(providers));
                changed = true;
            }
        }

        // update defaultStorage (comma separated)
        String defaultStorage = req.getParameter("defaultStorage");
        if (defaultStorage != null) {
            defaultStorage = defaultStorage.trim();
            if (!defaultStorage.isEmpty()) {
                String[] providers = defaultStorage.split(",");
                archiveSettings.setDefaultStorage(Arrays.asList(providers));
                changed = true;
            }
        }

        if (changed) {
            ResponseUtil.simpleResponse(resp, "success", true);
        } else {
            ResponseUtil.simpleResponse(resp, "warning", "no changes made");
        }
    }

}
