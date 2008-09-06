/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor.threadpool.telemetry;

import org.glassfish.admin.monitor.jvm.telemetry.*;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.api.monitoring.TelemetryProvider;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.provider.ProbeProviderListener;
import org.glassfish.flashlight.provider.ProbeProviderEventManager;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.jvnet.hk2.component.PostConstruct;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name="thread-pool")
@Scoped(Singleton.class)
public class ThreadPoolTelemetryBootstrap implements ProbeProviderListener, 
                                            TelemetryProvider, PostConstruct {

    @Inject
    Logger logger;
    @Inject
    private Domain domain;
    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private ProbeProviderEventManager ppem;
    @Inject
    private ProbeClientMediator pcm;

    private boolean threadPoolMonitoringEnabled = false;
    private TreeNode serverNode = null;
    private boolean threadPoolProviderRegistered = false;
    private boolean isThreadPoolTreeBuilt = false;
    private boolean probeProviderListenerRegistered = false;;
    private List<ThreadPoolTelemetry> threadPoolTMs = null;
    
    public ThreadPoolTelemetryBootstrap() {
    }

    public void postConstruct(){
        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        Level dbgLevel = Level.FINEST;
        Level defaultLevel = logger.getLevel();
        if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            logger.setLevel(dbgLevel);
        }
        logger.finest("[Monitor]In the ThreadPoolTelemetry bootstrap ************");

        //Build the top level monitoring tree
        buildTopLevelMonitoringTree();        
    }
    
    public void onLevelChange(String newLevel) {
        boolean newLevelEnabledValue = getEnabledValue(newLevel);
        logger.finest("[Monitor]In the Thread pool Level Change = " + newLevel + "  ************");
        if (threadPoolMonitoringEnabled != newLevelEnabledValue) {
            threadPoolMonitoringEnabled = newLevelEnabledValue;
        } else {
            // Might have changed from 'LOW' to 'HIGH' or vice-versa. Ignore.
            return;
        }
        //check if the monitoring level for web-container is 'on' and 
        // if Web Container is loaded, then register the ProveProviderListener
        if (threadPoolMonitoringEnabled) { 
            // enable flag turned from 'OFF' to 'ON'
              // (1)Could be that the telemetry object is not built
              // (2)Could be that the telemetry object is there but is disabled 
              //    explicitly by user. Now we need to enable them
            // enable flag turned from 'OFF' to 'ON'
            if (!probeProviderListenerRegistered) { 
                //Came the very first time into this method
                registerProbeProviderListener();
            } else { 
              //probeProvider is already registered, 
              // (1)Could be that the telemetry objects are not built, I dont care since
              //    for sure the ProbeProviderListener didn't fire any events. The check
              //    whether the telemetry objects are created is done in enableThreadPoolMon..()
              // (2)Could be that the telemetry objects are there but were disabled 
              //    explicitly by user. Now we need to enable them
                if (threadPoolProviderRegistered)
                    enableThreadPoolMonitoring(true);
            }
        } else { 
            //enable flag turned from 'ON' to 'OFF', so disable telemetry
            enableThreadPoolMonitoring(false);
        }
    }

    public void providerRegistered(String moduleName, String providerName, String appName) {
        try {
            
            logger.finest("[Monitor]Provider registered event received - providerName = " + 
                                providerName + " : module name = " + moduleName + 
                                " : appName = " + appName);
            if (providerName.equals("threadpool")){
                logger.finest("[Monitor]and it is Thread Pool");
                threadPoolProviderRegistered = true;
                if (isThreadPoolTreeBuilt || !threadPoolMonitoringEnabled) {
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildThreadPoolMonitoringTree();
            }
        }catch (Exception e) {
            //Never throw an exception as the Web container startup will have a problem
            //Show warning
            logger.finest("[Monitor]WARNING: Exception in WebMonitorStartup : " + 
                                    e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void providerUnregistered(String moduleName, String providerName, String appName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void registerProbeProviderListener() {
       // ppem.registerProbeProviderListener should be called only after
        // buildWebMonitoringConfigTree is invoked because of dependency???
        ppem.registerProbeProviderListener(this);
        probeProviderListenerRegistered = true;
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

    //builds the thread pool sub nodes
    private void buildThreadPoolMonitoringTree() {
        if (isThreadPoolTreeBuilt || !threadPoolMonitoringEnabled)
            return;
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        logger.finest("[Monitor]Thread Pool Monitoring tree is being built");
        // http-service
        TreeNode httpServiceNode = TreeNodeFactory.createTreeNode("http-service", null, "http-service");
        serverNode.addChild(httpServiceNode);
        //thread-pool
        TreeNode threadPoolNode = TreeNodeFactory.createTreeNode("thread-pool", null, "http-service");
        httpServiceNode.addChild(threadPoolNode);
        threadPoolTMs = new ArrayList<ThreadPoolTelemetry>();
        for (Config config : domain.getConfigs().getConfig()) {
            if (config.getName().equals("server-config")) {
                for (ThreadPool tp : config.getThreadPools().getThreadPool()) {
                    String id = tp.getThreadPoolId();
                    String maxTPSize = tp.getMaxThreadPoolSize();
                    //Create tree node
                    TreeNode tpNode = TreeNodeFactory.createTreeNode(id, null, "http-service");
                    threadPoolNode.addChild(tpNode);
                    ThreadPoolTelemetry threadPoolTM = 
                            new ThreadPoolTelemetry(tpNode, id, maxTPSize, logger);
                    Collection<ProbeClientMethodHandle> handles = 
                                        pcm.registerListener(threadPoolTM);
                    threadPoolTM.setProbeListenerHandles(handles);
                    threadPoolTMs.add(threadPoolTM);
                }
            }
        }
        isThreadPoolTreeBuilt = true;
    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }

    private void enableThreadPoolMonitoring(boolean isEnabled) {
        //Enable/Disable jvm telemetry
        if (threadPoolTMs != null) {
            for (ThreadPoolTelemetry threadPoolTM : threadPoolTMs)
                threadPoolTM.enableMonitoring(isEnabled);
        }
    }

}
