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
 * @(#) ResourceManager.java
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

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.event.ResourceDeployEvent;
import com.sun.enterprise.admin.event.ResourceDeployEventListener;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.util.i18n.StringManager;


import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;


/**
 * Handles resource deploy events. When a resource is updated/added,
 * admin server sends a notification to the server instance. This object
 * is the listener for all resource events.
 *
 * Resource deployers throws UnsupportedOperationException when
 * an operation is not supported and requires a server restart. It
 * catches the UnsupportedOperationException and notifies
 * admin event multicaster with RESTART_NEEDED flag.
 */
class ResourceManager implements ResourceDeployEventListener { 

    /** logger for this manager */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** context of the server instance runtime */
    private ServerContext serverContext_; 

    /** resource deployer factory */
    private ResourceDeployerFactory factory_ = null;
	private static StringManager localStrings = StringManager.getManager(
				"com.sun.enterprise.server");

    /**
     * Constructor. 
     * 
     * @param    sc    server context
     */
    public ResourceManager(ServerContext sc) {
        serverContext_ = sc;
        factory_ = new ResourceDeployerFactory(); 	 
    }

    // ---- START OF ResourceDeployEventListener METHODS ---------------------

    public void resourceDeployed(ResourceDeployEvent event) 
            throws AdminEventListenerException {

        assert(event.getJ2EEComponentType() == BaseDeployEvent.RESOURCE);

        try {
            Resources rbeans = getResources(event.getConfigContext());
            String type = event.getResourceType();
            
            ResourcesUtil.setEventConfigContext(event.getConfigContext());
            
            Object resource = 
                factory_.getResource(type, event.getResourceName(), rbeans);
            if (ResourcesUtil.createInstance().isEnabled((ConfigBean) resource)) {
                factory_.getResourceDeployer(type).deployResource(resource);
            }

            _logger.log(Level.INFO, "core.resourcedeployed",
                        type + ":" + event.getResourceName());
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"core.resourcedeploy_error", e);
            throw new AdminEventListenerException(e.getMessage());
        } finally {
            ResourcesUtil.resetEventConfigContext();
        }
    }

    public void resourceUndeployed(ResourceDeployEvent event) 
            throws AdminEventListenerException {

        assert(event.getJ2EEComponentType() == BaseDeployEvent.RESOURCE);
        
        try {
            // use config context prior to applying any changes
            Resources rbeans = getResources(event.getOldConfigContext());
            String type = event.getResourceType();
            Object resource = 
                factory_.getResource(type, event.getResourceName(), rbeans);
            ResourcesUtil.setEventConfigContext(event.getConfigContext());
            
            factory_.getResourceDeployer(type).undeployResource(resource);

            _logger.log(Level.INFO, "core.resourceundeployed",
                        type + ":" + event.getResourceName());
        } catch (UnsupportedOperationException nse) {
              AdminEventMulticaster.notifyFailure(event, AdminEventResult.RESTART_NEEDED);
            _logger.log(Level.INFO,"core.resource_undeployed_restart_needed", event.getResourceName());
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"core.resourceundeploy_error",e);
            throw new AdminEventListenerException(e.getMessage()); 
        } finally {
            ResourcesUtil.resetEventConfigContext();
        }
    }

    public void resourceRedeployed(ResourceDeployEvent event) 
            throws AdminEventListenerException {

        assert(event.getJ2EEComponentType() == BaseDeployEvent.RESOURCE);

        try {
            Resources rbeans = getResources(event.getConfigContext());
            String type = event.getResourceType();
            Object resource = 
                factory_.getResource(type, event.getResourceName(), rbeans);
            factory_.getResourceDeployer(type).redeployResource(resource);

            _logger.log(Level.INFO, "core.resourceredeployed",
                        type + ":" + event.getResourceName());
        } catch (UnsupportedOperationException nse) {
              AdminEventMulticaster.notifyFailure(event, AdminEventResult.RESTART_NEEDED);
            _logger.log(Level.INFO,"core.resource_redeployed_restart_needed", event.getResourceName());
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"core.resourceredeploy_error",e);
            throw new AdminEventListenerException(e.getMessage());
        }
    }

    public void resourceEnabled(ResourceDeployEvent event) 
            throws AdminEventListenerException {

        assert(event.getJ2EEComponentType() == BaseDeployEvent.RESOURCE);

        try {
            Resources rbeans = getResources(event.getConfigContext());
            String type = event.getResourceType();
            Object resource = 
                factory_.getResource(type, event.getResourceName(), rbeans);
            factory_.getResourceDeployer(type).enableResource(resource);

            _logger.log(Level.INFO, "core.resourceenabled",
                        type + ":" + event.getResourceName());
        } catch (UnsupportedOperationException nse) {
              AdminEventMulticaster.notifyFailure(event, AdminEventResult.RESTART_NEEDED);
            _logger.log(Level.INFO,"core.resource_enabled_restart_needed", event.getResourceName());
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"core.resourceenabled_error",e);
            throw new AdminEventListenerException(e.getMessage()); 
        }
    }

    public void resourceDisabled(ResourceDeployEvent event) 
            throws AdminEventListenerException {

        assert(event.getJ2EEComponentType() == BaseDeployEvent.RESOURCE);

        try {
            Resources rbeans = getResources(event.getConfigContext());
            String type = event.getResourceType();
            Object resource = 
                factory_.getResource(type, event.getResourceName(), rbeans);
            factory_.getResourceDeployer(type).disableResource(resource);

            _logger.log(Level.INFO, "core.resourcedisabled",
                        type + ":" + event.getResourceName());
        } catch (UnsupportedOperationException nse) {
              AdminEventMulticaster.notifyFailure(event, AdminEventResult.RESTART_NEEDED);
            _logger.log(Level.INFO,"core.resource_disabled_restart_needed", event.getResourceName());
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"core.resourcedisabled_error",e);
            throw new AdminEventListenerException(e.getMessage());
        }
    }

    public void resourceReferenceAdded(ResourceDeployEvent event) 
            throws AdminEventListenerException {

        resourceDeployed(event);
    }

    public void resourceReferenceRemoved(ResourceDeployEvent event)
            throws AdminEventListenerException {

        resourceUndeployed(event);
    }

    // ---- END OF ResourceDeployEventListener METHODS ---------------------

    /**
     * Returns the config resources bean from the given config context.
     *
     * @param    ctx    config context
     * 
     * @throws   ConfigException    if an error while getting the resources bean
     */
    private Resources getResources(ConfigContext ctx) throws ConfigException {

        //Resources rbeans = ServerBeansFactory.getServerBean(ctx).getResources();
        Resources rbeans = ServerBeansFactory.getDomainBean(ctx).getResources();

        if (rbeans == null) {
			String msg = localStrings.getString("resourceManager.resource_not_found");
                throw new ConfigException(msg); //XXX
        }
        return rbeans;
    }
}
