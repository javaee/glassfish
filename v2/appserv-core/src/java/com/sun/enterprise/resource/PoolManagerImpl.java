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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.transaction.*;
import javax.transaction.xa.*;
import com.sun.logging.*;
import com.sun.enterprise.*;
import com.sun.enterprise.Switch;
import com.sun.enterprise.distributedtx.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.resource.monitor.ConnectorServiceMonitoringLevelListener;
import com.sun.enterprise.resource.monitor.JDBCPoolMonitoringLevelListener;
import com.sun.enterprise.resource.monitor.ConnectorConnectionPoolStatsImpl;
import com.sun.enterprise.resource.monitor.JDBCConnectionPoolStatsImpl;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorConstants.PoolType;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;

import javax.resource.spi.ManagedConnection;
import javax.resource.ResourceException;


/**
 * @author Tony Ng, Aditya Gore
 * 
 */
public class PoolManagerImpl implements PoolManager {

    private ConcurrentHashMap poolTable;
    
    private ResourceManager resourceManager;
    private ResourceManager sysResourceManager;
    private ResourceManager noTxResourceManager;
    private LazyEnlistableResourceManagerImpl lazyEnlistableResourceManager;
    private PoolLifeCycle listener = null;

    // Create logger object per Java SDK 1.4 to log messages
    // introduced Santanu De, Sun Microsystems, March 2002
    
    static Logger _logger = null;
    static{
        _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    }

    MonitoringRegistry monitoringRegistry_;

    public PoolManagerImpl() {
        this.poolTable = new ConcurrentHashMap();

        resourceManager    = new ResourceManagerImpl();
        sysResourceManager = new SystemResourceManagerImpl();
        noTxResourceManager = new NoTxResourceManagerImpl();
        lazyEnlistableResourceManager = new LazyEnlistableResourceManagerImpl();

    }

    // self management hook
    public void registerPoolLifeCycleListner(PoolLifeCycle poolListener) {
        listener = poolListener;
    }

    public void createEmptyConnectionPool(String poolName, 
        PoolType pt) throws PoolingException 
    {
        //Create and initialise the connection pool
        createAndInitPool(poolName, pt);
        if (listener != null) {
            try {
               listener.poolCreated(poolName);
            } catch (Exception ex) {
	        _logger.log(Level.FINE, "Exception thrown on pool listener");
            }
        }
    }
    
    /**
     * Create and initialize pool if not created already.
     * 
     * @param poolName Name of the pool to be created
     */
    private ResourcePool createAndInitPool(final String poolName, PoolType pt) 
        throws PoolingException 
    {
        ResourcePool pool = getPool( poolName );
	if ( pool == null ) {
            pool = ResourcePoolFactoryImpl.newInstance( poolName, pt );
	    addPool( pool );
	    //--Monitoring
	    //create Stats object for this pool and add it to the stats 
	    //registry
	    try {
	        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
	        if ( runtime.getEnviron() == ConnectorConstants.SERVER ) {
	    	    if (isJdbcPool( poolName ) ) {
	    	        enableJDBCPoolMonitoring(pool, poolName);
	    	    } else {
	    	        enableConnectorConnectionPoolMonitoring(pool, poolName);
	    	    }
	        }
	    } catch( Exception e) {
	        _logger.log(Level.INFO, "poolmon.cannot_reg");
	        _logger.log(Level.FINE, "Exception while registering " +
	    	    "connection pool", e);
	    }
	    //--Monitoring End
        if (_logger.isLoggable(Level.FINE)){
            _logger.fine( "Created connection  pool  and added it to PoolManager :" + pool);
        }
    }
	return pool;
    }
    
    // invoked by DataSource objects to obtain a connection
    public Object getResource(ResourceSpec spec, ResourceAllocator alloc,
    ClientSecurityInfo info)
    throws PoolingException {
        
        Transaction tran = null;
        boolean transactional = alloc.isTransactional();
        
        if (transactional) {
            tran = getResourceManager( spec ).getTransaction();
        }
        
        ResourceHandle handle =
        getResourceFromPool(spec, alloc, info, tran);

        if( ! handle.supportsLazyAssociation() ) {
            spec.setLazyAssociatable( false ) ;
        }

        if (spec.isLazyAssociatable() && 
                spec.getConnectionToAssociate() != null) { 
            //If getConnectionToAssociate returns a connection that means
            //we need to associate a new connection with it
            try {
                Object connection = spec.getConnectionToAssociate();
                ManagedConnection dmc
                    = (ManagedConnection) handle.getResource();
                dmc.associateConnection( connection );
            } catch( ResourceException e ) {
                putbackDirectToPool( handle, spec.getConnectionPoolName());
                PoolingException pe = new PoolingException(
                    e.getMessage() );
                pe.initCause( e );
                throw pe;
            }
        }
        
        //If the ResourceAdapter does not support lazy enlistment
        //we cannot either
        if ( ! handle.supportsLazyEnlistment() ) {
            spec.setLazyEnlistable( false );
        }
    	handle.setResourceSpec(spec);
        
    	try {
	    if( handle.getResourceState().isUnenlisted()) {
                //The spec being used here is the spec with the updated
                //lazy enlistment info
                //Here's the real place where we care about the correct 
                //resource manager (which in turn depends upon the ResourceSpec)
                //and that's because if lazy enlistment needs to be done
                //we need to get the LazyEnlistableResourceManager
                getResourceManager( spec ).enlistResource(handle);
	    } 
    	} catch( Exception e) {
    	    //In the rare cases where enlistResource throws exception, we
    	    //should return the resource to the pool
    	    putbackDirectToPool( handle, spec.getConnectionPoolName());
    	    _logger.log(Level.WARNING, "poolmgr.err_enlisting_res_in_getconn");
  	        logFine("rm.enlistResource threw Exception. Returning resource to pool");
    	    //and rethrow the exception
    	    throw new PoolingException( e );
    
    	}
	
        return handle.getUserConnection();
    }
   
    public void registerResource(ResourceHandle handle)
    throws PoolingException {
        ResourceManager rm = getResourceManager(handle.getResourceSpec());
        rm.registerResource(handle);
    }

    public ResourceHandle getResourceFromPool(ResourceSpec spec,
                                                ResourceAllocator alloc,
                                                ClientSecurityInfo info,
                                                Transaction tran)
    throws PoolingException {
        ResourcePool pool = getPool( spec.getConnectionPoolName() );
        // pool.getResource() has been modified to:
        //      - be able to create new resource if needed
        //      - block the caller until a resource is acquired or
        //              the max-wait-time expires
        return pool.getResource(spec, alloc, tran);
    }
   
    
    private void enableConnectorConnectionPoolMonitoring(ResourcePool pool, 
        final String poolName) {
        
        MonitorableResourcePool mrp = null;
        final ResourcesUtil resUtil = ResourcesUtil.createInstance();
        
        if ( pool instanceof MonitorableResourcePool ) {
            mrp = (MonitorableResourcePool)pool;
        } else {
            return;
        }
        
        final com.sun.enterprise.config.serverbeans.ConnectorConnectionPool ccp =
                resUtil.getConnectorConnectionPoolByName(poolName);
        
        //this is a connector connection pool
        //check if monitoring level is ON and only then do 
        //registration
        if (getConnectorPoolMonitoringLevel() != MonitoringLevel.OFF ) {
            final ConnectorConnectionPoolStatsImpl ccpStatsImpl = 
	        new ConnectorConnectionPoolStatsImpl(mrp );

            if (getConnectorPoolMonitoringLevel() == MonitoringLevel.HIGH ) { 
                setMonitoringEnabledHigh( poolName );
            }
            if (getConnectorPoolMonitoringLevel() == MonitoringLevel.LOW ) { 
                setMonitoringEnabledLow(poolName );
            }

            AccessController.doPrivileged( new PrivilegedAction() {
                public Object run() {
                    try {
                        monitoringRegistry_.registerConnectorConnectionPoolStats(
			    ccpStatsImpl, 
                                poolName, 
                                resUtil.getAppName(ccp), 
                                resUtil.getRAName(ccp), 
                                null);
                    } catch( Exception mre ) {
                        _logger.log( Level.INFO, "poolmon.cannot_reg",
			    (mre.getMessage() != null ? mre.getMessage() : " ") );
                        _logger.fine("Error while enabling Connector Pool monitoring for pool "
			    + poolName + mre);
                        //reset monitoring state of the pool
                        disableMonitoring( poolName);
                    }
                    return null;
                }
            });
            if (_logger.isLoggable( Level.FINE ) ) {
                _logger.fine("Enabled pool monitoring at pool creation for " + poolName );
            }
        }
    }

    private void enableJDBCPoolMonitoring(ResourcePool pool, final String poolName) {
        MonitorableResourcePool mrp = null;

        if( pool instanceof MonitorableResourcePool ) {
            mrp = (MonitorableResourcePool) pool;
        } else {
            return;
        }
        
        //check if monitoring level is ON and only then
        //do registration
        if (getJdbcPoolMonitoringLevel() != MonitoringLevel.OFF) {
            final JDBCConnectionPoolStatsImpl jdbcStatsImpl =
                new JDBCConnectionPoolStatsImpl(mrp );
            if (getJdbcPoolMonitoringLevel() == MonitoringLevel.HIGH ) { 
                setMonitoringEnabledHigh( poolName );
            }
            if (getJdbcPoolMonitoringLevel() == MonitoringLevel.LOW ) { 
                setMonitoringEnabledLow(poolName );
            }
            AccessController.doPrivileged( new PrivilegedAction() {    
                public Object run() {
                    try { 
                        monitoringRegistry_.registerJDBCConnectionPoolStats(
			    jdbcStatsImpl, poolName, null );
                    } catch( Exception mre ) {
                        _logger.log( Level.INFO, "poolmon.cannot_reg",
			    (mre.getMessage() != null ? mre.getMessage() : " ") );
                        _logger.fine("Error while enabling JDBC Pool monitoring for pool "
			    + poolName + mre);

                        //reset monitoring state of the pool
                        disableMonitoring( poolName );
                    }
                    return null;
                }
            });
            
            if (_logger.isLoggable( Level.FINE ) ) {
                _logger.fine("Enabled pool monitoring at pool creation for " + poolName );
            }
        }
    }

    // called by EJB Transaction Manager
    public void resourceEnlisted(Transaction tran,
        ResourceHandle res) throws IllegalStateException 
    {
        
        String poolName = res.getResourceSpec().getConnectionPoolName();
        try {
            J2EETransaction j2eeTran = (J2EETransaction) tran;
            if (poolName != null && j2eeTran.getResources(poolName) == null) {
                addSyncListener(tran);
            }
        } catch (ClassCastException e) {
            addSyncListener(tran);
        }
	if ( poolName != null ) {
	    ResourcePool pool = getPool( poolName );
            if (pool != null) {
	        pool.resourceEnlisted(tran, res);
	    }
	}
    }

    private void addSyncListener(Transaction tran) {
        Synchronization sync = new SynchronizationListener(tran);
        try {
            tran.registerSynchronization(sync);
        } catch (Exception ex) {
            _logger.fine( "Error adding syncListener : " +
	        (ex.getMessage() != null ? ex.getMessage() : " "));
        }
    }
    
    // called by EJB Transaction Manager
    public void transactionCompleted(Transaction tran, int status)
    throws IllegalStateException {
        
	Iterator iter = ((J2EETransaction)tran).getAllParticipatingPools().iterator();
        while (iter.hasNext()) {
            ResourcePool pool = getPool((String)iter.next());
            if (_logger.isLoggable(Level.FINE)){
                _logger.fine( "calling transactionCompleted on " + pool.getPoolName() );
            }
        pool.transactionCompleted( tran, status );
        }

    }
    
    public void resourceClosed(ResourceHandle resource) {
        ResourceManager rm = getResourceManager(resource.getResourceSpec());
        rm.delistResource(resource, XAResource.TMSUCCESS);
        putbackResourceToPool(resource, false);
    }

    public void badResourceClosed(ResourceHandle resource){
        ResourceManager rm = getResourceManager(resource.getResourceSpec());
        rm.delistResource(resource, XAResource.TMSUCCESS);
        putbackBadResourceToPool(resource);
    }
    
    public void resourceErrorOccurred(ResourceHandle resource) {
        putbackResourceToPool(resource, true);
    }
    
    public void unregisterResource(ResourceHandle resource,
    int xaresFlag) {
        
        ResourceManager rm = getResourceManager(resource.getResourceSpec());
        rm.unregisterResource(resource,xaresFlag);
    }

    public void putbackBadResourceToPool(ResourceHandle h) {
        try {
            ResourceAllocator alloc = h.getResourceAllocator();
            alloc.cleanup(h);
        } catch (PoolingException ex) {
            //ignore, this connection will be destroyed anyway
        }
        // notify pool
        String poolName = h.getResourceSpec().getConnectionPoolName();
        if (poolName != null) {
            ResourcePool pool = (ResourcePool) poolTable.get(poolName);
            if (pool != null) {
                synchronized (pool) {
                    pool.resourceClosed(h);
                    h.setConnectionErrorOccurred();
                    pool.resourceErrorOccurred(h);
                }
            }
        }
    }

    public void putbackResourceToPool(ResourceHandle h,
    boolean errorOccurred) {
        
        // cleanup resource
        try {
            ResourceAllocator alloc = h.getResourceAllocator();
            alloc.cleanup(h);
        } catch (PoolingException ex) {
            errorOccurred = true;  // destroy resource
        }
        
        // notify pool
	String poolName = h.getResourceSpec().getConnectionPoolName();
	if ( poolName != null ) {
            ResourcePool pool = (ResourcePool) poolTable.get( poolName );
            if (pool != null) {
                if (errorOccurred) {
                    pool.resourceErrorOccurred(h);
                } else {
                    pool.resourceClosed(h);
                }
            }
	}
    }

    public void emptyResourcePool(ResourceSpec spec) {
        //ResourcePool pool = (ResourcePool) poolTable.get(spec.getConnectionPoolName());
	String poolName = spec.getConnectionPoolName();
	if ( poolName != null ) {
            ResourcePool pool = (ResourcePool) poolTable.get( poolName );
            if (pool != null) {
                pool.emptyPool();
            }
	}
    }

    public ResourceReferenceDescriptor getResourceReference(String jndiName) {
        
        InvocationManager i = Switch.getSwitch().getInvocationManager();
        if (i == null) return null;
        
        ComponentInvocation inv = null;
        
        inv = i.getCurrentInvocation();
        if (inv == null) {
            return null;
        }

        //@TODO : Check whether this call is needed
        int invType = inv.getInvocationType();
        Set refs = null;
        Object container = inv.getContainerContext();
        JndiNameEnvironment env = (JndiNameEnvironment)
        Switch.getSwitch().getDescriptorFor(container);
        // env can be null if it's CMP SQL generation
        if (env == null) return null;
        refs = env.getResourceReferenceDescriptors();
        
        Iterator iter = refs.iterator();
        
        while (iter.hasNext()) {
            ResourceReferenceDescriptor ref =
            (ResourceReferenceDescriptor) iter.next();
            String name = ref.getJndiName();
            if (jndiName.equals(name)) {
                return ref;
            }
        }
        // cannot find corresponding resource reference
        return null;
    }
    
    private ResourceManager getResourceManager(ResourceSpec spec) {
        if (spec.isNonTx()) {
            logFine( "@@@@ Returning noTxResourceManager");
            return noTxResourceManager;
        } else if (spec.isPM()) {
            logFine( "@@@@ Returning sysResourceManager");
            return sysResourceManager;
        } else if (spec.isLazyEnlistable() ) {
            logFine( "@@@@ Returning LazyEnlistableResourceManager");
            return lazyEnlistableResourceManager;
        } else {
            logFine( "@@@@ Returning resourceManager");
            return resourceManager;
        }
    }
    
    class SynchronizationListener implements Synchronization {
        
        private Transaction tran;
        
        SynchronizationListener(Transaction tran) {
            this.tran = tran;
        }
        
        public void afterCompletion(int status) {
            try {
                transactionCompleted(tran, status);
            } catch (Exception ex) {
                _logger.fine( "Exception in afterCompletion : " +
		    (ex.getMessage() != null ? ex.getMessage() : " " ));
            }
        }
        
        public void beforeCompletion() {
            // do nothing
        }
        
    }
    
    public void putbackDirectToPool(ResourceHandle h, String poolName ){
        // notify pool
	if ( poolName != null ) {
            ResourcePool pool = (ResourcePool) poolTable.get( poolName );
            if (pool != null) {
                pool.resourceClosed(h);
            }
	}
    }

    /**
     * Kill the pool with the specified pool name
     * 
     * @param poolName - The name of the pool to kill
     */
    public void killPool( String poolName ) {
        //empty the pool
        //and remove from poolTable
    	ResourcePool pool = (ResourcePool) poolTable.get( poolName );
    	if (pool != null ) {
    	    pool.cancelResizerTask();
    	    pool.emptyPool();
            if (_logger.isLoggable(Level.FINE)){
                _logger.fine("Removing pool " + pool + " from pooltable");
            }
        synchronized( poolTable ) {
    	        poolTable.remove( poolName );
	    }
        }

        // self management hook
        if (listener != null)
            listener.poolDestroyed(poolName);
            
    	//--Monitoring
    	try {
        	ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
        	
        	if ( runtime.getEnviron() == ConnectorConstants.SERVER ) {
        	    final String fPoolName = poolName;
        	    if (isJdbcPool( fPoolName )) {
        	        disableJDBCPoolMonitoring(fPoolName);
        	    } else {
        	        disableConnectorConnectionPoolMonitoring(fPoolName);
        	    }
        	}
    	} catch( Exception e) {
    	    _logger.log( Level.INFO, "poolmon.cannot_unreg");
    	}
    	//--Monitoring End
    }

    private void disableConnectorConnectionPoolMonitoring(final String fPoolName) {
        final ResourcesUtil resUtil = ResourcesUtil.createInstance();
        final com.sun.enterprise.config.serverbeans.ConnectorConnectionPool ccp =
                resUtil.getConnectorConnectionPoolByName(fPoolName);
        if (getConnectorPoolMonitoringLevel() != MonitoringLevel.OFF ) {
            AccessController.doPrivileged( new PrivilegedAction() {
                public Object run() {
                    try {    
                        monitoringRegistry_.unregisterConnectorConnectionPoolStats(
                                fPoolName,
                                resUtil.getAppName(ccp),
                                resUtil.getRAName(ccp) );
                    } catch( Exception mre) {
                        _logger.log( Level.INFO, "poolmon.cannot_unreg", 
                            mre.getMessage() );
                    }
                    return null;
                }
            });
        }
    }

    private void disableJDBCPoolMonitoring(final String fPoolName) {
        //We need to do this iff MonitoringLevel is not OFF
        //Otherwise, we haven't registered the pool anyways
         if ( getJdbcPoolMonitoringLevel() != MonitoringLevel.OFF ) {
             AccessController.doPrivileged( new PrivilegedAction() {
                 public Object run() {
                         try {    
                         monitoringRegistry_.unregisterJDBCConnectionPoolStats( 
                             fPoolName );
                     } catch( Exception mre) {
                        _logger.log( Level.INFO, "poolmon.cannot_unreg", 
                            mre.getMessage() );
                     }
                     return null;
             }
             });
        }
    }

    /**
     * Deletgates the task of setting a pool's properties to the actual
     * pool.
     * @param ccp - the ConnectorConnectionPool object that holds the new
     *              pool properties
     */
    public void reconfigPoolProperties( ConnectorConnectionPool ccp ) 
            throws PoolingException  {
        String poolName = ccp.getName();
        PoolManager poolmgr = Switch.getSwitch().getPoolManager();
        ResourcePool pool = (ResourcePool) poolmgr.getPoolTable().get( poolName );

        if (pool != null ) {
            pool.reconfigPoolProperties( ccp );
        }
    }
    
    /** 
     * Sets/resets the monitoringEnabled flag for each pool
     * 
     * @param poolName - the pool whose flag is to be set
     */
    public void disableMonitoring( String poolName ) {
       	ResourcePool pool = (ResourcePool) poolTable.get( poolName );
    	if (pool != null ) {
    	    pool.disableMonitoring();
    	}
    }

    /** 
     * Sets/resets the monitoringEnabledHigh flag for each pool
     * This flag indicates that the pool is being monitored at the
     * "HIGH" monitoring level
     *
     * @param poolName - the pool whose flag is to be set
     */
    public void setMonitoringEnabledHigh( String poolName ) {
       	ResourcePool pool = (ResourcePool) poolTable.get( poolName );
    	if (pool != null ) {
    	    pool.setMonitoringEnabledHigh();
    	}
    }

    /** 
     * Sets/resets the monitoringEnabledLow flag for each pool
     * This flag indicates that the pool is being monitored at the
     * "HIGH" monitoring level
     *
     * @param poolName - the pool whose flag is to be set
     */
    public void setMonitoringEnabledLow( String poolName ) {
       	ResourcePool pool = (ResourcePool) poolTable.get( poolName );
    	if (pool != null ) {
    	    pool.setMonitoringEnabledLow();
    	}
    }

    /**
     * Switch on matching in the pool.
     *
     * @param poolName Name of the pool
     */
    public boolean switchOnMatching(String poolName) {
        PoolManager poolmgr = Switch.getSwitch().getPoolManager();
	ResourcePool pool = (ResourcePool) poolmgr.getPoolTable().get( poolName );

	if (pool != null ) {
	    pool.switchOnMatching();
            return true;
        } else {
            return false;
        }
    }
    
    /*
     * Checks in the domain.xml via ResourcesUtil to see if this is 
     * a JDBC connection pool
     * returns true if it is
     */
    private boolean isJdbcPool( String poolName ) {
        JdbcConnectionPool[] pools = ResourcesUtil.createInstance().getJdbcConnectionPools();

        for (JdbcConnectionPool pool : pools) {
            if (poolName.equals(pool.getName())) {
                return true;
            }
        }

        return false;
    }

    public ConcurrentHashMap getPoolTable() {
        return poolTable;
    }

    private void addPool(ResourcePool pool) {
        if (_logger.isLoggable(Level.FINE)){
            _logger.fine("Adding pool " + pool.getPoolName() + "to pooltable");
        }
        synchronized (poolTable) {
            poolTable.put(pool.getPoolName(), pool);
        }
    }

    /**
     * Initialize monitoring by registering module monitoring level listeners
     * for Connector-service and JDBC Connector conncetion pools.
     * 
     * All Connector-Service and the old Connectorconnectionpool related module stats 
     * [namely connector-services' connection pools, Work management] are registered 
     * or unregistered, as the case maybe, in 
     * <code>ConnectorServiceModuleMonitoringLevelListener</code>
     * 
     * All JMS Service module related stats [as of now only JMS connection
     * factories] are <i>also</i> registered or unregistered, as the case maybe, 
     * in <code>ConnectorServiceModuleMonitoringLevelListener</code>
     * 
     * All JDBCConnectionPool related stats are registerd or unregistered, 
     * as the case maybe, in <code>JDBCPoolModuleMonitoringLevelListener</code>
     * 
     * (In 8.1 PE/SE/EE)
     * For backward compatability reasons: When the old connector connection pool 
     * or the new JMS-Service module's monitoring level is updated, the 
     * connector-service's module monitoring level listener is updated 
     * and vice-versa.
     */
    public void initializeMonitoring() {
        try {
            final ConnectorServiceMonitoringLevelListener csMonitoringListener
                = new ConnectorServiceMonitoringLevelListener();
            final JDBCPoolMonitoringLevelListener jdbcMonitoringListener
                = new JDBCPoolMonitoringLevelListener();
            
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    ServerContext ctxt = ApplicationServer.getServerContext();
                    if (ctxt != null) {
                        monitoringRegistry_ = ctxt.getMonitoringRegistry();
                        monitoringRegistry_.registerMonitoringLevelListener(
                            csMonitoringListener, MonitoredObjectType.CONNECTOR_SERVICE);
                        monitoringRegistry_.registerMonitoringLevelListener(
                                        jdbcMonitoringListener, 
                                        MonitoredObjectType.JDBC_CONN_POOL);
                    }
                    return null;
                }    
            });
            _logger.log( Level.FINE, "poolmon.init_monitoring_registry");
        } catch(Exception e) {
            _logger.log( Level.INFO, "poolmon.error_registering_listener", 
                e);
        }
    
    }
   
    /*
     * Gets the current monitoring level for Jdbc pools
     */
    private MonitoringLevel getJdbcPoolMonitoringLevel() {
        Config cfg = null;
	MonitoringLevel off = MonitoringLevel.OFF;
	MonitoringLevel l = off;

	try {
            cfg = ServerBeansFactory.getConfigBean( 
	        ApplicationServer.getServerContext().getConfigContext());
	    
            String lvl = 
	        cfg.getMonitoringService().getModuleMonitoringLevels().getJdbcConnectionPool();
	    l = MonitoringLevel.instance( lvl );
	    if (l == null ) {
	        //dont bother to throw an exception
	        return off;
	    }
	} catch (Exception e) {
	    return off;
	}
        return l;
    }

    /*
     * Gets the current monitoring level for connector pools
     */
    private MonitoringLevel getConnectorPoolMonitoringLevel() {
        Config cfg ;
	MonitoringLevel l ;

	try {
            cfg = ServerBeansFactory.getConfigBean( 
	        ApplicationServer.getServerContext().getConfigContext());
	    
            String lvl =
                    cfg.getMonitoringService().getModuleMonitoringLevels().getConnectorConnectionPool();
	    l = MonitoringLevel.instance( lvl );
	    if (l == null ) {
	        //dont bother to throw an exception
	        return MonitoringLevel.OFF; 
	    }
	} catch (Exception e) {
	    return MonitoringLevel.OFF;
	}
        return l;

    }

    /**
     * Kills all the connection pools in the server
     */

    public void killAllPools() {

        Iterator pools = poolTable.values().iterator();
        logFine("---Killing all pools");
        while (pools.hasNext()) {
            ResourcePool pool = (ResourcePool) pools.next();
            if (pool != null) {
                String name = pool.getPoolName();
                try {
                    if (_logger.isLoggable(Level.FINE)){
                        _logger.fine("Now killing pool : " + name);
                    }
                    killPool(name);
                } catch (Exception e) {
                    _logger.fine("Error killing pool : " + name + " :: "
                            + (e.getMessage() != null ? e.getMessage() : " "));
                }
            }
        }
        stopEmbeddedDerby();	
    }

     private void stopEmbeddedDerby(){
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        }
        catch(ClassNotFoundException cnfe){
            _logger.log(Level.FINE, "Derby.Driver.Not.Found", cnfe.fillInStackTrace());
        }
        //Embedded derby, when shutdown will throw this exception in all cases.
        catch(SQLException se){
             _logger.log(Level.FINE, "Derby.Shutdown.Exception", se.fillInStackTrace());
        }
        catch(Exception e){
             _logger.log(Level.FINE, "Derby.Shutdown.Exception", e.fillInStackTrace());
        }
    }


    public void killFreeConnectionsInPools() {
        Iterator pools = poolTable.values().iterator();
        logFine("Killing all free connections in pools");
        while (pools.hasNext()) {
            ResourcePool pool = (ResourcePool) pools.next();
            if (pool != null) {
                String name = pool.getPoolName();
                try {
                    if (name != null) {
                        ResourcePool poolToKill = (ResourcePool)
                                poolTable.get(name);
                        if (poolToKill != null) {
                            pool.emptyFreeConnectionsInPool();
                        }
                        if (_logger.isLoggable(Level.FINE)){
                            _logger.fine("Now killing free connections in pool : " + name);
                        }
                    }
                } catch (Exception e) {
                    _logger.fine("Error killing pool : " + name + " :: "
                            + (e.getMessage() != null ? e.getMessage() : " "));
                }
            }
        }

    }

    /**
     * Use this method if the string being passed does not <br>
     * involve multiple concatenations<br>
     * Avoid using this method in exception-catch blocks as they
     * are not frequently executed <br>
     * @param msg
     */
    private void logFine(String msg) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(msg);
        }
    }

    public ResourcePool getPool( String name ) {
        if ( name == null ) {
            return null;
        }
        return  (ResourcePool)
	    Switch.getSwitch().getPoolManager().getPoolTable().get( name );
	
    }

    public void setSelfManaged( String poolName, boolean flag ) {
        if ( poolName == null ) {
            return;
        }
        getPool( poolName ).setSelfManaged( flag );
    }

    /**
     * This method gets called by the LazyEnlistableConnectionManagerImpl when
     * a connection needs enlistment, i.e on use of a Statement etc.
     */
    public void lazyEnlist( ManagedConnection mc ) throws ResourceException {
        lazyEnlistableResourceManager.lazyEnlist( mc );
    }
    
    /*
     * Called by the InvocationManager at methodEnd. This method
     * will disassociate ManagedConnection instances from Connection
     * handles if the ResourceAdapter supports that.
     */
    public void postInvoke() throws InvocationException 
    {
        J2EETransactionManager tm = Switch.getSwitch().getTransactionManager();
        ComponentInvocation invToUse = 
            Switch.getSwitch().getInvocationManager().getCurrentInvocation();
            
        if ( invToUse == null ) {
            return;
        }

        Object comp = invToUse.getInstance();

        if ( comp == null ) {
            return;
        }

        
        List list = tm.getExistingResourceList(comp, invToUse );
        if (list == null ) {
            //For invocations of asadmin the ComponentInvocation does not
            //have any resources and hence the existingResourcesList is null
            return;
        }
        
        if (list.size() == 0) return;

        ResourceHandle[] handles = (ResourceHandle[])list.toArray( 
            new ResourceHandle[0] );
        for( ResourceHandle h : handles) {
            ResourceSpec spec = h.getResourceSpec();
            if ( spec.isLazyAssociatable() ) {
                //In this case we are assured that the managedConnection is
                //of type DissociatableManagedConnection
                javax.resource.spi.DissociatableManagedConnection mc = 
                    (javax.resource.spi.DissociatableManagedConnection) h.getResource();
                if ( h.isEnlisted() ) {
                    getResourceManager( spec ).delistResource(
                        h, XAResource.TMSUCCESS);
                }
                try {
                    mc.dissociateConnections();
                } catch( ResourceException re ) {
                    InvocationException ie = new InvocationException(
                        re.getMessage() );
                    ie.initCause( re );
                    throw ie;
                } finally {
                    if ( h.getResourceState().isBusy() ) {
                        putbackDirectToPool( h, spec.getConnectionPoolName() );
                    }
                }
                
            }
        }
        
    }
}

