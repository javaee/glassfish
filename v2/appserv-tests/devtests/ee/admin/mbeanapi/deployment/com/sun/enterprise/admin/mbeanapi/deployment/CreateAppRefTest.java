package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.DeployedItemRefConfig;


/**
 */
public class CreateAppRefTest extends BaseTest
{
    private final Cmd target;

    public CreateAppRefTest(final String user, 
        final String password, final String host, final int port, 
        final String refName, final boolean enabled, 
        final String virtualServers,final boolean lbEnabled,
        final int disableTimeoutInMinutes, final String appservTarget)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateAppRefCmd createCmd = 
                cmdFactory.createCreateAppRefCmd(refName, enabled, 
                    virtualServers, lbEnabled, disableTimeoutInMinutes, 
                    appservTarget);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());

        target = p2;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        final String appRef = args[0];
        final String target = args[1];
        final String virtualServers = args.length == 3 ? args[2] : null;

        new CreateAppRefTest(
                "admin", "password", "localhost", 8686, 
                appRef, false, virtualServers, false, 160, target).run();
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private DeployedItemRefConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (DeployedItemRefConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("Ref="+res.getName());
            System.out.println("Enabled="+res.getEnabled());
            System.out.println("VirtualServers="+res.getVirtualServers());
            //System.out.println("LBEnabled="+res.getLBEnabled());
            //System.out.println("DisableTimeoutInMinutes="+res.getDisableTimeoutInMinutes());
            return new Integer(0);
        }

    }
}
