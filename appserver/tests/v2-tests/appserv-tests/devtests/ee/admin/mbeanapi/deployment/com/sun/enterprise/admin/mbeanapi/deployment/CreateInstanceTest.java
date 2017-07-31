package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.config.StandaloneServerConfig;

/**
 */
public class CreateInstanceTest extends BaseTest
{
    private final Cmd target;

    public CreateInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName,
            final String nodeAgentName, final String configName, 
            final Map optional)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateInstanceCmd createInstanceCmd = cmdFactory.
                createCreateInstanceCmd(instanceName, nodeAgentName, 
                        configName, optional);

        target = new PipeCmd(connectCmd, createInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new CreateInstanceTest("admin", "password", "localhost", 8686,
            args[0], "n1", null, null).run();
    }
}
