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

package pt.ua.dicoogle.server.web.utils;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

/**
 * Common simple responses of success and failure
 * 
 * @author Frederico Silva <fredericosilva@ua.pt>
 */
public class ResponseUtil {

    public static void simpleResponse(HttpServletResponse resp, String name, boolean state) throws IOException {
        resp.setContentType("application/json");
        JSONObject object = new JSONObject();
        object.put(name, state);
        object.write(resp.getWriter());
    }

    public static void simpleResponse(HttpServletResponse resp, String name, String value) throws IOException {
        resp.setContentType("application/json");
        JSONObject object = new JSONObject();
        object.put(name, value);
        object.write(resp.getWriter());
    }

    public static void objectResponse(HttpServletResponse resp, List<Pair<?>> pairs) throws IOException {
        resp.setContentType("application/json");
        JSONObject object = new JSONObject();

        for (Pair<?> entry : pairs) {
            object.put(entry.getKey(), entry.getValue().toString());
        }

        object.write(resp.getWriter());
    }

    /**
     * Associates an error to an http response.
     *
     * @param resp the response
     * @param code the status code
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    public static void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(code);
        JSONObject obj = new JSONObject();
        obj.put("error", message);
        resp.getWriter().append(obj.toString());
    }

    /*
     * Generic Pair Util for Json response
     */
    public static class Pair<V> {
        String key;
        V value;

        public Pair(String key, V value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }


    }
}
