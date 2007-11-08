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

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.RangeStatistic;

import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.OrbConnectionManagerStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StatisticImpl;

import com.sun.corba.ee.spi.monitoring.MonitoringConstants;
import com.sun.corba.ee.spi.monitoring.MonitoringManager;
import com.sun.corba.ee.spi.monitoring.MonitoredObject;

/**
 * This is the implementation for the OrbConnectionManagerStats
 * and provides the implementation required to get the statistics
 * for an orb connection
 *
 * @author Pramod Gopinath
 */

public class OrbConnectionManagerStatsImpl
extends ORBCommonStatsImpl
implements OrbConnectionManagerStats
{

    private MonitoredObject connection;
    private String          connectionName;

    private static String   stringTotalConnections = 
        MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS;
    private static String   stringIdleConnections = 
        MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS;
    private static String   stringBusyConnections = 
        MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS;

    private MutableBoundedRangeStatisticImpl totalConnections;
    private MutableCountStatisticImpl        idleConnections;
    private MutableCountStatisticImpl        busyConnections;


    public OrbConnectionManagerStatsImpl( MonitoredObject connectionNode ) {
        this.connection     = connectionNode; 
        this.connectionName = connection.getName();

        initializeStats();
    }

    private void initializeStats() {
    	super.initialize(
            "com.sun.enterprise.admin.monitor.stats.OrbConnectionManagerStats");

        final long time = System.currentTimeMillis();

        totalConnections = 
            new MutableBoundedRangeStatisticImpl(
                new BoundedRangeStatisticImpl( 0, 0, 0, 
                    java.lang.Long.MAX_VALUE, 0,
                    "TotalConnections", "count",
                    connection.getAttribute( stringTotalConnections ).
                    getAttributeInfo( ).getDescription(), 
                    time, time ));

        idleConnections = 
            new MutableCountStatisticImpl(
                new CountStatisticImpl( 
                    "ConnectionsIdle", "count", 
                    connection.getAttribute( stringIdleConnections ).
                    getAttributeInfo( ).getDescription() ));

        busyConnections = 
            new MutableCountStatisticImpl(
                new CountStatisticImpl( 
                    "ConnectionsInUse", "count", 
                    connection.getAttribute( stringBusyConnections ).
                    getAttributeInfo( ).getDescription() ));
    }

    public BoundedRangeStatistic getTotalConnections() {

        long totalNumberOfConnections = ((Long) connection.getAttribute( 
            stringTotalConnections ).getValue()).longValue();

        totalConnections.setCount( totalNumberOfConnections );

        return (BoundedRangeStatistic) totalConnections.modifiableView();
    }


    public CountStatistic getConnectionsIdle() {

        long numberIdleConnections = ((Long) connection.getAttribute( 
	    stringIdleConnections ).getValue()).longValue();


        idleConnections.setCount( numberIdleConnections );

        return (CountStatistic) idleConnections.modifiableView();
    }

            
    public CountStatistic getConnectionsInUse() {

        long numberBusyConnections = ((Long) connection.getAttribute( 
	    stringBusyConnections ).getValue()).longValue();

        busyConnections.setCount( numberBusyConnections );

        return (CountStatistic) busyConnections.modifiableView();
    }


} //OrbConnectionManagerStatsImpl{ }
