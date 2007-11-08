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
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StatisticImpl;
import com.sun.enterprise.admin.monitor.stats.JVMMemoryStats;
import com.sun.enterprise.util.i18n.StringManager;

public class JVMMemoryStatsImpl implements JVMMemoryStats {
    
    private GenericStatsImpl baseStatsImpl;
    private static final String STATS_INTERFACE_NAME = 
                        "com.sun.enterprise.admin.monitor.stats.JVMMemoryStats";
    private MutableCountStatistic initHeapSize;
    private MutableCountStatistic usedHeapSize;
    private MutableCountStatistic maxHeapSize;
    private MutableCountStatistic commitHeapSize;
    private MutableCountStatistic initNonHeapSize;
    private MutableCountStatistic maxNonHeapSize;
    private MutableCountStatistic usedNonHeapSize;
    private MutableCountStatistic commitNonHeapSize;
    private MutableCountStatistic objPendingCount;
    private MemoryUsage heapUsage;
    private MemoryUsage nonheapUsage;
    private MemoryMXBean bean;
    private static final String BYTE_UNITS = "monitor.stats.byte_units";
    private static final StringManager localStrMgr = 
                StringManager.getManager(JVMMemoryStatsImpl.class);


    /** Creates a new instance of JVMMemoryStatsImpl */
    public JVMMemoryStatsImpl() {
        
        try {
            baseStatsImpl = new GenericStatsImpl(STATS_INTERFACE_NAME, this);
        } catch(Exception e) {
            
        }
        bean = ManagementFactory.getMemoryMXBean();
        heapUsage = bean.getHeapMemoryUsage();
        nonheapUsage = bean.getNonHeapMemoryUsage();
        
        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }            
    
    
    private void initializeStatistics() {
        String bytes_str = localStrMgr.getString(BYTE_UNITS);

        // Initialize the MutableCountStatistic for InitialHeapSize
        CountStatistic c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.initial_heap_size"), bytes_str,
            localStrMgr.getString("monitor.stats.initial_heap_size_desc"));
        initHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MaxHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.max_heap_size"), bytes_str,
            localStrMgr.getString("monitor.stats.max_heap_size_desc"));
        maxHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for UsedHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.used_heap_size"), bytes_str,
            localStrMgr.getString("monitor.stats.used_heap_size_desc"));
        usedHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for CommittedHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.committed_heap_size"), bytes_str,
            localStrMgr.getString("monitor.stats.committed_heap_size_desc"));
        commitHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for InitialNonHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.initial_non_heap_size"), bytes_str,
            localStrMgr.getString("monitor.stats.initial_non_heap_size_desc"));
        initNonHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MaxNonHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.max_non_heap_size"),bytes_str,
            localStrMgr.getString("monitor.stats.max_non_heap_size_desc"));
        maxNonHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for UsedNonHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.used_non_heap_size") ,bytes_str,
            localStrMgr.getString("monitor.stats.used_non_heap_size_desc"));
        usedNonHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for CommittedNonHeapSize
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.committed_non_heap_size"),
            bytes_str,
            localStrMgr.getString("monitor.stats.committed_non_heap_size_desc"));
        commitNonHeapSize = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for ObjectsPendingFinalization
        c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.objs_pending_serialization"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.objs_pending_serialization_desc"));
        objPendingCount = new MutableCountStatisticImpl(c);
    }
    

    public CountStatistic getCommittedHeapSize() {
        commitHeapSize.setCount(heapUsage.getCommitted());
        return (CountStatistic)commitHeapSize.unmodifiableView();
    }
    
    public CountStatistic getCommittedNonHeapSize() {
        commitNonHeapSize.setCount(nonheapUsage.getCommitted());
        return (CountStatistic)commitNonHeapSize.unmodifiableView();
    }
    
    public CountStatistic getInitHeapSize() {
        initHeapSize.setCount(heapUsage.getInit());
        return (CountStatistic)initHeapSize.unmodifiableView();
    }
    
    public CountStatistic getInitNonHeapSize() {
        initNonHeapSize.setCount(nonheapUsage.getCommitted());
        return (CountStatistic)initNonHeapSize.unmodifiableView();
    }
    
    public CountStatistic getMaxHeapSize() {
        maxHeapSize.setCount(heapUsage.getMax());
        return (CountStatistic)maxHeapSize.unmodifiableView();
    }
    
    public CountStatistic getMaxNonHeapSize() {
        maxNonHeapSize.setCount(nonheapUsage.getInit());
        return (CountStatistic)maxNonHeapSize.unmodifiableView();
    }
    
    public CountStatistic getObjectPendingFinalizationCount() {
        objPendingCount.setCount(bean.getObjectPendingFinalizationCount());
        return (CountStatistic)objPendingCount.unmodifiableView();
    }
    
    public CountStatistic getUsedHeapSize() {
        usedHeapSize.setCount(heapUsage.getUsed());
        return (CountStatistic)usedHeapSize.unmodifiableView();
    }
    
    public CountStatistic getUsedNonHeapSize() {
        usedNonHeapSize.setCount(nonheapUsage.getUsed());
        return (CountStatistic)usedNonHeapSize.unmodifiableView();
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
}
