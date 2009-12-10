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

package com.sun.enterprise.web;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.io.File;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.web.TldProvider;

import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.util.WebValidatorWithCL;
import com.sun.enterprise.web.Constants;
import com.sun.enterprise.web.jsp.JspProbeEmitterImpl;
import com.sun.enterprise.web.jsp.ResourceInjectorImpl;
import com.sun.logging.LogDomains;
import com.sun.appserv.web.cache.CacheManager;
import org.glassfish.loader.util.ASClassLoaderUtil;
//import com.sun.enterprise.server.PersistenceUnitLoaderImpl;
//import com.sun.enterprise.server.PersistenceUnitLoader;
//import com.sun.enterprise.config.ConfigException;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Jsp Servlet from sun-web.xml
 */

final class WebModuleListener
    implements LifecycleListener {

    /**
     * The logger used to log messages
     */
    private static final Logger _logger = LogDomains.getLogger(WebModuleListener.class, LogDomains.WEB_LOGGER);

    /**
     * This indicates whether debug logging is on or not
     */
    private static boolean _debugLog = _logger.isLoggable(Level.FINE);

    /**
     * Descriptor object associated with this web application.
     * Used for loading persistence units.
     */
    private WebBundleDescriptor wbd;

    /**
     * The exploded location for this web module.
     * Note this is not the generated location.
     */
    private File explodedLocation;
    
    private WebContainer webContainer;

    /**
     * Constructor.
     *
     * @param serverContext
     * @param explodedLocation The location where this web module is exploded
     * @param wbd descriptor for this module.
     */
    public WebModuleListener(WebContainer webContainer,
                             File explodedLocation,
                             WebBundleDescriptor wbd) {
        this.webContainer = webContainer;
        this.wbd = wbd;
        this.explodedLocation = explodedLocation;
    }


    /**
     * Process the START event for an associated WebModule
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the context we are associated with
        WebModule webModule;
        try {
            webModule = (WebModule) event.getLifecycle();
        } catch (ClassCastException e) {
            _logger.log(Level.WARNING, "webmodule.listener.classcastException",
                                        event.getLifecycle());
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
            // post processing DOL object for standalone web module
            if (wbd != null && wbd.getApplication() != null && 
                wbd.getApplication().isVirtual()) {
                wbd.setClassLoader(webModule.getLoader().getClassLoader());
                wbd.visit(new WebValidatorWithCL());
            }
            
            //loadPersistenceUnits(webModule);
            configureDefaultServlet(webModule);
            configureJsp(webModule);
            startCacheManager(webModule);
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            //unloadPersistenceUnits(webModule);
            stopCacheManager(webModule);
        }
    }


    //------------------------------------------------------- Private Methods

    /**
     * Configure all JSP related aspects of the web module, including
     * any relevant TLDs as well as the jsp config settings of the
     * JspServlet (using the values from sun-web.xml's jsp-config).
     */
    private void configureJsp(WebModule webModule) {

        webModule.getServletContext().setAttribute(
            "org.glassfish.jsp.isStandaloneWebapp",
            new Boolean(webModule.isStandalone()));

        // Find tld URI and set it to ServletContext attribute
        Collection<TldProvider> tldProviders =
            webContainer.getTldProviders();
        Map<URI, List<String>> tldMap = new HashMap<URI, List<String>>();
        for (TldProvider tldProvider : tldProviders) {
            // Skip any JSF related TLDs for non-JSF apps
            if ("jsfTld".equals(tldProvider.getName()) &&
                    !webModule.isJsfApplication()) {
                continue;
            }
            Map<URI, List<String>> tmap = tldProvider.getTldMap();
            if (tmap != null) {
                tldMap.putAll(tmap);
            }
        }
        webModule.getServletContext().setAttribute(
                "com.sun.appserv.tld.map", tldMap);

        /*
         * Discover all TLDs that are known to contain listener
         * declarations, and store the resulting map as a
         * ServletContext attribute
         */
        Map<URI, List<String>> tldListenerMap =
            new HashMap<URI, List<String>>();
        for (TldProvider tldProvider : tldProviders) {
            // Skip any JSF related TLDs for non-JSF apps
            if ("jsfTld".equals(tldProvider.getName()) &&
                    !webModule.isJsfApplication()) {
                continue;
            }
            Map<URI, List<String>> tmap = tldProvider.getTldListenerMap();
            if (tmap != null) {
                tldListenerMap.putAll(tmap);
            }
        }
        webModule.getServletContext().setAttribute(
            "com.sun.appserv.tldlistener.map", tldListenerMap);

        // set habitat for jsf injection
        webModule.getServletContext().setAttribute(
                Constants.HABITAT_ATTRIBUTE,
                webContainer.getServerContext().getDefaultHabitat());

        SunWebApp bean = webModule.getIasWebAppConfigBean();

        // Find the default jsp servlet
        Wrapper wrapper = (Wrapper) webModule.findChild(
            org.apache.catalina.core.Constants.JSP_SERVLET_NAME);
        if (wrapper == null) {
            return;
        }

        if (webModule.getTldValidation()) {
            wrapper.addInitParameter("enableTldValidation", "true");
        }
        if (bean != null && bean.getJspConfig()  != null) {
            WebProperty[]  props = bean.getJspConfig().getWebProperty();
            for (int i = 0; i < props.length; i++) {
                String pname = props[i].getAttributeValue("name");
                String pvalue = props[i].getAttributeValue("value");
                if (_debugLog) {
                    _logger.fine("jsp-config property for [" +
                                 webModule.getID() + "] is [" + pname +
                                 "] = [" + pvalue + "]");
                }
                wrapper.addInitParameter(pname, pvalue);
            }
        }
           
        // Override any log setting with the container wide logging level
        wrapper.addInitParameter("logVerbosityLevel",getJasperLogLevel());

        ResourceInjectorImpl resourceInjector = new ResourceInjectorImpl(
            webModule);
        webModule.getServletContext().setAttribute(
                "com.sun.appserv.jsp.resource.injector",
                resourceInjector);

        // START SJSAS 6311155
        String sysClassPath = ASClassLoaderUtil.getModuleClassPath(
            webContainer.getServerContext().getDefaultHabitat(),
            webModule.getID(), null
        );
        // If the configuration flag usMyFaces is set, remove jsf-api.jar
        // and jsf-impl.jar from the system class path
        Boolean useMyFaces = (Boolean)
            webModule.getServletContext().getAttribute(
                "com.sun.faces.useMyFaces");
        if (useMyFaces != null && useMyFaces) {
            sysClassPath =
                sysClassPath.replace("jsf-api.jar", "$disabled$.raj");
            sysClassPath =
                sysClassPath.replace("jsf-impl.jar", "$disabled$.raj");
            // jsf-connector.jar manifest has a Class-Path to jsf-impl.jar
            sysClassPath =
                sysClassPath.replace("jsf-connector.jar", "$disabled$.raj");
        }
        // TODO: combine with classpath from
        // servletContext.getAttribute(("org.apache.catalina.jsp_classpath")
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(" sysClasspath for " + webModule.getID() + " is \n"  +
                sysClassPath + "\n");
        }
        if (sysClassPath.equals("")) {
            // In embedded mode, habitat returns SingleModulesRegistry and
            // it has no modules.
            // Try "java.class.path" system property instead.
            sysClassPath = System.getProperty("java.class.path"); 
        }
        wrapper.addInitParameter("com.sun.appserv.jsp.classpath",
            sysClassPath);
        // END SJSAS 6311155

        // Configure JSP monitoring
        webModule.getServletContext().setAttribute(
            "org.glassfish.jsp.monitor.probeEmitter",
            new JspProbeEmitterImpl(webModule));

        // Pass BeanManager's ELResolver as ServletContext attribute
        // (see IT 11168)
        InvocationManager invocationMgr =
            webContainer.getInvocationManager();
        WebComponentInvocation inv = new WebComponentInvocation(webModule);
        try {
            invocationMgr.preInvoke(inv);
            InitialContext context = new InitialContext();
            BeanManager beanManager = (BeanManager)
                context.lookup("java:comp/BeanManager");
            if (beanManager != null) {
                webModule.getServletContext().setAttribute(
                    "org.glassfish.jsp.beanManagerELResolver",
                    beanManager.getELResolver());
            }
        } catch (NamingException e) {
            // Ignore
        } finally {
            invocationMgr.postInvoke(inv);
        }

    }

    /**
     * Determine the debug setting for JspServlet based on the iAS log
     * level.
     */
    private String getJasperLogLevel() {
        Level level = _logger.getLevel();
        if (level == null )
            return "warning";
        if (level.equals(Level.WARNING))
            return "warning";
        else if (level.equals(Level.FINE))
            return "information";
        else if (level.equals(Level.FINER) || level.equals(Level.FINEST))
            return "debug";
        else 
            return "warning";
    }

    private void startCacheManager(WebModule webModule) {

        SunWebApp bean  = webModule.getIasWebAppConfigBean();

        // Configure the cache, cache-mapping and other settings
        if (bean != null) {
            CacheManager cm = null;
            try {
                cm = CacheModule.configureResponseCache(webModule, bean);
            } catch (Exception ee) {
                _logger.log(Level.WARNING,
                           "webmodule.listener.cachemgrException", ee);
            }
        
            if (cm != null) {
                try {
                    // first start the CacheManager, if enabled
                    cm.start();
                    if (_debugLog) {
                        _logger.fine("Cache Manager started");
                    }
                    // set this manager as a context attribute so that 
                    // caching filters/tags can find it
                    ServletContext ctxt = webModule.getServletContext();
                    ctxt.setAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME, cm);

                } catch (LifecycleException ee) {
                    _logger.log(Level.WARNING, ee.getMessage(),
                                               ee.getCause());
                }
            }
        }
    }

    private void stopCacheManager(WebModule webModule) {
        ServletContext ctxt = webModule.getServletContext();
        CacheManager cm = (CacheManager)ctxt.getAttribute(
                                        CacheManager.CACHE_MANAGER_ATTR_NAME);
        if (cm != null) {
            try {
                cm.stop();
                if (_debugLog) {
                    _logger.fine("Cache Manager stopped");
                }
                ctxt.removeAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);
            } catch (LifecycleException ee) {
                _logger.log(Level.WARNING, ee.getMessage(), ee.getCause());
            }
        }
    }


    /**
     * Configures the given web module's DefaultServlet with the 
     * applicable web properties from sun-web.xml.
     */
    private void configureDefaultServlet(WebModule webModule) {

        // Find the DefaultServlet
        Wrapper wrapper = (Wrapper)webModule.findChild("default");
        if (wrapper == null) {
            return;
        }

        String servletClass = wrapper.getServletClassName();
        if (servletClass == null
                || !servletClass.equals(Globals.DEFAULT_SERVLET_CLASS_NAME)) {
            return;
        }

        String fileEncoding = webModule.getFileEncoding();
        if (fileEncoding != null) {
            wrapper.addInitParameter("fileEncoding", fileEncoding);
        }
    }
}
