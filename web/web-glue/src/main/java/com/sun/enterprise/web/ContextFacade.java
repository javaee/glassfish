/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.enterprise.web;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.UserDataConstraint;
import com.sun.enterprise.web.deploy.LoginConfigDecorator;
import org.apache.catalina.core.*;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.config.FormLoginConfig;
import org.glassfish.embeddable.web.config.LoginConfig;
import org.glassfish.embeddable.web.config.SecurityConfig;
import org.glassfish.embeddable.web.config.TransportGuarantee;

/**
 * Facade object which masks the internal <code>Context</code>
 * object from the web application.
 *
 * @author Amy Roh
 */
public class ContextFacade extends StandardContext implements Context {
        
    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param docRoot
     * @param contextRoot
     * @param classLoader
     *
     */
    public ContextFacade(File docRoot, String contextRoot, ClassLoader classLoader) {
        this.docRoot = docRoot;
        this.contextRoot = contextRoot;
        this.classLoader = classLoader;
    }

    /**
     * Wrapped web module.
     */
    private WebModule context = null;

    private File docRoot;

    private String contextRoot;

    private ClassLoader classLoader;

    private Map<String, String> servlets = new HashMap<String, String>();

    private Map<String, String[]> servletMappings = new HashMap<String, String[]>();

    protected Map<String, StandardWrapper> wrappers = new HashMap<String, StandardWrapper>();

    // ------------------------------------------------------------- Properties

    public File getDocRoot() {
        return docRoot;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    // ------------------------------------------------- ServletContext Methods

    public String getContextPath() {
        return context.getContextPath();
    }

    public ServletContext getContext(String uripath) {
        return context.getContext(uripath);
    }

    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    public int getEffectiveMajorVersion() {
        return context.getEffectiveMajorVersion();
    }

    public int getEffectiveMinorVersion() {
        return context.getEffectiveMinorVersion();
    }

    public String getMimeType(String file) {
        return context.getMimeType(file);
    }

    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }

    public URL getResource(String path)
        throws MalformedURLException {
        return context.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return context.getResourceAsStream(path);
    }

    public RequestDispatcher getRequestDispatcher(final String path) {
        return context.getRequestDispatcher(path);
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return context.getNamedDispatcher(name);
    }

    public Servlet getServlet(String name) {
        return context.getServlet(name);
    }

    public Enumeration<Servlet> getServlets() {
        return context.getServlets();
    }

    public Enumeration<String> getServletNames() {
        return context.getServletNames();
   }
    public void log(String msg) {
        context.log(msg);
    }

    public void log(Exception exception, String msg) {
        context.log(exception, msg);
    }

    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }

    public String getRealPath(String path) {
        return context.getRealPath(path);
    }

    public String getServerInfo() {
        return context.getServerInfo();
    }

    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return context.getInitParameterNames();
    }

    public boolean setInitParameter(String name, String value) {
        return context.setInitParameter(name, value);
    }

    public Object getAttribute(String name) {
        return context.getAttribute(name);
     }

    public Enumeration<String> getAttributeNames() {
        return context.getAttributeNames();
    }

    public void setAttribute(String name, Object object) {
        context.setAttribute(name, object);
    }

    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }

    public String getServletContextName() {
        return context.getServletContextName();
    }

    /**
     * Returns previously added servlets
     */
    public Map<String, String> getAddedServlets() {
        return servlets;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(
            String servletName, String className) {
        if (context != null) {
            return context.addServlet(servletName, className);
        } else {
            return addServletBefore(servletName, className);
        }
    }

    /*
    public Servlet findServlet(String name) {
        if (name == null)
            return (null);
        synchronized (servlets) {       // Required by post-start changes
            return servlets.get(name);
        }
    }*/

    public ServletRegistration.Dynamic addServletBefore(String servletName,
            String className) {
        if (servletName == null || className == null) {
            throw new NullPointerException("Null servlet instance or name");
        }

        DynamicServletRegistrationImpl regis =
                (DynamicServletRegistrationImpl)
                        servletRegisMap.get(servletName);
        if (regis == null) {
            StandardWrapper wrapper = new StandardWrapper();
            wrapper.setName(servletName);
            wrapper.setServletClassName(className);

            regis = (DynamicServletRegistrationImpl)
                    createDynamicServletRegistrationImpl((StandardWrapper) wrapper);

            servletRegisMap.put(servletName, regis);
            servlets.put(servletName, className);
            wrappers.put(servletName, wrapper);
        }

        return regis;
    }

    public Map<String, String[]> getServletMappings() {
        return servletMappings;
    }

    protected ServletRegistrationImpl createServletRegistrationImpl(
            StandardWrapper wrapper) {
        return new ServletRegistrationImpl(wrapper, this);
    }
    protected ServletRegistrationImpl createDynamicServletRegistrationImpl(
            StandardWrapper wrapper) {
        return new DynamicServletRegistrationImpl(wrapper, this);
    }

    public ServletRegistration.Dynamic addServlet(String servletName,
            Class <? extends Servlet> servletClass) {
        if (context != null) {
            return context.addServlet(servletName, servletClass);
        } else {
            return null;
        }
    }

    public ServletRegistration.Dynamic addServlet(
            String servletName, Servlet servlet) {
        if (context != null) {
            return context.addServlet(servletName, servlet);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> addServletMapping(String name,
                                         String[] urlPatterns) {
        servletMappings.put(name, urlPatterns);
        return Collections.emptySet();
    }

    public <T extends Servlet> T createServlet(Class<T> clazz)
            throws ServletException {
        return context.createServlet(clazz);
    }

    public ServletRegistration getServletRegistration(String servletName) {
        return context.getServletRegistration(servletName);
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return context.getServletRegistrations();
    }

    public FilterRegistration.Dynamic addFilter(
            String filterName, String className) {
        return context.addFilter(filterName, className);
    }

    public FilterRegistration.Dynamic addFilter(
            String filterName, Filter filter) {
        return context.addFilter(filterName, filter);
    }

    public FilterRegistration.Dynamic addFilter(String filterName,
            Class <? extends Filter> filterClass) {
        return context.addFilter(filterName, filterClass);
    }

    public <T extends Filter> T createFilter(Class<T> clazz)
            throws ServletException {
        return context.createFilter(clazz);
    }

    public FilterRegistration getFilterRegistration(String filterName) {
        return context.getFilterRegistration(filterName);
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return context.getFilterRegistrations();
    }

    public SessionCookieConfig getSessionCookieConfig() {        
        return context.getSessionCookieConfig();
    }
    
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        context.setSessionTrackingModes(sessionTrackingModes);
    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return context.getDefaultSessionTrackingModes();
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return context.getEffectiveSessionTrackingModes();
    }

    public void addListener(String className) {
        context.addListener(className);
    }

    public <T extends EventListener> void addListener(T t) {
        context.addListener(t);
    }

    public void addListener(Class <? extends EventListener> listenerClass) {
        context.addListener(listenerClass);
    }

    public <T extends EventListener> T createListener(Class<T> clazz)
            throws ServletException {
        return context.createListener(clazz);
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
        return context.getJspConfigDescriptor();
    }

    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        } else if (context != null) {
            return context.getClassLoader();
        } else {
            return null;
        }
    }

    public void declareRoles(String... roleNames) {
        // TBD
    }

    public String getPath() {
        return context.getPath();
    }

    public void setPath(String path) {
        context.setPath(path);
    }

    public String getDefaultWebXml() {
        return context.getDefaultWebXml();
    }

    public void setDefaultWebXml(String defaultWebXml) {
        context.setDefaultWebXml(defaultWebXml);
    }

    /**
     * Gets the underlying StandardContext to which this
     * ContextFacade is ultimately delegating.
     *
     * @return The underlying StandardContext
     */
    public WebModule getUnwrappedContext() {
        return context;
    }

    public void setUnwrappedContext(WebModule wm) {
        context = wm;
    }

    // --------------------------------------------------------- embedded Methods

    private SecurityConfig config;

    /**
     * Enables or disables directory listings on this <tt>Context</tt>.
     */
    public void setDirectoryListing(boolean directoryListing) {
        context.setDirectoryListing(directoryListing);
    }

    public boolean isDirectoryListing() {
        return context.isDirectoryListing();
    }

    /**
     * Set the security related configuration for this context
     */
    public void setSecurityConfig(SecurityConfig config) {
        this.config = config;
        if (config == null) {
            return;
        } else if (context != null) {
            context.setSecurityConfig(config);
        }
    }

    /**
     * Gets the security related configuration for this context
     */
    public SecurityConfig getSecurityConfig() {
        return config;
    }


}
