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

package com.sun.enterprise.connectors.work.monitor;

import com.sun.enterprise.admin.monitor.stats.ConnectorWorkMgmtStats;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.RangeStatisticImpl;
import com.sun.enterprise.connectors.ActiveInboundResourceAdapter;
import com.sun.enterprise.resource.monitor.AbstractStatsImpl;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;

/**
 * This class provides an implementation of the JDBCConnectionPoolStats
 * interface. The implementations of the interface methods primarily delegate the
 * task of statistic gathering to the work-manager of the inbound resource 
 * adapter
 * @author Sivakumar Thyagarajan
 */
public final class ConnectorWorkMgmtStatsImpl extends AbstractStatsImpl
                                        implements ConnectorWorkMgmtStats {

    private static final Logger _logger = 
                LogDomains.getLogger( LogDomains.RSR_LOGGER );
    private GenericStatsImpl gsImpl;
    
    private RangeStatistic activeWorkCount;
    private RangeStatistic waitQueueLength;
    private RangeStatistic workRequestWaitTime;
    private MutableCountStatistic submittedWorkCount;
    private MutableCountStatistic rejectedWorkCount;
    private MutableCountStatistic completedWorkCount;
    private MonitorableWorkManager workManager;
    
    public ConnectorWorkMgmtStatsImpl( ActiveInboundResourceAdapter inboundRA ) {
        this.workManager = (MonitorableWorkManager)inboundRA.
                                    getBootStrapContext().getWorkManager();
        initializeStatistics();
        try {
            gsImpl = new GenericStatsImpl(
            this.getClass().getInterfaces()[0].getName(), this );
            } catch( ClassNotFoundException cnfe ) {
            //@todo:add to reosurces file
                _logger.log( Level.INFO, "workmgmtmon.cnfe", "GenericStatsImpl" );
            }
    }
    

    private void initializeStatistics() {
        long time = System.currentTimeMillis();
        CountStatistic cs = null;
    
        cs = new CountStatisticImpl(0,
            "SubmittedWorkCount", "", 
        "Number of work objects submitted by a connector module for  execution"
        + "WorkQueue before executing",time, time);
        submittedWorkCount = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0,
            "RejectedWorkCount", "", 
        "Number of work objects rejected by the application server",time, time);
        rejectedWorkCount = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0,
            "CompletedWorkCount", "", 
        "Number of work objects completed execution",time, time);
        completedWorkCount = new MutableCountStatisticImpl( cs );

        //the low water mark is set with a seed value of 1 to 
        //ensure that the comparison with currentVal returns 
        //the correct low water mark the first time around
        //the least number of connections that we can use is always 1
        activeWorkCount = new RangeStatisticImpl(0, 0, 
            1, "ActiveWorkCount", "",
        "Number of active work objects",
        time, time);

        waitQueueLength = new RangeStatisticImpl(0, 0, 
            1, "WaitQueueLength", "",
        "Number of work objects waiting in the queue for execution",
        time, time);

        workRequestWaitTime = new RangeStatisticImpl(0, 0, 
                        1, "workRequestWaitTime", "",
                    "Number of work objects waiting in the queue for execution",
                    time, time);
    }

    public RangeStatistic getActiveWorkCount() {
        activeWorkCount = getUpdatedRangeStatistic(activeWorkCount, 
                        workManager.getCurrentActiveWorkCount(), 
                        workManager.getMaxActiveWorkCount(), 
                        workManager.getMinActiveWorkCount());
        return activeWorkCount;
    }

    public RangeStatistic getWaitQueueLength() {
        waitQueueLength = getUpdatedRangeStatistic(waitQueueLength, 
                        workManager.getWaitQueueLength(), 
                        workManager.getMaxWaitQueueLength(),
                        workManager.getMinWaitQueueLength());
        return waitQueueLength;
    }
    
    public RangeStatistic getWorkRequestWaitTime() {
        workRequestWaitTime = getUpdatedRangeStatistic(workRequestWaitTime, 0, 
                        workManager.getMaxWorkRequestWaitTime(),
                        workManager.getMinWorkRequestWaitTime());
        return workRequestWaitTime;
    }

    public CountStatistic getSubmittedWorkCount() {
        submittedWorkCount.setCount( workManager.getSubmittedWorkCount());
        return (CountStatistic) submittedWorkCount.unmodifiableView();
    }

    public CountStatistic getRejectedWorkCount() {
        rejectedWorkCount.setCount( workManager.getRejectedWorkCount());
        return (CountStatistic) rejectedWorkCount.unmodifiableView();
    }

    public CountStatistic getCompletedWorkCount() {
        completedWorkCount.setCount( workManager.getCompletedWorkCount());
        return (CountStatistic) completedWorkCount.unmodifiableView();
    }
}
