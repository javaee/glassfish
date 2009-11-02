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

package org.apache.catalina.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.*;
// START PWC 1.2
import java.security.SecurityPermission;
// START PWC 1.2

import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.*;
import javax.servlet.descriptor.*;

import org.apache.catalina.Globals;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.Resource;
import com.sun.grizzly.util.buf.CharChunk;
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.http.mapper.AlternateDocBase;
import com.sun.grizzly.util.http.mapper.MappingData;

/**
 * Standard implementation of <code>ServletContext</code> that represents
 * a web application's execution environment.  An instance of this class is
 * associated with each instance of <code>StandardContext</code>.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.15.2.1 $ $Date: 2008/04/17 18:37:06 $
 */
public class ApplicationContext implements ServletContext {

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContext(String basePath, StandardContext context) {
        this(basePath, null, context);
    }

    public ApplicationContext(
                String basePath,
                ArrayList<AlternateDocBase> alternateDocBases,
                StandardContext context) {
        super();
        this.context = context;
        this.basePath = basePath;
        this.alternateDocBases = alternateDocBases;

        //START PWC 6403328
        this.logPrefix = sm.getString("applicationContext.logPrefix",
                                      context.logName());
        //END PWC 6403328

        setAttribute("com.sun.faces.useMyFaces",
                     Boolean.valueOf(context.isUseMyFaces()));
    }


    // ----------------------------------------------------- Class Variables

    // START PWC 1.2
    private static final SecurityPermission GET_UNWRAPPED_CONTEXT_PERMISSION =
        new SecurityPermission("getUnwrappedContext");
    // END PWC 1.2


    // ----------------------------------------------------- Instance Variables

    /**
     * The context attributes for this context.
     */
    private Map<String, Object> attributes =
        new ConcurrentHashMap<String, Object>();

    /**
     * List of read only attributes for this context.
     */
    private HashMap<String, String> readOnlyAttributes =
        new HashMap<String, String>();

    /**
     * The Context instance with which we are associated.
     */
    private StandardContext context = null;

    /**
     * Empty collection to serve as the basis for empty enumerations.
     * <strong>DO NOT ADD ANY ELEMENTS TO THIS COLLECTION!</strong>
     */
    private static final ArrayList empty = new ArrayList();

    /**
     * The facade around this object.
     */
    private ServletContext facade = new ApplicationContextFacade(this);

    /**
     * The merged context initialization parameters for this Context.
     */
    private HashMap parameters = null;

    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);

    /**
     * Base path.
     */
    private String basePath = null;

    /**
     * Alternate doc bases
     */
    private ArrayList<AlternateDocBase> alternateDocBases = null;

    // START PWC 6403328
    /**
     * Log prefix string
     */
    private String logPrefix = null;
    // END PWC 6403328

    /**
     * Thread local data used during request dispatch.
     */
    private ThreadLocal<DispatchData> dispatchData =
        new ThreadLocal<DispatchData>();
    
    private boolean isRestricted;

    
    // --------------------------------------------------------- Public Methods

    /**
     * Return the resources object that is mapped to a specified path.
     * The path must begin with a "/" and is interpreted as relative to the
     * current context root.
     */
    public DirContext getResources() {
        return context.getResources();
    }


    // ------------------------------------------------- ServletContext Methods

    /**
     * Return the value of the specified context attribute, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the context attribute to return
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Return an enumeration of the names of the context attributes
     * associated with this context.
     */
    public Enumeration<String> getAttributeNames() {
        return new Enumerator(attributes.keySet(), true);
    }

    /**
     * Returns the context path of the web application.
     *
     * <p>The context path is the portion of the request URI that is used
     * to select the context of the request. The context path always comes
     * first in a request URI. The path starts with a "/" character but does
     * not end with a "/" character. For servlets in the default (root)
     * context, this method returns "".
     *
     * <p>It is possible that a servlet container may match a context by
     * more than one context path. In such cases the
     * {@link javax.servlet.http.HttpServletRequest#getContextPath()}
     * will return the actual context path used by the request and it may
     * differ from the path returned by this method.
     * The context path returned by this method should be considered as the
     * prime or preferred context path of the application.
     *
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return context.getPath();
    }

    /**
     * Return a <code>ServletContext</code> object that corresponds to a
     * specified URI on the server.  This method allows servlets to gain
     * access to the context for various parts of the server, and as needed
     * obtain <code>RequestDispatcher</code> objects or resources from the
     * context.  The given path must be absolute (beginning with a "/"),
     * and is interpreted based on our virtual host's document root.
     *
     * @param uri Absolute URI of a resource on the server
     */
    public ServletContext getContext(String uri) {

        // Validate the format of the specified argument
        if ((uri == null) || (!uri.startsWith("/"))) {
            return (null);
        }

        Context child = null;
        try {
            Host host = (Host) context.getParent();
            String mapuri = uri;
            while (true) {
                child = (Context) host.findChild(mapuri);
                if (child != null)
                    break;
                int slash = mapuri.lastIndexOf('/');
                if (slash < 0)
                    break;
                mapuri = mapuri.substring(0, slash);
            }
        } catch (Throwable t) {
            return (null);
        }

        if (child == null) {
            return (null);
        }

        if (context.getCrossContext()) {
            // If crossContext is enabled, can always return the context
            return child.getServletContext();
        } else if (child == context) {
            // Can still return the current context
            return context.getServletContext();
        } else {
            // Nothing to return
            return (null);
        }
    }

    /**
     * Return the value of the specified initialization parameter, or
     * <code>null</code> if this parameter does not exist.
     *
     * @param name Name of the initialization parameter to retrieve
     */
    public String getInitParameter(final String name) {
        mergeParameters();
        synchronized (parameters) {
            return ((String) parameters.get(name));
        }
    }

    /**
     * Return the names of the context's initialization parameters, or an
     * empty enumeration if the context has no initialization parameters.
     */
    public Enumeration<String> getInitParameterNames() {
        mergeParameters();
        synchronized (parameters) {
           return (new Enumerator(parameters.keySet()));
        }
    }

    /**
     * @return true if the context initialization parameter with the given
     * name and value was set successfully on this ServletContext, and false
     * if it was not set because this ServletContext already contains a
     * context initialization parameter with a matching name
     */
    @Override
    public boolean setInitParameter(String name, String value) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        try {
            context.addParameter(name, value);
            if (parameters != null) {
                // Avoid call to mergeParameters
                parameters.put(name, value);
            }
            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    /**
     * Return the major version of the Java Servlet API that we implement.
     */
    public int getMajorVersion() {
        return (Constants.MAJOR_VERSION);
    }

    /**
     * Return the minor version of the Java Servlet API that we implement.
     */
    public int getMinorVersion() {
        return (Constants.MINOR_VERSION);
    }

    /**
     * Gets the major version of the Servlet specification that the
     * application represented by this ServletContext is based on.
     */
    @Override
    public int getEffectiveMajorVersion() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getEffectiveMajorVersion();
    }
        
    /**
     * Gets the minor version of the Servlet specification that the
     * application represented by this ServletContext is based on.
     */
    @Override
    public int getEffectiveMinorVersion() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getEffectiveMinorVersion();
    }

    /**
     * Return the MIME type of the specified file, or <code>null</code> if
     * the MIME type cannot be determined.
     *
     * @param file Filename for which to identify a MIME type
     */
    public String getMimeType(String file) {

        if (file == null)
            return (null);
        int period = file.lastIndexOf(".");
        if (period < 0)
            return (null);
        String extension = file.substring(period + 1);
        if (extension.length() < 1)
            return (null);
        return (context.findMimeMapping(extension));

    }

    /**
     * Return a <code>RequestDispatcher</code> object that acts as a
     * wrapper for the named servlet.
     *
     * @param name Name of the servlet for which a dispatcher is requested
     */
    public RequestDispatcher getNamedDispatcher(String name) {

        // Validate the name argument
        if (name == null)
            return (null);

        // Create and return a corresponding request dispatcher
        Wrapper wrapper = (Wrapper) context.findChild(name);
        if (wrapper == null)
            return (null);
        
        return new ApplicationDispatcher(wrapper, null, null, null, null, name);
    }

    /**
     * @param path The virtual path to be translated
     *
     * @return the real path corresponding to the given virtual path, or
     * <code>null</code> if the container was unable to perform the
     * translation
     */
    public String getRealPath(String path) {

        if (!context.isFilesystemBased())
            return null;

        if (path == null) {
            return null;
        }

        File file = null;
        if (alternateDocBases == null
                || alternateDocBases.size() == 0) {
            file = new File(basePath, path);
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(
                                                path, alternateDocBases);
            if (match != null) {
                file = new File(match.getBasePath(), path);
            } else {
                // None of the url patterns for alternate doc bases matched
                file = new File(basePath, path);
            }
        }

        if (!file.exists()) {
            try {
                // Try looking up resource in
                // WEB-INF/lib/[*.jar]/META-INF/resources
                URL u = context.getLoader().getClassLoader().getResource(
                    Globals.META_INF_RESOURCES + path);
                return (u != null ? u.getPath() : file.getAbsolutePath());
            } catch (Exception e) {
                return null;
            }
        } else {
            return file.getAbsolutePath();
        }
    }

    /**
     * Return a <code>RequestDispatcher</code> instance that acts as a
     * wrapper for the resource at the given path.  The path must begin
     * with a "/" or be empty, and is interpreted as relative to the current
     * context root.
     *
     * @param path The path to the desired resource.
     */
    public RequestDispatcher getRequestDispatcher(String path) {

        // Validate the path argument
        if (path == null) {
            return null;
        }

        if (!path.startsWith("/") && !path.isEmpty()) {
            throw new IllegalArgumentException(
                sm.getString("applicationContext.requestDispatcher.iae",
                             path));
        }

        // Get query string
        String queryString = null;
        int pos = path.indexOf('?');
        if (pos >= 0) {
            queryString = path.substring(pos + 1);
            path = path.substring(0, pos);
        }

        path = RequestUtil.normalize(path);
        if (path == null)
            return (null);

        pos = path.length();

        // Use the thread local URI and mapping data
        DispatchData dd = dispatchData.get();
        if (dd == null) {
            dd = new DispatchData();
            dispatchData.set(dd);
        }

        MessageBytes uriMB = dd.uriMB;
        uriMB.recycle();

        // Retrieve the thread local mapping data
        MappingData mappingData = dd.mappingData;

        // Map the URI
        CharChunk uriCC = uriMB.getCharChunk();
        try {
            uriCC.append(context.getPath(), 0, context.getPath().length());
            /*
             * Ignore any trailing path params (separated by ';') for mapping
             * purposes
             */
            int semicolon = path.indexOf(';');
            if (pos >= 0 && semicolon > pos) {
                semicolon = -1;
            }
            uriCC.append(path, 0, semicolon > 0 ? semicolon : pos);
            context.getMapper().map(uriMB, mappingData);
            if (mappingData.wrapper == null) {
                return (null);
            }
            /*
             * Append any trailing path params (separated by ';') that were
             * ignored for mapping purposes, so that they're reflected in the
             * RequestDispatcher's requestURI
             */
            if (semicolon > 0) {
                uriCC.append(path, semicolon, pos - semicolon);
            }
        } catch (Exception e) {
            // Should never happen
            log(sm.getString("applicationContext.mapping.error"), e);
            return (null);
        }

        Wrapper wrapper = (Wrapper) mappingData.wrapper;
        String wrapperPath = mappingData.wrapperPath.toString();
        String pathInfo = mappingData.pathInfo.toString();

        mappingData.recycle();
        
        // Construct a RequestDispatcher to process this request
        return new ApplicationDispatcher
            (wrapper, uriCC.toString(), wrapperPath, pathInfo, 
             queryString, null);
    }

    /**
     * Return the URL to the resource that is mapped to a specified path.
     * The path must begin with a "/" and is interpreted as relative to the
     * current context root.
     *
     * @param path The path to the desired resource
     *
     * @exception MalformedURLException if the path is not given
     *  in the correct form
     */
    public URL getResource(String path)
        throws MalformedURLException {

        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException(
                sm.getString("applicationContext.resourcePaths.iae",
                             path));
        }
        
        path = RequestUtil.normalize(path);
        if (path == null)
            return (null);

        String libPath = "/WEB-INF/lib/";
        if ((path.startsWith(libPath)) && (path.endsWith(".jar"))) {
            File jarFile = null;
            if (context.isFilesystemBased()) {
                jarFile = new File(basePath, path);
            } else {
                jarFile = new File(context.getWorkPath(), path);
            }
            if (jarFile.exists()) {
                return jarFile.toURL();
            } else {
                return null;
            }

        } else {

            DirContext resources = null;
            if (alternateDocBases == null
                    || alternateDocBases.size() == 0) {
                resources = context.getResources();
            } else {
                AlternateDocBase match = AlternateDocBase.findMatch(
                                                path, alternateDocBases);
                if (match != null) {
                    resources = match.getResources();
                } else {
                    // None of the url patterns for alternate doc bases matched
                    resources = context.getResources();
                }
            }

            if (resources != null) {
                String fullPath = context.getName() + path;
                String hostName = context.getParent().getName();
                try {
                    resources.lookup(path);
                    return new URL
                        /* SJSAS 6318494
                        ("jndi", null, 0, getJNDIUri(hostName, fullPath),
                         */
                        // START SJAS 6318494
                        ("jndi", "", 0, getJNDIUri(hostName, fullPath),
                        // END SJSAS 6318494
		         new DirContextURLStreamHandler(resources));
                } catch (Exception e) {
                    try {
                        // Try looking up resource in
                        // WEB-INF/lib/[*.jar]/META-INF/resources
                        return context.getLoader().getClassLoader().getResource(Globals.META_INF_RESOURCES + path);
                    } catch (Exception ee) {
                        // do nothing
                    }
                }
            }
        }

        return (null);
    }

    /**
     * Return the requested resource as an <code>InputStream</code>.  The
     * path must be specified according to the rules described under
     * <code>getResource</code>.  If no such resource can be identified,
     * return <code>null</code>.
     *
     * @param path The path to the desired resource.
     */
    public InputStream getResourceAsStream(String path) {

        if (path == null || !path.startsWith("/"))
            return (null);

        path = RequestUtil.normalize(path);
        if (path == null)
            return (null);

        DirContext resources = null;

        if (alternateDocBases == null
                || alternateDocBases.size() == 0) {
            resources = context.getResources();
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(
                                path, alternateDocBases);
            if (match != null) {
                resources = match.getResources();
            } else {
                // None of the url patterns for alternate doc bases matched
                resources = context.getResources();
            }
        }

        if (resources != null) {
            try {
                Object resource = resources.lookup(path);
                if (resource instanceof Resource)
                    return (((Resource) resource).streamContent());
            } catch (Exception e) {
                try {
                    // Try looking up resource in
                    // WEB-INF/lib/[*.jar]/META-INF/resources
                    URL u = context.getLoader().getClassLoader().getResource(
                        Globals.META_INF_RESOURCES + path);
                    return (u != null ? u.openStream() : null);
                } catch (Exception ee) {
                    // do nothing
                }
            }
        }
        return (null);

    }

    /**
     * Return a Set containing the resource paths of resources member of the
     * specified collection. Each path will be a String starting with
     * a "/" character. The returned set is immutable.
     *
     * @param path Collection path
     */
    public Set<String> getResourcePaths(String path) {

        // Validate the path argument
        if (path == null) {
            return null;
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException
                (sm.getString("applicationContext.resourcePaths.iae", path));
        }

        path = RequestUtil.normalize(path);
        if (path == null)
            return (null);

        DirContext resources = null;

        if (alternateDocBases == null
                || alternateDocBases.size() == 0) {
            resources = context.getResources();
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(
                                path, alternateDocBases);
            if (match != null) {
                resources = match.getResources();
            } else {
                // None of the url patterns for alternate doc bases matched
                resources = context.getResources();
            }
        }

        if (resources != null) {
            return (getResourcePathsInternal(resources, path));
        }

        return (null);
    }

    /**
     * Internal implementation of getResourcesPath() logic.
     *
     * @param resources Directory context to search
     * @param path Collection path
     */
    private Set<String> getResourcePathsInternal(DirContext resources,
                                                 String path) {
        HashSet<String> set = new HashSet<String>();
        try {
            listCollectionPaths(set, resources, path);
        } catch (NamingException e) {
            // Ignore, need to check for resource paths underneath
            // WEB-INF/lib/[*.jar]/META-INF/resources, see next
        }
        try {
            // Trigger expansion of bundled JAR files
            URL u = context.getLoader().getClassLoader().getResource(
                Globals.META_INF_RESOURCES + path);
            String realPath = (u != null ? u.getPath() : null);
            if (realPath != null) {
                File[] children = new File(realPath).listFiles();
                StringBuilder sb = null;
                for (File child : children) {
                    sb = new StringBuilder(path);
                    sb.append("/");
                    sb.append(child.getName());
                    if (child.isDirectory()) {
                        sb.append("/");
                    }
                    set.add(sb.toString());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Return the name and version of the servlet container.
     */
    public String getServerInfo() {
        return (ServerInfo.getServerInfo());
    }

    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Servlet getServlet(String name) {
        return (null);
    }

    /**
     * Return the display name of this web application.
     */
    public String getServletContextName() {
        return (context.getDisplayName());
    }

    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Enumeration<String> getServletNames() {
        return (new Enumerator(empty));
    }

    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Enumeration<Servlet> getServlets() {
        return (new Enumerator(empty));
    }

    /**
     * Writes the specified message to a servlet log file.
     *
     * @param message Message to be written
     */
    public void log(String message) {
        Logger logger = context.getLogger();
        if (logger != null) {
            /* PWC 6403328
            logger.log(context.logName() + message, Logger.INFORMATION);
            */
            //START PWC 6403328
            logger.log(this.logPrefix + message, Logger.INFORMATION);
            //END PWC 6403328
        }
    }

    /**
     * Writes the specified exception and message to a servlet log file.
     *
     * @param exception Exception to be reported
     * @param message Message to be written
     *
     * @deprecated As of Java Servlet API 2.1, use
     *  <code>log(String, Throwable)</code> instead
     */
    public void log(Exception exception, String message) {        
        Logger logger = context.getLogger();
        if (logger != null)
            logger.log(exception, context.logName() + message);
    }

    /**
     * Writes the specified message and exception to a servlet log file.
     *
     * @param message Message to be written
     * @param throwable Exception to be reported
     */
    public void log(String message, Throwable throwable) {
        Logger logger = context.getLogger();
        if (logger != null)
            logger.log(context.logName() + message, throwable);
    }

    /**
     * Remove the context attribute with the specified name, if any.
     *
     * @param name Name of the context attribute to be removed
     */
    public void removeAttribute(String name) {
        Object value = null;
        boolean found = false;

        // Remove the specified attribute
        synchronized (attributes) {
            // Check for read only attribute
            if (readOnlyAttributes.containsKey(name))
                return;
            found = attributes.containsKey(name);
            if (found) {
                value = attributes.get(name);
                attributes.remove(name);
            } else {
                return;
            }
        }

        // Notify interested application event listeners
        List<EventListener> listeners = context.getApplicationEventListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event =
            new ServletContextAttributeEvent(context.getServletContext(),
                                             name, value);
        Iterator<EventListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            EventListener eventListener = iter.next();
            if (!(eventListener instanceof ServletContextAttributeListener)) {
                continue;
            }
            ServletContextAttributeListener listener =
                (ServletContextAttributeListener) eventListener;
            try {
                context.fireContainerEvent(
                    ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REMOVED,
                    listener);
                listener.attributeRemoved(event);
                context.fireContainerEvent(
                    ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REMOVED,
                    listener);
            } catch (Throwable t) {
                context.fireContainerEvent(
                    ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REMOVED,
                    listener);
                // FIXME - should we do anything besides log these?
                log(sm.getString("applicationContext.attributeEvent"), t);
            }
        }

    }

    /**
     * Bind the specified value with the specified context attribute name,
     * replacing any existing value for that name.
     *
     * @param name Attribute name to be bound
     * @param value New attribute value to be bound
     */
    public void setAttribute(String name, Object value) {

        // Name cannot be null
        if (name == null)
            throw new IllegalArgumentException
                (sm.getString("applicationContext.setAttribute.namenull"));

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }

        Object oldValue = null;
        boolean replaced = false;

        // Add or replace the specified attribute
        synchronized (attributes) {
            // Check for read only attribute
            if (readOnlyAttributes.containsKey(name))
                return;
            oldValue = attributes.get(name);
            if (oldValue != null)
                replaced = true;
            attributes.put(name, value);
        }
        
        if (name.equals(Globals.CLASS_PATH_ATTR) ||
                name.equals(Globals.JSP_TLD_URI_TO_LOCATION_MAP)) {
            setAttributeReadOnly(name);
        }
        
        // Notify interested application event listeners
        List<EventListener> listeners =
            context.getApplicationEventListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event = null;
        if (replaced) {
            event =
                new ServletContextAttributeEvent(context.getServletContext(),
                                                 name, oldValue);
        } else {
            event =
                new ServletContextAttributeEvent(context.getServletContext(),
                                                 name, value);
        }

        Iterator<EventListener> iter = listeners.iterator(); 
        while (iter.hasNext()) {
            EventListener eventListener = iter.next();
            if (!(eventListener instanceof ServletContextAttributeListener)) {
                continue;
	    }
            ServletContextAttributeListener listener =
                (ServletContextAttributeListener) eventListener;
            try {
                if (replaced) {
                    context.fireContainerEvent(
                        ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REPLACED,
                        listener);
                    listener.attributeReplaced(event);
                    context.fireContainerEvent(
                        ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REPLACED,
                        listener);
                } else {
                    context.fireContainerEvent(
                        ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_ADDED,
                        listener);
                    listener.attributeAdded(event);
                    context.fireContainerEvent(
                        ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_ADDED,
                        listener);
                }
            } catch (Throwable t) {
                if (replaced) {
                    context.fireContainerEvent(
                        ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REPLACED,
                        listener);
                } else {
                    context.fireContainerEvent(
                        ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_ADDED,
                        listener);
                }
                // FIXME - should we do anything besides log these?
                log(sm.getString("applicationContext.attributeEvent"), t);
            }
        }
    }

    /*
     * Adds the servlet with the given name and class name to this
     * servlet context.
     */
    @Override
    public ServletRegistration.Dynamic addServlet(
            String servletName, String className) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.addServlet(servletName, className);
    }

    /*
     * Registers the given servlet instance with this ServletContext
     * under the given <tt>servletName</tt>.
     */
    @Override
    public ServletRegistration.Dynamic addServlet(
            String servletName, Servlet servlet) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.addServlet(servletName, servlet);
    }

    /*
     * Adds the servlet with the given name and class type to this
     * servlet context.
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName,
            Class <? extends Servlet> servletClass) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.addServlet(servletName, servletClass);
    }

    /**
     * Instantiates the given Servlet class and performs any required
     * resource injection into the new Servlet instance before returning
     * it.
     */
    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz)
            throws ServletException {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.createServlet(clazz);
    }

    /**
     * Gets the ServletRegistration corresponding to the servlet with the
     * given <tt>servletName</tt>.
     */
    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getServletRegistration(servletName);
    }

    /**
     * Gets a Map of the ServletRegistration objects corresponding to all
     * currently registered servlets.
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getServletRegistrations();
    }

    /**
     * Adds the filter with the given name and class name to this servlet
     * context.
     */
    @Override
    public FilterRegistration.Dynamic addFilter(
            String filterName, String className) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.addFilter(filterName, className);
    }
    
    /*
     * Registers the given filter instance with this ServletContext
     * under the given <tt>filterName</tt>.
     */
    @Override
    public FilterRegistration.Dynamic addFilter(
            String filterName, Filter filter) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.addFilter(filterName, filter);
    }

    /**
     * Adds the filter with the given name and class type to this servlet
     * context.
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName,
            Class <? extends Filter> filterClass) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.addFilter(filterName, filterClass);
    }
    
    /**
     * Instantiates the given Filter class and performs any required
     * resource injection into the new Filter instance before returning
     * it.
     */
    @Override
    public <T extends Filter> T createFilter(Class<T> clazz)
            throws ServletException {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.createFilter(clazz);
    }

    /**
     * Gets the FilterRegistration corresponding to the filter with the
     * given <tt>filterName</tt>.
     */
    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getFilterRegistration(filterName);
    }

    /**
     * Gets a Map of the FilterRegistration objects corresponding to all
     * currently registered filters.
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getFilterRegistrations();
    }

    /**
     * Gets the <tt>SessionCookieConfig</tt> object through which various
     * properties of the session tracking cookies created on behalf of this
     * <tt>ServletContext</tt> may be configured.
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getSessionCookieConfig();        
    }
  
    /**
     * Sets the session tracking modes that are to become effective for this
     * <tt>ServletContext</tt>.
     */
    @Override
    public void setSessionTrackingModes(
            Set<SessionTrackingMode> sessionTrackingModes) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        context.setSessionTrackingModes(sessionTrackingModes);
    }

    /**
     * Gets the session tracking modes that are supported by default for this
     * <tt>ServletContext</tt>.
     *
     * @return set of the session tracking modes supported by default for
     * this <tt>ServletContext</tt>
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getDefaultSessionTrackingModes();
    }

    /**
     * Gets the session tracking modes that are in effect for this
     * <tt>ServletContext</tt>.
     *
     * @return set of the session tracking modes in effect for this
     * <tt>ServletContext</tt>
     */
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getEffectiveSessionTrackingModes();
    }

    /**
     * Adds the listener with the given class name to this ServletContext.
     */
    @Override
    public void addListener(String className) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        context.addListener(className);
    }

    /**
     * Adds the given listener to this ServletContext.
     */
    @Override
    public <T extends EventListener> void addListener(T t) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        context.addListener(t);
    }

    /**
     * Adds a listener of the given class type to this ServletContext.
     */
    @Override
    public void addListener(Class <? extends EventListener> listenerClass) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        context.addListener(listenerClass);
    }

    /**
     * Instantiates the given EventListener class and performs any
     * required resource injection into the new EventListener instance
     * before returning it.
     */
    @Override
    public <T extends EventListener> T createListener(Class<T> clazz)
            throws ServletException {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.createListener(clazz);
    }

    /**
     * Gets the <code>&lt;jsp-config&gt;</code> related configuration
     * that was aggregated from the <code>web.xml</code> and
     * <code>web-fragment.xml</code> descriptor files of the web application
     * represented by this ServletContext.
     */
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getJspConfigDescriptor();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        return context.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        if (isRestricted) {
            throw new UnsupportedOperationException(
                sm.getString("applicationContext.restrictedMethod"));
        }
        context.declareRoles(roleNames);
    }

    // START PWC 1.2
    /**
     * Gets the underlying StandardContext to which this ApplicationContext is
     * delegating.
     *
     * @return The underlying StandardContext
     */
    public StandardContext getUnwrappedContext() {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GET_UNWRAPPED_CONTEXT_PERMISSION);
        }

        return this.context;        
    }
    // END PWC 1.2


    // -------------------------------------------------------- Package Methods

    /**
     * Clear all application-created attributes.
     */
    void clearAttributes() {

        // Create list of attributes to be removed
        ArrayList list = new ArrayList();
        synchronized (attributes) {
            Iterator iter = attributes.keySet().iterator();
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }

        // Remove application originated attributes
        // (read only attributes will be left in place)
        Iterator keys = list.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            removeAttribute(key);
        }        
    }
    
    /**
     * Return the facade associated with this ApplicationContext.
     */
    protected ServletContext getFacade() {
        return this.facade;
    }

    /**
     * Set an attribute as read only.
     */
    void setAttributeReadOnly(String name) {
        synchronized (attributes) {
            if (attributes.containsKey(name))
                readOnlyAttributes.put(name, name);
        }
    }

    void setRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }


    // -------------------------------------------------------- Private Methods

    /**
     * Merge the context initialization parameters specified in the application
     * deployment descriptor with the application parameters described in the
     * server configuration, respecting the <code>override</code> property of
     * the application parameters appropriately.
     */
    private void mergeParameters() {
        if (parameters != null) {
            return;
        }
        HashMap results = new HashMap();
        for (String name : context.findParameters()) {
            results.put(name, context.findParameter(name));
        }
        List<ApplicationParameter> params =
            context.findApplicationParameters();
        synchronized(params) {
            Iterator<ApplicationParameter> i = params.iterator(); 
            while (i.hasNext()) {
                ApplicationParameter param = i.next();
                if (param.getOverride()) {
                    if (results.get(param.getName()) == null)
                        results.put(param.getName(), param.getValue());
                } else {
                    results.put(param.getName(), param.getValue());
                }
            }
        }
        parameters = results;
    }

    /**
     * List resource paths (recursively), and store all of them in the given
     * Set.
     */
    private static void listCollectionPaths
        (Set<String> set, DirContext resources, String path)
        throws NamingException {

        Enumeration<Binding> childPaths = resources.listBindings(path);
        while (childPaths.hasMoreElements()) {
            Binding binding = childPaths.nextElement();
            String name = binding.getName();
            StringBuffer childPath = new StringBuffer(path);
            if (!"/".equals(path) && !path.endsWith("/"))
                childPath.append("/");
            childPath.append(name);
            Object object = binding.getObject();
            if (object instanceof DirContext) {
                childPath.append("/");
            }
            set.add(childPath.toString());
        }
    }

    /**
     * Get full path, based on the host name and the context path.
     */
    private static String getJNDIUri(String hostName, String path) {
        if (!path.startsWith("/"))
            return "/" + hostName + "/" + path;
        else
            return "/" + hostName + path;
    }

    /**
     * Internal class used as thread-local storage when doing path
     * mapping during dispatch.
     */
    private static final class DispatchData {
        public MessageBytes uriMB;
        public MappingData mappingData;

        public DispatchData() {
            uriMB = MessageBytes.newInstance();
            CharChunk uriCC = uriMB.getCharChunk();
            uriCC.setLimit(-1);
            mappingData = new MappingData();
        }
    }
}
