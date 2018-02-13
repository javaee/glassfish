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

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.deploy.DeploymentMgr;
import java.io.*;

/**
 */
public class CmdFactory
{
    public CmdFactory()
    {
    }

    public ConnectCmd createConnectCmd(Phup phup)
    {
		return createConnectCmd(phup.user, phup.password, phup.host, phup.port);
	}
    public ConnectCmd createConnectCmd(String user, String password, 
            String host, int port)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(ConnectCmd.kHost, host);
        cmdEnv.put(ConnectCmd.kPort, new Integer(port));
        cmdEnv.put(ConnectCmd.kUser, user);
        cmdEnv.put(ConnectCmd.kPassword, password);

        return new ConnectCmd(cmdEnv);
    }

    public CreateInstanceCmd createCreateInstanceCmd(
            String instanceName, String nodeAgentName, 
            String configName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateInstanceCmd.kInstanceName, instanceName);
        cmdEnv.put(CreateInstanceCmd.kNodeAgentName, nodeAgentName);
        cmdEnv.put(CreateInstanceCmd.kConfigName, configName);
        cmdEnv.put(CreateInstanceCmd.kOptional, optional);

        return new CreateInstanceCmd(cmdEnv);
    }

    public DeleteInstanceCmd createDeleteInstanceCmd(
            String instanceName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteInstanceCmd.kInstanceName, instanceName);

        return new DeleteInstanceCmd(cmdEnv);
    }

    public StartInstanceCmd createStartInstanceCmd(
            String instanceName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(StartInstanceCmd.kInstanceName, instanceName);

        return new StartInstanceCmd(cmdEnv);
    }

    public StopInstanceCmd createStopInstanceCmd(
            String instanceName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(StopInstanceCmd.kInstanceName, instanceName);

        return new StopInstanceCmd(cmdEnv);
    }

    public DeployCmd createDeployCmd(String archive, String name, 
            String contextRoot, boolean enable, String appservTarget)
    {
        return createDeployCmd(archive, name, null, contextRoot, enable,
                true, true, false, true, false, appservTarget);
    }

    public DeployCmd createDeployCmd(String archive, String name, 
            String description, String contextRoot, boolean enable,
            boolean forceDeploy, boolean generateRMIStubs, 
            boolean availabilityEnabled, boolean cascade, boolean verify, 
            String target)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeployCmd.kArchive, archive);
        cmdEnv.put(DeployCmd.kTarget, target);

        final Map deployOptions = new HashMap(10);

        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_NAME_KEY, name);
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_DESCRIPTION_KEY, 
                description);
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_CONTEXT_ROOT_KEY, 
                contextRoot);
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_ENABLE_KEY, 
                new Boolean(enable).toString());
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_GENERATE_RMI_STUBS_KEY, 
                new Boolean(generateRMIStubs).toString());
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_FORCE_KEY, 
                new Boolean(forceDeploy).toString());
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_AVAILABILITY_ENABLED_KEY, 
                new Boolean(availabilityEnabled).toString());
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_CASCADE_KEY, 
                new Boolean(cascade).toString());
        deployOptions.put(DeploymentMgr.DEPLOY_OPTION_VERIFY_KEY, 
                new Boolean(verify).toString());
		
		try
		{
			// FIXME TBD Add to MBAPI...
			if(new File(archive).isDirectory())
				//deployOptions.put(DeploymentMgr.DEPLOY_OPTION_DIRECTORY_DEPLOYED_KEY, Boolean.TRUE.toString());
				deployOptions.put("directorydeployed", Boolean.TRUE.toString());
		}
		catch(Exception e)
		{
			//
		}
        cmdEnv.put(DeployCmd.kDeployOptions, deployOptions);

        return new DeployCmd(cmdEnv);
    }

    public CreateResourceAdapterConfigCmd createCreateResourceAdapterConfigCmd(
            String racName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateResourceAdapterConfigCmd.kRACName, racName);
        cmdEnv.put(CreateResourceAdapterConfigCmd.kOptional, optional);

        return new CreateResourceAdapterConfigCmd(cmdEnv);
    }

    public DeleteResourceAdapterConfigCmd createDeleteResourceAdapterConfigCmd(
            String racName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteResourceAdapterConfigCmd.kRACName, racName);

        return new DeleteResourceAdapterConfigCmd(cmdEnv);
    }

    public CreateJNDIResourceCmd createCreateJNDIResourceCmd(
            String jndiName, String jndiLookupName, String resType,
            String factoryClass, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateJNDIResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreateJNDIResourceCmd.kJNDILookupName, jndiLookupName);
        cmdEnv.put(CreateJNDIResourceCmd.kResType, resType);
        cmdEnv.put(CreateJNDIResourceCmd.kFactoryClass, factoryClass);
        cmdEnv.put(CreateJNDIResourceCmd.kOptional, optional);

        return new CreateJNDIResourceCmd(cmdEnv);
    }

    public DeleteJNDIResourceCmd createDeleteJNDIResourceCmd(String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteJNDIResourceCmd.kJNDIName, jndiName);

        return new DeleteJNDIResourceCmd(cmdEnv);
    }

    public CreateMailResourceCmd createCreateMailResourceCmd(
            String jndiName, String host, String user,
            String from, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateMailResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreateMailResourceCmd.kHost, host);
        cmdEnv.put(CreateMailResourceCmd.kUser, user);
        cmdEnv.put(CreateMailResourceCmd.kFrom, from);
        cmdEnv.put(CreateMailResourceCmd.kOptional, optional);

        return new CreateMailResourceCmd(cmdEnv);
    }

    public DeleteMailResourceCmd createDeleteMailResourceCmd(String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteMailResourceCmd.kJNDIName, jndiName);

        return new DeleteMailResourceCmd(cmdEnv);
    }

    public CreatePMFResourceCmd createCreatePMFResourceCmd(
            String jndiName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreatePMFResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreatePMFResourceCmd.kOptional, optional);

        return new CreatePMFResourceCmd(cmdEnv);
    }

    public DeletePMFResourceCmd createDeletePMFResourceCmd(String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeletePMFResourceCmd.kJNDIName, jndiName);

        return new DeletePMFResourceCmd(cmdEnv);
    }

    public CreateAdminObjectResourceCmd createCreateAdminObjectResourceCmd(
            String jndiName, String resType, String resAdapter, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateAdminObjectResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreateAdminObjectResourceCmd.kResType, resType);
        cmdEnv.put(CreateAdminObjectResourceCmd.kResAdapter, resAdapter);
        cmdEnv.put(CreateAdminObjectResourceCmd.kOptional, optional);

        return new CreateAdminObjectResourceCmd(cmdEnv);
    }

    public DeleteAdminObjectResourceCmd createDeleteAdminObjectResourceCmd(
        String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteAdminObjectResourceCmd.kJNDIName, jndiName);

        return new DeleteAdminObjectResourceCmd(cmdEnv);
    }

    public CreateCustomResourceCmd createCreateCustomResourceCmd(
            String jndiName, String resType, String factoryClass, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateCustomResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreateCustomResourceCmd.kResType, resType);
        cmdEnv.put(CreateCustomResourceCmd.kFactoryClass, factoryClass);
        cmdEnv.put(CreateCustomResourceCmd.kOptional, optional);

        return new CreateCustomResourceCmd(cmdEnv);
    }

    public DeleteCustomResourceCmd createDeleteCustomResourceCmd(
        String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteCustomResourceCmd.kJNDIName, jndiName);

        return new DeleteCustomResourceCmd(cmdEnv);
    }

    public CreateJDBCResourceCmd createCreateJDBCResourceCmd(
            String jndiName, String poolName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateJDBCResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreateJDBCResourceCmd.kPoolName, poolName);
        cmdEnv.put(CreateJDBCResourceCmd.kOptional, optional);

        return new CreateJDBCResourceCmd(cmdEnv);
    }

    public DeleteJDBCResourceCmd createDeleteJDBCResourceCmd(
        String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteJDBCResourceCmd.kJNDIName, jndiName);

        return new DeleteJDBCResourceCmd(cmdEnv);
    }

    public CreateConnectorResourceCmd createCreateConnectorResourceCmd(
            String jndiName, String poolName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateConnectorResourceCmd.kJNDIName, jndiName);
        cmdEnv.put(CreateConnectorResourceCmd.kPoolName, poolName);
        cmdEnv.put(CreateConnectorResourceCmd.kOptional, optional);

        return new CreateConnectorResourceCmd(cmdEnv);
    }

    public DeleteConnectorResourceCmd createDeleteConnectorResourceCmd(
        String jndiName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteConnectorResourceCmd.kJNDIName, jndiName);

        return new DeleteConnectorResourceCmd(cmdEnv);
    }

    public CreateConnectorConnectionPoolCmd 
    createCreateConnectorConnectionPoolCmd(
            String name, String resourceAdapterName, 
            String connectionDefinitionName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateConnectorConnectionPoolCmd.kName, name);
        cmdEnv.put(CreateConnectorConnectionPoolCmd.kResourceAdapterName, 
                resourceAdapterName);
        cmdEnv.put(CreateConnectorConnectionPoolCmd.kConnectionDefinitionName, 
                connectionDefinitionName);
        cmdEnv.put(CreateConnectorConnectionPoolCmd.kOptional, optional);

        return new CreateConnectorConnectionPoolCmd(cmdEnv);
    }

    public DeleteConnectorConnectionPoolCmd 
    createDeleteConnectorConnectionPoolCmd(String name)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteConnectorConnectionPoolCmd.kName, name);

        return new DeleteConnectorConnectionPoolCmd(cmdEnv);
    }

    public CreateJDBCConnectionPoolCmd 
    createCreateJDBCConnectionPoolCmd(
            String name, String datasourceClassname, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateJDBCConnectionPoolCmd.kName, name);
        cmdEnv.put(CreateJDBCConnectionPoolCmd.kDatasourceClassname, 
                datasourceClassname);
        cmdEnv.put(CreateJDBCConnectionPoolCmd.kOptional, optional);

        return new CreateJDBCConnectionPoolCmd(cmdEnv);
    }

    public DeleteJDBCConnectionPoolCmd 
    createDeleteJDBCConnectionPoolCmd(String name)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteJDBCConnectionPoolCmd.kName, name);

        return new DeleteJDBCConnectionPoolCmd(cmdEnv);
    }

	public UndeployCmd createUndeployCmd(String name, String target)
    {
        final CmdEnv cmdEnv = new CmdEnv();
        final Map undeployOptions = new HashMap(1);

		cmdEnv.put(DeployCmd.kTarget, target);

        undeployOptions.put(DeploymentMgr.DEPLOY_OPTION_NAME_KEY, name);
        cmdEnv.put(DeployCmd.kDeployOptions, undeployOptions);

        return new UndeployCmd(cmdEnv);
    }

    public ListDeployedAppsCmd createListDeployedAppsCmd(String target,
            String appType)
    {
        final CmdEnv cmdEnv = new CmdEnv();
        cmdEnv.put(ListDeployedAppsCmd.kTarget, target);
        cmdEnv.put(ListDeployedAppsCmd.kAppType, appType);

        return new ListDeployedAppsCmd(cmdEnv);
    }

    public CreateClusterCmd createCreateClusterCmd(
            String clusterName, String configName, Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateClusterCmd.kClusterName, clusterName);
        cmdEnv.put(CreateClusterCmd.kConfigName, configName);
        cmdEnv.put(CreateClusterCmd.kOptional, optional);

        return new CreateClusterCmd(cmdEnv);
    }

    public DeleteClusterCmd createDeleteClusterCmd(String clusterName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteClusterCmd.kClusterName, clusterName);

        return new DeleteClusterCmd(cmdEnv);
    }

    public StartClusterCmd createStartClusterCmd(String clusterName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(StartClusterCmd.kClusterName, clusterName);

        return new StartClusterCmd(cmdEnv);
    }

    public StopClusterCmd createStopClusterCmd(String clusterName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(StopClusterCmd.kClusterName, clusterName);

        return new StopClusterCmd(cmdEnv);
    }

    public CreateClusteredInstanceCmd createCreateClusteredInstanceCmd(
            String instanceName, String clusterName, String nodeAgentName, 
            Map optional)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateClusteredInstanceCmd.kInstanceName, instanceName);
        cmdEnv.put(CreateClusteredInstanceCmd.kNodeAgentName, nodeAgentName);
        cmdEnv.put(CreateClusteredInstanceCmd.kClusterName, clusterName);
        cmdEnv.put(CreateClusteredInstanceCmd.kOptional, optional);

        return new CreateClusteredInstanceCmd(cmdEnv);
    }

    public DeleteClusteredInstanceCmd createDeleteClusteredInstanceCmd(
            String instanceName)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(DeleteClusteredInstanceCmd.kInstanceName, instanceName);

        return new DeleteClusteredInstanceCmd(cmdEnv);
    }

    public VirtualServerCmd createVirtualServerCmd(String name, 
            String configName, String hosts, Map optional, String mode)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(VirtualServerCmd.kName, name);
        cmdEnv.put(VirtualServerCmd.kConfigName, configName);
        cmdEnv.put(VirtualServerCmd.kHosts, hosts);
        cmdEnv.put(VirtualServerCmd.kOptional, optional);

        return new VirtualServerCmd(cmdEnv, mode);
    }

    public CreateAppRefCmd createCreateAppRefCmd(String ref,
            boolean enabled, String virtualServers, boolean lbEnabled,
            int disableTimeoutInMinutes, String target)
    {
        final CmdEnv cmdEnv = new CmdEnv();

        cmdEnv.put(CreateAppRefCmd.kName, ref);
        cmdEnv.put(CreateAppRefCmd.kVirtualServers, virtualServers);
        cmdEnv.put(CreateAppRefCmd.kEnabled, new Boolean(enabled));
        cmdEnv.put(CreateAppRefCmd.kLBEnabled, new Boolean(lbEnabled));
        cmdEnv.put(CreateAppRefCmd.kDisableTimeoutInMinutes, 
            new Integer(disableTimeoutInMinutes));
        cmdEnv.put(CreateAppRefCmd.kTarget, target);

        return new CreateAppRefCmd(cmdEnv);
    }
}
