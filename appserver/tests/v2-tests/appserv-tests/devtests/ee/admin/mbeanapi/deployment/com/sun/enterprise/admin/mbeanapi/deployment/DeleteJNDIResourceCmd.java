package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteJNDIResourceCmd extends BaseResourceCmd
{
    public DeleteJNDIResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeJNDIResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
