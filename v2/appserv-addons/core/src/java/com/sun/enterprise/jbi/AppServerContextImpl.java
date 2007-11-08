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

/**
 *  AppServerContextImpl.java
 *
 *  Created on February 5, 2007, 10:15 AM
 */

package com.sun.enterprise.jbi;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.AdminServiceConfig;
import com.sun.appserv.management.config.JMXConnectorConfig;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.server.ApplicationServer;

import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * The JBI Runtime needs information on the servers/clusters in a domain, state of an
 * instance etc. This is the interface to get this application server context information.
 *
 * @author Sun Microsystems, Inc.
 */
public class AppServerContextImpl
            implements AppServerContext
{
    private static String SERVER = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    private static Logger _logger = LogDomains.getLogger(LogDomains.SERVER_LOGGER);

    private Method irMethod = null;
    private final String INSTANCE_REGISTRY = 
    "com.sun.enterprise.ee.admin.clientreg.InstanceRegistry";
    
    public AppServerContextImpl()
    {
        try {
                irMethod = (Method) java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction() {
                    public java.lang.Object run() throws Exception {
                        Class cls = Class.forName(INSTANCE_REGISTRY);
                        return cls.getMethod("getInstanceConnection", 
                        new Class[] {String.class});
                    }
                });
        } catch (Exception e) {
             _logger.log(Level.SEVERE, e.getMessage(), e);
             throw new RuntimeException(e.getMessage());
        }    
    }
    /*----------------------------------------------------------------------------------*\
     *         Common operations invoked from the DAS and non-DAS instances.            *
    \*----------------------------------------------------------------------------------*/
    
    /**
     * Determine if this instance is the central administration server.
     *
     * @return true if this instance is the central administration server.
     */
    public boolean isDAS()
    {
        AdminService adminSvc = AdminService.getAdminService();
        boolean isDAS = false;
        if (adminSvc != null)
        {
            isDAS =  adminSvc.isDas();
        }
        return isDAS;
    }
    
    /**
     * Get the "target" for this instance. If this instance :
     * <ul>
     *   <li> is the central admininistration server instance then target = "server" </li>
     *   <li> is a clustered instance then target = [cluster-name] </li>
     *   <li> is a standalone instance then target = [server-name] </li>
     * </ul>
     * <br/>
     * This is equivalent to calling getTargetName(getInstanceName()).
     *
     * @return the "target" for this instance.
     */
    public String getTargetName()
        throws Exception
    {
        String instanceName = getInstanceName();
        return getTargetName(instanceName);
    }

    

    
    /**
     * Get the name of the server instance. In case this is the central administartion 
     * server then instance name = "server".
     *
     * @return the name of the glassfish instance.
     */
    public String getInstanceName()
    {
        return System.getProperty("com.sun.aas.instanceName");
    }
    
    /**
     * Get the MBeanServerConnection for a specific instance.
     *
     * @param instanceName - instance name, the MBeanServerConnection for which is to 
     *                       be obtained.
     * @return the MBeanServerConnection to a given instance.
     * @throws Exception on errors
     */
    public MBeanServerConnection  getMBeanServerConnection(final String instanceName)
        throws Exception
    {
            // If the instance name is the same as the local instance return the
            // local MBeanServer
            if ( getInstanceName().equals(instanceName))
            {
                return getPlatformMBeanServer();
            }
            
            if ( isDAS() && !multipleServersSupported() )
            {
                // Developer profile
                throw new Exception("Developer profile does not support multiple server instances");
            }
            else
            {
                return (MBeanServerConnection) java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedExceptionAction() {
                     public java.lang.Object run() throws Exception {
                         return irMethod.invoke(null, new Object[] {instanceName});
                     }
                });
             }
    }
    
    /*----------------------------------------------------------------------------------*\
     *         Operations invoked from the DAS only.                                    *
    \*----------------------------------------------------------------------------------*/

    /**
     * Determine the runtime state of an instance.
     *
     * @param instanceName - name of the instance whose state is to be determined.
     * @return true if the specified instance is running false is it is shutdown
     */
    public  boolean isInstanceUp(String instanceName)
    {
        boolean isRunning = false;
        if ( isDAS() )
        {
            if ( SERVER.equals(instanceName))
            {
                // -- If DAS is running, so is the "server" instance.
                isRunning = true;
            }
            else
            {
                String  
                    instanceObjName = "amx:J2EEServer=" + instanceName + ",j2eeType=JVM,*";
                    
                ObjectName objName = null;
                try
                {
                    objName =  new ObjectName(instanceObjName);
                }
                catch(javax.management.MalformedObjectNameException mex)
                {
                    _logger.log(Level.SEVERE, mex.getMessage(), mex);
                    return false;
                }

                java.util.Set<ObjectName> nameSet = getPlatformMBeanServer().queryNames(objName, null);

                if ( (!nameSet.isEmpty()) )
                {
                    isRunning = true;
                }
            }
        }
        return isRunning;
    }
    
    /**
     * Determine if the central administraion server supports multiple servers.
     *
     * @return true if this administration server supports multiple servers.
     */
    public boolean multipleServersSupported()
    {
        if ( isDAS())
        {
            MBeanServer mbeanServer = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            DomainRoot domainRoot = ProxyFactory.getInstance(mbeanServer).getDomainRoot();

            return domainRoot.getSystemInfo().supportsFeature(
                SystemInfo.MULTIPLE_SERVERS_FEATURE);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Get the names of all the non-clustered standalone servers in the domain.
     *
     * @return an array of names of all the standalone servers in the domain.
     */
    public String[] getStandaloneServerNames()
    {
        try
        {
            Server[] servers = ServerHelper.getUnclusteredServers(getConfigContext(), false);
            return getServerNames(servers);
        }
        catch (Exception ex)
        {
            _logger.log(Level.WARNING, ex.getMessage(), ex);
            return new String[0];
        }
    }
    
    /**
     * Get the names of all the clsuters in the domain.
     *
     * @return an array of names of all the clusters in the domain. If the domain has zero
     * clusters then an empty array is returned.
     */
    public String[] getClusterNames()
    {
        try
        {
            Cluster[] clusters = ClusterHelper.getClustersInDomain(getConfigContext());
            return getClusterNames(clusters);
        }
        catch (Exception ex)
        {
            _logger.log(Level.WARNING, ex.getMessage(), ex);
            return new String[0];
        }
    }
    
    /**
     * Get the names of all the member servers in the specified cluster.
     *
     * @return an array of names of all the servers in the cluster. If the clusterName is
     * a non-existent cluster or does not have any member instances then an empty 
     * array is returned
     */ 
    public String[] getServersInCluster(String clusterName)
    {
        try
        {
            Server[] servers = ServerHelper.getServersInCluster(getConfigContext(), 
                clusterName);
            return getServerNames(servers);
        }
        catch (Exception ex)
        {
            _logger.log(Level.WARNING, ex.getMessage(), ex);
            return new String[0];
        }  
    }
    
    /**
     * Get the "target" for the specified instance.
     *
     * <ul>
     *   <li> is the central admininistration server instance then target = "server" </li>
     *   <li> is a clustered instance then target = [cluster-name] </li>
     *   <li> is a standalone instance then target = [server-name] </li>
     * </ul>
     *
     * @param instanceName - name of the instance, whose target is to be determined
     * @return the "target" for the specific instance
     */
    public String getTargetName(String instanceName)
        throws Exception
    {
        String targetName = instanceName;
        if ( isInstanceClustered(instanceName) )
        {
            // -- Target is the cluster name
            targetName  = getClusterForInstance(instanceName);
        }
        return targetName;
    }
    
    /**
     *
     * @return true if the instance is clustered
     * @param instanceName - instance name
     */
    public boolean isInstanceClustered(String instanceName)
        throws Exception
    {
        boolean isClustered = false;
        ConfigContext ctx = getConfigContext();
        if ( ctx != null )
        {
            isClustered = ServerHelper.isServerClustered(ctx, instanceName);
        }
        return isClustered;
    }

    /*----------------------------------------------------------------------------------*\
     *         Private helpers                                                          *
    \*----------------------------------------------------------------------------------*/

    /**
     * @return the name of the cluster the instance belongs to. If the instance does not belong 
     * to a cluster a null is returned.
     */
    private String getClusterForInstance(String instanceName)
        throws Exception
    {
        String clusterName = null;
        if ( isInstanceClustered(instanceName) )
        {
        	Cluster 
            	cluster = ClusterHelper.getClusterForInstance(getConfigContext(), instanceName);
        	clusterName = cluster.getName();
        }
        return clusterName;
    }
    
    /**
     * @return the ConfigContext based on whether this is  DAS or non-DAS instance.
     */
    private ConfigContext getConfigContext()
    {
        ConfigContext ctx = null;
        if (isDAS())
        {
            if ( AdminService.getAdminService() != null )
            {
                ctx = AdminService.getAdminService().getAdminContext().getAdminConfigContext();   
            }
        }
        else
        {
            if (ApplicationServer.getServerContext() != null)
            {
                ctx = ApplicationServer.getServerContext().getConfigContext();   
            }
        }
        return ctx;
    }
    
    /**
     * @return the DAS MBeanServer
     */
    private MBeanServer getPlatformMBeanServer()
    {
        return java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }
    
    /**
     * 
     * @return a Set containing the server names.
     */
    private String[] getServerNames(Server[] servers)
    {
        String[] ucServers = new String[servers.length];
        for (int i = 0; i < servers.length; i++) 
        {
            ucServers[i] = servers[i].getName();
        }
        return ucServers;
    }
    
    /**
     * @return a String array containing the cluster names.
     */
    private String[] getClusterNames(Cluster[] clusters)
    {
        String[] domClusters = new String[clusters.length];
        for (int i = 0; i < clusters.length; i++) 
        {
            domClusters[i] = clusters[i].getName();
        }
        return domClusters;
    }
}
