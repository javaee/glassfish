package com.sun.enterprise.admin.mbeanapi.deployment;


/**
 */
public class DeleteConfigCmd extends BaseCmd
{
    public static final String kConfigName = "ConfigName";

    public DeleteConfigCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeConfigConfig(getConfigName());

        return new Integer(0);
    }

    private String getConfigName()
    {
        return (String)getCmdEnv().get(kConfigName);
    }
}
