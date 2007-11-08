package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.ConnectorResourceConfig;

/**
 */
public class CreateConnectorResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kPoolName         = "PoolName";

    public CreateConnectorResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final ConnectorResourceConfig resource = getDomainConfig().createConnectorResourceConfig(
            getJNDIName(), getPoolName(), getOptional());
        return resource;
    }

    private String getPoolName()
    {
        return (String)getCmdEnv().get(kPoolName);
    }
}
