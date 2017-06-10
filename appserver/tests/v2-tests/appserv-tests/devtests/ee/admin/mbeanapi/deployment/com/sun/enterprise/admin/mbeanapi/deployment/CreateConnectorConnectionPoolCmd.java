package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;

/**
 */
public class CreateConnectorConnectionPoolCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kName      = "Name";
    public static final String kResourceAdapterName = "ResourceAdapterName";
    public static final String kConnectionDefinitionName = "ConnectionDefinitionName";

    public CreateConnectorConnectionPoolCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final ConnectorConnectionPoolConfig resource = getDomainConfig().createConnectorConnectionPoolConfig(
            getName(), getResourceAdapterName(), getConnectionDefinitionName(),
            getOptional());
        return resource;
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }

    private String getResourceAdapterName()
    {
        return (String)getCmdEnv().get(kResourceAdapterName);
    }

    private String getConnectionDefinitionName()
    {
        return (String)getCmdEnv().get(kConnectionDefinitionName);
    }
}
