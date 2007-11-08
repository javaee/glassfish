package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class DeleteInstanceTest extends BaseTest
{
    private final Cmd target;

    public DeleteInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final DeleteInstanceCmd deleteInstanceCmd = cmdFactory.
                createDeleteInstanceCmd(instanceName);

        target = new PipeCmd(connectCmd, deleteInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new DeleteInstanceTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
