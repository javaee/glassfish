package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import java.util.*;
import javax.management.*;
import com.sun.appserv.management.deploy.*;
import com.sun.appserv.management.config.*;

/**
 */
public class UndeployCmd extends DeployCmd
{
    public UndeployCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        undeploy();
        return null;
    }


    protected void undeploy() throws Exception
    {
        final String		appName		= getAppName();
        final DeploymentMgr	deployMgr	= getDeploymentMgr();

        if (!DEFAULT_DEPLOY_TARGET.equals(getTarget()))
        {
            final DeployedItemRefConfigCR	refMgr = 
                getDeployedItemRefConfigCR();

            stopApp();
            refMgr.removeDeployedItemRefConfig(appName);
        }
        final Map statusData = deployMgr.undeploy(appName, null);
        final DeploymentStatus status	= 
            DeploymentSupport.mapToDeploymentStatus( statusData );
        checkFailed(checkForException(status));
    }
}
