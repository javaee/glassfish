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
package com.sun.enterprise.resource;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.deployment.archivist.ConnectorArchivist;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.server.ResourceDeployer;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

/**
 * Handles Jdbc connection pool events in the server instance. When user adds a
 * jdbc connection pool , the admin instance emits resource event. The jdbc
 * connection pool events are propagated to this object.
 *
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author Tamil Vengan
 */

 // This class was created to fix the bug # 4650787

public class JdbcConnectionPoolDeployer implements ResourceDeployer {

    static private StringManager sm = StringManager.getManager(
            JdbcConnectionPoolDeployer.class);
    static private String msg = sm.getString("resource.restart_needed");
    
    static private Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    
    public synchronized void deployResource(Object resource) throws Exception {
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
     * @exception Exception thrown if fail
     */
	public synchronized void actualDeployResource(Object resource)  {
        _logger.fine(" JdbcConnectionPoolDeployer - actualDeployResource : " + resource );
        com.sun.enterprise.config.serverbeans.JdbcConnectionPool adminPool =
            (com.sun.enterprise.config.serverbeans.JdbcConnectionPool) resource;
        
	
	ConnectorConnectionPool connConnPool = createConnectorConnectionPool( 
	        adminPool);		
         
	
	//now do internal book keeping 
	try {
	    ConnectorRuntime.getRuntime().createConnectorConnectionPool(
	        connConnPool);
	} catch( ConnectorRuntimeException cre ) {
	    cre.printStackTrace();
	}
    }

    public synchronized void undeployResource(Object resource) throws Exception {
        _logger.fine(" JdbcConnectionPoolDeployer - unDeployResource : " +
                                "calling actualUndeploy of " + resource);
        actualUndeployResource(resource);
    }
    
    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param resource a resource object
     * @exception UnsupportedOperationException  Currently we are not supporting this method.
     */

    public synchronized void actualUndeployResource(Object resource) throws Exception{
        _logger.fine(" JdbcConnectionPoolDeployer - unDeployResource : " + resource);
        
        com.sun.enterprise.config.serverbeans.JdbcConnectionPool jdbcConnPool =
	    (com.sun.enterprise.config.serverbeans.JdbcConnectionPool) resource;

	String poolName = jdbcConnPool.getName();
	ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
	runtime.deleteConnectorConnectionPool( poolName );
	if ( _logger.isLoggable( Level.FINEST ) ) {
	    _logger.finest("Pool Undeployed");
	}
    }

    /**
     * Redeploy the resource into the server's runtime naming context
     *
     * @param resource a resource object
     * @exception UnsupportedOperationException Currently we are not supporting this method.
     */
    public synchronized void redeployResource(Object resource) throws Exception {
         
        com.sun.enterprise.config.serverbeans.JdbcConnectionPool adminPool
	        = (com.sun.enterprise.config.serverbeans.JdbcConnectionPool)
		resource;


        //Only if pool has already been deployed in this server-instance
        //reconfig this pool
        if (!ConnectorRuntime.getRuntime().
                        isConnectorConnectionPoolDeployed(adminPool.getName())) {
            
            _logger.fine("The JDBC connection pool " + adminPool.getName()
                            + " is not referred or not yet created in this server "
                            + "instance and hence pool redeployment is ignored");
            return;
        }
        
	ConnectorConnectionPool connConnPool = createConnectorConnectionPool( 
	        adminPool);		

        if (connConnPool == null) {
	    throw new ConnectorRuntimeException("Unable to create ConnectorConnectionPool"+
	            "from JDBC connection pool");
	}

	//now do internal book keeping 
	ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
	HashSet excludes = new HashSet();
	//add MCF config props to the set that need to be excluded
	//in checking for the equality of the props with old pool
	excludes.add( "TransactionIsolation");
	excludes.add( "GuaranteeIsolationLevel");
	excludes.add( "ValidationTableName");
	excludes.add( "ConnectionValidationRequired");
	excludes.add( "ValidationMethod");
	excludes.add( "StatementWrapping");
	excludes.add( "StatementTimeout");


    try {
	    _logger.finest("Calling reconfigure pool");
	    boolean poolRecreateRequired = 
	        runtime.reconfigureConnectorConnectionPool(connConnPool,
		        excludes);
	    if ( poolRecreateRequired ) {
	       _logger.finest("Pool recreation required");    
	       runtime.recreateConnectorConnectionPool( connConnPool ); 
	       _logger.finest("Pool recreation done");    
	    }
	} catch( ConnectorRuntimeException cre ) {
	    cre.printStackTrace();
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


    /**
     * Utility method to find a resource from Resources beans and convert
     * it to a resource object to be used by the implemented ResourceDeployer
     *
     * @param     name      connection pool name
     * @param     rbeans    Resources config-beans
     * @exception Exception thrown if fail
     */
    public Object getResource(String name, Resources rbeans) throws Exception {
        Object res = rbeans.getJdbcConnectionPoolByName(name);

        if (res == null) {
	    String msg = sm.getString("resource.no_resource",name);
            throw new Exception(msg);
        }

        return res;
    }

    /**    
     * Load the default RA descriptor from the installed system RAR
     * 
     * @param moduleDir - the location where the rar is installed
     * @return ConnectorDescriptor for the RAR
     *
     * @see ConnectorDescriptor;
     */
    private ConnectorDescriptor createConnectorDescriptor( 
            String moduleDir ) {

	ConnectorDescriptor connectorDescriptor = null ;
        FileArchive fa = new FileArchive();
	try {
            fa.open( moduleDir );  // directory where rar is exploded
            ConnectorArchivist archivist = new ConnectorArchivist();
            connectorDescriptor = (ConnectorDescriptor)
            archivist.open(fa);
	} catch( Exception ioe ) {
	    ioe.printStackTrace();
	}

	return connectorDescriptor;
    }


    /**
     * Pull out the MCF configuration properties and return them as an array
     * of EnvironmentProperty
     *
     * @param adminPool      - The JdbcConnectionPool to pull out properties from
     * @param conConnPool    - ConnectorConnectionPool which will be used by Resource Pool
     * @param connDesc - The ConnectorDescriptor for this JDBC RA
     * @return EnvironmentProperty[] array of MCF Config properties specified
     *         in this JDBC RA
     */
    private EnvironmentProperty[] getMCFConfigProperties(
            JdbcConnectionPool adminPool,
            ConnectorConnectionPool conConnPool, ConnectorDescriptor connDesc) {

        ArrayList propList = new ArrayList();

        propList.add(new EnvironmentProperty("ClassName",
                adminPool.getDatasourceClassname() == null ? "" :
                        adminPool.getDatasourceClassname(),
                "The datasource class name",
                "java.lang.String"));


        propList.add(new EnvironmentProperty("ConnectionValidationRequired",
                adminPool.isIsConnectionValidationRequired() + "",
                "Is connection validation required",
                "java.lang.String"));

        propList.add(new EnvironmentProperty("ValidationMethod",
                adminPool.getConnectionValidationMethod() == null ? "" :
                        adminPool.getConnectionValidationMethod(),
                "How the connection is validated",
                "java.lang.String"));

        propList.add(new EnvironmentProperty("ValidationTableName",
                adminPool.getValidationTableName() == null ?
                        "" : adminPool.getValidationTableName(),
                "Validation Table name",
                "java.lang.String"));

        propList.add(new EnvironmentProperty("TransactionIsolation",
                adminPool.getTransactionIsolationLevel() == null ? "" :
                        adminPool.getTransactionIsolationLevel(),
                "Transaction Isolatin Level",
                "java.lang.String"));

        propList.add(new EnvironmentProperty("GuaranteeIsolationLevel",
                adminPool.isIsIsolationLevelGuaranteed() + "",
                "Transaction Isolation Guarantee",
                "java.lang.String"));

        propList.add(new EnvironmentProperty("StatementWrapping",
                adminPool.isWrapJdbcObjects() + "",
                "Statement Wrapping",
                "java.lang.String"));


        propList.add(new EnvironmentProperty("StatementTimeout",
                adminPool.getStatementTimeoutInSeconds() + "",
                "Statement Timeout",
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
                String key = ((EnvironmentProperty) mcfConfigPropsIter.next()).
                        getName();
                mcfConPropKeys.put(key.toUpperCase(), key);
            }

            String driverProperties = "";
            for (ElementProperty rp : adminPool.getElementProperty()) {
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

                } else if ("LAZYCONNECTIONASSOCIATION".equals(name.toUpperCase())) {
                    ConnectionPoolObjectsUtils.setLazyEnlistAndLazyAssocProperties(rp.getValue(), adminPool, conConnPool);
                    logFine("LAZYCONNECTIONASSOCIATION");

                } else if ("LAZYCONNECTIONENLISTMENT".equals(name.toUpperCase())) {
                    conConnPool.setLazyConnectionEnlist(toBoolean(rp.getValue(), false));
                    logFine("LAZYCONNECTIONENLISTMENT");

                } else if ("ASSOCIATEWITHTHREAD".equals(name.toUpperCase())) {
                    conConnPool.setAssociateWithThread(toBoolean(rp.getValue(), false));
                    logFine("ASSOCIATEWITHTHREAD");

                } else if ("USERNAME".equals(name.toUpperCase()) ||
                        "USER".equals(name.toUpperCase())) {

                    propList.add(new EnvironmentProperty("User",
                            rp.getValue(), "user name", "java.lang.String"));

                } else if ("PASSWORD".equals(name.toUpperCase())) {

                    propList.add(new EnvironmentProperty("Password",
                            rp.getValue(), "Password", "java.lang.String"));

                } else if ("JDBC30DATASOURCE".equals(name.toUpperCase())) {

                    propList.add(new EnvironmentProperty("JDBC30DataSource",
                            rp.getValue(), "JDBC30DataSource", "java.lang.String"));

                } else if (mcfConPropKeys.containsKey(name.toUpperCase())) {

                    propList.add(new EnvironmentProperty(
                            (String) mcfConPropKeys.get(name.toUpperCase()),
                            rp.getValue() == null ? "" : rp.getValue(),
                            "Some property",
                            "java.lang.String"));
                } else {
                    driverProperties = driverProperties + "set" + name
                            + "#" + rp.getValue() + "##";
                }
            }

            if (!driverProperties.equals("")) {
                propList.add(new EnvironmentProperty("DriverProperties",
                        driverProperties,
                        "some proprietarty properties",
                        "java.lang.String"));
            }
        }


        propList.add(new EnvironmentProperty("Delimiter",
                "#", "delim", "java.lang.String"));
        //create an array of EnvironmentProperties from above list
        EnvironmentProperty[] eProps = new EnvironmentProperty[propList.size()];
        ListIterator propListIter = propList.listIterator();

        for (int i = 0; propListIter.hasNext(); i++) {
            eProps[i] = (EnvironmentProperty) propListIter.next();
        }

        return eProps;

    }

    private String getSystemModuleLocation(String moduleName) {
        String j2eeModuleDirName = System.getProperty(Constants.INSTALL_ROOT) + 
                File.separator + "lib" + File.separator + "install" + 
                File.separator + "applications" + File.separator + moduleName;

	return j2eeModuleDirName;
	
    }  
    private boolean toBoolean( Object prop, boolean defaultVal ) {
        if ( prop == null ) {
            return defaultVal;
        }
        return Boolean.valueOf(((String) prop).toLowerCase());
    }
   
    /**
     * Use this method if the string being passed does not <br>
     * involve multiple concatenations<br>
     * Avoid using this method in exception-catch blocks as they
     * are not frequently executed <br>
     * @param msg
     */
    private void logFine(String msg) {
        if (_logger.isLoggable(Level.FINE) && msg != null) {
            _logger.fine(msg);
        }
    }

    public ConnectorConnectionPool createConnectorConnectionPool( 
            JdbcConnectionPool adminPool) 
    {

	String moduleName = getModuleName( adminPool );
        String moduleDir = getSystemModuleLocation( moduleName );
	int txSupport = getTxSupport( moduleName );

	ConnectorDescriptor connDesc = createConnectorDescriptor( moduleDir );

        //Create the connector Connection Pool object from the configbean object
        ConnectorConnectionPool conConnPool = new ConnectorConnectionPool( 
	        adminPool.getName() );

        conConnPool.setTransactionSupport( txSupport );
        setConnectorConnectionPoolAttributes( conConnPool, adminPool );		
	
	

	//Initially create the ConnectorDescriptor
        ConnectorDescriptorInfo connDescInfo = 
	    createConnectorDescriptorInfo( connDesc, moduleName );


        connDescInfo.setMCFConfigProperties( 
	    getMCFConfigProperties( adminPool, conConnPool, connDesc ) );
        
        //since we are deploying a 1.0 RAR, this is null
	connDescInfo.setResourceAdapterConfigProperties((Set) null );

	conConnPool.setConnectorDescriptorInfo( connDescInfo );

	return conConnPool;

    }
    
    private String getModuleName( JdbcConnectionPool jcp ) {

        String resType = jcp.getResType();
	String dsClassName = jcp.getDatasourceClassname();

        String moduleName = ConnectorConstants.JDBCDATASOURCE_RA_NAME;
 
	if (resType == null) {
	    // default to non-xa
	    return moduleName;
	}

        if ("javax.sql.XADataSource".equals(resType) && dsClassName != null) {

            try {
                Class dsClass = Utility.loadClass( dsClassName );
                if (javax.sql.XADataSource.class.isAssignableFrom(dsClass)) {
                    return ConnectorConstants.JDBCXA_RA_NAME;
                }
            } catch (ClassNotFoundException e) {
                //ignore
            } 
        }

        if ("javax.sql.ConnectionPoolDataSource".equals(resType) && 
	    dsClassName != null) {

            try {
                Class dsClass = Utility.loadClass( dsClassName );
                if (javax.sql.ConnectionPoolDataSource.class.isAssignableFrom(dsClass)) {
                    return ConnectorConstants.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME;
                }
            } catch (ClassNotFoundException e) {
                //ignore
            } 
        }

	return moduleName;

    }

    private int getTxSupport( String moduleName ) {
        if ( ConnectorConstants.JDBCXA_RA_NAME.equals(moduleName)) {
	    return ConnectionPoolObjectsUtils.parseTransactionSupportString(
	        ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING ); 
	} 

	return ConnectionPoolObjectsUtils.parseTransactionSupportString(
	    ConnectorConstants.LOCAL_TRANSACTION_TX_SUPPORT_STRING );
    }

    private ConnectorDescriptorInfo createConnectorDescriptorInfo(
        ConnectorDescriptor connDesc, String moduleName )
    {
        ConnectorDescriptorInfo connDescInfo = new ConnectorDescriptorInfo();

    	connDescInfo.setManagedConnectionFactoryClass(
	    connDesc.getOutboundResourceAdapter().
	    getManagedConnectionFactoryImpl() );
	
	connDescInfo.setRarName( moduleName );

        connDescInfo.setResourceAdapterClassName( connDesc.
	    getResourceAdapterClass() );
        
	connDescInfo.setConnectionDefinitionName(
	    connDesc.getOutboundResourceAdapter().
	    getConnectionFactoryIntf() );
	
	connDescInfo.setConnectionFactoryClass(
	    connDesc.getOutboundResourceAdapter().
	    getConnectionFactoryImpl() );

	connDescInfo.setConnectionFactoryInterface(
	    connDesc.getOutboundResourceAdapter().
	    getConnectionFactoryIntf() );

	connDescInfo.setConnectionClass(
	    connDesc.getOutboundResourceAdapter().
	    getConnectionImpl() );
	
	connDescInfo.setConnectionInterface(
	    connDesc.getOutboundResourceAdapter().
	    getConnectionIntf() );

	return connDescInfo;    
    }

    private void setConnectorConnectionPoolAttributes(
            ConnectorConnectionPool ccp, JdbcConnectionPool adminPool) {
        ccp.setMaxPoolSize(adminPool.getMaxPoolSize() != null ?
                adminPool.getMaxPoolSize() :
                JdbcConnectionPool.getDefaultMaxPoolSize());

        ccp.setSteadyPoolSize(adminPool.getSteadyPoolSize() != null ?
                adminPool.getSteadyPoolSize() :
                JdbcConnectionPool.getDefaultSteadyPoolSize());

        ccp.setMaxWaitTimeInMillis(
                adminPool.getMaxWaitTimeInMillis() != null ?
                        adminPool.getMaxWaitTimeInMillis() :
                        JdbcConnectionPool.getDefaultMaxWaitTimeInMillis());

        ccp.setPoolResizeQuantity(
                adminPool.getPoolResizeQuantity() != null ?
                        adminPool.getPoolResizeQuantity() :
                        JdbcConnectionPool.getDefaultPoolResizeQuantity());

        ccp.setIdleTimeoutInSeconds(
                adminPool.getIdleTimeoutInSeconds() != null ?
                        adminPool.getIdleTimeoutInSeconds() :
                        JdbcConnectionPool.getDefaultIdleTimeoutInSeconds());

        ccp.setFailAllConnections(adminPool.isFailAllConnections());

        ccp.setConnectionValidationRequired(
                adminPool.isIsConnectionValidationRequired());

        ccp.setNonTransactional(
                adminPool.isNonTransactionalConnections());
        ccp.setNonComponent(adminPool.isAllowNonComponentCallers());

        //These are default properties of all Jdbc pools
        //So set them here first and then figure out from the parsing routine
        //if they need to be reset
        ccp.setMatchConnections(adminPool.isMatchConnections());
        ccp.setAssociateWithThread(adminPool.isAssociateWithThread());
        ccp.setConnectionLeakTracingTimeout(adminPool.getConnectionLeakTimeoutInSeconds());
        ccp.setConnectionReclaim(adminPool.isConnectionLeakReclaim());

        boolean lazyConnectionEnlistment = adminPool.isLazyConnectionEnlistment();
        boolean lazyConnectionAssociation = adminPool.isLazyConnectionAssociation();

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
        ccp.setMaxConnectionUsage(adminPool.getMaxConnectionUsageCount());

        ccp.setConCreationRetryAttempts(adminPool.getConnectionCreationRetryAttempts());
        ccp.setConCreationRetryInterval(
        adminPool.getConnectionCreationRetryIntervalInSeconds());

        ccp.setValidateAtmostOncePeriod(adminPool.getValidateAtmostOncePeriodInSeconds());
    }

}
