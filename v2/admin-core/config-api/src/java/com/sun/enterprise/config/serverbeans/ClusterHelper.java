/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * ClusterHelper.java
 *
 * Created on October 23, 2003, 11:29 AM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Config;

import java.util.ArrayList;

/**
 *
 * @author  kebbs
 */
public class ClusterHelper extends ConfigAPIHelper {
    
    /**
     * Return the given cluster array as a comma separated string
     */
    public static String getClustersAsString(Cluster[] clusters)
    {
        String result = "";
        for (int i = 0; i < clusters.length; i++) {
            result += clusters[i].getName();
            if (i < clusters.length - 1) {
                result += ",";
            }
        }
        return result;
    }
    
    /**
     * Return all the clusters in the domain or a zero length list.
     */
    public static Cluster[] getClustersInDomain(ConfigContext configContext) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);  
        final Clusters clusters = domain.getClusters();
        if (clusters != null) {
            if (clusters.getCluster() != null) {
                return clusters.getCluster();
            }
        }            
        return new Cluster[0];
    }
     
    /**
     * Return true if the given clusterName corresponds to the name of a cluster.
     */
    public static boolean isACluster(ConfigContext configContext, String clusterName) 
        throws ConfigException
    {        
        final Domain domain = getDomainConfigBean(configContext);        
        final Clusters clusters = domain.getClusters();
        if (clusters == null) {
            return false;
        }
        final Cluster cluster = clusters.getClusterByName(clusterName);
        return (cluster != null ? true : false);
    }
        
    /**
     * Return the named cluster. If the cluster does not exist, an exception will 
     * be thrown.
     */
    public static Cluster getClusterByName(ConfigContext configContext, String clusterName) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);          
        final Cluster cluster = domain.getClusters().getClusterByName(clusterName);
        if (cluster == null) {
            throw new ConfigException(_strMgr.getString("noSuchCluster", 
                clusterName));
        }
        return cluster;
    }
    
    /**
     * Return the cluster associated with the given instanceName. An exception is thrown
     * if the givne instanceName does not exist or is not a clustered server instance.
     *
     * FIXTHIS: We really need to give the server a reference to its cluster; rather than the 
     *  other way around. This will make this operation much faster and management easier in general.
     **/
    public static Cluster getClusterForInstance(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {
        Cluster[] clusters = getClustersInDomain(configContext);
        for (int i = 0; i < clusters.length; i++) {
            ServerRef[] servers = clusters[i].getServerRef();
            for (int j = 0; j < servers.length; j++) {
                if (servers[j].getRef().equals(instanceName)) {
                    // check to see if the server exists as a sanity check. 
                    // NOTE: we are not checking for duplicate server instances here.
                    Server server = ServerHelper.getServerByName(configContext, instanceName);
                    return (clusters[i]);
                }
            }
        }
        throw new ConfigException(_strMgr.getString("noSuchClusteredInstance", 
            instanceName));
    }
    
    
    /**
     * Return all the clusters referencing the given configuration or a zero length list if 
     * there are none.
     */
    public static Cluster[] getClustersReferencingConfig(ConfigContext configContext, String configName) 
        throws ConfigException
    {        
        //First ensure that the config exists
        Config config = getConfigByName(configContext, configName);
        
        //Now find all server instances that reference that config.
        Cluster[] clusters = getClustersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i].getConfigRef().equals(configName)) {
                result.add(clusters[i]);
            }            
        }
        return (Cluster[])result.toArray(new Cluster[result.size()]);
    }
   
    /**
     * Return all the clusters associasted with a node agent. In other words return all the
     * clusters for the instances managed by the given agentName.
     */
    public static Cluster[] getClustersForNodeAgent(ConfigContext configContext, String agentName) 
        throws ConfigException
    {
        Cluster[] clusters = getClustersInDomain(configContext);
        Server[] servers = ServerHelper.getServersOfANodeAgent(configContext, agentName);
        ArrayList result = new ArrayList();
        for (int i = 0; i < clusters.length; i++) {
            ServerRef[] serverRefs = clusters[i].getServerRef();
            for (int j = 0; j < serverRefs.length; j++) {                
                for (int k = 0; k < servers.length; k++) {
                    if (serverRefs[j].getRef().equals(servers[k].getName())) {
                        if (!result.contains(clusters[i])) {
                            result.add(clusters[i]);
                        }
                    }
                }
            }
        }
        return (Cluster[])result.toArray(new Cluster[result.size()]);
    }
    
    /**
     * Return the configuration associated with the given clusterName. An exception 
     * is thrown if the clusters configuration does not exist (which should never 
     * happen).
     */
    public static Config getConfigForCluster(ConfigContext configContext, String clusterName) 
        throws ConfigException
    {
        final Cluster cluster = getClusterByName(configContext, clusterName);                
        final Domain domain = getDomainConfigBean(configContext);                  
        final Config config = domain.getConfigs().getConfigByName(cluster.getConfigRef());
        if (config == null) {
            throw new ConfigException(_strMgr.getString("noSuchClusterConfig", 
                cluster.getConfigRef(), clusterName));
        }
        return config;
    }        
    
    /**
     * Return true if the cluster is standalone. A standalone cluster has a configuration
     * named <clusterName>-config and it configuration is referenced by the cluster 
     * (and its instances) only. No other clusters or servers may refer to its configuration.
     */
    public static boolean isClusterStandAlone(ConfigContext configContext, String clusterName) 
        throws ConfigException
    {
        final Cluster cluster = getClusterByName(configContext, clusterName);
        final String configName = cluster.getConfigRef();
        if (isConfigurationNameStandAlone(configName, clusterName)) {            
            if (isConfigurationReferencedByClusterOnly(configContext, configName, clusterName)) {
                return true;
            }
        }       
       return false;
    }    
        
    /**
     * Return true if the given server instance references the stated application.
     */
    public static boolean clusterReferencesApplication(ConfigContext configContext, 
        String clusterName, String appName) throws ConfigException
    {
        final Cluster cluster = getClusterByName(configContext, clusterName);         
        return clusterReferencesApplication(cluster, appName);
    }
    
    public static boolean clusterReferencesApplication(Cluster cluster, String appName) 
        throws ConfigException
    {
        final ApplicationRef ref = cluster.getApplicationRefByRef(appName);
        return (ref == null) ? false : true;
    }

    public static boolean clusterReferencesJdbcConPool(ConfigContext ctx, 
            String clusterName, String poolName) throws ConfigException 
    {

        final Cluster cluster = getClusterByName(ctx, clusterName);
        return clusterReferencesJdbcConPool(cluster, poolName);
    }
    
    public static boolean clusterReferencesJdbcConPool(Cluster cluster,
            String poolName) throws ConfigException
    {
        final ResourceRef ref = cluster.getResourceRefByRef(poolName);
        return (ref == null) ? false : true;
    }
       
    /**
     * Return the server instances referencing the given configName or a zero length list.
     */
    public static Cluster[] getClustersReferencingApplication(ConfigContext configContext, String appName) 
        throws ConfigException
    {                
        //Now find all server instances that reference the application
        Cluster[] clusters = getClustersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < clusters.length; i++) {
            if (clusterReferencesApplication(clusters[i], appName)) {
                result.add(clusters[i]);
            }            
        }
        return (Cluster[])result.toArray(new Cluster[result.size()]);
    }
    
    /**
     * Return true if the given server instance references the stated resource.
     */
    public static boolean clusterReferencesResource(ConfigContext configContext, 
        String clusterName, String resourceName) throws ConfigException
    {
        final Cluster cluster = getClusterByName(configContext, clusterName);         
        return clusterReferencesResource(cluster, resourceName);
    }
    
    public static boolean clusterReferencesResource(Cluster cluster,
        String resourceName) throws ConfigException
    {
        final ResourceRef ref = cluster.getResourceRefByRef(resourceName);
        return (ref == null) ? false : true;
    }
       
    /**
     * Return the server instances referencing the given configName or a zero length list.
     */
    public static Cluster[] getClustersReferencingResource(ConfigContext configContext, String resName) 
        throws ConfigException
    {                
        //Now find all server instances that reference the application
        Cluster[] clusters = getClustersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < clusters.length; i++) {
            if (clusterReferencesResource(clusters[i], resName)) {
                result.add(clusters[i]);
            }            
        }
        return (Cluster[])result.toArray(new Cluster[result.size()]);
    }
    
    
        /**
     * Return all the application refs of the server
     */
    public static ApplicationRef[] getApplicationReferences(ConfigContext configContext, 
        String clusterName) throws ConfigException
    {
        final Cluster cluster = getClusterByName(configContext, clusterName);
        if (cluster.getApplicationRef() == null) {
            return new ApplicationRef[0];
        } else {
            return cluster.getApplicationRef();
        }
    }
    
    /**
     * Return all the resource refs of the server
     */    
    public static ResourceRef[] getResourceReferences(ConfigContext configContext, 
        String clusterName) throws ConfigException
    {
        final Cluster cluster = getClusterByName(configContext, clusterName);
        if (cluster.getResourceRef() == null) {
            return new ResourceRef[0];
        } else {
            return cluster.getResourceRef();
        }
    }    
}
