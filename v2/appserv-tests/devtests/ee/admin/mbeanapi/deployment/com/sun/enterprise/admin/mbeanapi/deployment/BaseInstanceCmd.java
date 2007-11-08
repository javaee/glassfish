package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EECluster;

/**
 */
public abstract class BaseInstanceCmd extends BaseCmd
{
    public static final String kInstanceName    = "InstanceName";
    public static final String kClusterName     = "ClusterName";
    public static final String kConfigName      = "ConfigName";
    public static final String kNodeAgentName   = "NodeAgentName";
    public static final String kOptional        = "Optional";

    protected BaseInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    protected J2EEServer getJ2EEServer() throws Exception
    {
        final Map servers = getJ2EEDomain().getServerMap();
        return (J2EEServer)servers.get(getInstanceName());
    }

    protected J2EECluster getJ2EECluster() throws Exception
    {
        final Map clusters = getJ2EEDomain().getClusterMap();
        return (J2EECluster)clusters.get(getClusterName());
    }

    public String getInstanceName()
    {
        return (String)getCmdEnv().get(kInstanceName);
    }

    public String getClusterName()
    {
        return (String)getCmdEnv().get(kClusterName);
    }

    public String getNodeAgentName()
    {
        return (String)getCmdEnv().get(kNodeAgentName);
    }

    public String getConfigName()
    {
        return (String)getCmdEnv().get(kConfigName);
    }

    public Map getOptional()
    {
        return (Map)getCmdEnv().get(kOptional);
    }
}
