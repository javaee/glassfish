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
 * @(#) ApplicationClientModuleLoader.java
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

import com.sun.enterprise.server.event.ApplicationClientEvent;
import com.sun.enterprise.server.event.ApplicationLoaderEventNotifier;
import java.net.URL;
import java.util.Set;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.AppclientModulesManager;

import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.loader.EJBClassLoader;

// for jsr77
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import java.util.Iterator;
import javax.management.MBeanException;
import com.sun.enterprise.Switch;

/**
 * Application client loader loads and unloads stand alone Application client module.
 *
 * @author  Sreenivas Munnangi
 * @since   JDK1.4
 */
class ApplicationClientModuleLoader extends AbstractLoader {
 
    private ApplicationClientDescriptor acDescriptor = null;

    static Logger _logger=LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    private ApplicationLoaderEventNotifier appLoaderEventNotifier;

    /**
     * ApplicationClientModuleLoader loads one module.
     *
     * @param modID              the name of the Application client module
     * @param parentClassLoader  the parent class loader
     * @param acModulesManager   the application client module mgr
     */
    ApplicationClientModuleLoader(String modID, ClassLoader parentClassLoader,
            AppclientModulesManager acModulesManager) {

        super(modID, parentClassLoader, acModulesManager);

	setDescriptor();

         try {
             this.application = acModulesManager.getDescriptor(modID, parentClassLoader, false);
             appLoaderEventNotifier =
                 ApplicationLoaderEventNotifier.getInstance();
         } catch (ConfigException ce) {
             _logger.log(Level.SEVERE,"loader.error_while_loading_app_desc",
                         ce);
         }
    }
    
    /**
     *Loads the app client.
     *Although the app client logic is not actually loaded into the app server,
     *there may be some components interested in the load operation, so notify 
     *them.
     *
     * @param    jsr77    create jsr77 mBeans if true
     * @return   true     if load is successful
     */
    boolean doLoad(boolean jsr77) {
 	notifyAppClientEvent(ApplicationClientEvent.BEFORE_APPLICATION_CLIENT_LOAD);
 
        notifyAppClientEvent(ApplicationClientEvent.AFTER_APPLICATION_CLIENT_LOAD);
         
	return true;
    }
        
    /**
     *Unloads the app client.
     *Because no code is actually loaded into the server for app clients, there is
     *none to unload.  Still, notify any interested listeners.
     *
     * @param    jsr77    delete jsr77 mBeans if true
     * @return   true     if unload is successful
     */
    boolean unload(boolean jsr77) {
 	notifyAppClientEvent(ApplicationClientEvent.BEFORE_APPLICATION_CLIENT_UNLOAD);
 
        configManager.unregisterDescriptor(id);
         
        notifyAppClientEvent(ApplicationClientEvent.AFTER_APPLICATION_CLIENT_UNLOAD);
        return true;
    }


    /**
     * Create jsr77 root mBean
     */
    void createRootMBean() throws MBeanException {

	try {
            Switch.getSwitch().getManagementObjectManager().registerAppClient(
		acDescriptor,
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

        Switch.getSwitch().getManagementObjectManager().unregisterAppClient(
		acDescriptor,
		this.configManager.getInstanceEnvironment().getName());
    }


    /**
     * Create jsr77 mBeans for ejbs within this module
     */
    void createLeafMBeans() throws MBeanException {

	return;
    }


    /**
     * Create jsr77 mBeans for ejbs within this module
     */
    void createLeafMBean(Descriptor descriptor) throws MBeanException {

	return;
    }


    /**
     * Delete jsr77 mBeans for ejbs within this module
     */
    void deleteLeafMBeans() throws MBeanException {

	return;
    }

    /**
     * Delete jsr77 mBeans for ejbs within this module
     */
    void deleteLeafMBean(Descriptor descriptor) throws MBeanException {

	return;
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

	return;
    }

    private void setDescriptor() {
	acDescriptor = getApplicationClientDescriptor();
    }

    private ApplicationClientDescriptor getApplicationClientDescriptor() {
	ApplicationClientDescriptor appCD = null;
	try {
            Application app = configManager.getDescriptor(this.id, null, false);
            appCD = (ApplicationClientDescriptor) app.getStandaloneBundleDescriptor();
        } catch(ConfigException ex) {
            _logger.log(Level.WARNING,"Failed to get the ApplicationClientDescriptor");
        }
	return appCD;
    }

    /**
     *Notifies interested listeners of the load or unload operation.
     *@param the type of event to broadcast
     */
     protected void notifyAppClientEvent(int eventType) {
 	ApplicationClientEvent event = new ApplicationClientEvent(eventType,
 		getApplication(), getClassLoader(), getConfigContext());
 	appLoaderEventNotifier.notifyListeners(event);
     }
}
