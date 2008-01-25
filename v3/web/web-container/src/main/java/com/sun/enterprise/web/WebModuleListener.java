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
import java.util.ResourceBundle;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collection;
import java.text.MessageFormat;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.EntityManagerFactory;

import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.jasper.runtime.HttpJspBase;
import org.glassfish.api.deployment.MetaData;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.util.WebValidatorWithCL;
import com.sun.enterprise.deployment.util.WebBundleVisitor;
import com.sun.enterprise.server.ServerContext;
import com.sun.logging.LogDomains;
import com.sun.appserv.web.cache.CacheManager;
import com.sun.appserv.server.util.ASClassLoaderUtil;
import com.sun.appserv.BytecodePreprocessor;
//import com.sun.enterprise.server.PersistenceUnitLoaderImpl;
//import com.sun.enterprise.server.PersistenceUnitLoader;
import com.sun.enterprise.loader.InstrumentableClassLoader;
//import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Jsp Servlet from sun-web.xml
 */

final class WebModuleListener
    implements LifecycleListener {

    /**
     * The logger used to log messages
     */
    private static Logger _logger;

    /**
     * This indicates whether debug logging is on or not
     */
    private static boolean _debugLog;

    /**
     * The instance classpath, which is composed of the pathnames of
     * domain_root/lib/classes and domain_root/lib/[*.jar|*.zip] (in this
     * order), separated by the path-separator character.
     */
    private String instanceClassPath;

    /**
     * Descriptor object associated with this web application.
     * Used for loading persistence units.
     */
    private WebBundleDescriptor wbd;

    /**
     * The exploded location for this web module.
     * Note this is not the generated location.
     */
    private String explodedLocation;
    
    private ServerContext serverContext;
    
    private String javacClassPath= "";

    /**
     * Constructor.
     *
     * @param instanceClassPath The instance classpath, which is composed of
     * the pathnames of domain_root/lib/classes and
     * domain_root/lib/[*.jar|*.zip] (in this order), separated by the
     * path-separator character.
     * @param explodedLocation The location where this web module is exploded
     * @param wbd descriptor for this module.
     */
    public WebModuleListener(ServerContext serverContext,
                             String instanceClassPath,
                             String explodedLocation,
                             WebBundleDescriptor wbd) {
        this.serverContext = serverContext;
        this.instanceClassPath = instanceClassPath;
        this.wbd = wbd;
        this.explodedLocation = explodedLocation;
        
        WebDeployer webDeployer = serverContext.getDefaultHabitat().
                getComponent(WebDeployer.class);
        ModuleDefinition[] moduleDefs = webDeployer.getMetaData().getPublicAPIs();
        URI[] uris = null;
        for (int i=0; i<moduleDefs.length; i++) {
            uris = moduleDefs[i].getLocations();
            for (int j=0; j<uris.length; j++) {
                javacClassPath += uris[i].getPath() + File.pathSeparator;
            }
        }
        
        Module module = com.sun.enterprise.module.impl.ModuleImpl.find(Servlet.class);
        ModuleDefinition moduleDef = module.getModuleDefinition();
        uris = moduleDef.getLocations();
        javacClassPath = uris[0].getPath() + File.pathSeparator;
        for (int i=0; i<uris.length; i++) {
            javacClassPath += uris[i].getPath() + File.pathSeparator;
        }
        
        module = com.sun.enterprise.module.impl.ModuleImpl.find(javax.servlet.jsp.JspPage.class);
        moduleDef = module.getModuleDefinition();
        uris = moduleDef.getLocations();
        javacClassPath = uris[0].getPath() + File.pathSeparator;
        for (int i=0; i<uris.length; i++) {
            javacClassPath += uris[i].getPath() + File.pathSeparator;
        }
        
        module = com.sun.enterprise.module.impl.ModuleImpl.find(org.apache.jasper.runtime.HttpJspBase.class);
        moduleDef = module.getModuleDefinition();
        uris = moduleDef.getLocations();
        javacClassPath = uris[0].getPath() + File.pathSeparator;
        for (int i=0; i<uris.length; i++) {
            javacClassPath += uris[i].getPath() + File.pathSeparator;
        }
        
        
        javacClassPath += ".";
    }


    /**
     * Process the START event for an associated WebModule
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
            _debugLog = _logger.isLoggable(Level.FINE);
        }

        WebModule webModule;

        // Identify the context we are associated with
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
                wbd.visit((WebBundleVisitor) new WebValidatorWithCL());
            }
            
            //loadPersistenceUnits(webModule);
            configureDefaultServlet(webModule);
            configureJspParameters(webModule);
            startCacheManager(webModule);
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            //unloadPersistenceUnits(webModule);
            stopCacheManager(webModule);
        }
    }

    /*private void loadPersistenceUnits(final WebModule webModule) {
        _logger.logp(Level.FINE, "WebModuleListener", "loadPersistenceUnits",
                "wbd = {0} for {1}", new Object[]{wbd, webModule.getName()});
        if(wbd == null) {
            // for some system app like adminGUI, wbd is null
            return;
        }
        final Application application  = wbd.getApplication();
        // load PUs only for standaalone wars.
        // embedded wars are taken care of in ApplicationLoader.
        if(application != null && application.isVirtual()) {
            try{
                new PersistenceUnitLoaderImpl().load(new ApplicationInfoImpl(
                        explodedLocation, wbd, webModule));
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    private boolean unloadPersistenceUnits(final WebModule webModule) {
        _logger.logp(Level.FINE, "WebModuleListener", "unloadPersistenceUnits",
                "wbd = {0} for {1}", new Object[]{wbd, webModule.getName()});
        if(wbd == null) {
            // for some system app like adminGUI, wbd is null
            return true;
        }
        final Application application  = wbd.getApplication();
        // unload PUs only for standaalone wars.
        // embedded wars are taken care of in ApplicationLoader.
        if(application != null && application.isVirtual()) {
            try{
                new PersistenceUnitLoaderImpl().unload(new ApplicationInfoImpl(
                        explodedLocation, wbd, webModule));
            } catch(Exception e){
                _logger.log(Level.WARNING, e.getMessage(), e);
                return false;
            }
        }
        return true;
    }*/

    /**
     * implementation of
     * {@link com.sun.enterprise.server.PersistenceUnitLoader.ApplicationInfo}.
     *
    private static class ApplicationInfoImpl
            implements PersistenceUnitLoader.ApplicationInfo {
        private WebBundleDescriptor wbd;
        private String location;
        private InstrumentableClassLoader classLoader;
        public ApplicationInfoImpl(String location, WebBundleDescriptor wbd, WebModule wm) {
            this.wbd = wbd;
            this.location = location;
            this.classLoader = new InstrumentableWebappClassLoader(
                    WebappClassLoader.class.cast(wm.getLoader().getClassLoader()));
        }

        public Application getApplication() {
            return wbd.getApplication();
        }

        public InstrumentableClassLoader getClassLoader() {
            return classLoader;
        }

        public String getApplicationLocation() {
            return location;
        }

        /**
         * @return the precise collection of PUs that are referenced by this war
         *
        public Collection<? extends PersistenceUnitDescriptor>
                getReferencedPUs() {
            return wbd.findReferencedPUs();
        }

        /**
         * @return the list of EMFs that have been loaded for this war.
         *
        public Collection<? extends EntityManagerFactory> getEntityManagerFactories() {
            // since we are only responsible for standalone web module,
            // there is no need to search for EMFs in Application object.
            assert(wbd.getApplication().isVirtual());
            return wbd.getEntityManagerFactories();
        }

    } // class ApplicationInfoImpl*/

    /**
     * This class adapts WebappClassLoader to InstrumentableClassLoader that
     * is used by {@link PersistenceUnitLoader}.
     * It extends ClassLoader and overrides the public interfaces
     * of ClassLoader to delegate the calls to WebappClassLoader.
     */
    private static final class InstrumentableWebappClassLoader extends ClassLoader
            implements InstrumentableClassLoader {

        // the delegate
        private final WebappClassLoader webappClassLoader;

        public InstrumentableWebappClassLoader(WebappClassLoader webappClassLoader) {
            // set the delegate's parent as its parent
            super(webappClassLoader.getParent());
            this.webappClassLoader = webappClassLoader;
        }

        // implementation of InstrumentableClassLoader interface methods.

        public ClassLoader copy() {
            _logger.entering("WebModuleListener$InstrumentableWebappClassLoader", "copy");
            // set getParent() as the parent of the cloned class loader
            return new URLClassLoader(webappClassLoader.getURLs(), getParent());
        }

        public void addTransformer(final ClassTransformer transformer) {
            webappClassLoader.addByteCodePreprocessor(new BytecodePreprocessor(){
                /*
                 * This class adapts ClassFileTransformer to ByteCodePreprocessor that
                 * is used inside WebappClassLoader.
                 */

                public boolean initialize(Hashtable parameters) {
                    return true;
                }

                public byte[] preprocess(String resourceName, byte[] classBytes) {
                    try {
                        // convert java/lang/Object.class to java/lang/Object
                        String classname = resourceName.substring(0,
                                resourceName.length() - 6); // ".class" size = 6
                        byte[] newBytes = transformer.transform(
                                webappClassLoader, classname, null, null, classBytes);
                        // ClassFileTransformer returns null if no transformation
                        // took place, where as ByteCodePreprocessor is expected
                        // to return non-null byte array.
                        return newBytes == null ? classBytes : newBytes;
                    } catch (IllegalClassFormatException e) {
                        _logger.logp(Level.WARNING,
                                "WebModuleListener$InstrumentableClassLoader$BytecodePreprocessor",
                                "preprocess", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // override public interfaces of java.lang.ClassLoader
        // to delegate the calls to webappClassLoader

        @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
            return webappClassLoader.loadClass(name);
        }

        @Override public URL getResource(String name) {
            return webappClassLoader.getResource(name);
        }

        @Override public Enumeration<URL> getResources(String name)
                throws IOException {
            return webappClassLoader.getResources(name);
        }

        @Override public InputStream getResourceAsStream(String name) {
            return webappClassLoader.getResourceAsStream(name);
        }

        @Override public synchronized void setDefaultAssertionStatus(boolean enabled) {
            webappClassLoader.setDefaultAssertionStatus(enabled);
        }

        @Override public synchronized void setPackageAssertionStatus(
                String packageName, boolean enabled) {
            webappClassLoader.setPackageAssertionStatus(packageName, enabled);
        }

        @Override public synchronized void setClassAssertionStatus(
                String className, boolean enabled) {
            webappClassLoader.setClassAssertionStatus(className, enabled);
        }

        @Override public synchronized void clearAssertionStatus() {
            webappClassLoader.clearAssertionStatus();
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("InstrumentableWebappClassLoader\r\n");
            sb.append(" Parent -> ");
            sb.append(webappClassLoader);
            return sb.toString();
        }

    } // class InstrumentableWebappClassLoader

    //------------------------------------------------------- Private Methods

    /**
     * Configure the jsp config settings for the jspServlet  using the values
     * in sun-web.xml's jsp-config
     */
    private void configureJspParameters(WebModule webModule) {

        SunWebApp bean  = webModule.getIasWebAppConfigBean();

        // Find the default jsp servlet
        String name = webModule.findServletMapping(Constants.JSP_URL_PATTERN);
        Wrapper wrapper = (Wrapper)webModule.findChild(name);
        if (wrapper == null)
            return;

        String servletClass = wrapper.getServletClass();
        // If the jsp maps to the default JspServlet, then add 
        // the init parameters
        if (servletClass != null
                && servletClass.equals(Constants.APACHE_JSP_SERVLET_CLASS)) {

            if (webModule.getTldValidation()) {
                wrapper.addInitParameter("enableTldValidation", "true");
            }
            if (bean != null && bean.getJspConfig()  != null) {
                WebProperty[]  props = bean.getJspConfig().getWebProperty();
                for (int i = 0; i < props.length; i++) {
                    String pname = props[i].getAttributeValue("name");
                    String pvalue = props[i].getAttributeValue("value");
                    if (_debugLog) {
                        _logger.fine("jsp-config property for ["
                                     + webModule.getID() + "] is [" + pname
                                     + "] = [" + pvalue + "]");
                    }
                    wrapper.addInitParameter(pname, pvalue);
                }
            }
           
            // Override any log setting with the container wide logging level
            wrapper.addInitParameter("logVerbosityLevel",getJasperLogLevel());

            wrapper.addInitParameter("com.sun.appserv.jsp.resource.injector",
                                     "com.sun.enterprise.web.jsp.ResourceInjectorImpl");

            // START SJSAS 6311155
            String sysClassPath = ASClassLoaderUtil.getWebModuleClassPath(
                    serverContext.getDefaultHabitat(), webModule.getID());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine(" sysClasspath for " + webModule.getID() + " is \n" 
                                                               + sysClassPath + "\n");
            }
            if (instanceClassPath != null
                    && instanceClassPath.length() > 0) {
                sysClassPath += instanceClassPath;
            }
            if (javacClassPath != null
                    && javacClassPath.length() > 0) {
                sysClassPath += javacClassPath;
            }
            wrapper.addInitParameter("com.sun.appserv.jsp.classpath",
                                     sysClassPath);
            // END SJSAS 6311155
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
                                               ee.getThrowable());
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
                _logger.log(Level.WARNING, ee.getMessage(), ee.getThrowable());
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

        String servletClass = wrapper.getServletClass();
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
