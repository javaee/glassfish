package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public class ListClusteredInstancesCmd extends BaseInstanceCmd
{
    public ListClusteredInstancesCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        return getDomainConfig().getClusteredServerConfigMap();
    }
}
