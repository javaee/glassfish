package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.JDBCResourceConfig;

/**
 */
public class CreateJDBCResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kPoolName         = "PoolName";

    public CreateJDBCResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final JDBCResourceConfig resource = getDomainConfig().createJDBCResourceConfig(
            getJNDIName(), getPoolName(), getOptional());
        return resource;
    }

    private String getPoolName()
    {
        return (String)getCmdEnv().get(kPoolName);
    }
}
