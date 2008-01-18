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

package com.sun.enterprise.v3.web;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import com.sun.logging.LogDomains;
import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import com.sun.grizzly.tcp.Adapter;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Properties;
import java.util.Collection;
import java.util.HashSet;
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
public class WebDeployer extends JavaEEDeployer<WebContainer, WebApplication> {

    @Inject
    ServerContext sc;

    @Inject
    Domain domain;

    @Inject
    V3Environment env;

    @Inject
    GrizzlyService grizzlyAdapter;

    
    private static final String ADMIN_VS = "__asadmin";

    private static final String DEFAULT_WEB_XML = "default-web.xml";

    private WebModuleListener webModuleListener;

    private static WebBundleDescriptor defaultWebXMLWbd = null;

    /**
     * Constructor
     */
    public WebDeployer() {

        webModuleListener = new WebModuleListener();
    }
    

    protected String getModuleType () {
        return "web";
    }

    protected WebBundleDescriptor getDefaultBundleDescriptor() {
        return getDefaultWebXMLBundleDescriptor();
    }

    public WebApplication load(WebContainer container, DeploymentContext dc) {

        WebBundleDescriptor wbd = (WebBundleDescriptor)dc.getModuleMetaData(
            getModuleType(), Application.class).getStandaloneBundleDescriptor();

        ReadableArchive source = dc.getSource();

        String docBase = source.getURI().getSchemeSpecificPart();

        Properties params = dc.getCommandParameters();
        String ctxtRoot = "/" + params.getProperty(DeployCommand.NAME);
        List<String> targets = StringUtils.parseStringList(
            params.getProperty(DeployCommand.VIRTUAL_SERVERS), " ,");
        boolean loadToAll = (targets == null) || (targets.size() == 0);

        WebApplication webApplication = null;
        Container[] hosts = container.engine.findChildren();
        for (int i=0; i<hosts.length; i++) {
            StandardHost vs = (StandardHost) hosts[i];

            if (loadToAll && ADMIN_VS.equals(vs.getName())) {
                // Do not load to __asadmin
                continue;
            }

            if (loadToAll || targets.contains(vs.getName())
                    || isAliasMatched(targets,vs)) {

                StandardContext ctx = (StandardContext)
                    container.embedded.createContext(ctxtRoot, docBase);
                ctx.addLifecycleListener(webModuleListener);
                ctx.setJ2EEServer(container.instanceName);
                if (container.defaultWebXml != null) {
                    ctx.setDefaultWebXml(container.defaultWebXml);
                }
                ctx.setPrivileged(true);
                vs.addChild(ctx);
                webApplication = new WebApplication(container, ctx);
                dc.getLogger().info("Deployed web module " + ctx
                                    + " to virtual server " + vs.getName());

                Collection<String> c = new HashSet<String>();
                c.add(vs.getName());
                for (int port : vs.getPorts()) {
                    Adapter adapter = container.adapterMap.get(Integer.valueOf(port));                    
                    grizzlyAdapter.registerEndpoint(ctxtRoot, c, adapter, webApplication);
                }
            }
        }
        return webApplication;
    }

    public void unload(WebApplication webApplication, DeploymentContext dc) {

        Properties params = dc.getCommandParameters();
        String ctxtRoot = params.getProperty(DeployCommand.NAME);
        if (!ctxtRoot.equals("") && !ctxtRoot.startsWith("/") ) {
            ctxtRoot = "/" + ctxtRoot;
        } else if ("/".equals(ctxtRoot)) {
            ctxtRoot = "";
        }
        grizzlyAdapter.unregisterEndpoint(ctxtRoot, webApplication);

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
                    // ToDo : dochez : not good, we unregister from everywhere.
                    grizzlyAdapter.unregisterEndpoint(ctxtRoot, webApplication);
		}
            }
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
