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

import org.apache.tools.ant.BuildException;

import java.util.Map;
import java.util.HashMap;

public class InstanceTask extends IasAdmin {
	private String action;

	private static final String ACTION_START   = "start";
	private static final String ACTION_STOP    = "stop";
	private static final String ACTION_RESTART = "restart";
	private static final String ACTION_CREATE  = "create";
	private static final String ACTION_DESTROY = "destroy";

	private static final Map ACTION_MAP = new HashMap(5);
	static {
		ACTION_MAP.put(ACTION_START, "start-instance");
		ACTION_MAP.put(ACTION_STOP, "stop-instance");
		ACTION_MAP.put(ACTION_RESTART, null);
		ACTION_MAP.put(ACTION_CREATE, "create-instance");
		ACTION_MAP.put(ACTION_DESTROY, "delete-instance");
	};

	public void setAction(String action) {
		this.action = action;
	}

	protected void checkConfiguration() throws BuildException {
		super.checkConfiguration();
	
		if (action == null) {
			String msg = "The action command must be specified.";
			throw new BuildException(msg, getLocation());
		}

		if (!ACTION_MAP.containsKey(action)) {

			String msg = "The action command (\"" + action + "\") is invalid.";
			throw new BuildException(msg, getLocation());
		}
	}

	protected void checkConfiguration(Server aServer) throws BuildException {
		super.checkConfiguration(aServer);
		if ((action.equals(ACTION_CREATE) || action.equals(ACTION_DESTROY)) &&
				(aServer.getInstance() == null)) {
			String msg = "When creating or destroying an application server "
							+ "instance, the \"instance\" attribute is required.";
			throw new BuildException(msg, getLocation());
		}
	}

	protected void execute(Server aServer) throws BuildException {
		if (action.equals(ACTION_RESTART)) {
			execute(ACTION_STOP, aServer);
			execute(ACTION_START, aServer);
		} else {
			execute(action, aServer);
		}
	}

	private void execute(String anAction, Server aServer) throws BuildException {
		String cmdString = (String) ACTION_MAP.get(anAction);
		cmdString +=aServer.getCommandParameters(false);
		if (anAction.equals(ACTION_CREATE)) {
			cmdString += " --instanceport " + aServer.getInstanceport();
		}
		if (aServer.getInstance() != null) {
			cmdString += " " + aServer.getInstance();
		}
		execIasCommand(cmdString);
	}
}