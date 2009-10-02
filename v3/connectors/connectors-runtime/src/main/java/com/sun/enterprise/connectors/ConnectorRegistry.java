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

import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.config.serverbeans.SecurityMap;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.connectors.authentication.RuntimeSecurityMap;
import com.sun.enterprise.connectors.module.ConnectorApplication;
import com.sun.logging.LogDomains;

import javax.resource.spi.ManagedConnectionFactory;
import javax.validation.Validator;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This is an registry class which holds various objects in hashMaps,
 * hash tables, and vectors. These objects are updated and queried
 * during various funtionalities of rar deployment/undeployment, resource
 * creation/destruction
 * Ex. of these objects are ResourcesAdapter instances, security maps for
 * pool managed connection factories.
 * It follows singleton pattern. i.e only one instance at any point of time.
 *
 * @author Binod P.G and Srikanth P
 */

public class ConnectorRegistry {

    static final Logger _logger = LogDomains.getLogger(ConnectorRegistry.class, LogDomains.RSR_LOGGER);

    protected static final ConnectorRegistry connectorRegistryInstance = new ConnectorRegistry();

    /**
     * <code>resourceAdapters</code> keeps track of all active resource
     * adapters in the connector runtime.
     * String:resourceadapterName Vs ActiveResourceAdapter
     */
    protected final Map<String, ActiveResourceAdapter> resourceAdapters;

    protected final Map<String, PoolMetaData> factories;
    protected final Map<String, ResourceAdapterConfig> resourceAdapterConfig;
    protected final Map<String, ConnectorApplication> rarModules;
    protected final Map<String, Validator> beanValidators;

    /**
     * Return the ConnectorRegistry instance
     *
     * @return ConnectorRegistry instance which is a singleton
     */
    public static ConnectorRegistry getInstance() {
        _logger.fine("returning the connector registry");
        return connectorRegistryInstance;
    }

    /**
     * Protected constructor.
     * It is protected as it follows singleton pattern.
     */

    protected ConnectorRegistry() {
        resourceAdapters = Collections.synchronizedMap(new HashMap<String, ActiveResourceAdapter>());
        factories = Collections.synchronizedMap(new HashMap<String, PoolMetaData>());
        resourceAdapterConfig = Collections.synchronizedMap(new HashMap<String, ResourceAdapterConfig>());
        rarModules = Collections.synchronizedMap(new HashMap<String, ConnectorApplication>());
        beanValidators = Collections.synchronizedMap(new HashMap<String, Validator>());
        _logger.log(Level.FINE, "initialized the connector registry");
    }

    /**
     * Adds the object implementing ActiveResourceAdapter
     * interface to the registry.
     *
     * @param rarModuleName RarName which is the key
     * @param rar           ActiveResourceAdapter instance which is the value.
     */

    public void addActiveResourceAdapter(String rarModuleName,
                                         ActiveResourceAdapter rar) {
        resourceAdapters.put(rarModuleName, rar);
        _logger.log(Level.FINE,
                "Added the active resource adapter to connector registry",
                rarModuleName);
    }

    /**
     * Removes the object implementing ActiveResourceAdapter
     * interface from the registry.
     * This method is called whenever an active connector module
     * is removed from the Connector runtime. [eg. undeploy/recreate etc]
     *
     * @param rarModuleName RarName which is the key
     * @return true if successfully removed
     *         false if deletion fails.
     */

    public boolean removeActiveResourceAdapter(String rarModuleName) {
        Object o = resourceAdapters.remove(rarModuleName);

        if (o == null) {
            _logger.fine("Failed to remove the resource adapter from connector registry" +
                    rarModuleName);
            return false;
        } else {
            _logger.fine("removed the active resource adapter from connector registry" +
                    rarModuleName);
            return true;
        }
    }

    /**
     * Retrieves the object implementing ActiveResourceAdapter interface
     * from the registry. Key is the rarName.
     *
     * @param rarModuleName Rar name. It is the key
     * @return object implementing ActiveResourceAdapter interface
     */

    public ActiveResourceAdapter getActiveResourceAdapter(
            String rarModuleName) {
        if (rarModuleName != null) {
            _logger.fine(
                    "returning/found the resource adapter from connector registry" +
                            rarModuleName);
            return resourceAdapters.get(rarModuleName);
        } else {
            _logger.fine(
                    "Resourceadapter not found in connector registry.Returning null" +
                            rarModuleName);
            return null;
        }
    }


    /**
     * Adds the bean validator to the registry.
     *
     * @param rarModuleName RarName which is the key
     * @param validator to be added to registry
     */
    public void addBeanValidator(String rarModuleName, Validator validator){
        beanValidators.put(rarModuleName, validator);
        _logger.log(Level.FINE, "Added the bean validator for RAR [ "+rarModuleName+" ] to connector registry");
    }

    /**
     * Retrieves the bean validator of a resource-adapter
     * from the registry. Key is the rarName.
     *
     * @param rarModuleName Rar name. It is the key
     * @return bean validator
     */
    public Validator getBeanValidator(String rarModuleName){
        if (rarModuleName != null) {
            _logger.fine(
                    "returning/found the validator for RAR [ "+rarModuleName+" ] from connector registry");
            return beanValidators.get(rarModuleName);
        } else {
            _logger.fine(
                    "bean validator for RAR [ "+rarModuleName+" ] not found in connector registry.Returning null");
            return null;
        }
    }

    /**
     * Removes the bean validator of a resource-adapter
     * from the registry.
     * This method is called whenever an active connector module
     * is removed from the Connector runtime. [eg. undeploy/recreate etc]
     *
     * @param rarModuleName RarName which is the key
     * @return true if successfully removed
     *         false if deletion fails.
     */
    public boolean removeBeanValidator(String rarModuleName) {
        Object o = beanValidators.remove(rarModuleName);

        if (o == null) {
            _logger.fine("Failed to remove the bean validator for RAR [ "+rarModuleName+" ] from connector registry");
            return false;
        } else {
            _logger.fine("removed the active bean validator for RAR [ "+rarModuleName +" ] from connector registry");
            return true;
        }
    }

    /**
     * Checks if the MCF pertaining to the pool is instantiated and present
     * in the registry. Each pool has its own MCF instance.
     *
     * @param poolName Name of the pool
     * @return true if the MCF is found.
     *         false if MCF is not found
     */


    public boolean isMCFCreated(String poolName) {
        boolean created = factories.containsKey(poolName);
        _logger.fine("isMCFCreated " + poolName + " - " + created);
        return created;
    }


    /**
     * Remove MCF instance pertaining to the poolName from the registry.
     *
     * @param poolName Name of the pool
     * @return true if successfully removed.
     *         false if removal fails.
     */

    public boolean removeManagedConnectionFactory(String poolName) {
        if (factories.remove(poolName) == null) {
            _logger.log(Level.FINE,
                    "Failed to remove the MCF from connector registry.", poolName);
            return false;
        } else {
            _logger.fine("removeMCF " + poolName + " - " + !factories.containsKey(poolName));
            return true;
        }
    }

    /**
     * Add MCF instance pertaining to the poolName to the registry.
     *
     * @param poolName Name of the pool
     * @param pmd      MCF instance to be added.
     */
    public void addManagedConnectionFactory(String poolName,
                                            PoolMetaData pmd) {
        factories.put(poolName, pmd);
        _logger.fine("Added MCF to connector registry for: " + poolName);
    }

    /**
     * Retrieve MCF instance pertaining to the poolName from the registry.
     *
     * @param poolName Name of the pool
     * @return factory MCF instance retrieved.
     */


    public ManagedConnectionFactory getManagedConnectionFactory(
            String poolName) {
        if (poolName != null) {
            _logger.log(Level.FINE,
                    "Returning the MCF from connector registry.", poolName);

            PoolMetaData pmd = factories.get(poolName);
            if (pmd != null) {
                return pmd.getMCF();
            }

        }
        return null;
    }

    /**
     * Checks whether the rar is already deployed i.e registered with
     * connector registry
     *
     * @param rarModuleName rar Name.
     * @return true if rar is registered
     *         false if rar is not registered.
     */

    public boolean isRegistered(String rarModuleName) {
        _logger.log(Level.FINE,
                "Checking for MCF presence in connector registry.", rarModuleName);
        return resourceAdapters.containsKey(rarModuleName);
    }

    /**
     * Gets the connector descriptor pertaining the rar
     *
     * @param rarModuleName rarName
     * @return ConnectorDescriptor which represents the ra.xml
     */

    public ConnectorDescriptor getDescriptor(String rarModuleName) {
        ActiveResourceAdapter ar = null;
        if (rarModuleName != null) {
            ar = resourceAdapters.get(rarModuleName);
        }
        if (ar != null) {
            _logger.log(Level.FINE,
                    "Found/returing Connector descriptor in connector registry.",
                    rarModuleName);
            return ar.getDescriptor();
        } else {
            _logger.log(Level.FINE,
                    "Couldnot find Connector descriptor in connector registry.",
                    rarModuleName);
            return null;
        }
    }

    /** Gets the runtime equivalent of policies enforced by the Security Maps 
     *  pertaining to a pool from the Pool's Meta Data.   
     *  @param poolName Name of the pool
     *  @return runtimeSecurityMap in the form of HashMap of
     *   HashMaps (user and groups). 
     *  @see SecurityMapUtils.processSecurityMaps(SecurityMap[])
     */


    public RuntimeSecurityMap getRuntimeSecurityMap(String poolName) {
        if(poolName != null) {
            _logger.log(Level.FINE, "Returing the security map from connector registry.", poolName);
            PoolMetaData pmd = factories.get(poolName);
            return pmd.getRuntimeSecurityMap();
        } else {
            return null;
        }
    }

    /**
     * Get the resource adapter config properties object registered with
     * registry against the rarName.
     *
     * @param rarName Name of the rar
     * @return ResourceAdapter configuration object
     */

    public ResourceAdapterConfig getResourceAdapterConfig(String rarName) {
        if (rarName != null) {
            _logger.log(Level.FINE,
                    "Returing the resourceadapter Config from registry.", rarName);
            return resourceAdapterConfig.get(rarName);
        } else {
            return null;
        }
    }

    /**
     * Add the resource adapter config properties object to registry
     * against the rarName.
     *
     * @param rarName  Name of the rar
     * @param raConfig ResourceAdapter configuration object
     */

    public void addResourceAdapterConfig(String rarName,
                                         ResourceAdapterConfig raConfig) {
        if (rarName != null) {
            _logger.log(Level.FINE,
                    "Adding the resourceAdapter Config to connector registry.",
                    rarName);
            resourceAdapterConfig.put(rarName, raConfig);
        }
    }

    /**
     * Remove the resource adapter config properties object from registry
     *
     * @param rarName Name of the rar
     * @return true if successfully deleted
     *         false if deletion fails
     */

    public boolean removeResourceAdapterConfig(String rarName) {
        if (resourceAdapterConfig.remove(rarName) == null) {
            _logger.log(Level.FINE,
                    "failed to remove the resourceAdapter config from registry.",
                    rarName);
            return false;
        } else {
            _logger.log(Level.FINE,
                    "Removed the resourceAdapter config map from registry.",
                    rarName);
            return true;
        }
    }

    /**
     * Returns all Active Resource Adapters in the connector runtime.
     *
     * @return All active resource adapters in the connector runtime
     */
    public ActiveResourceAdapter[] getAllActiveResourceAdapters() {
        return this.resourceAdapters.values().toArray(new ActiveResourceAdapter[]{});
    }

    public PoolMetaData getPoolMetaData(String poolName) {
        return factories.get(poolName);
    }

    /**
     * register a connector application (rarModule) with the registry
     * @param rarModule resource-adapter module
     */
    public void addConnectorApplication(ConnectorApplication rarModule){
        rarModules.put(rarModule.getModuleName(), rarModule);
    }

    /**
     * retrieve a connector application (rarModule) from the registry
     * @param rarName resource-adapter name
     * @return ConnectorApplication app
     */
    public ConnectorApplication getConnectorApplication(String rarName){
        return rarModules.get(rarName);
    }

    /**
     * remove a connector application (rarModule) from the registry
     * @param rarName resource-adapter module
     */
    public void removeConnectorApplication(String rarName){
        rarModules.remove(rarName);
    }

    /**
     * get the list of resource-adapters that support this message-listener-type
     * @param messageListener message-listener class-name
     * @return List of resource-adapters
     */
    public List<String> getConnectorsSupportingMessageListener(String messageListener){

        List<String> rars = new ArrayList<String>();
        for(ActiveResourceAdapter ara : resourceAdapters.values()){
            ConnectorDescriptor desc = ara.getDescriptor();
            if(desc.getInBoundDefined()){
                if(desc.getInboundResourceAdapter().getMessageListener(messageListener) != null){
                    rars.add(ara.getModuleName());
                }
            }
        }
        return rars;
    }
}
