package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.StandaloneServerConfig;

/**
 */
public class CreateInstanceCmd extends BaseInstanceCmd
{
    public CreateInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        return (StandaloneServerConfig) getDomainConfig().createStandaloneServerConfig(getInstanceName(), 
                getNodeAgentName(), getConfigName(), getOptional());
    }
}
