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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Engine;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Embedded;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.catalina.logger.FileLogger;
import org.apache.tomcat.util.IntrospectionUtils;

//import org.openide.util.Lookup;
import org.glassfish.api.invocation.InvocationManager;
import org.jvnet.hk2.component.Habitat;

import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.WebBundleDescriptor; 
import com.sun.web.server.WebContainerListener;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.web.connector.coyote.PECoyoteConnector;
import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;


/**
 * Represents an embedded Catalina web container within the Application Server.
 *
 * This class is intended for use in versions of the application server
 * that *do not* have web-core as well as in versions that do. When used
 * with a web container that is integrated via the NSAPI j2ee-plugin, the 
 * virtual server implementation that <code>createHost</code> returns
 * is customized to "<code>com.sun.enterprise.web.NSAPIVirtualServer</code>".
 */
public final class EmbeddedWebContainer extends Embedded {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private Logger _logger = null;

    private WebContainerFeatureFactory webContainerFeatureFactory;

    private WebContainer webContainer;
    
    private ServerContext serverContext;

    private InvocationManager invocationManager;

    private InjectionManager injectionManager;

    /*
     * The value of the 'file' attribute of the log-service element
     */
    private String logServiceFile;

 
    // ------------------------------------------------------------ Constructor

    public EmbeddedWebContainer(Logger webLogger,
                                ServerContext serverContext,
                                WebContainer webContainer,
                                String logServiceFile) {
        super();
        _logger = webLogger;
        this.webContainer = webContainer;
        this.logServiceFile = logServiceFile;
        this.serverContext = serverContext;
        webContainerFeatureFactory = serverContext.getDefaultHabitat().getByContract(
                WebContainerFeatureFactory.class);      
        invocationManager = serverContext.getDefaultHabitat().getByContract(
                InvocationManager.class);
        injectionManager = serverContext.getDefaultHabitat().getByContract(
                InjectionManager.class);
    }
    

    // --------------------------------------------------------- Public Methods

    /**
     * Creates a virtual server.
     *
     * @param vsID Virtual server id
     * @param vsBean Bean corresponding to virtual-server element in domain.xml
     * @param vsDocRoot Virtual server docroot
     * @param vsMimeMap Virtual server MIME mappings
     *
     * @return The generated virtual server instance
     */
    public Host createHost(
                    String vsID,
                    com.sun.enterprise.config.serverbeans.VirtualServer vsBean,
                    String vsDocroot,
                    String vsLogFile,
                    MimeMap vsMimeMap) {

        //VirtualServer vs = webContainerFeatureFactory.getVirtualServer();
        VirtualServer vs = new VirtualServer();
        vs.setDebug(debug);
        vs.setAppBase(vsDocroot);
        vs.setName(vsID);
        vs.setID(vsID);
        vs.setBean(vsBean);
        vs.setMimeMap(vsMimeMap);

        String defaultContextXmlLocation = Constants.DEFAULT_CONTEXT_XML;
        String defaultWebXmlLocation = Constants.DEFAULT_WEB_XML;
    
        boolean allowLinking = false;
        String state = null;

        if (vsBean != null) {

            state = vsBean.getState();

            //Begin EE: 4920692 Make the default-web.xml be relocatable
            Property prop = ConfigBeansUtilities.getPropertyByName(vsBean, "default-web-xml");
            if (prop != null) {
                defaultWebXmlLocation = prop.getValue();
            }
            //End EE: 4920692 Make the default-web.xml be relocatable

            // allowLinking
            prop = ConfigBeansUtilities.getPropertyByName(vsBean, "allowLinking");
            if (prop != null) {
                allowLinking = Boolean.parseBoolean(prop.getValue());
            }

            prop = ConfigBeansUtilities.getPropertyByName(vsBean, "contextXmlDefault");
            if (prop != null) {
                defaultContextXmlLocation = prop.getValue();
            }

        }

        vs.setDefaultWebXmlLocation(defaultWebXmlLocation);
       
        vs.setDefaultContextXmlLocation(defaultContextXmlLocation);

        // Set vs state
        if (state == null) {
            state = VirtualServer.ON;
        }
        if (VirtualServer.DISABLED.equalsIgnoreCase(state)) {
            vs.setIsActive(false);
        } else {
            vs.setIsActive(Boolean.parseBoolean(state));
        }
        
        vs.setAllowLinking(allowLinking);

        if (vsLogFile != null && !vsLogFile.equals(logServiceFile)) {
            /*
             * Configure separate logger for this virtual server only if
             * 'log-file' attribute of this <virtual-server> and 'file'
             * attribute of <log-service> are different (See 6189219).
             */
            setLogFile(vs, vsLogFile);
        }
         
        ContainerListener listener = loadListener
                ("com.sun.enterprise.web.connector.extension.CatalinaListener");
        if ( listener != null )
            vs.addContainerListener(listener);     
        
        return vs;
    }
    
    /**
     * Create a web module/application.
     *
     * @param ctxPath  Context path for the web module
     * @param location Absolute pathname to the web module directory
     * @param defaultWebXmlLocation Location of default-web.xml
     */
    public Context createContext(String ctxPath, String location,
                                 String defaultContextXmlLocation,
                                 String defaultWebXmlLocation, 
                                 boolean useDOLforDeployment,
                                 WebBundleDescriptor wbd, String compEnvId) {

        File configFile = new File(location, Constants.WEB_CONTEXT_XML);
        WebModule context = new WebModule(webContainer);
        context.setDebug(debug);
        context.setPath(ctxPath);
        context.setDocBase(location);
        context.setCrossContext(true);
        context.setUseNaming(isUseNaming());
        context.setHasWebXml(wbd == null ? false : true);
        context.setWebBundleDescriptor(wbd);
        context.setManagerChecksFrequency(1);
        context.setComponentId(compEnvId);
        context.setServerContext(serverContext);
        //XXX memory support only at this point
        context.setManager(new StandardManager());

        if (configFile.exists()) {
            context.setConfigFile(configFile.getAbsolutePath());
        }
            
        ContextConfig config;
        if (useDOLforDeployment) {            
            config = new WebModuleContextConfig();  
            ((WebModuleContextConfig)config).setDescriptor(wbd);
        } else {
            config = new ContextConfig();
        }
        
        config.setDefaultContextXml(defaultContextXmlLocation);
        config.setDefaultWebXml(defaultWebXmlLocation);
        ((Lifecycle) context).addLifecycleListener(config);

        context.addLifecycleListener(new WebModuleListener(serverContext, 
                location, wbd));

        context.addInstanceListener(Constants.J2EE_INSTANCE_LISTENER);
        
        context.addContainerListener(
                new WebContainerListener(invocationManager, injectionManager));

        //context.addInstanceListener(
        //    "com.sun.enterprise.admin.monitor.callflow.WebContainerListener");
        
        return context;
    }

         
    /**
     * Util method to load classes that might get compiled after this class is
     * compiled.
     */
    private ContainerListener loadListener(String className){
        try{
            Class clazz = Class.forName(className);
            return (ContainerListener)clazz.newInstance();
        } catch (Throwable ex){
            _logger.log(Level.SEVERE,ex.getMessage() + ":" + className, ex);          
        }
        return null;
    }
    
   
    /**
     * Return the list of engines created (from Embedded API)
     */
    public Engine[] getEngines() {
        return engines;
    }

    /**
     * Returns the list of Connector objects associated with this 
     * EmbeddedWebContainer.
     *
     * @return The list of Connector objects associated with this 
     * EmbeddedWebContainer
     */
    public Connector[] getConnectors() {
        return connectors;
    }

    /*
     * Configures the given virtual server with the specified log file.
     *
     * @param vs The virtual server
     * @param logFile The value of the virtual server's log-file attribute in 
     * the domain.xml
     */
    protected void setLogFile(Host vs, String logFile) {

        String logPrefix = logFile;
        String logDir = null;
        String logSuffix = null;

        if (logPrefix == null || logPrefix.equals("")) {
            return;
        }

        int index = logPrefix.lastIndexOf(File.separatorChar);
        if (index != -1) {
            logDir = logPrefix.substring(0, index);
            logPrefix = logPrefix.substring(index+1);
        }
        
        index = logPrefix.indexOf('.');
        if (index != -1) {
            logSuffix = logPrefix.substring(index);
            logPrefix = logPrefix.substring(0, index);
        }

        logPrefix += "_";

        FileLogger contextLogger = new FileLogger();
        if (logDir != null) {
            contextLogger.setDirectory(logDir);
        }
        contextLogger.setPrefix(logPrefix);
        if (logSuffix != null) {
            contextLogger.setSuffix(logSuffix);
        }
        contextLogger.setTimestamp(true);

        vs.setLogger(contextLogger);
    }


    /**
     * Create a customized version of the Tomcat's 5 Coyote Connector. This
     * connector is required in order to support PE Web Programmatic login
     * functionality.
     * @param address InetAddress to bind to, or <code>null</code> if the
     * connector is supposed to bind to all addresses on this server
     * @param port Port number to listen to
     * @param protocol the http protocol to use.
     */
    public Connector createConnector(String address, int port,
				     String protocol) {

        if (address != null) {
            /*
             * InetAddress.toString() returns a string of the form
             * "<hostname>/<literal_IP>". Get the latter part, so that the
             * address can be parsed (back) into an InetAddress using
             * InetAddress.getByName().
             */
            int index = address.indexOf('/');
            if (index != -1) {
                address = address.substring(index + 1);
            }
        }

        _logger.log(Level.FINE,"Creating connector for address='" +
                  ((address == null) ? "ALL" : address) +
                  "' port='" + port + "' protocol='" + protocol + "'");

        WebConnector connector = new WebConnector();

        if (address != null) {
            connector.setAddress(address);
        }

        connector.setPort(port);

        if (protocol.equals("ajp")) {
            connector.setProtocolHandlerClassName(
                 "org.apache.jk.server.JkCoyoteHandler");
        } else if (protocol.equals("memory")) {
            connector.setProtocolHandlerClassName(
                 "org.apache.coyote.memory.MemoryProtocolHandler");
        } else if (protocol.equals("https")) {
            connector.setScheme("https");
            connector.setSecure(true);
        }

        return (connector);

    }
    

    /**
     * Create, configure, and return an Engine that will process all
     * HTTP requests received from one of the associated Connectors,
     * based on the specified properties.
     *
     * Do not create the JAAS default realm since all children will
     * have their own.
     */
    public Engine createEngine() {

        StandardEngine engine = new WebEngine(webContainer, _logger);

        engine.setDebug(debug);
        // Default host will be set to the first host added
        engine.setLogger(logger);       // Inherited by all children
        engine.setRealm(null);         // Inherited by all children
        
        //ContainerListener listener = loadListener
        //    ("com.sun.enterprise.admin.monitor.callflow.WebContainerListener");
        //if ( listener != null ) {
        //    engine.addContainerListener(listener);
        //}
        return (engine);

    }


    static class WebEngine extends StandardEngine {

        private WebContainer webContainer;
        private Logger _logger;
        private ResourceBundle _rb;

        public WebEngine(WebContainer webContainer, Logger _logger) {
            this.webContainer = webContainer;
            this._logger = _logger;
            this._rb = _logger.getResourceBundle();
        }

        public Realm getRealm(){
            return null;
        }

        /**
         * Starts the children (virtual servers) of this StandardEngine
         * concurrently.
         *
        protected void startChildren() {

            
            new File(webContainer.getAppsWorkRoot()).mkdirs();
            new File(webContainer.getModulesWorkRoot()).mkdirs();
            
            ArrayList<LifecycleStarter> starters
                = new ArrayList<LifecycleStarter>();

            Container children[] = findChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Lifecycle) {
                    LifecycleStarter starter =
                        new LifecycleStarter(((Lifecycle) children[i]));
                    starters.add(starter);
                    starter.submit();
                }
            }

            for (LifecycleStarter starter : starters) {
                Throwable t = starter.waitDone(); 
                if (t != null) {
                    Lifecycle container = starter.getContainer();
                    String msg = _rb.getString("embedded.startVirtualServerError");
                    msg = MessageFormat.format(msg, new Object[] { container });
                    _logger.log(Level.SEVERE, msg, t);
                }
            }
        }*/
    }
}
