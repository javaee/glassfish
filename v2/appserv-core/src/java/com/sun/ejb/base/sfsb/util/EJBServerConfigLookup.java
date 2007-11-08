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
 * EJBServerConfigLookup.java
 *
 * Created on January 5, 2004, 4:13 PM
 */

package com.sun.ejb.base.sfsb.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import com.sun.logging.LogDomains;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.EjbContainerAvailability;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.runtime.IASEjbExtraDescriptors;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;

/**
 *
 * @author  lwhite
 */
public class EJBServerConfigLookup {
    
    /**
    * The default store-pool-jndi-name (used by Ejb Container for 
    * Stateful Session Bean checkpointing and passivation to HADB
    */  
    protected final String DEFAULT_STORE_POOL_JNDI_NAME = "jdbc/hastore";
    
    /**
    * The default sfsb-ha-persistence-type (used by Ejb Container for 
    * Stateful Session Bean checkpointing and passivation to HADB
    */  
    protected final String DEFAULT_SFSB_HA_PERSISTENCE_TYPE = "file";    

    /**
    * The default sfsb-non-ha-persistence-type (used by Ejb Container for 
    * Stateful Session Bean checkpointing and passivation to HADB
    */  
    protected final String DEFAULT_SFSB_NON_HA_PERSISTENCE_TYPE = "file"; 
    
    private static final String REPLICATED_TYPE = "replicated";
    
    /**
    * The ejbDescriptor
    */  
    protected EjbDescriptor _ejbDescriptor = null;
    protected boolean	    _haEnabled = false;
    
    /**
    * The config context passed in via constructor
    * used when a dynamic config context is needed
    * rather than usual run-time config context e.g. deployment
    */    
    protected ConfigContext _configContext = null;     

    /** Creates a new instance of EJBServerConfigLookup */
    public EJBServerConfigLookup() {         
    }    
        
    /** Creates a new instance of EJBServerConfigLookup */
    public EJBServerConfigLookup(EjbDescriptor ejbDescriptor) {
        this();
        _ejbDescriptor = ejbDescriptor;        
    }
    
    /** Creates a new instance of EJBServerConfigLookup */
    public EJBServerConfigLookup(EjbDescriptor ejbDescriptor, ConfigContext configContext) {
        this(ejbDescriptor);
        _configContext = configContext;        
    }    
    
    /**
     * is monitoring enabled
     */
    protected static boolean _isDebugMonitoringEnabled = false;
    
    static
	{
            _isDebugMonitoringEnabled = checkDebugMonitoringEnabled();
	}
    
    /**
     * Is (any) monitoring enabled -- private or public
     * Statistics gathering is based on this value
     */    
    public static boolean isMonitoringEnabled() {
        //return (isDebugMonitoringEnabled() || isPublicMonitoringEnabled());
        return isDebugMonitoringEnabled();
    }
    
    /**
     * Is private (internal) monitoring enabled
     */    
    public static boolean isDebugMonitoringEnabled() {
        return _isDebugMonitoringEnabled;
    }    
    
    /**
    * Get the availability-enabled from domain.xml.
    * return false if not found
    */   
    public boolean getAvailabilityEnabledFromConfig() { 
        _logger.finest("in EJBServerConfigLookup>>getAvailabilityEnabledFromConfig");
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            _logger.fine("AvailabilityService was not defined - check domain.xml");
            return false;
        }        
        return as.isAvailabilityEnabled();
    }
    
    /**
    * Get the availability-service element from domain.xml.
    * return null if not found
    */    
    protected AvailabilityService getAvailabilityService() {
        Config configBean = this.getConfigBean();
        if(configBean == null) {
            return null;
        }
        return configBean.getAvailabilityService();
    }
    
    /**
    * Get the ServerContext for this server
    * return null if not found
    */    
    protected ServerContext getServerContext() {
        return ApplicationServer.getServerContext();
    }

    /**
    * Get the ConfigContext for this server
    * return null if not found
    */    
    protected ConfigContext getConfigContext() {
        ServerContext serverCtx = this.getServerContext();
        if(serverCtx == null) {
            return null;
        }
        return serverCtx.getConfigContext();
    } 
    
    /**
    * Get the ConfigContext for this server
    * return null if not found
    */    
    protected ConfigContext getConfigContextDynamic() {
        if (_configContext != null) {
            return _configContext;
        } else {
            return getConfigContext();
        }
    }     

    /**
    * Get the config element from domain.xml.
    * return null if not found
    */     
    private Config getConfigBean() {
        Config configBean = null;
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext(); 
        try {
            configBean = ServerBeansFactory.getConfigBean(configCtx);
        } catch (ConfigException ex) {}

        return configBean;
    }
    
    /**
    * Get the applications element from domain.xml.
    * return null if not found
    */     
    private Applications getApplicationsBean() {
        Applications applicationsBean = null;
        Domain domainBean = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContext();
        try {
            domainBean = ServerBeansFactory.getDomainBean(configCtx);
            if(domainBean != null) {
                applicationsBean = domainBean.getApplications();
            }
        } catch (ConfigException ex) {}

        return applicationsBean;
    }
    
    /**
    * Get the applications element from domain.xml.
    * return null if not found
    */     
    private Applications getApplicationsBeanDynamic() {
        Applications applicationsBean = null;
        Domain domainBean = null;
        ConfigContext configCtx = this.getConfigContextDynamic();
        try {
            domainBean = ServerBeansFactory.getDomainBean(configCtx);
            if(domainBean != null) {
                applicationsBean = domainBean.getApplications();
            }
        } catch (ConfigException ex) {}

        return applicationsBean;
    }    
    
    /**
    * Get the server element from domain.xml.
    * return null if not found
    */    
    private Server getServerBean() {
        Server serverBean = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContext();
        try {
            serverBean = ServerBeansFactory.getServerBean(configCtx);
        } catch (ConfigException ex) {}

        return serverBean;
    }    
    
    /**
    * Get the server name from domain.xml.
    * return null if not found
    */    
    private String getServerName() {
        String result = null;
        Server serverBean = this.getServerBean();
        if(serverBean != null) {
            result = serverBean.getName();
        }
        return result;
    }
    
    /**
    * Get the cluster name from domain.xml for this server.
    * return null if not found
    */ 
    public String getClusterName() {
        String result = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContext();
        
        String serverName = this.getServerName();
        if(serverName == null) {
            return result;
        }
        boolean isClustered = false;
        try {
            isClustered = ServerHelper.isServerClustered(configCtx, serverName);
        } catch (ConfigException ex) {}
        if(!isClustered) {
            result = serverName + "_nc";    //non-clustered example: server1_nc
            return result;
        }
        Cluster cluster = null;
        try {
            cluster = ClusterHelper.getClusterForInstance(configCtx, serverName);
        } catch (ConfigException ex) {}
        if(cluster != null) {
            result = cluster.getName();
        }
        return result;
    }    
    
    /**
    * Get the availability-enabled from domain.xml.
    * This takes into account:
    * global
    * ejb-container-availability
    * j2ee app if not stand-alone
    * ejb-module (if stand-alone)
    * return false if not found
    * FIXME: need to add taking the availability-enabled of the bean itself
    */   
    public boolean calculateEjbAvailabilityEnabledFromConfig() {
        _logger.finest("in EJBServerConfigLookup>>calculateEjbAvailabilityEnabledFromConfig");
        boolean isVirtual = this.isVirtualApplication();
        String appName = this.getApplicationName();
        
        boolean globalAvailability = 
            this.getAvailabilityEnabledFromConfig();
        boolean ejbContainerAvailability = 
            this.getEjbContainerAvailabilityEnabledFromConfig(globalAvailability);
        boolean ejbDescriptorAvailability = true;
//System.out.println("EJBServerConfigLookup: app isVirtual=" + isVirtual);        
        if (isVirtual) {
            boolean ejbModuleAvailability =
                this.getEjbModuleAvailability(appName, ejbContainerAvailability);
            ejbDescriptorAvailability =
                this.getAvailabilityEnabledFromEjbDescriptor(ejbModuleAvailability);
//System.out.println("virtual: ejbModuleAvailability= " + ejbModuleAvailability);
//System.out.println("virtual: ejbDescriptorAvailability= " + ejbDescriptorAvailability);
	    _haEnabled = globalAvailability 
                && ejbContainerAvailability
                && ejbModuleAvailability
                && ejbDescriptorAvailability;
        } else {
            boolean j2eeApplicationAvailability =
                this.getJ2eeApplicationAvailability(appName, ejbContainerAvailability);
            ejbDescriptorAvailability =
                this.getAvailabilityEnabledFromEjbDescriptor(j2eeApplicationAvailability);           
//System.out.println("non-virtual: j2eeApplicationAvailability= " + j2eeApplicationAvailability);
//System.out.println("non-virtual: ejbDescriptorAvailability= " + ejbDescriptorAvailability);            
	    _haEnabled = globalAvailability 
                && ejbContainerAvailability
                && j2eeApplicationAvailability
                && ejbDescriptorAvailability;            
        }
//System.out.println("_haEnabled returned = " + _haEnabled);        
	return _haEnabled;
    }

    public String getPersistenceStoreType() {
	return (_haEnabled)
	    ? getSfsbHaPersistenceTypeFromConfig()
	    : getSfsbNonHaPersistenceTypeFromConfig();
    }

    /**
    * return whether the bean is a "virtual" app - i.e. a stand-alone
    * ejb module
    */     
    private boolean isVirtualApplication() {
        Application application = _ejbDescriptor.getApplication();
        return application.isVirtual();
    }
    
    /**
    * return the name of the application to which the bean belongs
    * it will be the j2ee app name if the bean is part of a j2ee app
    * it will be the ejb module name if the bean is a stand-alone ejb module
    */     
    private String getApplicationName() {
        Application application = _ejbDescriptor.getApplication();
        return application.getRegistrationName();
    }    
    
    /**
    * Get the availability-enabled for the ejb container from domain.xml.
    * return inherited global availability-enabled if not found
    */   
    public boolean getEjbContainerAvailabilityEnabledFromConfig() {
        boolean globalAvailabilityEnabled = this.getAvailabilityEnabledFromConfig();
        _logger.finest("in EJBServerConfigLookup>>getEjbContainerAvailabilityEnabledFromConfig");
        EjbContainerAvailability eas = this.getEjbContainerAvailability();
        if(eas == null) {
            _logger.fine("EjbContainerAvailability was not defined - check domain.xml");
            return globalAvailabilityEnabled;
        }
        
        String easString = eas.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(easString);
        if(bool == null) {
            return globalAvailabilityEnabled;
        } else {
            return bool.booleanValue();
        }        
    }
    
    /**
    * Get the availability-enabled for the ejb container from domain.xml.
    * return inherited global availability-enabled if not found
    */   
    public boolean getEjbContainerAvailabilityEnabledFromConfig(boolean inheritedValue) {
        _logger.finest("in EJBServerConfigLookup>>getEjbContainerAvailabilityEnabledFromConfig");
        EjbContainerAvailability eas = this.getEjbContainerAvailability();
        if(eas == null) {
            _logger.fine("EjbContainerAvailability was not defined - check domain.xml");
            return inheritedValue;
        }
        
        String easString = eas.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(easString);
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        }        
    }

    /**
    * Get the deployed ejb module from domain.xml based on the name
    * return null if not found
    * @param name
    */      
    private EjbModule getEjbModuleByName(String name) {
        EjbModule result = null;
        Applications applicationsBean = this.getApplicationsBeanDynamic();
        if(applicationsBean == null) {
            return null;
        }
        return applicationsBean.getEjbModuleByName(name);
    }
    
    /**
    * return availability-enabled for the deployed ejb module 
    * from domain.xml based on the name
    * return inheritedValue if module not found or module availability
    * is not defined
    * @param name
    * @param inheritedValue
    */     
    private boolean getEjbModuleAvailability(String name, boolean inheritedValue) {
        EjbModule ejbModule =
            this.getEjbModuleByName(name);
    //FIXME remove after testing
//System.out.println("EJBServerConfigLookup>>getEjbModuleAvailability ejbModule = " + ejbModule);    
        if(ejbModule == null) {
            return false;
            //FIXME remove after testing
            //return inheritedValue;
        }
        /* FIXME: remove after testing
        String moduleString = ejbModule.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(moduleString);
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        } 
         */
        return ejbModule.isAvailabilityEnabled();
    }     

    /**
    * Get the deployed j2ee application from domain.xml based on the appName
    * return null if not found
    * @param appName
    */     
    private J2eeApplication getJ2eeApplicationByName(String appName) {
        J2eeApplication result = null;
        Applications applicationsBean = this.getApplicationsBeanDynamic();
        if(applicationsBean == null) {
            return null;
        }
        return applicationsBean.getJ2eeApplicationByName(appName);
    }
    
    /**
    * return availability-enabled for the deployed j2ee application
    * from domain.xml based on the app name
    * return inheritedValue if module not found or j2ee application 
    * availability is not defined
    * @param appName
    * @param inheritedValue
    */    
    private boolean getJ2eeApplicationAvailability(String appName, boolean inheritedValue) {
        J2eeApplication j2eeApp =
            this.getJ2eeApplicationByName(appName);
//FIXME remove after testing
//System.out.println("EJBServerConfigLookup>>getJ2eeApplicationAvailability j2eeApp = " + j2eeApp);
        if(j2eeApp == null) {            
            return false;
            //FIXME remove after testing
            //return inheritedValue;
        }
        /*  FIXME remove after testing
        String appString = j2eeApp.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(appString);
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        }
         */
        return j2eeApp.isAvailabilityEnabled();
    }     

    /**
    * return the ejb-container-availability element from domain.xml
    */     
    private EjbContainerAvailability getEjbContainerAvailability() {
        AvailabilityService availabilityServiceBean = this.getAvailabilityService();
        if(availabilityServiceBean == null) {
            return null;
        }
        return availabilityServiceBean.getEjbContainerAvailability();
    }
    
    /**
    * Get the availability-enabled for the bean from sun-ejb-jar.xml.
    * return true if not found
    */   
    public boolean getAvailabilityEnabledFromEjbDescriptor() { 
        _logger.finest("in EJBServerConfigLookup>>getAvailabilityEnabledFromEjbDescriptor");
        IASEjbExtraDescriptors extraDescriptors = 
            _ejbDescriptor.getIASEjbExtraDescriptors();
        if(extraDescriptors == null) {
            return true;
        }
        String availabilityEnabledString = 
            extraDescriptors.getAttributeValue(extraDescriptors.AVAILABILITY_ENABLED);
        
        Boolean bool = this.toBoolean(availabilityEnabledString);
        if(bool == null) {
            return true;
        } else {
            return bool.booleanValue();
        }         
        
    }    
    
    /**
    * Get the availability-enabled for the bean from sun-ejb-jar.xml.
    * return defaultValue if not found
    */   
    public boolean getAvailabilityEnabledFromEjbDescriptor(boolean inheritedValue) { 
        _logger.finest("in EJBServerConfigLookup>>getAvailabilityEnabledFromEjbDescriptor");
        IASEjbExtraDescriptors extraDescriptors = 
            _ejbDescriptor.getIASEjbExtraDescriptors();
        if(extraDescriptors == null) {
            return inheritedValue;
        }
        String availabilityEnabledString = 
            extraDescriptors.getAttributeValue(extraDescriptors.AVAILABILITY_ENABLED);
        
        Boolean bool = this.toBoolean(availabilityEnabledString);
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        }         
        
    }        
    
    /**
    * Get the store-pool-jndi-name from domain.xml.
    * This is the store-pool-name in <availability-service> element
    * it represents the default for both web & ejb container
    * return DEFAULT_STORE_POOL_JNDI_NAME if not found
    */    
    public String getStorePoolJndiNameFromConfig() {
        _logger.finest("in ServerConfigLookup>>getStorePoolJndiNameFromConfig");
        String result = DEFAULT_STORE_POOL_JNDI_NAME;
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return result;
        }
        String storePoolJndiName = as.getStorePoolName();
        if(storePoolJndiName != null) {
            result = storePoolJndiName;
        }
        return result;
    }    
    
    /**
    * Get the sfsb-store-pool-name from domain.xml.    
    * return DEFAULT_STORE_POOL_JNDI_NAME if not found
    */
    public String getHaStorePoolJndiNameFromConfig() {
        _logger.finest("in EJBServerConfigLookup>>getHaStorePoolJndiNameFromConfig");
        //String result = DEFAULT_STORE_POOL_JNDI_NAME;
        String result = this.getStorePoolJndiNameFromConfig();
        EjbContainerAvailability ejbContainerAvailabilityBean =
            this.getEjbContainerAvailability();
        if(ejbContainerAvailabilityBean == null) {
            return result;
        }
        String result2 = ejbContainerAvailabilityBean.getSfsbStorePoolName();
        if(result2 != null) {
            result = result2;
        }
        return result; 
    }
    
    /**
    * Get the sfsb-ha-persistence-type from domain.xml.    
    * return DEFAULT_SFSB_HA_PERSISTENCE_TYPE if not found
    */
    public String getSfsbHaPersistenceTypeFromConfig() {
        _logger.finest("in EJBServerConfigLookup>>getSfsbHaPersistenceTypeFromConfig");
        String result = DEFAULT_SFSB_HA_PERSISTENCE_TYPE;
        EjbContainerAvailability ejbContainerAvailabilityBean =
            this.getEjbContainerAvailability();
        if(ejbContainerAvailabilityBean == null) {
            return result;
        }
        String result2 = ejbContainerAvailabilityBean.getSfsbHaPersistenceType();
        if(result2 != null) {
            result = result2;
        }
        return result; 
    }    
    
    /**
    * Get the sfsb-non-ha-persistence-type from domain.xml.    
    * return DEFAULT_SFSB_NON_HA_PERSISTENCE_TYPE if not found
    */
    public String getSfsbNonHaPersistenceTypeFromConfig() {
        _logger.finest("in EJBServerConfigLookup>>getSfsbNonHaPersistenceTypeFromConfig");
        String result = DEFAULT_SFSB_NON_HA_PERSISTENCE_TYPE;
        EjbContainerAvailability ejbContainerAvailabilityBean =
            this.getEjbContainerAvailability();
        if(ejbContainerAvailabilityBean == null) {
            return result;
        }
        //String result2 = ejbContainerAvailabilityBean.getSfsbNonHaPersistenceType();
        String result2 = ejbContainerAvailabilityBean.getSfsbPersistenceType();
        if(result2 != null) {
            result = result2;
        }
        return result; 
    }
    
    public static boolean checkDebugMonitoringEnabled() {
        boolean result = false;
	try
        {
            Properties props = System.getProperties();
            String str=props.getProperty("MONITOR_EJB_CONTAINER");
            if(null!=str) {
                if( str.equalsIgnoreCase("TRUE"))
                    result=true;
            } 
        } catch(Exception e)
        {
            //do nothing just return false
        }
        return result;
    }     

    /**
    * convert the input value to the appropriate Boolean value
    * if input value is null, return null
    */    
    protected Boolean toBoolean(String value){
        if(value == null) return null;
        
        if (value.equalsIgnoreCase("true"))
            return Boolean.TRUE;
        if (value.equalsIgnoreCase("yes"))
            return Boolean.TRUE;
        if (value.equalsIgnoreCase("on") )
            return Boolean.TRUE;
        if (value.equalsIgnoreCase("1"))
            return Boolean.TRUE;
    
        return Boolean.FALSE;
    }     
    
    private boolean isReplicationTypeMemory() {
        return REPLICATED_TYPE.equalsIgnoreCase(getSfsbHaPersistenceTypeFromConfig());
    }
    
    public static final boolean needToAddSFSBVersionInterceptors() {
        boolean isClustered = false;
        boolean isEJBAvailabilityEnabled = false;
        boolean isStoreTypeMemory = false;

        
        try {
            EJBServerConfigLookup ejbSCLookup = new EJBServerConfigLookup();
            ConfigContext configCtx = ejbSCLookup.getConfigContext();
 
            isEJBAvailabilityEnabled = ejbSCLookup
                    .getEjbContainerAvailabilityEnabledFromConfig();
            _logger.log(Level.INFO,
                    "EJBSCLookup:: sc.getEjbContainerAvailabilityEnabledFromConfig() ==> "
                            + isEJBAvailabilityEnabled);
 
            try {
                isClustered = ServerHelper.isServerClustered(configCtx,
                        ejbSCLookup.getServerContext().getInstanceName());
            } catch (ConfigException conFigEx) {
                _logger.log(Level.INFO, "Got ConfigException. Will assume "
                        + "isClustered to be false", conFigEx);
                isClustered = false;
            }
            
            isStoreTypeMemory = ejbSCLookup.isReplicationTypeMemory();
        } catch (Exception ex) {
            _logger.log(Level.FINE,
                    "Got exception in needToAddSFSBVersionInterceptors ("
                        + ex + "). SFSBVersionInterceptors not added");
            _logger.log(Level.FINE, "Exception in needToAddSFSBVersionInterceptors", ex);
        }
 
        boolean result = isClustered && isEJBAvailabilityEnabled && isStoreTypeMemory;
        _logger.log(Level.FINE, "EJBServerConfigLookup::==> isClustered:"
                + isClustered + " ; isEJBAvailabilityEnabled: "
                + isEJBAvailabilityEnabled + " ; isStoreTypeMemory ==> "
                + isStoreTypeMemory + " ; result: " + result);
 
        // result = sc.getEjbContainerAvailabilityEnabledFromConfig();
        return result;
    }

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);    
    
}
