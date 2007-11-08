package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.j2ee.J2EECluster;

/**
 */
public class StopClusterCmd extends BaseInstanceCmd
{
    public StopClusterCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final J2EECluster j2eeCluster = getJ2EECluster();
        assert j2eeCluster != null;

        j2eeCluster.stop();

        return null;
    }
}
