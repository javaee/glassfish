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
 * HADBDeleteDB.java
 *
 * Created on June 1, 2004, 10:25 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.util.*;

/**
 *
 * @author  bnevins
 */
public class HADBRemoveCluster
{
	
	/** Creates a new instance of HADBDeleteDB */
	public HADBRemoveCluster(HADBInfo info)
	{
		this.info = info;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public Object[] remove() throws HADBSetupException
	{
		boolean doDeleteDB = true;
		
		try
		{
			HADBPingAgent pa = new HADBPingAgent(info);
			Object[] result = pa.ping();
			
			if(result == null || result.length < 2 || result[0].equals("false"))
				throw new HADBSetupException("hadbmgmt-res.PingBad", new Object[] { info.getClusterName(), result[1]} );
		}
		catch(HADBSetupException hse)
		{
			String s = hse.getMessage();
			s += "  ";
			String s2 = StringHelper.get("hadbmgmt-res.RemoveMAIsDead");
			info.addMsg(s + s2);
			LoggerHelper.warning(s2);
			doDeleteDB = false;
		}
		
		if(doDeleteDB)
			deleteDB();
		
		deleteResources();
		
		return info.prepMsgs();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void deleteDB() throws HADBSetupException
	{
		
		//First stop the database. For now we ignore the result (since the
		//database may already be in a stopped state).
		HADBUtils.setPhonyReturnValue(0);
		String[] commands = info.getStopCommands();
		HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);
		int exitValue = exec.exec();

		if(exitValue != 0)
			info.addMsg(StringHelper.get("hadbmgmt-res.StopFailed", info.getClusterName()));
		else
			info.addMsg(StringHelper.get("hadbmgmt-res.StopSucceeded", info.getClusterName()));

		//Now that the database is stopped, delete it
		commands = info.getDeleteCommands();
		HADBUtils.setPhonyReturnValue(0);
		exec = new HADBMExecutor(info.getExecutable(), commands);
		exitValue = exec.exec();

		if(exitValue != 0)
		{
			// maybe they already deleted it?  In any case, we may as well try
			//to get rid of the resources too.  If we throw an Exception the resources
			// will remain...
			String s = StringHelper.get("hadbmgmt-res.DeleteFailed",
				new Object[] {"" + exitValue, exec.getStdout(), exec.getStderr()} );
			LoggerHelper.warning(s);
			info.addMsg(s);
		}
		
		else
		{
			String s = StringHelper.get("hadbmgmt-res.DeleteDBOutput", exec.getStdout(), exec.getStderr());
			LoggerHelper.fine(s);
			info.addMsg(s);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void deleteResources()  throws HADBSetupException
	{
		HADBResourceManager mgr = new HADBResourceManager(info);
		try
		{
			// the idea here -- if there are failures, log it and keep going...
			mgr.deletePool();
			info.addMsg(StringHelper.get("hadbmgmt-res.DeletePoolSuccess"));
		}
		catch(HADBSetupException hse)
		{
			String s = StringHelper.get("hadbmgmt-res.DeletePoolFailed", hse.toString());
			LoggerHelper.warning(s);
			info.addMsg(s);
		}
		
		mgr.enableAvailabilityService(false);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	
	private HADBInfo	info;
}
