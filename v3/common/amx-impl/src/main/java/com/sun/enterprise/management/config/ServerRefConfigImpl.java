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
 */

package com.sun.enterprise.management.config;

import javax.management.ObjectName;

import com.sun.appserv.management.base.XTypes;
import com.sun.enterprise.management.support.Delegate;

import com.sun.appserv.management.base.Util;

/*
LOAD_BALANCER_MONITORING
This code never belonged here.
Decouple and reimplement correctly elsewhere; config
item should *never* depend on monitoring ones.

import static com.sun.appserv.management.base.XTypes.LOAD_BALANCER_MONITOR;
import com.sun.enterprise.management.support.LBDeregistrationUtil;
import com.sun.appserv.management.monitor.LoadBalancerMonitor;  
import com.sun.appserv.management.monitor.LoadBalancerClusterMonitor;
import com.sun.appserv.management.monitor.LoadBalancerServerMonitor;
import com.sun.appserv.management.monitor.LoadBalancerApplicationMonitor;
import com.sun.appserv.management.monitor.LoadBalancerContextRootMonitor;
*/

import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.ClusterRefConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.config.LBConfig;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
	Configuration for the &lt;server-ref&gt; element.
*/
public final class ServerRefConfigImpl extends AMXConfigImplBase 
        implements ConfigFactoryCallback{
    
    public ServerRefConfigImpl(final Delegate delegate)
    {
        super(delegate);
    }


    public ObjectName getHealthCheckerConfigObjectName()
    {
        return(getContaineeObjectName(XTypes.HEALTH_CHECKER_CONFIG));
    }

/*
    private HealthCheckerConfigFactory getHealthCheckerConfigFactory() {
        return new HealthCheckerConfigFactory(this);
    }

        public ObjectName
    createHealthCheckerConfig(
        String url, 
        String intervalInSeconds,
        String timeoutInSeconds)
    {
        return getHealthCheckerConfigFactory().create(  url,intervalInSeconds, timeoutInSeconds);
    }

    public void removeHealthCheckerConfig(final String name)
    {
        final ObjectName item = getHealthCheckerConfigObjectName();
        getHealthCheckerConfigFactory().remove(item);
    }
*/

/*
LOAD_BALANCER_MONITORING
See duplicated code in ClusterRefConfigImpl, ServerRefConfigImpl, 
DeployedItemRefConfigImpl.
This code never belonged here.
Decouple and reimplement correctly elsewhere; config
item should *never* depend on monitoring ones.

	// implements ServerRefConfig
    ServerRefDeregistrationHelper mHelper = null;
    
    protected void unregisterMisc() {
        try {
            mHelper = new ServerRefDeregistrationHelper(
                            getName(), getMBeanServer());
        } catch (Exception ex) {
            logWarning("ServerRefConfig unregisterMisc failed. " +
                    "ServerRefDeregistrationHelper creation failed" );
        }                
    }    

    public void postDeregister() {
        super.postDeregister();
        try {            
            if (getContainer() instanceof LBConfig) {
                //collect all load-balancer elements which refer
                //to this lb-config
                String lbConfigName = ((LBConfig)getContainer()).getName();
                Map<String,LoadBalancerConfig> relevantLoadBalancerConfigMap = 
                     mHelper.fetchLoadBalancerConfigs((LBConfig)getContainer());
                mHelper.unregisterMonitors(relevantLoadBalancerConfigMap);
            } else if (getContainer() instanceof ClusterConfig) {
                String clusterName = ((ClusterConfig)getContainer()).getName();
                Map<String,LoadBalancerConfig> relevantLoadBalancerConfigMap =
                    mHelper.fetchLoadBalancerConfigs((ClusterConfig)getContainer());
                mHelper.unregisterMonitors(clusterName, relevantLoadBalancerConfigMap);
            }
            mHelper = null;
        } catch (Exception ex) {
            logWarning("ServerRefConfig postDeregistration failed. " +
                    "Load Balancer Monitoring MBeans might be lying around\n" +
                    ExceptionUtil.toString(ex));
        }                
    }

    private class ServerRefDeregistrationHelper {    

        final static String J2EE_TYPE = "j2eeType";
        final ObjectNames objectNames = ObjectNames.getInstance(JMX_DOMAIN);    
        String serverName = null;
        MBeanServer mbs = null;

        public ServerRefDeregistrationHelper(String serverName, MBeanServer mbs) {
            this.mbs = mbs;
            this.serverName = serverName;
        }        
        
        void unregisterMonitors(Map<String,LoadBalancerConfig>  
            relevantLoadBalancerConfigMap) throws JMException {
            
            unregisterMonitors(serverName, relevantLoadBalancerConfigMap);
        }
        
        void unregisterMonitors(String clusterName,
            Map<String,LoadBalancerConfig>  relevantLoadBalancerConfigMap) 
            throws JMException {

            for (String loadBalancerName : relevantLoadBalancerConfigMap.keySet()) {
               LoadBalancerMonitor loadBalancerMonitor = 
                    LBDeregistrationUtil.getInstance(mbs).
                       fetchLBMonitoringRoot(loadBalancerName);

               Map<String,LoadBalancerClusterMonitor> loadBalancerClusterMonitorMap =
                   loadBalancerMonitor.getLoadBalancerClusterMonitorMap();

               LoadBalancerClusterMonitor loadBalancerClusterMonitor = 
                   loadBalancerClusterMonitorMap.get(clusterName);

               //loadBalancerServerMonitorMap will have only one element for every
               //standalone server as it is is wrapped up in a default cluster
               Map<String,LoadBalancerServerMonitor> loadBalancerServerMonitorMap =
                    loadBalancerClusterMonitor.getLoadBalancerServerMonitorMap();

               for (String serverName : loadBalancerServerMonitorMap.keySet()) {
                   LoadBalancerServerMonitor loadBalancerServerMonitor = 
                       loadBalancerServerMonitorMap.get(serverName);
                   Map<String,LoadBalancerApplicationMonitor> 
                       loadBalancerApplicationMonitorMap =
                           loadBalancerServerMonitor
                           .getLoadBalancerApplicationMonitorMap();

                   for (String appName : loadBalancerApplicationMonitorMap.keySet()) {
                        LoadBalancerApplicationMonitor loadBalancerApplicationMonitor = 
                            loadBalancerApplicationMonitorMap.get(appName);
                        Map<String,LoadBalancerContextRootMonitor> 
                            lbcrmm = loadBalancerApplicationMonitor
                                .getLoadBalancerContextRootMonitorMap();
                       for (String ctxRootName : lbcrmm.keySet()) {
                            LoadBalancerContextRootMonitor loadBalancerContextRootMonitor = 
                                lbcrmm.get(ctxRootName);
                            ObjectName loadBalancerContextRootMonitorObjName = 
                                Util.getObjectName(loadBalancerContextRootMonitor);
                            mbs.unregisterMBean(loadBalancerContextRootMonitorObjName);
                        }

                        ObjectName loadBalancerApplicationMonitorObjName = 
                        Util.getObjectName(loadBalancerApplicationMonitor);
                        mbs.unregisterMBean(
                            loadBalancerApplicationMonitorObjName);
                    }

                    ObjectName loadBalancerServerMonitorObjName = 
                        Util.getObjectName(loadBalancerServerMonitor);
                        mbs.unregisterMBean(loadBalancerServerMonitorObjName);
                }

                ObjectName loadBalancerClusterMonitorObjName = 
                    Util.getObjectName(loadBalancerClusterMonitor);
                mbs.unregisterMBean(loadBalancerClusterMonitorObjName);
            }
        }
        
        Map<String,LoadBalancerConfig> fetchLoadBalancerConfigs(
            LBConfig lbConfig) {
            Map<String, LBConfig> lbConfigMap = new HashMap<String, LBConfig>();
            lbConfigMap.put(lbConfig.getName(), lbConfig);
            return fetchLoadBalancerConfigs(lbConfigMap);
        }        
        
        Map<String,LoadBalancerConfig> fetchLoadBalancerConfigs(
            ClusterConfig clusterConfig) {

            String clusterName = clusterConfig.getName();
            //collect all cluster-ref elements which refer to this cluster
            Map<String,LBConfig> allLBConfigMap =
                 getDomainRoot().getDomainConfig().getLBConfigMap();

            Map<String, LBConfig> relevantLBConfigMap = new HashMap<String, LBConfig>();
            for (String lbConfigName : allLBConfigMap.keySet()) { 
                LBConfig lbConfig = allLBConfigMap.get(lbConfigName);
                Map<String,ClusterRefConfig> clusterRefConfigMap = 
                    lbConfig.getClusterRefConfigMap();
                for (String clusterRef : clusterRefConfigMap.keySet()) { 
                    if (clusterRef.equals(clusterName)) {
                        relevantLBConfigMap.put(lbConfigName, lbConfig);
                        break;
                    }
                }
            }
            return fetchLoadBalancerConfigs(relevantLBConfigMap);
        } 
        
        private Map<String,LoadBalancerConfig> fetchLoadBalancerConfigs(
            Map<String, LBConfig> lbConfigMap) {

            Map<String,LoadBalancerConfig> relevantLoadBalancerConfigMap =
                new HashMap<String,LoadBalancerConfig>();

            for (String lbConfigName : lbConfigMap.keySet()) {
                //collect all load-balancer elements which refer to this lb-config
                Map<String,LoadBalancerConfig> allLoadBalancerConfigMap =
                     getDomainRoot().getDomainConfig().getLoadBalancerConfigMap();


                for (String loadBalancerName : allLoadBalancerConfigMap.keySet()) {
                    LoadBalancerConfig loadBalancerConfig = 
                        allLoadBalancerConfigMap.get(loadBalancerName);
                    if (loadBalancerConfig.getLbConfigName().equals(lbConfigName))
                        relevantLoadBalancerConfigMap.put(
                            loadBalancerName, loadBalancerConfig);
                }
            }
            return relevantLoadBalancerConfigMap;
        }        

    }
    */
}