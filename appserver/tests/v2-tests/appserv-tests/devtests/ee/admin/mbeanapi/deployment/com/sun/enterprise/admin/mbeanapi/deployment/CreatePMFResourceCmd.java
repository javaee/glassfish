package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig;

/**
 */
public class CreatePMFResourceCmd extends BaseResourceCmd
    implements SourceCmd
{
    public CreatePMFResourceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final PersistenceManagerFactoryResourceConfig resource =
        	getDomainConfig().createPersistenceManagerFactoryResourceConfig(
            	getJNDIName(), getOptional());
        return resource;
    }
}
