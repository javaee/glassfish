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
package com.sun.enterprise.ee.synchronization;

import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.ConfigException;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

/**
 * Constructs the synchronization application requests for a given 
 * server instance.
 * 
 * @author Nazrul Islam
 */
public class ServerDirector {
    
    private static Logger _logger = Logger.getLogger(EELogDomains.
                        SYNCHRONIZATION_LOGGER);

    /**
     * Constructor!
     *
     * @param  ctx  config context
     * @param  serverName  name of the server name
     */
    public ServerDirector(ConfigContext ctx, String serverName) {
        _configCtx   = ctx;
        _serverName  = serverName;
    }

    /**
     * Returns a list of associated application directories/paths.
     *
     * @return   list of associated application dirs
     */
    public List constructIncludes() {
        buildJ2EEApplicationIncludes();
        buildEJBModuleIncludes();
        buildWebModuleIncludes();
        buildConnectorModuleIncludes();
        buildAppclientModuleIncludes();
        buildLifecycleModuleIncludes();

        return _includes;
    }

    /**
     * Returns a list of excluded directories/paths.
     *
     * @return   list of un-associated application dirs
     */
    public List constructExcludes() {
        buildJ2EEApplicationExcludes();
        buildEJBModuleExcludes();
        buildWebModuleExcludes();
        buildConnectorModuleExcludes();
        buildAppclientModuleExcludes();
        buildLifecycleModuleExcludes();

        return _excludes;
    }

    /**
     * Returns a list of synchronization requests of 
     * associated applications.
     */
    public List construct() {
        buildJ2EEApplications();
        buildEJBModules();
        buildWebModules();
        buildConnectorModules();
        buildAppclientModules();
        buildLifecycleModules();

        return _requests;
    }

    private void buildJ2EEApplicationExcludes() {

        J2eeApplication[] j2eeApps = null;
        try {
            j2eeApps = ServerHelper.getUnAssociatedJ2eeApplications(_configCtx, 
                                                                  _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_apps",_serverName);

        }

        _excludes.addAll( getJ2EEApplicationDirs(j2eeApps) );
    }

    private void buildJ2EEApplicationIncludes() {

        J2eeApplication[] j2eeApps = null;
        try {
            j2eeApps = ServerHelper.getAssociatedJ2eeApplications(_configCtx, 
                                                                  _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_apps",_serverName);

        }

        _includes.addAll( getJ2EEApplicationDirs(j2eeApps) );
    }

    private List getJ2EEApplicationDirs(J2eeApplication[] j2eeApps) {
            
        List list = new ArrayList();

        if (j2eeApps != null) {
            ApplicationRequestBuilder aBuilder = 
                new ApplicationRequestBuilder(_configCtx, _serverName);
            for (int i=0; i<j2eeApps.length; i++) {
                list.addAll( aBuilder.getAllDirectories(j2eeApps[i]) );
            }
        }

        return list;
    }

    private void buildJ2EEApplications() {

        J2eeApplication[] j2eeApps = null;
        try {
            j2eeApps = ServerHelper.getAssociatedJ2eeApplications(_configCtx, 
                                                                  _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_apps",_serverName);
        }
            
        if (j2eeApps != null) {
            for (int i=0; i<j2eeApps.length; i++) {
                ApplicationRequestBuilder aBuilder = 
                    new ApplicationRequestBuilder(_configCtx, _serverName);
                ApplicationSynchRequest asr = aBuilder.build(j2eeApps[i]);
                _requests.add(asr);
            }
        }
    }

    private void buildEJBModuleExcludes() {
        
        EjbModule[] ejbMods = null;
        try {
            ejbMods = ServerHelper.getUnAssociatedEjbModules(_configCtx, 
                                                           _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_ejbs",_serverName);
        }

        _excludes.addAll( getEJBModuleDirs(ejbMods) );
    }

    private void buildEJBModuleIncludes() {
        
        EjbModule[] ejbMods = null;
        try {
            ejbMods = ServerHelper.getAssociatedEjbModules(_configCtx, 
                                                           _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_ejbs",_serverName);
        }

        _includes.addAll( getEJBModuleDirs(ejbMods) );
    }

    private List getEJBModuleDirs(EjbModule[] ejbMods) {
        
        List list = new ArrayList();

        if (ejbMods != null) {
            EjbModuleRequestBuilder eBuilder = 
                new EjbModuleRequestBuilder(_configCtx, _serverName);
            for (int i=0; i<ejbMods.length; i++) {
                list.addAll( eBuilder.getAllDirectories(ejbMods[i]) );
            }
        }

        return list;
    }

    private void buildEJBModules() {
        
        EjbModule[] ejbMods = null;
        try {
            ejbMods = ServerHelper.getAssociatedEjbModules(_configCtx, 
                                                           _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_ejbs",_serverName);
        }
        
        if (ejbMods != null) {
            for (int i=0; i<ejbMods.length; i++) {
                EjbModuleRequestBuilder eBuilder = 
                    new EjbModuleRequestBuilder(_configCtx, _serverName);
                ApplicationSynchRequest asr = eBuilder.build(ejbMods[i]);
                _requests.add(asr);
            }
        }
    }

    private void buildWebModuleExcludes() {
        
        WebModule[] webMods = null;
        try {
            webMods = ServerHelper.getUnAssociatedWebModules(_configCtx, 
                                                           _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_web",_serverName);
        }

        _excludes.addAll( getWebModuleDirs(webMods) );
    }
        
    private void buildWebModuleIncludes() {
        
        WebModule[] webMods = null;
        try {
            webMods = ServerHelper.getAssociatedWebModules(_configCtx, 
                                                           _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_web",_serverName);
        }

        _includes.addAll( getWebModuleDirs(webMods) );
    }
        
    private List getWebModuleDirs(WebModule[] webMods) {

        List list = new ArrayList();

        if (webMods != null) {
            WebModuleRequestBuilder wBuilder = 
                new WebModuleRequestBuilder(_configCtx, _serverName);
            for (int i=0; i<webMods.length; i++) {
                list.addAll( wBuilder.getAllDirectories(webMods[i]) );
            }
        }

        return list;
    }

    private void buildWebModules() {
        
        WebModule[] webMods = null;
        try {
            webMods = ServerHelper.getAssociatedWebModules(_configCtx, 
                                                           _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_web",_serverName);
        }
        
        if (webMods != null) {
            for (int i=0; i<webMods.length; i++) {
                WebModuleRequestBuilder wBuilder = 
                    new WebModuleRequestBuilder(_configCtx, _serverName);
                ApplicationSynchRequest asr = wBuilder.build(webMods[i]);
                _requests.add(asr);
            }
        }
    }

    private void buildAppclientModuleExcludes() {
        
        AppclientModule[] appclientMods = null;
        try {
            appclientMods = ServerHelper.getUnAssociatedAppclientModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_appclient",_serverName);
        }
        _excludes.addAll( getAppclientModuleDirs(appclientMods) );
    }

    private void buildAppclientModuleIncludes() {
        
        AppclientModule[] appclientMods = null;
        try {
            appclientMods = ServerHelper.getAssociatedAppclientModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_appclient",_serverName);
        }
        _includes.addAll( getAppclientModuleDirs(appclientMods) );
    }

    private List getAppclientModuleDirs(AppclientModule[] appclientMods) {

        List list = new ArrayList();
        
        if (appclientMods != null) {
            AppclientModuleRequestBuilder acBuilder = 
                new AppclientModuleRequestBuilder(_configCtx, _serverName);
            for (int i=0; i<appclientMods.length; i++) {
                list.addAll(acBuilder.getAllDirectories(appclientMods[i]));
            }
        }

        return list;
    }

    private void buildAppclientModules() {
        
        AppclientModule[] acMods = null;
        try {
            acMods = ServerHelper.getAssociatedAppclientModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_appclient",_serverName);
        }
        
        if (acMods != null) {
            for (int i=0; i<acMods.length; i++) {
                AppclientModuleRequestBuilder acBuilder = 
                    new AppclientModuleRequestBuilder(_configCtx, _serverName);
                ApplicationSynchRequest asr = acBuilder.build(acMods[i]);
                _requests.add(asr);
            }
        }
    }

    private void buildConnectorModuleExcludes() {
        
        ConnectorModule[] connectorMods = null;
        try {
            connectorMods = ServerHelper.getUnAssociatedConnectorModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_connector",_serverName);
        }
        _excludes.addAll( getConnectorModuleDirs(connectorMods) );
    }
        
    private void buildConnectorModuleIncludes() {
        
        ConnectorModule[] connectorMods = null;
        try {
            connectorMods = ServerHelper.getAssociatedConnectorModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_connector",_serverName);
        }
        _includes.addAll( getConnectorModuleDirs(connectorMods) );
    }
        
    private List getConnectorModuleDirs(ConnectorModule[] connectorMods) {

        List list = new ArrayList();

        if (connectorMods != null) {
            ConnectorModuleRequestBuilder cBuilder = 
                new ConnectorModuleRequestBuilder(_configCtx, _serverName);
            for (int i=0; i<connectorMods.length; i++) {
                list.addAll(cBuilder.getAllDirectories(connectorMods[i]));
            }
        }

        return list;
    }

    private void buildConnectorModules() {
        
        ConnectorModule[] connectorMods = null;
        try {
            connectorMods = ServerHelper.getAssociatedConnectorModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_connector",_serverName);
        }
        
        if (connectorMods != null) {
            for (int i=0; i<connectorMods.length; i++) {
                ConnectorModuleRequestBuilder cBuilder = 
                    new ConnectorModuleRequestBuilder(_configCtx, _serverName);
                ApplicationSynchRequest asr = cBuilder.build(connectorMods[i]);
                _requests.add(asr);
            }
        }
    }

    private void buildLifecycleModuleExcludes() {
        
        LifecycleModule[] lifecycleMods = null;
        try {
            lifecycleMods = ServerHelper.getUnAssociatedLifecycleModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_lifecycle",_serverName);
        }
        _excludes.addAll( getLifecycleModuleDirs(lifecycleMods) );
    }

    private void buildLifecycleModuleIncludes() {
        
        LifecycleModule[] lifecycleMods = null;
        try {
            lifecycleMods = ServerHelper.getAssociatedLifecycleModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_lifecycle",_serverName);
        }
        _includes.addAll( getLifecycleModuleDirs(lifecycleMods) );
    }

    private List getLifecycleModuleDirs(LifecycleModule[] lifecycleMods) {

        List list = new ArrayList();
        
        if (lifecycleMods != null) {
            LifecycleModuleRequestBuilder lBuilder = 
                new LifecycleModuleRequestBuilder(_configCtx, _serverName);
            for (int i=0; i<lifecycleMods.length; i++) {
                list.addAll(lBuilder.getAllDirectories(lifecycleMods[i]));
            }
        }

        return list;
    }

    private void buildLifecycleModules() {
        
        LifecycleModule[] lifecycleMods = null;
        try {
            lifecycleMods = ServerHelper.getAssociatedLifecycleModules(
                                                    _configCtx, _serverName);
        } catch (ConfigException ce) {
            _logger.log(Level.FINE,
                "synchronization.config_no_lifecycle",_serverName);
        }
        
        if (lifecycleMods != null) {
            for (int i=0; i<lifecycleMods.length; i++) {
                LifecycleModuleRequestBuilder lBuilder = 
                    new LifecycleModuleRequestBuilder(_configCtx, _serverName);
                ApplicationSynchRequest asr = lBuilder.build(lifecycleMods[i]);
                _requests.add(asr);
            }
        }
    }


    // ---- INSTANCE VARIABLE - PRIVATE ------------------
    private String _serverName = null;
    private ConfigContext _configCtx = null;
    private ArrayList _requests = new ArrayList();
    private ArrayList _excludes = new ArrayList();
    private ArrayList _includes = new ArrayList();
}
