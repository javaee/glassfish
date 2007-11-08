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
package com.sun.enterprise.ee.admin.lbadmin.mbeans;

import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import java.util.Properties;


/**
 *  MBean interface representing configuration for the lb-config element.
 *
 * @author Satish Viswanatham
 */
public interface HTTPLBAdminConfigMBean {

    /**
     * List of available HTTP LB configs will be displayed.
     *
     * @param  configName  lb config name
     *
     * @return the list of HTTP LB config names
     *
     * @throws MBeanException   If the operation is failed
     */
    public String[] listLBConfigs(String configName) throws MBeanException;

    /**
     * LB config that references the server wil be returned
     *
     * @param  paramName         Name of the server
     * @return the list of HTTP LB config names
     *
     * @throws MBeanException   If the operation is failed
     */
    public String[] getLBConfigsForServer(String serverName) 
            throws MBeanException;

    /**
     * LB config that references the cluster wil be returned
     *
     * @param  paramName         Name of the cluser
     * @return the list of HTTP LB config names
     *
     * @throws MBeanException   If the operation is failed
     */
    public String[] getLBConfigsForCluster(String clusterName)
            throws MBeanException;

    /**
     * Returns the server names in a LB 
     *
     * @param  paramName         Name of the HTTP LB config
     * @return the list of servers
     *
     * @throws MBeanException   If the operation is failed
     */
    public String[] getServersInLBConfig(String configName)  
            throws MBeanException;

    /**
     * Returns the cluster names in a LB 
     *
     * @param  paramName         Name of the HTTP LB config
     * @return the list of clusters
     *
     * @throws MBeanException   If the operation is failed
     */
    public String[] getClustersInLBConfig(String configName) 
            throws MBeanException;
 
    /**
     * The create-http-lb-config command is used to create a load balancer
     * configuration. The load balancer configuration name must be unique in
     * the domain, and must not conflict with any Node Agent, configuration,
     * cluster, or server instance names in the domain. This gives a flat name
     * space so that the dotted name notation can be used to access any of 
     * these entities without ambiguity. 
     *
     * @param  configName  name of the lb configuration. If null, configuration
     *   name is automatically created from the target name by adding a 
     *   suffix.
     * @param  responseTimeoutInSeconds   timeout interval in seconds 
     *   within which response should be load balanced; else the instance
     *   would be considered unhealthy. Default value is 60. (type String)
     *
     * @param  httpsRouting   determines whether the load balancer should 
     *   route the incoming HTTPS request as HTTPS request to the instance.
     *   Default value is false (type Boolean).
     *
     * @param  reloadPollIntervalInSeconds    reload pool interval in 
     *   seconds at which load balancer has changed.  If it has change, load
     *   balancer would reload it. Value of 0 would imply that polling is 
     *   disabled (type String).
     *
     * @param  monitoringEnabled   determines whether monitoring is switched
     *   on or not. Default value is false (type Boolean).
     *
     * @param  routeCookieEnabled  determines whether route cookie is 
     *   enabled or not. Default value is true (type Boolean).
     *
     * @param   target    Name of the server or cluster
     * @param   properties Properties of the load balancer configuration
     *
     * @throws MBeanException   If the operation is failed
     */
    public void createLBConfig(String configName, 
            String responseTimeoutInSeconts, String httpsRouting, 
            String reloadPollIntervalInSeconds, String monitoringEnabled, 
            String routeCookieEnabled, String target, Properties props)
            throws MBeanException;

    /**
     * This deletes a load balancer configuration. The load balancer must not 
     * reference any clusters or instances before it can be deleted.
     *
     * @param   name    Name of the LB config
     *
     * @throws MBeanException   If the operation is failed
     */
    public void deleteLBConfig(String name) throws MBeanException;

    /**
     * This disables a server managed by the load balancer. The term quiesce
     * is sometimes used interchangably with disable. The basic function is to 
     * gracefully take a load balanced server offline with minimal impact 
     * to end users. Disabling therefore involves a timeout or grace period. 
     *
     * @param   target    Name of the server or cluster
     * @param   timeout disable timeout
     *
     * @throws MBeanException   If the operation is failed
     */
    public void disableServer(String target, String timeout) 
            throws MBeanException;
    
    /**
     * This is used to re-enable a previously disabled load balancer server.
     *
     * @param   target    Name of the server or cluster
     *
     * @throws MBeanException   If the operation is failed
     */
    public void enableServer(String target) throws MBeanException;
    
    /**
     * Disables a specific application managed by the load balancer.
     * The term quiesce is sometimes used interchangably with disable. 
     * The basic function is to gracefully take a load balanced application
     * offline with minimal impact to end users. Disabling therefore involves
     * a timeout or grace period. Disabling an application gives a finer 
     * granularity of control than disabling a server instance and is most 
     * useful when a cluster is hosting multiple independent applications. 
     *
     * @param   target     Name of the server or cluster
     * @param   appName    Name of the application
     * @param   timout     Timeout interval for disable operation. The timeout 
     *   (in minutes) to wait before disabling the specified target. This 
     *   allows for a graceful shutdown of the specified target. The default 
     *   value is 30 minutes. 
     *
     * @throws MBeanException   If the operation is failed
     */
    public void disableApplication(String target, String appName,
            String timeout) throws MBeanException;

    /**
     * This is used to re-enable a previously disabled load balancer
     * application.
     *
     * @param   target     Name of the server or cluster
     * @param   appName    Name of the application
     *
     * @throws MBeanException   If the operation is failed
     */
    public void enableApplication(String target, String appName)
            throws MBeanException;

    /**
     * This is to create a health checker to a cluster configuration. By 
     * default the healh checker will be configured.  This applies only 
     * to our native load balancer.
     *
     * @param   url   the URL to ping so as to determine the health state
     *   of a listener.
     *
     * @param   interval   specifies the interval in seconds at which health 
     *   checks of unhealthy instances carried out to check if the instances
     *   has turned healthy. Default value is 30 seconds. A value of 0 would
     *   imply that health check is disabled.
     *
     * @param   timeout    timeout interval in seconds within which response 
     *   should be obtained for a health check request; else the instance would
     *   be considered unhealthy.Default value is 10 seconds.
     *
     * @param   lbConfigName   name of the load balancer configuration
     *
     * @param   target   name of the target - cluster or stand alone 
     *  server instance
     *
     * @throws MBeanException   If the operation is failed
     */
    public void createHealthChecker(String url, String interval,
            String timeout, String lbConfigName, String target) 
            throws MBeanException;

    /**
     * Deletes a health checker from a load balancer configuration.  
     *
     * @param   lbConfigName   Name of http load balancer configuration name
     * @param   target   Name of a cluster or stand alone server instance
     *
     * @throws MBeanException   If the operation is failed
     */
    public void deleteHealthChecker(String lbConfigName, String target)
            throws MBeanException;

    /**
     * This is used to add an existing server to an existing load balancer 
     * configuration. 
     *
     * @param   target      Name of the server or cluster
     * @param   config      Name of the config
     *
     * @throws MBeanException   If the operation is failed
     */
    public void createLBRef(String target, String configName)
            throws MBeanException;

    /**
     * This is used to add an existing server to an existing load balancer 
     * configuration. In addition, this method creates health checker, set the
     * lb policy,policy module , and enables all instances and applications
     *
     * @param   target      Name of the server or cluster
     * @param   config      Name of the config
     * @param   lbPolicy    load balancer policy
     * @param   lbPolicyModule path to the shared library implementing the user-defined
     *          load balancing policy
     * @param   hcURL       health checker url
     * @param   hcInterval  interval in seconds for the health checker
     * @param   hcTimeout   timeout in seconds for the health checker
     * @param   enableInstances enable all instances in the target
     * @param   enableApps  enable all user applications in the target
     *
     * @throws MBeanException   If the operation is failed
     */

    public void createLBRef(String target, String configName, String lbPolicy,
            String lbPolicyModule, String hcURL, String  hcInterval, String hcTimeout,
            boolean enableInstances, boolean enableApps) throws MBeanException;

    /**
     * This is used to delete an instance reference from a load balancer
     * config. It is importatnt to note that if you wish not to interrupt
     * users accessing applications in the server you should ensure that
     * instance  been disabled before removal.
     *
     * @param   target      Name of the server or cluster
     * @param   config      Name of the config
     *
     * @throws MBeanException   If the operation is failed
     */
    public void deleteLBRef(String target, String configName)
            throws MBeanException;

    /**
     * This is used to explicitly create the loadbalancer.xml file consumed by 
     * the load balancer plugins. The exported file must then be manually 
     * copied to the load balancer plugins before the load balancer will
     * apply any changes.
     *
     * @param configName    Name of the load balancer
     *
     * @param filepath      The file name is optional and can be: 
     *   (a). omitted in which case a file named loadbalancer.xml is 
     *   created in the current directory.  
     *
     *   (b).  directory (relative or absolute) in which case a  file 
     *   named loadbalancer.xml is created in the specified directory. 
     *
     *   (c). file name (relative or absolute) in which case the named 
     *   file is created.
     *
     * @return name of the newly generated file
     * @throws MBeanException   If the operation is failed
     */
    public String exportLBConfig(String configName, String filePath) 
            throws MBeanException;
    
    /**
    * Applies changes to the specified lb.
    *
    * @param  lbName  name of the loadbalancer
    */
    public void applyLBChanges(String configName,String lbName) throws MBeanException;

    /**
     * Applies changes to all associated loadbalancers.
     */
    public void applyLBChanges(String configName) throws MBeanException;
    
}
