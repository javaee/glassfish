package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class DeleteClusteredInstanceCmd extends BaseInstanceCmd
{
    public DeleteClusteredInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeClusteredServerConfig(getInstanceName());

        return null;
    }
}
