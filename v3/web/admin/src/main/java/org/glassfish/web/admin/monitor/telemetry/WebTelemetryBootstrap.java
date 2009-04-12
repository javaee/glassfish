/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admin.monitor.telemetry;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.WebModule;
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
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import java.beans.PropertyChangeEvent;
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
@Service(name="web-container")
@Scoped(Singleton.class)
public class WebTelemetryBootstrap implements ProbeProviderListener, TelemetryProvider, 
                                                PostConstruct, ConfigListener {

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
            
    private boolean requestProviderRegistered = false;
    private boolean servletProviderRegistered = false;
    private boolean jspProviderRegistered = false;
    private boolean sessionProviderRegistered = false;
    private boolean probeProviderListenerRegistered = false;
    private boolean webMonitoringEnabled = false;
    private boolean isWebTreeBuilt = false;

    private WebRequestTelemetry webRequestTM = null;
    private SessionStatsTelemetry webSessionsTM = null;
    private List<SessionStatsTelemetry> vsSessionTMs = null;
    private ServletStatsTelemetry webServletsTM = null;
    private List<ServletStatsTelemetry> vsServletTMs = null;
    private List<WebRequestTelemetry> vsRequestTMs = null;
    private JspStatsTelemetry webJspTM = null;
    private List<JspStatsTelemetry> vsJspTMs = null;

    private TreeNode serverNode;
    private TreeNode webNode;
    private TreeNode webSessionNode;
    private TreeNode webServletNode;
    private TreeNode webJspNode;
    private TreeNode webRequestNode;
    private TreeNode applicationsNode;
    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;

    public WebTelemetryBootstrap() {
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
        logger.finest("[Monitor]In the WebTelemetry bootstrap ************");

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
        networkConfig = config.getNetworkConfig();
    }
    
    public void onLevelChange(String newLevel) {
        boolean newLevelEnabledValue = getEnabledValue(newLevel);
        logger.finest("[Monitor]In the Level Change = " + newLevel + "  ************");
        if (webMonitoringEnabled != newLevelEnabledValue) {
            webMonitoringEnabled = newLevelEnabledValue;
        } else {
            // Might have changed from 'LOW' to 'HIGH' or vice-versa. Ignore.
            return;
        }
        //check if the monitoring level for web-container is 'on' and 
        // if Web Container is loaded, then register the ProveProviderListener
        if (webMonitoringEnabled) { 
            // enable flag turned from 'OFF' to 'ON'
            if (!probeProviderListenerRegistered) { 
                //Came the very first time into this method
                registerProbeProviderListener();
            } else { 
              //probeProvider is already registered, 
              // (1)Could be that the telemetry objects are not built, I dont care since
              //    for sure the ProbeProviderListener didn't fire any events. The check
              //    whether the telemetry objects are created is done in enableWebMon..()
              // (2)Could be that the telemetry objects are there but were disabled 
              //    explicitly by user. Now we need to enable them
                enableWebMonitoring(true);
            }
            
        } else { 
            //enable flag turned from 'ON' to 'OFF', so disable telemetry
            enableWebMonitoring(false);
        }
    }

    public void providerRegistered(String moduleName, String providerName, String appName) {
        try {
            
            logger.finest("[Monitor]Provider registered event received - providerName = " + 
                                providerName + " : module name = " + moduleName + 
                                " : appName = " + appName);
            if (providerName.equals("session")){
                logger.finest("[Monitor]and it is Web session");
                buildWebMonitoringTree();
                sessionProviderRegistered = true;
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildSessionTelemetry();
            }
            if (providerName.equals("servlet")){
                logger.finest("[Monitor]and it is Web servlet");
                buildWebMonitoringTree();
                servletProviderRegistered = true;
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildServletTelemetry();
            }

            if (providerName.equals("jsp")){
                logger.finest("[Monitor]and it is Web jsp");
                buildWebMonitoringTree();
                jspProviderRegistered = true;
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildJspTelemetry();
            }
            if (providerName.equals("request")){
                logger.finest("[Monitor]and it is Web request");
                buildWebMonitoringTree();
                requestProviderRegistered = true;
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    return;
                }
                buildWebRequestTelemetry();
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
        //I dont see an implementation for this yet.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }

    // Handle the deploy/undeploy events
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
       for (PropertyChangeEvent event : events) {
           //if (event.getSource() instanceof ApplicationRef) {
            if (event.getPropertyName().equals("application-ref")) {
                String propName = event.getPropertyName();
                String appName = null;
                if (event.getNewValue() != null) {
                    //This means its a deployed event
                    appName = ((ApplicationRef)(event.getNewValue())).getRef();
                    updateApplicationSubTree(appName, true);
                } else if (event.getOldValue() != null) {
                    //This means its an undeployed event
                    appName = ((ApplicationRef)(event.getOldValue())).getRef();
                    updateApplicationSubTree(appName, false);
                }
                logger.finest("[Monitor] (Un)Deploy event received - name = " + propName + " : Value = " + appName);
           }
       }
        
        return null;
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

    private void buildWebMonitoringTree() {
        if (isWebTreeBuilt || !webMonitoringEnabled)
            return;
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        logger.finest("[Monitor]Web Monitoring tree is being built");
        // web
        webNode = TreeNodeFactory.createTreeNode("web", null, "web");
        serverNode.addChild(webNode);
        //web->session
        webSessionNode = TreeNodeFactory.createTreeNode("session", null, "web");
        webNode.addChild(webSessionNode);
        
        //web->servlet
        webServletNode = TreeNodeFactory.createTreeNode("servlet", null, "web");
        webNode.addChild(webServletNode);
        //web->jsp
        webJspNode = TreeNodeFactory.createTreeNode("jsp", null, "web");
        webNode.addChild(webJspNode);
        //web->request
        webRequestNode = TreeNodeFactory.createTreeNode("request", this, "web");
        webNode.addChild(webRequestNode);
        
        // applications
        applicationsNode = TreeNodeFactory.createTreeNode("applications", null, "web");
        serverNode.addChild(applicationsNode);
        // application
        List<Application> la = domain.getApplications().getModules(Application.class);
        for (Application sapp : la) {
            TreeNode app = TreeNodeFactory.createTreeNode(sapp.getName(), null, "web");
            applicationsNode.addChild(app);
            addVirtualServers(srvr, app, sapp.getName());
        }
        // j2ee application
        List<J2eeApplication> lja = domain.getApplications().getModules(J2eeApplication.class);
        for (J2eeApplication japp : lja) {
            TreeNode app = TreeNodeFactory.createTreeNode(japp.getName(), null, "web");
            applicationsNode.addChild(app);
        }
        // web modules
        List<WebModule> lm = domain.getApplications().getModules(WebModule.class);
        for (WebModule wm : lm) {
            TreeNode app = TreeNodeFactory.createTreeNode(wm.getName(), null, "web");
            applicationsNode.addChild(app);
            addVirtualServers(srvr, app, wm.getName());
        }
        isWebTreeBuilt = true;
    }
    

    private void updateApplicationSubTree(String appName, boolean isDeployed) {
        logger.finest("Updating the tree for the Deployed(" + isDeployed + ") App=" + appName);
        if (isDeployed) {
            if (!isWebTreeBuilt) 
                return;
            Server srvr = null;
            List<Server> ls = domain.getServers().getServer();
            for (Server sr : ls) {
                if ("server".equals(sr.getName())) {
                    srvr = sr;
                    break;
                }
            }
            TreeNode app = TreeNodeFactory.createTreeNode(appName, null, "web");
            applicationsNode.addChild(app);
            addVirtualServers(srvr, app, appName);
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                if (!appNode.getName().equals(appName)) 
                    continue;
                //Get all virtual servers for the app
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create TM's for each vsNode
                    buildWebRequestTelemetryForVS(vsNode, appName);
                    buildJSPTelemetryForVS(vsNode, appNode.getName());
                    buildServletTelemetryForVS(vsNode, appName);
                    buildSessionTelemetryForVS(vsNode, appName);
                }
            }
            
        } else {
            Collection<TreeNode> appNodes = applicationsNode.getChildNodes();
            TreeNode appNodeToRemove = null;
            for (TreeNode appNode : appNodes) {
                if (appNode.getName().equals(appName)) {
                    Collection<TreeNode> vsNodes = appNode.getChildNodes();
                    for (TreeNode vsNode : vsNodes) {
                        //remove the Telemetry objects
                        String vsName = vsNode.getName();
                        removeJSPTelemetryForVS(appName, vsName);
                        removeServletTelemetryForVS(appName, vsName);
                        removeSessionTelemetryForVS(appName, vsName);
                        removeRequestTelemetryForVS(appName, vsName);
                    }
                    appNode.setEnabled(false);
                    appNodeToRemove = appNode;
                    break;
                }
            }
            if (appNodeToRemove != null)
                applicationsNode.removeChild(appNodeToRemove);
        }
    }

    private void addVirtualServers(Server server, TreeNode tn, String appName) {
        // get the applications refs for the server
        for (ApplicationRef ar : server.getApplicationRef()) {
            if (appName.equals(ar.getRef())) {
                String vsL = ar.getVirtualServers();
                if (vsL != null) {
                    for (String str : vsL.split(",")) {
                        TreeNode vs = TreeNodeFactory.createTreeNode(str, null, "web");
                        tn.addChild(vs);
                    }
                } else {
                    //When the app is deployed without virtual-servers mentioned,
                    // then it is implicitly associated to all the user vitual-servers
                    addUserVirtualServers(tn);
                }
                return;
            }
        }
    }

    private void addUserVirtualServers(TreeNode tn) {
        List<Config> lc = domain.getConfigs().getConfig();
        Config config = null;
        for (Config cf : lc) {
            if (cf.getName().equals("server-config")) {
                config = cf;
                break;
            }
        }
        httpService = config.getHttpService();
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getId().equals("__asadmin")) {
                TreeNode vsNode = TreeNodeFactory.createTreeNode(vs.getId(), null, "web");
                tn.addChild(vsNode);
            }
        }
    }

    private void buildJspTelemetry() {
        if (webJspTM == null) {
            webJspTM = new JspStatsTelemetry(webJspNode, null, null, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webJspTM);
            webJspTM.setProbeListenerHandles(handles);
        } else { //Make sure you turn them on
            if (!webJspTM.isEnabled())
                webJspTM.enableMonitoring(true);
        }
        if (vsJspTMs == null) {
            vsJspTMs = new ArrayList<JspStatsTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    buildJSPTelemetryForVS(vsNode, appNode.getName());
                }
            }
        }else { //Make sure you turn them on
            for (JspStatsTelemetry jspTM : vsJspTMs) {
                if (!jspTM.isEnabled())
                    jspTM.enableMonitoring(true);
            }
        }
    }

    private void buildJSPTelemetryForVS(TreeNode vsNode, String appName) {
        if (!jspProviderRegistered)
            return;
        //Create sessionTM for each vsNode
        JspStatsTelemetry vsJspTM = 
                new JspStatsTelemetry(vsNode, 
                    appName, vsNode.getName(), 
                    webMonitoringEnabled, logger);
        Collection<ProbeClientMethodHandle> handles = 
                                    pcm.registerListener(vsJspTM);
        vsJspTM.setProbeListenerHandles(handles);
        vsJspTMs.add(vsJspTM);
    }
    
    private void removeJSPTelemetryForVS(String appName, String vsName) {
        if (!jspProviderRegistered || !isWebTreeBuilt || (vsJspTMs == null))
            return;
        List<JspStatsTelemetry> jspTMsToRemove = new ArrayList<JspStatsTelemetry>();
        for (JspStatsTelemetry vsJspTM : vsJspTMs){
            if (vsJspTM.getModuleName().equals(appName) && 
                            vsJspTM.getVSName().equals(vsName)) {
                jspTMsToRemove.add(vsJspTM);
                vsJspTM.enableMonitoring(false);
            }
        }
        for (JspStatsTelemetry jspTMToRemove : jspTMsToRemove) {
            vsJspTMs.remove(jspTMToRemove);
        }
    }
    
    private void buildServletTelemetry() {
        if (webServletsTM == null) {
            webServletsTM = new ServletStatsTelemetry(webServletNode, null, 
                                    null, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webServletsTM);
            webServletsTM.setProbeListenerHandles(handles);
        } else { //Make sure you turn them on
            if (webServletsTM.isEnabled())
                webServletsTM.enableMonitoring(true);
        }
        if (vsServletTMs == null) {
            vsServletTMs = new ArrayList<ServletStatsTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    buildServletTelemetryForVS(vsNode, appNode.getName());
                }
            }
        } else { //Make sure you turn them on
            for (ServletStatsTelemetry servletTM : vsServletTMs) {
                if (!servletTM.isEnabled())
                    servletTM.enableMonitoring(true);
            }
        }
    }

    private void buildServletTelemetryForVS(TreeNode vsNode, String appName) {
        if (!servletProviderRegistered)
            return;
        //Create sessionTM for each vsNode
        ServletStatsTelemetry vsServletTM = 
                new ServletStatsTelemetry(vsNode, 
                    appName, vsNode.getName(), 
                    webMonitoringEnabled, logger);
        Collection<ProbeClientMethodHandle> handles = 
                                pcm.registerListener(vsServletTM);
        vsServletTM.setProbeListenerHandles(handles);
        vsServletTMs.add(vsServletTM);
    }
    
    private void removeServletTelemetryForVS(String appName, String vsName) {
        if (!servletProviderRegistered || !isWebTreeBuilt || (vsServletTMs == null))
            return;
        List<ServletStatsTelemetry> servletTMsToRemove = new ArrayList<ServletStatsTelemetry>();
        for (ServletStatsTelemetry vsServletTM : vsServletTMs){
            if (vsServletTM.getModuleName().equals(appName) && 
                            vsServletTM.getVSName().equals(vsName)) {
                vsServletTM.enableMonitoring(false);
                servletTMsToRemove.add(vsServletTM);
            }
        }
        for (ServletStatsTelemetry servletTMToRemove : servletTMsToRemove) {
            vsServletTMs.remove(servletTMToRemove);
        }
    }
    
    private void buildSessionTelemetry() {
        if (webSessionsTM == null) {
            webSessionsTM = new SessionStatsTelemetry(webSessionNode, 
                            null, null, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webSessionsTM);
            webSessionsTM.setProbeListenerHandles(handles);
        } else { //Make sure you turn them on
            if (!webSessionsTM.isEnabled())
                webSessionsTM.enableMonitoring(true);
        }
        if (vsSessionTMs == null) {
            vsSessionTMs = new ArrayList<SessionStatsTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    buildSessionTelemetryForVS(vsNode, appNode.getName());
                }
            }
        } else { //Make sure you turn them on
            for (SessionStatsTelemetry sessionTM : vsSessionTMs) {
                if (!sessionTM.isEnabled())
                    sessionTM.enableMonitoring(true);
            }
        }
    }

    private void buildSessionTelemetryForVS(TreeNode vsNode, String appName) {
        if (!sessionProviderRegistered)
            return;
        //Create sessionTM for each vsNode
        SessionStatsTelemetry vsSessionTM = 
                new SessionStatsTelemetry(vsNode, 
                    appName, vsNode.getName(), 
                    webMonitoringEnabled, logger);
        Collection<ProbeClientMethodHandle> handles = 
                                pcm.registerListener(vsSessionTM);
        vsSessionTM.setProbeListenerHandles(handles);
        vsSessionTMs.add(vsSessionTM);
    }
    
    private void removeSessionTelemetryForVS(String appName, String vsName) {
        if (!sessionProviderRegistered || !isWebTreeBuilt || (vsSessionTMs == null))
            return;
        List<SessionStatsTelemetry> sessionTMsToRemove = new ArrayList<SessionStatsTelemetry>();
        for (SessionStatsTelemetry vsSessionTM : vsSessionTMs){
            if (vsSessionTM.getModuleName().equals(appName) && 
                            vsSessionTM.getVSName().equals(vsName)) {
                vsSessionTM.enableMonitoring(false);
                sessionTMsToRemove.add(vsSessionTM);
            }
        }
        for (SessionStatsTelemetry sessionTMToRemove : sessionTMsToRemove) {
            vsSessionTMs.remove(sessionTMToRemove);
        }
    }
    
    private void buildWebRequestTelemetry() {
        if (webRequestTM == null) {
            webRequestTM = new WebRequestTelemetry(webRequestNode, null, null, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webRequestTM);
            webRequestTM.setProbeListenerHandles(handles);
        } else { // Make sure you turn it on
            if (!webRequestTM.isEnabled())
                webRequestTM.enableMonitoring(true);
        }
        if (vsRequestTMs == null) {
            vsRequestTMs = new ArrayList<WebRequestTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    buildWebRequestTelemetryForVS(vsNode, appNode.getName());
                }
            }
        }else { //Make sure you turn them on
            for (WebRequestTelemetry requestTM : vsRequestTMs) {
                if (!requestTM.isEnabled())
                    requestTM.enableMonitoring(true);
            }
        }
    }

    private void buildWebRequestTelemetryForVS(TreeNode vsNode, String appName) {
        if (!requestProviderRegistered)
            return;
        //Create webRequestTM for each vsNode
        WebRequestTelemetry vsRequestTM = 
                new WebRequestTelemetry(vsNode, 
                    appName, vsNode.getName(), logger);
        Collection<ProbeClientMethodHandle> handles = 
                                    pcm.registerListener(vsRequestTM);
        vsRequestTM.setProbeListenerHandles(handles);
        vsRequestTMs.add(vsRequestTM);
    }

    private void removeRequestTelemetryForVS(String appName, String vsName) {
        if (!requestProviderRegistered || !isWebTreeBuilt || (vsRequestTMs == null))
            return;
        List<WebRequestTelemetry> requestTMsToRemove = new ArrayList<WebRequestTelemetry>();
        for (WebRequestTelemetry vsRequestTM : vsRequestTMs){
            if (vsRequestTM.getModuleName().equals(appName) && 
                            vsRequestTM.getVSName().equals(vsName)) {
                vsRequestTM.enableMonitoring(false);
                requestTMsToRemove.add(vsRequestTM);
            }
        }
        for (WebRequestTelemetry requestTMToRemove : requestTMsToRemove) {
            vsRequestTMs.remove(requestTMToRemove);
        }
    }
    
    private void enableWebMonitoring(boolean isEnabled) {
        //Enable/Disable webNode
        webNode.setEnabled(isEnabled);
        //Enable/Disable applicationsNode
        applicationsNode.setEnabled(isEnabled);
        
        //Enable/Disable session telemetry
        if (webSessionsTM != null)
            webSessionsTM.enableMonitoring(isEnabled);
        if (vsSessionTMs != null) {
            for (SessionStatsTelemetry sessionTM : vsSessionTMs) {
                sessionTM.enableMonitoring(isEnabled);
            }
        }
        
        //Enable/Disable Servlet telemetry
        if (webServletsTM != null)
            webServletsTM.enableMonitoring(isEnabled);
        if (vsServletTMs != null) {
            for (ServletStatsTelemetry servletTM : vsServletTMs) {
                servletTM.enableMonitoring(isEnabled);
            }
        }
        
        //Enable/Disable JSP telemetry
        if (webJspTM != null)
            webJspTM.enableMonitoring(isEnabled);
        if (vsJspTMs != null) {
            for (JspStatsTelemetry jspTM : vsJspTMs) {
                jspTM.enableMonitoring(isEnabled);
            }
        }
        
        //Enable/Disable Request telemetry
        if (webRequestTM != null)
            webRequestTM.enableMonitoring(isEnabled);
        if (vsRequestTMs != null) {
            for (WebRequestTelemetry requestTM : vsRequestTMs) {
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

    public static String getVirtualServerName(String hostName, String listenerPort) {
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
                    break;
                }
            }
            return virtualServer.getId();
        } catch (UnknownHostException ex) {
            Logger.getLogger(WebTelemetryBootstrap.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
