/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
