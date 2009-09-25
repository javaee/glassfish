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

package org.glassfish.admin.monitor.jvm;

//import com.sun.enterprise.config.serverbeans.MonitoringService;
//import com.sun.logging.LogDomains;
//import java.beans.PropertyVetoException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.glassfish.admin.monitor.jvm.config.JvmMI;
//import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
//import org.glassfish.api.event.Events;
//import org.glassfish.api.event.EventListener;
//import org.glassfish.api.event.EventListener.Event;
//import org.glassfish.api.event.EventTypes;
//import org.glassfish.api.event.RestrictTo;
//import org.glassfish.api.monitoring.MonitoringItem;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
//import org.jvnet.hk2.config.ConfigSupport;
//import org.jvnet.hk2.config.SingleConfigCode;
//import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.Startup;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service//(name="jvm")
//@Scoped(Singleton.class)
public class JVMStatsProviderBootstrap implements Startup,/*TelemetryProvider,*/ PostConstruct/*, EventListener */{

    //@Inject Events events;

    //@Inject
    //MonitoringService monitoringService;

    //protected static final Logger _logger = LogDomains.getLogger(
    //        JVMStatsProviderBootstrap.class, LogDomains.MONITORING_LOGGER);

    private JVMClassLoadingStatsProvider clStatsProvider = new JVMClassLoadingStatsProvider();
    private JVMCompilationStatsProvider compileStatsProvider = new JVMCompilationStatsProvider();
    private JVMMemoryStatsProvider memoryStatsProvider = new JVMMemoryStatsProvider();
    private JVMOSStatsProvider osStatsProvider = new JVMOSStatsProvider();
    private JVMRuntimeStatsProvider runtimeStatsProvider = new JVMRuntimeStatsProvider();
    private JVMThreadSystemStatsProvider threadSysStatsProvider = new JVMThreadSystemStatsProvider();
    private List<JVMGCStatsProvider> jvmStatsProviderList = new ArrayList();
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    public static final String JVM = "jvm";

    public void postConstruct(){
        //System.out.println (" In JVMStatsProviderBootstrap.PostConstruct ********");

        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        //Level dbgLevel = Level.FINEST;
        //Level defaultLevel = logger.getLevel();
        //if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            //logger.setLevel(dbgLevel);
        //}
        //logger.finest("[Monitor]In the JVMTelemetry bootstrap ************");
        //Build the top level monitoring tree   
        //buildTopLevelMonitoringTree();

        //createMonitoringConfig();

        /* register with monitoring */
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/class-loading-system", clStatsProvider);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/compilation-system", compileStatsProvider);
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            JVMGCStatsProvider jvmStatsProvider = new JVMGCStatsProvider(gc.getName());
            jvmStatsProviderList.add(jvmStatsProvider);
            StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/garbage-collectors/"+gc.getName(), jvmStatsProvider);
        }
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/memory", memoryStatsProvider);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/operating-system", osStatsProvider);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/runtime", runtimeStatsProvider);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/thread-system", threadSysStatsProvider);
        for (ThreadInfo t : threadBean.getThreadInfo(threadBean.getAllThreadIds(), 5)) {
            JVMThreadInfoStatsProvider threadInfoStatsProvider = new JVMThreadInfoStatsProvider(t);
            StatsProviderManager.register("jvm", PluginPoint.SERVER, 
                    "jvm/thread-system/thread-"+t.getThreadId(), threadInfoStatsProvider);
        }

        //events.register(this);
    }

    public Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

    /**
     * Creates jvm config element for monitoring.
     *
     * Check if the jvm monitoring config has been created.
     * If it has not, then add it.
     */
    /*private void createMonitoringConfig() {
        if (monitoringService == null) {
            _logger.log(Level.SEVERE, "monitoringService is null. jvm monitoring config not created");
            return;
        }
        List<MonitoringItem> itemList = monitoringService.getMonitoringItems();
        boolean hasMonitorConfig = false;
        for (MonitoringItem mi : itemList) {
            if (mi.getName().equals(JVM)) {
                hasMonitorConfig = true;
            }
        }

        try {
            if (!hasMonitorConfig) {
                ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {

                    public Object run(MonitoringService param) throws PropertyVetoException, TransactionFailure {

                        MonitoringItem newItem = param.createChild(JvmMI.class);
                        newItem.setName(JVM);
                        newItem.setLevel(MonitoringItem.LEVEL_OFF);
                        param.getMonitoringItems().add(newItem);
                        return newItem;
                    }
                }, monitoringService);
            }
        } catch (TransactionFailure tfe) {
            _logger.log(Level.SEVERE, "Exception adding jvm MonitoringItem", tfe);
        }
    }*/

    /*public void event(Event event) {
        if (event.name().equals(EventTypes.PREPARE_SHUTDOWN_NAME)) {
            StatsProviderManager.unregister(this.clStatsProvider);
            StatsProviderManager.unregister(this.compileStatsProvider);
            StatsProviderManager.unregister(this.memoryStatsProvider);
            StatsProviderManager.unregister(this.osStatsProvider);
            StatsProviderManager.unregister(this.runtimeStatsProvider);
            for (JVMGCStatsProvider gc : this.jvmStatsProviderList) {
                StatsProviderManager.unregister(gc);
            }
        }
    }*/

    /*public void onLevelChange(String newLevel) {
        boolean newLevelEnabledValue = getEnabledValue(newLevel);
        logger.finest("[Monitor]In the Level Change = " + newLevel + "  ************");
        if (jvmMonitoringEnabled != newLevelEnabledValue) {
            jvmMonitoringEnabled = newLevelEnabledValue;
        } else {
            // Might have changed from 'LOW' to 'HIGH' or vice-versa. Ignore.
            return;
        }
        //check if the monitoring level for web-container is 'on' and 
        // if Web Container is loaded, then register the ProveProviderListener
        if (jvmMonitoringEnabled) { 
            // enable flag turned from 'OFF' to 'ON'
              // (1)Could be that the telemetry object is not built
              // (2)Could be that the telemetry object is there but is disabled 
              //    explicitly by user. Now we need to enable them
            if (jvmTM != null)
                enableJVMMonitoring(true);
            else
                buildJVMTelemetry();
        } else { 
            //enable flag turned from 'ON' to 'OFF', so disable telemetry
            enableJVMMonitoring(false);
        }
    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }

    //builds the top level tree
    private void buildTopLevelMonitoringTree() {
        //check if serverNode exists
        if (serverNode != null)
            return;
        if (mrdr.get("server") != null) {
            serverNode = mrdr.get("server");
            return;
        }
        // server
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        serverNode = TreeNodeFactory.createTreeNode("server", null, "server");
        mrdr.add("server", serverNode);
    }

    private void buildJVMTelemetry() {
        if (jvmTM == null) {
            jvmTM = new JVMStatsProvider(serverNode, logger);
        }
    }

    private void enableJVMMonitoring(boolean isEnabled) {
        //Enable/Disable jvm telemetry
        //Delete jvmTM if the enabled flag is turned from 'ON' to 'OFF'
        if (!isEnabled) { //asking to disable
            jvmTM = null;
            if (serverNode != null) {
                TreeNode jvmNode = serverNode.getNode("jvm");
		if (jvmNode != null)
                    serverNode.removeChild(serverNode.getNode("jvm"));
            }
        } else { // asking to enable
            buildJVMTelemetry();
        }
    }*/
}
