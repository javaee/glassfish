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

package com.sun.enterprise.ee.web.stats;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.web.stats.WebModuleStatsImpl;
import com.sun.enterprise.ee.admin.monitor.stats.EEWebModuleStats;
import org.apache.catalina.Manager;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationManagerBase;
import com.sun.enterprise.ee.web.sessmgmt.HAStoreBase;
import com.sun.enterprise.ee.web.sessmgmt.WebModuleStatistics;
import com.sun.enterprise.admin.monitor.stats.AverageRangeStatistic;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableAverageRangeStatisticImpl;

/**
 * Class gathering web module statistics.
 */
public class EEWebModuleStatsImpl extends WebModuleStatsImpl
        implements EEWebModuleStats {

    private MutableCountStatistic sessionSizeLow;
    private MutableCountStatistic sessionSizeHigh;
    private MutableCountStatistic sessionSizeAvg;
    private MutableCountStatistic containerLatencyLow;
    private MutableCountStatistic containerLatencyHigh;
    private MutableCountStatistic containerLatencyAvg;
    private MutableCountStatistic sessionPersistTimeLow;
    private MutableCountStatistic sessionPersistTimeHigh;
    private MutableCountStatistic sessionPersistTimeAvg;
    private MutableCountStatistic cachedSessionsCurrent;
    private MutableCountStatistic passivatedSessionsCurrent;
    
    private WebModuleStatistics webModuleStatistics;


    /** 
     * Constructor.
     */
    public EEWebModuleStatsImpl() {
        super(com.sun.enterprise.ee.admin.monitor.stats.EEWebModuleStats.class);
        initializeEEStatistics();
    }    
    
    /**
     * Gets the webModuleStatistics from the sessionManager
     * For now, only ReplicationManagerBase derived managers have
     * WebModuleStatistics so persistence-type "memory"
     * and "file" will only report PE type stats
     *
     * @return webModuleStatistics
     */    
    private WebModuleStatistics getWebModuleStatistics() {
        if(webModuleStatistics == null) {
            if (sessionManager == null || !(sessionManager instanceof ReplicationManagerBase) ) {
                return null;
            }            
            webModuleStatistics = 
                ((ReplicationManagerBase) sessionManager).getWebModuleStatistics();
        }
        return webModuleStatistics;
    }

    /**
     * Gets the sessionManager; returns null if it is null
     * or if it is not derived from ReplicationManagerBase
     *
     * @return sessionManager
     */     
    private ReplicationManagerBase getSessionManager() {
        if (sessionManager == null || !(sessionManager instanceof ReplicationManagerBase) ) {
            return null;
        } else {
            return ((ReplicationManagerBase)sessionManager);
        }
    }

    /**
     * Gets the low/high/avg session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStatsImpl.
     *
     * @return session size stat
     */    
    public AverageRangeStatistic getSessionSize() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            return stats.getSessionSizeStat();
        } else {
            return WebModuleStatistics.createDefaultStat(WebModuleStatistics.SESSION_SIZE, "byte", "Low/High/Average Session");
            //return new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl("session_size", "byte", 0L, 0L, 0L));
        }        
    }    

    /**
     * Gets the lowest session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStatsImpl.
     *
     * @return Lowest session size
     */
    public CountStatistic getSessionSizeLow() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            sessionSizeLow.setCount(stats.getSessionSizeLow());
        } else {
            sessionSizeLow.setCount(-1);
        }
        return (CountStatistic) sessionSizeLow.unmodifiableView();
    }

    /**
     * Gets the highest session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStatsImpl.
     *
     * @return Highest session size
     */
    public CountStatistic getSessionSizeHigh() { 
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            sessionSizeHigh.setCount(stats.getSessionSizeHigh());
        } else {
            sessionSizeHigh.setCount(0);
        }
        return (CountStatistic) sessionSizeHigh.unmodifiableView();        
    }

    /**
     * Gets the average session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStatsImpl.
     *
     * @return Average session size
     */
    public CountStatistic getSessionSizeAvg() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            sessionSizeAvg.setCount(stats.getSessionSizeAverage());
        } else {
            sessionSizeAvg.setCount(-1);
        }
        return (CountStatistic) sessionSizeAvg.unmodifiableView();         
    }

    /**
     * Gets the low/high/avg latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return latency stat for the web container's part of the overall
     * request latency
     */    
    public AverageRangeStatistic getContainerLatency() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            return stats.getPipelineStat();
        } else {
            return WebModuleStatistics.createDefaultStat(WebModuleStatistics.CONTAINER_LATENCY, "millisecond", "Low/High/Average Container Latency");
            //return new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl("container_latency", "millisecond", 0L, 0L, 0L));
        }        
    }    

    /**
     * Gets the lowest latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return Lowest latency for the web container's part of the overall
     * request latency
     */
    public CountStatistic getContainerLatencyLow() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            containerLatencyLow.setCount(stats.getPipelineLow());
        } else {
            containerLatencyLow.setCount(-1);
        }
        return (CountStatistic) containerLatencyLow.unmodifiableView();        
    }

    /**
     * Gets the highest latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return Highest latency for the web container's part of the overall
     * request latency
     */
    public CountStatistic getContainerLatencyHigh() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            containerLatencyHigh.setCount(stats.getPipelineHigh());
        } else {
            containerLatencyHigh.setCount(-1);
        }
        return (CountStatistic) containerLatencyHigh.unmodifiableView();         
    }

    /**
     * Gets the average latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return Average latency for the web container's part of the overall
     * request latency
     */
    public CountStatistic getContainerLatencyAvg() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            containerLatencyAvg.setCount(stats.getPipelineAverage());
        } else {
            containerLatencyAvg.setCount(-1);
        }
        return (CountStatistic) containerLatencyAvg.unmodifiableView();         
    }

    /**
     * Gets the low/high/avg time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return stat for time taken to persist HTTP Session State to back-end
     * store
     */    
    public AverageRangeStatistic getSessionPersistTime() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            return stats.getValveSaveStat();
        } else {
            return WebModuleStatistics.createDefaultStat(WebModuleStatistics.SESSION_PERSIST_TIME, "millisecond", "Low/High/Average Session Persist Time");
            //return new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl("session_persist_time", "millisecond", 0L, 0L, 0L));
        }        
    }    

    /**
     * Gets the lowest time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return Lowest time taken to persist HTTP Session State to back-end
     * store
     */
    public CountStatistic getSessionPersistTimeLow() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            sessionPersistTimeLow.setCount(stats.getValveSaveLow());
        } else {
            sessionPersistTimeLow.setCount(-1);
        }
        return (CountStatistic) sessionPersistTimeLow.unmodifiableView();        
    }

    /**
     * Gets the highest time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return Highest time taken to persist HTTP Session State to back-end
     * store
     */
    public CountStatistic getSessionPersistTimeHigh() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            sessionPersistTimeHigh.setCount(stats.getValveSaveHigh());
        } else {
            sessionPersistTimeHigh.setCount(-1);
        }
        return (CountStatistic) sessionPersistTimeHigh.unmodifiableView();         
    }

    /**
     * Gets the average time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStatsImpl.
     *
     * @return Average time taken to persist HTTP Session State to back-end
     * store
     */
    public CountStatistic getSessionPersistTimeAvg() {
        WebModuleStatistics stats = null;
        stats = getWebModuleStatistics();
        if(stats != null) {
            sessionPersistTimeAvg.setCount(stats.getValveSaveAverage());
        } else {
            sessionPersistTimeAvg.setCount(-1);
        }
        return (CountStatistic) sessionPersistTimeAvg.unmodifiableView();         
    }
    
    /**
     * Gets the number of currently active sessions for the web
     * module associated with this EEWebModuleStatsImpl.
     *.
     * @return Number of currently active (or potentially active) sessions
     */
    public CountStatistic getActiveSessionsCurrent() {
        ReplicationManagerBase sessMgr = this.getSessionManager();
        if (sessMgr == null) {
            //activeSessionsCurrent.setCount(0);
            return super.getActiveSessionsCurrent();
        } else {
            activeSessionsCurrent.setCount(this.getNumberActiveSessions());
        }
        return (CountStatistic) activeSessionsCurrent.unmodifiableView();        
    }     

    /**
     * Gets the current number of sessions cached in memory for the web module
     * associated with this EEWebModuleStatsImpl.
     *
     * @return Current number of sessions cached in memory
     */
    public CountStatistic getCachedSessionsCurrent() {
        ReplicationManagerBase sessMgr = this.getSessionManager();
        if (sessMgr == null) {
            cachedSessionsCurrent.setCount(-1);
        } else {
            long cachedSessionsSize = sessMgr.getSessionsCacheSize();
            cachedSessionsCurrent.setCount(cachedSessionsSize);
        }
        return (CountStatistic) cachedSessionsCurrent.unmodifiableView();
    }

    /**
     * Gets the current number of sessions passivated for the web module
     * associated with this EEWebModuleStatsImpl.
     *
     * @return Current number of passivated sessions
     */
    public CountStatistic getPassivatedSessionsCurrent() {
        passivatedSessionsCurrent.setCount(this.getNumberPassivatedSessions());
        return (CountStatistic) passivatedSessionsCurrent.unmodifiableView();        
    }
    
    private long getNumberCachedSessions() {
        ReplicationManagerBase sessMgr = this.getSessionManager();
        if (sessMgr == null) {
            return -1;
        } else {
            return sessMgr.getSessionsCacheSize();
        }        
    }    
    
    private long getNumberStoredSessions() {
        ReplicationManagerBase sessMgr = this.getSessionManager();
        if (sessMgr == null) {
            return -1;
        } else {
            HAStoreBase store = (HAStoreBase) sessMgr.getStore();
            int numStoredSessions = -1;
            try {
                numStoredSessions = store.getSize(); 
            } catch (Exception ex) {
                //deliberate no-op
                numStoredSessions = -1;
            };            
            return numStoredSessions;
        }        
    }
    
    private long getNumberActiveSessions() {
        long activeSessions = 0;
        //long passivatedSessions = 0;
        long numStoredSessions = this.getNumberStoredSessions();
        long numCachedSessions = this.getNumberCachedSessions();
        if(numStoredSessions >= numCachedSessions) {
            activeSessions = numStoredSessions;
            //passivatedSessions = numStoredSessions - numCachedSessions;
        } else {
            activeSessions = numCachedSessions;
        }
        return activeSessions;
    }
    
    private long getNumberPassivatedSessions() {
        //long activeSessions = 0;
        long passivatedSessions = 0;
        long numStoredSessions = this.getNumberStoredSessions();
        long numCachedSessions = this.getNumberCachedSessions();
        if(numStoredSessions >= numCachedSessions) {
            //activeSessions = numStoredSessions;
            passivatedSessions = numStoredSessions - numCachedSessions;
        } else {
            //activeSessions = numCachedSessions;
        }
        return passivatedSessions;
    }    


    private void initializeEEStatistics() {
        
        sessionSizeLow = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionSizeLow"));
        sessionSizeHigh = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionSizeHigh"));
        sessionSizeAvg = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionSizeAvg"));
        containerLatencyLow = new MutableCountStatisticImpl(
                        new CountStatisticImpl("ContainerLatencyLow"));
        containerLatencyHigh = new MutableCountStatisticImpl(
                        new CountStatisticImpl("ContainerLatencyHigh"));
        containerLatencyAvg = new MutableCountStatisticImpl(
                        new CountStatisticImpl("ContainerLatencyAvg"));
        sessionPersistTimeLow = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionPersistTimeLow"));
        sessionPersistTimeHigh = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionPersistTimeHigh"));
        sessionPersistTimeAvg = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionPersistTimeAvg"));
        cachedSessionsCurrent = new MutableCountStatisticImpl(
                        new CountStatisticImpl("CachedSessionsCurrent"));
        passivatedSessionsCurrent = new MutableCountStatisticImpl(
                        new CountStatisticImpl("PassivatedSessionsCurrent"));

    }

}
