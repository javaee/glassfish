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
 * JxtaConnectErrorManager.java
 *
 * Created on July 13, 2006, 3:42 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

/**
 *
 * @author Larry White
 */
public class JxtaConnectErrorManager {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;    
    
    /**
    * How long to wait in ms until we throw a TimeoutException.  Default 
    * is 5 minutes.
    */
    private long timeoutMsecs = 1000 * 60 * 5; 
    
    /** 
    * When the transaction started
    */
    private long txStartTime;
    
    /**
    * How long the transaction took (valid only when txCompleted is true)
    */
    private long txDuration = 0;    
    
    /**
    * state information about the transaction
    */
    private static final int TX_IDLE = 1;
    private static final int TX_STARTED = 2;
    private static final int TX_COMPLETED = 3;
    
    private int txState = TX_IDLE;    
    
    /** Creates a new instance of JxtaConnectErrorManager */
    public JxtaConnectErrorManager(long timeoutSecs) {
        this.timeoutMsecs = timeoutSecs * 1000;
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }        
    }
    
    /**
    * Check the current error received from Jxta
    *
    * If the error is retryable, this method does the following things:
    *   - If we exceed transaction timeout, throw HATimeoutException 
    *  
    * @param e   
    *   the IOException that has been received
    *
    * @throws HATimeoutException
    *   if our transaction timeout is exceeded
    */
    public void checkError(IOException e) throws HATimeoutException {   

        checkTimeouts();

    }
    
    /**
    * Check to see if timeouts have been exceeded.  If timeTilError
    * is exceeded, we log an error saying we've been waiting too long
    * and this counts as an error.  If timeTillExit is exceeded, we
    * log an error and do a hard exit of the application
    */
    protected void checkTimeouts() throws HATimeoutException {
        if ( getElapsedTime() > timeoutMsecs ) {
            System.out.println("JxtaConnectErrorManager:timeout:getElapsedTime=" + getElapsedTime()
            + " timeoutMsecs=" + timeoutMsecs);
            throw new HATimeoutException("Unable to complete a transaction " +
            "after " + timeoutMsecs / 60000 + " minutes.");
        }
    }
    
    /**
    * Indicate that a transaction has started.   This initializes the
    * everything only the first time it is called for this transaction.
    * This allows you to safely call txStart() in a retry loop.
    */
    public synchronized void txStart() {
        if ( txState != TX_STARTED ) {
            txState = TX_STARTED;
            txStartTime = System.currentTimeMillis();
            txDuration = 0;
        }
    }

    /**
    * Indicate that a transaction has ended.  This stops the timer
    * and sets a flag so that txCompleted() returns true.
    */
    public void txEnd()
    {
        txState = TX_COMPLETED;
        txDuration = System.currentTimeMillis() - txStartTime;
    } 

    /**
    * Return true if this transaction has completed
    */
    public boolean isTxCompleted() {
        return txState == TX_COMPLETED;
    }
    
    /**
    * Find out how long it's been since we started the transaction
    * 
    * @return the elapsed time of the transaction in milliseconds
    */
    public long getElapsedTime() {
        if ( txState == TX_STARTED ) {
            return System.currentTimeMillis() - txStartTime;
        }
        else if ( txState == TX_COMPLETED )
        {
            return txDuration;
        }
        else
        {
            return 0;
        }
    }    
    
}
