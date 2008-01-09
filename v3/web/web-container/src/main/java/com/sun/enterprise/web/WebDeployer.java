/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.web;

//import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.v3.services.impl.GrizzlyAdapter;
import com.sun.logging.LogDomains;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import com.sun.grizzly.tcp.Adapter;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Web module deployer.
 *
 * @author jluehe
 * @author Jerome Dochez
 */
@Service
public class WebDeployer extends JavaEEDeployer implements Deployer<WebContainer, WebApplication>{

    
    @Inject
    ServerContext sc;

    @Inject
    Domain domain;

    @Inject
    V3Environment env;

    @Inject
    GrizzlyAdapter grizzlyAdapter;

    
    private static final String ADMIN_VS = "__asadmin";

    private static final String DEFAULT_WEB_XML = "default-web.xml";

    //private WebModuleListener webModuleListener;

    private static WebBundleDescriptor defaultWebXMLWbd = null;

    /**
     * Constructor
     */
    public WebDeployer() {
        //webModuleListener = new WebModuleListener();
    }
    

    protected String getModuleType () {
        return "web";
    }

    protected WebBundleDescriptor getDefaultBundleDescriptor() {
        return getDefaultWebXMLBundleDescriptor();
    }

    @Override
    protected Application parseModuleMetaData(DeploymentContext dc) throws Exception {
        Application app = super.parseModuleMetaData(dc);
        if (app.isVirtual()) {

            WebBundleDescriptor wbd = (WebBundleDescriptor) app.getStandaloneBundleDescriptor(); 
            // TO DO : this will need to be revisited to handle context root property
            Properties params = dc.getCommandParameters();
            if (params.getProperty(DeployCommand.CONTEXT_ROOT)!=null) {
                wbd.setContextRoot("/" + params.getProperty(DeployCommand.CONTEXT_ROOT));    
            } else {
                if (wbd.getContextRoot()==null || wbd.getContextRoot().length()==0) {
                   wbd.setContextRoot("/" + params.getProperty(DeployCommand.NAME)); 
                }
            }
            wbd.setName(params.getProperty(DeployCommand.NAME));
        }

        return app;
    }

    private WebModuleConfig loadWebModuleConfig(com.sun.enterprise.config.serverbeans.WebModule wm, 
        DeploymentContext dc) {

        WebModuleConfig wmInfo = new WebModuleConfig();

        if (wm != null) {
            wmInfo.setBean(wm);
            String wmID = wm.getName();
            String location = wm.getLocation();
        }
            
        WebBundleDescriptor wbd = (WebBundleDescriptor)dc.getModuleMetaData(
            getModuleType(), Application.class).getStandaloneBundleDescriptor();
        
        wmInfo.setDescriptor(wbd);
        
        /*
        List<Server> servers = domain.getServers().getServer();
        Server thisServer = null;
        for (Server server : servers) {
            if (sc.getInstanceName().equals(server.getName())) {
                thisServer = server;
            }
        }
        
        List<ApplicationRef> appRefs = thisServer.getApplicationRef();
        ApplicationRef appRef = null;
        for (ApplicationRef ar : appRefs) {
            //if (ar.getRef().equals(wmID)) {
            if (ar.getRef().equals(wbd.getName())) {
                appRef = ar;
            }
        }
            
        String vsIDs = appRef.getVirtualServers();
        wmInfo.setVirtualServers(vsIDs);
         */
       
        return wmInfo;
        
    } 
         
    public WebApplication load(WebContainer container, DeploymentContext dc) {
        
        // WebModule config-api not available yet
        WebModuleConfig wmInfo = loadWebModuleConfig(null, dc);
        
        String vsIDs = wmInfo.getVirtualServers();
        List vsList = StringUtils.parseStringList(vsIDs, " ,");
        
        WebBundleDescriptor wbd = (WebBundleDescriptor)dc.getModuleMetaData(
            getModuleType(), Application.class).getStandaloneBundleDescriptor();

        ReadableArchive source = dc.getSource();

        String docBase = source.getURI().getSchemeSpecificPart();

        Properties params = dc.getCommandParameters();
        List<String> targets = StringUtils.parseStringList(
            params.getProperty(DeployCommand.VIRTUAL_SERVERS), " ,");
        boolean loadToAll = (targets == null) || (targets.size() == 0);
        
        WebApplication webApplication = null;
        Engine[] engines = container.getEngines();
        Container[] hosts = engines[0].findChildren();
        for (int i=0; i<hosts.length; i++) {
            VirtualServer vs = (VirtualServer) hosts[i];

            if (loadToAll && ADMIN_VS.equals(vs.getName())) {
                // Do not load to __asadmin
                continue;
            }

            if (loadToAll || targets.contains(vs.getName())
                    || isAliasMatched(targets,vs)) {
                
                StandardContext ctx = container.loadWebModule(vs, dc, "null");     
                webApplication = new WebApplication(container, ctx);      
                registerEndpoint(container, vs, wbd.getContextRoot(), dc, webApplication);
                //loadAtLeastToOne = true;
                       
            }
        }
       
        /*    
        List applications = domain.getApplications().getLifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModule();
        com.sun.enterprise.config.serverbeans.WebModule webModule = null;
        for (Object module : applications) {
            if (module instanceof WebModule) {
                webModule = (com.sun.enterprise.config.serverbeans.WebModule) module;
            }
        }
        WebApplication webApplication = null;
        String location = webModule.getLocation();
        
        // If module root is relative then prefix it with the 
        // location of where all the standalone modules for 
        // this server instance are deployed
        File moduleBase = new File(location);
        String modulesRoot = container.getModulesRoot();
        if (!moduleBase.isAbsolute()) {
            location = modulesRoot+File.separator+location;
            try {
                webModule.setLocation(location);
            } catch (java.beans.PropertyVetoException ex) {
                
            }
        }
        
        WebModuleConfig wmInfo = loadWebModuleConfig(webModule, dc);
         
        
        if (!loadAtLeastToOne) {
            Object[] params = {wmInfo.getName(), vsIDs};
            container._logger.log(Level.SEVERE, "webcontainer.moduleNotLoadedToVS",
                    params);
        }*/
      
        return webApplication;
        
    }

    
    public void unload(WebApplication webApplication, DeploymentContext dc) {

        /*
        List applications = domain.getApplications().getLifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModule();
        com.sun.enterprise.config.serverbeans.WebModule webModule = null;
        for (Object module : applications) {
            if (module instanceof WebModule) {
                webModule = (com.sun.enterprise.config.serverbeans.WebModule) module;
            }
        }
        
        WebApplication webApplication = null;
        String location = webModule.getLocation();
        */
        
        Properties params = dc.getCommandParameters();
        String ctxtRoot = params.getProperty(DeployCommand.NAME);
        if (!ctxtRoot.equals("") && !ctxtRoot.startsWith("/") ) {
            ctxtRoot = "/" + ctxtRoot;
        } else if ("/".equals(ctxtRoot)) {
            ctxtRoot = "";
        }

        List<String> targets = StringUtils.parseStringList(
            params.getProperty(DeployCommand.VIRTUAL_SERVERS), " ,");
        boolean unloadFromAll = (targets == null) || (targets.size() == 0);

        Container[] hosts = webApplication.getContainer().engine.findChildren();
        for (int i = 0; i < hosts.length; i++) {
            StandardHost vs = (StandardHost) hosts[i];

            if (unloadFromAll && ADMIN_VS.equals(vs.getName())){
                // Do not unload from __asadmin
                continue;
            }

            if (unloadFromAll
                    || targets.contains(vs.getName())
                    || isAliasMatched(targets, vs)){

                StandardContext ctxt = (StandardContext)
                    vs.findChild(ctxtRoot);
                if (ctxt != null) {
                    vs.removeChild(ctxt);
                    try {
                        ctxt.destroy();
                    } catch (Exception ex) {
                        dc.getLogger().log(Level.WARNING,
                                           "Unable to destroy web module "
                                           + ctxt, ex);
                    }
                    dc.getLogger().info("Undeployed web module " + ctxt
                                        + " from virtual server "
                                        + vs.getName());
                    unregisterEndpoint(webApplication.getContainer(), vs, ctxtRoot, dc);
		}
            }
        }
    }
    

    private void registerEndpoint(WebContainer container,
                                  Host vs,
                                  String ctxtRoot,
                                  DeploymentContext dc, WebApplication webApp) {

        int[] ports = vs.getPorts();
        if (ports == null) {
            return;
        }

        for (int i=0; i<ports.length; i++) {
            Adapter adapter = container.adapterMap.get(Integer.valueOf(ports[i]));
            grizzlyAdapter.registerEndpoint(ctxtRoot, adapter, webApp);
            dc.getLogger().info("Registered adapter " + adapter
                                + " for web endpoint " + ctxtRoot
                                + " at port " + ports[i]);
        }
    }


    private void unregisterEndpoint(WebContainer container,
                                    Host vs,
                                    String ctxtRoot,
                                    DeploymentContext dc) {

        int[] ports = vs.getPorts();
        if (ports == null) {
            return;
        }

        for (int i=0; i<ports.length; i++) {
            Adapter adapter = container.adapterMap.get(Integer.valueOf(ports[i]));
            grizzlyAdapter.unregisterEndpoint(ctxtRoot);
            dc.getLogger().info("Unregistered web endpoint " + ctxtRoot
                                + " from port " + ports[i]);
        }
    }
    
        
    /*
     * @return true if the list of target virtual server names matches an
     * alias name of the given virtual server, and false otherwise
     */ 
    private boolean isAliasMatched(List targets, StandardHost vs){

        String[] aliasNames = vs.getAliases();
        for (int i=0; i<aliasNames.length; i++) {
            if (targets.contains(aliasNames[i]) ){
                return true;
            }
        }

        return false;
    }
    
    
    /**
     * Deploy on aliases as well as host.
     */
    private boolean verifyAlias(List vsList,VirtualServer vs){
        for(int i=0; i < vs.getAliases().length; i++){
            if (vsList.contains(vs.getAliases()[i]) ){
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * @return a copy of default WebBundleDescriptor populated from
     * default-web.xml
     */
    public WebBundleDescriptor getDefaultWebXMLBundleDescriptor() {
        initDefaultWebXMLBundleDescriptor();

        // when default-web.xml exists, add the default bundle descriptor
        // as the base web bundle descriptor
        WebBundleDescriptor defaultWebBundleDesc =
            new WebBundleDescriptor();
        if (defaultWebXMLWbd != null) {
            defaultWebBundleDesc.addWebBundleDescriptor(defaultWebXMLWbd);
        }
        return defaultWebBundleDesc;
    }


    /**
     * initialize the default WebBundleDescriptor from
     * default-web.xml
     */
    private synchronized void initDefaultWebXMLBundleDescriptor() {

        if (defaultWebXMLWbd != null) {
            return;
        }

        FileInputStream fis = null;

        try {
            // parse default-web.xml contents 
            String defaultWebXMLPath = env.getConfigDirPath() +
                File.separator + DEFAULT_WEB_XML;
            File file = new File(defaultWebXMLPath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                WebDeploymentDescriptorFile wddf =
                    new WebDeploymentDescriptorFile();
                wddf.setXMLValidation(false);
                defaultWebXMLWbd = (WebBundleDescriptor) wddf.read(fis);
            }
        } catch (Exception e) {
            LogDomains.getLogger(LogDomains.WEB_LOGGER).
                warning("Error in parsing default-web.xml");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

}
