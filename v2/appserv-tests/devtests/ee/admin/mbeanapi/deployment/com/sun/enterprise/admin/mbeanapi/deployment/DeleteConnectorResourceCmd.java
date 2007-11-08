package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteConnectorResourceCmd extends BaseResourceCmd
{
    public DeleteConnectorResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeConnectorResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
