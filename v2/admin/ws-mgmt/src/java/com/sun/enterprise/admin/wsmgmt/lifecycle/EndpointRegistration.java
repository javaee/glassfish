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

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.deployment.backend.DeploymentEventManager;
import com.sun.enterprise.admin.wsmgmt.repository.impl.cache.AppServDELImpl;

import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;
import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterRegistry;
import com.sun.enterprise.admin.wsmgmt.filter.impl.AggregateStatsFilter;
import com.sun.enterprise.admin.wsmgmt.stats.spi.StatsProviderManager;
import com.sun.enterprise.admin.wsmgmt.stats.spi.WebServiceEndpointStatsProvider;
import com.sun.enterprise.admin.wsmgmt.stats.impl.WebServiceEndpointStatsProviderImpl;
import com.sun.enterprise.admin.wsmgmt.stats.impl.WebServiceEndpointStatsImpl;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import javax.management.j2ee.statistics.Stats;
import
com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats;
import com.sun.enterprise.server.ApplicationServer;


/**
 * Manages filters for each web service end point, according to their
 * configuration and registers/unregisters the required dotted names.
 * 
 * @author Satish Viswanatham
 * @since  J2SE 5.0
 */
class EndpointRegistration  {

    /**
     * Contructor
     *
     * @param name  Name of the web service endpoint
     * @param moduleName Name of the module (bundleName)
     * @param boolean    Indicates if this module is standalone or not
     * @param vs         Name of the virtual server
     * @param j2eeAppName Name of the J2EE application
     * @param isEjbModule true, if the module of ejb type
     */
     public EndpointRegistration(String n, String modName, String cRoot,
     boolean isSA, String v, String appName, boolean isEjb) {

        name = n;
        moduleName = modName;
        this.ctxRoot = cRoot;
        if ((ctxRoot != null) && (ctxRoot.length() > 0) &&
            (ctxRoot.charAt(0) != '/')) {
            ctxRoot = "/" + ctxRoot;
        }
        if ("".equals(ctxRoot)) {
            ctxRoot = "/";
        }
        isStandAlone = isSA;
        vs = v;
        j2eeAppName = appName;
        isEjbModule = isEjb;
        endpoint =
        WebServiceMgrBackEnd.getManager().getFullyQualifiedName( j2eeAppName,
            moduleName, name);

     }

    /**
     * This method is used during initialization to install Filters required for
     * Monitoring. This method is also called during Monitoring level change
     * event and during deploy/un-deploy
     *
     * @throws
     */
    public void enableLOW() throws MonitoringRegistrationException {
    
            // check for re-deploy case 
            WebServiceEndpointStatsProvider prev =
            StatsProviderManager.getInstance().getEndpointStatsProvider(endpoint);

            if ( prev != null) {
                return;
            }

            Filter f = new AggregateStatsFilter();
            FilterRegistry.getInstance().registerFilter(
                Filter.PRE_PROCESS_REQUEST, endpoint, f);
            FilterRegistry.getInstance().registerFilter(
                Filter.POST_PROCESS_RESPONSE, endpoint, f);

            // register corresponding stats provider
            WebServiceEndpointStatsProvider prov = new
            WebServiceEndpointStatsProviderImpl();

            StatsProviderManager.getInstance().
                registerEndpointStatsProvider(endpoint, prov);

            MonitoringLevelListener listener = new
            WSMonitoringLevelListener(name, moduleName, ctxRoot, isStandAlone,
                vs, j2eeAppName, isEjbModule);
            Stats stats = new WebServiceEndpointStatsImpl(prov);

            String appName = null;
            if ( isStandAlone == true) {
                appName = null;
            } else {
                appName = j2eeAppName;
            }

            if ( isEjbModule ) {
                registry.registerWSAggregateStatsForEjb(stats, 
                    name, moduleName, appName, listener);
            } else {
                registry.registerWSAggregateStatsForWeb(stats, 
                    name, moduleName, ctxRoot, appName, vs, listener);
            }
    }

    public void disableLOW() 
        throws MonitoringRegistrationException {

            FilterRegistry.getInstance().unregisterFilterByName(
            Filter.PRE_PROCESS_REQUEST, endpoint,
            Constants.AGGREGATE_STATS_FILTER );
            FilterRegistry.getInstance().unregisterFilterByName(
            Filter.POST_PROCESS_RESPONSE, endpoint,
            Constants.AGGREGATE_STATS_FILTER );
            if ( isEjbModule ) {
               registry.unregisterWSAggregateStatsForEjb( 
                    name, moduleName, j2eeAppName);
            } else {
                registry.unregisterWSAggregateStatsForWeb( 
                    name, moduleName, ctxRoot, j2eeAppName, vs);
            }
            StatsProviderManager.getInstance().unregisterEndpointStatsProvider(
                endpoint);
    }

    private static MonitoringRegistry registry = ApplicationServer.
                getServerContext().getMonitoringRegistry();;

    // PRIVATE VARIABLES
    private String ctxRoot = null;
    String    name = null;
    String    moduleName = null;
    boolean    isStandAlone = false;
    String    vs = null;
    String    j2eeAppName = null;
    boolean    isEjbModule = false;
    String    endpoint = null;
}
