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

package com.sun.enterprise.web.stats;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Statistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.PWCConnectionQueueStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;

/**
 * Implementation of PWCConnectionQueueStats interface.
 */
public final class PWCConnectionQueueStatsImpl implements PWCConnectionQueueStats {

    private static final Logger _logger
        = LogDomains.getLogger(PWCConnectionQueueStatsImpl.class, LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;

    private MBeanServer server;
    private ObjectName connectionQueueName;

    private StringStatistic id;
    private MutableCountStatistic countTotalConnections;
    private MutableCountStatistic countQueued;
    private MutableCountStatistic peakQueued;
    private MutableCountStatistic maxQueued;
    private MutableCountStatistic countOverflows;	
    private MutableCountStatistic countTotalQueued;
    private MutableCountStatistic ticksTotalQueued;
    private MutableCountStatistic countQueued1MinuteAverage;
    private MutableCountStatistic countQueued5MinuteAverage;
    private MutableCountStatistic countQueued15MinuteAverage;


    /** 
     * Constructor.
     *
     * @param domain Domain name
     */
    public PWCConnectionQueueStatsImpl(String domain) {
       
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCConnectionQueueStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        String objNameStr = domain + ":type=PWCConnectionQueue,*";
        try {
            connectionQueueName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            String msg = _logger.getResourceBundle().getString(
                                    "webcontainer.objectNameCreationError");
            msg = MessageFormat.format(msg, new Object[] { objNameStr });
            _logger.log(Level.SEVERE, msg, t);
        }

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }


    /** 
     * Gets the ID of the connection queue
     *
     * @return The ID of the connection queue
     */
    public StringStatistic getId() {
        return id;
    }


    /**
     * Gets the total number of connections that have been accepted.
     *
     * @return Total number of connections that have been accepted.
     */    
    public CountStatistic getCountTotalConnections() {
        countTotalConnections.setCount(
            StatsUtil.getAggregateStatistic(server, connectionQueueName,
                                            "countTotalConnections"));
        return (CountStatistic)countTotalConnections.unmodifiableView();
    }
    

    /**
     * Gets the number of connections currently in the queue
     *
     * @return Number of connections currently in the queue
     */    
    public CountStatistic getCountQueued() {
        countQueued.setCount(
            StatsUtil.getAggregateStatistic(server, connectionQueueName,
                                            "countQueued"));
        return (CountStatistic)countQueued.unmodifiableView();
    }

    
    /**
     * Gets the largest number of connections that were in the queue
     * simultaneously.
     *
     * @return Largest number of connections that were in the queue
     * simultaneously
     */    
    public CountStatistic getPeakQueued() {
        peakQueued.setCount(
            StatsUtil.getAggregateStatistic(server, connectionQueueName,
                                            "peakQueued"));
        return (CountStatistic)peakQueued.unmodifiableView();
    }

    
    /**
     * Gets the maximum size of the connection queue
     *
     * @return Maximum size of the connection queue
     */    
    public CountStatistic getMaxQueued() {
        maxQueued.setCount(
            StatsUtil.getConstant(server, connectionQueueName, "maxQueued"));
        return (CountStatistic)maxQueued.unmodifiableView();
    }

    
    /** 
     * Gets the number of times the queue has been too full to accommodate
     * a connection
     *
     * @return Number of times the queue has been too full to accommodate
     * a connection
     */    
    public CountStatistic getCountOverflows() {
        countOverflows.setCount(
            StatsUtil.getAggregateStatistic(server, connectionQueueName,
                                            "countOverflows"));
        return (CountStatistic)countOverflows.unmodifiableView();
    }


    /** 
     * Gets the total number of connections that have been queued.
     *
     * A given connection may be queued multiple times, so
     * <code>counttotalqueued</code> may be greater than or equal to
     * <code>counttotalconnections</code>.
     *
     * @return Total number of connections that have been queued
     */        
    public CountStatistic getCountTotalQueued() {
        countTotalQueued.setCount(
            StatsUtil.getAggregateStatistic(server, connectionQueueName,
                                            "countTotalQueued"));
        return (CountStatistic)countTotalQueued.unmodifiableView();
    }


    /**
     * Gets the total number of ticks that connections have spent in the
     * queue.
     * 
     * A tick is a system-dependent unit of time.
     *
     * @return Total number of ticks that connections have spent in the
     * queue
     */
    public CountStatistic getTicksTotalQueued() {
        ticksTotalQueued.setCount(
            StatsUtil.getAverageStatistic(server, connectionQueueName,
                                            "ticksTotalQueued"));
        return (CountStatistic)ticksTotalQueued.unmodifiableView();
    }
    

    /** 
     * Gets the average number of connections queued in the last 1 minute
     *
     * @return Average number of connections queued in the last 1 minute
     */    
    public CountStatistic getCountQueued1MinuteAverage() {
        countQueued1MinuteAverage.setCount(
            StatsUtil.getAverageStatistic(server, connectionQueueName,
                                          "countQueued1MinuteAverage"));
        return (CountStatistic)countQueued1MinuteAverage.unmodifiableView();
    }


    /** 
     * Gets the average number of connections queued in the last 5 minutes
     *
     * @return Average number of connections queued in the last 5 minutes
     */    
    public CountStatistic getCountQueued5MinuteAverage() {
        countQueued5MinuteAverage.setCount(
            StatsUtil.getAverageStatistic(server, connectionQueueName,
                                          "countQueued5MinuteAverage"));
        return (CountStatistic)countQueued5MinuteAverage.unmodifiableView();
    }


    /** 
     * Gets the average number of connections queued in the last 15 minutes
     *
     * @return Average number of connections queued in the last 15 minutes
     */    
    public CountStatistic getCountQueued15MinuteAverage() {
        countQueued15MinuteAverage.setCount(
            StatsUtil.getAverageStatistic(server, connectionQueueName,
                                          "countQueued15MinuteAverage"));
        return (CountStatistic)countQueued15MinuteAverage.unmodifiableView();
    }

    
    /**
     * This method can be used to retrieve all the Statistics, exposed
     * by this implementation of Stats
     * @return Statistic[]
     */
    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }
    

    /**
     * Queries for a statistic with the given name.
     *
     * @param name Name of the statistic to query for
     *
     * @return Statistic for the given name
     */ 
    public Statistic getStatistic(String name) {
        return baseStatsImpl.getStatistic(name);
    }

    
    /**
     * Gets array of all statistic names exposed by this implementation of
     * <codeStats</code>
     *
     * @return Array of statistic names
     */ 
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

    
    private void initializeStatistics() {

        long startTime = System.currentTimeMillis();
        id = new StringStatisticImpl("",
                                     "Id",
                                     "String",
                                     "ID of the connection queue",
                                     startTime,
                                     startTime);

        CountStatistic c = new CountStatisticImpl("CountTotalConnections");
        countTotalConnections = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountQueued");
        countQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("PeakQueued");
        peakQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxQueued");
        maxQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountOverflows");
        countOverflows = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountTotalQueued");
        countTotalQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("TicksTotalQueued");
        ticksTotalQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountQueued1MinuteAverage");
        countQueued1MinuteAverage = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountQueued5MinuteAverage");
        countQueued5MinuteAverage = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountQueued15MinuteAverage");
        countQueued15MinuteAverage = new MutableCountStatisticImpl(c);
    }

}
