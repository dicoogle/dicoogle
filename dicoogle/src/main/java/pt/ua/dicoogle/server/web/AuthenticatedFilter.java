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
/**
 */

package pt.ua.dicoogle.server.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.ua.dicoogle.server.users.Role;
import pt.ua.dicoogle.server.users.User;
import pt.ua.dicoogle.server.web.auth.Authentication;

/** Servlet filter to ensure that the user is logged in
 * (has a valid session token in `Authorization`).
 */
public class AuthenticatedFilter implements Filter {

    public static final String NEEDS_ADMIN_PARAM = "needsAdmin";
    public static final String NEEDS_ROLE_PARAM = "needsRole";

    /** Whether the user needs to be an admin */
    private boolean needsAdmin = false;
    /** Whether the user needs to have this specific role (or be admin) */
    private Role needsRole = null;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        try {
            String needsAdminStr = fc.getInitParameter(NEEDS_ADMIN_PARAM);
            if (needsAdminStr != null && !needsAdminStr.isEmpty()) {
                needsAdmin = Boolean.parseBoolean(needsAdminStr);
            }
        } catch (IllegalArgumentException ex) {
            throw new ServletException("Invalid needsAdmin value for AuthenticatedFilter", ex);
        }

        try {
            String needsRoleStr = fc.getInitParameter(NEEDS_ROLE_PARAM);
            if (needsRoleStr != null && !needsRoleStr.isEmpty()) {
                needsRole = new Role(needsRoleStr);
            }
        } catch (IllegalArgumentException ex) {
            throw new ServletException("Invalid role for AuthenticatedFilter", ex);
        }
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc)
            throws IOException, ServletException {

        if (sreq instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) sreq;
            String token = req.getHeader("Authorization");

            // Authorization header must have a Dicoogle session token
            if (token == null) {
                unauthorized(sresp);
                return;
            }

            // user has to exist
            User user = Authentication.getInstance().getUsername(token);
            if (user == null) {
                forbidden(sresp);
                return;
            }

            // user must be admin if required by this endpoint
            if (needsAdmin && !user.isAdmin()) {
                forbidden(sresp);
                return;
            }

            // if a role is required by this endpoint,
            // the user must either have that role or be admin
            if (!user.isAdmin() && needsRole != null && !user.hasRole(needsRole)) {
                forbidden(sresp);
                return;
            }

            // OK
        }
        fc.doFilter(sreq, sresp);
    }

    private static void unauthorized(ServletResponse resp) throws IOException {
        if (resp instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private static void forbidden(ServletResponse resp) throws IOException {
        if (resp instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            httpResp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {}

}
