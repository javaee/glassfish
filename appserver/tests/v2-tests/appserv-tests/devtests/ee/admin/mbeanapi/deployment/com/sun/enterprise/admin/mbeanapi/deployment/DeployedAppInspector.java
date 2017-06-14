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
