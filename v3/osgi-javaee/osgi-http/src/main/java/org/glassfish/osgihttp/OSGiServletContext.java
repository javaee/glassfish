/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgihttp;

import org.osgi.service.http.HttpContext;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unlike Java EE Web Application model, there is no notion of "context path"
 * in OSGi HTTP service spec. Here the servlets can specify which context they
 * belong to by passing a {@link org.osgi.service.http.HttpContext} object.
 * Those HttpContext objects don't have any "path" attribute. As a result,
 * all the OSGi/HTTP servlets belonging to the same servlet context may not
 * have any of the path common to them. Internally, we register all the OSGi
 * servlets (actually we register {@link OSGiServletWrapper}
 * with the same {@link org.apache.catalina.Context} object. So we need a way to
 * demultiplex the OSGi servlet context. This class also delegates to
 * {@link HttpContext} for resource resolutions and security.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiServletContext implements ServletContext {

    // server wide context
    private final ServletContext delegate;
    private final HttpContext httpContext;
    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    public OSGiServletContext(ServletContext delegate, HttpContext httpContext) {
        this.delegate = delegate;
        this.httpContext = httpContext;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public String getContextPath() {
        return delegate.getContextPath();
    }

    public ServletContext getContext(String uri) {
        // TODO(Sahoo): This needs to be looked at
        return delegate.getContext(uri);
    }

    public String getInitParameter(final String name) {
        return delegate.getInitParameter(name);
    }

    public Enumeration getInitParameterNames() {
        return delegate.getInitParameterNames();
    }

    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    public int getEffectiveMajorVersion() {
        return delegate.getEffectiveMajorVersion();
    }

    public int getEffectiveMinorVersion() {
        return delegate.getEffectiveMinorVersion();
    }

    public String getMimeType(String file) {
        String mimeType = httpContext.getMimeType(file);
        return mimeType != null ? mimeType : delegate.getMimeType(file);
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return delegate.getNamedDispatcher(name);
    }

    public String getRealPath(String path) {
        return delegate.getRealPath(path);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return delegate.getRequestDispatcher(path);
    }

    public URL getResource(String path) throws MalformedURLException {
        return httpContext.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        try {
            URL url = getResource(path);
            return url != null ? url.openStream() : null;
        } catch (Exception e) {
        }
        return null;
    }

    public Set getResourcePaths(String path) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO(Sahoo):
    }

    public String getServerInfo() {
        return delegate.getServerInfo();
    }

    public Servlet getServlet(String name) throws ServletException {
        return delegate.getServlet(name);
    }

    public String getServletContextName() {
        return delegate.getServletContextName();
    }

    public Enumeration getServletNames() {
        return delegate.getServletNames();
    }

    public Enumeration getServlets() {
        return delegate.getServlets();
    }

    public void log(String message) {
        delegate.log(message);
    }

    public void log(Exception exception, String message) {
        delegate.log(exception, message);
    }

    public void log(String message, Throwable throwable) {
        delegate.log(message, throwable);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public ServletRegistration.Dynamic addServlet(
            String servletName, String className) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public void addServletMapping(String servletName, String[] urlPatterns) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public FilterRegistration.Dynamic addFilter(
            String filterName, String className) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public void addFilterMappingForServletNames(String filterName, EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public void addFilterMappingForUrlPatterns(String filterName, EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public void setSessionCookieConfig(SessionCookieConfig sessionCookieConfig) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public void setSessionTrackingModes(EnumSet<SessionTrackingMode> sessionTrackingModes) {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public EnumSet<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public EnumSet<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException(); // TODO(Sahoo):
    }

    public boolean setInitParameter(String name, String value) {
        return delegate.setInitParameter(name, value);
    }

    /*
    * Adds the servlet with the given name and class type to this servlet
    * context.
    *
    * <p>The registered servlet may be further configured via the returned
    * {@link ServletRegistration} object.
    *
    * @param servletName the name of the servlet
    * @param servletClass the class object from which the servlet will be
    * instantiated
    *
    * @return a ServletRegistration object that may be used to further
    * configure the registered servlet, or <tt>null</tt> if this
    * ServletContext already contains a servlet with a matching name
    * @throws IllegalStateException if this ServletContext has already
    * been initialized
    *
    * @since 3.0
    */
    public ServletRegistration.Dynamic addServlet(
            String servletName, Class<? extends Servlet> servletClass)
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public ServletRegistration getServletRegistration(String servletName)
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public Map<String, ServletRegistration> getServletRegistrations()
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public FilterRegistration.Dynamic addFilter(
            String filterName, Class<? extends Filter> filterClass)
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public FilterRegistration getFilterRegistration(String filterName)
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public Map<String, FilterRegistration> getFilterRegistrations()
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public ServletRegistration.Dynamic addServlet(
            String servletName, Servlet servlet)
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public FilterRegistration.Dynamic addFilter(
            String filterName, Filter filter)
    {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {
        //TODO(Sahoo): Not Yet Implemented
    }

    public <T extends Servlet> T createServlet(Class<T> c)
        throws ServletException
    {
        return null; //TODO(Sahoo): Not Yet Implemented
    }

    public <T extends Filter> T createFilter(Class<T> c)
        throws ServletException
    {
        return null; //TODO(Sahoo): Not Yet Implemented
    }

    public void addListener(String className)
    {
        //TODO(Sahoo): Not Yet Implemented
    }

    public <T extends EventListener> void addListener(T t) 
    {
        //TODO(Sahoo): Not Yet Implemented
    }

    public void addListener(Class <? extends EventListener> listenerClass)
    {
        //TODO(Sahoo): Not Yet Implemented
    }

    public <T extends EventListener> T createListener(Class<T> c)
            throws ServletException {
        return null; //TODO(Sahoo): Not Yet Implemented
    }

    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return delegate.getJspConfigDescriptor();
    }

    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    public void declareRoles(String... roleNames) {
        delegate.declareRoles(roleNames);
    }
}
