package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public abstract class BaseResourceCmd extends BaseCmd
{
    public static final String kJNDIName    = "JNDIName";
    public static final String kOptional    = "Optional";
    public static final String kTarget      = "Target";
    public static final String kRACName     = "resourceAdapterName";


    public BaseResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    protected String getJNDIName()
    {
        return (String)getCmdEnv().get(kJNDIName);
    }

    protected String getTarget()
    {
        return (String)getCmdEnv().get(kTarget);
    }

    protected Map getOptional()
    {
        return (Map)getCmdEnv().get(kOptional);
    }
}
