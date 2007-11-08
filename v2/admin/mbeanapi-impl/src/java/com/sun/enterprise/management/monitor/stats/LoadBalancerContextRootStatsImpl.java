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

package com.sun.enterprise.management.monitor.stats;

import java.util.Date;
import java.util.Map;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import com.sun.appserv.management.j2ee.statistics.CountStatisticImpl;
import com.sun.appserv.management.j2ee.statistics.TimeStatisticImpl;
import com.sun.appserv.management.monitor.statistics.LoadBalancerServerStats;
import com.sun.enterprise.admin.monitor.stats.lb.ClusterStats;
import com.sun.enterprise.admin.monitor.stats.lb.InstanceStats;
import com.sun.enterprise.admin.monitor.stats.lb.LoadBalancerStatsInterface;
import com.sun.enterprise.admin.monitor.stats.lb.InstanceStatsInterface;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.pluggable.LBFeatureFactory;


public final class LoadBalancerContextRootStatsImpl implements CustomStatsImpl {

    /** Returns the statistics for a load-balanced server instance
     * @return an array of {@link Statistic}     
     */
    public Statistic[] getStatistics() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface instance =  getInstanceStats(lbstats);
        return createStats(instance);
    }

    /** Returns average response time in milliseconds
     * @return an instance of {@link TimeStatistic}     
     */
    public TimeStatistic getResponseTime() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long avgRespTime = Long.parseLong(
                    istat.getApplicationStatsAverageResponseTime());
            long maxRespTime = Long.parseLong(
                    istat.getApplicationStatsMaxResponseTime());
            long minRespTime = Long.parseLong(
                    istat.getApplicationStatsMinResponseTime());
            long totalRequests = Long.parseLong(
                    istat.getApplicationStatsNumTotalRequests()); 
            long sampleTime = System.currentTimeMillis();
            return createResponseTimeStat(maxRespTime, 
            totalRequests, sampleTime, 
            minRespTime, avgRespTime);
        }
        return null;
    }

    /** Returns average response time in milliseconds
     * @return an instance of {@link TimeStatistic}     
     */
    public CountStatistic getAverageResponseTime() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long avgRespTime = Long.parseLong(
                    istat.getApplicationStatsAverageResponseTime());
            long sampleTime = System.currentTimeMillis();
            return createAverageRespTimeStat(sampleTime,avgRespTime);
        }
        return null;
    }
    
    /** Returns the number of failover requests
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getFailoverReqCount() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long time = Long.parseLong(
                    istat.getApplicationStatsNumFailoverRequests());
            long sampleTime = System.currentTimeMillis();
            return createFailoverReqCountStat(sampleTime,time);
        }
        return null;
    }
    
    /** Returns the number of error requests
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getErrorRequestCount() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long time = Long.parseLong(
                    istat.getApplicationStatsNumErrorRequests());
            long sampleTime = System.currentTimeMillis();
            return createErrorRequestCountStat(sampleTime,time);
        }
        return null;
    }
    
    /** Returns the number of active requests
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getActiveRequestCount() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long time = Long.parseLong(
                    istat.getApplicationStatsNumActiveRequests());
            long sampleTime = System.currentTimeMillis();
            return createActiveRequestCountStat(sampleTime,time);
        }
        return null;
    }

    /** Returns the number of total requests
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getTotalRequestCount() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long time = Long.parseLong(
                    istat.getApplicationStatsNumTotalRequests());
            long sampleTime = System.currentTimeMillis();
            return createTotalRequestCountStat(sampleTime,time);
        }
        return null;
    }

    /** Returns the number of times all idempotent 
     * urls in this application are accessed
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getIdempotentUrlAccessCount() {
        LoadBalancerStatsInterface lbstats = 
                lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        InstanceStatsInterface istat =  getInstanceStats(lbstats);
        if(istat!=null){
            long time = Long.parseLong(
                    istat.getApplicationStatsNumIdempotentUrlRequests());
            long sampleTime = System.currentTimeMillis();
            return createIdempotentUrlAccessCountStat(sampleTime,time);
        }
        return null;
    }
    

    public LoadBalancerContextRootStatsImpl(String lbName,String lbConfigName,
            String clusterName,String instanceName,String appName,
            String contextRoot) {
        this.lbName=lbName;
        this.clusterName=clusterName;
        this.instanceName=instanceName;
        this.appName=appName;
        this.contextRoot=contextRoot;
        this.lbConfigName=lbConfigName;
        this.lbff = ApplicationServer.getServerContext().
                getPluggableFeatureFactory().getLBFeatureFactory();
        this.startTime=System.currentTimeMillis();
    }

    public LoadBalancerContextRootStatsImpl() {
    }

    private String contextRoot = null;
    private String lbName = null;
    private String lbConfigName = null;
    private String clusterName = null;
    private String instanceName = null;
    private String appName = null;
    private LBFeatureFactory lbff = null;
    private long startTime = 0;
    
    private InstanceStatsInterface getInstanceStats(
            final LoadBalancerStatsInterface lbstats) {
        if(lbstats == null)
            return null;
        ClusterStats [] cstats = lbstats.getClusterStats();
        for(ClusterStats cstat :cstats){
            for(InstanceStats istat : cstat.getInstanceStats()){
                if(!istat.getId().equals(instanceName))
                    continue;
                String appid = istat.getApplicationStatsId();
                    if(!appid.equals(contextRoot))
                        continue;
                return istat;

            }
        }
        return null;
    }
    
    private Statistic[] createStats(InstanceStatsInterface istat) {
        Statistic[] emptyStats = createEmptyStats();
        if(istat==null)
            return emptyStats;
        long avgRespTime = Long.parseLong(
                istat.getApplicationStatsAverageResponseTime());
        long maxRespTime = Long.parseLong(
                istat.getApplicationStatsMaxResponseTime());
        long minRespTime = Long.parseLong(
                istat.getApplicationStatsMinResponseTime());
        long noActiveReq = Long.parseLong(
                istat.getApplicationStatsNumActiveRequests());
        long noErrorReq = Long.parseLong(
                istat.getApplicationStatsNumErrorRequests());
        long noFailoverReq = Long.parseLong(
                istat.getApplicationStatsNumFailoverRequests());
        long noIdempotentUrlReq = Long.parseLong(
                istat.getApplicationStatsNumIdempotentUrlRequests());
        long totalRequests = Long.parseLong(
                istat.getApplicationStatsNumTotalRequests());
        long sampleTime = System.currentTimeMillis();
        Statistic[] statArr = new Statistic[7];
        statArr[0] = createResponseTimeStat(maxRespTime, totalRequests, 
                sampleTime, minRespTime, avgRespTime);
        statArr[1] = createAverageRespTimeStat(sampleTime, avgRespTime);
        statArr[2] = createFailoverReqCountStat(sampleTime, noFailoverReq);
        statArr[3] = createErrorRequestCountStat(sampleTime, noErrorReq);
        statArr[4] = createActiveRequestCountStat(sampleTime, noActiveReq);
        statArr[5] = createTotalRequestCountStat(sampleTime, totalRequests);
        statArr[6] = createIdempotentUrlAccessCountStat(sampleTime, 
                noIdempotentUrlReq);

        return statArr;
    }
    
    private CountStatistic createAverageRespTimeStat(final long sampleTime, 
            final long count) {
        return new CountStatisticImpl("AverageResponseTime", 
                "Average Response Time", "MILLISECOND", startTime, sampleTime,count);
    }
    private CountStatistic createFailoverReqCountStat(final long sampleTime, 
            final long count) {
        return new CountStatisticImpl("FailoverReqCount", 
                "Failover Request Count", "count", startTime, sampleTime,count);
    }
    private CountStatistic createErrorRequestCountStat(final long sampleTime, 
            final long count) {
        return new CountStatisticImpl("ErrorRequestCount", 
                "Error Request Count", "count", startTime, sampleTime,count);
    }
    private CountStatistic createActiveRequestCountStat(final long sampleTime, 
            final long count) {
        return new CountStatisticImpl("ActiveRequestCount", 
                "Active Request Count", "count", startTime, sampleTime,count);
    }
    private CountStatistic createTotalRequestCountStat(final long sampleTime, 
            final long count) {
        return new CountStatisticImpl("TotalRequestCount", 
                "Total Request Count", "count", startTime, sampleTime,count);
    }
    private CountStatistic createIdempotentUrlAccessCountStat(final long sampleTime, 
            final long count) {
        return new CountStatisticImpl("IdempotentUrlAccessCount", 
                "Idempotent Url Access Count", "count", startTime, sampleTime,count);
    }

    private TimeStatistic createResponseTimeStat(final long maxRespTime, 
            final long totalRequests, final long sampleTime, 
            final long minRespTime, final long avgRespTime) {
        return new TimeStatisticImpl("ResponseTime", "Response Time", 
                "MILLISECOND", startTime, sampleTime,totalRequests,maxRespTime,
                minRespTime, totalRequests*avgRespTime);
    }

    private Statistic[] createEmptyStats() {
        Statistic[] statArr1 = new Statistic[7];
        statArr1[0] = createResponseTimeStat(-1, -1, -1, -1, -1);
        statArr1[1] = createAverageRespTimeStat(-1, -1);
        statArr1[2] = createFailoverReqCountStat(-1, -1);
        statArr1[3] = createErrorRequestCountStat(-1, -1);
        statArr1[4] = createActiveRequestCountStat(-1, -1);
        statArr1[5] = createTotalRequestCountStat(-1, -1);
        statArr1[6] = createIdempotentUrlAccessCountStat( -1, -1);
        return statArr1;
    }
        
    
}
