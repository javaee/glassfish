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
 * Configuration for the &lt;load-balancer&gt; element.
 */

package com.sun.enterprise.management.config;

import com.sun.enterprise.management.support.ObjectNames;
import java.util.Map;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;
import com.sun.enterprise.management.support.Delegate;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.config.LBConfig;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/*
LOAD_BALANCER_MONITORING
This code never belonged here.
Decouple and reimplement correctly elsewhere; config
item should *never* depend on monitoring ones.

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
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.config.LBConfig;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
  Configuration for the &lt;lb-config&gt; element.
*/
public final class LoadBalancerConfigImpl extends AMXConfigImplBase {
        
    public LoadBalancerConfigImpl(final Delegate delegate) {
        super(delegate);
    }
    
/*
LOAD_BALANCER_MONITORING
This code never belonged here.
Decouple and reimplement correctly elsewhere; config
item should *never* depend on monitoring ones.

    LoadBalancerDeregistrationHelper mHelper = null;
    String lbConfigName = null;

    protected void unregisterMisc() {
        try {
            LoadBalancerConfig lbc = ProxyFactory.getInstance(getMBeanServer())
                .getDomainRoot().getDomainConfig().getLoadBalancerConfigMap()
                .get(getName());
            LBConfig lbConfig = getDomainRoot().getDomainConfig()
                                .getLBConfigMap().get(lbConfigName);
            mHelper = new LoadBalancerDeregistrationHelper(
                getName(), lbConfig, getMBeanServer());
        } catch (Exception ex) {
            logWarning("LoadBalancerConfig unregisterMisc failed. " +
                    "LoadBalancerDeregistrationHelper creation failed");
        }                
    }

    public void postRegisterHook(Boolean registrationSucceeded) {
        super.postRegisterHook(registrationSucceeded);
        if (registrationSucceeded) {
            LoadBalancerConfig lbc = ProxyFactory.getInstance(getMBeanServer())
                .getDomainRoot().getDomainConfig().getLoadBalancerConfigMap()
                .get(getName());
            lbConfigName = lbc.getLbConfigName();
        }
    }    
    
    public void postDeregister() {
        super.postDeregister();
        try {            
            mHelper.unregisterMonitors();
            mHelper.unregisterRuntimeMBean();
            mHelper.removeLBConfigListener();
            mHelper = null;
        } catch (Exception ex) {
            logWarning("LoadBalancerConfig postDeregistration failed. " +
                    "Load Balancer Monitoring MBeans might be lying around\n" +
                    ExceptionUtil.toString(ex) );
        }                
    }

    private class LoadBalancerDeregistrationHelper {

        final static String J2EE_TYPE = "j2eeType";
        final ObjectNames objectNames = ObjectNames.getInstance(JMX_DOMAIN);    
        MBeanServer mbs = null;
        String loadBalancerName  = null;
        LBConfig lbConfig = null;
        
        public LoadBalancerDeregistrationHelper(
            String loadBalancerName, LBConfig lbConfig, MBeanServer mbs) {
            
            this.mbs = mbs;
            this.loadBalancerName = loadBalancerName;
            this.lbConfig = lbConfig;
        }

        void removeLBConfigListener() throws JMException {
            LBDeregistrationUtil.getInstance(mbs)
                .removeLBConfigListener(loadBalancerName, lbConfig);
        }
        
        void unregisterRuntimeMBean() throws JMException {
            LoadBalancer loadBalancer = ProxyFactory.getInstance(mbs)
                .getDomainRoot().getLoadBalancerMap().get(loadBalancerName);

            ObjectName lbObjName = Util.getObjectName(loadBalancer);
            mbs.unregisterMBean(lbObjName);
        }
        
        void unregisterMonitors() throws JMException {

            if (!lbConfig.getMonitoringEnabled()) return;
            
            LoadBalancerMonitor lbm = LBDeregistrationUtil.getInstance(mbs)
                .fetchLBMonitoringRoot(loadBalancerName);

           Map<String,LoadBalancerClusterMonitor> lbcmMap =
               lbm.getLoadBalancerClusterMonitorMap();
            
           for (LoadBalancerClusterMonitor lbcm : lbcmMap.values()) {
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
            
           ObjectName lbmObjName = Util.getObjectName(lbm);
           mbs.unregisterMBean(lbmObjName);
        }
    }
    */
}