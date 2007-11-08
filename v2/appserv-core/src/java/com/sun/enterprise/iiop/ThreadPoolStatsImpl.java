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
import com.sun.enterprise.admin.monitor.stats.ThreadPoolStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StatisticImpl;

import com.sun.corba.ee.spi.monitoring.MonitoringConstants;
import com.sun.corba.ee.spi.monitoring.MonitoringManager;
import com.sun.corba.ee.spi.monitoring.MonitoredObject;
import com.sun.corba.ee.impl.orbutil.ORBConstants;

/**
 * This is the implementation for the ThreadPoolStats
 * and provides the implementation required to get the statistics
 * for a threadpool
 *
 * @author Pramod Gopinath
 */

public class ThreadPoolStatsImpl
extends ORBCommonStatsImpl
implements ThreadPoolStats
{

    private MonitoredObject threadPool;
    private MonitoredObject workQueue;
    private String          threadPoolName;

    private MutableCountStatisticImpl        numberOfBusyThreads;
    private MutableCountStatisticImpl        numberOfAvailableThreads;
    private MutableBoundedRangeStatisticImpl currentNumberOfThreads;
    private MutableBoundedRangeStatisticImpl averageWorkCompletionTime;
    private MutableCountStatisticImpl        totalWorkItemsAdded;
    private MutableBoundedRangeStatisticImpl numberOfWorkItemsInQueue;
    private MutableBoundedRangeStatisticImpl averageTimeInQueue;

    private static final String stringNumberOfBusyThreads =
        MonitoringConstants.THREADPOOL_NUMBER_OF_BUSY_THREADS;
    private static final String stringNumberOfAvailableThreads =
        MonitoringConstants.THREADPOOL_NUMBER_OF_AVAILABLE_THREADS;
    private static final String stringCurrentNumberOfThreads =
        MonitoringConstants.THREADPOOL_CURRENT_NUMBER_OF_THREADS; 
    private static final String stringAverageWorkCompletionTime =
        MonitoringConstants.THREADPOOL_AVERAGE_WORK_COMPLETION_TIME;
    private static final String stringTotalWorkItemsAdded =
        MonitoringConstants.WORKQUEUE_TOTAL_WORK_ITEMS_ADDED;
    private static final String stringNumberOfWorkItemsInQueue =
        MonitoringConstants.WORKQUEUE_WORK_ITEMS_IN_QUEUE;
    private static final String stringAverageTimeInQueue =
        MonitoringConstants.WORKQUEUE_AVERAGE_TIME_IN_QUEUE;


    public ThreadPoolStatsImpl( MonitoredObject threadPool ) {
        this.threadPool     = threadPool;
        this.threadPoolName = threadPool.getName();

        getWorkQueueForThreadPool();

        initializeStats();
    }

    private void getWorkQueueForThreadPool() {
        Object[] workQueues = threadPool.getChildren().toArray();    
        workQueue = (MonitoredObject) workQueues[ 0 ];
    }


    private void initializeStats() {
    	super.initialize("com.sun.enterprise.admin.monitor.stats.ThreadPoolStats");

        final long time = System.currentTimeMillis();

        numberOfBusyThreads = 
            new MutableCountStatisticImpl( 
                new CountStatisticImpl( 0, stringNumberOfBusyThreads, "COUNT", 
                    threadPool.getAttribute( stringNumberOfBusyThreads ).
                    getAttributeInfo().getDescription(),
                    time, time ));

        numberOfAvailableThreads = 
            new MutableCountStatisticImpl( 
                new CountStatisticImpl( 0, stringNumberOfAvailableThreads, "count", 
                    threadPool.getAttribute( stringNumberOfAvailableThreads ).
                    getAttributeInfo().getDescription(), 
                    time, time ));

        currentNumberOfThreads = 
            new MutableBoundedRangeStatisticImpl(
                new BoundedRangeStatisticImpl( 0, 0, 0, java.lang.Long.MAX_VALUE, 0,
                    stringCurrentNumberOfThreads, "count",
                    threadPool.getAttribute( stringCurrentNumberOfThreads ).
                    getAttributeInfo().getDescription(), 
                    time, time ));

        averageWorkCompletionTime = 
            new MutableBoundedRangeStatisticImpl(
                new BoundedRangeStatisticImpl( 0, 0, 0, java.lang.Long.MAX_VALUE, 0,
                    stringAverageWorkCompletionTime, "Milliseconds",
                    threadPool.getAttribute( stringAverageWorkCompletionTime ).
                    getAttributeInfo().getDescription(), 
                    time, time ));

        MonitoredObject workQueue = threadPool.getChild( 
            ORBConstants.WORKQUEUE_DEFAULT_NAME );

        totalWorkItemsAdded = 
            new MutableCountStatisticImpl(
                new CountStatisticImpl( 0, stringTotalWorkItemsAdded, "count",
                    workQueue.getAttribute( stringTotalWorkItemsAdded ).
                    getAttributeInfo().getDescription(), 
                    time, time ));

            numberOfWorkItemsInQueue = 
            new MutableBoundedRangeStatisticImpl(
                new BoundedRangeStatisticImpl( 0, 0,0, java.lang.Long.MAX_VALUE, 0,
                    stringNumberOfWorkItemsInQueue, "count",
                    workQueue.getAttribute( stringNumberOfWorkItemsInQueue ).
                    getAttributeInfo( ).getDescription(), 
                    time, time ));

        averageTimeInQueue = 
            new MutableBoundedRangeStatisticImpl(
                new BoundedRangeStatisticImpl( 0, 0, 0, java.lang.Long.MAX_VALUE, 0,
                    stringAverageTimeInQueue, "Milliseconds",
                    workQueue.getAttribute( stringAverageTimeInQueue ).
                    getAttributeInfo( ).getDescription(), 
                    time, time ));

    }

    public CountStatistic getNumberOfBusyThreads() {
        long numBusyThreads = ((Long) threadPool.getAttribute( 
            stringNumberOfBusyThreads ).getValue()).longValue();

        numberOfBusyThreads.setCount( numBusyThreads );

        return (CountStatistic) numberOfBusyThreads.modifiableView();
    }

    public CountStatistic getNumberOfAvailableThreads() {
        long numAvailableThreads = ((Long) threadPool.getAttribute( 
            stringNumberOfAvailableThreads ).getValue()).longValue();

        numberOfAvailableThreads.setCount( numAvailableThreads ); 

        return (CountStatistic) numberOfAvailableThreads.modifiableView();
    }


    public BoundedRangeStatistic getCurrentNumberOfThreads() {
        long numCurrentThreads = ((Long) threadPool.getAttribute( 
            stringCurrentNumberOfThreads ).getValue()).longValue();

        currentNumberOfThreads.setCount( numCurrentThreads );

        return (BoundedRangeStatistic) currentNumberOfThreads.modifiableView();
    }


    public RangeStatistic getAverageWorkCompletionTime() {
        long avgWorkCompletionTime = ((Long) threadPool.getAttribute( 
	    stringAverageWorkCompletionTime ).getValue()).longValue();

        averageWorkCompletionTime.setCount( avgWorkCompletionTime );

        return (RangeStatistic) averageWorkCompletionTime.modifiableView();
    }


    public CountStatistic getTotalWorkItemsAdded() {
        long totWorkItemsAdded = ((Long) workQueue.getAttribute( 
	    stringTotalWorkItemsAdded ).getValue()).longValue();


        totalWorkItemsAdded.setCount( totWorkItemsAdded );

        return (CountStatistic) totalWorkItemsAdded.modifiableView();
    }

            
    public BoundedRangeStatistic getNumberOfWorkItemsInQueue() {
        long totWorkItemsInQueue = ((Long) workQueue.getAttribute( 
	    stringNumberOfWorkItemsInQueue ).getValue()).longValue();

        numberOfWorkItemsInQueue.setCount( totWorkItemsInQueue );

        return (BoundedRangeStatistic) numberOfWorkItemsInQueue.modifiableView();
    }


    public RangeStatistic getAverageTimeInQueue() {
        long avgTimeInQueue = ((Long) workQueue.getAttribute( 
	    stringAverageTimeInQueue ).getValue()).longValue();

        averageTimeInQueue.setCount( avgTimeInQueue );

        return (RangeStatistic) averageTimeInQueue.modifiableView();
    }


} //ThreadPoolStatsImpl{ }
