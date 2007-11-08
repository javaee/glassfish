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

package com.sun.enterprise.resource;

import java.util.*;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.logging.*;
import javax.transaction.xa.*;
import javax.naming.*;
import javax.sql.*;
import com.sun.jms.spi.xa.*;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.security.PasswordCredential;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import com.sun.enterprise.*;
import com.sun.enterprise.util.*;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.repository.*;
import com.sun.logging.*;
import com.sun.enterprise.jms.IASJmsUtil;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.transaction.XAResourceWrapper;

import javax.naming.spi.InitialContextFactory;
import javax.naming.InitialContext;

import java.io.File;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;

import com.sun.enterprise.connectors.ActiveInboundResourceAdapter;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.system.ActiveJmsResourceAdapter;
import com.sun.enterprise.connectors.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.util.RARUtils;

import com.sun.enterprise.autotxrecovery.TransactionRecovery;

import com.sun.jts.CosTransactions.RecoveryManager;
import com.sun.jts.CosTransactions.DelegatedRecoveryManager;


/**
 * This class handles the installation and configuration
 * of various resources including JDBC datasources,
 * resource adapters and connection factories
 *
 * @author Tony Ng
 */
public class ResourceInstaller {

    static final private String SET_ = "set";
    //GJCINT
    static final private String SET_CONNECTION_FACTORY_NAME = "setConnectionFactoryName";
    
    // static final private boolean debug = true;
    static final private boolean debug = false;
    
    private static LocalStringManagerImpl localStrings =
    new LocalStringManagerImpl(ResourceInstaller.class);


    private J2EEResourceFactory resFactory;
    private J2EEResourceCollection resourceInfo;

    //Commented from 9.1 as it is not used
    //ManagementObjectManager for registering JSR77 types
    //private ManagementObjectManager mgmtObjectMgr;

    // Create logger object per Java SDK 1.4 to log messages
    // introduced Santanu De, Sun Microsystems, March 2002
    
    private static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    
    public ResourceInstaller() {
        //Commented from 9.1 as it is not used
/*      connectorDescriptors = new Hashtable();
        mgmtObjectMgr = Switch.getSwitch().getManagementObjectManager(); */

        try {
            resFactory = ServerConfiguration.getJ2EEResourceFactory();
            resourceInfo = resFactory.loadDefaultResourceCollection();
        } catch(J2EEResourceException re) {
            _logger.log(Level.SEVERE,"J2EE.cannot_load_resources",re);
            
        }
    }

    
    private void loadAllJdbcResources() {
        
        ResourcesUtil resutil = ResourcesUtil.createInstance();
        try {
            com.sun.enterprise.config.serverbeans.JdbcResource[] jdbcResources =
                    (com.sun.enterprise.config.serverbeans.JdbcResource[])
                    (resutil.getJdbcResourcesAsMap().get(ResourcesUtil.RESOURCES));
            InitialContext ic = new InitialContext();
            for(int i=0; jdbcResources != null && i < jdbcResources.length; ++i) {
                if (jdbcResources[i].isEnabled()) {
                    try{
                        ic.lookup(jdbcResources[i].getJndiName());
                    } catch(Exception ex) {
                        _logger.log(Level.SEVERE,"error.loading.jdbc.resources.during.recovery",jdbcResources[i].getJndiName());
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log( Level.FINE, ex.toString(), ex);
                        }
                    }
                }
            }
        } catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"error.loading.jdbc.resources.during.recovery",ce.getMessage());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log( Level.FINE, ce.toString(), ce);
            }
        } catch(NamingException ne) {
            _logger.log(Level.SEVERE,"error.loading.jdbc.resources.during.recovery",ne.getMessage());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log( Level.FINE, ne.toString(), ne);
            }
        }
    }

    private void loadAllConnectorResources() {
        
        ResourcesUtil resutil = ResourcesUtil.createInstance();
        try {
            com.sun.enterprise.config.serverbeans.ConnectorResource[] connResources =
                    (com.sun.enterprise.config.serverbeans.ConnectorResource[])(resutil.
                    getAllConnectorResources().get(ResourcesUtil.RESOURCES));
            InitialContext ic = new InitialContext();
            for(int i=0; connResources != null && i < connResources.length; ++i) {
                if (connResources[i].isEnabled()) {
                    try{
                        ic.lookup(connResources[i].getJndiName());
                    } catch(NameNotFoundException ne) {
                        //If you are here then it is most probably an embedded RAR resource
                        //So we need to explicitly load that rar and create the resources
                        try{
                            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool connConnectionPool =
                                    resutil.getConnectorConnectionPoolByName(connResources[i].getPoolName());
                            createActiveResourceAdapter(connConnectionPool.getResourceAdapterName());
                            (new ConnectorResourceDeployer()).deployResource(connResources[i]);
                        }catch(Exception ex){
                            _logger.log(Level.SEVERE,"error.loading.connector.resources.during.recovery",connResources[i].getJndiName());
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log( Level.FINE, ne.toString(), ne);
                            }
                            _logger.log(Level.SEVERE,"error.loading.connector.resources.during.recovery",connResources[i].getJndiName());
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log( Level.FINE, ex.toString() , ex);
                            }
                        }
                    } catch(Exception ex) {
                        _logger.log(Level.SEVERE,"error.loading.connector.resources.during.recovery",connResources[i].getJndiName());
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log( Level.FINE, ex.toString() , ex);
                        }
                    }
                }
            }
        } catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"error.loading.connector.resources.during.recovery",ce.getMessage());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log( Level.FINE, ce.toString() , ce);
            }
        } catch(NamingException ne) {
            _logger.log(Level.SEVERE,"error.loading.connector.resources.during.recovery",ne.getMessage());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log( Level.FINE, ne.toString(), ne);
            }
        }
    }

        //Commented from 9.1 as it is not used
    /*private void loadAllJmsResources() {
        
        try {
            ResourcesUtil resutil = ResourcesUtil.createInstance();
            com.sun.enterprise.config.serverbeans.ConnectorResource[] jmsResources =
                    resutil.getAllJmsResources();
            InitialContext ic = new InitialContext();
            for(int i=0; jmsResources != null && i < jmsResources.length; ++i) {
                if(jmsResources[i].isEnabled()){
                    try{
                        ic.lookup(jmsResources[i].getJndiName());
                    } catch(Exception ex) {
                        _logger.log(Level.SEVERE,"error.loading.jms.resources.during.recovery",jmsResources[i]);
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log( Level.FINE, ex.toString(), ex );
                        }
                    }
                }
            }
        } catch(NamingException ne) {
            _logger.log(Level.SEVERE,"error.loading.jms.resources.during.recovery",ne);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log( Level.FINE, ne.toString(), ne );
            }
        }
        
    }
*/
    public boolean recoverIncompleteTx(boolean delegated, String logPath) throws Exception {
        boolean result = false;
            Vector jdbcConnList = new Vector();
            Vector jmsConnList = new Vector();
            Vector connectorConnList = new Vector();
        try {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Performing recovery of incomplete Tx...");
            }

            Vector xaresList = new Vector();
            Context ic =
            Switch.getSwitch().getNamingManager().getInitialContext();
            
            recoverJdbcXAResources(xaresList, jdbcConnList);
            recoverExternalJndiResourceJMSXAResources(ic,xaresList,jmsConnList);
            recoverConnectorXAResources(xaresList, connectorConnList);
            recoverInboundTransactions(xaresList);
            
            int size = xaresList.size();
            XAResource[] xaresArray = new XAResource[size];
            for (int i=0; i<size; i++) {
                xaresArray[i] = (XAResource) xaresList.elementAt(i);
            }
            String msg = localStrings.getLocalString
            ("xaresource.recovering", "Recovering {0} XA resources...",
            new Object[] {String.valueOf(size)});

            _logger.log(Level.FINE,msg);
            if (!delegated) {
                RecoveryManager.recoverIncompleteTx(xaresArray);
                result = true;
            }
            else
                result = DelegatedRecoveryManager.delegated_recover(logPath, xaresArray);
            
            return result;
        } finally {
            try {
                closeJdbcXAResources(jdbcConnList);
                closeConnectorXAResources(connectorConnList);
                closeJMSXAResources(jmsConnList);
            } catch (Exception ex1) {
                _logger.log(Level.WARNING,"xaresource.recover_error",ex1);            
            }
        }
    }

    public void recoverXAResources() {
        try {
            ConfigContext ctx =
                  ApplicationServer.getServerContext().getConfigContext();
            TransactionService txnService = null;
            txnService =
               ServerBeansFactory.getTransactionServiceBean(ctx);
            PluggableFeatureFactory pff = ApplicationServer.getServerContext().
                    getPluggableFeatureFactory();
            TransactionRecovery transactionRecoveryService = 
                    pff.getTransactionRecoveryService();
            //transactionRecoveryService.start(context) ;
            RecoveryManager.registerTransactionRecoveryService(transactionRecoveryService);
            if (!txnService.isAutomaticRecovery()) {
                return;
            }
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,"ejbserver.recovery",
                            "Perform recovery of XAResources...");
            }

            Vector xaresList = new Vector();
            Vector jdbcConnList = new Vector();
            Vector jmsConnList = new Vector();
            Vector connectorConnList = new Vector();
            Context ic =
            Switch.getSwitch().getNamingManager().getInitialContext();
            
            recoverJdbcXAResources(xaresList, jdbcConnList);
            recoverExternalJndiResourceJMSXAResources(ic,xaresList,jmsConnList);
            recoverConnectorXAResources(xaresList, connectorConnList);
            recoverInboundTransactions(xaresList);
            recoverThirdPartyResources(xaresList);
            
            int size = xaresList.size();
            XAResource[] xaresArray = new XAResource[size];
            for (int i=0; i<size; i++) {
                xaresArray[i] = (XAResource) xaresList.elementAt(i);
            }
            J2EETransactionManager tm =
            Switch.getSwitch().getTransactionManager();
            recoveryStarted();
            String msg = localStrings.getLocalString
            ("xaresource.recovering", "Recovering {0} XA resources...",
            new Object[] {String.valueOf(size)});

            _logger.log(Level.FINE,msg);
            tm.recover(xaresArray);
            recoveryCompleted();
            
            closeJdbcXAResources(jdbcConnList);
            closeConnectorXAResources(connectorConnList);
            closeJMSXAResources(jmsConnList);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"xaresource.recover_error",ex);            
        }
    }
    

    private void recoverThirdPartyResources(Vector xaresList) {
        Set<RecoveryResourceListener> listeners = 
        RecoveryResourceRegistry.getInstance().getListeners();

        for (RecoveryResourceListener rrl : listeners) {
             XAResource[] xars = rrl.getXAResources();
             for (XAResource xar : xars) {
                  xaresList.add(xar);
             }
        }
    }

    private void recoveryStarted() {
        Set<RecoveryResourceListener> listeners = 
        RecoveryResourceRegistry.getInstance().getListeners();

        for (RecoveryResourceListener rrl : listeners) {
             rrl.recoveryStarted();
        }
    }

    private void recoveryCompleted() {
        Set<RecoveryResourceListener> listeners = 
        RecoveryResourceRegistry.getInstance().getListeners();

        for (RecoveryResourceListener rrl : listeners) {
             rrl.recoveryCompleted();
        }
    }

    
    private void recoverJdbcXAResources( Vector xaresList, Vector connList) {
        
        ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
	com.sun.enterprise.config.ConfigBean[] jdbcres = null;

	try {
	    jdbcres = (com.sun.enterprise.config.serverbeans.JdbcResource[])
                    (resourceUtil.getJdbcResourcesAsMap().get(ResourcesUtil.RESOURCES));
	} catch( ConfigException ce ) {
	    _logger.log(Level.WARNING, ce.getMessage() );
	    return;
	}
        
	if ( jdbcres == null || jdbcres.length == 0 ) {
	    return;
	}
        
	Set jdbcPools = new HashSet();
	
	for (int i = 0; i < jdbcres.length; i++ ) {
	    try {
	        if ( resourceUtil.isEnabled( jdbcres[i]) ) {
	            com.sun.enterprise.config.serverbeans.JdbcConnectionPool 
	                pool = resourceUtil.getJdbcConnectionPoolByName( 
	                    ((com.sun.enterprise.config.serverbeans.JdbcResource)
	            	    jdbcres[i]).getPoolName() );
	            if ( pool != null && 
		            "javax.sql.XADataSource".equals(pool.getResType() ) ) {
	                jdbcPools.add( pool );
	            }
                if (_logger.isLoggable(Level.FINE)){
                        _logger.fine( "ResourceInstaller:: recoverJdbcXAResources :: "
	                    + "adding : " + ((com.sun.enterprise.config.serverbeans.JdbcResource)
	                    jdbcres[i]).getPoolName() );
                }
            }
	    } catch( ConfigException ce ) {
	        _logger.log(Level.WARNING, ce.getMessage() );
	    }
	}
	
        loadAllJdbcResources();
        // Read from the transaction-service , if the replacement of
        // Vendor XAResource class with our version required.
        // If yes, put the mapping in the xaresourcewrappers properties.
        Properties xaresourcewrappers = new Properties();
        try {
            ConfigContext ctx = 
                 ApplicationServer.getServerContext().getConfigContext();
            TransactionService txs = 
                 ServerBeansFactory.getTransactionServiceBean(ctx);
            xaresourcewrappers.put(
                "oracle.jdbc.xa.client.OracleXADataSource",
                "com.sun.enterprise.transaction.OracleXAResource");
            
            ElementProperty[] eprops = txs.getElementProperty();
            for(int i=0;i<eprops.length;i++){
                String name = eprops[i].getName();
                String value = eprops[i].getValue();
                if(name.equals("oracle-xa-recovery-workaround")) {
                    if("false".equals(value)){
                        xaresourcewrappers.remove(
                           "oracle.jdbc.xa.client.OracleXADataSource");
                    }
                } else if(name.equals("sybase-xa-recovery-workaround")) {
                    if(value.equals("true")) {
                        xaresourcewrappers.put(
                            "com.sybase.jdbc2.jdbc.SybXADataSource",
                            "com.sun.enterprise.transaction.SybaseXAResource");
                    }
                }
            }
        }catch(ConfigException cex){
            _logger.log(Level.INFO,"jdbc.config_error",cex);
            
        }

        ConnectorRuntime crt = ConnectorRuntime.getRuntime();

        //for(int i=0; i<jdbcConnectionPools.length; ++i) {
	Iterator iter = jdbcPools.iterator();
	while( iter.hasNext() ) {
	    com.sun.enterprise.config.serverbeans.JdbcConnectionPool 
	        jdbcConnectionPool = (com.sun.enterprise.config.serverbeans.JdbcConnectionPool) 
		iter.next();
            if(jdbcConnectionPool.getResType()==null 
                || jdbcConnectionPool.getName()==null
                || !jdbcConnectionPool.getResType().equals(
                          "javax.sql.XADataSource")) {
                continue;
            }
            String poolName = jdbcConnectionPool.getName();
            try {
                
                String[] dbUserPassword = 
                      resourceUtil.getdbUserPasswordOfJdbcConnectionPool(
                      jdbcConnectionPool);
                String dbUser = dbUserPassword[0];
                String dbPassword = dbUserPassword[1];
                ManagedConnectionFactory fac = 
                      crt.obtainManagedConnectionFactory(poolName);
                Subject subject = new Subject();
                PasswordCredential pc = new PasswordCredential(
                      dbUser, dbPassword.toCharArray());
                pc.setManagedConnectionFactory(fac);
                Principal prin = new ResourcePrincipal(dbUser, dbPassword);
                subject.getPrincipals().add(prin);
                subject.getPrivateCredentials().add(pc);
                ManagedConnection mc = fac.createManagedConnection(subject, null);
                connList.addElement(mc);
                try {
                    XAResource xares = mc.getXAResource();
                    if (xares != null) {

                // See if a wrapper class for the vendor XADataSource is 
                // specified if yes, replace the XAResouce class of database 
                // vendor with our own version 


                        String clName =
                             jdbcConnectionPool.getDatasourceClassname();
                        String wrapperclass = (String)xaresourcewrappers.get(
                             clName);
                        if(wrapperclass!=null){
                            XAResourceWrapper xaresWrapper = null;
                            try{
                               xaresWrapper = (XAResourceWrapper)Class.forName(
                                   wrapperclass).newInstance();
                            }catch(Exception ex){
                               throw ex;
                            }
                            xaresWrapper.init(mc,subject);
                            xaresList.addElement(xaresWrapper);
                        }else{
                            xaresList.addElement(xares);
                        }
                    }
                } catch (ResourceException ex) {
                    // ignored. Not at XA_TRANSACTION level
                }
            } catch (Exception ex) {
                _logger.log(Level.WARNING,"datasource.xadatasource_error",
                             poolName);
                _logger.log(Level.FINE,"datasource.xadatasource_error_excp",ex);

            }
        }
    }
    
    private void recoverConnectorXAResources(Vector xaresList,Vector connList){

        ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
	
	com.sun.enterprise.config.ConfigBean[] connRes = null;
	
	try {
	    connRes = (com.sun.enterprise.config.ConfigBean[])(resourceUtil.
                    getAllConnectorResources().get(ResourcesUtil.RESOURCES));
	} catch( ConfigException ce ) {
	    _logger.log(Level.WARNING, ce.getMessage() ) ;
	    return;
	}
	    
        if(connRes == null || connRes.length == 0 ) {
            return;
        }
        Set connPools = new HashSet();
	for (int i = 0 ; i < connRes.length; i++) {
	    try {
	        if ( resourceUtil.isEnabled( connRes[i]) ) {
	            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool pool = 
	                resourceUtil.getConnectorConnectionPoolByName( 
	                    ((com.sun.enterprise.config.serverbeans.ConnectorResource)
	            	connRes[i]).getPoolName() );
	            if ( pool != null && 
		         ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING.equals(
			         getTransactionSupport(pool)) ) {
	                connPools.add( pool );
                    if (_logger.isLoggable(Level.FINE)){
                        _logger.fine( "ResourceInstaller:: recoverConnectorXAResources :: "
	                        + "adding : " +
	            	    (((com.sun.enterprise.config.serverbeans.ConnectorResource)
	            	    (connRes)[i])).getPoolName() );
                    }
                }
	        }
	    } catch( ConfigException ce ) {
	        _logger.warning( ce.getMessage() );
	    }
	}
	loadAllConnectorResources();
	Iterator iter = connPools.iterator();
	
	_logger.log(Level.FINE,"Recovering pools : " + connPools.size());
        ConnectorRuntime crt = ConnectorRuntime.getRuntime();
        //for(int i = 0; i<connectorConnectionPools.length;++i) {
	while( iter.hasNext() ) {
	    com.sun.enterprise.config.serverbeans.ConnectorConnectionPool 
	        connPool = (com.sun.enterprise.config.serverbeans.ConnectorConnectionPool) 
	        iter.next();
            String poolName = connPool.getName();
            try {
                String[] dbUserPassword = 
                       resourceUtil.getdbUserPasswordOfConnectorConnectionPool(
                       connPool);
		if ( dbUserPassword == null ) {
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
                if(dbPassword==null) {
                    dbPassword = "";
                    _logger.log(Level.WARNING,
                        "datasource.xadatasource_nullpassword_error",poolName);
                }
                
		if(dbUser == null){
                    dbUser = "";
                    _logger.log(Level.WARNING,
                       "datasource.xadatasource_nulluser_error",poolName);
		}
	        String rarName = connPool.getResourceAdapterName();	
	 	if (ConnectorAdminServiceUtils.isJMSRA(rarName)) {
	        _logger.log(Level.FINE,"Performing recovery for JMS RA, poolName  " + poolName);
                    ManagedConnectionFactory[] mcfs =
                       crt.obtainManagedConnectionFactories(poolName);
		    _logger.log (Level.INFO, "JMS resource recovery has created CFs = " + mcfs.length);
  		    for (int i = 0; i<mcfs.length;i++) {
                	PasswordCredential pc = new PasswordCredential(
                           dbUser, dbPassword.toCharArray());
                        pc.setManagedConnectionFactory(mcfs[i]);
                    	Principal prin = 
				new ResourcePrincipal(dbUser, dbPassword);
                    	subject.getPrincipals().add(prin);
                    	subject.getPrivateCredentials().add(pc);
		    	ManagedConnection mc = mcfs[i].
				createManagedConnection(subject, null);	
                        connList.addElement(mc);
                    	try {
                                XAResource xares = mc.getXAResource();
                        	if (xares != null) {
       		                  xaresList.addElement(xares);
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
                    connList.addElement(mc);
                    try {
                        XAResource xares = mc.getXAResource();
                        if (xares != null) {
                         xaresList.addElement(xares);
                    }
                    } catch (ResourceException ex) {
                        // ignored. Not at XA_TRANSACTION level
                    }
		}
            } catch (Exception ex) {
                _logger.log(Level.WARNING,"datasource.xadatasource_error",
                        poolName);
                _logger.log(Level.FINE,"datasource.xadatasource_error_excp",ex);
            }
        }
	_logger.log(Level.FINE, "Total XAResources identified for recovery is " + xaresList.size());
	_logger.log(Level.FINE, "Total connections identified for recovery is " + connList.size());
    }
    private void recoverExternalJndiResourceJMSXAResources(Context ic, Vector xaresList, Vector connList) {
        Set res = resourceInfo.getResourcesByType
        (J2EEResource.EXTERNAL_JNDI_RESOURCE);
        
        recoverJMSXAResources(ic, xaresList, connList, res);
    }

        //Commented from 9.1 as it is not used
   /* private void recoverJMSResourceJMSXAResources(Context ic, Vector xaresList, Vector connList) {
        loadAllJmsResources();
        Set res = resourceInfo.getResourcesByType
        (J2EEResource.JMS_CNX_FACTORY);
        recoverJMSXAResources(ic, xaresList, connList, res);
        
        String jndiName = null;
        JMSXAConnectionFactory obj;
        try {
            jndiName = IASJmsUtil.MDB_CONTAINER_QUEUE_XACF;
            obj = (JMSXAConnectionFactory)ic.lookup(jndiName);
            recoverJMSXAResource(xaresList, connList, obj, true);
        }
        catch (Exception ex) {
            _logger.log(Level.SEVERE,"datasource.xadatasource_error",jndiName);
            _logger.log(Level.SEVERE,"datasource.xadatasource_error_excp",ex);
        }
        try {
            jndiName = IASJmsUtil.MDB_CONTAINER_TOPIC_XACF;
            obj = (JMSXAConnectionFactory)ic.lookup(jndiName);
            recoverJMSXAResource(xaresList, connList, obj, false);
        }
        catch (Exception ex) {
            _logger.log(Level.SEVERE,"datasource.xadatasource_error",jndiName);
            _logger.log(Level.SEVERE,"datasource.xadatasource_error_excp",ex);
        }
    }*/
    
    private void recoverJMSXAResources(Context ic,
    Vector xaresList, Vector connList, Set jmsRes) {
        for(Iterator iter = jmsRes.iterator(); iter.hasNext();) {
            J2EEResource next = (J2EEResource)iter.next();
            if (next instanceof ExternalJndiResource) {
                if (!((ExternalJndiResource)next).isJMSConnectionFactory()) {
                    continue;
                }
            }
            
            String jndiName = next.getName();
            try {
                JMSXAConnectionFactory obj;
                boolean isQueue;
                if (next instanceof ExternalJndiResource) {
                    Object objext = ic.lookup(jndiName);
                    if (!(objext instanceof javax.jms.ConnectionFactory)) {
                        throw new NamingException(localStrings.getLocalString("recovery.unexpected_objtype",
                        "Unexpected object type "+objext.getClass().getName()+" for "+ jndiName,
                        new Object[]{objext.getClass().getName(), jndiName}));
                    }
                    obj = (JMSXAConnectionFactory)IASJmsUtil.wrapJMSConnectionFactoryObject(objext);
                    isQueue = (objext instanceof javax.jms.QueueConnectionFactory);
                }
                else {
                    obj = (JMSXAConnectionFactory)ic.lookup(IASJmsUtil.getXAConnectionFactoryName(jndiName));
                    isQueue = (obj instanceof JMSXAQueueConnectionFactory);
                }
                recoverJMSXAResource(xaresList, connList, obj, isQueue);
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,"datasource.xadatasource_error",jndiName);
                _logger.log(Level.SEVERE,"datasource.xadatasource_error_excp",ex);
            }
        }
    }
    
    private void recoverJMSXAResource(Vector xaresList, Vector connList,
    JMSXAConnectionFactory obj, boolean isQueue ) throws Exception {
        if (isQueue) {
            JMSXAQueueConnectionFactory fac =
            (JMSXAQueueConnectionFactory) obj;
            JMSXAQueueConnection con = fac.createXAQueueConnection();
            connList.addElement(con);
            XAResource xares = con.createXAQueueSession
            (true, Session.AUTO_ACKNOWLEDGE).getXAResource();
            xaresList.addElement(xares);
        } else {
            // XATopicConnectionFactory
            JMSXATopicConnectionFactory fac =
            (JMSXATopicConnectionFactory) obj;
            JMSXATopicConnection con = fac.createXATopicConnection();
            connList.addElement(con);
            XAResource xares = con.createXATopicSession
            (true, Session.AUTO_ACKNOWLEDGE).getXAResource();
            xaresList.addElement(xares);
        }
    }
    
     private void recoverInboundTransactions(Vector xaresList) throws ConfigException{
   	  	try{
            _logger.log(Level.INFO, "Recovery of Inbound Transactions started.");   	  	
    	ResourcesUtil resutil = ResourcesUtil.createInstance();
    	// Get list of application deployment desc from all deployed apps and ejb-modules.
    	Application[] applications = resutil.getDeployedApplications();
    	
    	if (applications.length == 0){
    		_logger.log(Level.FINE, "No applications deployed.");
    		return;
    	}
    	// List of CMT enabled MDB descriptors on the application server instance. 
    	ArrayList xaEnabledMDBList = new ArrayList();
    	   	
    	for (int i=0; i<applications.length; i++){
     	Vector ejbDescVec = applications[i].getEjbDescriptors();
     		for (int j=0; j<ejbDescVec.size(); j++){
     			EjbDescriptor desc = (EjbDescriptor)ejbDescVec.elementAt(j);    
     			// If EjbDescriptor is an instance of a CMT enabled MDB descriptor,
     			// add it to the list of xaEnabledMDBList. 
     			if ( desc instanceof EjbMessageBeanDescriptor && 
     					desc.getTransactionType().
     						equals(EjbDescriptor.CONTAINER_TRANSACTION_TYPE)){
     				xaEnabledMDBList.add(desc);     				
     				_logger.log(Level.FINE, "Found a CMT MDB: "
     						+ desc.getEjbClassName());
     			}
     		}
    	}
    	
    	if (xaEnabledMDBList.size() == 0) {
			_logger.log(Level.FINE, "Found no CMT MDBs in all applications");
			return;
		}
    	
    	ConnectorRuntime cr = ConnectorRuntime.getRuntime();
    	ConnectorRegistry creg = ConnectorRegistry.getInstance();

         //@TODO : Check whether this call is needed
        ClassLoader cl = ConnectorClassLoader.getInstance();

    	// for each RA (key in the hashtable) get the list (value) of MDB Descriptors 
    	Hashtable mappings = createRAEjbMapping(xaEnabledMDBList);
    	// To iterate through the keys(ramid), get the key Set from Hashtable. 
    	Set raMidSet = mappings.keySet();
    	
    	Iterator iter = raMidSet.iterator();
    	
        //For each RA
    	while (iter.hasNext()){
    		
    		String raMid = (String)iter.next();
                ArrayList respectiveDesc = (ArrayList)mappings.get(raMid);
                
                try{
                    createActiveResourceAdapter(raMid);
                } catch(Exception ex) {
                    _logger.log(Level.SEVERE,"error.loading.connector.resources.during.recovery", raMid);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log( Level.FINE, ex.toString() , ex);
                    }
                }
                
                ActiveInboundResourceAdapter activeInboundRA = (ActiveInboundResourceAdapter) creg
                        .getActiveResourceAdapter(raMid);
                
                assert activeInboundRA instanceof ActiveInboundResourceAdapter;
                
                boolean isSystemJmsRA = false;
                if (activeInboundRA instanceof ActiveJmsResourceAdapter) {
                    isSystemJmsRA = true;
                }
                
                javax.resource.spi.ResourceAdapter resourceAdapter = activeInboundRA
                        .getResourceAdapter();
                // activationSpecList represents the ActivationSpec[] that would be
                // sent to the getXAResources() method.
                ArrayList activationSpecList = new ArrayList();
    		
    		try{
        	for (int i=0; i<respectiveDesc.size(); i++){
        		try{
        		// Get a MessageBeanDescriptor from respectiveDesc ArrayList
        		EjbMessageBeanDescriptor descriptor = 
        			(EjbMessageBeanDescriptor)respectiveDesc.get(i); 
                        // A descriptor using 1.3 System JMS RA style properties needs
                        // to be updated J2EE 1.4 style props.
                        if (isSystemJmsRA) {
                             //XXX: Find out the pool descriptor corres to MDB and update
                             //MDBRuntimeInfo with that. 
                             ((ActiveJmsResourceAdapter) activeInboundRA)
                                                            .updateMDBRuntimeInfo(descriptor, null);
                        }
        			
        		// Get the ActivationConfig Properties from the MDB Descriptor
        		Set activationConfigProps = 
        			RARUtils.getMergedActivationConfigProperties(descriptor);
        		// get message listener type
        		String msgListenerType = descriptor.getMessageListenerType();
        		
        		// start resource adapter and get ActivationSpec class for 
        		// the given message listener type from the ConnectorRuntime

                        ActivationSpec aspec = (ActivationSpec) (Class.forName(
                                                       cr.getActivationSpecClass(raMid,
                                                       msgListenerType), false, 
                                                       resourceAdapter.getClass().getClassLoader()).newInstance());
                        aspec.setResourceAdapter(resourceAdapter);
        		
        		// Populate ActivationSpec class with ActivationConfig properties
        		SetMethodAction sma = 
        			new SetMethodAction(aspec,activationConfigProps);
    			sma.run();
    			activationSpecList.add(aspec);
        		} catch (Exception e){
        			_logger.log(Level.WARNING, "Error creating ActivationSpec \n"+e.getMessage());
                                if(_logger.isLoggable(Level.FINE)){
                                    _logger.log(Level.FINE, e.toString(), e);
                                }
        		}
        	}
        	       	
        	// Get XA resources from RA.
        	
        	ActivationSpec[] activationSpecArray = (ActivationSpec[])activationSpecList.toArray(new ActivationSpec[] {}); 
    		XAResource[] xar = resourceAdapter.getXAResources(activationSpecArray);
        	
    		// Add the resources to the xaresList which is used by the RecoveryManager 
    		for(int p=0; p<xar.length; p++){
				xaresList.addElement(xar[p]);
			
    		}
    		// Catch UnsupportedOperationException if a RA does not support XA 
    		// which is fine.  
    		} catch (UnsupportedOperationException uoex){
        		_logger.log(Level.FINE, uoex.getMessage());
        		// otherwise catch the unexpected exception
        	} catch (Exception e) {
        		_logger.log(Level.SEVERE, e.getMessage());
        	}
       	}
    } catch (Exception e){
    	_logger.log(Level.SEVERE, e.getMessage());
    }
    
    }

     private void createActiveResourceAdapter(String rarModuleName) throws ConfigException, ConnectorRuntimeException {
         
         ConnectorRuntime cr = ConnectorRuntime.getRuntime();
         ResourcesUtil resutil = ResourcesUtil.createInstance();
         ConnectorRegistry creg = ConnectorRegistry.getInstance();
         
         if(creg.isRegistered(rarModuleName))
             return;
         
         // If RA is embedded RA, find location of exploded rar.
         if (ConnectorAdminServiceUtils.isEmbeddedConnectorModule(rarModuleName)) {
             String appName = ConnectorAdminServiceUtils .getApplicationName(rarModuleName);
             String rarFileName = ConnectorAdminServiceUtils
                     .getConnectorModuleName(rarModuleName)+".rar";
             ConnectorDescriptor cd = resutil.getConnectorDescriptorFromUri(appName, rarFileName);
             String loc = resutil.getApplicationDeployLocation(appName);
             loc = loc + File.separator + FileUtils.makeFriendlyFilename(rarFileName);
             
             // start RA
             cr.createActiveResourceAdapter(cd,rarModuleName, loc);
             
             // else if RA is not embedded, it is already present in the
             // ConnectorRegistry. Start it straight away.
         } else {
             cr.createActiveResourceAdapter(resutil.getLocation(rarModuleName), rarModuleName);
         }
     }

      private Hashtable createRAEjbMapping(ArrayList list){
    	
    	Hashtable ht = new Hashtable();

    	   	
    	for (int i=0; i<list.size(); i++){
    	    	ArrayList ejbmdbd = new ArrayList();
    		String ramid = 
    			((EjbMessageBeanDescriptor)list.get(i)).getResourceAdapterMid();
                if ((ramid == null) || (ramid.equalsIgnoreCase(""))) {
                    ramid = ConnectorConstants.DEFAULT_JMS_ADAPTER;
                }
    			
    		// If Hashtable contains the RAMid key, get the list of MDB descriptors 
    		// and add the current MDB Descriptor (list[i]) to the list and put the 
    		// pair back into hashtable. 
    		// Otherwise, add the RAMid and the current MDB Descriptor to the hashtable 
    		if (ht.containsKey(ramid)){
    			ejbmdbd = (ArrayList)ht.get(ramid);
    			ht.remove(ramid);
    		}
    		
    		ejbmdbd.add(list.get(i));
    		ht.put(ramid, ejbmdbd);
    	}
    	return ht;
    		
    }

   private void closeJdbcXAResources(Vector connList) {
        int size = connList.size();
        for (int i=0; i<size; i++) {
            try {
                ManagedConnection con = (ManagedConnection) connList.elementAt(i);
                con.destroy();
            } catch (Exception ex) {
                _logger.log(Level.FINE,"JDBC Resources cannot be closed",ex);
            }
        }
    }
    
    private void closeJMSXAResources(Vector connList) {
        int size = connList.size();
        for (int i=0; i<size; i++) {
            try {
                Object obj = connList.elementAt(i);
                ((JMSXAConnection) obj).close();
            } catch (JMSException ex) {
                
                // Since closing error has been advised to be ignored
                // so we are not logging the message as an exception
                // but treating the same as a debug message
                // Santanu De, Sun Microsystems, 2002.
                
                _logger.log(Level.FINE,"JMS Resources cannot be closed",ex);
                
            }
        }
    }
    
    private void closeConnectorXAResources(Vector connList) {
        int size = connList.size();
        for (int i=0; i<size; i++) {
            try {
                Object obj = connList.elementAt(i);
                ((ManagedConnection) obj).destroy();
            } catch (Exception ex) {
                // Since closing error has been advised to be ignored
                // so we are not logging the message as an exception
                // but treating the same as a debug message
                // Santanu De, Sun Microsystems, 2002.
                
                _logger.log(Level.FINE,"Connector Resources cannot be closed",ex);
                
            }
        }
    }

    /**
     * Create a name for the internal data source for PM use
     * based on the user-specified JNDI name.
     */
    public static String getPMJndiName(String userJndiName) {
        return userJndiName + ConnectorConstants.PM_JNDI_SUFFIX;
    }
    
    public String getSystemModuleLocation(String moduleName) {
        String j2eeModuleDirName = System.getProperty(Constants.INSTALL_ROOT) + 
	    File.separator + "lib" + 
	    File.separator + "install" + 
	    File.separator + "applications" +
	    File.separator + moduleName;
        
        return j2eeModuleDirName;
    }
   
    public void installPersistenceManagerResources() throws Exception {
        logFine("***** Installing PersistenceManagerResources *****");
        Set pmfDatasources = resourceInfo.getResourcesByType
        (J2EEResource.PMF_RESOURCE);
        
        for(Iterator iter = pmfDatasources.iterator(); iter.hasNext();) {
            logFine("***** In for loop PersistenceManagerResources *****");
            PMFResource next = (PMFResource) iter.next();
            installPersistenceManagerResource(next);
            
        } // end for
        
        logFine("End of for loop PersistenceManagerResources *****");
        
    } // end installPersistenceManagerResources
    
    public void installPersistenceManagerResource(PMFResource pmfRes) 
        throws Exception 
    {
        String jndiName = null;
        try {
            jndiName = pmfRes.getName();
            logFine("***** installPersistenceManagerResources jndiName *****" + jndiName);
            
            String factory = pmfRes.getFactoryClass();
            logFine("**** PersistenceManagerSettings - factory " + factory);
            Class pmfImplClass = Class.forName(factory);
            Object pmfImpl = pmfImplClass.newInstance();
            
            String ds_jndi = pmfRes.getJdbcResourceJndiName();
            if (ds_jndi != null && ds_jndi.length() > 0) {
                String ds_jndi_pm = ResourceInstaller.getPMJndiName(ds_jndi);
                logFine("**** PersistenceManagerSettings - ds_jndi " + ds_jndi_pm);
                //@TODO : Check whether this call is needed
                DataSource pmDataSource = null;
                try {
                    javax.naming.Context ctx = new javax.naming.InitialContext();
                    //@TODO : Check whether this call is needed
                    pmDataSource = (DataSource)ctx.lookup(ds_jndi_pm);

                    //ASSUMPTION
                    //factory must have the following method specified in JDO
                    //    public void setConnectionFactory(Object);
                    //GJCINT - changing to setConnectionFactoryName
                    Method connFacMethod = pmfImplClass.getMethod(
                    SET_CONNECTION_FACTORY_NAME,
                    new Class[] { String.class });
                    connFacMethod.invoke(pmfImpl, new Object[] { ds_jndi_pm });
                    
                } catch ( Exception ex ) {
                    _logger.log(Level.SEVERE,"jndi.persistence_manager_config",ds_jndi_pm);
                    _logger.log(Level.FINE,"jndi.persistence_manager_config_excp",ex);
		    throw ex;
                }
            }
            
            Set propSet = pmfRes.getProperties();
            Iterator propIter = propSet.iterator();
            while (propIter.hasNext()) {
                ResourceProperty prop = (ResourceProperty)propIter.next();
                String name = prop.getName();
                String value = (String)prop.getValue();
                if (_logger.isLoggable(Level.FINE)){
                    _logger.fine("**** PersistenceManager propSettings - " + name + " " + value);
                }
                String methodName = SET_ + name.substring(0, 1).toUpperCase() +
                name.substring(1);
                //ASSUMPTION
                //set property in pmf have a mutator with String as arg
                Method method = pmfImplClass.getMethod(methodName,
                new Class[] { String.class });
                //ASSUMPTION
                method.invoke(pmfImpl, new Object[] { value });
            }
            
            NamingManager nm = Switch.getSwitch().getNamingManager();
            nm.publishObject(jndiName, pmfImpl, true);
            
            logFine("***** After publishing PersistenceManagerResources *****" );
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"poolmgr.datasource_error",jndiName);
            _logger.log(Level.FINE,"poolmgr.datasource_error_excp",ex);
            throw ex; 
        }
    } // end installPersistenceManagerResource
    
    /**
     * Installs the given custom resource. It publishes the resource as a
     * javax.naming.Reference with the naming manager (jndi). This method gets
     * called during server initialization and custom resource deployer to
     * handle custom resource events.
     *
     * @param    customRes    custom resource
     */
    public void installCustomResource(CustomResource customRes) {
        
        NamingManager nm = Switch.getSwitch().getNamingManager();
        String bindName  = null;
        
        try {
            bindName = customRes.getName();
            
            if (debug) {
                _logger.log(Level.FINE,"***** installCustomResources jndiName *****"
                + bindName);
            }
            
            // bind a Reference to the object factory
            Reference ref = new Reference(customRes.getResType(),
            customRes.getFactoryClass(),
            null);
            
            // add resource properties as StringRefAddrs
            for (Iterator props=customRes.getProperties().iterator();
            props.hasNext(); ) {
                
                ResourceProperty prop = (ResourceProperty) props.next();
                
                ref.add(new StringRefAddr(prop.getName(),
                (String) prop.getValue()));
            }
            
            // publish the reference
            nm.publishObject(bindName, ref, true);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"customrsrc.create_ref_error",bindName);
            _logger.log(Level.SEVERE,"customrsrc.create_ref_error_excp",ex);
        }
    }
    
    /**
     * Installs all iAS 7.0 custom resource object factories. This method gets
     * called during server initialization.
     */
    public void installCustomResources() {
        
        Set customResSet =
        resourceInfo.getResourcesByType(J2EEResource.CUSTOM_RESOURCE);
        
        for (Iterator iter = customResSet.iterator(); iter.hasNext();) {
            
            CustomResource next = (CustomResource) iter.next();
            installCustomResource(next);
        }
    }
    
    /**
     * load and create an object instance
     */
    public static Object loadObject(String className) {
        Object obj = null;
        Class c;
        
        try {
            obj = Class.forName(className).newInstance();
        } catch (Exception cnf) {
            try {
                c = ClassLoader.getSystemClassLoader().loadClass(className);
                obj = c.newInstance();
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,"classloader.load_class_fail",className);
                _logger.log(Level.SEVERE,"classloader.load_class_fail_excp",ex.getMessage());
                
            }
        }
        
        return obj;
    }
    
    /**
     * Installs the given external jndi resource. This method gets called
     * during server initialization and from external jndi resource
     * deployer to handle resource events.
     *
     * @param    extJndiRes    external jndi resource
     */
    public void installExternalJndiResource(ExternalJndiResource extJndiRes) {
        
        NamingManager nm = Switch.getSwitch().getNamingManager();
        
        String bindName = null;
        
        try {
            bindName  = extJndiRes.getName();
            
            // create the external JNDI factory, its initial context and
            // pass them as references.
            String factoryClass    = extJndiRes.getFactoryClass();
            String jndiLookupName  = extJndiRes.getJndiLookupName();
            
            if (debug) {
                _logger.log(Level.FINE,"installExternalJndiResources jndiName "
                + bindName + " factoryClass " + factoryClass
                + " jndiLookupName = " + jndiLookupName);
            }
            
            
            Object factory = loadObject(factoryClass);
            if (factory == null) {
                _logger.log(Level.WARNING,"jndi.factory_load_error",factoryClass);
                return;
                
            } else if (! (factory instanceof javax.naming.spi.InitialContextFactory)) {
                _logger.log(Level.WARNING,"jndi.factory_class_unexpected",factoryClass);
                return;
            }
            
            // Get properties to create the initial naming context
            // for the target JNDI factory
            Hashtable env = new Hashtable();
            for (Iterator props = extJndiRes.getProperties().iterator();
            props.hasNext(); ) {
                
                ResourceProperty prop = (ResourceProperty) props.next();
                env.put(prop.getName(), prop.getValue());
            }
            
            Context context = null;
            try {
                context =
                ((InitialContextFactory)factory).getInitialContext(env);
                
            } catch (NamingException ne) {
                _logger.log(Level.SEVERE,"jndi.initial_context_error",factoryClass);
                _logger.log(Level.SEVERE,"jndi.initial_context_error_excp",ne.getMessage());
            }
            
            if (context == null) {
                _logger.log(Level.SEVERE,"jndi.factory_create_error",factoryClass);
                return;
            }
            
            // Bind a Reference to the proxy object factory; set the
            // initial context factory.
            //JndiProxyObjectFactory.setInitialContext(bindName, context);
            
            Reference ref = new Reference(extJndiRes.getResType(),
            "com.sun.enterprise.resource.JndiProxyObjectFactory",
            null);
            
            // unique JNDI name within server runtime
            ref.add(new StringRefAddr("jndiName", bindName));
            
            // target JNDI name
            ref.add(new StringRefAddr("jndiLookupName", jndiLookupName));
            
            // target JNDI factory class
            ref.add(new StringRefAddr("jndiFactoryClass", factoryClass));
            
            // add Context info as a reference address
            ref.add(new ProxyRefAddr(bindName, env));
            
            // Publish the reference
            nm.publishObject(bindName, ref, true);
            
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"customrsrc.create_ref_error",bindName);
            _logger.log(Level.SEVERE,"customrsrc.create_ref_error_excp",ex);
            
        }
    }
    
    /**
     * Installs all iAS 7.0 external JNDI resource proxy resources. This method
     * gets called during server initialization.
     */
    public void installExternalJndiResources() {
        
        // all available external jndi resources
        Set extResSet = resourceInfo.getResourcesByType(
        J2EEResource.EXTERNAL_JNDI_RESOURCE);
        
        for (Iterator iter=extResSet.iterator(); iter.hasNext();) {
            
            ExternalJndiResource next = (ExternalJndiResource) iter.next();
            
            // install the resource
            installExternalJndiResource(next);
        }
    }
    
    /**
     * Un-installs the external jndi resource.
     *
     * @param    resource    external jndi resource
     */
    public void uninstallExternalJndiResource(J2EEResource resource) {
        
        // remove from the collection
        resourceInfo.removeResource(resource);
        
        // removes the jndi context from the factory cache
        JndiProxyObjectFactory.removeInitialContext( resource.getName() );
        
        // removes the resource from jndi naming
        NamingManager nm = Switch.getSwitch().getNamingManager();
        try {
            nm.unpublishObject( resource.getName() );
            //START OF IASRI 4660565
            if (((ExternalJndiResource)resource).isJMSConnectionFactory()) {
                nm.unpublishObject(IASJmsUtil.getXAConnectionFactoryName(resource.getName()));
            }
            //END OF IASRI 4660565
        } catch (javax.naming.NamingException e) {
            _logger.log(Level.FINE,
            "Error while unpublishing resource: " + resource.getName(), e);
        }
    }
    
    /**
     * Installs the given mail resource. This method gets called during server
     * initialization and from mail resource deployer to handle resource events.
     *
     * @param    mailRes    mail resource
     */
    public void installMailResource(MailResource mailRes) {
        
        NamingManager nm = Switch.getSwitch().getNamingManager();
        String bindName = null;
        
        try {
            bindName  = mailRes.getName();
            
            MailConfiguration config = new MailConfiguration(mailRes);
            
            // Publish the objet
            nm.publishObject(bindName, config, true);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "mailrsrc.create_obj_error", bindName);
            _logger.log(Level.SEVERE, "mailrsrc.create_obj_error_excp", ex);
        }
    }
    
    /**
     * Installs all iAS 7.0 mail resources. This method gets called during
     * server initialization.
     */
    public void installMailResources() {
        
        // all available mail resources
        Set mailResSet = resourceInfo.getResourcesByType(
        J2EEResource.MAIL_RESOURCE);
        
        for (Iterator iter = mailResSet.iterator(); iter.hasNext();) {
            MailResource next = (MailResource)iter.next();
            
            // install the resource
            installMailResource(next);
        }
    }
    
    public void addResource(J2EEResource resource)throws PoolingException{
        try{
            resourceInfo.addResource(resource);
            resFactory.storeDefaultResourceCollection(resourceInfo);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"J2EE.add_resource_error",ex);
            throw new PoolingException(ex.toString());
        }
    }
    
    
    
    public void removeResource(J2EEResource resource)throws PoolingException{
        try{
            resourceInfo.removeResource(resource);
            resFactory.storeDefaultResourceCollection(resourceInfo);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"J2EE.remove_resource_error",ex);
            throw new PoolingException(ex.toString());
        }
    }

    private void logFine( String msg ) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine( msg );
        }
    }

    private String getTransactionSupport( 
        com.sun.enterprise.config.serverbeans.ConnectorConnectionPool pool) {

	String txSupport = pool.getTransactionSupport();

        if ( txSupport != null ) {
	    return txSupport;
	}
        
        try { 
	
	    txSupport = ConnectorRuntime.getRuntime().getConnectorDescriptor( 
	        pool.getResourceAdapterName() ).getOutboundResourceAdapter().
		getTransSupport() ;
	} catch( ConnectorRuntimeException cre ) {
	    _logger.log(Level.WARNING, cre.getMessage() );
	    txSupport = ConnectorConstants.NO_TRANSACTION_TX_SUPPORT_STRING;
	}

        return txSupport;		
    }
}
