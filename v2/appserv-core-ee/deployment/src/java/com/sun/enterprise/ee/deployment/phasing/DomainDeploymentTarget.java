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
package com.sun.enterprise.ee.deployment.phasing;

import java.util.ArrayList;

import com.sun.enterprise.deployment.phasing.DeploymentTarget;
import com.sun.enterprise.deployment.phasing.ServerDeploymentTarget;
import com.sun.enterprise.deployment.phasing.DeploymentTargetException;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.target.DomainTarget;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.server.Constants;

/**
 * Represents a group deployment target in SE/EE.
 * 
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public final class DomainDeploymentTarget extends DomainTarget implements DeploymentTarget {

    /** string manager */
    protected static final StringManager localStrings =
        StringManager.getManager( DomainDeploymentTarget.class );
    
    /** name of the target that this object represents */
    protected String thisTargetName = null;
    
    /** config context */
    protected ConfigContext configContext = null;
    
    /** 
     * Creates a new instance of GroupDeploymentTarget 
     * @param configContext config context object
     * @param groupName naem of the group that this object represents
     */
    public DomainDeploymentTarget(ConfigContext configContext, String domainName, String targetName) 
    {
        super(targetName, configContext);
        this.configContext = configContext;
        this.thisTargetName = targetName;  
        this.domainName = domainName;
    }
    

    /**
     * This method is used to get running/nonrunning/all modules of a certain type
     * on this target. Modules from one server are returned as all servers of a 
     * group are homogenous
     * Currently as we are having only homogenous group this method works on one server 
     * of group instead of all, as all servers will have the same list of modules
     * @param type ear/war/jar/rar
     * @param enabled true/false/null
     * @return list of modules
     * @throws DeploymentTargetException if operation fails
     */
    public String[] getModules(DeployableObjectType type, Boolean enabled) 
        throws DeploymentTargetException
    {
        //FIXTHIS. It is not clear whether we should return all modules in the domain.
        //This is what we are now doing.
        try {
            ServerDeploymentTarget sdTarget = new ServerDeploymentTarget(configContext, domainName, "");
            return sdTarget.getModules(getAppsInDomain(), type, enabled);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }  
    } 
    
    /**
     * Adds application reference to all servers part of this group
     * @param appName application/module name
     * @param enabled if true app-ref is enabled
     * @throws DeploymentTargetException if operation fails
     */
    public void addAppReference(String appName, boolean enabled, 
        String virtualServers) throws DeploymentTargetException 
    {
        //When deploying to the domain, there are no references that need to be added.
    }
    
    /**
     * Removes application reference from all servers part of this group
     * @param appName application/module name
     * @throws DeploymentTargetException if operation fails
     */
    public void removeAppReference(String appName) 
        throws DeploymentTargetException
    {
        //When undeploying from the domain, we assume that all app references have been 
        //removed.
    }
    
    /**
     * Sends application/module start stop events to the target
     * @param eventType APPLICATION_DEPLOYED/APPLICATION_UNDEPLOYED/APPLICATION_REDEPLOYED
     *                  MODULE_DEPLOYED,MODULE_REDEPLOYED,MODULE_UNDEPLOYED
     * @param appName name of the application
     * @param moduleType ejb/web/connector
     */
    public boolean sendStartEvent(int eventType, String appName, String moduleType)
        throws DeploymentTargetException
    {
        return sendStartEvent(eventType, appName, moduleType, false);
    }

    public boolean sendStartEvent(int eventType, String appName, String moduleType, boolean isForced) throws DeploymentTargetException {
        return sendStartEvent(eventType, appName, moduleType, isForced, 
        Constants.LOAD_UNSET);
    }

   public boolean sendStartEvent(int eventType, String appName, String moduleType, boolean isForced, int loadUnloadAction) throws DeploymentTargetException {
        try {
            boolean domainResult = true;
            String[] svrRef = getServersReferencingApp(appName);
            for(int i = 0; i < svrRef.length ; i++) {
                ServerDeploymentTarget sdTarget = new ServerDeploymentTarget(configContext, domainName, svrRef[i]);
                boolean serverResult = sdTarget.sendStartEvent(eventType, appName, moduleType, isForced, loadUnloadAction);
                if (!serverResult) {
                    domainResult = false;
                }
            }
            return domainResult;
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }
    
    /**
     * Sends application/module start stop events to the target
     * @param eventType APPLICATION_DEPLOYED/APPLICATION_UNDEPLOYED/APPLICATION_REDEPLOYED
     *                  MODULE_DEPLOYED,MODULE_REDEPLOYED,MODULE_UNDEPLOYED
     * @param appName name of the application
     * @param moduleType ejb/web/connector
     */
    public boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade)
        throws DeploymentTargetException
    {
        try {
            String[] svrRef = getServersReferencingApp(appName);
            for(int i = 0; i < svrRef.length ; i++) {
                ServerDeploymentTarget sdTarget = new ServerDeploymentTarget(configContext, domainName, svrRef[i]);
                sdTarget.sendStopEvent(eventType, appName, moduleType, cascade);
            }
            return true;
        } catch(Throwable t) {         
            throw new DeploymentTargetException(t);
        }          
    }
    
    public boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade, boolean force)
        throws DeploymentTargetException
    {   
        try {
            String[] svrRef = getServersReferencingApp(appName);
            for(int i = 0; i < svrRef.length ; i++) {
                ServerDeploymentTarget sdTarget = new ServerDeploymentTarget(configContext, domainName, svrRef[i]);
                sdTarget.sendStopEvent(eventType, appName, moduleType, cascade, force);               
            }
            return true;
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }          
    }   

    public boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade, boolean force, int loadUnloadAction)
        throws DeploymentTargetException
    {
        try {
            String[] svrRef = getServersReferencingApp(appName);
            for(int i = 0; i < svrRef.length ; i++) {
                ServerDeploymentTarget sdTarget = new ServerDeploymentTarget(configContext, domainName, svrRef[i]);
                sdTarget.sendStopEvent(eventType, appName, moduleType, cascade, force, loadUnloadAction);
            }
            return true;
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }

    public Target getTarget() 
    {
        return this;
    }

    /**
     * Returns server names of all servers part of this group. If group name is 
     * "domain" all servers under this domain, whether part of a group or standalone
     * Groupname will be "domain" in a case when target is not specified in the request.
     * are returned
     * @return list of server names
     */
    private String[] getServersReferencingApp(String appName) throws ConfigException 
    {                 
        //Return the list of servers referencing the given application
        Server[] servers = ServerHelper.getServersReferencingApplication(configContext, appName);
        final int size = servers.length;
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = servers[i].getName();
        }
        return result;
    }
    
    private String[] getAppsInDomain() throws ConfigException
    {
        return ApplicationHelper.getApplicationsInDomain(configContext);
    }
    
    /**
     * Returns name of the target
     * @return name 
     */
    public String getName() {
        return thisTargetName;
    }
  
    /**
     * Returns description of this target object
     * @return description
     */
    public String getDescription() {
        return localStrings.getString("enterprise.deployment.phasing.deploymenttarget.domain.description", thisTargetName);        
    }

    /** admin domain name */
    protected String domainName = null;    
}
