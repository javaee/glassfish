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
package com.sun.enterprise.connectors.work.monitor;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.j2ee.statistics.Stats;

import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.connectors.ActiveInboundResourceAdapter;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.connectors.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.server.ServerContext;
import com.sun.logging.LogDomains;

/**
 * Provides an implementation of the MonitoringLevelListener interface to
 * receive callbacks from admin regarding change in the monitoring level.
 * Though there are 3 monitoring levels defined by JSR77, we support
 * only 2 levels - OFF and ON (HIGH/LOW). So essentially, HIGH and LOW
 * for us is only ON
 *
 * @since s1aspe 8.1
 * @author Sivakumar Thyagarajan
 */
public final class ConnectorWorkMonitoringLevelListener implements
                    MonitoringLevelListener {

    private static final Logger _logger = LogDomains.getLogger( 
                    LogDomains.RSR_LOGGER );
    private MonitoringRegistry registry_;

    /**
     * @see com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener#setLevel(com.sun.enterprise.admin.monitor.registry.MonitoringLevel)
     * @deprecated
     */
    public void setLevel(MonitoringLevel level) {
    }

    /**
     * @see com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener#changeLevel(com.sun.enterprise.admin.monitor.registry.MonitoringLevel, com.sun.enterprise.admin.monitor.registry.MonitoringLevel, javax.management.j2ee.statistics.Stats)
     * @deprecated
     */
    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
                    Stats handback) {
    }

    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
                    MonitoredObjectType type) {
        if ( from == to ) {
            //Its a no-op, so return
            return;
        }

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ServerContext ctxt = ApplicationServer.getServerContext();
                if (ctxt != null ) {
                    registry_ = ctxt.getMonitoringRegistry();
                }
                return null;
            }
       });

        if (from == MonitoringLevel.OFF ) {
            if (to == MonitoringLevel.HIGH  || to == MonitoringLevel.LOW ) {
                switchOnMonitoring();
            }
        }

        if (from == MonitoringLevel.HIGH  || from == MonitoringLevel.LOW ) {
            if ( to == MonitoringLevel.OFF ) {
                switchOffMonitoring();
            }
        }
    }

    /**
     * Switch OFF Monitoring for Connector work management
     */
    private void switchOffMonitoring() {
        if ( _logger.isLoggable(Level.FINE) ) {
           _logger.fine("Switching level form ON to OFF");
        }
        
        //deregister
        AccessController.doPrivileged( new PrivilegedAction() {
            public Object run() {
                ActiveResourceAdapter[] allRAs = ConnectorRegistry.
                            getInstance().getAllActiveResourceAdapters();
                if ( allRAs == null ) {
                    return null;
                }

                ActiveInboundResourceAdapter inboundRA = null;
                for (int i = 0; i < allRAs.length; i++ ) {
                    if ( allRAs[i] != null 
                                    && allRAs[i] instanceof ActiveInboundResourceAdapter) {
                        try{
                            inboundRA = (ActiveInboundResourceAdapter)allRAs[i];
                            String moduleName  = inboundRA.getModuleName();
                            
                            //@todo :: after MBeans are modified
                            //Dont register system RARs as of now until MBean changes are complete.
                            if (ResourcesUtil.createInstance().belongToSystemRar(moduleName)) {
                                if (!ConnectorAdminServiceUtils.isJMSRA(moduleName)) {
                                    continue;
                                }
                            }
                            
                            registry_.unregisterConnectorWorkMgmtStats(
                                            ConnectorAdminServiceUtils.getApplicationName(moduleName), 
                                            ConnectorAdminServiceUtils.getConnectorModuleName(moduleName),
                                            ConnectorAdminServiceUtils.isJMSRA(moduleName));

                            //disable work mgmt monitoring
                            setWorkManagementMonitoring(inboundRA, false);
                        } catch( Exception mre ) {
                            _logger.log( Level.INFO, "poolmon.cannot_unreg");
                        }
                    }   
                }
                return null;
            }
        });
    }

    /**
     * Switch on Monitoring for Connector work management
     */
    private void switchOnMonitoring() {
        if ( _logger.isLoggable(Level.FINE) ) {
           _logger.fine("Switching level form OFF to ON");
        }
        
        AccessController.doPrivileged( new PrivilegedAction() {    
            public Object run() {
                ActiveResourceAdapter[] allRAs = ConnectorRegistry.
                            getInstance().getAllActiveResourceAdapters();
                if ( allRAs == null ) {
                    return null;
                }
                
                ActiveInboundResourceAdapter inboundRA = null;

                for (int i = 0; i < allRAs.length; i++ ) {
                        if ( allRAs[i] != null 
                                        && allRAs[i] instanceof ActiveInboundResourceAdapter) {
                            try{
                                
                                inboundRA = (ActiveInboundResourceAdapter)allRAs[i];
                                ConnectorWorkMgmtStatsImpl workstatsimpl =
                                    new ConnectorWorkMgmtStatsImpl(inboundRA);
                                String moduleName  = inboundRA.getModuleName();

                                //@todo :: after MBeans are modified
                                //Dont register system RARs as of now until MBean changes are complete.
                                if (ResourcesUtil.createInstance().belongToSystemRar(moduleName)) {
                                    if (!ConnectorAdminServiceUtils.isJMSRA(moduleName)) {
                                        continue;
                                    }
                                }
                                
                                //enable work mgmt monitoring
                                setWorkManagementMonitoring(inboundRA, true);
                                
                                registry_.registerConnectorWorkMgmtStats(
                                           workstatsimpl, 
                                           ConnectorAdminServiceUtils.getApplicationName(moduleName),
                                           ConnectorAdminServiceUtils.getConnectorModuleName(moduleName),
                                           ConnectorAdminServiceUtils.isJMSRA(moduleName),
                                           null);
                              
                            } catch (Exception mre) {
                                _logger.log( Level.WARNING, "poolmon.cannot_reg",
                                                mre.getMessage() );
                            }
                        }       
                }   
                return null;    
            }
        });
    }

    private void setWorkManagementMonitoring(ActiveInboundResourceAdapter 
                    adapter, boolean isEnabled) {
        MonitorableWorkManager mwm = (MonitorableWorkManager)adapter.
                                        getBootStrapContext().getWorkManager();
        mwm.setMonitoringEnabled(isEnabled);
    }
    
}
