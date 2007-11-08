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
 * HAWebEventPersistentManager.java
 *
 * Created on October 3, 2002, 2:18 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import org.apache.catalina.Session;
import org.apache.catalina.LifecycleException;
//START OF 6364900
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
//END OF 6364900

/**
 *
 * @author  lwhite
 */
public class HAWebEventPersistentManager 
        extends HAManagerBase implements WebEventPersistentManager {

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "HAWebEventPersistentManager/1.0";


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    private static final String name = "HAWebEventPersistentManager";    


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
    
    /** Creates a new instance of HAWebEventPersistentManager */
    public HAWebEventPersistentManager() {
        super();
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }        
    }        

    /** called from valve; does the save of session */ 
   /**
   * called from valve; does the save of session
   *
   * @param session 
   *   The session to store
   */    
    public void doValveSave(Session session) {
        
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        long startTime = 0L;
        if(isMonitoringEnabled()) {
            startTime = System.currentTimeMillis();
        }
        StorePool storePool = this.getStorePool();
        HAStorePoolElement haStore = null;

        try
        {                        
            haStore = (HAStorePoolElement) storePool.take();
            //HAStore cachedStore = (HAStore)this.getStore();
            //haStore.setSessions(cachedStore.getSessions());
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("GOT HAStore from pool");
            }
            try
            {
                haStore.setManager(this);
                //_logger.finest("HAStore has manager = " + this);
                //_logger.finest("ENTERING haStore.valveSave");
                haStore.valveSave(session);
                //get the main store instance
                HAStore backgroundStore = (HAStore) this.getStore();
                //update this one's cache
                backgroundStore.getSessions().put(session.getIdInternal(), session);
                //_logger.finest("FINISHED haStore.valveSave");
            } catch (Exception ex) {
                //FIXME handle exception from valveSave
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                haStore.setManager(null);
                if(haStore != null) {
                    try
                    {
                        storePool.put((StorePoolElement)haStore);
                        //_logger.finest("PUT HAStore into pool");
                        if(isMonitoringEnabled()) {
                            long endTime = System.currentTimeMillis();
                            long elapsedTime = (endTime - startTime);
                            if(_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("VALVE_TIME MILLIS = " + elapsedTime);
                            }
                            WebModuleStatistics stats = this.getWebModuleStatistics();
                            stats.processValveSave(elapsedTime);
                            if(_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("VALVE_TIME MILLIS = " + (endTime - startTime));
                            }
                        }
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }
   
    /** create and return a new session; delegates to session factory */
    protected Session createNewSession() {

        //_logger.finest("IN HAWebEventManager>>createNewSession");       
        Session sess = super.createNewSession();
        //_logger.finest("GOT SESSION VIA FACTORY: Class = " + sess.getClass().getName());
        
        return sess;
    }
    
    //START OF 6364900
    public void postRequestDispatcherProcess(ServletRequest request, ServletResponse response) {
        Session sess = this.getSession(request);
        if(sess != null) {
            doValveSave(sess);
        }
        return;
    }
    
    private Session getSession(ServletRequest request) {
        javax.servlet.http.HttpServletRequest httpReq = 
            (javax.servlet.http.HttpServletRequest) request;
        javax.servlet.http.HttpSession httpSess = httpReq.getSession(false);
        if(httpSess == null) {
            return null;
        }
        String id = httpSess.getId();
        Session sess = null;
        try {
            sess = this.findSession(id);
        } catch (java.io.IOException ex) {}

        return sess;
    } 
    //END OF 6364900    
    
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
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;      
    
    //StorePool _pool = null;     
    
}
