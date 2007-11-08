package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.config.StandaloneServerConfig;

/**
 */
public class InstanceLifecycleTest extends BaseTest
{
    private final Cmd target;

    public InstanceLifecycleTest(final String user, final String password,
            final String host, final int port, final String instanceName,
            final String nodeAgentName, final String configName, 
            final Map optional)
    {
        final CmdChainCmd chain = new CmdChainCmd();

        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);
        final CreateInstanceCmd createInstanceCmd = cmdFactory.
                createCreateInstanceCmd(instanceName, nodeAgentName, 
                        configName, optional);
        final StartInstanceCmd startInstanceCmd = cmdFactory.
                createStartInstanceCmd(instanceName);
        final StopInstanceCmd stopInstanceCmd = cmdFactory.
                createStopInstanceCmd(instanceName);
        final DeleteInstanceCmd deleteInstanceCmd = cmdFactory.
                createDeleteInstanceCmd(instanceName);

        chain.addCmd(new PipeCmd(connectCmd, createInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, startInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, stopInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, deleteInstanceCmd));

        target = chain;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new InstanceLifecycleTest("admin", "password", "localhost", 8686,
            args[0], "n1", null, null).run();
    }
}
