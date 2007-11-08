package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteCustomResourceCmd extends BaseResourceCmd
{
    public DeleteCustomResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeCustomResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
