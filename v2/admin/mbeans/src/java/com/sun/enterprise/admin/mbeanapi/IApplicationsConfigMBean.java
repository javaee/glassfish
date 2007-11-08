/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.admin.mbeanapi;

import java.util.Properties;

import javax.management.MBeanException;
import javax.management.ObjectName;

import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.deployment.backend.DeploymentStatus;

public interface IApplicationsConfigMBean {
    
    public DeploymentStatus deploy(Properties props)
        throws MBeanException, DeploymentException;
   
    public DeploymentStatus undeploy(Properties props)
        throws MBeanException, DeploymentException;

    public boolean deployJ2EEApplication(Properties props)
        throws MBeanException, DeploymentException;
    
    public boolean deployEJBJarModule(Properties props) 
        throws MBeanException, DeploymentException; 
      
    public boolean deployWarModule(Properties props)
        throws MBeanException, DeploymentException;
                
    public boolean deployConnectorModule(Properties props)
        throws MBeanException, DeploymentException;
    
    public String[] getTargets() 
        throws MBeanException, MBeanConfigException;

    public String[] getAvailableModules(String moduleType) 
        throws MBeanException, MBeanConfigException;
    
    public String[] getAvailableModules(String moduleType, String[] targetList)
        throws MBeanException, MBeanConfigException;
    
    public String[] getRunningModules(String moduleType, String[] targetList)
        throws MBeanException, MBeanConfigException;
    
    public String[] getNonRunningModules(String moduleType, String[] targetList)
        throws MBeanException, MBeanConfigException;
    
    public String[] getDeployedJ2EEApplications() 
        throws MBeanException, ServerInstanceException;
      
    public String[] getDeployedEJBModules() 
        throws MBeanException, ServerInstanceException;

    public String[] getDeployedWebModules() 
        throws MBeanException, ServerInstanceException;
    
    public String[] getDeployedConnectors() 
        throws MBeanException, ServerInstanceException;
    
    public ObjectName[] getAllDeployedComponents() 
        throws MBeanException, ServerInstanceException;

    public ObjectName[] getAllDeployedJ2EEApplications() 
        throws MBeanException, ServerInstanceException;
    
    public ObjectName[] getAllDeployedEJBModules() 
        throws MBeanException, ServerInstanceException;
    
    public ObjectName[] getAllDeployedWebModules() 
        throws MBeanException, ServerInstanceException;
    
    
    public ObjectName[] getAllDeployedConnectors() 
        throws MBeanException, ServerInstanceException;
    
    public ObjectName[] getAllDeployedComponents(String target) 
        throws MBeanException, ServerInstanceException;
    
    public ObjectName[] getAllDeployedJ2EEApplications(String target) 
        throws MBeanException, ServerInstanceException;


    public ObjectName[] getAllDeployedEJBModules(String target) 
        throws MBeanException, ServerInstanceException;
 
    public ObjectName[] getAllDeployedWebModules(String target) 
        throws MBeanException, ServerInstanceException;
 
    public ObjectName[] getAllDeployedConnectors(String target) 
        throws MBeanException, ServerInstanceException;
    

    public ObjectName[] getAllDeployedAppclientModules(String target)
        throws MBeanException, ServerInstanceException;
    
    public void associate(String appName, String target)
        throws MBeanException, MBeanConfigException;
    
    public void disassociate(String appName, String target)
        throws MBeanException, MBeanConfigException;
    
    public void enable(String appName, String type, String target)
        throws MBeanException, MBeanConfigException, DeploymentException;

    public void disable(String appName, String type, String target)
        throws MBeanException, MBeanConfigException, DeploymentException;
    
    public boolean getStatus(String name, String target)
        throws MBeanException, MBeanConfigException;
    
    public boolean isRedeploySupported()
        throws MBeanException, MBeanConfigException;   
    
  
    public String[] getAvailableVersions(String appName, String type) 
        throws MBeanException, MBeanConfigException;
        
    public String getDefaultVersion(String appName, String type) 
        throws MBeanException, MBeanConfigException;
        
    public String getLastModified(String appName, String type) 
        throws MBeanException, MBeanConfigException;    
    
    public boolean isRepositoryCleanerEnabled() 
        throws MBeanException, MBeanConfigException;
    

    public int getRepositoryCleanerPollingInterval() 
        throws MBeanException, MBeanConfigException;
    
    public void setRepositoryCleanerPollingInterval(int interval)
        throws MBeanException, MBeanConfigException;
    
    public int getMaxApplicationVersions() 
        throws MBeanException, MBeanConfigException;
        
    public void setMaxApplicationVersions(int maxVersions)
        throws MBeanException, MBeanConfigException;
        
    public boolean isAutoDeployEnabled() 
        throws MBeanException, MBeanConfigException;
    
    public void setAutoDeployEnabled() 
        throws MBeanException, MBeanConfigException;

    public boolean isAutoDeployJspPreCompilationEnabled()
        throws MBeanException, MBeanConfigException;
    
    public void setAutoDeployJspPreCompilationEnabled()
        throws MBeanException, MBeanConfigException;
    
    public String[] getModuleComponents(String standAloneModuleName)
        throws MBeanException, ServerInstanceException;

    public String[] getModuleComponents(String appName, String modName) 
        throws MBeanException, ServerInstanceException;

    public String[] getAllSystemConnectors() 
        throws MBeanException, ServerInstanceException;    
}
