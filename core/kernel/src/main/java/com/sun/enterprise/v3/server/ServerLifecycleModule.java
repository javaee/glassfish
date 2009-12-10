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

package com.sun.enterprise.v3.server;

import java.net.URLClassLoader;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

import java.text.MessageFormat;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleListener;
import com.sun.logging.LogDomains;

import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.internal.api.ServerContext;

/**
 * @author Sridatta Viswanath
 */

public final class ServerLifecycleModule {
    
    private LifecycleListener slcl;
    private String name;
    private String className;
    private String classpath;
    private int loadOrder;
    private boolean isFatal = false;
    private String statusMsg = "OK";
    
    private ServerContext ctx;
    private LifecycleEventContext leContext;
    private ClassLoader urlClassLoader;
    private Properties props = new Properties();

    private static Logger _logger = null;
    private static boolean _isTraceEnabled = false;
    private static ResourceBundle _rb = null;

    private final static String LIFECYCLE_PREFIX = "lifecycle_"; 

    ServerLifecycleModule(ServerContext ctx, String name, String className) {
        this.name = name;
        this.className = className;
        this.ctx = ctx;
        this.leContext = new LifecycleEventContextImpl(ctx);

        _logger = LogDomains.getLogger(ServerLifecycleModule.class, 
            LogDomains.CORE_LOGGER);
        _isTraceEnabled = _logger.isLoggable(Level.FINE);
        _rb = _logger.getResourceBundle();
    }
    
    void setClasspath(String classpath) {
        this.classpath = classpath;
    }
    
    void setProperty(String name, String value) {
        props.put(name, value);
    }
    
    Properties getProperties() {
        return this.props;
    }
    
    void setLoadOrder(int loadOrder) {
        this.loadOrder = loadOrder;
    }
    
    void setIsFatal(boolean isFatal) {
        this.isFatal = isFatal;
    }
        
    String getName() {
        return this.name;
    }
    
    String getClassName() {
        return this.className;
    }

    String getClasspath() {
        return this.classpath;
    }

    int getLoadOrder() {
        return this.loadOrder;
    }
    
    boolean isFatal() {
        return isFatal;
    }
    
    LifecycleListener loadServerLifecycle() throws ServerLifecycleException {
        ClassLoader classLoader = ctx.getLifecycleParentClassLoader();

        try {
            if (this.classpath != null) {
                URL[] urls = getURLs();

                if (urls != null) {
                    StringBuffer sb = new StringBuffer(128);
                    for(int i=0;i<urls.length;i++) {
                        sb.append(urls[i].toString());
                    }
                    if (_isTraceEnabled)
                        _logger.fine("Lifecycle module = " + getName() + 
                                        " has classpath URLs = " + sb.toString());
                }

                this.urlClassLoader = new URLClassLoader(urls, classLoader);
                classLoader = this.urlClassLoader;
            }

            Class cl = Class.forName(className, true, classLoader);
            slcl = (LifecycleListener) cl.newInstance();
        } catch (Exception ee) {
            ee.printStackTrace();
            String msg = _rb.getString("lifecyclemodule.load_exception");
            Object[] params = { this.name, ee.toString() };
            msg = MessageFormat.format(msg, params);

            _logger.log(Level.WARNING, msg);
        }

        return slcl;
    }
    
    private URL[] getURLs() {
        List<URL> urlList = ASClassLoaderUtil.getURLsFromClasspath(
            this.classpath, File.pathSeparator, "");
        return ASClassLoaderUtil.convertURLListToArray(urlList);
    }

    private void postEvent(int eventType, Object data)
                                    throws ServerLifecycleException {
        if (slcl == null) {
            if (isFatal) {
                String msg = _rb.getString("lifecyclemodule.loadExceptionIsFatal");
                Object[] params = { this.name };
                msg = MessageFormat.format(msg, params);

                throw new ServerLifecycleException(msg);
            }

            return;
        }

        if (urlClassLoader != null)
            setClassLoader();

        LifecycleEvent slcEvent= new LifecycleEvent(this, eventType, data, this.leContext);
        try {
            slcl.handleEvent(slcEvent);
        } catch (ServerLifecycleException sle) {

            String msg = _rb.getString("lifecyclemodule.event_ServerLifecycleException");

            Object[] params = { this.name };
            msg = MessageFormat.format(msg, params);

            _logger.log(Level.WARNING, msg, sle);

            if (isFatal)
                throw sle;
        } catch (Exception ee) {
            String msg = _rb.getString("lifecyclemodule.event_Exception");

            Object[] params = { this.name };
            msg = MessageFormat.format(msg, params);

            _logger.log(Level.WARNING, msg, ee);

            if (isFatal) {
                throw new ServerLifecycleException(_rb.getString("lifecyclemodule.event_exceptionIsFatal"), ee);
            }
        }
    }
    
    public void onInitialization() 
                                throws ServerLifecycleException {
        postEvent(LifecycleEvent.INIT_EVENT, props);
    }

    public void onStartup()
                                    throws ServerLifecycleException {
        postEvent(LifecycleEvent.STARTUP_EVENT, props);
    }
    
    public void onReady() throws ServerLifecycleException {
        postEvent(LifecycleEvent.READY_EVENT, props);
    }

    public void onShutdown() throws ServerLifecycleException {
        postEvent(LifecycleEvent.SHUTDOWN_EVENT, props);
    }
    
    public void onTermination() throws ServerLifecycleException {
        postEvent(LifecycleEvent.TERMINATION_EVENT, props);
    }
    
    private void setClassLoader() {
         // set the url class loader as the thread context class loader
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(urlClassLoader);
                    return null;
                }
            }
        );
    }

    /**
     * return status of this lifecycle module as a string
     */
    public String getStatus() {
        return statusMsg;
    }

    public String toString() {
        return "Server LifecycleListener support";
    }
}
