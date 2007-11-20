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
 * SessionManagerConfigurationHelper.java
 *
 * Created on December 19, 2003, 3:48 PM
 */

package com.sun.enterprise.web;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;
import com.sun.logging.LogDomains;

import com.sun.enterprise.config.ConfigContext;
import org.apache.catalina.core.StandardContext;
//import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.enterprise.deployment.runtime.web.SessionManager;
import com.sun.enterprise.deployment.runtime.web.ManagerProperties;
import com.sun.enterprise.deployment.runtime.web.StoreProperties;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 *
 * @author  lwhite
 */
public class SessionManagerConfigurationHelper {
    
    /** Creates a new instance of SessionManagerConfigurationHelper */
    public SessionManagerConfigurationHelper(
        WebModule ctx, SessionManager smBean, WebBundleDescriptor wbd, WebModuleConfig wmInfo) {
        _ctx = ctx;
        _smBean = smBean;
        _wbd = wbd;
        _wmInfo = wmInfo;
        _systemApps.add("com_sun_web_ui");
        _systemApps.add(Constants.DEFAULT_WEB_MODULE_PREFIX + "admingui");
        _systemApps.add("adminapp");
        _systemApps.add("admingui");
        
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
            _rb = _logger.getResourceBundle();
        }        
    }
    
    private boolean isSystemApp(String appName) {
        return _systemApps.contains(appName);
    }
    
    public void initializeConfiguration() {
        
        ConfigContext dynamicConfigContext = null;
        com.sun.enterprise.config.serverbeans.WebModule wmBean = _wmInfo.getBean();
        //System.out.println("SessionManagerConfigurationHelper>>initializeConfiguration: wmBean = " + wmBean);
        if(wmBean != null) {
            dynamicConfigContext = wmBean.getConfigContext();
        //System.out.println("SessionManagerConfigurationHelper>>initializeConfiguration: wmBeanOK dynamicConfigContext = " + dynamicConfigContext);
        }
        //System.out.println("SessionManagerConfigurationHelper>>initializeConfiguration: wmBeanNotOK dynamicConfigContext = " + dynamicConfigContext);
    
        boolean isAppDistributable = false;
        if (_wbd != null)         
            isAppDistributable = _wbd.isDistributable();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Web App Distributable (" + getApplicationId(_ctx) + "): " + isAppDistributable);    
        }        
        
        PersistenceType persistence = PersistenceType.MEMORY;
        String persistenceFrequency = null;
        String persistenceScope = null;
        
        //added code - check global availability-enabled
        //if availability-enabled set global ha defaults
        ServerConfigLookup serverConfigLookup = null;
    /*
    System.out.println("SessionManagerConfigurationHelper>>dynamicConfigContext = "
        + dynamicConfigContext);
     */
        if(dynamicConfigContext != null) {
            serverConfigLookup = new  ServerConfigLookup(dynamicConfigContext);
        } else {
            serverConfigLookup = new  ServerConfigLookup();
        }

        //this change brings takes into account both global and web-container
        //availability-enabled settings
        String contextRoot = ((WebModule)_ctx).getContextRoot();
        String j2eeAppName = ((StandardContext)_ctx).getJ2EEApplication();
        boolean isAvailabilityEnabled = 
            //serverConfigLookup.getAvailabilityEnabledFromConfig();
            serverConfigLookup.calculateWebAvailabilityEnabledFromConfig(contextRoot, j2eeAppName);
        //System.out.println("isAvailabilityEnabled = " + isAvailabilityEnabled);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("AvailabilityGloballyEnabled = " + isAvailabilityEnabled);
        }
        if(isAvailabilityEnabled) {
            //these are the global defaults if nothing is
            //set at domain.xml or sun-web.xml
            persistence = PersistenceType.HA;
            persistenceFrequency = "time-based";
            persistenceScope = "session";
        }
        
        //added code - if domain.xml default exists, then use that
        PersistenceType serverDefaultPersistenceType =
            serverConfigLookup.getPersistenceTypeFromConfig();

        if(serverDefaultPersistenceType != null) {        
        //if(serverDefaultPersistenceType != null && !(serverDefaultPersistenceType.equals(PersistenceType.MEMORY)) ) {
            persistence = serverDefaultPersistenceType;        
            persistenceFrequency = serverConfigLookup.getPersistenceFrequencyFromConfig();
            persistenceScope = serverConfigLookup.getPersistenceScopeFromConfig();
        }
        String insLevelPersistenceTypeString = null;
        if(persistence != null) {
            insLevelPersistenceTypeString = persistence.getType();
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("instance-level persistence-type = " + insLevelPersistenceTypeString);
            _logger.finest("instance-level persistenceFrequency = " + persistenceFrequency);
            _logger.finest("instance-level persistenceScope = " + persistenceScope);
        }
        
        String webAppLevelPersistenceFrequency = null;
        String webAppLevelPersistenceScope = null;

        if (_smBean != null) {
            // The persistence-type controls what properties of the 
            // session manager can be configured
            String pType = _smBean.getAttributeValue(SessionManager.PERSISTENCE_TYPE);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("webAppLevelPersistenceType = " + pType);
            }
            //changed code - default back to persistence
            //persistence = PersistenceType.parseType(pType);
            persistence = PersistenceType.parseType(pType, persistence);

            webAppLevelPersistenceFrequency = getPersistenceFrequency(_smBean);           
            webAppLevelPersistenceScope = getPersistenceScope(_smBean);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("webAppLevelPersistenceFrequency = " + webAppLevelPersistenceFrequency);
                _logger.finest("webAppLevelPersistenceScope = " + webAppLevelPersistenceScope);                    
            }
        }
        
        //use web app level values if they exist (i.e. not null)
        if(webAppLevelPersistenceFrequency != null) {
            persistenceFrequency = webAppLevelPersistenceFrequency;
        }
        if(webAppLevelPersistenceScope != null) {
            persistenceScope = webAppLevelPersistenceScope;
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN WebContainer>>ConfigureSessionManager after web level check");
            _logger.finest("AFTER_WEB_PERSISTENCE-TYPE IS = " + persistence.getType());
            _logger.finest("AFTER_WEB_PERSISTENCE_FREQUENCY IS = " + persistenceFrequency);
            _logger.finest("AFTER_WEB_PERSISTENCE_SCOPE IS = " + persistenceScope); 
        }
        
        //delegate remaining initialization to builder
        String frequency = null;
        String scope = null;
        if( persistence == PersistenceType.MEMORY 
            | persistence == PersistenceType.FILE 
            | persistence == PersistenceType.CUSTOM) {
            //deliberately leaving frequency & scope null
        } else {
            frequency = persistenceFrequency;
            scope = persistenceScope;
        }

        //if app is not distributable and non-memory option
        //is attempted, log error and set back to "memory"
        if(!isAppDistributable && persistence != PersistenceType.MEMORY) {
            String wmName = getApplicationId(_ctx);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("is " + wmName + " a system app: " + isSystemApp(wmName));
            }
            //suppress log error msg for default-web-module
            //log message only if availabilityenabled = true is attempted            
            if (isAvailabilityEnabled && !wmName.equals(Constants.DEFAULT_WEB_MODULE_NAME) && !this.isSystemApp(wmName)) { 
                //log error
                Object[] params = { getApplicationId(_ctx), persistence.getType(), frequency, scope };
                _logger.log(Level.INFO, "webcontainer.invalidSessionManagerConfig2",
                            params); 
            }    
            //set back to memory option
            persistence = PersistenceType.MEMORY;
            frequency = null;
            scope = null;            
        }
        
        //if availability-enabled is false, reset to "memory"
        if (!isAvailabilityEnabled && persistence != PersistenceType.FILE) {
            //set back to memory option
            persistence = PersistenceType.MEMORY;
            frequency = null;
            scope = null;             
        }
        
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN WebContainer>>ConfigureSessionManager before builder factory");
            _logger.finest("FINAL_PERSISTENCE-TYPE IS = " + persistence.getType());
            _logger.finest("FINAL_PERSISTENCE_FREQUENCY IS = " + frequency);
            _logger.finest("FINAL_PERSISTENCE_SCOPE IS = " + scope); 
        }
        
        _persistence = persistence;
        _persistenceFrequency = frequency;
        _persistenceScope = scope;
        
    }
    
    /**
     * The application id for this web module
     * HERCULES:add
     */    
    public String getApplicationId(WebModule ctx) {
        com.sun.enterprise.web.WebModule wm = 
            (com.sun.enterprise.web.WebModule)ctx;
        return wm.getID();
    }
    
    /**
     * Get the persistence frequency for this web module
     * (this is the value from sun-web.xml if defined
     * @param the session manager config bean
     * HERCULES:add
     */
    private String getPersistenceFrequency(SessionManager smBean) {
        String persistenceFrequency = null;        
        ManagerProperties mgrBean = smBean.getManagerProperties();
        if ((mgrBean != null) && (mgrBean.sizeWebProperty() > 0)) {
            WebProperty[] props = mgrBean.getWebProperty();
            for (int i = 0; i < props.length; i++) {
                //String name = props[i].getAttributeValue("name");
                //String value = props[i].getAttributeValue("value");                
                String name = props[i].getAttributeValue(WebProperty.NAME);
                String value = props[i].getAttributeValue(WebProperty.VALUE);
                if (name.equalsIgnoreCase("persistenceFrequency")) {
                    persistenceFrequency = value;
                }
            }
        }
        return persistenceFrequency;
    }
    
    /**
     * Get the persistence scope for this web module
     * (this is the value from sun-web.xml if defined
     * @param the session manager config bean
     * HERCULES:add
     */    
    private String getPersistenceScope(SessionManager smBean) {
        String persistenceScope = null;
        StoreProperties storeBean = smBean.getStoreProperties();
        if ((storeBean != null) && (storeBean.sizeWebProperty() > 0)) {
            WebProperty[] props = storeBean.getWebProperty();
            for (int i = 0; i < props.length; i++) {
                //String name = props[i].getAttributeValue("name");
                //String value = props[i].getAttributeValue("value");
                String name = props[i].getAttributeValue(WebProperty.NAME);
                String value = props[i].getAttributeValue(WebProperty.VALUE);                
                if (name.equalsIgnoreCase("persistenceScope")) {
                    persistenceScope = value;
                }
            }
        }
        return persistenceScope;
    } 
    
    public void checkInitialization() {
        if(!_initialized) {
            initializeConfiguration();
            _initialized = true;
        }
    }    
    
    public PersistenceType getPersistenceType() {
        checkInitialization();
        return _persistence;
    }
    
    public String getPersistenceFrequency() {
        checkInitialization();
        return _persistenceFrequency;
    } 
    
    public String getPersistenceScope() {
        checkInitialization();
        return _persistenceScope;
    }     
    
    WebModule _ctx = null;
    SessionManager _smBean = null; 
    WebBundleDescriptor _wbd = null;
    WebModuleConfig _wmInfo = null;
    PersistenceType _persistence = PersistenceType.MEMORY;
    String _persistenceFrequency = null;
    String _persistenceScope = null;
    boolean _initialized = false;
    ArrayList _systemApps = new ArrayList();
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static Logger _logger = null;
    
    /**
     * The resource bundle containing the message strings for _logger.
     */
    protected static ResourceBundle _rb = null;    
    
}
