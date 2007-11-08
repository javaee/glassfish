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
package com.sun.enterprise.server;

import com.sun.enterprise.connectors.util.ResourcesUtil;
import java.io.*;
import java.util.Properties;
import java.util.Vector;
import java.net.*;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import java.security.SecureRandom;
import com.sun.corba.ee.impl.orbutil.ORBConstants;

import com.sun.enterprise.*;
import com.sun.enterprise.util.*;
import com.sun.enterprise.log.*;
import com.sun.enterprise.repository.*;
import com.sun.enterprise.naming.*;
import com.sun.enterprise.distributedtx.*;
import com.sun.enterprise.appverification.factory.AppVerification;
import com.sun.enterprise.resource.ResourceInstaller;
import com.sun.enterprise.iiop.POAProtocolMgr;
import com.sun.enterprise.iiop.PEORBConfigurator;
import com.sun.enterprise.iiop.ORBMonitoring;
import com.sun.ejb.ContainerFactory;
import com.sun.ejb.containers.ContainerFactoryImpl;
import java.util.logging.*;
import com.sun.logging.*;

import com.sun.appserv.server.ServerLifecycleException;

import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ServerContextImpl;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.transaction.monitor.JTSMonitorMBean;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.util.diagnostics.Reporter;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.config.serverbeans.ElementProperty;


import javax.resource.spi.ManagedConnectionFactory;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.io.ConnectorDeploymentDescriptorFile;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.JmsRaUtil;

import com.sun.enterprise.deployment.backend.OptionalPkgDependency;

/**
 * J2EEServer is the main program for the J2EE Reference Implementation Server.
 * This process hosts all the services and containers in the RI, including
 * Web (Tomcat), EJB, CORBA, Naming, Security, Deployment and JMS.
 *
 * @author various
 */

public final class J2EEServer
{
    // SecureRandom number used for HTTPS and IIOP/SSL.
    // This number is accessed by iiop/IIOPSSLSocketFactory
    // & web/security/SSLSocketFactory classes.
    private static final Logger _logger = 
	LogDomains.getLogger(LogDomains.CORE_LOGGER);
    public static final SecureRandom secureRandom = new SecureRandom();
    static {
        secureRandom.setSeed(System.currentTimeMillis());
    }
    public static final String J2EE_HOME="com.sun.enterprise.home";

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
	new LocalStringManagerImpl(J2EEServer.class);

    private static java.io.PrintStream ostream = System.out;
    private static java.io.PrintStream estream = System.err;

    private static final int MAX_INITIAL_CONTEXT_RETRIES = 15;

    /** Property names */
    private static final String DefaultDbDir= "orb.db";
    private static final String LISTEN_PROP =
	"com.sun.CORBA.connection.ORBListenSocket";

    private static final String OUTPUT_LOG= "system.out";
    private static final String ERROR_LOG= "system.err";


    // Used for POAProtocolMgr to create poaids.db
    // Used for activation repository servers.db
    private File repositoryDir;

    //IASRI 4661409 private Properties defaultProperties;
    private int orbInitialPort;
    private Configuration conf = null;
    private ORB orb;
    private ProtocolManager protocolMgr;

    private ServerContext serverContext;
  
    /**
     * Default constructor.
     */
    public J2EEServer()
    {
    }

    private void run(String[] args, boolean verbose, boolean startJMS)
    {
        ConnectorRuntime.getRuntime().initialize(ConnectorRuntime.SERVER);
        try {
            if ( (serverContext != null) &&
                 (serverContext instanceof ServerContextImpl) ) {

                ServerContextImpl ctxImp = (ServerContextImpl)serverContext;
                String seedFile = ctxImp.getServerConfigPath() +
                    File.separator + "secure.seed";
                File secureSeedFile = new File(seedFile);

                // read the secure random from the file
                long seed = readSecureSeed(secureSeedFile);
                secureRandom.setSeed(seed);
                // generate a new one for the next startup
                seed = secureRandom.nextLong();
                writeSecureSeed(secureSeedFile, seed);
                secureSeedFile = null;

            } else {
                _logger.log(Level.FINE,"Unable to initialize secure seed.");
            }

       	    // Initialize System properties
	    Properties props = System.getProperties();

	    // set server-id and server-port so that persistent EJBs
	    // in J2EEServer will be reachable after crashes.
	    props.setProperty(J2EE_APPNAME, "j2ee");


	    // Moved it here since the ORBManager uses this
	    Switch theSwitch = Switch.getSwitch();
            theSwitch.setContainerType(Switch.EJBWEB_CONTAINER);


            Switch.getSwitch().setProviderManager(ProviderManager.getProviderManager());

            //set server context for ResourcesUtil
            ResourcesUtil.setServerContext(serverContext);

	    // initialize InvocationManager (needed by NamingManager, TM)
	    InvocationManager invMgr = new InvocationManagerImpl();
	    theSwitch.setInvocationManager(invMgr);

	    // initialize NamingManager
	    NamingManager nm = new NamingManagerImpl();
	    theSwitch.setNamingManager(nm);

            // initialize InjectionManager
            InjectionManager injectionMgr = new InjectionManagerImpl();
            theSwitch.setInjectionManager(injectionMgr);

	    J2EETransactionManager tm =
		J2EETransactionManagerImpl.createTransactionManager();
	    theSwitch.setTransactionManager(tm);


	    // Install JDBC data sources and Connector connection factories
            ResourceInstaller installer = theSwitch.getResourceInstaller();

            // Load System Resource Adapters.
	    ConnectorRuntime connRuntime = ConnectorRuntime.getRuntime();


            // instantiate and register the server-side RoleMapperFactory
            initRoleMapperFactory();
            
            //Deploy jms ra
/*
            JmsService jmsService = ServerBeansFactory.getConfigBean(
                            serverContext.getConfigContext()).getJmsService();
            if (jmsService.getType().equals("LOCAL")) {
                _logger.log(Level.INFO, "mqra.start", jmsService.getType());
                String mqraModuleName = ConnectorRuntime.DEFAULT_JMS_ADAPTER;
                String mqraModuleLoc = installer.getSystemModuleLocation(mqraModuleName);
                connRuntime.createActiveResourceAdapter(mqraModuleLoc,mqraModuleName,false);
            }
*/
            // register managed objects
            ManagementObjectManager mgmtObjectMgr =
		theSwitch.getManagementObjectManager();
 
            //mgmtObjectMgr.registerJVM();
            //mgmtObjectMgr.registerJ2EEDomain("Domain");
            //mgmtObjectMgr.registerJavaMailResource("JavaMail");
            //mgmtObjectMgr.registerJNDIResource("JNDI-" + nm.hashCode());
	    //mgmtObjectMgr.registerRMI_IIOPResource("ORB-" + orb.hashCode());
            //mgmtObjectMgr.registerJTAResource("JTA-" + tm.hashCode());

	    //initialize monitoring for all connector related stats
            ConnectorRuntime.getRuntime().initializeConnectorMonitoring();

            //installer.installJdbcDataSources();
            
            installer.installPersistenceManagerResources();
            installer.installCustomResources();
            installer.installExternalJndiResources();
            installer.installMailResources();

	    // Now happens thru resource adapter.
            //installer.installJMSResources();

	    // Create EJB ContainerFactory
	    ContainerFactory cf = new ContainerFactoryImpl();
	    theSwitch.setContainerFactory(cf);

	    //satisfy the dependencies between optional packages
	    try { 
                _logger.fine("satisfy.optionalpkg.dependency");
	        OptionalPkgDependency.satisfyOptionalPackageDependencies();                
	    } catch (Exception e) {
		_logger.log(Level.WARNING, 
			    "optionalpkg.error", e);	
	    }


	    //register the monitoring bean for JTS
	    com.sun.enterprise.admin.monitor.MonitoringHelper.registerTxnMonitoringMBean(new JTSMonitorMBean());
   
	    // Install UserTransaction object for use by standalone clients
	    nm.publishObject("UserTransaction", new UserTransactionImpl(),true);

            String startupComplete =
		localStrings.getLocalString("j2ee.started",
					    "J2EE server startup complete.");
	    _logger.log(Level.FINE,startupComplete);
        }
	catch (Exception ex) {
	    if(_logger.isLoggable(Level.SEVERE))
                _logger.log(Level.SEVERE,"enterprise.j2eeservice_running_exception",ex.toString());
	    if ( debug )
                _logger.log(Level.FINEST,"Exception running j2ee services",ex);
	    Log.err.flush();
	    throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /** register the RoleMapperFactory that should be used on the server side
     */
    private void initRoleMapperFactory() throws Exception
    {    
        Object o = null;
        Class c=null;
        // this should never fail.
        try {
            c = Class.forName("com.sun.enterprise.security.acl.RoleMapperFactory");
            if (c!=null) {
                o = c.newInstance();  
                if (o!=null && o instanceof SecurityRoleMapperFactory) {
                    SecurityRoleMapperFactoryMgr.registerFactory((SecurityRoleMapperFactory) o);
                }
            }
            if (o==null) {
                _logger.log(Level.SEVERE,localStrings.getLocalString("j2ee.norolemapper", 
								     "Cannot instantiate the SecurityRoleMapperFactory"));
            }
        } catch(Exception cnfe) {
            _logger.log(Level.SEVERE,
			localStrings.getLocalString("j2ee.norolemapper", "Cannot instantiate the SecurityRoleMapperFactory"), 
			cnfe);
            throw  cnfe;
        } 
    }
    
    /** read the secure random number from the file.
     *  If the seed is not present, the default expensive SecureRandom seed
     *  generation algorithm is invoked to return a new seed number
     *  @param File the file to be read - here secure.seed file.
     */
    private long readSecureSeed(File fname){
	byte[] seed;
	try {
            BufferedReader fis = new BufferedReader(new FileReader(fname));
            try{
                String line = fis.readLine();
                fis.close();
                // returning a long value.
                Long lseed = new Long(line);
                return lseed.longValue();
            } catch (IOException e){
		if (fis != null)
		    fis.close();
	    }
	} catch (Throwable e){  // IASRI 4666401 if all fails just create new
	}
        // BEGIN IASRI 4703002
        // In order to work around JVM bug 4709460 avoid internal seeding.
        // (Call setSeed again (see static block) to attempt to add some
        // minimal randomness; setSeed calls are cumulative)
        
        secureRandom.setSeed(System.currentTimeMillis());
        long newSeed = secureRandom.nextLong();
        return newSeed;
    }
    
    /** write the new secure seed to the secure.seed file to speed up
     * startup the next time the server is started.
     * @param File secure.seed file
     * @param long seed the value of the 8 byte seed.
     */
    private void writeSecureSeed(File fname, long seed){
	try{
	    FileOutputStream fos = new FileOutputStream(fname);
	    String sseed = Long.toString(seed);
	    fos.write(sseed.getBytes());
	    fos.close();
	}catch(IOException e){
	    String errmsg =
		localStrings.getLocalString("j2ee.startupslow",
					    "Cannot write the seed file for fast startup. The next startup will be slow.");

	    _logger.log(Level.WARNING,"enterprise.j2ee_startupslow");
	}

    }

    /**
     * Main for the J2EE server.
     */
    public static void main(ServerContext serverContext) throws ServerLifecycleException
    {
        String[] args = serverContext.getCmdLineArgs();
	if (args == null) args = new String[0];


	Utility.checkJVMVersion();
	
	// parse args
	boolean verbose = false;
	boolean startJMS = true;
	
	if (System.getProperty("j2ee.appverification.home") != null) {
	    AppVerification.setInstrument(true);
	}
	_logger.log(Level.FINE, "S1AS AVK Instrumentation "
		    + (AppVerification.doInstrument() ? "enabled" : "disabled"));

	try {

	    // setting ORB class and ORB singleton to SE values
	    //setting RMI-IIOP delegates to EE values
  	    ORBManager.setORBSystemProperties();

	    // start the server
	    J2EEServer j2ee = new J2EEServer();
	    j2ee.setServerContext(serverContext);
	    j2ee.run(args, verbose, startJMS);
	} catch(Exception e) {
	    _logger.log(Level.SEVERE,"enterprise.run_exception",e);
	    if(e.getMessage() != null) {		  
		if(_logger.isLoggable(Level.SEVERE))
		    _logger.log(Level.SEVERE,"enterprise.j2ee_server_error",e.getMessage());
	    }
	    _logger.log(Level.SEVERE,"enterprise.j2ee_server_error1");
	    System.err.flush();
	    throw new ServerLifecycleException(e.getMessage(), e);
	}
    }
  
    private void setServerContext(ServerContext serverContext) {
        this.serverContext = serverContext;
    }


    /**
     * Shutdown the J2EE server. 
     */
    public static void shutdown() {
        PEMain.shutdownStarted = true;
	try {
	    if(_logger.isLoggable(Level.FINE))
	        _logger.log(Level.FINE,localStrings.getLocalString("j2ee.shutdown","Shutting down the J2EE server..."));
	    
	    if (AppVerification.doInstrument()) {
	        AppVerification.getInstrumentLogger().writeResults();
	    }
	    
	    Log.flushAll();
	} catch(Throwable t) {
	    if(_logger.isLoggable(Level.SEVERE))
	        _logger.log(Level.SEVERE,"enterprise.shutdown_exception",t.toString());
	    if(_logger.isLoggable(Level.FINE))
	        _logger.log(Level.FINE,localStrings.getLocalString("j2ee.cannot.shutdown","Unable to shutdown the J2EE server..."));
	}
	if (PEMain.shutdownThreadInvoked == false)
	    System.exit(0);

    }
}

// End of file.




