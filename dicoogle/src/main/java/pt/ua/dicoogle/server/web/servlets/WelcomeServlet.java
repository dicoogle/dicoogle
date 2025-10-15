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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for serving welcome messages for the webapp.
 */
public class WelcomeServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(WelcomeServlet.class);

    // thread-local random generator to pick messages randomly
    private ThreadLocal<Random> random = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };

    /** A list of messages fetched from a file
     * (embedded resource by default,
     * overridable with JVM property `dicoogle.welcomeFile`)
     */
    private static String[] MESSAGES = loadMessages();

    /** A welcome message for when no messages are available */
    private static final String FALLBACK_MESSAGE = "Welcome to Dicoogle!";

    private static String[] loadMessages() {
        try {
            List<String> welcomeFull;
            String welcomeFile = System.getProperty("dicoogle.welcomeFile");
            if (welcomeFile != null) {
                welcomeFull = Files.readAllLines(Paths.get(welcomeFile));
            } else {
                InputStream is = WelcomeServlet.class.getResourceAsStream("/welcome.html");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                    welcomeFull = reader.lines().collect(Collectors.toList());
                }
            }
            List<String> messages = new ArrayList<>();
            String msg = "";
            for (String line : welcomeFull) {
                if (line.startsWith("#")) {
                    continue;
                }
                // identify end of message (an empty line)
                if (line.isEmpty()) {
                    messages.add(msg);
                    msg = "";
                } else {
                    msg += line + "\n";
                }
            }
            if (!msg.isEmpty()) {
                messages.add(msg);
            }
            if (messages.isEmpty()) {
                messages.add(FALLBACK_MESSAGE);
            }

            return messages.toArray(new String[0]);
        } catch (IOException e) {
            LOG.error("Failed to load welcome messages", e);
            return new String[] {FALLBACK_MESSAGE};
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = MESSAGES[random.get().nextInt(MESSAGES.length)];
        response.setContentType("text/plain");
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
