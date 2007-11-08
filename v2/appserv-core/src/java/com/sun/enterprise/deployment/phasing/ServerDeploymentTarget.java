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

/*
 * ServerDeploymentTarget.java
 *
 * Created on May 23, 2003, 3:50 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/ServerDeploymentTarget.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import java.util.ArrayList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.event.ModuleDeployEvent;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.target.ServerTarget;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;

/**
 * This class represents a server deployment target. 
 * @author  Sandhya E
 */
public class ServerDeploymentTarget extends ServerTarget implements DeploymentTarget {
    
    /** string manager */
    protected static final StringManager localStrings =
        StringManager.getManager( ServerDeploymentTarget.class );
    
    /** name of the target that this object represents */
    protected String thisTargetName = null;
    
    /** config context */
    protected ConfigContext configContext = null;
    
    /**
     * Creates a new instance of ServerDeploymentTarget 
     * @param configContext 
     * @param serverName name of the server this target represents
     */
    public ServerDeploymentTarget(ConfigContext configContext, String domainName, String serverName) {
        super(serverName, configContext);
        this.configContext = configContext;
        this.domainName = domainName;
        thisTargetName = serverName;
    }
    
    protected TargetType[] getValidTypes() {
        DeploymentTargetFactory tf = 
                DeploymentTargetFactory.getDeploymentTargetFactory();
        return tf.getValidDeploymentTargetTypes();
    }
    
    /**
     * Returns all the modules associated to this target of specified type and 
     * with specified status
     * @param type deployableObjectType of the modules to be listed
     * @param enabled if true only enabled modules are returned
     *                if false only disabled modules are returned
     *                if null all modules are returned
     * @throws DeploymentTargetException 
     */
    public String[] getModules(DeployableObjectType type, Boolean enabled)
        throws DeploymentTargetException 
    {
        try {
            return getModules(getAppsDeployedToServer(), type, enabled);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }
    
    public String[] getModules(String[] svrAppsList, DeployableObjectType type, 
        Boolean enabled) throws DeploymentTargetException {
        try {
            ArrayList returnList = new ArrayList();                        
            Applications appsConfigBean = (Applications) ConfigBeansFactory.getConfigBeanByXPath(
            configContext,
            ServerXPathHelper.XPATH_APPLICATIONS);
            if(type.isAPP()) {
                J2eeApplication[]  list = appsConfigBean.getJ2eeApplication();
                int i = 0;
                int k = 0;
                for(i=0; i< svrAppsList.length; i++) {
                    for(k =0 ; k > list.length ; k++) {
                        if(list[k].getName().equals(svrAppsList[i])) {
                            returnList.add(svrAppsList[i]);
                            break;
                        }
                    }
                }
            }
            else if(type.isEJB()) {
                EjbModule[] list = appsConfigBean.getEjbModule();
                for(int i=0; i< svrAppsList.length; i++) {
                    for(int k =0 ; k > list.length ; k++) {
                        if(list[k].getName().equals(svrAppsList[i])) {
                            returnList.add(svrAppsList[i]);
                            break;
                        }
                    }
                }
            }
            else if(type.isWEB()) {
                WebModule[] list = appsConfigBean.getWebModule();
                for(int i=0; i< svrAppsList.length; i++) {
                    for(int k =0 ; k > list.length ; k++) {
                        if(list[k].getName().equals(svrAppsList[i])) {
                            returnList.add(svrAppsList[i]);
                            break;
                        }
                    }
                }
            }
            else if(type.isCONN()) {
                ConnectorModule[] list = appsConfigBean.getConnectorModule();
                for(int i=0; i< svrAppsList.length; i++) {
                    for(int k = 0 ; k > list.length ; k++) {
                        if(list[k].getName().equals(svrAppsList[i])) {
                            returnList.add(svrAppsList[i]);
                            break;
                        }
                    }
                }
            }
            else if(type.isCAR()) {
                AppclientModule[] list = appsConfigBean.getAppclientModule();
                for(int i=0; i< svrAppsList.length; i++) {
                    for(int k = 0 ; k > list.length ; k++) {
                        if(list[k].getName().equals(svrAppsList[i])) {
                            returnList.add(svrAppsList[i]);
                            break;
                        }
                    }
                }
            }
            String[] returnValue = new String[returnList.size()];
            return (String[])returnList.toArray(returnValue);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }
  
    /**
     * An application-ref is added to the set of application refs of a server target. 
     * A new ref is added only in case it is not already present.
     * @param appName name of the app that is to be added as reference
     * @param enabled if true new reference is enabled, disabled if false
     * @return true if app-ref is added
     *         false if app-ref is not added as it is already there
     * @throws DeploymentTargetException if operation fails
     */
    public void addAppReference(String appName, boolean enabled, String virtualServers)
        throws DeploymentTargetException
    {
        try {
            if (!ServerHelper.serverReferencesApplication(configContext, 
                    getName(), appName)) {
                ApplicationReferenceHelper refHelper = new ApplicationReferenceHelper(
                    configContext);
                refHelper.createApplicationReference(getValidTypes(), getName(), 
                    enabled, virtualServers, appName);  
            }
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }
    
    /**
     * Specified application is dereferenced from the set of applications-refs of
     * this target.
     * @param appName name of the app that has to be dereferenced
     * @return true if app-ref has been removed
     *         false if app-ref is not there and hence could not be removed
     * @throws DeploymentTargetException if operation fails due to some exception
     */
    public void removeAppReference(String appName) 
        throws DeploymentTargetException {    
    
        try {
           ApplicationReferenceHelper refHelper = new ApplicationReferenceHelper(
                configContext);
            refHelper.deleteApplicationReference(getValidTypes(), getName(), appName);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }     
    }
    
    /**
     * Returns a list of all applications(including standalone) that are 
     * referenced by this server target
     * @return list of apps [an array of length 0 is returned 
     * @throws ConfigException
     */
    private String[] getAppsDeployedToServer() throws ConfigException
    {                        
        ApplicationRef[] apprefs = super.getApplicationRefs();       
        String[] appList = new String[apprefs.length];
        for(int i=0; i<apprefs.length; i++) {
            appList[i] = apprefs[i].getRef();
        }

        return appList;
    }
   
    /**
     * This method multicasts the deployment event to the remote target this object represents
     * @param eventType type of the event. It can be APPLICATION_DEPLOYED/APPLICATION_REDEPLOYED/ 
     *                  APPLICATION_UNDEPLOYED/MODULE_DEPLOYED/MODULE_UNDEPLOYED/MODULE_REDEPLOYED
     * @param appName name of the application/module that has been deployed/redeployed/undeployed
     * @param moduleType type of the module if it was a standalone module [ web/ejb/connector ]
     */
    public boolean sendStartEvent(int eventType, String appName, String moduleType)       
                  throws DeploymentTargetException {
       return sendStartEvent(eventType,appName, moduleType, false);
    }    

    /**
     * This method multicasts the deployment event to the remote target this object represents
     * @param eventType type of the event. It can be APPLICATION_DEPLOYED/APPLICATION_REDEPLOYED/
     *                  APPLICATION_UNDEPLOYED/MODULE_DEPLOYED/MODULE_UNDEPLOYED/MODULE_REDEPLOYED
     * @param appName name of the application/module that has been deployed/redeployed/undeployed
     * @param moduleType type of the module if it was a standalone module [ web/ejb/connector ]
     * @param isForced indicates if the deployment is forced.
     */
    public boolean sendStartEvent(int eventType, String appName, String moduleType, boolean isForced)       
                 throws DeploymentTargetException {
        try {
            return DeploymentServiceUtils.multicastEvent(eventType, appName, moduleType, false, isForced, thisTargetName);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }     
    }    
    
    public boolean sendStartEvent(int eventType, String appName, String moduleType, boolean isForced, int loadUnloadAction)
                 throws DeploymentTargetException {
        try {
            return DeploymentServiceUtils.multicastEvent(eventType, appName, moduleType, false, isForced, loadUnloadAction, thisTargetName);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }

    /**
     * This method multicasts the deployment event to the remote target this object represents
     * @param eventType type of the event. It can be APPLICATION_DEPLOYED/APPLICATION_REDEPLOYED/ 
     *                  APPLICATION_UNDEPLOYED/MODULE_DEPLOYED/MODULE_UNDEPLOYED/MODULE_REDEPLOYED
     * @param appName name of the application/module that has been deployed/redeployed/undeployed
     * @param moduleType type of the module if it was a standalone module [ web/ejb/connector ]
     */
    public boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade) 
        throws DeploymentTargetException {          
        try {    
            return DeploymentServiceUtils.multicastEvent(eventType, appName, moduleType, cascade, false, thisTargetName);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }   
    }    

    public boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade, boolean force)
        throws DeploymentTargetException {
        try {
            return DeploymentServiceUtils.multicastEvent(eventType, appName, moduleType, cascade, force, thisTargetName);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }
    
    public boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade, boolean force, int loadUnloadAction)
        throws DeploymentTargetException {
        try {
            return DeploymentServiceUtils.multicastEvent(eventType, appName, moduleType, cascade, force, loadUnloadAction, thisTargetName);
        } catch(Throwable t) {
            throw new DeploymentTargetException(t);
        }
    }

    public Target getTarget() 
    {
        return this;
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
        return localStrings.getString("enterprise.deployment.phasing.deploymenttarget.server.description", thisTargetName);        
    }

    private String getDomainName(){
        return domainName;
    }        


    /** admin domain name */
    private String domainName = null;
}
