package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.ClusteredServerConfig;

/**
 */
public class CreateClusteredInstanceCmd extends BaseInstanceCmd
{
    public CreateClusteredInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        return (ClusteredServerConfig) getDomainConfig().createClusteredServerConfig(getInstanceName(), 
                getClusterName(), getNodeAgentName(), getOptional());
    }
}
