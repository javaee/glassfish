package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.AdminObjectResourceConfig;

/**
 */
public class CreateAdminObjectResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kResType     = "ResType";
    public static final String kResAdapter  = "ResAdapter";

    public CreateAdminObjectResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final AdminObjectResourceConfig resource = getDomainConfig().createAdminObjectResourceConfig(
            getJNDIName(), getResType(), getResAdapter(), getOptional());
        return resource;
    }

    private String getResType()
    {
        return (String)getCmdEnv().get(kResType);
    }

    private String getResAdapter()
    {
        return (String)getCmdEnv().get(kResAdapter);
    }
}
