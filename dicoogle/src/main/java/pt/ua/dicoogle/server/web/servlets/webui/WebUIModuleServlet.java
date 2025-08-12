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

package pt.ua.dicoogle.server.web.servlets.webui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.plugins.webui.WebUIPlugin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Retrieval of web UI plugins and respective packages/modules.
 * 
 * <b>This API is unstable. It is currently only compatible with dicoogle-webcore 0.14.x</b>
 *
 * @author Eduardo Pinho
 */
public class WebUIModuleServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(WebUIModuleServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getRequestURI().substring("/webui/module/".length());
        if (name.isEmpty()) {
            resp.sendError(400);
            return;
        }
        logger.debug("Service request: retrieve webplugin module {}", name);

        String process = req.getParameter("process");

        resp.setContentType("text/javascript");
        boolean doProcess = process == null || Boolean.parseBoolean(process);

        WebUIPlugin plugin = PluginController.getInstance().getWebUIPlugin(name);
        if (plugin == null) {
            resp.sendError(404);
            return;
        }
        String js = PluginController.getInstance().getWebUIModuleJS(name);

        this.writeModule(resp.getWriter(), name, js, doProcess);
    }

    /** Convert from dash-lowercase to camelCase
     * @param s a string in dash-lowercase
     * @return a string in camelCase
     */
    public static String camelize(String s) {
        String[] words = s.split("-");
        if (words.length == 0)
            return "";
        String t = words[0];
        for (int i = 1; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                t += Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
            }
        }
        return t;
    }

    public static boolean isPrerelease(String version) {
        return version.indexOf('-') != -1;
    }

    public static void writeModule(Writer writer, String name, String module, boolean process) throws IOException {
        if (process) {
            writer.append("(function(r,f){\n");
            writer.append("var w=r.DicoogleWebcore||require(\"dicoogle-webcore\");");
            writer.append("if (w.__esModule)w=w.default;");
            writer.append("var c=(r.Dicoogle||require(\"dicoogle-client\"))();");
            writer.append("if (c.__esModule)c=c.default;");
            writer.append("var m={exports:{}};");
            writer.append("f(c,m,m.exports);");
            writer.append("var o=m.exports.__esModule?m.exports.default:m.exports;");
            writer.append("w.constructors[\"");
            writer.append(name);
            writer.append("\"]=o;");
            writer.append("w.onRegister(new o(),\"");
            writer.append(name);
            writer.append("\");");
            writer.append("})(this,function(Dicoogle,module,exports){\n");
        }
        writer.append(module);
        if (process) {
            if (!module.endsWith("\n")) {
                writer.append("\n");
            }
            writer.append("});\n");
        }
    }
}
