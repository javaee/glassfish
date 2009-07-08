/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
//import org.glassfish.api.event.Events;
//import org.glassfish.api.event.EventListener;
//import org.glassfish.api.event.EventListener.Event;
//import org.glassfish.api.event.EventTypes;
//import org.glassfish.api.event.RestrictTo;
import org.glassfish.probe.provider.PluginPoint;
import org.glassfish.probe.provider.StatsProviderManager;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.Startup;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service//(name="jvm")
//@Scoped(Singleton.class)
public class JVMStatsProviderBootstrap implements Startup,/*TelemetryProvider,*/ PostConstruct/*, EventListener */{

    //@Inject
    //Logger logger;

    //@Inject Events events;

    private JVMClassLoadingStatsProvider clStatsProvider = new JVMClassLoadingStatsProvider();
    private JVMCompilationStatsProvider compileStatsProvider = new JVMCompilationStatsProvider();
    private JVMMemoryStatsProvider memoryStatsProvider = new JVMMemoryStatsProvider();
    private JVMOSStatsProvider osStatsProvider = new JVMOSStatsProvider();
    private JVMRuntimeStatsProvider runtimeStatsProvider = new JVMRuntimeStatsProvider();
    private List<JVMGCStatsProvider> jvmStatsProviderList = new ArrayList();

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

        //events.register(this);
    }

    public Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

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
