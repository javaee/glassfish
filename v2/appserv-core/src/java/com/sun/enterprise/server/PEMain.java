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

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;

import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.Locale;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.web.WebContainer;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.server.logging.*;
import com.sun.enterprise.server.logging.stats.ErrorStatistics;

import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.event.ShutdownEvent;
import com.sun.enterprise.server.Shutdown;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactoryImpl;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.launcher.PELaunchFilter;

import com.sun.enterprise.jms.JmsProviderLifecycle;

import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.server.ss.ASLazyKernel;
import com.sun.enterprise.admin.server.core.channel.RRStateFactory;

/**kebbs**/
 import com.sun.enterprise.admin.server.core.jmx.SunoneInterceptor;

 import com.sun.enterprise.server.ondemand.OnDemandServer;

import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.audit.AuditManagerFactory;


import com.sun.enterprise.management.support.SystemInfoData;
import com.sun.appserv.management.util.misc.RunnableBase;
import com.sun.appserv.management.util.misc.RunnableBase.HowToRun;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.TimingDelta;

import com.sun.enterprise.util.FeatureAvailability;
import static com.sun.enterprise.util.FeatureAvailability.SERVER_STARTED_FEATURE;
import static com.sun.enterprise.util.FeatureAvailability.MBEAN_SERVER_FEATURE;

/**
  * Start up class for PE/RI
  */

public class PEMain {
    private static final long START_TIME_MILLIS = System.currentTimeMillis();
    public static long getStartTimeMillis() { return START_TIME_MILLIS; }
    
    static {
        // Note: This call must happen before any calls on the logger.
        ErrorStatistics.registerStartupTime();
    }

    //------------------------------------------------------------ Constructor

    /**
        Thread into which canb be inserted any code that can be run threaded to pre-initialize
        anything that <b>will</b> be needed during server startup.  There are generally many
        excess CPU cycles at startup on multi-core machines.
     */
    private static final class AtStartup extends RunnableBase {
        AtStartup() {
            super( "PEMain-AtStartup" );
        }

        private  void callAMXPreload() {
            try {
                final Class c = Class.forName( "com.sun.enterprise.management.support.Preload" );
                final java.lang.reflect.Method m  = c.getMethod( "preload", (Class[])null );
                m.invoke( (Object[])null );
            }
            catch( Exception e ) {
                throw new Error( e );
            }
        }

        protected void doRun() {
            final Thread thisThread = Thread.currentThread();
            thisThread.setPriority( thisThread.MIN_PRIORITY );
            callAMXPreload();
        }
    };
    
    /**
        Load the MBeanServer.
     */
    private static final class LoadMBeanServer extends RunnableBase {
        LoadMBeanServer() {
            super( "PEMain-LoadMBeanServer" );
        }
        protected void doRun() {
            final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            FeatureAvailability.getInstance().registerFeature(
                MBEAN_SERVER_FEATURE, mbeanServer);
                
            getLogger().log(Level.INFO,
                "pemain.mbeanserver_started", mbeanServer.getClass().getName());
        }
    };


    /**
     * This object will be created during the Init phase of NSAPI
     */
    public PEMain() {
        // Set the context class loader
        _loader = getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(_loader);
	AdminEventListenerRegistry.addShutdownEventListener(new Shutdown());
        _instance = this;
    }

    // ----------------------------------------------------- Instance Variables

    public static volatile boolean shutdownStarted = false;

    public static volatile boolean shutdownThreadInvoked = false;

    private static volatile ApplicationServer _server = null;

    private final static String STOP = "stop";

    private static volatile PEMain _instance = null;

    private final static String SERVER_INSTANCE =
                        System.getProperty("com.sun.aas.instanceName");

    /** local string manager */
    private static StringManager localStrings =
                        StringManager.getManager(PEMain.class);

    ServerContext context = null;

    //WARNING: _logger must be initialized upon demand in this case. The
    //reason is that this static init happens before the ServerContext
    //is initialized
    private static Logger _logger = null;

    private ClassLoader _loader = null;

    private boolean isStarted = false;

    private final static String instance_root =
    System.getProperty("com.sun.aas.instanceRoot");

    private static AuditManager auditManager =
            AuditManagerFactory.getAuditManagerInstance();

    private static synchronized Logger getLogger() {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
        }
        return _logger;
    }
    
    /**
        Code surrounded by this causes hangs of up to 150 seconds at startup, though it usually
        takes only a few milliseconds to a few seconds.
        The code is obsolete and should not be needed.
     */
    private static final boolean    USE_OLD_INSTANCE_STATUS_CHECK   = false;

    public static void main(String[] args) {
        new AtStartup().submit( HowToRun.RUN_IN_SEPARATE_THREAD );
        new LoadMBeanServer().submit( HowToRun.RUN_IN_SEPARATE_THREAD );
        

	// parse args
	boolean verbose = false;
	boolean dbg = false;

        //set the system locale for this instance
        setSystemLocale();

        if(args[0].trim().equals(STOP)) {
            // check if instance is already running
            if (isInstanceRunning()) {
                    PEMain.shutdown();
                } else {
                    getLogger().log(Level.INFO, "instance.notRunning");
            }
            return;
        }

	try {

        // add temporary switch for new ProcessLauncher.
        // This will be removed once PE using the new invocation classes
        // Redirect stdout and stderr if location supplied in System.properties
        // NOTE: In verbose more the stderr only goes to the console, this is exactly the
        // way the apache commons-launcher functioned.
        String verboseMode=System.getProperty("com.sun.aas.verboseMode", "false");
        // Temporarily commented out. 
        // Hemanth: 
        // Will move the new SystemOutandErrorHandler() here after doing
        // some testing. The code is commented out mainly because the Log
        // Rotation will not work with the stream opened here.
        /*
        if(System.getProperty("com.sun.aas.processLauncher") != null
            && !verboseMode.equals("true")) {

            // If applicable, redirect output and error streams
            String defaultLogFile = System.getProperty("com.sun.aas.defaultLogFile");
            if (defaultLogFile != null) {
                PrintStream printStream = new PrintStream(new FileOutputStream(defaultLogFile, true), true);
                System.setOut(printStream);
                System.setErr(printStream);
            }
        }
        */


        // read in parameters off the stdin if they exist
        try {
            // check to see is Identity info is already set from native launcher
            if (IdentityManager.getUser() == null) {
                IdentityManager.populateFromInputStreamQuietly();
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "pemain.failureOnReadingSecurityIdentity", e);
        }
	if (getLogger().isLoggable(Level.FINE)) {
	    getLogger().log(Level.FINE, IdentityManager.getFormatedContents());
	}



       //Set system properties that correspond directly to asenv.conf/bat. This
       //keeps us from having to pass them all from -D on the command line.
       ASenvPropertyReader reader = new ASenvPropertyReader(
           System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
       reader.setSystemProperties();

        if ( USE_OLD_INSTANCE_STATUS_CHECK ) {
            // check if instance is already running
            if (isInstanceAlreadyStarted()) {
                getLogger().log(Level.SEVERE, "instance.alreadyRunning");
            System.exit(0);
            }
        }

	    getLogger().log(Level.FINE, "instance.start", SERVER_INSTANCE);

	    //_server = new ApplicationServer();
	    _server = new OnDemandServer();

            // print server starting message	 
	    String cstr = localStrings.getStringWithDefault( "pemain.start",
                                "Sun Java System Application Server",
                                new String[] {Version.getFullVersion()});
	    getLogger().log(Level.INFO, cstr);

	    // if running in debug mode, print JPDA address and transport
	    String debugOptions = System.getProperty(
						PELaunchFilter.DEBUG_OPTIONS);
	    if ( debugOptions != null && !debugOptions.equals("") ) {
                String transport = PELaunchFilter.getDebugProperty(
						    debugOptions, "transport");
                String addr = PELaunchFilter.getDebugProperty(
						    debugOptions, "address");
		String str = localStrings.getStringWithDefault(
				"pemain.debugger.message",
				"Application server is listening at address " + addr + " for debugger to attach using transport " + transport,
				new Object[] {addr, transport});

		System.err.println(str);
	    }

	    PEMain peMain = new PEMain();

	    //adding shutdown hook in case the small window on WIN OS is closed
	    ShutdownThread shutdownThread = new ShutdownThread();
	    Runtime runtime = Runtime.getRuntime();
	    runtime.addShutdownHook(shutdownThread);

	    peMain.run(System.getProperty(Constants.IAS_ROOT));

        // add startup time metrics
        final long now     = System.currentTimeMillis();
        final long pemainMillis    = now - getStartTimeMillis();
        final long pelaunchMillis  = now - PELaunch.getStartTimeMillis();
        SystemInfoData.getInstance().addPerformanceMillis( "PEMain.startup", pemainMillis );
        SystemInfoData.getInstance().addPerformanceMillis( "PELaunch.startup", pelaunchMillis );
        
        getLogger().log(Level.INFO, "pemain.startup.complete");

        if (auditManager.isAuditOn()){
            auditManager.serverStarted();
        }

	} catch(Exception e) {
	    getLogger().log(Level.SEVERE, "pemain.error", e.getMessage());
	    System.exit(1);
	}

    }


    /**
    * return the ApplicationServer object
    */

    public static ApplicationServer getApplicationServer() {
	return _server;
    }

    public static PEMain getInstance() {
        return _instance;
    }

    /**
    * method to start the PE server
    */
    public void run(String rootDir) {

        try {
            RRStateFactory.removeStateFile();
        } catch (Exception e) {
            getLogger().log(Level.FINE, "Could not remove restart required state file", e);
        }

	try {
	    context = createServerContext(rootDir);

	} catch (ConfigException ce) {
            getLogger().log(Level.SEVERE, "j2eerunner.cannotCreateServerContext",ce);
        }


	// Set up to route System.Out & err thru log formatter
	new SystemOutandErrHandler();

        _server.setServerContext(context);

        // Execute this only once (i.e. during server startup)
        try {
    	    // initialized the application server.

            _server.onInitialization(context);

	    if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE,
                                "application.config_file" +
                                context.getServerConfigURL());
                getLogger().log(Level.FINE,
                    "application.default_locale" +
                    java.util.Locale.getDefault());
            }
	    _server.onStartup();

            _server.onReady();
            
            //When uncommented, this code can be called to load all config MBeans so that
            //lazy loading is effectively disabled.
            try {
                FeatureAvailability.getInstance().getMBeanServer().queryNames(null, null);
            } catch (Exception ex) {
                // ignore
            }
            FeatureAvailability.getInstance().registerFeature( SERVER_STARTED_FEATURE, "" );
        }
        catch (Exception ee) {
            getLogger().log(Level.SEVERE, "j2eerunner.initError", ee);
            getLogger().log(Level.SEVERE, "pemain.startup.failed");
            getLogger().log(Level.INFO, "shutdown.started");
            try {
                _server.onShutdown();
            } catch (ServerLifecycleException e) {
                getLogger().log(Level.SEVERE, "j2eerunner.initError", e);
            }
            try {
            _server.onTermination();
            } catch (ServerLifecycleException e) {
                getLogger().log(Level.SEVERE, "j2eerunner.initError", e);
            }
            getLogger().log(Level.SEVERE,"pemain.server.startup.failed.exit");
            System.exit(1);
        }
        synchronized ( this ) {
            isStarted = true;
            this.notifyAll();
        }
    }

    public boolean isStartingUp() {
        return !isStarted;
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Shutdown the J2EE PE/RI server. Called when "PEMain stop" is invoked,
     * in a "client" VM. Does a remote invocation on the server.
     */
    public static void shutdown() {
	try {
	    RMIClient rmiClient = new RMIClient(
						true,
						getStubFilePath(),
						getSeedFilePath());

	    ShutdownEvent shutdownEvent = new ShutdownEvent(SERVER_INSTANCE);
	    getLogger().log(Level.INFO,
			    "sending notification to server..." +
			    SERVER_INSTANCE);
	    AdminEventResult result = rmiClient.sendNotification(shutdownEvent);
	    getLogger().log(Level.INFO,
			    "server.shutdown_complete");

	} catch(Exception e) {
	    getLogger().log(Level.SEVERE, "j2eerunner.initError", e);
	}
    }


    /**
     * method to obtain stub file
     */
    public static String getStubFilePath() {

        return instance_root +
	  File.separatorChar +
	  "config" +
	  File.separatorChar +
	  "admch";
    }

   /**
     * method to obtain stub file
     */
    public static String getSeedFilePath() {

        return instance_root +
	  File.separatorChar +
	  "config" + File.separatorChar +
	  "admsn";
    }

    /**
     * Get status of the instance.
     */
    private static int getInstanceStatus() {
        String stubFile = getStubFilePath();
        String seedFile = getSeedFilePath();
        RMIClient rmiClient = new RMIClient(true, stubFile, seedFile);
        return rmiClient.getInstanceStatusCode();
    }

    /**
     * Is instance in running state.
     */
    private static boolean isInstanceRunning() {
        return (getInstanceStatus() == Status.kInstanceRunningCode);
    }

    /**
     * Is instance in starting or running state.
     */
    private static boolean isInstanceStartingOrRunning() {
        int statusCode = getInstanceStatus();
        return (statusCode == Status.kInstanceStartingCode
                || statusCode == Status.kInstanceRunningCode);
    }

    /**
     * method to check if server is already up or not
     */
    public static boolean isInstanceAlreadyStarted() {
        return isInstanceStartingOrRunning();
    }


    /**
     * Create and initialize a server context object and also initialize
     * its configuration context by reading the configuration file.
     */
    private ServerContext createServerContext(String rootDir) throws ConfigException {

        // setup the config and the initial server context
        String[] args = new String[0];
        ServerContextImpl context = new ServerContextImpl();

        context.setCmdLineArgs(args);
	context.setInstallRoot(rootDir);
	context.setInstanceName(SERVER_INSTANCE);
        try{
            String serverXml = context.getServerConfigURL();
	    // Read server.xml and create a read-only configuration context
            ConfigContext cfgContext =
		ConfigFactory.createConfigContext(serverXml,
                                                  true,
                                                  false,
                                                  true);
	    context.setConfigContext(cfgContext);
	} catch (Exception ex){

	    if (!(ex instanceof ConfigException)){
                getLogger().log(Level.SEVERE,"j2eerunner.server_context_excp",ex);
        	if (_logger == null){
		    System.err.println("Exception in creating server context");
		    ex.printStackTrace();
		}
	    }
	    throw new ConfigException(ex.getMessage());
        }

        PluggableFeatureFactory ff = PluggableFeatureFactoryImpl.getFactory();

        context.setPluggableFeatureFactory(ff);
        return context;
    }


   /**
    * Sets the default locale for this instance using the system property
    * com.sun.aas.defaultLocale set only in the PELauncheFilter. This is an
    * implementation specific property and not a standard Java system property.
    *
    * The locale must be specified in the following format: <br>
    * <br><i><language>_<country_<variant></i> <br>
    * For example: <i>en_US_UNIX </i><br>
    * Of course, not all of these options need to be specified.
    *
    */
    static void setSystemLocale() {
        String locale =
              System.getProperty(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY);
        if(locale != null && !"".equals(locale) ) {
            try {
                String[] tokens = locale.split("_",3);
                switch(tokens.length) {
                    case 0:
                        break;
                    case 1:
                        Locale.setDefault(new Locale(tokens[0]));
                        break;
                    case 2:
                        Locale.setDefault(new Locale(tokens[0],tokens[1]));
                        break;
                    default:
                        Locale.setDefault(new Locale(tokens[0],tokens[1],tokens[2]));
                        break;
                }
            } catch(Exception e) {
                getLogger().log(Level.WARNING, "locale.setdefault.error",locale);
            }
        }
    }
    
    private static class ShutdownThread extends Thread {

        public ShutdownThread() {
        }

        public void run() {
	    shutdownThreadInvoked = true;
	    if (PEMain.shutdownStarted == false) {
		PEMain.shutdown();
	    }
	}

    }

}
