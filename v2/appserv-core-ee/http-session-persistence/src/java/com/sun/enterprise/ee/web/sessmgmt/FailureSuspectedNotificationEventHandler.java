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
 * SuspectedFailureNotificationEventHandler.java
 *
 * Created on November 30, 2006, 10:27 AM
 * This class processes suspected failure notifications from GMS
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.HashMap;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.ee.cms.core.*;

/**
 *
 * @author lwhite
 */
public class FailureSuspectedNotificationEventHandler implements CallBack {
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    private static HashMap suspectedFailures = new HashMap();
    
    /** Creates a new instance of SuspectedFailureNotificationEventHandler */
    public FailureSuspectedNotificationEventHandler() {
    }
    
    public void processNotification(Signal notification) {
        String failedPartnerInstance = notification.getMemberToken();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Received Suspected Failure Notification: " + failedPartnerInstance);
        }
        //check and if 2nd suspected failure in 6 second window
        //then process the failure
        checkSuspectedFailureFor(failedPartnerInstance);
        // was this
        //if(!ReplicationHealthChecker.isStopping() 
        //    && !ReplicationHealthChecker.isReplicationCommunicationOperational()) {
        /*
        if(!ReplicationHealthChecker.isStopping()) {            
            JxtaReplicationReceiver jxtaReplicationReceiver
                = (JxtaReplicationReceiver) ReplicationHealthChecker.getReplicationReceiver();
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("suspected failure notification causing call to respondToFailure");
            }            
            jxtaReplicationReceiver.respondToFailure(failedPartnerInstance);
        }
         */        
    }
    
    static boolean isSuspectedFailureCredible(String instanceName) {
        long timeNow = System.currentTimeMillis();
        Long suspectedFailureTimeMillis 
            = (Long)suspectedFailures.get((String)instanceName);        
        if(suspectedFailureTimeMillis != null) {
            removeSuspectedFailure(instanceName);
            if((timeNow - suspectedFailureTimeMillis.longValue()) < 6000 ) {
                System.out.println("credible suspected failure of " + instanceName);          
                return true;
            } else {
                System.out.println("suspected failure is not pending or more than 6 sec old");
                //add the new suspected failure
                addSuspectedFailure(instanceName);
                return false;
            }
        } else {
            //this is the first suspected failure - adding it
            System.out.println("Received Suspected Failure Notification: adding suspected failure for" + instanceName);        
            addSuspectedFailure(instanceName);            
            return false;
        }
    }
    
    static void addSuspectedFailure(String instanceName) {
        suspectedFailures.put(instanceName, new Long(System.currentTimeMillis()));
    }
    
    static void removeSuspectedFailure(String instanceName) {
        suspectedFailures.remove(instanceName);
    }    

    //check and if 2nd suspected failure in 6 second window
    //then process the failure    
    static void checkSuspectedFailureFor(String instanceName) {
        if(!isSuspectedFailureCredible(instanceName)) {
            return;
        }
        if(!ReplicationHealthChecker.isStopping()) {            
            JxtaReplicationReceiver jxtaReplicationReceiver
                = (JxtaReplicationReceiver) ReplicationHealthChecker.getReplicationReceiver();
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("suspected failure notification causing call to respondToFailure");
            }            
            jxtaReplicationReceiver.respondToFailure(instanceName);
        } 
    }    
    
}
