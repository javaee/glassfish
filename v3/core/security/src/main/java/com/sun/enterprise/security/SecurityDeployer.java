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
package com.sun.enterprise.security;

import com.sun.enterprise.security.web.integration.WebSecurityManagerFactory;
import com.sun.enterprise.security.web.integration.WebSecurityManager;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DummyApplication;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.logging.LogDomains;


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * Security Deployer which generate and clean the security policies
 *
 */
@Service
public class SecurityDeployer extends SimpleDeployer<SecurityContainer, DummyApplication> {

    private static Logger _logger = null;
    @Inject
    private ServerContext serverContext;

    @Inject 
    private PolicyLoader policyLoader;
    static {
        _logger = LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
        initRoleMapperFactory();
        
    }
    // creates security policy if needed
    @Override
    protected void generateArtifacts(DeploymentContext dc)
            throws DeploymentException {
        generatePolicy(dc);
    }

    // removes security policy if needed
    @Override
    protected void cleanArtifacts(DeploymentContext dc)
            throws DeploymentException {
        removePolicy(dc);
    }
     
    @Override
    public DummyApplication load(SecurityContainer container, DeploymentContext context) {
        return new DummyApplication();
    }

    // TODO: need to add ear and standalone ejb module case
    protected void generatePolicy(DeploymentContext dc)
            throws DeploymentException {
        Properties params = dc.getCommandParameters();
        String appName = params.getProperty(DeployCommand.NAME);
        try {
            policyLoader.loadPolicy();
            Application app = dc.getModuleMetaData(Application.class);

            WebBundleDescriptor wbd = null;

            Set<WebBundleDescriptor> webDesc = app.getWebBundleDescriptors();
            Iterator<WebBundleDescriptor> iter = webDesc.iterator();
            if (iter.hasNext()) {
                wbd =  iter.next();
            }

            WebSecurityManagerFactory wsmf =
                    WebSecurityManagerFactory.getInstance();
            // this should create all permissions
            wsmf.newWebSecurityManager(wbd,serverContext);
            // for an application the securityRoleMapper should already be
            // created. I am just creating the web permissions and handing
            // it to the security component.
            String name = WebSecurityManager.getContextID(wbd);
            SecurityUtil.generatePolicyFile(name);

        } catch (IASSecurityException se) {
            String msg = "Error in generating security policy for " + appName;
            throw new DeploymentException(msg, se);
        }
    }

    // TODO: need to add ear and standalone ejb module case
    private void removePolicy(DeploymentContext dc) throws
            DeploymentException {
        Properties params = dc.getCommandParameters();
        String appName = params.getProperty(DeployCommand.NAME);

        try {
            WebSecurityManagerFactory wsmf =
                    WebSecurityManagerFactory.getInstance();
            String[] name = wsmf.getAndRemoveContextIdForWebAppName(appName);
            if (name != null) {
                if (name[0] != null) {
                    SecurityUtil.removePolicy(name[0]);
                    wsmf.removeWebSecurityManager(name[0]);
                }
            }
        } catch (IASSecurityException ex) {
            String msg = "Error in removing security policy for " + appName;
            _logger.log(Level.WARNING, msg, ex);
            throw new DeploymentException(msg, ex);
        }
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    //TODO: check if this is correct returning the Security Module def in getMetaData
    public MetaData getMetaData() {
        List<ModuleDefinition> apis = new ArrayList<ModuleDefinition>();
        Module module = modulesRegistry.makeModuleFor("org.glassfish:javax.javaee", "10.0-SNAPSHOT");
        if (module != null) {
            apis.add(module.getModuleDefinition());
        }
        
        module = modulesRegistry.makeModuleFor("org.glassfish.core:security", null);
        if (module != null) {
            apis.add(module.getModuleDefinition());
        }
        return new MetaData(false, apis.toArray(new ModuleDefinition[apis.size()]), null, new Class[] {Application.class});
    }

    private static void initRoleMapperFactory() //throws Exception
    {
        Object o = null;
        Class c = null;
        // this should never fail.
        try {
            c = Class.forName("com.sun.enterprise.security.acl.RoleMapperFactory");
            if (c != null) {
                o = c.newInstance();
                if (o != null && o instanceof SecurityRoleMapperFactory) {
                    SecurityRoleMapperFactoryMgr.registerFactory((SecurityRoleMapperFactory) o);
                }
            }
            if (o == null) {
            //               _logger.log(Level.SEVERE,_localStrings.getLocalString("j2ee.norolemapper", "Cannot instantiate the SecurityRoleMapperFactory"));
            }
        } catch (Exception cnfe) {
//            _logger.log(Level.SEVERE,
//			_localStrings.getLocalString("j2ee.norolemapper", "Cannot instantiate the SecurityRoleMapperFactory"), 
//			cnfe);
//		cnfe.printStackTrace();
//		throw new RuntimeException(cnfe);
        //   throw  cnfe;
        }
    }
}

