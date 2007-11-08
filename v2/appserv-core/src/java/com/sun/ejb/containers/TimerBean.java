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

package com.sun.ejb.containers;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.EJBContext;
import javax.ejb.EntityBean;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.EntityContext;
import javax.ejb.EJBLocalObject;
import javax.ejb.Timer;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.Status;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.Switch;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.deployment.EjbDescriptor;

import com.sun.ejb.EJBUtils;
import com.sun.ejb.ContainerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * TimerBean is a coarse-grained persistent representation
 * of an EJB Timer.  It is part of the EJB container but
 * implemented as a CMP 2.1 Entity bean.  The standard
 * CMP behavior is useful in implementing the transactional
 * properties of EJB timers.  When an EJB timer is created
 * by an application, it is not eligible for expiration until
 * the transaction commits.  Likewise, if a timer is cancelled
 * and the transaction rolls back, the timer must be reactivated.
 * To accomplish this, TimerBean registers callbacks with the
 * transaction manager and interacts with the EJBTimerService
 * accordingly.  
 *
 * @author Kenneth Saks
 */
public abstract class TimerBean implements EntityBean {

    private static final Logger logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);

    // Timer states
    private static final int ACTIVE    = 0;
    private static final int CANCELLED = 1;

    private EJBContextImpl context_;

    //
    // CMP fields
    //

    // primary key
    public abstract String getTimerId();      
    public abstract void setTimerId(String timerId);

    public abstract String getOwnerId();
    public abstract void setOwnerId(String ownerId);

    public abstract long getCreationTimeRaw();
    public abstract void setCreationTimeRaw(long creationTime);

    public abstract long getInitialExpirationRaw();
    public abstract void setInitialExpirationRaw(long initialExpiration);

    public abstract long getLastExpirationRaw();
    public abstract void setLastExpirationRaw(long lastExpiration);

    public abstract long getIntervalDuration();
    public abstract void setIntervalDuration(long intervalDuration);

    public abstract int getState();
    public abstract void setState(int state);

    public abstract long getContainerId();
    public abstract void setContainerId(long containerId);

    public abstract Blob getBlob();
    public abstract void setBlob(Blob blob);

    public abstract int getPkHashCode();
    public abstract void setPkHashCode(int pkHash);

    //
    // ejbSelect methods for timer ids
    //
    
    public abstract Set ejbSelectTimerIdsByContainer(long containerId)
        throws FinderException;
    public abstract Set ejbSelectTimerIdsByContainerAndState
        (long containerId, int state) throws FinderException;

    public abstract Set ejbSelectTimerIdsByContainerAndOwner
        (long containerId, String ownerId)
        throws FinderException;
    public abstract Set ejbSelectTimerIdsByContainerAndOwnerAndState
        (long containerId, String ownerId, int state) throws FinderException;

    public abstract Set ejbSelectAllTimerIdsByOwner(String ownerId) 
        throws FinderException;
    public abstract Set ejbSelectAllTimerIdsByOwnerAndState
        (String ownerId, int state) throws FinderException;

   
    //
    // ejbSelect methods for timer beans
    //

    public abstract Set ejbSelectTimersByContainer(long containerId)
        throws FinderException;
    public abstract Set ejbSelectTimersByContainerAndState
        (long containerId, int state) throws FinderException;

    public abstract Set ejbSelectTimersByContainerAndOwner
        (long containerId, String ownerId)
        throws FinderException;
    public abstract Set ejbSelectTimersByContainerAndOwnerAndState
        (long containerId, String ownerId, int state) throws FinderException;

    public abstract Set ejbSelectAllTimersByOwner(String ownerId) 
        throws FinderException;
    public abstract Set ejbSelectAllTimersByOwnerAndState
        (String ownerId, int state) throws FinderException;


    //
    // ejbSelect methods for timer counts
    //
    
    public abstract int ejbSelectCountTimersByContainer(long containerId)
        throws FinderException;
    public abstract int ejbSelectCountTimersByContainerAndState
        (long containerId, int state) throws FinderException;

    public abstract int ejbSelectCountTimersByContainerAndOwner
        (long containerId, String ownerId)
        throws FinderException;
    public abstract int ejbSelectCountTimersByContainerAndOwnerAndState
        (long containerId, String ownerId, int state) throws FinderException;

    public abstract int ejbSelectCountAllTimersByOwner(String ownerId) 
        throws FinderException;
    public abstract int ejbSelectCountAllTimersByOwnerAndState
        (String ownerId, int state) throws FinderException;

    //
    // These data members contain derived state for 
    // some immutable fields.
    //

    // deserialized state from blob
    private boolean blobLoaded_;
    private Object timedObjectPrimaryKey_;
    private transient Serializable info_;

    // Dates
    private transient Date creationTime_;
    private transient Date initialExpiration_;
    private transient Date lastExpiration_;
    
    public TimerPrimaryKey ejbCreate
        (String timerId, long containerId, String ownerId,
         Object timedObjectPrimaryKey, 
         Date initialExpiration, long intervalDuration, Serializable info)
        throws CreateException {

        setTimerId(timerId);
        
        setOwnerId(ownerId);

        return null;
    }
    
    public void ejbPostCreate(String timerId, long containerId, String ownerId,
                              Object timedObjectPrimaryKey, 
                              Date initialExpiration, 
                              long intervalDuration, Serializable info)
        throws CreateException {

        Date creationTime = new Date();
		setCreationTimeRaw(creationTime.getTime());
        creationTime_ = creationTime;

        setInitialExpirationRaw(initialExpiration.getTime());
        initialExpiration_ = initialExpiration;

        setLastExpirationRaw(0);
        lastExpiration_ = null;

        setIntervalDuration(intervalDuration);

        setContainerId(containerId);

        timedObjectPrimaryKey_  = timedObjectPrimaryKey;
        info_ = info;
        blobLoaded_ = true;

        Blob blob = null;
        try {
            blob = new Blob(timedObjectPrimaryKey, info);
        } catch(IOException ioe) {
            CreateException ce = new CreateException();
            ce.initCause(ioe);
            throw ce;
        }

        setBlob(blob);
        setState(ACTIVE);

        if( logger.isLoggable(Level.FINE) ) {
            logger.log(Level.FINE, "TimerBean.postCreate() ::timerId=" +
                       getTimerId() + " ::containerId=" + getContainerId() + 
                       " ::timedObjectPK=" + timedObjectPrimaryKey +
                       " ::info=" + info +
                       " ::initialExpiration=" + initialExpiration +
                       " ::intervalDuration=" + intervalDuration +
                       " :::state=" + stateToString(getState()) + 
                       " :::creationTime="  + creationTime +
                       " :::ownerId=" + getOwnerId()); 
        }

        //
        // Only proceed with transactional semantics if this timer
        // is owned by the current server instance.  NOTE that this
        // will *ALWAYS* be the case for timers created from EJB
        // applications via the javax.ejb.EJBTimerService.create methods.  
        //
        // For testing purposes, ejbCreate takes an ownerId parameter, 
        // which allows us to easily simulate other server instances 
        // by creating timers for them.  In those cases, we don't need
        // the timer transaction semantics and ejbTimeout logic.  Simulating
        // the creation of timers for the same application and different
        // server instances from a script is difficult since the
        // containerId is not generated until after deployment.  
        //
        if( timerOwnedByThisServer() ) {

            // Register a synchronization object to handle the commit/rollback
            // semantics and ejbTimeout notifications.
            Synchronization timerSynch = 
                new TimerSynch(new TimerPrimaryKey(getTimerId()), ACTIVE, 
                               getInitialExpiration(), 
                               getContainer(containerId));
            
            try {
                ContainerSynchronization containerSynch = getContainerSynch();
                containerSynch.addTimerSynchronization
                    (new TimerPrimaryKey(getTimerId()), timerSynch);
            } catch(Exception e) {
                CreateException ce = new CreateException();
                ce.initCause(e);
                throw ce;
            }
        }
    }

    /**
     * Checks whether this timer is owned by the server instance in
     * which we are running.
     */
    private boolean timerOwnedByThisServer() {
        String ownerIdOfThisServer = getOwnerIdOfThisServer();
        return ( (ownerIdOfThisServer != null) &&
                 (ownerIdOfThisServer.equals(getOwnerId())) );
    }

    private String getOwnerIdOfThisServer() {
        return getEJBTimerService().getOwnerIdOfThisServer();                
    }

    private static String stateToString(int state) {
        String stateStr = "UNKNOWN_TIMER_STATE";

        switch(state) {
            case ACTIVE : 
                stateStr = "TIMER_ACTIVE"; 
                break;
            case CANCELLED : 
                stateStr = "TIMER_CANCELLED";
                break;
            default : 
                stateStr = "UNKNOWN_TIMER_STATE";
                break;
        }

        return stateStr;
    }

    private static String txStatusToString(int txStatus) {
        String txStatusStr = "UNMATCHED TX STATUS";

        switch(txStatus) {
            case Status.STATUS_ACTIVE :
                txStatusStr = "TX_STATUS_ACTIVE";
                break;
            case Status.STATUS_COMMITTED : 
                txStatusStr = "TX_STATUS_COMMITTED"; 
                break;
            case Status.STATUS_COMMITTING : 
                txStatusStr = "TX_STATUS_COMMITTING";
                break;
            case Status.STATUS_MARKED_ROLLBACK :
                txStatusStr = "TX_STATUS_MARKED_ROLLBACK";
                break;
            case Status.STATUS_NO_TRANSACTION :
                txStatusStr = "TX_STATUS_NO_TRANSACTION";
                break;
            case Status.STATUS_PREPARED :
                txStatusStr = "TX_STATUS_PREPARED";
                break;
            case Status.STATUS_PREPARING :
                txStatusStr = "TX_STATUS_PREPARING";
                break;
            case Status.STATUS_ROLLEDBACK : 
                txStatusStr = "TX_STATUS_ROLLEDBACK";
                break;
            case Status.STATUS_ROLLING_BACK :
                txStatusStr = "TX_STATUS_ROLLING_BACK";
                break;               
            case Status.STATUS_UNKNOWN :
                txStatusStr = "TX_STATUS_UNKNOWN";
                break;
            default : 
                txStatusStr = "UNMATCHED TX STATUS";
                break;
        }

        return txStatusStr;
    }


    private ContainerSynchronization getContainerSynch() throws Exception {

        EntityContainer container = (EntityContainer) context_.getContainer();
        ContainerFactoryImpl containerFactory = (ContainerFactoryImpl)
            Switch.getSwitch().getContainerFactory();
        Transaction transaction = context_.getTransaction();

        if( transaction == null ) {
            logger.log(Level.FINE, "Context transaction = null. Using " +
                       "invocation instead.");
            InvocationManager iMgr = Switch.getSwitch().getInvocationManager();
            ComponentInvocation i = iMgr.getCurrentInvocation();
            transaction = i.transaction;
        }
        if( transaction == null ) {
            throw new Exception("transaction = null in getContainerSynch " +
                                "for timerId = " + getTimerId());
        }

        ContainerSynchronization containerSync = 
            containerFactory.getContainerSync(transaction);
        return containerSync;
    }

    private static EJBTimerService getEJBTimerService() {
        ContainerFactoryImpl containerFactory = (ContainerFactoryImpl)
            Switch.getSwitch().getContainerFactory();
        return containerFactory.getEJBTimerService();
    }

    private void loadBlob() {
        EJBTimerService timerService = getEJBTimerService();        
        ClassLoader cl = timerService.getTimerClassLoader(getContainerId());
        if( cl != null ) {
            loadBlob(cl);
        } else {
            throw new EJBException("No timer classloader for " + getTimerId());
        }
    }

    private void loadBlob(ClassLoader cl) {
        try {
            Blob blob = getBlob();
            timedObjectPrimaryKey_  = blob.getTimedObjectPrimaryKey(cl);
            info_ = blob.getInfo(cl);
            blobLoaded_ = true;
        } catch(Exception e) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }
    }

    public void setEntityContext(EntityContext context) {
        context_ = (EJBContextImpl) context;
    }
    
    public void unsetEntityContext() {
        context_ = null;      
    }
    
    public void ejbRemove() {}
    
    public void ejbLoad() {

        long lastExpirationRaw = getLastExpirationRaw();
        lastExpiration_ = (lastExpirationRaw > 0) ? 
            new Date(lastExpirationRaw) : null;
        
        // Populate derived state of immutable cmp fields.
        creationTime_ = new Date(getCreationTimeRaw());
        initialExpiration_ = new Date(getInitialExpirationRaw());

        // Lazily deserialize Blob state.  This makes the
        // Timer bootstrapping code easier, since some of the Timer
        // state must be loaded from the database before the 
        // container and application classloader are known.
        timedObjectPrimaryKey_ = null;
        info_       = null;
        blobLoaded_ = false;
    }

    public void ejbStore() {}
    
    public void ejbPassivate() {}
    
    public void ejbActivate() {}

    public boolean repeats() {
        return (getIntervalDuration() > 0);
    }

    public void cancel() throws Exception {

        // First set the timer to the cancelled state.  This step is
        // performed whether or not the current server instance owns
        // the timer.

        if( getState() == CANCELLED ) {
            // already cancelled
            return;
        }

        setState(CANCELLED);

        // Only proceed with JDK timer task cancellation if this timer
        // is owned by the current server instance.
        if( timerOwnedByThisServer() ) {
                    
            TimerPrimaryKey timerId = new TimerPrimaryKey(getTimerId());
            
            // Cancel existing timer task.  Save time at which task would
            // have executed in case cancellation is rolled back.  The 
            // nextTimeout can be null if the timer is currently being 
            // delivered.
            Date nextTimeout = getEJBTimerService().cancelTask(timerId);
            
            ContainerSynchronization containerSynch = getContainerSynch();
            Synchronization timerSynch = 
                containerSynch.getTimerSynchronization(timerId);
            
            if( timerSynch != null ) {
                // This timer was created and cancelled within the
                // same transaction.  No tx synchronization actions
                // are needed, since whether tx commits or rolls back,
                // timer will not exist.
                containerSynch.removeTimerSynchronization(timerId);
                getEJBTimerService().expungeTimer(timerId);
            } else {
                // Set tx synchronization action to handle timer cancellation.
                timerSynch = new TimerSynch(timerId, CANCELLED, nextTimeout,
                                        getContainer(getContainerId()));
                containerSynch.addTimerSynchronization(timerId, timerSynch);
            }

        }

        // NOTE that it's the caller's responsibility to call remove().
        return;
    }

    public Serializable getInfo() {
        if( !blobLoaded_ ) {
            loadBlob();
        }
        return info_;
    }

    public Object getTimedObjectPrimaryKey() {
        if( !blobLoaded_ ) {
            loadBlob();
        }
        return timedObjectPrimaryKey_;
    }   

    public Date getCreationTime() {
        return creationTime_;
    }

    public Date getInitialExpiration() {
        return initialExpiration_;
    }

    public Date getLastExpiration() {
        return lastExpiration_;
    }

    public void setLastExpiration(Date lastExpiration) {
        // can be null
        lastExpiration_ = lastExpiration;
        long lastExpirationRaw = (lastExpiration != null) ?
            lastExpiration.getTime() : 0;
        setLastExpirationRaw(lastExpirationRaw);
    }

    public boolean isActive() {
        return (getState() == ACTIVE);
    }

    public boolean isCancelled() {
        return (getState() == CANCELLED);
    }

    private Set toPKeys(Set ids) {
        Set pkeys = new HashSet();
        for(Iterator iter = ids.iterator(); iter.hasNext();) {
            pkeys.add(new TimerPrimaryKey((String) iter.next()));
        }
        return pkeys;
    }

    //
    // ejbHome methods for timer ids
    //

    public Set ejbHomeSelectTimerIdsByContainer(long containerId) 
        throws FinderException {
        return toPKeys(ejbSelectTimerIdsByContainer(containerId));
    }

    public Set ejbHomeSelectActiveTimerIdsByContainer(long containerId) 
        throws FinderException {
        return toPKeys(ejbSelectTimerIdsByContainerAndState(containerId, 
                                                            ACTIVE));
    }

    public Set ejbHomeSelectCancelledTimerIdsByContainer(long containerId)
        throws FinderException {
        return toPKeys(ejbSelectTimerIdsByContainerAndState
                       (containerId, CANCELLED));
    }

    public Set ejbHomeSelectTimerIdsOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return toPKeys(ejbSelectTimerIdsByContainerAndOwner
                         (containerId, getOwnerIdOfThisServer()));
    }

    public Set ejbHomeSelectActiveTimerIdsOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return toPKeys(ejbSelectTimerIdsByContainerAndOwnerAndState
                       (containerId, getOwnerIdOfThisServer(), ACTIVE));
    }

    public Set ejbHomeSelectCancelledTimerIdsOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return toPKeys(ejbSelectTimerIdsByContainerAndOwnerAndState
                       (containerId, getOwnerIdOfThisServer(), CANCELLED));
    }


    public Set ejbHomeSelectAllTimerIdsOwnedByThisServer()
        throws FinderException {
        return toPKeys(ejbSelectAllTimerIdsByOwner(getOwnerIdOfThisServer()));
    }
   
    public Set ejbHomeSelectAllActiveTimerIdsOwnedByThisServer()
        throws FinderException {
        return toPKeys(ejbSelectAllTimerIdsByOwnerAndState
                       (getOwnerIdOfThisServer(), ACTIVE));
    }

    public Set ejbHomeSelectAllCancelledTimerIdsOwnedByThisServer()
        throws FinderException {
        return toPKeys(ejbSelectAllTimerIdsByOwnerAndState
                       (getOwnerIdOfThisServer(), CANCELLED));
    }

    
    public Set ejbHomeSelectAllTimerIdsOwnedBy(String ownerId)
        throws FinderException {
        return toPKeys(ejbSelectAllTimerIdsByOwner(ownerId));
    }
   
    public Set ejbHomeSelectAllActiveTimerIdsOwnedBy(String ownerId)
        throws FinderException {
        return toPKeys(ejbSelectAllTimerIdsByOwnerAndState
                       (ownerId, ACTIVE));
    }

    public Set ejbHomeSelectAllCancelledTimerIdsOwnedBy(String ownerId)
        throws FinderException {
        return toPKeys(ejbSelectAllTimerIdsByOwnerAndState
                       (ownerId, CANCELLED));
    }

    //
    // ejbHome methods for timer beans
    //

    public Set ejbHomeSelectTimersByContainer(long containerId) 
        throws FinderException {
        return ejbSelectTimersByContainer(containerId);
    }

    public Set ejbHomeSelectActiveTimersByContainer(long containerId) 
        throws FinderException {
        return ejbSelectTimersByContainerAndState(containerId, 
                                                  ACTIVE);
    }

    public Set ejbHomeSelectCancelledTimersByContainer(long containerId)
        throws FinderException {
        return ejbSelectTimersByContainerAndState
                       (containerId, CANCELLED);
    }

    public Set ejbHomeSelectTimersOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return ejbSelectTimersByContainerAndOwner
                         (containerId, getOwnerIdOfThisServer());
    }

    public Set ejbHomeSelectActiveTimersOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return ejbSelectTimersByContainerAndOwnerAndState
                       (containerId, getOwnerIdOfThisServer(), ACTIVE);
    }

    public Set ejbHomeSelectCancelledTimersOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return ejbSelectTimersByContainerAndOwnerAndState
                       (containerId, getOwnerIdOfThisServer(), CANCELLED);
    }


    public Set ejbHomeSelectAllTimersOwnedByThisServer()
        throws FinderException {
        return ejbSelectAllTimersByOwner(getOwnerIdOfThisServer());
    }
   
    public Set ejbHomeSelectAllActiveTimersOwnedByThisServer()
        throws FinderException {
        return ejbSelectAllTimersByOwnerAndState
                       (getOwnerIdOfThisServer(), ACTIVE);
    }

    public Set ejbHomeSelectAllCancelledTimersOwnedByThisServer()
        throws FinderException {
        return ejbSelectAllTimersByOwnerAndState
                       (getOwnerIdOfThisServer(), CANCELLED);
    }

    
    public Set ejbHomeSelectAllTimersOwnedBy(String ownerId)
        throws FinderException {
        return ejbSelectAllTimersByOwner(ownerId);
    }
   
    public Set ejbHomeSelectAllActiveTimersOwnedBy(String ownerId)
        throws FinderException {
        return ejbSelectAllTimersByOwnerAndState
                       (ownerId, ACTIVE);
    }

    public Set ejbHomeSelectAllCancelledTimersOwnedBy(String ownerId)
        throws FinderException {
        return ejbSelectAllTimersByOwnerAndState
                       (ownerId, CANCELLED);
    }   


    //
    // ejbHome methods for timer counts
    //

    public int ejbHomeSelectCountTimersByContainer(long containerId) 
        throws FinderException {
        return ejbSelectCountTimersByContainer(containerId);
    }

    public int ejbHomeSelectCountActiveTimersByContainer(long containerId) 
        throws FinderException {
        return ejbSelectCountTimersByContainerAndState(containerId, 
                                                       ACTIVE);
    }

    public int ejbHomeSelectCountCancelledTimersByContainer(long containerId)
        throws FinderException {
        return ejbSelectCountTimersByContainerAndState
                       (containerId, CANCELLED);
    }

    public int ejbHomeSelectCountTimersOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return ejbSelectCountTimersByContainerAndOwner
                         (containerId, getOwnerIdOfThisServer());
    }

    public int ejbHomeSelectCountActiveTimersOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return ejbSelectCountTimersByContainerAndOwnerAndState
                       (containerId, getOwnerIdOfThisServer(), ACTIVE);
    }

    public int ejbHomeSelectCountCancelledTimersOwnedByThisServerByContainer
        (long containerId) 
        throws FinderException {
        return ejbSelectCountTimersByContainerAndOwnerAndState
                       (containerId, getOwnerIdOfThisServer(), CANCELLED);
    }


    public int ejbHomeSelectCountAllTimersOwnedByThisServer()
        throws FinderException {
        return ejbSelectCountAllTimersByOwner(getOwnerIdOfThisServer());
    }
   
    public int ejbHomeSelectCountAllActiveTimersOwnedByThisServer()
        throws FinderException {
        return ejbSelectCountAllTimersByOwnerAndState
                       (getOwnerIdOfThisServer(), ACTIVE);
    }

    public int ejbHomeSelectCountAllCancelledTimersOwnedByThisServer()
        throws FinderException {
        return ejbSelectCountAllTimersByOwnerAndState
                       (getOwnerIdOfThisServer(), CANCELLED);
    }

    
    public int ejbHomeSelectCountAllTimersOwnedBy(String ownerId)
        throws FinderException {
        return ejbSelectCountAllTimersByOwner(ownerId);
    }
   
    public int ejbHomeSelectCountAllActiveTimersOwnedBy(String ownerId)
        throws FinderException {
        return ejbSelectCountAllTimersByOwnerAndState
                       (ownerId, ACTIVE);
    }

    public int ejbHomeSelectCountAllCancelledTimersOwnedBy(String ownerId)
        throws FinderException {
        return ejbSelectCountAllTimersByOwnerAndState
                       (ownerId, CANCELLED);
    }   

    public boolean ejbHomeCheckStatus(String resourceJndiName,
                                      boolean checkDatabase) {

        boolean success = false;

        Connection connection = null;

        try {

            InitialContext ic = new InitialContext();
            
            DataSource dataSource = (DataSource) ic.lookup(resourceJndiName);

            if( checkDatabase ) {
                connection = dataSource.getConnection();
                
                connection.close();
                
                connection = null;
                
                // Now try to a query that will access the timer table itself.
                // Use a query that won't return a lot of data(even if the
                // table is large) to reduce the overhead of this check.
                ejbSelectCountTimersByContainer(0);
            }

            success = true;           
                        
        } catch(Exception e) {

            logger.log(Level.WARNING, "ejb.timer_service_init_error", 
                       "");
            // Log exception itself at FINE level.  The most likely cause
            // is a connection error when the database is not started.  This
            // is already logged twice by the jdbc layer.
            logger.log(Level.FINE, "ejb.timer_service_init_error", e);

        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                } catch(Exception e) {
                    logger.log(Level.FINE, "timer connection close exception",
                               e);
                }
            }
        }

        return success;
    }

    /**
     * Many DBs have a limitation that at most one field per DB
     * can hold binary data.  As a workaround, store both EJBLocalObject
     * and "info" as a single Serializable blob.  This is necessary 
     * since primary key of EJBLocalObject could be a compound object.
     * This class also isolates the portion of Timer data that is
     * associated with the TimedObject itself.  During deserialization,
     * we must use the application class loader for the timed object,
     * since both the primary key and info object can be application
     * classes.
     *
     */
    public static class Blob implements Serializable {

        private byte[] primaryKeyBytes_ = null;
        private byte[] infoBytes_ = null;

        public Blob() {
        }

        public Blob(Object primaryKey, Serializable info)
            throws IOException {
            if( primaryKey != null ) {
                primaryKeyBytes_ = EJBUtils.serializeObject(primaryKey);
            } 
            if( info != null ) {
                infoBytes_ = EJBUtils.serializeObject(info);
            }
        }
        
        public Object getTimedObjectPrimaryKey(ClassLoader cl) 
            throws Exception {
            Object pKey = null;
            if( primaryKeyBytes_ != null) {
                pKey = EJBUtils.deserializeObject(primaryKeyBytes_, cl);
                if( logger.isLoggable(Level.FINER) ) {
                    logger.log(Level.FINER, "Deserialized blob : " + pKey);
                }
            }
            return pKey;
        }

        public Serializable getInfo(ClassLoader cl) throws Exception {
            Serializable info = null;
            if( infoBytes_ != null) {
                info = (Serializable)EJBUtils.deserializeObject(infoBytes_, cl);
                if( logger.isLoggable(Level.FINER) ) {
                    logger.log(Level.FINER, "Deserialized blob : " + info);
                }
            }
            return info;
        }
    }

    private static class TimerSynch implements Synchronization {

        private TimerPrimaryKey timerId_;
        private int state_;
        private Date timeout_;
        private BaseContainer container_;
        
        public TimerSynch(TimerPrimaryKey timerId, int state, Date timeout,
                          BaseContainer container) {
            timerId_   = timerId;
            state_     = state;
            timeout_   = timeout;
            container_ = container;
        }

        public void afterCompletion(int status) {
            EJBTimerService timerService = getEJBTimerService();

            if( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "TimerSynch::afterCompletion. " +
                           "timer state = " + stateToString(state_) + 
                           " , " + "timer id = " + 
                           timerId_ + " , JTA TX status = " + 
                           txStatusToString(status) + " , " + 
                           "timeout = " + timeout_);
            }

            switch(state_) {
            case ACTIVE : 
                if( status == Status.STATUS_COMMITTED ) {
                    timerService.scheduleTask(timerId_, timeout_);
                    container_.incrementCreatedTimedObject();
                } else {
                    timerService.expungeTimer(timerId_);
                }
                break;
            case CANCELLED :
                if( status == Status.STATUS_ROLLEDBACK ) {
                    if( timeout_ != null ) {
                        // Timer was cancelled while in the SCHEDULED state.  
                        // Just schedule it again with the original timeout.
                        timerService.scheduleTask(timerId_, timeout_);
                    } else {
                        // Timer was cancelled from within its own ejbTimeout 
                        // and then rolledback. 
                        timerService.restoreTaskToDelivered(timerId_);
                    }
                } else {
                    timerService.expungeTimer(timerId_);
                    container_.incrementRemovedTimedObject();
                }
                break;
            }
        }

        public void beforeCompletion() {}

    }

    public static void testCreate(String timerId, EJBContext context,
                                   String ownerId,
                                  Date initialExpiration, 
                                   long intervalDuration, 
                                   Serializable info) throws CreateException {
        
        EJBTimerService ejbTimerService = getEJBTimerService();
        TimerLocalHome timerLocalHome = ejbTimerService.getTimerBeanHome();

        EjbDescriptor ejbDesc = (EjbDescriptor)
            Switch.getSwitch().
            getDescriptorFor(((EJBContextImpl) context).getContainer());
        long containerId = ejbDesc.getUniqueId();

        Object timedObjectPrimaryKey = (context instanceof EntityContext) ?
                ((EntityContext)context).getPrimaryKey() : null;

        timerLocalHome.create(timerId, containerId, ownerId,
                                     timedObjectPrimaryKey, initialExpiration,
                                     intervalDuration, info);
        return;
    }

    public static void testMigrate(String fromOwnerId) {

        EJBTimerService ejbTimerService = getEJBTimerService();
        ejbTimerService.migrateTimers(fromOwnerId);

    }

    private BaseContainer getContainer(long containerId) {
        ContainerFactory cf = Switch.getSwitch().getContainerFactory();
        return (BaseContainer) cf.getContainer(containerId);
    }

}
