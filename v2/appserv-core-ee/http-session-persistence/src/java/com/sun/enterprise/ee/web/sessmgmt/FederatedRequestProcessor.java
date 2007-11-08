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
 * FederatedRequestProcessor.java
 *
 * Created on October 26, 2006, 12:51 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import java.util.concurrent.*;

/**
 *
 * @author Larry White
 */
public class FederatedRequestProcessor implements Callable {
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    /**
     * Creates a new instance of FederatedRequestProcessor
     */
    public FederatedRequestProcessor(ReplicationState aSendQueryState, int numberOfRequests, long waitTime) {
        sendQueryState = aSendQueryState;
        counter = numberOfRequests;
        waitTimeMillis = waitTime;
    }
    
    /**
     * Creates a new instance of FederatedRequestProcessor
     */
    public FederatedRequestProcessor(ReplicationState aSendQueryState, int numberOfRequests, long waitTime, long version) {
        this(aSendQueryState, numberOfRequests, waitTime);
        this.version = version;
    }    
    
    public ReplicationState doFederatedQuery() {
        ReplicationState result = null;
        result = this.call();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("FederatedRequestProcessor>>doFederatedQuery:result = " + result);
        }        
        if(result != null) {
            ReplicationResponseRepository.putEntry(result);
        }
        cleanup();
        return result;
    }
    
    public ReplicationState call() {
        JxtaReplicationSender sender = null;
        synchronized(this) {
            //first send messages
            sender = JxtaReplicationSender.createInstance();
            sender.sendBroadcastQuery(sendQueryState);
            //now block and wait for time or all responses back
            try {
                wait(waitTimeMillis);
            } catch(InterruptedException ex) {
                //nothing
            }
        }
        return bestResult;
        
    }
    
    public synchronized void processQueryResponse(ReplicationState incomingState) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("FedReqProcessor:processQueryResponse:incomingVersion=" + incomingState.getVersion() 
                + " source_instance= " + incomingState.getInstanceName() 
                + " counter=" + counter);
        }        
        if(isLaterVersion(incomingState)) {
            bestResult = incomingState;
        }
        if(isUsingVersioning()) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("FederatedRequestProcessor:using version:incoming version= " + incomingState.getVersion());
            }            
            if(incomingState.getVersion() == version) {
                counter = 0;
            } else {
                counter--;
            }
        } else {
            counter--;
        }
        if(counter == 0) {
            this.notify();
        }
    }
    
    boolean isLaterVersion(ReplicationState incomingState) {
        if (bestResult == null) {
            return true;
        }
        //do not take null state over non-null state
        if (bestResult.getState() != null && incomingState.getState() == null) {
            return false;
        }
        return(incomingState.getVersion() > bestResult.getVersion());
    }
    
    private void cleanup() {
        bestResult = null;
        sendQueryState = null;
        counter = 0;
        waitTimeMillis = 0L;        
    }
    
    private boolean isUsingVersioning() {
        return (version >= 0L);
    }
    
    ReplicationState sendQueryState = null;
    ReplicationState bestResult = null;
    int counter = 0;
    long waitTimeMillis = 0L;
    long version = -1L;
    
}
