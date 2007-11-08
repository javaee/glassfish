package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.j2ee.J2EEServer;

/**
 */
public class StartInstanceCmd extends BaseInstanceCmd
{
    public StartInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final J2EEServer j2eeServer = getJ2EEServer();
        assert j2eeServer != null;

        j2eeServer.start();

        return null;
    }
}
