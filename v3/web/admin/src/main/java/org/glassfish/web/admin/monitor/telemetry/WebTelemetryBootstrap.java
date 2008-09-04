/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admin.monitor.telemetry;

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
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.provider.ProbeProviderEventManager;
import org.jvnet.hk2.component.PostConstruct;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name="web-container")
@Scoped(Singleton.class)
public class WebTelemetryBootstrap implements ProbeProviderListener, TelemetryProvider, PostConstruct {

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
    private JspStatsTelemetry webJspTM = null;
    private List<JspStatsTelemetry> vsJspTMs = null;

    private TreeNode serverNode;
    private TreeNode webNode;
    private TreeNode webSessionNode;
    private TreeNode webServletNode;
    private TreeNode webJspNode;
    private TreeNode webRequestNode;
    private TreeNode applicationsNode;

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
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    sessionProviderRegistered = true;
                    return;
                }
                buildSessionTelemetry();
            }
            if (providerName.equals("servlet")){
                logger.finest("[Monitor]and it is Web servlet");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    servletProviderRegistered = true;
                    return;
                }
                buildServletTelemetry();
            }

            if (providerName.equals("jsp")){
                logger.finest("[Monitor]and it is Web jsp");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    jspProviderRegistered = true;
                    return;
                }
                buildJspTelemetry();
            }
            if (providerName.equals("request")){
                logger.finest("[Monitor]and it is Web request");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    //The reason being either the tree already exists or the 
                    // monitoring is 'OFF'
                    requestProviderRegistered = true;
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
                    //Fix it!
                    TreeNode vs = TreeNodeFactory.createTreeNode("server", null, "web");
                    tn.addChild(vs);
                }
                return;
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
                    JspStatsTelemetry vsJspTM = 
                            new JspStatsTelemetry(vsNode, 
                                appNode.getName(), vsNode.getName(), 
                                webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = 
                                                pcm.registerListener(vsJspTM);
                    vsJspTM.setProbeListenerHandles(handles);
                    vsJspTMs.add(vsJspTM);
                }
            }
        }else { //Make sure you turn them on
            for (JspStatsTelemetry jspTM : vsJspTMs) {
                if (!jspTM.isEnabled())
                    jspTM.enableMonitoring(true);
            }
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
                    ServletStatsTelemetry vsServletTM = 
                            new ServletStatsTelemetry(vsNode, 
                                appNode.getName(), vsNode.getName(), 
                                webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = 
                                            pcm.registerListener(vsServletTM);
                    vsServletTM.setProbeListenerHandles(handles);
                    vsServletTMs.add(vsServletTM);
                }
            }
        } else { //Make sure you turn them on
            for (ServletStatsTelemetry servletTM : vsServletTMs) {
                if (!servletTM.isEnabled())
                    servletTM.enableMonitoring(true);
            }
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
                    SessionStatsTelemetry vsSessionTM = 
                            new SessionStatsTelemetry(vsNode, 
                                appNode.getName(), vsNode.getName(), 
                                webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = 
                                            pcm.registerListener(vsSessionTM);
                    vsSessionTM.setProbeListenerHandles(handles);
                    vsSessionTMs.add(vsSessionTM);
                }
            }
        } else { //Make sure you turn them on
            for (SessionStatsTelemetry sessionTM : vsSessionTMs) {
                if (!sessionTM.isEnabled())
                    sessionTM.enableMonitoring(true);
            }
        }
    }

    private void buildWebRequestTelemetry() {
        if (webRequestTM == null) {
            webRequestTM = new WebRequestTelemetry(webRequestNode, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webRequestTM);
            webRequestTM.setProbeListenerHandles(handles);
        } else { // Make sure you turn it on
            if (!webRequestTM.isEnabled())
                webRequestTM.enableMonitoring(true);
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
    }
}
