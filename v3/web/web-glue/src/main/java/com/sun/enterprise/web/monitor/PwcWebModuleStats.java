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
 * $Id: PwcWebModuleStats.java,v 1.3 2005/12/25 04:27:30 tcfujii Exp $
 * $Date: 2005/12/25 04:27:30 $
 * $Revision: 1.3 $
 *
 */

package com.sun.enterprise.web.monitor;

import java.io.Serializable;
import java.util.HashMap;

/** 
 * Monitoring interface for web modules.
 */
public interface PwcWebModuleStats extends Serializable {


    /**
     * Gets the number of JSPs that have been loaded in the web module.
     *.
     * @return Number of JSPs that have been loaded
     */
    public int getJspCount();


    /**
     * Gets the number of JSPs that have been reloaded in the web module.
     *.
     * @return Number of JSPs that have been reloaded
     */
    public int getJspReloadCount();


    /**
     * Gets the number of errors that were triggered by JSP invocations.
     *.
     * @return Number of errors triggered by JSP invocations
     */
    public int getJspErrorCount();


    /**
     * Gets the total number of sessions that have been created for the web
     * module.
     *.
     * @return Total number of sessions created
     */
    public int getSessionsTotal();


    /**
     * Gets the number of currently active sessions for the web
     * module.
     *.
     * @return Number of currently active sessions
     */
    public int getActiveSessionsCurrent();


    /**
     * Gets the maximum number of concurrently active sessions for the web
     * module.
     *
     * @return Maximum number of concurrently active sessions
     */
    public int getActiveSessionsHigh();


    /**
     * Gets the total number of rejected sessions for the web module.
     *
     * <p>This is the number of sessions that were not created because the
     * maximum allowed number of sessions were active.
     *.
     * @return Total number of rejected sessions
     */
    public int getRejectedSessionsTotal();


    /**
     * Gets the total number of expired sessions for the web module.
     *.
     * @return Total number of expired sessions
     */
    public int getExpiredSessionsTotal();


    /**
     * Gets the longest time (in seconds) that an expired session had been
     * alive.
     *
     * @return Longest time (in seconds) that an expired session had been
     * alive.
     */
    public int getSessionMaxAliveTimeSeconds();


    /**
     * Gets the average time (in seconds) that expired sessions had been
     * alive.
     *
     * @return Average time (in seconds) that expired sessions had been
     * alive.
     */
    public int getSessionAverageAliveTimeSeconds();


    /**
     * Gets the time when the web module was started.
     *
     * @return Time (in milliseconds since January 1, 1970, 00:00:00) when the
     * web module was started 
     */
    public long getStartTimeMillis();


    /**
     * Gets the cumulative processing times of all servlets in the web module
     * associated with this PwcWebModuleStats.
     *
     * @return Cumulative processing times of all servlets in the web module
     * associated with this PwcWebModuleStats
     */
    public long getServletProcessingTimesMillis();


    /**
     * Returns the session ids of all sessions currently active in the web
     * module associated with this PwcWebModuleStats.
     *
     * @return Session ids of all sessions currently active in the web module
     * associated with this PwcWebModuleStats
     */
    public String getSessionIds();


    /**
     * Returns information about the session with the given id.
     *
     * <p>The session information is organized as a HashMap, mapping 
     * session attribute names to the String representation of their values.
     *
     * @param id Session id
     *
     * @return HashMap mapping session attribute names to the String
     * representation of their values, or null if no session with the
     * specified id exists, or if the session does not have any attributes
     */
    public HashMap getSession(String id);


    /**
     * Resets this WebModuleStats.
     */
    public void reset();
        
}
