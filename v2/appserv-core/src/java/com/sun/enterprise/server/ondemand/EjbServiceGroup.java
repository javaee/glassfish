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

package com.sun.enterprise.server.ondemand;


import java.io.*;
import java.util.Properties;
import java.util.Vector;
import java.net.*;
import java.rmi.RemoteException;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import javax.management.ObjectName;
import org.omg.CORBA.ORB;
import com.sun.corba.ee.impl.orbutil.ORBConstants;

import com.sun.enterprise.*;
import com.sun.enterprise.util.*;
import com.sun.enterprise.log.*;
import com.sun.enterprise.naming.*;
import com.sun.enterprise.distributedtx.*;
import com.sun.enterprise.iiop.POAProtocolMgr;
import com.sun.enterprise.iiop.PEORBConfigurator;
import com.sun.enterprise.iiop.ORBMonitoring;
import com.sun.enterprise.naming.java.javaURLContext;
import java.util.logging.*;
import com.sun.logging.*;

import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.ServerLifecycleException;

import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ServerContextImpl;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.transaction.monitor.JTSMonitorMBean;


import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;

import com.sun.enterprise.deployment.backend.OptionalPkgDependency;
import com.sun.enterprise.deployment.*;

import com.sun.enterprise.server.ondemand.entry.EntryContext;
import com.sun.enterprise.server.ondemand.entry.EntryPoint;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.corba.ee.impl.folb.InitialGroupInfoService;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;



/**
 * Represents the group services needed by EJBs. The main components
 * of this servicegroup are ORB, JTS and system apps that use EJBs.
 *
 * @author Binod PG
 * @see ServiceGroup
 * @see ServiceGroupBuilder
 */
public class EjbServiceGroup extends ServiceGroup {

    public static final String J2EE_APPNAME =
        "com.sun.enterprise.appname";
    public static final String JTS_SERVER_ID =
        "com.sun.jts.persistentServerId";
    public static final String J2EE_SERVER_ID_PROP =
	"com.sun.enterprise.J2EEServerId";

    // private static final boolean debug = true;
    private static final boolean debug =
	com.sun.enterprise.util.logging.Debug.enabled;

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(com.sun.enterprise.server.J2EEServer.class);

    private static final int DEFAULT_SERVER_ID = 100 ;

    private final String MEJB_JNDI_NAME="ejb/mgmt/MEJB";


    // Used for POAProtocolMgr to create poaids.db
    // Used for activation repository servers.db
    private File repositoryDir;

    //IASRI 4661409 private Properties defaultProperties;
    private int orbInitialPort;
    private ORB orb;
    private ProtocolManager protocolMgr;

    private volatile boolean systemAppsLoading;

    /**
     * Triggers the start of the servicegroup. The entry context
     * that caused this startup is used by the servicegroup to obtain
     * any startup information it require.
     * 
     * @param context EntryContext object.
     * @see EntryContext.
     */
    public void start(EntryContext context) 
    throws ServiceGroupException {
        _start(context);
        if (context.getEntryPointType() != EntryPoint.STARTUP) {
            loadSystemApps();
        }
    }

    // Internal start.
    private void _start(EntryContext context) 
    throws ServiceGroupException {
        ServerContext serverContext = context.getServerContext();
        String[] args = serverContext.getCmdLineArgs();
        if (args == null) args = new String[0];
        try {
            startORB(args);
            //Lazy initialize jts tx manager.
            J2EETransactionManagerImpl.createJTSTransactionManager();
        } catch (Exception e) {
            throw new ServiceGroupException (e);
        }
    }

    /**
     * Stop the servicegroup. It stops all the lifecycle modules belongs to this
     * servicegroup.
     */
    public void stop(EntryContext context) throws ServiceGroupException {
        super.stopLifecycleServices();
    }

    /**
     * Abort the servicegroup. This is not called from anywhere as of now.
     */
    public void abort(EntryContext context) {
        super.stopLifecycleServices();
    }

    /**
     * Loads all the system apps belongs to this servicegroup.
     * @see OnDemandServices
     * @see SystemAppLoader
     */
    private void loadSystemApps() {
        SystemAppLoader loader = OnDemandServer.getSystemAppLoader();
        systemAppsLoading = true;
        loader.loadSystemApps(loader.getEjbServiceGroupSystemApps());
        systemAppsLoading = false;
    }

    /**
     * Analyse the entrycontext and specifies whether this servicegroup
     * can be started or not.
     *
     * @return boolean If true is returned, this servicegroup can be started
     * If false is returned, the entrycontext  is not recognized by the 
     * servicegroup.
     */
    public boolean analyseEntryContext( EntryContext context ) {

        if (_logger.isLoggable(Level.FINER)) {
            _logger.log(Level.FINER, 
            "Analysing the context in EJB ServiceGroup :" + context);
        }

        boolean result = false;
        try {
            ConfigContext ctxt = context.getServerContext().getConfigContext();
            Config conf = ServerBeansFactory.getConfigBean( ctxt );

            // If ondemand loading is switched off, indicate to load this
            // service group.
            if (context.getEntryPointType() == EntryPoint.STARTUP) {
                boolean onDemandStartup = ((Boolean) context.get()).booleanValue();
                if (onDemandStartup == false) {
                    result = true;
                }
            } else if (context.get() == null) {
                return false;
            }

            // If the app to be loaded contains atleast one EJB, return 0.
            // If a user resource adapter being deployed is 1.5 RA, then
            // For inbound transaction support, we need to start JTS and
            // inturn ORB.
            if (context.getEntryPointType() == EntryPoint.APPLOADER ) {
                Descriptor desc = (Descriptor) context.get();
                if (desc instanceof Application) {
                    Application app = (Application) desc;
                    if (app.getEjbComponentCount() > 0) {
                        result = true;
                    } else {
                        for (ConnectorDescriptor cd : 
                            (java.util.Set<ConnectorDescriptor>) app.getRarDescriptors()) {
                            if (!ResourcesUtil.createInstance().belongToSystemRar(cd.getName())) {
                                String raClass = cd.getResourceAdapterClass();
                                if (raClass != null && !raClass.trim().equals("")) {
                                    result = true;
                                    break;
                                }
                            }
                        }
                    }
                } else if (desc instanceof ConnectorDescriptor ) { 
                    ConnectorDescriptor cd = (ConnectorDescriptor) desc;
                    if (!ResourcesUtil.createInstance().belongToSystemRar(cd.getName())) {
                        String raClass = cd.getResourceAdapterClass();
                        if (raClass != null && !raClass.trim().equals("")) {
                            result = true;
                        }
                    }
                } else { 
                    result = desc instanceof EjbBundleDescriptor ||
                             desc instanceof EjbAbstractDescriptor;
                } 
            }

            // If someone is looking up a system app that belongs to 
            // EJB servicegroup or
            // If the resource being looked up is XA capable
            // then return true.
            if ( context.getEntryPointType() == EntryPoint.JNDI ) {
                if (systemAppsLoading) return false;
                String jndiName = (String) context.get();
                String JAVA = "java";
                String COMP = "comp";
                String ENV = "env";
                int minSize = (JAVA + ":" + COMP + "/" + ENV).length();

                // There are some corner cases, where app lookup transaction
                // manager directly and use it.
                String txMgr = javaURLContext.APPSERVER_TRANSACTION_MGR;

                if (jndiName.equals(txMgr)) {
                     return true;
                }

                if (_logger.isLoggable(Level.FINER)) {
                    _logger.log(Level.FINER, "Jndi name being analysed is :" + jndiName);
                }
     
                if (jndiName.length() > minSize && jndiName.startsWith(JAVA)) {
                    jndiName = jndiName.substring(JAVA.length() + 1);
                    if (jndiName.startsWith(COMP)) {
                        jndiName = jndiName.substring(COMP.length() + 1);
                        if (jndiName.startsWith(ENV)) {
                           jndiName = jndiName.substring(ENV.length() + 1);
                        }
                    }
                }

                if (jndiName.equalsIgnoreCase(MEJB_JNDI_NAME)) {
                    result = true;
                } else {
                    result = isXAResource(jndiName, ctxt);
                }
            }

            // If Mbean is accessing any of the system app belongs to this
            // servicegroup return true;
            if (context.getEntryPointType() == EntryPoint.MBEAN) {
                result = analyseObjectName((ObjectName) context.get());
            }

            // If any of the IIOP port is accessed, return true.
            if ( context.getEntryPointType() == EntryPoint.PORT ) {
                // Start IIOP listener ports
                IiopService iiopService = conf.getIiopService();
                IiopListener[] iiopListeners = iiopService.getIiopListener();
                for ( int i=0; i<iiopListeners.length; i++ ) {
                    int port = Integer.parseInt(iiopListeners[i].getPort());
                    if (port == ((Integer) context.get()).intValue() ) {
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    // Check if the resource is XA capable.
    private boolean isXAResource(String jndiName, ConfigContext conf) {

        try {
            ConfigBean res = ResourceHelper.findResource(conf, jndiName);
            if (res != null) {
                if(_logger.isLoggable(Level.FINE))
		    _logger.log(Level.FINE,"Got the resource :" + res);
                if (res instanceof JdbcResource) {
                    String poolName = ((JdbcResource) res).getPoolName();
                    JdbcConnectionPool pool = 
                    (JdbcConnectionPool) ResourceHelper.findResource(conf, poolName);
                    if(_logger.isLoggable(Level.FINE))
		        _logger.log(Level.FINE,"Got the Pool :" + pool);
                    if (pool.getResType() != null && 
                        pool.getResType().equals("javax.sql.XADataSource")) {
                        return true;
                    } else {
                        return false;
                    }
                }

                if (res instanceof ConnectorResource) {
                    String poolName = ((ConnectorResource) res).getPoolName();
                    Resources root = ((Domain)conf.getRootConfigBean()).getResources();
                    ConnectorConnectionPool pool = (ConnectorConnectionPool) 
                    root. getConnectorConnectionPoolByName(poolName);
                    if(_logger.isLoggable(Level.FINE))
		        _logger.log(Level.FINE,"Got the Pool :" + pool);
                    String txSupport = pool.getTransactionSupport();
                    // We pessimistically decide that RA supports XA, 
                    // when pool is not configured with transaction-support.
                    if (txSupport == null ||
                        txSupport.trim().equals("") ||
                        txSupport.trim().equals
                        (ConnectorRuntime.XA_TRANSACTION_TX_SUPPORT_STRING) ) {        
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
	    _logger.log(Level.FINE, e.getMessage(), e);
            return false;
        }

        return false;

    }

    // Does the objectname belongs to any of the system apps
    // in this servicegroup.
    private boolean analyseObjectName(ObjectName name) {

        /*
        if (name == null) {
            return true;
        }
       
        String cat = name.getKeyProperty("category");
        if (cat != null && cat.equals("monitor")) {
            return true;
        }
        */

        String nameStr = name.getKeyProperty("name");
        String ref = name.getKeyProperty("ref");
        String app = name.getKeyProperty("J2EEApplication");

        return belongsToThisServiceGroup(nameStr) || 
               belongsToThisServiceGroup(ref) || 
               belongsToThisServiceGroup(app); 

    }

    private boolean belongsToThisServiceGroup(String name) {
        SystemAppLoader appLoader = OnDemandServer.getSystemAppLoader();
        if (appLoader != null) {
            for (Object n : appLoader.getEjbServiceGroupSystemApps()) {
                if (((String) n).equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
 
    private boolean isEE() {
      return SystemPropertyConstants.CLUSTER_AWARE_FEATURE_FACTORY_CLASS.equals(
        System.getProperty(PluggableFeatureFactory.PLUGGABLE_FEATURES_PROPERTY_NAME));
    }    
    // Start ORB.
    private void startORB(String[] args)
    {
        try {
	    Properties jtsProperties ;

	    // create the ORB
	    try {
		// Initialize System properties
		Properties props = System.getProperties();

		// set server-id and server-port so that persistent EJBs
		// in J2EEServer will be reachable after crashes.
		props.setProperty(J2EE_APPNAME, "j2ee");

		// Set lateRegistration to true, so JTS doesnt get initialized.
		// JTS gets initialized later in protocolMgr.initTransactionService
		// Need recoverable for JMS.
		jtsProperties = initJTSProperties(true);

		orb = createORB();

        	// Done to indicate this is a server and
		// needs to create listen ports.
        	try {
            	    org.omg.CORBA.Object obj =
                	orb.resolve_initial_references("RootPOA");
        	} catch(org.omg.CORBA.ORBPackage.InvalidName in) {
		    _logger.log(Level.SEVERE,"enterprise.orb_reference_exception",in);
        	}
	    } catch ( Exception ex ) {
                if(_logger.isLoggable(Level.SEVERE))
		    _logger.log(Level.SEVERE,"enterprise.createorb_exception",ex.toString());
		if ( debug )
		    _logger.log(Level.FINEST,"Exception while creating ORB: ",ex);
                throw new RuntimeException("Unable to create ORB");
	    }

	    // Create the orbMonitoring to register the orb related statistic 
	    // check only on the Server Side
	    if (Switch.getSwitch().getContainerType() == Switch.EJBWEB_CONTAINER) {
		new ORBMonitoring( orb );  
	    }

            Switch theSwitch = Switch.getSwitch();

	    // create Protocol Mgr
	    protocolMgr = new com.sun.enterprise.iiop.POAProtocolMgr(orb);
	    theSwitch.setProtocolManager(protocolMgr);

	    protocolMgr.initializeNaming(repositoryDir, orbInitialPort);
	    ((POAProtocolMgr)protocolMgr).initializePOAs();

	    if(_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,localStrings.getLocalString("j2ee.naming",
		    "Naming service started: ") + orbInitialPort);
	    // END OF IASRI 4660742

            try {
                Switch.getSwitch().getProviderManager().initRemoteProvider();
            } catch (RemoteException re) {
                _logger.log(Level.WARNING, "init.remote.provider", re);
            }

	    if (isEE()) {
	        //the following class registers a remote object
	        // with Cos Naming that can be used from within the CORBA code
	        //to get hold of ServerGroupManager 
	        // the remote object then can give the client info about all the cluster
	        //instances.
	        _logger.fine("initializing iGIS");
		new InitialGroupInfoService(orb);	
		_logger.fine("binding name INITIAL_GIS...");		
	    }
	    // Initialize Transaction Service
	    PEORBConfigurator.initTransactionService(
		"com.sun.jts.CosTransactions.DefaultTransactionService", jtsProperties );

        } catch (Exception ex) {
	    if(_logger.isLoggable(Level.SEVERE))
                _logger.log(Level.SEVERE,"enterprise.j2eeservice_running_exception",ex.toString());
	    if ( debug )
                _logger.log(Level.FINEST,"Exception running j2ee services",ex);
	    Log.err.flush();
	    throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Initialize the ORB.
     */
    private ORB createORB()
	throws Exception
    {
        ORB orb = ORBManager.getORB() ;

	// J2EEServer's persistent server port is same as ORBInitialPort.
	orbInitialPort = ORBManager.getORBInitialPort();

        if(_logger.isLoggable(Level.FINE))
	    _logger.log(Level.FINE,localStrings.getLocalString(
		"j2ee.listenPort","J2EE server listen port: " ) + orbInitialPort);

	return orb ; 
    }

    /**
     * Extends props with the JTS-related properties
     * based on the specified parameters.
     * The properties will be used as part of ORB initialization call.
     */
    public static Properties initJTSProperties(boolean lateRegistration) 
    {
	Properties props = new Properties() ;

        if (!lateRegistration) {
            props.put("com.sun.corba.ee.CosTransactions.ORBJTSClass",
		      "com.sun.jts.CosTransactions.DefaultTransactionService");
        }

	com.sun.jts.CosTransactions.Configuration.setAsAppClientConatiner(false);
	//read server.xml and convert them to props
	//there's a member in the class - but standard way is below one
        //START OF IASRI 4661409
        ConfigContext ctx =
	    ApplicationServer.getServerContext().getConfigContext();
        TransactionService txnService = null;
        try{
            txnService =
		ServerBeansFactory.getTransactionServiceBean(ctx);
	    //                  ServerBeansFactory.getServerBean(ctx).getTransactionService();
	    // SRIK
	    props.put(com.sun.jts.CosTransactions.Configuration.HEURISTIC_DIRECTION,
		txnService.getHeuristicDecision());
	    props.put(com.sun.jts.CosTransactions.Configuration.KEYPOINT_COUNT,
		txnService.getKeypointInterval());

            if (txnService.isAutomaticRecovery()) {
		if(debug) {
		    _logger.log(Level.FINE,"Recoverable J2EE Server");
		}
                props.put(com.sun.jts.CosTransactions.Configuration.MANUAL_RECOVERY,
			  "true");
	    }

	    boolean disable_distributed_transaction_logging = false;
	    String dbLoggingResource = null ;
	    ElementProperty[] eprops = txnService.getElementProperty();
	    for (int index = 0; index < eprops.length; index++) {
		if ("disable-distributed-transaction-logging".equals(eprops[index].getName())) {
		    if (!("true".equals(eprops[index].getValue())))
			disable_distributed_transaction_logging = false;
		    else
			disable_distributed_transaction_logging = true;
		} else  if("xaresource-txn-timeout".equals(eprops[index].getName())){
		    String value = eprops[index].getValue();
		    _logger.log(Level.FINE,"XAResource transaction timeout is"+value);
		    if (value != null) {
			com.sun.jts.jta.TransactionManagerImpl.setXAResourceTimeOut(
			    Integer.parseInt(value));	
		    }
		} else if ("db-logging-resource".equals(eprops[index].getName())) {
                    dbLoggingResource = eprops[index].getValue();
                    _logger.log(Level.FINE,"Transaction DB Logging Resource Name" + dbLoggingResource);
                    if (dbLoggingResource == null || " ".equals(dbLoggingResource)) {
                        dbLoggingResource = "jdbc/TxnDS";
                    }
                }
	    }
            if (dbLoggingResource != null) {
                disable_distributed_transaction_logging = true;
                props.put(com.sun.jts.CosTransactions.Configuration.DB_LOG_RESOURCE,
                          dbLoggingResource);
            }

            /**
	       JTS_SERVER_ID needs to be unique for each for server instance.
	       This will be used as recovery identifier along with the hostname
	       for example: if the hostname is 'tulsa' and iiop-listener-port is 3700
	       recovery identifier will be tulsa,P3700
            **/
            String jtsServerId =
		String.valueOf(DEFAULT_SERVER_ID); // default value
            IiopService iiopServiceBean =
		ServerBeansFactory.getIiopServiceBean(ctx);
            if (iiopServiceBean != null) {
                IiopListener iiopListener = iiopServiceBean.getIiopListener(0);
                if (iiopListener != null) {
                    jtsServerId = iiopListener.getPort();
                }
            }
            props.put(JTS_SERVER_ID,jtsServerId);

            /* ServerId is an J2SE persistent server activation
 	       API.  ServerId is scoped at the ORBD.  Since
 	       There is no ORBD present in J2EE the value of
 	       ServerId is meaningless - except it must have
 	       SOME value if persistent POAs are created. */

	    // For clusters - all servers in the cluster MUST
	    // have the same ServerId so when failover happens
	    // and requests are delivered to a new server, the
	    // ServerId in the request will match the new server.
	    
	    String serverId = String.valueOf(DEFAULT_SERVER_ID);
	    System.setProperty(J2EE_SERVER_ID_PROP, serverId);

	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE,
			    "++++ Server id: " 
			    + props.getProperty(ORBConstants.ORB_SERVER_ID_PROPERTY));
	    }

	    /**
	     * if the auto recovery is true, always transaction logs will be written irrespective of
	     * disable_distributed_transaction_logging.
	     * if the auto recovery is false, then disable_distributed_transaction_logging will be used
	     * to write transaction logs are not.If disable_distributed_transaction_logging is set to
	     * false(by default false) logs will be written, set to true logs won't be written.
	     **/
            if (!txnService.isAutomaticRecovery() && disable_distributed_transaction_logging) {
                com.sun.jts.CosTransactions.Configuration.disableFileLogging();
            }
            if (dbLoggingResource == null) {
		String logdir=txnService.getTxLogDir();
            	if(logdir==null){
		    Domain svr = null;
		    svr = ServerBeansFactory.getDomainBean(ctx);
		    logdir = svr.getLogRoot();
		    if(logdir == null){
                    	logdir = FileUtil.getAbsolutePath(".."+File.separator+"logs");
		    }
		} else if( ! (new File(logdir)).isAbsolute()) {
                    if(_logger.isLoggable(Level.FINE))
		    	_logger.log(Level.FINE,"enterprise.relative_tx_log_dir" , logdir);
		    Domain svr = null;
		    svr = ServerBeansFactory.getDomainBean(ctx);
		    String logroot=svr.getLogRoot();
		    if(logroot != null){
			logdir = logroot + File.separator + logdir;
		    } else {
			logdir = FileUtil.getAbsolutePath(".."+File.separator+"logs"
			    +File.separator+logdir);
		    }
		}
		String instanceName = ApplicationServer.getServerContext().getInstanceName();
                logdir += File.separator + instanceName + File.separator+"tx";

            	if (debug) {
		    _logger.log(Level.FINE,"JTS log directory: " + logdir);
		    _logger.log(Level.FINE,"JTS Server id " + jtsServerId);
            	}
            	(new File(logdir)).mkdirs();
            	props.put(com.sun.jts.CosTransactions.Configuration.LOG_DIRECTORY,
			  logdir);
	    }
            props.put(com.sun.jts.CosTransactions.Configuration.COMMIT_RETRY,
                      txnService.getRetryTimeoutInSeconds());
	    props.put(com.sun.jts.CosTransactions.Configuration.INSTANCE_NAME,
		      ApplicationServer.getServerContext().getInstanceName());

        } catch(ConfigException e){
            throw new RuntimeException("Error reading configuration : "+e);
        }
	com.sun.jts.CosTransactions.Configuration.setProperties(props);
	return props ;
    }
}

// End of file.
