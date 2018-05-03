/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.valves;


import org.apache.catalina.*;
import static com.sun.logging.LogCleanerUtil.neutralizeForLog;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;

/**
 * <p>Implementation of a Valve that logs interesting contents from the
 * specified Request (before processing) and the corresponding Response
 * (after processing).  It is especially useful in debugging problems
 * related to headers and cookies.</p>
 *
 * <p>This Valve may be attached to any Container, depending on the granularity
 * of the logging you wish to perform.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2005/12/08 01:28:25 $
 */

public class RequestDumperValve extends ValveBase {

    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.RequestDumperValve/1.0";

    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Log the interesting request parameters, invoke the next Valve in the
     * sequence, and log the interesting response parameters.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
     public int invoke(Request request, Response response)
         throws IOException, ServletException {

        // Skip logging for non-HTTP requests and responses
        if (!(request instanceof HttpRequest) ||
            !(response instanceof HttpResponse)) {
             return INVOKE_NEXT;
        }

        HttpRequest hrequest = (HttpRequest) request;
        HttpServletRequest hreq =
            (HttpServletRequest) hrequest.getRequest();

        // Log pre-service information
        log("REQUEST URI       =" + hreq.getRequestURI());
        log("          authType=" + hreq.getAuthType());
        log(" characterEncoding=" + hreq.getCharacterEncoding());
        log("     contentLength=" + hreq.getContentLength());
        log("       contentType=" + hreq.getContentType());
        log("       contextPath=" + hreq.getContextPath());
        Cookie cookies[] = hreq.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                log("            cookie=" + cookies[i].getName() + "=" +
                    cookies[i].getValue());
        }
        Enumeration<String> hnames = hreq.getHeaderNames();
        while (hnames.hasMoreElements()) {
            String hname = hnames.nextElement();
            Enumeration<String> hvalues = hreq.getHeaders(hname);
            while (hvalues.hasMoreElements()) {
                String hvalue = hvalues.nextElement();
                log("            header=" + hname + "=" + hvalue);
            }
        }
        log("            locale=" + hreq.getLocale());
        log("            method=" + hreq.getMethod());
        Enumeration<String> pnames = hreq.getParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = pnames.nextElement();
            String pvalues[] = hreq.getParameterValues(pname);
            StringBuilder result = new StringBuilder(pname);
            result.append('=');
            for (int i = 0; i < pvalues.length; i++) {
                if (i > 0)
                    result.append(", ");
                result.append(pvalues[i]);
            }
            log("         parameter=" + result.toString());
        }
        log("          pathInfo=" + hreq.getPathInfo());
        log("          protocol=" + hreq.getProtocol());
        log("       queryString=" + hreq.getQueryString());
        log("        remoteAddr=" + hreq.getRemoteAddr());
        log("        remoteHost=" + hreq.getRemoteHost());
        log("        remoteUser=" + hreq.getRemoteUser());
        log("requestedSessionId=" + hreq.getRequestedSessionId());
        log("            scheme=" + hreq.getScheme());
        log("        serverName=" + hreq.getServerName());
        log("        serverPort=" + hreq.getServerPort());
        log("       servletPath=" + hreq.getServletPath());
        log("          isSecure=" + hreq.isSecure());
        log("---------------------------------------------------------------");

        // Perform the request
        return INVOKE_NEXT;
    }

     /**
      * Log the interesting response parameters.
      */
     public void postInvoke(Request request, Response response)
         throws IOException, ServletException {

        HttpRequest hrequest = (HttpRequest) request;
        HttpResponse hresponse = (HttpResponse) response;
        HttpServletRequest hreq =
            (HttpServletRequest) hrequest.getRequest();
        HttpServletResponse hres =
            (HttpServletResponse) hresponse.getResponse();

        // Log post-service information
        log("---------------------------------------------------------------");
        log("          authType=" + hreq.getAuthType());
        log("     contentLength=" + hresponse.getContentLength());
        log("       contentType=" + hresponse.getContentType());
        Cookie[] rcookies = hreq.getCookies();
        for (int i = 0; i < rcookies.length; i++) {
            log("            cookie=" + rcookies[i].getName() + "=" +
                rcookies[i].getValue() + "; domain=" +
                rcookies[i].getDomain() + "; path=" + rcookies[i].getPath());
        }
        for (String rhname : hres.getHeaderNames()) {
            for (String rhvalue : hres.getHeaders(rhname)) {
                log("            header=" + rhname + "=" + rhvalue);
            }
        }
        log("           message=" + hresponse.getMessage());
        log("        remoteUser=" + hreq.getRemoteUser());
        log("            status=" + hres.getStatus());
        log("===============================================================");

    }


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder("RequestDumperValve[");
        if (container != null)
            sb.append(container.getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        message = neutralizeForLog(message);
        Logger logger = container.getLogger();
        if (logger != null) {
            logger.log(this.toString() + ": " + message);
        } else {
            log.log(Level.INFO, this.toString() + ": " + message);
        }
    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param t Associated exception
     */
    protected void log(String message, Throwable t) {
        message = neutralizeForLog(message);
        Logger logger = container.getLogger();
        if (logger != null) {
            logger.log(this.toString() + ": " + message, t,
                Logger.WARNING);
        } else {
            log.log(Level.WARNING, this.toString() + ": " + message, t);
        }
    }


}
