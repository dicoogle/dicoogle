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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import pt.ua.dicoogle.core.settings.ServerSettingsManager;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;
import pt.ua.dicoogle.server.web.utils.ResponseUtil.Pair;

/**
 * 
 * @author Frederico Silva <fredericosilva@ua.pt>
 */
public class AETitleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Pair<?>> mpairs = new ArrayList<>();
        mpairs.add(new ResponseUtil.Pair<>("aetitle",
                ServerSettingsManager.getSettings().getDicomServicesSettings().getAETitle()));

        ResponseUtil.objectResponse(resp, mpairs);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String aetitle = req.getParameter("aetitle");

        if (StringUtils.isEmpty(aetitle)) {
            resp.sendError(402, "aetitle param not found");
            return;
        }

        ServerSettingsManager.getSettings().getDicomServicesSettings().setAETitle(aetitle);

        ResponseUtil.simpleResponse(resp, "success", true);

    }

}
