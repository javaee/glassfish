package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ServerConfig;

/**
 */
public class ListDeployedAppsCmd extends DeployCmd
{
    public static final String kAppType             = "AppType";

    public static final String kAll                 = "All";
    public static final String kEJBModules          = "EJBModules";
    public static final String kWebModules          = "WebModules";
    public static final String kApplications        = "Applications";
    public static final String kRARModules          = "RARModules";
    public static final String kLifecycleModules    = "LifecycleModules";

    public ListDeployedAppsCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        final String target = getTarget();

        final Cmd cmd = getListCmdByAppType(getAppType());
        final Set s1 = (Set)cmd.execute();

        if (DEFAULT_DEPLOY_TARGET.equals(target))
        {
            return s1;
        }

        final Set s2 = getDeployedItemRefs(target);

		Set[] sets = new Set[] { s1, s2};
        
		return sets;
		//return intersect(s1, s2);

    }

    private Set intersect(final Set s1, final Set s2)
    {
        final Set s = new HashSet();
        final Iterator it = s1.iterator();
        while (it.hasNext())
        {
            final String next = (String)it.next();
            if (s2.contains(next))
            {
                s.add(next);
            }
        }
        return s;
    }

    private Set getDeployedItemRefs(final String target) throws Exception
    {
		Object o = getClusterOrServer(target);
		
		if(o == null)
			throw new Exception("Can't find config for this target.");
		
		if(o instanceof ServerConfig)
			return ((ServerConfig)o).getDeployedItemRefConfigMap().keySet();

		if(o instanceof ClusterConfig)
			return ((ClusterConfig)o).getDeployedItemRefConfigMap().keySet();

		if(o instanceof ClusteredServerConfig)
			return ((ClusteredServerConfig)o).getDeployedItemRefConfigMap().keySet();
		
		throw new RuntimeException("???");
	}

    private String getAppType()
    {
        return (String)getCmdEnv().get(kAppType);
    }


    private Cmd getListCmdByAppType(final String appType) throws Exception
    {
        if (kAll.equals(appType))
        {
            return new ListAllAppsCmd();
        }
        else if (kApplications.equals(appType))
        {
            return new ListJ2EEApplicationsCmd();
        }
        else if (kEJBModules.equals(appType))
        {
            return new ListEJBModulesCmd();
        }
        else if (kWebModules.equals(appType))
        {
            return new ListWebModulesCmd();
        }
        else if (kRARModules.equals(appType))
        {
            return new ListRARModulesCmd();
        }
        else if (kLifecycleModules.equals(appType))
        {
            return new ListLifecycleModulesCmd();
        }
        else
        {
            throw new Exception("Unknown app type: " + appType);
        }
    }

    private final class ListAllAppsCmd implements Cmd
    {
        public Object execute() throws Exception
        {
            final DomainConfig domainConfig = getDomainConfig();
            final Set s = new HashSet();

            s.addAll(domainConfig.getJ2EEApplicationConfigMap().keySet());
            s.addAll(domainConfig.getEJBModuleConfigMap().keySet());
            s.addAll(domainConfig.getWebModuleConfigMap().keySet());
            s.addAll(domainConfig.getRARModuleConfigMap().keySet());
            s.addAll(domainConfig.getAppClientModuleConfigMap().keySet());
            s.addAll(domainConfig.getLifecycleModuleConfigMap().keySet());

            return s;
        }
    }

    private final class ListWebModulesCmd implements Cmd
    {
        public Object execute() throws Exception
        {
            final DomainConfig domainConfig = getDomainConfig();
            return domainConfig.getWebModuleConfigMap().keySet();
        }
    }

    private final class ListEJBModulesCmd implements Cmd
    {
        public Object execute() throws Exception
        {
            final DomainConfig domainConfig = getDomainConfig();
            return domainConfig.getEJBModuleConfigMap().keySet();
        }
    }

    private final class ListRARModulesCmd implements Cmd
    {
        public Object execute() throws Exception
        {
            final DomainConfig domainConfig = getDomainConfig();
            return domainConfig.getRARModuleConfigMap().keySet();
        }
    }

    private final class ListJ2EEApplicationsCmd implements Cmd
    {
        public Object execute() throws Exception
        {
            final DomainConfig domainConfig = getDomainConfig();
            return domainConfig.getJ2EEApplicationConfigMap().keySet();
        }
    }

    private final class ListLifecycleModulesCmd implements Cmd
    {
        public Object execute() throws Exception
        {
            final DomainConfig domainConfig = getDomainConfig();
            return domainConfig.getLifecycleModuleConfigMap().keySet();
        }
    }
}
