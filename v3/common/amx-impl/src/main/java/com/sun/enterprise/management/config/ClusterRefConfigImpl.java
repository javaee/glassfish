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

package com.sun.enterprise.management.config;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.appserv.management.base.XTypes;
import com.sun.enterprise.management.config.AMXConfigImplBase;
import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.support.AMXAttributeNameMapper;

import static com.sun.appserv.management.base.XTypes.LOAD_BALANCER;
import static com.sun.appserv.management.base.AMX.J2EE_TYPE_KEY;
import static com.sun.appserv.management.base.AMX.NAME_KEY;
import static com.sun.appserv.management.base.AMX.JMX_DOMAIN;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMXDebug;
/*
LOAD_BALANCER_MONITORING
This code never belonged here; improper dependency
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
	Configuration for the &lt;cluster-ref&gt; element.
*/
public final class ClusterRefConfigImpl extends AMXConfigImplBase 
    implements ConfigFactoryCallback {
	//implements ClusterRefConfig 
	
    public ClusterRefConfigImpl(final Delegate delegate)
    {
        super(delegate);
    }

/*
LOAD_BALANCER_MONITORING
This code never belonged here.
Decouple and reimplement correctly elsewhere; config
item should *never* depend on monitoring ones.

    volatile ClusterRefDeregistrationHelper mHelper = null;

    protected void unregisterMisc() {
        try {
            mHelper = new ClusterRefDeregistrationHelper(
                            getName(), getMBeanServer());
        } catch (Exception ex) {
            logWarning("ClusterRefConfig unregisterMisc failed. " +
                    "ClusterRefDeregistrationHelper creation failed");
        }                
    }

    public void postDeregister() {
        super.postDeregister();
        try {            
            Map<String,LoadBalancerConfig> relevantLoadBalancerConfigMap = 
                new HashMap<String,LoadBalancerConfig>();

            if (getContainer() instanceof LBConfig) {
                String lbConfigName = ((LBConfig)getContainer()).getName();
                relevantLoadBalancerConfigMap = 
                    mHelper.fetchLoadBalancerConfigs((LBConfig)getContainer());
                mHelper.unregisterMonitors(relevantLoadBalancerConfigMap);
            } 
            mHelper = null;
        } catch (Exception ex) {
            logWarning("ClusterRefConfig postDeregistration failed. " +
                    "Load Balancer Monitoring MBeans might be lying around\n" +
                    ExceptionUtil.toString(ex) );
        }                
    }

    private class ClusterRefDeregistrationHelper {

        final static String J2EE_TYPE = "j2eeType";
        final ObjectNames objectNames = ObjectNames.getInstance(JMX_DOMAIN);    
        String clusterName = null;
        MBeanServer mbs = null;

        public ClusterRefDeregistrationHelper(String clusterName, MBeanServer mbs) {
            this.mbs = mbs;
            this.clusterName = clusterName;
        }

        Map<String, LoadBalancerConfig> fetchLoadBalancerConfigs(LBConfig lbConfig) {
            String lbConfigName = lbConfig.getName();
            Map<String,LoadBalancerConfig> allLoadBalancerConfigMap =
                 getDomainRoot().getDomainConfig().getLoadBalancerConfigMap();
            Map<String,LoadBalancerConfig> 
                 relevantLoadBalancerConfigMap = new HashMap<String,LoadBalancerConfig>();

            for (String loadBalancerName : allLoadBalancerConfigMap.keySet()) {
                LoadBalancerConfig loadBalancerConfig = 
                    allLoadBalancerConfigMap.get(loadBalancerName);
                if (loadBalancerConfig.getLbConfigName().equals(lbConfigName))
                    relevantLoadBalancerConfigMap.put(
                        loadBalancerName, loadBalancerConfig);
            }
            
            return relevantLoadBalancerConfigMap;
        }

        void unregisterMonitors(
            Map<String,LoadBalancerConfig>  relevantLoadBalancerConfigMap) 
            throws JMException {

            for (String loadBalancerName : relevantLoadBalancerConfigMap.keySet()) {
               LoadBalancerMonitor loadBalancerMonitor = 
                    LBDeregistrationUtil.getInstance(mbs)
                        .fetchLBMonitoringRoot(loadBalancerName);

               Map<String,LoadBalancerClusterMonitor> lbcmMap =
                   loadBalancerMonitor.getLoadBalancerClusterMonitorMap();

               LoadBalancerClusterMonitor lbcm = lbcmMap.get(clusterName);

               //loadBalancerServerMonitorMap will have only one element for every
               //standalone server as it is is wrapped up in a default cluster
               Map<String,LoadBalancerServerMonitor> lbsmMap =
                    lbcm.getLoadBalancerServerMonitorMap();

               for (LoadBalancerServerMonitor lbsm : lbsmMap.values()) {

                   Map<String,LoadBalancerApplicationMonitor> 
                       lbamMap = lbsm.getLoadBalancerApplicationMonitorMap();

                   for (LoadBalancerApplicationMonitor lbam : lbamMap.values()) {
                        Map<String,LoadBalancerContextRootMonitor> 
                            lbcrmMap = lbam.getLoadBalancerContextRootMonitorMap();
                       for (LoadBalancerContextRootMonitor lbcrm : lbcrmMap.values()) {
                            ObjectName lbcrmObjName = Util.getObjectName(lbcrm);
                            mbs.unregisterMBean(lbcrmObjName );
                        }

                        ObjectName lbamObjName = Util.getObjectName(lbam);
                        mbs.unregisterMBean(lbamObjName);
                    }

                    ObjectName loadBalancerServerMonitorObjName = 
                        Util.getObjectName(lbsm);
                        mbs.unregisterMBean(loadBalancerServerMonitorObjName);
                }

                ObjectName lbcmObjName = Util.getObjectName(lbcm);
                mbs.unregisterMBean(lbcmObjName);
            }
        }
    }
*/
}







