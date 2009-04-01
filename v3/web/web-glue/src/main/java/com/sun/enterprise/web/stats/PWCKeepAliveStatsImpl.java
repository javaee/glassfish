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
import com.sun.enterprise.admin.monitor.stats.PWCKeepAliveStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;

/**
 * @author Jan Luehe
 */
public final class PWCKeepAliveStatsImpl implements PWCKeepAliveStats {

    private static final Logger _logger
        = LogDomains.getLogger(PWCKeepAliveStatsImpl.class, LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;

    private MBeanServer server;
    private ObjectName keepAliveName;

    private MutableCountStatistic countConnections;
    private MutableCountStatistic maxConnections;
    private MutableCountStatistic countHits;
    private MutableCountStatistic countFlushes;
    private MutableCountStatistic countRefusals;
    private MutableCountStatistic countTimeouts;
    private MutableCountStatistic secondsTimeouts;
	

    public PWCKeepAliveStatsImpl(String domain) {
       
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCKeepAliveStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        String objNameStr = domain + ":type=PWCKeepAlive,*";
        try {
            keepAliveName = new ObjectName(objNameStr);
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
     * Gets the number of connections in keep-alive mode.
     * 
     * @return Number of connections in keep-alive mode
     */    
    public CountStatistic getCountConnections() {
        countConnections.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countConnections"));
        return (CountStatistic)countConnections.unmodifiableView();
    }
    

    /** 
     * Gets the maximum number of concurrent connections in keep-alive mode.
     *
     * @return Maximum number of concurrent connections in keep-alive mode
     */    
    public CountStatistic getMaxConnections() {
        maxConnections.setCount(
            StatsUtil.getConstant(server, keepAliveName, "maxConnections"));
        return (CountStatistic)maxConnections.unmodifiableView();
    }

    
    /** 
     * Gets the number of requests received by connections in keep-alive mode.
     *
     * @return Number of requests received by connections in keep-alive mode.
     */    
    public CountStatistic getCountHits() {
        countHits.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countHits"));
        return (CountStatistic)countHits.unmodifiableView();
    }

    
    /** 
     * Gets the number of keep-alive connections that were closed
     *
     * @return Number of keep-alive connections that were closed
     */    
    public CountStatistic getCountFlushes() {
        countFlushes.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countFlushes"));
        return (CountStatistic)countFlushes.unmodifiableView();
    }

    
    /** 
     * Gets the number of keep-alive connections that were rejected.
     *
     * @return Number of keep-alive connections that were rejected.
     */    
    public CountStatistic getCountRefusals() {
        countRefusals.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countRefusals"));
        return (CountStatistic)countRefusals.unmodifiableView();
    }

    
    /** 
     * Gets the number of keep-alive connections that timed out.
     *
     * @return Number of keep-alive connections that timed out.
     */    
    public CountStatistic getCountTimeouts() {
        countTimeouts.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countTimeouts"));
        return (CountStatistic)countTimeouts.unmodifiableView();
    }

    
    /** 
     * Gets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @return Keep-alive timeout in number of seconds
     */    
    public CountStatistic getSecondsTimeouts() {
        secondsTimeouts.setCount(
            StatsUtil.getConstant(server, keepAliveName, "secondsTimeouts"));
        return (CountStatistic)secondsTimeouts.unmodifiableView();
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
        
        CountStatistic c = null;

        c = new CountStatisticImpl("CountConnections");
        countConnections = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxConnections");
        maxConnections = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountHits");
        countHits = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountFlushes");
        countFlushes = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountRefusals");
        countRefusals = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountTimeouts");
        countTimeouts = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("SecondsTimeouts");
        secondsTimeouts = new MutableCountStatisticImpl(c);
    }

}
