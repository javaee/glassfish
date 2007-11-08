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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Properties;
import java.util.Enumeration;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;

import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.deployment.backend.DeployableObjectInfo;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.ModuleContentLinker;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.SystemPropertyConstants;

public class WebModulesManager extends ModulesManager {
    public WebModulesManager(InstanceEnvironment env) throws ConfigException {
        super(env, true);
    }
    
    public WebModulesManager(InstanceEnvironment env, 
            boolean useBackupServerXml) throws ConfigException {
        super(env, useBackupServerXml);
        //FIXME: HACK START
	WebModule[] jArray = ((Applications)configBean).getWebModule();
	if(jArray!=null) {
            for(int i=0;i<jArray.length;i++) {
                jArray[i].setConfigContext(configContext);
		jArray[i].setXPath(ServerXPathHelper.getWebModuleIdXpathExpression(jArray[i].getName()));
            }
	}
	//FIXME: HACK END

    }
    
    /**
     * @return the module type this class is managing
     */
    public ModuleType getModuleType() {
        return ModuleType.WAR;
    }    

    private WebModule getWebModule(String modId) throws ConfigException {
        WebModule mod = (WebModule)
            ((Applications)this.configBean).getWebModuleByName(modId);
        
        if(mod == null)
            throw new ConfigException(Localizer.getValue(ExceptionType.NO_SUCH_WEB_MOD));
        
        return mod;
        
    }
    
    /**
     * Returns a list of all WebModule beans deployed on the server.
     */
    public WebModule[] listWebModules() throws ConfigException {
        WebModule[] modules = ((Applications)this.configBean).getWebModule();

        if (modules == null) { 
            return new WebModule[0];
        }
        
        ArrayList list = new ArrayList();
        for (int i=0; i<modules.length; i++) {
            // add the modules to the list if it is referenced
            // by this server
            if ( isReferenced(modules[i].getName()) ) {
                list.add(modules[i]);
            }
        }
        // returns an array of modules referenced by this server
        WebModule[] refList = new WebModule[list.size()];
        return ( (WebModule[]) list.toArray(refList) );
    }

    /**
     * Returns a list of (type java.lang.String) all web modules 
     * deployed to the server.
     */
    public List listIds() {

        ArrayList arr = new ArrayList();
        WebModule[] mods = ((Applications)this.configBean).getWebModule();

        // returns an empty list if there are no web modules in the domain
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
    
    public void remove(String modId) throws ConfigException {
        WebModule backEm = (WebModule)
            ((Applications)configBean).getWebModuleByName(modId);
        ((Applications)configBean).removeWebModule(backEm);
    }
    
    protected boolean isRegistered(String modId, ConfigBean bean) {
        ConfigBean cb = null;
        try {
            cb = ((Applications)bean).getWebModuleByName(modId);
        } catch(Exception cn) {
        }
        
        if(cb != null) return true;
        return false;
    }
        
    public boolean isEnabled(String modId)  throws ConfigException{
        return getWebModule(modId).isEnabled();
    }

    /**     
     * Checks whether this module is a systemmodule 
     * ResourceType in domain.xml should start with "system-"
     * @return true if resourceType starts with "system-"
     */     
    public boolean isSystem(String modId)  throws ConfigException{
        WebModule wm =  getWebModule(modId);
        String resourceType = wm.getObjectType();
        if(resourceType.startsWith(SYSTEM_PREFIX))
            return true;
        else
            return false;
    }

    /**
     * Checks whether this module is a system admin module
     * ResourceType in domain.xml should start with "system-admin"
     * @return true if resourceType starts with "system-admin"
     */
    public boolean isSystemAdmin(String modId)  throws ConfigException{
        WebModule wm =  getWebModule(modId);
        String resourceType = wm.getObjectType();
        if(resourceType.startsWith(SYSTEM_ADMIN_PREFIX))
            return true;
        else
            return false;
    }
    
   /**
     * Checks whether this module is a pre-deployed system module
     * ResourceType in domain.xml should start with "system"
     * Also it should be directory deployed
     * @return true if its a predeployed system module
     */
    public boolean isSystemPredeployed (String modId)  throws ConfigException{
        WebModule wm =  getWebModule(modId);
        String resourceType = wm.getObjectType();
        boolean isDirectoryDeployed = wm.isDirectoryDeployed();
        if (resourceType.startsWith(SYSTEM_PREFIX) && isDirectoryDeployed) { 
            return true;
        } else {
            return false;
        }
    }

    public void setEnable(String modId, boolean enable)  throws ConfigException{
        getWebModule(modId).setEnabled(enable);
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
            WebModule wm = getWebModule(modId);
            Enumeration tags = optionalAttributes.keys();
            while(tags.hasMoreElements())
            {
                String tag = (String)tags.nextElement();
                String value = optionalAttributes.getProperty(tag);
                wm.setAttributeValue(tag, value);
            }
        }
    }
    
    public String getLocation(String name) throws ConfigException {
        WebModule webModule = (WebModule)
            ((Applications)this.configBean).getWebModuleByName(name);
        String location = null;
        if (webModule != null)
            location = webModule.getLocation();
        return resolvePath(location);
    }

    public void setLocation(String name, String location) throws ConfigException {
        WebModule webModule = (WebModule)
            ((Applications)this.configBean).getWebModuleByName(name);
        if (webModule != null)
            webModule.setLocation(location);
    }

    // WBN 4-18-2002
	public String getJSPLocation(String name){
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.WEB); 
		return menv.getModuleJSPPath();
	}

    public String getGeneratedXMLLocation(String name){
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.WEB); 
        return menv.getModuleGeneratedXMLPath();
    }

	public String getStubLocation(String name){
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.WEB); 
		return menv.getModuleStubPath();
	}

    public String getDescription(String modId) throws ConfigException {
        return getWebModule(modId).getDescription();
    }
    
    public void setDescription(String modId, String desc) 
            throws ConfigException {
        getWebModule(modId).setDescription(desc);
    }
    
    public String getVirtualServers(String modId) throws ConfigException {
	Server server = ServerBeansFactory.getServerBean(configContext);
	ApplicationRef ar = server.getApplicationRefByRef(modId);
	return ar.getVirtualServers();
    }
   /* 
    public void setVirtualServers(String modId, String value) 
            throws ConfigException {
	Server server = ServerBeansFactory.getServerBean(configContext);
	ApplicationRef ar = server.getApplicationRefByRef(modId);
        ar.setVirtualServers(value);
    }
    */
    
    public String getContextRoot(String modId) throws ConfigException {
        return getWebModule(modId).getContextRoot();
    }
    
    public void setContextRoot(String modId, String value) 
            throws ConfigException {
        getWebModule(modId).setContextRoot(value);
    }

    /** 
     * Returns the deployment descriptor object for the specified web module.
     *
     * @param    modId    web module id
     * @param    cl       class loader to associate with the descriptors
     * @param    validateXml  use validating parser when true
     
     *
     * @return   the deployment descriptor object for this web module
     *
     * @throws   ConfigException if unable to load the deployment descriptor
     */
    public Application getDescriptor(String modId, ClassLoader cl, 
    		String moduleLoc, boolean validateXML) throws ConfigException {
        Application app = getDescriptor(modId, moduleLoc, validateXML);
	app.setClassLoader(cl);
	return app;
    }

    /** 
     * Returns the deployment descriptor object for the web module in the
     * specified directory.
     *
     * @param    modId        web module id
     * @param    modDir       The directory containing the web module
     *
     * @return   the deployment descriptor object for this web module
     *
     * @throws   ConfigException if unable to load the deployment descriptor
     */
    public Application getDescriptor(String modId, String modDir)
            throws ConfigException {

        return getDescriptor(modId, modDir, false);
    }

    /** 
     * Returns the deployment descriptor object for the web module in the
     * specified directory.
     *
     * @param    modId        web module id
     * @param    modDir       The directory containing the web module
     * @param    validateXml  use validating parser when true
     *
     * @return   the deployment descriptor object for this web module
     *
     * @throws   ConfigException if unable to load the deployment descriptor
     */
    public Application getDescriptor(String modId, String modDir,
            boolean validateXml) throws ConfigException {
            
        Application application = getRegisteredDescriptor(modId);
        if (application!=null) {
            return application;
        }
        try {
	    WebArchivist webArchivist = new WebArchivist();
	    webArchivist.setXMLValidation(validateXml);
	    
	    FileArchive archive = openDDArchive(modId, modDir);
            // Try to load the app from the serialized descriptor file.
            SerializedDescriptorHelper.Loader sdLoader = 
                    SerializedDescriptorHelper.load(modId, this);
            Application deserializedApplication = sdLoader.getApplication();
            if (deserializedApplication != null) {
                application = deserializedApplication;
            } else {
                application = ApplicationArchivist.openArchive(modId, webArchivist, archive, true);
                if(!isSystemAdmin(modId) && !isSystem(modId)) {
                    // we need to read persistence descriptors separately
                    // because they are read from appDir as oppsed to xmlDir.
                    readPersistenceDeploymentDescriptors(modDir, application);
                }
            }
            application.setGeneratedXMLDirectory(getGeneratedXMLLocation(modId));

            if (!application.getWebServiceDescriptors().isEmpty()) {
                ModuleContentLinker visitor = new ModuleContentLinker(archive, true);
                application.visit((com.sun.enterprise.deployment.util.ApplicationVisitor) visitor);
            }
            
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
