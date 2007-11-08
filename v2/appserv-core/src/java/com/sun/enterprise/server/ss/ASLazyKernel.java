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
package com.sun.enterprise.server.ss;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.enterprise.admin.server.core.AdminChannelLifecycle;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.jms.JmsProviderLifecycle;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ss.provider.ASClientSocketFactory;
import com.sun.enterprise.server.ss.provider.ASServerSocketFactory;
import com.sun.enterprise.server.ss.provider.PortConflictException;
import com.sun.enterprise.server.ss.provider.ASPlainSocketImpl;
import com.sun.logging.LogDomains;

/**
 * This is a helper class to start an ASSocketService instance on 
 * various ports.
 * We also setup socket factories.
 */
public final class ASLazyKernel {
    private static final String AS_SELECTOR_PROVIDER = 
        "com.sun.enterprise.server.ss.provider.ASSelectorProvider";
    private static final String SELECTOR_PROVIDER_PROP =
        "java.nio.channels.spi.SelectorProvider";
    private static final String QUICK_STARTUP =
	"com.sun.enterprise.server.ss.ASQuickStartup";	
    private static final String DEFAULT_SELECTOR_PROVIDER =
	"sun.nio.ch.DefaultSelectorProvider";
    // The hotspot vm name is "Java HotSpot(TM) Client VM"
    // or "Java HotSpot(TM) Server VM". We just need to
    // see if the VM name contains "Server".
    private static final String SERVER_VM_FLAG = "Server";

    private static boolean areJDKClassesCreatable = false;
    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    private Config conf = null;
    //private JmsProviderLifecycle jmslc = null;
    private AdminChannelLifecycle adminlc = null;


    public boolean startASSocketServices(ServerContext context) {
	try {
	    if ( initializeASSocketService() ) {
		logFine("STARTING ASSocketService");
                ASSocketService.initialize();
                initializeAdminService(context);
		setupSocketListeners(context);    
		setupInitialServices(context);
		logger.log( Level.INFO, "socketservice.init_done");
		return true;
	    } 
	    logFine("Not using ASSocketService. Proceeding with normal startup");
	} catch ( PortConflictException pex ) {
            logger.log(Level.SEVERE, "socketservice.port_conflict",  
                       new Object[]{String.valueOf(pex.getConflictedPort())});
            exitServer(context, pex.getConflictedPort());
	} catch ( Exception ex ) {

            logger.log(Level.SEVERE, "socketservice.unknown_ex", ex);
            exitServer(context,0);

        }
	return false;
    }

    private void exitServer(ServerContext context, int port) {
        logFine("Stopped MQ");
                                   
        abortAdminService(context, port);
        logFine("Aborted admin service");
                                              
        // Abort the server.
        System.exit(1);
    }

    private void setupSocketListeners(ServerContext scontext) 
    throws PortConflictException, com.sun.enterprise.config.ConfigException {

	ConfigContext ctxt = scontext.getConfigContext();
	conf = ServerBeansFactory.getConfigBean( ctxt );
	
	// Start HTTP listener ports
	HttpService httpService = conf.getHttpService();
        int backlog = 0;
        try {
            backlog = Integer.parseInt(
            httpService.getConnectionPool().getMaxPendingCount());
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
	HttpListener[] httpListeners = httpService.getHttpListener();
	for ( int i=0; i<httpListeners.length; i++ ) {
	    if ( !httpListeners[i].isEnabled() )
		continue;
            startService(httpListeners[i], false, backlog);
	    logFine("Started ASSocketService for HTTP(S) ");
	}

	// Start IIOP listener ports
	IiopService iiopService = conf.getIiopService();
	IiopListener[] iiopListeners = iiopService.getIiopListener();
	for ( int i=0; i<iiopListeners.length; i++ ) {
	    if ( !iiopListeners[i].isEnabled() )
		continue;
            startService(iiopListeners[i]);
	    logFine("Started ASSocketService for IIOP(S)");
	}

	// Start JMX Connector ports
	AdminService adminService = conf.getAdminService();
	JmxConnector[] jmxConnectors = adminService.getJmxConnector();
	for ( int i=0; i<jmxConnectors.length; i++ ) {
	    if ( !jmxConnectors[i].isEnabled() )
		continue;
            startService(jmxConnectors[i], false, 0);
	    logFine("Started ASSocketService for JMX Connector ");
	}

        // Start MQ ports
        JmsService jmsService_ = conf.getJmsService();
        String defaultJmsHost = jmsService_.getDefaultJmsHost();
        JmsHost jmsHost_ = null;

        if (defaultJmsHost==null || defaultJmsHost.equals("")) {
            jmsHost_ = ServerBeansFactory.getJmsHostBean(ctxt);
        } else {
            jmsHost_ = jmsService_.getJmsHostByName(defaultJmsHost);
        }
        if (jmsService_.getType().equalsIgnoreCase("embedded") &&
            jmsHost_ != null && jmsHost_.isEnabled()) {
            ASSocketServiceConfig ssConfig = 
            new ASSocketServiceConfig(jmsHost_);
            ssConfig.setPortTag(ServerTags.PORT);
	    (new ASSocketService(ssConfig)).start();
	    logFine("Started ASSocketService for JMS ");
        }
    }

    private void startService(ConfigBean bean) throws PortConflictException {
        startService(bean,true,0);
    }

    private void startService(ConfigBean bean, boolean flag, int backlog) 
    throws PortConflictException {
        ASSocketServiceConfig ssConfig = 
        new ASSocketServiceConfig(bean);
        ssConfig.setAddressTag(ServerTags.ADDRESS);
        ssConfig.setPortTag(ServerTags.PORT);
        ssConfig.setStartSelector(flag);
        ssConfig.setBacklog(backlog);
	(new ASSocketService(ssConfig)).start();
    }

    private void setupInitialServices( ServerContext context ) 
                                                        throws Exception {
	startAdminService( context );
    }

    private void initializeAdminService (ServerContext context) throws Exception{
        createAdminChannelLifecycle(); 
        adminlc.onInitialization(context);
    }

    private void startAdminService(ServerContext context) throws Exception{
        logFine( "About to start AdminService ");
        createAdminChannelLifecycle(); 
        adminlc.onStartup(context);
        adminlc.onReady(context);
    }

    private void abortAdminService(ServerContext sc, int port) {
        try {
            createAdminChannelLifecycle(); 
            adminlc.onAbort(port);
            ASSocketService.waitForClientNotification();
            adminlc.onShutdown();
            adminlc.onTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAdminChannelLifecycle() {
        if (adminlc == null) {
            adminlc = new AdminChannelLifecycle();
        } 
    }

    private void stopLifecycle(ServerLifecycle lifecycle) {
        try {
            lifecycle.onShutdown();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "socketservice.stop_lc_ex", t);
        }
        try {
            lifecycle.onTermination();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "socketservice.stop_lc_ex", t);
        }
    }

    private void startLifecycle(ServerLifecycle lifecycle, ServerContext sc) 
                                    throws Exception {
        lifecycle.onInitialization(sc);
        lifecycle.onStartup(sc);
        lifecycle.onReady(sc);
    }
    
    public static boolean isQuickStartupEnabled()  {
	// enabled by default if system property absent
        String quickStartValue = System.getProperty(QUICK_STARTUP);
        String vmName = System.getProperty("java.vm.name", "");
        if (quickStartValue == null && vmName.contains(SERVER_VM_FLAG)){
            return false;
        }
	if ( quickStartValue == null || quickStartValue.equals("true") ) {
	    return areJDKClassesCreatable();
	}
	return false;	
    }
    
    private static void logFine( String msg ) {
        if (logger.isLoggable(Level.FINE)) {
	    logger.fine( msg );
	}
    }
    
    /**
     * This method does the following: <br>
     * <ul>
     *   <li>Checks if the class sun.nio.ch.DefaultSelectorProvider is present
     *       and its "create()" method is callable</li>
     *   <li>Sets the system property that defines our impl of a SelectorProvider</li>
     *   <li>Creates and sets the SocketImplFactories for ServerSocket and Socket</li>
     *</ul>
     *
     * If a problem occurs in any of the above steps, we simply assume that we
     * should use the default startup behavior and proceed
     */
    private boolean initializeASSocketService() throws IOException {
	if ( isQuickStartupEnabled()) {
	    // Set the NIO SelectorProvider impl property
	    System.setProperty(SELECTOR_PROVIDER_PROP, 
			       AS_SELECTOR_PROVIDER);

	    // Set the SocketImplFactories for ServerSocket and Socket
	    // Note: if either of these factories was already set (almost
	    // impossible), an exception will be thrown and server will abort.
	    ServerSocket.setSocketFactory( new ASServerSocketFactory() );
	    Socket.setSocketImplFactory( new ASClientSocketFactory() );

	    return true;
	}
        return false;
    }

    /**
     * The whole quick startup for the appserver hinges on whether we can 
     * wrap JDK socket layer. So check it.
     */
    private static boolean areJDKClassesCreatable() {
	if ( areJDKClassesCreatable ) {
	     return true;
	}
        try {
	    Class clazz = Class.forName(DEFAULT_SELECTOR_PROVIDER);
	        
	    java.lang.reflect.Method createMeth = clazz.getMethod("create", 
								new Class[] {});
	    createMeth.invoke( null, new Object[] {} ); 

            //Attempt to create the JDK socketimpl.
            new ASPlainSocketImpl();

	} catch( Exception e ) {
	    logFine( "Exception in areJDKClassesCreatable : " + e );
	    return false;
	}
       
	//Since we could invoke "create" on a DefaultSelectorProvider object 
	//successfully, everything is ok
	areJDKClassesCreatable = true;
	return true;
    }

}
