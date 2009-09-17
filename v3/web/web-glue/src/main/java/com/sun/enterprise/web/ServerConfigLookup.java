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

package com.sun.enterprise.web;

import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.config.ConfigBeanProxy;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ExtensionModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ManagerProperties;
import org.jvnet.hk2.config.types.Property;
import com.sun.enterprise.config.serverbeans.SessionConfig;
import com.sun.enterprise.config.serverbeans.SessionManager;
import com.sun.enterprise.config.serverbeans.SessionProperties;
import com.sun.enterprise.config.serverbeans.StoreProperties;
import com.sun.enterprise.config.serverbeans.WebContainerAvailability;
import com.sun.enterprise.config.serverbeans.WebContainer;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.logging.LogDomains;

public class ServerConfigLookup {

    protected static final Logger _logger = LogDomains.getLogger(
            ServerConfigLookup.class, LogDomains.WEB_LOGGER);

    /**
     * The property name in domain.xml to obtain
     * the EE builder path - this property is not expected
     * now to change and if it ever did, then the directory
     * and package structure for the builder classes would
     * have to change also
     */  
    private static final String EE_BUILDER_PATH_PROPERTY_NAME =
        "ee-builder-path";      
  
    /**
     * The default path to the EE persistence strategy builders 
     */ 
    private static final String DEFAULT_EE_BUILDER_PATH =
        "com.sun.enterprise.ee.web.initialization";

    private Config configBean;

    private ClassLoaderHierarchy clh;


    /**
     * Constructor
     */
    public ServerConfigLookup(Config configBean, ClassLoaderHierarchy clh) {
        this.configBean = configBean;
        this.clh = clh;
    }

    /**
     * Get the session manager bean from domain.xml
     * return null if not defined or other problem
     */  
    public SessionManager getInstanceSessionManager() { 
        if (configBean == null) {
            return null;
        }
        
        WebContainer webContainerBean
            = configBean.getWebContainer();
        if (webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if (sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionManager();
    }    
    
    /**
     * Get the manager properties bean from domain.xml
     * return null if not defined or other problem
     */  
    public ManagerProperties getInstanceSessionManagerManagerProperties() {
        
        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getManagerProperties();
    } 
    
    /**
     * Get the store properties bean from domain.xml
     * return null if not defined or other problem
     */  
    public StoreProperties getInstanceSessionManagerStoreProperties() {
        
        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getStoreProperties();
    } 

    /**
     * Get the session properties bean from server.xml
     * return null if not defined or other problem
     */      
    public SessionProperties getInstanceSessionProperties() { 
        if (configBean == null) {
            return null;
        }
        
        WebContainer webContainerBean
            = configBean.getWebContainer();
        if (webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if (sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionProperties();
    }

    /**
     * Get the EE_BUILDER_PATH from server.xml.
     * this defaults to EE_BUILDER_PATH but can be modified
     * this is the fully qualified path to the EE builders
     */
    public String getEEBuilderPathFromConfig() {
        return getWebContainerAvailabilityPropertyString(
            EE_BUILDER_PATH_PROPERTY_NAME, DEFAULT_EE_BUILDER_PATH);
    }     

    /**
     * Get the availability-service element from domain.xml.
     * return null if not found
     */     
    protected AvailabilityService getAvailabilityService() {
        if (configBean == null) {
            return null;
        }

        return configBean.getAvailabilityService();
    }    

    /**
     * Get the availability-enabled from domain.xml.
     * return false if not found
     */   
    public boolean getAvailabilityEnabledFromConfig() {
        AvailabilityService as = getAvailabilityService();
        if (as == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("AvailabilityService was not defined - check domain.xml");
            }
            return false;
        }        

        Boolean bool = toBoolean(as.getAvailabilityEnabled());
        if (bool == null) {
            return false;
        } else {
            return bool;
        }       
    }

    /**
     * Get the web-container-availability element from domain.xml.
     * return null if not found
     */     
    private WebContainerAvailability getWebContainerAvailability() {
        AvailabilityService availabilityServiceBean = this.getAvailabilityService();
        if (availabilityServiceBean == null) {
            return null;
        }

        return availabilityServiceBean.getWebContainerAvailability();
    } 
    
    /**
     * Get the String value of the property under web-container-availability 
     * element from domain.xml whose name matches propName
     * return null if not found
     * @param propName
     */     
    protected String getWebContainerAvailabilityPropertyString(
                String propName) {
        return getWebContainerAvailabilityPropertyString(propName, null);
    }

    /**
     * Get the String value of the property under web-container-availability 
     * element from domain.xml whose name matches propName
     * return defaultValue if not found
     * @param propName
     */    
    protected String getWebContainerAvailabilityPropertyString(
                String propName,
                String defaultValue) {
        WebContainerAvailability wcAvailabilityBean = getWebContainerAvailability();
        if (wcAvailabilityBean == null) {
            return defaultValue;
        }

        List<Property> props = wcAvailabilityBean.getProperty();
        if (props == null) {
            return defaultValue;
        }

        for (Property prop : props) {
            String name = prop.getName();
            String value = prop.getValue();
            if (name.equalsIgnoreCase(propName)) {
                return value;
            }
        }

        return defaultValue;
    } 


    /**
     * Get the availability-enabled for the web container from domain.xml.
     * return inherited global availability-enabled if not found
     */   
    public boolean getWebContainerAvailabilityEnabledFromConfig() {
        boolean globalAvailabilityEnabled = getAvailabilityEnabledFromConfig();
        WebContainerAvailability was = getWebContainerAvailability();
        if (was == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainerAvailability not defined - check domain.xml");
            }
            return globalAvailabilityEnabled;
        }
        
        Boolean bool = toBoolean(was.getAvailabilityEnabled());
        if (bool == null) {
            return globalAvailabilityEnabled;
        } else {
            return bool;
        }       
    } 
    
    /**
     * Get the availability-enabled for the web container from domain.xml.
     * return inherited global availability-enabled if not found
     */   
    public boolean getWebContainerAvailabilityEnabledFromConfig(boolean inheritedValue) {
        WebContainerAvailability was = getWebContainerAvailability();
        if (was == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainerAvailability not defined - check domain.xml");
            }
            return inheritedValue;
        }
        
        Boolean bool = toBoolean(was.getAvailabilityEnabled());
        if (bool == null) {
            return inheritedValue;
        } else {
            return bool;
        }       
    }    

    /**
     * Get the availability-enabled from domain.xml.
     * This takes into account:
     * global
     * web-container-availability
     * web-module (if stand-alone)
     * return false if not found
     */   
    public boolean calculateWebAvailabilityEnabledFromConfig(WebModule ctx) { 
        // global availability from <availability-service> element
        boolean globalAvailability = getAvailabilityEnabledFromConfig();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("globalAvailability = " + globalAvailability);
        }

        // web container availability from <web-container-availability>
        // sub-element
        boolean webContainerAvailability = 
            getWebContainerAvailabilityEnabledFromConfig(globalAvailability);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("webContainerAvailability = " + webContainerAvailability);
        }        

        String webModuleAvailabilityString = null;
        ConfigBeanProxy bean = ctx.getBean();
        if (bean != null) {
            if (bean instanceof com.sun.enterprise.config.serverbeans.WebModule) {
                webModuleAvailabilityString =
                    ((com.sun.enterprise.config.serverbeans.WebModule) bean).getAvailabilityEnabled();
            } else if (bean instanceof ExtensionModule) {
                webModuleAvailabilityString =
                    ((ExtensionModule) bean).getAvailabilityEnabled();
            }
        }

        boolean webModuleAvailability = false;
        Boolean bool = toBoolean(webModuleAvailabilityString);
        if (bool != null) {
            webModuleAvailability = bool;
        }       

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("webModuleAvailability = " + webModuleAvailability);
        }

        return globalAvailability 
                && webContainerAvailability 
                && webModuleAvailability;
    }    

    /**
     * Get the persistenceType from domain.xml.
     * return null if not found
     */
    public PersistenceType getPersistenceTypeFromConfig() {
        String persistenceTypeString = null;      
        PersistenceType persistenceType = null;

        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        persistenceTypeString = webContainerAvailabilityBean.getPersistenceType();

        if (persistenceTypeString != null) {
            persistenceType = PersistenceType.parseType(persistenceTypeString);
        }
        if (persistenceType != null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SERVER.XML persistenceType= " + persistenceType.getType());
            }
        } else {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SERVER.XML persistenceType missing");
            }
        }

        return persistenceType;
    }     
    
    /**
     * Get the persistenceFrequency from domain.xml.
     * return null if not found
     */
    public String getPersistenceFrequencyFromConfig() { 
        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceFrequency();      
    }
    
    /**
     * Get the persistenceScope from domain.xml.
     * return null if not found
     */
    public String getPersistenceScopeFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceScope(); 
    }     

    /**
     * convert the input value to the appropriate Boolean value
     * if input value is null, return null
     */     
    protected Boolean toBoolean(String value) {
        if (value == null) return null;
        
        if (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("1")) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    /**
     * Loads the requested class using the Common Classloader
     *
     * @param className the name of the class to load
     *
     * @return the loaded class
     */
    Class loadClass(String className) throws Exception {
        return clh.getCommonClassLoader().loadClass(className);
    }
}
