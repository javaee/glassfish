/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.ejb.monitoring.stats;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;
import com.sun.enterprise.admin.monitor.stats.MutableTimeStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableTimeStatisticImpl;

import com.sun.ejb.containers.EjbContainerUtilImpl;

/**
 * Event listener for the Ejb monitoring events. Used by the probe framework 
 * to collect and display the data.
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
//@AMXMetadata(type="ejb-application-mon", group="monitoring", isSingleton=false)
//@ManagedObject
//@Description("Ejb Application Statistics")
public class EjbMonitoringStatsProvider {

    private Map<Method, EjbMethodStatsProvider> methodMonitorMap = 
            new HashMap<Method, EjbMethodStatsProvider>();
    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;

    protected static final Logger _logger =
            EjbContainerUtilImpl.getInstance().getLogger();

    static final String NODE = "/";
    static final String SEP = "-";
    static final String APPLICATION_NODE = "applications" + NODE;
    static final String METHOD_NODE = NODE + "bean-methods" + NODE;
    static final String MONITORING_NODE = "ejb-container";

    public EjbMonitoringStatsProvider(String appName, String moduleName, 
            String beanName, Method[] methods) {
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;
        for (Method m : methods) {
            EjbMethodStatsProvider monitor = new EjbMethodStatsProvider(m);
            methodMonitorMap.put(m, monitor);
        }
    }

    public void register() {
        String beanSubTreeNode = /** APPLICATION_NODE + **/
                appName + NODE + moduleName + NODE + beanName;
        beanSubTreeNode = beanSubTreeNode.replaceAll("\\.", "\\\\.").
               replaceAll("_jar", "\\\\.jar").replaceAll("_war", "\\\\.war");

        _logger.info("BEAN NODE NAME: " + beanSubTreeNode);
        StatsProviderManager.register(MONITORING_NODE, 
                    PluginPoint.APPLICATIONS, beanSubTreeNode, this);

        for ( Method m : methodMonitorMap.keySet()) {
            String subTreeNode = beanSubTreeNode + METHOD_NODE + stringify(m); 
            _logger.info("METHOD NODE NAME: " + subTreeNode);

            EjbMethodStatsProvider monitor = methodMonitorMap.get(m);
            //StatsProviderManager.register(MONITORING_NODE, PluginPoint.APPLICATIONS, subTreeNode, monitor);
            //monitor.registered();
        }

        registered = true;
    }

    @ProbeListener("glassfish:ejb:ejb-monitoring:methodStartEvent")
    public void ejbMethodStartEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("method") Method method) {
        if (isValidEvent(appName, modName, ejbName)) {
            log ("ejbMethodStartEvent", method);
            EjbMethodStatsProvider monitor = methodMonitorMap.get(method);
            if (monitor != null) {
                monitor.methodStart();
            }
        }
    }

    @ProbeListener("glassfish:ejb:ejb-monitoring:methodEndEvent")
    public void ejbMethodEndEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("exception") Throwable exception,
            @ProbeParam("method") Method method) {
        if (isValidEvent(appName, modName, ejbName)) {
            log ("ejbMethodEndEvent", method);
            EjbMethodStatsProvider monitor = methodMonitorMap.get(method);
            if (monitor != null) {
                monitor.methodEnd((exception == null));
            }
        }
    }

    @ProbeListener("glassfish:ejb:ejb-monitoring:beanCreatedEvent")
    public void ejbBeanCreatedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidEvent(appName, modName, ejbName)) {
            log ("ejbBeanCreatedEvent");
        }
    }

    @ProbeListener("glassfish:ejb:ejb-monitoring:beanDestroyedEvent")
    public void ejbBeanDestroyedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidEvent(appName, modName, ejbName)) {
            log ("ejbBeanDestroyedEvent");
        }
    }

    @ProbeListener("glassfish:ejb:ejb-monitoring:beanActivatedEvent")
    public void ejbBeanActivatedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidEvent(appName, modName, ejbName)) {
            log ("ejbBeanActivatedEvent");
        }
    }

    @ProbeListener("glassfish:ejb:ejb-monitoring:beanPassivatedEvent")
    public void ejbBeanPassivatedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidEvent(appName, modName, ejbName)) {
            log ("ejbBeanPassivatedEvent");
        }
    }

    public void unregister() {
        if (registered) {
            for ( EjbMethodStatsProvider l : methodMonitorMap.values()) {
                if (l.isRegistered()) {
                    StatsProviderManager.unregister(l);
                }
            }
            StatsProviderManager.unregister(this);
        }
    }

    private boolean isValidEvent(String appName, String moduleName,
            String beanName) {
        if ((this.appName == null && appName != null)
                || (this.appName != null && !this.appName.equals(appName))) {
            return false;
        }
        if ((this.moduleName == null && moduleName != null)
                || (this.moduleName != null && !this.moduleName.equals(moduleName))) {
            return false;
        }
        if ((this.beanName == null && beanName != null)
                || (this.beanName != null && !this.beanName.equals(beanName))) {
            return false;
        }

        return true;
    }

    private void log(String mname) {
        _logger.info("===> In EjbMonitoringStatsProvider for: [" 
                + mname + "] " + appName + "::" + moduleName + "::" + beanName);
    }

    private void log(String mname, Method m) {
        _logger.info("===> In EjbMonitoringStatsProvider for: [" 
                + mname + "] " + appName + "::" + moduleName + "::" + beanName
                + "::" + stringify(m));
    }

    private String stringify(Method m) {
        StringBuffer sb = new StringBuffer();
        sb.append(m.getName());
        Class[] args = m.getParameterTypes();
        for (Class c : args) {
            sb.append(SEP).append(c.getName());
        }
        return sb.toString().replaceAll("\\.", "\\\\.").replaceAll("_", "\\\\.");
    }
}
