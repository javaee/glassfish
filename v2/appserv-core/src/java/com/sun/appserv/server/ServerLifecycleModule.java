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

package com.sun.appserv.server;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.util.ResourceBundle;

import java.text.MessageFormat;

import com.sun.logging.LogDomains;
import com.sun.enterprise.Switch;
import com.sun.enterprise.InvocationException;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.ComponentInvocation;

import org.apache.catalina.Context;

import com.sun.enterprise.loader.ClassLoaderUtils;
import com.sun.enterprise.server.ServerContext;

/**
 * @author Sridatta Viswanath
 * @version $Id: ServerLifecycleModule.java,v 1.3 2005/12/25 04:13:01 tcfujii Exp $
 */

public final class ServerLifecycleModule {
    
    private LifecycleListener slcl;
    private String name;
    private String className;
    private String classpath;
    private int loadOrder;
    private boolean isFatal = true;
    private String statusMsg = "OK";
    
    private ServerContext ctx;
    private LifecycleEventContext leContext;
    private ClassLoader urlClassLoader;
    private Properties props = new Properties();

    private static Logger _logger = null;
    private static boolean _isTraceEnabled = false;
    private static ResourceBundle _rb = null;

    private ComponentInvocation lcmInvocation;

    ServerLifecycleModule(ServerContext ctx, String name, String className) {
        this.name = name;
        this.className = className;
        this.ctx = ctx;
        this.leContext = new LifecycleEventContextImpl(ctx);

        _logger = LogDomains.getLogger(LogDomains.ROOT_LOGGER);
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
    String getclasspath() {
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
            String msg = _rb.getString("lifecyclemodule.load_exception");
            Object[] params = { this.name, ee.toString() };
            msg = MessageFormat.format(msg, params);

            _logger.log(Level.WARNING, msg);
        }

        return slcl;
    }
    
    private URL[] getURLs() {
        return ClassLoaderUtils.getUrlsFromClasspath(this.classpath);
    }


    private void preInvoke(InvocationManager invMgr) 
                            throws ServerLifecycleException {
        try {
            invMgr.preInvoke(lcmInvocation);
        } catch (InvocationException ie) {
            String msg = _rb.getString("lifecyclemodule.preInvoke_exception");
            Object[] params = { this.name };
            msg = MessageFormat.format(msg, params);

            throw new ServerLifecycleException(msg, ie);
        }
    }

    private void postInvoke(InvocationManager invMgr)
                            throws ServerLifecycleException {
        try {
            invMgr.postInvoke(lcmInvocation);
        } catch (InvocationException ie) {
            String msg = _rb.getString("lifecyclemodule.postInvoke_exception");
            Object[] params = { this.name };
            msg = MessageFormat.format(msg, params);

            throw new ServerLifecycleException(msg, ie);
        }
    }

    private void postEvent(int eventType, Object data, InvocationManager invMgr)
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

        if (invMgr != null)
            preInvoke(invMgr);

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
        } finally {
            if (invMgr != null)
                postInvoke(invMgr);
        }
    }
    
    public void onInitialization(ServerContext context) 
                                throws ServerLifecycleException {
        postEvent(LifecycleEvent.INIT_EVENT, props, null);
    }

    public void onStartup(ServerContext context, Context invContext)
                                    throws ServerLifecycleException {

        /** create a ComponentInvocation for this module during startup; 
         *  as otherwise during Initialization ServerContext is not fully 
         *  established.
         */
        lcmInvocation = new ComponentInvocation(slcl, invContext);
        postEvent(LifecycleEvent.STARTUP_EVENT, null, ctx.getInvocationManager());
    }
    
    public void onReady(ServerContext context) throws ServerLifecycleException {
        postEvent(LifecycleEvent.READY_EVENT, null, ctx.getInvocationManager());
    }

    public void onShutdown() throws ServerLifecycleException {
        postEvent(LifecycleEvent.SHUTDOWN_EVENT, null, ctx.getInvocationManager());
    }
    
    public void onTermination() throws ServerLifecycleException {
        postEvent(LifecycleEvent.TERMINATION_EVENT, null, ctx.getInvocationManager());

        // clear the ComponentInvocation for this module
        lcmInvocation = null;
    }
    
    private void setClassLoader() {
         // set the common class loader as the thread context class loader
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
