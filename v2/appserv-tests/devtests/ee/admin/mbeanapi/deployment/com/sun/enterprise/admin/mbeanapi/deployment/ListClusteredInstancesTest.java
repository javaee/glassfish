package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.Set;

/**
 */
public class ListClusteredInstancesTest extends BaseTest
{
    private final Cmd target;

    public ListClusteredInstancesTest(final String user, final String password,
            final String host, final int port)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final ListClusteredInstancesCmd cmd = new ListClusteredInstancesCmd(
            new CmdEnv());

        target = new PipeCmd(connectCmd, cmd);
    }

    protected void runInternal() throws Exception
    {
        final Map clusteredServerProxyMap = (Map)target.execute();
        final Set names = clusteredServerProxyMap.keySet();
        System.out.println("Clustered Servers=" + names);
    }

    public static void main(String[] args) throws Exception
    {
        new ListClusteredInstancesTest("admin", "password", "localhost", 8686).
                run();
    }
}
