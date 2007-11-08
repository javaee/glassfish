package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class DeleteClusteredInstanceTest extends BaseTest
{
    private final Cmd target;

    public DeleteClusteredInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final DeleteClusteredInstanceCmd deleteClusteredInstanceCmd = 
                cmdFactory.createDeleteClusteredInstanceCmd(instanceName);

        target = new PipeCmd(connectCmd, deleteClusteredInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new DeleteClusteredInstanceTest("admin", "password", "localhost", 
            8686, args[0]).run();
    }
}
