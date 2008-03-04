package com.sun.enterprise.resource.pool;

import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.spi.PoolingException;

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

    public void badResourceClosed(ResourceHandle res) {
        throw new UnsupportedOperationException();
    }

    public void resourceErrorOccurred(ResourceHandle res) {
        throw new UnsupportedOperationException();
    }


    public void putbackBadResourceToPool(ResourceHandle h) {
        throw new UnsupportedOperationException();
    }

    public void emptyResourcePool(ResourceSpec spec) {
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

   //register the MonitoringLevelListeners
    public void initializeMonitoring() {
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


    public ConcurrentHashMap getMonitoredPoolTable() {
        throw new UnsupportedOperationException();
    }
}
