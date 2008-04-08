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

import java.util.LinkedList;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.PWCRequestStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;

/**
 * Implementation of the PWCRequestStats interface, which exposes
 * statistics about all HTTP listeners associated with a given virtual
 * server.
 */
public class PWCRequestStatsImpl implements PWCRequestStats {
    
    private GenericStatsImpl baseStatsImpl;

    private long startTime;

    /*
     * The HTTP listeners associated with the virtual server whose stats
     * are being exposed.
     */
    private LinkedList<HTTPListenerStatsImpl> httpListenerStats;

    private MutableCountStatistic countRequests;
    private MutableCountStatistic countBytesReceived;
    private MutableCountStatistic countBytesTransmitted;
    private MutableCountStatistic rateBytesTransmitted;
    private MutableCountStatistic maxByteTransmissionRate;
    private MutableCountStatistic countOpenConnections;
    private MutableCountStatistic maxOpenConnections;
    private MutableCountStatistic count2xx;
    private MutableCountStatistic count3xx;
    private MutableCountStatistic count4xx;
    private MutableCountStatistic count5xx;
    private MutableCountStatistic countOther;
    private MutableCountStatistic count200;
    private MutableCountStatistic count302;
    private MutableCountStatistic count304;
    private MutableCountStatistic count400;
    private MutableCountStatistic count401;
    private MutableCountStatistic count403;
    private MutableCountStatistic count404;
    private MutableCountStatistic count503;
	

    /** 
     * Constructor.
     *
     * @param domain Domain name
     */
    public PWCRequestStatsImpl(String domain) {

        httpListenerStats = new LinkedList();
       
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCRequestStats.class,
            this);
        
        // Initialize all the MutableStatistic Classes
        initializeStatistics();
    }

    /**
     * Adds the given <code>HTTPListenerStatsImpl</code> to the list of
     * <code>HTTPListenerStatsImpl</code> from which this 
     * <code>PWCRequestStatsImpl</code> gathers its stats.
     *
     * @param stats The <code>HTTPListenerStatsImpl</code> instance to add
     * to the list
     */
    public void addHttpListenerStats(HTTPListenerStatsImpl stats) {
        httpListenerStats.add(stats);
    }

    /** 
     * Gets the method of the last request serviced.
     *
     * @return Method of the last request serviced
     */    
    public StringStatistic getMethod() {

        String lastMethod = "unknown";
        long lastRequestTime = 0;

        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            HTTPListenerStatsImpl listener = httpListenerStats.get(i);
            if (listener.getLastRequestCompletionTime() > lastRequestTime) {
                lastRequestTime = listener.getLastRequestCompletionTime();
                lastMethod = listener.getLastRequestMethod();
            }
        }

        return new StringStatisticImpl(
                                lastMethod,
                                "method",
                                "String",
                                "Method of the last request serviced",
                                startTime,
                                System.currentTimeMillis());
    }

    /**
     * Gets the URI of the last request serviced.
     *
     * @return URI of the last request serviced
     */    
    public StringStatistic getUri() {

        String lastURI = "unknown";
        long lastRequestTime = 0;

        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            HTTPListenerStatsImpl listener = httpListenerStats.get(i);
            if (listener.getLastRequestCompletionTime() > lastRequestTime) {
                lastRequestTime = listener.getLastRequestCompletionTime();
                lastURI = listener.getLastRequestURI();
            }
        }

        return new StringStatisticImpl(
                                lastURI,
                                "uri",
                                "String",
                                "URI of the last request serviced",
                                startTime,
                                System.currentTimeMillis());
    }
    
    /** 
     * Gets the number of requests serviced.
     *
     * @return Number of requests serviced
     */    
    public CountStatistic getCountRequests() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getRequestCountLong();
        }

        countRequests.setCount(count);
        return (CountStatistic)countRequests.unmodifiableView();
    }

    /** 
     * Gets the number of bytes received.
     *
     * @return Number of bytes received, or 0 if this information is
     * not available
     */    
    public CountStatistic getCountBytesReceived() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getBytesReceivedLong();
        }

        countBytesReceived.setCount(count);
        return (CountStatistic)countBytesReceived.unmodifiableView();
    }
    
    /** 
     * Gets the number of bytes transmitted.
     *
     * @return Number of bytes transmitted, or 0 if this information
     * is not available
     */    
    public CountStatistic getCountBytesTransmitted() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getBytesSentLong();
        }

        countBytesTransmitted.setCount(count);
        return (CountStatistic)countBytesTransmitted.unmodifiableView();
    }
    
    /** 
     * Gets the rate (in bytes per second) at which data was transmitted
     * over some server-defined interval.
     * 
     * @return Rate (in bytes per second) at which data was
     * transmitted over some server-defined interval, or 0 if this
     * information is not available
     */    
    public CountStatistic getRateBytesTransmitted() {
        rateBytesTransmitted.setCount(0);
        return (CountStatistic)rateBytesTransmitted.unmodifiableView();
    }
    
    /** 
     * Gets the maximum rate at which data was transmitted over some
     * server-defined interval.
     *
     * @return Maximum rate at which data was transmitted over some
     * server-defined interval, or 0 if this information is not available.
     */    
    public CountStatistic getMaxByteTransmissionRate() {
        maxByteTransmissionRate.setCount(0);
        return (CountStatistic)maxByteTransmissionRate.unmodifiableView();
    }
    
    /** 
     * Gets the number of open connections.
     *
     * @return Number of open connections, or 0 if this information
     * is not available
     */    
    public CountStatistic getCountOpenConnections() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCountOpenConnectionsLong();
        }

        countOpenConnections.setCount(count);
        return (CountStatistic)countOpenConnections.unmodifiableView();
    }
    
    /** 
     * Gets the maximum number of open connections.
     *
     * @return Maximum number of open connections, or 0 if this
     * information is not available
     */    
    public CountStatistic getMaxOpenConnections() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getMaxOpenConnectionsLong();
        }

        maxOpenConnections.setCount(count);
        return (CountStatistic)maxOpenConnections.unmodifiableView();
    }

    /** 
     * Gets the number of 200-level responses sent.
     *
     * @return Number of 200-level responses sent
     */    
    public CountStatistic getCount2xx() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount2xxLong();
        }

        count2xx.setCount(count);
        return (CountStatistic)count2xx.unmodifiableView();
    }
    
    /**
     * Gets the number of 300-level responses sent.
     *
     * @return Number of 300-level responses sent
     */    
    public CountStatistic getCount3xx() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount3xxLong();
        }

        count3xx.setCount(count);
        return (CountStatistic)count3xx.unmodifiableView();
    }

    /**
     * Gets the number of 400-level responses sent.
     *
     * @return Number of 400-level responses sent
     */    
    public CountStatistic getCount4xx() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount4xxLong();
        }

        count4xx.setCount(count);
        return (CountStatistic)count4xx.unmodifiableView();
    }

    /**
     * Gets the number of 500-level responses sent.
     *
     * @return Number of 500-level responses sent
     */    
    public CountStatistic getCount5xx() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount5xxLong();
        }

        count5xx.setCount(count);
        return (CountStatistic)count5xx.unmodifiableView();
    }
    
    /**
     * Gets the number of responses sent that were not 200, 300, 400,
     * or 500 level.
     *
     * @return Number of responses sent that were not 200, 300, 400,
     * or 500 level
     */    
    public CountStatistic getCountOther() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCountOtherLong();
        }

        countOther.setCount(count);
        return (CountStatistic)countOther.unmodifiableView();
    }

    /**
     * Gets the number of responses with a 200 response code.
     *
     * @return Number of responses with a 200 response code
     */    
    public CountStatistic getCount200() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount200Long();
        }

        count200.setCount(count);
        return (CountStatistic)count200.unmodifiableView();
    }

    /**
     * Gets the number of responses with a 302 response code.
     *
     * @return Number of responses with a 302 response code
     */    
    public CountStatistic getCount302() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount302Long();
        }

        count302.setCount(count);
        return (CountStatistic)count302.unmodifiableView();
    }

    /**
     * Gets the number of responses with a 304 response code.
     *
     * @return Number of responses with a 304 response code
     */    
    public CountStatistic getCount304() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount304Long();
        }

        count304.setCount(count);
        return (CountStatistic)count304.unmodifiableView();
    }

    /**
     * Gets the number of responses with a 400 response code.
     *
     * @return Number of responses with a 400 response code
     */    
    public CountStatistic getCount400() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount400Long();
        }

        count400.setCount(count);
        return (CountStatistic)count400.unmodifiableView();
    }
    
    /**
     * Gets the number of responses with a 401 response code.
     *
     * @return Number of responses with a 401 response code
     */    
    public CountStatistic getCount401() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount401Long();
        }

        count401.setCount(count);
        return (CountStatistic)count401.unmodifiableView();
    }
    
    /**
     * Gets the number of responses with a 403 response code.
     *
     * @return Number of responses with a 403 response code
     */    
    public CountStatistic getCount403() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount403Long();
        }

        count403.setCount(count);
        return (CountStatistic)count403.unmodifiableView();
    }

    /**
     * Gets the number of responses with a 404 response code.
     *
     * @return Number of responses with a 404 response code
     */    
    public CountStatistic getCount404() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount404Long();
        }

        count404.setCount(count);
        return (CountStatistic)count404.unmodifiableView();
    }

    /**
     * Gets the number of responses with a 503 response code.
     *
     * @return Number of responses with a 503 response code
     */    
    public CountStatistic getCount503() {

        long count = 0;
        int size = httpListenerStats.size();
        for (int i=0; i<size; i++) {
            count += httpListenerStats.get(i).getCount503Long();
        }

        count503.setCount(count);
        return (CountStatistic)count503.unmodifiableView();
    }

    /**
     * Gets all statistics exposed by this PWCRequestStatsImpl
     * 
     * @return Array of all statistics exposed by this PWCRequestStatsImpl
     */
    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }

    /**
     * Gets the statistic with the given name.
     *
     * @param name Statistic name
     *
     * @return Statistic with the given name
     */ 
    public Statistic getStatistic(String name) {
        return baseStatsImpl.getStatistic(name);
    }

    /**
     * Gets the names of all statistics exposed by this PWCRequestStatsImpl
     *
     * @return Array of the names of all statistics exposed by this
     * PWCRequestStatsImpl
     */ 
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

   
    private void initializeStatistics() {

        startTime = System.currentTimeMillis();
        
        CountStatistic c = new CountStatisticImpl("CountRequests");
        countRequests = new MutableCountStatisticImpl(c);
        
        c = new CountStatisticImpl("CountBytesReceived");
        countBytesReceived = new MutableCountStatisticImpl(c);
        
        c = new CountStatisticImpl("CountBytesTransmitted");
        countBytesTransmitted = new MutableCountStatisticImpl(c);
        
        c = new CountStatisticImpl("RateBytesTransmitted");
        rateBytesTransmitted = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxByteTransmissionRate");
        maxByteTransmissionRate = new MutableCountStatisticImpl(c);
        
        c = new CountStatisticImpl("CountOpenConnections");
        countOpenConnections = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxOpenConnections");
        maxOpenConnections = new MutableCountStatisticImpl(c);
        
        c = new CountStatisticImpl("Count2xx");
        count2xx = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count3xx");
        count3xx = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count4xx");
        count4xx = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count5xx");
        count5xx = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountOther");
        countOther = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count200");
        count200 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count302");
        count302 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count304");
        count304 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count400");
        count400 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count401");
        count401 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count403");
        count403 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count404");
        count404 = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("Count503");
        count503 = new MutableCountStatisticImpl(c);
    }

}
