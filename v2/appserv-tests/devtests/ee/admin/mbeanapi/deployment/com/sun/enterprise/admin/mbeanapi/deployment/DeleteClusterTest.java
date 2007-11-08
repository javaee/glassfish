package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class DeleteClusterTest extends BaseTest
{
    private final Cmd target;

    public DeleteClusterTest(final String user, final String password,
            final String host, final int port, final String clusterName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final DeleteClusterCmd deleteClusterCmd = cmdFactory.
                createDeleteClusterCmd(clusterName);

        target = new PipeCmd(connectCmd, deleteClusterCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new DeleteClusterTest("admin", "password", "localhost", 8686, args[0]).
                run();
    }
}
