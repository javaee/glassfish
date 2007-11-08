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

public abstract class ComponentAdmin extends IasAdmin {
	private Component component;
	private List    components = new ArrayList();
	private List    filesets   = new ArrayList();

	protected static final String TYPE_APP    = "application";
	protected static final String TYPE_EJB    = "ejb";
	protected static final String TYPE_WEB    = "web";
	protected static final String TYPE_CONN   = "connector";
	protected static final String TYPE_CLIENT = "client";

	protected static final java.util.Map TYPE_MAP = new HashMap(4); // FIXME (4 or 5 elements?)
	static {
		TYPE_MAP.put("ear", TYPE_APP);
		TYPE_MAP.put("jar", TYPE_EJB);
		TYPE_MAP.put("war", TYPE_WEB);
		TYPE_MAP.put("rar", TYPE_CONN);
//		TYPE_MAP.put("???", TYPE_CLIENT);  // FIXME
	};

	public void setFile(File file) {
		getComponent().setFile(file);
	}

	public void setName(String name) {
		getComponent().setName(name);
	}

	public void setType(String type) {
		getComponent().setType(type);
	}

	public void setForce(boolean force) {
		getComponent().setForce(force);
	}

	public void setUpload(boolean upload) {
		getComponent().setUpload(upload);
	}

	public void setContextroot(String contextroot) {
		getComponent().setContextroot(contextroot);
	}

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }

	public Component createComponent() {
		log("createComponent", Project.MSG_DEBUG);
		Component newComponent = new Component(component);
		components.add(newComponent);
		return newComponent;
	}

	private Component getComponent() {
		if (component == null) {
			component = new Component();
			Iterator it = components.iterator();
			while (it.hasNext()) {
				Component aComponent = (Component)it.next();
				aComponent.setParent(component);
			}
			components.add(0, component);
		}

		return component;
	}

	protected void prepareToExecute() throws BuildException {
		super.prepareToExecute();
		processFilesets();
	}

	protected void execute(Server server) throws BuildException {
		Iterator iterator = components.iterator();
		while (iterator.hasNext()) {
			Component comp = (Component)iterator.next();
			String cmdString = getCommandString(server, comp);
			execIasCommand(cmdString);
		}
	}

	private void processFilesets() {
		for (int i = 0; i < filesets.size(); i++) {
			FileSet fileset = (FileSet) filesets.get(i);
			DirectoryScanner scanner = fileset.getDirectoryScanner(project);
			File baseDir = scanner.getBasedir();

			String[] files = scanner.getIncludedFiles();
			for (int j = 0; j < files.length; j++) {
				Component archive = new Component();
				archive.setFile(new File(baseDir, files[j]));
				components.add(archive);
			}

			String[] dirs = scanner.getIncludedDirectories();
			for (int j = 0; j < dirs.length; j++) {
				Component expandedArchive = new Component();
				expandedArchive.setFile(new File(baseDir, dirs[j]));
				components.add(expandedArchive);
			}
		}
	}

	protected void checkConfiguration() throws BuildException {
		super.checkConfiguration();

		if (components.size() == 0) {
			log("WARNING!  No components were specified.", Project.MSG_WARN);
		}

		log(components.size() + " components were found.", Project.MSG_DEBUG);

		Iterator iterator = components.iterator();
		while (iterator.hasNext()) {
			Component comp = (Component)iterator.next();
			checkComponentConfig(comp);
		}
	}

	protected void checkComponentConfig(Component comp) throws BuildException {
		log("Checking configuration for " + comp.getName(), Project.MSG_DEBUG);

		// if specified, file must exist (either directory or file)
		File theFile = comp.getFile();
		if ((theFile != null) && (!theFile.exists())) {
			String msg = "The file specified (" + theFile + ") could not be found.";
			throw new BuildException(msg, getLocation());
		}

		// name must be >0 characters
		String theName = comp.getName();
		if ((theName == null) || (theName.length() == 0)) {
			String msg = "A valid name for the component could not be determined.";
			throw new BuildException(msg, getLocation());
		}

		// type must be valid
		String theType = comp.getType();
		if ((theType != null) && (!TYPE_MAP.values().contains(theType))) {
			String msg = "The type specified (" + theType + ") is not valid.";
			throw new BuildException(msg, getLocation());
		}
	}

	protected abstract String getCommandString(Server server, Component comp);

	public class Component {
		private Component parent;
		private File      file;
		private String    name;
		private String    type;
		private boolean   force;
		private boolean   upload;
		private String    contextroot;

		private boolean forceIsSet = false;
		private boolean uploadIsSet = false;
		private boolean contextRootIsSet = false;

		private static final String  DEFAULT_TYPE   = TYPE_APP;
		private static final boolean DEFAULT_FORCE  = true;
		private static final boolean DEFAULT_UPLOAD = true;


		public Component() {
			this(null);
		}

		public Component(Component parent) {
			this.parent = parent;
		}

		public void setParent(Component parent) {
			this.parent = parent;
		}

		public void setFile(File file) {
			this.file = file;
		}

		protected File getFile() {
			return file;
		}

		public void setName(String name) {
			this.name = name;
		}

		protected String getName() {
			if (name == null) {
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

		public void setType(String type) {
			this.type = type;
		}

		/**
		 * 
		 */
		protected String getType() {
			if (type == null) {
				String fileName = null;
				String extension = null;
				int    lastIndex;

				if (file != null) {
					fileName = file.getName();
					lastIndex = fileName.lastIndexOf('.');
					if (lastIndex >= 0) {
						extension = fileName.substring(lastIndex + 1);
					}

					type = (String) TYPE_MAP.get(extension);
				}
			}

			if (type == null) {
				return (parent == null) ? null : parent.getType();
			}
			return type;
		}

		public void setForce(boolean force) {
			this.force = force;
			forceIsSet = true;
		}

		protected boolean getForce() {
			if (!forceIsSet) {
				return (parent == null) ? DEFAULT_FORCE : parent.getForce();
			}
			return force;
		}

		public void setUpload(boolean upload) {
			this.upload = upload;
			uploadIsSet = true;
		}

		protected boolean getUpload() {
			if (!uploadIsSet) {
				return (parent == null) ? DEFAULT_UPLOAD : parent.getUpload();
			}
			return upload;
		}

		public void setContextroot(String contextroot) {
			contextRootIsSet = true;
			this.contextroot = contextroot;
		}

		protected String getContextroot() {
			return (contextroot != null) ? contextroot : getName();
		}

		protected boolean contextRootIsSet() {
			return contextRootIsSet;
		}
	};
}