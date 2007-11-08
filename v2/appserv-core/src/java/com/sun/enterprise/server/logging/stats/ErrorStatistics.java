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
 * ErrorStatistics.java
 * $Id: ErrorStatistics.java,v 1.11 2007/05/05 05:35:46 tcfujii Exp $
 * $Date: 2007/05/05 05:35:46 $
 * $Revision: 1.11 $
 */

package com.sun.enterprise.server.logging.stats;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import com.sun.logging.LogDomains;
import com.sun.appserv.management.ext.logging.LogAnalyzer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.LogService;

/**
 * This keeps track of the error statistics (log levels SEVERE and WARNING).
 *
 * @author Ram Jeyaraman
 */
public class ErrorStatistics {
    
    // Static constants, classes and methods
    
    private static final int MIN_INTERVALS = 5;
    private static final int MAX_INTERVALS = 500;
    private static final long DEFAULT_INTERVAL = 3600*1000; // (1 hour) msecs
     
    private static final ErrorStatistics singleton = new ErrorStatistics();    
    
    public static ErrorStatistics singleton() {
        return singleton;
    }
    
    public static void registerStartupTime() {
        singleton(); // Loads the class which sets up the startup time.
    }
    
    private static class ErrorCount {
        
        private int severeCount;
        private int warningCount;
        
        void setSevereCount(int count) {
            this.severeCount = count;
        }
        
        int getSevereCount() {
            return this.severeCount;
        }
        
        void setWarningCount(int count) {
            this.warningCount = count;
        }
        
        int getWarningCount() {
            return this.warningCount;
        }
    }
    
    // Instance variables and methods
    
    private long interval;
    private int numOfIntervals;
    private long startTimeStamp;
    private HashMap<Long,HashMap<String,ErrorCount>> intervalMap;
    
    ErrorStatistics() {

        interval = DEFAULT_INTERVAL;
        numOfIntervals = MIN_INTERVALS;
        intervalMap = new HashMap<Long,HashMap<String,ErrorCount>>();
        startTimeStamp = System.currentTimeMillis();
    }

    /**
     * Get the number of intevals, for which error statistics is maintained.
     *
     * @return number of intervals.
     */    
    public int getNumOfIntervals() {
        try {
            ServerContext sc = ApplicationServer.getServerContext();
            if (sc != null) {
                LogService ls = ServerBeansFactory.getConfigBean(sc.getConfigContext()).getLogService();
                numOfIntervals = Integer.parseInt(ls.getRetainErrorStatisticsForHours());
            }
        }catch(Exception n) {
            numOfIntervals = MIN_INTERVALS;
        }
        return numOfIntervals;
    }
    
    /**
     * Set the number of time intervals, for which statistics will be 
     * maintained in memory. For example, if the interval is 1 hour, and
     * number of intervals is 10, statistics will be maintained for the most
     * recent 10 hours.
     */
    public void setNumOfIntervals(int intervals) {
        if ((intervals < MIN_INTERVALS) || (intervals > MAX_INTERVALS)) {
            throw new IllegalArgumentException(
                    "Number of intervals must be between " + MIN_INTERVALS +
                    " and " + MAX_INTERVALS);
        }
        numOfIntervals = intervals;
    }
    
    /**
     * Set interval duration. The interval is the duration, in milli seconds,
     * between consecutive time samples. For example, if the interval is 1 hour,
     * statistical data collected over time, will be organized by the hour.
     */
    public void setIntervalDuration(long interval) {
        this.interval = interval;
    }
    
    /**
     * Get the interval duration.
     *
     * @return interval duration, in millsecs.
     */
    public long getIntervalDuration() {
        return this.interval;
    }
    
    /**
     * This method is not synchronized. The caller must ensure that
     * this method is not used in a concurrent fashion. Currently,
     * the only caller is publish() method of FileandSyslogHandler.
     */
    public void updateStatistics(LogRecord record) {
        
        // Get information from log record.
        
        long logTimeStamp = record.getMillis();
        String logModuleId = record.getLoggerName();
        if (logModuleId == null) {
            Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
            logger.log(
                    Level.WARNING, "Update error statistics failed",
                    (new Throwable("Logger name is null.")).fillInStackTrace());
            return;
        }
        Level logLevel = record.getLevel();
        
        // Sanity check.
        /*
        boolean isAnErrorRecord =
            logLevel.equals(Level.SEVERE) || logLevel.equals(Level.WARNING);
        if (!isAnErrorRecord) {
            return; // no op.
        }
        */
        // Calculate the timestamp bucket for the log record.
        
        long hoursElapsed = (logTimeStamp-startTimeStamp)/interval;
        long timeStamp = startTimeStamp + hoursElapsed*interval;
        /*
        System.out.println((new java.text.SimpleDateFormat("HH:mm:ss")).
                                format(new java.util.Date(timeStamp)));
        */
        
        // Update the error statistics.
        
        HashMap<String,ErrorCount> moduleMap = intervalMap.get(timeStamp);
        if (moduleMap == null) {
            moduleMap = new  HashMap<String,ErrorCount>();
            trimHourMap(timeStamp); // removes stale entries            
            intervalMap.put(Long.valueOf(timeStamp), moduleMap);
        }
        
        ErrorCount errorCount = moduleMap.get(logModuleId);
        if (errorCount == null) {
            errorCount = new ErrorCount();
            moduleMap.put(logModuleId, errorCount);
        }

        if (logLevel.equals(Level.SEVERE)) {
            errorCount.setSevereCount(errorCount.getSevereCount()+1);
        } else { // Level == WARNING
            errorCount.setWarningCount(errorCount.getWarningCount()+1); 
        }
    }
    
    private void trimHourMap(long refTimeStamp) {
        Long[] timeStamps = intervalMap.keySet().toArray(new Long[0]);        
        for (int i = 0; i < timeStamps.length; i++) {
            long timeStamp = timeStamps[i];
            if ((refTimeStamp-timeStamp) >= interval*getNumOfIntervals()) {
                intervalMap.remove(timeStamp);
            }            
        }
    }
    
    // Methods for Log MBean.
    
    /**
     * @return a list of Map objects. Each map object contains
     * the tuple [TimeStamp, SevereCount, WarningCount].
     */
    public List<Map<String,Object>> getErrorInformation() {
        
        // Calculate the most recent timestamp bucket.
        
        long intervalsElapsed =
                (System.currentTimeMillis()-startTimeStamp)/interval;
        long recentTimeStamp = startTimeStamp + (intervalsElapsed*interval);
        
        // Gather the information for the past numOfIntervals.
        List<Map<String,Object>> results =
                new ArrayList<Map<String,Object>>();
        for (int i = 0; i < getNumOfIntervals(); i++) {
            long timeStamp = recentTimeStamp - interval*i;            
            HashMap<String,ErrorCount> moduleMap = intervalMap.get(timeStamp);
            int severeCount = 0, warningCount = 0;
            if (timeStamp<startTimeStamp) {
                severeCount  = -1;
                warningCount = -1;            
            }
            if (moduleMap != null) {
                for (ErrorCount error : moduleMap.values()) {
                    severeCount += error.getSevereCount();
                    warningCount += error.getWarningCount();
                }
            }
            HashMap<String,Object> entry = new HashMap<String,Object>();
            entry.put(LogAnalyzer.TIMESTAMP_KEY, timeStamp);
            entry.put(LogAnalyzer.SEVERE_COUNT_KEY, severeCount);
            entry.put(LogAnalyzer.WARNING_COUNT_KEY, warningCount);
            results.add(entry);
        }
        
        return results;
    }
       
    /**
     * @return a map. The key is module id (String) and the value is
     *         error count (Integer) for the specific module.
     */     
    public Map<String,Integer> getErrorDistribution(
            long timeStamp, Level level) {
                
        // Sanity check.

        if (!(level.equals(Level.SEVERE) || level.equals(Level.WARNING))) {
            throw new IllegalArgumentException("Log level: " + level);
        }
        
        // Gather the information.
        
        HashMap<String,ErrorCount> moduleMap = intervalMap.get(timeStamp);
        if (moduleMap == null) {
            return null;
        }
        
        Map<String,Integer> results = new HashMap<String,Integer>();
        for (String moduleId : moduleMap.keySet()) {
            ErrorCount errorCount = moduleMap.get(moduleId);
            if (level.equals(Level.SEVERE)) {
                results.put(moduleId, errorCount.getSevereCount());
            } else { // Level == WARNING
                results.put(moduleId, errorCount.getWarningCount());                
            }
        }
        
        return results;
    }
    
    // Unit test
    
    public static void main(String[] args) {

        // Create log records
        
        LogRecord srecord = new LogRecord(Level.SEVERE, "severe record");
        srecord.setMillis(System.currentTimeMillis());
        srecord.setLoggerName("com.wombat.smodule");
        
        LogRecord wrecord = new LogRecord(Level.WARNING, "warning record");
        wrecord.setMillis(System.currentTimeMillis());
        wrecord.setLoggerName("com.wombat.wmodule");
        
        // Update error statistics
        
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("HH:mm:ss");
        
        ErrorStatistics stats = new ErrorStatistics();
        long interval = 1000; // 1 second.
        stats.setIntervalDuration(interval);
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {}            
            for (int j = 0; j < 2; j++) {
                srecord.setMillis(System.currentTimeMillis());
                stats.updateStatistics(srecord);
                wrecord.setMillis(System.currentTimeMillis());
                stats.updateStatistics(wrecord);
            }
            System.out.printf("Interval(%1$s): %2$s\n", i,
                sdf.format(new java.util.Date(System.currentTimeMillis())));
        }
        
        // Query error statistics
        
        System.out.println("\nTimeStamp\tSevere\tWarning");
        System.out.println("--------------------------------");
        List<Map<String,Object>> list = stats.getErrorInformation();
        for (Map<String,Object> item : list) {
            long timeStamp = (Long) item.get(LogAnalyzer.TIMESTAMP_KEY);
            int severeCount = (Integer) item.get(LogAnalyzer.SEVERE_COUNT_KEY);
            int warningCount = 
                    (Integer) item.get(LogAnalyzer.WARNING_COUNT_KEY);
            System.out.printf("%1$s\t%2$s\t%3$s\n",
                              sdf.format(new java.util.Date(timeStamp)),
                              severeCount, warningCount);
        }
        
        for (Map<String,Object> item : list) {
            long timeStamp = (Long) item.get(LogAnalyzer.TIMESTAMP_KEY);
            Map<String,Integer> map =
                    stats.getErrorDistribution(timeStamp, Level.SEVERE);
            System.out.printf("\nModuleId\tLevel.SEVERE\t(%1$s)\n",
                              sdf.format(new java.util.Date(timeStamp)));
            System.out.println("------------------------------------------");
            for (String moduleId : map.keySet()) {
                System.out.printf("%1$s\t%2$s\n", moduleId, map.get(moduleId));
            }
            map = stats.getErrorDistribution(timeStamp, Level.WARNING);
            System.out.printf("\nModuleId\tLevel.WARNING\t(%1$s)\n",
                              sdf.format(new java.util.Date(timeStamp)));
            System.out.println("------------------------------------------");
            for (String moduleId : map.keySet()) {
                System.out.printf("%1$s\t%2$s\n", moduleId, map.get(moduleId));
            }
        }        
    }
}
