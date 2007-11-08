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

package com.sun.enterprise.admin.configbeans;

import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;

/**
 *
 * @author  kebbs
 */
public class ResourceReferenceHelper extends BaseConfigBean
{            
    private static final TargetType[] VALID_CREATE_DELETE_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.UNCLUSTERED_SERVER, TargetType.DAS};         

    private static final StringManager _strMgr = 
        StringManager.getManager(ResourceReferenceHelper.class);     

    public ResourceReferenceHelper(ConfigContext configContext) 
    {
        super(configContext);
    }

    private String validateResourceAndGetType(String referenceName, 
        boolean allowSystemRefs) throws ConfigException        
    {
        //Validate that there is indeed a resource with the specified name.  
        final String type = getResourceType(referenceName);     
        //Check to see whether we are creating or deleting a system resource
        //reference.
        if (!allowSystemRefs) {
            if (ResourceHelper.isSystemResource(getConfigContext(), referenceName)) {
                throw new ConfigException(_strMgr.getString("systemResourceRefNotAllowed",
                    referenceName));
            }
        }
        return type;
    }
    
    /**
     * Add the resource reference to the cluster (not its server instances).
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */
    private void addResourceReferenceToCluster(Cluster cluster, boolean enabled, 
        String referenceName) throws ConfigException
    {        
        ResourceRef ref = cluster.getResourceRefByRef(referenceName);
        if (ref != null) {
            //Resource ref already exists in cluster         
            throw new ConfigException(_strMgr.getString("clusterResourceRefAlreadyExists",
                referenceName, cluster.getName()));     
        }
        ref = new ResourceRef();
        ref.setEnabled(enabled);
        ref.setRef(referenceName);
        cluster.addResourceRef(ref, OVERWRITE);        
    }
    
    /**
     * Add the resource reference to the instances belonging to a cluster
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */
    private void addResourceReferenceToClusteredServers(Cluster cluster, Server[] servers, boolean enabled, 
        String referenceName) throws ConfigException
    {
        for (int i = 0; i < servers.length; i++) {
            final ResourceRef ref = servers[i].getResourceRefByRef(referenceName);
            if (ref != null) {
                //This indicates that the cluster is in an inconsistent state. Some of the 
                //instances in the cluster have the ref and some do not.
                throw new ConfigException(_strMgr.getString("clusterResourceRefInconsistency",
                    referenceName, cluster.getName(), servers[i].getName()));
            }
            addResourceReferenceToServer(servers[i], enabled, referenceName);
        }        
    }

    /**
     * Add the resource reference to a single server instance.
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */
    private void addResourceReferenceToServer(Server server, boolean enabled, 
        String referenceName) throws ConfigException
    {        
        ResourceRef ref = server.getResourceRefByRef(referenceName);
        if (ref != null) {
            //Resource ref already exists in server         
            throw new ConfigException(_strMgr.getString("serverResourceRefAlreadyExists",
                referenceName, server.getName()));         
        }
        ref = new ResourceRef();
        ref.setEnabled(enabled);
        ref.setRef(referenceName);
        server.addResourceRef(ref, OVERWRITE);        
    }
        
    /**
     * Return the resource type given the resource name. The assumption is that 
     * all resource names are unique.
     */
    public String getResourceType (String resourceName) throws ConfigException
    {
        final ConfigContext configContext = getConfigContext();
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
        final Resources resources = domain.getResources();
        if (resources.getAdminObjectResourceByJndiName(resourceName) != null) {
            return ServerTags.ADMIN_OBJECT_RESOURCE;
        } else if (resources.getConnectorConnectionPoolByName(resourceName) != null) {
            return ServerTags.CONNECTOR_CONNECTION_POOL;
        } else if (resources.getConnectorResourceByJndiName(resourceName) != null) {
            return ServerTags.CONNECTOR_RESOURCE;
        } else if (resources.getCustomResourceByJndiName(resourceName) != null) {
            return ServerTags.CUSTOM_RESOURCE;
        } else if (resources.getExternalJndiResourceByJndiName(resourceName) != null) {
            return ServerTags.EXTERNAL_JNDI_RESOURCE;
        } else if (resources.getJdbcConnectionPoolByName(resourceName) != null) {
            return ServerTags.JDBC_CONNECTION_POOL;
        } else if (resources.getJdbcResourceByJndiName(resourceName) != null) {
            return ServerTags.JDBC_RESOURCE_JNDI_NAME;
        } else if (resources.getMailResourceByJndiName(resourceName) != null) {
            return ServerTags.MAIL_RESOURCE;
        } else if (resources.getPersistenceManagerFactoryResourceByJndiName(resourceName) != null) {
            return ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
        } else if (resources.getResourceAdapterConfigByResourceAdapterName(resourceName) != null) {
            return ServerTags.RESOURCE_ADAPTER_CONFIG;
        } else {            
            throw new ConfigException(_strMgr.getString("resourceDoesNotExist",
                resourceName));             
        }        
    }

    /**
     * Add the resource reference to the specified target (cluster or
     * unclustered server).
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */
    public void createResourceReference(String targetName, boolean enabled,
        String referenceName, boolean allowSystemRefs) throws ConfigException 
    {
        createResourceReference(VALID_CREATE_DELETE_TYPES, targetName, enabled,
            referenceName, allowSystemRefs);
    }
    
    public void createResourceReference(String targetName, boolean enabled,
        String referenceName) throws ConfigException 
    {
        createResourceReference(targetName, enabled, referenceName, false);
    }
    
    public void createResourceReference(TargetType[] validTypes,
        String targetName, boolean enabled,
        String referenceName) throws ConfigException
    {
        createResourceReference(validTypes, targetName, enabled, referenceName, false);
    }
    
    public void createResourceReference(TargetType[] validTypes,
        String targetName, boolean enabled,
        String referenceName,
        boolean allowSystemRefs) throws ConfigException
    {
        //Validate that there is indeed a resource with the specified name and
        //check to see whether we are creating or deleting a system resource
        //reference.
        final String type = validateResourceAndGetType(referenceName,
            allowSystemRefs);             
        final ConfigContext configContext = getConfigContext();
        final Target target = TargetBuilder.INSTANCE.createTarget(
            validTypes, targetName, configContext);            
        if (target.getType() == TargetType.CLUSTER) {
            final Cluster cluster = ClusterHelper.getClusterByName(configContext, target.getName());
            addResourceReferenceToCluster(cluster, enabled, referenceName);
            final Server[] servers = ServerHelper.getServersInCluster(configContext, target.getName());
            addResourceReferenceToClusteredServers(cluster, servers, enabled, referenceName);            
        } else if (target.getType() == TargetType.SERVER ||
            target.getType() == TargetType.DAS) {
            final Server server = ServerHelper.getServerByName(configContext, target.getName());
            addResourceReferenceToServer(server, enabled, referenceName);
        } else {
            throw new ConfigException(_strMgr.getString("invalidClusterOrServerTarget",
                target.getName()));
        }      
    }     
    
    /**
     * Delete resource reference from the cluster.
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */    
    private void deleteResourceReferenceFromCluster(Cluster cluster,
        String referenceName) throws ConfigException
    {
        final ResourceRef ref = cluster.getResourceRefByRef(referenceName);
        if (ref == null) {
            //Resource ref already exists in cluster         
            throw new ConfigException(_strMgr.getString("clusterResourceRefDoesNotExist",
                cluster.getName(), referenceName));     
        }
        cluster.removeResourceRef(ref, OVERWRITE);        
    }
    
    /**
     * Delete resource reference from the server instances that are part of the cluster.
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */      
    private void deleteResourceReferenceFromClusteredServers(Cluster cluster, Server[] servers, 
        String referenceName) throws ConfigException
    {
        for (int i = 0; i < servers.length; i++) {
            final ResourceRef ref = servers[i].getResourceRefByRef(referenceName);
            if (ref == null) {
                //This indicates that the cluster is in an inconsistent state. Some of the 
                //instances in the cluster have the ref and some do not.
                throw new ConfigException(_strMgr.getString("clusterResourceRefInconsistency",
                    referenceName, cluster.getName(), servers[i].getName()));
            }
            deleteResourceReferenceFromServer(servers[i], referenceName);
        }        
    }

    /**
     * Delete resource reference from a single server instance.
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */  
    private void deleteResourceReferenceFromServer(Server server, 
        String referenceName) throws ConfigException
    {        
        final ResourceRef ref = server.getResourceRefByRef(referenceName);
        if (ref == null) {
            //Resource ref already exists in server         
            throw new ConfigException(_strMgr.getString("serverResourceRefDoesNotExist",
                 server.getName(), referenceName));         
        }         
        server.removeResourceRef(ref, OVERWRITE);
    }

    /**
     * Delete the resource reference from the specified target (cluster or
     * or unclustered server).
     *
     * NOTE: Much of this functionality is duplicated in ApplicationReferenceHelper. As 
     * such be aware that bugs fixed here should be fixed there as well.
     */        
    public void deleteResourceReference(String targetName, String referenceName,
        boolean allowSystemRefs) throws ConfigException
    {
        deleteResourceReference(VALID_CREATE_DELETE_TYPES, targetName,
            referenceName, allowSystemRefs);
    }
    
    public void deleteResourceReference(String targetName, String referenceName) 
        throws ConfigException
    {
        deleteResourceReference(targetName, referenceName, false);
    }
    
    public void deleteResourceReference(TargetType[] validTypes,
        String targetName, String referenceName) 
        throws ConfigException
    {
        deleteResourceReference(validTypes, targetName, referenceName, false);
    }
    
    public void deleteResourceReference(TargetType[] validTypes,
        String targetName, String referenceName, boolean allowSystemRefs) 
        throws ConfigException
    {                        
        //Validate that there is indeed a resource with the specified name and
        //check to see whether we are creating or deleting a system resource
        //reference.
        final String type = validateResourceAndGetType(referenceName, 
            allowSystemRefs);        
        final ConfigContext configContext = getConfigContext();
        final Target target = TargetBuilder.INSTANCE.createTarget(
            validTypes, targetName, configContext);            
        
        if (target.getType() == TargetType.SERVER ||
            target.getType() == TargetType.DAS) {
            final Server server = ServerHelper.getServerByName(configContext, target.getName());
            deleteResourceReferenceFromServer(server, referenceName);
        } else if (target.getType().equals(TargetType.CLUSTER)) {
            final Cluster cluster = ClusterHelper.getClusterByName(configContext, target.getName());
            deleteResourceReferenceFromCluster(cluster, referenceName);
            final Server[] servers = ServerHelper.getServersInCluster(configContext, target.getName());
            deleteResourceReferenceFromClusteredServers(cluster, servers, referenceName);            
        } else {
            throw new ConfigException(_strMgr.getString("invalidClusterOrServerTarget",
                target.getName()));
        }      

    }
}
