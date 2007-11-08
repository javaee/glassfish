package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.config.JDBCConnectionPoolConfig;

/**
 */
public class CreateJDBCConnectionPoolCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kName      = "Name";
    public static final String kDatasourceClassname = "DatasourceClassname";

    public CreateJDBCConnectionPoolCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final JDBCConnectionPoolConfig resource =
        	getDomainConfig().createJDBCConnectionPoolConfig(
            	getName(), getDatasourceClassname(), getOptional());
        return resource;
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }

    private String getDatasourceClassname()
    {
        return (String)getCmdEnv().get(kDatasourceClassname);
    }

}
