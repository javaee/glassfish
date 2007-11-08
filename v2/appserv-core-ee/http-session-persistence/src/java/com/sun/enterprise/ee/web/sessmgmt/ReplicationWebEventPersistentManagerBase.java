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
 * ReplicationWebEventPersistentManagerBase.java
 *
 * Created on October 31, 2006, 12:38 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;

/**
 *
 * @author Larry White
 */
public abstract class ReplicationWebEventPersistentManagerBase extends ReplicationManagerBase implements WebEventPersistentManager {
    
    /** Creates a new instance of ReplicationWebEventPersistentManagerBase */
    public ReplicationWebEventPersistentManagerBase() {
    }
    
    /** create and return a new session; delegates to session factory */
    protected Session createNewSession() {

        //_logger.finest("IN ReplicationWebEventPersistentManagerBase>>createNewSession");       
        Session sess = super.createNewSession();
        //_logger.finest("GOT SESSION VIA FACTORY: Class = " + sess.getClass().getName());
        
        return sess;
    }

    // ------------------------------------------------------------- Properties
    
    /** 
     * return the string of monitoring information
     * used by monitoring framework
     */
    public String getMonitorAttributeValues() {
        StringBuffer sb = new StringBuffer(500);
        WebModuleStatistics stats = this.getWebModuleStatistics();       
        sb.append("\nVALVE_SAVE_LOW=" + stats.getValveSaveLow());
        sb.append("\nVALVE_SAVE_HIGH=" + stats.getValveSaveHigh());
        sb.append("\nVALVE_SAVE_AVG=" + stats.getValveSaveAverage());
        sb.append("\nGET_CONN_LOW=" + stats.getGetConnectionLow());
        sb.append("\nGET_CONN_HIGH=" + stats.getGetConnectionHigh());
        sb.append("\nGET_CONN_AVG=" + stats.getGetConnectionAverage());
        sb.append("\nPUT_CONN_LOW=" + stats.getPutConnectionLow());
        sb.append("\nPUT_CONN_HIGH=" + stats.getPutConnectionHigh());
        sb.append("\nPUT_CONN_AVG=" + stats.getPutConnectionAverage());        
        sb.append("\nSTMT_PREP_LOW=" + stats.getStatementPrepLow());
        sb.append("\nSTMT_PREP_HIGH=" + stats.getStatementPrepHigh());
        sb.append("\nSTMT_PREP_AVG=" + stats.getStatementPrepAverage());         
        sb.append("\nEXECUTE_STMT_LOW=" + stats.getExecuteStatementLow());
        sb.append("\nEXECUTE_STMT_HIGH=" + stats.getExecuteStatementHigh());
        sb.append("\nEXECUTE_STMT_AVG=" + stats.getExecuteStatementAverage()); 
        sb.append("\nCOMMIT_LOW=" + stats.getCommitLow());
        sb.append("\nCOMMIT_HIGH=" + stats.getCommitHigh());
        sb.append("\nCOMMIT_AVG=" + stats.getCommitAverage());        
        sb.append("\nTOTAL_PIPELINE_LOW=" + stats.getPipelineLow());
        sb.append("\nTOTAL_PIPELINE_HIGH=" + stats.getPipelineHigh());
        sb.append("\nTOTAL_PIPELINE_AVG=" + stats.getPipelineAverage());
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
        HAStoreBase store = (HAStoreBase) this.getStore();
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
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }
    
    /**
     * Back up idle sessions.
     * Hercules: modified method we do not want
     * background saves when we are using web-event persistence-frequency
     */
    protected void processMaxIdleBackups() {       
        //this is a deliberate no-op for this manager
        return;
    }
    
    /**
     * Swap idle sessions out to Store if too many are active
     * Hercules: modified method
     */
    protected void processMaxActiveSwaps() {    
        //this is a deliberate no-op for this manager
        return;
    }
    
    /**
     * Swap idle sessions out to Store if they are idle too long.
     */
    protected void processMaxIdleSwaps() {    
        //this is a deliberate no-op for this manager
        return;
    }
    
    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     * We use this here to insure that pool entries are cleaned up
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException { 
        super.stop();
        this.setStorePool(null);
    }
    
}
