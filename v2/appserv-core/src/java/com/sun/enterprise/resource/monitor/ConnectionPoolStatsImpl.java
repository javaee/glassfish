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

import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;

import com.sun.enterprise.admin.monitor.stats.ConnectorConnectionPoolStats;
import com.sun.enterprise.admin.monitor.stats.JDBCConnectionPoolStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.RangeStatisticImpl;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.resource.MonitorableResourcePool;

/**
 * An abstract class that provides an implementation of the ConnectionPoolStats
 * interface.  This common implementation is used by the JDBCConnectionPoolStats
 * and ConnectorConnectionPoolStats implementations.
 * @author Sivakumar Thyagarajan
 */
public abstract class ConnectionPoolStatsImpl extends AbstractStatsImpl
                                    implements ConnectorConnectionPoolStats, JDBCConnectionPoolStats {
    
    protected MonitorableResourcePool pool_;
    
    private static StringManager localStrings =
        StringManager.getManager(ConnectionPoolStatsImpl.class);

    private MutableCountStatistic numConnFailedValidation_;
    private MutableCountStatistic numConnTimedOut_;
    private RangeStatistic numConnUsed_;
    
    //Since 8.1
    private RangeStatistic numConnFree_;
    private RangeStatistic numConnRequestWaitTime_;
    private MutableCountStatistic averageConnWaitTime;
    private MutableCountStatistic waitQueueLength;
    private MutableCountStatistic numConnCreated;
    private MutableCountStatistic numConnDestroyed;
    private MutableCountStatistic numConnAcquired;
    private MutableCountStatistic numConnReleased;
    
    //Since 9.0
    private MutableCountStatistic numConnMatched;
    private MutableCountStatistic numConnNotMatched;

    //Since 9.1
    private MutableCountStatistic numPotentialConnLeak;
    
    protected final static Logger _logger = LogDomains.getLogger( LogDomains.RSR_LOGGER );
    
    public CountStatistic getNumConnFailedValidation() {
        numConnFailedValidation_.setCount( 
            pool_.getNumConnFailedValidation() );
        return (CountStatistic)numConnFailedValidation_.unmodifiableView();
    }

    public CountStatistic getNumConnTimedOut() {
        numConnTimedOut_.setCount( 
            pool_.getNumConnTimedOut() );
        return (CountStatistic) numConnTimedOut_.unmodifiableView();
    }
    
    public RangeStatistic getNumConnUsed() {
        numConnUsed_ = getUpdatedRangeStatistic(numConnUsed_, 
                        pool_.getNumConnInUse(), pool_.getMaxNumConnUsed(), 
                        pool_.getMinNumConnUsed()); 
        return numConnUsed_;
    }

    /*
     * Initialize the Statistic objects for all collected
     * statistics.
     */
    protected void initializeStatistics() {

        long time = System.currentTimeMillis();
        CountStatistic cs = null;
    
        cs = new CountStatisticImpl(0,
            getLocalizedStringFor("num.conn.failed.validation", 
                            "NumConnFailedValidation"), 
            getLocalizedStringFor("stat.count", "Count"),
            getLocalizedStringFor("num.conn.failed.validation.desc", 
                            "Number Of Connections that failed validation"),
            time, time);
        numConnFailedValidation_ = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0,
            getLocalizedStringFor("num.conn.timedout", "NumConnTimedOut"), 
            getLocalizedStringFor("stat.count", "Count"),
            getLocalizedStringFor("num.conn.timedout.desc", 
                            "Number of Connection requests that timed out waiting"),
            time, time);
        numConnTimedOut_ = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0,
                        getLocalizedStringFor("num.conn.created","NumConnCreated"), 
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("num.conn.created.desc",
                                        "Number of Connection that have been created"),
                        time, time);
        numConnCreated = new MutableCountStatisticImpl( cs );
        
        cs = new CountStatisticImpl(0,
                        getLocalizedStringFor("num.conn.destroyed", 
                                        "NumConnDestroyed"),
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("num.conn.destroyed.desc", 
                                        "Number of Connection that have been destroyed") ,
                        time, time);
        numConnDestroyed = new MutableCountStatisticImpl( cs );
        
        cs = new CountStatisticImpl(0,
                        getLocalizedStringFor("num.conn.opened", "NumConnOpened"), 
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("num.conn.opened.desc", 
                                        "Number of Connection that have been acquired"),
                    time, time);
        numConnAcquired = new MutableCountStatisticImpl( cs );
        
        cs = new CountStatisticImpl(0,
                        getLocalizedStringFor("num.conn.closed", "NumConnClosed"), 
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("num.conn.closed.desc", 
                                        "Number of Connection that have been released"),
                    time, time);
        numConnReleased = new MutableCountStatisticImpl( cs );
        
        
        cs = new CountStatisticImpl(0,
                        getLocalizedStringFor("avg.conn.wait.time", 
                                        "AvgConnWaitTime"), 
                        getLocalizedStringFor("stat.milliseconds", "milliseconds"),
                        getLocalizedStringFor("avg.conn.wait.time.desc", 
                                        "Average wait time-duration per successful connection request"),
                        time, time);
        averageConnWaitTime = new MutableCountStatisticImpl( cs );
        
        cs = new CountStatisticImpl(0,
                        getLocalizedStringFor("wait.queue.length", 
                                        "WaitQueueLength"),
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("wait.queue.length.desc", 
                                        "Connection request Wait Queue length"),
                        time, time);
        waitQueueLength = new MutableCountStatisticImpl( cs );
        
        //the low water mark is set with a seed value of 1 to 
        //ensure that the comparison with currentVal returns 
        //the correct low water mark the first time around
        //the least number of connections that we can use is always 1
        numConnUsed_ = new RangeStatisticImpl(0, 0, 1, 
                        getLocalizedStringFor("num.conn.used", "NumConnUsed"), 
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("num.conn.used.desc", 
                                        "Number Of Connections used"),
                        time, time);
        
        numConnFree_ = new RangeStatisticImpl(0, 0, 1, 
                        getLocalizedStringFor("num.conn.free", "NumConnFree"), 
                        getLocalizedStringFor("stat.count", "Count"),
                        getLocalizedStringFor("num.conn.free.desc", 
                                        "Number Of Free Connections"),
                        time, time);
        numConnRequestWaitTime_ = new RangeStatisticImpl(0, 0, 1, 
                        getLocalizedStringFor("conn.request.wait.time", 
                                        "ConnRequestWaitTime"), 
                        getLocalizedStringFor("stat.milliseconds", "milliseconds"),
                        getLocalizedStringFor("conn.request.wait.time.desc", 
                                        "Max and min connection request wait times"),
                         time, time);
        
        cs = new CountStatisticImpl(0, 
        		getLocalizedStringFor("num.conn.matched", "NumConnMatched"), 
                getLocalizedStringFor("stat.count", "Count"),
                getLocalizedStringFor("num.conn.matched.desc", 
                                "Number of Connection that were successfully matched by the MCF. "),
            time, time);
        numConnMatched = new MutableCountStatisticImpl( cs );

        cs = new CountStatisticImpl(0, 
        		getLocalizedStringFor("num.conn.not.matched", "NumConnNotMatched"), 
                getLocalizedStringFor("stat.count", "Count"),
                getLocalizedStringFor("num.conn.not.matched.desc", 
                                "Number of Connection that were rejected by the MCF. "),
            time, time);
        numConnNotMatched = new MutableCountStatisticImpl( cs );
        
        cs = new CountStatisticImpl(0, 
        		getLocalizedStringFor("num.potential.connection.leak", "NumPotentialConnLeak"), 
                getLocalizedStringFor("stat.count", "Count"),
                getLocalizedStringFor("num.potential.connection.leak.desc", 
                                "Number of potential connections leak detected."),
            time, time);
        numPotentialConnLeak = new MutableCountStatisticImpl( cs );

    }

   
   // Begin - New Statistics for 8.1
   public RangeStatistic getNumConnFree(){
       numConnFree_ = getUpdatedRangeStatistic(numConnFree_, 
                       pool_.getNumConnFree(), pool_.getMaxNumConnFree(), 
                       pool_.getMinNumConnFree()); 
       return numConnFree_;
   }
   
   public CountStatistic getAverageConnWaitTime() {
       //Time taken by all connection requests divided by total number of 
       //connections acquired in the sampling period.
       long averageWaitTime = 0;
       if (getNumConnAcquired().getCount() != 0) {
           averageWaitTime = pool_.getTotalConnectionRequestWaitTime() / 
                                 (getNumConnAcquired().getCount());
       } else {
           averageWaitTime = 0;
       }

       averageConnWaitTime.setCount(averageWaitTime);
       return (CountStatistic)averageConnWaitTime.unmodifiableView();
   }   

   public RangeStatistic getConnRequestWaitTime() {
       numConnRequestWaitTime_ = getUpdatedRangeStatistic(
               numConnRequestWaitTime_ , pool_.getCurrentConnRequestWaitTime() , 
               pool_.getMaxConnRequestWaitTime(), 
               pool_.getMinConnRequestWaitTime());
       return numConnRequestWaitTime_;
   }
   
   public CountStatistic getNumConnCreated() {
       numConnCreated.setCount(pool_.getNumConnCreated());
       return (CountStatistic)numConnCreated.unmodifiableView();
   }
   
   public CountStatistic getNumConnDestroyed() {
       numConnDestroyed.setCount(pool_.getNumConnDestroyed());
       return (CountStatistic)numConnDestroyed.unmodifiableView();
   }
   
   public CountStatistic getNumConnAcquired(){
       numConnAcquired.setCount(pool_.getNumConnAcquired());
       return (CountStatistic)numConnAcquired.unmodifiableView();
   }
   
   public CountStatistic getNumConnReleased(){
       numConnReleased.setCount(pool_.getNumConnReleased());
       return (CountStatistic)numConnReleased.unmodifiableView();
   }
   
   
   public CountStatistic getWaitQueueLength() {
       waitQueueLength.setCount(pool_.getNumThreadWaiting());
       return (CountStatistic)waitQueueLength.unmodifiableView();
   }
   //END - New Statistics for 8.1
   
   //START - New Statistics for 9.0
   public CountStatistic getNumConnSuccessfullyMatched() {
   	   numConnMatched.setCount(pool_.getNumConnSuccessfullyMatched());
   	   return (CountStatistic)numConnMatched.unmodifiableView();
   }
   
   public CountStatistic getNumConnNotSuccessfullyMatched() {
	   numConnNotMatched.setCount(pool_.getNumConnNotSuccessfullyMatched());
   	   return (CountStatistic)numConnNotMatched.unmodifiableView();
   }
   
   public CountStatistic getNumPotentialConnLeak() {
	   numPotentialConnLeak.setCount(pool_.getNumPotentialConnLeak());
   	   return (CountStatistic)numPotentialConnLeak.unmodifiableView();
   }
   
   private String getLocalizedStringFor(String key, String defaultValue){ 
       return localStrings.getStringWithDefault(key , defaultValue);
   }
}
