package com.sun.enterprise.resource.pool;

import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Transaction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

public abstract class AbstractPoolManager implements PoolManager {
    /**
     * Obtain a transactional resource such as JDBC connection
     *
     * @param spec  Specification for the resource
     * @param alloc Allocator for the resource
     * @param info  Client security for this request
     * @return An object that represents a connection to the resource
     * @throws PoolingException Thrown if some error occurs while
     *                          obtaining the resource
     */

    private static Logger _logger = null;

    static {
        _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    }

    public Object getResource(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info) throws PoolingException {
        throw new UnsupportedOperationException();
    }

    public ResourceReferenceDescriptor getResourceReference(String jndiName) {
        throw new UnsupportedOperationException();
    }

    public void resourceEnlisted(Transaction tran, ResourceHandle res) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void resourceClosed(ResourceHandle res) {
        throw new UnsupportedOperationException();
    }

    public void badResourceClosed(ResourceHandle res) {
        throw new UnsupportedOperationException();
    }

    public void resourceErrorOccurred(ResourceHandle res) {
        throw new UnsupportedOperationException();
    }

    public void transactionCompleted(Transaction tran, int status) {
        throw new UnsupportedOperationException();
    }

    public void putbackResourceToPool(ResourceHandle h, boolean errorOccurred) {
        throw new UnsupportedOperationException();
    }

    public void putbackBadResourceToPool(ResourceHandle h) {
        throw new UnsupportedOperationException();
    }

    public void putbackDirectToPool(ResourceHandle h, String poolName) {
        throw new UnsupportedOperationException();
    }

    public ResourceHandle getResourceFromPool(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info, Transaction tran) throws PoolingException {
        throw new UnsupportedOperationException();
    }

    public void registerResource(ResourceHandle resource) throws PoolingException {
        throw new UnsupportedOperationException();
    }

    public void unregisterResource(ResourceHandle resource, int xaresFlag) {
        throw new UnsupportedOperationException();
    }

    public void emptyResourcePool(ResourceSpec spec) {
        throw new UnsupportedOperationException();
    }

    public void killPool(String poolName) {
        throw new UnsupportedOperationException();
    }

    public void reconfigPoolProperties(ConnectorConnectionPool ccp) throws PoolingException {
        throw new UnsupportedOperationException();
    }//sets/resets the monitoring levels for the pool

    public void disableMonitoring(String poolName) {
        throw new UnsupportedOperationException();
    }

    public void setMonitoringEnabledHigh(String poolName) {
        throw new UnsupportedOperationException();
    }

    public void setMonitoringEnabledLow(String poolName) {
        throw new UnsupportedOperationException();
    }//get the pooltable

    public ConcurrentHashMap getPoolTable() {
        throw new UnsupportedOperationException();
    }//register the MonitoringLevelListeners

    public void initializeMonitoring() {
        throw new UnsupportedOperationException();
    }

    public boolean switchOnMatching(String poolName) {
        throw new UnsupportedOperationException();
    }

    public void killAllPools() {
        throw new UnsupportedOperationException();
    }

    public void killFreeConnectionsInPools() {
        throw new UnsupportedOperationException();
    }

    public ResourcePool getPool(String poolName) {
        throw new UnsupportedOperationException();
    }

    public void setSelfManaged(String poolName, boolean flag) {
        throw new UnsupportedOperationException();
    }

    public void lazyEnlist(ManagedConnection mc) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    public void postInvoke() {
        throw new UnsupportedOperationException();
    }

    public ConcurrentHashMap getMonitoredPoolTable() {
        throw new UnsupportedOperationException();
    }
}
