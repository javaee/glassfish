package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteJDBCResourceCmd extends BaseResourceCmd
{
    public DeleteJDBCResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeJDBCResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
