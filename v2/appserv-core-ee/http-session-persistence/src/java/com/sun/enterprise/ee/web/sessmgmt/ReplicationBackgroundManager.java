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
 * ReplicationBackgroundManager.java
 *
 * Created on November 9, 2006, 3:31 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import org.apache.catalina.Session;

/**
 *
 * @author lwhite
 */
public class ReplicationBackgroundManager extends ReplicationManagerBase 
        implements ReplicationManager {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);     
    
    /** Creates a new instance of ReplicationBackgroundManager */
    public ReplicationBackgroundManager() {
    }
    
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "ReplicationBackgroundManager/1.0";


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    private static final String name = "ReplicationBackgroundManager";


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

    /** 
     * create and return a new session; delegates to session factory 
     */     
    protected Session createNewSession() {

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN ReplicationBackgroundManager>>createNewSession"); 
        }
        Session sess = super.createNewSession();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("GOT SESSION VIA FACTORY: Class = " + sess.getClass().getName());
        }

        return sess;
    }
    
    /**
     * Look for a session in the Store and, if found, restore
     * it in the Manager's list of active sessions if appropriate.
     * The session will be removed from the Store after swapping
     * in, but will not be added to the active session list if it
     * is invalid or past its expiration.
     */
    protected Session swapIn(String id) throws IOException {
        Session sess = super.swapIn(id);
        //must insure that newly loaded session is replicated now
        if(sess != null) {
            this.doValveSave(sess);
        }
        return sess;
    }    
    
    /** 
     * we don't support session versioning for time-based
     * because the client-side version will usually be ahead
     * of a fail-over version (by design) 
     */
    public boolean isSessionVersioningSupported() {
        return false;
    } 
    
     public ReplicationState processBroadcastfindsession(ReplicationState queryState) {
         //complete query and send back response
         if(_logger.isLoggable(Level.FINE)) {
             _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:instance: " + getInstanceName());
             _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:id=" + queryState.getId());                        
         }        
         //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:instance: " + getInstanceName());
         //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:id=" + queryState.getId() + "dumping:");
         //Thread.dumpStack();
         ReplicationState replicaState 
             = findReplicatedState(queryState);
         ReplicationState returnState = null;
         if(replicaState != null) {
             if(_logger.isLoggable(Level.FINE)) {
                 _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
                 _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
                 _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());                     
             }            
             //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
             //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
             //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());
             returnState = ReplicationState.createQueryResponseFrom(replicaState);
             if(_logger.isLoggable(Level.FINE)) {
                 _logger.fine("processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());                  
             }            
             //System.out.println("processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());
             //System.out.println("processBroadcastfindsession:replicaStateResponseState:" + returnState.getState());
             //FIXME may want to wait for ack before doing this
             //FIXME waiting for Jxta fix to put this next line back in
             replicatedSessions.remove(replicaState.getId());
         }
         //while here check and remove from manager cache if present
         this.clearFromManagerCache((String)queryState.getId());
         
         if(_logger.isLoggable(Level.FINE)) {
             _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);
         }          
         //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);
         return returnState;
 
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
    
}
