/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.appclient.jws;

import com.sun.enterprise.appclient.AppClientInfo;
import com.sun.enterprise.appclient.MainWithModuleSupport;
import com.sun.logging.LogDomains;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;



/**
 *Servlet that responds to all Java Web Start requests for app client-related
 *content.
 *<p>
 *This servlet uses the path info from the incoming request and tries to locate
 *a Content object in the shared Java Web Start information data structure, 
 *using the path info as a key.  If such content exists and is dynamic, then
 *the servlet substitutes some request-based properties (such as host and port)
 *in the content and writes the result as the response.  
 *<p>
 *If the content exists 
 *and is static, the servlet wraps the request setting the wrapped request's 
 *path from the relative path stored with the Content object.  
 *directory tree.  It then invokes the default servlet's method to process
 *the request.  This is made easy because this class extends the default servlet
 *class.
 *<p>
 *If the path info from the request does not map to any content, then the
 *servlet rejects the request as not-found.
 *
 * @author tjquinn
 */
public class JWSSystemServlet extends HttpServlet {
    
    private static final String ARG_QUERY_PARAM_NAME = "arg";
    private static final String PROP_QUERY_PARAM_NAME = "prop";
    private static final String VMARG_QUERY_PARAM_NAME = "vmarg";
    
    private static final String LAST_MODIFIED_HEADER_NAME = "Last-Modified";
    private static final String DATE_HEADER_NAME = "Date";
    
    private static final String GET_METHOD_NAME = "GET";
    
    private final String lineSep = System.getProperty("line.separator");
    
    private AppclientJWSSupportInfo jwsInfo = null;

    private Logger _logger=LogDomains.getLogger(NamingConventions.JWS_LOGGER);

    RequestDispatcher defaultDispatcher = null;

    /** Creates a new instance of JWSSystemServlet */
    public JWSSystemServlet() {
    }
    
    /**
     *Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        String dispatcherName = getInitParameter("defaultDispatcherName");
        if (dispatcherName==null)
        	dispatcherName = "default";
         defaultDispatcher = getServletConfig().getServletContext().getNamedDispatcher(dispatcherName);
        try {
            /*
             *Locate - or create - the data structure object.
             */
            jwsInfo = AppclientJWSSupportInfo.getInstance();
        } catch (Throwable thr) {
            throw new ServletException(thr);
        }
    }
    
    /**
     *Delegates the request to either this servlet's specialized handling for
     *dynamic content or to the default servlet's handling for static content.
     *@param request to be handled
     *@param response to be sent
     *@throws ServletException in case of any errors
     */
    public void service(ServletRequest request, ServletResponse response) throws ServletException {
        if ( ! (request instanceof HttpServletRequest) || ! (response instanceof HttpServletResponse)) {
            throw new ServletException("Expected HttpServletRequest and HttpServletResponse but received " + request.getClass().getName() + " and " + response.getClass().getName());
        }
        
        boolean isFine = _logger.isLoggable(Level.FINE);
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        /*
         *See if there is Content info
         *available for this request.
         */
        Content content = jwsInfo.getContent(req);
        if (content == null) {
            try {
                if (isFine) {
                    _logger.fine("JWSSystemServlet: content not found for request: method=" + req.getMethod() + ", pathInfo=" + req.getPathInfo());
                }

                resp.sendError(resp.SC_NOT_FOUND, req.getPathInfo());
            } catch (Throwable thr) {
                throw new ServletException("Error attempting to return not-found response", thr);
            }
        } else {
            /*
             *If the response is static, delegate to the default servlet logic.
             *Otherwise, handle it here.
             */
            if (content instanceof StaticContent) {
                processStaticContent(req, resp, content);
            } else {
                processDynamicContent(req, resp, content);
            }
        }
        
    }
    
    /**
     *Handles requests for static content by delegating the request to
     *the default servlet.
     *@param the request to be processed
     *@param the response to be composed
     *@param the content, looked up from the data structure, that describes
     *the static content itself
     *@throws ServletException in case of any error
     */
    private void processStaticContent(HttpServletRequest req, HttpServletResponse resp, Content content) throws ServletException {
        try {
            StaticContent sc = (StaticContent) content;
            
            /*
             *Set up the wrapped request with a URI to the file to be served that
             *is relative to the app server's installation root directory.  The
             *web container has initialized this web app's docbase to the app
             *server's installation root.
             */
            HttpServletRequest wrappedRequest = new JWSSystemServletRequestWrapper(req, sc.getRelativeURI());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("JWSSystemServlet: serving static content: method=" + wrappedRequest.getMethod() + ", mapped path=" + wrappedRequest.getPathInfo());
            }
            
            /*
             * Send to the dispatcher for the default servlet.
             */
            defaultDispatcher.forward(wrappedRequest, resp);

        } catch (Throwable thr) {
            throw new ServletException("Error processing static content for path " + content.getPath(), thr);
        }
    }
    
    /**
     *Handles requests for dynamic content.
     *@param the HTTP request to be serviced
     *@param the HTTP response to be composed
     *@param the content in which placeholders should be replaced before being written as the response
     *@throws ServletException for any errors
     */
    private void processDynamicContent(HttpServletRequest req, HttpServletResponse resp, Content content) throws ServletException {
        try {
            DynamicContent dc = (DynamicContent) content; 

            /*
             *Build the Properties object with the request-based tokens and values, then
             *see if the DynamicContent that has already been retrieved already 
             *has an instance with matching content.  During that search, if this
             *is a GET request then add the text to the DynamicContent's cache
             *if it is not already there.  This helps Java Web Start manage its
             *cache on the client side and avoid retrieving documents over the
             *network that its cache already contains.  
             */
            Properties requestPlaceholders = prepareRequestPlaceholders(req, dc);

            boolean isGetMethod = req.getMethod().equals(GET_METHOD_NAME);
            
            DynamicContent.Instance instance = dc.findInstance(requestPlaceholders, isGetMethod /* if method == GET, then create if no instance */);

            /*
             *The findInstance invocation will return a null only if the request is an HTTP HEAD request
             *(to check the timestamp) and the substituted content does not 
             *already appear in the DynamicContent's cache.
             */
            if (instance != null) {
                String responseText = instance.getText();
                Date instanceTimestamp = instance.getTimestamp();
                resp.setDateHeader(LAST_MODIFIED_HEADER_NAME, instanceTimestamp.getTime());
                resp.setDateHeader(DATE_HEADER_NAME, System.currentTimeMillis());
                resp.setContentType(dc.getMimeType());
                resp.setStatus(resp.SC_OK);
                /*
                 *Only for GET should the response actually contain the content.
                 */
                if (isGetMethod) {
                    PrintWriter pw = resp.getWriter();
                    pw.println(responseText);
                    pw.flush();
                }
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("JWSSystemServlet: serving dynamic content: method=" + req.getMethod() + 
                            ", pathInfo=" + req.getPathInfo() +
                            ", queryString=" + req.getQueryString() +
                            ", isGetMethod=" + isGetMethod +
                            ", content timestamp=" + instanceTimestamp + 
                            ", content=" + responseText
                            );
                }
            } else {
                /*
                 *The Java Web Start client has sent a HEAD request for 
                 *a URL that we recognize as one we should serve, but we have no
                 *such document (yet).  Report the requested
                 *document as here with the current date/time.  That will cause
                 *Java Web Start on the client to re-request the full document
                 *using a GET.
                 */
                resp.setDateHeader(LAST_MODIFIED_HEADER_NAME, System.currentTimeMillis());
                resp.setDateHeader(DATE_HEADER_NAME, System.currentTimeMillis());
                resp.setContentType(dc.getMimeType());
                resp.setStatus(resp.SC_OK);
            }
        } catch (Throwable thr) {
            throw new ServletException("Error processing dynamic content for path " + content.getPath(), thr);
        }
    }
    
    /**
     *Initializes a Properties object with the token names and values for
     *substitution in the dynamic content template.
     *@param the incoming request
     *@return Properties object containing the token names and values
     *@throws ServletException in case of an error preparing the placeholders
     */
    private Properties prepareRequestPlaceholders(
            HttpServletRequest request,
            DynamicContent content) throws ServletException {
        Properties answer = new Properties();
        
        answer.setProperty("request.scheme", request.getScheme());
        answer.setProperty("request.host", request.getServerName());
        answer.setProperty("request.port", Integer.toString(request.getServerPort()));
        
        /*
         *Treat query parameters with the name "arg" as command line arguments to the
         *app client.
         */

        String queryString = request.getQueryString();
        StringBuilder queryStringPropValue = new StringBuilder();
        if (queryString != null && queryString.length() > 0) {
            queryStringPropValue.append("?").append(queryString);
        }
        answer.setProperty("request.web.app.context.root", NamingConventions.SYSTEM_WEBAPP_URL);
        answer.setProperty("request.path", request.getPathInfo());
        answer.setProperty("request.query.string", queryStringPropValue.toString());

        answer.setProperty("security.setting", content.getJNLPSecuritySetting());
        
        /*
         *If the content origin is a user content origin, then find out if
         *the app client jar file recorded with this origin is signed or not.
         *Then use that to set the property for substitution in the dynamic JNLP.
         */
        ContentOrigin origin = content.getOrigin();
        answer.setProperty("appclient.user.code.is.signed", Boolean.toString(content.requiresElevatedPermissions()));

        processQueryParameters(queryString, answer);

        return answer;
    }

    private void processQueryParameters(String queryString, Properties answer) throws ServletException {
        if (queryString == null) {
            queryString = "";
        }
        String [] queryParams = null;
        try {
            queryParams = URLDecoder.decode(queryString, "UTF-8").split("&");
        } catch (UnsupportedEncodingException uee) {
            throw new ServletException("Error decoding query string",  uee);
        }

        QueryParams arguments = new ArgQueryParams();
        QueryParams properties = new PropQueryParams();
        QueryParams vmArguments = new VMArgQueryParams();
        QueryParams [] paramTypes = new QueryParams[] {arguments, properties, vmArguments};

        for (String param : queryParams) {
            for (QueryParams qpType : paramTypes) {
                if (qpType.processParameter(param)) {
                    break;
                }
            }
        }
        
        answer.setProperty("request.arguments", arguments.toString());
        answer.setProperty("request.properties", properties.toString());
        answer.setProperty("request.vmargs", vmArguments.toString());
    }
        
    private abstract class QueryParams {
        private String prefix;
        
        protected QueryParams(String prefix) {
            this.prefix = prefix;
        }
        
        private boolean handles(String prefix) {
            return prefix.equals(this.prefix);
        }
        
        protected abstract void processValue(String value);
        
        public abstract String toString();
        
        public boolean processParameter(String param) {
            boolean result = false;
            int equalsSign = param.indexOf("=");
            String value = "";
            String prefix;
            if (equalsSign != -1) {
                prefix = param.substring(0, equalsSign);
            } else {
                prefix = param;
            }
            if (handles(prefix)) {
                result = true;
                if ((equalsSign + 1) < param.length()) {
                    value = param.substring(equalsSign + 1);
                }
                processValue(value);
            }
            return result;
        }
    }
    
    private class ArgQueryParams extends QueryParams {
        private StringBuilder arguments = new StringBuilder();
        
        public ArgQueryParams() {
            super(ARG_QUERY_PARAM_NAME);
        }
        
        public void processValue(String value) {
            if (value.length() == 0) {
                value = "#missing#";
            }
            arguments.append("<argument>").append(value).append("</argument>").append(lineSep);
        }
        
        public String toString() {
            return arguments.toString();
        }
    }
    
    private class PropQueryParams extends QueryParams {
        private StringBuilder properties = new StringBuilder();
        
        public PropQueryParams() {
            super(PROP_QUERY_PARAM_NAME);
        }
        
        public void processValue(String value) {
            if (value.length() > 0) {
                int equalsSign = value.indexOf('=');
                String propValue = "";
                String propName;
                if (equalsSign > 0) {
                    propName = value.substring(0, equalsSign);
                    if ((equalsSign + 1) < value.length()) {
                        propValue = value.substring(equalsSign + 1);
                    }
                    properties.append("<property name=\"" + propName + "\" value=\"" + propValue + "\"/>").append(lineSep);
                }
            }
        }
        
        public String toString() {
            return properties.toString();
        }
        
    }
    
    private class VMArgQueryParams extends QueryParams {
        private StringBuilder vmArgs = new StringBuilder();
        
        public VMArgQueryParams() {
            super(VMARG_QUERY_PARAM_NAME);
        }
        
        public void processValue(String value) {
            vmArgs.append(value).append(" ");
        }
        
        public String toString() {
            return vmArgs.length() > 0 ? " java-vm=args=\"" + vmArgs.toString() + "\"" : "";
        }
    }
    
    
}
