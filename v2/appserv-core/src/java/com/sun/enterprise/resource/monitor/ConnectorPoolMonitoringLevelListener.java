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
package com.sun.enterprise.resource.monitor;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.j2ee.statistics.Stats;

import com.sun.enterprise.PoolManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.resource.MonitorableResourcePool;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.server.ServerContext;
import com.sun.logging.LogDomains;

import com.sun.enterprise.resource.ResourcePool;

/**
 * Provides an implementation of the MonitoringLevelListener interface to
 * receive callbacks from admin regarding change in the monitoring level.
 * Though there are 3 monitoring levels defined by JSR77, we support
 * only 2 levels - OFF and ON (HIGH/LOW). So essentially, HIGH and LOW
 * for us is only ON
 *
 * @author Aditya Gore
 * @since s1aspe 8.0
 */
//cleaned up - removed unnecessary/commented code.
public class ConnectorPoolMonitoringLevelListener implements MonitoringLevelListener {

    private MonitoringRegistry registry_;

    private static final MonitoringLevel OFF = MonitoringLevel.OFF;
    private static final MonitoringLevel HIGH = MonitoringLevel.HIGH;
    private static final MonitoringLevel LOW = MonitoringLevel.LOW;

    private static Logger _logger = LogDomains.getLogger( 
        LogDomains.RSR_LOGGER );

    public ConnectorPoolMonitoringLevelListener() {
    }

    /**
     * @see com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener#setLevel(com.sun.enterprise.admin.monitor.registry.MonitoringLevel)
     * @deprecated
     */
    //remove after admin removes it
    public void setLevel( MonitoringLevel level ) {}
    
    /**
     * This is the callback invoked by the MonitoringRegistry
     * on change in monitoring level
     * 
     * @param from - the past level
     * @param to - the new level
     * @param handback - the Stats object this listener was registered for
     * @deprecated 
     */
    public void changeLevel( MonitoringLevel from, MonitoringLevel to,
        Stats handback ) {}

    public void changeLevel(MonitoringLevel from, MonitoringLevel to, 
            MonitoredObjectType type) {
        
    	//If we were called, the MonitoredObjectType is definitely
    	//CONNECTOR_CONNECTION_POOL, so we can safely ignore it

        if ( from == to ) {
            //Its a no-op, so return
            return;
        }

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
    	        ServerContext ctxt = ApplicationServer.getServerContext();
                if (ctxt != null ) {
                    registry_ = ctxt.getMonitoringRegistry();
                }
                return null;
            }
        });

        if (from == OFF || from == LOW ) {
            if ( to == HIGH ) {
                logFine("Changing level from " + from +"  to HIGH");	
                transitionToHigh();
            }
        }
    
        if (from == HIGH  || from == LOW ) {
            if ( to == OFF ) {
                logFine("Switching level from " + from + " to OFF");
                switchOffMonitoring();
            }
        }

    	if (from == OFF || from == HIGH )  {
	    if ( to == LOW  ) {
		logFine("Changing level from " + from + " to LOW");	
                transitionToLow();
	    }
	}

    }

    
    
    
    /*
    private MonitorableResourcePool[] getConnectionFactories(){
        ResourcesUtil resUtil = ResourcesUtil.getInstance();

        ConnectorConnectionPool[] cp = resUtil.getConnectorConnectionPools();
        if (cp == null) {
            return null;
        }
        
        //ArrayList al = new ArrayList();
        ArrayList<MonitorableResourcePool> al = new ArrayList();
        HashMap allPools = getPoolManager().getPoolTable();
        for( int i = 0 ; i < cp.length; i++ ) {
            if (cp[i].getName().equalsIgnoreCase(
                            ConnectorConstants.DEFAULT_JMS_ADAPTER)) {
                MonitorableResourcePool p = (MonitorableResourcePool) 
                    allPools.get(cp[i].getName());
                al.add(p);
            }
        }
        return (MonitorableResourcePool[])al.toArray(
            new MonitorableResourcePool[]{});
    }
    */

    private PoolManager getPoolManager() {
        return Switch.getSwitch().getPoolManager();
    }

    private void transitionToHigh() {
        final ResourcesUtil resUtil = ResourcesUtil.createInstance();
        AccessController.doPrivileged( new PrivilegedAction() {    
            public Object run() {
                //we should create a new pool object everytime since
                //the stats have to be collected afresh
                Map<ConnectorConnectionPool, MonitorableResourcePool> poolMap
                    = resUtil.getPoolMap();
                if (poolMap == null) {
                    return null;
                }
        
                for (Map.Entry<ConnectorConnectionPool, MonitorableResourcePool> entry :
                        poolMap.entrySet() ) {
                    ConnectorConnectionPool ccp = entry.getKey();
                    MonitorableResourcePool pool = entry.getValue();
                    if ( pool != null ) {
                        try {
                            ConnectorConnectionPoolStatsImpl stats = new 
                                ConnectorConnectionPoolStatsImpl(pool);
                            getPoolManager().setMonitoringEnabledHigh(
                                pool.getPoolName());
                            registry_.registerConnectorConnectionPoolStats(
                                stats, pool.getPoolName(), 
                                resUtil.getAppName(ccp), 
                                resUtil.getRAName(ccp), 
                                null);
                        } catch (Exception mre) {
                            try {
                                _logger.log( Level.WARNING, "poolmon.cannot_reg",
                                    mre.getMessage() );
                                getPoolManager().disableMonitoring(
                                    pool.getPoolName());
                            } catch (Exception ex) {
                                //FIXME: ignore?
                            }
                        }
                    }       
                }
                return null;
            }
        });
    }

    private void switchOffMonitoring() {
        final ResourcesUtil resUtil = ResourcesUtil.createInstance();
        //deregister stats
        AccessController.doPrivileged( new PrivilegedAction() {
            public Object run() {
                //Unregister all connector connection pools
                Map<ConnectorConnectionPool, MonitorableResourcePool> 
                    pools = resUtil.getPoolMap();
                if (pools == null) {
                    return null;
                }
                        
                for (Map.Entry<ConnectorConnectionPool, MonitorableResourcePool> entry :  pools.entrySet() ) {
                    ConnectorConnectionPool ccp = entry.getKey();
                    MonitorableResourcePool pool = entry.getValue();
                    if (pool != null) {
                        try {
                            registry_.unregisterConnectorConnectionPoolStats(
                                pool.getPoolName(), 
                                resUtil.getAppName(ccp), 
                                resUtil.getRAName(ccp) );
                            getPoolManager().disableMonitoring(
                                pool.getPoolName());
                        } catch( Exception mre ) {
                            _logger.log( Level.INFO, "poolmon.cannot_unreg");
                        }
                    }
                }
                       
                return null;
            }
        });
    }

    private void transitionToLow() {
        final ResourcesUtil resUtil = ResourcesUtil.createInstance();
        AccessController.doPrivileged( new PrivilegedAction() {    
            public Object run() {
                //we should create a new pool object everytime since
                //the stats have to be collected afresh
                Map<ConnectorConnectionPool, MonitorableResourcePool> poolMap
                    = resUtil.getPoolMap();
                if (poolMap == null) {
                    return null;
                }
        
                for (Map.Entry<ConnectorConnectionPool, MonitorableResourcePool> entry :
                        poolMap.entrySet() ) {
                    ConnectorConnectionPool ccp = entry.getKey();
                    MonitorableResourcePool pool = entry.getValue();
                    if ( pool != null ) {
                        try {
                            ConnectorConnectionPoolStatsImpl stats = new 
                                ConnectorConnectionPoolStatsImpl(pool);
                            getPoolManager().setMonitoringEnabledLow(
                                pool.getPoolName());
                            registry_.registerConnectorConnectionPoolStats(
                                stats, pool.getPoolName(), 
                                resUtil.getAppName(ccp), 
                                resUtil.getRAName(ccp), null);
                        } catch (Exception mre) {
                            try {
                                _logger.log( Level.WARNING, "poolmon.cannot_reg",
                                    mre.getMessage() );
                                getPoolManager().disableMonitoring(
                                    pool.getPoolName());
                            } catch (Exception ex) {
                                //FIXME: ignore?
                            }
                        }
                    }       
                }
                return null;
            }
        });
    }

    private void logFine( String msg ) {
        if ( msg != null && _logger.isLoggable( Level.FINE ) ) {
            _logger.fine( msg );
        }
    }
}
