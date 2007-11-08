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
 * @(#) EJBModuleLoader.java
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

import java.util.Set;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.EjbModulesManager;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.loader.EJBClassLoader;

// for jsr77
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.deployment.io.EjbDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import java.util.Iterator;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MBeanException;
import com.sun.enterprise.Switch;

import com.sun.enterprise.server.event.ApplicationEvent;

/**
 * EJB loader loads and unloads stand alone ejb module.
 *
 * @author  Mahesh Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
class EJBModuleLoader extends AbstractLoader {
 
    static Logger _logger=LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    /**
     * EJBModuleLoaderer loads one module.
     *
     * @param modID              the name of the ejb module
     * @param parentClassLoader  the parent class loader
     * @param ejbModulesManager  the ejb module mgr for this VS
     */
    EJBModuleLoader(String modID, ClassLoader parentClassLoader,
            EjbModulesManager ejbModulesManager) {

        super(modID, parentClassLoader, ejbModulesManager);
        boolean createClassLoader = true;
        try {
            application = ejbModulesManager.getRegisteredDescriptor(modID);

            //application object would be null if this is during server
            //startup or deployment to remote instance
            if (application == null) {
                application= (Application) 
                    ejbModulesManager.getDescriptor(modID, parentClassLoader);
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
                    EJBClassPathUtils.getModuleClasspath(
                        modID, null, ejbModulesManager).toArray(new String[0]);
                initializeLoader(classPath, ejbModulesManager.getLocation(modID), ModuleType.EJB);
                application.setClassLoader(this.ejbClassLoader);
                if (application.isVirtual()) { //better be
                    BundleDescriptor bd = 
                        application.getStandaloneBundleDescriptor();
                    bd.setClassLoader(ejbClassLoader);
                }
            } else {
                initializeLoader(application.getClassLoader());
            }
        } catch (Exception confEx) {
            //@@ i18n
            _logger.log(Level.SEVERE, "ERROR while loading application " + modID);
            _logger.log(Level.SEVERE,"loader.error_while_loading_app_desc",
                        confEx);
        }
    }
    
    /**
     * Loads all the beans in this stand alone ejb module.
     * This routine creates the EJB and MDB container.
     *
     * @param    jsr77    create jsr77 mBeans if true
     * @return   true     if all ejbs loaded properly
     */
    boolean doLoad(boolean jsr77) {
	//Note: Application.isVirtual will be true for stand-alone module
	notifyAppEvent(ApplicationEvent.BEFORE_APPLICATION_LOAD);
        boolean pusLoaded = false;
        if(application.isVirtual()) { // standalone ejb jar
            // load persistence units for standalone ejb jars only
            // because for embedded ejb-jars, AppliationLoader loads them.
            if(!loadPersistenceUnits()) {
                return false; // abort loading at this point.
            } else {
                pusLoaded = true;
            }
        }
        boolean status = loadEjbs(jsr77);

	if (status == true) {
	    notifyAppEvent(ApplicationEvent.AFTER_APPLICATION_LOAD);
            loadWebserviceEndpoints(jsr77);
	} else {
            if(pusLoaded) { // unload iff we have loaded.
                unloadPersistenceUnits();
            }
        }

	return status;
    }
        
    /**
     * Unloads the beans in this stand alone ejb module.
     *
     * @param    jsr77    delete jsr77 mBeans if true
     * @return   true     if removed successful
     */
    boolean unload(boolean jsr77) {
        // undeploy the ejb modules

	//Note: Application.isVirtual will be true for stand-alone module
	notifyAppEvent(ApplicationEvent.BEFORE_APPLICATION_UNLOAD);

        boolean result = unloadEjbs(jsr77);
        unloadWebserviceEndpoints(jsr77);

        if(application.isVirtual()) { // standalone jar
            // unload persistence units for standalone ejb jars only
            // because for embedded ejb jar, AppliationLoader unloads them.
            result &= unloadPersistenceUnits();
        }

        configManager.unregisterDescriptor(id);

	notifyAppEvent(ApplicationEvent.AFTER_APPLICATION_UNLOAD);

        // helps garbage collector
        done();
        return result;
    }


    /**
     * Create jsr77 root mBean
     */
    void createRootMBean() throws MBeanException {

        EjbDeploymentDescriptorFile eddf = null;

        java.util.Set ejbBundles = this.application.getEjbBundleDescriptors();

        for(Iterator it=ejbBundles.iterator(); it.hasNext(); ) {

            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor)it.next();

	    try {
            	Switch.getSwitch().getManagementObjectManager().createEJBModuleMBean(
		    bundleDesc,
		    this.configManager.getInstanceEnvironment().getName(),
		    this.configManager.getLocation(this.id));
	    } catch (Exception e) {
	        throw new MBeanException(e);
	    }
        }
    }


    /**
     * Delete jsr77 root mBean
     */
    void deleteRootMBean() throws MBeanException {

        java.util.Set ejbBundles = this.application.getEjbBundleDescriptors();

        for(Iterator it=ejbBundles.iterator(); it.hasNext(); ) {

            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor)it.next();

            Switch.getSwitch().getManagementObjectManager().deleteEJBModuleMBean(bundleDesc,
                    this.configManager.getInstanceEnvironment().getName());
        }
    }


    /**
     * Create jsr77 mBeans for ejbs within this module
     */
    void createLeafMBeans() throws MBeanException {

        java.util.Set ejbBundles = this.application.getEjbBundleDescriptors();

        for(Iterator it=ejbBundles.iterator(); it.hasNext(); ) {

            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor)it.next();

            Switch.getSwitch().getManagementObjectManager().createEJBMBeans(bundleDesc,
                    this.configManager.getInstanceEnvironment().getName());
        }
    }


    /**
     * Create jsr77 mBeans for ejbs within this module
     */
    void createLeafMBean(Descriptor descriptor) throws MBeanException {

        EjbDescriptor ejbDescriptor = null;
        try {
            ejbDescriptor = (EjbDescriptor) descriptor;
        } catch (Exception e) {
            throw new MBeanException(e);
        }

        Switch.getSwitch().getManagementObjectManager().createEJBMBean(ejbDescriptor,
                    this.configManager.getInstanceEnvironment().getName());
    }


    /**
     * Delete jsr77 mBeans for ejbs within this module
     */
    void deleteLeafMBeans() throws MBeanException {

        java.util.Set ejbBundles = this.application.getEjbBundleDescriptors();

        for(Iterator it=ejbBundles.iterator(); it.hasNext(); ) {

            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor)it.next();

            Switch.getSwitch().getManagementObjectManager().deleteEJBMBeans(bundleDesc,
                    this.configManager.getInstanceEnvironment().getName());
        }
    }

    /**
     * Delete jsr77 mBeans for ejbs within this module
     */
    void deleteLeafMBean(Descriptor descriptor) throws MBeanException {

        EjbDescriptor ejbDescriptor = null;
        try {
            ejbDescriptor = (EjbDescriptor) descriptor;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
        Switch.getSwitch().getManagementObjectManager().deleteEJBMBean(ejbDescriptor,
                    this.configManager.getInstanceEnvironment().getName());
    }


    /**
     * Delete jsr77 mBeans for the module and its' components
     */
    void deleteLeafAndRootMBeans() throws MBeanException {
        deleteLeafMBeans();
        deleteRootMBean();
    }


    /**
     * Set state for the root mBean
     */
    void setState(int state) throws MBeanException {

        java.util.Set ejbBundles = this.application.getEjbBundleDescriptors();

        for(Iterator it=ejbBundles.iterator(); it.hasNext(); ) {

            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor)it.next();

            Switch.getSwitch().getManagementObjectManager().setEJBModuleState(state, bundleDesc,
                this.configManager.getInstanceEnvironment().getName());
        }
    }

}
