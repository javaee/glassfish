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
 * @(#) ApplicationLoader.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import com.sun.enterprise.Switch;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.config.ConfigException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.loader.EJBClassLoader;
import javax.management.MBeanException;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.ModuleDescriptor;

import com.sun.enterprise.server.event.ApplicationEvent;

/**
 * Application loader  loads and unloads an applicaton. 
 *
 * @author  Mahesh Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class ApplicationLoader extends AbstractLoader {
 
    /** logger to log loader messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    /** Indicates if the deployment being performed is a force Deploy */
    private boolean isForceDeploy = false;
    
    /**
     * ApplicationLoader loads one application.
     *
     * @param appID              the name of the application 
     * @param parentClassLoader  parent class loader for this application
     * @param appsManager        the AppsManager for this VS
     */
    public ApplicationLoader(String appID, ClassLoader parentClassLoader,
            AppsManager appsManager) {

        super(appID, parentClassLoader, appsManager);
        boolean createClassLoader = true;
        try {
            application = appsManager.getRegisteredDescriptor(appID);
            //application object would be null if this is during server
            //startup or deployment to remote instance
            if (application==null) {
                application = appsManager.getAppDescriptor(appID, parentClassLoader);
                createClassLoader = false;
            } 
            //not null during deployment on DAS or in some case of 
            //remote serer start up where the clasloader is initialized
            //by the ResourceUtil
            else {
                try {
                    //the following check is to make sure we do not create
                    //a new classloader if one is already initialized and
                    //not void.  note that the classloader used in deployment
                    //is voided (done called) at this point.
                    ClassLoader clazzloader = application.getClassLoader();
                    if (clazzloader != null 
                        && (clazzloader instanceof EJBClassLoader)
                        && !((EJBClassLoader)clazzloader).isDone()) {
                        createClassLoader = false;
                    }
                } catch (Exception ex) {}
            }
            
            if (createClassLoader) {
                String[] classPath = (String[])
                EJBClassPathUtils.getAppClasspath(application, appsManager).toArray(new String[0]);
            
                initializeLoader(classPath, appsManager.getLocation(application.getRegistrationName()), 
                                      ModuleType.EAR);
                application.setClassLoader(this.ejbClassLoader);
            } else {
                initializeLoader(application.getClassLoader());
            }

        } catch (Exception confEx) {
            //@@ i18n
            _logger.log(Level.SEVERE, "ERROR while loading application " + appID);
            _logger.log(Level.SEVERE,"loader.error_while_loading_app_desc",
                        confEx);
        }
    }
    
    /**
     * Called from ApplicationManager. Called to load an application.
     * This routine creates the EJB and MDB container.
     *
     * @param     jsr77  create jsr77 mBeans if true
     * @return    true   if all modules were loaded successfully
     */
    protected boolean doLoad(boolean jsr77) {
        boolean allModulesDeployed = false;

        //  Possible values for loading:
        //  1. LOAD_ALL is for loading regular application
        //  2. LOAD_RAR is for loading the rar part of the embedded rar
        //  3. LOAD_REST is for loading the rest part of the embedded rar
        //  Embedded rar is loaded in two steps so we can create connector
        //  resources in between.

        //set default value
        if (loadUnloadAction == Constants.LOAD_UNSET) {
            loadUnloadAction = Constants.LOAD_ALL;
        }

        if (loadUnloadAction == Constants.LOAD_ALL || 
            loadUnloadAction == Constants.LOAD_RAR) { 
	    notifyAppEvent(ApplicationEvent.BEFORE_APPLICATION_LOAD);
            allModulesDeployed = loadRars(jsr77);
            if (loadUnloadAction == Constants.LOAD_RAR) {       
                return allModulesDeployed;
            }
        }

        // Now load the EJB 3.0 persistence entities.
        allModulesDeployed = loadPersistenceUnits();
        if(!allModulesDeployed) return false;

        if (allModulesDeployed) {
            allModulesDeployed = loadEjbs(jsr77); 
	    notifyAppEvent(ApplicationEvent.AFTER_APPLICATION_LOAD);
	}
        if (!allModulesDeployed) {
            // remove all loaded pars
            unloadPersistenceUnits();
            return allModulesDeployed;
        }
        loadWebserviceEndpoints(jsr77);

        // The web modules of this application are loaded from
        // J2EERunner.confPostInit() as part of the WebContainer start()
        // method

        return allModulesDeployed;
    }


    /**
     * Unloads this application. 
     *
     * @param     jsr77  delete jsr77 mBeans if true
     * @return    true   if all modules were removed successfully
     */
    protected boolean unload(boolean jsr77) {
        
        // Possible values for unloading:
        // 1. UNLOAD_ALL is for unloading regular application
        // 2. UNLOAD_RAR is for unloading the rar part of the embedded rar
        // 3. UNLOAD_REST is for unloading the rest part of the embedded rar
        // Embedded rar is unloaded in two steps so we can delete connector
        // resources in between.

        //set default value
        if (loadUnloadAction == Constants.LOAD_UNSET) {
            loadUnloadAction = Constants.UNLOAD_ALL;
        }

        boolean wsUnloaded = false;
        boolean ejbUnloaded = false;
        boolean pusUnloaded = false;

        if (loadUnloadAction == Constants.UNLOAD_ALL || 
            loadUnloadAction == Constants.UNLOAD_REST) {
	    notifyAppEvent(ApplicationEvent.BEFORE_APPLICATION_UNLOAD);
            wsUnloaded = unloadWebserviceEndpoints(jsr77); 

            // undeploy the ejb modules
            ejbUnloaded = unloadEjbs(jsr77);

            // Web modules are undeployed as part of the NSAPI reconfig
            // callback interface implemented in J2EERunner

            //undeploy persistence units
            pusUnloaded = unloadPersistenceUnits();
        
            if (loadUnloadAction == Constants.UNLOAD_REST) {
                // return true status if components were unloaded OK
                if (wsUnloaded && ejbUnloaded && pusUnloaded) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        // undeploy rar module
        //START OF IASRI 4666595
        boolean rarUnloaded = unloadRars(jsr77);
        //END OF IASRI 4666595

        configManager.unregisterDescriptor(id);

        notifyAppEvent(ApplicationEvent.AFTER_APPLICATION_UNLOAD);

        // helps garbage collection
        done();

        if (loadUnloadAction == Constants.UNLOAD_RAR) {
            return rarUnloaded;
        } else {
            return (wsUnloaded && ejbUnloaded && pusUnloaded && rarUnloaded);
        }
    }

    /**
     * Create jsr77 root mBean
     */
    void createRootMBean() throws MBeanException {

	try {

            Switch.getSwitch().getManagementObjectManager().createAppMBean(
                this.application, 
                this.configManager.getInstanceEnvironment().getName(),
	        this.configManager.getLocation(this.id));

            Switch.getSwitch().getManagementObjectManager().createAppMBeanModules(
		this.application, 
            	this.configManager.getInstanceEnvironment().getName(),
		this.configManager.getLocation(this.id));

	} catch (Exception e) {
	    throw new MBeanException(e);
	}
    }

    /**
     * Delete jsr77 root mBean
     */
    void deleteRootMBean() throws MBeanException {
        Switch.getSwitch().getManagementObjectManager().deleteAppMBean(this.application, 
            this.configManager.getInstanceEnvironment().getName());
    }


    /**
     * Create jsr77 mBeans for all components within this application
     */
    void createLeafMBeans() throws MBeanException {
	try {
            Switch.getSwitch().getManagementObjectManager().createAppMBeans(
		this.application,
            	this.configManager.getInstanceEnvironment().getName(),
		this.configManager.getLocation(this.id));
	} catch (Exception e) {
	    throw new MBeanException(e);
	}
    }

    /**
     * Create jsr77 mBean for the leaf object
     * In case of application loader it will be a NO-OP
     */
    void createLeafMBean(Descriptor descriptor) throws MBeanException {
        if (descriptor instanceof EjbDescriptor) {
            EjbDescriptor ejbDescriptor = null;
            try {
                ejbDescriptor = (EjbDescriptor) descriptor;
            } catch (Exception e) {
                throw new MBeanException(e);
            }
            Switch.getSwitch().getManagementObjectManager().createEJBMBean(ejbDescriptor,
                this.configManager.getInstanceEnvironment().getName());
        } else if (descriptor instanceof ConnectorDescriptor) {
            ConnectorDescriptor cd = null;
            try {
                cd = (ConnectorDescriptor) descriptor;
            } catch (Exception e) {
                throw new MBeanException(e);
            }
            Switch.getSwitch().getManagementObjectManager().createRARMBean(cd,
                this.configManager.getInstanceEnvironment().getName());
        } 
    }

    /**
     * Delete jsr77 mBean for the leaf object
     * In case of application loader it will be a NO-OP
     */
    void deleteLeafMBean(Descriptor descriptor) throws MBeanException {
        if (descriptor instanceof EjbDescriptor) {
            EjbDescriptor ejbDescriptor = null;
            try {
                ejbDescriptor = (EjbDescriptor) descriptor;
            } catch (Exception e) {
                throw new MBeanException(e);
            }
            Switch.getSwitch().getManagementObjectManager().deleteEJBMBean(ejbDescriptor,
                this.configManager.getInstanceEnvironment().getName());
        } else if (descriptor instanceof ConnectorDescriptor) {
            ConnectorDescriptor cd = null;
            try {
                cd = (ConnectorDescriptor) descriptor;
            } catch (Exception e) {
                throw new MBeanException(e);
            }
            Switch.getSwitch().getManagementObjectManager().deleteRARMBean(cd,
                this.configManager.getInstanceEnvironment().getName());
        }
    }

    /**
     * Delete jsr77 mBeans for all components within this application
     */
    void deleteLeafMBeans() throws MBeanException {
        Switch.getSwitch().getManagementObjectManager().deleteAppMBeans(this.application,
            this.configManager.getInstanceEnvironment().getName());
    }

    /**
     * Delete jsr77 mBeans for the application and its' components
     */
    void deleteLeafAndRootMBeans() throws MBeanException {
        deleteLeafMBeans();
        deleteRootMBean();
    }


    /**
     * Set the state for the rootMBean
     */
    void setState(int state) throws MBeanException {
        if (application == null) {
            //the application object can be null if this is a redeployment
            //of an app that previously failed to load.  at the point of
            //failure we cleaned out the loaders, including the application
            //object.  therefore, if the application object is null, we
            //would log it and skip this step
            _logger.log(Level.FINE, 
            "Application descriptor is NULL. setState skipped");
        } else {
            Switch.getSwitch().getManagementObjectManager().setApplicationState(
                state, application, 
                configManager.getInstanceEnvironment().getName());
        }
    }

    public boolean isForceDeploy(){
        return this.isForceDeploy;
    }
    
    public void setForceDeploy(boolean isForceDeploy) {
        this.isForceDeploy = isForceDeploy;
    }
}
