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

import com.sun.enterprise.resource.pool.monitor.telemetry.*;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.resource.listener.PoolLifeCycle;
import com.sun.enterprise.resource.pool.JdbcPoolEmitterImpl;
import com.sun.enterprise.resource.pool.PoolLifeCycleListenerRegistry;
import com.sun.enterprise.resource.pool.PoolLifeCycleRegistry;
import com.sun.enterprise.resource.pool.PoolManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.monitoring.TelemetryProvider;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.provider.ProbeProviderEventManager;
import org.glassfish.flashlight.provider.ProbeProviderListener;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;
import org.glassfish.probe.provider.PluginPoint;
import org.glassfish.probe.provider.StatsProviderManager;

/**
 * Bootstrap operations of telemetry objects are done by this class.
 * Registering of provider, building the monitoring tree for jdbc connection
 * pool are done during the bootstrap. Telemetry objects are created here 
 * based on the pool's lifecycle and (un)registered to the registry.
 * 
 * This is an implementation of PoolLifeCycle. All pool creation or destroy 
 * events are got and based on the type, the tree is built for a pool is pool 
 * is created or tree is removed if pool is destroyed. Register/unregister of the
 * pool is done based on this. Monitoring levels when changed from HIGH-> OFF or
 * OFF->HIGH are taken care and appropriate monitoring levels are set.
 * 
 * @author shalini
 */
@Service(name = "jdbc-connection-pool")
@Scoped(Singleton.class)
public class JDBCPoolStatsProviderBootstrap implements TelemetryProvider,
        PostConstruct, PoolLifeCycle {

    @Inject
    private ProbeProviderEventManager ppem;
    @Inject
    Logger logger;
    @Inject
    private static Domain domain;
    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private ProbeClientMediator pcm;
    @Inject
    private PoolManager poolManager;

    private boolean poolProviderRegistered = false;
    private boolean probeProviderListenerRegistered = false;
    private boolean jdbcPoolMonitoringEnabled = false;
    private boolean isJdbcPoolTreeBuilt = false;
    
    private String monitoringLevel = null;
    
    private TreeNode serverNode;
    private TreeNode jdbcConnPoolNode = null;
    //List of all telemetry objects that are created and stored.
    private List<JDBCPoolStatsProvider> jdbcPoolTMs = null;

    public JDBCPoolStatsProviderBootstrap() {
        registerPoolLifeCycleListener();
        if(!isJdbcPoolTreeBuilt)
            buildJdbcPoolMonitoringTree();
    }

    /**
     * Handle the change in monitoring level of connection pools. 
     * Register the probe provider listener if monitoring is enabled. Set the 
     * appropriate monitoring level according to the new level change.
     * @param newLevel
     */
    public void onLevelChange(String newLevel) {
        
        boolean newLevelEnabledValue = getEnabledValue(newLevel);
        logger.finest("[Monitor] New monitoring level for connection pool " +
                "received : " + newLevel);
        if (jdbcPoolMonitoringEnabled != newLevelEnabledValue) {
            jdbcPoolMonitoringEnabled = newLevelEnabledValue;
        } else {
            // Might have changed from 'LOW' to 'HIGH' or vice-versa. Ignore.
            return;
        }
    }

    public void postConstruct() {
        Level dbgLevel = Level.FINEST;
        Level defaultLevel = logger.getLevel();
        if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            //logger.setLevel(dbgLevel);
        }
        logger.finest("[Monitor]In the JDBCPoolTelemetry bootstrap");
        
    }

    /**
     * Build monitoring tree for jdbc connection pool. Asadmin monitor would look
     * up this tree with get command as server.resources
     */
    private void buildJdbcPoolMonitoringTree() {
        //if (!jdbcPoolMonitoringEnabled) {
        //    return;
        //}
        logger.finest("[Monitor]Jdbc Connection Pool Monitoring tree is being built");
        try {

            //resources sub nodes (pool names)
            jdbcPoolTMs = new ArrayList<JDBCPoolStatsProvider>();

            // Add tree node to jdbcConnPoolNode for all the jdbc connection pools
            List<Resource> resources = domain.getResources().getResources();
            for (Resource resource : resources) {
                if (resource instanceof JdbcConnectionPool) {
                    JdbcConnectionPool pool = (JdbcConnectionPool) resource;
                    String poolName = pool.getName();
                    //Only if the pool is initialized, create the telemetry objects.
                    addAndRegisterTelemetryObject(poolName);
                }
            }
            
            isJdbcPoolTreeBuilt = true;
        } catch (Exception e) {
            isJdbcPoolTreeBuilt = false;
            logger.warning("[Monitor]WARNING: Exception in buildJdbcPoolMonitoringTree : " +
                    e.getLocalizedMessage());
        }
    }

    /**
     * Add the given pool name to the jdbcConnPoolNode by creating a telemetry
     * object and setting the probe listener handle for the same only if the 
     * pool has been initialized.
     * Also registers the pool lifecycle listener.
     * @param poolName
     */
    private boolean addAndRegisterTelemetryObject(String poolName) {
        boolean addedTelemetryObject = false;
        if (poolManager.getPool(poolName) != null) {
            //Found in the pool table (pool has been initialized/created)
            JDBCPoolStatsProvider jdbcPoolTM =
                    new JDBCPoolStatsProvider(poolName, logger);
            StatsProviderManager.register(
                    "jdbc",
                    PluginPoint.SERVER,
                    "resources/" + poolName,
                    new JDBCPoolStatsProvider(poolName, logger));
            
            //this jdbcPoolTM should be registered with the PoolLifeCycleListenerRegistry.
            //poolName and registry will different for each jdbcPoolTM
            String jdbcPoolName = jdbcPoolTM.getJdbcPoolName();
            PoolLifeCycleListenerRegistry poolRegistry = new PoolLifeCycleListenerRegistry(jdbcPoolName);
            poolRegistry.registerPoolLifeCycleListener(new JdbcPoolEmitterImpl(jdbcPoolName));
            jdbcPoolTM.setPoolRegistry(poolRegistry);
            
            //Add to the list of TMs for jdbc monitoring
            jdbcPoolTMs.add(jdbcPoolTM);
            
            addedTelemetryObject = true;
        }
        return addedTelemetryObject;
    }

    /**
     * Register <code> this </code> to PoolLifeCycle so as to listen to 
     * pool's lifecycle events - creation or destroy.
     * Adds <code> this </code> to all connection pools. 
     */
    private void registerPoolLifeCycleListener() {
        PoolLifeCycleRegistry poolLifeCycleRegistry = PoolLifeCycleRegistry.getRegistry();
        List<Resource> resources = domain.getResources().getResources();
        for (Resource resource : resources) {
            if (resource instanceof JdbcConnectionPool) {
                JdbcConnectionPool pool = (JdbcConnectionPool) resource;
                String poolName = pool.getName();
                poolLifeCycleRegistry.registerPoolLifeCycle(poolName, this);
            }
        }
    }
    
    /**
     * When a pool is destroyed, the pool's telemetry object should be removed
     * from the jdbc monitoring tree and unregistered from the pool lifecycle
     * listener event notifications. 
     * Additionally, the server node should no longer reflect this jdbc-connection
     * pool name in it hence removing it from the serverNode.
     * @param poolName
     */
    private void removeAndUnregisterTelemetryObject(String poolName) {
        Iterator i = jdbcPoolTMs.iterator();
        while (i.hasNext()) {
            JDBCPoolStatsProvider jdbcPoolTM = (JDBCPoolStatsProvider) i.next();
            if (poolName.equals(jdbcPoolTM.getJdbcPoolName())) {
                //Found the TM for this poolName
                //TreeNode poolNode = jdbcPoolTM.getJdbcPoolNode();
                //removeProbeListenerHandles(jdbcPoolTM);
                
                //Get registry and unregister this pool from the registry
                PoolLifeCycleListenerRegistry poolRegistry = jdbcPoolTM.getPoolRegistry();
                poolRegistry.unRegisterPoolLifeCycleListener(poolName);
                
                //Remove this iterator from jdbcPoolTMs
                i.remove();
                
                //Remove from JDBC monitoring tree
                //serverNode.removeChild(poolNode);
            }
        }
    }

    /**
     * Find if the monitoring is enabled based on the monitoring level : 
     * <code> strEnabled </code>
     * @param strEnabled
     * @return 
     */
    public boolean getEnabledValue(String strEnabled) {
        if ("OFF".equals(strEnabled)) {
            return false;
        }
        return true;
    }

    public static Domain getDomain() {
        return domain;
    }
    
    /**
     * When a pool is created (or initialized) the pool should be added 
     * to the monitoring tree (server.resources.*). Also, the pool lifecycle
     * listener needs to be registered for this pool to track events on change
     * of any monitoring attributes.
     * @param poolName
     */
    public void poolCreated(String poolName) {
        logger.finest("Pool created : " + poolName);
        if(addAndRegisterTelemetryObject(poolName)) {
            serverNode.addChild(jdbcConnPoolNode);
        }
        
    }

    /**
     * When a pool is destroyed, the pool's telemetry object should be 
     * removed from the monitoring tree. Also, the pool's lifecycle listener
     * should be unregistered.
     * @param poolName
     */
    public void poolDestroyed(String poolName) {
        logger.finest("Pool Destroyed : " + poolName);
        removeAndUnregisterTelemetryObject(poolName);
    }
}
