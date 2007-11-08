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
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleContentLinker;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.loader.InstrumentableClassLoader;

import java.util.logging.Level;
import java.util.*;

import com.sun.enterprise.util.io.FileUtils;

/**
 * Provides access to application information per server instance.
 */
public class AppsManager extends BaseManager {
    
    public AppsManager(InstanceEnvironment env) throws ConfigException {
        super(env, true);
    }
    
    public AppsManager(InstanceEnvironment env, boolean useBackupServerXml)
            throws ConfigException {
        super(env, useBackupServerXml);
        //FIXME: HACK START
	J2eeApplication[] jArray = ((Applications)configBean).getJ2eeApplication();
	if(jArray!=null) {
            for(int i=0;i<jArray.length;i++) {
                jArray[i].setConfigContext(configContext);
		jArray[i].setXPath(ServerXPathHelper.getAppIdXpathExpression(jArray[i].getName()));
            }
	}
	//FIXME: HACK END
    }    
    
    public ModuleType getModuleType() {
        return ModuleType.EAR;
    }
    
    public String getStubLocation(String appId) {

        ApplicationEnvironment env = 
            instanceEnvironment.getApplicationEnvironment(appId);
        return env.getAppStubPath();
    }

    public String getGeneratedXMLLocation(String name){
        ApplicationEnvironment env = instanceEnvironment.getApplicationEnvironment(name);
        return env.getAppGeneratedXMLPath();
    } 
    
    public String getJSPLocation(String appId) {

        ApplicationEnvironment env = 
            instanceEnvironment.getApplicationEnvironment(appId);
        return env.getAppJSPPath();
    }
    
    /*
    private String getDefaultVirtualServerId() throws ConfigException {
        ConnectionGroup cg = (ConnectionGroup)
        ConfigBean.getConfigBeanByXPath(this.configContext, 
                            ServerXPathHelper.XPATH_CONNECTION_GROUP);
        
        if(cg == null) {
            throw new ConfigException("Cannot find Default Virtual Server");
        }
        
        return cg.getDefaultVirtualServer();
        
    }
     */
    
    /**
     * Returns an array of all applications deployed with the server.
     */
    public J2eeApplication[] getAllApps() {
        J2eeApplication[] apps = ((Applications)this.configBean).getJ2eeApplication();
        if(apps == null) return new J2eeApplication[0];

        ArrayList list = new ArrayList();
        for (int i=0; i<apps.length; i++) {            
            // add the application to the list if it is referenced
            // by this server
            if ( isReferenced(apps[i].getName()) ) {
                list.add(apps[i]);
            }
        }
        // returns an array of applications referenced by this server
        J2eeApplication[] refList = new J2eeApplication[list.size()];
        return ( (J2eeApplication[]) list.toArray(refList) );
	}
    
    /**
     * Returns a list of all applications deployed with the server.
     */
    public List listIds() {
        ArrayList arr = new ArrayList();
        J2eeApplication[] apps = ((Applications)this.configBean).getJ2eeApplication();
        if(apps == null) return arr;
            
        for (int i=0;i<apps.length;i++) {
            String name = apps[i].getName();
            // adds the application to the list if 
            // it is referenced by this server             
            if ( isReferenced(name) ) {
                arr.add(name);
            }
        }        
        return arr;
    }
    
    public String getLocation(String appId) throws ConfigException {
        J2eeApplication app = (J2eeApplication) getJ2eeApplication(appId);
        InstanceEnvironment instEnv;
        if ((instEnv = getInstanceEnvironment()) == null) {
            throw new ConfigException("instEnv was null");
        }
        PropertyResolver resolver = new PropertyResolver(super.configContext, 
                instEnv.getName());
        String appLocation;
        if ((appLocation = app.getLocation()) == null) {
            throw new ConfigException("appLocation was null");
        };
        String resolvedPath = resolver.resolve(appLocation);
        return resolvedPath;
        
    }
    
    public boolean isEnabled(String appId) throws ConfigException {
        return getJ2eeApplication(appId).isEnabled();
    }

    /**
     *Reports whether Java Web Start access is enabled for the app clients in
     *the specified application.
     *@param appId the module ID of the app to check
     *@return boolean indicating whether access is permitted
     */
    public boolean isJavaWebStartEnabled(String appId) throws ConfigException {
        return getJ2eeApplication(appId).isJavaWebStartEnabled();
    }
    
    /**
     * Checks whether this application is a system app
	 * ResourceType in domain.xml should start with "system-"
     * @return true if resourceType starts with "system-"
     */
	public boolean isSystem(String appId) throws ConfigException {
	   J2eeApplication ja = getJ2eeApplication(appId);
	   String resourceType = ja.getObjectType();
	   if(resourceType.startsWith(SYSTEM_PREFIX))
	       return true;
	   else
	       return false;
	}
    
    /**
     * Checks whether this application is a system admin app
         * ResourceType in domain.xml should start with "system-admin"
     * @return true if resourceType starts with "system-admin"
     */
        public boolean isSystemAdmin(String appId) throws ConfigException {
           J2eeApplication ja = getJ2eeApplication(appId);
           String resourceType = ja.getObjectType();
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
    public boolean isSystemPredeployed (String appId)  throws ConfigException{
        J2eeApplication ja = getJ2eeApplication(appId);
        String resourceType = ja.getObjectType();
        boolean isDirectoryDeployed = ja.isDirectoryDeployed();
        if (resourceType.startsWith(SYSTEM_PREFIX) && isDirectoryDeployed) {
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean isRegistered(String appId, ConfigBean bean) {
        ConfigBean cb = null;
        try {
            cb = ((Applications)bean).getJ2eeApplicationByName(appId);
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
        J2eeApplication backJa = (J2eeApplication)
            ((Applications)configBean).getJ2eeApplicationByName(appId);
        ((Applications)configBean).removeJ2eeApplication(backJa);
    }
    
    /**
     * Identifies whether an application is enabled or disabled.
     *
     * @param appId unique idenitifier for the application
     * @param isEnabled flag for enabling or disabling the application
     */
    public void setEnable(String appId, boolean enabled)
            throws ConfigException {
        getJ2eeApplication(appId).setEnabled(enabled);
    }

	/**
     * Set the location for an App
     *
     * @param appId unique idenitifier for the application
     * @param location
     */
    public void setLocation(String appId, String location)
            throws ConfigException {
        getJ2eeApplication(appId).setLocation(location);
    }
    
    
	/**
     * Set the optional attributes for an App
     *
     * @param appId unique idenitifier for the application
     * @param optionalAttributes - pairs tag/value to set
     */
    public void setOptionalAttributes(String appId, Properties optionalAttributes)
            throws ConfigException {
        if(optionalAttributes!=null) {
            J2eeApplication ja = getJ2eeApplication(appId);
            Enumeration tags = optionalAttributes.keys();
            while(tags.hasMoreElements())
            {
                String tag = (String)tags.nextElement();
                String value = optionalAttributes.getProperty(tag);
                ja.setAttributeValue(tag, value);
            }
        }
    }

    /**
     * This method only returns from cache.  To force the creation of
     * an application object, see getAppDescriptor method.
     * Only parameter appID is used.  The rest of the params are kept for
     * backward compatibility purpose until we clean up the instance
     * managers completely.
     */
    public Application getDescriptor(
        String appID, ClassLoader cl, String loc, boolean validateXML)
   		    throws ConfigException {
        return getRegisteredDescriptor(appID);
    }

    /**
     * This method is called by ResourcesUtil during server start up
     * and by ApplicationLoader when the dd for this app is not registered.
     * We need to construct the top level application and its classloader
     * before calling getAppDescriptor(Application) to fully populate the
     * application object.
     */
    public Application getAppDescriptor(String appID, ClassLoader parentClassLoader) 
            throws ConfigException {

        Application application = getRegisteredDescriptor(appID);
        if (application != null) {
            return application;
        }
        Application deserializedApplication = null;

        // Try to load the previously-serialized form of the descriptor.
        SerializedDescriptorHelper.Loader sdLoader = 
                SerializedDescriptorHelper.load(appID, this);
        try {
            // partially load the deployment descriptor...
            ApplicationArchivist archivist = new ApplicationArchivist();
            FileArchive appArchive = new FileArchive();
            appArchive.open(getLocation(appID));

            //for upgrade senario, we still load from the original
            //application repository for application.xml first
            if (!archivist.hasStandardDeploymentDescriptor(appArchive)) {
                //read from generated/xml location
                appArchive.open(getGeneratedXMLLocation(appID));
            }

            deserializedApplication = sdLoader.getApplication();
            if (deserializedApplication != null) {
                application = deserializedApplication;
            } else {
                // There was no serialized descriptor file or it could not
                // be loaded, so load the application info from the XML descriptors.
                application = Application.createApplication(appArchive,false);
            }
            application.setRegistrationName(appID);
            String moduleRoot = getLocation(application.getRegistrationName());
            String[] classPaths = (String[]) EJBClassPathUtils.getAppClasspath(
                                                            application, this).toArray(new String[0]);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "[AppsManager] :: appID " + appID + " classpaths " 
                                                + classPaths + " moduleRoot " + moduleRoot 
                                                + " parentClassLoader " + parentClassLoader);
            }
            ClassLoader cl = EJBClassPathUtils.createEJBClassLoader(classPaths,
                                                            moduleRoot , appID, parentClassLoader,
                                                            application.getModuleType());
            application.setClassLoader(cl);
        } catch (Exception confEx) {
            _logger.log(Level.SEVERE,"loader.error_while_loading_app_desc", confEx);
            throw new ConfigException(confEx);
        }

        Application fullyLoadedApp = getAppDescriptor(application);
        
        // If needed, write out the new app into the serialized descriptor file.
        sdLoader.store(fullyLoadedApp);
        
        return fullyLoadedApp;
    }
    
    /**
     * Returns the deployment descriptor object for the application DD.
     * Given the top level application object, this method populates
     * it with all the submodule information by loading from disc.
     *
     * @param    application  the top level application
     *
     * @return   the deployment descriptor object for the given application id
     *
     * @throws   ConfigException  if unable to load the deployment descriptor
     */
    public Application getAppDescriptor(Application application) 
            throws ConfigException {

        if (application == null) {
            throw new ConfigException("Application object should not be null");
        }

        ClassLoader cl = application.getClassLoader();

        // We need to use a temp CL until we are done with validate().
        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=223
        // for details.
        if (cl instanceof InstrumentableClassLoader) {
            ClassLoader tcl = InstrumentableClassLoader.class.cast(cl).copy();
            application.setClassLoader(tcl);
            // set it in all the bundles as well,
            for (BundleDescriptor bd : application.getBundleDescriptors()) {
                bd.setClassLoader(tcl);
            }
        }
        String appId = application.getRegistrationName();
        
        // we need to load this puppy, save it in the cache...
        try {
            String appDir = getLocation(appId);
            FileArchive in = openDDArchive(appId, appDir);

            ApplicationArchivist archivist = new ApplicationArchivist();
            archivist.readModulesDescriptors(application, in);
            archivist.readRuntimeDeploymentDescriptor(in, application);
            if(!isSystemAdmin(appId) && !isSystem(appId)) {
                // we need to read persistence descriptors separately
                // because they are read from appDir as oppsed to xmlDir.
                readPersistenceDeploymentDescriptors(appDir, application);
            }
            archivist.setDescriptor(application);

            // use temp CL that is set in the application. For details,
            // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=223
            archivist.validate(application.getClassLoader());
            
            application.setGeneratedXMLDirectory(getGeneratedXMLLocation(appId));
            
            if (!application.getWebServiceDescriptors().isEmpty()) {
                ModuleContentLinker visitor = new ModuleContentLinker(in);
                application.visit((com.sun.enterprise.deployment.util.ApplicationVisitor) visitor);
            }

            // Now that validate() is called, we can set the actual CL.
            // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=223
            // for details.
            application.setClassLoader(cl);
            // set it in all the bundles as well,
            for (BundleDescriptor bd : application.getBundleDescriptors()) {
                bd.setClassLoader(cl);
            }
            registerDescriptor(appId, application);

            return application;
        } catch (ConfigException ce) {
            throw ce;
        } catch (Throwable t) {
			throw new ConfigException(
                Localizer.getValue(ExceptionType.FAIL_DD_LOAD, appId), t);
        }
    }

    public String getDescription(String id) throws ConfigException {
        return getJ2eeApplication(id).getDescription();
    }
    
    private J2eeApplication getJ2eeApplication(String appId)
            throws ConfigException {

        J2eeApplication app = (J2eeApplication)
            ((Applications)this.configBean).getJ2eeApplicationByName(appId);

        if(app == null)
			throw new ConfigException(
                Localizer.getValue(ExceptionType.APP_NOT_EXIST));
        return app;
        
    }
    
    public void setDescription(String id, String desc) throws ConfigException {
        getJ2eeApplication(id).setDescription(desc);
    }
    
    public String getVirtualServersByAppName(String appName) throws ConfigException {
            return ServerBeansFactory.getVirtualServersByAppName(configContext, appName);
    }
    
    /**
     * @return the registered descriptors map
     */
    public Map getRegisteredDescriptors() {
                
        if (apps==null) {
            synchronized (AppsManager.class) {
                if (apps==null) {
                    apps = new HashMap();
                }
            }
        } 
        return apps;
    }
    
    private static Map apps=null;
}
