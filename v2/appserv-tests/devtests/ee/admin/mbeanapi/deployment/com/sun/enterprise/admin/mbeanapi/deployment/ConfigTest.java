package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.Set;

import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
 */
public class ConfigTest extends BaseTest
{
    private final Cmd target;

    public ConfigTest(final String user, final String password,
            final String host, final int port, final String configName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CmdEnv cmdEnv = new CmdEnv();
        cmdEnv.put(DeleteConfigCmd.kConfigName, configName);

        final DeleteConfigCmd cmd = new DeleteConfigCmd(cmdEnv);

        target = new PipeCmd(connectCmd, cmd);
    }

    protected void runInternal() throws Exception
    {
        try
        {
            target.execute();
        }
        catch (Exception e)
        {
            System.out.println("Error: " + ExceptionUtil.getRootCause(e).
                getMessage());
        }
    }

    public static void main(String[] args) throws Exception
    {
        new ConfigTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
