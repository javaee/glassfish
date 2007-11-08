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
package com.sun.enterprise.admin.wsmgmt.stats.impl;

import javax.management.j2ee.statistics.CountStatistic;
import com.sun.appserv.management.j2ee.statistics.StringStatistic;
import com.sun.appserv.management.j2ee.statistics.NumberStatistic;

import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
//import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.appserv.management.j2ee.statistics.StringStatisticImpl;
import com.sun.appserv.management.j2ee.statistics.NumberStatisticImpl;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;

import com.sun.enterprise.admin.wsmgmt.stats.spi.WebServiceEndpointStatsProvider;
import com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats;

import java.util.logging.Logger;
import com.sun.enterprise.log.Log;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * A Class for providing stats for Web Service Endpoint.
 *
 * @author Satish Viswanatham
 */
public class WebServiceEndpointStatsImpl
    extends StatsImpl
    implements WebServiceEndpointAggregateStats
{
    private WebServiceEndpointStatsProvider delegate;

    private MutableCountStatisticImpl	    averageResponseTime;
    private MutableCountStatisticImpl	    responseTime;
    private MutableCountStatisticImpl	    minResponseTime;
    private MutableCountStatisticImpl	    maxResponseTime;
    private MutableCountStatisticImpl		totalFaults;
    private MutableCountStatisticImpl		totalSuccesses;
    private NumberStatisticImpl		throughput;
    private MutableCountStatisticImpl		totalAuthFailures;
    private MutableCountStatisticImpl		totalAuthSuccesses;
    private MutableCountStatisticImpl		requestSize;
    private MutableCountStatisticImpl		responseSize;
    private StringStatisticImpl		faultCode;
    private StringStatisticImpl		faultString;
    private StringStatisticImpl		faultActor;
    private StringStatisticImpl		clientHost;
    private StringStatisticImpl		clientUser;
    private static final StringManager _stringMgr =
            StringManager.getManager(WebServiceEndpointStatsProvider.class);
    

    public WebServiceEndpointStatsImpl(
        WebServiceEndpointStatsProvider delegate){
        this.delegate = delegate;

        initialize();
    }

    protected void initialize() {
       super.initialize(
       "com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats");

        averageResponseTime = new MutableCountStatisticImpl(
            new CountStatisticImpl("AverageResponseTime", DEFAULT_TIME_UNIT));
        minResponseTime = new MutableCountStatisticImpl(
            new CountStatisticImpl("MinResponseTime", DEFAULT_TIME_UNIT));
        maxResponseTime = new MutableCountStatisticImpl(
            new CountStatisticImpl("MaxResponseTime", DEFAULT_TIME_UNIT));
        responseTime = new MutableCountStatisticImpl(
            new CountStatisticImpl("ResponseTime", DEFAULT_TIME_UNIT));
        totalFaults = new MutableCountStatisticImpl(
            new CountStatisticImpl("TotalFaults"));
        totalSuccesses = new MutableCountStatisticImpl(
            new CountStatisticImpl("TotalNumSuccess"));
        totalAuthFailures = new MutableCountStatisticImpl(
            new CountStatisticImpl("TotalAuthFailures"));
        totalAuthSuccesses = new MutableCountStatisticImpl(
            new CountStatisticImpl("TotalAuthSuccesses"));
        long t = System.currentTimeMillis();
        String description = _stringMgr.getString("throughput.description");
        throughput = new NumberStatisticImpl(
            "Throughput", description, "Requests Per Second", t, t,
            new Double(0.0));
        requestSize = new MutableCountStatisticImpl(
            new CountStatisticImpl("RequestSize", DEFAULT_SIZE_UNIT));
        responseSize = new MutableCountStatisticImpl(
            new CountStatisticImpl("ResponseSize", DEFAULT_SIZE_UNIT));

    }

    public CountStatistic getAverageResponseTime(){
        averageResponseTime.setCount(delegate.getAverageResponseTime());
        return (CountStatistic) averageResponseTime.modifiableView();
    }

    public CountStatistic getResponseTime(){
        responseTime.setCount(delegate.getResponseTime());
        return (CountStatistic) responseTime.modifiableView();
    }

    public CountStatistic getMinResponseTime(){
        minResponseTime.setCount(delegate.getMinResponseTime());
        return (CountStatistic) minResponseTime.modifiableView();
    }

    public CountStatistic getMaxResponseTime(){
        maxResponseTime.setCount(delegate.getMaxResponseTime());
        return (CountStatistic) maxResponseTime.modifiableView();
    }

    public CountStatistic getTotalFaults(){
	    totalFaults.setCount(delegate.getTotalFailures());
        return (CountStatistic) totalFaults.modifiableView();
    }

    public CountStatistic getTotalNumSuccess(){
        totalSuccesses.setCount(delegate.getTotalSuccesses());
        return (CountStatistic) totalSuccesses.modifiableView();
    }

    public CountStatistic getTotalAuthFailures(){
        totalAuthFailures.setCount(delegate.getTotalAuthFailures());
        return (CountStatistic) totalAuthFailures.modifiableView();
    }

    public CountStatistic getTotalAuthSuccesses() {
        totalAuthSuccesses.setCount(delegate.getTotalAuthSuccesses());
        return (CountStatistic) totalAuthSuccesses.modifiableView();
    }

    public NumberStatistic getThroughput(){
        throughput.setNumber(new Double(delegate.getThroughput()));
        return (NumberStatistic) throughput;
    }


    private static String DEFAULT_TIME_UNIT = "milliseconds";
    private static String DEFAULT_SIZE_UNIT = "bytes";
}
