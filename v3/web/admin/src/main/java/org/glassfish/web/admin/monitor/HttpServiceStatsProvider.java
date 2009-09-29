/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.web.admin.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.annotations.Reset;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.external.statistics.impl.TimeStatisticImpl;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
@AMXMetadata(type="request-mon", group="monitoring")
@ManagedObject
@Description( "Web Container HTTP Service Statistics" )
public class HttpServiceStatsProvider implements PostConstruct {
    //Provides the longest response time for a request - not a cumulative value, 
    //but the largest response time from among the response times.
    //private Counter maxTime = CounterFactory.createCount();
    //Provides cumulative value of the times taken to process each request. 
    //The processing time is the average of request processing times over the request count.
    //private Counter processingTime = CounterFactory.createCount();
    //Provides cumulative number of the requests processed so far.
    //private Counter requestCount = CounterFactory.createCount();
    //Provides the cumulative value of the error count. The error count represents 
    //the number of cases where the response code was greater than or equal to 400.
    private CountStatisticImpl errorCount = new CountStatisticImpl("ErrorCount",
            StatisticImpl.UNIT_COUNT,
            "Cumulative value of the error count, with error count representing the number of cases where the response code was greater than or equal to 400");
    private CountStatisticImpl maxTime = new CountStatisticImpl("MaxTime",
            StatisticImpl.UNIT_MILLISECOND,
            "Longest response time for a request; not a cumulative value, but the largest response time from among the response times");
    private CountStatisticImpl processingTime = new CountStatisticImpl("ProcessingTime",
            StatisticImpl.UNIT_MILLISECOND,
            "Cumulative value of the times taken to process each request, with processing time being the average of request processing times over the request count");
    private CountStatisticImpl requestCount = new CountStatisticImpl("RequestCount",
            StatisticImpl.UNIT_COUNT,
            "Cumulative number of requests processed so far");
    private CountStatisticImpl count200 = new CountStatisticImpl("Count200", "count", "Number of responses with a status code equal to 200");
    private CountStatisticImpl count2xx = new CountStatisticImpl("Count2xx", "count", "Number of responses with a status code in the 2xx range");
    private CountStatisticImpl count302 = new CountStatisticImpl("Count302", "count", "Number of responses with a status code equal to 302");
    private CountStatisticImpl count304 = new CountStatisticImpl("Count304", "count", "Number of responses with a status code equal to 304");
    private CountStatisticImpl count3xx = new CountStatisticImpl("Count3xx", "count", "Number of responses with a status code in the 3xx range");
    private CountStatisticImpl count400 = new CountStatisticImpl("Count400", "count", "Number of responses with a status code equal to 400");
    private CountStatisticImpl count401 = new CountStatisticImpl("Count401", "count", "Number of responses with a status code equal to 401");
    private CountStatisticImpl count403 = new CountStatisticImpl("Count403", "count", "Number of responses with a status code equal to 403");
    private CountStatisticImpl count404 = new CountStatisticImpl("Count404", "count", "Number of responses with a status code equal to 404");
    private CountStatisticImpl count4xx = new CountStatisticImpl("Count4xx", "count", "Number of responses with a status code in the 4xx range");
    private CountStatisticImpl count503 = new CountStatisticImpl("Count503", "count", "Number of responses with a status code equal to 503");
    private CountStatisticImpl count5xx = new CountStatisticImpl("Count5xx", "count", "Number of responses with a status code in the 5xx range");
    private CountStatisticImpl countOther = new CountStatisticImpl("CountOther", "count", "Number of responses with a status code outside the 2xx, 3xx, 4xx, and 5xx range");
    private TimeStatisticImpl requestProcessTime = new TimeStatisticImpl(0L, 0L, 0L, 0L,
            "", "", "", System.currentTimeMillis(), -1L);
    private Logger logger = Logger.getLogger(HttpServiceStatsProvider.class.getName());
    private String virtualServerName = null;

    private ThreadLocal<TimeStatData> individualData = new ThreadLocal<TimeStatData> (){

        TimeStatData tsd;

        protected TimeStatData initialValue (){
            tsd = new TimeStatData ();
            return tsd;
        }
        public TimeStatData get (){
            if (tsd == null)
                tsd = new TimeStatData();
            return tsd;
        }

    };

    public HttpServiceStatsProvider(String vsName) {
        this.virtualServerName = vsName;
    }


    public void postConstruct() {
    }

    @ManagedAttribute(id="maxtime")
    @Description( "Provides the longest response time for a response - not a cumulative value, but the largest response time from among response times." )
    public CountStatistic getMaxTime() {
        maxTime.setCount(requestProcessTime.getMaxTime());
        return maxTime;
    }

    @ManagedAttribute(id="requestcount")
    @Description( "Provides cumulative number of requests processed so far" )
    public CountStatistic getCount() {
        requestCount.setCount(requestProcessTime.getCount());
        return requestCount;
    }

    @ManagedAttribute(id="processingtime")
    @Description( "Provides cumulative value of the times taken to process each request The processing time is the average request processing times over the request count." )
    public CountStatistic getTime() {
        processingTime.setCount(this.getProcessTime());
        return processingTime;
    }

    @ManagedAttribute(id="errorcount")
    @Description( "" )
    public CountStatistic getErrorCount() {
        return errorCount;
    }
    
    @ManagedAttribute(id="count200")
    @Description( "" )
    public CountStatistic getCount200() {
        return count200.getStatistic();
    }
    
    @ManagedAttribute(id="count2xx")
    @Description( "" )
    public CountStatistic getCount2xx() {
        return count2xx.getStatistic();
    }
    
    @ManagedAttribute(id="count302")
    @Description( "" )
    public CountStatistic getCount302() {
        return count302.getStatistic();
    }
    
    @ManagedAttribute(id="count304")
    @Description( "" )
    public CountStatistic getCount304() {
        return count304.getStatistic();
    }

    @ManagedAttribute(id="count3xx")
    @Description( "" )
    public CountStatistic getCount3xxt() {
        return count3xx.getStatistic();
    }

    @ManagedAttribute(id="count400")
    @Description( "" )
    public CountStatistic getCount400() {
        return count400.getStatistic();
    }

    @ManagedAttribute(id="count401")
    @Description( "" )
    public CountStatistic getCount401() {
        return count401.getStatistic();
    }

    @ManagedAttribute(id="count403")
    @Description( "" )
    public CountStatistic getCount403() {
        return count403.getStatistic();
    }

    @ManagedAttribute(id="count404")
    @Description( "" )
    public CountStatistic getCount404() {
        return count404.getStatistic();
    }

    @ManagedAttribute(id="count4xx")
    @Description( "" )
    public CountStatistic getCount4xx() {
        return count4xx.getStatistic();
    }

    @ManagedAttribute(id="count503")
    @Description( "" )
    public CountStatistic getCount503() {
        return count503.getStatistic();
    }

    @ManagedAttribute(id="count5xx")
    @Description( "" )
    public CountStatistic getCount5xx() {
        return this.count5xx.getStatistic();
    }

    @ManagedAttribute(id="countother")
    @Description( "" )
    public CountStatistic getCountOther() {
        return this.countOther.getStatistic();
    }

    @ProbeListener("glassfish:web:http-service:requestStartEvent")
    public void requestStartEvent(
            @ProbeParam("hostName") String hostName,
            @ProbeParam("serverName") String serverName,
            @ProbeParam("serverPort") int serverPort,
            @ProbeParam("contextPath") String contextPath,
            @ProbeParam("servletPath") String servletPath) {
        if ((hostName != null) && (hostName.equals(virtualServerName))) {
            individualData.get().setEntryTime(System.currentTimeMillis());
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(
                    "[TM]requestStartEvent received - virtual-server = " +
                    serverName + " : port = " + serverPort);
            }
        }
    }

    @ProbeListener("glassfish:web:http-service:requestEndEvent")
    public void requestEndEvent(
            @ProbeParam("hostName") String hostName,
            @ProbeParam("serverName") String serverName,
            @ProbeParam("serverPort") int serverPort,
            @ProbeParam("contextPath") String contextPath,
            @ProbeParam("servletPath") String servletPath,
            @ProbeParam("statusCode") int statusCode) {
        if ((hostName != null) && (hostName.equals(virtualServerName))) {
            TimeStatData tsd = individualData.get();
            tsd.setExitTime(System.currentTimeMillis());
            requestProcessTime.incrementCount(tsd.getTotalTime());
            incrementStatsCounter(statusCode);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(
                    "[TM]requestEndEvent received - virtual-server = " +
                    serverName + ": application = " +
                    contextPath + " : servlet = " +
                    servletPath + " :Response code = " +
                    statusCode + " :Response time = " +
                    tsd.getTotalTime());
            }
        }
    }

    
    public long getProcessTime() {
        long count = requestProcessTime.getCount();
        long processTime = -1L;
        if (count != 0) {
            processTime = requestProcessTime.getTotalTime()/count;
        }
        return processTime;
    }

    private void incrementStatsCounter(int statusCode) {
        switch (statusCode) {
            case 200:
                count200.increment();
                break;
            case 302:
                count302.increment();
                break;
            case 304:
                count304.increment();
                break;
            case 400:
                count400.increment();
                break;
            case 401:
                count401.increment();
                break;
            case 403:
                count403.increment();
                break;
            case 404:
                count404.increment();
                break;
            case 503:
                count503.increment();
                break;
            default:
                break;
        }

        if (200 <= statusCode && statusCode <=299) {
            count2xx.increment();
        } else if (300 <= statusCode && statusCode <=399) {
            count3xx.increment();
        } else if (400 <= statusCode && statusCode <=499) {
            count4xx.increment();
        } else if (500 <= statusCode && statusCode <=599) {
            count5xx.increment();
        } else {
            countOther.increment();
        }

        if (statusCode >= 400)
            errorCount.increment();
    }

    //Need to add this because requestProcessTime needs to be reset.
    //Since it is not exposed as public statistic the reset() would not get
    //called on its TimeStatisticImpl object so we need to use @Reset instead.
    //If @Reset is used then reset() won't be called on the statistic impl objects
    //so all the stats need to reset here as well.
    @Reset
    public void reset() {
        this.requestProcessTime.reset();
        this.count200.reset();
        this.count2xx.reset();
        this.count302.reset();
        this.count304.reset();
        this.count3xx.reset();
        this.count400.reset();
        this.count401.reset();
        this.count403.reset();
        this.count404.reset();
        this.count4xx.reset();
        this.count503.reset();
        this.count5xx.reset();
        this.countOther.reset();
        this.errorCount.reset();
        this.maxTime.reset();
        this.processingTime.reset();
        this.requestCount.reset();
    }

    private class TimeStatData {
        private long entryTime = 0;
        private long exitTime = 0;
        private long totalTime = 0;


        public long getEntryTime() {
            return entryTime;
        }

        public void setEntryTime(long entryTime) {
            this.entryTime = entryTime;
        }

        public long getExitTime() {
            return exitTime;
        }

        public void setExitTime(long exitTime) {
            this.exitTime = exitTime;
        }

        public long getTotalTime() {
            totalTime = exitTime - entryTime;
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }
        public void setReset (){
            entryTime = 0;
            exitTime = 0;
            totalTime = 0;
        }
    }
}
