package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class UndeploymentTest extends BaseTest
{
    private Cmd cmd;

    public UndeploymentTest(String user, String password, String host, 
            int port, String name, String target)
    {
        CmdFactory cmdFactory = getCmdFactory();

        ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        UndeployCmd undeployCmd = cmdFactory.
                createUndeployCmd(name, target);

        cmd = new PipeCmd(connectCmd, undeployCmd);
    }

    protected void runInternal() throws Exception
    {
        cmd.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String appName            = args[0];
        final String appserverTarget    = args.length == 2 ? args[1] : null;

        new UndeploymentTest("admin", "password", "localhost", 8686,
            appName, appserverTarget).run();
    }
}
