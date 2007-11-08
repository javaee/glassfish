package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class StartClusterTest extends BaseTest
{
    private final Cmd target;

    public StartClusterTest(final String user, final String password,
            final String host, final int port, final String clusterName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final StartClusterCmd startClusterCmd = cmdFactory.
                createStartClusterCmd(clusterName);

        target = new PipeCmd(connectCmd, startClusterCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new StartClusterTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
