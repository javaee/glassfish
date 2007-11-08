
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

import com.sun.enterprise.ee.admin.PortReplacedException;

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import javax.management.MBeanException;
import javax.management.ObjectName;

import java.util.Properties;


/**
 * Interface ServersConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface ServersConfigMBean {   
     
    /**
     * Method listServerInstancesAsString lists the names (and optionally status e.g. running
     * stopped) of the servers associated with the specified target. 
     *
     * @param targetName. The following targets are supported: "domain" -- lists
     * all the servers in the domain, cluster-name -- lists the servers in 
     * the specified cluster, instance-name -- lists the specified server instance,
     * node-agent-name -- lists all of the servers managed by the specified 
     * node agent.
     * @param andStatus true implies that the status (e.g. running, stopped) will
     * be returned along with the server name.
     *
     * @return the list of server names (optionally containing status) for the
     * specified target.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String[] listServerInstancesAsString(String targetName, boolean andStatus)
        throws InstanceException, MBeanException;

    /**
     * Method listServerInstances lists the JMX object names of the servers associated 
     * with the specified target. 
     *
     * @param targetName. The following targets are supported: "domain" -- returns
     * the names of all servers in the domain, cluster-name -- returns the names of 
     * servers in the specified cluster, instance-name -- returns the name of 
     * the specified server instance, node-agent-name -- returns the names of the 
     * servers managed by the specified node agent.
     *
     * @return the list of JMX object names for the servers associated with 
     * the specified target.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName[] listServerInstances(String targetName)
        throws InstanceException, MBeanException;

    /**
     * Method getRuntimeStatus fetches the runtime status for the specified server.
     *
     * @param server the name of the server whose runtime status is to
     * be fetched.
     *
     * @return the runtime status of the specified server.
     *     
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatus getRuntimeStatus(String serverName)
        throws InstanceException, MBeanException;

    /**
     * Method getServerInstanceRuntimeStatus fetches a list of runtime status 
     * objects corresponding to the server instances mathing the target.
     *
     * @param targetName The following targets are supported: "domain" -- returns
     * the status of all servers in the domain, cluster-name -- returns the status of 
     * servers in the specified cluster, instance-name -- returns the status of 
     * the specified server instance, node-agent-name -- returns the status of the 
     * servers managed by the specified node agent.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     * @return a status for each instance specified by the given target
     */    
    RuntimeStatusList getServerInstanceRuntimeStatus(String targetName) 
        throws InstanceException, MBeanException;
    
    /**
     * Method clearRuntimeStatus clears the runtime status of the specified server.
     * This results in the list of recent error messages being deleted.
     *     
     * @param serverName server the name of the server whose runtime status is to
     * be cleared.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void clearRuntimeStatus(String serverName)
        throws InstanceException, MBeanException;

    /**
     * Method startServerInstance attempts to start the specified server
     * if it is not running and is reachable. For an instance to be reachable, 
     * its Node Agent must be running.      
     *
     * @param serverName The name of the server instance to start.
     *
     * @throws InstanceException
     * @throws MBeanException
     * @return the status of the instance
     */
    RuntimeStatus startServerInstance(String serverName)
        throws InstanceException, MBeanException;

    /**
     * Method stopServerInstance attempts to stop the server if it is running and is
     * reachable. For an instance to be reachable, its Node Agent must
     * be running. 
     *
     *
     * @param serverName The name of the server instance to stop.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void stopServerInstance(String serverName)
        throws InstanceException, MBeanException;

    /**
     * Method stopServerInstance attempts to stop the server if it is 
     * running and is reachable. For an instance to be reachable, its 
     * Node Agent must be running. 
     *
     * @param serverName The name of the server instance to stop.
     * @param timeout Timeout before forcibly killing the instance
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void stopServerInstance(String serverName, String timeout) 
        throws InstanceException, MBeanException;

    /**
     * Method deleteServerInstance attempts to delete the server intstance. A server 
     * instance must be stopped before it can be deleted. If the server instance is
     * a standalone server instance its standalone configuration (named "serverName-config") 
     * is also implicitly deleted.
     *
     * @param serverName The name of the server instance to delete.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void deleteServerInstance(String serverName)
        throws InstanceException, MBeanException;

    /**
     * Method createServerInstance creates a new server instance. There are three types of
     * servers, clustered, unclustered standalone, and unclustered shared. 
     * A shared cluster shares its configuration
     * with one or more clusters or unclustered server instances. A standalone
     * cluster has its own configuration that is implicitly created when the
     * cluster is created. This configuration is named "serverName-config" and
     * is created by creating an exact copy of the default configuration (named
     * "default-config"). The exception is that system properties can be
     * augmented or overwritten. The newly created cluster initially contains
     * no server instances, applications, or resources. A clustered server instance 
     * inherits its configuration from its parent cluster.
     *
     * @param nodeAgentName the name of the node agent which will manage the server instance. This 
     * node agent may or may not exist. If it does not exist, then it will be implicitly created.
     * @param serverName the name of the new server instance.
     * @param configName when non-null the clusterName must be null and this creates a shared 
     * server instance. When both configName and clusterName are null, then a standalone 
     * server instance is created.
     * @param clusterName when non-null the configName must be null and this creates a clustered 
     * server instance. When both configName and clusterName are null, then a standalone 
     * server instance is created.
     * @param props an optional list of system properties. These properties are added as
     * system properties of the newly created server instance and are also relevant
     * when a standalone configuration is created (i.e. when the configName parameter is
     * non-null). When specified, these list system properties that can augment or
     * overwrite the system properties defined in the default-config. 
     *
     * @return The JMX object name of the newly created server.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName createServerInstance(
        String nodeAgentName, String serverName, String configName,
            String clusterName, Properties props)
                throws InstanceException, PortReplacedException, MBeanException;

    /**
     * Method listDASServerInstanceAsString returns the name of the Domain
     * Administration Server (DAS) and optionally its status (e.g.
     * running, stopped).
     *
     * @param andStatus if true the status of the DAS will be returned.
     *
     * @return the DAS server name (typically "server") and optionally
     * its status.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String listDASServerInstanceAsString(boolean andStatus)
        throws InstanceException, MBeanException;

    /**
     * Method listDASServerInstance returns the JMX object name of the 
     * Domain Administration Server (DAS).
     *     
     * @return The JMX object name of the DAS.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName listDASServerInstance()
        throws InstanceException, MBeanException;

    /**
     * Method listUnclusteredServerInstancesAsString lists the 
     * unclustered server instances in the domain and optionally 
     * their status (e.g. running, stopped).     
     *
     * @param andStatus if true the status of the unclustered
     * servers will be returned.
     *
     * @return the list of unclustered server instance names optionally
     * with their status.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String[] listUnclusteredServerInstancesAsString(boolean andStatus)
        throws InstanceException, MBeanException;

    /**
     * Method listUnclusteredServerInstances returns the 
     * JMX object names of the unclustered server instances in the domain.
     *
     *
     * @return the list of JMX object names of the unclustered server instances.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName[] listUnclusteredServerInstances()
        throws InstanceException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
