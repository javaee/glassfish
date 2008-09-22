/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor.httpservice.telemetry;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.InetAddress;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import com.sun.enterprise.config.serverbeans.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
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
    private boolean httpServiceProviderRegistered = false;
    private boolean isHttpServiceTreeBuilt = false;
    private boolean probeProviderListenerRegistered = false;;
    private TreeNode httpServiceNode =  null;
    private static HttpService httpService = null;
    private List<HttpServiceRequestTelemetry> vsRequestTMs = null;
    
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

        //Build the top level monitoring tree
        buildTopLevelMonitoringTree();        

        List<Config> lc = domain.getConfigs().getConfig();
        Config config = null;
        for (Config cf : lc) {
            if (cf.getName().equals("server-config")) {
                config = cf;
                break;
            }
        }
        httpService = config.getHttpService();
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
                if (httpServiceProviderRegistered)
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
                httpServiceProviderRegistered = true;
                if (isHttpServiceTreeBuilt || !httpServiceMonitoringEnabled) {
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildHttpServiceMonitoringTree();
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
            if (serverNode.getNode("http-service") == null){
                // http-service
                httpServiceNode = TreeNodeFactory.createTreeNode("http-service", null, "http-service");
                serverNode.addChild(httpServiceNode);
            }
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
        // http-service
        httpServiceNode = TreeNodeFactory.createTreeNode("http-service", null, "http-service");
        serverNode.addChild(httpServiceNode);
    }

    //builds the thread pool sub nodes
    private void buildHttpServiceMonitoringTree() {
        if (isHttpServiceTreeBuilt || !httpServiceMonitoringEnabled)
            return;
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        logger.finest("[Monitor]Http Service Monitoring tree is being built");
        //http-service sub-nodes
        vsRequestTMs = new ArrayList<HttpServiceRequestTelemetry>();
        
        for (VirtualServer vs : httpService.getVirtualServer()) {
            TreeNode vsNode = TreeNodeFactory.createTreeNode(vs.getId(), null, "http-service");
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
        
        isHttpServiceTreeBuilt = true;
    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }

    private void enableHttpServiceMonitoring(boolean isEnabled) {
        //Enable/Disable thread-pool telemetry
        httpServiceNode.setEnabled(isEnabled);
        if (vsRequestTMs != null) {
            for (HttpServiceRequestTelemetry requestTM : vsRequestTMs) {
                requestTM.enableMonitoring(isEnabled);
            }
        }
    }

    public static String getAppName(String contextRoot) {
        if (contextRoot == null)
            return null;
        // first check in web modules
        List<WebModule> lm = domain.getApplications().getModules(WebModule.class);
        for (WebModule wm : lm) {
            if (contextRoot.equals(wm.getContextRoot())) {
                return (wm.getName());
            }
        }
        // then check under applications (introduced in V3 not j2ee app)
        List<Application> la = domain.getApplications().getModules(Application.class);
        for (Application sapp : la) {
            if (contextRoot.equals(sapp.getContextRoot())) {
                return (sapp.getName());
            }
        }
        return null;
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
            HttpListener httpListener = null;

            for (HttpListener hl : httpService.getHttpListener()) {
                if (hl.getPort().equals(listenerPort)) {
                    httpListener = hl;
                }
                break;
            }
            VirtualServer virtualServer = null;
            for (VirtualServer vs : httpService.getVirtualServer()) {
                if (vs.getHosts().contains(hostName) && vs.getHttpListeners().contains(httpListener.getId())) {
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
