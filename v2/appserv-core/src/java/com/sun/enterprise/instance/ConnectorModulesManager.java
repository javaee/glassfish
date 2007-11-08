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
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.ConnectorArchivist;
import com.sun.enterprise.deployment.backend.DeployableObjectInfo;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.Switch;
import com.sun.enterprise.connectors.util.ResourcesUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.Properties;
import java.util.Set;

public class ConnectorModulesManager extends ModulesManager {
    public ConnectorModulesManager(InstanceEnvironment env) throws ConfigException {
        super(env, true);
    }
    
    public ConnectorModulesManager(InstanceEnvironment env, boolean useBackupServerXml) throws ConfigException {
        super(env, useBackupServerXml);
        //FIXME: HACK START
	ConnectorModule[] jArray = ((Applications)configBean).getConnectorModule();
	if(jArray!=null) {
            for(int i=0;i<jArray.length;i++) {
                jArray[i].setConfigContext(configContext);
		jArray[i].setXPath(ServerXPathHelper.getConnectorModuleIdXpathExpression(jArray[i].getName()));
            }
	}
	//FIXME: HACK END

    }
    
    /**
     * @return the module type this class is managing
     */
    public ModuleType getModuleType() {
        return ModuleType.RAR;
    }    

    /**
     * Returns a list of all Connector modules deployed with the server.
     */
    public ConnectorModule[] listConnectorModules() {
        ConnectorModule[] mods = ((Applications)this.configBean).getConnectorModule();
        if(mods == null) return new ConnectorModule[0];

        ArrayList list = new ArrayList();
        for (int i=0; i<mods.length; i++) {
            // add the modules to the list if it is referenced
            // by this server
            if ( isReferenced(mods[i].getName()) ) {
                list.add(mods[i]);
            }
        }
        // returns an array of modules referenced by this server
        ConnectorModule[] refList = new ConnectorModule[list.size()];
        return ( (ConnectorModule[]) list.toArray(refList) );
	}    
	
	/**
     * Returns a list of all Connector modules deployed with the server.
     */
    public List listIds() {
        ArrayList arr = new ArrayList();
        ConnectorModule[] mods = ((Applications)this.configBean).getConnectorModule();
        if(mods == null) return arr;
        for (int i=0;i<mods.length;i++) {          
            String name = mods[i].getName();
            // adds the web module to the list if 
            // it is referenced by this server 
            if ( isReferenced(name) ) {
                arr.add(name);
            }            
        }
        return arr;
    }
    
    private ConnectorModule getConnectorModule(String modId) throws ConfigException {
        ConnectorModule mod = (ConnectorModule)
            ((Applications)this.configBean).getConnectorModuleByName(modId);
        
        if(mod == null)
            throw new ConfigException(Localizer.getValue(ExceptionType.NO_SUCH_CON_MOD));
        
        return mod;
        
    }
    
    public String getGeneratedXMLLocation(String name){
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.CONN);        
        return menv.getModuleGeneratedXMLPath();
    } 

    public void remove(String modId) throws ConfigException {
        ConnectorModule backEm = (ConnectorModule)((Applications)configBean).getConnectorModuleByName(modId);
        ((Applications)configBean).removeConnectorModule(backEm);
    }
    
    protected boolean isRegistered(String modId, ConfigBean bean) {
        ConfigBean cb = null;
        try {
            cb = ((Applications)bean).getConnectorModuleByName(modId);
        } catch(Exception cn) {
        }
        
        if(cb != null) return true;
        return false;
    }
    
    public boolean isEnabled(String modId)  throws ConfigException{
        return getConnectorModule(modId).isEnabled();
    }

    /**     
     * Checks whether this module is a systemmodule 
     * ResourceType in domain.xml should start with "system-"
     * @return true if resourceType starts with "system-"
     */     
    public boolean isSystem(String modId)  throws ConfigException{
        ConnectorModule cm =  getConnectorModule(modId);
        String resourceType = cm.getObjectType();
        if(resourceType.startsWith(SYSTEM_PREFIX))
            return true;
        else
            return false;
    }

   /**
     * Checks whether this module is a pre-deployed system module
     * ResourceType in domain.xml should start with "system"
     * Also it should be directory deployed.
     * @return true if its a predeployed system module
     */
    public boolean isSystemPredeployed (String modId)  throws ConfigException{
        ConnectorModule cm =  getConnectorModule(modId);
        String resourceType = cm.getObjectType();
        boolean isDirectoryDeployed = cm.isDirectoryDeployed();
        if (resourceType.startsWith(SYSTEM_PREFIX) && isDirectoryDeployed) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Checks whether this module is a system admin module
     * ResourceType in domain.xml should start with "system-admin"
     * @return true if resourceType starts with "system-admin"
     */
    public boolean isSystemAdmin(String modId)  throws ConfigException{
        ConnectorModule cm =  getConnectorModule(modId);
        String resourceType = cm.getObjectType();
        if(resourceType.startsWith(SYSTEM_ADMIN_PREFIX))
            return true;
        else
            return false;
    }

    public void setEnable(String modId, boolean enable)  throws ConfigException{
        getConnectorModule(modId).setEnabled(enable);
    }
    
	/**
     * Set the optional attributes for an module
     *
     * @param modId unique idenitifier for the module
     * @param optionalAttributes - pairs tag/value to set
     */
    public void setOptionalAttributes(String modId, Properties optionalAttributes)
            throws ConfigException {
        if(optionalAttributes!=null) {
            ConnectorModule cm = getConnectorModule(modId);
            Enumeration tags = optionalAttributes.keys();
            while(tags.hasMoreElements())
            {
                String tag = (String)tags.nextElement();
                String value = optionalAttributes.getProperty(tag);
                cm.setAttributeValue(tag, value);
            }
        }
    }
    
    public String getLocation(String name) {
        String location  = null;
        if (ResourcesUtil.createInstance().belongToSystemRar(name)) {
            location = Switch.getSwitch().
                       getResourceInstaller().getSystemModuleLocation(name);
        } else {
            ConnectorModule connectorModule = (ConnectorModule)
            ((Applications)this.configBean).getConnectorModuleByName(name);
            location = connectorModule.getLocation();
        }
        return resolvePath(location);
    }

    public void setLocation(String modId, String location)  throws ConfigException{
        ConnectorModule connectorModule = (ConnectorModule)
        ((Applications)this.configBean).getConnectorModuleByName(modId);
        connectorModule.setLocation(location);
    }
    
    public String getDescription(String modId) throws ConfigException {
        return getConnectorModule(modId).getDescription();
    }
    
    public void setDescription(String modId, String desc) throws ConfigException {
        getConnectorModule(modId).setDescription(desc);
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
    public Application getDescriptor(String modId, ClassLoader cl,
            String modDir, boolean validateXml) throws ConfigException {

        Application application = getRegisteredDescriptor(modId);
        if (application!=null) {
            application.setClassLoader(cl);
            return application;
        }                
        try {
	    ConnectorArchivist connectorArchivist = new ConnectorArchivist();
	    connectorArchivist.setXMLValidation(validateXml);
            connectorArchivist.setClassLoader(cl);
	    
            FileArchive archive = new FileArchive();
            archive.open(modDir);
            // Try to load the app from the serialized descriptor file.
            SerializedDescriptorHelper.Loader sdLoader = SerializedDescriptorHelper.load(modId, this);
            Application deserializedApplication = sdLoader.getApplication();
            if (deserializedApplication != null) {
                application = deserializedApplication;
            } else {
                application = ApplicationArchivist.openArchive(modId, connectorArchivist, archive, true);
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
