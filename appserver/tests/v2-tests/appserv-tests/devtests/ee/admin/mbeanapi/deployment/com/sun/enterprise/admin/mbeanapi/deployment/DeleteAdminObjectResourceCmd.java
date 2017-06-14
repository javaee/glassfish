package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.AdminObjectResourceConfig;

/**
 */
public class DeleteAdminObjectResourceCmd extends BaseResourceCmd
{
    public DeleteAdminObjectResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        getDomainConfig().removeAdminObjectResourceConfig(getJNDIName());

        return new Integer(0);
    }
}
