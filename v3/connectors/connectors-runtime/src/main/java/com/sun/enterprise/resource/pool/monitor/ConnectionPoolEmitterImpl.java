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
package com.sun.enterprise.resource.pool.monitor;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;
import com.sun.enterprise.resource.pool.monitor.ConnectorConnPoolProbeProvider;
import com.sun.enterprise.resource.pool.monitor.JdbcConnPoolProbeProvider;

/**
 * Implementation of PoolLifeCycleListener interface to listen to events related
 * to jdbc monitoring. The methods invoke the probe providers internally to 
 * provide the monitoring related information.
 * 
 * @author Shalini M
 */
public class ConnectionPoolEmitterImpl implements PoolLifeCycleListener {
    private String poolName;
    private ConnectorConnPoolProbeProvider connectorConnProbeProvider;
    private JdbcConnPoolProbeProvider jdbcProbeProvider;

    /**
     * Constructor.
     *
     * @param jdbcPool the jdbc connection pool on whose behalf this
     * ConnectionPoolEmitterImpl emits jdbc pool related probe events
     */
    public ConnectionPoolEmitterImpl(String poolName) {
        this.poolName = poolName;
        this.connectorConnProbeProvider = ConnectorRuntime.getRuntime().getConnectorConnPoolProvider();
        this.jdbcProbeProvider = ConnectorRuntime.getRuntime().getJdbcConnPoolProvider();
    }

    /**
     * Fires probe event that a stack trace is to be printed on the server.log.
     * The stack trace is mainly related to connection leak tracing for the 
     * given jdbc connection pool.
     * @param stackTrace
     */
    public void toString(StringBuffer stackTrace) {
        stackTrace.append("\n Monitoring Statistics for \n" + poolName);
        connectorConnProbeProvider.toString(poolName, stackTrace);
        jdbcProbeProvider.toString(poolName, stackTrace);
    }
    
    /**
     * Fires probe event that a connection has been acquired by the application 
     * for the given jdbc connection pool.
     */
    public void connectionAcquired() {
        connectorConnProbeProvider.connectionAcquiredEvent(poolName);
        jdbcProbeProvider.connectionAcquiredEvent(poolName);
    }

    /**
     * Fires probe event related to the fact that a connection request is served
     * in the time <code>timeTakenInMillis</code> for the given jdbc connection 
     * pool.
     * 
     * @param timeTakenInMillis time taken to serve a connection
     */    
    public void connectionRequestServed(long timeTakenInMillis) {
        connectorConnProbeProvider.connectionRequestServedEvent(poolName, timeTakenInMillis);
        jdbcProbeProvider.connectionRequestServedEvent(poolName, timeTakenInMillis);
    }

    /**
     * Fires probe event related to the fact that the given jdbc connection pool
     * has got a connection timed-out event.
     */
    public void connectionTimedOut() {
        connectorConnProbeProvider.connectionTimedOutEvent(poolName);
        jdbcProbeProvider.connectionTimedOutEvent(poolName);
    }

    public void connectionNotMatched() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void connectionMatched() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Fires probe event that a connection is destroyed for the 
     * given jdbc connection pool.
     */
    public void connectionDestroyed() {
        connectorConnProbeProvider.connectionDestroyedEvent(poolName);
        jdbcProbeProvider.connectionDestroyedEvent(poolName);
    }

    /**
     * Fires probe event that a connection is released for the given jdbc
     * connection pool.
     */
    public void connectionReleased() {
        connectorConnProbeProvider.connectionReleasedEvent(poolName);
        jdbcProbeProvider.connectionReleasedEvent(poolName);
    }

    /**
     * Fires probe event that a connection is created for the given jdbc
     * connection pool.
     */
    public void connectionCreated() {
        connectorConnProbeProvider.connectionCreatedEvent(poolName);
        jdbcProbeProvider.connectionCreatedEvent(poolName);
    }
    
    /**
     * Fires probe event related to the fact that the given jdbc connection pool
     * has got a connection leak event.
     *
     */
    public void foundPotentialConnectionLeak() {
        connectorConnProbeProvider.potentialConnLeakEvent(poolName);
        jdbcProbeProvider.potentialConnLeakEvent(poolName);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection validation failed event.
     * 
     * @param count number of times the validation failed
     */
    public void connectionValidationFailed(int count) {
        connectorConnProbeProvider.connectionValidationFailedEvent(poolName, count);
        jdbcProbeProvider.connectionValidationFailedEvent(poolName, count);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection used event.
     */
    public void connectionUsed() {
        connectorConnProbeProvider.connectionUsedEvent(poolName);
        jdbcProbeProvider.connectionUsedEvent(poolName);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection freed event.
     * 
     * @param count number of connections freed to pool
     */
    public void connectionsFreed(int count) {
        connectorConnProbeProvider.connectionsFreedEvent(poolName, count);
        jdbcProbeProvider.connectionsFreedEvent(poolName, count);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement connection used event.
     * 
     * @param beingDestroyed if the connection is destroyed due to error
     * @param steadyPoolSize 
     */
    public void decrementConnectionUsed(boolean beingDestroyed, int steadyPoolSize) {
        connectorConnProbeProvider.decrementConnectionUsedEvent(poolName, beingDestroyed, steadyPoolSize);
        jdbcProbeProvider.decrementConnectionUsedEvent(poolName, beingDestroyed, steadyPoolSize);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement free connections size event.
     * 
     * @param steadyPoolSize 
     */
    public void decrementFreeConnectionsSize(int steadyPoolSize) {
        connectorConnProbeProvider.decrementFreeConnectionsSizeEvent(poolName, steadyPoolSize);
        jdbcProbeProvider.decrementFreeConnectionsSizeEvent(poolName, steadyPoolSize);
    }
}
