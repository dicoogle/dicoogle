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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.datastructs.dim.BulkAnnotation;
import pt.ua.dicoogle.sdk.datastructs.dim.Point2D;
import pt.ua.dicoogle.server.web.dicom.ROIExtractor;
import pt.ua.dicoogle.server.web.utils.cache.WSICache;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ROIServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ROIServlet.class);
    private static final long serialVersionUID = 1L;

    private final ROIExtractor roiExtractor;
    private final WSICache wsiCache;

    /**
     * Creates ROI servlet servlet.
     *
     */
    public ROIServlet() {
        this.roiExtractor = new ROIExtractor();
        this.wsiCache = WSICache.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sopInstanceUID = request.getParameter("uid");
        String x = request.getParameter("x");
        String y = request.getParameter("y");
        String width = request.getParameter("width");
        String height = request.getParameter("height");

        if(sopInstanceUID == null || sopInstanceUID.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "SOPInstanceUID provided was invalid");
            return;
        }

        if(x == null || x.isEmpty() || y == null || y.isEmpty() || width == null || width.isEmpty() || height == null || height.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "ROI provided was invalid");
            return;
        }

        BulkAnnotation annotation;
        try{
            int nX = Integer.parseInt(x);
            int nY = Integer.parseInt(y);
            int nWidth = Integer.parseInt(width);
            int nHeight = Integer.parseInt(height);
            annotation = new BulkAnnotation();
            Point2D tl = new Point2D(nX, nY);
            Point2D tr = new Point2D(nX + nWidth, nY);
            Point2D bl = new Point2D(nX, nY + nHeight);
            Point2D br = new Point2D(nX + nWidth, nY + nHeight);
            List<Point2D> points = new ArrayList<>();
            points.add(tl); points.add(tr); points.add(bl); points.add(br);
            annotation.setPoints(points);
            annotation.setAnnotationType(BulkAnnotation.AnnotationType.RECTANGLE);
        } catch (NumberFormatException e){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "ROI provided was invalid");
            return;
        }

        DicomMetaData metaData = getDicomMetadata(sopInstanceUID);

        BufferedImage bi = roiExtractor.extractROI(metaData, annotation);

        if(bi != null){
            response.setContentType("image/jpeg");
            OutputStream out = response.getOutputStream();
            ImageIO.write(bi, "jpg", out);
            out.close();
            return;
        }

        response.sendError(Status.CLIENT_ERROR_NOT_FOUND.getCode(), "Could not build ROI with the provided UID");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String jsonString = IOUtils.toString(request.getReader());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode body = mapper.readTree(jsonString);

        if(!body.has("uid")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "SOPInstanceUID provided was invalid");
            return;
        }

        if(!body.has("type")){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Annotation type must be provided");
            return;
        }

        String sopInstanceUID = body.get("uid").asText();
        String type = body.get("type").asText();
        List<Point2D> points = mapper.readValue(body.get("points").toString(), new TypeReference<List<Point2D>>(){});

        BulkAnnotation annotation = new BulkAnnotation();
        annotation.setPoints(points);
        annotation.setAnnotationType(BulkAnnotation.AnnotationType.valueOf(type));

        DicomMetaData dicomMetaData = this.getDicomMetadata(sopInstanceUID);

        BufferedImage bi = roiExtractor.extractROI(dicomMetaData, annotation);

        if(bi != null){
            response.setContentType("image/jpeg");
            OutputStream out = response.getOutputStream();
            ImageIO.write(bi, "jpg", out);
            out.close();
            return;
        }

        response.sendError(Status.CLIENT_ERROR_NOT_FOUND.getCode(), "Could not build ROI with the provided UID");
    }

    private DicomMetaData getDicomMetadata(String sop) throws IOException{
        return wsiCache.get(sop);
    }

}