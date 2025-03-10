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
package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.data.Status;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.mlprovider.MLModelTrainInfo;
import pt.ua.dicoogle.sdk.mlprovider.MLProviderInterface;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This servlet returns a detailed list of metrics described in @see pt.ua.dicoogle.sdk.mlprovider.MLModelTrainInfo.
 * Usually it should only be available when a model has already been trained at least once.
 * Model is identified by the provider and model ID.
 */
public class ModelinfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        String provider = request.getParameter("provider");
        String modelID = request.getParameter("modelID");

        if (provider == null || provider.isEmpty()) {
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider was not specified");
            return;
        }

        if (modelID == null || modelID.isEmpty()) {
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Model was not specified");
            return;
        }

        MLProviderInterface mlPlugin = PluginController.getInstance().getMachineLearningProviderByName(provider, true);
        if (mlPlugin == null) {
            ResponseUtil.sendError(response, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider not found");
            return;
        }

        MLModelTrainInfo info = mlPlugin.modelInfo(modelID);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, info);
        out.close();
        out.flush();
    }

}
