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

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.tools.ant.taskdefs.optional.sun.appserv;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract class which includes base functionality for Tasks targeting the
 * Sun ONE Application Server 8.  This includes support for the following
 * attributes:
 *
 *   <ul>
 *     <li><i>user</i> -- The username used when logging into the application
 *                        server administration instance.  This attribute is 
 *						  inherited by nested &lt;server&gt; elements.  Defaults to 
 *						  "admin"
 *     <li><i>password</i> -- The password used when logging into the application 
 *                            server administration instance.  This attribute
 *							  is inherited by nested &lt;server&gt; elements
 *     <li><i>host</i> -- Target server for the command(s).  When deploying to a 
 *                        remote server, the fully qualified hostname should be
 *						  used.  This attribute is inherited by nested &lt;server&gt; 
 *						  elements.  Defaults to "localhost"
 *     <li><i>port</i> -- Admin port on the target server.  This attribute is 
 *                        inherited by nested &lt;server&gt; elements.  Defaults to 
 *						  "4848"
 *     <li><i>secure</i> -- This attribute uses SSL/TLS to communicate  with  the
 *                          domain application server.
 *     <li><i>instance</i> -- Target application server instance for the command.  
 *                            This attribute is inherited by nested &lt;server%gt 
 *							  elements.  If not specified, the default instance 
 *							  name will be used
 *     <li><i>sunonehome</i> -- This attribute has been deprecated, use asinstall.dir
 *                            instead.
 *                            The installation directory for the local Sun ONE 
 *                            Application Server 8 installation -- this is 
 *							  used to find the correct administrative classes.  
 *							  If not specified, the task will check to see if 
 *							  the "sunone.home" parameter has been set.  
 *							  Otherwise, the Sun ONE Application Server 8
 *							  admin classes must be on the system classpath
 *     <li><i>asinstalldir</i> -- The installation directory for the local Sun ONE 
 *                            Application Server 8 installation -- this is 
 *							  used to find the correct administrative classes.  
 *							  If not specified, the task will check to see if 
 *							  the "asinstall.dir" parameter has been set.  
 *							  Otherwise, the Sun ONE Application Server 8
 *							  admin classes must be on the system classpath
 *   </ul>
 * <p>
 * In addition, this abstract class provides support for nested &lt;server&gt;
 * elements.  These nested elements enable commands to be executed against 
 * multiple Sun ONE Application Servers in a single Ant command.  Attributes may 
 * be specified in the "root" command element and those values will be used 
 * ("inherited") by all of the nested &lt;server&gt; components.  If an attribute is 
 * specified in both elements, the value specified in the &lt;server&gt; element will 
 * be used.
 *
 * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
 */
public abstract class AppServerAdmin extends Task {
    static Method invokeCLI = null;
    static Class adminMain = null;
    static Class inputsAndOutputs = null;
    static Class systemPropertyConstants = null;
    
    
	/**
	 * This Server instance will store user settings from the "root" command
	 * element.
	 */
	protected Server server;

	/**
	 * The list of servers will store user settings from nested &lt;server&gt;
	 * elements.
	 */
	protected List   servers = new ArrayList();

	/** Sun ONE Application Server 8 installation directory */
    private File   asinstalldir; 

	/**
	 * This attribute is used only for debugging and is not included in the 
	 * task documentation.  When set to "true" the task behaves normally.  When
	 * set to "false", each admin command is logged but not actually executed.
	 */
	private boolean executeCommand = true;

	/** Constants used to invoke the admin utilies using Java reflection */
	private static final String CLASS_INPUTS_AND_OUTPUTS = 
							"com.sun.enterprise.cli.framework.InputsAndOutputs";
	private static final String CLASS_ADMIN_MAIN = 
							"com.sun.enterprise.cli.framework.CLIMain";
	private static final String METHOD_INVOKE_CLI = "invokeCLI";

    LocalStringsManager lsm = new LocalStringsManager();

    private static final String CLASS_SYSTEM_PROPERTY_CONSTANTS = 
							"com.sun.enterprise.util.SystemPropertyConstants";

	/**
	 * Creates a new instance with the infrastructure for running admin commands.
	 */
	public AppServerAdmin() {
		server = getNewServer();
	}

	/**
	 * Sets the username used when logging into the application server 
	 * administration instance.
	 *
	 * @param user The username used to logon
	 */
	public void setUser(String user) {
		server.setUser(user);  // Delegates to server object
	}

	/**
	 * Sets the password used when logging into the application server 
	 * administration instance.
	 *
	 * @param password The password used to logon
	 */
	public void setPassword(String password) {
       final String msg = lsm.getString("DeprecatedAttribute", new Object[] {"password",
         "passwordfile"});
        log(msg, Project.MSG_WARN);
		server.setPassword(password);  // Delegates to server object
	}

	/**
	 * Sets the passwordfile used when logging into the application server 
	 * administration instance.
	 *
	 * @param password The password used to logon
	 */
	public void setPasswordfile(String passwordfile) {
		server.setPasswordfile(passwordfile);  // Delegates to server object
	}

	/**
	 * Sets the hostname where the application server administration instance
	 * is running.
	 *
	 * @param host The host name where the administration instance is running
	 */
	public void setHost(String host) {
		server.setHost(host);  // Delegates to server object
	}

	/**
	 * Sets the port where the application server administration instance is
	 * running.
	 *
	 * @param port The port number of the administration instance
	 */
	public void setPort(int port) {
		server.setPort(port);  // Delegates to server object
	}

    /**
	 * Sets secure option
	 *
	 * @param secure. The secure if true, uses SSL/TLS to communicate 
     *                with the domain application server.
	 */
	public void setSecure(String secure) {
		server.setSecure(secure);  // Delegates to server object
	}


	/**
	 * Sets the application server instance name.  Typically, this is the
	 * "target" for administrative commands.
	 *
	 * @param instance The name of the application server instance
	 */
	public void setInstance(String instance) {
		server.setInstance(instance);  // Delegates to server object
	}

	/**
	 * This attribute is used only for debugging and is not included in the 
	 * task documentation.  When set to "true" the task behaves normally.  When
	 * set to "false", each admin command is logged but not actually executed.
	 *
	 * @param executeCommand Whether or not to execute the admin commands
	 *                       generated by this task
	 */
	public void setExecuteCommand(boolean executeCommand) {
		this.executeCommand = executeCommand;
	}

	/**
	 * Specifies the installation directory for the Sun ONE Application Server
	 * 8.  This may be used if the application server is installed on the 
	 * local machine.
	 *
	 * @param sunonehome The home directory for the user's app server 
	 *                   installation.
	 */
	public void setSunonehome(File sunonehome) {
        final String msg = lsm.getString("DeprecatedAttribute", new Object[] {"sunonehome",
         "asinstalldir"});
        log(msg, Project.MSG_WARN);
        this.asinstalldir = sunonehome;
	}
    
	/**
	 * Specifies the installation directory for the Sun ONE Application Server
	 * 8.  This may be used if the application server is installed on the 
	 * local machine.
	 *
	 * @param asinstalldir The home directory for the user's app server 
	 *                   installation.
	 */
	public void setAsinstalldir(File asinstalldir) {
		this.asinstalldir = asinstalldir;
	}

    
	/**
	 * Returns the asinstalldir attribute specify by in the build script.
	 * If asinstalldir hasn't been explicitly set (using
	 * the <code>setAsinstalldir</code> method), the value stored in the <code>
	 * sunone.home</code> property will be returned.
	 *
	 * @return File representing the app server installation directory.  Returns
	 *         <code>null</code> if the installation directory hasn't been
	 *         explictly set and the <code>sunone.home</code> property isn't set.
     * @throws ClassNotFoundException if asinstalldir is an invalid directory
	 */
	protected File getAsinstalldir() throws ClassNotFoundException {
		if (asinstalldir == null) {
			String home = getProject().getProperty("asinstall.dir");
			if (home != null) {
                asinstalldir = new File(home);
			}
            else {
                home = getProject().getProperty("sunone.home");
                if (home != null)
                {
                    final String msg = lsm.getString("DeprecatedProperty", new Object[] {"sunone.home", "asinstall.dir"});
                    log(msg, Project.MSG_WARN);
                    asinstalldir = new File(home);
                }
                
            }
		}
        if (asinstalldir!=null) verifyAsinstalldir(asinstalldir);
		return asinstalldir;
	}


    /**
     * verify if asinsatlldir attribute is valid.
     * asinstalldir must be a valid directory and must contain the config directory.
     *
     * @return true if asinstalldir is valid
     * @throws ClassNotFoundException if asinstalldir is an invalid directory
     */
    private boolean verifyAsinstalldir(File home) throws ClassNotFoundException{
        if (home!= null && home.isDirectory()) {
            if ( new File(home, "config").isDirectory() ) {
                return true;
            } 
        }
        throw new ClassNotFoundException("ClassCouldNotBeFound");
    }
    

    /**
     * returns the appserver server installation directory
     * first it will get asinstalldir from attribute of ant task.
     * if user did not specify asinstalldir, then the retrieve the install directory
     * from java system property using installrootConstant.
     * if all else fails, an ClassNotFoundException is thrown and the appropraite
     * msg will be displayed to bhe user.
     * @param installRootconstant - the install root constant to retrieve from
     * java system property.
     * @return install directory
     * @throws ClassNotFoundException if install directory can not be determined.
     */
    private String getInstallRoot(final String installRootConstant) throws ClassNotFoundException{
        final File installDir = getAsinstalldir();
        String installRoot;
        if (installDir == null) {
            installRoot = System.getProperty(installRootConstant);
            if (installRoot == null)
                throw new ClassNotFoundException("ClassCouldNotBeFound");
        } else {
            installRoot = installDir.getPath();
            System.setProperty(installRootConstant, installRoot);
        }
        return installRoot;
    }
    

    /**
     * Creates a nested <code>server</code> element.
	 *
	 * @return Server which has been added
     */
	public Server createServer() {
		log("createServer", Project.MSG_DEBUG);
		Server aNestedServer = getNewServer();
		servers.add(aNestedServer);
		return aNestedServer;
	}

	/**
	 * Builds a new server instance.  This method is intended to be overridden
	 * by subclasses which implement their own subclass of Server.
	 *
	 * @return new Server instance.
	 */
	protected Server getNewServer() {
		return new Server(server);
	}

    /**
     * Does the work.
	 *
	 * @throws BuildException If the user selections are invalid or an exception
	 *                        occurs while executing the app server admin 
	 *                        commands.
     */
	public void execute() throws BuildException {
		prepareToExecute();
		checkConfiguration();

		Iterator it = servers.iterator();
		while (it.hasNext()) {
			Server aServer = (Server)it.next();
			execute(aServer);
		}
	}

    /**
     * Does any clean-up work required before the user configuration is checked
	 * and the command(s) executed.
     */
	protected void prepareToExecute() {
		if (servers.size() == 0) {
			servers.add(server);
		}
	}

    /**
     * Verifies that the options and parameters selected by the user are valid
	 * and consistent.
	 *
	 * @throws BuildException If the user selections are invalid.
     */
	protected void checkConfiguration() throws BuildException {

		log(servers.size() + " servers were found.", Project.MSG_DEBUG);

		// At least one target server must be specified
		if (servers.size() == 0) {
            final String msg = lsm.getString("SpecifyOneServer");
			throw new BuildException(msg, getLocation());
		}

		// Check the configuration of each target server specified
		Iterator it = servers.iterator();
		while (it.hasNext()) {
			Server aServer = (Server)it.next();
			checkConfiguration(aServer);
		}
	}

    /**
     * Verifies that the options and parameters selected by the user are valid
	 * and consistent for a given server.
	 *
	 * @param aServer The server whose configuration is to be validated.
	 * @throws BuildException If the user selections are invalid.
     */
	protected abstract void checkConfiguration(Server aServer) throws BuildException;

    /**
     * Builds and executes the admin command on the server specified.
	 *
	 * @param aServer The target server for the administrative command.
	 * @throws BuildException If any error occurs while executing the command.
     */
	protected abstract void execute(Server server) throws BuildException;

    /**
     * Executes a command-string using the Sun ONE Application Server
	 * administrative CLI infrastructure.
	 *
	 * @param command The administrative command String to be executed.
	 * @throws BuildException If any error occurs while executing the command.
     */
	protected void execAdminCommand(String command) throws BuildException {
		log("Executing: " + command, Project.MSG_INFO);

		try {
            
            if (invokeCLI == null) {
                java.lang.ClassLoader antClassLoader = new AntClassLoader(
                    AppservClassLoader.getClassLoader(), getProject(), null, false);
                log("class = " + ((AntClassLoader)antClassLoader).getClasspath(), Project.MSG_DEBUG);
                inputsAndOutputs = Class.forName(CLASS_INPUTS_AND_OUTPUTS, true, antClassLoader);
                adminMain = Class.forName(CLASS_ADMIN_MAIN, true, antClassLoader);
                systemPropertyConstants = Class.forName(CLASS_SYSTEM_PROPERTY_CONSTANTS, true, antClassLoader);
            }

            
            log("***** INSTALL_ROOT_PROPERTY = " + (String)systemPropertyConstants.getField("INSTALL_ROOT_PROPERTY").get(null), Project.MSG_DEBUG);

            final String installRootConstant = (String)systemPropertyConstants.getField("INSTALL_ROOT_PROPERTY").get(null);
            final String configRootConstant = (String)systemPropertyConstants.getField("CONFIG_ROOT_PROPERTY").get(null);

            final String installRoot =  getInstallRoot(installRootConstant);
            
            log("installRoot: " + installRoot, Project.MSG_DEBUG);
            final String libraryPath = (String) System.getProperty("java.library.path");

            System.setProperty("java.library.path", installRoot+"/lib"+File.pathSeparator+libraryPath);
            System.setProperty(installRootConstant, installRoot);
            System.setProperty(configRootConstant, installRoot+"/config");
            System.setProperty("java.endorsed.dirs", installRoot+"/lib/endorsed");

            //debug display all system properties
            /*
            for (java.util.Enumeration en = System.getProperties().propertyNames(); en.hasMoreElements() ;) 
            {
                String name = (String) en.nextElement();
                String value = (String) System.getProperties().getProperty(name);
                log("System.property = " + name + " " + value, Project.MSG_DEBUG);
            }
            */
            
			Class[] parameterClasses = {String.class, inputsAndOutputs};
			invokeCLI = adminMain.getDeclaredMethod(METHOD_INVOKE_CLI, parameterClasses);

			Object[] parameters = {command, null};

			if (executeCommand) {
				invokeCLI.invoke(adminMain, parameters);
			}
		} catch (ClassNotFoundException e) {
            final String msg = lsm.getString("ClassCouldNotBeFound", new Object[] {e.getMessage()});
			throw new BuildException(msg, getLocation());
        } catch (NoSuchMethodException e) {
            final String msg = lsm.getString("CouldNotFindInvokeCLI", new Object[] {e.getMessage()});
			throw new BuildException(msg, getLocation());
		} catch (InvocationTargetException e) {
            final String msg = lsm.getString("ExceptionOccuredRunningTheCommand", new Object[] {e.getTargetException().getMessage()});
			throw new BuildException(msg, getLocation());
		} catch (IllegalAccessException e) {
            final String msg = lsm.getString("ExceptionOccuredInvokeingCLI", new Object[] {e.getMessage()});
			throw new BuildException(msg, getLocation());
		} catch (Exception e) {
            throw new BuildException(e.getMessage(), getLocation());
        }
        
        
	}

	/**
	 * This inner class is used to represent administration instances of the
	 * Sun ONE Application Server.  The administration instance may be sent
	 * commands which enable users to configure their application servers.
	 *
     * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
	 */
	public class Server {
		private Server parent;        // Attr values may be inherited from parent
		private String user;          // Username used to logon to admin instance
		private String password;      // Password used to logon to admin instance
		private String passwordfile;      // Passwordfile used to logon to admin instance
		private String host;          // Hostname where admin instance is running
		private int    port;          // Port number of admin instance
        private String secure;       // Secure option indiccating communicating with admin server in secured mode.
		private String instance;      // Name of the "target" instance for command

		protected static final String DEFAULT_USER = "admin";
		protected static final String DEFAULT_HOST = "localhost";
		protected static final String DEFAULT_PORT = "4848";

		/**
		 * Constructs a new Server object without specifying a parent server
		 * from which values are inherited.
		 */
		public Server() {
			this(null);
		}

		/**
		 * Constructs a new Server object and specifies the parent server from 
		 * which attribute values are inherited
		 * 
		 * @param server The parent server for this object.
		 */
		public Server(Server parent) {
			this.parent = parent;
		}

		/**
		 * Sets the parent server for this server.  If attribute values are not
		 * explicitly set for this object, they may be inherited from the parent
		 * object.
		 * 
		 * @param parent The parent server for this object.
		 */
		public void setParent(Server parent) {
			this.parent = parent;
		}

		/**
		 * Gets the parent server for this server.  If attribute values are not
		 * explicitly set for this object, they may be inherited from the parent
		 * object.
		 * 
		 * @return The parent server for this object.
		 */
		public Server getParent() {
			return parent;
		}

		/**
		 * Sets the username used when logging into the application server 
		 * administration instance.
		 *
		 * @param user The username used to logon
		 */
		public void setUser(String user) {
			this.user = user;
		}

		/**
		 * Returns the username used when logging into the application server 
		 * administration instance.
		 *
		 * @return The username used to logon
		 */
		protected String getUser() {
			if (user == null) {
				return (parent == null) ? DEFAULT_USER : parent.getUser();
			}
			return user;
		}

		/**
		 * Sets the password used when logging into the application server 
		 * administration instance.
		 *
		 * @param password The password used to logon
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * Returns the password or password command
		 * e.g.
		 * " --password admin123 "
		 * " --passwordfile /somefile "
		 * @return The password command.  If no password was specified, return null.
		 */
		protected String getPasswordCommand() {

			// note that hasPassword() returns false when there is no password anywhere
			// up the family tree...
			
			if(!hasPassword())
				return null;
			
			// PRECEDENCE CHART:
			// 1. passwordfile in this instance
			// 2. password in this instance
			// 3. whatever is eventually returned by parent instances -- which all use the 2 precedence rules above...
			
			// 1.
			if(passwordfile != null)
				return " --passwordfile " + passwordfile + " ";
			
			// 2.
			if (password != null) 
				return " --password " + password + " ";
			
			// 3.
			if( parent != null)	
				return parent.getPasswordCommand();
			
			// nobody has a password!!
			return null;
		}
		/**
		 * Returns the password used when logging into the application server 
		 * administration instance.
		 *
		 * Only this and the enclosing class have any business getting the password.
		 * Use hasPassword() to see if a password was passed in either directly or in a file
		 * @return The password used to logon
		 */
		private String getPassword() {
			if (password == null) {
				return (parent == null) ? null : parent.getPassword();
			}
			return password;
		}
		/**
		 * Returns whether or not a password or passwordfile is available.
		 */
		protected boolean hasPassword() {
			
			if(passwordfile != null || password != null)
				return true;
			
			if(parent != null)
				return parent.hasPassword();

			// no password, no passwordfile, no parent...
			return false;
		}
		
		/**
		 * Sets the password used when logging into the application server 
		 * administration instance.
		 *
		 * @param password The password used to logon
		 */
		public void setPasswordfile(String passwordfile) {
                // need quotes around the passwordfile value since in Windows
                // the escape character (\) is used as file separator therefore
                // quotes is used to disambiguate the escape character.
			this.passwordfile = "\"" + new File(passwordfile).getPath() + "\"";
		}

		/**
		 * Returns the passwordfile used when logging into the application server 
		 * administration instance.
		 *
		 * Outsiders need to call getPasswordCommand to make it transparent how
		 * the password is arriving.
		 * @return The passwordfile used to logon
		 */
		private String getPasswordfile() {
			if (passwordfile == null) {
				return (parent == null) ? null : parent.getPasswordfile();
			}
			return passwordfile;
		}

		/**
		 * Sets the hostname where the application server administration 
		 * instance is running.
		 *
		 * @param host The host name where the administration instance is running
		 */
		public void setHost(String host) {
			this.host = host;
		}

		/**
		 * Returns the hostname where the application server administration 
		 * instance is running.
		 *
		 * @return The host name where the administration instance is running
		 */
		protected String getHost() {
			if (host == null) {
				return (parent == null) ? null : parent.getHost();
			}
			return host;
		}

		/**
		 * Sets the port where the application server administration instance is
		 * running.
		 *
		 * @param port The port number of the administration instance
		 */
		public void setPort(int port) {
			this.port = port;
		}

		/**
		 * Returns the port where the application server administration instance
		 * is running.
		 *
		 * @return The port number of the administration instance
		 */
		protected int getPort() {
			if (port == 0) {
				return (parent == null) ? 0 : parent.getPort();
			}
			return port;
		}

        /**
		 * Sets secure option indication if communicating th appserver in
         * secured mode.
		 *
		 * @param secure - true if communicating with appserver in secured mode
		 */
		public void setSecure(String secure) {
			this.secure = secure;
		}

		/**
		 * Returns the secure option. 
		 *
		 * @return secure
		 */
		protected String getSecure() {
            if (secure == null) {
				return (parent == null) ? null : parent.getSecure();
			}
			return secure;
		}

		/**
		 * Sets the application server instance name.  Typically, this is the
		 * "target" for administrative commands.
		 *
		 * @param instance The name of the application server instance
		 */
		public void setInstance(String instance) {
			this.instance = instance;
		}

		/**
		 * Returns the application server instance name.  Typically, this is the
		 * "target" for administrative commands.
		 *
		 * @return The name of the application server instance
		 */
		protected String getInstance() {
			if (instance == null) {
				return (parent == null) ? null : parent.getInstance();
			}
			return instance;
		}

		/**
		 * Builds the server-related command-line parameters.  This includes the
		 * --user, --password, --host, --port, and (optionally) --instance
		 * parameters.
		 *
		 * @param includeInstance Indicates if the --instance parameter should
		 *                        be include in the command string.  Some admin
		 *                        commands do not allow the --instance parameter.
		 * @return String representation of the server-related command-line
		 *         parameters.
		 */
		protected String getCommandParameters(boolean includeInstance) {
			StringBuffer cmdString = new StringBuffer();
			cmdString.append(" --user ").append(getUser());
			
			// note: you definitely don't want the overhead of calling getPasswordCommand()
			// twice like the other lines below.  
			// So we call it once and save it in a String...
			String pwc = getPasswordCommand();
			
			if(pwc != null){
				cmdString.append(pwc);
			}
			
			if (getHost() != null) {
				cmdString.append(" --host ").append(getHost());
			}
			if (getPort() != 0) {
				cmdString.append(" --port ").append(getPort());
			}
			if (includeInstance && (getInstance() != null)) {
				cmdString.append(" --instance ").append(getInstance());
			}
            if (getSecure() != null) {
				cmdString.append(" --secure=").append(getSecure());                    
            }
            

			return cmdString.toString();
		}

		/** 
		 * Returns a string representation of the object.
		 *
		 * @return a string representation of the object.
		 */
		public String toString() {
			StringBuffer sb;
			sb = new StringBuffer((getHost() == null) ? DEFAULT_HOST : getHost());
			sb.append(':');
			sb.append((getPort() == 0) ? DEFAULT_PORT : String.valueOf(getPort()));
			return sb.toString();
		}
	}; // End of Server inner class
}
