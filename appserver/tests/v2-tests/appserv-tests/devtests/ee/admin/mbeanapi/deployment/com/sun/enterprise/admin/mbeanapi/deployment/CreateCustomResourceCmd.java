package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.CustomResourceConfig;

/**
 */
public class CreateCustomResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kResType         = "ResType";
    public static final String kFactoryClass    = "FactoryClass";

    public CreateCustomResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final CustomResourceConfig resource = getDomainConfig().createCustomResourceConfig(
            getJNDIName(), getResType(), getFactoryClass(), getOptional());
        return resource;
    }

    private String getResType()
    {
        return (String)getCmdEnv().get(kResType);
    }

    private String getFactoryClass()
    {
        return (String)getCmdEnv().get(kFactoryClass);
    }
}
