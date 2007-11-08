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

/*
 * $Id: JVMStatsImpl.java,v 1.3 2005/12/25 04:16:39 tcfujii Exp $
 * $Date: 2005/12/25 04:16:39 $
 * $Revision: 1.3 $
 */

package com.sun.enterprise.server.stats;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.JVMStats;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Provides an implementation of the JVMStats interface, defined as part
 * of JSR77.
 * It specifies the monitoring statistics for a JVM
 */ 

public class JVMStatsImpl implements JVMStats {
    
    private final long initTime;
    private GenericStatsImpl baseStatsImpl;
    private MutableCountStatistic uptime;
    private MutableBoundedRangeStatisticImpl heapSize;
    private static final String STATS_INTERFACE_NAME = 
                        "javax.management.j2ee.statistics.JVMStats";
    private static StringManager sm = 
                StringManager.getManager(JVMStatsImpl.class);                        

    /**
     * Constructor for JVMStatsImpl
     * In addition to initializing the MutableStatistic objects, we also
     * instantiate a GenericStatsImpl object here. All requests for 
     * getStatistics(), getStatistic() & getStatisticNames() are 
     * delegated to this instance of GenericStatsImpl.
     */
    public JVMStatsImpl() {
		
		initTime = System.currentTimeMillis();
		 try {
            baseStatsImpl = new GenericStatsImpl(STATS_INTERFACE_NAME, this);
        }catch(ClassNotFoundException cnfe){
            // TODO: Handle ClassNotFoundException
        }
        
        // Initialize a MutableCountStatistic
        CountStatistic u = new CountStatisticImpl( 
                    sm.getString("jvmstats.jvm_uptime"), 
                    sm.getString("jvmstats.milli_seconds"), 
                    sm.getString("jvmstats.jvm_uptime_desc") );
        uptime = new MutableCountStatisticImpl(u);
        
        
        // Initialize a MutableBoundedRangeStatistic
        long upper = Runtime.getRuntime().maxMemory();
        BoundedRangeStatistic h =  new BoundedRangeStatisticImpl(
                    sm.getString("jvmstats.jvm_heapsize"),
                    sm.getString("jvmstats.bytes"),
                    sm.getString("jvmstats.jvm_heapsize_desc"),
                    0, upper, 0); 
        
        heapSize = new MutableBoundedRangeStatisticImpl(h);
    }
    
    /**
     * Method to query the size of the JVM's heap
     * @return BoundedRangeStatistic
     */
    public BoundedRangeStatistic getHeapSize() {
        long heap = Runtime.getRuntime().totalMemory();
        heapSize.setCount(heap);
        return (BoundedRangeStatistic) heapSize.unmodifiableView();
    }
    
    /**
     * Method to query the amount of time that the JVM has been running
     * @return CountStatistic
     */
    public CountStatistic getUpTime() {
        long curTime = System.currentTimeMillis();
        long upTime = curTime - initTime;
        uptime.setCount(upTime);
        return (CountStatistic) uptime.unmodifiableView();
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
