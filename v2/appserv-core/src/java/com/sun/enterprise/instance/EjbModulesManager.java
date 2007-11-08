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
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.EjbArchivist;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleContentLinker;
import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.loader.InstrumentableClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.*;

//The RelativePathResolver is used to translate relative paths containing 
//embedded system properties (e.g. ${com.sun.aas.instanceRoot}/applications) 
//into absolute paths

import com.sun.enterprise.util.io.FileUtils;

public class EjbModulesManager extends ModulesManager{

    public EjbModulesManager(InstanceEnvironment env) throws ConfigException {
        super(env, true);
    }

    public EjbModulesManager(InstanceEnvironment env, 
            boolean useBackupServerXml) throws ConfigException {
        super(env, useBackupServerXml);
        //FIXME: HACK START
	EjbModule[] jArray = ((Applications)configBean).getEjbModule();
	if(jArray!=null) {
            for(int i=0;i<jArray.length;i++) {
                jArray[i].setConfigContext(configContext);
		jArray[i].setXPath(ServerXPathHelper.getEjbModuleIdXpathExpression(jArray[i].getName()));
            }
	}
	//FIXME: HACK END
    }
    
    /**
     * @return the module type this class is managing
     */
    public ModuleType getModuleType() {
        return ModuleType.EJB;
    }    

    /**
     * Returns an array of all ejb modules deployed with the server.
     */
    public EjbModule[] listEjbModules() {
        EjbModule[] mods = ((Applications)this.configBean).getEjbModule();
        if(mods == null) return new EjbModule[0];

        ArrayList list = new ArrayList();
        for (int i=0; i<mods.length; i++) {
            // add the modules to the list if it is referenced
            // by this server
            if ( isReferenced(mods[i].getName()) ) {
                list.add(mods[i]);
            }            
        }
        // returns an array of modules referenced by this server
        EjbModule[] refList = new EjbModule[list.size()];
        return ( (EjbModule[]) list.toArray(refList) );
    }

	/**
     * Returns a list of all ejb modules deployed with the server.
     */
    public List listIds() {
        ArrayList arr = new ArrayList();
        EjbModule[] mods = ((Applications)this.configBean).getEjbModule();
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
    
    private EjbModule getEjbModule(String modId) throws ConfigException {
        EjbModule mod = (EjbModule)
            ((Applications)this.configBean).getEjbModuleByName(modId);
        
        if(mod == null)
            throw new ConfigException(Localizer.getValue(ExceptionType.NO_SUCH_EJB_MOD));
        
        return mod;
        
    }
    
    public void remove(String modID) throws ConfigException {
        removeEjbModule(modID);
    }
    
     private void removeEjbModule(String modId) throws ConfigException {
        EjbModule backEm = (EjbModule)
            ((Applications)configBean).getEjbModuleByName(modId);
        ((Applications)configBean).removeEjbModule(backEm);
    }
    
    protected boolean isRegistered(String appId, ConfigBean bean) {
        ConfigBean cb = null;
        try {
            cb = ((Applications)bean).getEjbModuleByName(appId);
        } catch(Exception cn) {
        }
        
        if(cb != null) return true;
        return false;
    }
    
    public boolean isShared(String modId)  throws ConfigException{
        //FIXME NYI
        return false; //getEjbModule(modId).isShared();
    }
    
    public boolean isEnabled(String modId)  throws ConfigException{
        return getEjbModule(modId).isEnabled();
    }

    /**     
     * Checks whether this module is a systemmodule 
     * ResourceType in domain.xml should start with "system-"
     * @return true if resourceType starts with "system-"
     */     
    public boolean isSystem(String modId)  throws ConfigException{
        EjbModule em =  getEjbModule(modId);
        String resourceType = em.getObjectType();
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
        EjbModule em =  getEjbModule(modId);
        String resourceType = em.getObjectType();
        if(resourceType.startsWith(SYSTEM_ADMIN_PREFIX))
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
        EjbModule em =  getEjbModule(modId);
        String resourceType = em.getObjectType();
        boolean isDirectoryDeployed = em.isDirectoryDeployed();
        if (resourceType.startsWith(SYSTEM_PREFIX) && isDirectoryDeployed) {
            return true;
        } else {
            return false;
        }
    }

    
    public void setShared(String modId, boolean shared)  throws ConfigException{
         //FIXME NYI
        //getEjbModule(modId).setShared(shared);
    }
    
    public void setEnable(String modId, boolean enable)  throws ConfigException{
        getEjbModule(modId).setEnabled(enable);
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
            EjbModule em = getEjbModule(modId);
            Enumeration tags = optionalAttributes.keys();
            while(tags.hasMoreElements())
            {
                String tag = (String)tags.nextElement();
                String value = optionalAttributes.getProperty(tag);
                em.setAttributeValue(tag, value);
            }
        }
    }
    
    public String getLocation(String name) throws ConfigException {
        EjbModule ejbModule = (EjbModule)
            ((Applications)this.configBean).getEjbModuleByName(name);
        String location = ejbModule.getLocation();
        return resolvePath(location);
    }

	/**
     * Set the location for an EJB Module
     *
     * @param modId unique idenitifier for the ejb module
     * @param location full path String
     */
    
	public void setLocation(String modId, String location)  throws ConfigException{
        getEjbModule(modId).setLocation(location);
    }
   
    public String getStubLocation(String name) {
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.EJB); 
        return menv.getModuleStubPath();
    }
    
    public String getGeneratedXMLLocation(String name){
        ModuleEnvironment menv = instanceEnvironment.getModuleEnvironment(name,
                                                     DeployableObjectType.EJB);        
        return menv.getModuleGeneratedXMLPath();
    } 
    
    public String getDescription(String modId) throws ConfigException {
        return getEjbModule(modId).getDescription();
    }
    
    public void setDescription(String modId, String desc) 
            throws ConfigException {
        getEjbModule(modId).setDescription(desc);
    }

    /**
     * This method is called only by ResourcesUtil during server start up.
     * We need to construct the application classloader before calling into
     * other getDescriptor method to fully populate the application object.
     */
    public Application getDescriptor(
        String moduleID, ClassLoader parentClassLoader) 
            throws ConfigException {

        ClassLoader cl = new EJBClassLoader(parentClassLoader);
        String[] classPaths = (String[]) EJBClassPathUtils.getModuleClasspath(
                                moduleID, null, this).toArray(new String[0]);

        if (classPaths != null) {
            int classPathSize    = classPaths.length;
            for (int i=0; i<classPathSize; i++) {
                try {
                    ((EJBClassLoader) cl).appendURL(new File(classPaths[i]));
                } catch (IOException ioe) {
                    //@@ i18n
                    _logger.log(Level.WARNING, "Cannot convert path to URL: " + classPaths[i]);
                }
            }
        }

        return getDescriptor(moduleID, cl, false);
    }

    /** 
     * Returns the deployment descriptor object for this ejb module.
     *
     * @param    modId        ejb module id
     * @param    cl           ejb class loader
     * @param    validateXml  use validating parser when true
     *
     * @return   the deployment descriptor object for this ejb module
     *
     * @throws   ConfigException  if unable to load the deployment descriptor
     */
    public Application getDescriptor(String modId, ClassLoader cl, 
    		String loc, boolean validateXML) throws ConfigException {
        
        return getDescriptor(modId, cl, validateXML);
    }

    /** 
     * Returns the deployment descriptor object for this ejb module. This
     * method gets called when deployment backend is running verification 
     * during deployment.
     *
     * @param    modId        ejb module id
     * @param    cl           ejb class loader
     * @param    validateXml  use validating parser when true
     * @param    verify       sets cmp mappings, doctype, etc when true 
     *
     * @return   the deployment descriptor object for this ejb module
     *
     * @throws   ConfigException  if unable to load the deployment descriptor
     */
    public Application getDescriptor(String modId, ClassLoader cl,
            boolean validateXml) throws ConfigException {

        Application application = getRegisteredDescriptor(modId);
        if (application!=null) {
            application.setClassLoader(cl);
            return application;
        }
        try {
            String moduleDir             = getLocation(modId);
	    EjbArchivist ejbArchivist = new EjbArchivist();
	    ejbArchivist.setXMLValidation(validateXml);

            // We need to use a temp CL until we are done with validate(),
            // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=223
            ClassLoader tcl = (cl instanceof InstrumentableClassLoader) ?
                    InstrumentableClassLoader.class.cast(cl).copy() : cl;
            ejbArchivist.setClassLoader(tcl);

	    FileArchive archive = openDDArchive(modId, moduleDir);
            
            // Try to load the app from the serialized descriptor file.
            SerializedDescriptorHelper.Loader sdLoader =
                    SerializedDescriptorHelper.load(modId, this);
            Application deserializedApplication = sdLoader.getApplication();
            if (deserializedApplication != null) {
                application = deserializedApplication;
            } else {
                application = ApplicationArchivist.openArchive(modId, ejbArchivist, archive, true);
                if(!isSystemAdmin(modId) && !isSystem(modId)) {
                    // we need to read persistence descriptors separately
                    // because they are read from appDir as oppsed to xmlDir.
                    readPersistenceDeploymentDescriptors(moduleDir, application);
                }
            }

            // We need to use a temp CL until the end of this method...
            // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=223
            application.setClassLoader(tcl);
            // set it in all the bundles as well,
            for (BundleDescriptor bd : application.getBundleDescriptors()) {
                bd.setClassLoader(tcl);
            }
            application.setGeneratedXMLDirectory(getGeneratedXMLLocation(modId));
            
            if (!application.getWebServiceDescriptors().isEmpty()) {
                ModuleContentLinker visitor = new ModuleContentLinker(archive, true);
                application.visit((com.sun.enterprise.deployment.util.ApplicationVisitor) visitor);
            }

            // Now, let's set the actual class loader
            application.setClassLoader(cl);
            // set it in all the bundles as well,
            for (BundleDescriptor bd : application.getBundleDescriptors()) {
                bd.setClassLoader(cl);
            }
            registerDescriptor(modId, application);

            // If needed, save this app in serialized form for faster loading next time.
            sdLoader.store(application);
            
            return application;
        } catch (ConfigException ce) {
            throw ce;
        } catch (IOException ioe) {
            throw new ConfigException(Localizer.getValue(
                ExceptionType.IO_ERROR_LOADING_DD, modId), ioe);
        } catch (Throwable t) {
            throw new ConfigException(Localizer.getValue(
                        ExceptionType.FAIL_DD_LOAD, modId), t);
        }
    }
}
