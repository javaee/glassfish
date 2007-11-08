package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.Set;

/**
 */
public class ListDeployedAppsTest extends BaseTest
{
    private final Cmd targetCmd;

    public ListDeployedAppsTest(final String user, final String password,
            final String host, final int port, final String target,
            final String appType)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final ListDeployedAppsCmd cmd = cmdFactory.createListDeployedAppsCmd(
                target, appType);

        targetCmd = new PipeCmd(connectCmd, cmd);
    }

    protected void runInternal() throws Exception
    {
        Set[] sets = (Set[])targetCmd.execute();

		//System.out.println("Set1: " + sets[0]);
		//System.out.println("Set2: " + sets[1]);
		
		if(sets.length > 1)
			sets[0].retainAll(sets[1]);
		
		results = new String[sets[0].size()];
		sets[0].toArray(results);
    }

	String[] getResults()
	{
		return results;
	}
    public static void main(String[] args) throws Exception
    {
        new ListDeployedAppsTest("admin", "password", "localhost", 8686, 
                args[0], args[1]).run();
    }
	
	private String[] results;
}
