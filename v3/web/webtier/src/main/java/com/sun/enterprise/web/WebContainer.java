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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
//end HERCULES:add
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Deployer;
import org.apache.catalina.Engine;
import org.apache.catalina.Loader;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.TldConfig;
import org.apache.catalina.util.ServerInfo;
import org.apache.coyote.tomcat5.CoyoteAdapter;
import org.apache.coyote.tomcat5.CoyoteRequest;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.xmlparser.ParserUtils;

import com.sun.grizzly.util.http.mapper.Mapper;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.DasConfig;
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
import com.sun.enterprise.config.serverbeans.SessionProperties;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.runtime.web.ManagerProperties;
import com.sun.enterprise.deployment.runtime.web.SessionManager;
import com.sun.enterprise.deployment.runtime.web.StoreProperties;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.util.WebValidatorWithoutCL;
//import com.sun.enterprise.instance.WebModulesManager;
//import com.sun.enterprise.instance.AppsManager;
//import com.sun.enterprise.management.util.J2EEModuleUtil;
//import com.sun.enterprise.server.StandaloneWebModulesManager;
import org.glassfish.internal.api.ServerContext;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.Result;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.web.connector.coyote.PECoyoteConnector;
import com.sun.enterprise.web.logger.IASLogger;
import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;
import com.sun.appserv.security.provider.ProxyHandler;
//import com.sun.appserv.server.ServerLifecycleException;
import org.glassfish.web.loader.util.ASClassLoaderUtil;
import com.sun.appserv.server.util.Version;
import com.sun.logging.LogDomains;

// monitoring imports
import com.sun.enterprise.admin.monitor.stats.ServletStats;
import com.sun.enterprise.web.stats.ServletStatsImpl;
import com.sun.enterprise.web.monitor.PwcServletStats;
import com.sun.enterprise.web.monitor.impl.PwcServletStatsImpl;
import com.sun.enterprise.admin.monitor.stats.WebModuleStats;
import com.sun.enterprise.web.stats.WebModuleStatsImpl;
import com.sun.enterprise.web.monitor.impl.PwcWebModuleStatsImpl;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import com.sun.enterprise.web.stats.PWCVirtualServerStatsImpl;
import com.sun.enterprise.web.stats.PWCRequestStatsImpl;

// Begin EE: 4927099 load only associated applications
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Servers;
// End EE: 4927099 load only associated applications

// V3 imports
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import javax.naming.spi.NamingManager;
import javax.servlet.jsp.JspFactory;
import java.lang.reflect.Method;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.catalina.Realm;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.security.common.CipherInfo;

import com.sun.enterprise.security.integration.RealmInitializer;

/**
 * Web container service
 *
 * @author jluehe
 * @author amyroh
 */
@Service(name="com.sun.enterprise.web.WebContainer")
public class WebContainer implements org.glassfish.api.container.Container, PostConstruct, PreDestroy {
        //MonitoringLevelListener {

    // -------------------------------------------------- Constants & Statics

    public static final String DISPATCHER_MAX_DEPTH="dispatcher-max-depth";

    static final int DEFAULT_REAP_INTERVAL = 60;   // 1 minute

    public static final String JWS_APPCLIENT_EAR_NAME = "__JWSappclients";
    public static final String JWS_APPCLIENT_WAR_NAME = "sys";
    private static final String JWS_APPCLIENT_MODULE_NAME = JWS_APPCLIENT_EAR_NAME + ":" + JWS_APPCLIENT_WAR_NAME + ".war";

    private static final String DOC_BUILDER_FACTORY_PROPERTY =
            "javax.xml.parsers.DocumentBuilderFactory";
    private static final String DOC_BUILDER_FACTORY_IMPL =
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";

    private static final String DEFAULT_KEYSTORE_TYPE = "JKS";
    private static final String DEFAULT_TRUSTSTORE_TYPE = "JKS";

    private static final String DOL_DEPLOYMENT =
            "com.sun.enterprise.web.deployment.backend";
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger = LogDomains.getLogger(
            LogDomains.WEB_LOGGER);

    /**
     * The current <code>WebContainer</code> instance used (single).
     */
    protected static WebContainer webContainer;

    //HERCULES:add
    //added for monitoring
    private static boolean debugMonitoring=false;
    private static long debugMonitoringPeriodMS = 30000L;
    private static WebContainerTimer _timer = new WebContainerTimer(true);
    //added for monitoring
    //END HERCULES:add

    /**
     * Are we using Tomcat deployment backend or DOL?
     */
    protected static boolean useDOLforDeployment = true;

    // ----------------------------------------------------- Instance Variables

    @Inject
    Domain domain;

    @Inject
    ServerContext _serverContext;

    @Inject
    ComponentEnvManager componentEnvManager;

    @Inject(optional=true)
    DasConfig dasConfig;
    
    @Inject
    RequestDispatcher dispatcher;
    
    HashMap<String, Integer> portMap = new HashMap<String, Integer>();
    HashMap<Integer, CoyoteAdapter> adapterMap = new HashMap<Integer, CoyoteAdapter>();

    EmbeddedWebContainer _embedded;
    Engine engine;
    String instanceName;

    private WebConnector jkConnector;

    /**
     * The id of this web container object.
     */
    private String _id = null;
   
    /**
     * Allow disabling accessLog mechanism
     */
    protected boolean globalAccessLoggingEnabled = true;
    
    /**
     * AccessLog buffer size for storing logs.
     */
    protected String globalAccessLogBufferSize = null;   
   
    /**
     * AccessLog interval before the valve flush its buffer to the disk.
     */
    protected String globalAccessLogWriteInterval = null;  
   
    /**
     * The default-redirect port
     */
    protected int defaultRedirectPort = -1;

    /**
     * <tt>false</tt> when the Grizzly File Cache is enabled. When disabled
     * the Servlet Container temporary Naming cache is used when loading the
     * resources.
     */
    protected boolean catalinaCachingAllowed = true;

    @Inject
    protected ServerEnvironment instance = null;
    
    // TODO
    //protected WebModulesManager webModulesManager = null;
    //protected AppsManager appsManager = null;

    /**
     * The schema2beans object that represents the root node of server.xml.
     */
    private Server _serverBean = null;


    /**
     * The resource bundle containing the message strings for _logger.
     */
    protected static final ResourceBundle _rb = _logger.getResourceBundle();

    /*
     * The current web container monitoring level
     */
    protected static MonitoringLevel monitoringLevel;

    /**
     * The current level of logging verbosity for this object.
     */
    protected Level _logLevel = null;

    /**
     * Controls the verbosity of the web container subsystem's debug messages.
     *
     * This value is non-zero only when the iAS level is one of FINE, FINER
     * or FINEST.
     */
    protected int _debug = 0;

    /**
     * Top-level directory for files generated (compiled JSPs) by
     *  standalone web modules.
     */
    private File _modulesWorkRoot = null;

    // START S1AS 6178005
    /**
     * Top-level directory where ejb stubs of standalone web modules are stored
     */
    private String modulesStubRoot = null;
    // END S1AS 6178005

    /**
     * Absolute path for location where all the deployed
     * standalone modules are stored for this Server Instance.
     */
    protected File _modulesRoot = null;

    /**
     * Top-level directory for files generated by application web modules.
     */
    private File _appsWorkRoot = null;

    // START S1AS 6178005
    /**
     * Top-level directory where ejb stubs for applications are stored.
     */
    private File appsStubRoot = null;
    // END S1AS 6178005

    /**
     * Indicates whether dynamic reloading is enabled (as specified by
     * the dynamic-reload-enabled attribute of <applications> in server.xml)
     */
    private boolean _reloadingEnabled = false;

    /**
     * The number of seconds between checks for modified classes (if
     * dynamic reloading is enabled).
     *
     * This value is specified by the reload-poll-interval attribute of
     * <applications> in server.xml.
     */
    private int _pollInterval = 2;

    /**
     * Adds/removes standalone web modules to the reload monitor thread
     * (when dynamic reloading is enabled in server.xml).
     */
    //private StandaloneWebModulesManager _reloadManager = null;

    /**
     * The lifecycle event support for this component.
     */
    //private LifecycleSupport _lifecycle = new LifecycleSupport(this);

    /**
     * Has this component been started yet?
     */
    protected boolean _started = false;

    /**
     * The global (at the http-service level) ssoEnabled property.
     */
    protected boolean globalSSOEnabled = true;

    //private EjbWebServiceRegistryListener ejbWebServiceRegistryListener;

    protected WebContainerFeatureFactory webContainerFeatureFactory;

    /**
     * The value of the instance-level session property named "enableCookies"
     */
    boolean instanceEnableCookies = true;

    private Config cfg;

    private ServerConfigLookup serverConfigLookup;

    /**
     * Static initialization
     */
    static {
        if (System.getProperty(DOL_DEPLOYMENT) != null){
            useDOLforDeployment = Boolean.valueOf(
                    System.getProperty(DOL_DEPLOYMENT)).booleanValue();
        }
    }
    
    public void postConstruct() {

        setJspFactory();

        _modulesWorkRoot = instance.getWebModuleCompileJspPath();
        _appsWorkRoot = instance.getApplicationCompileJspPath();
        _modulesRoot = instance.getApplicationRepositoryPath();
        
        setTldScan();

        // START S1AS 6178005
        modulesStubRoot = instance.getModuleStubPath();
        appsStubRoot = instance.getApplicationStubPath();
        // END S1AS 6178005

        // TODO: ParserUtils should become a @Service and it should initialize itself.
        // TODO: there should be only one EntityResolver for both DigesterFactory
        // and ParserUtils
        File root = _serverContext.getInstallRoot();
        File libRoot = new File(root, "lib");
        File schemas = new File(libRoot, "schemas");
        File dtds = new File(libRoot, "dtds");

        try {
            ParserUtils.setSchemaResourcePrefix(schemas.toURL().toString());
        } catch(MalformedURLException e) {
            _logger.log(Level.SEVERE, "Exception setting the schemas/dtds location", e);
        }

        instanceName = _serverContext.getInstanceName();

        /* FIXME
        webContainerFeatureFactory = _serverContext.getPluggableFeatureFactory().getWebContainerFeatureFactory();
        */
        webContainerFeatureFactory =
            _serverContext.getDefaultHabitat().getComponent(
                PEWebContainerFeatureFactoryImpl.class);

        /* TODO : revisit later
         ejbWebServiceRegistryListener = new EjbWebServiceRegistryListener(this);
         try {
            webModulesManager = new WebModulesManager(instance);
            appsManager = new AppsManager(instance);
        } catch (ConfigException cx) {
            _logger.log(Level.WARNING,
                "Error in creating web modules manager: ", cx);
        }
         */

        Config cfg = _serverContext.getDefaultHabitat().getComponent(Config.class);
        serverConfigLookup = new ServerConfigLookup(cfg);
        configureDynamicReloadingSettings();
        LogService logService = cfg.getLogService();
        initLogLevel(logService);
        initMonitoringLevel(cfg.getMonitoringService());

        String maxDepth = null;
        if(cfg.getWebContainer()!=null)
            maxDepth = cfg.getWebContainer().getPropertyValue(DISPATCHER_MAX_DEPTH);
        if (maxDepth != null) {
            int depth = -1;
            try {
                depth = Integer.parseInt(maxDepth);
            } catch (NumberFormatException e) {}
                
            if (depth > 0) {
                CoyoteRequest.setMaxDispatchDepth(depth);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Maximum depth for nested request "
                            + "dispatches set to "
                            + maxDepth);
                }
            }
        }
    
        String logServiceFile = null;
        if (logService != null) {
            logServiceFile = logService.getFile();
        }
        
        _embedded = new EmbeddedWebContainer(_logger, _serverContext, this, logServiceFile);
        
        _embedded.setCatalinaHome(instance.getDomainRoot().getAbsolutePath());
        _embedded.setCatalinaBase(instance.getDomainRoot().getAbsolutePath());
        _embedded.setUseNaming(false);
        if (_debug > 1)
            _embedded.setDebug(_debug);
        _embedded.setLogger(new IASLogger(_logger));
        
        // TODO (Sahoo): Stop using ModuleImpl
        engine = _embedded.createEngine();
        engine.setParentClassLoader(EmbeddedWebContainer.class.getClassLoader());
        _embedded.addEngine(engine);
        ((StandardEngine) engine).setDomain(_serverContext.getDefaultDomainName());
        ((StandardEngine) engine).setName(_serverContext.getDefaultDomainName());

        /*
         * Set the server name and version.
         * Allow customers to override this information by specifying
         * product.name system property. For example, some customers prefer
         * not to disclose the product name and version for security
         * reasons, in which case they would set the value of the
         * product.name system property to the empty string.
         */
        String serverInfo = System.getProperty("product.name");
        if (serverInfo != null) {
            ServerInfo.setServerInfo(serverInfo);
        } else {
            ServerInfo.setServerInfo(Version.getVersion());
            System.setProperty("product.name", Version.getVersion());
        }

        //HERCULES:add
        //added for internal monitoring
        WebDebugMonitor debugMonitor = new WebDebugMonitor();
        HashMap monitorMap = debugMonitor.getDebugMonitoringDetails();
        debugMonitoring = ((Boolean) monitorMap.get("debugMonitoring")).booleanValue();
        debugMonitoringPeriodMS = ((Long) monitorMap.get(
                "debugMonitoringPeriodMS")).longValue();

        if (debugMonitoring) {
            _timer.schedule(new DebugMonitor(_embedded), 0L,
                    debugMonitoringPeriodMS);
        }
        //added for internal monitoring
        //END HERCULES:add

        if (System.getProperty(DOC_BUILDER_FACTORY_PROPERTY) == null) {
            System.setProperty(DOC_BUILDER_FACTORY_PROPERTY,
                    DOC_BUILDER_FACTORY_IMPL);
        }

        initInstanceSessionProperties();

        //HERCULES:mod
        /*
        registerAdminEvents();
        registerMonitoringLevelEvents();
        initHealthChecker();
        if(isNativeReplicationEnabled()) {
            initReplicationReceiver();
        }
         */
        long btime = 0L;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("before schema check");
            btime = System.currentTimeMillis();
        }
        doSchemaCheck();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("after schema check time: " + (System.currentTimeMillis() - btime));
        }
        //end HERCULES:mod

        // TODO : revisit later
        //ejbWebServiceRegistryListener.register(_serverContext.getDefaultHabitat());

        List<Config> configs = domain.getConfigs().getConfig();
        for (Config aConfig : configs) {

            HttpService httpService = aConfig.getHttpService();

            // Configure HTTP listeners
            List<HttpListener> httpListeners = httpService.getHttpListener();
            for (HttpListener httpListener : httpListeners) {
                if ("admin-listener".equals(httpListener.getId())) {
                    // XXX TBD
                    continue;
                } else {
                    createHttpListener(httpListener, httpService);
                }
            }
            createJKConnector(httpService);
            setDefaultRedirectPort(defaultRedirectPort);
            
            // Configure virtual servers
            List<com.sun.enterprise.config.serverbeans.VirtualServer> virtualServers = httpService.getVirtualServer();
            for (com.sun.enterprise.config.serverbeans.VirtualServer vs : virtualServers) {
                createVirtualServer(vs, httpService, aConfig.getSecurityService());
                _logger.info("Created virtual server " + vs.getId());
            }
        }

        loadDefaultWebModules();
       
        //_lifecycle.fireLifecycleEvent(START_EVENT, null);
        _started = true;
        // start the embedded container
        try {
            _embedded.start();
        } catch (LifecycleException le) {
            _logger.log(Level.SEVERE,
                               "Unable to start web container", le);
            return;
        }
        
        // TODO not yet
        //enableVirtualServerMonitoring();
        
        /*
        if (_reloadingEnabled) {
            // Enable dynamic reloading (via the .reload file) for all
            // standalone web-modules that are marked as enabled

            Applications appsBean = null;
            try {
                appsBean = ServerBeansFactory.getApplicationsBean(_configContext);
            } catch (ConfigException e) {
               String msg = _rb.getString("webcontainer.appsConfigError");
               _logger.log(Level.SEVERE, msg, e);
            }

            _reloadManager = new StandaloneWebModulesManager(_id,
                                                             _modulesRoot,
                                                             _pollInterval);
            if (appsBean != null) {
                com.sun.enterprise.config.serverbeans.WebModule[] wmBeans = appsBean.getWebModule();
                if (wmBeans != null && wmBeans.length > 0) {
                    _reloadManager.addWebModules(wmBeans);
                }
            }

        }
        enableAllWSEndpoints();    
         */
    }

    public void preDestroy() {
        try {
            _embedded.stop();
        } catch(LifecycleException le) {
            _logger.log(Level.SEVERE, "Unable to stop web container", le);
            return;
        }
        _timer.cancel();
    }

    public String getName() {
        return "Web";
    }

    public Class<? extends WebDeployer> getDeployer() {
        return WebDeployer.class;
    }
    
    /**
     * Use an http-listener subelements and creates a corresponding 
     * Tomcat Connector for each.
     *
     * @param httpService The http-service element
     * @param httpListener the configuration element.
     */       
    private void createHttpListener(HttpListener httpListener,
                                             HttpService httpService){
        if (!Boolean.valueOf(httpListener.getEnabled())) {
            _logger.warning(httpListener.getId()+" HTTP listener is disabled " +
                    Boolean.valueOf(httpListener.getEnabled()));
            return;
        }
                                 
        int port = 8080;
        WebConnector connector;
        
        checkHostnameUniqueness(httpListener.getId(), httpService);

        try {
            port = Integer.parseInt(httpListener.getPort());
        } catch (NumberFormatException nfe) {
            String msg = _rb.getString("pewebcontainer.http_listener.invalid_port");
            msg = MessageFormat.format(msg,
                    httpListener.getPort(),
                    httpListener.getId());
            throw new IllegalArgumentException(msg);
        }

        /*
         * Create Connector. Connector is SSL-enabled if
         * 'security-enabled' attribute in <http-listener>
         * element is set to TRUE.
         */
        boolean isSecure = Boolean.valueOf(httpListener.getSecurityEnabled());
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
            (WebConnector)_embedded.createConnector(address,port,
                                                         isSecure);
        
        _logger.info("Created HTTP listener " + httpListener.getId());
        connector.setName(httpListener.getId());

        configureConnector(connector,httpListener,isSecure,httpService);

        if ( _logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE, "create.listenerport",
                new Object[] {port, connector});
        }

        _embedded.addConnector(connector);
    
        portMap.put(httpListener.getId(),
                    Integer.valueOf(httpListener.getPort()));
        CoyoteAdapter coyoteAdapter = new CoyoteAdapter(connector);
        adapterMap.put(Integer.valueOf(httpListener.getPort()), coyoteAdapter);

        // If we already know the redirect port, then set it now
        // This situation will occurs when dynamic reconfiguration occurs
        if ( defaultRedirectPort != -1 ){
            connector.setRedirectPort(defaultRedirectPort);
        }
        
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

            jkConnector = (WebConnector) _embedded.createConnector("0.0.0.0", 
                                            port, "ajp");

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
    private void configureConnector(WebConnector connector,
                                    HttpListener httpListener,
                                    boolean isSecure,
                                    HttpService httpService) {

        configureConnectionPool(connector, httpService.getConnectionPool());

        /* TODO 
        WebContainerFeatureFactory wcFeatureFactory = _serverContext.getDefaultHabitat().getComponent(WebContainerFeatureFactory.class);
        String sslImplementationName = 
            webFeatureFactory.getSSLImplementationName();
        
        if (sslImplementationName != null) {
            connector.setProperty("sSLImplementation",sslImplementationName);
        }*/
        
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
        connector.setXpoweredBy(Boolean.valueOf(httpListener.getXpoweredBy()));
        
        // Application root
        connector.setWebAppRootPath(getModulesRoot().getAbsolutePath());
        
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
                                                WebConnector connector){
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
                                            WebConnector connector)
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
                                               WebConnector connector){
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
                if (listenerId.equals(vsListeners.get(j))) {
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
                        if (host.equals(otherHosts.get(l))) {
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
    
    private void createVirtualServer(
                com.sun.enterprise.config.serverbeans.VirtualServer vsBean,
                HttpService httpService, SecurityService securityService) {
        
        MimeMap mm = null;
        String vs_id = vsBean.getId();

        String docroot =
                vsBean.getPropertyValue("docroot");
        
        validateDocroot(docroot,
                        vs_id,
                        vsBean.getDefaultWebModule());

        VirtualServer vs = createVS(vs_id, vsBean, docroot, mm,
                                    httpService.getHttpProtocol());

        // cache control
        Property cacheProp = vsBean.getProperty("setCacheControl");
        if ( cacheProp != null ){
            vs.configureCacheControl(cacheProp.getValue());   
        }        

        // Set Host alias names
        List<String> aliasNames =
            StringUtils.parseStringList(vsBean.getHosts(), ",");
        for (String alias: aliasNames){
            // XXX remove once ${com.sun.aas.hostName} has been properly
            // resolved thru parametric replacement
            if ("${com.sun.aas.hostName}".equals(alias)) {
                try {
                    alias = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    _logger.log(Level.SEVERE,
                                       "Unable to get local host name",
                                       e);
                }
            }
            vs.addAlias(alias);
        }
        
        PEAccessLogValve accessLogValve = vs.getAccessLogValve();
        boolean startAccessLog = accessLogValve.configure(
            vs_id, vsBean, httpService, domain,
            _serverContext.getDefaultHabitat(), webContainerFeatureFactory,
            globalAccessLogBufferSize, globalAccessLogWriteInterval);
        if (startAccessLog
                && vs.isAccessLoggingEnabled(globalAccessLoggingEnabled)) {
            vs.addValve(accessLogValve);
        }

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
        engine.addChild(vs);
        
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
                        vs_id, docroot);
                throw new IllegalArgumentException(msg);
            } else if (!isValid) {

                _logger.log(Level.WARNING, "virtual-server " + vs_id 
                            + " has an invalid docroot: " + docroot );
            }
        } else if (defaultWebModule == null) {
            String msg = _rb.getString("pewebcontainer.virtual_server.missing_docroot");
            msg = MessageFormat.format(msg, vs_id);
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
            if (Boolean.valueOf(httpListeners[i].getEnabled())){
                Integer port = portMap.get(httpListeners[i].getId());
                if (port != null) {
                    portsList.add(port);
                }               
            } else {
                if ((vs.getName().equalsIgnoreCase(VirtualServer.ADMIN_VS))) {
                    String msg = _rb.getString(
                        "pewebcontainer.httpListener.mustNotDisable");
                    msg = MessageFormat.format(
                        msg,
                            httpListeners[i].getId(),
                            vs.getName());
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
                 _logger.info("Virtual Server "+vs.getID()+" set port "+ports[i]);
            }
            if (addJkListenerPort) {
                ports[i] = jkConnector.getPort();
                 _logger.info("Virtual Server "+vs.getID()+" set jk port "+ports[i]);
            }
            vs.setPorts(ports);
        }
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
                    "PWCRequestStats");
            _logger.log(Level.WARNING, msg, mre);
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
        if (Boolean.valueOf(sslConfig.getClientAuthEnabled())) {
            connector.setClientAuth(true);
        }

        // ssl protocol variants
        StringBuffer sslProtocolsBuf = new StringBuffer();
        boolean needComma = false;
        if (Boolean.valueOf(sslConfig.getSsl2Enabled())) {
            sslProtocolsBuf.append("SSLv2");
            needComma = true;
        }
        if (Boolean.valueOf(sslConfig.getSsl3Enabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            } else {
                needComma = true;
            }
            sslProtocolsBuf.append("SSLv3");
        }
        if (Boolean.valueOf(sslConfig.getTlsEnabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            }
            sslProtocolsBuf.append("TLSv1");
        }
        if (Boolean.valueOf(sslConfig.getSsl3Enabled()) || Boolean.valueOf(sslConfig.getTlsEnabled())) {
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
                        keepAlive.getTimeoutInSeconds(),
                        Integer.toString(timeoutInSeconds));
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
                        keepAlive.getMaxConnections(),
                        Integer.toString(maxConnections));
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
                        keepAlive.getThreadCount(),
                        Integer.toString(threadCount));
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
                msg, ConfigBeansUtilities.getDefaultQueueSizeInBytes(),
                    Integer.toString(connector.getQueueSizeInBytes()));
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
                msg, cp.getMaxPendingCount(),
                    Integer.toString(connector.getSocketServerBacklog()));
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
                msg, cp.getReceiveBufferSizeInBytes(),
                    Integer.toString(connector.getBufferSize()));
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
                msg, cp.getSendBufferSizeInBytes(),
                    Integer.toString(connector.getMaxHttpHeaderSize()));
            _logger.log(Level.WARNING, msg, ex);
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
        Connector[] connectors = _embedded.findConnectors();
                    
        for (int i=0; i < connectors.length; i++){    
            configureRequestProcessing(rp,(PECoyoteConnector)connectors[i]);
        }
    }
    
    /**
     * Configures an HTTP connector with the given request-processing
     * config.
     *
     * @param rp http-service config to use
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
            msg = MessageFormat.format(msg, className);
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
            msg = MessageFormat.format(msg, propertiesURL);
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
            msg = MessageFormat.format(msg, connector.getPort());
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
            String value = properties.getProperty(name);
            if (value != null) {
                IntrospectionUtils.setProperty(connector, name, value);
            }

        }
    }    

    // ------------------------------------------------------------ Properties

    /**
     * Return the web container identifier.
     */
    public String getID() {
        return _id;
    }

    // --------------------------------------------------------- HADB Health Status

    private HealthChecker _healthChecker = null;
    public HealthChecker getHealthChecker() {
        return _healthChecker;
    }

    private void initHealthChecker() {
        //added the pluggable interface way of getting the health checker
        HealthChecker healthChecker = null;
        try {
            healthChecker = webContainerFeatureFactory.getHADBHealthChecker(this);
        } catch (NoClassDefFoundError ex) {
            _logger.log(Level.WARNING,
                    "hadbhealthchecker.hadbClientJarsMissing");
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("WebContainer>>initHealthChecker - healthChecker = " + healthChecker);
        }
        if(healthChecker != null) {
            _healthChecker = healthChecker;
            try {
                _healthChecker.start();
            } catch (LifecycleException ex) {}
        }
    }

    private void stopHealthChecker() {
        if(_healthChecker != null) {
            try {
                _healthChecker.stop();
            } catch (LifecycleException ex) {}
            _healthChecker = null;
        }
    }

    // --------------------------------------------------------- end HADB Health Status

    // --------------------------------------------------------- start Replication
    /*
    private ReplicationReceiver _replicationReceiver = null;
    public ReplicationReceiver getReplicationReceiver() {
        return _replicationReceiver;
    }

    private void initReplicationReceiver() {
        ReplicationReceiver replicationReceiver =
                webFeatureFactory.getReplicationReceiver(_embedded);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("WebContainer>>initReplicationReceiver - replicationReceiver = " + replicationReceiver);
        }
        if(replicationReceiver != null) {
            _replicationReceiver = replicationReceiver;
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainer:about to call replicationReceiver>>init()" + _replicationReceiver);
            }
            _replicationReceiver.init();
        }
    }

    private void stopReplicationReceiver() {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("WebContainer:about to call replicationReceiver>>stop()" + _replicationReceiver);
        }
        try {
            _replicationReceiver.stop();
        } catch(Exception ex) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainer:error during replicationReceiver>>stop(): can be ignored" + _replicationReceiver);
            }
        };
        _replicationReceiver = null;
    }

    private boolean isNativeReplicationEnabled() {
        /*ServerConfigLookup lookup = new ServerConfigLookup();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("GMS ENABLED:" + lookup.isGMSEnabled());
            _logger.finest("NATIVE REPLICATION ENABLED:" + lookup.isNativeReplicationEnabledFromConfig());
        }
        return lookup.isGMSEnabled() && lookup.isNativeReplicationEnabledFromConfig();

        return false;
    }
     **/

    // --------------------------------------------------------- end Replication

    // -------------------------------------- Monitoring Level Event Processing
    /*
    public void registerMonitoringLevelEvents() {
        MonitoringRegistry monitoringRegistry =
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);
        monitoringRegistry.registerMonitoringLevelListener(
                this, MonitoredObjectType.SERVLET);
    }

    public void unregisterMonitoringLevelEvents() {
        MonitoringRegistry monitoringRegistry =
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);
        monitoringRegistry.unregisterMonitoringLevelListener(this);
    }

    public void setLevel(MonitoringLevel level) {
        // deprecated, ignore
    }

    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
            Stats handback) {
        // deprecated, ignore
    }

    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
            MonitoredObjectType type) {
        if (MonitoredObjectType.SERVLET.equals(type)) {
            monitoringLevel = to;
            if(MonitoringLevel.OFF.equals(to)) {
                this.resetMonitorStatistics();
            }
        }
    }

    /**
     * Returns the current monitoring level.
     *
     * @return The current monitoring level
     *
    public static MonitoringLevel getMonitoringLevel() {
        return monitoringLevel;
    }

    /**
     * Returns true if monitoring is enabled, false otherwise.
     *
     * @return true if monitoring is enabled, false otherwise
     *
    public static boolean isMonitoringEnabled() {
        return (!MonitoringLevel.OFF.equals(monitoringLevel));
    }

    private void resetMonitorStatistics() {
        MonitorUtil.resetMonitorStats(_embedded,
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class));
    }

    */
    // -------------------------------------------------------- Public Methods

    /**
     * Create a virtual server/host.
     */
    public VirtualServer createVS(String vsID,
            com.sun.enterprise.config.serverbeans.VirtualServer vsBean,
            String docroot,
            MimeMap mimeMap,
            HttpProtocol httpProtocol) {

        // Initialize the docroot
        VirtualServer vs = (VirtualServer) _embedded.createHost(vsID,
                vsBean,
                docroot,
                vsBean.getLogFile(),
                mimeMap);

        //TODO
        //vs.configureVirtualServerState();  
        vs.configureRemoteAddressFilterValve();
        vs.configureRemoteHostFilterValve(httpProtocol);
        vs.configureSSOValve(globalSSOEnabled, webContainerFeatureFactory);
        vs.configureRedirect();
        vs.configureErrorPage();

        return vs;
    }

    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     *
    public void addLifecycleListener(LifecycleListener listener) {
        _lifecycle.addLifecycleListener(listener);
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     *
    public void removeLifecycleListener(LifecycleListener listener) {
        _lifecycle.removeLifecycleListener(listener);
    }
    */
    private void doSchemaCheck() {
        SchemaUpdater schemaUpdater = null;
        try {

            schemaUpdater = _serverContext.getDefaultHabitat().getComponent(SchemaUpdater.class);
        } catch (NoClassDefFoundError ex) {
            //one warning already logged so this one is level fine
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("HADB Warning - client jars missing - ok if not running with HADB");
            }
        }
        if(schemaUpdater != null) {
            try {
                schemaUpdater.doSchemaCheck();
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, "schemaupdater.error", ex);
            }
        }
    }

    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {
        // Validate and update our current component state
        if (!_started) {
            String msg = _rb.getString("webcontainer.notStarted");
            throw new LifecycleException(msg);
        }

        //ejbWebServiceRegistryListener.unregister(_serverContext.getDefaultHabitat());

        //HERCULES:mod
        //unregisterMonitoringLevelEvents();
        stopHealthChecker();
        WebContainerStartStopOperation startStopOperation =
                this.getWebContainerStartStopOperation();
        ArrayList shutdownCleanupCapablesList = startStopOperation.doPreStop();
        //end HERCULES:mod

        _started = false;

        // stop the embedded container
        try{
            _embedded.stop();
        } catch (LifecycleException ex){
            if (ex.getMessage().indexOf("has not been started") == -1){
                throw ex;
            }
        }
/*
        if (_reloadManager != null) {
            // Remove the entries from the reload monitor thread corresponding
            // to this web container object that is being stopped
            _reloadManager.stop();
            _reloadManager = null;
        }
 */
        //HERCULES:mod
        startStopOperation.doPostStop(shutdownCleanupCapablesList);
        //end HERCULES:mod
    }

    /**
     * Get the webContainerStartStopOperation
     * used for doing shutdown cleanup work
     */
    public WebContainerStartStopOperation getWebContainerStartStopOperation() {

        //added the pluggable interface way of getting the start/stop operation
        if (webContainerFeatureFactory==null) {
            return null;
        }
        WebContainerStartStopOperation startStopOperation =
                webContainerFeatureFactory.getWebContainerStartStopOperation();

        //startStopOperation.init(_embedded);
        return startStopOperation;
    }

    // -------------------------------------------------------- Private Methods

    /**
     * Configures a default web module for each virtual server.
     *
     * If a virtual server does not specify any default-web-module, and none
     * of its web modules are loaded at "/", this method will create and load
     * a default context for the virtual server, based on the virtual server's
     * docroot.
     */

    protected void loadDefaultWebModules() {

        Engine[] engines =  _embedded.getEngines();
        String defaultPath = null;
        
        for (int j=0; j<engines.length; j++) {
            Container[] vsArray = engines[j].findChildren();
            for (int i = 0; i < vsArray.length; i++) {
                if (vsArray[i] instanceof VirtualServer) {

                    VirtualServer vs = (VirtualServer) vsArray[i];

                    WebModuleConfig wmInfo = vs.getDefaultWebModule(domain, 
                            _serverContext.getDefaultHabitat().getComponent(
                            WebDeployer.class) );
                    if (wmInfo != null) {
                        defaultPath = wmInfo.getContextPath();
                        // Virtual server declares default-web-module
                        try {
                            updateDefaultWebModule(vs, vs.getPorts(),
                                    wmInfo);
                        } catch (LifecycleException le) {
                            String msg = _rb.getString(
                                    "webcontainer.defaultWebModuleError");
                            msg = MessageFormat.format(
                                    msg,
                                    defaultPath,
                                    vs.getName());
                            _logger.log(Level.SEVERE, msg, le);
                        }

                    } else {
                        // Create default web module off of virtual
                        // server's docroot if necessary
                        wmInfo = vs.createSystemDefaultWebModuleIfNecessary(
                                _serverContext.getDefaultHabitat().getComponent(
                                WebDeployer.class));
                        if (wmInfo != null) {
                            defaultPath = wmInfo.getContextPath();
                            loadStandaloneWebModule(vs, wmInfo);
                        }
                        for (int port : vs.getPorts()) {
                            CoyoteAdapter adapter = adapterMap.get(Integer.valueOf(port));
                            WebApplication application = new WebApplication(this, 
                                    wmInfo, dispatcher);
                            //@TODO change EndportRegistrationException processing if required
                            try {
                                dispatcher.registerEndpoint(wmInfo.getContextPath(),
                                        adapter, application);
                            } catch(EndpointRegistrationException e) {
                                String msg = _rb.getString(
                                        "webcontainer.defaultWebModuleError");
                                msg = MessageFormat.format(
                                        msg,
                                        new Object[]{defaultPath,
                                    vs.getName()
                                });
                                _logger.log(Level.SEVERE, msg, e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Load the specified web module as a standalone module on the specified
     * virtual server.
     */
    protected void loadStandaloneWebModule(VirtualServer vs,
            WebModuleConfig wmInfo) {
        loadWebModule(vs, wmInfo, "null");
    }

    /**
     * Loads all the web modules that are configured for every
     * j2ee-application specified in server.xml.
     */
    protected void loadAllJ2EEApplicationWebModules() {
        loadAllJ2EEApplicationWebModules(false);
    }

    /**
     * Loads all the web modules that are configured for every
     * j2ee-application specified in server.xml.
     *
     * @param isStartUp true, if this is called during server startup
     */
    private void loadAllJ2EEApplicationWebModules(boolean isStartUp) {

        Domain domain = _serverContext.getDefaultHabitat().getComponent(Domain.class);
        Applications appsBean = domain.getApplications();

        if (appsBean != null) {
            for (J2eeApplication module : appsBean.getModules(J2eeApplication.class)) {
                if (isReferenced(module.getName())) {
                    loadJ2EEApplicationWebModules(module);
                }
            }
        }
    }

    /**
     * Loads all the web modules that are configured for the specified
     * j2ee-application.
     */
    public void loadJ2EEApplicationWebModules(J2eeApplication j2eeAppBean) {
       /* if ((j2eeAppBean != null) &&
            isEnabled(j2eeAppBean.getConfigContext(), j2eeAppBean.getName())) {
            String id = j2eeAppBean.getName();
            String location = j2eeAppBean.getLocation();
            String resourceType = j2eeAppBean.getObjectType();

            ApplicationRegistry registry = ApplicationRegistry.getInstance();
            ClassLoader appLoader = registry.getClassLoaderForApplication(id);
            if (appLoader != null) {
                Application appDesc = registry.getApplication(appLoader);

                // Check to see if this app had deployed successfully (4663247)
                if(appDesc == null){
                    Object[] params = { id };
                    _logger.log(Level.SEVERE, "webcontainer.notLoaded",
                                params);
                } else {
                    //Hercules: add
                    ConfigContext eventConfigContext = j2eeAppBean.getConfigContext();
                    //end Hercules: add
                    String j2eeApplication = appDesc.getRegistrationName();
                    Set wbds = appDesc.getWebBundleDescriptors();
                    WebBundleDescriptor wbd = null;
                    com.sun.enterprise.config.serverbeans.WebModule wm = null;
                    WebModuleConfig wmInfo = null;
                    for (Iterator itr = wbds.iterator(); itr.hasNext(); ) {
                        StringBuffer dir = new StringBuffer(location);
                        wbd = (WebBundleDescriptor) itr.next();
                        String moduleName = wbd.getModuleDescriptor().getPath();
                        dir.append(File.separator);
                        dir.append(DeploymentUtils.getRelativeEmbeddedModulePath(location, moduleName));

                        wm = new com.sun.enterprise.config.serverbeans.WebModule();
                        //Hercules add
                        wm.setConfigContext(eventConfigContext);
                        //end Hercules add
                        wm.setName(moduleName);
                        wm.setContextRoot(wbd.getContextRoot());
                        wm.setLocation(dir.toString());
                        wm.setEnabled(true);
                        wm.setObjectType(resourceType);

                        wmInfo = new WebModuleConfig();
                        wmInfo.setBean(wm);
                        wmInfo.setDescriptor(wbd);
                        wmInfo.setParentLoader(appLoader);
                        wmInfo.setVirtualServers(
                                getVirtualServers(id,
                                        j2eeAppBean.getConfigContext()));
                        loadWebModule(wmInfo, j2eeApplication);
                    }
                }//end if(appDesc != null)
            }
        }
        **/
    }

    /**
     * Whether or not a component (either an application or a module) should be
     * enabled is defined by the "enable" attribute on both the
     * application/module element and the application-ref element.
     *
     * @param moduleName The name of the component (application or module)
     * @return boolean
     */
    protected boolean isEnabled(String moduleName) {
        // TODO dochez : optimize
        Domain domain = _serverContext.getDefaultHabitat().getComponent(Domain.class);
        /* applications = domain.getApplications().getLifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModule();
        com.sun.enterprise.config.serverbeans.WebModule webModule = null;
        for (Object module : applications) {
            if (module instanceof WebModule) {
                if (moduleName.equals(((com.sun.enterprise.config.serverbeans.WebModule) module).getName())) {
                    webModule = (com.sun.enterprise.config.serverbeans.WebModule) module;
                }
            }
        }    em
        ServerContext env = _serverContext.getDefaultHabitat().getComponent(ServerContext.class);
        List<Server> servers = domain.getServers().getServer();
        Server thisServer = null;
        for (Server server : servers) {
            if (env.getInstanceName().equals(server.getName())) {
                thisServer = server;
            }
        }
        List<ApplicationRef> appRefs = thisServer.getApplicationRef();
        ApplicationRef appRef = null;
        for (ApplicationRef ar : appRefs) {
            if (ar.getRef().equals(moduleName)) {
                appRef = ar;
            }
        }

        return ((webModule != null && Boolean.valueOf(webModule.getEnabled())) &&
                (appRef != null && Boolean.valueOf(appRef.getEnabled())));
         */
        return true;
    }

    /**
     * Creates and configures a web module for each virtual server
     * that the web module is hosted under.
     *
     * If no virtual servers are specified, then the web module is
     * loaded on EVERY virtual server.
     */
    public List<Result<WebModule>> loadWebModule(
            WebModuleConfig wmInfo, String j2eeApplication) {

        String vsIDs = wmInfo.getVirtualServers();
        List vsList = StringUtils.parseStringList(vsIDs, " ,");
        boolean loadToAll = (vsList == null) || (vsList.size() == 0);
        boolean loadAtLeastToOne = false;

        Engine[] engines =  _embedded.getEngines();

        List<Result<WebModule>> results = new ArrayList<Result<WebModule>>();
        for (int j=0; j<engines.length; j++) {
            Container[] vsArray = engines[j].findChildren();
            for (int i = 0; i < vsArray.length; i++) {
                if (vsArray[i] instanceof VirtualServer) {
                    VirtualServer vs = (VirtualServer) vsArray[i];

                    /*
                     * Fix for bug# 4913636:
                     * If the vsList is null and the virtual server is
                     * __asadmin, continue with next iteration
                     * because we don't want to load user apps on __asadmin
                     */
                    if (vs.getID().equals(VirtualServer.ADMIN_VS) && loadToAll) {
                        continue;
                    }

                    if ( loadToAll
                            || vsList.contains(vs.getID())
                            || verifyAlias(vsList,vs)){

                        results.add(loadWebModule(vs, wmInfo, j2eeApplication));
                        loadAtLeastToOne = true;
                    }
                }
            }
        }
        if (!loadAtLeastToOne) {
            Object[] params = {wmInfo.getName(), vsIDs};
            _logger.log(Level.SEVERE, "webcontainer.moduleNotLoadedToVS",
                    params);
        }
        return results;
    }


    /**
     * Deploy on aliases as well as host.
     */
    private boolean verifyAlias(List vsList,VirtualServer vs){
        for(int i=0; i < vs.getAliases().length; i++){
            if (vsList.contains(vs.getAliases()[i]) ){
                return true;
            }
        }
        return false;
    }

    /**
     * Creates and configures a web module and adds it to the specified
     * virtual server.
     */
    protected Result<WebModule> loadWebModule(VirtualServer vs, WebModuleConfig wmInfo,
            String j2eeApplication) {
        String wmName = wmInfo.getName();
        String wmContextPath = wmInfo.getContextPath();

        if (wmContextPath.equals("") && vs.getDefaultWebModuleID() != null) {
            _logger.log(Level.WARNING, "webcontainer.defaultWebModuleConflict",
                    new Object[] { wmName, wmContextPath, vs.getID() });
            return null;
        }

        /* TODO appsWorkRoot
        if (wmName.indexOf(Constants.NAME_SEPARATOR) != -1) {
            wmInfo.setWorkDirBase(_appsWorkRoot);
            // START S1AS 6178005
            wmInfo.setStubBaseDir(appsStubRoot);
            // END S1AS 6178005
        } else {
            wmInfo.setWorkDirBase(_modulesWorkRoot);
            // START S1AS 6178005
            wmInfo.setStubBaseDir(modulesStubRoot);
            // END S1AS 6178005
        }*/

        String displayContextPath = null;
        if (wmContextPath.equals(""))
            displayContextPath = "/";
        else
            displayContextPath = wmContextPath;

        HashMap adHocPaths = null;
        HashMap adHocSubtrees = null;
        WebModule ctx = (WebModule)vs.findChild(wmContextPath);
        if (ctx != null) {
            if (ctx instanceof AdHocWebModule) {
                /*
                 * Found ad-hoc web module which has been created by web
                 * container in order to store mappings for ad-hoc paths
                 * and subtrees.
                 * All these mappings must be propagated to the context
                 * that is being deployed.
                 */
                if (ctx.hasAdHocPaths()) {
                    adHocPaths = ctx.getAdHocPaths();
                }
                if (ctx.hasAdHocSubtrees()) {
                    adHocSubtrees = ctx.getAdHocSubtrees();
                }
                vs.removeChild(ctx);
            } else if (Constants.DEFAULT_WEB_MODULE_NAME
                    .equals(ctx.getModuleName())) {
                /*
                 * Dummy context that was created just off of a docroot,
                 * (see
                 * VirtualServer.createSystemDefaultWebModuleIfNecessary()).
                 * Unload it so it can be replaced with the web module to be
                 * loaded
                 */
                unloadWebModule(wmContextPath,
                        ctx.getJ2EEApplication(),
                        vs.getName(),
                        null,
                        true);
            } else if (!ctx.getAvailable()){
                /*
                 * Context has been marked unavailable by a previous
                 * call to disableWebModule. Mark the context as available and
                 * return
                 */
                ctx.setAvailable(true);
                return null;
            } else {
                Object[] params = { vs.getID(), displayContextPath, wmName };
                _logger.log(Level.WARNING, "webcontainer.duplicateContextRoot",
                        params);
                return null;
            }
        }

        if (_logger.isLoggable(Level.FINEST)) {
            Object[] params = { wmName, vs.getID(), displayContextPath };
            _logger.log(Level.FINEST, "webcontainer.loadModule", params);
        }

        File docBase = null;
        if (JWS_APPCLIENT_MODULE_NAME.equals(wmName)) {
            docBase = new File(System.getProperty("com.sun.aas.installRoot"));
        } else {
            docBase = wmInfo.getLocation();
        }

        // Object containing web.xml information
        WebBundleDescriptor wbd = wmInfo.getDescriptor();

        Throwable exception = null;
        String compEnvId = null;
        try {
            compEnvId = componentEnvManager.bindToComponentNamespace(wbd);
        } catch(Exception ex) {
            exception = ex;
        }

        ctx = (WebModule) _embedded.createContext(wmContextPath,
                docBase,
                vs.getDefaultContextXmlLocation(),
                vs.getDefaultWebXmlLocation(),
                useDOLforDeployment,
                wmInfo.getDescriptor(), compEnvId);

        // for now disable JNDI
        ctx.setUseNaming(false);

        // Set JSR 77 object name and attributes
        Engine engine = (Engine) vs.getParent();
        if (engine != null) {
            ctx.setEngineName(engine.getName());
            ctx.setJvmRoute(engine.getJvmRoute());
        }
        String j2eeServer = _serverContext.getInstanceName();
        String domain = _serverContext.getDefaultDomainName();
        String server = domain + ":j2eeType=J2EEServer,name=" + j2eeServer;
//        String[] javaVMs = J2EEModuleUtil.getjavaVMs();
        ctx.setDomain(domain);

        ctx.setJ2EEServer(j2eeServer);
        ctx.setJ2EEApplication(j2eeApplication);
        ctx.setServer(server);
        //       ctx.setJavaVMs(javaVMs);
        ctx.setCachingAllowed(false);
        ctx.setCacheControls(vs.getCacheControls());
        ctx.setBean(wmInfo.getBean());

        if (adHocPaths != null) {
            ctx.addAdHocPaths(adHocPaths);
        }
        if (adHocSubtrees != null) {
            ctx.addAdHocSubtrees(adHocSubtrees);
        }
        
        //Set the context root
        if (wmInfo.getDescriptor() != null) {
            ctx.setContextRoot(wmInfo.getDescriptor().getContextRoot());
        } else {
            // Should never happen.
            _logger.log(Level.WARNING, "Unable to set context root", wmInfo);
        }

        //
        // Ensure that the generated directory for JSPs in the document root
        // (i.e. those that are serviced by a system default-web-module)
        // is different for each virtual server.
        //
        String wmInfoWorkDir = wmInfo.getWorkDir();
        if (wmInfoWorkDir != null) {
            StringBuffer workDir = new StringBuffer(wmInfo.getWorkDir());
            if (wmName.equals(Constants.DEFAULT_WEB_MODULE_NAME)) {
                workDir.append("-");
                workDir.append(FileUtils.makeFriendlyFilename(vs.getID()));
            }
            ctx.setWorkDir(workDir.toString());
        }

        ClassLoader parentLoader = wmInfo.getParentLoader();
        if (parentLoader == null) {
            // Use the shared classloader as the parent for all
            // standalone web-modules
            parentLoader = _serverContext.getSharedClassLoader();
        }
        ctx.setParentClassLoader(parentLoader);

        try{
            // Determine if an alternate DD is set for this web-module in
            // the application
            if (wbd != null) {
                String altDDName = wbd.getModuleDescriptor().
                        getAlternateDescriptor();
                if (altDDName != null) {
                    // we should load the alt dd from generated/xml directory
                    // first, then fall back to original app location.

                    // if we have alt dd, it must be an embedded web module
                    String appName =  wmName.substring(0,
                            wmName.indexOf(Constants.NAME_SEPARATOR));
                    ServerEnvironment env = this._serverContext.getDefaultHabitat().getComponent(ServerEnvironment.class);
                    String appLoc = env.getApplicationGeneratedXMLPath() + File.separator +
                            appName;
                    if (! FileUtils.safeIsDirectory(appLoc)) {
                        appLoc = wmInfo.getLocation()+"/..";
                    }

                    if (altDDName.startsWith("/")) {
                        altDDName = appLoc+altDDName.trim();
                    } else {
                        altDDName = appLoc+"/"+altDDName.trim();
                    }
                    Object[] objs = {altDDName, wmName};
                    _logger.log(Level.INFO, "webcontainer.altDDName", objs);
                    ctx.setAltDDName(altDDName);

                }
                // time to update the Web Services related information in
                // our runtime jsr77 mbeans. We publish two extra properties
                // hasWebServices and endpointAddresses for webservices
                // enable web applications.
                if (wbd.hasWebServices()) {
                    ctx.setHasWebServices(true);

                    // creates the list of endpoint addresses
                    String[] endpointAddresses;
                    WebServicesDescriptor webService = wbd.getWebServices();
                    Vector endpointList = new Vector();
                    for (Iterator endpoints = webService.getEndpoints().iterator();
                    endpoints.hasNext();) {
                        WebServiceEndpoint wse = (WebServiceEndpoint) endpoints.next();
                        if (wbd.getContextRoot()!=null) {
                            endpointList.add(wbd.getContextRoot() + "/"
                                    + wse.getEndpointAddressUri());
                        } else {
                            endpointList.add(wse.getEndpointAddressUri());
                        }
                    }
                    endpointAddresses = new String[endpointList.size()];
                    endpointList.copyInto(endpointAddresses);

                    ctx.setEndpointAddresses(endpointAddresses);

                } else {
                    ctx.setHasWebServices(false);
                }
            }

            // Object containing sun-web.xml information
            SunWebApp iasBean = null;

            // The default context is the only case when wbd == null
            if (wbd != null)
                iasBean = wbd.getSunDescriptor();

            // set the sun-web config bean
            ctx.setIasWebAppConfigBean(iasBean);
            ctx.setID(wmName);

            // Configure SingleThreadedServletPools, work/tmp directory etc
            configureMiscSettings(ctx, iasBean, vs, displayContextPath);

            // Configure alternate docroots if dummy web module
            if (Constants.DEFAULT_WEB_MODULE_NAME.equals(ctx.getID())) {
                ctx.setAlternateDocBases(vs.getProperties());
            }

            // Configure the class loader delegation model, classpath etc
            Loader loader = configureLoader(ctx, iasBean, wmInfo);

            // Set the class loader on the DOL object
            if (wbd != null && wbd.hasWebServices())
                wbd.addExtraAttribute("WEBLOADER", loader);

            // Enable dynamic reloading
            if (_reloadingEnabled) {/*
                if (_reloadManager == null) {
                    _reloadManager = new StandaloneWebModulesManager(
                                                            _id,
                                                            _modulesRoot,
                                                            _pollInterval);
                }
                _reloadManager.addWebModule(wmInfo.getBean());
             **/
            }

            // Configure the session manager and other related settings
            // HERCULES:mod - take into account if app is distributable
            // passing in WebBundleDescriptor which has info about whether
            // app is distributable
            ctx.configureSessionSettings(wbd, wmInfo);
            // END HERCULES:mod

            // set i18n info from locale-charset-info tag in sun-web.xml
            ctx.setI18nInfo();

            if (wbd != null) {
                String resourceType = wmInfo.getObjectType();
                boolean isSystem = (resourceType != null &&
                        resourceType.startsWith("system-"));
                // security will generate policy for system default web module
                if (!wmName.startsWith(Constants.DEFAULT_WEB_MODULE_NAME)) {
                    // TODO : v3 : dochez Need to remove dependency on security
                    Realm realm = this._serverContext.getDefaultHabitat().getByContract(Realm.class);
                    if ("null".equals(j2eeApplication)) {
                        /*
                        * Standalone webapps inherit the realm referenced by
                        * the virtual server on which they are being deployed,
                        * unless they specify their own
                        */
                    
                        if (realm != null && realm instanceof RealmInitializer) {
                            ((RealmInitializer)realm).initializeRealm(
                                wbd, isSystem, vs.getAuthRealmName());
                            ctx.setRealm(realm);
                        }
                    } else {
                        if (realm != null && realm instanceof RealmInitializer) {
                            ((RealmInitializer)realm).initializeRealm(
                                wbd, isSystem, null);
                            ctx.setRealm(realm);
                        }
                    }
                }

                // post processing DOL object for standalone web module
                if (wbd.getApplication() != null &&
                        wbd.getApplication().isVirtual()) {
                    wbd.visit(new WebValidatorWithoutCL());
                }

                NamingManager namingMgr = _serverContext.getDefaultHabitat().getComponent(NamingManager.class);
                if (namingMgr!=null) {
                    Method m = namingMgr.getClass().getMethod("bindObjects", Object.class);
                    m.invoke(namingMgr, wbd);
                }
            }

            // Add virtual server mime mappings, if present
            addMimeMappings(ctx, vs.getMimeMap());

        } catch (Throwable ex){
            exception = ex;
        }

        if (wbd != null && wbd.getApplication() != null) {
            // no dummy web module

            String moduleName;
            // S1AS BEGIN WORKAROUND FOR 6174360
            if (wbd.getApplication().isVirtual()) {
                // this is a standalone module
                moduleName = wbd.getApplication().getRegistrationName();
            } else {
                moduleName = wbd.getModuleDescriptor().getArchiveUri();
            }
            // S1AS END WORKAROUND FOR 6174360
            ctx.setModuleName(moduleName);
        } else {
            ctx.setModuleName(Constants.DEFAULT_WEB_MODULE_NAME);
        }

        try {
            vs.addChild(ctx);
        } catch (Throwable ex){
            exception = ex;
        }

        Result<WebModule> result;
        if (exception != null){
            ctx.setAvailable(false);

            _logger.log(Level.SEVERE, "webcontainer.webModuleDisabled", exception);
            result = new Result(exception);
        } else {
            result = new Result(ctx);
        }

        enableWSMonitoring(wbd, j2eeServer);
        //return exception;
        return result;

    }


    /*
     * Updates the given virtual server with the given default path.
     *
     * The given default path corresponds to the context path of one of the
     * web contexts deployed on the virtual server that has been designated
     * as the virtual server's new default-web-module.
     *
     * @param virtualServer The virtual server to update
     * @param ports The port numbers of the HTTP listeners with which the
     * given virtual server is associated
     * @param defaultContextPath The context path of the web module that has
     * been designated as the virtual server's new default web module, or null
     * if the virtual server no longer has any default-web-module
     */
    protected void updateDefaultWebModule(VirtualServer virtualServer,
            int[] ports,
            WebModuleConfig wmInfo)
            throws LifecycleException {
        
        String defaultContextPath = wmInfo.getContextPath();
        if (defaultContextPath != null
                && !defaultContextPath.startsWith("/")) {
            defaultContextPath = "/" + defaultContextPath;
            wmInfo.getDescriptor().setContextRoot(defaultContextPath);
        }

        Connector[] connectors = _embedded.findConnectors();
        for (int i=0; i<connectors.length; i++) {
            PECoyoteConnector conn = (PECoyoteConnector) connectors[i];
            int port = conn.getPort();
            for (int j=0; j<ports.length; j++) {
                if (port == ports[j]) {
                    Mapper mapper = conn.getMapper();
                    try {
                        mapper.setDefaultContextPath(virtualServer.getName(),
                                defaultContextPath);
                        CoyoteAdapter adapter = adapterMap.get(Integer.valueOf(port));
                        WebApplication application = new WebApplication(this, 
                                wmInfo, dispatcher);
                        dispatcher.registerEndpoint("/", adapter, application);
                    } catch (Exception e) {
                        throw new LifecycleException(e);
                    }
                }
            }
        }
    }

    /**
     * Utility Method to access the ServerContext
     */
    public ServerContext getServerContext() {
        return _serverContext;
    }

    ServerConfigLookup getServerConfigLookup() {
        return serverConfigLookup;
    }

    /**
     * @return The work root directory of all webapps bundled in EAR Files
     */
    File getAppsWorkRoot() {
        return _appsWorkRoot;
    }

    /**
     * @return The work root directory of all standalone webapps
     */
    File getModulesWorkRoot() {
        return _modulesWorkRoot;
    }

    /**
     * Configure the class loader for the web module based on the
     * settings in sun-web.xml's class-loader element (if any).
     */
    private Loader configureLoader(WebModule ctx, SunWebApp bean,
            WebModuleConfig wmInfo) {

        com.sun.enterprise.deployment.runtime.web.ClassLoader clBean = null;

        WebappLoader loader = new V3WebappLoader(wmInfo.getAppClassLoader());

        loader.setUseMyFaces(ctx.isUseMyFaces());

        if (bean != null) {
            clBean = bean.getClassLoader();
        }
        if (clBean != null) {
            configureLoaderAttributes(loader, clBean, ctx);
            configureLoaderProperties(loader, clBean);
        } else {
            loader.setDelegate(true);
        }

        // START S1AS 6178005
        String stubPath = wmInfo.getStubPath();
        if (stubPath != null) {
            loader.addRepository("file:" + stubPath + File.separator);
        }
        // END S1AS 6178005

        addLibs(loader, ctx);

        // START PE 4985680
        /**
         * Adds the given package name to the list of packages that may
         * always be overriden, regardless of whether they belong to a
         * protected namespace
         */
        String packagesName =
                System.getProperty("com.sun.enterprise.overrideablejavaxpackages");

        if (packagesName != null) {
            List overridablePackages =
                    StringUtils.parseStringList(packagesName, " ,");
            for( int i=0; i < overridablePackages.size(); i++){
                loader.addOverridablePackage((String)overridablePackages.get(i));
            }
        }
        // END PE 4985680

        ctx.setLoader(loader);

        return loader;
    }

    /**
     * Configure miscellaneous settings such as the pool size for
     * single threaded servlets, specifying a temporary directory other
     * than the default etc.
     *
     * Since the work directory is used when configuring the session manager
     * persistence settings, this method must be invoked prior to
     * <code>configureSessionSettings</code>.
     */
    private void configureMiscSettings(WebModule ctx, SunWebApp bean,
            VirtualServer vs, String contextPath) {

        /*
         * Web app inherits setting of allowLinking property from vs on which
         * it is being deployed, but may override it using allowLinking
         * property in its sun-web.xml
         */
        boolean allowLinking = vs.getAllowLinking();

        if ((bean != null) && (bean.sizeWebProperty() > 0)) {
            WebProperty[] props = bean.getWebProperty();
            for (int i = 0; i < props.length; i++) {

                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name == null || value == null) {
                    throw new IllegalArgumentException(
                            _rb.getString("webcontainer.nullWebProperty"));
                }

                if (name.equalsIgnoreCase("singleThreadedServletPoolSize")) {
                    int poolSize = ctx.getSTMPoolSize();
                    try {
                        poolSize = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        Object[] params =
                        { value, contextPath, Integer.toString(poolSize) };
                        _logger.log(Level.WARNING,
                                "webcontainer.invalidServletPoolSize",
                                params);
                    }
                    if (poolSize > 0) {
                        ctx.setSTMPoolSize(poolSize);
                    }

                } else if (name.equalsIgnoreCase("tempdir")) {
                    ctx.setWorkDir(value);
                } else if (name.equalsIgnoreCase("crossContextAllowed")) {
                    boolean crossContext = Boolean.parseBoolean(value);
                    ctx.setCrossContext(crossContext);
                } else if (name.equalsIgnoreCase("allowLinking")) {
                    allowLinking = ConfigBeansUtilities.toBoolean(value);
                    // START S1AS8PE 4817642
                } else if (name.equalsIgnoreCase("reuseSessionID")) {
                    boolean reuse = ConfigBeansUtilities.toBoolean(value);
                    ctx.setReuseSessionID(reuse);
                    if (reuse) {
                        Object[] params = { contextPath,
                        vs.getID() };
                        _logger.log(Level.WARNING,
                                "webcontainer.sessionIDsReused",
                                params);
                    }
                    // END S1AS8PE 4817642
                } else if(name.equalsIgnoreCase("useResponseCTForHeaders")) {
                    if(value.equalsIgnoreCase("true")) {
                        ctx.setResponseCTForHeaders();
                    }
                } else if(name.equalsIgnoreCase("encodeCookies")) {
                    boolean flag = ConfigBeansUtilities.toBoolean(value);
                    ctx.setEncodeCookies(flag);
                    // START RIMOD 4642650
                } else if (name.equalsIgnoreCase("relativeRedirectAllowed")) {
                    boolean relativeRedirect = ConfigBeansUtilities.toBoolean(value);
                    ctx.setAllowRelativeRedirect(relativeRedirect);
                    // END RIMOD 4642650
                } else if (name.equalsIgnoreCase("fileEncoding")) {
                    ctx.setFileEncoding(value);
                } else if (name.equalsIgnoreCase("enableTldValidation")
                &&  ConfigBeansUtilities.toBoolean(value)) {
                    ctx.setTldValidation(true);
                } else if (name.equalsIgnoreCase("enableTldNamespaceAware")
                &&  ConfigBeansUtilities.toBoolean(value)) {
                    ctx.setTldNamespaceAware(true);
                } else if (name.equalsIgnoreCase("securePagesWithPragma")){
                    boolean securePagesWithPragma = ConfigBeansUtilities.toBoolean(value);
                    ctx.setSecurePagesWithPragma(securePagesWithPragma);
                } else if (name.equalsIgnoreCase("useMyFaces")){
                    ctx.setUseMyFaces(ConfigBeansUtilities.toBoolean(value));
                } else if (name.startsWith("alternatedocroot_")) {
                    ctx.parseAlternateDocBase(name, value);
                } else {
                    Object[] params = { name, value };
                    _logger.log(Level.WARNING, "webcontainer.invalidProperty",
                            params);
                }
            }
        }

        ctx.setAllowLinking(allowLinking);
    }


    /**
     * The application id for this web module
     * HERCULES:add
     */
    public String getApplicationId(WebModule wm) {
        return wm.getID();
    }


    /**
     * Return the Absolute path for location where all the deployed
     * standalone modules are stored for this Server Instance.
     */
    public File getModulesRoot() {
        return _modulesRoot;
    }


    /**
     * Get the persistence frequency for this web module
     * (this is the value from sun-web.xml if defined
     * @param smBean  the session manager config bean
     * HERCULES:add
     */
    private String getPersistenceFrequency(SessionManager smBean) {
        String persistenceFrequency = null;
        ManagerProperties mgrBean = smBean.getManagerProperties();
        if ((mgrBean != null) && (mgrBean.sizeWebProperty() > 0)) {
            WebProperty[] props = mgrBean.getWebProperty();
            for (int i = 0; i < props.length; i++) {

                String name = props[i].getAttributeValue(WebProperty.NAME);
                String value = props[i].getAttributeValue(WebProperty.VALUE);
                if (name == null || value == null) {
                    throw new IllegalArgumentException(
                            _rb.getString("webcontainer.nullWebProperty"));
                }

                if (name.equalsIgnoreCase("persistenceFrequency")) {
                    persistenceFrequency = value;
                    break;
                }
            }
        }
        return persistenceFrequency;
    }

    /**
     * Get the persistence scope for this web module
     * (this is the value from sun-web.xml if defined
     * @param smBean the session manager config bean
     * HERCULES:add
     */
    private String getPersistenceScope(SessionManager smBean) {
        String persistenceScope = null;
        StoreProperties storeBean = smBean.getStoreProperties();
        if ((storeBean != null) && (storeBean.sizeWebProperty() > 0)) {
            WebProperty[] props = storeBean.getWebProperty();
            for (int i = 0; i < props.length; i++) {

                String name = props[i].getAttributeValue(WebProperty.NAME);
                String value = props[i].getAttributeValue(WebProperty.VALUE);
                if (name == null || value == null) {
                    throw new IllegalArgumentException(
                            _rb.getString("webcontainer.nullWebProperty"));
                }

                if (name.equalsIgnoreCase("persistenceScope")) {
                    persistenceScope = value;
                    break;
                }
            }
        }
        return persistenceScope;
    }


    /**
     * Undeploy a web application.
     *
     * @param contextRoot the context's name to undeploy
     * @param appName the J2EE appname used at deployment time
     * @param virtualServers List of current virtual-server object.
     * @param wbd Web bundle descriptor, or null if dummy context
     *        (see loadWebModule())
     */
    public void unloadWebModule(String contextRoot,
            String appName,
            String virtualServers,
            WebBundleDescriptor wbd) {
        unloadWebModule(contextRoot, appName, virtualServers, wbd, false);
    }

    /**
     * Undeploy a web application.
     *
     * @param contextRoot the context's name to undeploy
     * @param appName the J2EE appname used at deployment time
     * @param virtualServers List of current virtual-server object.
     * @param wbd Web bundle descriptor, or null if dummy context
     *        (see loadWebModule())
     * @param dummy true if the web module to be undeployed is a dummy web
     *        module, that is, a web module created off of a virtual server's
     *        docroot
     */
    public void unloadWebModule(String contextRoot,
            String appName,
            String virtualServers,
            WebBundleDescriptor wbd,
            boolean dummy) {
        disableWSMonitoring(wbd);

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("WebContainer.unloadWebModule(): contextRoot: "
                    + contextRoot + " appName:" + appName);
        }

        cleanSecurityContext(appName);

        // tomcat contextRoot starts with "/"
        if (!contextRoot.equals("") && !contextRoot.startsWith("/") ) {
            contextRoot = "/" + contextRoot;
        } else if ("/".equals(contextRoot)) {
            // Make corresponding change as in WebModuleConfig.getContextPath()
            contextRoot = "";
        }

        Engine[] engines = _embedded.getEngines();
        List hostList = StringUtils.parseStringList(virtualServers, " ,");
        boolean unloadToAll = (hostList == null) || (hostList.size() == 0);
        boolean hasBeenUndeployed = false;
        Container[] hostArray = null;
        VirtualServer host = null;
        WebModule context = null;

        for (int j = 0; j < engines.length; j++) {
            hostArray = engines[j].findChildren();
            for (int i = 0; i < hostArray.length; i++) {
                host = (VirtualServer) hostArray[i];

                /**
                 * Related to Bug: 4904290
                 * Do not unloadload module on ADMIN_VS
                 */
                if ( unloadToAll && host.getName().equalsIgnoreCase(
                        VirtualServer.ADMIN_VS)){
                    continue;
                }

                if (unloadToAll
                        || hostList.contains(host.getName())
                        || verifyAlias(hostList,host)){

                    context = (WebModule) host.findChild(contextRoot);
                    if (context != null) {
                        host.removeChild(context);
                        try {
                            context.destroy();
                        } catch (Exception ex) {
                            _logger.log(Level.WARNING,
                                    "[WebContainer] Context " + contextRoot
                                    + " threw exception in destroy()", ex);
                        }
                        if (_logger.isLoggable(Level.FINEST)) {
                            _logger.log(Level.FINEST,
                                    "[WebContainer] Context " + contextRoot
                                    + " undeployed from " + host);
                        }
                        hasBeenUndeployed = true;
                        host.fireContainerEvent(
                                Deployer.REMOVE_EVENT, context);
                        /*
                         * If the web module that has been unloaded
                         * contained any mappings for ad-hoc paths,
                         * those mappings must be preserved by registering an
                         * ad-hoc web module at the same context root
                         */
                        if (context.hasAdHocPaths()
                        || context.hasAdHocSubtrees()) {
                            WebModule wm = createAdHocWebModule(
                                    host,
                                    contextRoot,
                                    context.getJ2EEApplication());
                            wm.addAdHocPaths(context.getAdHocPaths());
                            wm.addAdHocSubtrees(context.getAdHocSubtrees());
                        }

                        // START GlassFish 141
                        if (!dummy) {
                            WebModuleConfig wmInfo =
                                    host.createSystemDefaultWebModuleIfNecessary(
                                    _serverContext.getDefaultHabitat().getComponent(
                                    WebDeployer.class));
                            if (wmInfo != null) {
                                loadStandaloneWebModule(host, wmInfo);
                            }
                        }
                        // END GlassFish 141
                    }
                }
            }
        }

        if (!hasBeenUndeployed) {
            _logger.log(Level.SEVERE,
                    "[WebContainer] Undeployment failed for context "
                    + contextRoot);
        }
    }


    /**
     * Creates and configures a web module for each virtual server
     * that the web module is hosted under. If the web module has been
     * disabled by a call to disableWebModule, enable the module
     * instead of re-crearing new one.
     *
     * If no virtual servers are specified, then the web module is
     * enabled` on EVERY virtual server.
     */
    public void enableWebModule(WebModuleConfig wmInfo,
                                String j2eeApplication){
        String vsIDs = wmInfo.getVirtualServers();
        List vsList = StringUtils.parseStringList(vsIDs, " ,");
        boolean enabledToAll = (vsList == null) || (vsList.size() == 0);

        Engine[] engines =  _embedded.getEngines();

        for (int j=0; j<engines.length; j++) {
            Container[] vsArray = engines[j].findChildren();
            for (int i = 0; i < vsArray.length; i++) {
                if (vsArray[i] instanceof VirtualServer) {
                    VirtualServer vs = (VirtualServer) vsArray[i];

                    /*
                     * Fix for 4913636: If vsList is null and virtual server is
                     * equal to __asadmin, continue with next iteration
                     * because we don't want to load user apps on __asadmin
                     */
                    if (vs.getID().equals(VirtualServer.ADMIN_VS) && enabledToAll) {
                        continue;
                    }

                    if ( enabledToAll
                            || vsList.contains(vs.getID())
                            || verifyAlias(vsList,vs)){

                        loadWebModule(vs, wmInfo, j2eeApplication);
                    }
                }
            }
        }
    }

    /**
     * Suspends the web application with the given appName that has been
     * deployed at the given contextRoot on the given virtual servers.
     *
     * @param contextRoot the context root
     * @param appName the J2EE appname used at deployment time
     * @param hosts the comma- or space-separated list of virtual servers
     */
    public boolean suspendWebModule(String contextRoot,
                                    String appName,
                                    String hosts) {
        return suspendWebModule(contextRoot, appName,
                                StringUtils.parseStringList(hosts, " ,"));
    }

    /**
     * Suspends the web application with the given appName that has been
     * deployed at the given contextRoot on the given virtual servers.
     *
     * @param contextRoot the context root
     * @param appName the J2EE appname used at deployment time
     * @param hosts the list of virtual servers
     */
    public boolean suspendWebModule(String contextRoot,
                                    String appName,
                                    List<String> hosts) {
        // tomcat contextRoot starts with "/"
        if (!contextRoot.equals("") && !contextRoot.startsWith("/") ) {
            contextRoot = "/" + contextRoot;
        }

        Engine[] engines = _embedded.getEngines();
        boolean suspendOnAll = (hosts == null) || (hosts.size() == 0);
        boolean hasBeenSuspended = false;
        Container[] hostArray = null;
        VirtualServer host = null;
        Context context = null;

        for (int j = 0; j < engines.length; j++) {
            hostArray = engines[j].findChildren();
            for (int i = 0; i < hostArray.length; i++) {
                host = (VirtualServer) hostArray[i];

                /**
                 * Related to Bug: 4904290
                 * Do not unloadload module on ADMIN_VS
                 */
                if (suspendOnAll
                        && host.getName().equalsIgnoreCase(VirtualServer.ADMIN_VS)){
                    continue;
                }

                if (suspendOnAll
                        || hosts.contains(host.getName())
                        || verifyAlias(hosts,host)){

                    context = (Context) host.findChild(contextRoot);
                    if (context != null) {
                        context.setAvailable(false);
                        if (_logger.isLoggable(Level.FINEST)) {
                            _logger.log(Level.FINEST,
                                    "[WebContainer] Context "
                                    + contextRoot + " disabled from "
                                    + host);
                        }
                        hasBeenSuspended = true;
                    }
                }
            }
        }

        if (!hasBeenSuspended){
            _logger.log(Level.WARNING,
                    "[WebContainer] Unable to disable web module at " +
                    "context root " + contextRoot);
        }

        return hasBeenSuspended;
    }


    /**
     * Save the server-wide dynamic reloading settings for use when
     * configuring each web module.
     */
    private void configureDynamicReloadingSettings() {
        if (dasConfig != null) {
            _reloadingEnabled = Boolean.parseBoolean(dasConfig.getDynamicReloadEnabled());
            String seconds = dasConfig.getDynamicReloadPollIntervalInSeconds();
            if (seconds != null) {
                try {
                    _pollInterval = Integer.parseInt(seconds);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    /**
     * Set the level for the web container logger and determine the
     * debug setting for Catalina's containers based on the iAS log
     * level.
     */
    private void setLogLevel(Level level) {
        _logLevel = level;
        _logger.setLevel(_logLevel);

        // Determine the appropriate value for Catalina's debug level
        if (level.equals(Level.FINE))
            _debug = 1;
        else if (level.equals(Level.FINER))
            _debug = 2;
        else if (level.equals(Level.FINEST))
            _debug = 5;
        else
            _debug = 0;
    }


   /*
    * Initializes the web container log level from the domain.xml.
    *
    * @param logserviceBean The log service bean from which to retrieve the
    *        web container log level
    */
    private void initLogLevel(LogService logserviceBean) {

        Level level = Level.INFO;
        
        if (logserviceBean != null
                && logserviceBean.getModuleLogLevels() != null) {
            try {
                level = Level.parse(logserviceBean.getModuleLogLevels().getWebContainer());
            } catch (IllegalArgumentException iae) {
                _logger.log(Level.WARNING,
                            "Unable to parse web-container log level",
                            iae);
            }
        }

        setLogLevel(level);
    }


   /*
    * Initializes the web container monitoring level from the domain.xml.
    *
    * @param monitoringBean The monitoring service bean from which to retrieve
    *        the web container monitoring level
    */
    private void initMonitoringLevel(MonitoringService monitoringBean) {

        monitoringLevel = MonitoringLevel.OFF; // default per DTD

        if (monitoringBean != null) {
            ModuleMonitoringLevels levels =
                    monitoringBean.getModuleMonitoringLevels();
            if (levels != null) {
                monitoringLevel = MonitoringLevel.instance(
                        levels.getWebContainer());
            }
        }
    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }


    /**
     * Clean security policy generated at deployment time.
     * NOTE: This routine calls destroy on the WebSecurityManagers,
     * but that does not cause deletion of the underlying policy (files).
     * The underlying policy is deleted when removePolicy
     * (in AppDeployerBase and WebModuleDeployer) is called.
     * @param appName  the app name
     */
    private void cleanSecurityContext(String appName) {
/*	String cIDs[] = webSecurityManagerFactory.getContextIdsOfApp(appName);

        for (int i=0; cIDs != null && i <cIDs.length; i++) {
            WebSecurityManager wsm
                = webSecurityManagerFactory.getWebSecurityManager(cIDs[i]);

            _logger.log(Level.FINE,"[JACC]: Removing WebSecurityManager: "
                + cIDs[i]);

            if (wsm != null) {
                try {
                    wsm.destroy();
                } catch (javax.security.jacc.PolicyContextException pce){
                    // log it and continue
                    _logger.log(Level.WARNING,
                            "Unable to destroy WebSecurityManager",
                            pce);
                }
                webSecurityManagerFactory.removeWebSecurityManager(cIDs[i]);
            }
        }
 */
    }

    /**
     * Virtual servers are maintained in the reference contained
     * in Server element. First, we need to find the server
     * and then get the virtual server from the correct reference
     *
     * @param appName Name of the app to get vs
     * @param configCtx config context to use to obtain virtual servers. If
     *     this parameter is null, config context cached at startup time will
     *     be used.
     *
     * @return virtual servers as a string (separated by space or comma)
     */
    /*private String getVirtualServers(String appName, ConfigContext configCtx) {

        Server server = _serverContext.getDefaultHabitat().getComponent(Server.class);
        for (ApplicationRef ref : server.getApplicationRef()) {
            if (ref.getRef().equals(appName)) {
                return ref.getVirtualServers();          }
        }
        return null;
    }*/

    /**
     * Adds the given mime mappings to those of the specified context, unless
     * they're already present in the context (that is, the mime mappings of
     * the specified context, which correspond to those in default-web.xml,
     * can't be overridden).
     *
     * @param ctx The StandardContext to whose mime mappings to add
     * @param mimeMap The mime mappings to be added
     */
    private void addMimeMappings(StandardContext ctx, MimeMap mimeMap) {
        if (mimeMap == null) {
            return;
        }

        for (Iterator itr = mimeMap.getExtensions(); itr.hasNext(); ) {
            String extension = (String) itr.next();
            if (ctx.findMimeMapping(extension) == null) {
                ctx.addMimeMapping(extension, mimeMap.getType(extension));
            }
        }
    }

    /**
     * Enables monitoring of web service endpoints in for all webmodules.
     */
    private void enableAllWSEndpoints() {
        for (J2eeApplication appBean : domain.getApplications().getModules(J2eeApplication.class)) {
            //Begin EE: 4927099 - load only associated applications
            if ( isReferenced(appBean.getName()) ) {
                enableWSMonitoring(appBean.getName());
            }
            //End EE: 4927099 - load only associated applications
        }
    }

    /**
     * Disables monitoring of web service endpoints in a webmodule.
     *
     * @param wbd WebBundleDescriptor of the web module
     *
     * @return boolean true, if web service mbeans were unloaded successfully
     */
    private boolean disableWSMonitoring(WebBundleDescriptor wbd)    {

        boolean result = true;
/*
            try {
            Switch.getSwitch().getManagementObjectManager().
                deleteWSEndpointMBeans(wbd, instance.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
 */
        return true;
    }

    /**
     * Enables monitoring of web service endpoints in a webmodule.
     *
     * @param id ID of the application
     *
     * @return boolean true, if web service mbeans were intialized successfully
     */
    private boolean enableWSMonitoring(String id)    {

        boolean result = true;
/*
       ApplicationRegistry registry = ApplicationRegistry.getInstance();
            ClassLoader appLoader = registry.getClassLoaderForApplication(id);
            if (appLoader != null) {
                Application appDesc = registry.getApplication(appLoader);

                // Check to see if this app had deployed successfully (4663247)
                if(appDesc == null){
                    Object[] params = { id };
                    _logger.log(Level.SEVERE, "webcontainer.notLoaded",
                                params);
                } else {
                    //end Hercules: add
                    String j2eeApplication = appDesc.getRegistrationName();
                    Set wbds = appDesc.getWebBundleDescriptors();
                    WebBundleDescriptor wbd = null;
                    for (Iterator itr = wbds.iterator(); itr.hasNext(); ) {
                        wbd = (WebBundleDescriptor) itr.next();

                        try {
                            Switch.getSwitch().getManagementObjectManager().
                            createWSEndpointMBeans(wbd, instance.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
 */
        return true;
    }

    void enableWSMonitoring(WebBundleDescriptor wbd, String serverName) {
/*
        try {
             Switch.getSwitch().getManagementObjectManager().
             createWSEndpointMBeans(wbd, serverName);
         } catch (Exception e) {
             e.printStackTrace();
         }
 *
 */
    }

    /**
     * Enables monitoring on the Servlets in a webmodule.
     *
     * @param ctx Web module to be monitored
     * @param vsId  The engine that is currently loading the webmodule
     */
    void enableMonitoring(WebModule ctx, String vsId) {
        /*  XXX not yet
        if (!ctx.hasWebXml()) {
            // Ad-hoc module
            return;
        }

        String j2eeServer = _serverContext.getInstanceName();

        // Register web module stats
        registerWebModuleStats(ctx.getJ2EEApplication(), j2eeServer, vsId,
                ctx, null);

        // Register stats for each of the web module's servlets
        Container[] children = ctx.findChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                registerServletStats(ctx.getJ2EEApplication(), j2eeServer,
                        ctx.getModuleName(), vsId,
                        ctx.getEncodedPath(),
                        children[i].getName(), null);
            }
        }
         */
    }


    /**
     * Disables monitoring on the given web module.
     */
    protected void disableMonitoring(WebModule ctx, String vsId) {
        /*
        if (!ctx.hasWebXml()) {
            // Ad-hoc module
            return;
        }

        /*
         * Standalone webmodules are loaded with the application name set to
         * the string "null"
         *
        String appName = ctx.getJ2EEApplication();
        if ("null".equalsIgnoreCase(appName)) {
            appName = null;
        }

        // Unregister stats for each of the web module's servlets
        Container[] children = ctx.findChildren();
        if (children != null) {
            for (int k = 0; k < children.length; k++) {
                unregisterServletStats(appName, ctx.getModuleName(),
                        ctx.getEncodedPath(), vsId,
                        children[k].getName());
            }
        }
        // Unregister web module stats
        unregisterWebModuleStats(appName, ctx.getModuleName(),
                ctx.getEncodedPath(), vsId);
         */

    }

    /*
     * Includes, from TLD scanning, any user specified JARs on the classpath
     * and higher up in the classloader delegation chain that are known to
     * contain any TLDs or TLD listeners.
     */
    private void setTldScan() {

        HashSet<String> tldJars = null;
        HashSet<String> tldListeners = null;

        String prop = System.getProperty("com.sun.enterprise.taglibs");
        if (prop != null && prop.trim().length() > 0) {
            tldJars = new HashSet<String>(
                    StringUtils.parseStringList(prop, ","));
        }

        prop = System.getProperty("com.sun.enterprise.taglisteners");
        if (prop != null && prop.trim().length() > 0) {
            tldListeners = new HashSet<String>(
                    StringUtils.parseStringList(prop, ","));
        }

        // Check to see if we need to scan the parent classloader when
        // searching for TLD listener. JSF application mandate the search, as
        // well as shared TLD added to the
        // property com.sun.enterprise.taglisteners
        if ( tldListeners != null && tldListeners.size() > 0 ){
            // By default, domain.xml contains no element.
            TldConfig.setScanParentTldListener(true);
        }

        TldConfig.setTldListeners(tldListeners);
        TldLocationsCache.setTldJars(tldJars);
    }


    // Begin EE: 4927099 load only associated applications
    /**
     * Returns true if the names application is referenced by this server.
     *
     * @param  appName  name of the application id
     * @return true if the names application is referenced by this server
     */
    private boolean isReferenced(String appName) {

        Servers servers = domain.getServers();
        for (Server server : servers.getServer()) {
            if (server.getName().equals(_serverContext.getInstanceName())) {
                for (ApplicationRef ref : server.getApplicationRef()) {
                    if (ref.getRef().equals(appName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // End EE: 4927099 load only associated applications


    /*
     * Creates a ServletStats instance for the servlet with the given
     * <code>servletName</code> and registers it with the monitoring registry.
     *
     * @param j2eeApplication String representing the J2EE Application to which
     *        the web module belongs, or null if the web module is stand-alone
     * @param j2eeServer The server instance name
     * @param webModuleName The name of the web module to which the servlet
     *        belongs
     * @param vsId The id of the virtual server on which the web module has
     *        been deployed
     * @param contextRoot Context root at which web module has been deployed
     * @param servletName The servlet name
     * @param listener The listener for monitoring level changes
     */
    private void registerServletStats(String j2eeApplication,
            String j2eeServer,
            String webModuleName,
            String vsId,
            String contextRoot,
            String servletName,
            MonitoringLevelListener listener) {

        PwcServletStats pwcServletStats = new PwcServletStatsImpl(
                _serverContext.getDefaultDomainName(),
                vsId, contextRoot, servletName,
                j2eeApplication, j2eeServer);
        ServletStats servletStats = new ServletStatsImpl(pwcServletStats);


        MonitoringRegistry monitoringRegistry =
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);

        /*
         * Standalone webmodules are loaded with the application name set to
         * the string "null"
         */
        String app = ("null".equalsIgnoreCase(j2eeApplication) ?
            null : j2eeApplication);
        try {
            monitoringRegistry.registerServletStats(servletStats,
                    app,
                    webModuleName,
                    contextRoot,
                    vsId,
                    servletName,
                    listener);
        } catch(Exception e) {
            _logger.log(Level.WARNING,
                    "Exception during registration of servletstats",
                    e);
        }
    }


    /*
     * Removes the ServletStats instance associated with the servlet with the
     * given <code>servletName</code> from the monitoring registry.
     *
     * @param j2eeApplication String representing the J2EE Application to which
     *        the web module belongs, or null if the web module is stand-alone
     * @param webModuleName The name of the web module to which the servlet
     *        belongs
     * @param contextRoot Context root at which web module has been deployed
     * @param vsId The id of the virtual server on which the web module has
     *        been deployed
     * @param servletName The servlet name
     */
    private void unregisterServletStats(String j2eeApplication,
            String webModuleName,
            String contextRoot,
            String vsId,
            String servletName) {

        MonitoringRegistry monitoringRegistry =
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);

        try {
            monitoringRegistry.unregisterServletStats(j2eeApplication,
                    webModuleName,
                    contextRoot,
                    vsId,
                    servletName);
        } catch (Exception e) {
            _logger.log(Level.WARNING,
                    "Exception during unregistration of servletstats",
                    e);
        }
    }


    /*
     * Creates a WebModuleStats instance for the given web module and
     * registers it with the monitoring registry.
     *
     * @param j2eeApplication String representing the J2EE Application to which
     *        the web module belongs, or null if the web module is stand-alone
     * @param j2eeServer The server instance name
     * @param vsId The id of the virtual server on which the web module has
     *        been deployed
     * @param ctx The web module
     * @param listener The listener for monitoring level changes
     *
     * @return The WebModuleStats that was registered with the monitoring
     * registry
     */
    private WebModuleStats registerWebModuleStats(
            String j2eeApplication,
            String j2eeServer,
            String vsId,
            WebModule ctx,
            MonitoringLevelListener listener) {

        WebModuleStatsImpl webStats = (WebModuleStatsImpl)
                webContainerFeatureFactory.getWebModuleStats();
        PwcWebModuleStatsImpl pwcWebStats = new PwcWebModuleStatsImpl(
                ctx.getObjectName(),
                ctx.getEncodedPath(),
                _serverContext.getDefaultDomainName(),
                vsId,
                j2eeApplication,
                j2eeServer);
        webStats.setPwcWebModuleStats(pwcWebStats);
        webStats.setSessionManager(ctx.getManager());

        MonitoringRegistry monitoringRegistry =
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);


        /*
         * Standalone webmodules are loaded with the application name set to
         * the string "null"
         */
        String app = ("null".equalsIgnoreCase(j2eeApplication) ?
            null : j2eeApplication);
        try {
            monitoringRegistry.registerWebModuleStats(webStats,
                    app,
                    ctx.getModuleName(),
                    ctx.getEncodedPath(),
                    vsId,
                    listener);
        } catch (Exception e) {
            _logger.log(Level.WARNING,
                    "Fail to register WebModuleStats for "
                    + ctx.getModuleName() + " deployed on " + vsId, e);
        }
        return webStats;
    }


    /*
     * Removes the WebModuleStats instance associated with the web module with
     * the given <code>webModuleName</code> from the monitoring registry.
     *
     * @param j2eeApplication String representing the J2EE Application to which
     *        the web module belongs, or null if the web module is stand-alone
     * @param webModuleName The web module name
     * @param contextRoot Context root at which web module has been deployed
     * @param vsId The id of the virtual server on which the web module has
     *        been deployed
     */
    private void unregisterWebModuleStats(String j2eeApplication,
            String webModuleName,
            String contextRoot,
            String vsId) {

        MonitoringRegistry monitoringRegistry =
                _serverContext.getDefaultHabitat().getComponent(MonitoringRegistry.class);


        try {
            monitoringRegistry.unregisterWebModuleStats(j2eeApplication,
                    webModuleName,
                    contextRoot,
                    vsId);
        } catch (Exception e) {
            _logger.log(Level.WARNING,
                    "Fail to unregister WebModuleStats for "
                    + webModuleName + " deployed on " + vsId, e);
        }
    }

    /**
     * Invoked when a  reference is created from a
     * server instance (or cluster) to a particular module.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     *
     * public void moduleReferenceAdded(ModuleDeployEvent event)
     * { //    throws AdminEventListenerException {
     *
     * }*/

    /**
     * Invoked when a reference is removed from a
     * server instance (or cluster) to a particular module.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     *
     * public void moduleReferenceRemoved(ModuleDeployEvent event)
     * { //     throws AdminEventListenerException {
     *
     * }*/


    /**
     * Invoked when an application reference is created from a
     * server instance (or cluster) to a particular application.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     *
     * public void applicationReferenceAdded(ApplicationDeployEvent event)
     * {//       throws AdminEventListenerException {
     *
     * }*/

    /**
     * Invoked when a reference is removed from a
     * server instance (or cluster) to a particular application.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     *
     * public void applicationReferenceRemoved(ApplicationDeployEvent event)
     * { // throws AdminEventListenerException {
     * }*/

    /**
     * Return the current <code>WebContainer</code> child instance.
     */
    public static WebContainer getInstance(){
        return webContainer;
    }

    /**
     * Return the parent/top-level container in _embedded for virtual
     * servers.
     */
    public Engine[] getEngines() {
        return _embedded.getEngines();
    }


    /**
     * Registers the given ad-hoc path at the given context root.
     *
     * @param path The ad-hoc path to register
     * @param ctxtRoot The context root at which to register
     * @param appName The name of the application with which the ad-hoc path is
     * associated
     * @param servletInfo Info about the ad-hoc servlet that will service
     * requests on the given path
     */
    public void registerAdHocPath(String path,
            String ctxtRoot,
            String appName,
            AdHocServletInfo servletInfo) {

        registerAdHocPathAndSubtree(path, null, ctxtRoot, appName, servletInfo);
    }


    /**
     * Registers the given ad-hoc path and subtree at the given context root.
     *
     * @param path The ad-hoc path to register
     * @param subtree The ad-hoc subtree path to register
     * @param ctxtRoot The context root at which to register
     * @param appName The name of the application with which the ad-hoc path
     * and subtree are associated
     * @param servletInfo Info about the ad-hoc servlet that will service
     * requests on the given ad-hoc path and subtree
     */
    public void registerAdHocPathAndSubtree(String path,
            String subtree,
            String ctxtRoot,
            String appName,
            AdHocServletInfo servletInfo) {
        WebModule wm = null;

        Engine[] engines =  _embedded.getEngines();
        for (int j=0; j<engines.length; j++) {

            Container[] vsList = engines[j].findChildren();
            for (int i = 0; i < vsList.length; i++) {

                VirtualServer vs = (VirtualServer)vsList[i];
                if (vs.getName().equalsIgnoreCase(VirtualServer.ADMIN_VS)) {
                    // Do not deploy on admin vs
                    continue;
                }

                wm = (WebModule) vs.findChild(ctxtRoot);
                if (wm == null) {
                    wm = createAdHocWebModule(vs, ctxtRoot, appName);
                }
                wm.addAdHocPathAndSubtree(path, subtree, servletInfo);
            }
        }
    }


    /**
     * Unregisters the given ad-hoc path from the given context root.
     *
     * @param path The ad-hoc path to unregister
     * @param ctxtRoot The context root from which to unregister
     */
    public void unregisterAdHocPath(String path, String ctxtRoot) {
        unregisterAdHocPathAndSubtree(path, null, ctxtRoot);
    }


    /**
     * Unregisters the given ad-hoc path and subtree from the given context
     * root.
     *
     * @param path The ad-hoc path to unregister
     * @param subtree The ad-hoc subtree to unregister
     * @param ctxtRoot The context root from which to unregister
     */
    public void unregisterAdHocPathAndSubtree(String path,
            String subtree,
            String ctxtRoot) {

        WebModule wm = null;

        Engine[] engines =  _embedded.getEngines();
        for (int j=0; j<engines.length; j++) {

            Container[] vsList = engines[j].findChildren();
            for (int i = 0; i < vsList.length; i++) {

                VirtualServer vs = (VirtualServer) vsList[i];
                if (vs.getName().equalsIgnoreCase(VirtualServer.ADMIN_VS)) {
                    // Do not undeploy from admin vs, because we never
                    // deployed onto it
                    continue;
                }

                wm = (WebModule) vs.findChild(ctxtRoot);
                if (wm == null) {
                    continue;
                }

                /*
                 * If the web module was created by the container for the
                 * sole purpose of mapping ad-hoc paths and subtrees,
                 * and does no longer contain any ad-hoc paths or subtrees,
                 * remove the web module.
                 */
                wm.removeAdHocPath(path);
                wm.removeAdHocSubtree(subtree);
                if ((wm instanceof AdHocWebModule)
                && !wm.hasAdHocPaths()
                && !wm.hasAdHocSubtrees()) {
                    vs.removeChild(wm);
                    try {
                        wm.destroy();
                    } catch (Exception ex) {
                        _logger.log(Level.WARNING,
                                "[WebContainer] Context " + wm.getPath()
                                + " threw exception in destroy()", ex);
                    }
                }
            }
        }
    }


    /*
     * Creates an ad-hoc web module and registers it on the given virtual
     * server at the given context root.
     *
     * @param vs The virtual server on which to add the ad-hoc web module
     * @param ctxtRoot The context root at which to register the ad-hoc
     * web module
     * @param appName The name of the application to which the ad-hoc module
     * being generated belongs
     *
     * @return The newly created ad-hoc web module
     */
    private WebModule createAdHocWebModule(VirtualServer vs,
            String ctxtRoot,
            String appName) {

        AdHocWebModule wm = new AdHocWebModule(this);

        wm.restrictedSetPipeline(new WebPipeline(wm));

        // The Parent ClassLoader of the AdhocWebModule was null
        // [System ClassLoader]. With the new hierarchy, the thread context
        // classloader needs to be set.
        //if (Boolean.getBoolean(com.sun.enterprise.server.PELaunch.USE_NEW_CLASSLOADER_PROPERTY)) {
        wm.setParentClassLoader(
                Thread.currentThread().getContextClassLoader());
        //}

        wm.setContextRoot(ctxtRoot);
        wm.setJ2EEApplication(appName);
        wm.setName(ctxtRoot);
        wm.setDocBase(vs.getAppBase());
        wm.setEngineName(vs.getParent().getName());

        String domain = _serverContext.getDefaultDomainName();
        wm.setDomain(domain);

        String j2eeServer = _serverContext.getInstanceName();
        wm.setJ2EEServer(j2eeServer);

        String server = domain + ":j2eeType=J2EEServer,name=" + j2eeServer;
        wm.setServer(server);

        wm.setCrossContext(true);
        //wm.setJavaVMs(J2EEModuleUtil.getjavaVMs());

        vs.addChild(wm);

        return wm;
    }


    /**
     * Adds all libraries specified via the --libraries deployment option to
     * the given loader associated with the given web context.
     *
     * @param loader The loader to configure
     * @param ctx The loader's associated web context
     */
    private void addLibs(Loader loader, WebModule ctx) {

        String list = ASClassLoaderUtil.getLibrariesForModule(WebModule.class, ctx.getID());
        if (list == null) {
            return;
        }
        String[] libs = list.split(",");
        if (libs == null) {
            return;
        }

        File libDir = instance.getLibPath();
        String libDirPath = libDir.getAbsolutePath();
        String appLibsPrefix = libDirPath + File.separator + "applibs"
                + File.separator;

        for (int i=0; i<libs.length; i++) {
            try {
                URL url = new URL(libs[i]);
                loader.addRepository(libs[i]);
            } catch (MalformedURLException e) {
                // Not a URL, interpret as file
                File file = new File(libs[i]);
                if (file.isAbsolute()) {
                    loader.addRepository("file:" + file.getAbsolutePath());
                } else {
                    loader.addRepository("file:" + appLibsPrefix + libs[i]);
                }
            }
        }
    }


    /**
     * Configures the given classloader with its attributes specified in
     * sun-web.xml.
     *
     * @param loader The classloader to configure
     * @param clBean The class-loader info from sun-web.xml
     * @param ctx The classloader's associated web module
     */
    private void configureLoaderAttributes(
            Loader loader,
            com.sun.enterprise.deployment.runtime.web.ClassLoader clBean,
            WebModule ctx) {

        String value = clBean.getAttributeValue(
                com.sun.enterprise.deployment.runtime.web.ClassLoader.DELEGATE);

        /*
         * The DOL will *always* return a value: If 'delegate' has not been
         * configured in sun-web.xml, its default value will be returned,
         * which is FALSE in the case of sun-web-app_2_2-0.dtd and
         * sun-web-app_2_3-0.dtd, and TRUE in the case of
         * sun-web-app_2_4-0.dtd.
         */
        boolean delegate = ConfigBeansUtilities.toBoolean(value);
        loader.setDelegate(delegate);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("WebModule[" + ctx.getPath()
            + "]: Setting delegate to " + delegate);
        }

        // Get any extra paths to be added to the class path of this
        // class loader
        value = clBean.getAttributeValue(
                com.sun.enterprise.deployment.runtime.web.ClassLoader.EXTRA_CLASS_PATH);
        if (value != null) {
            // Parse the extra classpath into its ':' and ';' separated
            // components. Ignore ':' as a separator if it is preceded by
            // '\'
            String[] pathElements = value.split(";|((?<!\\\\):)");
            if (pathElements != null) {
                for (String path : pathElements) {
                    path = path.replace("\\:", ":");
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("WebModule[" + ctx.getPath()
                        + "]: Adding " + path
                                + " to the classpath");
                    }

                    try {
                        URL url = new URL(path);
                        loader.addRepository(path);
                    } catch (MalformedURLException mue1) {
                        // Not a URL, interpret as file
                        File file = new File(path);
                        // START GlassFish 904
                        if (!file.isAbsolute()) {
                            // Resolve relative extra class path to the
                            // context's docroot
                            file = new File(ctx.getDocBase(), path);
                        }
                        // END GlassFish 904

                        try {
                            URL url = file.toURI().toURL();
                            loader.addRepository(url.toString());
                        } catch (MalformedURLException mue2) {
                            String msg = _rb.getString(
                                    "webcontainer.classpathError");
                            Object[] params = { path };
                            msg = MessageFormat.format(msg, params);
                            _logger.log(Level.SEVERE, msg, mue2);
                        }
                    }
                }
            }
        }

        value = clBean.getAttributeValue(
                com.sun.enterprise.deployment.runtime.web.ClassLoader.DYNAMIC_RELOAD_INTERVAL);
        if (value != null) {
            // Log warning if dynamic-reload-interval is specified
            // in sun-web.xml since it is not supported
            _logger.log(Level.WARNING,
                    "webcontainer.dynamicReloadInterval");
        }
    }


    /**
     * Configures the given classloader with its properties specified in
     * sun-web.xml.
     *
     * @param loader The classloader to configure
     * @param clBean The class-loader info from sun-web.xml
     */
    private void configureLoaderProperties(
            Loader loader,
            com.sun.enterprise.deployment.runtime.web.ClassLoader clBean) {

        String name = null;
        String value = null;

        WebProperty[] props = clBean.getWebProperty();
        if (props == null || props.length == 0) {
            return;
        }

        for (int i = 0; i < props.length; i++) {

            name = props[i].getAttributeValue(WebProperty.NAME);
            value = props[i].getAttributeValue(WebProperty.VALUE);

            if (name == null || value == null) {
                throw new IllegalArgumentException(
                        _rb.getString("webcontainer.nullWebProperty"));
            }

            if (name.equalsIgnoreCase("ignoreHiddenJarFiles")) {
                loader.setIgnoreHiddenJarFiles(ConfigBeansUtilities.toBoolean(value));
            } else {
                Object[] params = { name, value };
                _logger.log(Level.WARNING,
                        "webcontainer.invalidProperty",
                        params);
            }
        }
    }


    /**
     * Removes the dummy module (the module created off of a virtual server's
     * docroot) from the given virtual server if such a module exists.
     *
     * @param vs The virtual server whose dummy module is to be removed
     */
    void removeDummyModule(VirtualServer vs) {
        WebModule ctx = (WebModule)vs.findChild("");
        if (ctx != null
                && Constants.DEFAULT_WEB_MODULE_NAME.equals(
                ctx.getModuleName())) {
            unloadWebModule("", ctx.getJ2EEApplication(),
                    vs.getName(), null, true);
        }
    }


    /**
     * Initializes the instance-level session properties (read from
     * config.web-container.session-config.session-properties in domain.xml).
     */
    private void initInstanceSessionProperties() {

        SessionProperties spBean =
            serverConfigLookup.getInstanceSessionProperties();

        if (spBean == null || spBean.getProperty() == null) {
            return;
        }

        List<Property> props = spBean.getProperty();
        if (props == null) {
            return;
        }

        for (Property prop : props) {
            String propName = prop.getName();
            String propValue = prop.getValue();
            if (propName == null || propValue == null) {
                throw new IllegalArgumentException(
                        _rb.getString("webcontainer.nullWebProperty"));
            }

            if (propName.equalsIgnoreCase("enableCookies")) {
                instanceEnableCookies = ConfigBeansUtilities.toBoolean(propValue);
            } else {
                Object[] params = { propName };
                _logger.log(Level.INFO, "webcontainer.notYet", params);
            }
        }
    }

    private static synchronized void setJspFactory() {
        if (JspFactory.getDefaultFactory() == null) {
            JspFactory.setDefaultFactory(new JspFactoryImpl());
        }
    }

}

class V3WebappLoader extends WebappLoader {

    final ClassLoader cl;

    V3WebappLoader(ClassLoader cl) {
        super();
        this.cl = cl;
    }

    @Override
    protected ClassLoader createClassLoader() throws Exception {
        return cl;
    }

    /**
     * Stops the nested classloader
     */
    @Override
    public void stopNestedClassLoader() {
        // Do nothing. The nested (Webapp)ClassLoader is stopped in
        // WebApplication.stop()
    }

}
