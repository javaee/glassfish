package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.ResourceAdapterConfig;

/**
 */
public class CreateResourceAdapterConfigCmd extends BaseResourceCmd
    implements SourceCmd
{
    public CreateResourceAdapterConfigCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final ResourceAdapterConfig rac = getDomainConfig().createResourceAdapterConfig(
            getRACName(), getOptional());
        return rac;
    }

    private String getRACName()
    {
        return (String)getCmdEnv().get(kRACName);
    }
}
