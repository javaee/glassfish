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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.jms;

import java.util.*;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleImpl;

import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.Constants;

import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.Switch;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.messaging.jmq.jmsspi.JMSAdmin;
import java.util.logging.*;
import com.sun.logging.LogDomains;

import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.util.SystemPropertyConstants;
/**
 * This class provides service for starting (currently only ping) the
 * configured JMS provider at J2EE server intialization and stopping
 * the configured JMS provider at J2EE server termination
 *
 */
public final class JmsProviderLifecycle extends ServerLifecycleImpl {
    private ConfigContext ctx = null;
    private static JmsService jmsService_ =null;
    //ROB: config changes
    private JmsHost jmsHost_ =null;

    private static JMSAdmin jmsAdmin_ = null; 
    private boolean onShutdown = false;

    private static boolean startedByMe_ = false;

    private boolean autoShutdown_ = true;

    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.JMS_LOGGER);
    private static boolean debug = true;

    private static final int NOTCHECKED = 1;
    private static final int SUCCESSFUL = 2;
    private static final int FAILED = 3;
    private static final int REMOTESTARTUP = 4;
    private static int startupStatus = NOTCHECKED;

    private static String instanceName = null;
    private static String iMQBin = null;

    private static String exception = null;

    private static String url = null;
    
    //Flag to set AS9 to use MQSPI. This flag could be used by users
    //to revert to AS8.x behavior of using the MQ SPI for AS/MQ lifecycle 
    //integration. Setting a system variable to this value
    //and to true, reverts to AS8.x behavior
    private static final String AS_INTEGRATION_VIA_MQ_SPI 
                    = "com.sun.enterprise.jms.ASMQSPIIntegration";
    
    private static boolean useMQRAForBrokerLifecycle = true;
    
    //determine if MQ SPI needs to be used.
    static {
        try {
            String s = System.getProperty(AS_INTEGRATION_VIA_MQ_SPI);
            if (s != null) {
                useMQRAForBrokerLifecycle = !((new Boolean(s)).booleanValue());
            }
            String status = (useMQRAForBrokerLifecycle ? "Using MQ RA for Broker lifecycle control" : 
                "Using MQ SPI for Broker Lifecycle control");
            _logger.log(Level.INFO, status);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "Exception while reading " 
                    + AS_INTEGRATION_VIA_MQ_SPI +  "property" + ex.getMessage());
            _logger.log(Level.INFO, "Using MQ RA for Broker Lifecycle control");
            _logger.log(Level.FINE, ex.getMessage());
        }
    }
            

    /**
     * THIS METHOD MUST BE CALLED BEFORE any of the IASJmsUtil, IASJmsConfig
     * method get called.
     *
     * Server is initializing subsystems and setting up the runtime
     * environment.
     *
     * Prepare for the beginning of active use of the public methods
     * of this subsystem. This method is called before any of the
     * public methods of this subsystem are utilized.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already
     * been started
     * @exception ServerLifecycleException if this subsystem detects a
     * fatal error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext sc)
        throws ServerLifecycleException {
        if (!shouldUseMQRAForLifecycleControl()) {

            if (jmsService_ != null) {
                // Dont allow duplicate initializations.
                return;
            }

            try {
                jmsAdmin_ = null;
                ctx = sc.getConfigContext();

                //ROB: config changes
                //JavaConfig jc = ServerBeansFactory.getServerBean(
                //    ctx).getJavaConfig();
                JavaConfig jc = ServerBeansFactory.getJavaConfigBean(ctx);

                String java_home = jc.getJavaHome();

                //ROB: config changes
                //jmsService_ = ServerBeansFactory.getServerBean(ctx).getJmsService();
                jmsService_ = ServerBeansFactory.getJmsServiceBean(ctx);
                String defaultJmsHost = jmsService_.getDefaultJmsHost();

                if (defaultJmsHost==null || defaultJmsHost.equals("")) {
                    jmsHost_ = ServerBeansFactory.getJmsHostBean(ctx);
                } else {
                    jmsHost_ = jmsService_.getJmsHostByName(defaultJmsHost);
                }

    //ROB: config changes
    /*
                if (jmsService_ != null && jmsService_.isEnabled()) {
                    String portStr = jmsService_.getPort();
                    String username = jmsService_.getAdminUserName();
                    String password = jmsService_.getAdminPassword();
    */
                String type = jmsService_.getType();
                if (!(type.equals("LOCAL"))) {
                    startupStatus = REMOTESTARTUP;
                    _logger.log(Level.INFO, "jms.broker_notlocal", type);
                    return;
                }

                if (jmsHost_ != null && jmsHost_.isEnabled()) {
                    String portStr = jmsHost_.getPort();
                    String username = jmsHost_.getAdminUserName();
                    String password = jmsHost_.getAdminPassword();

                    Vector v = new Vector();
                    if (java_home != null) {
                        v.add("-javahome");
                        v.add(java_home);
                    }

                    String mqInstanceDir = 
                           sc.getInstanceEnvironment().getInstancesRoot() +
                           java.io.File.separator + IASJmsUtil.MQ_DIR_NAME;

                    // If the directory doesnt exist, create it.
                    // It is necessary for windows.
                    java.io.File instanceDir = new java.io.File(mqInstanceDir);
                    if (!(instanceDir.exists() && instanceDir.isDirectory())) {
                        instanceDir.mkdirs();
                    }

                    v.add("-varhome");
                    v.add(mqInstanceDir);


                    String tmpstr = jmsService_.getStartArgs();
                    if (tmpstr != null) {
                        StringTokenizer st = new StringTokenizer(tmpstr, " ");
                        while (st.hasMoreTokens()) {
                            String t = st.nextToken();
                            v.add(t);
                        }
                    }

                    String[] startArgs = (String []) v.toArray(new String[0]);

                    // Extract the information from the optional properties.
                    // Valid property names : "auto-shutdown"
                    ElementProperty[] jmsProperties =
                        jmsService_.getElementProperty();

                    if (jmsProperties != null) {
                        for (int ii=0; ii < jmsProperties.length; ii++) {
                            ElementProperty p = jmsProperties[ii];
                            String name = p.getName();

                            if (name.equals("auto-shutdown"))
                                autoShutdown_ =
                                    Boolean.valueOf(p.getValue()).booleanValue();
                        }
                    }

                    // If the property was not specified, then look for the 
                    // imqRoot as defined by the com.sun.aas.imqRoot property   
                    iMQBin = java.lang.System.getProperty(SystemPropertyConstants.IMQ_BIN_PROPERTY);

                    // Finally if all else fails (though this should never happen)
                    // look for IMQ relative to the installation directory
                    if (iMQBin == null) {
                        String IMQ_INSTALL_SUBDIR = java.io.File.separator + 
                            ".." + java.io.File.separator + ".." +
                            java.io.File.separator + "imq" +
                            java.io.File.separator + "bin";
                        iMQBin = sc.getInstallRoot() + IMQ_INSTALL_SUBDIR;
                    }

                    //
                    // Calculate the imq broker executable path.
                    //
                    String asInstance = sc.getInstanceName();
                    String domainName = ServerManager.instance().getDomainName();
                    instanceName = IASJmsUtil.getBrokerInstanceName(
                        domainName, asInstance, jmsService_);

                    String localhost = "127.0.0.1";
                    url = localhost + ((portStr == null) ?
                        "" : ":" + portStr);
                    if (username == null) {
                        jmsAdmin_ =
                            IASJmsUtil.getJMSAdminFactory().getJMSAdmin(url);
                    }
                    else {
                        jmsAdmin_ =
                            IASJmsUtil.getJMSAdminFactory().getJMSAdmin(url,
                                username, password);
                    }

                    // First ping the provider to see if it is already
                    // running.
                    boolean running;
                    try {
                        jmsAdmin_.pingProvider();
                        running = true;
                    }
                    catch (Exception e) {
                        running = false;
                    }

                    if (running) {
                        _logger.fine(
                            "Broker is already running. Trying to attach.");
                        String s = null;
                        try {
                            s = attachToJmsProvider();
                        }
                        catch (Exception e) {
                            _logger.log(Level.INFO, "jms.broker_attach_failed");
                            _logger.log(Level.INFO,
                                "jms.broker_log_location", instanceName);
                            throw new ServerLifecycleException(e.getMessage(), e);
                        }
                        if (s.equals(instanceName)) {
                            // An iMQ broker instance with the same name
                            // is already running.  Since the instance
                            // names match, it is probably a broker
                            // process left behind by the application
                            // server. Treat it as if it was started by
                            // this application server process...
                            startedByMe_ = true;
                            _logger.log(Level.INFO, "jms.broker_found",
                                instanceName);
                        }
                        else {
                            startedByMe_ = false;
                            Object[] params = {s, portStr};
                            _logger.log(Level.SEVERE, "jms.broker_already_up",
                                params);
                            throw new ServerLifecycleException(
                                "JmsProviderLifecycle error.");
                        }
                    }
                    else {
                        _logger.fine("Starting JMS broker : imq-home=" + iMQBin +
                            ", stargArgs=" + tmpstr +
                            ", instanceName=" + instanceName);

                        try {
                            jmsAdmin_.startProvider(iMQBin,
                                startArgs, instanceName);
                        }
                        catch (Exception e) {
                            _logger.log(Level.INFO, "jms.broker_exec_failed");
                            _logger.log(Level.INFO,
                                "jms.broker_log_location", instanceName);
                            throw new ServerLifecycleException(e.getMessage(), e);
                        }


                    }
                }
            }
            catch (Exception e) {
                jmsService_ = null;
                _logger.log(Level.SEVERE, "jms.broker_startup_failed");

                // The exception will be logged at the outer level...

                throw new ServerLifecycleException(e.getMessage(), e);
            }
        }
    }

    private String attachToJmsProvider() throws Exception {
        jmsAdmin_.connectToProvider();
        String name = jmsAdmin_.getProviderInstanceName();
        jmsAdmin_.disconnectFromProvider();
        return name;
    }

    private static void waitForJmsProvider
                     (long initTimeout) throws Exception {

        boolean ready = false;

        // It is debatable where to put startTime. If we set the
        // time right after startProvider as startTime, we may land into
        // some issues. In a single CPU system, the startTime
        // may not reflect the correct meaning. If we consider startTime
        // as cpuTime, here it is assumed that there is no time spend
        // for JMS startup until now. Actually CPU should have spend time. 
        long startTime = java.lang.System.currentTimeMillis();

        while (true) {
            try {
                jmsAdmin_.pingProvider();
                ready = true;
                break;
            }
            catch (Exception e) {}

            if (java.lang.System.currentTimeMillis() - startTime >=
                initTimeout) {
                break;
            }

            try {
                Thread.sleep(2000);
            }
            catch (Exception e) {}
        }

        // If provider is not ready, call pingProvider again
        // to generate the exception...
        if (!ready)
            jmsAdmin_.pingProvider();

    }

    //AS7.0 has one server instance per JVM
    public static JMSAdmin getJMSAdmin() {
        return jmsAdmin_;
    }

    public boolean isNOJMS() {
        return false;
    }

    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc)
        throws ServerLifecycleException {
        //Start ActiveJMSRA
        try {
            String module = ConnectorConstants.DEFAULT_JMS_ADAPTER;
            String loc = Switch.getSwitch().getResourceInstaller().
                         getSystemModuleLocation(module);
            ConnectorRuntime.getRuntime().createActiveResourceAdapter(
                         loc, module);
        } catch (ConnectorRuntimeException e) {
            e.printStackTrace();
            _logger.log(Level.INFO, "Failed to start JMS RA");
            throw new ServerLifecycleException("Failed to start JMS RA", e);
        }
       
    }

    /**
     * Check Jms Provider and make sure that it is started.
     * When resource adapter starts up, it will also doublecheck 
     * using this static method.
     */
    public static void checkProviderStartup() 
        throws ServerLifecycleException {
        if (shouldUseMQRAForLifecycleControl()) {
            return;
        } 

	switch (startupStatus) {
	    case NOTCHECKED    : break;
	    case SUCCESSFUL    : return;
	    case FAILED        : 
	         throw new ServerLifecycleException
		 ("MQ startup failed :" + exception);
	    case REMOTESTARTUP : return;
	}

	String iMQInstance =  System.getProperty(Constants.INSTALL_ROOT) +
	                      java.io.File.separator + "imq" +
	                      java.io.File.separator + "var" +
	                      java.io.File.separator + "instances"; 

        String initTimeoutStr = null;
        long initTimeout = 30 * 1000;

        initTimeoutStr = jmsService_.getInitTimeoutInSeconds();

        if (initTimeoutStr != null)
            initTimeout = Integer.parseInt(initTimeoutStr) * 1000;

        Exception excp = null;
        try {
             waitForJmsProvider(initTimeout);
	     startupStatus = SUCCESSFUL;
        }
        catch (javax.jms.JMSSecurityException e) {
	    excp = e;

	    // Provider is up and we have a wrong username and password 
	    // configured. We should shut this down. Recreate the admin
	    // with defult user and password.
            startedByMe_ = true;
	    try {
                jmsAdmin_ =
                    IASJmsUtil.getJMSAdminFactory().getJMSAdmin(url,
		    IASJmsUtil.DEFAULT_USER,
		    IASJmsUtil.DEFAULT_PASSWORD);
	    } catch (Exception ex) {
	    }
	}
        catch (Exception e1) {
	    excp = e1;
        }

	if (excp != null) {
             startupStatus = FAILED;
             _logger.log(Level.INFO, "jms.broker_ping_failed",
                         Long.toString(initTimeout));
             _logger.log(Level.INFO,
                    "jms.broker_instance_dir", iMQInstance);
             _logger.log(Level.INFO,
                    "jms.broker_log_location",instanceName);
	     exception = excp.getMessage();
             throw new ServerLifecycleException(excp.getMessage(), excp);
	}

        startedByMe_ = true;

        Object[] params = {instanceName, iMQBin};
            _logger.log(Level.INFO, "jms.broker_started", params);

    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onShutdown()
        throws ServerLifecycleException {
        checkProviderStartup();
        onShutdown = true;
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
    public void onTermination()
        throws ServerLifecycleException {
        onShutdown = true;
        //need not do anything if lifecycke managed by the RA.
        if (!shouldUseMQRAForLifecycleControl()) {

            try {
                if (startupStatus == REMOTESTARTUP ||
                    autoShutdown_ == false ||
                    jmsService_ == null ||
                    jmsService_.isEnabled() == false)
                    return;

                if (jmsAdmin_ == null || startedByMe_ == false)
                    return;

                _logger.log(Level.INFO, "jms.broker_shutting_down");
                jmsAdmin_.connectToProvider();
            }
            catch (Exception e) {
                throw new ServerLifecycleException(e.getMessage(), e);
            }

            try {
                jmsAdmin_.shutdownProvider();
                _logger.log(Level.INFO, "jms.broker_shutdown_complete");
            }
            catch (Exception e) {} // Ignore this exception.
        }
    }
    
    public static boolean shouldUseMQRAForLifecycleControl() {
        return JmsProviderLifecycle.useMQRAForBrokerLifecycle;
    }

}
