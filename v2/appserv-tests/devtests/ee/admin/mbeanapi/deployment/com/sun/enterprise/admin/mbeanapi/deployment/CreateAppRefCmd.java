package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.DeployedItemRefConfigCR;

/**
 */
public class CreateAppRefCmd extends DeployCmd implements SourceCmd
{
    public static String kName      = "Name";
    public static String kEnabled   = "Enabled";
    public static String kLBEnabled = "LBEnabled";
    public static String kVirtualServers = "VirtualServers";
    public static String kDisableTimeoutInMinutes = "DisableTimeoutInMinutes";

    public CreateAppRefCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        return getDeployedItemRefConfigCR().
            createDeployedItemRefConfig(getEnabled(), getName(), 
                getVirtualServers(), getLBEnabled(), 
                getDisableTimeoutInMinutes());
    }

    private boolean getEnabled()
    {
        return ((Boolean)getCmdEnv().get(kEnabled)).booleanValue();
    }

    private boolean getLBEnabled()
    {
        return ((Boolean)getCmdEnv().get(kLBEnabled)).booleanValue();
    }

    private String getVirtualServers()
    {
        return (String)getCmdEnv().get(kVirtualServers);
    }

    private int getDisableTimeoutInMinutes()
    {
        return ((Integer)getCmdEnv().get(kDisableTimeoutInMinutes)).
            intValue();
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }
}
