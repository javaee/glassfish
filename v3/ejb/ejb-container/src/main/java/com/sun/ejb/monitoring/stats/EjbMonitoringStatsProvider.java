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

import com.sun.ejb.containers.EjbContainerUtilImpl;

/**
 * Event listener for the Ejb monitoring events. Used by the probe framework 
 * to collect and display the data.
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
@AMXMetadata(type="ejb-application-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Ejb Application Statistics")
public class EjbMonitoringStatsProvider {

    private Map<String, EjbMethodStatsProvider> methodMonitorMap = 
            new HashMap<String, EjbMethodStatsProvider>();

    String appName = null;
    String moduleName = null;
    String beanName = null;
    boolean registered = false;

    private CountStatisticImpl createStat = new CountStatisticImpl("CreateCount", 
            "count", "Number of times EJB create method is called");

    private CountStatisticImpl removeStat = new CountStatisticImpl("RemoveCount", 
            "count", "Number of times EJB remove method is called");

    static final Logger _logger = EjbContainerUtilImpl.getInstance().getLogger();

    public EjbMonitoringStatsProvider(String appName, String moduleName, 
            String beanName) {
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;
    }

    public void addMethods(String appName, String moduleName, 
            String beanName, Method[] methods) {
        if (isValidRequest(appName, moduleName, beanName)) {
            for (Method m : methods) {
                String mname = EjbMonitoringUtils.stringify(m);
                if (methodMonitorMap.get(mname) == null) {
                    // Use 1 monitor for all methods that represent the same method in the bean
                    EjbMethodStatsProvider monitor = new EjbMethodStatsProvider(mname);
                    methodMonitorMap.put(mname, monitor);
                }
            }
        }
    }

    public void register() {
        String beanSubTreeNode = EjbMonitoringUtils.registerComponent(
                appName, moduleName, beanName, this);
        if ( beanSubTreeNode != null) {
            registered = true;
            for ( String mname : methodMonitorMap.keySet()) {
                EjbMethodStatsProvider monitor = methodMonitorMap.get(mname);
                String node = EjbMonitoringUtils.registerMethod(beanSubTreeNode, mname, monitor);
                if (node != null) {
                    monitor.registered();
                }
            }
        }
    }

    public void unregister() {
        if (registered) {
            registered = false;
            StatsProviderManager.unregister(this);
            for ( EjbMethodStatsProvider monitor : methodMonitorMap.values()) {
                if (monitor.isRegistered()) {
                    monitor.unregistered();
                    StatsProviderManager.unregister(monitor);
                }
            }
        }
    }

    @ProbeListener("glassfish:ejb:bean:methodStartEvent")
    public void ejbMethodStartEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("method") Method method) {
        if (isValidRequest(appName, modName, ejbName)) {
            log ("ejbMethodStartEvent", method);
            EjbMethodStatsProvider monitor = methodMonitorMap.get(
                    EjbMonitoringUtils.stringify(method));
            if (monitor != null) {
                monitor.methodStart();
            }
        }
    }

    @ProbeListener("glassfish:ejb:bean:methodEndEvent")
    public void ejbMethodEndEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("exception") Throwable exception,
            @ProbeParam("method") Method method) {
        if (isValidRequest(appName, modName, ejbName)) {
            log ("ejbMethodEndEvent", method);
            EjbMethodStatsProvider monitor = methodMonitorMap.get(
                   EjbMonitoringUtils.stringify(method));
            if (monitor != null) {
                monitor.methodEnd((exception == null));
            }
        }
    }

    @ProbeListener("glassfish:ejb:bean:beanCreatedEvent")
    public void ejbBeanCreatedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            log ("ejbBeanCreatedEvent");
            createStat.increment();
        }
    }

    @ProbeListener("glassfish:ejb:bean:beanDestroyedEvent")
    public void ejbBeanDestroyedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            log ("ejbBeanDestroyedEvent");
            removeStat.increment();
        }
    }

    @ProbeListener("glassfish:ejb:bean:beanActivatedEvent")
    public void ejbBeanActivatedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            log ("ejbBeanActivatedEvent");
        }
    }

    @ProbeListener("glassfish:ejb:bean:beanPassivatedEvent")
    public void ejbBeanPassivatedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            log ("ejbBeanPassivatedEvent");
        }
    }

    @ManagedAttribute(id="createcount")
    @Description( "Number of times EJB create method is called")
    public CountStatistic getCreateCount() {
        return createStat.getStatistic();
    }

    @ManagedAttribute(id="removecount")
    @Description( "Number of times EJB remove method is called")
    public CountStatistic getRemoveCount() {
        return removeStat.getStatistic();
    }

    boolean isValidRequest(String appName, String moduleName,
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

    void log(String mname, String provider) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("===> In " + provider + " for: [" 
                    + mname + "] " + appName + "::" + moduleName + "::" + beanName);
        }
    }

    private void log(String mname) {
        log(mname, "EjbMonitoringStatsProvider");
    }

    private void log(String mname, Method m) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("===> In EjbMonitoringStatsProvider for: [" 
                    + mname + "] " + appName + "::" + moduleName + "::" + beanName
                    + "::" + EjbMonitoringUtils.stringify(m));
        }
    }

}
