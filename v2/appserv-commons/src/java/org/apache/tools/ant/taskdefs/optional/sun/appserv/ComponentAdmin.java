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
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Abstract class which includes base functionality for Tasks which utilize J2EE
 * components.  This includes support for the following component-related
 * attributes:
 *
 *   <ul>
 *     <li><i>file</i> -- The filename of the component.  Although this is often
 *                        refers to a J2EE archive file, some commands may allow
 *                        this attribute to refer to a directory where a J2EE
 *                        archive has been exploded
 *     <li><i>name</i> -- The display-name for the J2EE component.  This is the
 *                        "friendly name" which will appear in the admin GUI and
 *                        administrative commands
 *     <li><i>type</i> -- The component type.  Valid types are "application", 
 *                        "ejb", "web", "connector" and "client" 
 *     <li><i>force</i> -- A boolean attribute which indicates if the command
 *                         should overwrite existing values or components.
 *                         Defaults to <code>true</code>
 *     <li><i>upload</i> -- A boolean attribute which indicates if the component
 *                          should be transferred to the (potentially) remote
 *                          server before executing the command.  If the 
 *                          application server is running on the local machine,
 *                          this may be set to <code>false</code> to reduce 
 *                          execution time.  Defaults to <code>true</code>
 *     <li><i>contextroot</i> -- The context root for a web module (WAR file).  
 *                               This attribute is only used when deploying WAR
 *                               files to the application server
 *   </ul>
 * <p>
 * In addition, this abstract class provides support for nested &lt;component&gt;
 * elements.  These nested elements enable commands to be executed using multiple 
 * components in a single Ant command.  Attributes may  be specified in the 
 * "root" command element and those values will be used ("inherited") by all of 
 * the nested &lt;component&gt; components.  If an attribute is specified in 
 * both elements, the value specified in the &lt;component&gt; element will be 
 * used.
 *
 * Furthermore, this abstract class provides support for nested &lt;fileset&gt;
 * elements.  These may be used to match multiple components which will be used
 * for the respective application server admin commands.
 *
 * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
 */
public abstract class ComponentAdmin extends AppServerAdmin {

    LocalStringsManager lsm = new LocalStringsManager();

	/**
	 * This Component instance will store user settings from the "root" command
	 * element.
	 */
	protected Component component;

	/**
	 * The list of components will store user settings from nested 
	 * &lt;component&gt; elements.
	 */
	protected List components = new ArrayList();

	/**
	 * The list of component archives which match a nested <fileset> element.
	 */
	private List filesets   = new ArrayList();

	protected static final String TYPE_APP    = "application";
	protected static final String TYPE_EJB    = "ejb";
	protected static final String TYPE_WEB    = "web";
	protected static final String TYPE_CONN   = "connector";
	protected static final String TYPE_CLIENT = "client";

	/**
	 * Constants for each of the component types.  In addition, the archive file
	 * extensions are mapped to their appropriate component types.  Note that
	 * ".jar" maps only to EJB although this is a valid file extension for both
	 * EJB JARs and application clients.
	 */
	protected static final java.util.Map TYPE_MAP = new HashMap(4);
	static {
		TYPE_MAP.put("ear", TYPE_APP);
		TYPE_MAP.put("jar", TYPE_EJB);
		TYPE_MAP.put("war", TYPE_WEB);
		TYPE_MAP.put("rar", TYPE_CONN);
	};

	/**
	 * Creates a new instance with the infrastructure for running admin commands
	 * which use components (EARs, WARs, JARs, and RARs).
	 */
	public ComponentAdmin() {
		super();
		component = getNewComponent();
	}

	/**
	 * Builds a new component instance.  This method is intended to be overridden
	 * by subclasses which implement their own subclass of Component.
	 *
	 * @return new Component instance.
	 */
	protected Component getNewComponent() {
		 return new Component(component);
	}

	/**
	 * Sets the filename for the component file (or directory) which is used by
	 * the administrative command.
	 *
	 * @param file The component file archive or directory
	 */
	public void setFile(File file) {
		component.setFile(file);  // Delegates to component object
	}

	/**
	 * Sets the display-name for the J2EE component.  This is the "friendly name" 
	 * which will appear in the admin GUI and administrative commands
	 *
	 * @param name The component display-name
	 */
	public void setName(String name) {
		component.setName(name);  // Delegates to component object
	}

	/**
	 * Sets the component type.  Valid types are "application", "ejb", "web", 
	 * "connector" and "client" 
	 *
	 * @param type The component type
	 */
	public void setType(String type) {
		component.setType(type);  // Delegates to component object
	}

	/**
	 * Sets the deployment target.  
	 *
	 * @param target The deployment target
	 */
	public void setTarget(String target) {
		component.setTarget(target);  // Delegates to component object
	}

    /**
     * Adds a nested <code>fileset</code> element.
	 *
	 * @param fileset The nested fileset component.
     */
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }

    /**
     * Creates a nested <code>component</code> element.
	 *
	 * @return Component which has been added
     */
	public Component createComponent() {
		Component newComponent = getNewComponent();
		components.add(newComponent);
		return newComponent;
	}

	protected void prepareToExecute() throws BuildException {
		super.prepareToExecute();
		processFilesets();
		if (components.size() == 0) {
			components.add(component);
		}

	}

	protected void execute(Server aServer) throws BuildException {
		Iterator iterator = components.iterator();
		while (iterator.hasNext()) {  // Execute command using each component
			Component comp = (Component)iterator.next();
			String cmdString = getCommandString(aServer, comp);
			execAdminCommand(cmdString);
		}
	}

    /**
     * Examines each nested fileset and adds the matching files and directories
	 * to the "components" List object.
     */
	private void processFilesets() {
		for (int i = 0; i < filesets.size(); i++) {
			FileSet fileset = (FileSet) filesets.get(i);
			DirectoryScanner scanner = fileset.getDirectoryScanner(project);
			File baseDir = scanner.getBasedir();

			String[] files = scanner.getIncludedFiles();
			for (int j = 0; j < files.length; j++) {
				Component archive = getNewComponent();
				archive.setFile(new File(baseDir, files[j]));
				components.add(archive);
			}

			String[] dirs = scanner.getIncludedDirectories();
			for (int j = 0; j < dirs.length; j++) {
				Component expandedArchive = getNewComponent();
				expandedArchive.setFile(new File(baseDir, dirs[j]));
				components.add(expandedArchive);
			}
		}
	}

	protected void checkConfiguration() throws BuildException {
		super.checkConfiguration();

		log(components.size() + " components were found.", Project.MSG_DEBUG);

		if (components.size() == 0) {  // This isn't necessarily a failure
			log(lsm.getString("NoComponentsSpecified"), Project.MSG_WARN);
		}

	}

    /**
     * Verifies that the options and parameters selected by the user are valid
	 * and consistent for a given server.
	 *
	 * @param aServer The server whose configuration is to be validated.
	 * @throws BuildException If the user selections are invalid.
     */
	protected void checkConfiguration(Server aServer) throws BuildException {
		String hostname = aServer.getHost();
		if (hostname == null) {
			hostname = "localhost";
		}
		log("Checking server config for " + hostname, Project.MSG_DEBUG);

		if (!aServer.hasPassword()) {
			throw new BuildException(lsm.getString("PasswordMustBeSpecified", 
							       new Object[] {getTaskName(), 
							       hostname}), getLocation());
		}

		Iterator iterator = components.iterator();
		while (iterator.hasNext()) {  // Check the config on each component found
			Component comp = (Component)iterator.next();
			checkComponentConfig(aServer, comp);
		}
	}

    /**
     * Verifies the options and parameters selected by the user are valid and
	 * consistent for a given server and given component.
	 *
	 * @param aServer The server where the command will be executed.
	 * @param comp The component which is the target of the command.
     */
	protected void checkComponentConfig(Server aServer, Component comp) {
		log("Checking config for server \"" + aServer + "\" and component \""
				+ comp + "\"", Project.MSG_DEBUG);

		// if specified, file must exist (either directory or file)
		File theFile = comp.getFile();
		log("The file for this component: " + theFile, Project.MSG_DEBUG);
		if ((theFile != null) && (!theFile.exists())) {
			throw new BuildException(lsm.getString("FileCouldNotBeFound",
							       new Object[] {theFile}), 
							       getLocation());
		}

		// name must be >0 characters
		String theName = comp.getName();
		if ((theName == null) || (theName.length() == 0)) {
			throw new BuildException(lsm.getString("CouldNotDetermineComponentName"),
						 getLocation());
		}

		// type must be valid
		String theType = comp.getType();
		if ((theType != null) && (!TYPE_MAP.values().contains(theType))) {
			throw new BuildException(lsm.getString("TypeNotValid", 
							       new Object[] {theType}), 
						 getLocation());
		}

	}

    /**
     * Gets the Sun ONE Application Server command to be executed on the
	 * specified server for the given component.
	 *
	 * @param aServer The server where the command will be executed.
	 * @param comp The component which is the target of the command.
     */
	protected abstract String getCommandString(Server aServer, Component comp);

	/**
	 * This inner class is used to represent J2EE components used with the
	 * Sun ONE Application Server.
	 *
     * @author Greg Nelson <a href="mailto:gn@sun.com">gn@sun.com</a>
	 */
	public class Component {
		protected Component parent;       // Attr values are inherited from parent
		private   File      file;         // Component archive or directory
		private   String    name;         // Display-name for the component
		private   String    type;         // Component type -- app, web, ejb, etc
		private	  String    target;       // Target application server entity

		/*
		 * Default values for some attributes.
		 */
		private static final String  DEFAULT_TYPE   = TYPE_APP;

		/**
		 * Constructs a new Component object without specifying a parent
		 * component from which values are inherited.
		 */
		public Component() {
			this(null);
		}

		/**
		 * Constructs a new Component object and specifies the parent component
		 * from which attribute values are inherited.
		 * 
		 * @param component The parent component for this object.
		 */
		public Component(Component parent) {
			this.parent = parent;
		}

		/**
		 * Sets the parent server for this component.  If attribute values are not
		 * explicitly set for this object, they may be inherited from the parent
		 * object.
		 * 
		 * @param parent The parent server for this object.
		 */
		public void setParent(Component parent) {
			this.parent = parent;
		}

		/**
		 * Sets the filename for the component file (or directory) which is used
		 * by the administrative command.
		 *
		 * @param file The component file archive or directory
		 */
		public void setFile(File file) {
			this.file = file;
		}

		/**
		 * Returns the filename for the component file (or directory) which is 
		 * used by the administrative command.
		 *
		 * @return The component file archive or directory
		 */
		protected File getFile() {
			return file;
		}

		/**
		 * Sets the display-name for the J2EE component.  This is the "friendly 
		 * name" which will appear in the admin GUI and administrative commands.
		 *
		 * @param name The component display-name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the display-name for the J2EE component.  This is the
		 * "friendly name" which will appear in the admin GUI and administrative
		 * commands.
		 *
		 * @return The component display-name
		 */
		protected String getName() {
			if (name == null) {
				/* Use the file name (before the last '.') as the default name */
				String fileName = null;

				if (file != null) {
					fileName = file.getName();
				} else {
					return null;
				}

				int index = fileName.indexOf('.');
				name = (index < 0) ? fileName : fileName.substring(0, index);
			}

			return name;
		}

		/**
		 * Sets the component type.  Valid types are "application", "ejb", "web", 
		 * "connector" and "client" 
		 *
		 * @param type The component type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Returns the component type.  Valid types are "application", "ejb",
		 * "web", "connector" and "client" 
		 *
		 * @return The component type
		 */
		protected String getType() {
			if (type == null) {
			    return null;
			}
			return type;
		}

		/**
		 * Sets the deployment target name, 
		 * ex. valid server instance name or cluster name
		 *
		 * @param target Valid target name
		 */
		public void setTarget(String target) {
			this.target = target;
		}

		/**
		 * Returns the name of deployment target
		 *
		 * @return The target name
		 */
		protected String getTarget() {
			if (target == null) {
			    return null;
			}
			return target;
		}

		public String toString() {
			return getName();
		}
	};  // end of Component inner class
}
