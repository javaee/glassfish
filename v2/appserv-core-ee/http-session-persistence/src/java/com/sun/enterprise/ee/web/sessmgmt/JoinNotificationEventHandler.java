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
 * JoinNotificationEventHandler.java
 *
 * Created on November 30, 2006, 10:12 AM
 * This class processes join notifications from GMS
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.ee.cms.core.*;


/**
 *
 * @author lwhite
 */
public class JoinNotificationEventHandler implements CallBack {
    
    protected final static String SENDER_PIPE 
        = AbstractPipeWrapper.SENDER_PIPE;
    protected final static String RECEIVER_PIPE
        = AbstractPipeWrapper.RECEIVER_PIPE;
    private static HashMap pendingJoinsOld = new HashMap();
    
    private static HashMap pendingJoins = new HashMap();
    private static AtomicBoolean processingJoinFlag 
        = new AtomicBoolean(false);
    
    /** Creates a new instance of JoinNotificationEventHandler */
    public JoinNotificationEventHandler() {
    }
    
    public void processNotification(Signal notification) {
        String newPartnerInstance = notification.getMemberToken();
        System.out.println("Received Join Notification: " + newPartnerInstance);

        System.out.println("Received Join Notification: adding pending join for" + newPartnerInstance);        
        addPendingJoin(newPartnerInstance);

        /*
        if(!ReplicationHealthChecker.isStopping() 
            && !ReplicationHealthChecker.isReplicationCommunicationOperational()) {
         */

        /*
        if(!ReplicationHealthChecker.isStopping()) {            
            JxtaReplicationReceiver jxtaReplicationReceiver
                = (JxtaReplicationReceiver) ReplicationHealthChecker.getReplicationReceiver();
            System.out.println("join notification causing call to reconnect sender side");
            jxtaReplicationReceiver.connectSenderSideToNew(newPartnerInstance);
        } 
         */
      
    }
    
    //*********************OLD VERSIONS************************
    
    static boolean isJoinPendingOld(String instanceName) {
        long timeNow = System.currentTimeMillis();
        Long pendingJoinTimeMillis = (Long)pendingJoinsOld.get((String)instanceName);
        if(pendingJoinTimeMillis != null &&
            (timeNow - pendingJoinTimeMillis.longValue()) < 300000 ) {
            System.out.println("join is pending");
            removePendingJoinOld(instanceName);
            return true;
        } else {
            System.out.println("join is not pending or more than 5 min old");
            return false;
        }
    }
    
    static void addPendingJoinOld(String instanceName) {
        pendingJoinsOld.put(instanceName, new Long(System.currentTimeMillis()));
    }
    
    static void removePendingJoinOld(String instanceName) {
        pendingJoinsOld.remove(instanceName);
    }    

    static void checkAndDoJoinForOld(String newPartnerInstance) {
        if(!isJoinPendingOld(newPartnerInstance)) {
            return;
        }
        if(!ReplicationHealthChecker.isStopping()) {            
            JxtaReplicationReceiver jxtaReplicationReceiver
                = (JxtaReplicationReceiver) ReplicationHealthChecker.getReplicationReceiver();
            System.out.println("join notification AND readiness causing call to reconnect sender side");
            jxtaReplicationReceiver.connectSenderSideToNew(newPartnerInstance);
        }
    }
    //*********************END OLD VERSIONS************************
    //*******************new versions start**********************
    
    static boolean isJoinPending(String instanceName) {
        long timeNow = System.currentTimeMillis();
        PendingJoin pendingJoin = (PendingJoin)pendingJoins.get((String)instanceName);
        if(pendingJoin == null) {
            return false;
        }
        long pendingJoinTimeMillis = pendingJoin.getCreationTime();
        //pending join is marked valid or pending for less than 5 min
        if(pendingJoin.isValid() || (timeNow - pendingJoinTimeMillis) < 300000 ) {
            System.out.println("join is pending");
            //set as valid pendingJoin in case it is not processed
            //right away
            pendingJoin.setValid(true);
            //do not remove until we are processing
            //removePendingJoin(instanceName);
            return true;
        } else {
            //pending join is not marked valid and more than 5 min old
            //so it should be removed
            System.out.println("join is not pending or more than 5 min old");
            removePendingJoin(instanceName);
            return false;
        }
    }
    
    static void addPendingJoin(String instanceName) {
        PendingJoin pendingJoin = new PendingJoin(instanceName, System.currentTimeMillis());
        pendingJoins.put(instanceName, pendingJoin);
    }
    
    static void removePendingJoin(String instanceName) {
        pendingJoins.remove(instanceName);
    }    

    static void checkAndDoJoinFor(String newPartnerInstance) {
        //do not bother if stopping
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }
        //do not bother if no pending join for this proposed instance
        //this could include a pending join timing out after 5 min.
        if(!isJoinPending(newPartnerInstance)) {
            //FIXME next line is work-around for gms join notifications
            //failing to arrive consistently
            //so if a readyMsg triggers the checkAndDoJoinFor and we 
            //do not yet have a pending join, rather than quit we
            //add the pending join ourselves here and try to go ahead
            addPendingJoin(newPartnerInstance);
            //next line had this return statement temporarily commented out
            //return;
            //end FIXME
        }
        if(reserveProcessingJoin()) {
            System.out.println("got reservation:entering doProcessJoin:instanceName:" + newPartnerInstance);
            //we have successfully reserved join processing
            // - go ahead and process
            doProcessJoin(newPartnerInstance);
        } else {
            System.out.println("refused reservation:entering doPollCheckForJoinProcessing");            
            //must enter a poll wait
            doPollCheckForJoinProcessing(1000L);
        }
    }
    
    private static void doProcessJoin(String newPartnerInstance) {
        long startTime = System.currentTimeMillis();
        System.out.println("In doProcessJoin:newPartnerInstance = " + newPartnerInstance + " this instance = " + getInstanceName());
        JxtaReplicationReceiver jxtaReplicationReceiver
            = (JxtaReplicationReceiver) ReplicationHealthChecker.getReplicationReceiver();
        System.out.println("join notification AND readiness causing call to reconnect sender side");
        //not necessary we have already successfully reserved join processing
        //setProcessingJoin(true);
        try {
            jxtaReplicationReceiver.connectSenderSideToNew(newPartnerInstance);
        } finally {            
            removePendingJoin(newPartnerInstance);
            setProcessingJoin(false);
            System.out.println("doProcessJoin: this instance = " + getInstanceName() + " for: " + newPartnerInstance + " took " + (System.currentTimeMillis() - startTime) + " millis");
        }
    }
    
    private static void doPollCheckForJoinProcessing(long sleepTime) {
        //FIXME might want a timeout for this
        System.out.println("In doPollCheckForJoinProcessing");
        boolean shouldContinue = true;
        while(shouldContinue) {
            try {
                Thread.currentThread().wait(sleepTime);
            } catch (InterruptedException ex) {
                //deliberate no-op
            }
            PendingJoin nextPendingJoin = getNextPendingJoin();
            if(nextPendingJoin == null) {
                shouldContinue = false;
                break;
            }
            String nextPartnerInstance = nextPendingJoin.getInstanceName();
            checkAndDoJoinFor(nextPartnerInstance);
        }
        
    }
    
    private static PendingJoin getNextPendingJoin() {
        //get next pending join - earliest timestamp
        PendingJoin result = null;
        ArrayList pendingJoinsToBeRemoved = new ArrayList();
        Collection pendJoins = pendingJoins.values();
        Iterator it = pendJoins.iterator();
        //while iterating also collect and then remove any expired instanceNames
        while(it.hasNext()) {
            PendingJoin nextPendingJoin = (PendingJoin)it.next();
            //skip and add to remove list any expired instanceName
            if(nextPendingJoin.isCandidateForRemoval(30000L)) {
                pendingJoinsToBeRemoved.add(nextPendingJoin.getInstanceName());
                continue;
            }
            if(result == null) {
                result = nextPendingJoin;
            } else {
                if(nextPendingJoin.getCreationTime() < result.getCreationTime()) {
                    result = nextPendingJoin;
                }
            }
        }
        removeAll(pendingJoinsToBeRemoved);
        return result;
    }
    
    private static void removeAll(ArrayList removeList) {
        for(int i=0; i<removeList.size(); i++) {
            String nextInstanceName = (String)removeList.get(i);
            removePendingJoin(nextInstanceName);
        }
    }
    
    public static boolean isProcessingJoin() {
        return processingJoinFlag.get();
    }
    
    /** attempt to set processing to true if it is false
     * else it fails
     */
    public static boolean reserveProcessingJoin() {
        return (processingJoinFlag.compareAndSet(false, true));
    }    
    
    public static void setProcessingJoin(boolean value) {
        processingJoinFlag.set(value);
    }
    //*******************new versions end********************** 
    
    static String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }
    
}
