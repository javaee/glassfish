/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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


package org.apache.catalina.connector;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;
import org.glassfish.web.valve.GlassFishValve;
import com.sun.grizzly.tcp.ActionCode;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.grizzly.util.buf.CharChunk;
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.buf.UEncoder;
import com.sun.grizzly.util.http.mapper.MappingData;
import com.sun.appserv.ProxyHandler;

/**
 * Implementation of a request processor which delegates the processing to a
 * Coyote processor.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.34 $ $Date: 2007/08/24 18:38:28 $
 */

public class CoyoteAdapter
    implements Adapter 
 {
    private static Logger log = Logger.getLogger(CoyoteAdapter.class.getName());

    // -------------------------------------------------------------- Constants

    protected boolean v3Enabled = 
        Boolean.valueOf(System.getProperty("v3.grizzly.useMapper", "true")).booleanValue();
    
    
    public static final int ADAPTER_NOTES = 1;

    static final String JVM_ROUTE = System.getProperty("jvmRoute");

    protected static final boolean ALLOW_BACKSLASH = 
        Boolean.valueOf(System.getProperty("com.sun.grizzly.tcp.tomcat5.CoyoteAdapter.ALLOW_BACKSLASH", "false")).booleanValue();

    private static final boolean COLLAPSE_ADJACENT_SLASHES =
        Boolean.valueOf(System.getProperty(
            "com.sun.enterprise.web.collapseAdjacentSlashes", "true")).booleanValue();

    // START CR 6309511
    /**
     * The match string for identifying a session ID parameter.
     */
    private static final String SESSION_PARAMETER =
        ";" + Globals.SESSION_PARAMETER_NAME + "=";
    // END CR 6309511

    /**
     * When mod_jk is used, the adapter must be invoked the same way 
     * Tomcat does by invoking service(...) and the afterService(...). This
     * is a hack to make it compatible with Tomcat 5|6.
     */
    private boolean compatWithTomcat = false;
    
    private String serverName = ServerInfo.getPublicServerInfo();
    
    // Make sure this value is always aligned with {@link ContainerMapper}
    // (@see com.sun.enterprise.v3.service.impl.ContainerMapper)
    private final static int MAPPING_DATA = 12;
    
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new CoyoteProcessor associated with the specified connector.
     *
     * @param connector CoyoteConnector that owns this processor
     */
    public CoyoteAdapter(Connector connector) {
        super();
        this.connector = connector;
        this.debug = connector.getDebug();
        // START GlassFish 936
        urlEncoder.addSafeCharacter('/');
        // END GlassFish 936
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The CoyoteConnector with which this processor is associated.
     */
    private Connector connector = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;

    // START GlassFish 936
    private UEncoder urlEncoder = new UEncoder();
    // END GlassFish 936

    /**
     * The string manager for this package.
     */
    protected StringManager sm =
        StringManager.getManager(Constants.Package);


    // -------------------------------------------------------- Adapter Methods


    /**
     * Service method.
     */
    @Override
    public void service(com.sun.grizzly.tcp.Request req,
                        com.sun.grizzly.tcp.Response res)
            throws Exception {

        Request request = (Request) req.getNote(ADAPTER_NOTES);
        Response response = (Response) res.getNote(ADAPTER_NOTES);
        
        // Grizzly already parsed, decoded, and mapped the request.
        // Let's re-use this info here, before firing the
        // requestStartEvent probe, so that the mapping data will be
        // available to any probe event listener via standard
        // ServletRequest APIs (such as getContextPath())
        MappingData md = (MappingData)req.getNote(MAPPING_DATA);
        if (md == null){
            v3Enabled = false;
        } else {
            v3Enabled = true;
        }
            
        if (request == null) {

            // Create objects
            request = (Request) connector.createRequest();
            request.setCoyoteRequest(req);
            response = (Response) connector.createResponse();
            response.setCoyoteResponse(res);

            // Link objects
            request.setResponse(response);
            response.setRequest(request);

            // Set as notes
            req.setNote(ADAPTER_NOTES, request);
            res.setNote(ADAPTER_NOTES, response);

            // Set query string encoding
            req.getParameters().setQueryStringEncoding
                (connector.getURIEncoding());
        }

        if (v3Enabled && !compatWithTomcat) {
            if (md != null) {
                request.setMappingData(md);
                request.updatePaths(md);
            }
        }

        try {
            doService(req, request, res, response);
        } catch (IOException e) {
            // Recycle the wrapper request and response
            request.recycle();
            response.recycle();
        } catch (Throwable t) {
            log.log(Level.SEVERE, sm.getString("coyoteAdapter.service"), t);
            // Recycle the wrapper request and response
            request.recycle();
            response.recycle();
        }
    }


    private void doService(com.sun.grizzly.tcp.Request req,
                           Request request,
                           com.sun.grizzly.tcp.Response res,
                           Response response)
            throws Exception {
        
        // START SJSAS 6331392
        // Check connector for disabled state
        if (!connector.isEnabled()) {
            String msg = sm.getString("coyoteAdapter.listenerOff",
                                      String.valueOf(connector.getPort()));
            if (log.isLoggable(Level.FINE)) {
                log.fine(msg);            
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }
        // END SJSAS 6331392

        if (connector.isXpoweredBy()) {
            response.addHeader("X-Powered-By", "Servlet/3.0");
        }


        // Parse and set Catalina and configuration specific 
        // request parameters
        if ( postParseRequest(req, request, res, response) ) {

            // START S1AS 6188932
            boolean authPassthroughEnabled = 
                connector.getAuthPassthroughEnabled();
            ProxyHandler proxyHandler = connector.getProxyHandler();
            if (authPassthroughEnabled && proxyHandler != null) {

                // START SJSAS 6397218
                if (proxyHandler.getSSLKeysize(
                        (HttpServletRequest)request.getRequest()) > 0) {
                    request.setSecure(true);
                }
                // END SJSAS 6397218

                X509Certificate[] certs = null;
                try {
                    certs = proxyHandler.getSSLClientCertificateChain(
                                request.getRequest());
                } catch (CertificateException ce) {
                    log.log(Level.SEVERE,
                            sm.getString("coyoteAdapter.proxyAuthCertError"),
                            ce);
                }
                if (certs != null) {
                    request.setAttribute(Globals.CERTIFICATES_ATTR,
                                         certs);
                }
                    
            }
            // END S1AS 6188932
            
            if (serverName != null && !serverName.isEmpty()) {
                response.addHeader("Server", serverName);
            }
                
            // Invoke the web container
            connector.requestStartEvent(request.getRequest(),
                request.getHost(), request.getContext());
            Container container = connector.getContainer();
            try {
                if (container.getPipeline().hasNonBasicValves() ||
                        container.hasCustomPipeline()) {
                    container.getPipeline().invoke(request, response);
                } else {
                    // Invoke host directly
                    Host host = request.getHost();
                    if (host == null) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        response.setDetailMessage(sm.getString(
                                "standardEngine.noHost",
                                request.getRequest().getServerName()));
                        return;
                    }
                    if (host.getPipeline().hasNonBasicValves() ||
                            host.hasCustomPipeline()) {
                        host.getPipeline().invoke(request, response);
                    } else {
                        GlassFishValve hostValve = host.getPipeline().getBasic();
                        hostValve.invoke(request, response);
                        // Error handling
                        hostValve.postInvoke(request, response); 
                    }
                }
            } finally {
                connector.requestEndEvent(request.getRequest(),
                    request.getHost(), request.getContext(),
                    response.getStatus());
            }
        }

        /* GlassFish Issue 798
        response.finishResponse();
        req.action( ActionCode.ACTION_POST_REQUEST , null);
         */
        // START GlassFish Issue 798
        if (compatWithTomcat) {
            afterService(req, res);
        }
        // END GlassFish Issue 798    
    }

    // START GlassFish Issue 798
    /**
     * Finish the response and close the connection based on the connection
     * header.
     */
    @Override
    public void afterService(com.sun.grizzly.tcp.Request req,
                             com.sun.grizzly.tcp.Response res)
            throws Exception{

        Request request = (Request) req.getNote(ADAPTER_NOTES);
        Response response = (Response) res.getNote(ADAPTER_NOTES);
        
        if ( request == null || response == null) return;
        
        try{
            if (!res.isSuspended()){
                response.finishResponse();
                req.action( ActionCode.ACTION_POST_REQUEST , null);
            } else {
                if (request != null) {
                    request.onAfterService();
                }
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, sm.getString("coyoteAdapter.service"), t);
        } finally {
            if (!res.isSuspended()){
                // Recycle the wrapper request and response
                request.recycle();
                response.recycle();
            }
        }
    }
    // END GlassFish Issue 798
    // ------------------------------------------------------ Protected Methods


    /**
     * Parse additional request parameters.
     */
    protected boolean postParseRequest(com.sun.grizzly.tcp.Request req,
                                       Request request,
                                       com.sun.grizzly.tcp.Response res,
                                       Response response)
        throws Exception {
        // XXX the processor needs to set a correct scheme and port prior to this point, 
        // in ajp13 protocols dont make sense to get the port from the connector..
        // XXX the processor may have set a correct scheme and port prior to this point, 
        // in ajp13 protocols dont make sense to get the port from the connector...
        // otherwise, use connector configuration
        if (! req.scheme().isNull()) {
            // use processor specified scheme to determine secure state
            request.setSecure(req.scheme().equals("https"));
        } else {
            // use connector scheme and secure configuration, (defaults to
            // "http" and false respectively)
            req.scheme().setString(connector.getScheme());
            request.setSecure(connector.getSecure());
        }

        // FIXME: the code below doesnt belongs to here, 
        // this is only have sense 
        // in Http11, not in ajp13..
        // At this point the Host header has been processed.
        // Override if the proxyPort/proxyHost are set 
        String proxyName = connector.getProxyName();
        int proxyPort = connector.getProxyPort();
        if (proxyPort != 0) {
            req.setServerPort(proxyPort);
        }
        if (proxyName != null) {
            req.serverName().setString(proxyName);
        }

        // URI decoding
        MessageBytes decodedURI = req.decodedURI();
        if (compatWithTomcat || !v3Enabled) {           
            decodedURI.duplicate(req.requestURI());
            try {
              req.getURLDecoder().convert(decodedURI, false);
            } catch (IOException ioe) {
              res.setStatus(400);
              res.setMessage("Invalid URI: " + ioe.getMessage());
              return false;
            }

            /* GlassFish Issue 2339
            // Normalize decoded URI
            if (!normalize(req.decodedURI())) {
                res.setStatus(400);
                res.setMessage("Invalid URI");
                return false;
            }
            */

            // Set the remote principal
            String principal = req.getRemoteUser().toString();
            if (principal != null) {
                request.setUserPrincipal(new CoyotePrincipal(principal));
            }

            // Set the authorization type
            String authtype = req.getAuthType().toString();
            if (authtype != null) {
                request.setAuthType(authtype);
            }

            /* CR 6309511
            // URI character decoding
            convertURI(decodedURI, request);

            // Parse session Id
            parseSessionId(req, request);
             */
            // START CR 6309511
            // URI character decoding
            request.convertURI(decodedURI);

            // START GlassFish Issue 2339
            // Normalize decoded URI
            if (!normalize(decodedURI)) {
                res.setStatus(400);
                res.setMessage("Invalid URI");
                return false;
            }
            // END GlassFish Issue 2339
        }
        // END CR 6309511

        /*
         * Remove any parameters from the URI, so they won't be considered
         * by the mapping algorithm, and save them in a temporary CharChunk,
         * so that any session id param may be parsed once the target
         * context, which may use a custom session parameter name, has been
         * identified
         */
        CharChunk uriParamsCC = request.getURIParams();
        CharChunk uriCC = decodedURI.getCharChunk();
        int semicolon = uriCC.indexOf(';');
        if (semicolon > 0) {
            uriParamsCC.setChars(uriCC.getBuffer(), semicolon,
                uriCC.getEnd() - semicolon);
            decodedURI.setChars(uriCC.getBuffer(), uriCC.getStart(),
                semicolon);
        }
 
        if (compatWithTomcat || !v3Enabled) {
            /*mod_jk*/
            connector.getMapper().map(req.serverName(), decodedURI, 
                                  request.getMappingData());
            MappingData md = request.getMappingData();
            req.setNote(MAPPING_DATA, md);
            request.updatePaths(md);
        }

        Context ctx = (Context) request.getMappingData().context;

        // Parse session id
        if (ctx != null && !uriParamsCC.isNull()) {
            String sessionParam = SESSION_PARAMETER;
            if (ctx.isSessionCookieConfigInitialized() &&
                    ctx.getSessionCookieName() != null) {
                sessionParam = ";" + ctx.getSessionCookieName() + "=";
            }
            request.parseSessionId(sessionParam, uriParamsCC);
        }

        // START GlassFish 1024
        request.setDefaultContext(request.getMappingData().isDefaultContext);
        // END GlassFish 1024

        // START SJSAS 6253524
        // request.setContext((Context) request.getMappingData().context);
        // END SJSAS 6253524
        // START SJSAS 6253524
        request.setContext(ctx);
        // END SJSAS 6253524

        request.setWrapper((Wrapper) request.getMappingData().wrapper);

        // Filter trace method
        if (!connector.getAllowTrace() 
                && req.method().equalsIgnoreCase("TRACE")) {
            Wrapper wrapper = request.getWrapper();
            String header = null;
            if (wrapper != null) {
                String[] methods = wrapper.getServletMethods();
                if (methods != null) {
                    for (int i=0; i<methods.length; i++) {
                        // Exclude TRACE from methods returned in Allow header
                        if ("TRACE".equals(methods[i])) {
                            continue;
                        }
                        if (header == null) {
                            header = methods[i];
                        } else {
                            header += ", " + methods[i];
                        }
                    }
                }
            }                               
            res.setStatus(405);
            res.addHeader("Allow", header);
            res.setMessage("TRACE method is not allowed");
            return false;
        }

        // Possible redirect
        MessageBytes redirectPathMB = request.getMappingData().redirectPath;
        // START SJSAS 6253524
        // if (!redirectPathMB.isNull()) {
        // END SJSAS 6253524
        // START SJSAS 6253524
        if (!redirectPathMB.isNull()
            && (!ctx.hasAdHocPaths()
                || (ctx.getAdHocServletName(((HttpServletRequest)
                        request.getRequest()).getServletPath()) == null))) {
        // END SJSAS 6253524
            String redirectPath = redirectPathMB.toString();
            String query = request.getQueryString();
            if (request.isRequestedSessionIdFromURL()) {
                // This is not optimal, but as this is not very common, it
                // shouldn't matter
                redirectPath = redirectPath + ";" + Globals.SESSION_PARAMETER_NAME + "=" 
                    + request.getRequestedSessionId();
            }            
            // START GlassFish 936
            redirectPath = urlEncoder.encodeURL(redirectPath);
            // END GlassFish 936
            if (query != null) {
                // This is not optimal, but as this is not very common, it
                // shouldn't matter
                redirectPath = redirectPath + "?" + query;
            }

            // START CR 6590921
            boolean authPassthroughEnabled = 
                connector.getAuthPassthroughEnabled();
            ProxyHandler proxyHandler = connector.getProxyHandler();
            if (authPassthroughEnabled && proxyHandler != null) {

                if (proxyHandler.getSSLKeysize(
                        (HttpServletRequest)request.getRequest()) > 0) {
                    request.setSecure(true);
                }
            }
            // END CR 6590921

            // Issue a permanent redirect
            response.sendRedirect(redirectPath, false);

            return false;
        }

        // Parse session Id
        /* CR 6309511
        parseSessionCookiesId(req, request);
         */
        // START CR 6309511
        request.parseSessionCookiesId();
        // END CR 6309511

        // START SJSAS 6346226
        request.parseJrouteCookie();
        // END SJSAS 6346226

        return true;
    }


    /**
     * Normalize URI.
     * <p>
     * This method normalizes "\", "//", "/./" and "/../". This method will
     * return false when trying to go above the root, or if the URI contains
     * a null byte.
     * 
     * @param uriMB URI to be normalized
     */
    public static boolean normalize(MessageBytes uriMB) {

        int type = uriMB.getType();
        if (type == MessageBytes.T_CHARS) {
            return normalizeChars(uriMB);
        } else {
            return normalizeBytes(uriMB);
        }
    }


    private static boolean normalizeBytes(MessageBytes uriMB) {

        ByteChunk uriBC = uriMB.getByteChunk();
        byte[] b = uriBC.getBytes();
        int start = uriBC.getStart();
        int end = uriBC.getEnd();

        // An empty URL is not acceptable
        if (start == end)
            return false;

        // URL * is acceptable
        if ((end - start == 1) && b[start] == (byte) '*')
          return true;

        int pos = 0;
        int index = 0;

        // Replace '\' with '/'
        // Check for null byte
        for (pos = start; pos < end; pos++) {
            if (b[pos] == (byte) '\\') {
                if (ALLOW_BACKSLASH) {
                    b[pos] = (byte) '/';
                } else {
                    return false;
                }
            }
            if (b[pos] == (byte) 0) {
                return false;
            }
        }

        // The URL must start with '/'
        if (b[start] != (byte) '/') {
            return false;
        }

        // Replace "//" with "/"
        if (COLLAPSE_ADJACENT_SLASHES) {
            for (pos = start; pos < (end - 1); pos++) {
                if (b[pos] == (byte) '/') {
                    while ((pos + 1 < end) && (b[pos + 1] == (byte) '/')) {
                        copyBytes(b, pos, pos + 1, end - pos - 1);
                        end--;
                    }
                }
            }
        }

        // If the URI ends with "/." or "/..", then we append an extra "/"
        // Note: It is possible to extend the URI by 1 without any side effect
        // as the next character is a non-significant WS.
        if (((end - start) > 2) && (b[end - 1] == (byte) '.')) {
            if ((b[end - 2] == (byte) '/') 
                || ((b[end - 2] == (byte) '.') 
                    && (b[end - 3] == (byte) '/'))) {
                b[end] = (byte) '/';
                end++;
            }
        }

        uriBC.setEnd(end);

        index = 0;

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            index = uriBC.indexOf("/./", 0, 3, index);
            if (index < 0)
                break;
            copyBytes(b, start + index, start + index + 2, 
                      end - start - index - 2);
            end = end - 2;
            uriBC.setEnd(end);
        }

        index = 0;

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            index = uriBC.indexOf("/../", 0, 4, index);
            if (index < 0)
                break;
            // Prevent from going outside our context
            if (index == 0)
                return false;
            int index2 = -1;
            for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos --) {
                if (b[pos] == (byte) '/') {
                    index2 = pos;
                }
            }
            copyBytes(b, start + index2, start + index + 3,
                      end - start - index - 3);
            end = end + index2 - index - 3;
            uriBC.setEnd(end);
            index = index2;
        }

        uriBC.setBytes(b, start, end);

        return true;

    }


    private static boolean normalizeChars(MessageBytes uriMB) {

        CharChunk uriCC = uriMB.getCharChunk();
        char[] c = uriCC.getChars();
        int start = uriCC.getStart();
        int end = uriCC.getEnd();

        // URL * is acceptable
        if ((end - start == 1) && c[start] == (char) '*')
          return true;

        int pos = 0;
        int index = 0;

        // Replace '\' with '/'
        // Check for null char
        for (pos = start; pos < end; pos++) {
            if (c[pos] == (char) '\\') {
                if (ALLOW_BACKSLASH) {
                    c[pos] = (char) '/';
                } else {
                    return false;
                }
            }
            if (c[pos] == (char) 0) {
                return false;
            }
        }

        // The URL must start with '/'
        if (c[start] != (char) '/') {
            return false;
        }

        // Replace "//" with "/"
        if (COLLAPSE_ADJACENT_SLASHES) {
            for (pos = start; pos < (end - 1); pos++) {
                if (c[pos] == (char) '/') {
                    while ((pos + 1 < end) && (c[pos + 1] == (char) '/')) {
                        copyChars(c, pos, pos + 1, end - pos - 1);
                        end--;
                    }
                }
            }
        }	

        // If the URI ends with "/." or "/..", then we append an extra "/"
        // Note: It is possible to extend the URI by 1 without any side effect
        // as the next character is a non-significant WS.
        if (((end - start) > 2) && (c[end - 1] == (char) '.')) {
            if ((c[end - 2] == (char) '/') 
                || ((c[end - 2] == (char) '.') 
                    && (c[end - 3] == (char) '/'))) {
                c[end] = (char) '/';
                end++;
            }
        }

        uriCC.setEnd(end);

        index = 0;

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            index = uriCC.indexOf("/./", 0, 3, index);
            if (index < 0)
                break;
            copyChars(c, start + index, start + index + 2, 
                      end - start - index - 2);
            end = end - 2;
            uriCC.setEnd(end);
        }

        index = 0;

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            index = uriCC.indexOf("/../", 0, 4, index);
            if (index < 0)
                break;
            // Prevent from going outside our context
            if (index == 0)
                return false;
            int index2 = -1;
            for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos --) {
                if (c[pos] == (char) '/') {
                    index2 = pos;
                }
            }
            copyChars(c, start + index2, start + index + 3,
                      end - start - index - 3);
            end = end + index2 - index - 3;
            uriCC.setEnd(end);
            index = index2;
        }

        uriCC.setChars(c, start, end);

        return true;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Copy an array of bytes to a different position. Used during 
     * normalization.
     */
    protected static void copyBytes(byte[] b, int dest, int src, int len) {
        for (int pos = 0; pos < len; pos++) {
            b[pos + dest] = b[pos + src];
        }
    }


    /**
     * Copy an array of chars to a different position. Used during 
     * normalization.
     */
    private static void copyChars(char[] c, int dest, int src, int len) {
        for (int pos = 0; pos < len; pos++) {
            c[pos + dest] = c[pos + src];
        }
    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        log.info( message );
    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {
        log.log(Level.SEVERE, message, throwable);
    }


     /**
      * Character conversion of the a US-ASCII MessageBytes.
      */
    /* CR 6309511
     protected void convertMB(MessageBytes mb) {
 
        // This is of course only meaningful for bytes
        if (mb.getType() != MessageBytes.T_BYTES)
            return;
        
        ByteChunk bc = mb.getByteChunk();
        CharChunk cc = mb.getCharChunk();
        cc.allocate(bc.getLength(), -1);

        // Default encoding: fast conversion
        byte[] bbuf = bc.getBuffer();
        char[] cbuf = cc.getBuffer();
        int start = bc.getStart();
        for (int i = 0; i < bc.getLength(); i++) {
            cbuf[i] = (char) (bbuf[i + start] & 0xff);
        }
        mb.setChars(cbuf, 0, bc.getLength());
   
     }
     */

    
    // START SJSAS 6349248
    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {
        if ( connector != null && connector.getContainer() != null) {
            try{
                ((ContainerBase)connector.getContainer())
                    .fireContainerEvent(type,data);
            } catch (Throwable t){
                log.log(Level.SEVERE, sm.getString("coyoteAdapter.service"), t);
            }
        }
    }
    // END SJSAS 6349248

    
    /**
     * Return true when an instance is executed the same way it does in Tomcat.
     */
    public boolean isCompatWithTomcat() {
        return compatWithTomcat;
    }

    
    /**
     * <tt>true</tt> if this class needs to be compatible with Tomcat
     * Adapter class. Since Tomcat Adapter implementation doesn't support 
     * the afterService method, the afterService method must be invoked 
     * inside the service method.
     */
    public void setCompatWithTomcat(boolean compatWithTomcat) {
        this.compatWithTomcat = compatWithTomcat;

        // Add server header
        if (compatWithTomcat){
            serverName = "Apache/" + serverName;
        } else {
            // Recalculate.
            serverName = ServerInfo.getPublicServerInfo();
        }
    }


    /**
     * Gets the port of this CoyoteAdapter.
     *
     * @return the port of this CoyoteAdapter
     */
    public int getPort() {
        return connector.getPort();
    }       
}
