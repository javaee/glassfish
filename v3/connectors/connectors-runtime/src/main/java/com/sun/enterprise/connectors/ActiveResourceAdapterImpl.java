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

import com.sun.appserv.connectors.spi.*;
import com.sun.appserv.connectors.internal.api.*;
import com.sun.enterprise.connectors.util.*;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import javax.resource.spi.ManagedConnectionFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Iterator;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;


/**
 * This class represents the abstraction of a 1.0 complient rar.
 * It holds the ra.xml (connector decriptor) values, class loader used to
 * to load the Resource adapter class and managed connection factory and
 * module name (rar) to which it belongs.
 * It is also the base class for ActiveOutboundResourceAdapter(a 1.5 compliant
 * outbound rar).
 *
 * @author  Srikanth P, Binod PG
 */

@Service
@Scoped(PerLookup.class)
public class ActiveResourceAdapterImpl implements ActiveResourceAdapter {

    protected ConnectorDescriptor desc_;
    protected String moduleName_;
    protected ClassLoader jcl_;
    protected ConnectionDefDescriptor[] connectionDefs_;
    protected ConnectorRuntime connectorRuntime_ = null;

    private static Logger _logger = LogDomains.getLogger(ActiveResourceAdapterImpl.class, LogDomains.RSR_LOGGER);
    private StringManager localStrings =
            StringManager.getManager(ActiveResourceAdapterImpl.class);

    /**
     * Constructor.
     *
     * @param desc       Connector Descriptor. Holds the all ra.xml values
     * @param moduleName Name of the module i.e rar Name. Incase of
     *                   embedded resource adapters its name will be appName#rarName
     * @param jcl        Classloader used to load the ResourceAdapter and managed
     *                   connection factory class.
     *                   values to domain.xml.
     */
    public void init(javax.resource.spi.ResourceAdapter ra, ConnectorDescriptor desc,
                                         String moduleName, ClassLoader jcl) throws ConnectorRuntimeException {
        this.desc_ = desc;
        moduleName_ = moduleName;
        jcl_ = jcl;
        connectorRuntime_ = ConnectorRuntime.getRuntime();
        connectionDefs_ = ConnectorDDTransformUtils.getConnectionDefs(desc_);
        validateWorkContextSupport(desc);
    }

    public ActiveResourceAdapterImpl(){
    }

    /**
     * check whether the <i>required-work-context</i> list mandated by the resource-adapter
     * is supported by the application server
     * @param desc ConnectorDescriptor
     * @throws ConnectorRuntimeException when unable to support any of the requested work-context type.
     */
    private void validateWorkContextSupport(ConnectorDescriptor desc) throws ConnectorRuntimeException {
        Set workContexts = desc.getRequiredWorkContexts();
        Iterator workContextsIterator = workContexts.iterator();

        WorkContextHandler workContextHandler = connectorRuntime_ .getWorkContextHandler();
        while(workContextsIterator.hasNext()){
            String ic = (String)workContextsIterator.next();
            boolean supported = workContextHandler.isContextSupported(true, ic );
            if(!supported){
                String errorMsg = "Unsupported work context [ "+ ic + " ] ";
                Object params[] = new Object[]{ic, desc.getName()};
                _logger.log(Level.WARNING,"unsupported.work.context", params);
                throw new ConnectorRuntimeException(errorMsg);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getModuleName() {
        return moduleName_;
    }

    /**
     * It initializes the resource adapter. It also creates the default pools
     * and resources of all the connection definitions.
     *
     * @throws ConnectorRuntimeException This exception is thrown if the
     *                                   ra.xml is invalid or the default pools and resources couldn't
     *                                   be created
     */
    public void setup() throws ConnectorRuntimeException {
        //TODO V3 COMMENT AOBUT RAR 1.0
        if (connectionDefs_ == null || connectionDefs_.length != 1) {
            _logger.log(Level.SEVERE, "rardeployment.invalid_connector_desc", moduleName_);
            String i18nMsg = localStrings.getString("ccp_adm.invalid_connector_desc", moduleName_);
            throw new ConnectorRuntimeException(i18nMsg);
        }
        if (isServer() && !isSystemRar(moduleName_)) {
            createAllConnectorResources();
        }
        _logger.log(Level.FINE, "Completed Active Resource adapter setup", moduleName_);
    }

    /**
     * Check if the execution environment is appserver runtime or application
     * client container.
     *
     * @return boolean if the environment is appserver runtime
     */
    protected boolean isServer() {
        if (connectorRuntime_.getEnvironment() == ConnectorConstants.SERVER) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates both the default connector connection pools and resources
     *
     * @throws ConnectorRuntimeException when unable to create resources
     */
    protected void createAllConnectorResources() throws ConnectorRuntimeException {
        try {

            if (desc_.getSunDescriptor() != null && desc_.getSunDescriptor().getResourceAdapter() != null) {

                // sun-ra.xml exists
                String jndiName = (String) desc_.getSunDescriptor().
                        getResourceAdapter().getValue(ResourceAdapter.JNDI_NAME);

                if (jndiName == null || jndiName.equals("")) {
                    // jndiName is empty, do not create duplicate pools, use setting in sun-ra.xml
                    createDefaultConnectorConnectionPools(true);
                } else {
                    // jndiName is not empty, so create duplicate pools, both default and sun-ra.xml
                    createSunRAConnectionPool();
                    createDefaultConnectorConnectionPools(false);
                }
            } else {
                // sun-ra.xml doesn't exist, so create default pools
                createDefaultConnectorConnectionPools(false);
            }

            // always create default connector resources
            createDefaultConnectorResources();
        } catch (ConnectorRuntimeException cre) {
            //Connector deployment should _not_ fail if default connector
            //connector pool and resource creation fails.
            _logger.log(Level.SEVERE, "rardeployment.defaultpoolresourcecreation.failed", cre);
            _logger.log(Level.FINE, "Error while trying to create the default connector" +
                    "connection pool and resource", cre);
        } catch (Exception e) {
            //Connector deployment should _not_ fail if default connector
            //connector pool and resource creation fails.
            _logger.log(Level.SEVERE, "rardeployment.defaultpoolresourcecreation.failed", e);
            _logger.log(Level.FINE, "Error while trying to create the default connector" +
                    "connection pool and resource", e);
        }
    }

    /**
     * Deletes both the default connector connection pools and resources
     */
    protected void destroyAllConnectorResources() {
        if (!(isSystemRar(moduleName_))) {
            deleteDefaultConnectorResources();
            deleteDefaultConnectorConnectionPools();

            // Added to ensure clean-up of the Sun RA connection pool
            if (desc_.getSunDescriptor() != null &&
                    desc_.getSunDescriptor().getResourceAdapter() != null) {

                // sun-ra.xml exists
                String jndiName = (String) desc_.getSunDescriptor().
                        getResourceAdapter().getValue(ResourceAdapter.JNDI_NAME);

                if (jndiName == null || jndiName.equals("")) {
                    // jndiName is empty, sunRA pool not created, so don't need to delete

                } else {
                    // jndiName is not empty, need to delete pool
                    deleteSunRAConnectionPool();
                }
            }
        }
    }

    protected boolean isSystemRar(String moduleName) {
        return ConnectorsUtil.belongsToSystemRA(moduleName);
    }

    /**
     * Deletes the default connector connection pools.
     */
    protected void deleteDefaultConnectorConnectionPools() {
        for (ConnectionDefDescriptor aConnectionDefs_ : connectionDefs_) {
            String connectionDefName = aConnectionDefs_.getConnectionFactoryIntf();
            String resourceJndiName = connectorRuntime_.getDefaultPoolName(moduleName_, connectionDefName);
            try {
                connectorRuntime_.deleteConnectorConnectionPool(resourceJndiName);
            } catch (ConnectorRuntimeException cre) {
                _logger.log(Level.WARNING, "rar.undeployment.default_pool_delete_fail", resourceJndiName);
            }
        }
    }

    /**
     * Deletes the default connector resources.
     */
    protected void deleteDefaultConnectorResources() {
        for (ConnectionDefDescriptor aConnectionDefs_ : connectionDefs_) {
            String connectionDefName = aConnectionDefs_.getConnectionFactoryIntf();
            String resourceJndiName = connectorRuntime_.getDefaultResourceName(moduleName_, connectionDefName);
            try {
                connectorRuntime_.deleteConnectorResource(resourceJndiName);
            } catch (ConnectorRuntimeException cre) {
                _logger.log(Level.WARNING, "rar.undeployment.default_resource_delete_fail", resourceJndiName);
                _logger.log(Level.FINE, "Error while trying to delete the default connector resource", cre);
            }
        }
    }

    /**
     * uninitializes the resource adapter. It also destroys the default pools
     * and resources
     */
    public void destroy() {
        if (isServer()) {
            destroyAllConnectorResources();
        }
    }

    /**
     * Returns the Connector descriptor which represents/holds ra.xml
     *
     * @return ConnectorDescriptor Representation of ra.xml.
     */
    public ConnectorDescriptor getDescriptor() {
        return desc_;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(ConnectorDescriptor cd, String moduleName) {

        boolean adminObjectsDefined = false;
        Set adminObjects = cd.getAdminObjects();
        if (adminObjects != null && adminObjects.size() > 0) {
            adminObjectsDefined = true;
        }

        return  (!cd.getInBoundDefined()) &&
                (cd.getOutBoundDefined() && cd.getOutboundResourceAdapter().getConnectionDefs().size() < 2 ) &&
                !adminObjectsDefined &&
                ("".equals(cd.getResourceAdapterClass())
                );

    }

    /**
     * {@inheritDoc}
     */
    public ManagedConnectionFactory[] createManagedConnectionFactories(
            ConnectorConnectionPool ccp, ClassLoader jcl) {
        throw new UnsupportedOperationException("This operation is not supported");
    }


    /**
     * Creates managed Connection factory instance.
     *
     * @param ccp Connector connection pool which contains the pool properties
     *            and ra.xml values pertaining to managed connection factory
     *            class. These values are used in MCF creation.
     * @param jcl Classloader used to managed connection factory class.
     * @return ManagedConnectionFactory created managed connection factory
     *         instance
     */
    public ManagedConnectionFactory createManagedConnectionFactory(
            ConnectorConnectionPool ccp, ClassLoader jcl) {
        final String mcfClass = ccp.getConnectorDescriptorInfo().getManagedConnectionFactoryClass();
        try {

            ManagedConnectionFactory mcf = null;
            mcf = instantiateMCF(mcfClass, jcl);

            if (mcf instanceof ConfigurableTransactionSupport) {
                TransactionSupport ts = ConnectionPoolObjectsUtils.getTransactionSupport(
                                ccp.getTransactionSupport());
                ((ConfigurableTransactionSupport)mcf).setTransactionSupport(ts);
            }

            SetMethodAction setMethodAction = new SetMethodAction
                    (mcf, ccp.getConnectorDescriptorInfo().getMCFConfigProperties());
            setMethodAction.run();
            _logger.log(Level.FINE, "Created MCF object : ", mcfClass);
            return mcf;
        } catch (ClassNotFoundException Ex) {
            _logger.log(Level.SEVERE, "rardeployment.class_not_found", new Object[]{mcfClass, Ex.getMessage()});
            _logger.log(Level.FINE, "rardeployment.class_not_found", Ex);
            return null;
        } catch (InstantiationException Ex) {
            _logger.log(Level.SEVERE, "rardeployment.class_instantiation_error", new Object[]{mcfClass, Ex.getMessage()});
            _logger.log(Level.FINE, "rardeployment.class_instantiation_error", Ex);
            return null;
        } catch (IllegalAccessException Ex) {
            _logger.log(Level.SEVERE, "rardeployment.illegalaccess_error", new Object[]{mcfClass, Ex.getMessage()});
            _logger.log(Level.FINE, "rardeployment.illegalaccess_error", Ex);
            return null;
        } catch (Exception Ex) {
            _logger.log(Level.SEVERE, "rardeployment.mcfcreation_error", new Object[]{mcfClass, Ex.getMessage()});
            _logger.log(Level.FINE, "rardeployment.mcfcreation_error", Ex);
            return null;
        }
    }

    /**
     * sets the logWriter for the MCF being instantiated.<br>
     * Resource Adapter implementer can make use of this logWriter<br>
     *
     * @param mcf ManagedConnectionFactory
     */
    private void setLogWriter(ManagedConnectionFactory mcf) {
        PrintWriterAdapter adapter = new PrintWriterAdapter(ConnectorRuntime.getRuntime().getResourceAdapterLogWriter());
        try {
            mcf.setLogWriter(adapter);
        } catch (Exception e) {
            Object[] params = new Object[]{mcf.getClass().getName(), e.toString()};
            _logger.log(Level.WARNING, "rardeployment.logwriter_error", params);
            _logger.log(Level.FINE, "Unable to set LogWriter for ManagedConnectionFactory : " + mcf.getClass().getName(), e);
        }
    }

    protected ManagedConnectionFactory instantiateMCF(String mcfClass, ClassLoader loader)
            throws Exception {
        ManagedConnectionFactory mcf = null;

        if (jcl_ != null) {
            mcf = (ManagedConnectionFactory) jcl_.loadClass(mcfClass).newInstance();
        } else if (loader != null) {
            mcf = (ManagedConnectionFactory) loader.loadClass(mcfClass).newInstance();
        } else {
            //mcf = (ManagedConnectionFactory) Class.forName(mcfClass).newInstance();
            mcf = (ManagedConnectionFactory)Thread.currentThread().getContextClassLoader().loadClass(mcfClass).newInstance();
        }
        setLogWriter(mcf);
        return mcf;
    }


    /**
     * Creates default connector resource
     *
     * @throws ConnectorRuntimeException when unable to create connector resources
     */
    protected void createDefaultConnectorResources()
            throws ConnectorRuntimeException {
        for (ConnectionDefDescriptor descriptor : connectionDefs_) {

            String connectionDefName = descriptor.getConnectionFactoryIntf();
            String resourceName = connectorRuntime_.getDefaultResourceName(moduleName_, connectionDefName);
            String poolName = connectorRuntime_.getDefaultPoolName(moduleName_, connectionDefName);

            connectorRuntime_.createConnectorResource(resourceName, poolName, null);
            desc_.addDefaultResourceName(resourceName);

            _logger.log(Level.FINE, "Created default connector resource [ " + resourceName + " ] " );
        }
    }

    /**
     * Creates default connector connection pool
     *
     * @param useSunRA whether to use default pool settings or settings in sun-ra.xml
     * @throws ConnectorRuntimeException when unable to create connector connection pools
     */
    protected void createDefaultConnectorConnectionPools(boolean useSunRA)
            throws ConnectorRuntimeException {

        for (ConnectionDefDescriptor descriptor : connectionDefs_) {
            String poolName = connectorRuntime_.getDefaultPoolName(moduleName_, descriptor.getConnectionFactoryIntf());

            ConnectorDescriptorInfo connectorDescriptorInfo =
                    ConnectorDDTransformUtils.getConnectorDescriptorInfo(descriptor);
            connectorDescriptorInfo.setRarName(moduleName_);
            connectorDescriptorInfo.setResourceAdapterClassName(desc_.getResourceAdapterClass());
            ConnectorConnectionPool connectorPoolObj;

            // if useSunRA is true, then create connectorPoolObject using settings
            // from sunRAXML
            if (useSunRA) {
                connectorPoolObj =
                        ConnectionPoolObjectsUtils.createSunRaConnectorPoolObject(poolName, desc_, moduleName_);
            } else {
                connectorPoolObj =
                        ConnectionPoolObjectsUtils.createDefaultConnectorPoolObject(poolName, moduleName_);
            }

            connectorPoolObj.setConnectorDescriptorInfo(connectorDescriptorInfo);
            connectorRuntime_.createConnectorConnectionPool(connectorPoolObj);
            _logger.log(Level.FINE, "Created default connection pool [ "+ poolName + " ] ");
        }
    }

    /**
     * Creates connector connection pool pertaining to sun-ra.xml. This is
     * only for 1.0 complient rars.
     *
     * @throws ConnectorRuntimeException Thrown when pool creation fails.
     */
    private void createSunRAConnectionPool() throws ConnectorRuntimeException {

        String defaultPoolName = connectorRuntime_.getDefaultPoolName(
                moduleName_, connectionDefs_[0].getConnectionFactoryIntf());

        String sunRAPoolName = defaultPoolName + ConnectorConstants.SUN_RA_POOL;

        ConnectorDescriptorInfo connectorDescriptorInfo =
                ConnectorDDTransformUtils.getConnectorDescriptorInfo(connectionDefs_[0]);
        connectorDescriptorInfo.setRarName(moduleName_);
        connectorDescriptorInfo.setResourceAdapterClassName(desc_.getResourceAdapterClass());
        ConnectorConnectionPool connectorPoolObj =
                ConnectionPoolObjectsUtils.createSunRaConnectorPoolObject(sunRAPoolName, desc_, moduleName_);

        connectorPoolObj.setConnectorDescriptorInfo(connectorDescriptorInfo);
        connectorRuntime_.createConnectorConnectionPool(connectorPoolObj);
        _logger.log(Level.FINE, "Created SUN-RA connection pool:", sunRAPoolName);

        String jndiName = (String) desc_.getSunDescriptor().
                getResourceAdapter().getValue(ResourceAdapter.JNDI_NAME);
        connectorRuntime_.createConnectorResource(jndiName, sunRAPoolName, null);
        _logger.log(Level.FINE, "Created SUN-RA connector resource : ", jndiName);

    }

    /**
     * Added to clean up the connector connection pool pertaining to sun-ra.xml. This is
     * only for 1.0 complient rars.
     */
    private void deleteSunRAConnectionPool() {

        String defaultPoolName = connectorRuntime_.getDefaultPoolName(
                moduleName_, connectionDefs_[0].getConnectionFactoryIntf());

        String sunRAPoolName = defaultPoolName + ConnectorConstants.SUN_RA_POOL;
        try {
            connectorRuntime_.deleteConnectorConnectionPool(sunRAPoolName);
        } catch (ConnectorRuntimeException cre) {
            _logger.log(Level.WARNING, "rar.undeployment.sun_ra_pool_delete_fail", sunRAPoolName);
        }
    }

    /**
     * Returns the class loader that is used to load the RAR.
     *
     * @return <code>ClassLoader</code> object.
     */
    public ClassLoader getClassLoader() {
        return jcl_;
    }

    /**
     * Retrieves the resource adapter java bean.
     *
     * @return <code>ResourceAdapter</code>
     */
    public javax.resource.spi.ResourceAdapter getResourceAdapter() {
        throw new UnsupportedOperationException("1.0 RA will not have ResourceAdapter bean");
    }

}
