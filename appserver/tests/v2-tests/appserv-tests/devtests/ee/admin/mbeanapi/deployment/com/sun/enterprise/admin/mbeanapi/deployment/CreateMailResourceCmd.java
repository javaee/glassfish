package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.MailResourceConfig;

/**
 */
public class CreateMailResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public static final String kHost    = "Host";
    public static final String kUser    = "User";
    public static final String kFrom    = "From";

    public CreateMailResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final MailResourceConfig resource = getDomainConfig().createMailResourceConfig(
            getJNDIName(), getHost(), getUser(), getFrom(), getOptional());
        return resource;
    }

    private String getHost()
    {
        return (String)getCmdEnv().get(kHost);
    }

    private String getUser()
    {
        return (String)getCmdEnv().get(kUser);
    }

    private String getFrom()
    {
        return (String)getCmdEnv().get(kFrom);
    }
}
