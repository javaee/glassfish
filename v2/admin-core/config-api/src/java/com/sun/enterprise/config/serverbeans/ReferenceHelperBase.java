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
package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;

import com.sun.enterprise.util.i18n.StringManager;

import java.util.ArrayList;

public abstract class ReferenceHelperBase {
    
    protected static final StringManager _strMgr=StringManager.getManager(ReferenceHelperBase.class);        
       
    public ReferenceHelperBase() {
    }
    
    protected abstract Server[] getReferencingServers(ConfigContext configContext, String name) 
        throws ConfigException;
    protected abstract Cluster[] getReferencingClusters(ConfigContext configContext, String name) 
        throws ConfigException;
    
    /**
     * Is the configuration referenced by anyone (i.e. any server instance or cluster
     */
    public boolean isReferenced(ConfigContext configContext, String name) 
        throws ConfigException
    {
        //See if any servers are referencing the resource
        Server[] servers = getReferencingServers(configContext, name);        
        if (servers.length > 0) {
            return true;
        }
        //See if any clusters are referencing the resource
        Cluster[] clusters = getReferencingClusters(configContext, name);
        if (clusters.length > 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * server instance.
     */
    public boolean isReferencedByServerOnly(ConfigContext configContext, 
        String name, String serverName) throws ConfigException        
    {        
        Server[] servers = getReferencingServers(configContext, name); 
        if (servers.length == 1 && servers[0].getName().equals(serverName)) {
            //The stated server is the only one referencing the config so see if any 
            //clusters reference the config
            Cluster[] clusters = getReferencingClusters(configContext, name);
            if (clusters.length == 0) {
                return true;
            } 
        }
        return false;
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * cluster.
     */
    public boolean isReferencedByClusterOnly(ConfigContext configContext, 
        String name, String clusterName) throws ConfigException        
    {                       
        //See if any clusters reference the config
        Cluster[] clusters = getReferencingClusters(configContext, name);
        if (clusters.length == 1 && clusters[0].getName().equals(clusterName)) {
            Server[] servers = getReferencingServers(configContext, name); 
            if (servers.length == 0) {
                return true;
            } else {
                //Check to see that the only servers referencing the configuration are those that
                //are also part of the cluster.
                Server[] clusterServers = ServerHelper.getServersInCluster(configContext, clusterName);
                if (clusterServers.length == servers.length) {
                    boolean found = false;
                    for (int i = 0; i < clusterServers.length; i++) {
                        found = false;
                        for (int j = 0; j < servers.length; j++) {
                            if (clusterServers[i].getName().equals(servers[j].getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    }
                    if (found) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Find all the servers or clusters associated with the given configuration and return them 
     * as a comma separated list.
     */
    public String getReferenceesAsString(ConfigContext configContext, String name) 
        throws ConfigException
    {        
        Server[] servers = getReferencingServers(configContext, name);
        Cluster[] clusters = getReferencingClusters(configContext, name);
        String serverList = ServerHelper.getServersAsString(servers);                
        String clusterList = ClusterHelper.getClustersAsString(clusters);
        if (serverList.length() > 0) {
            if (clusterList.length() > 0) {
                return serverList + "," + clusterList;
            } 
            return serverList;
        } else {
            return clusterList;
        }        
    }    
}
