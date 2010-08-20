/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import org.glassfish.resource.common.PoolInfo;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;
import com.sun.logging.LogDomains;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;

/**
 * Implementation of PoolLifeCycleListener interface to listen to events related
 * to jdbc monitoring. The methods invoke the probe providers internally to 
 * provide the monitoring related information.
 * 
 * @author Shalini M
 */
public class ConnectionPoolEmitterImpl implements PoolLifeCycleListener {
    private String poolName;
    //TODO ASR check all methods for poolName usage which is incorrect
    private PoolInfo poolInfo;
    private ConnectionPoolProbeProvider poolProbeProvider;
    //Map of app names and respective emitters for a pool.
    private Map<PoolInfo, Map<String, ConnectionPoolAppEmitterImpl>> appStatsMap = null;
    //Map of app names for a resource handle id
    private Map<Long, String> resourceAppAssociationMap;
    private static Logger _logger = LogDomains.getLogger(ConnectionPoolEmitterImpl.class,
            LogDomains.RSR_LOGGER);
    private List<JdbcConnPoolAppStatsProvider> jdbcPoolAppStatsProviders = null;
    private List<ConnectorConnPoolAppStatsProvider> ccPoolAppStatsProviders = null;
    private ConnectorRuntime runtime;

    /**
     * Constructor.
     *
     * @param jdbcPool the jdbc connection pool on whose behalf this
     * ConnectionPoolEmitterImpl emits jdbc pool related probe events
     */
    public ConnectionPoolEmitterImpl(PoolInfo poolInfo, ConnectionPoolProbeProvider provider) {
        this.poolInfo = poolInfo;
        this.poolName = poolInfo.getName();
        this.poolProbeProvider = provider;
        this.jdbcPoolAppStatsProviders = new ArrayList<JdbcConnPoolAppStatsProvider>();
        this.ccPoolAppStatsProviders = new ArrayList<ConnectorConnPoolAppStatsProvider>();
        this.appStatsMap = new HashMap<PoolInfo, Map<String, ConnectionPoolAppEmitterImpl>>();
        this.resourceAppAssociationMap = new HashMap<Long, String>();
        runtime = ConnectorRuntime.getRuntime();
    }

    /**
     * Fires probe event that a stack trace is to be printed on the server.log.
     * The stack trace is mainly related to connection leak tracing for the 
     * given jdbc connection pool.
     * @param stackTrace
     */
    public void toString(StringBuffer stackTrace) {
        stackTrace.append("\n Monitoring Statistics for \n" + poolName);
        poolProbeProvider.toString(poolName, stackTrace);
    }
    
    /**
     * Fires probe event that a connection has been acquired by the application 
     * for the given jdbc connection pool.
     */
    public void connectionAcquired(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter =
                detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.connectionAcquiredEvent(poolName);
        if(appEmitter != null) {
            appEmitter.connectionAcquired();
        }
    }

    /**
     * Fires probe event related to the fact that a connection request is served
     * in the time <code>timeTakenInMillis</code> for the given jdbc connection 
     * pool.
     * 
     * @param timeTakenInMillis time taken to serve a connection
     */    
    public void connectionRequestServed(long timeTakenInMillis) {
        poolProbeProvider.connectionRequestServedEvent(poolName, timeTakenInMillis);
    }

    /**
     * Fires probe event related to the fact that the given jdbc connection pool
     * has got a connection timed-out event.
     */
    public void connectionTimedOut() {
        poolProbeProvider.connectionTimedOutEvent(poolName);
    }

    /**
     * Fires probe event that a connection under test does not match the 
     * current request for the given jdbc connection pool.
     */
    public void connectionNotMatched() {
        poolProbeProvider.connectionNotMatchedEvent(poolName);        
    }

    /**
     * Fires probe event that a connection under test matches the current
     * request for the given jdbc connection pool.
     */
    public void connectionMatched() {
        poolProbeProvider.connectionMatchedEvent(poolName);        
    }

    /**
     * Fires probe event that a connection is destroyed for the 
     * given jdbc connection pool.
     */
    public void connectionDestroyed(long resourceHandleId) {
        poolProbeProvider.connectionDestroyedEvent(poolName);
        //Clearing the resource handle id appName mappings stored
        resourceAppAssociationMap.remove(resourceHandleId);
    }

    /**
     * Fires probe event that a connection is released for the given jdbc
     * connection pool.
     */
    public void connectionReleased(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter =
                detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.connectionReleasedEvent(poolName);
        if(appEmitter != null) {
            appEmitter.connectionReleased();
        }        
    }

    /**
     * Fires probe event that a connection is created for the given jdbc
     * connection pool.
     */
    public void connectionCreated() {
        poolProbeProvider.connectionCreatedEvent(poolName);
    }
    
    /**
     * Fires probe event related to the fact that the given jdbc connection pool
     * has got a connection leak event.
     *
     */
    public void foundPotentialConnectionLeak() {
        poolProbeProvider.potentialConnLeakEvent(poolName);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection validation failed event.
     * 
     * @param count number of times the validation failed
     */
    public void connectionValidationFailed(int count) {
        poolProbeProvider.connectionValidationFailedEvent(poolName, count);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection used event.
     */
    public void connectionUsed(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter =
                detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.connectionUsedEvent(poolName);
        if (appEmitter != null) {
            appEmitter.connectionUsed();
        }
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection freed event.
     * 
     * @param count number of connections freed to pool
     */
    public void connectionsFreed(int count) {
        poolProbeProvider.connectionsFreedEvent(poolName, count);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement connection used event.
     * 
     */
    public void decrementConnectionUsed(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter =
                detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.decrementConnectionUsedEvent(poolName);
        if(appEmitter != null) {
            appEmitter.decrementConnectionUsed();
        }
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement free connections size event.
     * 
     */
    public void decrementNumConnFree() {
        poolProbeProvider.decrementNumConnFreeEvent(poolName);
    }
    
    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement free connections size event.
     * 
     * @param beingDestroyed if the connection is destroyed due to error
     * @param steadyPoolSize 
     */
    public void incrementNumConnFree(boolean beingDestroyed, int steadyPoolSize) {
        poolProbeProvider.incrementNumConnFreeEvent(poolName, beingDestroyed, steadyPoolSize);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool's 
     * wait queue length has been incremented
     * 
     */
    public void connectionRequestQueued() {
        poolProbeProvider.connectionRequestQueuedEvent(poolName);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool's 
     * wait queue length has been decremented.
     * 
     */
    public void connectionRequestDequeued() {
        poolProbeProvider.connectionRequestDequeuedEvent(poolName);
    }

    private String getAppName(long resourceHandleId) {
        Context ic = null;
        String appName = null;
        try {
            ic = new InitialContext();
            appName = (String) ic.lookup("java:app/AppName");
        } catch (NamingException ex) {
            _logger.log(Level.FINE, "Unable to get application name using "
                    + "java:app/AppName method");
            appName = resourceAppAssociationMap.remove(resourceHandleId);
        }
        resourceAppAssociationMap.put(resourceHandleId, appName);
        
        return appName;
    }

    /**
     * Detect if a Stats Provider has already been registered to the
     * monitoring framework for this appName and if so, return the specific
     * emitter. If not already registered, create and register the
     * Stats Provider object to the monitoring framework and add to the list
     * of emitters.
     *
     * @param appName
     * @return
     */
    private ConnectionPoolAppEmitterImpl detectAppBasedProviders(String appName) {

        ConnectionPoolAppProbeProvider probeAppProvider = null;
        ConnectionPoolAppEmitterImpl connPoolAppEmitter = null;

        if (appName == null) {
            //Case when appname cannot be detected. Emitter cannot exist for
            //a null appName for any pool.
            return null;
        }

        if (appStatsMap.containsKey(poolInfo)) {
            //Some apps have been registered for this pool.
            //Find if this appName is already registered.
            //All appEmitters for this pool
            Map<String, ConnectionPoolAppEmitterImpl> appEmitters = appStatsMap.get(poolInfo);
            //Check if the appEmitters list has an emitter for the appName.
            ConnectionPoolAppEmitterImpl emitter = appEmitters.get(appName);
            if(emitter != null) {
                //This appName has already been registered to StatsProviderManager
                return emitter;
            } else {
                if (!ConnectorsUtil.isApplicationScopedResource(poolInfo)) {
                    //register to the StatsProviderManager and add to the list.
                    probeAppProvider = registerConnectionPool(appName);
                    connPoolAppEmitter = addToList(appName, probeAppProvider,
                            appEmitters);
                }
            }
        } else {
            if (!ConnectorsUtil.isApplicationScopedResource(poolInfo)) {
                //Does not contain any app providers associated with this poolname
                //Create a map of app emitters for the appName and add them to the
                //appStatsMap
                probeAppProvider = registerConnectionPool(appName);
                Map<String, ConnectionPoolAppEmitterImpl> appEmitters =
                        new HashMap<String, ConnectionPoolAppEmitterImpl>();
                connPoolAppEmitter = addToList(appName, probeAppProvider, appEmitters);
            }
        }
        return connPoolAppEmitter;
    }

    /**
     * Register the jdbc/connector connection pool Stats Provider object to the
     * monitoring framework under the specific application name monitoring
     * sub tree.
     *
     * @param appName
     * @return
     */
    private ConnectionPoolAppProbeProvider registerConnectionPool(String appName) {
        ConnectionPoolAppProbeProvider probeAppProvider = null;
        ResourcePool pool = runtime.getConnectionPoolConfig(poolInfo);
        if (pool instanceof JdbcConnectionPool) {
            probeAppProvider = new JdbcConnPoolAppProbeProvider();
            JdbcConnPoolAppStatsProvider jdbcPoolAppStatsProvider =
                    new JdbcConnPoolAppStatsProvider(poolInfo, appName);
            StatsProviderManager.register(
                    ContainerMonitoring.JDBC_CONNECTION_POOL,
                    PluginPoint.SERVER,
                    "resources/" + poolName + "/" + appName, jdbcPoolAppStatsProvider);
            jdbcPoolAppStatsProviders.add(jdbcPoolAppStatsProvider);
        } else if (pool instanceof ConnectorConnectionPool) {
            probeAppProvider = new ConnectorConnPoolAppProbeProvider();
            ConnectorConnPoolAppStatsProvider ccPoolAppStatsProvider =
                    new ConnectorConnPoolAppStatsProvider(poolInfo, appName);
            StatsProviderManager.register(
                    ContainerMonitoring.CONNECTOR_CONNECTION_POOL,
                    PluginPoint.SERVER,
                    "resources/" + poolName + "/" + appName, ccPoolAppStatsProvider);
            ccPoolAppStatsProviders.add(ccPoolAppStatsProvider);
        }
        return probeAppProvider;
    }

    /**
     * Add to the pool emitters list. the connection pool application emitter
     * for the specific poolInfo and appName.
     * @param appName
     * @param probeAppProvider
     * @param appEmitters
     * @return
     */
    private ConnectionPoolAppEmitterImpl addToList(String appName,
            ConnectionPoolAppProbeProvider probeAppProvider,
            Map<String, ConnectionPoolAppEmitterImpl> appEmitters) {
        ConnectionPoolAppEmitterImpl connPoolAppEmitter = null;
        if (probeAppProvider != null) {
            //Add the newly created probe provider to the list.
            connPoolAppEmitter = new ConnectionPoolAppEmitterImpl(poolName,
                    appName, probeAppProvider);
            appEmitters.put(appName, connPoolAppEmitter);
            appStatsMap.put(poolInfo, appEmitters);
        }
        runtime.getProbeProviderUtil().
                getConnPoolBootstrap().addToPoolEmitters(poolInfo, this);
        return connPoolAppEmitter;
    }

    /**
     * Unregister the AppStatsProviders registered for this connection pool.
     */
    public void unregisterAppStatsProviders() {
        Iterator jdbcProviders = jdbcPoolAppStatsProviders.iterator();
        while (jdbcProviders.hasNext()) {
            JdbcConnPoolAppStatsProvider jdbcPoolAppStatsProvider =
                    (JdbcConnPoolAppStatsProvider) jdbcProviders.next();
            StatsProviderManager.unregister(jdbcPoolAppStatsProvider);
        }
        Iterator ccProviders = ccPoolAppStatsProviders.iterator();
        while (ccProviders.hasNext()) {
            ConnectorConnPoolAppStatsProvider ccPoolAppStatsProvider =
                    (ConnectorConnPoolAppStatsProvider) ccProviders.next();
            StatsProviderManager.unregister(ccPoolAppStatsProvider);
        }
        jdbcPoolAppStatsProviders.clear();
        ccPoolAppStatsProviders.clear();
    }
}
