
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
package com.sun.enterprise.ee.admin.mbeanapi;



import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import javax.management.MBeanException;
import javax.management.ObjectName;

import java.util.Properties;


/**
 * Interface ClustersConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface ClustersConfigMBean {

    /**
     * Method listClustersAsString lists the names (and optionally status e.g. running
     * stopped) of the clusters associated with the specified target. A cluster
     * is considered running if at least one of its server instances is running.
     *
     * @param targetName. The following targets are supported: "domain" -- lists
     * all the clusters in the domain, cluster-name -- lists the specified cluster,
     * clustered-instance-name -- lists the cluster to which the specified server
     * instance belongs, node-agent-name -- lists all of the clusters to which
     * instances managed by the specified node agent belong.
     * @param withStatus true implies that the status (e.g. running, stopped) will
     * be returned along with the cluster name.
     *
     * @return the list of cluster names (optionally containing status) for the
     * specified target.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String[] listClustersAsString(String targetName, boolean withStatus)
        throws InstanceException, MBeanException;

    /**
     * Method listClusters lists the JMX object names of the clusters associated
     * with the specified target.
     *
     * @param targetName The following targets are supported: "domain" -- lists
     * all the clusters in the domain, cluster-name -- lists the specified cluster,
     * clustered-instance-name -- lists the cluster to which the specified server
     * instance belongs, node-agent-name -- lists all of the clusters to which
     * instances managed by the specified node agent belong.
     *
     * @return the list of cluster JMX object names for the specified target.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName[] listClusters(String targetName)
        throws InstanceException, MBeanException;

    /**
     * Method startCluster attempts to start all of the reachable instances in
     * the specified cluster. For an instance to be reachable, its Node Agent must
     * be running. This method will not return until an attempt has been
     * made to start all instances in the cluster; however, no indication
     * of failure will be returned should one of the instances in the
     * cluster fail to start or is not reachable.
     *
     * @param clusterName the name of the cluster to start.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList startCluster(String clusterName)
        throws InstanceException, MBeanException;
	
    RuntimeStatusList startCluster(String autoHadbOverride, String clusterName)
        throws InstanceException, MBeanException;
        
    /**
     * Method stopCluster attempts to stop all of the running server instances
     * in the cluster. An instance must be reachable (through its Node
     * Agent) to be stopped. This method will not return until an attempt has been
     * made to stop all instances in the cluster; however, no indication
     * of failure will not be returned should one of the instances in the
     * cluster fail to stop or is not reachable.
     *
     *
     * @param clusterName the name of the cluster to stop.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList stopCluster(String clusterName)
        throws InstanceException, MBeanException;        
    
	RuntimeStatusList stopCluster(String autoHadbOverride, String clusterName)
        throws InstanceException, MBeanException;
    
		
    /**
     * Method deleteCluster attempts to delete the cluster. To be deleted a cluster
     * must contain no server instances. If the cluster is a standalone
     * cluster, then its corresponding configuration is deleted as well.
     *
     * @param clusterName the name of the cluster to delete.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void deleteCluster(String clusterName)
        throws InstanceException, MBeanException;

    /**
     * Method createCluster creates a new cluster. There are two types of
     * clusters, standalone and shared. A shared cluster shares its configuration
     * with one or more clusters or unclustered server instances. A standalone
     * cluster has its own configuration that is implicitly created when the
     * cluster is created. This configuration is named "clusterName-config" and
     * is created by creating an exact copy of the default configuration (named
     * "default-config"). The exception is that system properties can be
     * augmented or overwritten. The newly created cluster initially contains
     * no server instances, applications, or resources.
     *
     * @param clusterName the name of the cluster to create.
     * @param configName an optional parameter. When not specified (i.e. null) a standalone
     * cluster is created (i.e. a configuration named "clusterName-config" is implicitly
     * created). When specified the name of an existing configuration must be specified
     * and a shared cluster is created. The config name cannot be the default configuraiton
     * (i.e. "default-config") or the configuration of the Domain Administration Server
     * (typically named "server-config").
     * @param props an optional list of system properties. These properties are only relevant
     * when a standalone configuration is created (i.e. when the configName parameter is
     * non-null). When specified, these list system properties that can augment or
     * overwrite the system properties defined in the default-config.
     *
     * @return The JMX object name of the newly created cluster.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName createCluster(
        String clusterName, String configName, Properties props)
            throws InstanceException, MBeanException;

	
    ObjectName createCluster(
		String		clusterName, 
        String		configName, 
		Properties	props,
		String		hosts,
		String		haagentport,
		String		haadminpassword,
		String		haadminpasswordfile,
		String		devicesize,
		Properties	haprops,
		Boolean		autohadb,
		String		portbase) throws InstanceException, MBeanException;
	
    /**
     * Method getRuntimeStatus fetches the runtime status for the specified cluster.
     * A cluster is considered running if any of the instances in the cluster are
     * running. Furthermore, a cluster will need to be restarted if any of
     * the instances need to be restarted. Finally, the list of error messages
     * for the server instances is not returned and each server must be
     * queried individually.
     *
     * @param clusterName the name of the cluster whose runtime status is to
     * be fetched.
     *
     * @return the runtime status of the specified cluster.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList getRuntimeStatus(String clusterName)
        throws InstanceException, MBeanException;    

    /**
     * Method clearRuntimeStatus clears the runtime status for the specified cluster.
     * Specifically, the list of recently occurring error messages for each
     * instance in the cluster is cleared.
     *
     * @param clusterName the name of the cluster whose runtime status is to
     * be clearead.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void clearRuntimeStatus(String clusterName)
        throws InstanceException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
