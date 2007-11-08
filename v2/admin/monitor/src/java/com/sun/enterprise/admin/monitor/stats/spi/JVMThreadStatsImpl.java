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

package com.sun.enterprise.admin.monitor.stats.spi;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.JVMThreadStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StatisticImpl;
import com.sun.enterprise.util.i18n.StringManager;


public class JVMThreadStatsImpl implements JVMThreadStats {
    
    private GenericStatsImpl baseStatsImpl;
    private static final String STATS_INTERFACE_NAME = 
                        "com.sun.enterprise.admin.monitor.stats.JVMThreadStats";
    private ThreadMXBean bean;
    private MutableCountStatistic curThreadCpuTime;
    private MutableCountStatistic daemonThreadCount;
    private MutableCountStatistic peakThreadCount;
    private MutableCountStatistic threadCount;
    private MutableCountStatistic totalStartedThreadCount;
    final long initTime;
    static String DELIMITER = ",";
    

    private static final StringManager localStrMgr = 
                StringManager.getManager(JVMThreadStatsImpl.class);

    /** Creates a new instance of JVMThreadStatsImpl */
    public JVMThreadStatsImpl() {
        initTime = System.currentTimeMillis ();
        try {
            baseStatsImpl = new GenericStatsImpl(STATS_INTERFACE_NAME, this);
        } catch(Exception e) {
            
        }
        
        bean = ManagementFactory.getThreadMXBean();
        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
    public StringStatistic getAllThreadIds() {
        long[] ids = bean.getAllThreadIds();
        String idString = new String();
        for(int i = 0; i < ids.length; i++)
        {
            idString = idString.concat(String.valueOf(ids[i]));
            idString = idString.concat(DELIMITER);
        }

        return new StringStatisticImpl(idString, 
                   localStrMgr.getString("monitor.stats.live_threads"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.live_threads_desc"), 
                   initTime,
                   System.currentTimeMillis()); 
    }
    
    public CountStatistic getCurrentThreadCPUTime() {        
        curThreadCpuTime.setCount(bean.getCurrentThreadCpuTime());
        return (CountStatistic) curThreadCpuTime.unmodifiableView ();
    }
    
    public CountStatistic getDaemonThreadCount() {
        daemonThreadCount.setCount(bean.getDaemonThreadCount());
        return (CountStatistic) daemonThreadCount.unmodifiableView();
    }
    
    public StringStatistic getMonitorDeadlockedThreads() {
        long[] ids = bean.findMonitorDeadlockedThreads();
        String idString = new String();
        if(ids != null) {
            for(int i = 0; i < ids.length; i++) {
                idString = idString.concat(String.valueOf(ids[i]));
                idString = idString.concat(DELIMITER);
            }
        }
        if((ids == null) || (ids.length == 0)) {
            idString = idString.concat(
                localStrMgr.getString("monitor.stats.no_thread_deadlock"));
        }
        return new StringStatisticImpl(idString, 
                   localStrMgr.getString("monitor.stats.dlocked_threads"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.dlocked_threads_desc"), 
                   initTime,
                   System.currentTimeMillis()); 

    }
    
    public CountStatistic getPeakThreadCount() {
        peakThreadCount.setCount(bean.getPeakThreadCount());
        return (CountStatistic) peakThreadCount.unmodifiableView();
    }
    
    public CountStatistic getThreadCount() {
        threadCount.setCount(bean.getThreadCount());
        return (CountStatistic) threadCount.unmodifiableView();
    }
    
    public CountStatistic getTotalStartedThreadCount() {
        totalStartedThreadCount.setCount(bean.getTotalStartedThreadCount());
        return (CountStatistic) totalStartedThreadCount.unmodifiableView();
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
    
    private void initializeStatistics() {
        
        // Initialize the MutableCountStatistic for CurrentThreadCpuTime
        CountStatistic c = new CountStatisticImpl( 
                localStrMgr.getString("thread_cpu_time"),
                localStrMgr.getString("monitor.stats.nano_sec_units"),
                localStrMgr.getString("thread_cpu_time_desc"));
        curThreadCpuTime = new MutableCountStatisticImpl(c);

        // Initialize the MutableCountStatistic for DaemonThreadCount
        c= new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.daemon_thread_count"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.daemon_thread_count_desc"));
        daemonThreadCount = new MutableCountStatisticImpl(c);

        // Initialize the MutableCountStatistic for PeakThreadCount
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.peak_thread_count"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.peak_thread_count_desc"));
        peakThreadCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for ThreadCount
        c = new CountStatisticImpl(
                localStrMgr.getString("monitor.stats.thread_count"),
                StatisticImpl.DEFAULT_UNIT,
                localStrMgr.getString("monitor.stats.thread_count_desc"));
        threadCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for TotalStartedThreadCount
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.started_thread_count"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.started_thread_count_desc"));
        totalStartedThreadCount = new MutableCountStatisticImpl(c);
    }
}
