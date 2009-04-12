/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admin.monitor.telemetry;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.monitoring.TelemetryProvider;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.provider.ProbeProviderEventManager;
import org.glassfish.flashlight.provider.ProbeProviderListener;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name="http-service")
@Scoped(Singleton.class)
public class HttpServiceTelemetryBootstrap implements ProbeProviderListener, 
                                            TelemetryProvider, PostConstruct {

    @Inject
    Logger logger;
    @Inject
    private static Domain domain;
    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private ProbeProviderEventManager ppem;
    @Inject
    private ProbeClientMediator pcm;

    private boolean httpServiceMonitoringEnabled = false;
    private TreeNode serverNode = null;
    private boolean requestProviderRegistered = false;
    private boolean isRequestTreeBuilt = false;
    private boolean probeProviderListenerRegistered = false;;
    private TreeNode httpServiceNode =  null;
    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;
    private List<HttpServiceRequestTelemetry> vsRequestTMs = null;
    //private boolean threadPoolProviderRegistered = false;
    //private boolean isThreadPoolTreeBuilt = false;
    //private TreeNode threadPoolNode =  null;
    //private List<ThreadPoolTelemetry> threadPoolTMs = null;
    
    public HttpServiceTelemetryBootstrap() {
    }

    public void postConstruct(){
        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        Level dbgLevel = Level.FINEST;
        Level defaultLevel = logger.getLevel();
        if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            //logger.setLevel(dbgLevel);
        }
        logger.finest("[Monitor]In the HttpServiceRequestTelemetry bootstrap ************");

        List<Config> lc = domain.getConfigs().getConfig();
        Config config = null;
        for (Config cf : lc) {
            if (cf.getName().equals("server-config")) {
                config = cf;
                break;
            }
        }
        httpService = config.getHttpService();
        networkConfig = config.getNetworkConfig();
    }
    
    public void onLevelChange(String newLevel) {
        boolean newLevelEnabledValue = getEnabledValue(newLevel);
        logger.finest("[Monitor]In the Http Service Level Change = " + newLevel + "  ************");
        if (httpServiceMonitoringEnabled != newLevelEnabledValue) {
            httpServiceMonitoringEnabled = newLevelEnabledValue;
        } else {
            // Might have changed from 'LOW' to 'HIGH' or vice-versa. Ignore.
            return;
        }
        //check if the monitoring level for web-container is 'on' and 
        // if Web Container is loaded, then register the ProveProviderListener
        if (httpServiceMonitoringEnabled) { 
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
              //    whether the telemetry objects are created is done in enableHttpServiceMon..()
              // (2)Could be that the telemetry objects are there but were disabled 
              //    explicitly by user. Now we need to enable them
                enableHttpServiceMonitoring(true);
            }
        } else { 
            //enable flag turned from 'ON' to 'OFF', so disable telemetry
            enableHttpServiceMonitoring(false);
        }
    }

    public void providerRegistered(String moduleName, String providerName, String appName) {
        try {
            
            logger.finest("[Monitor]Provider registered event received - providerName = " + 
                                providerName + " : module name = " + moduleName + 
                                " : appName = " + appName);
            if (providerName.equals("request")){
                logger.finest("[Monitor]and it is Http Request");
                requestProviderRegistered = true;
                if (isRequestTreeBuilt || !httpServiceMonitoringEnabled) {
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildRequestMonitoringTree();
            }
            /*
            if (providerName.equals("threadpool")){
                logger.finest("[Monitor]and it is Thread Pool");
                threadPoolProviderRegistered = true;
                if (isThreadPoolTreeBuilt || !httpServiceMonitoringEnabled) {
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildThreadPoolMonitoringTree();
            }
            */
        }catch (Exception e) {
            //Never throw an exception as the Web container startup will have a problem
            //Show warning
            logger.finest("[Monitor]WARNING: Exception in HttpService Monitor Startup : " + 
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
            httpServiceNode = serverNode.getNode("http-service");
            if (httpServiceNode == null){
                // http-service
                httpServiceNode = TreeNodeFactory.createTreeNode("http-service", null, "http-service");
                serverNode.addChild(httpServiceNode);
            }
            return;
        }

        serverNode = TreeNodeFactory.createTreeNode("server", null, "server");
        mrdr.add("server", serverNode);
        // http-service
        httpServiceNode = TreeNodeFactory.createTreeNode("http-service", null, "http-service");
        serverNode.addChild(httpServiceNode);
    }

    //builds the thread pool sub nodes
    private void buildRequestMonitoringTree() {
        if (isRequestTreeBuilt || !httpServiceMonitoringEnabled)
            return;
        //Build the top level monitoring tree
        buildTopLevelMonitoringTree();        

        logger.finest("[Monitor]Http Service Monitoring tree is being built");
        //http-service sub-nodes
        vsRequestTMs = new ArrayList<HttpServiceRequestTelemetry>();
        
        for (VirtualServer vs : httpService.getVirtualServer()) {
            TreeNode vsNode = TreeNodeFactory.createTreeNode(vs.getId(), null, "virtual-server");
            httpServiceNode.addChild(vsNode);
            TreeNode requestNode = TreeNodeFactory.createTreeNode("request", null, "http-service");
            vsNode.addChild(requestNode);
            HttpServiceRequestTelemetry vsRequestTM = 
                    new HttpServiceRequestTelemetry(requestNode, vs.getId(), logger);
            Collection<ProbeClientMethodHandle> handles = 
                                        pcm.registerListener(vsRequestTM);
            vsRequestTM.setProbeListenerHandles(handles);
            vsRequestTMs.add(vsRequestTM);
        }
        
        isRequestTreeBuilt = true;
    }

    //builds the thread pool sub nodes
    /*
    private void buildThreadPoolMonitoringTree() {
        if (isThreadPoolTreeBuilt || !httpServiceMonitoringEnabled)
            return;
        //Build the top level monitoring tree
        buildTopLevelMonitoringTree();        

        //thread-pool
        threadPoolNode = TreeNodeFactory.createTreeNode("thread-pool", null, "http-service");
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
    */
    
    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }

    private void enableHttpServiceMonitoring(boolean isEnabled) {
        //Enable/Disable thread-pool telemetry
        httpServiceNode.setEnabled(isEnabled);
        
        if (requestProviderRegistered){
            if (vsRequestTMs != null) {
                for (HttpServiceRequestTelemetry requestTM : vsRequestTMs) {
                    requestTM.enableMonitoring(isEnabled);
                }
            }
        }
        /*
        if (threadPoolProviderRegistered) {
            //Enable/Disable thread-pool telemetry
            threadPoolNode.setEnabled(isEnabled);
            if (threadPoolTMs != null) {
                for (ThreadPoolTelemetry threadPoolTM : threadPoolTMs)
                    threadPoolTM.enableMonitoring(isEnabled);
            }
        }
        */
    }

    public static String getVirtualServer(String hostName, String listenerPort) {
        try {
            //
            if (hostName == null) {
                return null;
            }
            if (hostName.equals("localhost")) {
                hostName = InetAddress.getLocalHost().getHostName();
            }
            NetworkListener listener = null;

            for (NetworkListener hl : networkConfig.getNetworkListeners().getNetworkListener()) {
                if (hl.getPort().equals(listenerPort)) {
                    listener = hl;
                    break;
                }
            }
            VirtualServer virtualServer = null;
            for (VirtualServer vs : httpService.getVirtualServer()) {
                if (vs.getHosts().contains(hostName)
                    && vs.getNetworkListeners().contains(listener.getName())) {
                    virtualServer = vs;
                }
            }
            return virtualServer.getId();
        } catch (UnknownHostException ex) {
            Logger.getLogger(HttpServiceTelemetryBootstrap.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
