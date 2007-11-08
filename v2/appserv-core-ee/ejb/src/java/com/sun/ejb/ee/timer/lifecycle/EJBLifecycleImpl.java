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

package com.sun.ejb.ee.timer.lifecycle;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ServerContext;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.ee.cms.core.Action;
import com.sun.enterprise.ee.cms.core.FailureNotificationActionFactory;
import com.sun.enterprise.ee.cms.core.PlannedShutdownAction;
import com.sun.enterprise.ee.cms.core.PlannedShutdownActionFactory;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.ActionFactory;
import com.sun.enterprise.ee.cms.core.MessageActionFactory;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.GMSNotEnabledException;
import com.sun.enterprise.ee.cms.core.GMSNotInitializedException;
import com.sun.enterprise.ee.cms.core.GroupManagementService;

import com.sun.ejb.spi.distributed.DistributedEJBService;
import com.sun.ejb.spi.distributed.DistributedEJBServiceFactory;

import com.sun.enterprise.config.serverbeans.*;

import com.sun.logging.LogDomains;

/**
 * Implementation of ServerLifecycle interface conforming to application server
 * lifecycle programming model.
 * @author 
 * Date: 
 * @version 
 */
public class EJBLifecycleImpl 
    implements ServerLifecycle 
{
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
    
    private static final String GMS_READ_ONLY_COMPONENT_NAME =
        "__GMS__READ_ONLY_BEAN__";
    
    private GroupManagementService gms;
    
    public void onInitialization(ServerContext serverContext) 
    throws ServerLifecycleException 
    {
    }

    public void onStartup(ServerContext serverContext) 
    throws ServerLifecycleException 
    {
    }

    public void onReady(ServerContext serverContext)
        throws ServerLifecycleException {
        
        final String timerMigrationNotEnabled =
                "EJBLifeCycle: Automatic "
                + " timer migration component not enabled "
                + " for standalone server instance";
        final String timerMigrationNotEnabledForDAS =
                "EJBLifeCycle: Automatic "
                + " timer migration component not enabled "
                + " for DAS instance";
        try {
            AdminService adminService = AdminService.getAdminService();
            if ((adminService != null) && (! adminService.isDas())) {
                try {
                    Cluster cluster = ClusterHelper.getClusterForInstance(
                            serverContext.getConfigContext(),
                            serverContext.getInstanceName());
                    if (cluster != null) {
                        try {
                            this.gms = (GroupManagementService) GMSFactory.getGMSModule(
                                    cluster.getName());
                            _logger.log(Level.INFO, "EJBLifecycle: Got GMS module for: " + cluster.getName());
                        } catch (GMSNotEnabledException gmsNotEnabledEx) {
                            _logger.log(Level.FINE, "EJBLifeCycle: GMS *NOT* Enabled.",
                                    gmsNotEnabledEx);
                        } catch (GMSNotInitializedException gmsNotInitializedEx) {
                            _logger.log(Level.SEVERE, "EJBLifeCycle: GMS *NOT* Initialized",
                                    gmsNotInitializedEx);
                        } catch (GMSException gmsEx) {
                            _logger.log(Level.SEVERE, "EJBLifeCycle: Could not get GMS module",
                                    gmsEx);
                        }
                    } else {
                        _logger.log(Level.FINE, timerMigrationNotEnabled);
                    }
                } catch (ConfigException configEx) {
                    _logger.log(Level.FINE, timerMigrationNotEnabled,
                            configEx);
                } catch (Exception ex) {
                    _logger.log(Level.SEVERE,
                            "EJBLifecycle: Exception getting GMS module", ex);
                }
            } else {
                _logger.log(Level.WARNING, timerMigrationNotEnabledForDAS);
            }
            
            if (gms != null) {
                registerEJBTimerComponents();
                registerReadOnlyBeanComponents();
            }
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,
                    "EJBLifecycle: Exception during registration of listeners", ex);
        }
    }

    public void onShutdown() 
    throws ServerLifecycleException 
    {
    }

    public void onTermination() 
    throws ServerLifecycleException 
    {
    }

    /**
     * Private methods that are used to register components to GMS
     */
    private void registerEJBTimerComponents() {

        /**
         * We only register interest in the Planned Shutdown event here.
         * Because of the dependency between transaction recovery and
         * timer migration, the timer migration operation during an
         * unexpected failure is initiated by the transaction recovery 
         * subsystem.  
         */

        gms.addActionFactory(new PlannedShutdownActionFactory() {
            public Action produceAction() {
                return new EJBTimerPlannedShutdownActionImpl();
            }
        });
        _logger.log(Level.FINE,
            "EJBLifecycle: Registered PlannedShutdownNotification...");

        DistributedEJBService distributedEJBService = 
            DistributedEJBServiceFactory.getDistributedEJBService();

        distributedEJBService.setPerformDBReadBeforeTimeout( true );
        
    }
    
    private void registerReadOnlyBeanComponents() {
        if (gms != null) {
            gms.addActionFactory(new ReadOnlyBeanMessageActionFactoryImpl(
                            gms, GMS_READ_ONLY_COMPONENT_NAME),
                            GMS_READ_ONLY_COMPONENT_NAME);
            
            _logger.log(Level.FINE,
                "EJBLifecycle: Registered ReadOnlyBeanMessageActionFactory...");
        }
    }

} //EJBLifecycleImpl{}
