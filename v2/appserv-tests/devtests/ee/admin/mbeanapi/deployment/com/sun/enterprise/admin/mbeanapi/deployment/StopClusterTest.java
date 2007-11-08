package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class StopClusterTest extends BaseTest
{
    private final Cmd target;

    public StopClusterTest(final String user, final String password,
            final String host, final int port, final String clusterName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final StopClusterCmd stopClusterCmd = 
                cmdFactory.createStopClusterCmd(clusterName);

        target = new PipeCmd(connectCmd, stopClusterCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new StopClusterTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
