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

package com.sun.ejb.spi.sfsb.store;

import java.util.Map;

import com.sun.ejb.spi.stats.MonitorableSFSBStoreManager;

/**
 * Interface that will be used by the SFSB container to interact
 *  with a session store.
 *
 * Note that there is a one-one association between Container and Store
 *
 * @author  Mahesh Kannan
 */
public interface SFSBStoreManager {
    
    /**
     * Store session data in this beanState
     * This method used only for checkpointing; use passivateSave for passivating
     */
    public void checkpointSave(SFSBBeanState beanState)
        throws SFSBStoreManagerException;    
      
    /**
     * A Factory method to create a SFSBBeanState. The StoreManager
     *	is responsible for filling the SFSBBeanState with the 
     *	correct ClusterId, containerId and SFSBStoreManager
     */
    public SFSBBeanState createSFSBBeanState(Object sessionId,
	    long lastAccess, boolean isNew, byte[] state);

    /**
     *Get the SFSBStoreManagerMonitor
     */
    public MonitorableSFSBStoreManager getMonitorableSFSBStoreManager();

    /**
     * Return the SFSBBeanState containing 
     * the stored session data identified by this sessionKey
     * @return the SFSBBeanState containing stored session data 
     * or null if the sessionKey is invalid / removed
     */ 
    public SFSBBeanState getState(Object sessionKey)
        throws SFSBStoreManagerException;    
     
    /**
     * Called from the Container during container creation
     */
    public void initSessionStore(Map storeEnv)
        throws SFSBStoreManagerException;    
    
    /**
     * Store session data in this beanState
     * This method used only for passivation; use checkpointSave for checkpointing
     */
    public void passivateSave(SFSBBeanState beanState)
        throws SFSBStoreManagerException; 
    
    /**
     * Remove the session data identified by this sessionKey
     */
    public void remove(Object sessionKey)
        throws SFSBStoreManagerException; 
    
    /**
     * Remove all session data for this container
     * called during undeployment
     */
    public void removeAll()
        throws SFSBStoreManagerException;    
      
    /**
     * Remove all the idle/expired session data 
     * that are idle for idleTimeoutInSeconds (passed during initSessionStore())
     */
    public int removeExpiredSessions()
        throws SFSBStoreManagerException;    
      
    /**
     * Called during shutdown of instance
     */
    public void shutdown()
        throws SFSBStoreManagerException;

    /**
     * update only the lastAccessTime to the value time
     * Used when the session has been accessed as well
     * as periodically to keep session alive
     */
    public void updateLastAccessTime(Object sessionKey, long time)
        throws SFSBStoreManagerException;     
    
}
