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

import com.sun.appserv.connectors.spi.ConnectorConstants.PoolType;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.rm.NoTxResourceManagerImpl;
import com.sun.enterprise.resource.rm.ResourceManager;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.annotations.Service;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

/**
 * @author Tony Ng, Aditya Gore
 */
@Service
public class PoolManagerImpl extends AbstractPoolManager {

    private ConcurrentHashMap<String, ResourcePool> poolTable;
    private ResourceManager noTxResourceManager;
    private static Logger _logger = null;

    static {
        _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    }

    public PoolManagerImpl() {
        this.poolTable = new ConcurrentHashMap<String, ResourcePool>();
        noTxResourceManager = new NoTxResourceManagerImpl();
    }

    public void createEmptyConnectionPool(String poolName,
                                          PoolType pt) throws PoolingException {
        //Create and initialise the connection pool
        createAndInitPool(poolName, pt);
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
            _logger.log(Level.WARNING, "poolmgr.err_enlisting_res_in_getconn");
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
	ResourcePool pool = (ResourcePool) getPoolTable().get( poolName );

	if (pool != null ) {
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
        }
        return noTxResourceManager;
    }

    public void resourceClosed(ResourceHandle resource) {
        ResourceManager rm = getResourceManager(resource.getResourceSpec());
        rm.delistResource(resource, XAResource.TMSUCCESS);
        putbackResourceToPool(resource, false);
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
        }
    }

    public ResourceReferenceDescriptor getResourceReference(String jndiName) {
        Set descriptors = ConnectorRuntime.getRuntime().getResourceReferenceDescriptor();

        for (Object descriptor : descriptors) {
            ResourceReferenceDescriptor ref =
                    (ResourceReferenceDescriptor) descriptor;
            String name = ref.getJndiName();
            if (jndiName.equals(name)) {
                return ref;
            }
        }
        return null;
    }
}

