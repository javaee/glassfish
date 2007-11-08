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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import java.util.Map;
import java.util.HashMap;

/**
 * This task controls Sun ONE Application Server 7 instances.  It supports 
 * starting, stopping, restarting, creating, and removing application server 
 * instances.
 *
 * In addition to the server-based attributes, this task introduces one 
 * attribute: 
 *   <ul>
 *     <li><i>action</i> -- The control command for the application server.  
 *                          Valid values are "start", "stop", "restart", 
 *                          "create" and "delete".  A restart will first send 
 *                          the stop command and subsequently send the start 
 *                          command
 *   </ul>
 * <p>
 *
 * @see    AppServerAdmin
 * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
 */
public class InstanceTask extends AppServerAdmin {
	private String action = null;

	/*
	 * Constants for the valid actions.  In addition, the action strings are
	 * mapped to their appropriate commands in the Sun ONE Application Server CLI
	 */
	private static final String ACTION_START   = "start";
	private static final String ACTION_STOP    = "stop";
	private static final String ACTION_RESTART = "restart";
	private static final String ACTION_CREATE  = "create";
	private static final String ACTION_DELETE = "delete";

	private static final Map ACTION_MAP = new HashMap(5);
	static {
		ACTION_MAP.put(ACTION_START, "start-instance");
		ACTION_MAP.put(ACTION_STOP, "stop-instance");
		ACTION_MAP.put(ACTION_RESTART, "restart-instance");
		ACTION_MAP.put(ACTION_CREATE, "create-instance");
		ACTION_MAP.put(ACTION_DELETE, "delete-instance");
	};

    LocalStringsManager lsm = new LocalStringsManager();


	/**
	 * Sets the action for the instance command.
	 *
	 * @param action The action for the instance command.
	 */
	public void setAction(String action) {
		this.action = action;
	}

	protected Server getNewServer() {
		return new InstanceServer(server);
	}

        /**
         * Sets the nodeagent to be used by a new application server instance.
         *
         * @param nodeagent The nodeagent of the new application server 
         *                     instance
         */
        public void setNodeagent(String nodeagent) {
            ((InstanceServer)server).setNodeagent(nodeagent);  // Delegates to server object
        }

        /**
         * Sets the property to be used by a new application server instance.
         *
         * @param property The nodeagent of the new application server 
         *                     instance
         */
        public void setProperty(String property) {
            ((InstanceServer)server).setProperty(property);  // Delegates to server object
        }

        /**
         * Sets the config to be used by a new application server instance.
         *
         * @param config The config of the new application server 
         *                     instance
         */
        public void setConfig(String config) {
            ((InstanceServer)server).setConfig(config);  // Delegates to server object
        }

        /**
         * Sets the cluster to be used by a new application server instance.
         *
         * @param cluster The cluster of the new application server 
         *                     instance
         */
        public void setCluster(String cluster) {
            ((InstanceServer)server).setCluster(cluster);  // Delegates to server object
        }

	/**
	 * Set if the instance should be started in debug mode.  Defaults to 
	 * <code>false</code>.
	 *
	 * @param debug If set to <code>true</code>, the instance will be 
	 * in debug mode.
	 */
	public void setDebug(boolean debug) {
            final String msg = lsm.getString("AttributeNotSupported", 
                                                new Object[] {"debug"});
            log(msg, Project.MSG_WARN);
            //((InstanceServer)server).setDebug(debug);  // Delegates to server object
	}

	/**
	 * Set if the instance should be started without an admin server
	 * running.
	 *
	 * @param local If set to <code>true</code>, the instance will be 
	 *              started without the need for an admin server.
	 */
	public void setLocal(boolean local) {
            final String msg = lsm.getString("AttributeNotSupported", 
                                                new Object[] {"local"});
            log(msg, Project.MSG_WARN);
            //((InstanceServer)server).setLocal(local);  // Delegates to server object
	}

	/**
	 * Sets the domain for the administrative command -- some commands allow the
	 * domain to be set for "local" commands instead of username, password, host
	 * and password.
	 *
	 * @param domain The domain name for the administrative command.
	 */
	public void setDomain(String domain) {
            final String msg = lsm.getString("AttributeNotSupported", 
                                                new Object[] {"domain"});
            log(msg, Project.MSG_WARN);
            //((InstanceServer)server).setDomain(domain);  // Delegates to server object
	}

	/**
	 * Sets the port number to be used by a new application server instance.
	 *
	 * @param instanceport The port number of the new application server instance
	 */
	public void setInstanceport(int instanceport) {
            final String msg = lsm.getString("AttributeNotSupported", 
                                                new Object[] {"instanceport"});
            log(msg, Project.MSG_WARN);
            //((InstanceServer)server).setInstanceport(instanceport);
	}

	protected void checkConfiguration() throws BuildException {
            if (action == null) {
                final String msg = lsm.getString("ActionCommandMustBeSpecified");
                throw new BuildException(msg, getLocation());
            }

            if (!ACTION_MAP.containsKey(action)) {
                final String msg = lsm.getString("InvalidActionCommand", new Object[] {action});
                throw new BuildException(msg, getLocation());
            }
            super.checkConfiguration();
        }

	protected void checkConfiguration(Server aServer) throws BuildException {
            if (aServer.getInstance() == null) {
                final String msg = lsm.getString("InstanceAttributeRequired");
                throw new BuildException(msg, getLocation());
            }
            InstanceServer instanceSvr = (InstanceServer) aServer;
/*
		if (instanceSvr.isLocal() || (instanceSvr.getDomain() != null)) {
			if (instanceSvr.getHost() != null) {
                final String msg = lsm.getString("HostAttributeIgnored");
				log(msg, Project.MSG_WARN);
			}
			if (instanceSvr.getPort() != 0) {
                final String msg = lsm.getString("PortAttributeIgnored");
				log(msg, Project.MSG_WARN);
			}
			if ((instanceSvr.getUser() != null) && (!instanceSvr.getUser().equals("admin"))) {
                final String msg = lsm.getString("UserAttributeIgnored");
				log(msg, Project.MSG_WARN);
			}
			if (instanceSvr.hasPassword()) {
                final String msg = lsm.getString("PasswordAttributeIgnored");
				log(msg, Project.MSG_WARN);
			}
		} else {
			if (!instanceSvr.hasPassword()) {
                final String msg = lsm.getString("PasswordAttributeNotSpecified");
				throw new BuildException(msg, getLocation());
			}
		}

		if ((instanceSvr.getDomain() != null) && !instanceSvr.isLocal()) {
            final String msg = lsm.getString("DomainAttributeIgnored");
			log(msg, Project.MSG_WARN);
		}
*/
            if (!instanceSvr.hasPassword()) {
                final String msg = lsm.getString("PasswordAttributeNotSpecified");
                throw new BuildException(msg, getLocation());
            }
            if (action.equals(ACTION_CREATE)) {
                if (instanceSvr.getNodeagent() == null) {
                    final String msg = lsm.getString("AttributeMustBeSpecified", new Object[] {"nodeagent"});
                    throw new BuildException(msg, getLocation());
                }
            }
	}

	protected void execute(Server aServer) throws BuildException {
		InstanceServer instanceSvr = (InstanceServer) aServer;
        
        if ( instanceSvr.getConfig() != null &&
             instanceSvr.getCluster() != null ) {
            final String msg = lsm.getString("MutuallyExclusivelyAttribute",
                                             new Object[] {"config", "cluster"});
            throw new BuildException(msg, getLocation());
        }

		StringBuffer cmd = new StringBuffer((String) ACTION_MAP.get(action));
                cmd.append(instanceSvr.getCommandParameters(false));
		if (action.equals(ACTION_CREATE)) {
			//cmd.append(" --instanceport " + instanceSvr.getInstanceport());
                    cmd.append(" --nodeagent " + instanceSvr.getNodeagent());
                    if (instanceSvr.getConfig() != null)
                        cmd.append(" --config " + instanceSvr.getConfig());
                    if (instanceSvr.getCluster() != null)
                        cmd.append(" --cluster " + instanceSvr.getCluster());
                    if (instanceSvr.getProperty() != null)
                        cmd.append(" --systemproperties " + instanceSvr.getProperty());
		} else if (action.equals(ACTION_START)) {
			//cmd.append(" --debug=" + instanceSvr.isDebug());
		}
/*
		if (instanceSvr.isLocal()) {
			if (instanceSvr.getDomain() != null) {
				cmd.append(" --domain ").append(instanceSvr.getDomain());
			}
			cmd.append(" --local=true");			
		} else {
			cmd.append(instanceSvr.getCommandParameters(false));
		}
*/
		cmd.append(' ').append(instanceSvr.getInstance());

		execAdminCommand(cmd.toString());

/*		String cmdString = (String) ACTION_MAP.get(action);
		cmdString += aServer.getCommandParameters(false);
		if (action.equals(ACTION_CREATE)) {
			cmdString += " --instanceport " + aServer.getInstanceport();
		}
		if (aServer.getInstance() != null) {
			cmdString += " " + aServer.getInstance();
		}
		execAdminCommand(cmdString);
 */
	}

	/**
	 * This inner class is used to represent administration instances of the
	 * Sun ONE Application Server for instance-based command.  The admin
	 * instance may be sent commands which enable users to configure their 
	 * application servers.
	 *
     * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
	 */
	public class InstanceServer extends Server {
		private boolean debug;  // Starts instance in debug mode if true
		private boolean local;  // Starts instance without admin instance if true
		private String  domain; // Domain where the admin command is executed
		private int     instanceport;  // Port new server instance should use
	    private String nodeagent; // nodeagent when create instance command is executed
	    private String config; // instance configuration when create instance command is executed
	    private String cluster; // cluster name when create instance command is executed
	    private String property; // property when create instance command is executed

		/*
		 * It's easy to determine if reference types have been explicitly set by
		 * the user, since they're null if unset.  These variables track whether
		 * or not the boolean attribute have been explictly set by the user or
		 * not.
		 */
		private boolean debugIsSet = false;
		private boolean localIsSet = false;

		private static final boolean DEFAULT_DEBUG = false;
		private static final boolean DEFAULT_LOCAL = false;

		/**
		 * Constructs a new InstanceServer object without specifying a parent
		 * server from which values are inherited.
		 */
		public InstanceServer() {
			this(null);
		}

		/**
		 * Constructs a new InstanceServer object and specifies the parent server
		 * from which attribute values are inherited
		 * 
		 * @param server The parent server for this object.
		 */
		public InstanceServer(Server theParent) {
			super(theParent);
		}

		/**
		 * Set if the instance should be started in debug mode.  Defaults to 
		 * <code>false</code>.
		 *
		 * @param debug If set to <code>true</code>, the instance will be 
		 * in debug mode.
		 */
		public void setDebug(boolean debug) {
		        this.debug = debug;
			debugIsSet = true; // Indicates that "debug" has been explicitly set
		}

		/**
		 * Indicates if the instance should be started in debug mode. 
		 *
		 * @return <code>true</code> if the instance should be started in debug
		 *         mode.
		 */
		protected boolean isDebug() {
			InstanceServer theParent = (InstanceServer) getParent();
			if (!debugIsSet) {
				return (theParent == null) ? DEFAULT_DEBUG : theParent.isDebug();
			}
			return debug;
		}

		/**
		 * Set if the instance should be started without an admin server
		 * running.
		 *
		 * @param local If set to <code>true</code>, the instance will be 
		 *              started without the need for an admin server.
		 */
		public void setLocal(boolean local) {
			this.local = local;
			localIsSet = true; // Indicates that "local" has been explicitly set
		}

		/**
		 * Indicates if the instance should be started without an admin server
		 * running.
		 *
		 * @return <code>true</code> if the instance should be started in 
		 *         without an admin server.
		 */
		protected boolean isLocal() {
			InstanceServer theParent = (InstanceServer) getParent();
			if (!localIsSet) {
				return (theParent == null) ? DEFAULT_LOCAL : theParent.isLocal();
			}
			return local;
		}

		/**
		 * Sets the domain for the administrative command -- some commands allow the
		 * domain to be set for "local" commands instead of username, password, host
		 * and password.
		 *
		 * @param domain The domain name for the administrative command.
		 */
		public void setDomain(String domain) {
			this.domain = domain;
		}

		/**
		 * Returns the domain for the administrative command.
		 *
		 * @return The domain name where the command is to be executed
		 */
		protected String getDomain() {
			InstanceServer theParent = (InstanceServer) getParent();
			if (domain == null) {
				return (theParent == null) ? null : theParent.getDomain();
			}
			return domain;
		}

		/**
		 * Sets the port number to be used by a new application server instance.
		 *
		 * @param instanceport The port number of the new application server 
		 *                     instance
		 */
		public void setInstanceport(int instanceport) {
			this.instanceport = instanceport;
		}

		/**
		 * Returns the port number to be used by a new application server 
		 * instance.
		 *
		 * @return The port number of the new application server instance
		 */
		protected int getInstanceport() {
			InstanceServer theParent = (InstanceServer) getParent();
			if ((instanceport == 0) && (theParent != null)) {
				return theParent.getInstanceport();
			}
			return instanceport;
		}

                /**
                 * Sets the nodeagent to be used by a new application server instance.
                 *
                 * @param nodeagent The nodeagent of the new application server 
                 *                     instance
                 */
                public void setNodeagent(String nodeagent) {
                    this.nodeagent = nodeagent;
                }

                /**
                 * Returns the nodeagent to be used by a new application server 
                 * instance.
                 *
                 * @return The nodeagent of the new application server instance
                 */
                protected String getNodeagent() {
                    InstanceServer theParent = (InstanceServer) getParent();
                    return nodeagent;
                }

                /**
                 * Sets the cluster to be used by a new application server instance.
                 *
                 * @param cluster The cluster of the new application server 
                 *                     instance
                 */
                public void setCluster(String cluster) {
                    this.cluster = cluster;
                }

                /**
                 * Returns the cluster to be used by a new application server 
                 * instance.
                 *
                 * @return The cluster of the new application server instance
                 */
                protected String getCluster() {
                    InstanceServer theParent = (InstanceServer) getParent();
                    return cluster;
                }
                /**
                 * Sets the config to be used by a new application server instance.
                 *
                 * @param config The config of the new application server 
                 *                     instance
                 */
                public void setConfig(String config) {
                    this.config = config;
                }

                /**
                 * Returns the config to be used by a new application server 
                 * instance.
                 *
                 * @return The config of the new application server instance
                 */
                protected String getConfig() {
                    return config;
                }
                /**
                 * Sets the property to be used by a new application server instance.
                 *
                 * @param property The nodeagent of the new application server 
                 *                     instance
                 */
                public void setProperty(String property) {
                    this.property = property;
                }

                /**
                 * Returns the property to be used by a new application server 
                 * instance.
                 *
                 * @return The property of the new application server instance
                 */
                protected String getProperty() {
                    return property;
                }
        }
}
