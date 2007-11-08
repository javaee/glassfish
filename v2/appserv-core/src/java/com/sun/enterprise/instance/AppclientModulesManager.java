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

package com.sun.enterprise.instance;

import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.backend.DeployableObjectInfo;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.Properties;
import java.util.Set;

//The RelativePathResolver is used to translate relative paths containing 
//embedded system properties (e.g. ${com.sun.aas.instanceRoot}/applications) 
//into absolute paths
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.util.io.FileUtils;

/**
 * Provides access to app client information per server instance.
 */
public class AppclientModulesManager extends ModulesManager {
    
    public AppclientModulesManager(InstanceEnvironment env) throws ConfigException {
        super(env, true);
    }
    
    public AppclientModulesManager(InstanceEnvironment env, boolean useBackupServerXml) throws ConfigException {
        super(env, useBackupServerXml);
        
        //FIXME: HACK START
	AppclientModule[] jArray = ((Applications)configBean).getAppclientModule();
	if(jArray!=null) {
            for(int i=0;i<jArray.length;i++) {
                jArray[i].setConfigContext(configContext);
		//jArray[i].setXPath(ServerXPathHelper.getAppIdXpathExpression(jArray[i].getName()));
		jArray[i].setXPath(ServerXPathHelper.getAppClientModuleIdXpathExpression(jArray[i].getName()));
            }
	}
	//FIXME: HACK END
    }    
        
    /**
     * @return the module type this class is managing
     */
    public ModuleType getModuleType() {
        return ModuleType.CAR;
    }
        
    /**
     * Returns an array of all applications deployed with the server.
     */
    public AppclientModule[] getAllApps() {
        AppclientModule[] apps = ((Applications)this.configBean).getAppclientModule();
        if(apps == null) return new AppclientModule[0];
        
        ArrayList list = new ArrayList();
        for (int i=0; i<apps.length; i++) {            
            // add the modules to the list if it is referenced
            // by this server
            if ( isReferenced(apps[i].getName()) ) {
                list.add(apps[i]);
            }
        }
        // returns an array of modules referenced by this server
        AppclientModule[] refList = new AppclientModule[list.size()];
        return ( (AppclientModule[]) list.toArray(refList) );      
	}
    
    /**
     * Returns a list of all applications deployed with the server.
     */
    public List listIds() {
        ArrayList arr = new ArrayList();
        AppclientModule[] apps = ((Applications)this.configBean).getAppclientModule();
        if(apps == null) return arr;
            
        for (int i=0;i<apps.length;i++) {
            String name = apps[i].getName();
            // adds the web module to the list if 
            // it is referenced by this server             
            if ( isReferenced(name) ) {
                arr.add(name);
            }
        }        
        return arr;
    }
    
    public String getLocation(String appId) throws ConfigException {
        AppclientModule app = (AppclientModule)
        ((Applications)this.configBean).getAppclientModuleByName(appId);
        return resolvePath(app.getLocation());
    }
    
    
    
    /**
     * There is no persistent state store in domain.xml
     * Hence, return true always.
     *
     */
    public boolean isEnabled(String appId) throws ConfigException {
        return true;
    }

    /**
     *Reports whether Java Web Start access is enabled for the specified app client.
     *@param appId the module ID of the app client to check
     *@return boolean indicating whether access is permitted
     */
    public boolean isJavaWebStartEnabled(String appId) throws ConfigException {
        AppclientModule app = (AppclientModule) ((Applications)this.configBean).getAppclientModuleByName(appId);
        return app.isJavaWebStartEnabled();
    }
    
    /**
     * Checks whether this application is a system app
     *
     * appclients are always non-system apps
     *	
     * @return false
     */
    public boolean isSystem(String appId) throws ConfigException {
        return false;
    }

    /**
     * Checks whether this application is a system admin app
     *
     * appclients are always non-system apps
     *
     * @return false
     */
    public boolean isSystemAdmin(String appId) throws ConfigException {
        return false;
    }

    public boolean isSystemPredeployed(String appId) throws ConfigException {
        return false;
    }

    public String getStubLocation(String name) {
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.CAR); 
        return menv.getModuleStubPath();
    }    
    
    public String getGeneratedXMLLocation(String name){
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.CAR); 
        return menv.getModuleGeneratedXMLPath();
    }

    protected boolean isRegistered(String appId, ConfigBean bean) {
        ConfigBean cb = null;
        try {
            cb = ((Applications)bean).getAppclientModuleByName(appId);
        } catch(Exception cn) {
        }
        
        if(cb != null) return true;
        return false;
    }
    
    /**
     * Removes the application information from the configuration file.
     *
     * @param appId a unique identifier for the application
     */
    public void remove(String appId) throws ConfigException {
        AppclientModule backJa = (AppclientModule)
            ((Applications)configBean).getAppclientModuleByName(appId);
        ((Applications)configBean).removeAppclientModule(backJa);
    }
    
    /**
     * Identifies whether an application is enabled or disabled.
     *
     * Appclients do not need to have a persistent enable state
     * Hence, this method does nothing for now.
     *
     * @param appId unique idenitifier for the application
     * @param isEnabled flag for enabling or disabling the application
     */
    public void setEnable(String appId, boolean enabled)
            throws ConfigException {
        //Enabled is not persisted for appclients.
    }

	/**
     * Set the location for an App
     *
     * @param appId unique idenitifier for the application
     * @param location
     */
    public void setLocation(String appId, String location)
            throws ConfigException {
        getAppclientModule(appId).setLocation(location);
    }
    
    
    /**
     * Set the optional attributes for an App
     *
     * This method is required by the base class.
     * Since there are no optional attributes for appclient
     * don't do anything in this method
     *
     * @param appId unique idenitifier for the application
     * @param optionalAttributes - pairs tag/value to set
     */
    public void setOptionalAttributes(String appId, Properties optionalAttributes)
            throws ConfigException {
                //Not required for appclient.
    }
        
    public String getDescription(String id) throws ConfigException {
        return getAppclientModule(id).getDescription();
    }
    
    private AppclientModule getAppclientModule(String appId)
            throws ConfigException {

        AppclientModule app = (AppclientModule)
            ((Applications)this.configBean).getAppclientModuleByName(appId);

        if(app == null)
			throw new ConfigException(
                Localizer.getValue(ExceptionType.APP_NOT_EXIST));
        return app;
        
    }
    
    public void setDescription(String id, String desc) throws ConfigException {
        getAppclientModule(id).setDescription(desc);
    }
    
    /** 
     * Returns the deployment descriptor object for the connector module
     * in the specified directory.
     *
     * @param    modId        web module id
     * @param    modDir       The directory containing the web module
     * @param    validateXml  use validating parser when true
     * @param    verify       true indicates that verifier option is ON
     *
     * @return   the deployment descriptor object for this web module
     *
     * @throws   ConfigException if unable to load the deployment descriptor
     */
     public Application getDescriptor(String modId, ClassLoader cl, String modDir,
     	 boolean validateXml) throws ConfigException {
	 
        Application application = getRegisteredDescriptor(modId);
        if (application!=null) {
            application.setClassLoader(cl);
            return application;
        }             
	 try {
	     AppClientArchivist appClientArchivist = new AppClientArchivist();
	     appClientArchivist.setXMLValidation(validateXml);
             appClientArchivist.setClassLoader(cl);
	     
	     FileArchive archive = openDDArchive(modId, modDir);
             
             // Try to load the app from the serialized descriptor file.
             SerializedDescriptorHelper.Loader sdLoader =
                     SerializedDescriptorHelper.load(modId, this);
             Application deserializedApplication = sdLoader.getApplication();
             if (deserializedApplication != null) {
                 application = deserializedApplication;
             } else {
                application = ApplicationArchivist.openArchive(modId, appClientArchivist, archive, true);
             }
	     application.setClassLoader(cl);
             application.setGeneratedXMLDirectory(getGeneratedXMLLocation(modId));
             registerDescriptor(modId, application);
             
            // If needed, save this app in serialized form for faster loading next time.
            sdLoader.store(application);
            
            return application;	     
	 } catch (IOException ioe) {
	     throw new ConfigException(Localizer.getValue(
	     ExceptionType.IO_ERROR_LOADING_DD, modId), ioe);
	 } catch (Throwable t) {
	     throw new ConfigException(Localizer.getValue(
	     ExceptionType.FAIL_DD_LOAD, modId), t);
	 }
     }   
}
