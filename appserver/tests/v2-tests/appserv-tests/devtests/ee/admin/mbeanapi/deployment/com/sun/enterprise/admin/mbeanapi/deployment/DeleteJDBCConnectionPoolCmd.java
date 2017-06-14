package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 */
public class DeleteJDBCConnectionPoolCmd extends BaseResourceCmd
{
    public static final String kName = "Name";

    public DeleteJDBCConnectionPoolCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeJDBCConnectionPoolConfig(getName());

        return new Integer(0);
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }
}
