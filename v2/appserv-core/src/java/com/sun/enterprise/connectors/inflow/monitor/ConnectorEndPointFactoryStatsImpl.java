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

package com.sun.enterprise.connectors.inflow.monitor;

import com.sun.enterprise.admin.monitor.stats.ConnectorEndPointFactoryStats;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.resource.monitor.AbstractStatsImpl;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.j2ee.statistics.CountStatistic;

/**
 * This class provides an implementation of the JDBCConnectionPoolStats
 * interface. The implementations of the interface methods primarily delegate the
 * task of statistic gathering to the work-manager of the inbound resource 
 * adapter
 * @author Sivakumar Thyagarajan
 */
public final class ConnectorEndPointFactoryStatsImpl extends AbstractStatsImpl
                                        implements ConnectorEndPointFactoryStats {

    private static final Logger _logger = 
                LogDomains.getLogger( LogDomains.RSR_LOGGER );
    private GenericStatsImpl gsImpl;
    
    private CountStatistic endPointsCreatedCount;
    private CountStatistic messagesDeliveredCount;
    private CountStatistic runTimeExceptionsCount;

    //@discuss: should this be for a bean-id combination?
    public ConnectorEndPointFactoryStatsImpl() {
        initializeStatistics();
        try {
            gsImpl = new GenericStatsImpl(
            this.getClass().getInterfaces()[0].getName(), this );
            } catch( ClassNotFoundException cnfe ) {
            //@todo:add to reosurces file
                _logger.log( Level.INFO, "endpointfacmon.cnfe", "GenericStatsImpl" );
            }
    }
    

    private void initializeStatistics() {
        long time = System.currentTimeMillis();
        CountStatistic cs = null;
    
        cs = new CountStatisticImpl(0,
            "endPointsCreated", "", 
        "The number of endpoints created using this endpoint factory"
            ,time, time);
        endPointsCreatedCount = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0, "messagesDelivered", "", 
                    "The number of messages delivered to endpoints created using this " +
                    "endpoint factory",time, time);
        messagesDeliveredCount = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0, "runtimeExceptionCount", "", 
                    "The number of runtime exceptions thown by endpoints created" +
                    "by this end point factory",time, time);
        runTimeExceptionsCount = new MutableCountStatisticImpl( cs );
    }


    /* (non-Javadoc)
     * @see com.sun.enterprise.admin.monitor.stats.ConnectorEndPointFactoryStats#getMessagesCreatedCount()
     */
    public CountStatistic getMessagesCreatedCount() {
        //@todo to implement
        return null;
    }


    /* (non-Javadoc)
     * @see com.sun.enterprise.admin.monitor.stats.ConnectorEndPointFactoryStats#getDeliveredMessageCount()
     */
    public CountStatistic getDeliveredMessageCount() {
        //@todo to implement
        return null;
    }


    /* (non-Javadoc)
     * @see com.sun.enterprise.admin.monitor.stats.ConnectorEndPointFactoryStats#getRuntimeExceptionsCount()
     */
    public CountStatistic getRuntimeExceptionsCount() {
        //@todo to implement
        return null;
    }
}
