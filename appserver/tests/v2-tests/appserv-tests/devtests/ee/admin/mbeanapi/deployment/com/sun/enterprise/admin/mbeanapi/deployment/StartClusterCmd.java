package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.j2ee.J2EECluster;

/**
 */
public class StartClusterCmd extends BaseInstanceCmd
{
    public StartClusterCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final J2EECluster j2eeCluster = getJ2EECluster();
        assert j2eeCluster != null;

        j2eeCluster.start();

        return null;
    }
}
