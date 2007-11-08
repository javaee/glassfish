package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public class CreateClusterTest extends BaseTest
{
    private final Cmd target;

    public CreateClusterTest(final String user, final String password,
            final String host, final int port, final String clusterName,
            final String configName, final Map optional)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateClusterCmd createClusterCmd = 
                cmdFactory.createCreateClusterCmd(
                    clusterName, configName, optional);

        target = new PipeCmd(connectCmd, createClusterCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String clusterName    = args[0];
        final String configName     = args.length == 2 ? args[1] : null;

        new CreateClusterTest("admin", "password", "localhost", 8686,
            clusterName, configName, null).run();
    }
}
