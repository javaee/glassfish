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
 * $Id: EEWebModuleStats.java,v 1.1.1.1 2006/08/08 19:48:30 dpatil Exp $
 * $Date: 2006/08/08 19:48:30 $
 * $Revision: 1.1.1.1 $
 *
 */

package com.sun.enterprise.ee.admin.monitor.stats;

import javax.management.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.AverageRangeStatistic;
import com.sun.enterprise.admin.monitor.stats.WebModuleStats;

/** 
 * Interface for querying web module statistics.
 */
public interface EEWebModuleStats extends WebModuleStats {

    /**
     * Gets the lowest session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStats.
     *
     * @return Lowest session size
     */
    //public CountStatistic getSessionSizeLow();

    /**
     * Gets the highest session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStats.
     *
     * @return Highest session size
     */
    //public CountStatistic getSessionSizeHigh();

    /**
     * Gets the average session size (in bytes for serialized session) for the
     * web module associated with this EEWebModuleStats.
     *
     * @return Average session size
     */
    //public CountStatistic getSessionSizeAvg();

    /**
     * Gets the session size (high/low/average)(in bytes for serialized session) 
     * for the web module associated with this EEWebModuleStats.
     *
     * @return session size
     */    
    public AverageRangeStatistic getSessionSize();

    /**
     * Gets the lowest latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStats.
     *
     * @return Lowest latency for the web container's part of the overall
     * request latency
     */
    //public CountStatistic getContainerLatencyLow();

    /**
     * Gets the highest latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStats.
     *
     * @return Highest latency for the web container's part of the overall
     * request latency
     */
    //public CountStatistic getContainerLatencyHigh();

    /**
     * Gets the average latency (in milliseconds) for the web container's part
     * of the overall request latency for the web module associated with this
     * EEWebModuleStats.
     *
     * @return Average latency for the web container's part of the overall
     * request latency
     */
    //public CountStatistic getContainerLatencyAvg();
    
    /**
     * Gets the latency (high/low/average) for the web container's part 
     * of the overall request latency
     *
     * @return latency for the web container's part of the overall
     * request latency
     */    
    public AverageRangeStatistic getContainerLatency();    

    /**
     * Gets the lowest time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStats.
     *
     * @return Lowest time taken to persist HTTP Session State to back-end
     * store
     */
    //public CountStatistic getSessionPersistTimeLow();

    /**
     * Gets the highest time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStats.
     *
     * @return Highest time taken to persist HTTP Session State to back-end
     * store
     */
    //public CountStatistic getSessionPersistTimeHigh();

    /**
     * Gets the average time (in milliseconds) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStats.
     *
     * @return Average time taken to persist HTTP Session State to back-end
     * store
     */
    //public CountStatistic getSessionPersistTimeAvg();
    
    /**
     * Gets the time (in milliseconds) (low/high/average) taken to persist HTTP Session
     * State to back-end store for the web module associated with this
     * EEWebModuleStats.
     *
     * @return time taken to persist HTTP Session State to back-end
     * store
     */    
    public AverageRangeStatistic getSessionPersistTime();     

    /**
     * Gets the current number of sessions cached in memory for the web module
     * associated with this EEWebModuleStats.
     *
     * @return Current number of sessions cached in memory
     */
    public CountStatistic getCachedSessionsCurrent();

    /**
     * Gets the current number of sessions passivated for the web module
     * associated with this EEWebModuleStats.
     *
     * @return Current number of passivated sessions
     */
    public CountStatistic getPassivatedSessionsCurrent();

}
