package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public class CreateClusteredInstanceTest extends BaseTest
{
    private final Cmd target;

    public CreateClusteredInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName,
            final String clusterName, final String nodeAgentName, 
            final Map optional)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateClusteredInstanceCmd createClusteredInstanceCmd = 
                cmdFactory.createCreateClusteredInstanceCmd(
                    instanceName, clusterName, nodeAgentName, optional);

        target = new PipeCmd(connectCmd, createClusteredInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String instanceName   = args[0];
        final String clusterName    = args[1];
        final String nodeAgentName  = args[2];

        new CreateClusteredInstanceTest("admin", "password", "localhost", 8686,
            instanceName, clusterName, nodeAgentName, null).run();
    }
}
