package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteInstanceCmd extends BaseInstanceCmd
{
    public DeleteInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeStandaloneServerConfig(getInstanceName());

        return null;
    }
}
