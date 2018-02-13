/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.Set;

/**
 */
class DeployedAppInspector
{
	DeployedAppInspector(ConnectCmd ccmd, String target)
	{
		CmdFactory cmdFactory = Env.getCmdFactory();
		ListDeployedAppsCmd cmd = cmdFactory.createListDeployedAppsCmd(target, "All");
		targetCmd = new PipeCmd(ccmd, cmd);
	}

	////////////////////////////////////////////////////////////////////////////
	
	DeployedAppInspector(String user, String password, String host, 
			int port, String target)
	{
		CmdFactory cmdFactory = Env.getCmdFactory();
		ConnectCmd connectCmd = cmdFactory.createConnectCmd(user, password, host, port);
		ListDeployedAppsCmd cmd = cmdFactory.createListDeployedAppsCmd(target, "All");
		targetCmd = new PipeCmd(connectCmd, cmd);
	}

	////////////////////////////////////////////////////////////////////////////
	
	boolean isDeployed(String id) throws DeploymentTestsException
	{
		try
		{
			if(results == null)
				refresh();
		
			return results.contains(id);
		}
		catch(Exception e)
		{
			throw new DeploymentTestsException("Exception caught in DeployedAppInspector.isDeployed().", e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	
	void refresh()
	{
		Set[] sets = null;
		
		try
		{
			sets = (Set[])targetCmd.execute();
		}
		catch(Exception e)
		{
			// note: the called method literally declares 'throws Exception' !!!
			throw new RuntimeException(e);
		}
		
		// get the intersection of the 2 sets
		if(sets.length > 1)
			sets[0].retainAll(sets[1]);
		
		results = sets[0];
	}

	////////////////////////////////////////////////////////////////////////////
	
	private Set results;
	private Cmd targetCmd;
}
