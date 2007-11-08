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
import java.net.InetAddress;
import java.net.UnknownHostException;

//iAS imports
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.server.Constants;

/**
	@author  kedar
	@version 1.0
*/

/**
	A class that defines the Server Instance so that it can be created.
	Identifies all the required attributes of an (iAS) Instance.
    Depends upon the value of Install Root ie. "com.sun.aas.instanceRoot"
*/

public class InstanceDefinition 
{
	public	static final int		DEFAULT_PORT	    = 80;
	public	static final int		DEFAULT_JMS_PORT	= 7676;

	public	static final String		DEFAULT_JMS_USER	= "admin";
	public	static final String		DEFAULT_JMS_PW	    = "admin";

	public	static final String		SPACE			    = " ";
	public	static final String		WINDOWS_BIN_EXT		= ".exe";
	public	static final String		BIN_DIR_NAME		= "bin";
	public	static final String		LIB_DIR_NAME		= "lib";

    private		String			mJavaHome		= System.getProperty("java.home");
	private		String			mImqHome		= null;
 	private		String			mServerName		= null;
	private		int				mHttpPort		= DEFAULT_PORT;
	private		String			mIdentifier		= null;
	private		String			mMailHost		= null;
	private		String			mUser			= null;
	private		String			mDocRoot		= null;
	private		String			mPortString		= null;

    private     int             mJMSPort        = DEFAULT_JMS_PORT;
	private		String			mJMSPortString	= null;
	private		String			mJMSUser        = null;
	private		String			mJMSPasswd      = null;

    /*
        The cases are different for Windows and non-Windows platforms. 
        For Non-Windows platforms, starting an instance in secure and
        non-secure mode is different than that on windows platform.
        For Non-windows platforms, the startserv/stopserv scripts in
        addition to the gettokens executable is required.
        Also the restart of a server instance is possible only on
        Non-windows platforms.
    */
	
	public static final String			UNIX_START_COMMAND_NAME	        = "startserv";
	public static final String			UNIX_STOP_COMMAND_NAME	        = "stopserv";
	public static final String			UNIX_GETTOKENS_COMMAND_NAME 	= "gettokens";
	public static final String			UNIX_RESTART_COMMAND_NAME	    = "restartserv";

    
    /*
        On Windows platform, unlike the non-windows platforms, there is different
        executable that is used. The name of that executable is startsec.exe. This
        executable calls into iws source code for pcontrol.cpp which uses shared
        memory for passing the key-pair (db) password and other passwords to the
        server process.
        Note that this code runs in admin-server's memory and it forks startsec.exe
        which gets its own environment. The most important variable that is necessary
        for startsec.exe to run is ADMSERV_ROOT which points to the parent directory 
        of the folder where the entire directory structure of admin-server (and hence
        all the other instances in same domain) is stored.
    */
	public static final String			WIN_START_COMMAND_NAME	        = "startsec.exe";
	public static final String			WIN_STOP_COMMAND_NAME	        = "stopserv.bat";
	public static final String			WIN_GETTOKENS_COMMAND_NAME	    = "gettokens.exe";

    public final String         JMS_NODE_PATH = ServerXPathHelper.XPATH_JMS_SERVICE;

	/** 
		Creates new InstanceDefinition.
		@param serverName String representing the fully qualified hostName e.g. www.sun.com
		@param httpPort is integer specifying port at which this server should start listening to requests.
		@param identifier String that is the server-id e.g. prod_server.
		@param mailHost is the name of the mail host.
		@param user is the name of user with which we can start this instance. (Unix/Linux)
		@param docRoot is the absolute location for the webserver docroot.
		@param jmsPort integer represeting the port of jms-service.
		@param jmsUser user name to connect to the jms-service.
		@param jmsPasswd password to connect to the jms-service.
	*/
	
    public InstanceDefinition(String serverName, int httpPort, 
		String identifier, String mailHost, String user, String docRoot,
        int jmsPort, String jmsUser, String jmsPasswd) {
		initialize(serverName, httpPort, identifier, mailHost, user, docRoot,
                   jmsPort, jmsUser, jmsPasswd);
    }

	/**
		Creates new InstanceDefinition with the given parameters.
		@param id String representing the unique id of the server. (e.g. prod_server)
		@param port is the HTTP port.
		Note that the servername would default to the local machine's IP address,
		mailhost will default to the "localhost", server user will default to
		current user and docroot will default to <ias-install>/docs
	*/
	public InstanceDefinition (String id, int port)	{
        if (id == null || port <= 0)
            throw new IllegalArgumentException(Localizer.getValue(ExceptionType.ILLEGAL_PORT));
		String serverName	= createLocalHostName();
		String mailHost		= createLocalHostName();
		String user			= System.getProperty("user.name");
		String docRoot		= ServerManager.INSTANCE_CFG_ROOT
                + "/" + ServerManager.DOC_DIR_NAME;
		initialize(serverName, port, id, mailHost, user, docRoot,
                   DEFAULT_JMS_PORT, DEFAULT_JMS_USER, DEFAULT_JMS_PW);
	}
	
	private void initialize(String serverName, int httpPort, String identifier,
		String mailHost, String user, String docRoot, int jmsPort, 
        String jmsUser, String jmsPasswd) {
        if (serverName  == null||   identifier  == null||
            mailHost    == null||   user        == null||
            user        == null||   docRoot     == null|| 
            jmsUser     == null||   jmsPasswd   == null ) {
            throw new IllegalArgumentException();
        }
        if (httpPort <= 0 || jmsPort <= 0) {
            throw new IllegalArgumentException();
        }
		mServerName		= serverName;
		mHttpPort		= httpPort;
		mIdentifier		= identifier;
		mMailHost		= mailHost;
		mUser			= user;
		mDocRoot		= docRoot;
		mPortString		= "" + mHttpPort;
        mJMSPort        = jmsPort;
        mJMSPortString  = jmsPort +"";
        mJMSUser        = jmsUser;
        mJMSPasswd      = jmsPasswd;
	}

	public String getID() {
		return mIdentifier;
	}
	
    public int getPort() {
        return mHttpPort;
    }

    public String getServerName() {
        return mServerName;
    }

    public String getAdminJavaHome() throws ConfigException {
        ConfigContext configContext;
        InstanceEnvironment instanceEnvironment = 
               new InstanceEnvironment(ServerManager.ADMINSERVER_ID);
        String fileUrl  = instanceEnvironment.getConfigFilePath();
        configContext   = ConfigFactory.createConfigContext(fileUrl);
        ConfigBean configbean = ConfigBeansFactory.getConfigBeanByXPath(
                configContext, ServerXPathHelper.XPATH_JAVACONFIG);
        mJavaHome = configbean.getAttributeValue(ServerTags.JAVA_HOME);
        return mJavaHome;
    }   

    public String getDocRoot()
    {
        return ( mDocRoot );
    }

	  // The following is copied in:
	  // tools.CreateInstanceCommand, tools.CreateDomainCommand,
	  // instance.InstanceDefinition.
	  // We need to refactor this method into one common class. - It
	  // has not state, so could be refactored as a static method.
  
	private String createLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException ue) {
		  return "localhost";
		}
	}
   
	
	/**
		Returns the complete path of the <code> start </code>
		command for starting this instance. Note that it returns the
		platform-specific string that could be exec'ed.
	 
		@return String representing path of start command for this instance.
	*/

	public String[] getStartCommand() {
		String[] startCommand = null;
        /* Note that for windows, the startup executable is stored
           at the install-root - one per installation. startsec.exe
           just takes the instance name as the only parameter.
        */
		if (OS.isWindows()) {
            startCommand = getCompleteWindowsStartCommand();
		}
		else {
        /* Note that for non-windows platforms, the startup executable is stored
           at the instance directory - one per instance. startserv
           script does not take any parameter.
        */
            startCommand = getCompleteNonWindowsStartCommand();
		}
		return ( startCommand );
	}
    
    /**
     * Returns the windows start command. Note that it is 
     * [install-root]/bin/startsec.exe.
    */
    private String[] getCompleteWindowsStartCommand()
    {

        String[] names = new String[] {
            System.getProperty(Constants.INSTALL_ROOT), /* install-root */
            BIN_DIR_NAME,                               /* "bin" */
            WIN_START_COMMAND_NAME                      /* "startsec.exe" */
        };
        String programName = StringUtils.makeFilePath(names, false);

        /* startsec requires server-id as the first(only) parameter */

        return ( new String[] {
            programName,                                /* path to strtsec */
            mIdentifier,                                /* "server-id" */
            System.getProperty(Constants.IAS_ROOT),     /* path to domain root */
        } );
    }
    
    
    /* Returns the start command for non windows platforms.
       This is <instance-root>/bin/startserv
    */
    private String[] getCompleteNonWindowsStartCommand()
    {
        String[] names = new String[] {
            System.getProperty(Constants.IAS_ROOT),     /* upto domain */
            BIN_DIR_NAME,                               /* "bin" */
            UNIX_START_COMMAND_NAME                     /* "startserv" */
        };
        String programName = StringUtils.makeFilePath(names, false);
        
        return ( new String[]{programName} );
    }
	/**
		Returns the complete path of the <code> gettokens </code>
		command for getting security tokens of this instance. 
        It returns the array of Strings which form the complete
        command line.
	 
		@return String[] representing getSecurityTokensCommand
	*/

	public String[] getGetSecurityTokensCommand() {
		String[] command = null;
        String onlyCommand = null;
		if (OS.isWindows()) {
            onlyCommand = getWindowsSecTokensCommand();
        }
		else {
            onlyCommand = getNonWindowsSecTokensCommand();
		}
        
        command = new String[] {
            onlyCommand,                                /* path of gettokens     */
            mIdentifier,                                /* "instance-id": param1 */
            System.getProperty(Constants.IAS_ROOT),     /* path to domain root */
        };
		return ( command );
	}
    
    /* Returns the fully qualified name of the gettokens command for
       windows, where it is in install-root/bin.*/
    private String getWindowsSecTokensCommand()
    {
        String[] names = new String[] {
            System.getProperty(Constants.INSTALL_ROOT),     /* install-root */
            BIN_DIR_NAME,                                   /* "bin"        */
            WIN_GETTOKENS_COMMAND_NAME                      /* "gettokens.exe" */
        }; 

        return ( StringUtils.makeFilePath(names, false) );
    }
    /* Returns the fully qualified name of the gettokens command for
       non-windows, where it is in install-root/lib.*/

    private String getNonWindowsSecTokensCommand()
    {
        String[] names = new String[] {
            System.getProperty(Constants.INSTALL_ROOT),     /* install-root */
            LIB_DIR_NAME,                                   /* "lib"        */
            UNIX_GETTOKENS_COMMAND_NAME                     /* "gettokens" */ 
        };

        return ( StringUtils.makeFilePath(names, false) );
    }

	/**
		Returns the complete path of the <code> stop </code>
		command for stopping this instance. Note that it returns the
		platform-specific string that could be exec'ed.
	 
		@return String representing path of stop command for this instance.
	*/

	public String[] getStopCommand() {
		String[] stopCommand    = new String[1];

        String command          = null; 
		if (OS.isWindows()) {
			command = WIN_STOP_COMMAND_NAME;
		}
		else {
			command = UNIX_STOP_COMMAND_NAME;
		}
        String[] names = new String[] {
            System.getProperty(Constants.IAS_ROOT),     /*upto a domain */
            BIN_DIR_NAME,                               /* "bin"        */
            command                                     /* "stopserv" or "stopserv.bat"*/
        };

		stopCommand[0] = StringUtils.makeFilePath(names, false);
		
		return ( stopCommand );
	}

	/**
		Returns the complete path of the <code> restart </code>
		script for restarting this instance. Note that it returns the
		platform-specific string that could be exec'ed.
	 
		@return String representing path of restart command for this 
        instance.
	*/

	public String[] getRestartCommand() {
        if (OS.isWindows()) {
            throw new UnsupportedOperationException(Localizer.getValue(ExceptionType.ILLEGAL_RESTART));
        }
        String[] restartCommand = new String[1];

		String[] names = new String[] { 
            System.getProperty(Constants.IAS_ROOT),             /* upto a domain */
            BIN_DIR_NAME,                                       /* "bin" */
            UNIX_RESTART_COMMAND_NAME                           /* "restartserv" */
        };
		restartCommand[0] = StringUtils.makeFilePath(names, false);

		return ( restartCommand );
	}

	/**
		Overridden definition of toString for this Instance Definition.
		Shows the ServerName, Port, Identifier, MailHostName, User and
		Doc Root for the instance.
	*/
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(mServerName);
		sb.append(SPACE + mPortString);
		sb.append(SPACE + mIdentifier);
		sb.append(SPACE + mMailHost);
		sb.append(SPACE + mUser);
		sb.append(SPACE + mDocRoot);
		
		return ( sb.toString() );
	}

    /**
     * Sets the user (owner) of the instance.
     */
    public void setUser(String user)
    {
        if ((user != null) && (user.length() > 0))
        {
            mUser = user;
        }
    }

    /**
     * Getter for the instance-user instance variable.
     */
    public String getUser()
    {
        return mUser;
    }
}
