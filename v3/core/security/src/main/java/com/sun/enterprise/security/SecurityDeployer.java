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

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.deployment.common.IASDeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DummyApplication;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.logging.LogDomains;
import com.sun.web.security.WebSecurityManager;
import com.sun.web.security.WebSecurityManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Service;

/**
 * Security Deployer which generate and clean the security policies
 *
 */
@Service
public class SecurityDeployer extends SimpleDeployer 
        <SecurityContainer, DummyApplication>  {

    private static Logger _logger=null;
    
    static {
        _logger=LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    }
    // creates security policy if needed
    @Override
    protected void generateArtifacts(DeploymentContext dc) 
        throws IASDeploymentException {
        generatePolicy(dc);
    }

    // removes security policy if needed
    @Override
    protected void cleanArtifacts(DeploymentContext dc)
        throws IASDeploymentException {
        removePolicy(dc);
    }

    // TODO: need to add ear and standalone ejb module case
    protected void generatePolicy(DeploymentContext dc) 
        throws IASDeploymentException {
        Properties params = dc.getCommandParameters();
        String appName = params.getProperty(DeployCommand.NAME);

        try {
            Application app = dc.getModuleMetaData(Application.class);
            // standalone web module case
            if (app.isVirtual()) {
                WebBundleDescriptor wbd = 
                    (WebBundleDescriptor) app.getStandaloneBundleDescriptor(); 

                WebSecurityManagerFactory wsmf = 
                    WebSecurityManagerFactory.getInstance();
                // this should create all permissions
                wsmf.newWebSecurityManager(wbd);
                // for an application the securityRoleMapper should already be 
                // created. I am just creating the web permissions and handing 
                // it to the security component.
                String name = WebSecurityManager.getContextID(wbd) ;
                SecurityUtil.generatePolicyFile(name);
            }
        } catch(IASSecurityException se){
            String msg = "Error in generating security policy for " + appName;
            throw new IASDeploymentException(msg, se);         
        }
    }

    // TODO: need to add ear and standalone ejb module case
    private void removePolicy(DeploymentContext dc) throws 
        IASDeploymentException {
        Properties params = dc.getCommandParameters();
        String appName = params.getProperty(DeployCommand.NAME);

        try {                
            WebSecurityManagerFactory wsmf = 
                WebSecurityManagerFactory.getInstance();
            String[] name
                = wsmf.getAndRemoveContextIdForWebAppName(appName);
            if (name != null){
                if(name[0] != null) {
                    SecurityUtil.removePolicy(name[0]);
                    wsmf.removeWebSecurityManager(name[0]);
                }
            }
        } catch(IASSecurityException ex) {
            String msg = "Error in removing security policy for " + appName;
            _logger.log(Level.WARNING, msg, ex);
            throw new IASDeploymentException(msg, ex); 
        }        
    }

    //TODO: check if this is correct returning the Security Module def in getMetaData
    public MetaData getMetaData() {
        List<ModuleDefinition> apis = new ArrayList<ModuleDefinition>();
        Module module = modulesRegistry.makeModuleFor("javax.javaee:javaee", "5.0");
        if (module!=null) {
            apis.add(module.getModuleDefinition());
        }
        module = modulesRegistry.makeModuleFor("org.glassfish.core.security:core-security", null);
        if (module!=null) {
            apis.add(module.getModuleDefinition());
        }
        return new MetaData(false, apis.toArray(new ModuleDefinition[apis.size()]));
    }
}

