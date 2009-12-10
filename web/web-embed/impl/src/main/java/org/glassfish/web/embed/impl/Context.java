/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 */

package org.glassfish.web.embed.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.web.config.SecurityConfig;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.Constants;
import org.apache.catalina.core.StandardContext;


/**
 * Representation of a web application.
 *
 * @author Amy Roh
 */
// TODO: Add support for configuring environment entries
public class Context extends StandardContext implements org.glassfish.api.embedded.web.Context {


    // ----------------------------------------------------- Instance Variables
    
    private SecurityConfig config;
    
    // --------------------------------------------------------- Public Methods

    /**
     * Enables or disables directory listings on this <tt>Context</tt>.
     *
     * @param directoryListing true if directory listings are to be
     * enabled on this <tt>Context</tt>, false otherwise
     */
    public void setDirectoryListing(boolean directoryListing) {
        Wrapper wrapper = (Wrapper) findChild(Constants.DEFAULT_SERVLET_NAME);
        if (wrapper !=null) {
            wrapper.addInitParameter("listings", Boolean.toString(directoryListing));
        }
        
    }

    /**
     * Checks whether directory listings are enabled or disabled on this
     * <tt>Context</tt>.
     *
     * @return true if directory listings are enabled on this 
     * <tt>Context</tt>, false otherwise
     */
    public boolean isDirectoryListing() {               
        Wrapper wrapper = (Wrapper) findChild(Constants.DEFAULT_SERVLET_NAME);
        if (wrapper !=null) {
            return Boolean.parseBoolean(wrapper.findInitParameter("listings"));
        }
        return false;
    }

    /**
     * Set the security related configuration for this context
     *
     * @see org.glassfish.web.embed.config.SecurityConfig
     *
     * @param config the security configuration for this context
     */
    public void setSecurityConfig(SecurityConfig config) {
        this.config = config;
        // TODO 
    }

    /**
     * Gets the security related configuration for this context
     *
     * @see org.glassfish.web.embed.config.SecurityConfig
     *
     * @return the security configuration for this context
     */
    public SecurityConfig getSecurityConfig() {
        return config;
    }

    
    // ------------------------------------------------- ServletContext Methods
            
    /**
     * Return the value of the specified context attribute, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the context attribute to return
     */
    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }
    
    /**
     * Return an enumeration of the names of the context attributes
     * associated with this context.
     */
    public Enumeration<String> getAttributeNames() {
        return context.getAttributeNames();
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
        return context.getContextPath();
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
        return context.getContext(uri);
    }
    
    /**
     * Return the value of the specified initialization parameter, or
     * <code>null</code> if this parameter does not exist.
     *
     * @param name Name of the initialization parameter to retrieve
     */
    public String getInitParameter(final String name) {
        return context.getInitParameter(name);
    }

    /**
     * Return the names of the context's initialization parameters, or an
     * empty enumeration if the context has no initialization parameters.
     */
    public Enumeration<String> getInitParameterNames() {
        return context.getInitParameterNames();
    }
    
    /**
     * @return true if the context initialization parameter with the given
     * name and value was set successfully on this ServletContext, and false
     * if it was not set because this ServletContext already contains a
     * context initialization parameter with a matching name
     */
    public boolean setInitParameter(String name, String value) {
        return context.setInitParameter(name, value);
    }    
        
    /**
     * Return the major version of the Java Servlet API that we implement.
     */
    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    /**
     * Return the minor version of the Java Servlet API that we implement.
     */
    public int getMinorVersion() {
        return context.getMinorVersion();
    }
        
    /**
     * Return the MIME type of the specified file, or <code>null</code> if
     * the MIME type cannot be determined.
     *
     * @param file Filename for which to identify a MIME type
     */
    public String getMimeType(String file) {
        return context.getMimeType(file);
    }
        
    /**
     * Return a <code>RequestDispatcher</code> object that acts as a
     * wrapper for the named servlet.
     *
     * @param name Name of the servlet for which a dispatcher is requested
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        return context.getNamedDispatcher(name);
    }
    
    /**
     * Return the display name of this web application.
     */
    public String getServletContextName() {
        return getDisplayName();
    }
        
    /**
     * Remove the context attribute with the specified name, if any.
     *
     * @param name Name of the context attribute to be removed
     */
    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }
            
    /**
     * Bind the specified value with the specified context attribute name,
     * replacing any existing value for that name.
     *
     * @param name Attribute name to be bound
     * @param value New attribute value to be bound
     */
    public void setAttribute(String name, Object value) {
        context.setAttribute(name, value);
    }
       
       


        
    /**
     * Return the name and version of the servlet container.
     */
    public String getServerInfo() {
        return context.getServerInfo();
    }
    
        
    /**
     * @param path The virtual path to be translated
     *
     * @return the real path corresponding to the given virtual path, or
     * <code>null</code> if the container was unable to perform the
     * translation
     */
    public String getRealPath(String path) {
        return context.getRealPath(path);
    }    
    
    /**
     * Writes the specified message to a servlet log file.
     *
     * @param message Message to be written
     */
    public void log(String message) {
        context.log(message);
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
        context.log(exception, message);
    }       
        
    /**
     * Writes the specified message and exception to a servlet log file.
     *
     * @param message Message to be written
     * @param throwable Exception to be reported
     */
    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }
        
    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Servlet getServlet(String name) {
        return context.getServlet(name);
    }
            
    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Enumeration<String> getServletNames() {
        return context.getServletNames();
    }            
        
    /**
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     */
    public Enumeration<Servlet> getServlets() {
        return context.getServlets();
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
        return context.getResourceAsStream(path);
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
        return context.getResource(path);
    }
        
    /**
     * Return a Set containing the resource paths of resources member of the
     * specified collection. Each path will be a String starting with
     * a "/" character. The returned set is immutable.
     *
     * @param path Collection path
     */
    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }    
    

         
    /**
     * Return a <code>RequestDispatcher</code> instance that acts as a
     * wrapper for the resource at the given path.  The path must begin
     * with a "/" and is interpreted as relative to the current context root.
     *
     * @param path The path to the desired resource.
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        return context.getRequestDispatcher(path);
    }
    
    
    // ------------------------------------------------- Lifecycle Methods
            
    /**
     * Enables this component.
     * 
     * @throws LifecycleException if this component fails to be enabled
     */    
    public void enable() throws LifecycleException {               
       try {
            start();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
    }

    /**
     * Disables this component.
     * 
     * @throws LifecycleException if this component fails to be disabled
     */
    public void disable() throws LifecycleException {
       try {
            stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }        
    }
    
    
}
