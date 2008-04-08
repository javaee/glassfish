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

package com.sun.enterprise.web.stats;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.ServletStats;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.web.monitor.PwcServletStats;


public class ServletStatsImpl implements ServletStats {
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;
    private MutableCountStatistic maxTimeMillis;
    private MutableCountStatistic processingTimeMillis;
    private TimeStatistic serviceTimeMillis;
    private MutableCountStatistic requestCount;
    private MutableCountStatistic errorCount;
    private PwcServletStats pwcServletStats;

    
    /**
     * Constructor.
     *
     * @param pwcServletStats PwcServletStats instance to which to delegate
     */
    public ServletStatsImpl(PwcServletStats pwcServletStats) {

        this.pwcServletStats = pwcServletStats;

        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.ServletStats.class, this);

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
    
    /**
     * The maximum processing time of a servlet request
     * @return CountStatistic
     */
    public CountStatistic getMaxTime() {
        maxTimeMillis.setCount(pwcServletStats.getMaxTimeMillis());
        return (CountStatistic)maxTimeMillis.unmodifiableView();
    }

    
    /**
     * Gets the total execution time of the servlet's service method.
     *
     * @return Total execution time of the servlet's service method
     */
    public CountStatistic getProcessingTime() {
        processingTimeMillis.setCount(pwcServletStats.getProcessingTimeMillis());
        return (CountStatistic)processingTimeMillis.unmodifiableView();
    }
    

    /**
     * Gets the execution time of the servlet's service method as a
     * TimeStatistic.
     *
     * @return Execution time of the servlet's service method
     */
    public TimeStatistic getServiceTime() {
        return serviceTimeMillis;
    }


    /**
     * Number of requests processed by this servlet.
     * @return CountStatistic
     */
    public CountStatistic getRequestCount() {
        requestCount.setCount(pwcServletStats.getRequestCount());
        return (CountStatistic)requestCount.unmodifiableView();
    }

    
    /** 
     * The errorCount represents the number of cases where the response
     * code was >= 400
     * @return CountStatistic
     */
    public CountStatistic getErrorCount() {
        errorCount.setCount(pwcServletStats.getErrorCount());
        return (CountStatistic)errorCount.unmodifiableView();
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
        
       // Initialize the MutableCountStatistic for ErrorCount
        CountStatistic c = new CountStatisticImpl("ErrorCount");
        errorCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MaxTime
        c = new CountStatisticImpl("MaxTime", "milliseconds");
        maxTimeMillis = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for ProcessingTime
        c = new CountStatisticImpl("ProcessingTime", "milliseconds");
        processingTimeMillis = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for RequestCount
        c = new CountStatisticImpl("RequestCount");
        requestCount = new MutableCountStatisticImpl(c);

        // Initialize the MutableTimeStatistic for ServiceTime
        serviceTimeMillis = new ServletTimeStatisticImpl("ServiceTime",
                                                         "milliseconds",
                                                         pwcServletStats);
    }
    
}
