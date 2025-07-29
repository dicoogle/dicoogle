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
package pt.ua.dicoogle.server.web.servlets;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletOutputStream;

import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.task.JointQueryTask;
import pt.ua.dicoogle.sdk.task.Task;
import pt.ua.dicoogle.sdk.utils.QueryParseException;
import pt.ua.dicoogle.server.web.dicom.Convert2PNG;
import pt.ua.dicoogle.server.web.dicom.Information;
import pt.ua.dicoogle.server.web.utils.LocalImageCache;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;

/**
 * Handles the requests for DICOM frames, returning them as PNG images.
 * Also maintains a cache of the images already served to speed-up the next requests (minimizing server load by doing way less conversions).
 *
 * @author Antonio
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class ImageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ImageServlet.class);
    private static final long serialVersionUID = 1L;

    public static final int BUFFER_SIZE = 1500; // byte size for read-write ring bufer, optimized for regular TCP connection windows

    private final LocalImageCache cache;

    /**
     * Creates an image servlet.
     *
     * @param cache the local image caching system, can be null and if so no caching mechanism will be used.
     */
    public ImageServlet(LocalImageCache cache) {
        this.cache = cache;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sopInstanceUID = request.getParameter("SOPInstanceUID");
        String uri = request.getParameter("uri");
        String qsThumbnail = request.getParameter("thumbnail");
        boolean thumbnail = qsThumbnail != null && (qsThumbnail.isEmpty() || Boolean.valueOf(qsThumbnail));

        if (sopInstanceUID == null) {
            if (uri == null) {
                response.sendError(400, "URI or SOP Instance UID not provided");
                return;
            }
        } else {
            sopInstanceUID = sopInstanceUID.trim();
            if (sopInstanceUID.isEmpty()) {
                response.sendError(400, "Invalid SOP Instance UID!");
                return;
            }
        }
        String[] providerArray = request.getParameterValues("provider");
        List<String> providers = providerArray == null ? null : Arrays.asList(providerArray);
        String sFrame = request.getParameter("frame");
        int frame;
        if (sFrame == null) {
            frame = 0;
        } else {
            frame = Integer.parseInt(sFrame);
        }

        StorageInputStream imgFile;
        if (sopInstanceUID != null) {
            try {
                // get the image file for that SOP Instance UID
                imgFile = getFileFromSOPInstanceUID(sopInstanceUID, providers);
            } catch (QueryParseException ex) {
                ResponseUtil.sendError(response, 400, "Failed to parse the query: " + ex.getMessage());
                return;
            }
            // if no .dcm file was found tell the client
            if (imgFile == null) {
                ResponseUtil.sendError(response, 404, "No image file for supplied SOP Instance UID");
                return;
            }
        } else {
            try {
                // get the image file by the URI
                URI imgUri = new URI(uri);
                StorageInterface storageInt = PluginController.getInstance().getStorageForSchema(imgUri);
                StorageInputStream storageItem = storageInt.get(imgUri);
                // take the first valid storage
                if (storageItem == null) {
                    ResponseUtil.sendError(response, 404, "No image file for supplied URI");
                    return;
                }
                imgFile = storageItem;

            } catch (URISyntaxException ex) {
                ResponseUtil.sendError(response, 400, "Bad URI syntax");
                return;
            }
        }

        // if there is a cache available then use it
        if (cache != null && cache.isRunning()) {

            try {
                InputStream istream = cache.get(imgFile.getURI(), frame, thumbnail);
                response.setContentType("image/png");
                try (ServletOutputStream out = response.getOutputStream()) {
                    IOUtils.copy(istream, out);
                }
            } catch (IOException ex) {
                logger.warn("Could not convert the image", ex);
                ResponseUtil.sendError(response, 500, "Failed to convert the image");
            } catch (RuntimeException ex) {
                logger.error("Unexpected exception", ex);
                ResponseUtil.sendError(response, 500, "Unexpected internal server error");
            }

        } else {
            // if the cache is invalid or not running convert the image and return it "on-the-fly"
            try {
                ByteArrayOutputStream pngStream = getPNGStream(imgFile, frame, thumbnail);
                response.setContentType("image/png"); // set the appropriate type for the PNG image
                response.setContentLength(pngStream.size()); // set the image size
                try (ServletOutputStream out = response.getOutputStream()) {
                    pngStream.writeTo(out);
                    pngStream.flush();
                }
            } catch (IOException ex) {
                logger.warn("Could not convert the image", ex);
                ResponseUtil.sendError(response, 500, "Failed to convert the image");
            }
        }
    }

    private ByteArrayOutputStream getPNGStream(StorageInputStream imgFile, int frame, boolean thumbnail)
            throws IOException {
        ByteArrayOutputStream pngStream;
        if (thumbnail) {
            // retrieve thumbnail dimension settings
            int thumbSize = ServerSettingsManager.getSettings().getArchiveSettings().getThumbnailSize();
            pngStream = Convert2PNG.DICOM2ScaledPNGStream(imgFile, frame, thumbSize, thumbSize);
        } else {
            pngStream = Convert2PNG.DICOM2PNGStream(imgFile, frame);
        }
        return pngStream;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO this service does not make sense as a POST.
        // either remove or relocate to another resource

        String sop = req.getParameter("SOPInstanceUID");
        if (sop == null) {
            resp.sendError(500, "No SOPInstanceUID in Request");
            return;
        }

        float frameRate = Information.getFrameRateFromImage(sop);
        if (frameRate == -1) {
            resp.sendError(500, "Cannot Locate the image File.");
            return;
        }

        int nFrames = Information.getNumberOfFramesInFile(sop);

        JSONObject r = new JSONObject();
        r.put("SOPInstanceUID", sop);
        r.put("NumberOfFrames", nFrames);
        r.put("FrameRate", frameRate);

        resp.setContentType("application/json");

        PrintWriter wr = resp.getWriter();
        wr.print(r.toString());
    }

    private static StorageInputStream getFileFromSOPInstanceUID(String sopInstanceUID, List<String> providers)
            throws IOException {
        JointQueryTask qt = new JointQueryTask() {
            @Override
            public void onCompletion() {}

            @Override
            public void onReceive(Task<Iterable<SearchResult>> e) {}
        };
        try {
            PluginController pc = PluginController.getInstance();
            if (providers == null) {
                // use only DIM sources
                providers = ServerSettingsManager.getSettings().getArchiveSettings().getDIMProviders();
                if (providers == null) {
                    // fallback to all query providers
                    providers = pc.getQueryProvidersName(true);
                } else {
                    // exclude unknown query providers
                    providers.removeIf(pName -> pc.getQueryProviderByName(pName, true) == null);
                    if (providers.isEmpty()) {
                        // fallback to all query providers
                        providers = pc.getQueryProvidersName(true);
                    }
                }
            }
            Iterator<SearchResult> it =
                    pc.query(qt, providers, "SOPInstanceUID:\"" + sopInstanceUID + '"').get().iterator();
            if (!it.hasNext()) {
                throw new IOException("No such image of SOPInstanceUID " + sopInstanceUID);
            }
            SearchResult res = it.next();
            StorageInterface storage = pc.getStorageForSchema(res.getURI());
            if (storage == null) {
                throw new IOException("Unsupported file scheme");
            }
            StorageInputStream item = storage.get(res.getURI());
            if (item == null) {
                throw new IOException("No storage item found");
            }
            return item;
        } catch (InterruptedException | ExecutionException ex) {
            throw new IOException(ex);
        }
    }
}
