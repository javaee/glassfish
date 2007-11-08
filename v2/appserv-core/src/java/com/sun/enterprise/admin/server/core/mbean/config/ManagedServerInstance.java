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

package com.sun.enterprise.admin.server.core.mbean.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.admin.common.exception.AFException;
import com.sun.enterprise.admin.common.exception.AFJDBCResourceException;
import com.sun.enterprise.admin.common.exception.AFResourceException;
import com.sun.enterprise.admin.common.exception.AFRuntimeStoreException;
import com.sun.enterprise.admin.common.exception.ControlException;
import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.exception.IllegalStateException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.exception.PortInUseException;
import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.InitConfFileBean;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.RequestID;
import com.sun.enterprise.admin.common.ServerInstanceStatus;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.ApplicationDeployEvent;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.ConfigChangeEvent;
import com.sun.enterprise.admin.event.EventBuilder;
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.EventStack;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.event.ResourceDeployEvent;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.server.core.ManualChangeManager;
import com.sun.enterprise.admin.server.core.mbean.config.Domain2ServerTransformer;
import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.util.Assert;
import com.sun.enterprise.admin.util.ExceptionUtil;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.util.StringValidator;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.JmsRaMapping;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.InstanceDefinition;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.net.NetUtils;


/**
    The MBean that represents a managed Server Instance for iAS SE. In
    other words it represents the <strong> management interface </strong> of
    a Server Instance.
    <p>
    The MBeanServer will have as many instances of this MBean as there are
    Server Instances <strong> created </strong> in Admin Server. Whenever a
    Server Instance is created, MBeanServer registers an instance of
    this MBean. Note that some of the MBeans may represent Server Instances
    that are not running. Since there is an MBean for a Server Instance that
    is not running, one can configure such a Server Instance and then start it.
    <p>
    ObjectName of this MBean is ias:type=ServerInstance, name=<instanceName>
*/

public class ManagedServerInstance  extends ConfigMBeanBase implements ConfigAttributeName.Server
{
    private HostAndPort mHostAndPort    = null;
    private int		mStartMode      = AdminConstants.kNonDebugMode;
    private String	mInstanceName   = null;
    private boolean	mAutoStart      = false;

    public static final Logger sLogger =
            Logger.getLogger(AdminConstants.kLoggerName);

    private static final int CONFIG_CHANGED             = 7;

    /* New for 8.0  */
    /* This flag is defined, so that we can get rid of the transformation
     * by simply changing its value to false. Also, an attempt is made to keep
     * this mbean independent of jaxp api in import. */
    static final boolean PORT_DOMAIN_TO_SERVER = false; /* package access */
    /* New for 8.0  */

    /**
     * MAPLIST array defines mapping between "external" name and its location in XML relatively base node
     */
    private static final String[][] MAPLIST  =
    {
/*
        {kName             , ATTRIBUTE + ServerTags.NAME},
        // {kLocale           , ATTRIBUTE + ServerTags.LOCALE},
        {kLogRoot          , ATTRIBUTE + ServerTags.LOG_ROOT},
        {kSessionStore     , ATTRIBUTE + ServerTags.SESSION_STORE},
        {kApplicationRoot  , ATTRIBUTE + ServerTags.APPLICATION_ROOT},
        //the following attrs are from application sub element
        {kAppDynamicReloadEnabled  , ServerXPathHelper.XPATH_CONFIG + 
				     ServerXPathHelper.XPATH_SEPARATOR +
                                     ServerTags.ADMIN_SERVICE + ServerXPathHelper.XPATH_SEPARATOR +
                                     ServerTags.DAS_CONFIG + ServerXPathHelper.XPATH_SEPARATOR + 
                                     ATTRIBUTE + ServerTags.DYNAMIC_RELOAD_ENABLED},
        {kAppReloadPollInterval,     ServerXPathHelper.XPATH_CONFIG + 
                                     ServerXPathHelper.XPATH_SEPARATOR +
                                     ServerTags.ADMIN_SERVICE + ServerXPathHelper.XPATH_SEPARATOR +
                                     ServerTags.DAS_CONFIG + ServerXPathHelper.XPATH_SEPARATOR + 
                                     ATTRIBUTE + ServerTags.DYNAMIC_RELOAD_POLL_INTERVAL_IN_SECONDS},
 */
    };
    /**
     * ATTRIBUTES array specifies attributes descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   ATTRIBUTES  =
    {
/*
 kName             + ", String,        R" ,
    //    kLocale           + ", String,        RW" ,
        kLogRoot          + ", String,        RW" ,
        kSessionStore     + ", String,        RW" ,
        kApplicationRoot  + ", String,        RW" ,
        //the following attrs are from application sub element
        kAppDynamicReloadEnabled  + ", boolean,        RW" ,
        kAppReloadPollInterval    + ", int,            RW" ,
*/
 };

    /**
     * OPERATIONS array specifies operations descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   OPERATIONS  =
    {
      "start(),  ACTION, ManagedServerInstance.start1.operation", //we are using non-generic property names here for providing separate definitions to each overloading method
      "start(String[] passwords),  ACTION, ManagedServerInstance.start1.operation",
      "start(boolean debug, String[] passwords), ACTION, ManagedServerInstance.start2.operation",
      "startInDebugMode(), ACTION",
      "startInDebugMode(String[] passwords), ACTION",
      "restart(),  ACTION",
      "stop(int timeoutSeconds), ACTION",
      "getDeployedJ2EEApplications(), INFO ",
      "getDeployedJ2EEModules(),      INFO ",
      "getEnabledJ2EEApplications(),  INFO ",
      "getDisabledJ2EEApplications(), INFO ",
      "getEnabledJ2EEModules(),       INFO ",
      "getDisabledJ2EEModules(),      INFO ",
//      "registerDataSource(String dataSourceXMLFileName), ACTION ",
//      "removeDataSource(String dataSourceName),  ACTION ",
//      "getDataSourceNames(), INFO ",
      "setHttpPort(int port ),    ACTION ",
      "getHostAndPort(),  INFO ",
      "getHttpPort(),            INFO ",
      "getStatus(),              INFO ",
      "reconfigure(),            ACTION ",
      "startMonitor(),           ACTION ",
      "getMonitorableComponentNames(), INFO ",
      "stopMonitor(),            ACTION ",
      "postRegister(Boolean registrationDone), ACTION ",
      "preRegister(javax.management.MBeanServer server, javax.management.ObjectName name), ACTION ",
      "postDeregister(),         ACTION ",
      "preDeregister(),          ACTION ",
//      "getCertNicknames(),       INFO ",
      "getInstanceRoot(),        INFO ",
      "getSecurityPasswordTokens(),       INFO ",
      "isRestartNeeded(),        INFO",
      "getUserNames(),        INFO",
      "getGroupNames(),        INFO",
      "getUserGroupNames(String userName),        INFO",
      "addUser(String userName, String password, String[] groupList),       INFO ",
      "removeUser(String userName),       INFO ",
      "updateUser(String userName, String password, String[] groupList),       INFO ",
    };

    /**
        Default constructor sets MBean description tables
    */
    public ManagedServerInstance() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    public ManagedServerInstance(String instanceName, HostAndPort hostPort,
		boolean autoStart) throws PortInUseException,MBeanConfigException
    {
        this(instanceName, hostPort, autoStart, null);
    }

    public ManagedServerInstance(String instanceName, HostAndPort hostPort,
		boolean autoStart, AdminContext adminContext)
        throws PortInUseException,MBeanConfigException
    {
        this(); //set description tables
        setAdminContext(adminContext);
        initialize(ObjectNames.kServerInstance, new String[]{instanceName});


        if (instanceName == null || hostPort == null)
        {
            throw new IllegalArgumentException();
        }
        /**
         * This check is probably not required here. Having this check
         * causes unnecessary problems when there is a running instance &
         * the MBeanServer creates a new MBean for that instance (lazy
         * MBean loading). - Ramakanth 01/25/2002 11:17pm
         */
        mInstanceName   = instanceName;
        mHostAndPort    = hostPort;
        mAutoStart      = autoStart;
    }

    /**
        A method to start this Server Instance asynchronously. Note that starting
        a Server Instance is thought as a long running operation and hence
        could be tracked for progress. Thus the method returns a RequestID that
        can later be queried for. The MBean that represents this Server Instance
        is already present in the MBeanServer.
        <p>
        By default the Server Instance will be started in non-debug mode.

    */
    public RequestID start() throws ControlException
    {
        return ( this.start(false, null) );
    }

    public RequestID start(String[] passwords) throws ControlException
    {
        return ( this.start(false, passwords) );
    }

    public RequestID start(boolean debug, String[] passwords) throws ControlException
    {
        try
        {
            if (getInstanceStatusCode() == Status.kInstanceRunningCode)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.cannot_start_already_running" );
                throw new IllegalStateException( msg );
            }
            if ( debug )
            {
                mStartMode = AdminConstants.kDebugMode;
            }
            else
            {
                if (isDebug())
                {
                    setDebug(false);
                    //What's wrong in leaving the debug options?
                    setDebugOptions(null);
                    super.getConfigContext().flush();
                    applyConfigChanges();
                }
            }
            /* calling start script */
            if(debug)
            {
                String strPort = "?";
                try{
                    strPort = String.valueOf(getDebugPort());
                }
                catch (Exception e)
                {
                }
                sLogger.log(Level.INFO, "mbean.start_instance_debug", 
                               new Object[]{mInstanceName, strPort});
            }
            else
            {
                sLogger.log(Level.INFO, "mbean.start_instance", mInstanceName);
            }

            InstanceDefinition instance = new InstanceDefinition(mInstanceName,
                    mHostAndPort.getPort());
            ServerManager.instance().startServerInstance(instance,passwords);
            /* calling start script*/

	    // check if the instance has started
	    //
        sLogger.log(Level.INFO, "mbean.check_start_instance", mInstanceName);
	    long timeoutMillis = 240000;
	    long sleepTime = 2000;
	    long timeBefore = java.lang.System.currentTimeMillis();
	    long timeAfter = java.lang.System.currentTimeMillis();
	    boolean timeoutReached = false;
	    while ((! timeoutReached) &&
		   (! (getInstanceStatusCode() == Status.kInstanceRunningCode))) {
		Thread.sleep(sleepTime);
	    	timeAfter = java.lang.System.currentTimeMillis();
		timeoutReached = (timeAfter - timeBefore) >= timeoutMillis;
	    }
	    if (timeoutReached) {
                sLogger.log(Level.INFO, "mbean.start_instance_timeout", mInstanceName);
		try {
			Long tLong = new Long(timeoutMillis/1000);
			stop(tLong.intValue());
		} catch (ControlException cex) {
				String msg = localStrings.getString( "admin.server.core.mbean.config.timeout_reached_server_stopping_exception", mInstanceName );
            	//throw new ControlException( msg );
		}
				String msg = localStrings.getString( "admin.server.core.mbean.config.timeout_reached_server_starting_exception", mInstanceName );
            	throw new ControlException( msg );
	    } else {
		sLogger.log(Level.INFO, "mbean.start_instance_success", mInstanceName);
	    }
	    //
        }
        catch (Exception e)
        {
            sLogger.log(Level.SEVERE,
                    "mbean.start_instance_failed", mInstanceName);
            sLogger.log(Level.SEVERE,
                    "mbean.start_instance_failed_details", e);
            throw new ControlException(e.getMessage());
        }
        return ( null );
    }

    /**
     */
    public int startInDebugMode(String[] passwords) throws ControlException
    {
        int port = -1;
        try
        {
            boolean isChanged = false;
            if (!isDebug())
            {
                setDebug(true);
                isChanged = true;
            }
            port = getDebugPort();
            if (port == -1)
            {
                port = NetUtils.getFreePort();
                if (port == 0)
                {
                    sLogger.log(Level.SEVERE, "general.free_port_failed");
					String msg = localStrings.getString( "admin.server.core.mbean.config.no_free_port" );
                    throw new Exception( msg );
                }
                setDebugPort(port);
                isChanged = true;
            }
            if (isChanged)
            {
                super.getConfigContext().flush();
                applyConfigChanges();
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.set_debug_failed", e);
            throw new ControlException(e.getLocalizedMessage());
        }
        start(true, passwords);
        return port;
    }
    public int startInDebugMode() throws ControlException
    {
        return this.startInDebugMode(null);
    }

    /**
     * returns true if security=on for this instance (in "live" init.conf)
     */
    public String[] getSecurityPasswordTokens() throws ControlException
    {
        try
        {
            //first testing existance of password file
            InstanceEnvironment env = new InstanceEnvironment(mInstanceName);
            String pwdFileName = env.getSecurityPasswordsFilePath();
            File pwdFile = new File(pwdFileName);
            if(pwdFile.exists())
                return null;
            //now test the security attribute in init.conf file in "live" directory
            InitConfFileBean conf = new InitConfFileBean();
            conf.initialise(mInstanceName, false);
            String security = conf.get_mag_var(InitConfFileBean.INITCONF_SECURITY_ATTRIBUTE);
            if(security.equalsIgnoreCase(InitConfFileBean.INITCONF_VALUE_ON))
            {
                InstanceDefinition instance = new InstanceDefinition(mInstanceName,
                        mHostAndPort.getPort());
                return ServerManager.instance().getSecurityTokensForInstance(instance);
                //FIXME: should be replaced by receiving actual tokens from security.db
                // this temp solution is just for starting UI work
                //return new String[]{"internal"};
            }
            return null;
        }
        catch(Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.security_check_failed", e);
            throw new ControlException(e.getMessage());
        }
    }

    private static final int TIME_OUT_SECONDS = 120;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ManagedServerInstance.class );

    /**
        Restarts a running instance.
    */
    public RequestID restart() throws ControlException
    {
        RequestID reqId = null;
        
        String adminId          = AdminService.getAdminService().getInstanceName();
/*        if (mInstanceName.equals(adminId))
        {
            String msg = localStrings.getString( "admin.server.core.mbean.config.cannot_restart_admin_instance" );
            throw new IllegalStateException( msg );
        }
*/
        final RMIClient rmiClient = AdminChannel.getRMIClient(mInstanceName);

        if (rmiClient.getInstanceStatusCode() != Status.kInstanceRunningCode)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.instance_not_running_cannot_restart" );
            throw new IllegalStateException( msg );
        }

        try
        {
            /* calling restart script */
            sLogger.log(Level.INFO, "mbean.restart_instance", mInstanceName);
            InstanceDefinition instance = new InstanceDefinition(
                                               mInstanceName,
                                               getHostAndPort().getPort());
            ServerManager.instance().restartServerInstance(instance);
            /*
             * There is certain time lapse between executing the subprocess
             * and updating the instance stub file on the disk. The following
             * check compensates for this time lapse. Moreover this check
             * seems to be more definitive than waiting for a random interval,
             * say 5 seconds.
             */
            int i = 0; int MAX_TIMES = 3;
            while ((rmiClient.getInstanceStatusCode() !=
                    Status.kInstanceNotRunningCode) && (i < MAX_TIMES))
            {
                Thread.currentThread().sleep(5000);
                i++;
            }
        }
        catch (Exception e)
        {
            throw new ControlException(e.getMessage());
        }
        ManagedInstanceTimer tt = new ManagedInstanceTimer(
            TIME_OUT_SECONDS, 0,
            new TimerCallback()
            {
                public boolean check() throws Exception
                {
                    return (rmiClient.getInstanceStatusCode() ==
                            Status.kInstanceRunningCode);
                }
            } );
        tt.run(); //synchronous

        if (rmiClient.getInstanceStatusCode() != Status.kInstanceRunningCode)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.timeout_while_restarting_server" );
            try
            {
                stop(TIME_OUT_SECONDS);
            }
            catch (ControlException ce)
            {
                msg += localStrings.getString( "admin.server.core.mbean.config.server_stop_exception" );
            }
            throw new ControlException(msg + mInstanceName);
        }

        return reqId;
    }

    private static final class ManagedInstanceTimer implements Runnable
    {
        private final   int             timeOutSeconds;
        private final   TimerCallback   callBack;
        private final   int             startAfterSeconds;
        private boolean                 timeOutReached;
        private long                    startTime;

        ManagedInstanceTimer(int timeOutSeconds,
                             int startAfterSeconds,
                             TimerCallback callBack)
        {
            this.timeOutSeconds     = timeOutSeconds;
            this.startAfterSeconds  = startAfterSeconds;
            this.callBack           = callBack;
            this.timeOutReached     = false;
        }

        public void run()
        {
            startTime = java.lang.System.currentTimeMillis();
            try
            {
                Thread.currentThread().sleep(startAfterSeconds * 1000);
                while (!timeOutReached() && !callBack.check())
                {
                    try
                    {
                        Thread.currentThread().sleep(1000);
                        computeTimeOut();
                    }
                    catch (InterruptedException ie)
                    {
                        sLogger.warning(ie.toString());
                        timeOutReached = true;
                    }
                }
            }
            catch (Exception e)
            {
                sLogger.warning(e.toString());
                timeOutReached = true;
            }
        }

        private boolean timeOutReached()
        {
            return timeOutReached;
        }

        private void computeTimeOut()
        {
            long currentTime = java.lang.System.currentTimeMillis();
            timeOutReached =
                ((currentTime - startTime) >= (timeOutSeconds * 1000));
        }
    }

    interface TimerCallback
    {
        boolean check() throws Exception;
    }

    public void stop(int timeoutSeconds) throws ControlException
    {
        String adminId          = AdminService.getAdminService().getInstanceName();
        if (mInstanceName.equals(adminId))
        {
            sLogger.log(Level.INFO, "mbean.shutdown_started");
            new Thread(new ShutdownThread(adminId)).start();
            return;
        }
        if (getInstanceStatusCode() == Status.kInstanceNotRunningCode)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.not_running_cannot_stop" );
            throw new IllegalStateException( msg );
        }

        /* calling stop script */
        try
        {
            sLogger.log(Level.INFO, "mbean.stop_instance", mInstanceName);
            InstanceDefinition instance = new InstanceDefinition(mInstanceName,
                    mHostAndPort.getPort());
            ServerManager.instance().stopServerInstance(instance);

	    // check if the instance has stopped
	    //
            sLogger.log(Level.INFO, "mbean.check_stop_instance", mInstanceName);
	    long timeoutMillis = (timeoutSeconds * 1000);
	    long sleepTime = 2000;
	    long timeBefore = java.lang.System.currentTimeMillis();
	    long timeAfter = java.lang.System.currentTimeMillis();
	    boolean timeoutReached = false;
            while ((! timeoutReached) &&
	           (! (getInstanceStatusCode() == Status.kInstanceNotRunningCode))) {
		Thread.sleep(sleepTime);
	    	timeAfter = java.lang.System.currentTimeMillis();
		timeoutReached = (timeAfter - timeBefore) >= timeoutMillis;
	    }
	    if (timeoutReached) {
                sLogger.log(Level.INFO, "mbean.stop_instance_timeout", mInstanceName);
				String msg = localStrings.getString( "admin.server.core.mbean.config.timeout_while_stopping_server", mInstanceName );
            	throw new ControlException( msg );
	    } else {
		sLogger.log(Level.INFO, "mbean.stop_instance_success", mInstanceName);
	    }
	    //
        }
        catch (Exception e)
        {
            sLogger.log(Level.SEVERE,
                    "mbean.stop_instance_failed", mInstanceName);
            throw new ControlException(e.getMessage());
        }
        /* calling stop script*/
    }
    
    /**
        Returns the names of deployed applications to this server instance.
    */
    public String[] getDeployedJ2EEApplications() throws ServerInstanceException
    {
        String[] apps = new String[0];
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications appsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext, ServerXPathHelper.XPATH_APPLICATIONS);
            J2eeApplication[] j2eeApps = appsConfigBean.getJ2eeApplication();
            if (j2eeApps != null)
            {
                apps = new String[j2eeApps.length];
                for(int i=0; i<j2eeApps.length; i++)
                {
                    apps[i] = j2eeApps[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return ( apps );
    }

    /**
    */
    public String[] getDeployedJ2EEModules() throws ServerInstanceException
    {
		String msg = localStrings.getString( "admin.server.core.mbean.config.not_supported_yet" );
        throw new UnsupportedOperationException( msg );
    }

    /**
     * Returns an array of standalone ejb module names that are deployed to
     * this server instance.
     * @return an array of deployed ejb module names.  Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public String[] getDeployedEJBModules() throws ServerInstanceException
    {
        String[] ejbModules = new String[0];
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications appsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext, ServerXPathHelper.XPATH_APPLICATIONS);
            EjbModule[] modules = appsConfigBean.getEjbModule();
            if (modules != null)
            {
                ejbModules = new String[modules.length];
                for(int i=0; i<modules.length; i++)
                {
                    ejbModules[i] = modules[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return ejbModules;
    }

    /**
     * Returns an array of standalone war module names that are deployed to
     * this server instance.
     * @return an array of deployed web module names. Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public String[] getDeployedWebModules() throws ServerInstanceException
    {
        String[] webModules = new String[0];
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications appsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext, ServerXPathHelper.XPATH_APPLICATIONS);
            WebModule[] modules = appsConfigBean.getWebModule();
            if (modules != null)
            {
                webModules = new String[modules.length];
                for(int i=0; i<modules.length; i++)
                {
                    webModules[i] = modules[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return webModules;
    }

    /**
     * Returns an array of deployed connectors.
     * @return an array of deployed connectors. Returns an array of 0 length
     * if none are deployed.
     * @throws ServerinstanceException
     */
    public String[] getDeployedConnectors() throws ServerInstanceException
    {
        String[] connectors = new String[0];
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications appsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext, ServerXPathHelper.XPATH_APPLICATIONS);
            ConnectorModule[] connectorConfigBeans =
                                    appsConfigBean.getConnectorModule();
            if (connectorConfigBeans != null)
            {
                connectors = new String[connectorConfigBeans.length];
                for(int i = 0; i < connectors.length; i++)
                {
                    connectors[i] = connectorConfigBeans[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return connectors;
    }

    /**
    */
    public String[] getEnabledJ2EEApplications() throws ServerInstanceException
    {
        return ( null );
    }

    /**
    */
    public String[] getDisabledJ2EEApplications() throws ServerInstanceException
    {
        return ( null );
    }

    /**
    */
    public String[] getEnabledJ2EEModules() throws ServerInstanceException
    {
        return ( null );
    }

    /**
    */
    public String[] getDisabledJ2EEModules() throws ServerInstanceException
    {
        return ( null );
    }


    /**
    */
    public void createResource(String resourceXMLFile)
        throws AFResourceException
    {
        try
        {
           ResourcesXMLParser allResources =
                new ResourcesXMLParser(resourceXMLFile);
            Iterator resourceIter = allResources.getResources();
            while (resourceIter.hasNext())
            {
                Resource resource = (Resource) resourceIter.next();
                if (resource.getType() == Resource.JDBC_RESOURCE)
                {
                    Properties attributes = resource.getAttributes();
                    String jndiName =
                        attributes.getProperty( ResourcesXMLParser.JNDI_NAME);
                    String poolName =
                        attributes.getProperty( ResourcesXMLParser.POOL_NAME);
                    createJDBCResource(jndiName, poolName);
                    addJDBCResourceAttribute(resource, attributes, jndiName);
                }
                else if (resource.getType() == Resource.JMS_RESOURCE)
                {
                    Properties attributes = resource.getAttributes();
                    String jndiName =
                        attributes.getProperty( ResourcesXMLParser.JNDI_NAME);
                    String resType =
                        attributes.getProperty(ResourcesXMLParser.RES_TYPE);
/*                    String factoryClass =
                        attributes.getProperty(ResourcesXMLParser.FACTORY_CLASS);
*/
                    createJMSResource(jndiName, resType, new Properties()/*, factoryClass*/);
                    addJMSResourceAttribute(resource, attributes, jndiName);

                }
                else if (resource.getType() == Resource.EXT_JNDI_RESOURCE)
                {
                    Properties attributes = resource.getAttributes();
                    String jndiName =
                        attributes.getProperty(ResourcesXMLParser.JNDI_NAME);
                    String jndiLookupName =
                        attributes.getProperty(ResourcesXMLParser.JNDI_LOOKUP);
                    String resType =
                        attributes.getProperty(ResourcesXMLParser.RES_TYPE);
                    String factoryClass =
                        attributes.getProperty(ResourcesXMLParser.FACTORY_CLASS);

                    createJNDIResource(jndiName, jndiLookupName, resType,
                                        factoryClass);
                    addJNDIResourceAttribute(resource, attributes, jndiName);
                }
                else if (resource.getType() == Resource.PERSISTENCE_RESOURCE)
                {
                    Properties attributes = resource.getAttributes();
                    String jndiName =
                        attributes.getProperty(ResourcesXMLParser.JNDI_NAME);

                    //Bug# 4661145
                    createPersistenceManagerFactoryResource(jndiName);
                    addPersistenceManagerFactoryAttribute(resource,
                                                          attributes,
                                                          jndiName);
                }
                else if (resource.getType() == Resource.MAIL_RESOURCE)
                {
                    Properties attributes = resource.getAttributes();
                    String jndiName =
                        attributes.getProperty(ResourcesXMLParser.JNDI_NAME);
                    String host =
                        attributes.getProperty(ResourcesXMLParser.MAIL_HOST);
                    String user =
                        attributes.getProperty(ResourcesXMLParser.MAIL_USER);
                    String fromAddress = attributes.getProperty(
                                    ResourcesXMLParser.MAIL_FROM_ADDRESS);
                    createJavaMailResource(jndiName, host, user, fromAddress);
                    addJavaMailAttribute(resource, attributes, jndiName);
                }
                else if (resource.getType() == Resource.CUSTOM_RESOURCE)
                {
                    Properties attributes = resource.getAttributes();
                    String jndiName =
                        attributes.getProperty(ResourcesXMLParser.JNDI_NAME);
                    String resType =
                        attributes.getProperty(ResourcesXMLParser.RES_TYPE);
                    String factoryClass =
                        attributes.getProperty(ResourcesXMLParser.FACTORY_CLASS);

                    createCustomResource(jndiName, resType, factoryClass);
                    addCustomResourceAttribute(resource, attributes, jndiName);
                }
                else if (resource.getType() == Resource.JDBC_CONN_POOL)
                {
                    Properties attributes = resource.getAttributes();
                    String name = attributes.getProperty(
                                    ResourcesXMLParser.CONNECTION_POOL_NAME);
                    String datasourceClass =
                        attributes.getProperty(ResourcesXMLParser.DATASOURCE_CLASS);

                    createJDBCConnectionPool(name, datasourceClass);
                    addJDBCConnectionPoolAttribute(resource, attributes, name);
                }
            }
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getMessage());
        }
    }


    public void createJDBCConnectionPool(String id,
                                         String datasourceClassName)
        throws AFResourceException
    {
        ArgChecker.checkValid(id, "id",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(datasourceClassName, "datasourceClassName",
                              StringValidator.getInstance()); //noi18n

        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", id);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JdbcConnectionPool resource = new JdbcConnectionPool();
            resource.setName(id);
            resource.setDatasourceClassname(datasourceClassName);
            resourcesBean.addJdbcConnectionPool(resource);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    private void addJDBCConnectionPoolAttribute(Resource resource,
                                                Properties attributes,
                                                String name)
                 throws AFResourceException
    {
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                      (Resources)ConfigBeansFactory.getConfigBeanByXPath(
                      serverContext,
                      ServerXPathHelper.XPATH_RESOURCES);
            JdbcConnectionPool jdbc_pool_resource =
                               resourcesBean.getJdbcConnectionPoolByName(name);
            String sSteadyPoolSize =
                   attributes.getProperty(ResourcesXMLParser.STEADY_POOL_SIZE);
            String sMaxPoolSize =
                   attributes.getProperty(ResourcesXMLParser.MAX_POOL_SIZE);
            String sMaxWaitTimeInMillis =
                   attributes.getProperty(
                   ResourcesXMLParser.MAX_WAIT_TIME_IN_MILLIS);
            String sPoolSizeQuantity =
                   attributes.getProperty(ResourcesXMLParser.POOL_SIZE_QUANTITY);
            String sIdleTimeoutInSec =
                   attributes.getProperty(
                   ResourcesXMLParser.IDLE_TIME_OUT_IN_SECONDS);
            String sIsConnectionValidationRequired =
                   attributes.getProperty(
                   ResourcesXMLParser.IS_CONNECTION_VALIDATION_REQUIRED);
            String sConnectionValidationMethod =
                   attributes.getProperty(
                   ResourcesXMLParser.CONNECTION_VALIDATION_METHOD);
            String sFailAllConnection =
                   attributes.getProperty(
                   ResourcesXMLParser.FAIL_ALL_CONNECTIONS);
            String sValidationTableName =
                   attributes.getProperty(
                   ResourcesXMLParser.VALIDATION_TABLE_NAME);
            String sResType =
                   attributes.getProperty(
                   ResourcesXMLParser.RES_TYPE);
            String sTransIsolationLevel =
                   attributes.getProperty(
                   ResourcesXMLParser.TRANS_ISOLATION_LEVEL);
            String sIsIsolationLevelQuaranteed =
                   attributes.getProperty(
                   ResourcesXMLParser.IS_ISOLATION_LEVEL_GUARANTEED);

            if (sSteadyPoolSize != null) {
                jdbc_pool_resource.setSteadyPoolSize(sSteadyPoolSize);
            }
            if (sMaxPoolSize != null) {
                jdbc_pool_resource.setMaxPoolSize(sMaxPoolSize);
            }
            if (sMaxWaitTimeInMillis != null) {
                jdbc_pool_resource.setMaxWaitTimeInMillis(sMaxWaitTimeInMillis);
            }
            if (sPoolSizeQuantity != null) {
                jdbc_pool_resource.setPoolResizeQuantity(sPoolSizeQuantity);
            }
            if (sIdleTimeoutInSec != null) {
                jdbc_pool_resource.setIdleTimeoutInSeconds(sIdleTimeoutInSec);
            }
            if (sIsConnectionValidationRequired != null) {
                jdbc_pool_resource.setIsConnectionValidationRequired(
                                   Boolean.valueOf(sIsConnectionValidationRequired).booleanValue());
            }
            if (sConnectionValidationMethod != null) {
                jdbc_pool_resource.setConnectionValidationMethod(
                                   sConnectionValidationMethod);
            }
            if (sFailAllConnection != null) {
                jdbc_pool_resource.setFailAllConnections(Boolean.valueOf(
                                   sFailAllConnection).booleanValue());
            }
            if (sValidationTableName != null) {
                jdbc_pool_resource.setValidationTableName(sValidationTableName);
            }
            if (sResType != null) {
                jdbc_pool_resource.setResType(sResType);
            }
            if (sTransIsolationLevel != null) {
                jdbc_pool_resource.setTransactionIsolationLevel(
                                   sTransIsolationLevel);
            }
            if (sIsIsolationLevelQuaranteed != null) {
                jdbc_pool_resource.setIsIsolationLevelGuaranteed(
                     Boolean.valueOf(sIsIsolationLevelQuaranteed).booleanValue());
            }

            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                jdbc_pool_resource.setDescription(sDescription);
            }
            //element property
            ElementProperty[] epArray = resource.getElementProperty();
            if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   jdbc_pool_resource.addElementProperty(epArray[ii]);
            }
            serverContext.flush();
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public void deleteJDBCConnectionPool(String poolName)
        throws AFResourceException
    {
        ArgChecker.checkValid(poolName, "poolName",
                              StringValidator.getInstance()); //noi18n
        sLogger.log(Level.FINE, "mbean.delele_jdbc_pool", poolName);
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JdbcConnectionPool resource =
                resourcesBean.getJdbcConnectionPoolByName(poolName);
            //Bug# 4682650
            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", poolName );
                throw new Exception( msg );
            }
            resourcesBean.removeJdbcConnectionPool(resource);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_jdbc_pool_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listJDBCConnectionPools() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JdbcConnectionPool[] connectionPools = resourcesBean.getJdbcConnectionPool();
            if (connectionPools != null)
            {
                sa = new String[connectionPools.length];
                for(int i=0; i<connectionPools.length; i++)
                {
                    sa[i] = connectionPools[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }

    /**
     * Registers a jdbc datasource with the given jndiName & properties.
     */
    public void createJDBCResource(String   jndiName,
                                   String   poolName)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(poolName, "poolName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JdbcResource resource = new JdbcResource();
            resource.setJndiName(jndiName);
            resource.setPoolName(poolName);
            //resource.setEnabled(true);
            resourcesBean.addJdbcResource(resource);
	    createResourceRef(jndiName);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    private void createResourceRef(String name) throws Exception {
            ConfigContext serverContext = getConfigContext(mInstanceName);
  	    // add reference in PE case
	    Server server = ServerBeansFactory.getServerBean(serverContext); 
	    ResourceRef rr = new ResourceRef();
	    rr.setRef(name);
	    server.addResourceRef(rr);
	    // end add ref

    } 

    private void deleteResourceRef(String name) throws Exception {
            ConfigContext serverContext = getConfigContext(mInstanceName);
  	    // add reference in PE case
	    Server server = ServerBeansFactory.getServerBean(serverContext); 
	    ResourceRef rr = server.getResourceRefByRef(name);
	    if(rr != null) {
	        server.removeResourceRef(rr);
	    }
    } 

    private void addJDBCResourceAttribute(Resource resource,
                                          Properties attributes,
                                          String jndiName)
                 throws AFResourceException
    {
        //adding rest of the attributes
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                      (Resources)ConfigBeansFactory.getConfigBeanByXPath(
                      serverContext, ServerXPathHelper.XPATH_RESOURCES);
            JdbcResource jdbc_resource =
                         resourcesBean.getJdbcResourceByJndiName(jndiName);
//ms1            String enabledName =
//ms1                   attributes.getProperty(ResourcesXMLParser.ENABLED);
//ms1            if (enabledName != null) {
//ms1                jdbc_resource.setEnabled(Boolean.valueOf(enabledName).booleanValue());
//ms1            } //if
            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                jdbc_resource.setDescription(sDescription);
            }
            //element property
            /*
            ElementProperty[] epArray = resource.getElementProperty();
            if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   jdbc_resource.addElementProperty(epArray[ii]);
            }
            */

            serverContext.flush();
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public void deleteJDBCResource(String jndiName) throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.delete_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JdbcResource resource =
                resourcesBean.getJdbcResourceByJndiName(jndiName);
            //Bug# 4682650
            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                throw new Exception( msg );
            }
            resourcesBean.removeJdbcResource(resource);
            deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listJDBCResources() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JdbcResource[] jdbcResources = resourcesBean.getJdbcResource();
            if (jdbcResources != null)
            {
                sa = new String[jdbcResources.length];
                for(int i=0; i<jdbcResources.length; i++)
                {
                    sa[i] = jdbcResources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }

    /**
     * Registers a Java Mail resource.
     */
    public void createJavaMailResource(String   jndiName,
                                       String   host,
                                       String   user,
                                       String   fromAddress)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(host, "host",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(user, "user",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(fromAddress, "fromAddress",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            MailResource resource = new MailResource();
            resource.setJndiName(jndiName);
            resource.setHost(host);
            resource.setUser(user);
            resource.setFrom(fromAddress);
//ms1            resource.setEnabled(true);
            resourcesBean.addMailResource(resource);
	    createResourceRef(jndiName);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    private void addJavaMailAttribute(Resource resource,
                                      Properties attributes,
                                      String jndiName)
                 throws AFResourceException
    {
        //adding rest of the attributes
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                      (Resources)ConfigBeansFactory.getConfigBeanByXPath(
                      serverContext,
                      ServerXPathHelper.XPATH_RESOURCES);
            MailResource mail_resource = resourcesBean.getMailResourceByJndiName(jndiName);
            String sStoreProto =
                   attributes.getProperty(ResourcesXMLParser.MAIL_STORE_PROTO);
            String sStoreProtoClass =
                   attributes.getProperty(ResourcesXMLParser.MAIL_STORE_PROTO_CLASS);
            String sTransProto =
                   attributes.getProperty(ResourcesXMLParser.MAIL_TRANS_PROTO);
            String sTransProtoClass =
                   attributes.getProperty(ResourcesXMLParser.MAIL_TRANS_PROTO_CLASS);
            String sDebug =
                   attributes.getProperty(ResourcesXMLParser.MAIL_DEBUG);
//ms1            String sEnabled =
//ms1                   attributes.getProperty(ResourcesXMLParser.ENABLED);

            if (sStoreProto != null) {
                mail_resource.setStoreProtocol(sStoreProto);
            }
            if (sStoreProtoClass != null) {
                mail_resource.setStoreProtocolClass(sStoreProtoClass);
            }
            if (sTransProto != null ) {
                mail_resource.setTransportProtocol(sTransProto);
            }
            if (sTransProtoClass != null) {
                mail_resource.setTransportProtocolClass(sTransProtoClass);
            }
            if (sDebug != null) {
                mail_resource.setDebug(Boolean.valueOf(sDebug).booleanValue());
            }
//ms1            if (sEnabled != null) {
//ms1                mail_resource.setEnabled(Boolean.valueOf(sEnabled).booleanValue());
//ms1            }
            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                mail_resource.setDescription(sDescription);
             }
             //element property
             ElementProperty[] epArray = resource.getElementProperty();
             if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   mail_resource.addElementProperty(epArray[ii]);
             }
             serverContext.flush();
         }
         catch (Exception e)
         {
             throw new AFResourceException(e.getLocalizedMessage());
         }
    }

    public void deleteJavaMailResource(String jndiName)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.delete_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            MailResource resource =
                resourcesBean.getMailResourceByJndiName(jndiName);
            //Bug# 4682650
            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                throw new Exception( msg );
            }
            resourcesBean.removeMailResource(resource);
            deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listJavaMailResources() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            MailResource[] mailResources = resourcesBean.getMailResource();
            if (mailResources != null)
            {
                sa = new String[mailResources.length];
                for(int i=0; i<mailResources.length; i++)
                {
                    sa[i] = mailResources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }
    

    public void createJMSResource(String    jndiName,
                                  String    resourceType
                                  /* String    factoryClassName*/)
        throws AFResourceException
    {
        createJMSResource(jndiName, resourceType, new Properties());
    }
    

    public void createJMSResource(String    jndiName,
                                  String    resourceType,
                                  Properties props/*,
                                  String    factoryClassName*/)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        /*ArgChecker.checkValid(factoryClassName, "factoryClassName",
                              StringValidator.getInstance()); //noi18n  */
        ArgChecker.checkValid(resourceType, "resourceType",
                              StringValidator.getInstance()); //noi18n

        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
	    JmsRaMapping ramap = ConnectorRuntime.getRuntime().getJmsRaMapping();

                /* Map MQ properties to Resource adapter properties */
		Enumeration en = props.keys();
		Properties properties = new Properties();
	        while (en.hasMoreElements()) {
		    String key = (String) en.nextElement();
		    String raKey = ramap.getMappedName(key);
		    if (raKey == null) raKey = key;
		    properties.put(raKey, (String) props.get(key));
		}

                // Add a connector-connection-pool & a connector-resource
                String raName = ConnectorRuntime.getRuntime().DEFAULT_JMS_ADAPTER;

                if (resourceType.equalsIgnoreCase("javax.jms.TopicConnectionFactory") ||
                    resourceType.equalsIgnoreCase("javax.jms.QueueConnectionFactory"))
                {
                    String defPoolName = ConnectorRuntime.getRuntime().getDefaultPoolName(jndiName);
                    com.sun.enterprise.config.serverbeans.ConnectorConnectionPool conPool = 
                        new com.sun.enterprise.config.serverbeans.ConnectorConnectionPool();
                    conPool.setResourceAdapterName(raName);
                    conPool.setConnectionDefinitionName(resourceType);
                    conPool.setName(defPoolName);
                    conPool.setMaxPoolSize("250");
                    conPool.setSteadyPoolSize("1");
                    resourcesBean.addConnectorConnectionPool(conPool);
                    
                    // Add connector-resource
                    com.sun.enterprise.config.serverbeans.ConnectorResource resource = 
                        new com.sun.enterprise.config.serverbeans.ConnectorResource();
                    resource.setJndiName(jndiName);
                    resource.setPoolName(defPoolName);
                    //if (description != null) resource.setDescription(description);
                    //resource.setEnabled(true);
                    // Add the property elements.
                    if (properties != null) {
                        Enumeration e = properties.keys();
                        String n,v;
                        ElementProperty el = null;
                        while (e.hasMoreElements()) {
                            n = (String) e.nextElement();
                            v = (String) properties.get(n);
                            el = new ElementProperty();
			    el.setName(n);
                            el.setValue(v);
                            conPool.addElementProperty(el);                
                        }
                    }                    
                    resourcesBean.addConnectorResource(resource);
	            createResourceRef(jndiName);
                }
                else {
                    // create admin object
                    addAdminObject(resourceType, properties, raName, jndiName);
                }
                
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }


    private void addJMSResourceAttribute(Resource resource,
                                         Properties attributes,
                                         String jndiName)
                 throws AFResourceException
    {
/*
        //adding rest of the attributes
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                     (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                      ServerXPathHelper.XPATH_RESOURCES);
            JmsResource jms_resource =
                        resourcesBean.getJmsResourceByJndiName(jndiName);
//ms1            String enabledName =
//ms1                   attributes.getProperty( ResourcesXMLParser.ENABLED);
//ms1            if (enabledName != null) {
//ms1                jms_resource.setEnabled(Boolean.valueOf(enabledName).booleanValue());
//ms1            }
            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                jms_resource.setDescription(sDescription);
            }
            //element property
                ElementProperty[] epArray = resource.getElementProperty();
            if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   jms_resource.addElementProperty(epArray[ii]);
            }

            serverContext.flush();
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getLocalizedMessage());
        }
*/
    }

 
   public void deleteJMSResource(String jndiName)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        try
        {
                // delete the connector-resource & its pool if the resource is referencing 
                // its default jms connector connection pool.
                ConfigContext serverContext = getConfigContext(mInstanceName);
                Resources resourcesBean =
                    (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                                ServerXPathHelper.XPATH_RESOURCES);
                com.sun.enterprise.config.serverbeans.ConnectorResource resource =
                    resourcesBean.getConnectorResourceByJndiName(jndiName);

                if (resource == null)
                {
                    // delete any admin objects with this jndi name
                    deleteAdminObject(jndiName);
	            /*
                    String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                    throw new Exception( msg );
	            */
                } else {
                    String defPoolName = ConnectorRuntime.getRuntime().getDefaultPoolName(jndiName);
                    if (resource.getPoolName().equals(defPoolName)) {
                        resourcesBean.removeConnectorResource(resource);
                        deleteConnectorConnectionPool(defPoolName);
                    }
	        }
                
                serverContext.flush();                
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }


    public String[] listJMSResources() throws AFException
    {
        String[] sa = null;
/*
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            JmsResource[] jmsResources = resourcesBean.getJmsResource();
            if (jmsResources != null)
            {
                sa = new String[jmsResources.length];
                for(int i=0; i<jmsResources.length; i++)
                {
                    sa[i] = jmsResources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
*/
	return null;
    }

    public void createJNDIResource(String   jndiName,
                                   String   jndiLookupName,
                                   String   resourceType,
                                   String   factoryClass)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(jndiLookupName, "jndiLookupName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(resourceType, "resourceType",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(factoryClass, "factoryClass",
                              StringValidator.getInstance()); //noi18n

        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            ExternalJndiResource resource =
                new ExternalJndiResource();
            resource.setJndiName(jndiName);
            resource.setJndiLookupName(jndiLookupName);
            resource.setResType(resourceType);
            resource.setFactoryClass(factoryClass);
//ms1            resource.setEnabled(true);
            resourcesBean.addExternalJndiResource(resource);
	    createResourceRef(jndiName);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    private void addJNDIResourceAttribute(Resource resource,
                                          Properties attributes,
                                          String jndiName)
                 throws AFResourceException
    {
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                      (Resources)ConfigBeansFactory.getConfigBeanByXPath(
                       serverContext,
                       ServerXPathHelper.XPATH_RESOURCES);
            ExternalJndiResource jndi_resource =
                                 resourcesBean.getExternalJndiResourceByJndiName(jndiName);
//ms1            String sEnabled =
//ms1                   attributes.getProperty(ResourcesXMLParser.ENABLED);
//ms1            if (sEnabled != null) {
//ms1                jndi_resource.setEnabled(Boolean.valueOf(sEnabled).booleanValue());
//ms1            }
            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                jndi_resource.setDescription(sDescription);
            }
            //element property
            ElementProperty[] epArray = resource.getElementProperty();
            if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   jndi_resource.addElementProperty(epArray[ii]);
            }
            serverContext.flush();
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public void deleteJNDIResource(String jndiName) throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.delete_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            ExternalJndiResource resource =
                resourcesBean.getExternalJndiResourceByJndiName(jndiName);
            //Bug# 4682650
            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                throw new Exception( msg );
            }
            resourcesBean.removeExternalJndiResource(resource);
            deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listJNDIResources() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            ExternalJndiResource[] jndiResources = resourcesBean.getExternalJndiResource();
            if (jndiResources != null)
            {
                sa = new String[jndiResources.length];
                for(int i=0; i<jndiResources.length; i++)
                {
                    sa[i] = jndiResources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }

    /**
     */
    public void createPersistenceManagerFactoryResource(String jndiName)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n

        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            PersistenceManagerFactoryResource resource =
                new PersistenceManagerFactoryResource();
            resource.setJndiName(jndiName);
            //resource.setFactoryClass(factoryClassName);
            //resource.setJdbcResourceJndiName(jdbcResJndiName);
//ms1            resource.setEnabled(true);
            resourcesBean.addPersistenceManagerFactoryResource(resource);
	    createResourceRef(jndiName);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    private void addPersistenceManagerFactoryAttribute(Resource resource,
                                                       Properties attributes,
                                                       String jndiName)
                 throws AFResourceException
    {
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                      (Resources)ConfigBeansFactory.getConfigBeanByXPath(
                      serverContext,
                      ServerXPathHelper.XPATH_RESOURCES);
            PersistenceManagerFactoryResource pmf_resource =
                      resourcesBean.getPersistenceManagerFactoryResourceByJndiName(jndiName);
            String factoryClass =
                   attributes.getProperty(ResourcesXMLParser.FACTORY_CLASS);
            String resName =
                   attributes.getProperty(ResourcesXMLParser.JDBC_RESOURCE_JNDI_NAME);
            String sEnabled =
                   attributes.getProperty(ResourcesXMLParser.ENABLED);
            if (factoryClass != null) {
                pmf_resource.setFactoryClass(factoryClass);
            }
            if (resName != null) {
                pmf_resource.setJdbcResourceJndiName(resName);
            }
//ms1            if (sEnabled != null) {
//ms1                pmf_resource.setEnabled(Boolean.valueOf(sEnabled).booleanValue());
//ms1            }
            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                pmf_resource.setDescription(sDescription);
            }
            //element property
            ElementProperty[] epArray = resource.getElementProperty();
            if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   pmf_resource.addElementProperty(epArray[ii]);
            }
            serverContext.flush();
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public void deletePersistenceManagerFactoryResource(String jndiName)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.delete_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            PersistenceManagerFactoryResource resource = resourcesBean.
                getPersistenceManagerFactoryResourceByJndiName(jndiName);
            //Bug# 4682650
            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource" );
                throw new Exception( msg );
            }
            resourcesBean.removePersistenceManagerFactoryResource(resource);
            deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listPersistenceManagerFactoryResources()
        throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            PersistenceManagerFactoryResource[] pmFactoryResources =
                resourcesBean.getPersistenceManagerFactoryResource();
            if (pmFactoryResources != null)
            {
                sa = new String[pmFactoryResources.length];
                for(int i=0; i<pmFactoryResources.length; i++)
                {
                    sa[i] = pmFactoryResources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }

    public void createCustomResource(String jndiName,
                                     String resourceType,
                                     String factoryClass)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(factoryClass, "factoryClass",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(resourceType, "resourceType",
                              StringValidator.getInstance()); //noi18n

        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            CustomResource resource = new CustomResource();
            resource.setJndiName(jndiName);
            resource.setResType(resourceType);
            resource.setFactoryClass(factoryClass);
//ms1            resource.setEnabled(true);
            resourcesBean.addCustomResource(resource);
	    createResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    private void addCustomResourceAttribute(Resource resource,
                                            Properties attributes,
                                            String jndiName)
                 throws AFResourceException
    {
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
            (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                       ServerXPathHelper.XPATH_RESOURCES);
            CustomResource custom_resource =
                           resourcesBean.getCustomResourceByJndiName(jndiName);
            String sEnabled =
                   attributes.getProperty(ResourcesXMLParser.ENABLED);
//ms1            if (sEnabled != null) {
//ms1                custom_resource.setEnabled(Boolean.valueOf(sEnabled).booleanValue());
//ms1            }
            //description
            String sDescription = resource.getDescription();
            if (sDescription != null) {
                custom_resource.setDescription(sDescription);
            }
            //element property
            ElementProperty[] epArray = resource.getElementProperty();
            if (epArray != null) {
                for (int ii=0; ii<epArray.length; ii++)
                   custom_resource.addElementProperty(epArray[ii]);
            }
            serverContext.flush();
        }
        catch (Exception e)
        {
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public void deleteCustomResource(String jndiName)
        throws AFResourceException
    {
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINE, "mbean.delete_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            CustomResource resource =
                resourcesBean.getCustomResourceByJndiName(jndiName);
            //Bug# 4682650
            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                throw new Exception( msg );
            }
            resourcesBean.removeCustomResource(resource);
            deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listCustomResources() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            CustomResource[] customResources = resourcesBean.getCustomResource();
            if (customResources != null)
            {
                sa = new String[customResources.length];
                for(int i=0; i<customResources.length; i++)
                {
                    sa[i] = customResources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }

    /**
    */
    public void setHttpPort(int port)
    {
    }

    public HostAndPort getHostAndPort() throws ServerInstanceException 
    {
        return getHostAndPort(false);
    }
        
    public HostAndPort getHostAndPort(boolean securityEnabled) throws ServerInstanceException
    {
        HostAndPort hAndp = null;
        try
        {
//ms1            Server          server  = (Server) super.getBaseConfigBean();
            Config          config  = (Config) super.getConfigBeanByXPath(ServerXPathHelper.XPATH_CONFIG);
            HttpService     https   = config.getHttpService();

            HttpListener[] hlArray = https.getHttpListener();
            //check not needed since there should always be atleast 1 httplistener
            //if you don't find one, use first one.
            HttpListener ls = hlArray[0];
            //default is the first one that is enabled.
            for(int i = 0;i<hlArray.length;i++) {
                if(hlArray[i].isEnabled() && (hlArray[i].isSecurityEnabled()==securityEnabled)) {
                    ls = hlArray[i];
                    break;
                }
            }

            String          port    = ls.getPort();
            int             intPort = Integer.parseInt (port);
            hAndp = new HostAndPort(ls.getServerName(), intPort);
        }
        catch (Exception e)
        {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return hAndp;
    }

    public int getHttpPort()
    {
        return 9000;
    }

    public ServerInstanceStatus getStatus() throws ControlException
    {
        int statusCode = getInstanceStatusCode();
        ServerInstanceStatus status = new ServerInstanceStatus(statusCode);
        //Bug# 4686443
        if (isDebug())
        {
            status.setDebug(true);
            try
            {
                status.setDebugPort(getDebugPort());
            }
            catch (Exception e)
            {
                sLogger.finest
                ("Problem with getting port:ManagedServerInstance:getStatus");
                throw new ControlException(e.getLocalizedMessage());
            }
        }
        sLogger.exiting(getClass().getName(), "getStatus",
                        status.getStatusString()); //noi18n
        return ( status );
    }

    /**
     * Is instance restart required. Restart is required if dynamic
     * reconfiguration on the instance could not be dones and the user has
     * not restarted the instance since then.
     */
    public boolean isRestartNeeded()
    {
        AdminEventCache cache = AdminEventCache.getInstance(mInstanceName);
        boolean restartRequired = cache.isInstanceRestartNeeded();
        if(restartRequired)
        {
            RMIClient rc = AdminChannel.getRMIClient(mInstanceName);
            if(!rc.isAlive())
                restartRequired = false;
        }
        return restartRequired;
    }

    private boolean isAlive()
    {
        boolean alive = false;
        RMIClient serverInstancePinger = AdminChannel.
                getRMIClient(mInstanceName);

        alive = serverInstancePinger.isAlive();
        return ( alive );
    }

    private int getInstanceStatusCode() {
        int statusCode = Status.kInstanceNotRunningCode;
        RMIClient serverInstancePinger = AdminChannel.getRMIClient(mInstanceName);
        statusCode = serverInstancePinger.getInstanceStatusCode();
        return statusCode;
    }

    /**
        Issues a notification to this Server Instance, so that it reads the
        configuration file. It may be that the modified
        configuration can't be completely applied to the Server Instance
        without restarting it. In such cases, the user has to restart it.
        The configuration parameters that do not require the Instance restart
        will take effect immediately.
    */
    public void reconfigure()
    {
    }

    /**
     * Is apply needed to publish changes in backup area to live config.
     * The method returns true if there are changes to server.xml, init.conf,
     * obj.conf or mime types fules in backup area.
     */

    public boolean isApplyNeeded() throws ServerInstanceException
    {
        return isApplyNeeded(true);
    }


    /**
     * checkAllFiles: if false will only check server xml for changes
     * if true will check all files server.xml, conf, mime.type, acl etc files
     */
    public boolean isApplyNeeded(boolean checkAllFiles) throws ServerInstanceException
    {
        boolean applyNeeded = false;
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            applyNeeded = serverContext.isChanged();
        }
        catch (ConfigException e)
        {
            throw new ServerInstanceException(e.getMessage());
        }
        if (!applyNeeded && checkAllFiles) {
            InstanceEnvironment ie = new InstanceEnvironment(mInstanceName);
            applyNeeded = ie.hasRealmsKeyConfFileChanged();
        }
        return applyNeeded;
    }

    /**
     * Copies the file from backup directory to the real config directory
     * so that the configuration is stored on disk.
     * There is no guarantee of any transactional support.
     * @return true: restart is required.
     */
    public boolean overwriteConfigChanges() throws ServerInstanceException
    {
       	try {
       	    return applyChanges(true);
	} catch (AFRuntimeStoreException e) {
            InstanceEnvironment ie = new InstanceEnvironment(mInstanceName);

             if(ie.canReloadManualXmlChanges()) {
		try {
                    ie.useManualServerXmlChanges();
                    reloadAfterChange(ie);
		} catch(ConfigException ce) {
		    // Sorry cant do anything.
		}
                return true;
             } else {
		throw e;
	     }
	}

    }
    /**
     * Copies the file from backup directory to the real config directory
     * so that the configuration is stored on disk.
     * There is no guarantee of any transactional support.
     * @return True means requires restart.
     */
    public boolean applyConfigChanges() throws ServerInstanceException
    {
        return applyChanges(false);
    }

    private boolean applyChanges(boolean force) throws ServerInstanceException
    {
        boolean requiresRestart = false;
        try
        {
            InstanceEnvironment instanceEnv = new InstanceEnvironment(mInstanceName);
            /* TOMCAT_BEGIN Ramakanth*/
            boolean hasConfChanges = false;
            boolean hasMimeChanges = false;
            hasConfChanges = instanceEnv.hasRealmsKeyConfFileChanged();
            /* TOMCAT_END Ramakanth*/

            instanceEnv.applyChangesFromBackup(force);
            // multicastEvent(CONFIG_CHANGED, null);
            /* New for 8.0 - temporary - gen server.xml b4 notif */
            /* This call most likely goes away */
            this.transform(instanceEnv);
            /* New for 8.0 - temporary - gen server.xml b4 notif */
            
            /* TOMCAT_BEGIN Ramakanth*/
            requiresRestart = sendNotificationOnApply(hasConfChanges, hasMimeChanges);
            /* TOMCAT_END Ramakanth*/
        }
        catch (ConfigException e)
        {
            throw new ServerInstanceException(e.getMessage());
        }
        return requiresRestart;
    }

    public boolean canApplyConfigChanges() throws ConfigException {
        boolean b = false;
        InstanceEnvironment ie = new InstanceEnvironment(mInstanceName);
        if(AdminService.ENABLE_PERFORMANCE_THREAD) {
            b = (!ManualChangeManager.hasHotChanged(mInstanceName));
        } else {
            b = (!ie.hasHotChanged());
        }
        if((!b)  && ie.canReloadManualXmlChanges()) {
            ie.useManualServerXmlChanges();
            reloadAfterChange(ie);
            boolean requiresRestart = ie.restartRequired();
            if (requiresRestart) {
                // The instance was not started after manual config changes, so
                // persist the restart required state.
                AdminEventCache cache =
                        AdminEventCache.getInstance(mInstanceName);
                cache.setRestartNeeded(true);
            }
            return true;
        }
        return b;
    }

    void reloadAfterChange(InstanceEnvironment instanceEnv) throws ConfigException
    {
        // 2. unregister all instance related mbeans
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        ObjectName[] objectNames  = ObjectNameHelper.getInstanceRelatedMBeans(mbs, mInstanceName);
        for(int i=0; i<objectNames.length; i++)
        {
            try
            {
                mbs.unregisterMBean(objectNames[i]);
            }
            catch(Exception e)
            {
                sLogger.log(Level.WARNING, "Exception: ", e);
            }
        }
        // 3. Message to Config to refresh contexts
        String fileUrl  = instanceEnv.getBackupConfigFilePath();
        ConfigFactory.removeConfigContext(fileUrl);

            /*
            //Refresh true is a poor man's solution
            ConfigContext ctx = ConfigFactory.createConfigContext(instanceEnv.getBackupConfigFilePath());
            ctx.refresh(true);
             */
    }

    public boolean useManualConfigChanges() throws ServerInstanceException
    {
        boolean requiresRestart = false;
        try
        {
            InstanceEnvironment instanceEnv = new InstanceEnvironment(mInstanceName);

            // 1. copy from hot to back
            instanceEnv.useManualConfigChanges();

            reloadAfterChange(instanceEnv);

            // multicastEvent(CONFIG_CHANGED, null);
            /* New for 8.0 - temporary */
            this.transform(instanceEnv);
            /* New for 8.0 - temporary */
            requiresRestart = instanceEnv.restartRequired();
            if (requiresRestart) {
                // The instance was not started after manual config changes, so
                // persist the restart required state.
                AdminEventCache cache =
                        AdminEventCache.getInstance(mInstanceName);
                cache.setRestartNeeded(true);
            }
        }
        catch (ConfigException e)
        {
            throw new ServerInstanceException(e.getMessage());
        }
        return requiresRestart;
    }
    /**
        Initiates the monitoring process for this Server Instance. Note that
        various components within the Server Instance will actually provide
        the monitoring data. This method however establishes the underlying
        infrastructure and basic configuration of monitoring system. Following
        are the essential things accomplished by this method:
        <li> Establish the live monitor connection with running Server Instance.
        <li> Read the configuration store to know the various components
            that can be monitored. (e.g. EJB Container, Web Container etc.)
        <li> Register one MonitorMBean per monitorable component
            in Admin Server's MBeanServer.
        <li> Instruct all the core containers to prepare for giving out the
            named monitoring data.
        <p>
        Object Name of each Monitor MBean is:
        ias:type=monitor, MonitorComponent=<ComponentName>
    */
    public void startMonitor()
    {
    }

    /**
        Gets a list of names of components that are monitorable. This will
        most likely be stored in a configuration store.

        @return String[] list of names of monitorable components
    */
    public String[] getMonitorableComponentNames()
    {
        return ( null );
    }

    /**
        Stops the monitoring process for this Server Instance. Following are
        the consequences of this:
        <li> deregister all the Monitor MBeans.
        <li> Instruct all the monitoring data releasing core containers
            to stop releasing data.
        <li> Release the monitor link between Admin Server and this Server
        Instance.
    */
    public void stopMonitor()
    {
    }

    /**
     */
    public void createLifeCycleModule(String moduleName, String className)
        throws AFException
    {
        ArgChecker.checkValid(moduleName, "moduleName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(className, "className",
                              StringValidator.getInstance()); //noi18n

        try
        {
            sLogger.log(Level.FINEST, "mbean.create_lifecycle", moduleName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext,
                    ServerXPathHelper.XPATH_APPLICATIONS);
            LifecycleModule module = new LifecycleModule();
            module.setName(moduleName);
            module.setClassName(className);
//ms1            module.setEnabled(true); //Enabling by default
            applicationsConfigBean.addLifecycleModule(module);
	    createResourceRef(moduleName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_lifecycle_failed", e);
            throw new AFException(e.getMessage());
        }
    }

    /**
     */
    public void deleteLifeCycleModule(String moduleName) throws AFException
    {
        ArgChecker.checkValid(moduleName, "moduleName",
                              StringValidator.getInstance()); //noi18n
        try
        {
            sLogger.log(Level.FINEST, "mbean.delete_lifecycle", moduleName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext,
                    ServerXPathHelper.XPATH_APPLICATIONS);
            LifecycleModule module =
                applicationsConfigBean.getLifecycleModuleByName(moduleName);
            applicationsConfigBean.removeLifecycleModule(module);
            deleteResourceRef(moduleName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_lifecycle_failed", e);
            throw new AFException(e.getMessage());
        }
    }

    /**
     */
    public String[] listLifeCycleModules() throws AFException
    {
        String[] modules = new String[0];
        try
        {
            sLogger.log(Level.FINEST, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext,
                    ServerXPathHelper.XPATH_APPLICATIONS);
            LifecycleModule[] lifecycleConfigBeans =
                applicationsConfigBean.getLifecycleModule();
            if ((lifecycleConfigBeans != null) &&
                (lifecycleConfigBeans.length > 0))
            {
                modules = new String[lifecycleConfigBeans.length];
                for (int i = 0; i < lifecycleConfigBeans.length; i++)
                {
                    modules[i] = lifecycleConfigBeans[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getMessage());
        }
        return modules;
    }

    /**
     * Returns the location of the client stub jar that is generated by EJBC
     * during deployment of the given application.
     * @param appName application or module name by which an application
     * or an EJB module has been deployed.
     * @param int appType The type of the application whether it is an EAR
     * or EJB Jar.
     * @return Returns the absolute path to the client-stub-jar file.
     * @throws AFException
     */
    public String getClientStubJarLocation(String appName, int appType)
        throws AFException
    {
        ArgChecker.checkValid(appName, "appName", //noi18n
                              StringValidator.getInstance());
        if ((appType != DeploymentConstants.EAR) &&
            (appType != DeploymentConstants.EJB))
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.expected_application_type" );
            throw new AFException( msg );
        }
        String clientJarLocation = null;
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            InstanceEnvironment iEnv = new InstanceEnvironment(mInstanceName);
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext, ServerXPathHelper.XPATH_APPLICATIONS);
            String appLocation = null;
            switch (appType)
            {
                case DeploymentConstants.EAR :
                    J2eeApplication app = applicationsConfigBean.
                                            getJ2eeApplicationByName(appName);
                    AppsManager appsManager = new AppsManager(iEnv);
                    appLocation = appsManager.getGeneratedXMLLocation(appName);

                    // for upgrade scenario, we fall back to the original 
                    // location
                    if (appLocation == null ||
                        !FileUtils.safeIsDirectory(appLocation)) {
                        appLocation = app.getLocation();
                    }
                    break;
                case DeploymentConstants.EJB :
                    EjbModule module = applicationsConfigBean.
                                            getEjbModuleByName(appName);
                    EjbModulesManager ejbManager = new EjbModulesManager(iEnv);
                    appLocation = ejbManager.getGeneratedXMLLocation(appName);

                    // for upgrade scenario, we fall back to the original 
                    // location
                    if (appLocation == null ||
                        !FileUtils.safeIsDirectory(appLocation)) {
                        appLocation = module.getLocation();
                    }
                    break;
            }
            clientJarLocation = appLocation + java.io.File.separator +
                                appName + AdminConstants.CLIENT_JAR;
            sLogger.log(Level.INFO, "mbean.cl_jar_loc", clientJarLocation);
        }
        catch (Exception e)
        {
            throw new AFException(e.getLocalizedMessage());
        }
        return clientJarLocation;
    }

   /**
     * Returns the location of the client stub jar that is generated by EJBC
     * during deployment of the given application.
     * @param appName application or module name by which an application
     * or an EJB module has been deployed.
     * @param int appType The type of the application whether it is an EAR
     * or EJB Jar.
     * @return Returns the absolute path to the client-stub-jar file.
     * @throws AFException
     */
    public String getWsdlFileLocation(String appName, String moduleName, int appType, String wsdlFileUri)
        throws AFException
    {
        ArgChecker.checkValid(appName, "appName", //noi18n
                              StringValidator.getInstance());
        if ((appType != DeploymentConstants.EAR) &&
            (appType != DeploymentConstants.EJB) &&
            (appType != DeploymentConstants.WAR))
        {
	    String msg = localStrings.getString( "admin.server.core.mbean.config.expected_application_type" );
            throw new AFException( msg );
        }
        String wsdlFileLocation = null;
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            InstanceEnvironment iEnv = new InstanceEnvironment(mInstanceName);
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    serverContext, ServerXPathHelper.XPATH_APPLICATIONS);
            String appLocation = null;
            AbstractArchive moduleArchive = null;
            switch (appType)
            {
                case DeploymentConstants.EAR :
                    AppsManager appsManager = new AppsManager(iEnv);
                    appLocation = appsManager.getGeneratedXMLLocation(appName);
                    if (appLocation == null || !FileUtils.safeIsDirectory(appLocation)) {
                        J2eeApplication app = applicationsConfigBean.
                                            getJ2eeApplicationByName(appName);
                        appLocation = app.getLocation();
                    }
                    break;
                case DeploymentConstants.EJB :
                    EjbModulesManager ejbManager = new EjbModulesManager(iEnv);
                    appLocation = ejbManager.getGeneratedXMLLocation(appName);
                    if (appLocation == null || !FileUtils.safeIsDirectory(appLocation)) {
                        EjbModule module = applicationsConfigBean.
                                            getEjbModuleByName(appName);
                        appLocation = module.getLocation();
                    }
                    break;
                case DeploymentConstants.WAR:
                    WebModulesManager webManager = new WebModulesManager(iEnv);
                    appLocation = webManager.getGeneratedXMLLocation(appName);
                    if (appLocation == null || !FileUtils.safeIsDirectory(appLocation)) {
                        WebModule webModule = applicationsConfigBean.
                                            getWebModuleByName(appName);
                        appLocation = webModule.getLocation();
                    }
                    break;
            }
            
            FileArchive appArchive = new FileArchive();
            appArchive.open(appLocation);
            if (moduleName!=null) {
                moduleArchive = appArchive.getEmbeddedArchive(moduleName);            
            } else {
                moduleArchive = appArchive;
            }
            wsdlFileLocation = moduleArchive.getArchiveUri() + java.io.File.separator +
                                wsdlFileUri.replace('/', java.io.File.separatorChar);
        }
        catch (Exception e)
        {
            throw new AFException(e.getLocalizedMessage());
        }
        return wsdlFileLocation;
    }    

/* Implementation of MBeanRegistration - STARTS */

    /**
     * Allows the MBean to perform any operations needed after having been
     * registered in the MBean server or after the registration has failed.
     *
     * @param registrationDone Indicates whether or not the MBean has been successfully registered in
     * the MBean server. The value false means that the registration phase has failed.
     */
    public void postRegister(Boolean registrationDone)
    {
    }

    /**
     * Allows the MBean to perform any operations it needs before being registered
     * in the MBean server. If the name of the MBean is not specified, the
     * MBean can provide a name for its registration. If any exception is
     * raised, the MBean will not be registered in the MBean server.
     *
     * @param server The MBean server in which the MBean will be registered.
     * @param name The object name of the MBean.
     *
     * @return  The name of the MBean registered.
     *
     * @exception java.lang.Exception This exception should be caught by the MBean server and re-thrown
     * as an <CODE>MBeanRegistrationException</CODE>.
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws
		Exception
    {
		ObjectName serverInstanceMBeanObjectName = null;
		serverInstanceMBeanObjectName = new ObjectName("ias:type=serverinstance, name=");
		return ( serverInstanceMBeanObjectName );
    }

    /**
     * Allows the MBean to perform any operations needed after having been
     * de-registered in the MBean server.
     */
    public void postDeregister()
    {
    }

    /**
     * Allows the MBean to perform any operations it needs before being de-registered
     * by the MBean server.
     *
     * @exception java.langException  This exception should be caught by the MBean server and re-thrown
     * as an <CODE>MBeanRegistrationException</CODE>.
     */
    public void preDeregister() throws Exception
    {
    }

    /* Implementation of MBeanRegistration - ENDS */

    /**
            Register the MBean that represents the application and all the other
            MBeans within the application.
    */

    private void registerApplicationMBean(String archiveName)
        throws MBeanException
    {
        ObjectName appObjectName =
            ObjectNames.getApplicationObjectName(mInstanceName, archiveName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        if (!mbs.isRegistered(appObjectName))
        {
            try
            {
                ManagedJ2EEApplication app =
                    new ManagedJ2EEApplication(mInstanceName, archiveName,
                            this.getAdminContext());

                mbs.registerMBean(app, appObjectName);
            }
            catch (javax.management.InstanceAlreadyExistsException iae)
            {
                ExceptionUtil.ignoreException(iae);
            }
            catch (Exception e)
            {
                throw new MBeanException(e);
            }
        }
    }

    private void registerEJBModuleMBean(String archiveName)
        throws MBeanException
    {
        ObjectName moduleObjectName =
            ObjectNames.getEjbModuleObjectName(mInstanceName, archiveName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        if (!mbs.isRegistered(moduleObjectName))
        {
            try
            {
                ManagedStandaloneJ2EEEjbJarModule module =
                    new ManagedStandaloneJ2EEEjbJarModule(mInstanceName,
                            archiveName, this.getAdminContext());
                mbs.registerMBean(module, moduleObjectName);
            }
            catch (javax.management.InstanceAlreadyExistsException iae)
            {
                ExceptionUtil.ignoreException(iae);
            }
            catch (Exception e)
            {
                throw new MBeanException(e);
            }
        }
    }

    private void registerWebModuleMBean(String archiveName)
    {
        ObjectName moduleObjectName =
            ObjectNames.getWebModuleObjectName(mInstanceName, archiveName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        if (!mbs.isRegistered(moduleObjectName))
        {
            /*Debug.println("registerWebModuleMBean " +
                          moduleObjectName.toString());
            */
        }
    }

    private void registerConnectorModuleMBean(String archiveName)
    {
        ObjectName moduleObjectName = ObjectNames.getConnectorModuleObjectName(
                                        mInstanceName, archiveName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        if (!mbs.isRegistered(moduleObjectName))
        {
            /*Debug.println("registerConnectorModuleMBean " +
                          moduleObjectName.toString());
            */
        }
    }

    /*
    private void unregisterApplicationMBean(String appName)
        throws javax.management.InstanceNotFoundException
    {
        ObjectName appObjectName =
            ObjectNames.getApplicationObjectName(mInstanceName, appName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        mbs.unregisterMBean(appObjectName);
    }

    private void unregisterModuleMBean(String moduleName, int moduleType)
        throws javax.management.InstanceNotFoundException
    {
        ObjectName moduleObjectName = null;
        switch (moduleType)
        {
            case DeploymentConstants.EJB :
                moduleObjectName = ObjectNames.getEjbModuleObjectName(
                                    mInstanceName, moduleName);
                break;
            case DeploymentConstants.WAR :
                moduleObjectName = ObjectNames.getWebModuleObjectName(
                                    mInstanceName, moduleName);
                break;
            case DeploymentConstants.RAR :
                moduleObjectName = ObjectNames.getConnectorModuleObjectName(
                                    mInstanceName, moduleName);
                break;
        }
        Assert.assertit((moduleObjectName != null), "null object name"); //i18n
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        mbs.unregisterMBean(moduleObjectName);
    }
    */

    ConfigContext getConfigContext(String instanceName)
        throws ConfigException
    {
        /*
        InstanceEnvironment instanceEnvironment =
            new InstanceEnvironment(instanceName);
        */
		/*Everything should be set from the backup file */
        /*
		String fileUrl = instanceEnvironment.getBackupConfigFilePath();
        ConfigContext configContext   =
            ConfigFactory.createConfigContext(fileUrl);
        return configContext;
        */
        return getConfigContext();
    }

    /**
        Get module type string used ModuleDeployEvent using the integer
        constants used by deployment backend
    */

    private String getModuleTypeString(int moduleType)
    {
        String moduleTypeString = null;
        switch (moduleType)
        {
            case DeploymentConstants.EJB    :
                moduleTypeString = ModuleDeployEvent.TYPE_EJBMODULE;
                break;
            case DeploymentConstants.WAR    :
                moduleTypeString = ModuleDeployEvent.TYPE_WEBMODULE;
                break;
            case DeploymentConstants.RAR    :
                moduleTypeString = ModuleDeployEvent.TYPE_CONNECTOR;
                break;
        }
        return moduleTypeString;
    }

    /**
        Convenience method to multicast events. This method is being used to
        events other than ModuleDeployEvent.
    */

    private boolean multicastEvent(int eventType, String entityName) 
        throws DeploymentException
    {
        return multicastEvent(eventType, entityName, null);
    }

    public boolean multicastEvent(int eventType, String entityName,
            String moduleType) throws DeploymentException {
        return multicastEvent(eventType, entityName, moduleType, false);
    }

    /**
        Multicasts the event to the respective listeners. The listeners are
        multicast from here even if the instance is not running. The
        AdminEventMulticaster should take care of it.
        @return true if the instance is up and event was sent and successfully
            handled or if the instance is down, false otherwise.
    */
    public boolean multicastEvent(int eventType, String entityName,
          String moduleType , boolean cascade) throws DeploymentException {
	    return multicastEvent(eventType, entityName, moduleType, cascade, false, null);
    }

                                                                                                                                               
    /**
     *  Multicasts the event to the respective listeners. The listeners are
     *  multicast from here even if the instance is not running. The
     *  AdminEventMulticaster should take care of it.
     *  @return true if the instance is up and event was sent and successfully
     *      handled or if the instance is down, false otherwise.
     */
    public boolean multicastEvent(int eventType, String entityName,
           String moduleType , boolean cascade, boolean forceDeploy, String targetName) throws DeploymentException
    {
        String name = getInstanceName();
        AdminEvent event = null;
        EventBuilder builder = new EventBuilder();

        //XXX Can we put the following 4 lines be done in the EventBuilder?
        EventStack stack = EventContext.getEventStackFromThreadLocal();
        ConfigContext ctx = stack.getConfigContext();
        stack.setTarget(targetName);
        stack.setConfigChangeList(ctx.getConfigChangeList());

        try{
            if (eventType == BaseDeployEvent.APPLICATION_DEPLOYED)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.DEPLOY, entityName, false, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_UNDEPLOYED)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.UNDEPLOY, entityName, cascade, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_REDEPLOYED)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.REDEPLOY, entityName, false, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.MODULE_DEPLOYED)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.DEPLOY, entityName, moduleType, cascade, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.MODULE_UNDEPLOYED)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.UNDEPLOY, entityName, moduleType, cascade, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.MODULE_REDEPLOYED)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.REDEPLOY, entityName, moduleType);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_ENABLE)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.ENABLE, entityName, false, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_DISABLE)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.DISABLE, entityName, false, forceDeploy);
            }
            else if(eventType == BaseDeployEvent.MODULE_ENABLE)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.ENABLE, entityName, moduleType, false, forceDeploy);
            }
            else if(eventType == BaseDeployEvent.MODULE_DISABLE)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.DISABLE, entityName, moduleType, false, forceDeploy);
            } 
            else if(eventType == BaseDeployEvent.APPLICATION_REFERENCED)
            {
                      event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.ADD_REFERENCE, entityName, false, forceDeploy);
            }
            else if(eventType == BaseDeployEvent.APPLICATION_UNREFERENCED)
            {
                    event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.REMOVE_REFERENCE, entityName, false, forceDeploy);
            }
            else if (eventType == CONFIG_CHANGED)
            {
                event = builder.createConfigChangeEvent(targetName, null);
            }
            else
            {
                String msg = 
                    localStrings.getString( "admin.server.core.mbean.config.no_such_event", 
                                            new Integer(eventType) );
                throw new RuntimeException( msg );
            }
        } catch (ConfigException ex) {
            DeploymentException de = new DeploymentException(ex.getMessage());
            de.initCause(ex);
            throw de;
        }

        //set target destination for the event
        if (targetName != null) {
            event.setTargetDestination(targetName);
        }

        /* New for 8.0 - temporary */
        this.transform(new InstanceEnvironment(name));
        /* New for 8.0 - temporary */

        if (event instanceof ApplicationDeployEvent
                || event instanceof ModuleDeployEvent) {
            AdminEventCache.populateConfigChange(getConfigContext(), event);
        }
        int statusCode = getInstanceStatusCode();
        if (statusCode == Status.kInstanceStoppingCode || statusCode == Status.kInstanceNotRunningCode) {
            sLogger.log(Level.INFO, "mbean.inst_down_skip_event", mInstanceName);
            return true;
        }
        if (sLogger.isLoggable(Level.FINEST)) {
            sLogger.log(Level.FINEST, "mbean.event_sent", event.getEventInfo());
        } else {
            sLogger.log(Level.INFO, "mbean.send_event", event.toString());
        }

        AdminEventResult multicastResult =
                AdminEventMulticaster.multicastEvent(event);
        sLogger.log(Level.FINE, "mbean.event_res",
                multicastResult.getResultCode());
        sLogger.log(Level.INFO, "mbean.event_reply",
                multicastResult.getAllMessagesAsString());
        boolean eventSuccess = true;
        if (!AdminEventResult.SUCCESS.equals(multicastResult.getResultCode())) {
            AdminEventCache cache =
                    AdminEventCache.getInstance(mInstanceName);
            cache.setRestartNeeded(true);

            // if there is an exception thrown when loading modules
            // rethrow the exception
            AdminEventListenerException ale = null;
            ale = multicastResult.getFirstAdminEventListenerException();
            if (ale != null) {
                sLogger.log(Level.WARNING, "mbean.event_failed", 
                    ale.getMessage());
                DeploymentException de = 
                    new DeploymentException(ale.getMessage());
                de.initCause(ale);
                throw de;
            }
        }
        return eventSuccess;
    }

    public String getInstanceName() {
        return mInstanceName;
    }

    /**
     * Processes the pending events.
     * @param confFilesChanged true if cgi changes were done to init.conf,
     *     or obj.conf
     * @param mimeFilesChanged true if cgi changes were done to mime file(s).
     * @return Returns true if any of the changes require a server restart,
     *     false otherwise. If the instance is not running, the method returns
     *     false. If instance goes down prior to completion of all
     *     notifications, the method returns true.
     */
    private boolean sendNotificationOnApply(boolean confFilesChanged,
            boolean mimeFilesChanged) throws ConfigException {
        ConfigContext context = getConfigContext(mInstanceName);
        AdminEventCache cache =
                AdminEventCache.getInstance(mInstanceName);
        ArrayList changeList = context.getConfigChangeList();
        context.resetConfigChangeList();
        cache.processConfigChangeList(changeList, confFilesChanged,
                mimeFilesChanged);
        ArrayList eventList = cache.getAndResetCachedEvents();
        if (getInstanceStatusCode() != Status.kInstanceRunningCode) {
            sLogger.log(Level.INFO, "mbean.inst_down_skip_event", mInstanceName);
            return false;
        }
        boolean requiresRestart = cache.isInstanceRestartNeeded();
        Iterator iter = eventList.iterator();
        while (iter.hasNext()) {
            AdminEvent event = (AdminEvent)iter.next();
            if (sLogger.isLoggable(Level.FINEST)) {
                sLogger.log(Level.FINEST, "mbean.event_sent",
                        event.getEventInfo());
            } else {
                sLogger.log(Level.INFO, "mbean.send_event", event.toString());
            }
            AdminEventResult result = AdminEventMulticaster.multicastEvent(event);
            sLogger.log(Level.FINE, "mbean.event_res", result.getResultCode());
            sLogger.log(Level.FINEST, "mbean.event_reply",
                    result.getAllMessagesAsString());
            if (!AdminEventResult.SUCCESS.equals(result.getResultCode()))
            {
                requiresRestart = true;
                cache.setRestartNeeded(true);
                // DEBUGing - Mahesh
                sLogger.log(Level.INFO, "applyChanges: AdminEventMulticaster.multiCastEvent indicates serverRestart=true , resultCode=" + result.getResultCode(), event.toString());
                sLogger.log(Level.WARNING, "mbean.notif_failed");
            }
        }
        return requiresRestart;
    }

    /**
     * Deletes a file from the temporary location.
     * Deletes the given file only if it is in the temporary location.
     */
    private void deleteFile(String filePath)
    {
        File f = new File(filePath);
        if (f.exists())
        {
            File parentDir = f.getParentFile();
            File tmpDir = new File(AdminService.getAdminService().
                    getTempDirPath(), mInstanceName);
            /* note that the above call may return a null */
            if (tmpDir != null && tmpDir.equals(parentDir))
            {
                boolean couldDelete = f.delete();
                if (couldDelete)
                {
                    sLogger.log(Level.FINE, "mbean.delete_temp_file_ok", filePath);
                }
                else
                {
                    sLogger.log(Level.INFO, "mbean.delete_temp_file_failed", filePath);
                }
            }
        }
    }

    private boolean isAppExists(String appName, int appType)
    {
        ConfigBean appConfigBean = null;
        try
        {
            ConfigContext serverContext = super.getConfigContext();
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                                    serverContext,
                                    ServerXPathHelper.XPATH_APPLICATIONS);
            if (applicationsConfigBean != null)
            {
                switch (appType)
                {
                    case DeploymentConstants.EAR :
                    {
                        appConfigBean = applicationsConfigBean.
                                            getJ2eeApplicationByName(appName);
                        break;
                    }
                    case DeploymentConstants.EJB :
                    {
                        appConfigBean = applicationsConfigBean.
                                            getEjbModuleByName(appName);
                        break;
                    }
                    case DeploymentConstants.WAR :
                    {
                        appConfigBean = applicationsConfigBean.
                                            getWebModuleByName(appName);
                        break;
                    }
                    case DeploymentConstants.RAR :
                    {
                        appConfigBean = applicationsConfigBean.
                                            getConnectorModuleByName(appName);
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "appexists failed", e); //noi18N
        }
        return (appConfigBean != null);
    }
    
    /**
     * @return true if a module identified with the module name and it's
     * type is already deployed in this app server instance
     */
    private boolean isAppExists(String moduleName, ModuleType moduleType)
    {
        ConfigBean moduleConfigBean = null;
        try
        {
            ConfigContext serverContext = super.getConfigContext();
            Applications applicationsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                                    serverContext,
                                    ServerXPathHelper.XPATH_APPLICATIONS);
            if (applicationsConfigBean != null)
            {
                if (moduleType.equals(ModuleType.EAR))
                {
                    moduleConfigBean = applicationsConfigBean.
                                        getJ2eeApplicationByName(moduleName);
		} else
                if (moduleType.equals(ModuleType.EJB))
                {
                        moduleConfigBean = applicationsConfigBean.
                                            getEjbModuleByName(moduleName);
		} else
                if (moduleType.equals(ModuleType.WAR))
                {
                        moduleConfigBean = applicationsConfigBean.
                                            getWebModuleByName(moduleName);
		} else
                if (moduleType.equals(ModuleType.RAR))
                {
                        moduleConfigBean = applicationsConfigBean.
                                            getConnectorModuleByName(moduleName);
		}
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "appexists failed", e); //noi18N
        }
        return (moduleConfigBean != null);
    }
    


    /**
     * Checks the debug-enabled attribute of java-config in server.xml.
     */
    private final boolean isDebug()
    {
        boolean isDebug = false;
        try
        {
            JavaConfig jvmConfig = getJavaConfigBean();
            String value = jvmConfig.getAttributeValue(
                                ServerTags.DEBUG_ENABLED);
            if (value != null)
            {
                isDebug = Boolean.valueOf(value).booleanValue();
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.is_debug_failed", e);
        }
        return isDebug;
    }

    /**
     * Sets the debug-enabled attribute of the java-config element.
     */
    private final void setDebug(boolean debug) throws Exception
    {
        JavaConfig  jvmConfig   = getJavaConfigBean();
        String      value       = String.valueOf(debug);
        jvmConfig.setAttributeValue(ServerTags.DEBUG_ENABLED, value);
    }

    /**
     * Sets the debug-options attribute of the java-config element.
     */
    private final void setDebugOptions(String options) throws Exception
    {
        JavaConfig jvmConfig = getJavaConfigBean();
        jvmConfig.setAttributeValue(ServerTags.DEBUG_OPTIONS, options);
    }

    /**
     * Gets the debug-options attribute of the java-config element.
     */
    private final String getDebugOptions() throws Exception
    {
        JavaConfig jvmConfig = getJavaConfigBean();
        return jvmConfig.getAttributeValue(ServerTags.DEBUG_OPTIONS);
    }

    /**
     * Gets the default value of the debug-options attribute.
     */
    private final String getDefaultDebugOptions() throws Exception
    {
        return JavaConfig.getDefaultAttributeValue(ServerTags.DEBUG_OPTIONS);
    }

    private final int getDebugPort() throws Exception
    {
        int debugPort = -1;
        final String debugOptions = getDebugOptions();
        if (debugOptions != null)
        {
            int index = debugOptions.indexOf("-Xrunjdwp");
            if (index >= 0)
            {
                final String jdwpOption = debugOptions.substring(index);
                index = jdwpOption.indexOf("address=");
                if (index >= 0)
                {
                    final String nvPairs = jdwpOption.substring(index);
                    StringTokenizer optionTok =
                            new StringTokenizer(nvPairs, "=, ");
                    optionTok.nextToken(); //Skip 'address' token
                    String portValue =  optionTok.nextToken();
                    debugPort = Integer.parseInt(portValue);
                }
            }
        }
        return debugPort;
    }

    /**
     * Sets the debug port in debug-options attribute.
     */
    private final void setDebugPort(int port) throws Exception
    {
        StringBuffer sb = new StringBuffer();
        String debugOptions = getDefaultDebugOptions();
        StringTokenizer strTok = new StringTokenizer(debugOptions, " ");
        while (strTok.hasMoreTokens())
        {
            String option = strTok.nextToken();
            sb.append(option);
            if (option.startsWith("-Xrunjdwp"))
            {
                sb.append(",address=" + port);
            }
            if (strTok.hasMoreTokens())
            {
                sb.append(' ');
            }
        }
        debugOptions = sb.toString();
        //Remove the following statement once dtd is changed
        //to include these as default options.
        debugOptions += " -Xnoagent -Djava.compiler=NONE";
        sLogger.log(Level.INFO, "mbean.debug_options", debugOptions);
        setDebugOptions(debugOptions);
    }

    private final JavaConfig getJavaConfigBean() throws Exception
    {
//ms1        Server server = (Server) super.getBaseConfigBean();
//ms1        assert server != null;
        Config          config  = (Config) super.getConfigBeanByXPath(ServerXPathHelper.XPATH_CONFIG);
        JavaConfig jvmConfig = config.getJavaConfig();
        jvmConfig.setConfigContext(super.getConfigContext());
        return jvmConfig;
    }


    // ****************************************************************************
    //Security Realms keyfile operations
    // ****************************************************************************
    private FileRealm getInstanceRealmKeyFile() throws MBeanConfigException
    {
        InstanceEnvironment env = new InstanceEnvironment(mInstanceName);
        try
        {
            return new FileRealm(env.getBackupRealmsKeyFilePath());
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
        catch(NoSuchRealmException nsr)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_realm", mInstanceName);
            throw new MBeanConfigException(nsr.getMessage());
        }

    }
    private void saveInstanceRealmKeyFile(FileRealm realm) throws MBeanConfigException
    {
        InstanceEnvironment env = new InstanceEnvironment(mInstanceName);
        try
        {
            final String filePath = env.getBackupRealmsKeyFilePath();
            sLogger.log(Level.INFO, "filerealm.write", filePath);
            realm.writeKeyFile(filePath);
        }
        catch(IOException ioe)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.realm_io_error", mInstanceName);
            sLogger.log(Level.WARNING, "filerealm.writeerror", ioe);
            throw new MBeanConfigException(ioe.getMessage());
        }
    }


    private String[] convertEnumerationToStringArray(Enumeration e)
    {
        ArrayList list = new ArrayList();
        while(e.hasMoreElements())
            list.add(e.nextElement());
        return (String[])list.toArray(new String[list.size()]);
    }

    /**
     * Returns names of all the users from instance realm keyfile
     */
    public String[] getUserNames() throws MBeanConfigException
    {
        FileRealm realm = getInstanceRealmKeyFile();
        try
        {
            return convertEnumerationToStringArray(realm.getUserNames());
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
    }

    /**
     * Returns names of all the groups from the instance realm keyfile
     */
    public String[] getGroupNames() throws MBeanConfigException
    {
        FileRealm realm = getInstanceRealmKeyFile();
        try
        {
            return convertEnumerationToStringArray(realm.getGroupNames());
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
    }

    /**
     * Returns the name of all the groups that this user belongs to from the instance realm keyfile
     */
    public String[] getUserGroupNames(String userName) throws MBeanConfigException
    {
        FileRealm realm = getInstanceRealmKeyFile();
        try
        {
            return convertEnumerationToStringArray(realm.getGroupNames(userName));
        }
        catch(NoSuchUserException nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
    }

    /**
     * Adds new user to file realm. User cannot exist already.
     */
    public void addUser(String userName, String password, String[] groupList) throws MBeanConfigException
    {
        FileRealm realm = getInstanceRealmKeyFile();
        try
        {
            realm.addUser(userName, password, groupList);
            saveInstanceRealmKeyFile(realm);
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
        catch(IASSecurityException ise)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.security_exception", mInstanceName, userName, bre.getMessage());
            throw new MBeanConfigException(ise.getMessage());
        }

    }

    /**
     * Remove user from file realm. User must exist.
     */
    public void removeUser(String userName) throws MBeanConfigException
    {
        FileRealm realm = getInstanceRealmKeyFile();
        try
        {
            realm.removeUser(userName);
            saveInstanceRealmKeyFile(realm);
        }
        catch(NoSuchUserException nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
    }

    /**
     * Update data for an existing user. User must exist. This is equivalent to calling removeUser() followed by addUser().
     */
    public void updateUser(String userName, String password, String[] groupList) throws MBeanConfigException
    {
        FileRealm realm = getInstanceRealmKeyFile();
        try
        {
            realm.updateUser(userName, userName, password, groupList);
            saveInstanceRealmKeyFile(realm);
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
        catch(NoSuchUserException nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
        catch(IASSecurityException ise)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.security_exception", mInstanceName, userName, bre.getMessage());
            throw new MBeanConfigException(ise.getMessage());
        }
    }

    /**
     * this private method is for testing deployed file or directory
     * throws exception if null-name or wrong type
     */
    private void testDeployedFile(String archiveName, boolean bDirectory) throws DeploymentException
    {
        if(archiveName==null)
        {
            String msg =  localStrings.getString( "admin.server.core.mbean.config.deploy_null_name");
            throw new DeploymentException(msg);
        }
        File f = new File(archiveName);
        if(bDirectory && !f.isDirectory())
        {
            String msg =  localStrings.getString( "admin.server.core.mbean.config.deploy_not_directory", archiveName);
            throw new DeploymentException(msg);
        }
        else
            if(!bDirectory && !f.isFile())
            {
                String msg =  localStrings.getString( "admin.server.core.mbean.config.deploy_not_file", archiveName);
                throw new DeploymentException(msg);
            }
    }

    /**
     * Returns Instance dir path
     */
    public String getInstanceRoot()
    {
        InstanceEnvironment env = new InstanceEnvironment(mInstanceName);
        return env.getInstancesRoot();
    }

    /** This method checks if any of the virtual servers has the given web
     * module as default-web-module. If yes, it throws exception.
     * @param webModuleName the name of the web module.
     * @throws ConfigException if any of the virtual servers has this web
     * module as default-web-module.
     */
    private void checkWebModuleReferences(String webModuleName)
        throws ConfigException
    {
        ArrayList virtualServerIds = new ArrayList();

        ConfigContext context   = super.getConfigContext();
//ms1        Server      rootElement = ServerBeansFactory.getServerBean(context);
        Config          config  = (Config) super.getConfigBeanByXPath(ServerXPathHelper.XPATH_CONFIG);
        HttpService httpService = config.getHttpService();
            VirtualServer[] virtualServers = httpService.getVirtualServer();
            for (int j = 0; j < virtualServers.length; j++) 
            {
                VirtualServer aServer   = virtualServers[j];
                String defWebModule     = aServer.getDefaultWebModule();
                if ((defWebModule != null) && 
                    (defWebModule.equals(webModuleName)))
                {
                    virtualServerIds.add(aServer.getId());
                }
            }
        if (!virtualServerIds.isEmpty())
        {
            throw new ConfigException(localStrings.getString(
            "admin.server.core.mbean.config.def_web_module_refs_exist",
            virtualServerIds.toString(), webModuleName));
        }
    }

    private void chownDir(File dir, String user) {
        if (dir == null || user == null || user.trim().equals("")) {
            return;
        }
        String err = null;
        /*installConfig is removed and we need better alternative */
        /*
        installConfig cfg = new installConfig();
        err = cfg.chownDir(dir.getAbsolutePath(), user);
        if (err != null) {
            sLogger.log(Level.WARNING, err);
        }
        */
    }
    
    // BEGIN S1AS8_CONNECTORS_CLI_COMMANDS
    public void addAdminObject(String type, Properties properties, String raName, String jndiName) 
        throws AFResourceException
    {
        /*
        ArgChecker.checkValid(id, "id",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(datasourceClassName, "datasourceClassName",
                              StringValidator.getInstance()); //noi18n
        */

        try
        {
            sLogger.log(Level.FINE, "mbean.create_admin_object_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            AdminObjectResource resource = new AdminObjectResource();
            resource.setJndiName(jndiName);
            resource.setResType(type);
            resource.setResAdapter(raName);
            resourcesBean.addAdminObjectResource(resource);
            // Add the property elements.
            Enumeration e = properties.keys();
            String n, v;
            ElementProperty el = null;
            while (e.hasMoreElements()) {
                n = (String) e.nextElement();
                v = (String) properties.get(n);
                el = new ElementProperty();
                el.setName(n);
                el.setValue(v);
                resource.addElementProperty(el);                
            }
	    createResourceRef(jndiName);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public void deleteAdminObject(String jndiName)
    throws AFResourceException
    {
        /*
        ArgChecker.checkValid(poolName, "poolName",
                              StringValidator.getInstance()); //noi18n
         **/
        sLogger.log(Level.FINE, "mbean.delele_admin_object", jndiName);
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.AdminObjectResource resource =
                resourcesBean.getAdminObjectResourceByJndiName(jndiName);

            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                throw new Exception( msg );
            }
            resourcesBean.removeAdminObjectResource(resource);
	    deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_connector_connection_pool_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }
    
    public String[] listAdminObjects() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.AdminObjectResource[] resources = 
                resourcesBean.getAdminObjectResource();
            if (resources != null)
            {
                sa = new String[resources.length];
                for(int i=0; i<resources.length; i++)
                {
                    sa[i] = resources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }
    
    
    

    public void createConnectorConnectionPool(String raName, String conDefn, String steadyPoolSize,
                String maxPoolSize, String maxWaitTime, String poolResizeQty, String idleTimeout, 
                Boolean failAllConns, Properties properties, String poolName) 
        throws AFResourceException
    {
        /*
        ArgChecker.checkValid(id, "id",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(datasourceClassName, "datasourceClassName",
                              StringValidator.getInstance()); //noi18n
        */

        try
        {
            sLogger.log(Level.FINE, "mbean.create_connector_connection_pool", poolName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool conPool = 
                new com.sun.enterprise.config.serverbeans.ConnectorConnectionPool();
            conPool.setResourceAdapterName(raName);
            conPool.setConnectionDefinitionName(conDefn);
            if (steadyPoolSize != null) conPool.setSteadyPoolSize(steadyPoolSize);
            if (maxPoolSize != null) conPool.setMaxPoolSize(maxPoolSize);
            if (maxWaitTime != null) conPool.setMaxWaitTimeInMillis(maxWaitTime);
            if (poolResizeQty != null) conPool.setPoolResizeQuantity(poolResizeQty);
            if (idleTimeout != null) conPool.setIdleTimeoutInSeconds(idleTimeout);
            if (failAllConns != null) conPool.setFailAllConnections(failAllConns.booleanValue());
            conPool.setName(poolName);
            resourcesBean.addConnectorConnectionPool(conPool);
            // Add the property elements.
            if (properties != null) {
                Enumeration e = properties.keys();
                String n, v;
                ElementProperty el = null;
                while (e.hasMoreElements()) {
                    n = (String) e.nextElement();
                    v = (String) properties.get(n);
                    el = new ElementProperty();
                    el.setName(n);
                    el.setValue(v);
                    conPool.addElementProperty(el);                
                }
            }
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }
    
    public void deleteConnectorConnectionPool(String poolName)
        throws AFResourceException
    {
        ArgChecker.checkValid(poolName, "poolName",
                              StringValidator.getInstance()); //noi18n
        sLogger.log(Level.FINE, "mbean.delele_connector_connection_pool", poolName);
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool resource =
                resourcesBean.getConnectorConnectionPoolByName(poolName);

            if (resource == null)
            {
	        /*
		String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", poolName );
                throw new Exception( msg );
	        */
            }
            resourcesBean.removeConnectorConnectionPool(resource);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_connector_connection_pool_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }

    public String[] listConnectorConnectionPools() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool[] connectionPools = 
                resourcesBean.getConnectorConnectionPool();
            if (connectionPools != null)
            {
                sa = new String[connectionPools.length];
                for(int i=0; i<connectionPools.length; i++)
                {
                    sa[i] = connectionPools[i].getName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }
    
    /**
     * Registers a connector resource with the given jndiName & properties.
     */
    public void createConnectorResource(String   jndiName, String poolName, String description,
                                   Boolean enabled, Properties properties   )
        throws AFResourceException
    {
        /*
        ArgChecker.checkValid(jndiName, "jndiName",
                              StringValidator.getInstance()); //noi18n
        ArgChecker.checkValid(poolName, "poolName",
                              StringValidator.getInstance()); //noi18n
         */
        try
        {
            sLogger.log(Level.FINE, "mbean.create_resource", jndiName);
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            /*
            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool poolResource =
                resourcesBean.getConnectorConnectionPoolByName(poolName);   
            if (poolResource == null)
                throw new AFResourceException("mbean.create_resource : non-existent poolname : " + poolName);
             */
                
            com.sun.enterprise.config.serverbeans.ConnectorResource resource = 
                new com.sun.enterprise.config.serverbeans.ConnectorResource();
            resource.setJndiName(jndiName);
            resource.setPoolName(poolName);
            if (description != null) resource.setDescription(description);
            //resource.setEnabled(true);
            resourcesBean.addConnectorResource(resource);
            // Add the property elements
            if (properties != null) {
                Enumeration e = properties.keys();
                String n, v;
                ElementProperty el = null;
                while (e.hasMoreElements()) {
                    n = (String) e.nextElement();
                    v = (String) properties.get(n);
                    el = new ElementProperty();
                    el.setName(n);
                    el.setValue(v);
                    resource.addElementProperty(el);
                }
            }
	    createResourceRef(jndiName);
            serverContext.flush();

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.create_resource_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }
    
    public void deleteConnectorResource(String jndiName)
        throws AFResourceException
    {
        /*
        ArgChecker.checkValid(poolName, "poolName",
                              StringValidator.getInstance()); //noi18n
         */
        sLogger.log(Level.FINE, "mbean.delete_connector_resource", jndiName);
        try
        {
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.ConnectorResource resource =
                resourcesBean.getConnectorResourceByJndiName(jndiName);

            if (resource == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_such_resource", jndiName );
                throw new Exception( msg );
            }
            resourcesBean.removeConnectorResource(resource);
	    deleteResourceRef(jndiName);
            serverContext.flush();
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.delete_connector_connection_pool_failed", e);
            throw new AFResourceException(e.getLocalizedMessage());
        }
    }
    
    public String[] listConnectorResources() throws AFException
    {
        String[] sa = null;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_components");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.ConnectorResource[] resources = 
                resourcesBean.getConnectorResource();
            if (resources != null)
            {
                sa = new String[resources.length];
                for(int i=0; i<resources.length; i++)
                {
                    sa[i] = resources[i].getJndiName();
                }
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        return sa;
    }    
    /* temporary method */
    private void transform(InstanceEnvironment ie) {
        if (PORT_DOMAIN_TO_SERVER) {
            final String domainXMLPath = ie.getConfigFilePath();
            final String serverXMLPath = java.lang.System.getProperty(Constants.IAS_ROOT) 
                + "/" 
                + ie.getName()
                + "/config/" 
                + ie.kServerXMLFileName;
            new Domain2ServerTransformer(domainXMLPath, serverXMLPath).transform();
        }
         
    }
    /* temporary method */
    public String[] listConnectorJMSResources(String type) throws AFException
    {
        Vector v = new Vector();
        String[] sa = null;
        int j = 0;
        try
        {
            sLogger.log(Level.FINE, "mbean.list_jms_resources");
            ConfigContext serverContext = getConfigContext(mInstanceName);
            Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            // Look for connector-resource elements
            if (type == null || type.equals("javax.jms.TopicConnectionFactory") ||
                type.equals("javax.jms.QueueConnectionFactory")) {
                com.sun.enterprise.config.serverbeans.ConnectorResource[] resources =
                    resourcesBean.getConnectorResource();
                if (resources != null)
                {
                    sa = new String[resources.length];
                    for(int i=0; i<resources.length; i++)
                    {
                        String jndiName = resources[i].getJndiName();
                        String defPoolName = ConnectorRuntime.getRuntime().getDefaultPoolName(jndiName);
                        if (resources[i].getPoolName().equals(defPoolName) ) {
                            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool poolResource =
                                    resourcesBean.getConnectorConnectionPoolByName(defPoolName);
                            if (type == null || (poolResource != null && poolResource.getConnectionDefinitionName().equals(type))) {
                                v.addElement(resources[i].getJndiName());
                             }
                        }
                    }
                }
            }
            // Look for admin-object elements
            if (type == null || type.equals("javax.jms.Topic") ||
                type.equals("javax.jms.Queue")) {
                com.sun.enterprise.config.serverbeans.AdminObjectResource[] resources =
                    resourcesBean.getAdminObjectResource();
                if (resources != null)
                {
                    sa = new String[resources.length];
                    for(int i=0; i<resources.length; i++)
                    {
                        if (type == null || resources[i].getResType().equals(type)) {
                            v.addElement(resources[i].getJndiName());
                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.list_jms_resources_failed", e);
            throw new AFException(e.getLocalizedMessage());
        }
        sa = new String[v.size()];
        for (int k = 0; k < sa.length; k++)
                sa[k] = (String) v.elementAt(k);
        return sa;
    }



}
