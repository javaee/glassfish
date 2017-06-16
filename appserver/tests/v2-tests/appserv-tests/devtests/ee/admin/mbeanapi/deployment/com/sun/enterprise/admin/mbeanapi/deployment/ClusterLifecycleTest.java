package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public class ClusterLifecycleTest extends BaseTest
{
    private final Cmd target;

    public ClusterLifecycleTest(final String user, final String password,
            final String host, final int port, final String clusterName, 
            final String instanceName, final String nodeAgentName, 
            final String configName, final Map optional)
    {
        final CmdChainCmd chain = new CmdChainCmd();

        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateClusterCmd createClusterCmd = 
                cmdFactory.createCreateClusterCmd(
                    clusterName, configName, optional);

        final CreateClusteredInstanceCmd createClusteredInstanceCmd = 
                cmdFactory.createCreateClusteredInstanceCmd(instanceName, 
                        clusterName, nodeAgentName, optional);

        final StartClusterCmd startClusterCmd = 
                cmdFactory.createStartClusterCmd(clusterName);

        final StopClusterCmd stopClusterCmd = 
                cmdFactory.createStopClusterCmd(clusterName);

        final DeleteClusteredInstanceCmd deleteClusteredInstanceCmd = 
                cmdFactory.createDeleteClusteredInstanceCmd(instanceName);

        final DeleteClusterCmd deleteClusterCmd = cmdFactory.
                createDeleteClusterCmd(clusterName);

        chain.addCmd(new PipeCmd(connectCmd, createClusterCmd));
        chain.addCmd(new PipeCmd(connectCmd, createClusteredInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, startClusterCmd));
        chain.addCmd(new PipeCmd(connectCmd, stopClusterCmd));
        chain.addCmd(new PipeCmd(connectCmd, deleteClusteredInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, deleteClusterCmd));

        target = chain;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String clusterName    = args[0];
        final String instanceName   = args[1];
        final String nodeAgentName  = args[2];
        final String configName     = args.length == 4 ? args[3] : null;

        new ClusterLifecycleTest("admin", "password", "localhost", 8686,
            clusterName, instanceName, nodeAgentName, configName, null).run();
    }
}
