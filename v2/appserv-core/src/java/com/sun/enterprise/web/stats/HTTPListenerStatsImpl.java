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

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Statistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.HTTPListenerStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;


/**
 * Implementation of the HTTPListenerStats interface
 * All the methods defined in this interface are serviced by querying 
 * Tomcat's MBeans for ThreadPool or GlobalRequestProcessor
 * @author Murali Vempaty
 * @since S1AS8.0
 * @version 1.0
 */
public final class HTTPListenerStatsImpl implements HTTPListenerStats {
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;

    private MBeanServer server;
    private ObjectName tpName;  // ObjectName for ThreadPoolMBean
    private ObjectName grpName; // ObjectName for GlobalRequestProcessor MBean
    private MutableCountStatistic bytesReceived;
    private MutableCountStatistic bytesSent;
    private MutableCountStatistic errorCount;
    private MutableCountStatistic maxTime;
    private MutableCountStatistic processingTime;
    private MutableCountStatistic requestCount;
    private MutableCountStatistic curThreadCount;
    private MutableCountStatistic curThreadsBusy;
    private MutableCountStatistic maxThreads;
    private MutableCountStatistic maxSpareThreads;
    private MutableCountStatistic minSpareThreads;
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
    private MutableCountStatistic countOpenConnections;
    private MutableCountStatistic maxOpenConnections;
	

    /**
     * Creates a new instance of HTTPListenerStatsImpl 
     * The ObjectNames of the ThreadPool & GlobalRequestProcessor MBeans
     * follow the pattern:
     * <domain>:type=ThreadPool,name=http<port>
     * <domain>:type=GlobalRequestProcessor,name=http<port>
     * for example: server:type=ThreadPool,name=http1043
     *
     * @param domain    domain in which Tomcat's MBeans are registered
     *                  This is usually the name of the virtual-server,
     *                  to which this listener belongs
     *
     * @param port      port at which the listener is receiving requests
     */
    public HTTPListenerStatsImpl(String domain, int port) {

        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.HTTPListenerStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        // construct the ObjectNames of the GlobalRequestProcessor & 
        // ThreadPool MBeans
        // TODO: use an ObjectNameFactory to construct the ObjectNames
        // of the MBeans, instead of hardcoding
        String objNameStr = domain + ":type=Selector,name=http" + port;
        try {
            tpName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            String msg = _logger.getResourceBundle().getString(
                                    "webcontainer.objectNameCreationError");
            msg = MessageFormat.format(msg, new Object[] { objNameStr });
            _logger.log(Level.SEVERE, msg, t);
        }

        objNameStr = domain + ":type=GlobalRequestProcessor"
                + ",name=http" + port;
        try {
            grpName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            String msg = _logger.getResourceBundle().getString(
                                    "webcontainer.objectNameCreationError");
            msg = MessageFormat.format(msg, new Object[] { objNameStr });
            _logger.log(Level.SEVERE, msg, t);
        }
        
        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
    // GlobalRequestProcessor Statistics
    public CountStatistic getBytesReceived() {
        bytesReceived.setCount(getBytesReceivedLong());
        return (CountStatistic)bytesReceived.unmodifiableView();
    }
    
    public CountStatistic getBytesSent() {
        bytesSent.setCount(getBytesSentLong());
        return (CountStatistic)bytesSent.unmodifiableView();
    }
    
    public CountStatistic getProcessingTime() {
        Object countObj = StatsUtil.getStatistic(server, grpName, "processingTime");
        processingTime.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)processingTime.unmodifiableView();
    }
    
    public CountStatistic getRequestCount() {
        requestCount.setCount(getRequestCountLong());
        return (CountStatistic)requestCount.unmodifiableView();
    }
    
    public CountStatistic getErrorCount() {
        Object countObj = StatsUtil.getStatistic(server, grpName, "errorCount");
        errorCount.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)errorCount.unmodifiableView();
    }
    
    public CountStatistic getMaxTime() {
        Object countObj = StatsUtil.getStatistic(server, grpName, "maxTime");
        maxTime.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)maxTime.unmodifiableView();
    }


    /**
     * Returns the number of responses with a status code in the 2xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 2xx range
     */
    public CountStatistic getCount2xx() {
        count2xx.setCount(getCount2xxLong());
        return (CountStatistic)count2xx.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code in the 3xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 3xx range
     */
    public CountStatistic getCount3xx() {
        count3xx.setCount(getCount3xxLong());
        return (CountStatistic)count3xx.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code in the 4xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 4xx range
     */
    public CountStatistic getCount4xx() {
        count4xx.setCount(getCount4xxLong());
        return (CountStatistic)count4xx.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code in the 5xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 5xx range
     */
    public CountStatistic getCount5xx() {
        count5xx.setCount(getCount5xxLong());
        return (CountStatistic)count5xx.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code outside the 2xx,
     * 3xx, 4xx, and 5xx range, sent by the HTTP listener whose statistics
     * are exposed by this <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code outside the 2xx, 3xx,
     * 4xx, and 5xx range
     */
    public CountStatistic getCountOther() {
        countOther.setCount(getCountOtherLong());
        return (CountStatistic)countOther.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 200
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 200
     */
    public CountStatistic getCount200() {
        count200.setCount(getCount200Long());
        return (CountStatistic)count200.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 302
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 302
     */
    public CountStatistic getCount302() {
        count302.setCount(getCount302Long());
        return (CountStatistic)count302.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 304
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 304
     */
    public CountStatistic getCount304() {
        count304.setCount(getCount304Long());
        return (CountStatistic)count304.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 400
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 400
     */
    public CountStatistic getCount400() {
        count400.setCount(getCount400Long());
        return (CountStatistic)count400.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 401
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 401
     */
    public CountStatistic getCount401() {
        count401.setCount(getCount401Long());
        return (CountStatistic)count401.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 403
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 403
     */
    public CountStatistic getCount403() {
        count403.setCount(getCount403Long());
        return (CountStatistic)count403.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 404
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 404
     */
    public CountStatistic getCount404() {
        count404.setCount(getCount404Long());
        return (CountStatistic)count404.unmodifiableView();
    }

    /**
     * Returns the number of responses with a status code equal to 503
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 503
     */
    public CountStatistic getCount503() {
        count503.setCount(getCount503Long());
        return (CountStatistic)count503.unmodifiableView();
    }

    /**
     * Returns the number of open connections managed by the HTTP listener
     * whose statistics are exposed by this <code>HTTPListenerStats</code>.
     *
     * @return Number of open connections
     */
    public CountStatistic getCountOpenConnections() {
        countOpenConnections.setCount(getCountOpenConnectionsLong());
        return (CountStatistic)countOpenConnections.unmodifiableView();
    }

    /**
     * Returns the maximum number of open connections managed by the HTTP
     * listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Maximum number of open connections
     */
    public CountStatistic getMaxOpenConnections() {
        maxOpenConnections.setCount(getMaxOpenConnectionsLong());
        return (CountStatistic)maxOpenConnections.unmodifiableView();
    }

    // ThreadPool Statistics

    public CountStatistic getCurrentThreadCount() {
        Object countObj = StatsUtil.getStatistic(server, tpName,
                                                 "currentThreadCountStats");
        curThreadCount.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)curThreadCount.unmodifiableView();
    }
    
    public CountStatistic getCurrentThreadsBusy() {
        Object countObj = StatsUtil.getStatistic(server, tpName,
                                                 "currentThreadsBusyStats");
        curThreadsBusy.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)curThreadsBusy.unmodifiableView();
    }
    
    public CountStatistic getMaxSpareThreads() {
        Object countObj = StatsUtil.getStatistic(server, tpName,
                                                 "maxSpareThreadsStats");
        maxSpareThreads.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)maxSpareThreads.unmodifiableView();
    }
    
    public CountStatistic getMaxThreads() {
        Object countObj = StatsUtil.getStatistic(server, tpName,
                                                 "maxThreadsStats");
        maxThreads.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)maxThreads.unmodifiableView();
    }
    
    public CountStatistic getMinSpareThreads() {
        Object countObj = StatsUtil.getStatistic(server, tpName,
                                                 "minSpareThreadsStats");
        minSpareThreads.setCount(StatsUtil.getLongValue(countObj));
        return (CountStatistic)minSpareThreads.unmodifiableView();
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
     * queries for a Statistic by name.
     * @return  Statistic
     */ 
    public Statistic getStatistic(String str) {
        return baseStatsImpl.getStatistic(str);
    }
    
    /**
     * returns an array of names of all the Statistics, that can be
     * retrieved from this implementation of Stats
     * @return  String[]
     */ 
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }


    /*
     * Package-protected methods
     */

    long getBytesReceivedLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "bytesReceived");
        return StatsUtil.getLongValue(countObj);
    }

    long getBytesSentLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "bytesSent");
        return StatsUtil.getLongValue(countObj);
    }

    long getRequestCountLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "requestCount");
        return StatsUtil.getLongValue(countObj);
    }

    long getCountOpenConnectionsLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "countOpenConnections");
        return StatsUtil.getLongValue(countObj);
    }

    long getMaxOpenConnectionsLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "maxOpenConnections");
        return StatsUtil.getLongValue(countObj);

    }

    long getCount2xxLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count2xx");
        return StatsUtil.getLongValue(countObj);

    }

    long getCount3xxLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count3xx");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount4xxLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count4xx");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount5xxLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count5xx");
        return StatsUtil.getLongValue(countObj);
    }

    long getCountOtherLong() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "countOther");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount200Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count200");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount302Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count302");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount304Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count304");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount400Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count400");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount401Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count401");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount403Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count403");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount404Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count404");
        return StatsUtil.getLongValue(countObj);
    }

    long getCount503Long() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "count503");
        return StatsUtil.getLongValue(countObj);
    }


    /**
     * Gets the URI of the last request serviced by the HTTP listener
     * associated with this HTTPListenerStatsImpl.
     *
     * @return The URI of the last request serviced
     */
    String getLastRequestURI() {
        return (String) StatsUtil.getStatistic(server, grpName,
                                               "lastRequestURI");
    }

    /**
     * Gets the HTTP method of the last request serviced by the HTTP listener
     * associated with this HTTPListenerStatsImpl.
     *
     * @return The HTTP method of the last request serviced
     */
    String getLastRequestMethod() {
        return (String) StatsUtil.getStatistic(server, grpName,
                                               "lastRequestMethod");
    }

    /**
     * Gets the time (in milliseconds since January 1, 1970, 00:00:00) when
     * the last request serviced by the HTTP listener associated with this
     * HTTPListenerStatsImpl was completed.
     *
     * @return The time when the last request was completed.
     */
    long getLastRequestCompletionTime() {
        Object countObj = StatsUtil.getStatistic(server, grpName,
                                                 "lastRequestCompletionTime");
        return StatsUtil.getLongValue(countObj);
    }


    /*
     * Private methods
     */
   
    private void initializeStatistics() {
        
        // Initialize the MutableCountStatistic for BytesReceived
        CountStatistic c = new CountStatisticImpl("BytesReceived");
        bytesReceived = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for BytesSent
        c = new CountStatisticImpl("BytesSent");
        bytesSent = new MutableCountStatisticImpl(c);

        // Initialize the MutableCountStatistic for ErrorCount
        c = new CountStatisticImpl("ErrorCount");
        errorCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MaxTime
        c = new CountStatisticImpl("MaxTime", "milliseconds");
        maxTime = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for ProcessingTime
        c = new CountStatisticImpl("ProcessingTime", "milliseconds");
        processingTime = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for RequestCount
        c = new CountStatisticImpl("RequestCount");
        requestCount = new MutableCountStatisticImpl(c);

        // Initialize the MutableCountStatistic for CurrentThreadCount
        c = new CountStatisticImpl("CurrentThreadCount");
        curThreadCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for CurrentThreadsBusy
        c = new CountStatisticImpl("CurrentThreadsBusy");
        curThreadsBusy = new MutableCountStatisticImpl(c);

        // Initialize the MutableCountStatistic for MaxThreads
        c = new CountStatisticImpl("MaxThreads");
        maxThreads = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MaxSpareThreads
        c = new CountStatisticImpl("MaxSpareThreads");
        maxSpareThreads = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MinSpareThreads
        c = new CountStatisticImpl("MinSpareThreads");
        minSpareThreads = new MutableCountStatisticImpl(c);

        // Init Count2xx
        c = new CountStatisticImpl("Count2xx");
        count2xx = new MutableCountStatisticImpl(c);

        // Init Count3xx
        c = new CountStatisticImpl("Count3xx");
        count3xx = new MutableCountStatisticImpl(c);

        // Init Count4xx
        c = new CountStatisticImpl("Count4xx");
        count4xx = new MutableCountStatisticImpl(c);

        // Init Count5xx
        c = new CountStatisticImpl("Count5xx");
        count5xx = new MutableCountStatisticImpl(c);

        // Init CountOther
        c = new CountStatisticImpl("CountOther");
        countOther = new MutableCountStatisticImpl(c);

        // Init Count200
        c = new CountStatisticImpl("Count200");
        count200 = new MutableCountStatisticImpl(c);

        // Init Count302
        c = new CountStatisticImpl("Count302");
        count302 = new MutableCountStatisticImpl(c);

        // Init Count304
        c = new CountStatisticImpl("Count304");
        count304 = new MutableCountStatisticImpl(c);

        // Init Count400
        c = new CountStatisticImpl("Count400");
        count400 = new MutableCountStatisticImpl(c);

        // Init Count401
        c = new CountStatisticImpl("Count401");
        count401 = new MutableCountStatisticImpl(c);

        // Init Count403
        c = new CountStatisticImpl("Count403");
        count403 = new MutableCountStatisticImpl(c);

        // Init Count404
        c = new CountStatisticImpl("Count404");
        count404 = new MutableCountStatisticImpl(c);

        // Init Count503
        c = new CountStatisticImpl("Count503");
        count503 = new MutableCountStatisticImpl(c);

        // Init CountOpenConnections
        c = new CountStatisticImpl("CountOpenConnections");
        countOpenConnections = new MutableCountStatisticImpl(c);

        // Init MaxOpenConnections
        c = new CountStatisticImpl("MaxOpenConnections");
        maxOpenConnections = new MutableCountStatisticImpl(c);
    }

}
