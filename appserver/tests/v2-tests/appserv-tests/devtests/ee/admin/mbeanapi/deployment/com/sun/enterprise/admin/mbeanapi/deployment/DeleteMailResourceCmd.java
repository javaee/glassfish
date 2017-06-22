package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteMailResourceCmd extends BaseResourceCmd
{
    public DeleteMailResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeMailResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
