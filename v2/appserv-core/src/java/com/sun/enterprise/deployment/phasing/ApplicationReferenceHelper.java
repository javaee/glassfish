/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/*
 * ConfigsConfigMBean.java
 *
 * Created on October 27, 2003, 9:45 AM
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;

import com.sun.enterprise.admin.config.BaseConfigMBean;

/**
 *
 * @author  kebbs
 */
public class ApplicationReferenceHelper implements IAdminConstants
{        
    private static final StringManager _strMgr = 
        StringManager.getManager(ApplicationReferenceHelper.class);

    private ConfigContext _configContext = null;

    private static final TargetType[] targetTypes =  
        new TargetType[] {TargetType.CLUSTER, TargetType.SERVER, 
                          TargetType.DAS};
    
    /** Creates a new instance of EEApplicationsConfigMBean */
    public ApplicationReferenceHelper(ConfigContext configContext) 
    {
        _configContext = configContext;
    } 
        
    private ConfigContext getConfigContext()
    {
        return _configContext;
    }
    
    /**
     * Add an application reference to the cluster.
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */        
    private void addApplicationReferenceToCluster(Cluster cluster, boolean enabled, 
        String virtualServers, String referenceName) throws ConfigException
    {
        ApplicationRef ref = cluster.getApplicationRefByRef(referenceName);
        if (ref != null) {
            //Application ref already exists in cluster         
            throw new ConfigException(_strMgr.getString("clusterApplicationRefAlreadyExists",
                referenceName, cluster.getName()));     
        }
        ref = new ApplicationRef();
        ref.setEnabled(enabled);
        ref.setRef(referenceName);
        ref.setVirtualServers(virtualServers);
        cluster.addApplicationRef(ref, BaseConfigMBean.OVERWRITE);        
    }
    
    /**
     * Add an application reference to the server instances in the cluster.
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */         
    private void addApplicationReferenceToClusteredServers(Cluster cluster, Server[] servers, boolean enabled, 
        String virtualServers, String referenceName) throws ConfigException
    {
        for (int i = 0; i < servers.length; i++) {
            final ApplicationRef ref = servers[i].getApplicationRefByRef(referenceName);
            if (ref != null) {
                //This indicates that the cluster is in an inconsistent state. Some of the 
                //instances in the cluster have the ref and some do not.
                throw new ConfigException(_strMgr.getString("clusterApplicationRefInconsistency",
                    referenceName, cluster.getName(), servers[i].getName()));
            }
            addApplicationReferenceToServer(servers[i], enabled, virtualServers, 
                referenceName);
        }        
    }

    /**
     * Add an application reference to a single server instance.
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */         
    private void addApplicationReferenceToServer(Server server, boolean enabled, 
        String virtualServers, String referenceName) throws ConfigException
    {        
        ApplicationRef ref = server.getApplicationRefByRef(referenceName);
        if (ref != null) {
            //Resource ref already exists in server         
            throw new ConfigException(_strMgr.getString("serverApplicationRefAlreadyExists",
                referenceName, server.getName()));         
        }
        ref = new ApplicationRef();
        ref.setEnabled(enabled);
        ref.setRef(referenceName);
        ref.setVirtualServers(virtualServers);
        server.addApplicationRef(ref, BaseConfigMBean.OVERWRITE);        
    }
    
    /**
     * Given the application name, return its type.
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */         
    protected String getApplicationType (String appName) throws ConfigException
    {
        final ConfigContext configContext = getConfigContext();
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
        final Applications applications = domain.getApplications();
        if (applications.getAppclientModuleByName(appName) != null) {
            return ServerTags.APPCLIENT_MODULE;
        } else if (applications.getConnectorModuleByName(appName) != null) {
            return ServerTags.CONNECTOR_MODULE;
        } else if (applications.getEjbModuleByName(appName) != null) {
            return ServerTags.EJB_MODULE;
        } else if (applications.getJ2eeApplicationByName(appName) != null) {
            return ServerTags.J2EE_APPLICATION;
        } else if (applications.getLifecycleModuleByName(appName) != null) {
            return ServerTags.LIFECYCLE_MODULE;
        } else if (applications.getWebModuleByName(appName) != null) {
            return ServerTags.WEB_MODULE;
        } else {             
            throw new ConfigException(_strMgr.getString("applicationDoesNotExist",
                appName));                  
        }        
    }
    
    /**
     * Create an application reference in the specified target (cluster 
     * unclustered server).
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */                 
    public void createApplicationReference(TargetType[] validTypes,
        String targetName, boolean enabled,
        String virtualServers, String referenceName) throws ConfigException 
    {                         
        final ConfigContext configContext = getConfigContext();
        //Validate that there is indeed a resource with the specified name.  
        final String type = getApplicationType(referenceName);
        final Target target = TargetBuilder.INSTANCE.createTarget(
            validTypes, targetName, configContext);            
        if (target.getType() == TargetType.CLUSTER) {
            final Cluster cluster = ClusterHelper.getClusterByName(configContext, target.getName());
            addApplicationReferenceToCluster(cluster, enabled, virtualServers, referenceName);
            final Server[] servers = ServerHelper.getServersInCluster(configContext, target.getName());
            addApplicationReferenceToClusteredServers(cluster, servers, enabled, 
                virtualServers, referenceName);            
        } else if (target.getType() == TargetType.SERVER ||
            target.getType() == TargetType.DAS) {
            final Server server = ServerHelper.getServerByName(configContext, target.getName());
            addApplicationReferenceToServer(server, enabled, virtualServers, referenceName);
        } else {
            throw new ConfigException(_strMgr.getString("invalidClusterOrServerTarget",
                target.getName()));
        }              
    }
        
    /**
     * Delete an application reference from the cluster.
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */         
    private void deleteApplicationReferenceFromCluster(Cluster cluster,
        String referenceName) throws ConfigException
    {
        final ApplicationRef ref = cluster.getApplicationRefByRef(referenceName);
        if (ref == null) {
            //Application ref already exists in cluster         
            throw new ConfigException(_strMgr.getString("clusterApplicationRefDoesNotExist",
                cluster.getName(), referenceName));     
        }
        cluster.removeApplicationRef(ref, BaseConfigMBean.OVERWRITE);        
    }
    
    /**
     * Delete the application references from all servers that are part of a cluster. 
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */         
    private void deleteApplicationReferenceFromClusteredServers(Cluster cluster, Server[] servers, 
        String referenceName) throws ConfigException
    {
        for (int i = 0; i < servers.length; i++) {
            final ApplicationRef ref = servers[i].getApplicationRefByRef(referenceName);
            if (ref == null) {
                //This indicates that the cluster is in an inconsistent state. Some of the 
                //instances in the cluster have the ref and some do not.
                throw new ConfigException(_strMgr.getString("clusterApplicationRefInconsistency",
                    referenceName, cluster.getName(), servers[i].getName()));
            }
            deleteApplicationReferenceFromServer(servers[i], referenceName);
        }        
    }

    /**
     * Delete application reference from a single server instance.
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */         
    private void deleteApplicationReferenceFromServer(Server server, 
        String referenceName) throws ConfigException
    {        
        final ApplicationRef ref = server.getApplicationRefByRef(referenceName);
        if (ref == null) {
            //Application ref already exists in server         
            throw new ConfigException(_strMgr.getString("serverApplicationRefDoesNotExist",
                 server.getName(), referenceName));         
        }         
        server.removeApplicationRef(ref, BaseConfigMBean.OVERWRITE);
    }

    /**
     * Delete application reference from the given target (cluster or unclustered
     * server instance).
     *
     * NOTE: Much of this functionality is duplicated in ResourcesMBeanHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */             
    public void deleteApplicationReference(TargetType[] validTypes,
        String targetName, String referenceName)
        throws ConfigException
    {        
        final ConfigContext configContext = getConfigContext();
        //Validate that there is indeed a resource with the specified name.  
        final String type = getApplicationType(referenceName);                             
        final Target target = TargetBuilder.INSTANCE.createTarget(
            validTypes, targetName, configContext);            
        if (target.getType() == TargetType.SERVER || 
            target.getType() == TargetType.DAS) {
            final Server server = ServerHelper.getServerByName(configContext, targetName);
            deleteApplicationReferenceFromServer(server, referenceName);
        } else if (target.getType() == TargetType.CLUSTER) {
            final Cluster cluster = ClusterHelper.getClusterByName(configContext, targetName);
            deleteApplicationReferenceFromCluster(cluster, referenceName);
            final Server[] servers = ServerHelper.getServersInCluster(configContext, targetName);
            deleteApplicationReferenceFromClusteredServers(cluster, servers, referenceName);            
        } else {
            throw new ConfigException(_strMgr.getString("invalidClusterOrServerTarget",
                targetName));
        }      
    }       
    
    /**
     *Locates the app ref for the specified application on the given target.
     *@param targetName the name of the target of interest
     *@param appName the name of the application of interest
     *@return the ApplicationRef for the app on the target; null if there is none
     */
    public static ApplicationRef findCurrentAppRef(
            DeploymentContext deploymentCtx,
            String targetName, 
            String appName) throws ConfigException {
        ApplicationRef appRef = null;
        final ConfigContext configContext = deploymentCtx.getConfigContext();

        final Target target = TargetBuilder.INSTANCE.createTarget(
            targetTypes, targetName, configContext);

        if (target.getType() == TargetType.CLUSTER ||
            target.getType() == TargetType.STANDALONE_CLUSTER)
        {
            Cluster cluster = ClusterHelper.getClusterByName(
                configContext, target.getName());
            if (cluster != null) {
                appRef = cluster.getApplicationRefByRef(appName);
            }
        }
        else
        {
            Server server =  ServerHelper.getServerByName(
                configContext, target.getName());
            if (server != null) {
                appRef = server.getApplicationRefByRef(appName);
            }
        }
                                                                                
        return appRef;
    }
}
