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
*/

package com.sun.enterprise.management.config;

import com.sun.enterprise.management.support.ObjectNames;
import javax.management.ObjectName;

import com.sun.enterprise.management.config.AMXConfigImplBase;
import com.sun.enterprise.management.support.Delegate;
	
import com.sun.enterprise.management.support.AMXAttributeNameMapper;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import static com.sun.appserv.management.base.XTypes.LOAD_BALANCER;
import static com.sun.appserv.management.base.AMX.J2EE_TYPE_KEY;
import static com.sun.appserv.management.base.AMX.NAME_KEY;
import static com.sun.appserv.management.base.AMX.JMX_DOMAIN;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMXDebug;
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
	Configuration for the &lt;application-ref&gt; element
	found within a &lt;server&gt;
*/
public final class DeployedItemRefConfigImpl  extends AMXConfigImplBase
{
		public
	DeployedItemRefConfigImpl( final Delegate delegate )
	{
		super(  delegate );
	}
	
/*
LOAD_BALANCER_MONITORING
See duplicated code in ClusterRefConfigImpl, ServerRefConfigImpl, 
DeployedItemRefConfigImpl.
This code never belonged here.
Decouple and reimplement correctly elsewhere; config
item should *never* depend on monitoring ones.

        DeployedItemRefDeRegistrationHelper mHelper = null;
        
        protected void unregisterMisc() {
            try {
                mHelper = new DeployedItemRefDeRegistrationHelper(
                                getName(), getMBeanServer());
            } catch (Exception ex) {
                logWarning("DeployedItemRefConfig unregisterMisc failed. " +
                    "DeployedItemRefDeregistrationHelper creation failed");            
            }                
        }

        public void postDeregister() {
            super.postDeregister();
            try {            
                
                Map<String,LoadBalancerConfig> relevantLoadBalancerConfigMap = 
                    new HashMap<String,LoadBalancerConfig>();
                if (getContainer() instanceof ServerConfig) {
                    relevantLoadBalancerConfigMap = 
                        mHelper.fetchLoadBalancerConfigs((ServerConfig)getContainer());
                    mHelper.unregisterMonitors(relevantLoadBalancerConfigMap);
                } else if (getContainer() instanceof ClusterConfig) {
                    relevantLoadBalancerConfigMap = 
                        mHelper.fetchLoadBalancerConfigs((ClusterConfig)getContainer());
                    mHelper.unregisterMonitors(relevantLoadBalancerConfigMap);
                }
                mHelper = null;
             } catch (Exception ex) {
                logWarning("DeployedItemRef postDeregistration failed. " +
                    "Load Balancer Monitoring MBeans might be lying around " +
                    "if this application is being load balanced\n" +
                    ExceptionUtil.toString(ex) );
             }                
        }

        private class DeployedItemRefDeRegistrationHelper {

            final static String J2EE_TYPE = "j2eeType";
            final ObjectNames objectNames = ObjectNames.getInstance(JMX_DOMAIN);    
            String appName = null;
            MBeanServer mbs = null;
            
            public DeployedItemRefDeRegistrationHelper(String appName, MBeanServer mbs) {
                this.mbs = mbs;
                this.appName = appName;
            }

            void unregisterMonitors(
                Map<String,LoadBalancerConfig>  relevantLoadBalancerConfigMap) 
                throws JMException {

                for (String loadBalancerName : relevantLoadBalancerConfigMap.keySet()) {
                   LoadBalancerConfig loadBalancerConfig = 
                       relevantLoadBalancerConfigMap.get(loadBalancerName);
                   LoadBalancerMonitor loadBalancerMonitor = 
                       LBDeregistrationUtil.getInstance(mbs)
                           .fetchLBMonitoringRoot(loadBalancerName);
                   Collection<LoadBalancerApplicationMonitor> lbamList =
                       fetchLoadBalancerApplicationMonitors(appName, loadBalancerMonitor);
                   for (LoadBalancerApplicationMonitor lbam : lbamList) {
                        Map<String,LoadBalancerContextRootMonitor> 
                            lbcrmm = lbam.getLoadBalancerContextRootMonitorMap();
                       for (String ctxRootName : lbcrmm.keySet()) {
                            LoadBalancerContextRootMonitor loadBalancerContextRootMonitor = 
                                lbcrmm.get(ctxRootName);
                            ObjectName loadBalancerContextRootMonitorObjName = 
                                Util.getObjectName(loadBalancerContextRootMonitor);
                            mbs.unregisterMBean(loadBalancerContextRootMonitorObjName);
                        }
                        ObjectName lbamObjName = Util.getObjectName(lbam);
                        mbs.unregisterMBean(lbamObjName);
                    }
                }
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

            Map<String,LoadBalancerConfig> fetchLoadBalancerConfigs(
                ServerConfig serverConfig) {

                Map<String, LBConfig> relevantLBConfigMap = new HashMap<String, LBConfig>();

                String serverName = serverConfig.getName();

                //get all <lb-config> elements in the domain
                Map<String,LBConfig> allLBConfigMap =
                     getDomainRoot().getDomainConfig().getLBConfigMap();
                //get all <cluster> elements in the domain
                Map<String,ClusterConfig> allClusterConfigMap =
                     getDomainRoot().getDomainConfig().getClusterConfigMap();

                for (String lbConfigName : allLBConfigMap.keySet()) { 
                    LBConfig lbConfig = allLBConfigMap.get(lbConfigName);

                    Map<String,ClusterConfig> 
                        relevantClusterConfigMap = new HashMap<String,ClusterConfig>();

                    Map<String,ClusterRefConfig> clusterRefConfigMap = 
                        lbConfig.getClusterRefConfigMap();
                    for (String clusterName : clusterRefConfigMap.keySet()) 
                        relevantClusterConfigMap.put(
                            clusterName, allClusterConfigMap.get(clusterName));

                    for (ClusterConfig clusterConfig : 
                            relevantClusterConfigMap.values()) { 
                        Map<String,ServerRefConfig> serverRefConfigMap = 
                            clusterConfig.getServerRefConfigMap();
                        for (String serverRef : serverRefConfigMap.keySet()) { 
                            if (serverRef.equals(serverName)) {
                                relevantLBConfigMap.put(lbConfigName, lbConfig);
                                break;
                            }
                        }
                    }

                    Map<String,ServerRefConfig> serverRefConfigMap = 
                        lbConfig.getServerRefConfigMap();
                    for (String serverRef : serverRefConfigMap.keySet()) { 
                        if (serverRef.equals(serverName)) {
                            relevantLBConfigMap.put(lbConfigName, lbConfig);
                            break;
                        }
                    }
                }

                return fetchLoadBalancerConfigs(relevantLBConfigMap);
            }

            private Collection<LoadBalancerApplicationMonitor> 
                fetchLoadBalancerApplicationMonitors(
                    String ipAppName, LoadBalancerMonitor loadBalancerMonitor) {

                Collection<LoadBalancerApplicationMonitor> result = new ArrayList<LoadBalancerApplicationMonitor>();
                Map<String,LoadBalancerClusterMonitor> lbcmMap =
                        loadBalancerMonitor.getLoadBalancerClusterMonitorMap();
                for (String clusterName : lbcmMap.keySet()) {
                    LoadBalancerClusterMonitor lbcm = lbcmMap.get(clusterName);
                    Map<String,LoadBalancerServerMonitor> lbsmMap =
                        lbcm.getLoadBalancerServerMonitorMap();
                    for (String serverName : lbsmMap.keySet()) {
                        LoadBalancerServerMonitor lbsm = lbsmMap.get(serverName);
                        Map<String, LoadBalancerApplicationMonitor> lbamMap =
                            lbsm.getLoadBalancerApplicationMonitorMap();
                        if (lbamMap.get(ipAppName) != null) 
                            result.add(lbamMap.get(ipAppName));
                    }
                }
                return result;
            }

            private Map<String,LoadBalancerConfig> fetchLoadBalancerConfigs(
                LBConfig lbConfig) {
                Map<String, LBConfig> lbConfigMap = new HashMap<String, LBConfig>();
                lbConfigMap.put(lbConfig.getName(), lbConfig);
                return fetchLoadBalancerConfigs(lbConfigMap);
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