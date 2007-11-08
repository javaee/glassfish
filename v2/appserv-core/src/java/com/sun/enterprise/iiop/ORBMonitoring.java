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
package com.sun.enterprise.iiop;

import java.util.Iterator;
import java.util.logging.*;

import org.omg.CORBA.ORB;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;

import com.sun.enterprise.admin.monitor.stats.OrbConnectionManagerStats;
import com.sun.enterprise.admin.monitor.stats.ThreadPoolStats;

import com.sun.corba.ee.spi.monitoring.MonitoringConstants;
import com.sun.corba.ee.spi.monitoring.MonitoringFactories;
import com.sun.corba.ee.spi.monitoring.MonitoringManager;
import com.sun.corba.ee.spi.monitoring.MonitoredObject;

import com.sun.logging.*;


/**
 * This file provides for the registration of the ThreadPool 
 * and orb connection statistics capture.
 *
 * @author Pramod Gopinath
 */


public class ORBMonitoring {
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);

    private ORB orb;
    private MonitoringRegistry registry;

    public ORBMonitoring(ORB orb) {
        this.orb = orb;
        registry = ApplicationServer.getServerContext().getMonitoringRegistry();
        registerOrbStatistics();
    }

    public void registerOrbStatistics() {

        registerThreadPoolStats(); 
        registerORBConnectionStats(); 
    }

    /**
     * This method is called to register the thread pool stats
     * For each threadpool create an object that would be registerd with the
     * admin framework.
     *
     */
    private void registerThreadPoolStats() {
        Iterator threadPoolsIterator;
	MonitoredObject threadPoolRoot = 
	    MonitoringFactories.getMonitoringManagerFactory().createMonitoringManager( 
		MonitoringConstants.DEFAULT_MONITORING_ROOT, null ).
		getRootMonitoredObject();

        try {
            MonitoredObject rootThreadPoolNode = threadPoolRoot.getChild(
                   MonitoringConstants.THREADPOOL_MONITORING_ROOT );

            threadPoolsIterator = rootThreadPoolNode.getChildren().iterator();
        } catch( Exception ex ) {
            _logger.log( Level.WARNING, 
                "Unexpected exception caught when accessing ThreadPool Stats:", ex );
            return;
        } 

        while( threadPoolsIterator.hasNext() ) {
            MonitoredObject threadPool = (MonitoredObject) threadPoolsIterator.next();

            try {
                ThreadPoolStats threadPoolStats = 
                    new ThreadPoolStatsImpl( threadPool );

                registry.registerThreadPoolStats( threadPoolStats, 
		    threadPoolRoot.getName() + 
                    "." + MonitoringConstants.THREADPOOL_MONITORING_ROOT +
                     "." + threadPool.getName(),
                    null );
            } catch( MonitoringRegistrationException mex ) {
                //TODO: localize these messages
                _logger.log( Level.WARNING, 
                    "Unable to register ThreadPoolStats due to following exception ", mex );
            } catch( Exception ex ) {
                //TODO: localize these messages
                _logger.log( Level.WARNING, 
                    "Unexpected exception caught when registring ThreadPoolStats", ex );
            }
        }
    }// registerThreadPoolStats() 


    /**
     * This method is called to register the orb connection manager stats
     * For each connection create an object that would be registerd with the
     * admin framework.
     *
     */
    private void registerORBConnectionStats() {
        Iterator outboundConnectionListIterator;
        Iterator inboundConnectionListIterator;

        try {
	    com.sun.corba.ee.spi.orb.ORB internalORB = 
		(com.sun.corba.ee.spi.orb.ORB)orb ;
	    MonitoredObject orbRoot = 
		internalORB.getMonitoringManager().getRootMonitoredObject() ;

            MonitoredObject rootConnectionNode = 
                orbRoot.getChild( MonitoringConstants.CONNECTION_MONITORING_ROOT );
    
            MonitoredObject rootOutboundConnections =
                rootConnectionNode.getChild( 
                    MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT );
            MonitoredObject rootInboundConnections =
                rootConnectionNode.getChild( 
                    MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT );
             
            if( rootOutboundConnections != null ) {
                outboundConnectionListIterator = 
                    rootOutboundConnections.getChildren().iterator();

                while( outboundConnectionListIterator.hasNext() ) {
                    MonitoredObject outboundConnection = 
                        (MonitoredObject) outboundConnectionListIterator.next();

                    try {
                        OrbConnectionManagerStats connectionManagerStats = 
                            new OrbConnectionManagerStatsImpl( outboundConnection );

                        registry.registerOrbConnectionManagerStats( connectionManagerStats, 
			    orbRoot.getName() + 
                            "." + MonitoringConstants.CONNECTION_MONITORING_ROOT +
                            "." + MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT +
                            "." + outboundConnection.getName(),
                            null );

                    } catch( MonitoringRegistrationException mex ) {
                        //TODO: localize these messages
                        _logger.log( Level.WARNING, 
                            "Unable to register Outbound ORB Connections - ", mex );
                    } catch( Exception ex ) {
                        //TODO: localize these messages
                        _logger.log( Level.WARNING, 
                            "Unexpected exception caught when registering Outbound ORB Connections", ex );
                    }
                } 
            } 

            if( rootInboundConnections != null ) {
                inboundConnectionListIterator = 
                    rootInboundConnections.getChildren().iterator();

                while( inboundConnectionListIterator.hasNext() ) {
                    MonitoredObject inboundConnection = 
                        (MonitoredObject) inboundConnectionListIterator.next();
        
                    try {
                        OrbConnectionManagerStats connectionManagerStats = 
                            new OrbConnectionManagerStatsImpl( inboundConnection );
        
                        registry.registerOrbConnectionManagerStats( connectionManagerStats, 
			    orbRoot.getName() + 
                            "." + MonitoringConstants.CONNECTION_MONITORING_ROOT +
                            "." + MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT +
                            "." + inboundConnection.getName(),
                            null );
                    } catch( MonitoringRegistrationException mex ) {
                        //TODO: localize these messages
                        _logger.log( Level.WARNING, 
                            "Unable to register Inbound ORB Connections - ", mex );
                    } catch( Exception ex ) {
                        //TODO: localize these messages
                        _logger.log( Level.WARNING, 
                            "Unexpected exception caught when registering Inbound ORB Connections", ex );
                    }
                } //while()
            } //if()

        } catch( Exception ex ) {
            _logger.log( Level.WARNING, 
                "Unexpected exception caught when accessing ORB Connection Stats:", ex );
            return;
        } 


    } //registerORBConnectionStats() 

} //ORBMonitoring{}
