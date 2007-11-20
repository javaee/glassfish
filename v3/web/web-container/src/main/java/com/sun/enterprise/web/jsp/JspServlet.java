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

package com.sun.enterprise.web.jsp;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.lang.RuntimePermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.jasper.EmbededServletOptions;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JspEngineContext;
import org.apache.jasper.compiler.JspMangler;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.jasper.servlet.JasperLoader;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.logging.DefaultLogger;

/**
 * This is an iPlanet adaptation of the Apache Jasper JSPServlet.
 * This servlet has several performance enhancements over the Apache
 * JspServlet. These include:
 * - Reducing the overall number of file stats per request
 * - Checking for JSP modifications based on a reload interval
 * - Caching compilation exceptions and recompiling the JSP only when
 *   it is modified
 */
public class JspServlet extends HttpServlet {
    protected ServletContext context = null;
    protected Map jsps = null;
    protected ServletConfig config;
    protected Options options;
    protected URLClassLoader parentClassLoader;
    private PermissionCollection permissionCollection = null;
    private CodeSource codeSource = null;

    // Time in millisecs to check for changes in jsps to force recompilation
    private long reloadInterval = 0L;

    // if this flag is false, the JSPs are not checked for modifications
    // and are never recompiled
    private boolean checkJSPmods = true;

    // flag for whether debug messages need to be logged
    private boolean debugLogEnabled = false;

    // directory under which to generate the servlets
    String outputDir = null;

    // urls used by JasperLoader to load generated jsp class files
    URL[] loaderURLs = null;

    static boolean firstTime = true;

    public void init(ServletConfig config)
        throws ServletException {

        super.init(config);
        this.config = config;
        this.context = config.getServletContext();
        
        Constants.jasperLog = new DefaultLogger(this.context);
        Constants.jasperLog.setName("JASPER_LOG");
        Constants.jasperLog.setTimestamp("false");
        Constants.jasperLog.setVerbosityLevel(
                   config.getInitParameter("logVerbosityLevel"));

        debugLogEnabled = Constants.jasperLog.matchVerbosityLevel(Logger.DEBUG);

        // reload-interval (specified in seconds) is the interval at which
        // JSP files are checked for modifications. Values that have 'special'
        // significance are: 
        //   0 : Check JSPs for modifications on every request 
        //  -1 : do not check for JSP modifications and disable recompilation
        String interval = config.getInitParameter("reload-interval");
        if (interval != null) {
            try {
                this.reloadInterval = Integer.parseInt(interval) * 1000;
                if (this.reloadInterval < 0) {
                    checkJSPmods = false;
                    Constants.message("jsp.message.recompile.disabled", 
                                      Logger.INFORMATION );
                } else if (this.reloadInterval > 0) {
                    Constants.message("jsp.message.reload.interval", 
                              new Object[] {interval}, Logger.INFORMATION );
                }
            } catch (NumberFormatException nfe) {
                Constants.message("jsp.warning.interval.invalid", 
                              new Object[] {interval}, Logger.WARNING );
            }
        }

        // In case of checking JSP for mods, use a HashMap instead of a
        // Hashtable since we anyway synchronize on all accesses to the jsp
        // wrappers for the sake of ref counting, so this avoids double
        // synchronization
        if (checkJSPmods)
            jsps = new HashMap();
        else
            jsps = new Hashtable();

        options = new EmbededServletOptions(config, context);

        outputDir = options.getScratchDir().toString();

        // set the loader urls to the output dir since that is where the
        // java classes corresponding to the jsps can be found
        File f = new File(outputDir);

        // If the toplevel output directory does not exist, then
        // create it at this point before adding it to the classloader path
        // If the directory does not exist when adding to the classloader,
        // the classloader has problems loading the classes later
        if (f.exists() == false) {
            f.mkdirs();
        }
        
        loaderURLs = new URL[1];
        try {
            loaderURLs[0] = f.toURL();
        } catch(MalformedURLException mfe) {
            throw new ServletException(mfe);
        }

        // Get the parent class loader. The servlet container is responsible
        // for providing a URLClassLoader for the web application context
        // the JspServlet is being used in.
        parentClassLoader =
            (URLClassLoader) Thread.currentThread().getContextClassLoader();
        if (parentClassLoader == null)
            parentClassLoader = (URLClassLoader)this.getClass().getClassLoader();
        String loaderString = "<none>";
        if (parentClassLoader != null)
            loaderString = parentClassLoader.toString();

        if (debugLogEnabled)
            Constants.message("jsp.message.parent_class_loader_is",
                              new Object[] {loaderString}, Logger.DEBUG);

        // Setup the PermissionCollection for this web app context
        // based on the permissions configured for the root of the
        // web app context directory, then add a file read permission
        // for that directory.
        Policy policy = Policy.getPolicy();
        if( policy != null ) {
            try {          
                // Get the permissions for the web app context
                String contextDir = context.getRealPath("/");
                if( contextDir == null )
                    contextDir = outputDir;
                URL url = new URL("file:" + contextDir);
                codeSource = new CodeSource(url,null);
                permissionCollection = policy.getPermissions(codeSource);
                // Create a file read permission for web app context directory
                if (contextDir.endsWith(File.separator))
                    contextDir = contextDir + "-";
                else
                    contextDir = contextDir + File.separator + "-";
                permissionCollection.add( new FilePermission(contextDir,"read") );
                // Allow the JSP to access org.apache.jasper.runtime.HttpJspBase
                permissionCollection.add( new RuntimePermission(
                    "accessClassInPackage.org.apache.jasper.runtime") );
                if (parentClassLoader instanceof URLClassLoader) {
                    URL [] urls = parentClassLoader.getURLs();
                    String jarUrl = null;
                    String jndiUrl = null;
                    for (int i=0; i<urls.length; i++) {
                        if (jndiUrl == null && urls[i].toString().startsWith("jndi:") ) {
                            jndiUrl = urls[i].toString() + "-";
                        }
                        if (jarUrl == null && urls[i].toString().startsWith("jar:jndi:") ) {
                            jarUrl = urls[i].toString();
                            jarUrl = jarUrl.substring(0,jarUrl.length() - 2);
                            jarUrl = jarUrl.substring(0,jarUrl.lastIndexOf('/')) + "/-";
                        }
                    }
                    if (jarUrl != null) {
                        permissionCollection.add( new FilePermission(jarUrl,"read") );
                        permissionCollection.add( new FilePermission(jarUrl.substring(4),"read") );
                    }
                    if (jndiUrl != null)
                        permissionCollection.add( new FilePermission(jndiUrl,"read") );
                }
            } catch(MalformedURLException mfe) {}
        }

        if (firstTime) {
            firstTime = false;
            if( System.getSecurityManager() != null ) {
                // Make sure classes needed at runtime by a JSP servlet
                // are already loaded by the class loader so that we
                // don't get a defineClassInPackage security exception.
                String apacheBase = "org.apache.jasper.";
                String iplanetBase = "com.sun.enterprise.web.jsp.";
                try {
                    parentClassLoader.loadClass( apacheBase +
                        "runtime.JspFactoryImpl$PrivilegedGetPageContext");
                    parentClassLoader.loadClass( apacheBase +
                        "runtime.JspFactoryImpl$PrivilegedReleasePageContext");
                    parentClassLoader.loadClass( apacheBase +
                        "runtime.JspRuntimeLibrary");
                    parentClassLoader.loadClass( apacheBase +
                        "runtime.JspRuntimeLibrary$PrivilegedIntrospectHelper");
                    parentClassLoader.loadClass( apacheBase +
                        "runtime.ServletResponseWrapperInclude");
                    this.getClass().getClassLoader().loadClass( iplanetBase +
                        "JspServlet$JspServletWrapper");
                } catch (ClassNotFoundException ex) {
                    Constants.jasperLog.log(
                        Constants.getString("jsp.message.preload.failure"),
                        ex, Logger.WARNING);
                }
            }
            Constants.message("jsp.message.scratch.dir.is", 
                              new Object[] {outputDir}, Logger.INFORMATION );
            Constants.message("jsp.message.dont.modify.servlets", Logger.INFORMATION);
            JspFactory.setDefaultFactory(new JspFactoryImpl());
        }
    }

    public void service(HttpServletRequest request, 
                        HttpServletResponse response)
        throws ServletException, IOException {
        try {
            String jspUri;
            String includeUri 
                = (String) request.getAttribute(Constants.INC_SERVLET_PATH);

            if (includeUri == null)
                jspUri = request.getServletPath();
            else
                jspUri = includeUri;

            String jspFile = (String) request.getAttribute(Constants.JSP_FILE);
            if (jspFile != null)
                jspUri = jspFile;

            if (debugLogEnabled) {
                Logger jasperLog = Constants.jasperLog;
                jasperLog.log("JspEngine --> "+jspUri);
                jasperLog.log("   ServletPath: "+request.getServletPath());
                jasperLog.log("      PathInfo: "+request.getPathInfo());
                jasperLog.log("      RealPath: "+context.getRealPath(jspUri));
                jasperLog.log("    RequestURI: "+request.getRequestURI());
                jasperLog.log("   QueryString: "+request.getQueryString());
            }

            serviceJspFile(request, response, jspUri);

        } catch (RuntimeException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }

    /**
     * This is the main service function which creates the wrapper, loads
     * the JSP if not loaded, checks for JSP modifications if specified,
     * recompiles the JSP if needed and finally calls the service function
     * on the wrapper.
     */
    private void serviceJspFile(HttpServletRequest request, 
                                HttpServletResponse response, String jspUri)
        throws ServletException, IOException {

        JspServletWrapper wrapper = null;
        try {
            if (checkJSPmods) {
                // this increments the refcount
                wrapper = getWrapper(jspUri);
                if (wrapper == null) {
                    // ensure that only one thread creates the wrapper
                    synchronized (this) {
                        wrapper = getWrapper(jspUri);
                        if (wrapper == null) {
                            // create a new wrapper and load the jsp inside it
                            wrapper = new JspServletWrapper(jspUri);
                            wrapper.loadJSP(request, response);

                            // add the new wrapper to the map, this increments
                            // the refcount as well
                            putWrapper(jspUri, wrapper);
                        }
                    }
                } else if (wrapper.isJspFileModified()) {
                    // create a new wrapper and load the jsp inside it
                    JspServletWrapper newWrapper =
                                      new JspServletWrapper(jspUri);
                    newWrapper.loadJSP(request, response);

                    // add the new wrapper to the map, this increments the
                    // refcount as well
                    putWrapper(jspUri, newWrapper);

                    // decrement the refcount on the old wrapper
                    releaseWrapper(wrapper);
                    wrapper = newWrapper;
                }
            } else {
                wrapper = (JspServletWrapper) jsps.get(jspUri);
                if (wrapper == null) {
                    // ensure that only one thread creates the wrapper
                    synchronized (this) {
                        wrapper = (JspServletWrapper) jsps.get(jspUri);
                        if (wrapper == null) {
                            // create a new wrapper and load the jsp inside it
                            wrapper = new JspServletWrapper(jspUri);
                            wrapper.loadJSP(request, response);

                            // add the new wrapper to the map
                            jsps.put(jspUri, wrapper);
                        }
                    }
                }
            }

            // throw any compile exception generated during compilation
            JasperException compileException = wrapper.getCompileException();
            if (compileException != null)
                throw compileException;

            // service the request if it is not a precompile request
            if (!preCompile(request))
                wrapper.service(request, response);

        } catch (FileNotFoundException ex) {
            // remove the wrapper from the map. In the case where we are not
            // checking for JSP mods, the wrapper would never have been in
            // the map since the exception would be thrown in loadJSP
            if (checkJSPmods)
                removeWrapper(jspUri);

            String includeRequestUri = (String)
                request.getAttribute("javax.servlet.include.request_uri");
            if (includeRequestUri != null) {
                // This file was included. Throw an exception as
                // a response.sendError() will be ignored by the
                // servlet engine.
                throw new ServletException(ex);
            } else {
                try {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                                       ex.getMessage());
                } catch (IllegalStateException ise) {
                    Constants.jasperLog.log(Constants.getString
                                            ("jsp.error.file.not.found",
                                             new Object[] {ex.getMessage()}),
                                             ex, Logger.ERROR);
                }
            }
        } finally {
            // decrement the refcount even in case of an exception
            if (checkJSPmods)
                releaseWrapper(wrapper);
        }
    }

    /**
     * The following methods allow synchronized access to the jsps
     * map as well and perform refcounting on the wrappers as well.
     * These methods are called only when we check for JSP modifications
     */
    private synchronized JspServletWrapper getWrapper(String jspUri) {
        JspServletWrapper wrapper = (JspServletWrapper) jsps.get(jspUri);
        if (wrapper != null)
            wrapper.incrementRefCount();
        return wrapper;
    }

    private synchronized void releaseWrapper(JspServletWrapper wrapper) {
        if (wrapper != null)
            wrapper.decrementRefCount();
    }

    private synchronized void putWrapper(String jspUri,
                                         JspServletWrapper wrapper) {
        wrapper.incrementRefCount();
        JspServletWrapper replaced =
                          (JspServletWrapper)jsps.put(jspUri, wrapper);

        // flag the wrapper that was replaced for destruction
        if (replaced != null)
            replaced.tryDestroy();
    }

    private synchronized void removeWrapper(String jspUri) {
        JspServletWrapper removed = (JspServletWrapper)jsps.remove(jspUri);

        // flag the wrapper that was removed for destruction
        if (removed != null)
            removed.tryDestroy();
    }

    /**
     * <p>Look for a <em>precompilation request</em> as described in
     * Section 8.4.2 of the JSP 1.2 Specification. <strong>WARNING</strong>
     * we cannot use <code>request.getParameter()</code> for this, because
     * that will trigger parsing all of the request parameters, and not give
     * a servlet the opportunity to call
     * <code>request.setCharacterEncoding()</code> first.</p>
     *
     * @param request The servlet requset we are processing
     *
     * @exception ServletException if an invalid parameter value for the
     *  <code>jsp_precompile</code> parameter name is specified
     */
    boolean preCompile(HttpServletRequest request) 
        throws ServletException {

        String queryString = request.getQueryString();
        if (queryString == null)
            return (false);
        int start = queryString.indexOf(Constants.PRECOMPILE);
        if (start < 0)
            return (false);
        queryString =
            queryString.substring(start + Constants.PRECOMPILE.length());
        if (queryString.length() == 0)
            return (true);             // ?jsp_precompile
        if (queryString.startsWith("&"))
            return (true);             // ?jsp_precompile&foo=bar...
        if (!queryString.startsWith("="))
            return (false);            // part of some other name or value
        int limit = queryString.length();
        int ampersand = queryString.indexOf("&");
        if (ampersand > 0)
            limit = ampersand;
        String value = queryString.substring(1, limit);
        if (value.equals("true"))
            return (true);             // ?jsp_precompile=true
        else if (value.equals("false"))
            return (true);             // ?jsp_precompile=false
        else
            throw new ServletException("Cannot have request parameter " +
                                       Constants.PRECOMPILE + " set to " +
                                       value);
    }

    public void destroy() {
        if (Constants.jasperLog != null)
            Constants.jasperLog.log("JspServlet.destroy()", Logger.INFORMATION);

        // ensure that only one thread destroys the jsps
        synchronized (this) {
            Iterator iter = jsps.values().iterator();
            while (iter.hasNext())
                ((JspServletWrapper)iter.next()).destroy();

            jsps.clear();
        }
    }

    /**
     * This is an embedded class within the JspServlet. Each JSP uri is
     * associated with a separate wrapper class.
     */
    class JspServletWrapper {
        String jspUri;
        File jspFile = null;
        boolean jspFileExists = true;

        String jspClassName = null;
        Class servletClass = null;
        Servlet theServlet = null;

        // used for reference counting
        int refCount = 0;
        boolean markedForDestroy = false;

        URLClassLoader loader = null;
        
        // A volatile on a long guarantees atomic read/write
        volatile long lastCheckedTime = 0L;
        volatile long jspLastModifiedTime = 0L;

        // cached compile exception
        JasperException compileException = null;

        JspServletWrapper(String jspUri)
            throws ServletException, FileNotFoundException {
            this.jspUri = jspUri;

            String jspFileName = context.getRealPath(jspUri);
            if (jspFileName == null)
                throw new FileNotFoundException(jspUri);

            jspFile = new File(jspFileName);
            jspFileExists = jspFile.exists();
            if (checkJSPmods && !jspFileExists)
                throw new FileNotFoundException(jspUri);

            JspMangler mangler = new JspMangler(jspUri, outputDir);
            this.jspClassName = mangler.getPackageName() + "." + 
                                mangler.getClassName();
        }

        public void service(HttpServletRequest request, 
                            HttpServletResponse response)
            throws ServletException, IOException {

            if (theServlet instanceof SingleThreadModel) {
                // sync on the wrapper so that the freshness
                // of the page is determined right before servicing
                synchronized (this) {
                    theServlet.service(request, response);
                }
            } else {
                theServlet.service(request, response);
            }
        }

        /**
         * this function checks once every reload interval whether the
         * JSP file has been modified. Note that this function also sets
         * jspLastModifiedTime if the file has been modified and hence
         * is not idempotent.
         */
        private boolean isJspFileModified()
            throws FileNotFoundException {

            boolean res = false;

            long currTime = System.currentTimeMillis();
            if (currTime >= (reloadInterval + lastCheckedTime)) {

                long lastModTime = jspFile.lastModified();

                // check if jsp file exists
                if (lastModTime == 0L)
                    throw new FileNotFoundException(jspUri);

                // check if jsp file has been modified
                if (lastModTime != jspLastModifiedTime) {
                    // ensure that only one thread sets the jspLastModifiedTime
                    synchronized (this) {
                        if (lastModTime != jspLastModifiedTime) {
                            // set the last modification time so that the jsp
                            // is not considered to be outdated anymore
                            jspLastModifiedTime = lastModTime;
                            res = true;
                        }
                    }
                }

                // update the time the jsp file was checked for being outdated
                lastCheckedTime = currTime;
            }
            return res;
        }

        /**
         * This function compiles the JSP if necessary and loads it.
         */
        private void loadJSP(HttpServletRequest req,
                             HttpServletResponse res) 
            throws ServletException, FileNotFoundException {

            if (!checkJSPmods && !jspFileExists) {
                // Check if the JSP can be loaded in case it has been
                // precompiled
                try {
                    loadAndInit();
                } catch (JasperException ex) {
                    Constants.jasperLog.log(ex.getMessage(), ex.getRootCause(),
                                            Logger.INFORMATION);
                    throw new FileNotFoundException(jspUri);
                }
                return;
            }

            // First try context attribute; if that fails then use the 
            // classpath init parameter. 
            String classpath =
                  (String) context.getAttribute(Constants.SERVLET_CLASSPATH);

            if (debugLogEnabled)
                Constants.message("jsp.message.context.classpath", 
                                  new Object[] {
                                      classpath == null ? "<none>" : classpath
                                  }, Logger.DEBUG);

            JspCompilationContext ctxt = new JspEngineContext(parentClassLoader,
                                                  classpath, context, jspUri,
                                                  false, options,
                                                  req, res);
            Compiler compiler = ctxt.createCompiler();

            if (checkJSPmods) {
                // set the time that the jsp file has last been checked for
                // being outdated and the JSP last mod time
                lastCheckedTime = System.currentTimeMillis();
                jspLastModifiedTime = jspFile.lastModified();
            }

            // compile and load the JSP
            try {
                compiler.compile();
                loadAndInit();
            } catch (JasperException ex) {
                compileException = ex;
            } catch (FileNotFoundException ex) {
                compiler.removeGeneratedFiles();
                throw ex;
            } catch (Exception ex) {
                compileException = new JasperException(
                       Constants.getString("jsp.error.unable.compile"), ex);
            }
        }

        private void loadAndInit()
            throws JasperException, ServletException {

            try {
                loader = new JasperLoader(loaderURLs, jspClassName,
                                          parentClassLoader,
                                          permissionCollection,
                                          codeSource);
                servletClass = loader.loadClass(jspClassName);
            } catch (ClassNotFoundException cnfe) {
                throw new JasperException(
                    Constants.getString("jsp.error.unable.load"), cnfe);
            }

            try {
                theServlet = (Servlet) servletClass.newInstance();
            } catch (Exception ex) {
                throw new JasperException(ex);
            }
            theServlet.init(JspServlet.this.config);
        }

        // returns the cached compilation exception
        private JasperException getCompileException() {
            return compileException;
        }

        private void incrementRefCount() {
            refCount++;
        }

        private void decrementRefCount() {
            refCount--;
            if ((refCount == 0) && markedForDestroy)
                destroy();
        }

        // try to destroy the JSP, the actual destroy occurs only when
        // the refcount goes down to 0
        private void tryDestroy() {
            if (refCount == 0)
                destroy();
            markedForDestroy = true;
        }

        private void destroy() {
            if (theServlet != null)
                theServlet.destroy();

            servletClass = null;
            theServlet = null;

            jspFile = null;
            loader = null;
        }
    }
}
