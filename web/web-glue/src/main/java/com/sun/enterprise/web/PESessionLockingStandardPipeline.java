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

package com.sun.enterprise.web;

import java.io.IOException;
import java.util.logging.Level;
import javax.servlet.ServletException;
import com.sun.logging.LogDomains;
import org.apache.catalina.*;
import org.apache.catalina.session.StandardSession;

/**
 *
 * @author Larry White
 */
public class PESessionLockingStandardPipeline extends WebPipeline {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final java.util.logging.Logger _logger =
        LogDomains.getLogger(PESessionLockingStandardPipeline.class, LogDomains.WEB_LOGGER);
    
    /** 
     * creates an instance of PESessionLockingStandardPipeline
     * @param container
     */       
    public PESessionLockingStandardPipeline(Container container) {
        super(container);
    }    
    
    /**
     * Cause the specified request and response to be processed by the Valves
     * associated with this pipeline, until one of these valves causes the
     * response to be created and returned.  The implementation must ensure
     * that multiple simultaneous requests (on different threads) can be
     * processed through the same Pipeline without interfering with each
     * other's control flow.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {
        
        Session sess = this.lockSession(request);
        try {
            super.invoke(request, response);
        } finally {
            this.unlockSession(request);
        }
    }    
    
    
    /** 
     * lock the session associated with this request
     * this will be a foreground lock
     * checks for background lock to clear
     * and does a decay poll loop to wait until
     * it is clear; after 5 times it takes control for 
     * the foreground
     *
     * @param request
     *
     * @return the session that's been locked
     */     
    protected Session lockSession(Request request) {
        Session sess = request.getSessionInternal(false);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN LOCK_SESSION: sess =" + sess);
        }
        // Now lock the session
        if(sess != null) {
            long pollTime = 200L;
            int maxNumberOfRetries = 7;
            int tryNumber = 0;
            boolean keepTrying = true;
            boolean lockResult = false;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("locking session: sess =" + sess);
            }
            StandardSession haSess = (StandardSession) sess;
            // Try to lock up to maxNumberOfRetries times.
            // Poll and wait starting with 200 ms.
            while(keepTrying) {
                lockResult = haSess.lockForeground();
                if(lockResult) {
                    keepTrying = false;
                    break;
                }
                tryNumber++;
                if(tryNumber < maxNumberOfRetries) {
                    pollTime = pollTime * 2L;
                    threadSleep(pollTime);
                } else {
                    // Tried to wait and lock maxNumberOfRetries times.
                    // Unlock the background so we can take over.
                    _logger.warning("this should not happen-breaking background lock: sess =" + sess);
                    haSess.unlockBackground();
                }              
            }
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("finished locking session: sess =" + sess);
                _logger.finest("LOCK = " + haSess.getSessionLock());
            }
        }

        return sess;
    }    
    
    protected void threadSleep(long sleepTime) {

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            ;
        }

    }    
    
    /** 
     * unlock the session associated with this request
     * @param request
     */     
    protected void unlockSession(Request request) {
        Session sess = request.getSessionInternal(false);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN UNLOCK_SESSION: sess = " + sess);
        }
        // Now unlock the session
        if(sess != null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("unlocking session: sess =" + sess);
            }
            StandardSession haSess = (StandardSession) sess;
            haSess.unlockForeground();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("finished unlocking session: sess =" + sess);
                _logger.finest("LOCK = " + haSess.getSessionLock());
            }
        }        
    }     
}
