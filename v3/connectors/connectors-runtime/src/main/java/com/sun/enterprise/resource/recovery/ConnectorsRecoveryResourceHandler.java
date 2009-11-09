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
package com.sun.enterprise.resource.recovery;

import java.security.Principal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsClassLoaderUtil;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty ;
import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.resource.deployer.ConnectorResourceDeployer;
import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import org.jvnet.hk2.config.types.Property;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

/**
 * Recovery handler for connector resources
 *
 * @author Jagadish Ramu
 */
@Service
public class ConnectorsRecoveryResourceHandler implements RecoveryResourceHandler {

    @Inject
    private Resources resources;

    @Inject
    private ConnectorsClassLoaderUtil cclUtil;

    @Inject
    private Habitat connectorRuntimeHabitat;

    @Inject
    private Habitat connectorResourceDeployerHabitat;

    @Inject
    private Habitat applicationLoaderServiceHabitat;

    private static Logger _logger = LogDomains.getLogger(ConnectorsRecoveryResourceHandler.class, LogDomains.RSR_LOGGER);

    /**
     * does a lookup of resources so that they are loaded for sure.
     */
    private void loadAllConnectorResources() {

        try {
            Collection<ConnectorResource> connResources = resources.getResources(ConnectorResource.class);
            InitialContext ic = new InitialContext();
            for (ConnectorResource connResource : connResources) {
                if (isEnabled(connResource)) {
                    try {
                        ic.lookup(connResource.getJndiName());
                    //} catch (NameNotFoundException ne) {
                    } catch (NamingException ne) {
                        //If you are here then it is most probably an embedded RAR resource
                        //So we need to explicitly load that rar and create the resources
                        try {
                            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool connConnectionPool =
                                    getConnectorConnectionPoolByName(connResource.getPoolName());
                            //TODO V3 ideally this should not happen if connector modules (and embedded rars) are loaded before recovery
                            createActiveResourceAdapter(connConnectionPool.getResourceAdapterName());
                            getConnectorResourceDeployer().deployResource(connResource);
                        } catch (Exception ex) {
                            _logger.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", connResource.getJndiName());
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, ne.toString(), ne);
                            }
                            _logger.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", connResource.getJndiName());
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, ex.toString(), ex);
                            }
                        }
                    } catch (Exception ex) {
                        _logger.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", connResource.getJndiName());
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log(Level.FINE, ex.toString(), ex);
                        }
                    }
                }
            }
        } catch (NamingException ne) {
            _logger.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", ne.getMessage());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ne.toString(), ne);
            }
        }
    }

    private ConnectorResourceDeployer getConnectorResourceDeployer() {
        return connectorResourceDeployerHabitat.getComponent(ConnectorResourceDeployer.class);
    }

    /**
     * provides a connector connection pool configuration
     * @param poolName pool-name
     * @return ccp
     */
    private ConnectorConnectionPool getConnectorConnectionPoolByName(String poolName) {
        ConnectorConnectionPool result = null;
        Collection<ConnectorConnectionPool> ccPools = resources.getResources(ConnectorConnectionPool.class);
        for (ConnectorConnectionPool ccp : ccPools) {
            if (ccp.getName().equals(poolName)) {
                result = ccp;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void loadXAResourcesAndItsConnections(List xaresList, List connList) {

        Collection<ConnectorResource> connectorResources = resources.getResources(ConnectorResource.class);

        if (connectorResources == null || connectorResources.size() == 0) {
            return;
        }

        //TODO V3 done so as to initialize connectors-runtime before loading jdbc-resources. need a better way ?
        ConnectorRuntime crt = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class);

        //TODO V3 done so as to load all connector-modules. need to load only connector-modules instead of all apps
        applicationLoaderServiceHabitat.getComponent(Startup.class,"ApplicationLoaderService");

        List<ConnectorConnectionPool> connPools = new ArrayList<ConnectorConnectionPool>();
        for (Resource resource : connectorResources) {
            ConnectorResource connResource = (ConnectorResource) resource;
            if (isEnabled(connResource)) {
                ConnectorConnectionPool pool = getConnectorConnectionPoolByName(connResource.getPoolName());
                if (pool != null &&
                        ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING.equals(
                                getTransactionSupport(pool))) {
                    connPools.add(pool);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("ConnectorsRecoveryResourceHandler loadXAResourcesAndItsConnections :: "
                                + "adding : " + connResource.getPoolName());
                    }
                }
            }
        }
        loadAllConnectorResources();

        _logger.log(Level.FINE, "Recovering pools : " + connPools.size());

        for(ConnectorConnectionPool connPool : connPools){
            String poolName = connPool.getName();
            try {
                String[] dbUserPassword = getdbUserPasswordOfConnectorConnectionPool(connPool);
                if (dbUserPassword == null) {
                    continue;
                }
                String dbUser = dbUserPassword[0];
                String dbPassword = dbUserPassword[1];
                Subject subject = new Subject();

                //If username or password of the connector connection pool
                //is null a warning is logged and recovery continues with
                //empty String username or password as the case may be,
                //because some databases allow null[as in empty string]
                //username [pointbase interprets this as "root"]/password.
                if (dbPassword == null) {
                    dbPassword = "";
                    _logger.log(Level.WARNING,
                            "datasource.xadatasource_nullpassword_error", poolName);
                }

                if (dbUser == null) {
                    dbUser = "";
                    _logger.log(Level.WARNING,
                            "datasource.xadatasource_nulluser_error", poolName);
                }
                String rarName = connPool.getResourceAdapterName();
                //TODO V3 JMS-RA ??
                if (ConnectorAdminServiceUtils.isJMSRA(rarName)) {
                    _logger.log(Level.FINE, "Performing recovery for JMS RA, poolName  " + poolName);
                    ManagedConnectionFactory[] mcfs =
                            crt.obtainManagedConnectionFactories(poolName);
                    _logger.log(Level.INFO, "JMS resource recovery has created CFs = " + mcfs.length);
                    for (int i = 0; i < mcfs.length; i++) {
                        PasswordCredential pc = new PasswordCredential(
                                dbUser, dbPassword.toCharArray());
                        pc.setManagedConnectionFactory(mcfs[i]);
                        Principal prin =
                                new ResourcePrincipal(dbUser, dbPassword);
                        subject.getPrincipals().add(prin);
                        subject.getPrivateCredentials().add(pc);
                        ManagedConnection mc = mcfs[i].
                                createManagedConnection(subject, null);
                        connList.add(mc);
                        try {
                            XAResource xares = mc.getXAResource();
                            if (xares != null) {
                                xaresList.add(xares);
                            }
                        } catch (ResourceException ex) {
                            // ignored. Not at XA_TRANSACTION level
                        }
                    }

                } else {
                    ManagedConnectionFactory mcf =
                            crt.obtainManagedConnectionFactory(poolName);
                    PasswordCredential pc = new PasswordCredential(
                            dbUser, dbPassword.toCharArray());
                    pc.setManagedConnectionFactory(mcf);
                    Principal prin = new ResourcePrincipal(dbUser, dbPassword);
                    subject.getPrincipals().add(prin);
                    subject.getPrivateCredentials().add(pc);
                    ManagedConnection mc = mcf.createManagedConnection(subject, null);
                    connList.add(mc);
                    try {
                        XAResource xares = mc.getXAResource();
                        if (xares != null) {
                            xaresList.add(xares);
                        }
                    } catch (ResourceException ex) {
                        // ignored. Not at XA_TRANSACTION level
                    }
                }
            } catch (Exception ex) {
                _logger.log(Level.WARNING, "datasource.xadatasource_error",
                        poolName);
                _logger.log(Level.FINE, "datasource.xadatasource_error_excp", ex);
            }
        }
        _logger.log(Level.FINE, "Total XAResources identified for recovery is " + xaresList.size());
        _logger.log(Level.FINE, "Total connections identified for recovery is " + connList.size());
    }

    /**
     * provides the transaction support for the pool.
     * If none specified in the pool, tx support at RA level will be returned.
     * @param pool connector connection pool
     * @return tx support level
     */
    private String getTransactionSupport(ConnectorConnectionPool pool) {

        String txSupport = pool.getTransactionSupport();

        if (txSupport != null) {
            return txSupport;
        }

        try {
            txSupport = ConnectorRuntime.getRuntime().getConnectorDescriptor(
                    pool.getResourceAdapterName()).getOutboundResourceAdapter().
                    getTransSupport();
        } catch (ConnectorRuntimeException cre) {
            Object params[] = new Object[]{pool.getResourceAdapterName(), cre};
            _logger.log(Level.WARNING, "error.retrieving.tx-support.from.rar", params);
            _logger.finest("setting no-tx-support as tx-support-level for pool : " + pool.getName());
            txSupport = ConnectorConstants.NO_TRANSACTION_TX_SUPPORT_STRING;
        }

        return txSupport;
    }

    /**
     * {@inheritDoc}
     */
    public void closeConnections(List connList) {
        for (Object obj : connList) {
            try {
                ((ManagedConnection) obj).destroy();
            } catch (Exception ex) {
                // Since closing error has been advised to be ignored
                // so we are not logging the message as an exception
                // but treating the same as a debug message
                // Santanu De, Sun Microsystems, 2002.
                _logger.log(Level.WARNING, "Connector Resource could not be closed", ex);
            }
        }
    }

    //TODO V3 can't this be generic or can't this be handled by ConnectorsUtil / ResourcesUtil
    private boolean isEnabled(ConnectorResource resource) {
        return Boolean.valueOf(resource.getEnabled());
    }

    private String[] getdbUserPasswordOfConnectorConnectionPool(
            ConnectorConnectionPool connectorConnectionPool) {

        String[] userPassword = new String[2];
        userPassword[0] = null;
        userPassword[1] = null;
        List<Property> properties = connectorConnectionPool.getProperty();
        if (properties != null) {
            boolean foundUserPassword = false;
            for (Property elementProperty : properties) {
                String prop = elementProperty.getName().toUpperCase();

                if ("USERNAME".equals(prop) || "USER".equals(prop)) {
                    userPassword[0] = elementProperty.getValue();
                    foundUserPassword = true;
                } else if ("PASSWORD".equals(prop)) {
                    userPassword[1] = elementProperty.getValue();
                    foundUserPassword = true;
                }
            }
            if (foundUserPassword == true) {
                return userPassword;
            }
        }

        String poolName = connectorConnectionPool.getName();
        String rarName = connectorConnectionPool.getResourceAdapterName();
        String connectionDefName =
                connectorConnectionPool.getConnectionDefinitionName();
        ConnectorRegistry connectorRegistry =
                ConnectorRegistry.getInstance();
        ConnectorDescriptor connectorDescriptor =
                connectorRegistry.getDescriptor(rarName);
        ConnectionDefDescriptor cdd =
                connectorDescriptor.getConnectionDefinitionByCFType(
                        connectionDefName);
        Set configProps = cdd.getConfigProperties();
        for (Iterator iter = configProps.iterator(); iter.hasNext();) {
            ConnectorConfigProperty  envProp = (ConnectorConfigProperty ) iter.next();
            String prop = envProp.getName().toUpperCase();

            if ("USER".equals(prop) || "USERNAME".equals(prop)) {

                userPassword[0] = envProp.getValue();
            } else if ("PASSWORD".equals(prop)) {
                userPassword[1] = envProp.getValue();
            }
        }

        if (userPassword[0] != null && !"".equals(userPassword[0].trim())) {
            return userPassword;
        }

        //else read the default username and password from the ra.xml
        ManagedConnectionFactory mcf =
                connectorRegistry.getManagedConnectionFactory(poolName);
        userPassword[0] = ConnectionPoolObjectsUtils.getValueFromMCF(
                "UserName", poolName, mcf);
        userPassword[1] = ConnectionPoolObjectsUtils.getValueFromMCF(
                "Password", poolName, mcf);

        return userPassword;
    }

    private void createActiveResourceAdapter(String rarModuleName) throws ConnectorRuntimeException {

        ConnectorRuntime cr = ConnectorRuntime.getRuntime();
        ConnectorRegistry creg = ConnectorRegistry.getInstance();

        if (creg.isRegistered(rarModuleName))
            return;

        if (ConnectorAdminServiceUtils.isEmbeddedConnectorModule(rarModuleName)) {
             cr.createActiveResourceAdapterForEmbeddedRar(rarModuleName);
        } else {
            String moduleDir = ConfigBeansUtilities.getLocation(rarModuleName);
            ClassLoader loader = cr.createConnectorClassLoader(moduleDir, null, rarModuleName);
            cr.createActiveResourceAdapter(moduleDir, rarModuleName, loader);
        }
    }
}
