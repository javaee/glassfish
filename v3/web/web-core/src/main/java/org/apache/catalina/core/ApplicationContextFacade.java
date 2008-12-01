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


import java.io.InputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import javax.naming.NamingException;
import javax.naming.Binding;
import javax.naming.directory.DirContext;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityUtil;

/**
 * Facade object which masks the internal <code>ApplicationContext</code>
 * object from the web application.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Revision: 1.7.6.1 $ $Date: 2008/04/17 18:37:06 $
 */

public final class ApplicationContextFacade
    implements ServletContext {
        
    // ---------------------------------------------------------- Attributes
    /**
     * Cache Class object used for reflection.
     */
    private static HashMap classCache = new HashMap();
    
    static {
        Class[] clazz = new Class[]{String.class};
        classCache.put("getContext", clazz);
        classCache.put("getMimeType", clazz);
        classCache.put("getResourcePaths", clazz);
        classCache.put("getResource", clazz);
        classCache.put("getResourceAsStream", clazz);
        classCache.put("getRequestDispatcher", clazz);
        classCache.put("getNamedDispatcher", clazz);
        classCache.put("getServlet", clazz);
        classCache.put("getInitParameter", clazz);
        classCache.put("setAttribute", new Class[]{String.class, Object.class});
        classCache.put("removeAttribute", clazz);
        classCache.put("getRealPath", clazz);
        classCache.put("getAttribute", clazz);
        classCache.put("log", clazz);        
    }
    
    /**
     * Cache method object.
     */
    private HashMap objectCache;
    
    
    private static Logger sysLog = Logger.getLogger(
        ApplicationContextFacade.class.getName());
    
        
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContextFacade(ApplicationContext context) {
        super();
        this.context = context;
        
        objectCache = new HashMap();
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped application context.
     */
    private ApplicationContext context = null;
    


    // ------------------------------------------------- ServletContext Methods

    public String getContextPath() {
        return context.getContextPath();
    }


    public ServletContext getContext(String uripath) {
        ServletContext theContext = null;
        if (SecurityUtil.isPackageProtectionEnabled()) {
            theContext = (ServletContext)
                doPrivileged("getContext", new Object[]{uripath});
        } else {
            theContext = context.getContext(uripath);
        }
        if ((theContext != null) &&
            (theContext instanceof ApplicationContext)){
            theContext = ((ApplicationContext)theContext).getFacade();
        }
        return (theContext);
    }


    public int getMajorVersion() {
        return context.getMajorVersion();
    }


    public int getMinorVersion() {
        return context.getMinorVersion();
    }


    public String getMimeType(String file) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String)doPrivileged("getMimeType", new Object[]{file});
        } else {
            return context.getMimeType(file);
        }
    }


    public Set getResourcePaths(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()){
            return (Set)doPrivileged("getResourcePaths", new Object[]{path});
        } else {
            return context.getResourcePaths(path);
        }
    }


    public URL getResource(String path)
        throws MalformedURLException {
        if (Globals.IS_SECURITY_ENABLED) {
            try {
                return (URL) invokeMethod(context, "getResource", 
                                          new Object[]{path});
            } catch(Throwable t) {
                if (t instanceof MalformedURLException){
                    throw (MalformedURLException)t;
                }
                return null;
            }
        } else {
            return context.getResource(path);
        }
    }


    public InputStream getResourceAsStream(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (InputStream) doPrivileged("getResourceAsStream", 
                                              new Object[]{path});
        } else {
            return context.getResourceAsStream(path);
        }
    }


    public RequestDispatcher getRequestDispatcher(final String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (RequestDispatcher) doPrivileged("getRequestDispatcher", 
                                                    new Object[]{path});
        } else {
            return context.getRequestDispatcher(path);
        }
    }


    public RequestDispatcher getNamedDispatcher(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (RequestDispatcher) doPrivileged("getNamedDispatcher", 
                                                    new Object[]{name});
        } else {
            return context.getNamedDispatcher(name);
        }
    }


    public Servlet getServlet(String name)
        throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            try {
                return (Servlet) invokeMethod(context, "getServlet", 
                                              new Object[]{name});
            } catch (Throwable t) {
                if (t instanceof ServletException) {
                    throw (ServletException) t;
                }
                return null;
            }
        } else {
            return context.getServlet(name);
        }
    }


    public Enumeration getServlets() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration) doPrivileged("getServlets", null);
        } else {
            return context.getServlets();
        }
    }


    public Enumeration getServletNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration) doPrivileged("getServletNames", null);
        } else {
            return context.getServletNames();
        }
   }


    public void log(String msg) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Object[]{msg} );
        } else {
            context.log(msg);
        }
    }


    public void log(Exception exception, String msg) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Class[]{Exception.class, String.class}, 
                         new Object[]{exception,msg});
        } else {
            context.log(exception, msg);
        }
    }


    public void log(String message, Throwable throwable) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Class[]{String.class, Throwable.class}, 
                         new Object[]{message, throwable});
        } else {
            context.log(message, throwable);
        }
    }


    public String getRealPath(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getRealPath", new Object[]{path});
        } else {
            return context.getRealPath(path);
        }
    }


    public String getServerInfo() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getServerInfo", null);
        } else {
            return context.getServerInfo();
        }
    }


    public String getInitParameter(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getInitParameter", 
                                         new Object[]{name});
        } else {
            return context.getInitParameter(name);
        }
    }


    public Enumeration getInitParameterNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration) doPrivileged("getInitParameterNames", null);
        } else {
            return context.getInitParameterNames();
        }
    }


    public Object getAttribute(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getAttribute", new Object[]{name});
        } else {
            return context.getAttribute(name);
        }
     }


    public Enumeration getAttributeNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration) doPrivileged("getAttributeNames", null);
        } else {
            return context.getAttributeNames();
        }
    }


    public void setAttribute(String name, Object object) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setAttribute", new Object[]{name,object});
        } else {
            context.setAttribute(name, object);
        }
    }


    public void removeAttribute(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("removeAttribute", new Object[]{name});
        } else {
            context.removeAttribute(name);
        }
    }


    public String getServletContextName() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getServletContextName", null);
        } else {
            return context.getServletContextName();
        }
    }


    /*
     * Adds the servlet with the given name, description, class name,
     * init parameters, and loadOnStartup, to this servlet context.
     */
    public void addServlet(String servletName,
                           String description,
                           String className,
                           Map<String, String> initParameters,
                           int loadOnStartup,
                           boolean isAsyncSupported) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addServlet",
                         new Object[] {servletName, description, className,
                                       initParameters, loadOnStartup,
                                       isAsyncSupported});
        } else {
            context.addServlet(servletName, description, className,
                               initParameters, loadOnStartup,
                               isAsyncSupported);
        }
    }


    /**
     * Adds servlet mappings from the given url patterns to the servlet
     * with the given servlet name to this servlet context.
     */
    public void addServletMapping(String servletName,
                                  String[] urlPatterns) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addServletMapping",
                         new Object[] {servletName, urlPatterns});
        } else {
            context.addServletMapping(servletName, urlPatterns);
        }
    }


    /**
     * Adds the filter with the given name, description, and class name to
     * this servlet context.
     */
    public void addFilter(String filterName,
                          String description,
                          String className,
                          Map<String, String> initParameters,
                          boolean isAsyncSupported) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addFilter",
                         new Object[] {filterName, description, className,
                                       initParameters, isAsyncSupported});
        } else {
            context.addFilter(filterName, description, className,
                              initParameters, isAsyncSupported);
        }
    }
    
    
    /**
     * Adds a filter mapping with the given servlet names, and
     * dispatcher types for the filter with the given filter name to this
     * servlet context.
     *
     * @param filterName the name of the filter for which the filter
     * mapping is added
     * @param dispatcherTypes the dispatcher types of the filter mapping,
     * or null if the default <tt>DispatcherType.REQUEST</tt> is to be used
     * @param isMatchAfter true if the given filter mapping should be matched
     * against requests after any declared filter mappings of this servlet
     * context, and false if it is supposed to be matched before any declared
     * filter mappings of this servlet context
     * @param servletNames the servlet names of the filter mapping
     */
    public void addFilterMappingForServletNames(String filterName,
                                    EnumSet<DispatcherType> dispatcherTypes,
                                    boolean isMatchAfter,
                                    String... servletNames) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addFilterMappingForServletNames",
                         new Object[] {filterName, dispatcherTypes,
                                        isMatchAfter, servletNames});
        } else {
            context.addFilterMappingForServletNames(filterName, dispatcherTypes, 
                                                isMatchAfter, servletNames);
        }
    }

     
    /**
     * Adds a filter mapping with the given url patterns, and
     * dispatcher types for the filter with the given filter name to this
     * servlet context.     
     *
     * @param filterName the name of the filter for which the filter
     * mapping is added
     * @param dispatcherTypes the dispatcher types of the filter mapping,
     * or null if the default <tt>DispatcherType.REQUEST</tt> is to be used
     * @param isMatchAfter true if the given filter mapping should be matched
     * against requests after any declared filter mappings of this servlet
     * context, and false if it is supposed to be matched before any declared
     * filter mappings of this servlet context
     * @param urlPatterns the url patterns of the filter mapping
     */
    public void addFilterMappingForUrlPatterns(String filterName,
                                  EnumSet<DispatcherType> dispatcherTypes,
                                  boolean isMatchAfter,
                                  String... urlPatterns) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addFilterMappingForUrlPatterns",
                         new Object[] {filterName, dispatcherTypes,
                                        isMatchAfter, urlPatterns});
        } else {
            context.addFilterMappingForUrlPatterns(filterName, dispatcherTypes,
                                        isMatchAfter, urlPatterns);
        }
    }

        
    /**
     * Sets the session tracking cookie configuration for this 
     * <tt>ServletContext</tt>.
     *
     * <p>The given <tt>SessionCookieConfig</tt> replaces any
     * session tracking cookie configuration that was previously set.
     *
     * @param sessionCookieConfig 
     * @throws IllegalStateException if this <tt>ServletContext</tt> has
     * already been initialized
     */
    public void setSessionCookieConfig(SessionCookieConfig sessionCookieConfig) {
                        
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setSessionCookieConfig",
                         new Object[] {sessionCookieConfig});
        } else {
            context.setSessionCookieConfig(sessionCookieConfig);
        }
        
    }
 
     
    /**
     * Gets the session tracking cookie configuration of this 
     * <tt>ServletContext</tt>.
     *
     * @return the session tracking cookie configuration of this 
     * <tt>ServletContext</tt>, or <tt>null</tt> if no such configuration
     * was ever set for this <tt>ServletContext</tt>
     */
    public SessionCookieConfig getSessionCookieConfig() {
                
        
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (SessionCookieConfig) doPrivileged("getSessionCookieConfig", null);
        } else {
            return context.getSessionCookieConfig();
        }
        
    }
    

    /**
     * Sets the session tracking modes that are to become effective for this
     * <tt>ServletContext</tt>.
     */
    public void setSessionTrackingModes(EnumSet<SessionTrackingMode> sessionTrackingModes) {
        context.setSessionTrackingModes(sessionTrackingModes);
    }


    /**
     * Gets the session tracking modes that are supported by default for this
     * <tt>ServletContext</tt>.
     *
     * @return enum set of the session tracking modes supported by default for
     * this <tt>ServletContext</tt>
     */
    public EnumSet<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return context.getDefaultSessionTrackingModes();
    }


    /**
     * Gets the session tracking modes that are in effect for this
     * <tt>ServletContext</tt>.
     *
     * @return enum set of the session tracking modes in effect for this
     * <tt>ServletContext</tt>
     */
    public EnumSet<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return context.getEffectiveSessionTrackingModes();
    }


    // START PWC 1.2
    /**
     * Gets the underlying StandardContext to which this
     * ApplicationContextFacade is ultimately delegating.
     *
     * @return The underlying StandardContext
     */
    public StandardContext getUnwrappedContext() {
        return context.getUnwrappedContext();
    }
    // END PWC 1.2

       
    /**
     * Use reflection to invoke the requested method. Cache the method object 
     * to speed up the process
     * @param appContext The AppliationContext object on which the method
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private Object doPrivileged(ApplicationContext appContext,
                                final String methodName, 
                                Object[] params) {
        try{
            return invokeMethod(appContext, methodName, params );
        } catch (Throwable t){
            throw new RuntimeException(t.getMessage());
        } finally {
            params = null;
        }

    }


    /**
     * Use reflection to invoke the requested method. Cache the method object 
     * to speed up the process
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private Object doPrivileged(final String methodName, Object[] params){
        try{
            return invokeMethod(context, methodName, params);
        }catch(Throwable t){
            throw new RuntimeException(t.getMessage());
        } finally {
            params = null;
        }
    }

    
    /**
     * Use reflection to invoke the requested method. Cache the method object 
     * to speed up the process
     * @param appContext The AppliationContext object on which the method
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private Object invokeMethod(ApplicationContext appContext,
                                final String methodName, 
                                Object[] params) 
        throws Throwable{

        try{
            Method method = (Method)objectCache.get(methodName);
            if (method == null){
                method = appContext.getClass()
                    .getMethod(methodName, (Class[])classCache.get(methodName));
                objectCache.put(methodName, method);
            }
            
            return executeMethod(method,appContext,params);
        } catch (Exception ex){
            handleException(ex, methodName);
            return null;
        } finally {
            params = null;
        }
    }
    
    /**
     * Use reflection to invoke the requested method. Cache the method object 
     * to speed up the process
     * @param appContext The AppliationContext object on which the method
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */    
    private Object doPrivileged(final String methodName, 
                                final Class[] clazz,
                                Object[] params){

        try{
            Method method = context.getClass()
                    .getMethod(methodName, (Class[])clazz);
            return executeMethod(method,context,params);
        } catch (Exception ex){
            try{
                handleException(ex, methodName);
            }catch (Throwable t){
                throw new RuntimeException(t.getMessage());
            }
            return null;
        } finally {
            params = null;
        }
    }
    
    
    /**
     * Executes the method of the specified <code>ApplicationContext</code>
     * @param method The method object to be invoked.
     * @param context The AppliationContext object on which the method
     *                   will be invoked
     * @param params The arguments passed to the called method.
     */
    private Object executeMethod(final Method method, 
                                 final ApplicationContext context,
                                 final Object[] params) 
            throws PrivilegedActionException, 
                   IllegalAccessException,
                   InvocationTargetException {
                                     
        if (Globals.IS_SECURITY_ENABLED){
           return AccessController.doPrivileged(new PrivilegedExceptionAction(){
                public Object run() throws IllegalAccessException, InvocationTargetException{
                    return method.invoke(context,  params);
                }
            });
        } else {
            return method.invoke(context, params);
        }        
    }

    
    /**
     * Throw the real exception.
     * @param ex The current exception
     */
    private void handleException(Exception ex, String methodName)
	    throws Throwable {

        Throwable realException;

        if (sysLog.isLoggable(Level.FINE)) {   
            sysLog.log(Level.FINE, "ApplicationContextFacade." + methodName,
                       ex);
        }

	if (ex instanceof PrivilegedActionException) {
            ex = ((PrivilegedActionException) ex).getException();
	}

        if (ex instanceof InvocationTargetException) {
            realException =
		((InvocationTargetException) ex).getTargetException();
        } else {
            realException = ex;
        }   

        throw realException;
    }
}
