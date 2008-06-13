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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.web;


import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import org.glassfish.internal.api.ServerContext;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Module;
import org.glassfish.web.loader.util.ASClassLoaderUtil;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.web.JSPCompiler;
import org.glassfish.deployment.common.DeploymentException;

import java.util.*;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Web module deployer.
 *
 * @author jluehe
 * @author Jerome Dochez
 */
@Service
public class WebDeployer extends JavaEEDeployer<WebContainer, WebApplication>{

    
    @Inject
    ServerContext sc;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;

    @Inject
    RequestDispatcher dispatcher;

    private static final String DEFAULT_WEB_XML = "default-web.xml";

    private static WebBundleDescriptor defaultWebXMLWbd = null;

    /**
     * Constructor
     */
    public WebDeployer() {
    }
    

    protected String getModuleType () {
        return "web";
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        List<ModuleDefinition> apis = new ArrayList<ModuleDefinition>();
        Module module = modulesRegistry.makeModuleFor("org.glassfish:javax.javaee",null);
        if (module!=null) {
            apis.add(module.getModuleDefinition());
        }

        String[] otherExportedPackages = new String[] {
                "org.glassfish.web:webtier",
                "org.glassfish.web:jsf-connector",
                "org.glassfish.web:jstl-impl",
                "org.glassfish.web:jspimpl",
                "org.glassfish.external:grizzly-module",
                "org.glassfish.external:grizzly-optionals" };

        for (String otherExportedPackage : otherExportedPackages) {
            module = modulesRegistry.makeModuleFor(otherExportedPackage, null);
            if (module != null) {
                apis.add(module.getModuleDefinition());
            }
        }

        return new MetaData(false, apis.toArray(new ModuleDefinition[apis.size()]),
                new Class[] { Application.class }, null);
    }    

    protected WebBundleDescriptor getDefaultBundleDescriptor() {
        return getDefaultWebXMLBundleDescriptor();
    }

    @Override
    protected Application parseModuleMetaData(DeploymentContext dc) throws Exception {
        Application app = super.parseModuleMetaData(dc);

        WebBundleDescriptor wbd = null;

        Set<WebBundleDescriptor> webDesc = app.getWebBundleDescriptors();
        Iterator<WebBundleDescriptor> iter = webDesc.iterator();
        if (iter.hasNext()) {
            wbd =  iter.next();
        }

        // the context root should be set using the following precedence
        // for standalone web module
        // 1. User specified value through DeployCommand
        // 2. Context root value specified through sun-web.xml
        // 3. The default context root
        // 4. archive name
        Properties params = dc.getCommandParameters();
        String contextRoot = params.getProperty(ParameterNames.CONTEXT_ROOT);
        if(contextRoot==null) {
            contextRoot = wbd.getContextRoot();
            if("".equals(contextRoot))
                contextRoot = null;
        }
        if(contextRoot==null)
            contextRoot = params.getProperty(ParameterNames.NAME);
        if(contextRoot==null)
            contextRoot = dc.getSource().getName();

        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        wbd.setContextRoot(contextRoot);
        wbd.setName(params.getProperty(ParameterNames.NAME));

        // set the context root to deployment context props so this value
        // will be persisted in domain.xml
        dc.getProps().setProperty(ServerTags.CONTEXT_ROOT, contextRoot);

        return app;
    }

    private WebModuleConfig loadWebModuleConfig(DeploymentContext dc) {
        
        WebModuleConfig wmInfo = null;
        
        try {
            ReadableArchive source = dc.getSource();
            Properties params = dc.getCommandParameters();
            String virtualServers = params.getProperty(ParameterNames.VIRTUAL_SERVERS);
        
            wmInfo = new WebModuleConfig();
            
            WebBundleDescriptor wbd = null;

            Application app = dc.getModuleMetaData(Application.class);
            Set<WebBundleDescriptor> webDesc = app.getWebBundleDescriptors();
            Iterator<WebBundleDescriptor> iter = webDesc.iterator();
            if (iter.hasNext()) {
                wbd = iter.next();
            }

            wmInfo.setDescriptor(wbd);
            wmInfo.setVirtualServers(virtualServers);
            wmInfo.setLocation(dc.getSourceDir());
            wmInfo.setObjectType(dc.getProps().getProperty(ServerTags.OBJECT_TYPE));
            wmInfo.setWorkDir(dc.getScratchDir(env.kCompileJspDirName).getAbsolutePath());
        } catch (Exception ex) {
            dc.getLogger().log(Level.WARNING, "loadWebModuleConfig", ex);
        }
        
        return wmInfo;
        
    }
    
    @Override
    protected void generateArtifacts(DeploymentContext dc) 
        throws DeploymentException {
        final Properties params = dc.getCommandParameters();
        final boolean precompileJSP = Boolean.parseBoolean(params.getProperty(ParameterNames.PRECOMPILE_JSP));
        if (precompileJSP) {
            //call JSPCompiler... 
            runJSPC(dc);
        }
    }

         
    public WebApplication load(WebContainer container, DeploymentContext dc) {
        
        WebModuleConfig wmInfo = loadWebModuleConfig(dc);    

        return new WebApplication(container, wmInfo, dispatcher);
    }

    
    public void unload(WebApplication webApplication, DeploymentContext dc) {
        
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

        InputStream fis = null;

        try {
            // parse default-web.xml contents 
            URL defaultWebXml = getDefaultWebXML();
            if (defaultWebXml!=null)  {
                fis = defaultWebXml.openStream();
                WebDeploymentDescriptorFile wddf =
                    new WebDeploymentDescriptorFile();
                wddf.setXMLValidation(false);
                defaultWebXMLWbd = wddf.read(fis);
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

    /**
     * Obtains the location of <tt>default-web.xml</tt>.
     * This allows subclasses to load the file from elsewhere.
     *
     * @return
     *      null if not found, in which case the default web.xml will not be read
     *      and <tt>web.xml</tt> in the applications need to have everything.
     */
    protected URL getDefaultWebXML() throws IOException {
        File file = new File(env.getConfigDirPath(),DEFAULT_WEB_XML);
        if (file.exists())
            return file.toURI().toURL();
        else
            return null;
    }

    /**
     * This method setups the in/outDir and classpath and invoke
     * JSPCompiler.
     * @param dc - DeploymentContext to get command parameters and
     *             source directory and compile jsp directory.
     * @throws DeploymentException if JSPCompiler is unsuccessful.
     */
    void runJSPC(final DeploymentContext dc) throws DeploymentException {
        final WebBundleDescriptor wbd = (WebBundleDescriptor)dc.getModuleMetaData(
              Application.class).getStandaloneBundleDescriptor();
        try {
            final File outDir = dc.getScratchDir(env.kCompileJspDirName);
            final File inDir  = dc.getSourceDir();
            boolean delegate = true;
            com.sun.enterprise.deployment.runtime.web.ClassLoader clBean =
                    wbd.getSunDescriptor().getClassLoader();
            if (clBean != null) {
                String value = clBean.getAttributeValue(
                    com.sun.enterprise.deployment.runtime.web.ClassLoader.DELEGATE);
                delegate = ConfigBeansUtilities.toBoolean(value);
            }

            StringBuffer classpath = new StringBuffer();
            classpath.append(super.getCommonClassPath());
            classpath.append(File.pathSeparatorChar);
            classpath.append(ASClassLoaderUtil.getWebModuleClassPath(
                    sc.getDefaultHabitat(),
                    wbd.getApplication().getName(), delegate));
            JSPCompiler.compile(inDir, outDir, wbd, classpath.toString(), sc);
        } catch (DeploymentException de) {
            dc.getLogger().log(Level.SEVERE, "Error compiling JSP", de);
            throw de;
        }
    }
}
