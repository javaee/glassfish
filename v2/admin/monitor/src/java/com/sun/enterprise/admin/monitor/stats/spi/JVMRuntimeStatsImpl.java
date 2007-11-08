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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.JVMRuntimeStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.util.i18n.StringManager;

public class JVMRuntimeStatsImpl implements JVMRuntimeStats {
    
    private GenericStatsImpl baseStatsImpl;
    private static final String STATS_INTERFACE_NAME = 
                        "com.sun.enterprise.admin.monitor.stats.JVMRuntimeStats";
    private MBeanServer server;
    private MutableCountStatistic upTime;
    private StringStatistic name;
    private RuntimeMXBean bean;
    final long initTime;
    private static final StringManager localStrMgr = 
                StringManager.getManager(JVMRuntimeStatsImpl.class);
    

    /** Creates a new instance of JVMRuntimeStatsImpl */
    public JVMRuntimeStatsImpl () {
        
        initTime = System.currentTimeMillis ();
        try {
            baseStatsImpl = new GenericStatsImpl(STATS_INTERFACE_NAME, this);
        } catch(Exception e) {
            
        }
        // get an instance of the MBeanServer
        // server = getPlatformMBeanServer();
        bean = ManagementFactory.getRuntimeMXBean();
        
        // initialize all the MutableStatistic Classes
        initializeStatistics();

    }
    
    public StringStatistic getName() {
        
        String name = bean.getName();
        
        return new StringStatisticImpl(name, 
                   localStrMgr.getString("monitor.stats.name_string"), 
                   localStrMgr.getString("monitor.stats.string_units"), 
                   localStrMgr.getString("monitor.stats.name_of_vm"), 
                   initTime,
                   System.currentTimeMillis()); 

    }
    
    public StringStatistic getBootClasspath () {
        
        String bclasspath = bean.getBootClassPath();
        
        return new StringStatisticImpl(bclasspath, 
               localStrMgr.getString("monitor.stats.boot_class_path_string"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_boot_class_path"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getClassPath () {
        
        String cpath = bean.getClassPath();
        
        return new StringStatisticImpl(cpath, 
               localStrMgr.getString("monitor.stats.class_path_string"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_class_path"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getInputArguments () {
        
        ArrayList inputArgs = new ArrayList();
        inputArgs.addAll(bean.getInputArguments());
        
        return new StringStatisticImpl(inputArgs.toString (), 
               localStrMgr.getString("monitor.stats.input_args_string"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_input_args"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getLibraryPath () {
        
        String libpath = bean.getLibraryPath();
        
        return new StringStatisticImpl(libpath, 
               localStrMgr.getString("monitor.stats.lib_path_string"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_lib_path"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getManagementSpecVersion () {
        
        String ver = bean.getManagementSpecVersion();
        
        return new StringStatisticImpl(ver, 
               localStrMgr.getString("monitor.stats.lib_mgmt_spec_ver"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_mgmt_spec_ver"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getSpecName () {
        
        String specname = bean.getSpecName();
        
        return new StringStatisticImpl(specname, 
               localStrMgr.getString("monitor.stats.spec_name"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_spec"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getSpecVendor () {
        
        String specvendor = bean.getSpecVendor();
        
        return new StringStatisticImpl(specvendor, 
               localStrMgr.getString("monitor.stats.spec_vendor"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_spec_vendor"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getSpecVersion () {
        
        String specver = bean.getSpecVersion();
        
        return new StringStatisticImpl(specver, 
               localStrMgr.getString("monitor.stats.jvm_spec_vers"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_jvm_spec_vers"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public CountStatistic getUptime () {
        long u = bean.getUptime();
        upTime.setCount (u);
        return (CountStatistic)upTime.unmodifiableView ();
    }
    
    public StringStatistic getVmName () {
        
        String vmname = bean.getVmName();
        
        return new StringStatisticImpl(vmname, 
               localStrMgr.getString("monitor.stats.jvm_impl_name"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_jvm_impl"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getVmVendor () {
        
        String vmvendor = bean.getVmVendor();
        
        return new StringStatisticImpl(vmvendor, 
               localStrMgr.getString("monitor.stats.jvm_vendor"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_jvm_vendor"), 
               initTime,
               System.currentTimeMillis()); 
    }
    
    public StringStatistic getVmVersion () {
        
        String vmver = bean.getVmVersion();
        
        return new StringStatisticImpl(vmver, 
               localStrMgr.getString("monitor.stats.jvm_vers"), 
               localStrMgr.getString("monitor.stats.string_units"), 
               localStrMgr.getString("monitor.stats.name_of_jvm_vers"), 
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
        
       // Initialize the MutableCountStatistic for TotalCompilationTime
        CountStatistic c = new CountStatisticImpl(
            localStrMgr.getString("monitor.stats.total_compilation_time"),
            localStrMgr.getString("monitor.stats.milli_sec_units"),
            localStrMgr.getString("monitor.stats.milli_sec_units_desc"));
        upTime = new MutableCountStatisticImpl(c);
    }
    
}
