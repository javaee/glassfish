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
package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.rm.*;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocationHandler;
import com.sun.enterprise.connectors.ConnectorConnectionPool;

import com.sun.enterprise.resource.listener.PoolLifeCycle;
import javax.transaction.Transaction;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import javax.resource.spi.ManagedConnection;
import javax.resource.ResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tony Ng, Aditya Gore
 */
@Service
public class PoolManagerImpl extends AbstractPoolManager implements ComponentInvocationHandler {

    private ConcurrentHashMap<String, ResourcePool> poolTable;

    private ResourceManager resourceManager;
    private ResourceManager sysResourceManager;
    private ResourceManager noTxResourceManager;
    private LazyEnlistableResourceManagerImpl lazyEnlistableResourceManager;

    private static Logger _logger = null;

    @Inject
    private Habitat connectorRuntimeHabitat;

    private ConnectorRuntime runtime;

    static {
        _logger = LogDomains.getLogger(PoolManagerImpl.class, LogDomains.RSR_LOGGER);
    }
    private PoolLifeCycle listener;

    public PoolManagerImpl() {
        this.poolTable = new ConcurrentHashMap<String, ResourcePool>();
        resourceManager = new ResourceManagerImpl();
        sysResourceManager = new SystemResourceManagerImpl();
        noTxResourceManager = new NoTxResourceManagerImpl();
        lazyEnlistableResourceManager = new LazyEnlistableResourceManagerImpl();
    }

    public void createEmptyConnectionPool(String poolName,
                                          PoolType pt) throws PoolingException {
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
     * @param pt       - PoolType
     * @return ResourcePool - newly created pool
     * @throws PoolingException when unable to create/initialize pool
     */
    private ResourcePool createAndInitPool(final String poolName, PoolType pt)
            throws PoolingException {
        ResourcePool pool = getPool(poolName);
        if (pool == null) {
            pool = ResourcePoolFactoryImpl.newInstance(poolName, pt);
            addPool(pool);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Created connection  pool  and added it to PoolManager :" + pool);
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
            tran = getResourceManager(spec).getTransaction();
        }

        ResourceHandle handle =
                getResourceFromPool(spec, alloc, info, tran);

        if (!handle.supportsLazyAssociation()) {
            spec.setLazyAssociatable(false);
        }

        if (spec.isLazyAssociatable() &&
                spec.getConnectionToAssociate() != null) {
            //If getConnectionToAssociate returns a connection that means
            //we need to associate a new connection with it
            try {
                Object connection = spec.getConnectionToAssociate();
                ManagedConnection dmc
                        = (ManagedConnection) handle.getResource();
                dmc.associateConnection(connection);
            } catch (ResourceException e) {
                putbackDirectToPool(handle, spec.getConnectionPoolName());
                PoolingException pe = new PoolingException(
                        e.getMessage());
                pe.initCause(e);
                throw pe;
            }
        }

        //If the ResourceAdapter does not support lazy enlistment
        //we cannot either
        if (!handle.supportsLazyEnlistment()) {
            spec.setLazyEnlistable(false);
        }

        handle.setResourceSpec(spec);

        try {
            if (handle.getResourceState().isUnenlisted()) {
                //The spec being used here is the spec with the updated
                //lazy enlistment info
                //Here's the real place where we care about the correct 
                //resource manager (which in turn depends upon the ResourceSpec)
                //and that's because if lazy enlistment needs to be done
                //we need to get the LazyEnlistableResourceManager
                getResourceManager(spec).enlistResource(handle);
            }
        } catch (Exception e) {
            //In the rare cases where enlistResource throws exception, we
            //should return the resource to the pool
            putbackDirectToPool(handle, spec.getConnectionPoolName());
            _logger.log(Level.WARNING, "poolmgr.err_enlisting_res_in_getconn",
                    spec.getConnectionPoolName());
            logFine("rm.enlistResource threw Exception. Returning resource to pool");
            //and rethrow the exception
            throw new PoolingException(e);

        }

        return handle.getUserConnection();
    }

    public void putbackDirectToPool(ResourceHandle h, String poolName) {
        // notify pool
        if (poolName != null) {
            ResourcePool pool = poolTable.get(poolName);
            if (pool != null) {
                pool.resourceClosed(h);
            }
        }
    }

    public ResourceHandle getResourceFromPool(ResourceSpec spec, ResourceAllocator alloc,
                                              ClientSecurityInfo info, Transaction tran)
            throws PoolingException {
        ResourcePool pool = getPool(spec.getConnectionPoolName());
        // pool.getResource() has been modified to:
        //      - be able to create new resource if needed
        //      - block the caller until a resource is acquired or
        //              the max-wait-time expires
        return pool.getResource(spec, alloc, tran);
    }

    /**
     * Switch on matching in the pool.
     *
     * @param poolName Name of the pool
     */
    public boolean switchOnMatching(String poolName) {
        ResourcePool pool = (ResourcePool) getPoolTable().get(poolName);

        if (pool != null) {
            pool.switchOnMatching();
            return true;
        } else {
            return false;
        }
    }


    public ConcurrentHashMap getPoolTable() {
        return poolTable;
    }


    private void addPool(ResourcePool pool) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Adding pool " + pool.getPoolName() + "to pooltable");
        }
        synchronized (poolTable) {
            poolTable.put(pool.getPoolName(), pool);
        }
    }


    private ResourceManager getResourceManager(ResourceSpec spec) {
        if (spec.isNonTx()) {
            logFine("Returning noTxResourceManager");
            return noTxResourceManager;
        } else if (spec.isPM()) {
            logFine("Returning sysResourceManager");
            return sysResourceManager;
        } else if (spec.isLazyEnlistable()) {
            logFine("Returning LazyEnlistableResourceManager");
            return lazyEnlistableResourceManager;
        } else {
            logFine("Returning resourceManager");
            return resourceManager;
        }
    }

    private void addSyncListener(Transaction tran) {
        Synchronization sync = new SynchronizationListener(tran);
        try {
            tran.registerSynchronization(sync);
        } catch (Exception ex) {
            _logger.fine("Error adding syncListener : " +
                    (ex.getMessage() != null ? ex.getMessage() : " "));
        }
    }

    // called by EJB Transaction Manager
    public void transactionCompleted(Transaction tran, int status)
            throws IllegalStateException {

        Iterator iter = ((JavaEETransaction) tran).getAllParticipatingPools().iterator();
        while (iter.hasNext()) {
            ResourcePool pool = getPool((String) iter.next());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("calling transactionCompleted on " + pool.getPoolName());
            }
            pool.transactionCompleted(tran, status);
        }
    }

    public void resourceEnlisted(Transaction tran, com.sun.appserv.connectors.internal.api.ResourceHandle h)
            throws IllegalStateException {
        ResourceHandle res = (ResourceHandle) h;

        String poolName = res.getResourceSpec().getConnectionPoolName();
        try {
            JavaEETransaction j2eeTran = (JavaEETransaction) tran;
            if (poolName != null && j2eeTran.getResources(poolName) == null) {
                addSyncListener(tran);
            }
        } catch (ClassCastException e) {
            addSyncListener(tran);
        }
        if (poolName != null) {
            ResourcePool pool = getPool(poolName);
            if (pool != null) {
                pool.resourceEnlisted(tran, res);
            }
        }
    }

    /**
     * This method gets called by the LazyEnlistableConnectionManagerImpl when
     * a connection needs enlistment, i.e on use of a Statement etc.
     */
    public void lazyEnlist(ManagedConnection mc) throws ResourceException {
        lazyEnlistableResourceManager.lazyEnlist(mc);
    }


    private ConnectorRuntime getConnectorRuntime() {
        //TODO V3 not synchronized
        if(runtime == null){
            runtime = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);
        }
        return runtime;
    }

    public void registerResource(com.sun.appserv.connectors.internal.api.ResourceHandle handle) throws PoolingException {
        ResourceHandle h = (ResourceHandle)handle;
        ResourceManager rm = getResourceManager(h.getResourceSpec());
        rm.registerResource(h);
    }

    public void registerPoolLifeCycleListener(PoolLifeCycle poolListener) {
        listener = poolListener;
    }

    public void unregisterPoolLifeCycleListener() {
        listener = null;
    }
    
    public void unregisterResource(com.sun.appserv.connectors.internal.api.ResourceHandle resource, int xaresFlag) {
        ResourceHandle h = (ResourceHandle)resource;
        ResourceManager rm = getResourceManager(h.getResourceSpec());
        rm.unregisterResource(h, xaresFlag);
    }

    public void resourceClosed(ResourceHandle resource) {
        ResourceManager rm = getResourceManager(resource.getResourceSpec());
        rm.delistResource(resource, XAResource.TMSUCCESS);
        putbackResourceToPool(resource, false);
    }

    public void badResourceClosed(ResourceHandle resource) {
        ResourceManager rm = getResourceManager(resource.getResourceSpec());
        rm.delistResource(resource, XAResource.TMSUCCESS);
        putbackBadResourceToPool(resource);
    }

    public void resourceErrorOccurred(ResourceHandle resource) {
        putbackResourceToPool(resource, true);
    }

    public void putbackBadResourceToPool(ResourceHandle h) {

        // cleanup resource
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
        if (poolName != null) {
            ResourcePool pool = poolTable.get(poolName);
            if (pool != null) {
                if (errorOccurred) {
                    pool.resourceErrorOccurred(h);
                } else {
                    pool.resourceClosed(h);
                }
            }
        }
    }


    /**
     * Use this method if the string being passed does not <br>
     * involve multiple concatenations<br>
     * Avoid using this method in exception-catch blocks as they
     * are not frequently executed <br>
     *
     * @param msg String
     */
    private void logFine(String msg) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(msg);
        }
    }

    public ResourcePool getPool(String name) {
        if (name == null) {
            return null;
        }
        return poolTable.get(name);
    }

    /**
     * Kill the pool with the specified pool name
     *
     * @param poolName - The name of the pool to kill
     */
    public void killPool(String poolName) {

        //empty the pool
        //and remove from poolTable
        ResourcePool pool = poolTable.get(poolName);
        if (pool != null) {
            pool.cancelResizerTask();
            pool.emptyPool();
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Removing pool " + pool + " from pooltable");
            }
            synchronized (poolTable) {
                poolTable.remove(poolName);

            }
            if (listener != null)
                listener.poolDestroyed(poolName);
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
                           ResourcePool poolToKill = poolTable.get(name);
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
    
    public ResourceReferenceDescriptor getResourceReference(String jndiName) {
        Set descriptors = getConnectorRuntime().getResourceReferenceDescriptor();

        if (descriptors != null) {
            for (Object descriptor : descriptors) {
                ResourceReferenceDescriptor ref =
                        (ResourceReferenceDescriptor) descriptor;
                String name = ref.getJndiName();
                if (jndiName.equals(name)) {
                    return ref;
                }
            }
        }
        return null;
    }

    public void beforePreInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                                ComponentInvocation newInv) throws InvocationException {
        //no-op
    }

    public void afterPreInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                               ComponentInvocation curInv) throws InvocationException {
        //no-op
    }

    public void beforePostInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                                 ComponentInvocation curInv) throws InvocationException {
        //no-op
    }

    /*
    * Called by the InvocationManager at methodEnd. This method
    * will disassociate ManagedConnection instances from Connection
    * handles if the ResourceAdapter supports that.
    */
    public void afterPostInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                                ComponentInvocation curInv) throws InvocationException {
        postInvoke(curInv);
    }

    private void postInvoke(ComponentInvocation curInv){

        ComponentInvocation invToUse = curInv;
/*
        if(invToUse == null){
            invToUse = getConnectorRuntime().getInvocationManager().getCurrentInvocation();
        }
*/
        if (invToUse == null) {
            return;
        }

        Object comp = invToUse.getInstance();

        if (comp == null) {
            return;
        }

        handleLazilyAssociatedConnectionPools(comp, invToUse);
    }

    /**
     * If the connections associated with the component are lazily-associatable, dissociate them.
     * @param comp Component that acquired connections
     * @param invToUse component invocation
     */
    private void handleLazilyAssociatedConnectionPools(Object comp, ComponentInvocation invToUse) {
        JavaEETransactionManager tm = getConnectorRuntime().getTransactionManager();
        List list = tm.getExistingResourceList(comp, invToUse);
        if (list == null) {
            //For invocations of asadmin the ComponentInvocation does not
            //have any resources and hence the existingResourcesList is null
            return;
        }

        if (list.size() == 0) return;

        ResourceHandle[] handles = (ResourceHandle[]) list.toArray(
                new ResourceHandle[0]);
        for (ResourceHandle h : handles) {
            ResourceSpec spec = h.getResourceSpec();
            if (spec.isLazyAssociatable()) {
                //In this case we are assured that the managedConnection is
                //of type DissociatableManagedConnection
                javax.resource.spi.DissociatableManagedConnection mc =
                        (javax.resource.spi.DissociatableManagedConnection) h.getResource();
                if (h.isEnlisted()) {
                    getResourceManager(spec).delistResource(
                            h, XAResource.TMSUCCESS);
                }
                try {
                    mc.dissociateConnections();
                } catch (ResourceException re) {
                    InvocationException ie = new InvocationException(
                            re.getMessage());
                    ie.initCause(re);
                    throw ie;
                } finally {
                    if (h.getResourceState().isBusy()) {
                        putbackDirectToPool(h, spec.getConnectionPoolName());
                    }
                }
            }
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
                _logger.fine("Exception in afterCompletion : " +
                        (ex.getMessage() != null ? ex.getMessage() : " "));
            }
        }

        public void beforeCompletion() {
            // do nothing
        }
    }

    public void reconfigPoolProperties(ConnectorConnectionPool ccp) throws PoolingException {
        String poolName = ccp.getName();
        ResourcePool pool = (ResourcePool) getPoolTable().get( poolName );

        if (pool != null ) {
            pool.reconfigPoolProperties( ccp );
        }        
    }

    /**
     * Flush Connection pool by reinitializing the connections 
     * established in the pool.
     * @param poolName
     * @throws com.sun.appserv.connectors.internal.api.PoolingException
     */
    public boolean flushConnectionPool(String poolName) throws PoolingException {
        boolean result = false;
        ResourcePool pool = (ResourcePool) getPoolTable().get( poolName );
        
        if(pool != null) {
            result = pool.flushConnectionPool();
        } else {
            _logger.log(Level.WARNING, "poolmgr.flush_noop_pool_not_initialized", poolName);
            throw new PoolingException("Flush Connection Pool for pool " + 
                    poolName + " failed. Please see server.log for more details.");            
        }
        return result;
    }

    /**
     * Get connection pool status.
     * @param poolName
     * @return
     */
    public PoolStatus getPoolStatus(String poolName) {
        ResourcePool pool = (ResourcePool) poolTable.get(poolName);
        return pool.getPoolStatus();
    }
}
