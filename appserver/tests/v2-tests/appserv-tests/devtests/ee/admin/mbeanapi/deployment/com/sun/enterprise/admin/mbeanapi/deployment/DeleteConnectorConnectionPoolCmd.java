package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteConnectorConnectionPoolCmd extends BaseResourceCmd
{
    public static final String kName = "Name";

    public DeleteConnectorConnectionPoolCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeConnectorConnectionPoolConfig(getName());

        return new Integer(0);
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }
}
