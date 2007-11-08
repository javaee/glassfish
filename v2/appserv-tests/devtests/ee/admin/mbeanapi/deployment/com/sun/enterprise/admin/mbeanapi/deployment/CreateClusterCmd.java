package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class CreateClusterCmd extends BaseInstanceCmd
{
    public CreateClusterCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        return getDomainConfig().createClusterConfig(getClusterName(), getConfigName(), getOptional());
    }
}
