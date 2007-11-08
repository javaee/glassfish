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
import java.util.ArrayList;
import java.lang.management.ThreadInfo;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.JVMThreadInfoStats;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StatisticImpl;
import com.sun.enterprise.util.i18n.StringManager;

public class JVMThreadInfoStatsImpl implements JVMThreadInfoStats {
    
    private GenericStatsImpl baseStatsImpl;
    private static final String STATS_INTERFACE_NAME = 
                        "com.sun.enterprise.admin.monitor.stats.JVMThreadInfoStats";
    private static final StringManager localStrMgr = 
                StringManager.getManager(JVMThreadStatsImpl.class);

    private ThreadInfo info;
    private long initTime;
    MutableCountStatistic blockedCount;
    MutableCountStatistic blockedTime;
    MutableCountStatistic lockOwnerId;
    MutableCountStatistic threadId;
    MutableCountStatistic waitingCount;
    MutableCountStatistic waitingTime;
    static String NEWLINE = "\n";
    

    /** Creates a new instance of JVMThreadInfoStatsImpl */
    public JVMThreadInfoStatsImpl(ThreadInfo tInfo) {
        try {
            baseStatsImpl = new GenericStatsImpl(STATS_INTERFACE_NAME, this);
        } catch(Exception e) {
        }
        initTime = System.currentTimeMillis();
        info = tInfo;
        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
    public CountStatistic getBlockedCount() {
        long blockCount = info.getBlockedCount();
        blockedCount.setCount(blockCount);
        return (CountStatistic)blockedCount.unmodifiableView ();
    }
    
    public CountStatistic getBlockedTime() {
        long blockTime = info.getBlockedTime();
        blockedTime.setCount(blockTime);
        return (CountStatistic)blockedTime.unmodifiableView ();
    }
    
    public StringStatistic getLockName() {
             
        String lockName = info.getLockName();
        if((lockName == null) || ("".equals(lockName)))
            lockName = localStrMgr.getString("monitor.stats.no_lock");

        return new StringStatisticImpl(lockName, 
                   localStrMgr.getString("monitor.stats.lock_name"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.lock_desc"), 
                   initTime,
                   System.currentTimeMillis());

    }
    
    public CountStatistic getLockOwnerId() {
        long id = info.getLockOwnerId();
        lockOwnerId.setCount(id);
        return (CountStatistic)lockOwnerId.unmodifiableView ();
    }
    
    public StringStatistic getLockOwnerName() {
        String lockOwnerName = info.getLockOwnerName();
        if((lockOwnerName == null) || ( "".equals(lockOwnerName)))
            lockOwnerName = localStrMgr.getString("monitor.stats.no_owner");

        return new StringStatisticImpl(lockOwnerName, 
                   localStrMgr.getString("monitor.stats.lock_owner_name"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.lock_owner_desc"), 
                   initTime,
                   System.currentTimeMillis());
    }
    
    public StringStatistic getStackTrace() {
        StackTraceElement[] trace = info.getStackTrace();
        String traceString = new String();
        if(trace != null) {
            for(int i = 0; i < trace.length; i++) {
                traceString = traceString.concat(trace[i].toString());
                traceString = traceString.concat(NEWLINE);
            }
        }
        return new StringStatisticImpl(traceString, 
                   localStrMgr.getString("monitor.stats.stack_trace_name"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.stack_trace_desc"), 
                   initTime,
                   System.currentTimeMillis());
        
        
    }
    
    public CountStatistic getThreadId() {
        long id = info.getThreadId();
        threadId.setCount(id);
        return (CountStatistic)threadId.unmodifiableView ();
    }
    
    public StringStatistic getThreadName() {
     
        String name = info.getThreadName();

        return new StringStatisticImpl(name, 
                   localStrMgr.getString("monitor.stats.thread_name"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.thread_name_desc"), 
                   initTime,
                   System.currentTimeMillis());
    }
    
    public StringStatistic getThreadState() {
        
        return new StringStatisticImpl(info.getThreadState().toString(),
                   localStrMgr.getString("monitor.stats.thread_state"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.thread_state_desc"), 
                   initTime,
                   System.currentTimeMillis());
    }
    
    public CountStatistic getWaitedCount() {
        long waitCount = info.getWaitedCount();
        waitingCount.setCount(waitCount);
        return (CountStatistic)waitingCount.unmodifiableView ();
    }
    
    public CountStatistic getWaitedTime() {
        long waitTime = info.getWaitedTime();
        waitingTime.setCount(waitTime);
        return (CountStatistic)waitingTime.unmodifiableView ();
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
        
        // Initialize the MutableCountStatistic for BlockedTime
        CountStatistic c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.blocked_time"), 
            localStrMgr.getString("monitor.stats.milli_sec_units"),
            localStrMgr.getString("monitor.stats.blocked_time_desc"));
        blockedTime = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for BlockedCount
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.blocked_count"), 
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.blocked_count_desc"));
        blockedCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for LockOwnerId
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.lock_owner_id"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.lock_owner_id_desc"));
        blockedCount = new MutableCountStatisticImpl(c);
        lockOwnerId = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for ThreadId
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.thread_id"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.thread_id_desc"));
        threadId = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for WaitingCount
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.waiting_count"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.waiting_count_desc"));
        waitingCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for WaitingTime
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.waiting_time"), 
            localStrMgr.getString("monitor.stats.milli_sec_units"),
            localStrMgr.getString("monitor.stats.waiting_time_desc")); 
        waitingTime = new MutableCountStatisticImpl(c);
    }
}
