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
import com.sun.appserv.management.j2ee.statistics.CountStatisticImpl;
import com.sun.appserv.management.j2ee.statistics.StringStatistic;
import com.sun.appserv.management.j2ee.statistics.StringStatisticImpl;
import com.sun.appserv.management.monitor.statistics.LoadBalancerServerStats;
import com.sun.enterprise.admin.monitor.stats.lb.ClusterStats;
import com.sun.enterprise.admin.monitor.stats.lb.InstanceStats;
import com.sun.enterprise.admin.monitor.stats.lb.LoadBalancerStatsInterface;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.pluggable.LBFeatureFactory;

public final class LoadBalancerServerStatsImpl implements CustomStatsImpl {
    
    /** Returns the statistics for a load-balanced server instance
     * @return an array of {@link Statistic}     
     */
    public Statistic[] getStatistics() {
        LoadBalancerStatsInterface lbstats = lbff.getLoadBalancerMonitoringStats(lbConfigName,lbName);
        Statistic[] statArr1 = new Statistic[3];
        StringStatistic stat11 = new StringStatisticImpl("Health", "Instance Health", "state of the server", 0, 0, "");
        CountStatistic stat21 = new CountStatisticImpl("NumberOfActiveRequests", "Number Of Active Requests", "count", 0, 0, 0);
        CountStatistic stat31 = new CountStatisticImpl("NumberOfTotalRequests", "Number Of Total Requests", "count", 0, 0, 0);
        statArr1[0]=stat11;
        statArr1[1]=stat21;
        statArr1[2]=stat31;
        if(lbstats == null)
            return statArr1;
        ClusterStats [] cstats = lbstats.getClusterStats();
        for(ClusterStats cstat :cstats){
            for(InstanceStats istat : cstat.getInstanceStats()){
                if(!istat.getId().equals(instanceName))
                    continue;
                    Statistic[] statArr = new Statistic[3];
                    String health = istat.getHealth();
                    long activeReq = Long.parseLong(istat.getNumActiveRequests());
                    long totalReq = Long.parseLong(istat.getNumTotalRequests());
                    long sampleTime = new Date().getTime();
                    StringStatistic stat1 = new StringStatisticImpl("Health", "Instance Health", "milliseconds", startTime, sampleTime, health);
                    CountStatistic stat2 = new CountStatisticImpl("NumberOfActiveRequests", "Number Of Active Requests", "count", startTime, sampleTime, activeReq);
                    CountStatistic stat3 = new CountStatisticImpl("NumberOfTotalRequests", "Number Of Total Requests", "count", startTime, sampleTime, totalReq);
                    statArr[0] = stat1;
                    statArr[1] = stat2;
                    statArr[2] = stat3;
                    return statArr;
            }
        }
        return null;
    }

    /** Returns the server's health status - Healthy, Unhealthy or Quiesced.
     * @return an instance of {@link StringStatistic}     
     */
    public StringStatistic getHealth() {
        Statistic[] statArr = getStatistics();
        return (StringStatistic)statArr[0];
    }
    
    /** Returns the number of active requests on this instance
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getNumberOfActiveRequests() {
        Statistic[] statArr = getStatistics();
        return (CountStatistic)statArr[1];
    }
    
    /** Returns the total number of requests for this instance.
     * @return an instance of {@link CountStatistic}     
     */
    public CountStatistic getNumberOfTotalRequests() {
        Statistic[] statArr = getStatistics();
        return (CountStatistic)statArr[2];
    }


    public LoadBalancerServerStatsImpl(String lbName,String lbConfigName,String clusterName,String instanceName) {
        this.lbName=lbName;
        this.clusterName=clusterName;
        this.instanceName=instanceName;
        this.lbConfigName=lbConfigName;
        this.lbff = ApplicationServer.getServerContext().getPluggableFeatureFactory().getLBFeatureFactory();
        this.startTime=new Date().getTime();
   }
    
    public LoadBalancerServerStatsImpl() {
    }
    
    private String instanceName = null;
    private String lbName = null;
    private String lbConfigName = null;
    private String clusterName = null;
    private LBFeatureFactory lbff = null;
    private long startTime = 0;

    
}
