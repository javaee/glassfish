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

package com.sun.enterprise.instance;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//iAS imports
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.ExecException;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.OS;
/*installConfig is removed and we need better alternative */
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.admin.common.InitConfFileBean;

/**
 * A class that acts as a helper when one has to take care of
 * various instances. Such a situation would probably occur in case
 * of administration as it has to administer all the instances in the
 * given install. It is supposed to be a singleton.
 * @author Jeet Kaul - originially.
 * @author Kedar Mhaswade.
 */

public class ServerManager {

	public static final String		INSTALL_ROOT	=
		System.getProperty(Constants.INSTALL_ROOT);
    public static final String      INSTANCE_CFG_ROOT =
        System.getProperty(Constants.IAS_ROOT);
    public static final String      DOC_DIR_NAME = "docroot";
	public static final String		ADMINSERVER_ID	= "admin-server";

	private static final ServerManager	SHARED_INSTANCE = new ServerManager();
    private static Logger sLogger =
            Logger.getLogger(AdminConstants.kLoggerName);

	/** Private constructor so that nobody else creates the instance */

	private ServerManager() {
	}

	/** Static Method to get a reference to the singleton.
	 * @return instance of ServerManager class.
	*/

	public static ServerManager instance(){
		return SHARED_INSTANCE;
	}

	/**
	 * A method to find whether a Server Instance with given name exists
	 * in the install. The instanceID should be the one designated by the user
	 * while creating the instance. An Instance is recognized by the configuration
	 * stored in a folder (on file system) named <strong> https-</strong> id. (This is
	 * for historical reasons). So, there are as many instances as there are
	 * folders beginning with string https-. No other consideration will be
	 * given. This method always ignores admin server.
	 * @param instanceID representing the id of the Server Instance. May not be null.
	 * @return true if the given instance's config directory exists, false otherwise.
	*/

	public boolean instanceExists(String instanceID) {
        //KE: FIXTHIS: We can only tell if an instance exists by looking 
        //at domain.xml
        if (instanceID.equals("server")) {
            return true;
        }
        return false;
	}

	/**
	 * The instance names are not registered in any configuration file for iASSE.
	 * Hence this method scans the folder on disk where installation is done, each time
	 * it is called and returns the instance names. Note that all the instances
	 * are denoted by https-id folders in INSTALL_ROOT. This method returns
	 * an array of all ids. Note that admin server itself is NOT returned
	 * as an Instance.
	 * @return an array of all instance names. An array of zero length, if
	 * there isn't any. Never returns a null.
	 * @throws SecurityException, if there is no read permission to INSTALL_ROOT.
	*/

	public String[] getInstanceNames(boolean countAdmin) {
        //KE: FIXTHIS. We can get instances from domain.xml only.
        return new String[] {"server"};
	}

	/**
	 * Returns the number of instances depending on whether the admin
	 * instance is to be counted.
	 * @param countAdmin true if the returned list should contain admin
	 * @return integer representing the number of server instances.
	*/

	public int getNumInstances(boolean countAdmin) {
		return ( getInstanceNames(countAdmin).length );
	}

	/**
		Creates the given ServerInstance. InstanceDefinition may not be null.
		Gives call to underlying cgi program
		to actually create the new instance of iAS. The creation of instance
		consists of creation of directory structure as per the definition of
		instance.
		Note that this method does an expensive Runtime.exec() internally.

		@param instance the InstanceDefinition pertaining to instance to be created.
		@throws ConfigException if there is any error in arguments and/or the
		underlying program throws an error.
		@throws IllegalArgumentException in case of null instance.
	*/

	public void createServerInstance(InstanceDefinition instance) throws
		IOException, ConfigException
        {
            /*
             * Be very careful with this method. All the paths that are
             * required to be put into the various config files for
             * an instance have forward slashes regardless of the
             * underlying platform.
            */
            /* Removing this entire method as it is not applicable to PE */
            throw new UnsupportedOperationException("createServerInstance - not in PE");
	    }

	/**
		A method to delete the server instance with given name or id.
		Unlike other methods to create, start and stop the instances, this
		method does not depend on other lower level API. The file system folder
		where the instance's configuration resides, is deleted.
        Running instance should be soped before this call (in ServerController).
		On NT the Service needs to be deleted - which is not yet implemented (01/26/02).
		@param instanceName name of the instance(id) to be deleted - may not be null.
		@throws ConfigException if the action encounters some problems.
	*/
	public void deleteServerInstance(String instanceName) throws
		ConfigException {
            /* Removing this entire method as it is not applicable to PE */
        throw new UnsupportedOperationException("deleteServerInstance - not in PE");
	}
	/**
		Starts the given instance. The argument may not be null.
		Gives call to start program inside the proper directory structure of
		that instance (e.g. /export/iplanet/ias7/https-test/start).
		The createServerInstance call has to succeed for this call to work.
		Note that it gives call to an expensive Runtime.exec().
		@param instance the InstanceDefinition pertaining to instance to be created.
		@throws ConfigException if there is any error in arguments and/or the
		underlying program throws an error.
		@throws IllegalArgumentException in case of null instance.
	 */

	public void startServerInstance(InstanceDefinition instance) throws
		ConfigException, RuntimeException {
		startServerInstance(instance, null);
	}

    public void startServerInstance(InstanceDefinition instance, String[] passwords) throws
		ConfigException, RuntimeException {
		if (instance == null) {
			throw new IllegalArgumentException();
		}
		String[] startCommand = instance.getStartCommand();
        String[] inputLines = null;
        if(passwords!=null)
            inputLines = passwords;
        else
            inputLines = new String[]{}; //to provoke stream closing
        //startCommand = startCommand + " " + instance.getID() + " " + INSTALL_ROOT;
        try {
            sLogger.log(Level.FINE, "general.exec_cmd", startCommand[0]);
			ProcessExecutor executor = new ProcessExecutor(startCommand, inputLines);
			executor.execute();
		}
                catch (ExecException ee) {
                    sLogger.log(Level.WARNING, "general.exec_cmd", ee);
                    throw new RuntimeException(Localizer.getValue(ExceptionType.SERVER_NO_START));
                }
		catch (Exception e) {
			throw new ConfigException(e.getMessage());
		}
	}

	/**
		Stops the given instance. The argument may not be null.
		Gives call to stop program inside the proper directory structure of
		that instance (e.g. /export/iplanet/ias7/https-test/stop).
		The createServerInstance call has to succeed for this call to work.
		Note that it gives call to an expensive Runtime.exec().
		@param instance the InstanceDefinition pertaining to instance to be created.
		@throws ConfigException if there is any error in arguments and/or the
		underlying program throws an error.
		@throws IllegalArgumentException in case of null instance.
	 */

	public void stopServerInstance(InstanceDefinition instance) throws
		ConfigException {
		if (instance == null) {
			throw new IllegalArgumentException();
		}
		String stopCommand[] = instance.getStopCommand();
		try {
            sLogger.log(Level.FINE, "general.exec_cmd", stopCommand[0]);
			ProcessExecutor executor = new ProcessExecutor(stopCommand);
			executor.execute();
		}
		catch (Exception e) {
			throw new ConfigException(e.getMessage());
		}
	}

    /**
     */
	public void restartServerInstance(InstanceDefinition instance)
            throws ConfigException {
		if (instance == null) {
			throw new IllegalArgumentException();
		}
		String[] restartCommand = instance.getRestartCommand();
		try {
            sLogger.log(Level.FINE, "general.exec_cmd", restartCommand[0]);
			ProcessExecutor executor = new ProcessExecutor(restartCommand);
			executor.execute();
		}
		catch (Exception e) {
			throw new ConfigException(e.getMessage());
		}
	}

    public String[] getSecurityTokensForInstance(InstanceDefinition instance) throws 
		ConfigException, RuntimeException {
		if (instance == null) {
			throw new IllegalArgumentException();
		}
		String[] command = instance.getGetSecurityTokensCommand();
        String[] inputLines = null;
        try {
            sLogger.log(Level.FINE, "general.gettokens_cmd", command[0]);
			ProcessExecutor executor = new ProcessExecutor(command);
			return executor.execute(true);
		}
                catch (ExecException ee) {
                        throw new RuntimeException(Localizer.getValue(ExceptionType.NO_RECEIVE_TOKENS));
                }
		catch (Exception e) {
			throw new ConfigException(e.getMessage());
		}
	}

    /**
        Returns the path of the mime.types.template file for this installation.
        This templates file is stored in the templates directory.
        @return path of template file for mime.types
    */
    public String getMimeTypesTemplateFilePath() {
        final String libDirName             = "lib";
        final String installDirName         = "install";
        final String templateDirName        = "templates";
        final String mimeTemplateFileName   = "mime.types.template";

        String [] fileNames = new String[] {INSTALL_ROOT, libDirName,
            installDirName, templateDirName, mimeTemplateFileName};
        return ( StringUtils.makeFilePath(fileNames, false) );
    }

    /**
     * This method is a wrapper over the other routine to get the port.
     * It provides logging support and default ports.
     * @param integer that caller indicates to return in case of a failure.
     * @return a free port if available, the passed port otherwise.
    */
    private int getFreePort(int defaultPort) {
        int port = NetUtils.getFreePort();
        if (port == 0) {
            /* log the stuff */
            sLogger.log(Level.SEVERE, "general.free_port_failed");
            return defaultPort;
        }
        else {
            Integer portInteger = new Integer(port);
            sLogger.log(Level.INFO, "general.free_port", portInteger);
            return port;
        }
    }

    /** Method to check whether the given port has clash with the
     *  http ports of other instances.  It checks in the server.xml
     *  of various instances for this.*/

    private boolean portTakenByHTTP(int port) {
        boolean     portTaken   = true;
        String      portString  = "" + port;
        String[]    instances = getInstanceNames(true);
        /* count admin server also for this purpose */
        try {
            for (int i = 0 ; i < instances.length ; i++) {
                String instanceName = instances[i];
                InstanceEnvironment inst =
                        new InstanceEnvironment(instanceName);
                String backURL = inst.getConfigFilePath();
                ConfigContext context = ConfigFactory.
                        createConfigContext(backURL);
                Config rootElement = ServerBeansFactory.getConfigBean(context);

                HttpService httpService = rootElement.getHttpService();
                HttpListener[] httpListeners =
                        httpService.getHttpListener();
                for (int j = 0 ; j < httpListeners.length ; j++) {
                    String aPort = httpListeners[j].getPort();
                    aPort = aPort.trim();
                    sLogger.log(Level.FINE, "port = " + aPort);
                    if (aPort.equals(portString)) {
                        sLogger.log(Level.WARNING,
                                "general.port_occupied", instanceName);
                        return portTaken;
                    }
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "general.port_derivation_failed", e);
        }
        return ( false );
    }

    /**
     * Checks whether this port is occupied by any of the orb listeners.
    */
    private boolean portTakenByORB(int port) {
        boolean     portTaken   = true;
        String      portString  = "" + port;
        String[]    instances = getInstanceNames(true);
        /* count admin server also for this purpose */
        try {
            for (int i = 0 ; i < instances.length ; i++) {
                String instanceName = instances[i];
                InstanceEnvironment inst =
                        new InstanceEnvironment(instanceName);
                String backURL = inst.getConfigFilePath();
                ConfigContext context = ConfigFactory.
                        createConfigContext(backURL);

                Config rootElement = ServerBeansFactory.getConfigBean(context);

                IiopService iiopService = rootElement.getIiopService();
                IiopListener[] iiopListeners = iiopService.getIiopListener();
                for (int j = 0 ; j < iiopListeners.length ; j++) {
                    String aPort = iiopListeners[j].getPort();
                    aPort = aPort.trim();
                    sLogger.log(Level.FINE, "port = " + aPort);
                    if (aPort.equals(portString)) {
                        sLogger.log(Level.WARNING,
                                "general.port_occupied", instanceName);
                        return portTaken;
                    }
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "general.port_derivation_failed", e);
        }
        return ( false );
    }


    /** Checks whether this port is taken by jms provider (imq broker).
     */
    private boolean portTakenByJMS(int port) {
        boolean     portTaken   = true;
        String      portString  = "" + port;
        String[]    instances = getInstanceNames(true);
        /* count admin server also for this purpose */
        try {
            for (int i = 0 ; i < instances.length ; i++) {
                String instanceName = instances[i];
                InstanceEnvironment inst =
                        new InstanceEnvironment(instanceName);
                String backURL = inst.getConfigFilePath();
                ConfigContext context = ConfigFactory.
                        createConfigContext(backURL);
                JmsHost jmsHost = ServerBeansFactory.getJmsHostBean(context);
                String aPort = jmsHost.getPort();
                aPort = aPort.trim();
                sLogger.log(Level.FINE, "port = " + aPort);
                if (aPort.equals(portString)) {
                    sLogger.log(Level.WARNING,
                            "general.port_occupied", instanceName);
                    return portTaken;
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "general.port_derivation_failed", e);
        }
        return ( false );
    }
	/**
	 * Provides the domain name from the domain root
	 */
	public String getDomainName(){
		File domainRoot = new File(INSTANCE_CFG_ROOT);
		return domainRoot.getName();
	}

    public String getInstanceUser(InstanceEnvironment env) throws IOException
    {
        /*
            Need an alternative. PE conf does'nt include init.conf. 
            Ramakanth 04/23/2003
         */
        /*
        if (env == null)
        {
            throw new IllegalArgumentException("env cant be null");
        }
        InitConfFileBean initConf = new InitConfFileBean();
        initConf.readConfig(env.getInitFilePath());
        String instanceUser = initConf.get_mag_var("User");
        return instanceUser;
        */
        return System.getProperty("user.name");
    }

	/* for quick test please uncomment */
/*
	public static void main(String args[])  throws Exception {
	 Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

		logger.log(Level.INFO,"Tests for com.sun.aas.instanceRoot given on command line...");
		ServerManager sm = ServerManager.instance();
		int num = sm.getNumInstances(false);
		logger.log(Level.INFO,"No of instances excluding admin is = " + num);
		String dom = sm.getDomainName();
		System.out.println("Domain is : " + dom+"\n");
		String[] instances = sm.getInstanceNames(false);
		for (int i = 0 ; i < num ; i++) {
			logger.log(Level.INFO,"instance id = " + instances[i]);
		}
		num = sm.getNumInstances(true);
		//logger.log(Level.INFO,"No of instances including admin is = " + num);
		instances = sm.getInstanceNames(true);
		for (int j = 0 ; j < num ; j++) {
			//logger.log(Level.INFO,"instance id = " + instances[j]);
		}
		//logger.log(Level.INFO,"The instance with name ias1 exists: " + sm.instanceExists("ias1"));
        sm.deleteServerInstance("ias1");
        sm.deleteServerInstance("admserv");
	}
*/
    /* for quick test please uncomment */
}
