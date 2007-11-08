package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class StopInstanceTest extends BaseTest
{
    private final Cmd target;

    public StopInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final StopInstanceCmd stopInstanceCmd = cmdFactory.
                createStopInstanceCmd(instanceName);

        target = new PipeCmd(connectCmd, stopInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new StopInstanceTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
