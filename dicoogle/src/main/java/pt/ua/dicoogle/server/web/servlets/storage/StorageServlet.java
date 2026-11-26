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
package pt.ua.dicoogle.server.web.servlets.storage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;

/**
 * Servlet for storage operations
 */
public class StorageServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(StorageServlet.class);

    /** Store the content of the request body into the storage
     * provider for the given scheme.
     * 
     * <p>Query parameter <b>scheme</b> defines the Dicoogle item URI scheme of the provider (e.g. "file").
     *
     * <p>The request body must contain the raw data to write in a new object,
     * in <code>application/dicom</code>.
     * Note that unlike DICOMWeb STOW-RS, this servlet does not support multipart requests
     * and can only store one item at a time.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String scheme = req.getParameter("scheme");
        if (StringUtils.isEmpty(scheme)) {
            ResponseUtil.sendError(resp, 400, "No scheme supplied");
            return;
        }

        StorageInterface storage = PluginController.getInstance().getStorageForSchema(scheme);
        if (storage == null) {
            ResponseUtil.sendError(resp, 400, "Unrecognized scheme");
            return;
        }

        // check the content type
        String contentType = req.getHeader("Content-Type");
        // remove attributes from content type
        if (contentType == null || !contentType.split(";")[0].equalsIgnoreCase("application/dicom")) {
            resp.setHeader("Accept-Post", "application/dicom");
            ResponseUtil.sendError(resp, 415, "Unsupported content: must be in single part application/dicom");
            return;
        }

        LOG.info("Item store request via POST /storage");
        URI uri = storage.store(new DicomInputStream(new BufferedInputStream(req.getInputStream())));
        if (uri == null) {
            // storage provider refused the object
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write("{\"message\": \"content refused by the storage provider\"}");
            return;
        }

        // dispatch indexers
        PluginController.getInstance().index(uri);

        resp.setHeader("Content-Type", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(resp.getWriter(), new StoredItem(uri.toString()));
    }

    public static class StoredItem implements java.io.Serializable {
        public final String uri;

        public StoredItem(String uri) {
            this.uri = uri;
        }
    }

}
