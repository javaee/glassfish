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
package com.sun.enterprise.admin.server.core;

//JDK imports
import com.sun.enterprise.admin.event.ClusterEvent;
import com.sun.enterprise.admin.event.MBeanElementChangeEvent;
import com.sun.enterprise.admin.event.MBeanElementChangeEventListener;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

//JMX imports
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
//admin imports
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.server.core.jmx.SunoneInterceptor;

import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.AdminEventListener;
import com.sun.enterprise.admin.event.tx.TransactionsRecoveryEvent;
import com.sun.enterprise.admin.event.DynamicReconfigEvent;
import com.sun.enterprise.admin.monitor.GenericMonitorMBean;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.instance.InstanceEnvironment;

import com.sun.enterprise.server.Constants;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ServerContextImpl;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.ServerLifecycle;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;

//Logging related imports
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

//Utility methods
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.JvmInfoUtil;
import com.sun.enterprise.util.i18n.StringManager;

//autodeploy
import com.sun.enterprise.deployment.autodeploy.AutoDeployController;
import com.sun.enterprise.deployment.autodeploy.AutoDeployControllerFactroy;
import com.sun.enterprise.deployment.autodeploy.AutoDeployControllerFactroyImpl;
import com.sun.enterprise.deployment.autodeploy.AutoDeploymentException;

//web service
import com.sun.enterprise.webservice.WsUtil;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.server.core.jmx.AppServerMBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.server.core.channel.RRStateFactory;

//AMX: can't import, module build-order dependency
//import com.sun.enterprise.management.support.Loader;

import com.sun.enterprise.Switch;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.AgentAdapter;

import com.sun.enterprise.server.logging.AMXLoggingHook;

import com.sun.enterprise.util.FeatureAvailability;

import com.sun.appserv.management.util.misc.RunnableBase;
import com.sun.enterprise.interceptor.DynamicInterceptor;

/**
 * Admin service is a singleton in every instance and acts as gateway to server
 * administration tasks. 
 */
public class AdminService {

    /**
     * Admin service type DASD. Used to identify admin service in a server
     * instance that acts as DAS and also allows normal server operations like
     * deployment and running of user applications.
     */
    public static final String TYPE_DASD = "das-and-server";

    /**
     * Admin service type DAS. Used to identify admin service in a server
     * instance dedicated to administration. 
     */
    public static final String TYPE_DAS = "das";

    /**
     * Admin service type SERVER. Used to identify admin service in a server
     * instance dedicated to running user applications.
     */
    public static final String TYPE_SERVER = "server";

    /*
     * Flag to enable performance. If the flag is enabled, checking for manual
     * changes is done every 2 minutes instead of at every access to config
     * files.
     */
    public static final boolean ENABLE_PERFORMANCE_THREAD = true;
    
    /** The logger from LogDomains */
    /* Note that this is the ONLY class that is dependent on the LogDomains.
     * Once registered through LogDomains, others can get it using standard
     * call Logger.getLogger() */
    public static final Logger sLogger 
            = LogDomains.getLogger(AdminConstants.kLoggerName);

    private static final StringManager localStrings =
        StringManager.getManager(AdminService.class);

    private static AdminService adminService = null;

    private static final String COM_SUN_APPSERV = "com.sun.appserv";
    
    /** prefix for administrative domain name */
    private static final String ADMIN_DOMAIN_NAME_PREFIX = COM_SUN_APPSERV + ".";

    /** default administrative domain name */
    private static final String DEF_ADMIN_DOMAIN_NAME=ADMIN_DOMAIN_NAME_PREFIX + "server";

    private static final String  kTempDirNamePrefix   = "s1astemp";
    private static final String  kGUITempDirName      = "gui";
    private static final String  kTempDirNameSuffix   = 
            "" + ((System.getProperty(Constants.IAS_ROOT) != null) ?
            System.getProperty(Constants.IAS_ROOT).hashCode() : 0);
    /* The temporary directory name should be a function of domain name, 
     * admin-server id and the install root, otherwise it won't
     * work for the multiple installations on the same machine. */
    private static final String SS_MBEAN_CLASS = "com.sun.enterprise.admin.mbeans.SystemServicesMBean";
    private volatile String adminServiceType;
    private volatile ServerContext context;
    private volatile AdminContext adminContext;
    private volatile ServerLifecycle adminChannel;
    private volatile String mTempDirPath;
    private volatile String mGUITempDirPath;
    
    /** A field to denote the default name for all the config and runtime mbeans that are 
     * generated from the meta-information (descriptor, like admin-mbean-descriptor.xml) */
    public static final String PRIVATE_MBEAN_DOMAIN_NAME = "com.sun.appserv";
    public static final String DAS_DIAGNOSTIC_MBEAN_CLASS_NAME =
                "com.sun.enterprise.admin.mbeans.DomainDiagnostics";

    private final AutoDeployStarter autoDeployStarter = new AutoDeployStarter();
    

    /**
     * private constructor. AdminService instance should be initialized
     * through package method createAdminService(). 
     */
    private AdminService() {
    }
    
    /**
     * Create admin service. Admin Service is initialized by an internal
     * lifecycle module (AdminServiceLifeCycle).
     */
    static AdminService createAdminService(ServerContext sc) {
        System.setProperty("com.sun.aas.admin.logger.name",
                AdminConstants.kLoggerName);
        String type = TYPE_SERVER;
        if (ServerManager.ADMINSERVER_ID.equals(sc.getInstanceName())) {
            type = TYPE_DAS;
        }
        ConfigContext cc = sc.getConfigContext();
        try {
            com.sun.enterprise.config.serverbeans.AdminService as =
                    ServerBeansFactory.getConfigBean(cc).getAdminService();
            if (as != null) {
                type = as.getType();
            }
        } catch (ConfigException ce) {
            ce.printStackTrace();
            sLogger.log(Level.WARNING, "core.admin_service_default_config",
                    type);
        }
        AdminService as = instantiateAdminService(type);
        as.setContext(sc);
        AdminContext ac = sc.getPluggableFeatureFactory().getAdminContext();
        if (ac instanceof AdminContextImpl) {
            ((AdminContextImpl)ac).setServerContext((ServerContextImpl)sc);
        }
        as.setAdminContext(ac);
        setAdminService(as);
        return as;
    }

    /**
     * Instantiate admin service. This method verifies that specified type
     * is a known type and then creates an admin service object initialized
     * for the specified type.
     */
    private static AdminService instantiateAdminService(String type) {
        AdminService as = null;
        if (TYPE_DASD.equals(type) || TYPE_DAS.equals(type)
                || TYPE_SERVER.equals(type)) {
             as = new AdminService();
             as.setType(type);
        } else {
            throw new RuntimeException(localStrings.getString(
                    "admin.server.core.unknown_admin_service_type", type));
        }
        return as;
    }

    /**
     * Set AdminService. This method should be called by createInstance()
     * method after instantiating the service. This enables other objects to use
     * static method getAdminService to get access to admin service.
     */
    private static void setAdminService(AdminService srv) {
        adminService = srv;
    }

    /**
     * Get admin service object. Admin service is started when server starts
     * up.
     */
    public static AdminService getAdminService() {
        return adminService;
    }

    /**
     * Initialize admin service. This is called just after creating the
     * instance and before any public methods are called on admin service.
     * Initializes the admin server's MBeanServer.
     * Currently initializes the MBeanServer with default implementation in
     * com.sun.enterprise.admin.server.core.jmx.MBeanServerImpl.
     * Should there be multiple implementations, this method needs to be
     * modified.
     * @throws LifeCycleException in case the initialzation fails.
     */
    void init() throws ServerLifecycleException {
        if (isDas()) {
            // remove restart required state file when starting up DAS
            RRStateFactory.removeStateFile();
        }
        adminChannel = new AdminChannelLifecycle();
        adminChannel.onInitialization(context);
        
        // Publish PID for this VM in the config directory
        // This publishing has to happen after the AdminService init
        publishPID();        

        final DynamicInterceptor dyn = (DynamicInterceptor)getMBeanServer();
        final MBeanServer delegateMBeanServer = dyn.getDelegateMBeanServer();
        adminContext.setMBeanServer( dyn );
        
        final SunoneInterceptor sunone =
            SunoneInterceptor.createInstance(adminContext, dyn, delegateMBeanServer);
        dyn.addHook( COM_SUN_APPSERV, sunone );
        FeatureAvailability.getInstance().registerFeature(
            FeatureAvailability.SUN_ONE_INTERCEPTOR_FEATURE, "true");
        AMXLoggingHook.enableLoggingHook();
        sunone.registerConfigMBeans();
            
        sLogger.log(Level.INFO, "core.sunone_interceptor_enabled");
        initCallFlow();

        //initialize JKS properties.
        setupJKS();
    }

    private void setupJKS() {
        String configDir = System.getProperty("com.sun.aas.instanceRoot") + 
            File.separator + "config";
        java.io.File nssFile = new File(configDir, "key3.db");
        if (!nssFile.exists()) {
            if (System.getProperty("javax.net.ssl.keyStore") == null) {
                System.setProperty("javax.net.ssl.keyStore", configDir + 
                    File.separator + "keystore.jks");
                System.setProperty("javax.net.ssl.trustStore", configDir +
                    File.separator + "cacerts.jks");
            }
        }
    } 

    private void initCallFlow() {
        try {
            final Class cl = Class.forName(
                    "com.sun.enterprise.admin.monitor.callflow.AgentImpl");
            final Method method = cl.getMethod("getInstance", null);
            final Agent agent = (Agent) method.invoke(null, null);
            Switch.getSwitch().setCallFlowAgent(agent);
        } catch (Throwable t) {
            sLogger.log(Level.SEVERE, "core.callflow_agent_init_failed", t);
            Switch.getSwitch().setCallFlowAgent(new AgentAdapter());
        }
    }

    /**
     * Start admin service. This is called prior to any public method call
     * on admin service. This should be used to prepare admin service to
     * receive public method calls.
     */
    void start() throws ServerLifecycleException {
        com.sun.enterprise.ManagementObjectManager mgmtObjManager = com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();
        if (isDas()) {
            startAdminInstance();            
            mgmtObjManager.registerJ2EEDomain();
        }
        
        initializeAMXMBeans( isDas() );
            
        // Register JVM and J2EEServer managed objects
        mgmtObjManager.registerJVM();
        mgmtObjManager.registerJ2EEServer();   
            
        if (isDas()) {
	    // das j2ee server should have been registered prior to this
	    // so
	    // mgmtObjManager.registerJ2EEServer() should run before this
            mgmtObjManager.registerDasJ2EEServers();
            mgmtObjManager.registerAllJ2EEClusters();
	}

        if (canRunUserApps()) {
            startNormalInstance();            
        }


        initializePerInstanceSystemService();
        //This code initializes dotted names by enumerating
        // all objectnames using domain.xml
        initializeDottedNames();
        registerTransactionsRecoveryEventMBean();
        mgmtObjManager.registerTransactionService();
        
        // initialize the Web Service Container services
        WsUtil.start();        
        // register DASDiagnosticMBean
        createDASDiagnosticMBean();

    }

    /**
     * Admin service is ready. All other services have started up successfully.
     */
    void ready() throws ServerLifecycleException {
        if (adminChannel != null) {
            adminChannel.onReady(context);
        }
        initiateCustomMBeanLoading();
        registerJVMMonitoringMBeans();
        
        // would be cleaner to issue JMX Notifications for a variety
        // of events, but that would mean adding an MBean for AdminService.
        notifyAMXThatAdminServiceIsReady();
        
        // notify all node agents in this domain to know THIS DAS. This is 
        // useful when DAS has moved. Also, only the running Node Agents will
        // be notified.
        notifyNodeagents();
        
        if (isDas()) {
           // autoDeployStarter *must* run in a separate thread, or the
           // this call won't return because it will wait for the server
           // to start using the current thread, which must return in order
           // for the server to be considered to have started.
           autoDeployStarter.submit(RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD);
        }
        
        NoopClusterEventListener noop = new NoopClusterEventListener();
        if (!isDas()) {
            AdminEventListenerRegistry.addEventListener(ClusterEvent.eventType, noop);
        }
    }

    /**
     * Stop admin service. This is called when shutdown process starts.
     */
    void stop() throws ServerLifecycleException {
        
        if (adminChannel != null) {
            adminChannel.onShutdown();
        }
        com.sun.enterprise.ManagementObjectManager mgmtObjManager = com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();
        
        //FIXE ME - Prakash
        // Unregister J2EEDomain, j2eeserver and jvm managed objects.
        
        if (isDas()) {
            //stop checking for manual changes
            if(ENABLE_PERFORMANCE_THREAD) ManualChangeTracker.stop();
            stopAdminInstance();
        }
        if (canRunUserApps()) {
            //autodeploy service stoped
            autoDeployStarter.waitDone();
            autoDeployStarter.stopAutoDeployService();            
            stopNormalInstance();
        }
        // as we are killing the VM, there is no real need of shutting down MBS.
    }

    /**
     * Destroy admin service. This is called just before JVM is destroyed.
     */
    void destroy() throws ServerLifecycleException {
    }

    /**
     * Get server context.
     */
    public ServerContext getContext() {
        return ((adminService == null) ? null : adminService.context);
    }

    /**
     * Set context to specified value. Typically, lifecycle manager will call
     * this method during initialization to set appropriate context.
     */
    void setContext(ServerContext ctx) {
        context = ctx;
    }

    /**
     * Get admin context.
     */
    public AdminContext getAdminContext() {
        return adminContext;
    }

    /**
     * Returns the administrative domain name for this server. If 
     * administrative domain name property is not defined, 
     * DEF_ADMIN_DOMAIN_NAME is returned.
     *
     * @return  administrative domain name 
     */
    public String getAdministrativeDomainName() throws ConfigException {

        String serverName = adminContext.getServerName();
        ConfigContext ctx = adminContext.getAdminConfigContext();
        String aDomainName = 
            ServerHelper.getAdministrativeDomainName(ctx, serverName);

        // return default administrative domain name if administrative domain
        // name property is not defined
        if ( (aDomainName == null) || ("".equals(aDomainName)) ) {
            return DEF_ADMIN_DOMAIN_NAME;
        } else {
            return ADMIN_DOMAIN_NAME_PREFIX + aDomainName;
        }
    }

    /**
     * Set admin context.
     */
    protected void setAdminContext(AdminContext ctx) {
        adminContext = ctx;
        adminContext.setMBeanServer( getMBeanServer() );
    }

    /**
     * Get name of server instance.
     */
    public String getInstanceName() {
        return ((context == null) ? null : context.getInstanceName());
    }

    /**
     * Get root monitoring MBean. Monitoring MBeans within a server instance
     * are organized in a tree. This object represents the root of the tree.
     */
    public GenericMonitorMBean getRootMonitorMBean() {
        return GenericMonitorMBean.getRoot();
    }

    /**
     * Helper method to start admin server instance
     */
    private void startAdminInstance() throws ServerLifecycleException {
        setAdminInstanceProperties();
        // Test code here
        // new com.sun.enterprise.admin.event.EventTester();
        // Uncomment following line to test monitoring
        // new com.sun.enterprise.admin.monitor.MonitoringTester();
        createTempDir();
    }

    /**
     * Helper method to stop admin server instance.
     */
    private void stopAdminInstance() throws ServerLifecycleException {
        deleteTempDir();
    }

    /**
     * Helper method to start normal (non-admin) instances
     */
    private void startNormalInstance() throws ServerLifecycleException {
        initLogManagerReconfigSupport();
        registerTransactionsRecoveryEventListener();
        // Test code here
        // new com.sun.enterprise.admin.event.EventTester("str");
    }

    /**
     * Helper method to stop normal (non-admin) instances
     */
    private void stopNormalInstance() throws ServerLifecycleException {
    }

    /**
     * Is this admin service running within admin server instance. Admin service
     * runs in all server instances.
     * @return true if the service is running in an admin server instance,
     *    false otherwise.
     * @deprecated Use the method isDas() instead.
     */
    public boolean isAdminInstance() {
        if (ServerManager.ADMINSERVER_ID.equals(context.getInstanceName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is admin service configured for administration of domain. A domain has
     * several server instances and typically one of them is capable of
     * administering the other server instances in the domain.
     * @return true if the admin service is running in an instance that can
     *     administer other instances in the domain, false otherwise.
     */
    public boolean isDas() {
        if (TYPE_DASD.equals(adminServiceType)
                || TYPE_DAS.equals(adminServiceType)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is admin service running in a server instance that is primarily intended
     * for running user applications. Admin service runs in all server
     * instances, but sometimes there is one server instance dedicated to
     * administration of other instances in the domain and user applications
     * can not not be run on that instance.
     * @return true if user applications can be run on the server instance
     *     where this admin service is running, false otherwise.
     */
    public boolean canRunUserApps() {
        if (TYPE_DAS.equals(adminServiceType)) {
            return false;
        } else {
            return true;
        }
    }
 
    /**
     * Get admin service type. Admin service type is defined in admin service
     * configuration.
     * @return one of TYPE_DAS, TYPE_DASD, TYPE_SERVER
     */
    public String getType() {
        return adminServiceType;
    }

    /**
     * Set type of admin service.
     */
    private void setType(String type) {
        adminServiceType = type;
    }
    
    /**
     Creates the time stamp files (if do not exist) for the instances that the
     admin server manages.    
    */
    private void createTimeStampFilesForInstances() {
        String[] instanceIds = ServerManager.instance().getInstanceNames(true);
        for (int i = 0 ; i < instanceIds.length ; i ++) {
            try {
                String instanceId = instanceIds[i];
                InstanceEnvironment ie = new InstanceEnvironment(instanceId);
                ie.createTimeStampFiles();
		        sLogger.log(Level.FINE, "core.ts_files_ok", instanceId);
            }
            catch (Exception e) {
                //Log the exception for this instance, squelching is OK?
                sLogger.log(Level.WARNING, "core.ts_files_failed", e);
            }
        }
        
    }

    /**
        Gets the name of the temporary folder where the admin-server would be
        creating some temporary data. Note that this method is not a pure 
        accessor in the sense that it will create the folder on disk if
        it does not exist.
        @return String representing the absolute path of temporary folder, which
        may be null if the file creation fails.
    */
    public String getTempDirPath() {
        if (mTempDirPath == null) {
        /* Give it one more try, if on the startup the
           user does not have enough space and/or the temp
           file was not created. Note that the first-time failure will
           be detected in the log at startup.
           Also note that this additional safety does not come without
           the overhead of the null check for every call to this function.
       */
            createTempDir();
        }
        
        return ( mTempDirPath );
    }

    /**
        Gets the name of the temporary folder where the admin-GUI would be
        creating some temporary data. Note that this method is not a pure 
        accessor in the sense that it will create the folder on disk if
        it does not exist.
        @return String representing the absolute path of temporary folder
        for GUI, which may be null if the file creation fails.
    */
    public String getGUITempDirPath() {
        if (mGUITempDirPath == null) {
        /* Give it one more try, if on the startup the
           user does not have enough space and/or the temp
           file was not created. Note that the first-time failure will
           be detected in the log at startup.
           Also note that this additional safety does not come without
           the overhead of the null check for every call to this function.
       */
            createTempDir();
        }
        else {
            final File gd = new File(mGUITempDirPath);
            if (!gd.exists()) {
                final boolean s = gd.mkdirs();
                if (!s) {
                   sLogger.log(Level.WARNING, "core.gui_tmp_folder_creation_failed", mGUITempDirPath);
                }
            }
        }
        
        return ( mGUITempDirPath );
    }

    private void createTempDir() {
        try {
            String domainName       = ServerManager.instance().getDomainName();
	        String localTmpDir      = System.getProperty("java.io.tmpdir");
            String asTempDirName    = kTempDirNamePrefix + domainName + 
                    context.getInstanceName() + kTempDirNameSuffix;
            /* for admin server, e.g. </tmp>/s1astempdomain1admin-server */
            File tempFolder     = new File(localTmpDir, asTempDirName);
            mTempDirPath        = tempFolder.getCanonicalPath();

            /* for GUI, e.g. </tmp>/s1astempdomain1admin-server/gui*/
            File guiTempFolder  = new File(mTempDirPath, kGUITempDirName);
            mGUITempDirPath     = guiTempFolder.getCanonicalPath();
            if (tempFolder.exists()) {
                /*
                    If it exists, the delete method during earlier admin-server
                    lifecycle was not able to delete the directory and
                    that should be OK. The intent here is to create a 
                    directory if there does not exist one.
                */
                sLogger.log(Level.FINEST, "core.tmp_folder_exists", 
                    mTempDirPath);
                return;
            }
            /* try to create admin-server's temporary directory */
            boolean couldCreate = tempFolder.mkdirs();
            if (! couldCreate) {
                sLogger.log(Level.WARNING, "core.tmp_folder_creation_failed", 
                        mTempDirPath);
            }
            else {
                sLogger.log(Level.FINEST, "core.tmp_folder_created_ok", 
                    mTempDirPath);
            }
            /* try to create temporary directory for GUI*/
            couldCreate = guiTempFolder.mkdirs();
            if (! couldCreate) {
                sLogger.log(Level.WARNING, "core.gui_tmp_folder_creation_failed", 
                        mGUITempDirPath);
            }
            else {
                sLogger.log(Level.FINEST, "core.gui_tmp_folder_created_ok", 
                    mGUITempDirPath);
            }
        }
        catch(Throwable t) {
            sLogger.log(Level.WARNING, "core.tmp_folder_creation_failed", t);
        }
    }

    private void deleteTempDir() {
        try {
            FileUtils.whack(new File(mTempDirPath));
            sLogger.log(Level.FINEST, "core.tmp_folder_deleted_ok", 
                mTempDirPath);
        }
        catch(Throwable t) {
            sLogger.log(Level.WARNING, "core.tmp_folder_deletion_failed", 
                    mTempDirPath);
        }
    }

    /**
     * Set system properties for admin server instance. Some of the admin code
     * relies on the property <code>com.sun.aas.javaRoot</code>. This method
     * will initialize the property using the value set by JVM plugin. If the
     * property is already set, it will not be changed.
     */
    private void setAdminInstanceProperties() {
        try {
            String adminJavaRoot = System.getProperty(ADMIN_JAVAROOT);
            String pluginJavaRoot = System.getProperty(PLUGIN_JAVAROOT);
            if (adminJavaRoot == null) {
                if (pluginJavaRoot != null) {
                    System.setProperty(ADMIN_JAVAROOT, pluginJavaRoot);
                } else {
                    sLogger.log(Level.WARNING, "core.no_java_home");
                }
            }
        } catch (Throwable t) {
            sLogger.log(Level.WARNING, "core.set_admin_property_failed");
            sLogger.log(Level.FINE, "general.unexpected_exception", t);
        }
    }

    private static final String ADMIN_JAVAROOT = "com.sun.aas.javaRoot";
    private static final String PLUGIN_JAVAROOT = "JAVA_HOME";

    private final class AutoDeployStarter extends RunnableBase {
        // note that methods using this variable are 'synchronized', both
        // for visibilility and mutual exclusion purposes.
        private AutoDeployController mAutoDeployController;
         
        AutoDeployStarter() {
        }
        
        protected synchronized void doRun() throws AutoDeploymentException {
            // block until the server has started
            FeatureAvailability.getInstance().waitForFeature(
                FeatureAvailability.SERVER_STARTED_FEATURE,"AutoDeployStarter");
            
            try {
                final AutoDeployControllerFactroy /*[sic]*/ factory =
                    new AutoDeployControllerFactroyImpl();
                mAutoDeployController = factory.createAutoDeployController(context);
                if( mAutoDeployController !=null ) {
                    mAutoDeployController.enableAutoDeploy();
                }
            }
            catch( Throwable t ) {
                sLogger.log(Level.WARNING, "general.unexpected_exception", t);
            }
        }
        
        private synchronized void stopAutoDeployService(){
            if(mAutoDeployController !=null) {
                mAutoDeployController.disableAutoDeploy();
                mAutoDeployController = null;
            }
        }
    }    
            
    /**
     * MBeanRegistry is used to generate all dotted names
     * available in the system. This is achived by iterating
     * through domain.xml and generating object name and
     * dotted name for every element (some special cases)
     *
     * Dotted name is generated from the descriptor entry
     * 
     * Once these are generated, an mbean is notified about
     * these dotted names. In short, this mbean will keep track 
     * of the dotted names and is used by cli for get/set
     * commands
     */
    private void initializeDottedNames() {
        try {
            MBeanRegistry mr = MBeanRegistryFactory.getAdminMBeanRegistry();   
            mr.generateAndRegisterAllDottedNames(context.getConfigContext(), 
                           context.getDefaultDomainName());
        } catch (Throwable t) {
             sLogger.log(Level.WARNING, "admin.dotted_names_init_exception", t);
        }
    }
	
    private void registerTransactionsRecoveryEventListener() {
        try
        {
             AdminEventListenerRegistry.addEventListener(
                TransactionsRecoveryEvent.eventType,
                (AdminEventListener)(new com.sun.enterprise.transaction.TransactionsRecoveryEventListenerImpl()));
        }
        catch (Throwable t)
        {
             sLogger.log(Level.WARNING, "admin.transactions_recovery_listener_registration_exception", t);
        }

    }
    private void registerTransactionsRecoveryEventMBean() {
        try {
            MBeanRegistryFactory.getAdminMBeanRegistry().instantiateMBean(
                    "transactions-recovery", new String[]{adminContext.getDomainName()}, null, null, true );
        } catch (Throwable t) {
            sLogger.log(Level.FINE, "core.transactions_recovery_mbean_register_error", t);
        }
    }

    
    private void initLogManagerReconfigSupport() {
        registerLogManagerMBean();
        registerLogManagerEventListener();
        registerDynamicReconfigEventListener();
    }

    private void registerLogManagerMBean() {
        final MBeanServer mbs = FeatureAvailability.getInstance().getMBeanServer();
        try {
            Object mbean =
                    com.sun.enterprise.server.logging.LogMBean.getInstance();
            mbs.registerMBean(mbean, getLogManagerMBeanName());
        } catch (Throwable t) {
            sLogger.log(Level.WARNING, "core.logmgr_mbean_not_registered");
            sLogger.log(Level.FINE, "core.logmgr_mbean_register_error", t);
        }
    }

    private ObjectName getLogManagerMBeanName()
            throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put("server", adminContext.getServerName());
        props.put("category", "runtime");
        props.put("name", "logmanager");
        ObjectName on = new ObjectName(adminContext.getDomainName(), props);
        return on;
    }

    private void registerLogManagerEventListener() {
         AdminEventListenerRegistry.addLogLevelChangeEventListener(
                new com.sun.enterprise.server.logging.LogLevelChangeEventListenerImpl());
    }

    private void registerDynamicReconfigEventListener() {
         AdminEventListenerRegistry.addEventListener(
                DynamicReconfigEvent.eventType, new 
                com.sun.enterprise.admin.server.core.channel.DynamicReconfigEventListenerImpl());
    }
    /** Registers the file transfer service (aka SystemServicesMBean) that will
     * be registered in all the instances. Note that this MBean will have slightly
     * different ObjectName depending upon where it is registered.
     * @see ObjectNames.getPerInstanceSystemServicesObjectName
     * <p>
     * The principal use of this MBean is in <ul>
     * <li> download of stubs in case of DAS </li>
     * <li> synchronization in case of non DAS instances </li>.
     */
    private void initializePerInstanceSystemService() throws ServerLifecycleException {
        ObjectName on   = null;
        try {
            final MBeanServer mbs = getMBeanServer();
            on = ObjectNames.getPerInstanceSystemServicesObjectName(this.getInstanceName());
            final Object impl = Class.forName(SS_MBEAN_CLASS).newInstance();
            mbs.registerMBean(impl, on);
            sLogger.finer("Admin Message: System Services MBean Registered with on: " + on.toString());
        }
        catch (final Exception e) {
            sLogger.log(Level.WARNING, "core.system.service.mbean.not.registered", on.toString());
        }
    }
    
    

    private static final String AMX_DAS_LOADER_CLASSNAME    =
        "com.sun.enterprise.management.support.Loader";
        
    private static final String AMX_NON_DAS_LOADER_CLASSNAME    =
        "com.sun.enterprise.management.support.NonDASLoader";
        
    public static final String AMX_LOADER_DEFAULT_OBJECTNAME    =
        "amx-support:name=mbean-loader";
    
    private volatile ObjectName mAMXLoaderObjectName   = null;
        
    /**
        Initializes AMX MBeans
        Uses Class.forName() due to build-order issues.
     */
    private void initializeAMXMBeans( boolean isDAS ) {
        try {
            // can't 'import' it because it's in the admin module, which
            // compiles after the module this file is in.
            final String loaderClassname    =
                isDAS ? AMX_DAS_LOADER_CLASSNAME : AMX_NON_DAS_LOADER_CLASSNAME;
                
            final Class    loaderClass = Class.forName( loaderClassname );
            final Object   loader      = loaderClass.newInstance();
            ObjectName     tempObjectName  = new ObjectName( AMX_LOADER_DEFAULT_OBJECTNAME );
            
            mAMXLoaderObjectName  =
                getMBeanServer().registerMBean( loader, tempObjectName ).getObjectName();
            
            sLogger.log(Level.INFO, "mbean.init_amx_success");
        }
        catch(Exception e) {
            // this is really a fatal error; not even the AMX loader works
            sLogger.log(Level.SEVERE, "mbean.init_amx_failed", e);
            throw new RuntimeException( e );
        }
    }
    
        private static MBeanServer
    getMBeanServer() {
        return FeatureAvailability.getInstance().getMBeanServer();
    }
    

    /**
        Make a [synchronous] call to AMX. Semantics are that this should
        be a quick call, not a long-running one.
     */
    private void notifyAMXThatAdminServiceIsReady() {
        try {
            getMBeanServer().invoke( mAMXLoaderObjectName, "adminServiceReady", null, null );
        }
        catch( Exception e ) {
            throw new RuntimeException( e );
        }
    }
    
            
    private void initiateCustomMBeanLoading() throws ServerLifecycleException {
        try {
            final MBeanServer mbs = getMBeanServer();
            final ConfigContext cc = context.getConfigContext();
            new CustomMBeanRegistrationHelper(mbs, cc).registerMBeans();
        } catch (final Exception e) {
            sLogger.log(Level.WARNING, "core.custom.mbean.registration.failure");
        }
        initializeMBeanEventListeners();
    }

    // createDASDiagnosticMBean
    private void createDASDiagnosticMBean() {
        try {
                final MBeanServer mbs = getMBeanServer();
                ObjectName on = new ObjectName(
                                PRIVATE_MBEAN_DOMAIN_NAME + ":" +
                "type=DomainDiagnostics,name=" + getInstanceName() + ",category=monitor");
                        Class cl = Class.forName(DAS_DIAGNOSTIC_MBEAN_CLASS_NAME);
                mbs.registerMBean(cl.newInstance(), on);
        } catch (Throwable t) {
            t.printStackTrace();
            sLogger.log(Level.INFO, "core.das_diag__mbean_not_registered", t.getMessage());
        }
    }
    private void initializeMBeanEventListeners() {
        try {
            Class c = null; //Class for listener of MBeanElementChange events
            if (this.isDas()) {
                c  = Class.forName("com.sun.enterprise.admin.mbeans.custom.InProcessMBeanElementChangeEventListenerImpl");
            }
            else {
                c = Class.forName("com.sun.enterprise.ee.admin.mbeans.RemoteMBeanElementChangeEventListenerImpl");
            }
            final Object o = c.newInstance();
            AdminEventListenerRegistry.addEventListener(MBeanElementChangeEvent.EVENT_TYPE, (MBeanElementChangeEventListener)o);            
        } catch (final Exception e) {
            e.printStackTrace(); //ok to squelch
        }
    }
    
    private void registerJVMMonitoringMBeans()  throws ServerLifecycleException {
        try {
            //  Use a dummy ObjectName; the MBeans modify the supplied one
            final ObjectName dummy = new ObjectName("dummy:name=dummy");
            
            if (this.isDas()) {
                final Object jvmInfoCollector =
                    Class.forName("com.sun.enterprise.admin.mbeans.jvm.JVMInformationCollector").newInstance();
                
                // com.sun.appserv:type=JVMInformationCollector,category=monitor,server=server
                final ObjectName actualObjectName =
                    getMBeanServer().registerMBean( jvmInfoCollector, dummy).getObjectName();
            }
            
            // com.sun.appserv:type=JVMInformation,category=monitor,server=server
            final Object jvmInfo = Class.forName("com.sun.enterprise.admin.mbeans.jvm.JVMInformation").newInstance();
            final ObjectName actualObjectName = getMBeanServer().registerMBean( jvmInfo, dummy ).getObjectName();
        }
        catch ( final Exception e) {
            sLogger.log(Level.WARNING, "core.jvm.mbeans.not.registered");
        }
    }

    private void notifyNodeagents() {
        try { 
            if (isDAS()) {
                Class naNotifierClass = Class.forName(
                    "com.sun.enterprise.ee.nodeagent.NodeAgentNotifier");
                Class[] params = new Class[1];
                params[0] = ServerContext.class;
                Constructor c = naNotifierClass.getConstructor(params);
                Object naNotifier = c.newInstance(context);
                Thread notifyNodeAgents = new Thread((Runnable)naNotifier);
                notifyNodeAgents.setDaemon(true);
                notifyNodeAgents.start();
            }
        } catch (ConfigException ex) {
            ex.printStackTrace();
            sLogger.log(Level.WARNING, "core.could_not_determine_whether_instance_is_das");
        } catch (Exception ex) {
            ex.printStackTrace();
            sLogger.log(Level.WARNING, 
                "core.notification_for_nodeagents_could_not_be_invoked");
        }
    }
    
    private boolean isDAS() throws ConfigException {
      return ServerHelper.isDAS( 
       AdminService.getAdminService().getAdminContext().getAdminConfigContext(), 
       context.getInstanceName());
    }

    private void publishPID() {
        JvmInfoUtil jvminfo = new JvmInfoUtil();
	System.out.println(System.getProperty("com.sun.aas.instanceRoot") +
                       "/config/" + SystemPropertyConstants.PID_FILE);
        jvminfo.logPID(System.getProperty("com.sun.aas.instanceRoot") + 
                       File.separator + "config" + File.separator + 
		       SystemPropertyConstants.PID_FILE);
    }    
}
