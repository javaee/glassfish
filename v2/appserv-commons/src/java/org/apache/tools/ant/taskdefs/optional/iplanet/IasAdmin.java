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

package org.apache.tools.ant.taskdefs.optional.iplanet;

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

public abstract class IasAdmin extends Task {
	private Server server;
	private List   servers = new ArrayList();
	private File   ias7home;

	private static final boolean ACTUALLY_EXEC_COMMAND = true;

	private static final String[] CLASSPATH_ELEMENTS = { 
									"ias7se/iWS/bin/https/jar/iascli.jar",
									"ias7se/iWS/bin/https/jar/adminshared.jar",
									"ias7se/iWS/bin/https/jar/jmxri.jar"};
	private static final String CLASS_INPUTS_AND_OUTPUTS = 
							"com.sun.enterprise.tools.cli.framework.InputsAndOutputs";
	private static final String CLASS_IAS_ADMIN_MAIN = 
							"com.sun.enterprise.tools.cli.IasAdminMain";
	private static final String METHOD_INVOKE_CLI = "invokeCLI";

	public void setUser(String user) {
		getServer().setUser(user);
	}

	public void setPassword(String password) {
		log("setPassword:  " + password, Project.MSG_DEBUG);
		getServer().setPassword(password);
	}

	public void setHost(String host) {
		getServer().setHost(host);
	}

	public void setPort(int port) {
		getServer().setPort(port);
	}

	public void setInstanceport(int instanceport) {
		getServer().setInstanceport(instanceport);
	}

	public void setInstance(String instance) {
		getServer().setInstance(instance);
	}

	public void setIas7home(File iashome) {
		this.ias7home = ias7home;
	}

	/**
	 * Returns null if iashome hasn't been explicitly set and the property
	 * ias7.home isn't set.
	 */
	protected File getIas7home() {
		if (ias7home == null) {
			String home = getProject().getProperty("ias7.home");
			if (home != null) {
				ias7home = new File(home);
			}
		}
		
		return ias7home;
	}

	public Server createServer() {
		log("createServer", Project.MSG_DEBUG);
		Server aServer = new Server(server);
		servers.add(aServer);
		return aServer;
	}

	private Server getServer() {
		if (server == null) {
			server = new Server();
			Iterator it = servers.iterator();
			while (it.hasNext()) {
				Server aServer = (Server)it.next();
				aServer.setParent(server);
			}
		}
		return server;
	}

	public void execute() throws BuildException {
		prepareToExecute();
		checkConfiguration();

		Iterator it = servers.iterator();
		while (it.hasNext()) {
			Server aServer = (Server)it.next();
			execute(aServer);
		}
	}

	protected void prepareToExecute() throws BuildException {
		if ((servers.size() == 0) && (server != null)) {
			servers.add(server);
		}
	}

	protected void checkConfiguration() throws BuildException {

		if (servers.size() == 0) {
			String msg = "At least one server must be specified.";
			throw new BuildException(msg, getLocation());
		}

		Iterator it = servers.iterator();
		while (it.hasNext()) {
			Server aServer = (Server)it.next();
			checkConfiguration(server);
		}
	}

	protected void checkConfiguration(Server aServer) throws BuildException {
		if (aServer.getPassword() == null) {
			String msg = "A password must be specified for each server.  A "
							+ "password for " + aServer.getHost() + " was "
							+ "not specified.";
			throw new BuildException(msg, getLocation());
		}
	}

	protected abstract void execute(Server server) throws BuildException;

	protected void execIasCommand(String command) {
		log("Executing: " + command, Project.MSG_INFO);

		try {
			Path iasClasspath = new Path(getProject(), getIasClasspath());
			iasClasspath.append(Path.systemClasspath);
			log("Using classpath: " + iasClasspath.toString(), Project.MSG_DEBUG);

			ClassLoader antClassLoader = new AntClassLoader(getProject(), iasClasspath);

			Class inputsAndOutputs = 
						Class.forName(CLASS_INPUTS_AND_OUTPUTS, true, antClassLoader);
			Class iasAdminMain = 
						Class.forName(CLASS_IAS_ADMIN_MAIN, true, antClassLoader);

			Class[] parameterClasses = {String.class, inputsAndOutputs};
			Method invokeCLI =  
					iasAdminMain.getDeclaredMethod(METHOD_INVOKE_CLI, parameterClasses);

			Object[] parameters = {command, null};

			if (ACTUALLY_EXEC_COMMAND) {
				invokeCLI.invoke(iasAdminMain, parameters);
			}
		} catch (ClassNotFoundException e) {
			String msg = "An iPlanet Application Server 7.0 admin CLI "
							+ "class could not be found (" + e.getMessage()
							+ ").  Use the ias7home attribute, set the "
							+ "ias7.home property, or add the appropriate "
							+ "JARs to the classpath.";
			throw new BuildException(msg, getLocation());
		} catch (NoSuchMethodException e) {
			String msg = "The \"invokeCLI\" method couldn't be found on the "
							+ "IasAdminMain class.  The exception message "
							+ "is:  " + e.getMessage();
			throw new BuildException(msg, getLocation());
		} catch (InvocationTargetException e) {
			String msg = "An exception occurred while running the command.  The "
							+ "exception message is: "
							+ e.getTargetException().getMessage();
			throw new BuildException(msg, getLocation());
		} catch (IllegalAccessException e) {
			String msg = "An exception occurred while trying to invoke the "
							+ "\"invokeCLI\" method.  The exception message "
							+ "is: " + e.getMessage();
			throw new BuildException(msg, getLocation());
		}
	}

	private String getIasClasspath() {
		StringBuffer classpath = new StringBuffer();
		String[] elements = getClasspathElements();
		for (int i = 0; i < elements.length; i++ ) {
			classpath.append(new File(getIas7home(), elements[i]).getPath());
			classpath.append(':');
		}

		return classpath.toString();
	}


	/**
	 * Returns the JARs and directories that should be added to the classpath
	 * when calling the IasAdminMain class.  All elements are relative to the
	 * iPlanet Application Server 7.0 installation directory.
	 */
	protected String[] getClasspathElements() {
		return CLASSPATH_ELEMENTS;
	}

	public class Server {
		private Server parent; 
		private String user;
		private String password;
		private String host;
		private int    port;
		private int    instanceport;
		private String instance;

		private static final String DEFAULT_USER = "admin";
		private static final String DEFAULT_HOST = "localhost";
		private static final int    DEFAULT_PORT = 8000;

		public Server() {
			this(null);
		}

		public Server(Server parent) {
			this.parent = parent;
		}

		public void setParent(Server parent) {
			this.parent = parent;
		}

		public void setUser(String user) {
			this.user = user;
		}

		protected String getUser() {
			if (user == null) {
				return (parent == null) ? DEFAULT_USER : parent.getUser();
			}
			return user;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		protected String getPassword() {
			if (password == null) {
				return (parent == null) ? null : parent.getPassword();
			}
			return password;
		}

		public void setHost(String host) {
			this.host = host;
		}

		protected String getHost() {
			if (host == null) {
				return (parent == null) ? DEFAULT_HOST : parent.getHost();
			}
			return host;
		}

		public void setPort(int port) {
			this.port = port;
		}

		protected int getPort() {
			if (port == 0) {
				return (parent == null) ? DEFAULT_PORT : parent.getPort();
			}
			return port;
		}

		public void setInstanceport(int instanceport) {
			this.instanceport = instanceport;
		}

		protected int getInstanceport() {
			if ((instanceport == 0) && (parent != null)) {
				return parent.getInstanceport();
			}
			return instanceport;
		}

		public void setInstance(String instance) {
			this.instance = instance;
		}

		protected String getInstance() {
			if (instance == null) {
				return (parent == null) ? null : parent.getInstance();
			}
			return instance;
		}

		protected String getCommandParameters(boolean includeInstance) {
			StringBuffer cmdString = new StringBuffer();
			cmdString.append(" --user ").append(getUser());
			cmdString.append(" --password ").append(getPassword());
			cmdString.append(" --host ").append(getHost());
			cmdString.append(" --port ").append(getPort());
			if (includeInstance && (getInstance() != null)) {
				cmdString.append(" --instance ").append(getInstance());
			}

			return cmdString.toString();
		}
	};
}
