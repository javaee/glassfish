package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.config.JNDIResourceConfig;

/**
 */
public class CreateJNDIResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kJNDILookupName      = "JNDILookupName";
    public static final String kResType             = "ResType";
    public static final String kFactoryClass        = "FactoryClass";

    public CreateJNDIResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final JNDIResourceConfig resource = getDomainConfig().createJNDIResourceConfig(
            getJNDIName(), getJNDILookupName(), getResType(),
            getFactoryClass(), getOptional());
        return resource;
    }

    private String getJNDILookupName()
    {
        return (String)getCmdEnv().get(kJNDILookupName);
    }

    private String getFactoryClass()
    {
        return (String)getCmdEnv().get(kFactoryClass);
    }

    private String getResType()
    {
        return (String)getCmdEnv().get(kResType);
    }
}
