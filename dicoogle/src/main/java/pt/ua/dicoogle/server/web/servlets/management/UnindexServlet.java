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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;
import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.datastructs.UnindexReport;
import pt.ua.dicoogle.sdk.task.JointQueryTask;
import pt.ua.dicoogle.sdk.task.Task;

/** Unindexer servlet.
 * 
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class UnindexServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UnindexServlet.class);

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Getting Parameters.
        String[] paramUri = req.getParameterValues("uri");
        String[] paramSop = req.getParameterValues("SOPInstanceUID");
        String[] paramSeries = req.getParameterValues("SeriesInstanceUID");
        String[] paramStudy = req.getParameterValues("StudyInstanceUID");
        String[] providerArr = req.getParameterValues("provider");

        List<String> providers = providerArr != null ? Arrays.asList(providerArr) : null;

        int paramCount = (paramUri != null ? 1 : 0) + (paramSop != null ? 1 : 0) + (paramSeries != null ? 1 : 0)
                + (paramStudy != null ? 1 : 0);

        if (paramCount == 0) {
            ResponseUtil.sendError(resp, 400,
                    "No arguments provided; must include either one of `uri`, `SOPInstanceUID`, `SeriesInstanceUID` or `StudyInstanceUID`");
            return;
        } else if (paramCount > 1) {
            ResponseUtil.sendError(resp, 400,
                    "No arguments provided; must include either one of `uri`, `SOPInstanceUID`, `SeriesInstanceUID` or `StudyInstanceUID`");
            return;
        }

        PluginController pc = PluginController.getInstance();

        long indexed = 0;
        long failed = 0;
        long notfound = 0;

        Collection<URI> uris = resolveURIs(paramUri, paramSop, paramSeries, paramStudy);

        // if only one entry, do it inline
        if (uris.size() <= 1) {
            for (URI uri : uris) {
                try {
                    pc.unindex(uri, providers);
                    indexed += 1;
                } catch (RuntimeException ex) {
                    logger.error("Failed to unindex {}", uri, ex);
                    failed += 1;
                }
            }

        } else {
            // if many, use bulk unindexing
            List<Task<UnindexReport>> tasks = new ArrayList<>();

            if (providers == null) {
                providers = pc.getIndexingPlugins(true).stream().map(p -> p.getName()).collect(Collectors.toList());
            }
            for (String indexProvider : providers) {
                tasks.add(pc.unindex(indexProvider, uris, null));
            }

            int i = 0;
            for (Task<UnindexReport> task : tasks) {
                try {
                    UnindexReport report = task.get();
                    indexed = uris.size() - report.notUnindexedFileCount();
                    failed = report.failedFileCount();
                    notfound = report.getNotFound().size();
                } catch (Exception ex) {
                    logger.error("Task to unindex items in {} failed", providers.get(i), ex);
                }
            }
        }

        logger.info("Finished unindexing {} out of {} files", indexed, uris.size());
        resp.setContentType("application/json");
        JSONObject obj = new JSONObject();
        obj.put("indexed", indexed);
        obj.put("failed", failed);
        obj.put("notFound", notfound);
        resp.setStatus(200);
        resp.getWriter().write(obj.toString());
    }

    /// Convert the given parameters into a list of URIs
    private static Collection<URI> resolveURIs(String[] paramUri, String[] paramSop, String[] paramSeries,
            String[] paramStudy) {
        if (paramUri != null) {
            return Stream.of(paramUri).map(URI::create).collect(Collectors.toList());
        }
        String attribute = null;
        if (paramSop != null) {
            attribute = "SOPInstanceUID";
        } else if (paramSeries != null) {
            attribute = "SeriesInstanceUID";
        } else if (paramStudy != null) {
            attribute = "StudyInstanceUID";
        }

        final String dcmAttribute = attribute;
        List<String> dicomProviders = ServerSettingsManager.getSettings().getArchiveSettings().getDIMProviders();
        if (dicomProviders.isEmpty()) {
            return Arrays.stream(paramSop).flatMap(uid -> {
                // translate to URIs
                JointQueryTask holder = new JointQueryTask() {
                    @Override
                    public void onReceive(Task<Iterable<SearchResult>> e) {}

                    @Override
                    public void onCompletion() {}
                };
                try {
                    return StreamSupport.stream(PluginController.getInstance()
                            .queryAll(holder, dcmAttribute + ":\"" + uid + '"').get().spliterator(), false);
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }).map(r -> r.getURI()).collect(Collectors.toList());

        }
        String dicomProvider = dicomProviders.iterator().next();
        return Arrays.stream(paramSop).flatMap(uid -> {
            // translate to URIs
            QueryInterface dicom = PluginController.getInstance().getQueryProviderByName(dicomProvider, false);

            return StreamSupport.stream(dicom.query(dcmAttribute + ":\"" + uid + '"').spliterator(), false);
        }).map(r -> r.getURI()).collect(Collectors.toList());
    }
}
