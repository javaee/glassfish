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

package com.sun.ejb.ee.sfsb.store;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.ejb.base.sfsb.util.EJBServerConfigLookup;
import com.sun.ejb.spi.sfsb.util.SFSBUUIDUtil;
import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;

import com.sun.ejb.Container;

import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.ShutdownCleanupCapable;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.Application;

import java.sql.Connection;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p> </p>
 * <p>Company: Sun Microsystems Inc.</p>
 * @author Sridhar Satuloori
 * @version
 */

public abstract class BaseSFSBStoreManager
    implements SFSBStoreManager, ShutdownCleanupCapable {

    /** 
     * Container to which this store belongs
     */
    protected Container container = null;
    
    /** 
     * id of the Container to which this store belongs
     */
    protected long containerID;
    
    /** 
     * the id of the cluster
     */    
    protected String clusterID = null;
    
    /** 
     * the idleTimeoutInSeconds
     */     
    protected int idleTimeoutInSeconds;
    
    //FIXME containerId (as opposed to containerID) seems out of use
    //remove after testing
    protected String containerId = null;
    
    protected EJBModuleStatistics _statistics = new EJBModuleStatistics();


    protected static final Logger _logger;
    static {
        _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
    }
    
    /** 
     * debug flag
     */
    protected boolean debug = false;

    public BaseSFSBStoreManager() {
    }
    
    /**
     * Called from the Container during container creation
     */
    public void initSessionStore(Map storeEnv)
        throws SFSBStoreManagerException {
        if(storeEnv.get("clusterId") != null) {
            String clusId = (String) storeEnv.get("clusterId");
            this.clusterID = clusId;
        }
        if(storeEnv.get("containerId") != null) {
            Long contIdLong = (Long) storeEnv.get("containerId");
            long contId = contIdLong.longValue();
            this.containerID = contId;
        }
        if(storeEnv.get("idleTimeoutInSeconds") != null) {
            Integer timeoutInt = (Integer) storeEnv.get("idleTimeoutInSeconds");
            int timeout = timeoutInt.intValue();
            this.idleTimeoutInSeconds = timeout;
        }                              
    }     

    /**
     * Called from the Container during container creation
     * FIXME: not used -- remove during cleanup
     */
    public void initSessionStore(String clusterId, long containerId,
                               int idleTimeoutInSeconds) {
        if(_logger.isLoggable(Level.FINER)) {                           
            _logger.entering("BaseSFSBStoreManager", "initSessionStore",
                             new Object[] {clusterId, new Long(containerId),
                             new Integer(idleTimeoutInSeconds)});
        }
        this.clusterID = clusterId;
        this.containerID = containerId;
        this.idleTimeoutInSeconds = idleTimeoutInSeconds;
        if(_logger.isLoggable(Level.FINER)) { 
            _logger.exiting("HASFSBStoreManager", "initSessionStore");
        }
    }
    
    /** A Factory method to create a SFSBBeanState. The StoreManager
     * 	is responsible for filling the SFSBBeanState with the
     * 	correct ClusterId, containerId and SFSBStoreManager
     */
    public SFSBBeanState createSFSBBeanState(Object sessionId, long lastAccess, boolean isNew, byte[] state) {
        return new SFSBBeanState(
            this.getClusterID(),
            containerID,  //long
            sessionId,
            lastAccess,
            isNew,
            state,
            (SFSBStoreManager)this);
    }    

    public void setContainer(Container ejbContainer) {
        this.container = ejbContainer;
    }

    public Container getContainer() {
        return container;
    }

    /** 
     * 	return the containerId	
     */
    public String getContainerID() {
        //FIXME: for now this is converting the long passed
        //in to the manager to a String that the store class uses
        //later convert the Store class to use a long also
        Long longContId = new Long(containerID);
        return longContId.toString();
        /*
        EjbDescriptor ejbd = this.getContainer().getEjbDescriptor();
        EjbDescriptor ed = container.getEjbDescriptor();
        String ejbName = ed.getName();
        String jarName = ed.getEjbBundleDescriptor().getArchivist().getArchiveUri();
        Application app = ed.getEjbBundleDescriptor().getApplication();
        String appName = null;
        if (app.isVirtual()) { // standlone module
            appName = null;
            containerId = ejbName + ":" + jarName;
        }
        else {
            appName = app.getRegistrationName();
            containerId = ejbName + ":" + jarName + ":" + appName;
        }
        return containerId;
         */
    }
    
    /** 
     * 	return the clusterId	
     */
    public String getClusterID() {
        if (clusterID == null || clusterID.equals("")) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            this.clusterID = lookup.getClusterIdFromConfig();
        }
        if(_logger.isLoggable(Level.FINEST)) { 
            _logger.log(Level.FINEST,
                "Cluster Id in BaseSFSBStoreManager.getClusterID=" + clusterID);
        }
        return clusterID;
    }
    
    public int getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds;
    }
    
    /** return the ejb module statistics */
    public EJBModuleStatistics getEJBModuleStatistics() {
        return _statistics;
    }    
    
    protected boolean isMonitoringEnabled() {
        return EJBServerConfigLookup.isMonitoringEnabled();
    }
    
    /** this method is used to append debug monitor statistics */
    protected void appendStats(StringBuffer sbuf) {
        //deliberate no-op here
    }

    public void shutdown() {

    }
    
    public int doShutdownCleanup(){ return 0;}
    public void doCloseCachedConnection(){}
    public void putConnection(Connection conn){}  

    public void debug(String s) {
        System.out.println(s);
    }
    
}
