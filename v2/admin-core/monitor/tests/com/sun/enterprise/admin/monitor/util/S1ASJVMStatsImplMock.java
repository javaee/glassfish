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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * S1ASJVMStatsImpl.java
 *
 * Created on August 1, 2003, 10:44 AM
 */

package com.sun.enterprise.admin.monitor.util;
import javax.management.j2ee.statistics.*;
import java.lang.reflect.*;
import java.util.logging.*;
/**
 * Mock class for using JVMStats as a component that will register and
 * unregister with a MonitoringRegistration object which in turn created a 
 * dynamic MBean that will be registered with the MBeanServer using an ObjectName
 * that has a unique identifier.
 * @author  sg112326
 */
public class S1ASJVMStatsImplMock implements S1ASJVMStatsMock {
    private long initTime;
    public static final String LOGGER_NAME="this.is.console";
    final Logger logger; 

    /** Creates a new instance of JVMStatsImpl */
    public S1ASJVMStatsImplMock() {
        initTime = System.currentTimeMillis();
        logger = Logger.getLogger(LOGGER_NAME);
    }
    
    public javax.management.j2ee.statistics.Statistic getStatistic(String statisticName) {
        if(statisticName != null){
            try{
                return (Statistic)(this.getClass().getMethod("get"+statisticName, null)).invoke(this,null);
            }
            catch(Exception e){
               e.printStackTrace();
            }
        }        
        throw new NullPointerException("Cannot find Statistic as Statistic Name is not provided");
    }
    
    
    public String[] getStatisticNames() {
        return new String[]{"HeapSize","MaxMemory","UpTime", "AvailableProcessors"};
    }
    
    public javax.management.j2ee.statistics.Statistic[] getStatistics() {
        Statistic[] stats = new Statistic[]{};
        for(int i=0;i<getStatisticNames().length;i++){
            stats[i]=getStatistic(getStatisticNames()[i]);
        }
        return stats;
    }
    
    public CountStatistic getAvailableProcessors(){
        int availProcs = Runtime.getRuntime().availableProcessors();
        return StatisticFactory.getCountStatistic(availProcs, "AvailableProcessors", 
                                                   "Numbers", "Processors Available to the JVM",
                                                   System.currentTimeMillis(), initTime);
    }
    
    public javax.management.j2ee.statistics.BoundedRangeStatistic getHeapSize() {
        long heapSize = Runtime.getRuntime().totalMemory();
        long upper = Runtime.getRuntime().maxMemory();
        return StatisticFactory.getBoundedRangeStatistic(heapSize, heapSize, heapSize, 
                                                         upper, 0, "HeapSize", "bytes", 
                                                         "JVM Heap Size", initTime, 
                                                         System.currentTimeMillis());
    }
    
    public CountStatistic getMaxMemory() {
        long maxMem = Runtime.getRuntime().maxMemory();
        return StatisticFactory.getCountStatistic(maxMem, "MaxMemory", 
                                                   "bytes", "Memory Available to the JVM",
                                                   System.currentTimeMillis(), initTime);
    }
    
    public CountStatistic getUpTime() {
        long curTime = System.currentTimeMillis();
        long upTime = curTime - initTime;
        return StatisticFactory.getCountStatistic(upTime, "UpTime", "milliseconds", 
                                                  "Amount of time the JVM has been running", 
                                                   curTime, initTime);    
    }
}
