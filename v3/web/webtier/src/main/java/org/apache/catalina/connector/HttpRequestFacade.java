

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */


package org.apache.catalina.connector;


import java.security.Principal;
import java.util.Locale;
import java.util.Enumeration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.session.StandardSessionFacade;
import org.apache.catalina.util.StringManager;


/**
 * Facade class that wraps a Catalina-internal <b>HttpRequest</b>
 * object.  All methods are delegated to the wrapped request.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.3 $ $Date: 2006/08/14 20:45:37 $
 */

public final class HttpRequestFacade
    extends RequestFacade
    implements HttpServletRequest {


    // ----------------------------------------------------- Constants


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified request.
     *
     * @param request The request to be wrapped
     */
    public HttpRequestFacade(HttpRequest request) {
        super(request);
    }


    // --------------------------------------------- HttpServletRequest Methods


    public String getAuthType() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getAuthType();
    }


    public Cookie[] getCookies() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getCookies();
    }


    public long getDateHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getDateHeader(name);
    }


    public String getHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getHeader(name);
    }


    public Enumeration getHeaders(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getHeaders(name);
    }


    public Enumeration getHeaderNames() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getHeaderNames();
    }


    public int getIntHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getIntHeader(name);
    }


    public String getMethod() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getMethod();
    }


    public String getPathInfo() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getPathInfo();
    }


    public String getPathTranslated() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getPathTranslated();
    }


    public String getContextPath() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getContextPath();
    }


    public String getQueryString() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getQueryString();
    }


    public String getRemoteUser() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRemoteUser();
    }


    public boolean isUserInRole(String role) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isUserInRole(role);
    }


    public java.security.Principal getUserPrincipal() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getUserPrincipal();
    }


    public String getRequestedSessionId() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRequestedSessionId();
    }


    public String getRequestURI() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRequestURI();
    }


    public StringBuffer getRequestURL() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRequestURL();
    }


    public String getServletPath() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getServletPath();
    }


    public HttpSession getSession(boolean create) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        HttpSession session =
            ((HttpServletRequest) request).getSession(create);
        if (session == null)
            return null;
        else
            return new StandardSessionFacade(session);
    }


    public HttpSession getSession() {
        return getSession(true);
    }


    public boolean isRequestedSessionIdValid() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdValid();
    }


    public boolean isRequestedSessionIdFromCookie() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdFromCookie();
    }


    public boolean isRequestedSessionIdFromURL() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdFromURL();
    }


    public boolean isRequestedSessionIdFromUrl() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdFromURL();
    }


}
