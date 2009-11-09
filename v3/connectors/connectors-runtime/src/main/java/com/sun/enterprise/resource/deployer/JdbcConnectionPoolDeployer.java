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
 * @(#) JdbcConnectionPoolDeployer.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.config.types.Property;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Jdbc connection pool events in the server instance. When user adds a
 * jdbc connection pool , the admin instance emits resource event. The jdbc
 * connection pool events are propagated to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author Tamil Vengan
 */

// This class was created to fix the bug # 4650787

@Service
@Scoped(Singleton.class)
public class JdbcConnectionPoolDeployer implements ResourceDeployer {

    @Inject
    private ConnectorRuntime runtime;

    static private StringManager sm = StringManager.getManager(
            JdbcConnectionPoolDeployer.class);
    static private String msg = sm.getString("resource.restart_needed");

    static private Logger _logger = LogDomains.getLogger(JdbcConnectionPoolDeployer.class,LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public void deployResource(Object resource) throws Exception {
        //deployResource is not synchronized as there is only one caller
        //ResourceProxy which is synchronized

        //intentional no-op
        //From 8.1 PE/SE/EE, JDBC connection pools are no more resources and 
        //they would be available only to server instances that have a resoruce-ref
        //that maps to a pool. So deploy resource would not be called during 
        //JDBC connection pool creation. The actualDeployResource method 
        //below is invoked by JdbcResourceDeployer when a resource-ref for a 
        //resource that is pointed to this pool is added to a server instance

        _logger.fine(" JdbcConnectionPoolDeployer - deployResource : " + resource + " calling actualDeploy");
        actualDeployResource(resource);
    }

    /**
     * Deploy the resource into the server's runtime naming context
     *
     * @param resource a resource object
     * @throws Exception thrown if fail
     */
    public void actualDeployResource(Object resource) {
        _logger.fine(" JdbcConnectionPoolDeployer - actualDeployResource : " + resource);
        JdbcConnectionPool adminPool = (JdbcConnectionPool) resource;
        try {
            ConnectorConnectionPool connConnPool = createConnectorConnectionPool(adminPool);
            //now do internal book keeping
            runtime.createConnectorConnectionPool(connConnPool);
        } catch (Exception e) {
            Object params[] = new Object[]{adminPool.getName(), e};
            _logger.log(Level.WARNING, "error.creating.jdbc.pool", params);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource) throws Exception {
        _logger.fine(" JdbcConnectionPoolDeployer - unDeployResource : " +
                "calling actualUndeploy of " + resource);
        actualUndeployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource){
        return resource instanceof JdbcConnectionPool;
    }


    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param resource a resource object
     * @throws UnsupportedOperationException Currently we are not supporting this method.
     */

    public synchronized void actualUndeployResource(Object resource) throws Exception {
        _logger.fine(" JdbcConnectionPoolDeployer - unDeployResource : " + resource);

        JdbcConnectionPool jdbcConnPool = (JdbcConnectionPool) resource;

        String poolName = jdbcConnPool.getName();
        runtime.deleteConnectorConnectionPool(poolName);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Pool Undeployed");
        }
    }

    /**
     * Pull out the MCF configuration properties and return them as an array
     * of ConnectorConfigProperty
     *
     * @param adminPool   - The JdbcConnectionPool to pull out properties from
     * @param conConnPool - ConnectorConnectionPool which will be used by Resource Pool
     * @param connDesc    - The ConnectorDescriptor for this JDBC RA
     * @return ConnectorConfigProperty [] array of MCF Config properties specified
     *         in this JDBC RA
     */
    private ConnectorConfigProperty [] getMCFConfigProperties(
            JdbcConnectionPool adminPool,
            ConnectorConnectionPool conConnPool, ConnectorDescriptor connDesc) {

        ArrayList propList = new ArrayList();

        if(adminPool.getResType() != null) {
            if (ConnectorConstants.JAVA_SQL_DRIVER.equals(adminPool.getResType())) {
                propList.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDriverClassname() == null ? "" : adminPool.getDriverClassname(),
                        "The driver class name", "java.lang.String"));
            } else {
                propList.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDatasourceClassname() == null ? "" : adminPool.getDatasourceClassname(),
                        "The datasource class name", "java.lang.String"));
            }
        } else {
            //When resType is null, one of these classnames would be specified
            if(adminPool.getDriverClassname() != null) {
                propList.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDriverClassname() == null ? "" : adminPool.getDriverClassname(),
                        "The driver class name", "java.lang.String"));                
            } else if(adminPool.getDatasourceClassname() != null) {
                propList.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDatasourceClassname() == null ? "" : adminPool.getDatasourceClassname(),
                        "The datasource class name", "java.lang.String"));                
            }
        }
        propList.add(new ConnectorConfigProperty ("ConnectionValidationRequired",
                adminPool.getIsConnectionValidationRequired() + "",
                "Is connection validation required",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("ValidationMethod",
                adminPool.getConnectionValidationMethod() == null ? "" :
                        adminPool.getConnectionValidationMethod(),
                "How the connection is validated",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("ValidationTableName",
                adminPool.getValidationTableName() == null ?
                        "" : adminPool.getValidationTableName(),
                "Validation Table name",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty("ValidationClassName",
                adminPool.getValidationClassname() == null ?
                        "" : adminPool.getValidationClassname(),
                "Validation Class name",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("TransactionIsolation",
                adminPool.getTransactionIsolationLevel() == null ? "" :
                        adminPool.getTransactionIsolationLevel(),
                "Transaction Isolatin Level",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("GuaranteeIsolationLevel",
                adminPool.getIsIsolationLevelGuaranteed() + "",
                "Transaction Isolation Guarantee",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("StatementWrapping",
                adminPool.getWrapJdbcObjects() + "",
                "Statement Wrapping",
                "java.lang.String"));


        propList.add(new ConnectorConfigProperty ("StatementTimeout",
                adminPool.getStatementTimeoutInSeconds() + "",
                "Statement Timeout",
                "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("StatementCacheSize",
                adminPool.getStatementCacheSize() + "",
                "Statement Cache Size",
                "java.lang.String"));
                    
        propList.add(new ConnectorConfigProperty("InitSql",
                adminPool.getInitSql() + "", 
		"InitSql", 
		"java.lang.String"));

        propList.add(new ConnectorConfigProperty("SqlTraceListeners",
                adminPool.getSqlTraceListeners() + "",
                "Sql Trace Listeners",
                "java.lang.String"));
        
        //dump user defined poperties into the list
        Set connDefDescSet = connDesc.getOutboundResourceAdapter().
                getConnectionDefs();
        //since this a 1.0 RAR, we will have only 1 connDefDesc
        if (connDefDescSet.size() != 1) {
            throw new MissingResourceException("Only one connDefDesc present",
                    null, null);
        }

        Iterator iter = connDefDescSet.iterator();

        //Now get the set of MCF config properties associated with each
        //connection-definition . Each element here is an EnviromnentProperty
        Set mcfConfigProps = null;
        while (iter.hasNext()) {
            mcfConfigProps = ((ConnectionDefDescriptor) iter.next()).
                    getConfigProperties();
        }
        if (mcfConfigProps != null) {

            Map mcfConPropKeys = new HashMap();
            Iterator mcfConfigPropsIter = mcfConfigProps.iterator();
            while (mcfConfigPropsIter.hasNext()) {
                String key = ((ConnectorConfigProperty ) mcfConfigPropsIter.next()).
                        getName();
                mcfConPropKeys.put(key.toUpperCase(), key);
            }

            String driverProperties = "";
            for (Property rp : adminPool.getProperty()) {
                if (rp == null) {
                    continue;
                }
                String name = rp.getName();

                //The idea here is to convert the Environment Properties coming from
                //the admin connection pool to standard pool properties thereby
                //making it easy to compare in the event of a reconfig
                if ("MATCHCONNECTIONS".equals(name.toUpperCase())) {
                    //JDBC - matchConnections if not set is decided by the ConnectionManager
                    //so default is false
                    conConnPool.setMatchConnections(toBoolean(rp.getValue(), false));
                    logFine("MATCHCONNECTIONS");

                }else if ("ASSOCIATEWITHTHREAD".equals(name.toUpperCase())) {
                    conConnPool.setAssociateWithThread(toBoolean(rp.getValue(), false));
                    logFine("ASSOCIATEWITHTHREAD");

                } else if ("POOLING".equals(name.toUpperCase())) {
                    conConnPool.setPooling(toBoolean(rp.getValue(), true));
                    logFine("POOLING");

                } else if ("PING".equals(name.toUpperCase())) {
                    conConnPool.setPingDuringPoolCreation(toBoolean(rp.getValue(), false));
                    logFine("PING");
                } else if ("POOLDATASTRUCTURE".equals(name.toUpperCase())) {
                    conConnPool.setPoolDataStructureType(rp.getValue());
                    logFine("POOLDATASTRUCTURE");

                }else if ("POOLWAITQUEUE".equals(name.toUpperCase())) {
                    conConnPool.setPoolWaitQueue(rp.getValue());
                    logFine("POOLWAITQUEUE");

                } else if ("DATASTRUCTUREPARAMETERS".equals(name.toUpperCase())) {
                    conConnPool.setDataStructureParameters(rp.getValue());
                    logFine("DATASTRUCTUREPARAMETERS");

                } else if ("USERNAME".equals(name.toUpperCase()) ||
                        "USER".equals(name.toUpperCase())) {

                    propList.add(new ConnectorConfigProperty ("User",
                            rp.getValue(), "user name", "java.lang.String"));

                } else if ("PASSWORD".equals(name.toUpperCase())) {

                    propList.add(new ConnectorConfigProperty ("Password",
                            rp.getValue(), "Password", "java.lang.String"));

                } else if ("JDBC30DATASOURCE".equals(name.toUpperCase())) {

                    propList.add(new ConnectorConfigProperty ("JDBC30DataSource",
                            rp.getValue(), "JDBC30DataSource", "java.lang.String"));

                } else if ("PREFER-VALIDATE-OVER-RECREATE".equals(name.toUpperCase())) {
                    String value = rp.getValue();
                    conConnPool.setPreferValidateOverRecreate(toBoolean(value, false));
                    logFine("PREFER-VALIDATE-OVER-RECREATE : " + value);
                    
                } else if ("STATEMENT-CACHE-TYPE".equals(name.toUpperCase())) {
                    
                    propList.add(new ConnectorConfigProperty ("StatementCacheType",
                            rp.getValue(), "StatementCacheType", "java.lang.String"));
                } 
                else if (mcfConPropKeys.containsKey(name.toUpperCase())) {

                    propList.add(new ConnectorConfigProperty (
                            (String) mcfConPropKeys.get(name.toUpperCase()),
                            rp.getValue() == null ? "" : rp.getValue(),
                            "Some property",
                            "java.lang.String"));
                } else {
                    driverProperties = driverProperties + "set" + escape(name)
                            + "#" + escape(rp.getValue()) + "##";
                }
            }

            if (!driverProperties.equals("")) {
                propList.add(new ConnectorConfigProperty ("DriverProperties",
                        driverProperties,
                        "some proprietarty properties",
                        "java.lang.String"));
            }
        }


        propList.add(new ConnectorConfigProperty ("Delimiter",
                "#", "delim", "java.lang.String"));

        propList.add(new ConnectorConfigProperty ("EscapeCharacter",
                "\\", "escapeCharacter", "java.lang.String"));

        //create an array of EnvironmentProperties from above list
        ConnectorConfigProperty [] eProps = new ConnectorConfigProperty [propList.size()];
        ListIterator propListIter = propList.listIterator();

        for (int i = 0; propListIter.hasNext(); i++) {
            eProps[i] = (ConnectorConfigProperty) propListIter.next();
        }

        return eProps;

    }

    /**
     * To escape the "delimiter" characters that are internally used by Connector & JDBCRA.
     *
     * @param value String that need to be escaped
     * @return Escaped value
     */
    private String escape(String value) {
        CharSequence seq = "\\";
        CharSequence replacement = "\\\\";
        value = value.replace(seq, replacement);

        seq = "#";
        replacement = "\\#";
        value = value.replace(seq, replacement);
        return value;
    }


    private boolean toBoolean(Object prop, boolean defaultVal) {
        if (prop == null) {
            return defaultVal;
        }
        return Boolean.valueOf(((String) prop).toLowerCase());
    }

    /**
     * Use this method if the string being passed does not <br>
     * involve multiple concatenations<br>
     * Avoid using this method in exception-catch blocks as they
     * are not frequently executed <br>
     *
     * @param msg
     */
    private void logFine(String msg) {
        if (_logger.isLoggable(Level.FINE) && msg != null) {
            _logger.fine(msg);
        }
    }

    public ConnectorConnectionPool createConnectorConnectionPool(JdbcConnectionPool adminPool)
            throws ConnectorRuntimeException {

        String moduleName = ResourcesUtil.createInstance().getRANameofJdbcConnectionPool(adminPool);
        int txSupport = getTxSupport(moduleName);

        ConnectorDescriptor connDesc = runtime.getConnectorDescriptor(moduleName);

        //Create the connector Connection Pool object from the configbean object
        ConnectorConnectionPool conConnPool = new ConnectorConnectionPool(
                adminPool.getName());

        conConnPool.setTransactionSupport(txSupport);
        setConnectorConnectionPoolAttributes(conConnPool, adminPool);

        //Initially create the ConnectorDescriptor
        ConnectorDescriptorInfo connDescInfo =
                createConnectorDescriptorInfo(connDesc, moduleName);


        connDescInfo.setMCFConfigProperties(
                getMCFConfigProperties(adminPool, conConnPool, connDesc));

        //since we are deploying a 1.0 RAR, this is null
        connDescInfo.setResourceAdapterConfigProperties((Set) null);

        conConnPool.setConnectorDescriptorInfo(connDescInfo);

        return conConnPool;
    }


    private int getTxSupport(String moduleName) {
        if (ConnectorConstants.JDBCXA_RA_NAME.equals(moduleName)) {
           return ConnectionPoolObjectsUtils.parseTransactionSupportString(
               ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING );
        }
        return ConnectionPoolObjectsUtils.parseTransactionSupportString(
                ConnectorConstants.LOCAL_TRANSACTION_TX_SUPPORT_STRING);
    }

    private ConnectorDescriptorInfo createConnectorDescriptorInfo(
            ConnectorDescriptor connDesc, String moduleName) {
        ConnectorDescriptorInfo connDescInfo = new ConnectorDescriptorInfo();

        connDescInfo.setManagedConnectionFactoryClass(
                connDesc.getOutboundResourceAdapter().
                        getManagedConnectionFactoryImpl());

        connDescInfo.setRarName(moduleName);

        connDescInfo.setResourceAdapterClassName(connDesc.
                getResourceAdapterClass());

        connDescInfo.setConnectionDefinitionName(
                connDesc.getOutboundResourceAdapter().getConnectionFactoryIntf());

        connDescInfo.setConnectionFactoryClass(
                connDesc.getOutboundResourceAdapter().getConnectionFactoryImpl());

        connDescInfo.setConnectionFactoryInterface(
                connDesc.getOutboundResourceAdapter().getConnectionFactoryIntf());

        connDescInfo.setConnectionClass(
                connDesc.getOutboundResourceAdapter().getConnectionImpl());

        connDescInfo.setConnectionInterface(
                connDesc.getOutboundResourceAdapter().getConnectionIntf());

        return connDescInfo;
    }

    private void setConnectorConnectionPoolAttributes(
            ConnectorConnectionPool ccp, JdbcConnectionPool adminPool) {
        ccp.setMaxPoolSize(adminPool.getMaxPoolSize());
        ccp.setSteadyPoolSize(adminPool.getSteadyPoolSize());
        ccp.setMaxWaitTimeInMillis(adminPool.getMaxWaitTimeInMillis());

        ccp.setPoolResizeQuantity(adminPool.getPoolResizeQuantity());

        ccp.setIdleTimeoutInSeconds(adminPool.getIdleTimeoutInSeconds());

        ccp.setFailAllConnections(Boolean.valueOf(adminPool.getFailAllConnections()));

        ccp.setConnectionValidationRequired(Boolean.valueOf(adminPool.getIsConnectionValidationRequired()));

        ccp.setNonTransactional(Boolean.valueOf(adminPool.getNonTransactionalConnections()));

        ccp.setPingDuringPoolCreation(Boolean.valueOf(adminPool.getPing()));
        //These are default properties of all Jdbc pools
        //So set them here first and then figure out from the parsing routine
        //if they need to be reset
        ccp.setMatchConnections(Boolean.valueOf(adminPool.getMatchConnections()));
        ccp.setAssociateWithThread(Boolean.valueOf(adminPool.getAssociateWithThread()));
        ccp.setConnectionLeakTracingTimeout(adminPool.getConnectionLeakTimeoutInSeconds());
        ccp.setConnectionReclaim(Boolean.valueOf(adminPool.getConnectionLeakReclaim()));

        boolean lazyConnectionEnlistment = Boolean.valueOf(adminPool.getLazyConnectionEnlistment());
        boolean lazyConnectionAssociation = Boolean.valueOf(adminPool.getLazyConnectionAssociation());

        //lazy-connection-enlistment need to be ON for lazy-connection-association to work.
        if (lazyConnectionAssociation) {
            if (lazyConnectionEnlistment) {
                ccp.setLazyConnectionAssoc(true);
                ccp.setLazyConnectionEnlist(true);
            } else {
                _logger.log(Level.SEVERE, "conn_pool_obj_utils.lazy_enlist-lazy_assoc-invalid-combination",
                        adminPool.getName());
                String i18nMsg = sm.getString(
                        "cpou.lazy_enlist-lazy_assoc-invalid-combination", adminPool.getName());
                throw new RuntimeException(i18nMsg);
            }
        } else {
            ccp.setLazyConnectionAssoc(lazyConnectionAssociation);
            ccp.setLazyConnectionEnlist(lazyConnectionEnlistment);
        }
        
        boolean pooling = Boolean.valueOf(adminPool.getPooling());
        
        if(!pooling) {
            //Throw exception if assoc with thread is set to true.
            if(Boolean.valueOf(adminPool.getAssociateWithThread())) {
                _logger.log(Level.SEVERE, "conn_pool_obj_utils.pooling_disabled_assocwiththread_invalid_combination",
                        adminPool.getName());
                String i18nMsg = sm.getString(
                        "cpou.pooling_disabled_assocwiththread_invalid_combination", adminPool.getName());
                throw new RuntimeException(i18nMsg);
            }
            //TODO : Throw exception if flush connection pool is set
            
            //Below are useful in pooled environment only.
            //Throw warning for connection validation/validate-atmost-once/
            //match-connections/max-connection-usage-count/idele-timeout
            if(Boolean.valueOf(adminPool.getIsConnectionValidationRequired())) {
                _logger.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_conn_validation_invalid_combination",
                        adminPool.getName());                
            }
            if(Integer.parseInt(adminPool.getValidateAtmostOncePeriodInSeconds()) > 0) {
                _logger.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_validate_atmost_once_invalid_combination",
                        adminPool.getName());                                
            }
            if(Boolean.valueOf(adminPool.getMatchConnections())) {
                _logger.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_match_connections_invalid_combination",
                        adminPool.getName());                                                
            }
            if(Integer.parseInt(adminPool.getMaxConnectionUsageCount()) > 0) {
                _logger.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_max_conn_usage_invalid_combination",
                        adminPool.getName());                                                                
            }
            if(Integer.parseInt(adminPool.getIdleTimeoutInSeconds()) > 0) {
                _logger.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_idle_timeout_invalid_combination",
                        adminPool.getName());                
            }
        }
        ccp.setPooling(pooling);
        ccp.setMaxConnectionUsage(adminPool.getMaxConnectionUsageCount());

        ccp.setConCreationRetryAttempts(adminPool.getConnectionCreationRetryAttempts());
        ccp.setConCreationRetryInterval(
                adminPool.getConnectionCreationRetryIntervalInSeconds());

        ccp.setValidateAtmostOncePeriod(adminPool.getValidateAtmostOncePeriodInSeconds());
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized void redeployResource(Object resource) throws Exception {

        JdbcConnectionPool adminPool = (JdbcConnectionPool) resource;


        //Only if pool has already been deployed in this server-instance
        //reconfig this pool
        if (!runtime.isConnectorConnectionPoolDeployed(adminPool.getName())) {

            _logger.fine("The JDBC connection pool " + adminPool.getName()
                    + " is not referred or not yet created in this server "
                    + "instance and hence pool redeployment is ignored");
            return;
        }

        ConnectorConnectionPool connConnPool = createConnectorConnectionPool(
                adminPool);

        if (connConnPool == null) {
            throw new ConnectorRuntimeException("Unable to create ConnectorConnectionPool" +
                    "from JDBC connection pool");
        }

        //now do internal book keeping
        HashSet excludes = new HashSet();
        //add MCF config props to the set that need to be excluded
        //in checking for the equality of the props with old pool
        excludes.add("TransactionIsolation");
        excludes.add("GuaranteeIsolationLevel");
        excludes.add("ValidationTableName");
        excludes.add("ConnectionValidationRequired");
        excludes.add("ValidationMethod");
        excludes.add("StatementWrapping");
        excludes.add("StatementTimeout");
        excludes.add("ValidationClassName");


        try {
            _logger.finest("Calling reconfigure pool");
            boolean poolRecreateRequired =
                    runtime.reconfigureConnectorConnectionPool(connConnPool,
                            excludes);
            if (poolRecreateRequired) {
                _logger.finest("Pool recreation required");
                runtime.recreateConnectorConnectionPool(connConnPool);
                _logger.finest("Pool recreation done");
            }
        } catch (ConnectorRuntimeException cre) {
            Object params[] = new Object[]{adminPool.getName(), cre};
            _logger.log(Level.WARNING, "error.redeploying.jdbc.pool", params);
            throw cre;
        }
    }

    /**
     * Enable the resource in the server's runtime naming context
     *
     * @param resource a resource object
     * @exception UnsupportedOperationException Currently we are not supporting this method.
     *
     */
	public synchronized void enableResource(Object resource) throws Exception {
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Disable the resource in the server's runtime naming context
     *
     * @param resource a resource object
     * @exception UnsupportedOperationException Currently we are not supporting this method.
     *
     */
	public synchronized void disableResource(Object resource) throws Exception {
        throw new UnsupportedOperationException(msg);
    }
}
