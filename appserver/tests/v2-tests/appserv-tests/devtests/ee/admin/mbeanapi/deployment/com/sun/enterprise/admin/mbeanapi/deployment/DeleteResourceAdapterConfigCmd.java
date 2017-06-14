package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;


/**
 */
public class DeleteResourceAdapterConfigCmd extends BaseResourceCmd
{
    public DeleteResourceAdapterConfigCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeResourceAdapterConfig(getRACName());
        return new Integer(0);
    }

    private String getRACName()
    {
        return (String)getCmdEnv().get(kRACName);
    }
}
