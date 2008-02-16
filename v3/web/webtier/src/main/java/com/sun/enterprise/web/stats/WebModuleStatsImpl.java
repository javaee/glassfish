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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.StringTokenizer;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.web.MonitorStatsCapable;
import com.sun.enterprise.admin.monitor.stats.WebModuleStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.web.monitor.PwcWebModuleStats;
import org.apache.catalina.Manager;


/**
 * Class gathering web module statistics.
 */
public class WebModuleStatsImpl implements WebModuleStats {

    private static final Logger _logger
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    protected Manager sessionManager;
    protected GenericStatsImpl baseStatsImpl;

    private PwcWebModuleStats pwcWebStats;
    private long initTime;

    /*
     * JSP related stats
     */
    protected MutableCountStatistic jspCount;
    protected MutableCountStatistic jspReloadCount;
    protected MutableCountStatistic jspErrorCount;

    /*
     * Session management related stats
     */
    protected MutableCountStatistic sessionsTotal;
    protected MutableCountStatistic activeSessionsCurrent;
    protected MutableCountStatistic activeSessionsHigh;
    protected MutableCountStatistic rejectedSessionsTotal;
    protected MutableCountStatistic expiredSessionsTotal;
    protected MutableCountStatistic processingTimeMillis;
    

    /** 
     * Constructor.
     */
    public WebModuleStatsImpl() {
        this(com.sun.enterprise.admin.monitor.stats.WebModuleStats.class);
    }

    /**
     * Constructor.
     *
     * @param inter Interface exposed by this instance to the monitoring
     * framework
     */
    protected WebModuleStatsImpl(Class inter) {
        baseStatsImpl = new GenericStatsImpl(inter, this);

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }

    
    /**
     * Sets the session manager that this WebModuleStatsImpl is going to query
     * for session-related stats.
     *
     * @param manager Session manager
     */
    public void setSessionManager(Manager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Null session manager");
        }
        this.sessionManager = manager;
    }


    /**
     * Sets the PwcWebModuleStats instance to which this WebModuleStatsImpl is
     * going to delegate.
     *
     * @param pwcWebStats PwcWebModuleStats instance to which to delegate
     */
    public void setPwcWebModuleStats(PwcWebModuleStats pwcWebStats) {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        this.pwcWebStats = pwcWebStats;
    }


    /**
     * Gets the number of JSPs that have been loaded in the web module
     * associated with this WebModuleStatsImpl.
     *.
     * @return Number of JSPs that have been loaded
     */
    public CountStatistic getJspCount() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        jspCount.setCount(pwcWebStats.getJspCount());
        return (CountStatistic) jspCount.unmodifiableView();
    }
    
    
    /**
     * Gets the number of JSPs that have been reloaded in the web module
     * associated with this WebModuleStatsImpl.
     *.
     * @return Number of JSPs that have been reloaded
     */
    public CountStatistic getJspReloadCount() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        jspReloadCount.setCount(pwcWebStats.getJspReloadCount());
        return (CountStatistic) jspReloadCount.unmodifiableView();
    }


    /**
     * Gets the number of errors that were triggered by JSP invocations.
     *.
     * @return Number of errors triggered by JSP invocations
     */
    public CountStatistic getJspErrorCount() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        jspErrorCount.setCount(pwcWebStats.getJspErrorCount());
        return (CountStatistic) jspErrorCount.unmodifiableView();
    }


    /**
     * Gets the total number of sessions that have been created for the web
     * module associated with this WebModuleStatsImpl.
     *.
     * @return Total number of sessions created
     */
    public CountStatistic getSessionsTotal() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        sessionsTotal.setCount(pwcWebStats.getSessionsTotal());
        return (CountStatistic) sessionsTotal.unmodifiableView();
    }


    /**
     * Gets the number of currently active sessions for the web
     * module associated with this WebModuleStatsImpl.
     *.
     * @return Number of currently active sessions
     */
    public CountStatistic getActiveSessionsCurrent() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        activeSessionsCurrent.setCount(pwcWebStats.getActiveSessionsCurrent());
        return (CountStatistic) activeSessionsCurrent.unmodifiableView();
    }


    /**
     * Gets the maximum number of concurrently active sessions for the web
     * module associated with this WebModuleStatsImpl.
     *
     * @return Maximum number of concurrently active sessions
     */
    public CountStatistic getActiveSessionsHigh() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        activeSessionsHigh.setCount(pwcWebStats.getActiveSessionsHigh());
        return (CountStatistic) activeSessionsHigh.unmodifiableView();
    }


    /**
     * Gets the total number of rejected sessions for the web
     * module associated with this WebModuleStatsImpl.
     *
     * <p>This is the number of sessions that were not created because the
     * maximum allowed number of sessions were active.
     *
     * @return Total number of rejected sessions
     */
    public CountStatistic getRejectedSessionsTotal() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        rejectedSessionsTotal.setCount(pwcWebStats.getRejectedSessionsTotal());
        return (CountStatistic) rejectedSessionsTotal.unmodifiableView();
    }


    /**
     * Gets the total number of expired sessions for the web
     * module associated with this WebModuleStatsImpl.
     *.
     * @return Total number of expired sessions
     */
    public CountStatistic getExpiredSessionsTotal() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        expiredSessionsTotal.setCount(pwcWebStats.getExpiredSessionsTotal());
        return (CountStatistic) expiredSessionsTotal.unmodifiableView();
    }


    /**
     * Gets the cumulative processing times of all servlets in the web module
     * associated with this WebModuleStatsImpl.
     *
     * @return Cumulative processing times of all servlets in the web module
     * associated with this WebModuleStatsImpl
     */
    public CountStatistic getServletProcessingTimes() {
        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }
        processingTimeMillis.setCount(
            pwcWebStats.getServletProcessingTimesMillis());
        return (CountStatistic) processingTimeMillis.unmodifiableView();
    }


    /**
     * Returns comma-separated list of all sessions currently active in the web
     * module associated with this WebModuleStatsImpl.
     *
     * @return Comma-separated list of all sessions currently active in the
     * web module associated with this WebModuleStatsImpl
     */
    public StringStatistic getSessions() {
    
        StringBuffer sb = null;

        if (pwcWebStats == null) {
            throw new IllegalArgumentException("Null PwcWebModuleStats");
        }

        String sessionIds = pwcWebStats.getSessionIds();
        if (sessionIds != null) {
            sb = new StringBuffer();
            StringTokenizer tokenizer = new StringTokenizer(sessionIds, " ");
            boolean first = true;
            while (tokenizer.hasMoreTokens()) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                String sessionId = tokenizer.nextToken();
                sb.append(sessionId);
                HashMap map = pwcWebStats.getSession(sessionId);
                if (map != null) { 
                    sb.append(":");
                    sb.append(map);
                }
            }
        }

        return new StringStatisticImpl(
                    sb != null ? sb.toString(): null,
                    "Sessions",
                    "String",
                    "List of currently active sessions",
                    initTime,
                    System.currentTimeMillis()); 
    }


    /**
     * Resets this WebModuleStats.
     */
    public void reset() {

        // Reset session stats
        if (sessionManager instanceof MonitorStatsCapable) {
            ((MonitorStatsCapable)sessionManager).resetMonitorStats();
        }

        if (pwcWebStats != null) {
            pwcWebStats.reset();
        }
    }


    /**
     * Gets all statistics exposed by this WebModuleStatsImpl instance.
     *
     * @return Array of all statistics exposed by this WebModuleStatsImpl
     * instance
     */
    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }

    
    /**
     * Queries for a statistic by name.
     *
     * @param name The name of the statistic to query for
     * 
     * @return The statistic corresponding to the given name
     */ 
    public Statistic getStatistic(String name) {
        return baseStatsImpl.getStatistic(name);
    }

    
    /**
     * Returns the names of all statistics that may be retrieved from this
     * WebModuleStatsImpl instance.
     *
     * @return Array of names of all statistics exposed by this
     * WebModuleStatsImpl instance
     */ 
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

    
    private void initializeStatistics() {
        
        jspCount = new MutableCountStatisticImpl(
                        new CountStatisticImpl("JspCount"));
        jspReloadCount = new MutableCountStatisticImpl(
                        new CountStatisticImpl("JspReloadCount"));
        jspErrorCount = new MutableCountStatisticImpl(
                        new CountStatisticImpl("JspErrorCount"));
        sessionsTotal = new MutableCountStatisticImpl(
                        new CountStatisticImpl("SessionsTotal"));
        activeSessionsCurrent = new MutableCountStatisticImpl(
                        new CountStatisticImpl("ActiveSessionsCurrent"));
        activeSessionsHigh = new MutableCountStatisticImpl(
                        new CountStatisticImpl("ActiveSessionsHigh"));
        rejectedSessionsTotal = new MutableCountStatisticImpl(
                        new CountStatisticImpl("RejectedSessionsTotal"));
        expiredSessionsTotal = new MutableCountStatisticImpl(
                        new CountStatisticImpl("ExpiredSessionsTotal"));
        processingTimeMillis = new MutableCountStatisticImpl(
                    new CountStatisticImpl("ServletProcessingTimes"));
        initTime = System.currentTimeMillis ();
    }

}
