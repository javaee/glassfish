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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.connectors.util.JmsRaUtil;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.connectors.system.ActiveJmsResourceAdapter;
import com.sun.enterprise.connectors.util.*;
import com.sun.enterprise.connectors.work.monitor.ConnectorWorkMgmtStatsImpl;
import com.sun.enterprise.connectors.work.monitor.MonitorableWorkManager;
import com.sun.enterprise.NamingManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.connectors.inflow.MessageEndpointFactoryInfo;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.server.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;
import java.util.Properties;
import java.util.Iterator;
import java.util.logging.*;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.naming.Reference;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;

/**
 * This class represents a live inbound resource adapter, i.e.

 * A resource adapter is considered active after start()
 * and before stop() is called.
 *
 * @author	Binod P G, Sivakumar Thyagarajan 
 */

public class ActiveInboundResourceAdapter extends 
                                ActiveOutboundResourceAdapter {
    
    protected ResourceAdapter resourceadapter_; //runtime instance

    //beanID -> endpoint factory and its activation spec
    private Hashtable<String, MessageEndpointFactoryInfo> factories_; 

    protected String moduleName_;
    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    private StringManager localStrings = 
        StringManager.getManager( ActiveInboundResourceAdapter.class );

    private BootstrapContext bootStrapContextImpl;

    /**
     * Creates an active inbound resource adapter. Sets all RA java bean 
     * properties and issues a start.
     *
     * @param ra <code>ResourceAdapter<code> java bean.
     * @param desc <code>ConnectorDescriptor</code> object.
     * @param moduleName Resource adapter module name.
     * @param jcl <code>ClassLoader</code> instance.
     * @throws ConnectorRuntimeException If there is a failure in loading
     *         or starting the resource adapter.
     */
    public ActiveInboundResourceAdapter(
            ResourceAdapter ra, ConnectorDescriptor desc, String moduleName, 
            ClassLoader jcl) throws ConnectorRuntimeException {
        super(desc,moduleName,jcl);
        this.resourceadapter_ = ra;
        this.factories_       = new Hashtable<String, MessageEndpointFactoryInfo>();
        this.moduleName_      = moduleName;
        try {
                loadRAConfiguration();
                ConnectorRegistry registry = ConnectorRegistry.getInstance();
                String poolId = null;
                ResourceAdapterConfig raConfig = 
                              registry.getResourceAdapterConfig(moduleName_);
                if (raConfig != null) {
                    poolId = raConfig.getThreadPoolIds();
                }
                this.bootStrapContextImpl = new BootstrapContextImpl(poolId);
                
                if (this.moduleName_.equals(ConnectorRuntime.DEFAULT_JMS_ADAPTER)) {
                    java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedExceptionAction() {
                        public java.lang.Object run() throws
                                  ResourceAdapterInternalException {
                            resourceadapter_.start(bootStrapContextImpl);
                            return null;
                        }
                    });
                } else {
                    resourceadapter_.start(bootStrapContextImpl);
                }

                setupMonitoring();
                
        } catch(ResourceAdapterInternalException ex) {
                _logger.log(Level.SEVERE,"rardeployment.start_failed",ex);
            String i18nMsg = localStrings.getString(
                "rardeployment.start_failed", ex.getMessage());
                ConnectorRuntimeException cre = 
                    new ConnectorRuntimeException( i18nMsg );
                cre.initCause( ex );	    
                throw cre;
        } catch(PrivilegedActionException pex) {
                _logger.log(Level.SEVERE,"rardeployment.start_failed",pex.getException());
            String i18nMsg = localStrings.getString(
                "rardeployment.start_failed", pex.getException().getMessage());
                ConnectorRuntimeException cre = 
                    new ConnectorRuntimeException( i18nMsg );
                cre.initCause( pex.getException() );	    
                throw cre;
        } catch (Throwable t) {
            _logger.log(Level.SEVERE,"rardeployment.start_failed",t);
	    t.printStackTrace(); 
            String i18nMsg = localStrings.getString(
                "rardeployment.start_failed", t.getMessage() );
        		ConnectorRuntimeException cre = 
        		    new ConnectorRuntimeException( i18nMsg );
	     if (t.getCause() != null) {	
		/* Put the correct cause of the exception
 	 	 * Fixes issue : 6543333
		 */
       	        cre.initCause( t.getCause());	    
  	     } else {
		cre.initCause(t);
	     } 
       	     throw cre;
        }
    }

    private void setupMonitoring() {
        //if monitoring enabled - register workstatsimpl for
        //this inbound resource adapter and endpointfactory stats
        //for inbound-resource-adapter&message-listener-type
        registerWorkStats();
    }

    private void unSetupMonitoring() {
        //if monitoring enabled - register workstatsimpl for
        //this inbound resource adapter and endpointfactory stats
        //for inbound-resource-adapter&message-listener-type
        unRegisterWorkStats();
    }
    


    private void unRegisterWorkStats() {
        if ( getWorkStatsMonitoringLevel() != MonitoringLevel.OFF ) {
            AccessController.doPrivileged( new PrivilegedAction() {
                public Object run() {
                    try {
                        ServerContext ctxt = ApplicationServer.getServerContext();
                        MonitoringRegistry monRegistry = ctxt.getMonitoringRegistry();
                        String moduleName = ActiveInboundResourceAdapter.this.getModuleName();
                        monRegistry.unregisterConnectorWorkMgmtStats(
                                        ConnectorAdminServiceUtils.getApplicationName(moduleName), 
                                        ConnectorAdminServiceUtils.getConnectorModuleName(moduleName),
                                        ConnectorAdminServiceUtils.isJMSRA(moduleName));
                    } catch( Exception mre) {
                       _logger.log( Level.INFO, "poolmon.cannot_unreg", 
                           mre.getMessage() );
                    }
                    return null;
                }
            });
       }
    }

    private void registerWorkStats() {
        if (getWorkStatsMonitoringLevel() != MonitoringLevel.OFF) {
            
            ((MonitorableWorkManager)this.getBootStrapContext().getWorkManager())
                                                .setMonitoringEnabled(true);
            
            AccessController.doPrivileged( new PrivilegedAction() {
                public Object run() {
                    try {
                        
                        ServerContext ctxt = ApplicationServer.getServerContext();
                        MonitoringRegistry monRegistry = ctxt.getMonitoringRegistry();
                        String moduleName = ActiveInboundResourceAdapter.this.getModuleName();
                        //@todo :: after MBeans are modified
                        //Dont register system RARs as of now until MBean changes are complete.
                        if (ResourcesUtil.createInstance().belongToSystemRar(moduleName)) {
                            if (!ConnectorAdminServiceUtils.isJMSRA(moduleName)) {
                                return null;
                            }
                        }
                        
                        ConnectorWorkMgmtStatsImpl workstatsimpl =
                            new ConnectorWorkMgmtStatsImpl(ActiveInboundResourceAdapter.this);
                        monRegistry.registerConnectorWorkMgmtStats(
                                   workstatsimpl, 
                                   ConnectorAdminServiceUtils.getApplicationName(moduleName),
                                   ConnectorAdminServiceUtils.getConnectorModuleName(moduleName),
                                   ConnectorAdminServiceUtils.isJMSRA(moduleName),
                                   null);
                    } catch( Exception mre ) {
                        _logger.log( Level.INFO, "poolmon.cannot_reg",
                        mre.getMessage() );
                    }
                
                return null;
                }
            });
            if ( _logger.isLoggable( Level.FINE ) ) {
                _logger.fine("Enabled work monitoring at IBRA creation for "
                                + this.getModuleName());
            }
        }
    }

    /*
     * Gets the current monitoring level for Jdbc pools
     */
    private MonitoringLevel getWorkStatsMonitoringLevel() {
        Config cfg = null;
        MonitoringLevel off = MonitoringLevel.OFF;
        MonitoringLevel l = null;
        try {
            cfg = ServerBeansFactory.getConfigBean(
                            ApplicationServer.getServerContext().getConfigContext());
            String lvl = null;
            lvl  = cfg.getMonitoringService().getModuleMonitoringLevels().getConnectorService();
            l = MonitoringLevel.instance( lvl );
            if (l == null ) {
                //dont bother to throw an exception
                return off;
            }
            return l;
        } catch (Exception e) {
            return off;
        }
    }
    

    /**
     * Retrieves the resource adapter java bean.
     *
     * @return <code>ResourceAdapter</code>
     */
    public ResourceAdapter getResourceAdapter() {
        return this.resourceadapter_;
    }

    /**
     * Does the necessary initial setup. Creates the default pool and
     * resource.
     * 
     * @throws ConnectorRuntimeException If there is a failure  
     */
    public void setup() throws ConnectorRuntimeException {
        if(connectionDefs_ == null || connectionDefs_.length == 0) {
            return;
        }
        obtainServerXMLvalue();
        ResourcesUtil resUtil = ResourcesUtil.createInstance();

        if(isServer() && !resUtil.belongToSystemRar(moduleName_)) {
            createAllConnectorResources();
        }
    }

    private void obtainServerXMLvalue(){

    }

    /**
     * Destroys default pools and resources. Stops the Resource adapter
     * java bean.
     */
    public void destroy() {
        if(isServer() && (connectionDefs_ != null) && 
                             (connectionDefs_.length != 0)) {
            destroyAllConnectorResources();
            //deactivateEndpoints as well!
            Iterator<MessageEndpointFactoryInfo> iter = getAllEndpointFactories().iterator();
            while (iter.hasNext()) {
                MessageEndpointFactoryInfo element = iter.next(); 
                try {
                    this.resourceadapter_.endpointDeactivation(
                        element.getEndpointFactory(),element.getActivationSpec());
                } catch (RuntimeException e) {
                    _logger.warning(e.getMessage());
                    _logger.log(Level.FINE, "Error during endpointDeactivation ", e);
                }
            }
        }
    	try {
            _logger.fine("Calling Resource Adapter stop" + 
                            this.getModuleName());
            resourceadapter_.stop();
            _logger.fine("Resource Adapter stop call of " + 
                            this.getModuleName() + "returned successfully");
            _logger.log(Level.FINE, "rar_stop_call_successful");
    	} catch (Throwable t) {
               _logger.log(Level.SEVERE,"rardeployment.stop_warning",t);
    	}
    }

    /**
     * Creates an instance of <code>ManagedConnectionFactory</code>
     * object using the connection pool properties. Also set the 
     * <code>ResourceAdapterAssociation</code>
     *
     * @param pool <code>ConnectorConnectionPool</code> properties.
     * @param jcl <code>ClassLoader</code>
     */
    public ManagedConnectionFactory createManagedConnectionFactory (
                    ConnectorConnectionPool pool, ClassLoader jcl) {
        ManagedConnectionFactory mcf = null;
        mcf = super.createManagedConnectionFactory(pool,jcl);

        if (mcf instanceof ResourceAdapterAssociation) {
           try{
               ((ResourceAdapterAssociation) mcf).setResourceAdapter(
                           this.resourceadapter_);
           }catch(ResourceException ex) {
               _logger.log(Level.SEVERE,"rardeployment.assoc_failed",ex);
           }
        }

        return mcf;
    }

    private void writePoolResourceToServerXML() {
    }

    /**
     * Returns information about endpoint factory. 
     *
     * @param id Id of the endpoint factory.
     * @return <code>MessageEndpointFactoryIndo</code> object.
     */
    public MessageEndpointFactoryInfo getEndpointFactoryInfo(String id) {
        return factories_.get(id);
    }

    /*
     * @return A set of Map.Entry that has the bean ID as the key
     *         and the MessageEndpointFactoryInfo as value
     *         A shallow copy only to avoid concurrency issues.
     */
    public Set getAllEndpointFactoryInfo() {
        Hashtable infos = (Hashtable<String, MessageEndpointFactoryInfo>) factories_.clone();
        return infos.entrySet();
    }

    /**
     * Adds endpoint factory information.
     *
     * @param id Unique identifier of the endpoint factory.
     * @param info <code>MessageEndpointFactoryInfo</code> object.
     */
    public void addEndpointFactoryInfo(
        String id, MessageEndpointFactoryInfo info) {
        factories_.put(id, info);
    }

    /**
     * Removes information about an endpoint factory 
     * 
     * @param id Unique identifier of the endpoint factory to be 
     *           removed.
     */
    public void removeEndpointFactoryInfo(String id) {
        factories_.remove(id);
    }

    /**
     * Retrieves the information about all endpoint factories.
     *
     * @return a <code>Collection</code> of <code>MessageEndpointFactory</code>
     *         objects.
     */
    public Collection<MessageEndpointFactoryInfo> getAllEndpointFactories() {
        return factories_.values();
    }

    /**
     * Creates an admin object.
     * 
     * @param appName Name of application, in case of embedded rar.
     * @param connectorName Module name of the resource adapter.
     * @param jndiName JNDI name to be registered.
     * @param adminObjectType Interface name of the admin object.
     * @param props <code>Properties</code> object containing name/value
     *              pairs of properties.
     */
    public void addAdminObject (
            String appName,
            String connectorName,
            String jndiName,
            String adminObjectType,
            Properties props)
        throws ConnectorRuntimeException {
        if (props == null) {
            // empty properties
            props = new Properties();
        }

        ConnectorRegistry registry = null;
        try{
            registry = ConnectorRegistry.getInstance();
        }catch(Exception e) {
        }
        ConnectorDescriptor desc = registry.getDescriptor(connectorName);
        AdminObject aoDesc =
            desc.getAdminObjectByType(adminObjectType);

        AdministeredObjectResource aor = new AdministeredObjectResource(
                                                 jndiName);
        aor.initialize(aoDesc);
        aor.setResourceAdapter(connectorName);

        Object[] envProps = aoDesc.getConfigProperties().toArray();
        
        //Add default config properties to aor
        //Override them if same config properties are provided by the user 
        for (int i = 0; i < envProps.length; i++) {
            EnvironmentProperty envProp = (EnvironmentProperty) envProps[i];
            String name = envProp.getName();
            String userValue = (String)props.remove(name);
            if (userValue != null)    
                aor.addConfigProperty(new EnvironmentProperty(
                              name, userValue, userValue, envProp.getType()));
            else
                aor.addConfigProperty(envProp);
        }
        
        //Add non-default config properties provided by the user to aor
        Iterator iter = props.keySet().iterator();
        while(iter.hasNext()){
            String name = (String) iter.next();
            String userValue = props.getProperty(name);
            if(userValue != null)
                aor.addConfigProperty(new EnvironmentProperty(
                        name, userValue, userValue));
            
        }

        // bind to JNDI namespace
	try{

            Reference ref = aor.createAdminObjectReference();
            NamingManager nm = Switch.getSwitch().getNamingManager();
            nm.publishObject(jndiName, ref, true);

        } catch (NamingException ex) {
	    String i18nMsg = localStrings.getString(
	        "aira.cannot_bind_admin_obj");
            throw new ConnectorRuntimeException( i18nMsg );
        }
    }

    /**
     * Loads RA javabean. This method is protected, so that any system
     * resource adapter can have specific configuration done during the
     * loading.
     *
     * @throws ConnectorRuntimeException if there is a failure.
     */
    protected void loadRAConfiguration() throws ConnectorRuntimeException {
	try {
            ElementProperty[] raConfigProps = null;
            Set mergedProps = null;
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            ResourceAdapterConfig raConfig = 
                    registry.getResourceAdapterConfig(moduleName_);
            if(raConfig != null) {
                raConfigProps = raConfig.getElementProperty();
            }
            if(raConfigProps != null) {
                mergedProps = ConnectorDDTransformUtils.mergeProps(
                        raConfigProps,getDescriptor().getConfigProperties());
            } else {
                mergedProps = ConnectorDDTransformUtils.mergeProps(
                                  new ElementProperty[]{},
                                  getDescriptor().getConfigProperties());
            }

            //HACK !
            if (this.moduleName_.equals(ConnectorRuntime.DEFAULT_JMS_ADAPTER)) {
                    if (ConnectorRuntime.getRuntime().isServer()) {
                        hackMergedProps(mergedProps);
                    }
            }
            logMergedProperties(mergedProps);
            SetMethodAction setMethodAction = new SetMethodAction
                                (this.resourceadapter_, mergedProps);
            setMethodAction.run();
        } catch(Exception e) {
            String i18nMsg = localStrings.getString(
                "ccp_adm.wrong_params_for_create", e.getMessage() );
            ConnectorRuntimeException cre =
                new ConnectorRuntimeException( i18nMsg );
            cre.initCause( e );
            throw cre;
        }
    }

    /**
     * This is a HACK to remove the connection URL
     * in the case of PE LOCAL/EMBEDDED before setting the properties
     * to the RA. If this was not done, MQ RA incorrectly assumed
     * that the passed in connection URL is one additional
     * URL, apart from the default URL derived from brokerhost:brokerport
     * and reported a PE connection url limitation.
     *
     */
   private void hackMergedProps(Set mergedProps) {

        String brokerType = null;

        for (Iterator iter = mergedProps.iterator(); iter.hasNext();) {
            EnvironmentProperty element = (EnvironmentProperty) iter.next();
            if (element.getName().equals(ActiveJmsResourceAdapter.BROKERTYPE)) {
                     brokerType = element.getValue();
            }
        }
	boolean cluster = false;
	try {
		cluster = JmsRaUtil.isClustered();
	} catch (Exception e) {
		e.printStackTrace();
	}
	// hack is required only for nonclustered nonremote brokers.
	if (!cluster) {
        if (brokerType.equals(ActiveJmsResourceAdapter.LOCAL)
                || brokerType.equals(ActiveJmsResourceAdapter.EMBEDDED)
		|| brokerType.equals(ActiveJmsResourceAdapter.DIRECT)) 
        {              
		for (Iterator iter = mergedProps.iterator(); iter.hasNext();) {
                EnvironmentProperty element = (EnvironmentProperty) iter.next();
                if (element.getName().equals(ActiveJmsResourceAdapter.CONNECTION_URL)) {
                    iter.remove();
                }
          }
    	}
	}
    }


    private void logMergedProperties(Set mergedProps) {
			_logger.fine("Passing in the following properties " +
					"before calling RA.start of " + this.moduleName_);
            StringBuffer b = new StringBuffer();
            
			for (Iterator iter = mergedProps.iterator(); iter.hasNext();) {
				EnvironmentProperty element = (EnvironmentProperty) iter.next();
                b.append( "\nName: " + element.getName() 
						+ " Value: " + element.getValue() );
			}
            _logger.fine(b.toString());
	}

	public BootstrapContext getBootStrapContext(){
        return this.bootStrapContextImpl;
    }

}
