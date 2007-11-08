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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * HABackgroundManager.java
 *
 * Created on October 3, 2002, 2:02 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import org.apache.catalina.Session;

/**
 *
 * @author  lwhite
 */
public class HABackgroundManager extends HAManagerBase {    
    
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "HABackgroundManager/1.0";


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    private static final String name = "HABackgroundManager";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }

    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }
    
    /** Creates a new instance of HABackgroundManager */
    public HABackgroundManager() {
        super();
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }        
    }     

    /** 
     * create and return a new session; delegates to session factory 
     */     
    protected Session createNewSession() {

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN HABackgroundManager>>createNewSession"); 
        }
        Session sess = super.createNewSession();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("GOT SESSION VIA FACTORY: Class = " + sess.getClass().getName());
        }

        return sess;
    } 

    /** 
     * return the string of monitoring information
     * used by monitoring framework
     */    
    public String getMonitorAttributeValues() {
        StringBuffer sb = new StringBuffer(500);
        WebModuleStatistics stats = this.getWebModuleStatistics();
        sb.append("\nBACKGROUND_SAVE_LOW=" + stats.getBackgroundSaveLow());
        sb.append("\nBACKGROUND_SAVE_HIGH=" + stats.getBackgroundSaveHigh());
        sb.append("\nBACKGROUND_SAVE_AVG=" + stats.getBackgroundSaveAverage());
        sb.append("\nSESSION_SIZE_LOW=" + stats.getSessionSizeLow());
        sb.append("\nSESSION_SIZE_HIGH=" + stats.getSessionSizeHigh());
        sb.append("\nSESSION_SIZE_AVG=" + stats.getSessionSizeAverage());
        long cacheHits = stats.getCacheHits();
        long cacheMisses = stats.getCacheMisses();
        long cacheTotal = cacheHits + cacheMisses;
        long ratio = -1;
        if(cacheTotal != 0)
            ratio = cacheHits / cacheTotal;
        sb.append("\nCACHE_HIT_RATIO=" + ratio);
        int numCachedSessions = sessions.size();
        sb.append("\nNUMBER_CACHED_SESSIONS=" + numCachedSessions);
        HAStore store = (HAStore) this.getStore();
        int numStoredSessions = -1;
        try {
            numStoredSessions = store.getSize();
        } catch (Exception ex) {
            //deliberate no-op
            assert true;
        };
        int activeSessions = 0;
        int passivatedSessions = 0;
        if(numStoredSessions >= numCachedSessions) {
            activeSessions = numStoredSessions;
            passivatedSessions = numStoredSessions - numCachedSessions;
        } else {
            activeSessions = numCachedSessions;
        }        
        sb.append("\nNUMBER_ACTIVE_SESSIONS=" + activeSessions);
        sb.append("\nNUMBER_PASSIVATED_SESSIONS=" + passivatedSessions);    
        
        stats.resetStats();
        return sb.toString();
    }        
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;      
        
}
