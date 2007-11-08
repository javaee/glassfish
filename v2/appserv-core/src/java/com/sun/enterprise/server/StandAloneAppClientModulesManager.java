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

package com.sun.enterprise.server;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.DeployEventListenerHelper;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.event.ModuleDeployEventListener;
import com.sun.enterprise.appclient.jws.AppclientJWSSupportManager;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.management.StateManageable;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanException;


/**
 * AppClientModules Manager  acts as a listener for the deployment events.
 *
 * @author  Sreenivas Munnangi
 * @since   JDK1.4
 */

class StandAloneAppClientModulesManager extends AbstractManager
    implements ModuleDeployEventListener {

    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private static StringManager localStrings = StringManager.getManager(
            "com.sun.enterprise.server");

    /**
     * Application Client Modules Manager
     */
    StandAloneAppClientModulesManager(
	AppclientModulesManager acModuleManager, ClassLoader sharedCL) {

	super(sharedCL, acModuleManager);

        AdminEventListenerRegistry.addModuleDeployEventListener(this);

        /* Make sure the manager is alive to receive all start-up load events. */
        AppclientJWSSupportManager.getInstance();

    }

    /**
     * Invoked when a standalone application client module is deployed.
     */
    public synchronized void moduleDeployed(ModuleDeployEvent event)
        throws AdminEventListenerException {


        boolean jsr77 = false;

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneAppClientModulesManager moduleDeployed");
            _logger.log(Level.FINEST, "ModuleType=" + event.getModuleType());
        }

        if (event.getModuleType().equals(event.TYPE_APPCLIENT)) {

            DeployEventListenerHelper.getDeployEventListenerHelper().synchronize(event);

            String modID = event.getModuleName();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "modID=" + modID);
            }

            try {
                // refreshes the config context with the context from this event
                this.configManager.refreshConfigContext(event.getConfigContext());

                // set jsr77 flag
                // which is used to signify if the event is deploy or undeploy
                // to create or delete jsr77 mBeans
                String action = event.getAction();
                if ((action.equals(BaseDeployEvent.DEPLOY)) ||
                        (action.equals(BaseDeployEvent.REDEPLOY))) {
                    jsr77 = true;
                }

                if (!moduleDeployed(jsr77, modID, event.getConfigContext())) {

                    // throw an exception if load fails
                    String msg = localStrings.getString("appClientModule deploy failed",
                            modID);
                    throw new AdminEventListenerException(msg);
                }
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce.getMessage());
            }
        }
    }


    /**
     * Invoked when a standalone application client module is undeployed.
     */

    public synchronized void moduleUndeployed(ModuleDeployEvent event)
        throws AdminEventListenerException {

        boolean jsr77 = false;

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneAppClientModulesManager moduleUndeployed");
        }

        String action = event.getAction();

        if ((action.equals(BaseDeployEvent.UNDEPLOY)) ||
                (action.equals(BaseDeployEvent.REDEPLOY))) {
            jsr77 = true;
        }

        try {
            if (event.getModuleType().equals(event.TYPE_APPCLIENT)) {
                String modID = event.getModuleName();

                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "UnDeploying module: " + modID);
                }

                // unload and throw exception if it fails
                if (!moduleUndeployed(jsr77, modID)) {
                    String msg = localStrings.getString("appclient.appclient_undeployed_failed",
                            modID);
                    throw new AdminEventListenerException(msg);
                }

            }
        } catch (Exception e) {
            throw new AdminEventListenerException(e.getMessage());
        }

    }


    /**
     * Invoked when a standalone application client module is redeployed.
     */

    public synchronized void moduleRedeployed(ModuleDeployEvent event)
        throws AdminEventListenerException {

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneAppClientModulesManager moduleRedeployed");
        }

        if (event.getModuleType().equals(event.TYPE_APPCLIENT)) {

            String modID = event.getModuleName();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "ReDeploying module: " + modID);
            }

            moduleUndeployed(event);
            moduleDeployed(event);
        }
    }

    /**
     * Invoked when a standalone application client module is enabled.
     */
    public synchronized void moduleEnabled(ModuleDeployEvent event)
        throws AdminEventListenerException {

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneAppClientModulesManager moduleEnabled");
        }

	// for an application client module
	// the operations enable/disable or start/stop do not make sense

	return;
    }


    /**
     * Invoked when a standalone application client module is disabled.
     */

    public synchronized void moduleDisabled(ModuleDeployEvent event)
        throws AdminEventListenerException {

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneAppClientModulesManager moduleDisabled");
        }

	// for an application client module
	// the operations enable/disable or start/stop do not make sense

	return;
    }


    /**
     * Deployed event handling
     */

    private boolean moduleDeployed(boolean jsr77, String modID, ConfigContext dynamicConfigContext) {

	boolean result = false;
        boolean loadJSR77 = jsr77 || loadJSR77(modID, DeployableObjectType.CAR);
	try {
            AbstractLoader modLoader = getLoader(modID);

            // create root mBean for this module
            if (loadJSR77) {
                try {
                    modLoader.createRootMBean();
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_creating_jsr77_root_mbean",mbe);
                }
            }

            modLoader.setConfigContext(dynamicConfigContext);
            result = modLoader.load(loadJSR77);
            if (result) {
                this.id2loader.put(modID, modLoader);
            }
	} catch (Exception ce) {
            _logger.log(Level.WARNING,
                "core.error_while_loading_application_client_module",ce);
            result = false;
	}

        return result;
    }


    /**
     * Undeployed event handling
     */

    private boolean moduleUndeployed(boolean jsr77, String modID) {

	boolean result = false;
        try {
            ApplicationClientModuleLoader modLoader =
                (ApplicationClientModuleLoader) this.id2loader.remove(modID);

            if (modLoader == null) {
                return true;
            }

            // delete root mBean for this module
            if (jsr77) {
                try {
                    modLoader.deleteRootMBean();
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_deleting_jsr77_root_mbean",mbe);
                    }
            }

            result = modLoader.unload(jsr77);

        } catch (Exception ce) {
            _logger.log(Level.WARNING,
                    "core.error_while_unloading_application_client_module",ce);
            result = false;
	}

        return result;
    }

    /**
     * Returns loader
     */

    protected AbstractLoader getLoader(String moduleId) {
	return new ApplicationClientModuleLoader(
			moduleId, 
			this.parentClassLoader, 
			(AppclientModulesManager) this.configManager);
    }

 
/**
     * Invoked when a  reference is created from a
     * server instance (or cluster) to a particular module.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void moduleReferenceAdded(ModuleDeployEvent event)
            throws AdminEventListenerException {
                
}

    /**
     * Invoked when a reference is removed from a
     * server instance (or cluster) to a particular module.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void moduleReferenceRemoved(ModuleDeployEvent event)
            throws AdminEventListenerException {
                
    }

}
