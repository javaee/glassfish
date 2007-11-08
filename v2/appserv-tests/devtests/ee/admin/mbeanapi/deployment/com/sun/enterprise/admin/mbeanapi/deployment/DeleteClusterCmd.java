package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteClusterCmd extends BaseInstanceCmd
{
    public DeleteClusterCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeClusterConfig(getClusterName());

        return null;
    }
}
