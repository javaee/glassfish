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

import java.io.File;

/**
 * This task deploys J2EE components to the Sun ONE Application Server 7.  The 
 * following components may be deployed:
 *   <ul>
 *     <li>Enterprise application (EAR file) 
 *     <li>Web application (WAR file) 
 *     <li>Enterprise Java Bean (EJB-JAR file) 
 *     <li>Enterprise connector (RAR file) 
 *     <li>Application client   
 *   </ul>
 *
 * @see    AppServerAdmin
 * @see    ComponentAdmin
 * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
 */
public class DeployTask extends ComponentAdmin {
	private static final String DEPLOY_COMMAND = "deploy";
	private static final String DEPLOY_DIR_COMMAND = "deploydir";
    LocalStringsManager lsm = new LocalStringsManager();

	protected Server getNewServer() {
		return new DeployServer(server);
	}

	protected Component getNewComponent() {
		return new DeployComponent(component);
	}

	/**
	 * Set if the component should be transferred to the (potentially) 
	 * remote server before executing the command.  Defaults to
	 * <code>true</code>.
	 *
	 * @param upload If set to <code>true</code>, components are transferred to  
	 * the server before the command is executed.
	 */
	public void setUpload(boolean upload) {
		((DeployServer)server).setUpload(upload);  // Delegates to server object
	}

	/**
	 * Specifies the virtual server (or servers) where the component will be
	 * deployed.  This applies to only EAR and WAR files.
	 *
	 * @param virtualServers The name of the virtual server or the names of
	 *                       multiple virtual servers separated by commas (',')
	 */
	public void setVirtualServers(String virtualServers) {
		((DeployServer)server).setVirtualServers(virtualServers);
	}
	
	/**
	 * Sets if the command should overwrite existing values or components.
	 *
	 * @param force If set to <code>true</code>, components are automatically 
	 * overwritten.  If set to <code>false</code>, administrative commands will 
	 * fail if the component already exists.
	 */
	public void setForce(boolean force) {
		((DeployComponent)component).setForce(force);  // Delegates to component object
	}

    /**
	 * Sets if the command is enabled or disabled.
	 *
	 * @param enabled If set to <code>true</code>, component is enabled.
	 * If set to <code>false</code>, component is disabled.
	 */
	public void setEnabled(boolean enabled) {
		((DeployComponent)component).setEnabled(enabled);  // Delegates to component object
	}

	/**
	 * Sets the directory where the JAR file with client stubs will be saved.
	 *
	 * @param dir The directory where client stubs will be saved.
	 */
	public void setRetrieveStubs(File stubsDir) {
		((DeployComponent)component).setRetrieveStubs(stubsDir);  // Delegates to component object
	}

	/**
	 * Sets the context root for a web module (WAR file). This attribute is only 
	 * used when deploying WAR files to the application server.
	 *
	 * @param contextroot The contextroot for the web application.
	 */
	public void setContextroot(String contextroot) {
		((DeployComponent)component).setContextroot(contextroot); // Delegates to component obj
	}

    /**
	 * Sets the deploymentplan, a jar containing the sun-specifi descriptors which should be
     * passed along when deploying a pure ear.
	 *
	 * @param deploymentplan
	 */
	public void setDeploymentplan(File deploymentplan) {
		((DeployComponent)component).setDeploymentplan(deploymentplan); // Delegates to component obj
	}
    
    /**
	 * Sets the dbvendorname - the name of database vendor being used.
	 *
	 * @param dbvendorname
	 */
	public void setDbvendorname(String dbvendorname) {
		((DeployComponent)component).setDbvendorname(dbvendorname); // Delegates to component obj
	}
    
	/**
	 * Sets if creates tables at deploy of an application with unmapped CMP beans.
	 *
	 * @param createtables
	 */
	public void setCreatetables(boolean createtables) {
		((DeployComponent)component).setCreatetables(createtables);  // Delegates to component object
	}

    /**
	 * Sets if drops tables at undeploy of an already deployed application with
     * unmapped CMP beans.
	 *
	 * @param dropandcreatetables
	 */
	public void setDropandcreatetables(boolean dropandcreatetables) {
		((DeployComponent)component).setDropandcreatetables(dropandcreatetables);  // Delegates to component object
	}

    /**
	 * Sets the unique table names for all the beans and results in a hascode added
     * to the table names.
	 *
	 * @param uniquetablenames
	 */
	public void setUniquetablenames(boolean uniquetablenames) {
		((DeployComponent)component).setUniquetablenames(uniquetablenames);  // Delegates to component object
	}

    /**
	 * Sets availabilityenabled option.  If <code>false</code> then all SFSB checkpointing is disabled for
     * either the given j2ee app or the given ejb module.  If it is <code>true</code> then the j2ee app
     * or stand-alone ejb modules may be ha enabled. 
	 *
	 * @param availabilityenabled
	 */
	public void setAvailabilityenabled(boolean availabilityenabled) {
		((DeployComponent)component).setAvailabilityenabled(availabilityenabled);  // Delegates to component object
	}

    /**
	 * Sets generatermistubs option.  If <code>true</code> then generate the static RMI-IIOP 
     * stubs and put it in the client.jar.
	 *
	 * @param generatermistubs
	 */
	public void setGeneratermistubs(boolean generatermistubs) {
		((DeployComponent)component).setGeneratermistubs(generatermistubs);  // Delegates to component object
	}


	/**
	 * Sets if the command should precompile JSPs when deploying them to the
	 * server.
	 *
	 * @param precompile If set to <code>true</code>, JSPs are automatically 
	 * compiled.
	 */
	public void setPrecompileJsp(boolean precompile) {
		((DeployComponent)component).setPrecompileJsp(precompile);  // Delegates to component object
	}

	/**
	 * Sets if the deployment descriptors should be verified as part of the
	 * deployment process.
	 *
	 * @param verify If set to <code>true</code>, the deployment descriptors
	 *               will be verified.
	 */
	public void setVerify(boolean verify) {
		((DeployComponent)component).setVerify(verify);
	}

	/**
     * Creates a nested <code>server</code> element.
	 *
	 * @return Server which has been added
     */
	public Server createServer() {
		log("createServer using DeployServer object", Project.MSG_DEBUG);
		Server aNestedServer = new DeployServer(server);
		servers.add(aNestedServer);
		return aNestedServer;
	}

	protected void checkComponentConfig(Server aServer, Component comp)
			throws BuildException {
		log("Checking component and server config in DeployTask", Project.MSG_DEBUG);

		DeployServer deployServer = (DeployServer) aServer;
		DeployComponent deployComp = (DeployComponent) comp;

		// file must be specified
		File theFile = comp.getFile();
		if (theFile == null) {
			throw new BuildException(lsm.getString("ComponentFileMustBeSpecified",
						 new Object[] {getTaskName()}), getLocation());
		}

		/*
		 * The file attribute is checked before the superclass implementation.
		 * If the superclass is called first and no file is specified, a 
		 * somewhat misleading error message is provided complaining the 
		 * component name couldn't be determined.
		 */
		super.checkComponentConfig(aServer, comp);

		// if an "exploded" archive is being deployed, --upload is not allowed
		if (theFile.isDirectory() && deployServer.getUpload()) {
			String hostname = aServer.getHost();
			if (hostname == null) {
				hostname = "localhost";
			}
			throw new BuildException(lsm.getString("UploadMayNotBeSetToTrue",
						 new Object[] {theFile.getAbsolutePath()}), 
						 getLocation());
		}

		// retrievestubs (if specified) must be a directory
		File theDir = deployComp.getRetrieveStubs();
		if (theDir != null) {
			if (!theDir.exists()) {
				throw new BuildException(lsm.getString(
							 "RetrieveStubsDirectoryDoesNotExist",
							 new Object[] {theDir}), getLocation());
			}
			if (!theDir.isDirectory()) {
			    throw new BuildException(lsm.getString(
                                                         "RetrievesStbusDoesNotReferToADirectory", 
							 new Object[] {theDir}), getLocation());
			}
		}
	}	

	protected String getCommandString(Server aServer, Component comp)
                                      throws BuildException {
		DeployServer deployServer = (DeployServer) aServer;
		DeployComponent deployComp = (DeployComponent) comp;

        CheckForMutuallyExclusiveAttribute(deployComp);

		StringBuffer cmdString;
		boolean isFile = comp.getFile().isFile();
		
		cmdString = new StringBuffer(isFile ? DEPLOY_COMMAND : DEPLOY_DIR_COMMAND);
		cmdString.append(aServer.getCommandParameters(true));
		if (comp.getType() != null) {
  		    log(lsm.getString("DeprecatedTypeAttribute"), Project.MSG_WARN);
		}
		cmdString.append(" --force=").append(deployComp.isForce());
        cmdString.append(" --enabled=").append(deployComp.isEnabled());
		cmdString.append(" --name ").append(comp.getName());
		cmdString.append(" --verify=").append(deployComp.isVerify());
        cmdString.append(" --precompilejsp=").append(deployComp.isPrecompileJsp());
		if (isFile) {
			cmdString.append(" --upload=").append(deployServer.getUpload());
		}
        if (deployServer.getVirtualServers() != null) {
			cmdString.append(" --virtualservers ");
            cmdString.append(deployServer.getVirtualServers());
		}

		if (deployComp.getRetrieveStubs() != null) {
			cmdString.append(" --retrieve ");
			cmdString.append("\""+deployComp.getRetrieveStubs()+"\"");
		}
        if (deployComp.getDeploymentplan() != null) {
			cmdString.append(" --deploymentplan ");
			cmdString.append("\""+deployComp.getDeploymentplan()+"\"");
		}

		if (deployComp.contextRootIsSet()) {
            cmdString.append(" --contextroot ").append(deployComp.getContextroot());
		}
        if (deployComp.getDbvendorname() != null) {
            cmdString.append(" --dbvendorname ").append(deployComp.getDbvendorname());
		}
        if (deployComp.createtablesIsSet) {
            cmdString.append(" --createtables=").append(deployComp.getCreatetables());
            deployComp.createtablesIsSet = false;  // set it back to false
		}
        if (deployComp.dropandcreatetablesIsSet) {
            cmdString.append(" --dropandcreatetables=").append(deployComp.getDropandcreatetables());
            deployComp.dropandcreatetablesIsSet = false; // set it back to false
		}
        if (deployComp.uniquetablenamesIsSet) {
            cmdString.append(" --uniquetablenames=").append(deployComp.getUniquetablenames());
            deployComp.uniquetablenamesIsSet = false;  // set it back to false
		}
        if (deployComp.availabilityenabledIsSet) {
            cmdString.append(" --availabilityenabled=").append(deployComp.getAvailabilityenabled());
            deployComp.availabilityenabledIsSet = false;   // set it back to false
		}
        if (deployComp.generatermistubsIsSet) {
            cmdString.append(" --generatermistubs=").append(deployComp.getGeneratermistubs());
            deployComp.generatermistubsIsSet = false;   // set it back to false
		}

		// check the value and append target
		String lTarget = deployComp.getTarget();
		if ((lTarget != null) && (lTarget.length() > 0)) {
	    		cmdString.append(" --target ").append(lTarget);
		}

		cmdString.append(" ").append("\""+comp.getFile().getPath()+"\"");

		return cmdString.toString();
	}


        /**
         * This private class checks for any mutually exclusive attributes.
         * If mutually exclusive attributes that are specified, then a
         * BuildException is thrown.
         */
    private void CheckForMutuallyExclusiveAttribute(DeployComponent deployComp)
        throws BuildException
    {
        if (deployComp.createtablesIsSet &&
            deployComp.dropandcreatetablesIsSet ) {
            final String msg = lsm.getString("MutuallyExclusivelyAttribute",
                                             new Object[] {"createtables",
                                                           "dropandcreatetables"});
            throw new BuildException(msg, getLocation());
        }
    }
    

	/**
	 * This inner class is used to represent administration instances of the
	 * Sun ONE Application Server for the deployment process.  The administration
	 * instance may be sent commands which enable users to configure their 
	 * application servers.
	 *
         * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
	 */
	public class DeployServer extends Server {
		private boolean upload;          // Uploads component to server if true
		private String  virtualServers = null;  // Comma-separated list of virtual svrs

		/*
		 * It's easy to determine if reference types have been explicitly set by
		 * the user, since they're null if unset.  These variables track whether
		 * or not the boolean attribute have been explictly set by the user or
		 * not.
		 */
		private boolean uploadIsSet = false;

		private static final boolean DEFAULT_UPLOAD = true;

		/**
		 * Constructs a new DeployServer object without specifying a parent
		 * server from which values are inherited.
		 */
		public DeployServer() {
			this(null);
		}

		/**
		 * Constructs a new DeployServer object and specifies the parent server
		 * from which attribute values are inherited
		 * 
		 * @param server The parent server for this object.
		 */
		public DeployServer(Server theParent) {
			super(theParent);
		}

		/**
		 * Set if the component should be transferred to the (potentially) 
		 * remote server before executing the command.  Defaults to 
		 * <code>true</code>.
		 *
		 * @param upload If set to <code>true</code>, components are transferred 
		 * to the server before the command is executed.
		 */
		public void setUpload(boolean upload) {
			this.upload = upload;
			uploadIsSet = true; // Indicates that "upload" has been explicitly set
		}

		/**
		 * Indicates if the component should be transferred to the (potentially) 
		 * remote server before executing the command.
		 *
		 * @return <code>true</code> if components are to be transferred to the
		 *         server before the command is executed.
		 */
		protected boolean getUpload() {
			DeployServer theParent = (DeployServer) getParent();
			if (!uploadIsSet) {
				return (theParent == null) ? DEFAULT_UPLOAD : theParent.getUpload();
			}
			return upload;
		}
		
		/**
		 * Specifies the virtual server (or servers) where the component will be
		 * deployed.  This applies to only EAR and WAR files.
		 *
		 * @param virtualServers The name of the virtual server or the names of
		 *                       multiple virtual servers separated by commas (',')
		 */
		public void setVirtualServers(String virtualServers) {
			this.virtualServers = virtualServers;
		}

		/**
		 * Returns the name of the virtual server (or servers) where the
		 * component will be deployed.  If multiple servers are specified, the
		 * names will be separated by commas (',')
		 * 
		 * @return the name of the virtual server (or servers)
		 */
		public String getVirtualServers() {
			return virtualServers;
		}
	}


	/**
	 * This inner class is used to represent J2EE components deployed to the
	 * Sun ONE Application Server.
	 *
     * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
	 */
	public class DeployComponent extends Component {
		private boolean   force;                // Overwrites existing component if true
		private boolean   precompile;	        // Precompiles JSPs on server if true
		private File      stubsDir;             // Directory where client stubs are saved
		private String    contextroot;          // Context root for web applications
		private boolean   verify;               // Verifies descriptors if true
        private boolean   enabled;              // Disables or enables the component.
                                                // Default is true.
        private File      deploymentplan;       // Deployment plan which is a jar containing
                                                // the sun-specific descriptors.
        private boolean   availabilityenabled;  // If "false" then all SFSB checkpointing is
                                                // disabled for either the given j2ee app or
                                                // the given ejb module.  If it is "true" then
                                                // the j2ee app or stand-alone jeb modules may
                                                // ha enabled.  Default value is "false".
        private boolean   generatermistubs;     // If "true" then generate the static RMI-IIOP
                                                // stubs and put it in the client.jar.
                                                // Default value is "false".
        private String    dbvendorname;         // Name of database vendor being used
        private boolean   createtables;         // Creates tables at deploy of an application
                                                // with unmaped CMP beans
        private boolean   dropandcreatetables;  // Drops tables at undeploy of an already
                                                // deployed application with unmapped CMP beans.
        private boolean   uniquetablenames;     // Guarantees unique table names for all the
                                                // beans and results in a hashcode added to
                                                // the tablenames.

		/*
		 * It's easy to determine if reference types have been explicitly set by
		 * the user, since they're null if unset.  These variables track whether
		 * or not the boolean attribute have been explictly set by the user or
		 * not.
		 */
		private boolean forceIsSet      = false;
        private boolean enabledIsSet    = false;
		private boolean precompileIsSet = false;
		private boolean verifyIsSet     = false;
        public  boolean createtablesIsSet = false;
        public  boolean dropandcreatetablesIsSet = false;
        public  boolean uniquetablenamesIsSet = false;
        public  boolean availabilityenabledIsSet = false;
        public  boolean generatermistubsIsSet = false;
        

		/*
		 * Default values for some attributes.
		 */
		private static final boolean DEFAULT_FORCE       = true;
		private static final boolean DEFAULT_PRECOMPILE  = false;
		private static final boolean DEFAULT_VERIFY      = false;
        private static final boolean DEFAULT_ENABLED     = true;

		/**
		 * Constructs a new DeployComponent object and specifies the parent
		 * component from which attribute values are inherited
		 * 
		 * @param theParent The parent component for this object.
		 */
		public DeployComponent(Component theParent) {
			super(theParent);
		}

		/**
		 * Sets if the command should overwrite existing values or components.
		 *
		 * @param force If set to <code>true</code>, components are 
		 * automatically overwritten.  If set to <code>false</code>, 
		 * administrative commands will fail if the component already exists.
		 */
		public void setForce(boolean force) {
			this.force = force;
			forceIsSet = true;  // Indicates that "force" has been explicitly set
		}

		/**
		 * Indicates if the command should overwrite existing values or 
		 * components.
		 *
		 * @return <code>true</code> if components are to be automatically 
		 *         overwritten.
		 */
		protected boolean isForce() {
			if (!forceIsSet) {
				return (parent == null) ? DEFAULT_FORCE : ((DeployComponent)parent).isForce();
			}
			return force;
		}
        
		/**
		 * Sets if the command should overwrite existing values or components.
		 *
		 * @param enabled If set to <code>true</code>, component is enabled
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			enabledIsSet = true;  // Indicates that "enabled" has been explicitly set
		}

		/**
		 * @return <code>true</code> if component is to be enabled.
		 */
		protected boolean isEnabled() {
			if (!enabledIsSet) {
				return (parent == null) ? DEFAULT_ENABLED : ((DeployComponent)parent).isEnabled();
			}
			return enabled;
		}

        /**
		 * Sets <code>false</code> then all SFSB checkingpointing is disabled for either
         * the given j2ee app or the given ejb module.  If it is <code>true</code> then
         * the j2ee app or stand-alone ejb modules may be ha enabled.
         * Defaul value is <code>false</code>.
		 *
		 * @param availabilityenabled
		 */
		public void setAvailabilityenabled(boolean availabilityenabled) {
			this.availabilityenabled = availabilityenabled;
			availabilityenabledIsSet = true;  // Indicates that "availabilityenabled" has been explicitly set
		}

		/**
		 * @return <code>true</code> then the j2ee app or stand-alone ejb modules may be
         * ha enabled.
         * <code>false</code> then all SFSB checkpointing is disabled for either the
         * given ejb module.
		 */
		protected boolean getAvailabilityenabled() {
			return availabilityenabled;
		}

        
        /**
		 * Sets <code>true</code> then generate the static RMI-IIOP stubs and put it
         * in the client.jar.
		 *
		 * @param generatermistubs
		 */
		public void setGeneratermistubs(boolean generatermistubs) {
			this.generatermistubs = generatermistubs;
			generatermistubsIsSet = true;  // Indicates that "availabilityenabled" has been explicitly set
		}

		/**
		 * @return <code>true</code> then generate the static RMI-IIOP stubs and put
         * it in the client.jar.
		 */
		protected boolean getGeneratermistubs() {
			return generatermistubs;
		}

		/**
		 * Sets the directory where the JAR file with client stubs will be saved.
		 *
		 * @param dir The directory where client stubs will be saved.
		 */
		public void setRetrieveStubs(File stubsDir) {
			this.stubsDir = stubsDir;
		}

		/**
		 * Returns the directory where the JAR file with client stubs will be
		 * saved.
		 *
		 * @return The directory where client stubs will be saved or <code>null
		 *         </code> if no directory has been specified.
		 */
		protected File getRetrieveStubs() {
			if (stubsDir == null) {
				return (parent == null) ? null : ((DeployComponent)parent).getRetrieveStubs();
			}
			return stubsDir;
		}

		/**
		 * Sets the context root for a web module (WAR file). This attribute is 
		 * only used when deploying WAR files to the application server.
		 *
		 * @param contextroot The contextroot for the web application.
		 */
		public void setContextroot(String contextroot) {
			this.contextroot = contextroot;
		}

		/**
		 * Returns the context root for a web module (WAR file).
		 *
		 * @return The contextroot for the web application.
		 */
		protected String getContextroot() {
			return (contextroot != null) ? contextroot : getName();
		}

		/**
		 * Indicates if the user has explicitly set the contextroot attribute.
		 *
		 * @return boolean indicating if the contextroot has been explicitly set.
		 */
		protected boolean contextRootIsSet() {
			return (contextroot != null);
		}


		/**
		 * Sets deploymentplan - a jar containing the sun-specific
         * descriptors which should be passed along when deploying
         * a pure ear.
		 *
		 * @param deploymentplan - name of the jar file.
		 */
		public void setDeploymentplan(File deploymentplan) {
			this.deploymentplan = deploymentplan;
		}

		/**
		 * Returns the deploymentplan, the name of the jar file
         * containing the sun-specific descriptors which should
         * be pssed along with deploying a pure ear.
		 *
		 * @return deploymentplan
		 */
		protected File getDeploymentplan() {
            if (deploymentplan == null) {
				return (parent == null) ? null : ((DeployComponent)parent).getDeploymentplan();
			}
			return deploymentplan;
		}

		/**
		 * Sets dbvendorname. dbvendorname is the name of database 
		 *
		 * @param dbvendorname is the database vendor name 
		 */
		public void setDbvendorname(String dbvendorname) {
			this.dbvendorname = dbvendorname;
		}

		/**
		 * Returns the dbvendorname for the database vendor being used
		 *
		 * @return the dbvendorname
		 */
		protected String getDbvendorname() {
			return (dbvendorname != null) ? dbvendorname : null;
		}

        /**
		 * Creates tables at deploy of an application with unmapped CMP beans.
		 *
		 * @param createtables
		 */
		public void setCreatetables(boolean createtables) {
			this.createtables = createtables;
			createtablesIsSet = true;  // Indicates that "createtables" has been explicitly set
		}

		/**
		 * Indicates if the createtables has been set.
		 *
		 * @return <code>true</code> if createtables has been set else return false.
		 */
		protected boolean getCreatetables() {
			return createtables;
		}

        /**
		 * Drop tables at undeploy of an already deployed application with unmapped
         * CMP beans.  If not specified, the tables will be dropped if the
         * drop-tables-at-undeploy entry in the cmp-resource element of sun-ejb-jar.xml
         * file is set to true.
		 *
		 * @param dropandcreatetables
		 */
		public void setDropandcreatetables(boolean dropandcreatetables) {
			this.dropandcreatetables = dropandcreatetables;
			dropandcreatetablesIsSet = true;  // Indicates that "dropandcreatetables"
                                              // has been explicitly set
		}

		/**
		 * @return the value of dropandcreatetables
		 */
		protected boolean getDropandcreatetables() {
			return dropandcreatetables;
		}
        
        /**
         * Guarantees unique table names for all the beans and results in a hashcode
         * added to the table names.
		 *
		 * @param uniquetablenames
		 */
		public void setUniquetablenames(boolean uniquetablenames) {
			this.uniquetablenames = uniquetablenames;
			uniquetablenamesIsSet = true;     // Indicates that "uniquetablenames"
                                              // has been explicitly set
		}

		/**
		 * @return the value of uniquetablenamse
		 */
		protected boolean getUniquetablenames() {
			return uniquetablenames;
		}

        

		/**
		 * Sets if JSPs should be compiled when deployed to the server.
		 *
		 * @param precompile If set to <code>true</code>, JSPs are automatically
		 * compiled when deployed to the server.
		 */
		public void setPrecompileJsp(boolean precompile) {
			this.precompile = precompile;
			precompileIsSet = true;  // Indicates attribute was explicitly set
		}

		/**
		 * Indicates if JSPs will automatically be compiled when deployed to the
		 * server.
		 *
		 * @return <code>true</code> if JSPs are to be automatically compiled.
		 */
		protected boolean isPrecompileJsp() {
			if (!precompileIsSet) {
				return (parent == null) ? DEFAULT_PRECOMPILE : ((DeployComponent)parent).isPrecompileJsp();
			}
			return precompile;
		}

		/**
		 * Sets if the deployment descriptors should be verified as part of the
		 * deployment process.
		 *
		 * @param verify If set to <code>true</code>, the deployment descriptors
		 *               will be verified.
		 */
		public void setVerify(boolean verify) {
			this.verify = verify;
			verifyIsSet = true;  // Indicates attribute was explicitly set
		}

		/**
		 * Indicates if deployment descriptors will automatically be verified
		 * as part of the deployment process.
		 *
		 * @return <code>true</code> if deployment descriptors will be verified
		 */
		protected boolean isVerify() {
			if (!verifyIsSet) {
				return (parent == null) ? DEFAULT_VERIFY : ((DeployComponent)parent).isVerify();
			}
			return verify;
		}
	}
}
