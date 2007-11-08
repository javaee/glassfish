package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeletePMFResourceCmd extends BaseResourceCmd
{
    public DeletePMFResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removePersistenceManagerFactoryResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
