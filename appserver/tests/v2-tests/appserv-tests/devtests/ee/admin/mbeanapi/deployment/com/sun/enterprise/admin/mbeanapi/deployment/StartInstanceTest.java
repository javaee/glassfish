package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class StartInstanceTest extends BaseTest
{
    private final Cmd target;

    public StartInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final StartInstanceCmd startInstanceCmd = cmdFactory.
                createStartInstanceCmd(instanceName);

        target = new PipeCmd(connectCmd, startInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new StartInstanceTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
