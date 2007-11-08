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
import org.apache.tools.ant.BuildException;

import java.io.File;

public class DeployTask extends ComponentAdmin {
	private static final String DEPLOY_COMMAND = "deploy";

	protected void checkComponentConfig(Component comp) throws BuildException {
		super.checkComponentConfig(comp);

		// file must exist (either directory or file)
		File theFile = comp.getFile();
		if ((theFile == null) || (!theFile.exists())) {
			String msg = "The file specified (" + theFile + ") could not be found.";
			throw new BuildException(msg, getLocation());
		}

		// contextroot only makes sense when deploying web components
		String theType = comp.getType();
		if ((theType != null) && (!theType.equals(TYPE_WEB)) && (comp.contextRootIsSet())) {
			String msg = "The \"contextroot\" attribute doesn't apply to "
							+ comp.getType() + " files -- it only applies to war files.";
			log(msg, Project.MSG_WARN);
		}
	}

	protected String getCommandString(Server server, Component comp) {
		StringBuffer cmdString = new StringBuffer(DEPLOY_COMMAND);
		cmdString.append(server.getCommandParameters(true));
		if (comp.getType() != null) {
			cmdString.append(" --type ").append(comp.getType());
			if (comp.getType().equals(TYPE_WEB)) {
				cmdString.append(" --contextroot ").append(comp.getContextroot());
			}
		}

		cmdString.append(" --force=").append(comp.getForce());
		cmdString.append(" --name ").append(comp.getName());
		cmdString.append(" --upload=").append(comp.getUpload());
		cmdString.append(" ").append(comp.getFile().getPath());

		return cmdString.toString();
	}
}