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

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import com.sun.enterprise.Switch;
import com.sun.enterprise.server.J2EEServer;
import com.sun.enterprise.security.audit.AuditModuleEventListenerImpl;
import com.sun.enterprise.security.audit.SecurityServiceEventListenerImpl;
import com.sun.enterprise.security.auth.realm.AuthRealmEventListenerImpl;
import com.sun.enterprise.security.auth.realm.UserMgmtEventListenerImpl;
import com.sun.enterprise.security.jmac.config.MessageSecurityConfigEventListenerImpl;

import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.instance.InstanceFactory;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.loader.ClassLoaderUtils;
import com.sun.enterprise.util.logging.Debug;

import com.sun.enterprise.util.ConnectorClassLoader;
import com.sun.enterprise.resource.ResourceInstaller;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;

import com.sun.enterprise.server.pluggable.InternalServicesList;

import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.MessageSecurityConfigEvent;
import com.sun.enterprise.admin.event.jms.JmsServiceEvent;
import com.sun.enterprise.admin.event.jms.JmsHostEvent;
import com.sun.enterprise.admin.alert.AlertConfigurator;
//import com.sun.enterprise.admin.event.EventTester;

//IASRI 4717059 BEGIN
//import com.sun.ejb.containers.ReadOnlyBeanNotifierImpl;
//IASRI 4717059 END

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.server.logging.ServerLogManager;


import com.sun.appserv.management.util.misc.TimingDelta;
import com.sun.appserv.management.util.misc.RunnableBase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ApplicationServer is the main entry point for iAS 7.0 J2EE server instance.
 * This code implements the server lifecycle state transitions and it relies on
 * various iAS 7.0 subsystems such as Web/EJB containers throught its lifecycle.
 * com.sun.enterprise.J2EEServer is the RI counerpart of this code; the evolving
 * iAS server implementation replaces functionality of RI J2EEServer.
 *
 * NOTES on services by name:
 * Default services implementing lifecycle interface for orderly startup
 * shutdown and hot deployment.
 * Each service entry is of the form {name, className}.
 * Note: Make sure LifecycleModuleService is the last subsystem in this list.
 */
public class ApplicationServer {
    /** server logger */
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** ServerContext -- server-wide runtime environment context **/
    private static ServerContext context = null;

    /** services -- standard runtime service objects **/
    private  final List<ServerLifecycle> services = new ArrayList<ServerLifecycle>();

    /** common class loader for the server */
    private ClassLoader commonClassLoader;

    /** share class loader for the server */
    private ClassLoader connectorClassLoader;

    /** 
     * Default constructor.
     */
    public ApplicationServer() {}

    /**
     * Server is initializing subsystems and setting up the runtime environment.
     * Prepare for the beginning of active use of the public methods of this
     * subsystem. This method is called before any of the public methods of 
     * this subsystem are utilized.  
     *
     * @param context ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *  started
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onInitialization( final ServerContext context) 
                throws ServerLifecycleException
    {
        
        // sets the server context
        this.context = context;

        // NOTE: side effects of reInitializeServerLoggers invoke various MBean related code
        // that really ought not to run yet, and that code hits synchronized code being relied
        // upon (indirectly via a Logger).  The thread initializing the MBeanServer (see PEMain)
        // will try to acquire that same synchronized lock and then hang.
        // See CR6555027.
        com.sun.enterprise.util.FeatureAvailability.getInstance().waitForMBeanServer();
        
        // initialize all server loggers that were not initialized
        // Note: reInitializeServerLoggers should be called after 
        //       setting the server context and before performing any
        //       other server startup activities that needs a logger

        ServerLogManager.reInitializeServerLoggers();
        
        // print jvm info
        printStartupInfo();

        // system class loader 
        ClassLoader parentOfCommonCL = this.getClass().getClassLoader();
        //Shared CL
        //In the new CL hierarchy the parent of common classloader
        //is the shared chain.
        if(Boolean.getBoolean(com.sun.enterprise.server.PELaunch.USE_NEW_CLASSLOADER_PROPERTY))
            parentOfCommonCL = PELaunch.getSharedChain();

        final InstanceEnvironment env = this.context.getInstanceEnvironment();

        try {
            final String dir     =  env.getLibClassesPath();
            final String jarDir  = env.getLibPath();

            // constructs the common class loader. System class loader 
            // is the parent of common class loader. Common class loader 
            // includes <instance>/lib/classes dir, <instance>/lib/*.jar 
            // and <instance>/lib/*.zip
            commonClassLoader = 
                ClassLoaderUtils.getClassLoader(new File[] {new File(dir)}, 
                                                new File[] {new File(jarDir)},
                                                parentOfCommonCL
                                                );
            logCommonClassLoaderDetails();
            

            // ignore if null
            if (commonClassLoader == null) {
                commonClassLoader = parentOfCommonCL;
            }
        } catch (IOException ioe) {
            _logger.log(Level.WARNING, "server.ioexception", ioe);
            commonClassLoader = parentOfCommonCL;
        } catch (Throwable th) {
            _logger.log(Level.WARNING, "server.exception", th);
            commonClassLoader = parentOfCommonCL;
        }

        // Create the connector class loader. This will be the parent class
        // loader for all j2ee components!! Common class loader is the 
        // parent of connector class loader.
        connectorClassLoader =
            ConnectorClassLoader.getInstance(commonClassLoader);
        
        // sets the class loaders in the server context
        if (this.context instanceof ServerContextImpl) {
            final ServerContextImpl contextImpl = (ServerContextImpl) this.context;

            // set the common classloader in the server context
            contextImpl.setCommonClassLoader(commonClassLoader);

            // sets the shared class loader
            contextImpl.setSharedClassLoader(connectorClassLoader);

            // sets the life cycle parent class loader
            contextImpl.setLifecycleParentClassLoader(connectorClassLoader);
        }

        // final common class loader for the anonymous inner class
        final ClassLoader commonCL = commonClassLoader;

        // set the common class loader as the thread context class loader
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(commonCL);
                    return null;
                }
            }
            );

	//added the pluggable interface way of getting the list of services
	final InternalServicesList servicesList = 
		context.getPluggableFeatureFactory().getInternalServicesList();

	final String[][] servicesByName = servicesList.getServicesByName();
	if (servicesByName == null) {
	      _logger.log(Level.SEVERE, "services.null");
	      throw new ServerLifecycleException();
	}

        // security manager is set inside SecurityLifecycle

	// Instantiate the built-in runtime services
        final List<ServerLifecycle> temp = instantiateRuntimeServices( context, servicesByName );
        services.addAll( temp );
	        
        // Initialize the built-in runtime services
        for ( final ServerLifecycle service : services) {
	        try {
                service.onInitialization(context);
            } catch (ServerLifecycleException e) {		   
                _logger.log(Level.SEVERE, "service.notinit", 
                    new Object[] {service, e.toString()});
                throw e;
            }
        }

        //add listeners for security dynamic reconfiguration
        AdminEventListenerRegistry.addAuditModuleEventListener(
                new AuditModuleEventListenerImpl());
        AdminEventListenerRegistry.addAuthRealmEventListener(
                new AuthRealmEventListenerImpl());
        AdminEventListenerRegistry.addSecurityServiceEventListener(
                new SecurityServiceEventListenerImpl());
        AdminEventListenerRegistry.addUserMgmtEventListener(
                new UserMgmtEventListenerImpl());
        AdminEventListenerRegistry.addEventListener(
                MessageSecurityConfigEvent.eventType,
                new MessageSecurityConfigEventListenerImpl());

            // Call RI J2EEServer initialization code. Note that all resources
            // specified in server.xml will be loaded by the common class loader.
        try {
            J2EEServer.main(context);
        } catch (Exception e) {
            throw new ServerLifecycleException(e);
        }

        // final connector class loader for the anonymous inner class
        final ClassLoader connCL = connectorClassLoader;

        // set the connector class loader as the thread context class loader
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().
                        setContextClassLoader(connCL);
                        return null;
                    }
                }
        );
    }
    
    //Log all URLs loaded by the common classloader
    private void logCommonClassLoaderDetails() {
    	Logger _log = LogDomains.getLogger(LogDomains.LOADER_LOGGER);
    	if (_log.isLoggable( Level.FINE ) ) {
            if (commonClassLoader instanceof URLClassLoader) {
            	URL[] u = ((URLClassLoader)commonClassLoader).getURLs();
                StringBuffer sbuf = new StringBuffer();
                for (int i = 0; i < u.length; i++) {
                	sbuf.append(u[i].toExternalForm() + " ,");
				}
                
            	_log.log(Level.FINE, "Common classloader contents: [ " + sbuf.toString() + " ].") ;
            }
    	}
	}

        static protected final ServerLifecycle
    instantiateOneServerLifecycle(
        final ServerContext serverContext,
        final String classname )
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        //final TimingDelta delta = new TimingDelta();
        
        final Class c  = Class.forName( classname );
       
        ServerLifecycle item = null;

        // call a constructor that takes a ServerContext if it exists, since
        // some services might make efficient use of it during construction.
        // otherwise use a no-argument constructor
        try {
            final Constructor constructor = c.getConstructor( ServerContext.class );
            item   = (ServerLifecycle)constructor.newInstance( serverContext );
        }
        catch( Exception e ) {
            item = (ServerLifecycle)c.newInstance();
        }

        //System.out.println( "Instantiate " + classname + ": " + delta.elapsedMillis() );
        return item;
    }
    
    /** Index into String[] of the classname for a ServerLifecycle.*/
    private static final int CLASSNAME_IDX = 1;
    
        protected List<ServerLifecycle>
    instantiateRuntimeServices(
        final ServerContext serverContext,
        final String[][]    servicesByName ) throws ServerLifecycleException {
        final List<String>  classnames  = new ArrayList<String>();
        
        for (int i=0; i < servicesByName.length; i++) {
                final String[] serviceInfo = servicesByName[i];
                final String classname = serviceInfo[ CLASSNAME_IDX ];
                classnames.add( classname );
        }
        
        return instantiateRuntimeServices( serverContext, classnames );
    }
    

    private static final class Instantiator extends RunnableBase {
        private final String        mClassname;
        private final ServerContext mServerContext;
        private volatile ServerLifecycle    mService;
        
        public Instantiator(
            final ServerContext serverContext,
            final String        classname ) {
            mServerContext = serverContext;
            mClassname     = classname;
            mService       = null;
        }
        
        protected void doRun()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
        {
            mService = instantiateOneServerLifecycle(  mServerContext, mClassname );
        }
        public ServerLifecycle getService() {
            waitDoneThrow();
            return mService;
        }
    }
    
    /** Changing to RUN_IN_SEPARATE_THREAD achieves some efficiences at
      startup, but leaving it as 'RUN_IN_CURRENT_THREAD' until time allows
      testing. */
    private static final RunnableBase.HowToRun HOW_TO_RUN =
        RunnableBase.HowToRun.RUN_IN_CURRENT_THREAD;
    
    /** 
     * Go through the list of built-in runtime services and instantiate them.
     */
        protected List<ServerLifecycle>
    instantiateRuntimeServices(
        final ServerContext serverContext,
        final List<String>  classnames )
        throws ServerLifecycleException {
        final Instantiator[]    instantiators = new Instantiator[ classnames.size() ];
        
        // Instantiate service objects 
        int idx = 0;
        for ( final String classname : classnames ) {
            instantiators[idx]    = new Instantiator( serverContext, classname );
            instantiators[idx].submit( );
            ++idx;
        }
        
        // collect the resulting ServerLifecycle 
        final List<ServerLifecycle> serviceList = new ArrayList<ServerLifecycle>();
        for ( int i = 0; i < instantiators.length; ++i ) {
            serviceList.add( instantiators[i].getService() );
        }

        return serviceList;
    }

    /**
     * Server is starting up applications
     *
     * @param context ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     *
     * Note: Currently, with the J2EEServer code handles initialization and
     * applications startup in one place <code> J2EEServer.run() </code>.
     *
     * XXX: This separation needs to be worked on to make it cleaner.
     */
    public void onStartup() throws ServerLifecycleException
    {
        // startup the built-in runtime services
        for ( final ServerLifecycle service : services) {
	        try {
                service.onStartup(context);
            } catch (ServerLifecycleException e) {		  
                _logger.log(Level.SEVERE, "service.notstarted", 
                    new Object[] {service, e.toString()});
                throw e;
            }
        }
        // _REVISIT_: Mover AlertConfigurator to configure after onReady()
        AlertConfigurator.getAlertConfigurator( ).configure( );
    }

    /**
     * Server has complted loading the applications and is ready to 
     * serve requests.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onReady() throws ServerLifecycleException {

        try {
            // registers the resource manager as a listener for the 
            // resource deploy events
            AdminEventListenerRegistry.addResourceDeployEventListener(
                                              new ResourceManager(context));
            // registers listeners for JmsService and JmsHost changes.
            AdminEventListenerRegistry.addEventListener(JmsServiceEvent.eventType,
                                       new JmsServiceEventListener());
            AdminEventListenerRegistry.addEventListener(JmsHostEvent.eventType,
                                       new JmsHostEventListener());

            // Since onReady is called by a new thread, we have to again set
            // the common class loader as the thread context class loader
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        Thread.currentThread().setContextClassLoader
                            (commonClassLoader);
                        return null;
                    }
                }
            );            
            
            /*
             * com.sun.enterprise.server.startupHook is an internal
             * undocumented property that can be used by iplanet developers
             * to invoke some custom code just after server startup happens.
             * Currently it is used by the ORB to easily benchmark and profile
             * the local invocation path of remote interfaces in the EJB
             * without having to deal with servlets or client/server processes
             * 
             * If the property com.sun.enterprise.server.startupHook is
             * defined then "main" in that class is executed
             * Note: This should always be the last piece of code executed
             *       in this method
             */
            String startupHook = null;

            startupHook = System.getProperty(
                              "com.sun.enterprise.server.startupHook");
            if (startupHook != null) {
                try {
                    // Call the custom startup hook specified
                    Class hookClass = Class.forName(startupHook);
                    java.lang.reflect.Method hookMethod =
                        hookClass.getMethod("main",
                                    new Class[] { ServerContext.class });
                    hookMethod.invoke(null, new Object[] { context });
                }
                catch (Exception ex) {
                    if (Debug.enabled) {
                          _logger.log(Level.FINE, "server.exception", ex);
                    }
                }
            }

        } catch (Exception ee) {
            throw new ServerLifecycleException(ee);
        }


        // notify the built-in runtime services that we are ready
        for ( final ServerLifecycle service : services) {
	        try {
                service.onReady(context);
            } catch (ServerLifecycleException e) {
              _logger.log(Level.SEVERE, "service.notready", 
                      new Object[] {service, e.toString()});
            }
        }
    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() throws ServerLifecycleException 
    {
	
        // shutdown the built-in runtime services (in the reverse order).
        for (int i = services.size(); i > 0; i--) {
            final ServerLifecycle service = services.get(i-1);

	    _logger.log(Level.FINE,"service.shutdown", services.get(i-1));
            try {
                service.onShutdown();
            } catch (Exception e) {
            //do nothing since we want to continue shutting down other LFC
            //modules
                    _logger.log(Level.WARNING, "server.exception", e);	     
            }            
        }
    }

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onTermination() throws ServerLifecycleException 
    {
        //Kill all connections in the appserver's connection pools
	if (_logger.isLoggable( Level.FINE ) ) {
	    _logger.log(Level.FINE, "Killing all pools in the appserver");
	}
	try {
	    Switch.getSwitch().getPoolManager().killAllPools();
	} catch( Throwable t ) {
	    if (_logger.isLoggable( Level.FINE ) ) {
	        _logger.log(Level.FINE, "exception : " +t);
	    }
	}
 // terminate the built-in runtime services (in the reverse order)
        for (int i = services.size(); i > 0; i--) {
            final ServerLifecycle service = services.get(i-1);

	    _logger.log(Level.FINE,"service.shutdown",
					services.get(i-1));
	    try {
            service.onTermination();
	    } catch (Exception e) {
		//do nothing since we want to continue shutting down other LFC
		//modules
                _logger.log(Level.WARNING, "server.exception", e);	
	    }
        }

        services.clear();
    }

    /**
     * Get the server runtime context; returns a valid context only 
     * after initialization; a null otherwise.
     *
     * @return    a server runtime context or null if not initialized
     */
    public static ServerContext getServerContext() {
        return context;
    }

    // Called by PE Main
    protected void setServerContext(ServerContext context) {
        this.context = context;
    }

    private void printStartupInfo() {
        _logger.log(Level.INFO, "j2eerunner.printstartinfo", 	
                        new Object[] {
                           System.getProperty("java.vm.name"), 
                           System.getProperty("java.version"), 
                           System.getProperty("java.vm.vendor") });
    }
}
