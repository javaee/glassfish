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
package com.sun.enterprise.admin.wsmgmt.lifecycle;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;


/**
 * Installs filters for each web service end point, according to their
 * configuration.
 * 
 * @author Satish Viswanatham
 * @since  J2SE 5.0
 */
class MonitoringLifeCycleImpl  {


    public void initializeMonitoring(String appId, String modName,
        String ctxRoot, boolean isStandAlone, boolean isEjbModule, String vs,
        WebServiceConfig wsConfig) throws MonitoringRegistrationException {

      if ( wsConfig == null ) {
        return;
      }

      String monLevel = wsConfig.getMonitoringLevel();

      MonitoringLevel newLevel = MonitoringLevel.instance(monLevel);
      if ( newLevel.equals(MonitoringLevel.OFF) ) {
        // monitoring is off, just return
        return;
      }

      instrumentMonitoring(wsConfig.getEndpointName(), modName, ctxRoot,
                           isStandAlone, vs, appId, MonitoringLevel.OFF,
                           newLevel, isEjbModule);   
    }

    public void uninitializeMonitoring(String appId, String modName,
        String ctxRoot, boolean isStandAlone, boolean isEjbModule, String vs,
        WebServiceConfig wsConfig) throws MonitoringRegistrationException {

      if (wsConfig == null) {
        return;
      }
      
      String monLevel = wsConfig.getMonitoringLevel();

      MonitoringLevel oldLevel = MonitoringLevel.instance(monLevel);
      if ( oldLevel.equals(MonitoringLevel.OFF) ) {
        // monitoring is off, just return
        return;
      }

      instrumentMonitoring(wsConfig.getEndpointName(),modName, ctxRoot,
                           isStandAlone, vs, appId, oldLevel,
                           MonitoringLevel.OFF, isEjbModule);   
    }

    /**
     * This method is used during initialization to install Filters required for
     * Monitoring. This method is also called during Monitoring level change
     * event and during deploy/un-deploy
     *
     * @param name  Name of the web service endpoint
     * @param moduleName Name of the module (bundleName)
     * @param boolean    Indicates if this module is standalone or not
     * @param vs         Name of the virtual server
     * @param j2eeAppName Name of the J2EE application
     * @param oldLevel    Old Monitoring Level
     * @param newLevel    New Monitoring Level
     * @param isEjbModule true, if the module of ejb type
     * 
     * @throws
     */
    public void instrumentMonitoring(String name, String moduleName,
        String ctxRoot, boolean isStandAlone, String vs, String j2eeAppName,
        MonitoringLevel oldLevel, MonitoringLevel newLevel,
        boolean isEjbModule) 
        throws MonitoringRegistrationException {
    
        if ( name == null) {
            return;
        }

        EndpointRegistration epr = new EndpointRegistration( name, moduleName,
            ctxRoot, isStandAlone, vs, j2eeAppName, isEjbModule);

        // both Monitoring levels are same, so just return
        if ( oldLevel == newLevel ) {
            return;
        }

        if ( oldLevel == OFF ) {
            if ( newLevel == LOW  ) {
                epr.enableLOW();
            }  
            if ( newLevel == HIGH ) {
                epr.enableLOW();
                // and enable high functionality too
            }  
        }

        if ( oldLevel == LOW ) {
            if ( newLevel == HIGH) {
                // XXX not implemented yet
            }
            if ( newLevel == OFF ){
                epr.disableLOW();
            }
        }

        if ( oldLevel == HIGH ) {
            if ( newLevel == LOW ){
                // not implemented yet
                // keep only LOW
            }
            if ( newLevel == OFF ){
                epr.disableLOW();
            }
        }
    }

    // -- PRIVATE VARIABLES ---
    static MonitoringLifeCycleImpl flcm = null;

    private static final MonitoringLevel OFF = MonitoringLevel.OFF;
    private static final MonitoringLevel HIGH = MonitoringLevel.HIGH;
    private static final MonitoringLevel LOW = MonitoringLevel.LOW;

}
