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
 * HADBPingAgent.java
 *
 * Created on April 6, 2005, 4:41 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import javax.management.remote.*;
import java.util.*;

/**
 *
 * @author bnevins
 */
public class HADBPingAgent
{
	public HADBPingAgent(HADBInfo Info) throws HADBSetupException
	{
		info = Info;
		final String[]	hostsS	= info.getHostsArray();
		port					= info.getAgentPort();
		
		// make a *set* of hosts -- there could easily be duplicates!
		hosts = new HashSet<String>();
		
		for(String host : hostsS)
			hosts.add(host);
		
		results = new PingResult[hosts.size()];
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public Object[] ping() throws HADBSetupException
	{
		int i = 0;
		for(String host : hosts)
		{
			if(HADBUtils.pingWithJMX())
				results[i++] = pingWithJMX(host);
			else
				results[i++] = pingWithHADBM(host);
		}
		
		// post-process the results into an Object[2]
		// Object[0] == "true" or "false" -- overall (i.e. if ANY of the hosts
		//    pinged true, overall is true - else it is false
		// Object[1] == comments

		Object[] ret = new Object[2];
		boolean overall = false;
		StringBuilder finalMesg = new StringBuilder("Details:");
		
		for(PingResult result : results)
		{
			if(result.ok)
				overall = true;
			
			finalMesg.append('\n').append(result.mesg);
		}
		
		ret[0] = overall ? "true" : "false";
		ret[1] = finalMesg.toString();
		return ret;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private PingResult pingWithHADBM(String host) throws HADBSetupException
	{
		HADBUtils.setPhonyReturnValue(0);
		String[]		commands	= info.getIsAliveCommands(host);
		String			agentURL	= info.getAgentURL(host);
		int				exitValue	= -1;
		HADBMExecutor	exec		= null;
		
		// treat any Exception as an "unpingable" state.
		try
		{
			exec = new HADBMExecutor(info.getExecutable(), commands);
			exitValue = exec.exec();
		}
		catch(Exception e)
		{
			exitValue = -1;
		}
		
		if(exitValue == 0)
		{
			return new PingResult(true, StringHelper.get("hadbmgmt-res.PingGood", agentURL));
		}
		else
		{
			return new PingResult(false, StringHelper.get("hadbmgmt-res.PingBad", agentURL, exec.getStdout() + exec.getStderr()));
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	private PingResult pingWithJMX(String host) throws HADBSetupException
	{
		info.assertSetup();
		
		final String	protocol	= "jmxmp";
		JMXServiceURL	url			= null;
		try
		{
			url = new JMXServiceURL(protocol, host, port);
			JMXConnector jc = JMXConnectorFactory.connect(url);
			return new PingResult(true, StringHelper.get("hadbmgmt-res.PingGood", url));
		}
		catch(Exception e)
		{
			return new PingResult(false, StringHelper.get("hadbmgmt-res.PingBad", url, e));
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private HADBInfo			info;
	private int					port;
	private Set<String>			hosts;
	private PingResult[]		results;
	
	private static class PingResult
	{
		private PingResult(boolean OK, String Mesg)
		{
			ok = OK;
			mesg = Mesg;
		}
		String mesg;
		boolean ok;
	}
}
