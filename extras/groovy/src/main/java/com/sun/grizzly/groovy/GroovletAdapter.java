/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.grizzly.groovy;

import com.sun.grizzly.http.servlet.HttpServletRequestImpl;
import com.sun.grizzly.http.servlet.HttpServletResponseImpl;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.util.http.Globals;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.servlet.ServletBinding;
import groovy.servlet.ServletCategory;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
    
/**
 * Adapter implementation that bridge Groovy with Grizzly.
 *
 * @author Martin Grebac
 */
public class GroovletAdapter extends GrizzlyAdapter /*implements Adapter*/ {
    
    private GroovyScriptEngine scriptEngine = null;
    
    protected String contextRoot = null;
    
    /**
     * The replace method to use on the matcher.
     * <pre>
     * true - replaceAll(resourceNameReplacement); (default)
     * false - replaceFirst(resourceNameReplacement);
     * </pre>
     */
    protected boolean resourceNameReplaceAll;

    /**
     * <b>Null</b> or compiled pattern matcher read from "resource.name.regex"
     *  and used in {@link AbstractHttpServlet#getResourceConnection(String)}.
     */
    protected Matcher resourceNameMatcher;
    
    /**
     * The replacement used by the resource name matcher.     
     */
    protected String resourceNameReplacement;
    
    boolean verbose = true;
    
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
        
    public String getContextRoot() {
        return contextRoot;
    }
    
    @Override
    public void service(GrizzlyRequest request, GrizzlyResponse response) {
        
        try {
            String uri = getScriptUri(request);
            String ctxRoot = getContextRoot();
            if (ctxRoot != null) {
                if (uri.startsWith(ctxRoot) && (uri.length() > ctxRoot.length())) {
                    uri = uri.substring(ctxRoot.length() + 1);
                }
            } else {
                uri = uri.substring(1);
            }

            final String scriptUri = uri;

            if (!(scriptUri.endsWith(".groovy") || (scriptUri.endsWith(".gdo")))) {
                return;
            }

            if (scriptEngine == null) {
                URL[] roots = new URL[] {
                                    new File(this.getRootFolder()).toURL(),
                                    new File(new File(this.getRootFolder()).getPath() + "/src/groovy").toURL()
                                };
                scriptEngine = new GroovyScriptEngine(roots);
            }
            
            // Set it to HTML by default
            response.setContentType("text/html");

            final Binding b = new ServletBinding(
                                    new HttpServletRequestImpl(request), 
                                    new HttpServletResponseImpl(response), 
                                    null);

            // Run the script
            try {
                Closure closure = new Closure(scriptEngine) {

                    public Object call() {
                        try {
                            return ((GroovyScriptEngine) getDelegate()).run(scriptUri, b);
                        } catch (ResourceException e) {
                            throw new RuntimeException(e);
                        } catch (ScriptException e) {
                            throw new RuntimeException(e);
                        }
                    }

                };
                GroovyCategorySupport.use(ServletCategory.class, closure);               
                /*
                 * Set reponse code 200.
                 */
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (RuntimeException runtimeException) {
                StringBuffer error = new StringBuffer("GroovyServlet Error: ");
                error.append(" script: '");
                error.append(scriptUri);
                error.append("': ");
                Throwable e = runtimeException.getCause();
                /*
                 * Null cause?!
                 */
                if (e == null) {
                    error.append(" Script processing failed.");
                    error.append(runtimeException.getMessage());
                    if (runtimeException.getStackTrace().length > 0)
                        error.append(runtimeException.getStackTrace()[0].toString());
                    System.err.println(error.toString());
                    runtimeException.printStackTrace(System.err);
                    try {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.toString());
                    } catch (IOException ex) {
                        Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
                /*
                 * Resource not found.
                 */
                if (e instanceof ResourceException) {
                    error.append(" Script not found, sending 404.");
                    System.err.println(error.toString());
                    try {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } catch (IOException ex) {
                        Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
                /*
                 * Other internal error. Perhaps syntax?! 
                 */
                error.append(e.getMessage());
                if (e.getStackTrace().length > 0)
                    error.append(e.getStackTrace()[0].toString());
                System.err.println(e.toString());
                runtimeException.printStackTrace(System.err);
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                } catch (IOException ex) {
                    Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } finally {
                try {
                    /*
                     * Finally, flush the response buffer.
                     */
                    response.flushBuffer();
                    // servletContext.log("Flushed response buffer.");
                } catch (IOException ex) {
                    Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
                }
                // servletContext.log("Flushed response buffer.");
            }
            
//        } catch (URISyntaxException ex) {
//            Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GroovletAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected String getScriptUri(GrizzlyRequest request) {
        log("Logging request class and its class loader:");
        log(" c = request.getClass() :\"" + request.getClass() + "\"");
        log(" l = c.getClassLoader() :\"" + request.getClass().getClassLoader() + "\"");
        log(" l.getClass()           :\"" + request.getClass().getClassLoader().getClass() + "\"");

        String uri = null;
        String info = null;

        // Check to see if the requested script/template source file has been the
        // target of a RequestDispatcher.include().
        uri = (String) request.getAttribute(Globals.INCLUDE_SERVLET_PATH_ATTR);
        if (uri == null) {
            uri = request.getDecodedRequestURI();
        }
        if (uri != null) {
            // Requested script/template file has been target of 
            // RequestDispatcher.include(). Its path is assembled from the relevant
            // javax.servlet.include.* request attributes and returned!
            info = (String) request.getAttribute(Globals.INCLUDE_PATH_INFO_ATTR);
            if (info != null) {
                uri += info;
            }
            File file = new File(getRootFolder(),uri);
            if (file.isDirectory()) {
                uri += "index.html";
                file = new File(file,uri);
            }
        }        
        return uri;
    }

    @Override
    public void afterService(GrizzlyRequest request, GrizzlyResponse response) throws Exception {

    }

}