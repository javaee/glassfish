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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.Properties;

import org.apache.catalina.Container;
import org.apache.catalina.Connector;
import org.apache.catalina.startup.Embedded;
import org.apache.catalina.Valve;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.coyote.tomcat5.CoyoteConnector;
import org.apache.tomcat.util.IntrospectionUtils;

//import com.sun.enterprise.config.ConfigContext;
//import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.ConnectionPool;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.HttpFileCache;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.Property; 
import com.sun.enterprise.config.serverbeans.RequestProcessing;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.Ssl;
//import com.sun.enterprise.config.serverbeans.Mime;

import com.sun.enterprise.server.ServerContext;
//import com.sun.enterprise.admin.common.PasswordConfReader;
import com.sun.enterprise.web.connector.coyote.PECoyoteConnector;
import com.sun.enterprise.web.logger.IASLogger;
import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.enterprise.util.StringUtils;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.ProxyHandler;

//dynamic reconfiguration
//import com.sun.enterprise.admin.event.EventListenerRegistry;
//import com.sun.enterprise.admin.event.http.HSHttpListenerEvent;
//import com.sun.enterprise.admin.event.http.HSVirtualServerEvent;

// monitoring imports
import com.sun.enterprise.admin.monitor.stats.HTTPListenerStats;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;

//import com.sun.enterprise.web.reconfig.ReconfigListener;

import com.sun.enterprise.web.stats.HTTPListenerStatsImpl;
import com.sun.enterprise.web.stats.PWCFileCacheStatsImpl;
import com.sun.enterprise.web.stats.PWCKeepAliveStatsImpl;
import com.sun.enterprise.web.stats.PWCThreadPoolStatsImpl;
import com.sun.enterprise.web.stats.PWCVirtualServerStatsImpl;
import com.sun.enterprise.web.stats.PWCConnectionQueueStatsImpl;
import com.sun.enterprise.web.stats.PWCRequestStatsImpl;

import com.sun.enterprise.security.CipherInfo;

/**
 * Represents the web container for PE and EE (since 9.0)
 */
public class PEWebContainer extends WebContainer {
    //implements MonitoringLevelListener {

    private PECoyoteConnector jkConnector;
 
    /**
     * Maps http-listener id to Tomcat Connector
     */
    private HashMap<String,PECoyoteConnector> connectorMap;
    
   
    /**
     * Allow disabling accessLog mechanism
     */
    private boolean globalAccessLoggingEnabled = true;
    
    
   /**
    * AccessLog buffer size for storing logs.
    */
   private String globalAccessLogBufferSize = null;
   
   
   /**
    * AccessLog interval before the valve flush its buffer to the disk.
    */
   private String globalAccessLogWriteInterval = null;  

   
    /**
     * The default-redirect port.
     */
    protected int defaultRedirectPort = -1;

    private static final String DEFAULT_KEYSTORE_TYPE = "JKS";
    private static final String DEFAULT_TRUSTSTORE_TYPE = "JKS";
    
   // --------------------------------------------------------- Constructor

    /**
     * This creates the embedded Catalina/Jasper container 
     * and sets the config properties on the container.
     */
    protected PEWebContainer(String id, ServerContext context) {

        //super(id, context);
    }

    /**
     * Initialize Tomcat internal objects.
     */
    protected void init(ServerContext context){ 

        connectorMap = new HashMap<String,PECoyoteConnector>();

        Config config = context.getDefaultHabitat().getComponent(Config.class);
        Server serverBean = context.getDefaultHabitat().getComponent(Server.class);
        
        
        // Create Tomcat Engine
        createEngine();

        HttpService httpService = config.getHttpService();
        configureNotSupported(httpService);

        createConnectors(httpService);
        createJKConnector(httpService);

        createHosts(httpService, config.getSecurityService(), globalAccessLoggingEnabled, serverBean);


        checkDefaultVirtualServerPort(httpService);

        //registerReconfigListeners(this);
    }


    /**
     * Starts the AJP connector that will listen to call from Apache using
     * mod_jk, mod_jk2 or mod_ajp.
     */
    private void createJKConnector(HttpService httpService) {

        String portString = System.getProperty("com.sun.enterprise.web.connector.enableJK");

        if (portString == null) {
            // do not create JK Connector if property is not set
            return;
        } else {
            int port = 8009;
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException ex) {
                // use default port 8009
                port = 8009;
            }

            jkConnector = (PECoyoteConnector)
                ((Embedded)_embedded).createConnector("0.0.0.0", port,
                                                      "ajp");

            configureJKProperties(jkConnector);

            String defaultHost = "server";
            jkConnector.setDefaultHost(defaultHost);        
            jkConnector.setDomain(_serverContext.getDefaultDomainName());
            jkConnector.setLogger(_logger); 
            jkConnector.setName("httpd-listener");
        
            configureHttpProtocol(jkConnector, httpService.getHttpProtocol());

            _logger.log(Level.INFO, "Apache mod_jk/jk2 attached to virtual-server "
                                + defaultHost + " listening on port: "
                                + portString);

            _embedded.addConnector(jkConnector);       
        }
    }


    /**
     * Enumerates the http-listener subelements of the given http-service
     * element, and creates a corresponding Tomcat Connector for each.
     *
     * @param httpService The http-service element
     */
    public void createConnectors(HttpService httpService) {
        
        // Attach http-listeners to Engine
        for (HttpListener httpListener : httpService.getHttpListener()) {
            if (!Boolean.getBoolean(httpListener.getEnabled())) {
                continue;
            }
            createConnector(httpListener,httpService);               
        }
        setDefaultRedirectPort(defaultRedirectPort);
    }

    
    /**
     * Use an http-listener subelements and creates a corresponding 
     * Tomcat Connector for each.
     *
     * @param httpService The http-service element
     * @param httpListener the configuration element.
     */       
    public PECoyoteConnector createConnector(HttpListener httpListener,
                                             HttpService httpService){
        if (!Boolean.getBoolean(httpListener.getEnabled())) {
            return null;
        }
                                 
        int port = 8080;
        PECoyoteConnector connector;
        
        checkHostnameUniqueness(httpListener.getId(), httpService);

        try {
            port = Integer.parseInt(httpListener.getPort());
        } catch (NumberFormatException nfe) {
            String msg = _rb.getString("pewebcontainer.http_listener.invalid_port");
            msg = MessageFormat.format(msg,
                                       new Object[] {httpListener.getPort(),
                                       httpListener.getId() });
            throw new IllegalArgumentException(msg);
        }

        /*
         * Create Connector. Connector is SSL-enabled if
         * 'security-enabled' attribute in <http-listener>
         * element is set to TRUE.
         */
        boolean isSecure = Boolean.getBoolean(httpListener.getSecurityEnabled());
        if (isSecure && defaultRedirectPort == -1) {
            defaultRedirectPort = port;
        }
        String address = httpListener.getAddress();
        if ("any".equals(address) || "ANY".equals(address)
                || "INADDR_ANY".equals(address)) {
            address = null;
            /*
             * Setting 'address' to NULL will cause Tomcat to pass a
             * NULL InetAddress argument to the java.net.ServerSocket
             * constructor, meaning that the server socket will accept
             * connections on any/all local addresses.
             */
        }

        connector = 
            (PECoyoteConnector)_embedded.createConnector(address,port,
                                                         isSecure);
        connector.setName(httpListener.getId());

        configureConnector(connector,httpListener,isSecure,httpService);

        if ( _logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE, "create.listenerport",
                new Object[] {Integer.valueOf(port), connector});
        }

        _embedded.addConnector(connector);

        connectorMap.put(httpListener.getId(), connector);       

        // If we already know the redirect port, then set it now
        // This situation will occurs when dynamic reconfiguration occurs
        if ( defaultRedirectPort != -1 ){
            connector.setRedirectPort(defaultRedirectPort);
        }

        return connector;
    }
    

    /**
     * Assigns the given redirect port to each Connector whose corresponding
     * http-listener element in domain.xml does not specify its own
     * redirect-port attribute.
     *
     * The given defaultRedirectPort corresponds to the port number of the 
     * first security-enabled http-listener in domain.xml.
     *
     * This method does nothing if none of the http-listener elements is
     * security-enabled, in which case Tomcat's default redirect port (443) 
     * will be used.
     *
     * @param defaultRedirectPort The redirect port to be assigned to any
     * Connector object that doesn't specify its own
     */
    private void setDefaultRedirectPort(int defaultRedirectPort) {
        if (defaultRedirectPort != -1) {
            Connector[] connectors = _embedded.getConnectors();
            for (int i=0; i<connectors.length; i++) {
                if (connectors[i].getRedirectPort() == -1) {
                    connectors[i].setRedirectPort(defaultRedirectPort);
                }
            } 
        }
    }


    /**
     * Makes sure that for each http-listener, the virtual server referenced
     * as the http-listener's default-virtual-server actually contains the
     * referencing http-listener in its http-listeners attribute
     */
    private void checkDefaultVirtualServerPort(HttpService httpService) {
        List<HttpListener> httpListeners = httpService.getHttpListener();
        if (httpListeners == null) {
            return;
        }

        for (HttpListener httpListener : httpListeners) {
            if (!Boolean.getBoolean(httpListener.getEnabled())) {
                continue;
            }
            int port = Integer.parseInt(httpListener.getPort());
            String defaultVsName = httpListener.getDefaultVirtualServer();
            VirtualServer defaultVs = (VirtualServer)
                _embedded.getEngines()[0].findChild(defaultVsName);
            if (defaultVs == null) {
                _logger.log(Level.SEVERE,
                            "pewebcontainer.defaultVsMissing",
                            new Object[] { defaultVsName, httpListener.getId()});
            }
 
            boolean found = false;
            int[] ports = defaultVs.getPorts();
            for (int i=0; i<ports.length; i++) {
                if (ports[i] == port) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                _logger.log(Level.SEVERE,
                            "pewebcontainer.defaultVsHttpListenerDisconnect",
                            new Object[] { defaultVsName, httpListener.getId() });
            }
    
        }
    }


    /*
     * Creates Tomcat Engine.
     */
    protected void createEngine(){

        String engineName = "com.sun.appserv";

        Engine engine = _embedded.createEngine();
        _embedded.addEngine(engine);

        ((StandardEngine)engine).setName(engineName);
        if (isTomcatUsingDefaultDomain()) {
            ((StandardEngine)engine).setDomain(
                        _serverContext.getDefaultDomainName());
        } else {
            ((StandardEngine)engine).setDomain(engineName);
        }
        _logger.log(Level.FINE, "Creating Engine " + engineName);
    }
    
    
    /**
     * Enumerates the virtual-server subelements of the given http-service
     * element, and creates a corresponding Tomcat Host for each.
     */
    public void createHost(
                    com.sun.enterprise.config.serverbeans.VirtualServer vse,
                    //ConfigContext configContext,
                    boolean enableMonitoring) { // throws ConfigException{
        
        Config config = _serverContext.getDefaultHabitat().getComponent(Config.class);

        Server serverBean = _serverContext.getDefaultHabitat().getComponent(Server.class);
        if (serverBean==null) {
            _logger.log(Level.SEVERE, "webcontainer.configError");
        }
       
        VirtualServer vs = createHost(config.getHttpService(),
                                      config.getSecurityService(),
                                      vse, 
                                      globalAccessLoggingEnabled,
                                      serverBean);
        if ( enableMonitoring ){             
            enableVirtualServerMonitoring(vs);
        }
    }  
    
    
    /**
     * Enumerates the virtual-server subelements of the given http-service
     * element, and creates a corresponding Tomcat Host for each.
     *
     * @param httpService The http-service element
     * @param securityService The security-service element
     * @param globalAccessLoggingEnabled The value of the accessLoggingEnabled
     * property of <http-service>
     */
    protected void createHosts(HttpService httpService,
                               SecurityService securityService,
                               boolean globalAccessLoggingEnabled,
                               Server serverBean) {

        for (com.sun.enterprise.config.serverbeans.VirtualServer vse : httpService.getVirtualServer()) {
            createHost(httpService, securityService, vse, globalAccessLoggingEnabled,
                       serverBean);
        }
    }
          
    
    /**
     * Creates a corresponding Tomcat Host from a virtual-server config bean.
     *
     * Note: if you add supports for a new property, you MUST also add the
     *       logic in the updateHostProperties method.
     *
     * @param httpService The http-service element
     * @param securityService The security-service element
     * @param vse The virtual-server configuration bean.
     * @param globalAccessLoggingEnabled The value of the accessLoggingEnabled
     * property of <http-service>
     */            
    protected VirtualServer createHost(
                       HttpService httpService,
                       SecurityService securityService,
                       com.sun.enterprise.config.serverbeans.VirtualServer vse,
                       boolean globalAccessLoggingEnabled,
                       Server serverBean){
            
        Engine[] engines = _embedded.getEngines();
        String docroot = null;
        MimeMap mm = null;
        String vs_id = vse.getId();

        Property element = ConfigBeansUtilities.getPropertyByName(vse, "docroot");
        if ( element != null){
            docroot = element.getValue();
        } else {
            docroot = vse.getDocroot();
        }
        
        validateDocroot(docroot,
                        vs_id,
                        vse.getDefaultWebModule());

        VirtualServer vs = createVS(vs_id, vse, docroot,
                                    vse.getLogFile(), mm,
                                    httpService.getHttpProtocol());

        // cache control
        Property cacheProp = ConfigBeansUtilities.getPropertyByName(vse, 
                                                    "setCacheControl");
        if ( cacheProp != null ){
            vs.configureCacheControl(cacheProp.getValue());   
        }

        /*PEAccessLogValve accessLogValve = vs.getAccessLogValve();
        boolean startAccessLog = accessLogValve.configure(
            vs_id, vse, httpService, domain, instance,
            _serverContext.getPluggableFeatureFactory().
                getWebContainerFeatureFactory(),
            globalAccessLogBufferSize, globalAccessLogWriteInterval);
        if (startAccessLog
                && vs.isAccessLoggingEnabled(globalAccessLoggingEnabled)) {
            vs.addValve(accessLogValve);
        }*/

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "Created virtual server " + vs_id);
        }

        /*
         * We must configure the Host with its associated port numbers and
         * alias names before adding it as an engine child and thereby
         * starting it, because a MapperListener, which is associated with
         * an HTTP listener and receives notifications about Host
         * registrations, relies on these Host properties in order to determine
         * whether a new Host needs to be added to the HTTP listener's Mapper.
         */
        configureHost(vs, httpService, securityService);

        // Add Host to Engine
        engines[0].addChild(vs);

        return vs;
    }
    
    
    /**
     * Validate the docroot properties of a virtual-server.
     */
    protected boolean validateDocroot(String docroot, String vs_id, 
                                      String defaultWebModule){
        
        // docroot vs default-web-module
        if (docroot != null ) {
            // If the docroot is invalid and there is no default module,
            // stop the process.
            boolean isValid = new File(docroot).exists();
            if ( !isValid && defaultWebModule == null){

                String msg = 
                    _rb.getString("pewebcontainer.virtual_server.invalid_docroot");
                msg = MessageFormat.format(msg, 
                                        new Object[] { vs_id , docroot});
                throw new IllegalArgumentException(msg);
            } else if (!isValid) {

                _logger.log(Level.WARNING, "virtual-server " + vs_id 
                            + " has an invalid docroot: " + docroot );
            }
        } else if (defaultWebModule == null) {
            String msg = _rb.getString("pewebcontainer.virtual_server.missing_docroot");
            msg = MessageFormat.format(msg, new Object[] { vs_id });
            throw new IllegalArgumentException(msg);
        }
        return true;
    }
    

    /**
     * Configures the given virtual server.
     *
     * @param vs The virtual server to be configured
     * @param httpService The http-service element of which the given
     * virtual server is a subelement
     * @param securityService The security-service element
     */
    protected void configureHost(VirtualServer vs,
                                 HttpService httpService,
                                 SecurityService securityService) {
        com.sun.enterprise.config.serverbeans.VirtualServer vsBean
            = vs.getBean();
       
        vs.configureAliases();

        // Set the ports with which this virtual server is associated
        List<String> listeners = StringUtils.parseStringList(
                                                vsBean.getHttpListeners(), ",");
        if (listeners == null) {
            return;
        }
        
        HttpListener[] httpListeners = new HttpListener[listeners.size()];
        for (int i=0; i < listeners.size(); i++){
            for (HttpListener httpListener : httpService.getHttpListener()) {
                if (httpListener.getId().equals(listeners.get(i))) {
                    httpListeners[i] = httpListener;
                }
            }
        }
                
        configureHostPortNumbers(vs, httpListeners);
        vs.configureCatalinaProperties();

        vs.configureAuthRealm(securityService);
    }
    
    
    /**
     * Configures the given virtual server with the port numbers of its
     * associated http listeners.
     *
     * @param vs The virtual server to configure
     * @param httpListeners The http listeners with which the given virtual
     * server is associated
     */
    protected void configureHostPortNumbers(VirtualServer vs,
                                            HttpListener[] httpListeners){
       
        boolean addJkListenerPort = (jkConnector != null
            && !vs.getName().equalsIgnoreCase(VirtualServer.ADMIN_VS));

        ArrayList<Integer> portsList = new ArrayList();
       
        for (int i=0; i < httpListeners.length; i++){
            if (Boolean.getBoolean(httpListeners[i].getEnabled())){
                PECoyoteConnector conn = connectorMap.get(
                    httpListeners[i].getId());
                if (conn != null) {
                    portsList.add(Integer.valueOf(conn.getPort()));
                }
            } else {
                if ((vs.getName().equalsIgnoreCase(VirtualServer.ADMIN_VS))) {
                    String msg = _rb.getString(
                        "pewebcontainer.httpListener.mustNotDisable");
                    msg = MessageFormat.format(
                        msg,
                        new Object[] { httpListeners[i].getId(),
                                       vs.getName() });
                    throw new IllegalArgumentException(msg);
                }
            }            
        }

        int numPorts = portsList.size();
        if (addJkListenerPort) {
            numPorts++;
        }
        if (numPorts > 0) {
            int[] ports = new int[numPorts];
            int i=0;
            for (i=0; i<portsList.size(); i++) {
                ports[i] = portsList.get(i).intValue();
            }
            if (addJkListenerPort) {
                ports[i] = jkConnector.getPort();
            }
            vs.setPorts(ports);
        }
    }
    
    
    /**
     * Log any unsupported domain.xml attribute or element.
     */
    private final void configureNotSupported(HttpService httpService){

        if (!_logger.isLoggable(Level.FINE)) {
            return;
        }
        
        if ( httpService.getHttpFileCache() != null ) {        
            if ( httpService.getHttpFileCache().getHashInitSize() != null) {
                _logger.log(Level.FINE,
                            "pewebcontainer.unsupportedAttribute.hash-init-size");
            }
        }

            for ( HttpListener httpListener: httpService.getHttpListener()){
                if ( httpListener.getFamily() != null ){
                    _logger.log(Level.FINE,
                            "pewebcontainer.unsupportedAttribute.family");            
                } 

            if ( httpListener.getExternalPort() != null ){
                _logger.log(
                    Level.FINE,
                    "pewebcontainer.unsupportedAttribute.external-port");
            }
        }
        
        if ( httpService.getHttpProtocol() != null ){
            if ( httpService.getHttpProtocol().getVersion() != null) {
                _logger.log(
                    Level.FINE,
                    "pewebcontainer.unsupportedAttribute.version");
            }

            if ( ConfigBeansUtilities.getPropertyByName(httpService.getHttpProtocol(), "ssl-enabled") 
                    != null) {
                _logger.log(Level.FINE,
                            "pewebcontainer.unsupportedAttribute.ssl-enabled");
            }   
        }
    }
    
    
    

    /**
     * Create a PEWebContainer object 
     */
    public static WebContainer createInstance(ServerContext context) {
        // Create the web container associated with this configuration
        webContainer = new PEWebContainer(new String("0"), context);
        _logger.log(Level.FINE, "Creating new instance of PEWebContainer.");
        
        // Initialize the Tomcat internal's object.
        ((PEWebContainer)webContainer).init(context);
        _logger.log(Level.FINE, "Initialization of PEWebContainer.");	
        return webContainer;
    }


    /**
     * Start the web container
     */
    public void startInstance() throws ServerLifecycleException {
        _logger.log(Level.INFO, "pewebcontainer.start");
/*
        try {
            //super.start();
         } catch (LifecycleException le) {

             // check if there is an embedded exception, if so, throw that
             Throwable ex = le.getThrowable();
             if (ex == null)
                 ex = le;

             String msg = _rb.getString("webcontainer.startError");
             throw new ServerLifecycleException(msg, ex);
        }
*/
        // the server has started up, now enable monitoring.
        enableVirtualServerMonitoring();
        enableHttpMonitoring();
    }


    /**
     * Stop the web container 
     */
    public void stopInstance() throws ServerLifecycleException {
        _logger.log(Level.INFO, "pewebcontainer.stop");
        try {
            super.stop();
        } catch (LifecycleException le) {
            // check if there is an embedded exception, if so, throw that
            Throwable ex = le.getThrowable();
            if (ex == null)
                ex = le;

            String msg = _rb.getString("webcontainer.stopError");
            throw new ServerLifecycleException(msg, ex);
        }
    }


    public static PEWebContainer getPEWebContainer() {         
        return (PEWebContainer)webContainer;
    }
 

    /*
     * Configures the given connector.
     *
     * @param connector The connector to configure
     * @param httpListener The http-listener that corresponds to the given
     * connector
     * @param isSecure true if the connector is security-enabled, false
     * otherwise
     * @param httpServiceProps The http-service properties
     */
    private void configureConnector(PECoyoteConnector connector,
                                    HttpListener httpListener,
                                    boolean isSecure,
                                    HttpService httpService) {

        configureConnectionPool(connector, httpService.getConnectionPool());

        WebContainerFeatureFactory wcFeatureFactory = _serverContext.getDefaultHabitat().getComponent(WebContainerFeatureFactory.class);
        String sslImplementationName = 
            webFeatureFactory.getSSLImplementationName();
        
        if (sslImplementationName != null) {
            connector.setProperty("sSLImplementation",sslImplementationName);
        }
        
        connector.setDomain(_serverContext.getDefaultDomainName());
        connector.setLogger(_logger);         
        
        configureSSL(connector, httpListener);
        configureKeepAlive(connector, httpService.getKeepAlive());
        configureHttpProtocol(connector, httpService.getHttpProtocol());     
        configureRequestProcessing(httpService.getRequestProcessing(),connector);
        configureFileCache(connector, httpService.getHttpFileCache());
        
        // default-virtual-server
        connector.setDefaultHost(httpListener.getDefaultVirtualServer());

        // xpoweredBy
        connector.setXpoweredBy(Boolean.getBoolean(httpListener.getXpoweredBy()));
        
        // Application root
        connector.setWebAppRootPath(getModulesRoot());
        
        // server-name (may contain scheme and colon-separated port number)
        String serverName = httpListener.getServerName();
        if (serverName != null && serverName.length() > 0) {
            // Ignore scheme, which was required for webcore issued redirects
            // in 8.x EE
            if (serverName.startsWith("http://")) {
                serverName = serverName.substring("http://".length());
            } else if (serverName.startsWith("https://")) {
                serverName = serverName.substring("https://".length());
            }
            int index = serverName.indexOf(':');
            if (index != -1) {
                connector.setProxyName(serverName.substring(0, index).trim());
                String serverPort = serverName.substring(index+1).trim();
                if (serverPort.length() > 0) {
                    try {
                        connector.setProxyPort(Integer.parseInt(serverPort));
                    } catch (NumberFormatException nfe) {
                        _logger.log(Level.SEVERE,
                            "pewebcontainer.invalid_proxy_port",
                            new Object[] { serverPort, httpListener.getId() });
		    }
                }
            } else {
                connector.setProxyName(serverName);
            }
        }

        boolean blockingEnabled = Boolean.valueOf(
                        httpListener.getBlockingEnabled());
        if (blockingEnabled){
            connector.setBlocking(blockingEnabled);
        }

        // redirect-port
        String redirectPort = httpListener.getRedirectPort();
        if (redirectPort != null && !redirectPort.equals("")) {
            try {
                connector.setRedirectPort(Integer.parseInt(redirectPort));
            } catch (NumberFormatException nfe) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_redirect_port",
                    new Object[] {
                        redirectPort,
                        httpListener.getId(),
                        Integer.toString(connector.getRedirectPort()) });
            }  
        } else {
            connector.setRedirectPort(-1);
        }

        // acceptor-threads
        String acceptorThreads = httpListener.getAcceptorThreads();
        if (acceptorThreads != null) {
            try {
                connector.setSelectorReadThreadsCount
                    (Integer.parseInt(acceptorThreads));
            } catch (NumberFormatException nfe) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_acceptor_threads",
                    new Object[] {
                        acceptorThreads,
                        httpListener.getId(),
                        Integer.toString(connector.getMaxProcessors()) });
            }  
        }
        
        // Configure Connector with keystore password and location
        if (isSecure) {
            configureConnectorKeysAndCerts(connector);
        }
        
        configureHttpServiceProperties(httpService,connector);      

        // Override http-service property if defined.
        configureHttpListenerProperties(httpListener,connector);
    }

    
    /**
     * Configure http-listener properties
     */
    public void configureHttpListenerProperties(HttpListener httpListener,
                                                PECoyoteConnector connector){
        // Configure Connector with <http-service> properties
        for (Property httpListenerProp  : httpListener.getProperty()) { 
            String propName = httpListenerProp.getName();
            String propValue = httpListenerProp.getValue();
            if (!configureHttpListenerProperty(propName,
                                               propValue,
                                               connector)){
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_http_listener_property",
                    propName);                    
            }
        }    
    }        
       
    
    /**
     * Configure http-listener property.
     * return true if the property exists and has been set.
     */
    protected boolean configureHttpListenerProperty(
                                            String propName, 
                                            String propValue,
                                            PECoyoteConnector connector)
                                            throws NumberFormatException {
                                                        
        if ("bufferSize".equals(propName)) {
            connector.setBufferSize(Integer.parseInt(propValue)); 
            return true; 
        } else if ("recycle-objects".equals(propName)) {
            connector
                .setRecycleObjects(ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("reader-threads".equals(propName)) {
            connector
                .setMaxReadWorkerThreads(Integer.parseInt(propValue));
            return true;            
        } else if ("acceptor-queue-length".equals(propName)) {
            connector
                .setMinAcceptQueueLength(Integer.parseInt(propValue));
            return true;            
        } else if ("reader-queue-length".equals(propName)) {
            connector
                .setMinReadQueueLength(Integer.parseInt(propValue));
            return true;            
        } else if ("use-nio-direct-bytebuffer".equals(propName)) {
            connector
                .setUseDirectByteBuffer(ConfigBeansUtilities.toBoolean(propValue));
            return true;   
        } else if ("maxKeepAliveRequests".equals(propName)) {
            connector
                .setMaxKeepAliveRequests(Integer.parseInt(propValue));
            return true;           
        } else if ("reader-selectors".equals(propName)) {
            connector
                .setSelectorReadThreadsCount(Integer.parseInt(propValue));
            return true;
        } else if ("authPassthroughEnabled".equals(propName)) {
            connector.setAuthPassthroughEnabled(
                                        ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("maxPostSize".equals(propName)) {
            connector.setMaxPostSize(Integer.parseInt(propValue));
            return true;
        } else if ("compression".equals(propName)) {
            connector.setProperty("compression",propValue);
            return true;
        } else if ("compressableMimeType".equals(propName)) {
            connector.setProperty("compressableMimeType",propValue);
            return true;       
        } else if ("noCompressionUserAgents".equals(propName)) {
            connector.setProperty("noCompressionUserAgents",propValue);
            return true;   
        } else if ("compressionMinSize".equals(propName)) {
            connector.setProperty("compressionMinSize",propValue);
            return true;             
        } else if ("restrictedUserAgents".equals(propName)) {
            connector.setProperty("restrictedUserAgents",propValue);
            return true;             
        } else if ("blocking".equals(propName)) {
            connector.setBlocking(ConfigBeansUtilities.toBoolean(propValue));
            return true;             
        } else if ("selectorThreadImpl".equals(propName)) {
            connector.setSelectorThreadImpl(propValue);
            return true;             
        } else if ("cometSupport".equals(propName)) {
            connector.setProperty(propName,ConfigBeansUtilities.toBoolean(propValue));
            return true;     
        } else if ("rcmSupport".equals(propName)) {
            connector.setProperty(propName,ConfigBeansUtilities.toBoolean(propValue));
            return true;    
        } else if ("connectionUploadTimeout".equals(propName)) {
            connector.setConnectionUploadTimeout(Integer.parseInt(propValue));
            return true;            
        } else if ("disableUploadTimeout".equals(propName)) {
            connector.setDisableUploadTimeout(ConfigBeansUtilities.toBoolean(propValue));
            return true;             
        } else if ("proxiedProtocols".equals(propName)) {
            connector.setProperty(propName,propValue);
            return true;              
        } else if ("proxyHandler".equals(propName)) {
            setProxyHandler(connector, propValue);
            return true;
        } else if ("uriEncoding".equals(propName)) {
            connector.setURIEncoding(propValue);
            return true;
        } else if ("chunkingDisabled".equals(propName)
                || "chunking-disabled".equals(propName)) {
            connector.setChunkingDisabled(ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("crlFile".equals(propName)) {
            connector.setCrlFile(propValue);
            return true;
        } else if ("trustAlgorithm".equals(propName)) {
            connector.setTrustAlgorithm(propValue);
            return true;
        } else if ("trustMaxCertLength".equals(propName)) {
            connector.setTrustMaxCertLength(propValue);
            return true;
        } else {
            return false;
        }   
    }   
    

    /**
     * Configure http-service properties.
     */
    public void configureHttpServiceProperties(HttpService httpService,
                                               PECoyoteConnector connector){
        // Configure Connector with <http-service> properties
        List<Property> httpServiceProps = httpService.getProperty();

        // Set default ProxyHandler impl, may be overriden by
        // proxyHandler property
        connector.setProxyHandler(new ProxyHandlerImpl());

        if (httpServiceProps != null) {
            for (Property httpServiceProp : httpServiceProps) {
                String propName = httpServiceProp.getName();
                String propValue = httpServiceProp.getValue();
                               
                if (configureHttpListenerProperty(propName,
                                                  propValue, 
                                                  connector)){
                    continue;
                }
                
                if ("connectionTimeout".equals(propName)) {
                    connector.setConnectionTimeout(
                                                Integer.parseInt(propValue));
                } else if ("tcpNoDelay".equals(propName)) {
                    connector.setTcpNoDelay(ConfigBeansUtilities.toBoolean(propValue));
                } else if ("traceEnabled".equals(propName)) {
                    connector.setAllowTrace(ConfigBeansUtilities.toBoolean(propValue));
                } else if (Constants.ACCESS_LOGGING_ENABLED.equals(propName)) {
                    globalAccessLoggingEnabled = ConfigBeansUtilities.toBoolean(propValue);
                } else if (Constants.ACCESS_LOG_WRITE_INTERVAL_PROPERTY.equals(
                                propName)) {
                    globalAccessLogWriteInterval = propValue;
                } else if (Constants.ACCESS_LOG_BUFFER_SIZE_PROPERTY.equals(
                                propName)) {
                    globalAccessLogBufferSize = propValue;
                } else if ("authPassthroughEnabled".equals(propName)) {
                    connector.setAuthPassthroughEnabled(
                                    ConfigBeansUtilities.toBoolean(propValue));
                } else if ("ssl-session-timeout".equals(propName)) {
                    connector.setSSLSessionTimeout(propValue);
                } else if ("ssl3-session-timeout".equals(propName)) {
                    connector.setSSL3SessionTimeout(propValue);
                } else if ("ssl-cache-entries".equals(propName)) {
                    connector.setSSLSessionCacheSize(propValue);
                } else if ("proxyHandler".equals(propName)) {
                    setProxyHandler(connector, propValue);
                } else if (Constants.SSO_ENABLED.equals(propName)) {
                    globalSSOEnabled = ConfigBeansUtilities.toBoolean(propValue);
                } else {
                    _logger.log(Level.WARNING,
                        "pewebcontainer.invalid_http_service_property",
                        httpServiceProp.getName());
                }
            }
        }    
    }


    /*
     * Parses the given comma-separated string of cipher suite names,
     * converts each cipher suite that is enabled (i.e., not preceded by a
     * '-') to the corresponding JSSE cipher suite name, and returns a string
     * of comma-separated JSSE cipher suite names.
     *
     * @param sslCiphers String of SSL ciphers to parse
     *
     * @return String of comma-separated JSSE cipher suite names, or null if
     * none of the cipher suites in the given string are enabled or can be
     * mapped to corresponding JSSE cipher suite names
     */
    private String getJSSECiphers(String ciphers) {

        String cipher = null;
        StringBuffer enabledCiphers = null;
        boolean first = true;

        int index = ciphers.indexOf(',');
        if (index != -1) {
            int fromIndex = 0;
            while (index != -1) {
                cipher = ciphers.substring(fromIndex, index).trim();
                if (cipher.length() > 0 && !cipher.startsWith("-")) {
                    if (cipher.startsWith("+")) {
                        cipher = cipher.substring(1);
		    }
                    String jsseCipher = getJSSECipher(cipher);
                    if (jsseCipher == null) {
                        _logger.log(Level.WARNING,
                            "pewebcontainer.unrecognized_cipher", cipher);
                    } else {
                        if (enabledCiphers == null) {
                            enabledCiphers = new StringBuffer();
                        }
                        if (!first) {
                            enabledCiphers.append(", ");
                        } else {
                            first = false;
                        }
                        enabledCiphers.append(jsseCipher);
                    }
                }
                fromIndex = index + 1;
                index = ciphers.indexOf(',', fromIndex);
            }
            cipher = ciphers.substring(fromIndex);
        } else {
            cipher = ciphers;
        }

        if (cipher != null) {
            cipher = cipher.trim();
            if (cipher.length() > 0 && !cipher.startsWith("-")) {
                if (cipher.startsWith("+")) {
                    cipher = cipher.substring(1);
                }
                String jsseCipher = getJSSECipher(cipher);
                if (jsseCipher == null) {
                    _logger.log(Level.WARNING,
                                "pewebcontainer.unrecognized_cipher", cipher);
                } else {
                    if (enabledCiphers == null) {
                        enabledCiphers = new StringBuffer();
                    }
                    if (!first) {
                        enabledCiphers.append(", ");
                    } else {
                        first = false;
                    }
                    enabledCiphers.append(jsseCipher);
                }
            }
        }

        return (enabledCiphers == null ? null : enabledCiphers.toString());
    }


    /*
     * Converts the given cipher suite name to the corresponding JSSE cipher.
     *
     * @param cipher The cipher suite name to convert
     *
     * @return The corresponding JSSE cipher suite name, or null if the given
     * cipher suite name can not be mapped
     */
    private String getJSSECipher(String cipher) {

        String jsseCipher = null;

        CipherInfo ci = CipherInfo.getCipherInfo(cipher);
        if( ci != null ) {
            jsseCipher = ci.getCipherName();
        }

        return jsseCipher;
    }
    

    /**
     * Registers various HTTP related monitoring stats.
     */
    private void enableHttpMonitoring(){

        String vsId;
        int port;
        HttpService httpService;
        
        ServerContext sc = getServerContext();
        Config config = _serverContext.getDefaultHabitat().getComponent(Config.class);
        MonitoringRegistry mReg = _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);

        httpService = config.getHttpService();
        if (httpService==null) {
            _logger.log(Level.WARNING,
                        "Unable to find HttpServiceBean in config");
            return;
        }

        // keep-alive
        try {
            mReg.registerPWCKeepAliveStats(
                new PWCKeepAliveStatsImpl(sc.getDefaultDomainName()),
                null);
        } catch (MonitoringRegistrationException mre) {
            String msg = _logger.getResourceBundle().getString(
                                            "web.monitoringRegistrationError");
            msg = MessageFormat.format(msg,
                                       new Object[] { "PWCKeepAliveStats" });
            _logger.log(Level.WARNING, msg, mre);
        }
        
        // file-cache
        try {
            mReg.registerPWCFileCacheStats(
                new PWCFileCacheStatsImpl(sc.getDefaultDomainName()),null);
        } catch (MonitoringRegistrationException mre) {
            String msg = _logger.getResourceBundle().getString(
                                            "web.monitoringRegistrationError");
            msg = MessageFormat.format(msg,
                                       new Object[] { "PWCFileCacheStats" });
            _logger.log(Level.WARNING, msg, mre);
        }

        // pwc-thread-pool
        try {
            mReg.registerPWCThreadPoolStats(
                new PWCThreadPoolStatsImpl(sc.getDefaultDomainName()),
                null);
        } catch (MonitoringRegistrationException mre) {
            String msg = _logger.getResourceBundle().getString(
                                            "web.monitoringRegistrationError");
            msg = MessageFormat.format(msg,
                                       new Object[] { "PWCThreadPoolStats" });
            _logger.log(Level.WARNING, msg, mre);
        }

        // connection-queue
        try {
            mReg.registerPWCConnectionQueueStats(
                new PWCConnectionQueueStatsImpl(sc.getDefaultDomainName()),
                null);
        } catch (MonitoringRegistrationException mre) {
            String msg = _logger.getResourceBundle().getString(
                                            "web.monitoringRegistrationError");
            msg = MessageFormat.format(msg,
                                       new Object[] { "PWCConnectionQueueStats" });
            _logger.log(Level.WARNING, msg, mre);
        }

        HttpListener currentListener; 
        for (com.sun.enterprise.config.serverbeans.VirtualServer vs : httpService.getVirtualServer()) {
            vsId = vs.getId();
            if(!vsId.equalsIgnoreCase(VirtualServer.ADMIN_VS)) {
               
                VirtualServer virtualServer = 
                        (VirtualServer)getEngines()[0].findChild(vsId);

                if (virtualServer == null){
                    _logger.log(Level.WARNING,
                                "Invalid virtual-server: " + vsId);
                    continue;
                }
                List listeners = StringUtils.parseStringList(
                                            vs.getHttpListeners(), ",");
                if(listeners != null) {
                    ListIterator iter = listeners.listIterator();
                    while(iter.hasNext()) {
                       
                        currentListener = null;
                        
                        for (HttpListener httpListener : httpService.getHttpListener()) {
                            if (httpListener.getId().equals(iter.next().toString())) {
                                currentListener = httpListener;
                            }
                        } 

			if (currentListener == null)
			    continue;
                        
                        enableHttpListenerMonitoring(virtualServer, 
                                Integer.parseInt(currentListener.getPort()),
                                currentListener.getId());
                    }
                }
            }
        }
    }
 
    
    /**
     * Register http-listener monitoring statistics.
     */
    protected void enableHttpListenerMonitoring(VirtualServer virtualServer,
            int port, String httpListenerId){
            
        PWCRequestStatsImpl pwcRequestStatsImpl = 
                virtualServer.getPWCRequestStatsImpl();
        
        if ( pwcRequestStatsImpl == null ){
            pwcRequestStatsImpl = new PWCRequestStatsImpl(
                    getServerContext().getDefaultDomainName());
            virtualServer.setPWCRequestStatsImpl(pwcRequestStatsImpl);
        }
 
        HTTPListenerStatsImpl httpStats;
        MonitoringRegistry mReg = getServerContext().getDefaultHabitat().getComponent(MonitoringRegistry.class);
        String vsId = virtualServer.getID();
        
        if (isTomcatUsingDefaultDomain()) {
            httpStats = new HTTPListenerStatsImpl(
                    getServerContext().getDefaultDomainName(),port);
        } else {
            httpStats = new HTTPListenerStatsImpl(vsId,port);
        }

        try {
            mReg.registerHttpListenerStats(httpStats, httpListenerId, vsId, null);
            pwcRequestStatsImpl.addHttpListenerStats(httpStats);
        } catch (MonitoringRegistrationException mre) {
            String msg =
                _logger.getResourceBundle().getString(
                    "web.monitoringRegistrationError");
            msg = MessageFormat.format(
                    msg,
                    new Object[] { "HTTPListenerStats" });
            _logger.log(Level.WARNING, msg, mre);
        }        
    }
    

    /*
     * Ensures that the host names of all virtual servers associated with the
     * HTTP listener with the given listener id are unique.
     *
     * @param listenerId The id of the HTTP listener whose associated virtual
     * servers are checked for uniqueness of host names
     * @param httpService The http-service element whose virtual servers are
     * checked
     */
    private void checkHostnameUniqueness(String listenerId,
                                         HttpService httpService) {

        ArrayList listenerVses = null;

        // Determine all the virtual servers associated with the given listener
        for (com.sun.enterprise.config.serverbeans.VirtualServer vse : httpService.getVirtualServer()) {
            List vsListeners =
                StringUtils.parseStringList(vse.getHttpListeners(), ",");
            for (int j=0; vsListeners!=null && j<vsListeners.size(); j++) {
                if (listenerId.equals((String)vsListeners.get(j))) {
                    if (listenerVses == null) {
                        listenerVses = new ArrayList();
                    }
                    listenerVses.add(vse);
                    break;
                }
            }
        }
        if (listenerVses == null) {
            return;
        }

        for (int i=0; i<listenerVses.size(); i++) {
            com.sun.enterprise.config.serverbeans.VirtualServer vs
                = (com.sun.enterprise.config.serverbeans.VirtualServer) listenerVses.get(i);
            List hosts = StringUtils.parseStringList(vs.getHosts(), ",");
            for (int j=0; hosts!=null && j<hosts.size(); j++) {
                String host = (String) hosts.get(j);
                for (int k=0; k<listenerVses.size(); k++) {
                    if (k <= i) {
                        continue;
                    }
                    com.sun.enterprise.config.serverbeans.VirtualServer otherVs
                        = (com.sun.enterprise.config.serverbeans.VirtualServer)
                            listenerVses.get(k);
                    List otherHosts = StringUtils.parseStringList(otherVs.getHosts(), ",");
                    for (int l=0; otherHosts!=null && l<otherHosts.size(); l++) {
                        if (host.equals((String) otherHosts.get(l))) {
                            _logger.log(Level.SEVERE,
                                        "pewebcontainer.duplicate_host_name",
                                        new Object[] { host, vs.getId(),
                                                       otherVs.getId(),
                                                       listenerId });
                        }
                    }
		}    
            }        
        }            
    }


    /**
     * is Tomcat using default domain name as its domain
     */
    protected boolean isTomcatUsingDefaultDomain() {
        // need to be careful and make sure tomcat jmx mapping works
        // since setting this to true might result in undeployment problems
        return true;
    }


    /**
     * Overrides the implementation of this method in the WebContainer.java
     * superclass by doing nothing.
     *
     * This is to prevent web modules that are bundled in EARs from being
     * loaded twice during startup, since they are already being loaded by
     * com.sun.enterprise.server.TomcatApplicationLoader.load(). See 4925655.
     */
    protected void loadAllJ2EEApplicationWebModules() {
        // Do nothing
    }
    

    /*
     * Enables monitoring of all virtual servers.
     */
    private void enableVirtualServerMonitoring() {
        Engine[] engines = _embedded.getEngines();
        for (int j = 0; j < engines.length; j++) {
            Container[] hostArray = engines[j].findChildren();
            for (int i = 0; i < hostArray.length; i++) {
                VirtualServer vs = (VirtualServer) hostArray[i];
                enableVirtualServerMonitoring(vs);            
            }
        }
    }

    
    /*
     * Enables monitoring of all virtual servers.
     */
    private void enableVirtualServerMonitoring(VirtualServer vs){        
        ServerContext sc = getServerContext();
        MonitoringRegistry monitoringRegistry = sc.getDefaultHabitat().getComponent(MonitoringRegistry.class);

        
        PWCVirtualServerStatsImpl vsStats = new PWCVirtualServerStatsImpl(vs);
        try {
            monitoringRegistry.registerPWCVirtualServerStats(vsStats,
                                                             vs.getID(),
                                                             null);
        } catch (Exception e) {
            _logger.log(Level.WARNING,
                        "Unable to register PWCVirtualServerStats for "
                        + vs.getID(), e);
        }

        PWCRequestStatsImpl pwcRequestStatsImpl = 
                new PWCRequestStatsImpl(sc.getDefaultDomainName());  
        vs.setPWCRequestStatsImpl(pwcRequestStatsImpl);
        
        try {
            monitoringRegistry.registerPWCRequestStats(pwcRequestStatsImpl,
                        vs.getID(),
                        null);
        } catch (MonitoringRegistrationException mre) {
            String msg = _logger.getResourceBundle().getString(
                            "web.monitoringRegistrationError");
            msg = MessageFormat.format(
                            msg,
                            new Object[] { "PWCRequestStats" });
            _logger.log(Level.WARNING, msg, mre);
        }
    }
    
    /*
     * Configures the SSL properties on the given PECoyoteConnector from the
     * SSL config of the given HTTP listener.
     *
     * @param connector PECoyoteConnector to configure
     * @param httpListener HTTP listener whose SSL config to use
     */
    private void configureSSL(PECoyoteConnector connector,
                              HttpListener httpListener) {

        Ssl sslConfig = httpListener.getSsl();
        if (sslConfig == null) {
            return;
        }

        // client-auth
        if (Boolean.getBoolean(sslConfig.getClientAuthEnabled())) {
            connector.setClientAuth(true);
        }

        // ssl protocol variants
        StringBuffer sslProtocolsBuf = new StringBuffer();
        boolean needComma = false;
        if (Boolean.getBoolean(sslConfig.getSsl2Enabled())) {
            sslProtocolsBuf.append("SSLv2");
            needComma = true;
        }
        if (Boolean.getBoolean(sslConfig.getSsl3Enabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            } else {
                needComma = true;
            }
            sslProtocolsBuf.append("SSLv3");
        }
        if (Boolean.getBoolean(sslConfig.getTlsEnabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            }
            sslProtocolsBuf.append("TLSv1");
        }
        if (Boolean.getBoolean(sslConfig.getSsl3Enabled()) || Boolean.getBoolean(sslConfig.getTlsEnabled())) {
            sslProtocolsBuf.append(", SSLv2Hello");
        }

        if (sslProtocolsBuf.length() == 0) {
            _logger.log(Level.WARNING,
                        "pewebcontainer.all_ssl_protocols_disabled",
                        httpListener.getId());
        } else {
            connector.setSslProtocols(sslProtocolsBuf.toString());
        }

        // cert-nickname
        String certNickname = sslConfig.getCertNickname();
        if (certNickname != null && certNickname.length() > 0) {
            connector.setKeyAlias(sslConfig.getCertNickname());
        }

        // ssl3-tls-ciphers
        String ciphers = sslConfig.getSsl3TlsCiphers();
        if (ciphers != null) {
            String jsseCiphers = getJSSECiphers(ciphers);
            if (jsseCiphers == null) {
                _logger.log(Level.WARNING,
                            "pewebcontainer.all_ciphers_disabled",
                            httpListener.getId());
            } else {
                connector.setCiphers(jsseCiphers);
            }
        }            
    }


    /*
     * Configures the keep-alive properties on the given PECoyoteConnector
     * from the given keep-alive config.
     *
     * @param connector PECoyoteConnector to configure
     * @param keepAlive Keep-alive config to use
     */
    private void configureKeepAlive(PECoyoteConnector connector,
                                    KeepAlive keepAlive) {

        // timeout-in-seconds, default is 60 as per sun-domain_1_1.dtd
        int timeoutInSeconds = 60;

        // max-connections, default is 256 as per sun-domain_1_1.dtd
        int maxConnections = 256;

        // thread-count, default is 1 as per sun-domain_1_1.dtd
        int threadCount = 1;

        if (keepAlive != null) {
            // timeout-in-seconds
            try {
	        timeoutInSeconds = Integer.parseInt(
                                keepAlive.getTimeoutInSeconds());
            } catch (NumberFormatException ex) {
                String msg = _rb.getString(
                    "pewebcontainer.invalidKeepAliveTimeout");
                msg = MessageFormat.format(
                    msg,
                    new Object[] { keepAlive.getTimeoutInSeconds(),
                                   Integer.toString(timeoutInSeconds)});
                _logger.log(Level.WARNING, msg, ex);
            }

            // max-connections
            try {
	        maxConnections = Integer.parseInt(
                                keepAlive.getMaxConnections());
            } catch (NumberFormatException ex) {
                String msg = _rb.getString(
                    "pewebcontainer.invalidKeepAliveMaxConnections");
                msg = MessageFormat.format(
                    msg,
                    new Object[] { keepAlive.getMaxConnections(),
                                   Integer.toString(maxConnections)});
                _logger.log(Level.WARNING, msg, ex);
            }

            // thread-count
            try {
	        threadCount = Integer.parseInt(keepAlive.getThreadCount());
            } catch (NumberFormatException ex) {
                String msg = _rb.getString(
                    "pewebcontainer.invalidKeepAliveThreadCount");
                msg = MessageFormat.format(
                    msg,
                    new Object[] { keepAlive.getThreadCount(),
                                   Integer.toString(threadCount)});
                _logger.log(Level.WARNING, msg, ex);
            }
        }
        
        connector.setKeepAliveTimeoutInSeconds(timeoutInSeconds);
        connector.setMaxKeepAliveRequests(maxConnections);
        connector.setKeepAliveThreadCount(threadCount);
    }
    

    /*
     * Configures the given HTTP connector with connection-pool related info.
     */
    private void configureConnectionPool(PECoyoteConnector connector,
                                         ConnectionPool cp) {
        if (cp == null) {
            return;
        }
            
        try{
            int queueSizeInBytes = 
                    Integer.parseInt(cp.getQueueSizeInBytes());
            if (queueSizeInBytes <= -1){
                _logger.log(
                    Level.WARNING,
                    "pewebcontainer.invalidQueueSizeInBytes",
                    new Object[] 
                        { cp.getQueueSizeInBytes(),
                          Integer.toString(
                                  connector.getQueueSizeInBytes())});
            } else {
                connector.setQueueSizeInBytes(queueSizeInBytes);
            }
        } catch (NumberFormatException ex){
            String msg = _rb.getString("pewebcontainer.invalidQueueSizeInBytes");
            msg = MessageFormat.format(
                msg, new Object[] 
                    { ConfigBeansUtilities.getDefaultQueueSizeInBytes(),
                      Integer.toString(connector.getQueueSizeInBytes())});
            _logger.log(Level.WARNING, msg, ex);
        }
        
        
        try{
            int ssBackLog = Integer.parseInt(cp.getMaxPendingCount());
            if (ssBackLog <= 0){
                _logger.log(
                    Level.WARNING,
                    "pewebcontainer.invalidMaxPendingCount",
                    new Object[] 
                        { cp.getMaxPendingCount(),
                          Integer.toString(connector.getSocketServerBacklog())});
            } else {
                connector.setSocketServerBacklog(ssBackLog);
            }
        } catch (NumberFormatException ex){
            String msg = _rb.getString("pewebcontainer.invalidMaxPendingCount");
            msg = MessageFormat.format(
                msg, new Object[] 
                    { cp.getMaxPendingCount(),
                      Integer.toString(connector.getSocketServerBacklog())});
            _logger.log(Level.WARNING, msg, ex);
        }
        
        
        try{
            int bufferSize = 
                        Integer.parseInt(cp.getReceiveBufferSizeInBytes());
            if ( bufferSize <= 0 ){
                _logger.log(
                    Level.WARNING,
                    "pewebcontainer.invalidBufferSize",
                    new Object[] 
                        { cp.getReceiveBufferSizeInBytes(),
                          Integer.toString(connector.getBufferSize())});
            } else {
                connector.setBufferSize(bufferSize);
            }
        } catch (NumberFormatException ex) {
            String msg = _rb.getString("pewebcontainer.invalidBufferSize");
            msg = MessageFormat.format(
                msg, new Object[] 
                    { cp.getReceiveBufferSizeInBytes(),
                      Integer.toString(connector.getBufferSize())});
            _logger.log(Level.WARNING, msg, ex);
        }

        try{
            int maxHttpHeaderSize = 
                          Integer.parseInt(cp.getSendBufferSizeInBytes());
            if ( maxHttpHeaderSize <= 0 ){
                _logger.log(
                    Level.WARNING,
                    "pewebcontainer.invalidMaxHttpHeaderSize",
                    new Object[] 
                        { cp.getSendBufferSizeInBytes(),
                          Integer.toString(connector.getMaxHttpHeaderSize())});
            } else {
                connector.setMaxHttpHeaderSize(maxHttpHeaderSize);
            }
        } catch (NumberFormatException ex){
            String msg = _rb.getString(
                "pewebcontainer.invalidMaxHttpHeaderSize");
            msg = MessageFormat.format(
                msg, new Object[] 
                    { cp.getSendBufferSizeInBytes(),
                      Integer.toString(connector.getMaxHttpHeaderSize())});
            _logger.log(Level.WARNING, msg, ex);
        }
    }


    /** 
     * Registers listeners for dynamic reconfiguration with the
     * AdminEventListenerRegistry
     *
    public static void registerReconfigListeners(PEWebContainer peweb){
        ReconfigListener reconfigListener = new ReconfigListener(peweb);
        AdminEventListenerRegistry.addEventListener(
                                    HSVirtualServerEvent.eventType, 
                                    reconfigListener); 
        AdminEventListenerRegistry.addEventListener(
                                    HSHttpListenerEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSAccessLogEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSServiceEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSHttpProtocolEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSHttpFileCacheEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSConnectionPoolEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSKeepAliveEvent.eventType, 
                                    reconfigListener);
        AdminEventListenerRegistry.addEventListener(
                                    HSRequestProcessingEvent.eventType, 
                                    reconfigListener);
    }*/
    
    
    /** 
     * Delete virtual-server.
     * @param httpService element which contains the configuration info.
     */
    public void deleteHost(HttpService httpService) throws LifecycleException{
    
        Engine[] engines = _embedded.getEngines();
        VirtualServer virtualServer;
        // First we need to find which virtual-server was deleted. In
        // reconfig/VirtualServerReconfig, it is impossible to lookup
        // the vsBean because the element is removed from domain.xml
        // before handleDelete is invoked.
        Container[] virtualServers = engines[0].findChildren();
        for (int i=0;i < virtualServers.length; i++){
            for (com.sun.enterprise.config.serverbeans.VirtualServer vse : httpService.getVirtualServer()) {
                if ( virtualServers[i].getName().equals(vse.getId())){
                    virtualServers[i] = null;
                    break;
                }
            }
        }       
        
        for (int i=0;i < virtualServers.length; i++){
            virtualServer = (VirtualServer)virtualServers[i];
            
            if (virtualServer != null ){           
                if (virtualServer.getID().equals(VirtualServer.ADMIN_VS)) {
                    throw new 
                      LifecycleException("Cannot delete admin virtual-server.");
                }     

                Container[] webModules = virtualServer.findChildren();
                for (int j=0; j < webModules.length; j++){
                    unloadWebModule(webModules[j].getName(),
                                    webModules[j].getName(), 
                                    virtualServer.getID(),
                                    null);
                }
                try {                
                    virtualServer.destroy();
                } catch (Exception e) {
                    _logger.log(Level.WARNING,
                                "Error during destruction of virtual server "
                                + virtualServer.getID(), e);
                }
            }
        }
    }
    
    
    /**
     * Updates a virtual-server element.
     *
     * @param vsBean the virtual-server config bean.
     * @param httpService element which contains the configuration info.
     */
    public void updateHost(
                    com.sun.enterprise.config.serverbeans.VirtualServer vsBean,
                    HttpService httpService,
                    Server serverBean) 
                throws LifecycleException {

        Engine[] engines = _embedded.getEngines();
        VirtualServer virtualServer = 
            (VirtualServer)engines[0].findChild(vsBean.getId());

        // Must retrieve the old default-web-module before updating the
        // virtual server with the new vsBean, because default-web-module is
        // read from vsBean
        String oldDefaultWebModule = virtualServer.getDefaultWebModuleID();

        virtualServer.setBean(vsBean);

        _embedded.setLogFile(virtualServer,vsBean.getLogFile());

        virtualServer.configureVirtualServerState();
        
        virtualServer.clearAliases();
        virtualServer.configureAliases();

        String docroot = vsBean.getDocroot();
        if (docroot != null) {
            updateDocroot(docroot, virtualServer, vsBean);
        }

        int[] oldPorts = virtualServer.getPorts();

        List<String> listeners = StringUtils.parseStringList(
            vsBean.getHttpListeners(), ",");
        if (listeners != null) {
            HttpListener[] httpListeners = new HttpListener[listeners.size()];
            for (int i=0; i < listeners.size(); i++){
                for (HttpListener httpListener : httpService.getHttpListener()) {
                    if (httpListener.getId().equals(listeners.get(i))) {
                        httpListeners[i] = httpListener;
                    }
                }
            }
            // Update the port numbers with which the virtual server is
            // associated
            configureHostPortNumbers(virtualServer, httpListeners); 
        } else {
            // The virtual server is not associated with any http listeners
            virtualServer.setPorts(new int[0]);
        }

        int[] newPorts = virtualServer.getPorts();

        // Disassociate the virtual server from all http listeners that
        // have been removed from its http-listeners attribute
        for (int i=0; i<oldPorts.length; i++) {
            boolean found = false;
            for (int j=0; j<newPorts.length; j++) {
                if (oldPorts[i] == newPorts[j]) {
                    found = true;
                }
            }
            if (!found) {
                // http listener was removed
                Connector[] connectors = _embedded.findConnectors();
                for (int k=0; k<connectors.length; k++) {
                    PECoyoteConnector conn = (PECoyoteConnector)
                        connectors[k];
                    if (oldPorts[i] == conn.getPort()) {
                        try {
                             conn.getMapperListener().unregisterHost(
                                virtualServer.getJmxName());
                        } catch (Exception e) {
                            throw new LifecycleException(e);
                        }
                    } 
                }
                
            }
        }

        // Associate the virtual server with all http listeners that
        // have been added to its http-listeners attribute
        for (int i=0; i<newPorts.length; i++) {
            boolean found = false;
            for (int j=0; j<oldPorts.length; j++) {
                if (newPorts[i] == oldPorts[j]) {
                    found = true;
                }
            }
            if (!found) {
                // http listener was added
                Connector[] connectors = _embedded.findConnectors();
                for (int k=0; k<connectors.length; k++) {
                    PECoyoteConnector conn = (PECoyoteConnector)
                        connectors[k];
                    if (newPorts[i] == conn.getPort()) {
                        if (!conn.isAvailable()){
                            conn.start();
                            enableHttpListenerMonitoring(
                                virtualServer,
                                conn.getPort(),
                                conn.getName());
                        }
                        try {
                            conn.getMapperListener().registerHost(
                                virtualServer.getJmxName());
                        } catch (Exception e) {
                            throw new LifecycleException(e);
                        }
                    }
                }
            } 
        }

        // Remove the old default web module if one was configured, by
        // passing in "null" as the default context path
        if (oldDefaultWebModule != null) {
            updateDefaultWebModule(virtualServer, oldPorts, null);
        }

        // Add the new default web module
        String newDefaultContextPath = virtualServer.getDefaultContextPath(
            serverBean);
        if (newDefaultContextPath != null) {
            // Remove dummy context that was created off of docroot, if such
            // a context exists
            removeDummyModule(virtualServer);
            updateDefaultWebModule(virtualServer,
                                   virtualServer.getPorts(),
                                   newDefaultContextPath);
        } else {
            WebModuleConfig wmc = 
                virtualServer.createSystemDefaultWebModuleIfNecessary();
            if ( wmc != null) {
                loadStandaloneWebModule(virtualServer,wmc);
            }
        } 
    }
    
    
    /**
     * Update virtual-server properties.
     */
    public void updateHostProperties(
                    com.sun.enterprise.config.serverbeans.VirtualServer vsBean,
                    String name, 
                    String value,
                    HttpService httpService,
                    SecurityService securityService) {
                        
        Engine[] engines = _embedded.getEngines();
        VirtualServer vs = (VirtualServer)engines[0].findChild(vsBean.getId());
        vs.setBean(vsBean);
        
        if (name == null) {
            return;
        }

        if ("docroot".equals(name)) {
            updateDocroot(value, vs, vsBean);
        } else if (name.startsWith("alternatedocroot_")) {
            updateAlternateDocroot(vs, vsBean);
        } else if ("setCacheControl".equals(name)){
            vs.configureCacheControl(value);
        } else if (Constants.ACCESS_LOG_PROPERTY.equals(name)){
            vs.reconfigureAccessLog(globalAccessLogBufferSize,
                                    globalAccessLogWriteInterval,
                                    _serverContext.getDefaultHabitat(),
                                    domain,
                                    globalAccessLoggingEnabled);
        } else if (Constants.ACCESS_LOG_WRITE_INTERVAL_PROPERTY.equals(name)){
            vs.reconfigureAccessLog(globalAccessLogBufferSize,
                                    globalAccessLogWriteInterval,
                                    _serverContext.getDefaultHabitat(),
                                    domain,
                                    globalAccessLoggingEnabled);
        } else if (Constants.ACCESS_LOG_BUFFER_SIZE_PROPERTY.equals(name)){
            vs.reconfigureAccessLog(globalAccessLogBufferSize,
                                    globalAccessLogWriteInterval,
                                    _serverContext.getDefaultHabitat(),
                                    domain,
                                    globalAccessLoggingEnabled);
        } else if ("allowRemoteHost".equals(name)
                || "denyRemoteHost".equals(name)) {
            vs.configureRemoteHostFilterValve(httpService.getHttpProtocol());
        } else if ("allowRemoteAddress".equals(name)
                || "denyRemoteAddress".equals(name)) {
            vs.configureRemoteAddressFilterValve();
        } else if (Constants.SSO_ENABLED.equals(name)) {
            vs.configureSSOValve(globalSSOEnabled, webFeatureFactory);
        } else if ("authRealm".equals(name)) {
            vs.configureAuthRealm(securityService);
        } else if (name.startsWith("send-error")) {
            vs.configureErrorPage();
        } else if (name.startsWith("redirect")) {
            vs.configureRedirect();
        } else if (name.startsWith("contextXmlDefault")) {
            vs.setDefaultContextXmlLocation(value);
        }
    }
    

    /**
     * Processes an update to the http-service element, by updating each
     * http-listener
     */
    public void updateHttpService(HttpService httpService)
            throws LifecycleException {

        if (httpService == null) {
            return;
        }

        /*
         * Update each virtual server with the sso-enabled and
         * access logging related properties of the updated http-service
         */	
        Property ssoEnabled = null;
        Property accessLoggingEnabled = null;
        Property accessLogWriteInterval = null;
        Property accessLogBufferSize = null;
        List<Property> props = httpService.getProperty();
        if (props != null) {
            for (Property prop : props) {
                if (Constants.SSO_ENABLED.equals(prop.getName())) {
                    ssoEnabled = prop;
                    globalSSOEnabled = ConfigBeansUtilities.toBoolean(
                            prop.getValue());
                } else if (Constants.ACCESS_LOGGING_ENABLED.equals(
                                prop.getName())) {
                    accessLoggingEnabled = prop;
                    globalAccessLoggingEnabled = ConfigBeansUtilities.toBoolean(
                                prop.getValue());
                } else if (Constants.ACCESS_LOG_WRITE_INTERVAL_PROPERTY.equals(
                                prop.getName())) {
                    accessLogWriteInterval = prop;
                    globalAccessLogWriteInterval = prop.getValue();
                } else if (Constants.ACCESS_LOG_BUFFER_SIZE_PROPERTY.equals(
                                prop.getName())) {
                    accessLogBufferSize = prop;
                    globalAccessLogBufferSize = prop.getValue();
                }
            }
        }

        List<com.sun.enterprise.config.serverbeans.VirtualServer> virtualServers =
            httpService.getVirtualServer();
        if (virtualServers != null
                && (ssoEnabled != null || accessLoggingEnabled != null
                    || accessLogWriteInterval != null
                    || accessLogBufferSize != null)) {
            for (com.sun.enterprise.config.serverbeans.VirtualServer virtualServer :
                    virtualServers) {
                if (ssoEnabled != null) {
                    updateHostProperties(virtualServer,
                                         ssoEnabled.getName(), 
                                         ssoEnabled.getValue(),
                                         httpService,
                                         null);
                }
                if (accessLoggingEnabled != null) {
                    updateHostProperties(virtualServer,
                                         accessLoggingEnabled.getName(), 
                                         accessLoggingEnabled.getValue(),
                                         httpService,
                                         null);
                }
                if (accessLogWriteInterval != null) {
                    updateHostProperties(virtualServer,
                                         accessLogWriteInterval.getName(), 
                                         accessLogWriteInterval.getValue(),
                                         httpService,
                                         null);
                }
                if (accessLogBufferSize != null) {
                    updateHostProperties(virtualServer,
                                         accessLogBufferSize.getName(), 
                                         accessLogBufferSize.getValue(),
                                         httpService,
                                         null);
                }
            }
        }

        List<HttpListener> httpListeners = httpService.getHttpListener();
        if (httpListeners != null) {
            for (HttpListener httpListener : httpListeners) {
                updateConnector(httpListener, httpService);
            }
        }
    }

    
    /**
     * Update an http-listener property
     * @param httpListener the configuration bean.
     * @param propName the property name
     * @param propValue the property value
     */
    public void updateConnectorProperty(HttpListener httpListener,
                                        String propName,
                                        String propValue) 
        throws LifecycleException{
       
        PECoyoteConnector connector = connectorMap.get(httpListener.getId());
        if (connector != null) {
            configureHttpListenerProperty(propName,propValue,connector);
        }
    }


    /**
     * Update an http-listener
     * @param httpService the configuration bean.
     */
    public void updateConnector(HttpListener httpListener,
                                HttpService httpService)
            throws LifecycleException {
            
        if (httpListener.getDefaultVirtualServer()
                                    .equals(VirtualServer.ADMIN_VS)){
            return;
        }
        
        PECoyoteConnector connector = connectorMap.get(httpListener.getId());
        if (connector != null) {
            _embedded.removeConnector(connector);
            connectorMap.remove(httpListener.getId());
        }

        if (!Boolean.getBoolean(httpListener.getEnabled())) {
            return;
        }

        connector = createConnector(httpListener, httpService);
        
        // The connector is not enabled.
        if (connector == null) return;
        
        String virtualServerName = httpListener.getDefaultVirtualServer();
        VirtualServer virtualServer = (VirtualServer)
                     _embedded.getEngines()[0].findChild(virtualServerName);
        
        boolean found = false;
        int[] ports = virtualServer.getPorts();
        for (int i=0; i<ports.length; i++) {
            if (ports[i] == connector.getPort()) {
                found = true;
                break;
            }
        }
        if (!found) {
            int[] newPorts = new int[ports.length + 1];
            System.arraycopy(ports, 0, newPorts, 0, ports.length);
            newPorts[ports.length] = connector.getPort();
            virtualServer.setPorts(newPorts);
        }

        connector.start();
    }
    
    
    /**
     * Stop and delete the selected http-listener.
     * @param httpService the configuration bean.
     */
    public void deleteConnector(HttpService httpService) 
                                                    throws LifecycleException{
        
        Connector[] connectors = (Connector[])_embedded.findConnectors().clone();
                   
        for (int i=0; i < connectors.length; i++){ 
            for (HttpListener httpListener : httpService.getHttpListener()) {
                if ( ((PECoyoteConnector)connectors[i]).getPort() 
                                  == Integer.parseInt(httpListener.getPort())){
                    connectors[i] = null;
                    break;
                }
            }            
        }
        
        for (int i=0; i < connectors.length; i++){
            if ( connectors[i] != null ){
                _embedded.removeConnector((PECoyoteConnector)connectors[i]);
            }
        }                  
    }  
    

    /**
     * Reconfigures the access log valve of each virtual server with the
     * updated attributes of the <access-log> element from domain.xml.
     */
    public void updateAccessLog(HttpService httpService) {
        Container[] virtualServers = _embedded.getEngines()[0].findChildren();
        for (int i=0; i<virtualServers.length; i++) {
            ((VirtualServer) virtualServers[i]).reconfigureAccessLog(
                httpService,
                _serverContext.getDefaultHabitat().getComponent(
                        WebContainerFeatureFactory.class));
        }
    }


    /**
     * Updates the docroot of the given virtual server
     */
    private void updateDocroot(
            String docroot,
            VirtualServer vs,
            com.sun.enterprise.config.serverbeans.VirtualServer vsBean) {

        boolean isValid = validateDocroot(docroot,
                                          vsBean.getId(),
                                          vsBean.getDefaultWebModule());
        if (isValid) {
            vs.setAppBase(docroot);
            removeDummyModule(vs);
            WebModuleConfig wmInfo = 
                vs.createSystemDefaultWebModuleIfNecessary();
            if (wmInfo != null) {
                loadStandaloneWebModule(vs, wmInfo);
            }
        }
    }


    private void updateAlternateDocroot(
            VirtualServer vs,
            com.sun.enterprise.config.serverbeans.VirtualServer vsBean) {

        removeDummyModule(vs);
        WebModuleConfig wmInfo = 
            vs.createSystemDefaultWebModuleIfNecessary();
        if (wmInfo != null) {
            loadStandaloneWebModule(vs, wmInfo);
        }
    }

   
    /*
     * Configures the given HTTP connector with the given http-protocol
     * config.
     *
     * @param connector HTTP connector to configure
     * @param httpProtocol http-protocol config to use
     */
    private void configureHttpProtocol(PECoyoteConnector connector,
                                       HttpProtocol httpProtocol) {
    
        if (httpProtocol == null) {
            return;
        }

        /*connector.setEnableLookups(httpProtocol.isDnsLookupEnabled());
        connector.setForcedRequestType(httpProtocol.getForcedType());
        connector.setDefaultResponseType(httpProtocol.getDefaultType());
         */
    }
    
    
    /**
     * Configure the Grizzly FileCache mechanism
     */
    private void configureFileCache(PECoyoteConnector connector,
                                    HttpFileCache httpFileCache){
        if ( httpFileCache == null ) return;
        
        /*catalinaCachingAllowed = !(httpFileCache.isGloballyEnabled() &&
                ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));
        
        connector.setFileCacheEnabled(httpFileCache.isGloballyEnabled());   */      
        connector.setLargeFileCacheEnabled(
            ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));
        
        if (httpFileCache.getMaxAgeInSeconds() != null){
            connector.setSecondsMaxAge(
                Integer.parseInt(httpFileCache.getMaxAgeInSeconds()));
        }
        
        if (httpFileCache.getMaxFilesCount() != null){
            connector.setMaxCacheEntries(
                Integer.parseInt(httpFileCache.getMaxFilesCount()));
        }
        
        if (httpFileCache.getSmallFileSizeLimitInBytes() != null){
            connector.setMinEntrySize(
                Integer.parseInt(httpFileCache.getSmallFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSizeLimitInBytes() != null){
            connector.setMaxEntrySize(
                Integer.parseInt(httpFileCache.getMediumFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSpaceInBytes() != null){
            connector.setMaxLargeCacheSize(
                Integer.parseInt(httpFileCache.getMediumFileSpaceInBytes()));
        }
        
        if (httpFileCache.getSmallFileSpaceInBytes() != null){
            connector.setMaxSmallCacheSize(
                Integer.parseInt(httpFileCache.getSmallFileSpaceInBytes())); 
        }
    }
    
    
    /**
     * Configures all HTTP connector with the given request-processing
     * config.
     *
     * @param httpService http-service config to use
     */
    protected void configureRequestProcessing(HttpService httpService){

        RequestProcessing rp = httpService.getRequestProcessing();
        Connector[] connectors = (Connector[])_embedded.findConnectors();       
                    
        for (int i=0; i < connectors.length; i++){    
            configureRequestProcessing(rp,(PECoyoteConnector)connectors[i]);
        }
    }
    
    
    /**
     * Configures an HTTP connector with the given request-processing
     * config.
     *
     * @param RequestProcessing http-service config to use
     * @param connector the connector used.
     */
    protected void configureRequestProcessing(RequestProcessing rp, 
                                              PECoyoteConnector connector){
        if (rp == null) return;

        try{
            connector.setMaxProcessors(
                    Integer.parseInt(rp.getThreadCount()));
            connector.setMinProcessors(
                    Integer.parseInt(rp.getInitialThreadCount()));
            connector.setProcessorWorkerThreadsTimeout(
                    Integer.parseInt(rp.getRequestTimeoutInSeconds())); 
            connector.setProcessorWorkerThreadsIncrement(
                    Integer.parseInt(rp.getThreadIncrement()));
            connector.setMaxHttpHeaderSize(
                   Integer.parseInt(rp.getHeaderBufferLengthInBytes()));
        } catch (NumberFormatException ex){
            _logger.log(Level.WARNING, " Invalid request-processing attribute", 
                    ex);                      
        }             
    }


    /*
     * Loads and instantiates the ProxyHandler implementation
     * class with the specified name, and sets the instantiated 
     * ProxyHandler on the given connector.
     *
     * @param connector The HTTP connector to configure
     * @param className The ProxyHandler implementation class name
     */
    private void setProxyHandler(PECoyoteConnector connector,
                                 String className) {

        Object handler = null;
        try {
            Class handlerClass = Class.forName(className);
            handler = handlerClass.newInstance();
        } catch (Exception e) {
            String msg = _rb.getString(
                "pewebcontainer.proxyHandlerClassLoadError");
            msg = MessageFormat.format(msg, new Object[] { className });
            _logger.log(Level.SEVERE, msg, e);
        }
        if (handler != null) {
            if (!(handler instanceof ProxyHandler)) {
                _logger.log(
                    Level.SEVERE,
                    "pewebcontainer.proxyHandlerClassInvalid",
                    className);
            } else {
                connector.setProxyHandler((ProxyHandler) handler);
            }                
        } 
    }


    /*
     * Configures the given HTTP listener with its keystore and truststore.
     *
     * @param connector The HTTP listener to be configured
     */
    private void configureConnectorKeysAndCerts(PECoyoteConnector connector) {

        /*
         * Keystore
         */
        String prop = System.getProperty("javax.net.ssl.keyStore");
        if (prop != null) {
            // PE
            connector.setKeystoreFile(prop);
            connector.setKeystoreType(DEFAULT_KEYSTORE_TYPE);
        }

        /*
         * Get keystore password from password.conf file.
         * Notice that JSSE, the underlying SSL implementation in PE,
         * currently does not support individual key entry passwords
         * that are different from the keystore password.
         *
        String ksPasswd = null;
        try {
            ksPasswd = PasswordConfReader.getKeyStorePassword();
        } catch (IOException ioe) {
            // Ignore
        }
        if (ksPasswd == null) {
            ksPasswd = System.getProperty("javax.net.ssl.keyStorePassword");
        }
        if (ksPasswd != null) {
            try {
                connector.setKeystorePass(ksPasswd);
            } catch (Exception e) {
                _logger.log(Level.SEVERE,
                    "pewebcontainer.http_listener_keystore_password_exception",
                    e);
            }
        }*/

        /*
	 * Truststore
         */
        prop = System.getProperty("javax.net.ssl.trustStore");
        if (prop != null) {
            // PE
            connector.setTruststore(prop);
            connector.setTruststoreType(DEFAULT_TRUSTSTORE_TYPE);
        }
    }


    /*
     * Load the glassfish-jk.properties
     *
     * @param connector The JK connector to configure
     */
    private void configureJKProperties(PECoyoteConnector connector) {

        String propertiesURL =
            System.getProperty("com.sun.enterprise.web.connector.enableJK.propertyFile");

        if (propertiesURL == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                 _logger.finest(
                 "com.sun.enterprise.web.connector.enableJK.propertyFile not defined");
            }
            return;
        } 

        if (_logger.isLoggable(Level.FINEST)) {
             _logger.finest("Loading glassfish-jk.properties from " +propertiesURL);
        }

        File propertiesFile   = new File(propertiesURL);
        if ( !propertiesFile.exists() ) {
            String msg = _rb.getString( "pewebcontainer.missingJKProperties" );
            msg = MessageFormat.format(msg, new Object[] { propertiesURL });
            _logger.log(Level.WARNING, msg);
            return;
        }

        Properties properties = null;
        InputStream is = null;
 
        try {
            FileInputStream fis = new FileInputStream(propertiesFile);
            is = new BufferedInputStream(fis);
            properties = new Properties();
            properties.load(is);

        } catch (Exception ex) {
            String msg = _rb.getString("pewebcontainer.configureJK");
            msg = MessageFormat.format(
                msg, 
                new Object[] { Integer.valueOf(connector.getPort()) });
            _logger.log(Level.SEVERE, msg, ex);

            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {}
                }
            }

        Enumeration enumeration = properties.keys();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = (String) properties.getProperty(name);
            if (value != null) {
                IntrospectionUtils.setProperty(connector, name, value);
            }

        }
    }

}

