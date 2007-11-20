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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import org.apache.catalina.Container;
import org.apache.catalina.Manager;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.StandardSession;

/**
 *
 * @author Larry White
 */
public class PESessionLockingStandardPipeline extends WebPipeline {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger =
        LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
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
        
        this.lockSession(request);
        try {
            super.invoke(request, response);
        } finally {
            this.unlockSession(request);
        }
    }    
    
    /** 
     * get the session associated with this request
     * @param request
     */    
    protected Session getSession(Request request) {
        ServletRequest servletReq = request.getRequest();
        HttpServletRequest httpReq = 
            (HttpServletRequest) servletReq;
        HttpSession httpSess = httpReq.getSession(false);
        if(httpSess == null)
            //need to null out session
            //httpReq.setSession(null);
            return null;
        String id = httpSess.getId();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("SESSION_ID=" + id);
        }
        Manager mgr = this.getContainer().getManager();
        PersistentManagerBase pmb = (PersistentManagerBase)mgr;
        Session sess = null;
        try {
            sess = pmb.findSession(id);
        } catch (java.io.IOException ex) {}
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("RETRIEVED_SESSION=" + sess);
        }
         return sess;
    }          
    
    /** 
     * lock the session associated with this request
     * this will be a foreground lock
     * checks for background lock to clear
     * and does a decay poll loop to wait until
     * it is clear; after 5 times it takes control for 
     * the foreground
     * @param request
     */     
    protected boolean lockSession(Request request) throws ServletException {
        boolean result = false;
        Session sess = this.getSession(request);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN LOCK_SESSION: sess =" + sess);
        }
        //now lock the session
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
            //try to lock up to maxNumberOfRetries times
            //poll and wait starting with 200 ms
            while(keepTrying) {
                lockResult = haSess.lockForeground();
                if(lockResult) {
                    keepTrying = false;
                    result = true;
                    break;
                }
                tryNumber++;
                if(tryNumber < maxNumberOfRetries) {
                    pollTime = pollTime * 2L;
                    threadSleep(pollTime);
                } else {
                    //tried to wait and lock maxNumberOfRetries times; throw an exception
                    //throw new ServletException("unable to acquire session lock");
                    //instead of above; unlock the background so we can take over
                    _logger.warning("this should not happen-breaking background lock: sess =" + sess);
                    haSess.unlockBackground();
                }              
            }
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("finished locking session: sess =" + sess);
                _logger.finest("LOCK = " + haSess.getSessionLock());
            }
        }
        return result;
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
        Session sess = this.getSession(request);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN UNLOCK_SESSION: sess = " + sess);
        }
        //now unlock the session
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
