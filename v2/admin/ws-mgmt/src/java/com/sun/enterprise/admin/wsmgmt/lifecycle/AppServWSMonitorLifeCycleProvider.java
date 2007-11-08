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

import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.sun.enterprise.admin.monitor.WSMonitorLifeCycleProvider;
import com.sun.enterprise.admin.monitor.WSMonitorLifeCycleFactory;

import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigFactory;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigProvider;
import com.sun.enterprise.admin.wsmgmt.config.impl.WebServiceConfigImpl;
import com.sun.enterprise.admin.wsmgmt.config.impl.AppServConfigProvider;

import com.sun.enterprise.admin.wsmgmt.transform.TransformMgr;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.admin.wsmgmt.msg.MessageTraceMgr;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This is the mechanism to initialize web service management functionality. 
 * <br>
 */
public class AppServWSMonitorLifeCycleProvider implements WSMonitorLifeCycleProvider {

    public AppServWSMonitorLifeCycleProvider() {
        try {
            cfgProv = ConfigFactory.getConfigFactory().getConfigProvider();
            InstanceEnvironment ienv =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            appsMgr = new AppsManager(ienv);
        } catch (Exception e) {
            _logger.fine(" Exception during initialization of AppServWSMonitorLifeCycleProvider " + e.getMessage());
            // log exception in FINE level. This excpetion should never occur.
        }
    }

    /**
     * Returns the unique identifier for this RepositoryProvider object.
     *
     * @return fully qualified class name of this RepositoryProvider
     */
    public String getProviderID() {
        return WSMonitorLifeCycleFactory.WSMGMT_DEFAULT_PROVIDER;
    }

    /**
     * Registers the web service endpoints in this application 
     *
     * @param app   application object
     */
    void instrumentWebServiceEndpoints(String appId, BundleDescriptor bd,
        boolean shouldRegister) {

        boolean isEjb = false;
        boolean isStandAlone = false;
        String modName = null;
        String ctxRoot = null;
        String vs  = WebServiceMgrBackEnd.DEFAULT_VIRTUAL_SERVER;
        Server server = null;
        try {
            ServerContext sCtx = ApplicationServer.getServerContext();
            server = ServerHelper.getServerByName(sCtx.getConfigContext(),
                                                  sCtx.getInstanceName());
            ApplicationRef aRef = server.getApplicationRefByRef(appId);
            if (aRef != null) {
                vs = aRef.getVirtualServers();
                if ((vs == null) || ( vs.length() == 0)) {
                    vs = WebServiceMgrBackEnd.DEFAULT_VIRTUAL_SERVER;
                }
            } else {
                    throw new IllegalArgumentException();
            }
        } catch( Exception e) {
            throw new RuntimeException(e);
        }
        
        modName = bd.getModuleDescriptor().getArchiveUri();

        if ( bd instanceof EjbBundleDescriptor ) {
            isEjb = true;
        } else if ( bd instanceof WebBundleDescriptor ) {
            isEjb = false;
            ctxRoot = ((WebBundleDescriptor) bd).getContextRoot();
        } else {
            return;
        }
        
        Application app = bd.getApplication();
        if ( app == null) {
            String msg = _stringMgr.getString("Application_NotFound", appId);
            throw new RuntimeException(msg);
        }
        isStandAlone = app.isVirtual();

        Collection wsCollec = bd.getWebServices().getWebServices();
        Set wsSet = new HashSet();
        for (Iterator i1 = wsCollec.iterator(); i1.hasNext();) {
            WebService ws = (WebService) i1.next();
            wsSet.addAll(ws.getEndpoints());
        }
        for (Iterator i2 = wsSet.iterator(); i2.hasNext();) {
            com.sun.enterprise.deployment.WebServiceEndpoint wse = 
            (com.sun.enterprise.deployment.WebServiceEndpoint) i2.next();
            String epName = wse.getEndpointName();
                if ( shouldRegister == true ) {
                    instrument( epName, modName, ctxRoot, isStandAlone, appId,
                            isEjb, vs);
                } else {
                    uninstrument( epName, modName, ctxRoot, isStandAlone, appId,
                              isEjb, vs);
                }
        }
    }

    /**
     * un registers the web service endpoints in this application 
     *
     * @param app   application object
     */
     public void unregisterWebServiceEndpoints(String appId, 
         BundleDescriptor bd) {
        instrumentWebServiceEndpoints(appId, bd, false);
     }

    /**
     * registers the web service endpoints in this application 
     *
     * @param app   application object
     */
     public void registerWebServiceEndpoints(String appId,BundleDescriptor bd) {
        instrumentWebServiceEndpoints(appId, bd, true);
     }

    /**
     * Returns the list of Web Service Endpoint config in this application 
     *
     * @param appName   Name of the application
     *
     * @return the array of WebServiceConfig
     */
     void instrument(String endpoint, String modName, String ctxRoot,
        boolean isStandAlone, String appId, boolean isEjbModule, String vs) {

            WebServiceConfigImpl wsConfig = (WebServiceConfigImpl) cfgProv.
                getWebServiceConfig( appId, modName, isStandAlone, endpoint) ;

            if (wsConfig == null) {
                return;
            }
            if (isStandAlone) {
                modName = appId;
                appId = null;
            }

            TransformMgr.getInstance().init(appId, wsConfig);

             try {
                 new MonitoringLifeCycleImpl().initializeMonitoring(appId,
                 modName, ctxRoot, isStandAlone, isEjbModule, vs, wsConfig);
            } catch ( Exception e) {
                // log a warning
                _logger.fine("Exception during monitoring initialization " +
                e.getMessage());
            }
        }

    /**
     * Returns the list of Web Service Endpoint config in this application 
     *
     * @param appName   Name of the application
     *
     * @return the array of WebServiceConfig
     */
     void uninstrument(String endpoint, String modName, String ctxRoot,
        boolean isStandAlone,
        String appId, boolean isEjbModule, String vs) {

            try {
                 MessageTraceMgr.getInstance().disable(appId, endpoint);

            } catch ( Exception e) {
                // log a warning
                _logger.fine("Exception while disabling trace" +
                e.getMessage());
            }
            WebServiceConfigImpl wsConfig = (WebServiceConfigImpl) cfgProv.
                getWebServiceConfig(appId, modName, isStandAlone, endpoint) ;

            if (wsConfig == null) {
                return;
            }
            if (isStandAlone) {
                modName = appId;
                appId = null;
            }

            TransformMgr.getInstance().stop(appId, wsConfig);

            try {
                 new MonitoringLifeCycleImpl().uninitializeMonitoring(appId,
                 modName, ctxRoot, isStandAlone, isEjbModule, vs, wsConfig);
            } catch ( Exception e) {
                // log a warning
                _logger.fine("Exception during monitoring shutdown " +
                e.getMessage());
            }
        }

    public void reconfigureMonitoring(WebServiceEndpoint ep, String appId,
    MonitoringLevel oldLevel, MonitoringLevel newLevel) throws MonitoringRegistrationException {
    
        // There no work needed for LOW to HIGH or HIGH or LOW changes.
        // because the monitoring stats collected are the same.

        if ( (oldLevel != MonitoringLevel.OFF) &&
            (newLevel != MonitoringLevel.OFF) ) {
            return;
        }

        if ( ep == null) {
            return;
        }

        boolean isEjb = false;
        boolean isStandAlone = false;
        String modName = null;
        String ctxRoot = null;
        String appName = null;
        String epName = null;
        String vs  = WebServiceMgrBackEnd.DEFAULT_VIRTUAL_SERVER;

        ConfigBean parent = (ConfigBean) ep.parent();
        if (parent instanceof J2eeApplication) {
            isStandAlone = false;
            appName = appId;
            modName =
            WebServiceMgrBackEnd.getManager().getModuleName(ep.getName());
            // set isEjb to true in case of ejb embedded module
            if (modName.endsWith("jar")) {
                isEjb = true;
            } else {
                if (appsMgr != null) {
                    Application app = null;
                    try {
                        app = appsMgr.getDescriptor(appId);
                    } catch (ConfigException ce) {
                        // log a warning    
                    }
                    if (app != null) {
                        WebBundleDescriptor wbd
                            = app.getWebBundleDescriptorByUri(modName);
                        if (wbd != null) {
                            ctxRoot = wbd.getContextRoot();
                        }
                    }
                }
            }
        } else if (parent instanceof WebModule) {
            isStandAlone = true;
            appName = null;
            modName = appId;
            ctxRoot = ((WebModule) parent).getContextRoot();
        } else if (parent instanceof EjbModule) {
            isStandAlone = true;
            isEjb = true;
            appName = null;
            modName = appId;
        }
        epName= WebServiceMgrBackEnd.getManager().getEndpointName(ep.getName());
        
        new MonitoringLifeCycleImpl().instrumentMonitoring(epName, 
                modName, ctxRoot, isStandAlone, vs, appName,
                oldLevel, newLevel, isEjb);

    }


    // PRIVATE VARS
    private AppsManager appsMgr = null;
    private ConfigProvider cfgProv = null;
    private static final StringManager _stringMgr = 
        StringManager.getManager(AppServWSMonitorLifeCycleProvider.class);
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);


}
