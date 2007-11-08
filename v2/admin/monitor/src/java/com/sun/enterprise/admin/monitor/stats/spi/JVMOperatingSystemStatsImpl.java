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
import java.lang.management.OperatingSystemMXBean;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.JVMOperatingSystemStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StatisticImpl;
import com.sun.enterprise.util.i18n.StringManager;

public class JVMOperatingSystemStatsImpl implements JVMOperatingSystemStats {
    
    private GenericStatsImpl baseStatsImpl;
    private static final String STATS_INTERFACE_NAME = 
                        "com.sun.enterprise.admin.monitor.stats.JVMOperatingSystemStats";
    private MBeanServer server;
    private MutableCountStatistic availProcs;
    private StringStatistic name;
    private OperatingSystemMXBean bean;
    final long initTime;
    private static final StringManager localStrMgr = 
                StringManager.getManager(JVMOperatingSystemStatsImpl.class);
    

    /** Creates a new instance of JVMOperatingSystemStatsImpl */
    public JVMOperatingSystemStatsImpl () {
        
        initTime = System.currentTimeMillis ();
        try {
            baseStatsImpl = new GenericStatsImpl(STATS_INTERFACE_NAME, this);
        } catch(Exception e) {
            
        }
        // get an instance of the MBeanServer
        // server = getPlatformMBeanServer();
        bean = ManagementFactory.getOperatingSystemMXBean();
        
        // initialize all the MutableStatistic Classes
        initializeStatistics();

    }
    
    public StringStatistic getName() {
        
        String name = bean.getName();
        
        return new StringStatisticImpl(name, 
                   localStrMgr.getString("monitor.stats.name_string"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.name_of_os"), 
                   initTime,
                   System.currentTimeMillis()); 
    }
    
    public StringStatistic getArch () {
        
        String arch = bean.getArch();
        
        return new StringStatisticImpl(arch, 
                   localStrMgr.getString("monitor.stats.arch_string"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.arch_of_os"), 
                   initTime,
                   System.currentTimeMillis()); 
    }
    
    public CountStatistic getAvailableProcessors () {
        long procs = bean.getAvailableProcessors();
        availProcs.setCount (procs);
        return (CountStatistic)availProcs.unmodifiableView ();
    }
    
    public StringStatistic getVersion () {
        
        String version = bean.getVersion();
        
        return new StringStatisticImpl(version, 
                   localStrMgr.getString("monitor.stats.vers_string"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.vers_of_os"), 
                   initTime,
                   System.currentTimeMillis()); 
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
        
       // Initialize the MutableCountStatistic for AvailableProcessors
        CountStatistic c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.avail_procs"),
            StatisticImpl.DEFAULT_UNIT,
            localStrMgr.getString("monitor.stats.avail_procs_desc"));
        availProcs = new MutableCountStatisticImpl(c);
    }            
}
