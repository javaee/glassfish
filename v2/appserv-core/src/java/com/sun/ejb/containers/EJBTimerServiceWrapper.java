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

import java.util.Date;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.io.Serializable;

import javax.ejb.EJBLocalObject;
import javax.ejb.TimerService;
import javax.ejb.Timer;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.CreateException;

import com.sun.enterprise.Switch;

/*
 * EJBTimerServiceWrappers is the application-level representation
 * of the EJB timer service. 
 *
 * @author Kenneth Saks
 */
public class EJBTimerServiceWrapper implements TimerService {

    private EJBTimerService timerService_;
    private EJBContextImpl ejbContext_;
    private long containerId_;

    private boolean entity_;

    // Only used for entity beans
    private Object timedObjectPrimaryKey_;

    public EJBTimerServiceWrapper(EJBTimerService timerService,
                                  EJBContextImpl ejbContext) 
    {
        timerService_ = timerService;
        ejbContext_   = ejbContext;
        BaseContainer container = (BaseContainer) ejbContext.getContainer(); 
        containerId_  = container.getEjbDescriptor().getUniqueId();
        entity_       = false;
        timedObjectPrimaryKey_   = null;
    }

    public EJBTimerServiceWrapper(EJBTimerService timerService,
                                  EntityContextImpl entityContext) 
    {
        this(timerService, ((EJBContextImpl)entityContext));
        entity_       = true;
        // Delay access of primary key since this might have been called 
        // from ejbCreate
        timedObjectPrimaryKey_   = null;
    }

    public Timer createTimer(long duration, Serializable info) 
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();

        if( duration < 0 ) {
            throw new IllegalArgumentException("invalid duration=" + duration);
        } 
                             
        TimerPrimaryKey timerId = null;

        try {
            timerId = timerService_.createTimer
                (containerId_, getTimedObjectPrimaryKey(), duration, 0, info);
        } catch(CreateException ce) {            
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ce);
            throw ejbEx;            
        }

        return new TimerWrapper(timerId, timerService_);
    }

    public Timer createTimer(long initialDuration, long intervalDuration, 
                             Serializable info) 
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();

        if( initialDuration < 0 ) {
            throw new IllegalArgumentException("invalid initial duration = " +
                                               initialDuration);
        } else if( intervalDuration < 0 ) {
            throw new IllegalArgumentException("invalid interval duration = " +
                                               intervalDuration);
        }
                             
        TimerPrimaryKey timerId = null;

        try {
            timerId = timerService_.createTimer
                (containerId_, getTimedObjectPrimaryKey(), initialDuration, 
                 intervalDuration, info);
        } catch(CreateException ce) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ce);
            throw ejbEx;                       
        }

        return new TimerWrapper(timerId, timerService_);
    }

    public Timer createTimer(Date expiration, Serializable info) 
        throws IllegalArgumentException, IllegalStateException, EJBException {
                             
        checkCreateTimerCallPermission();

        if( expiration == null ) {
            throw new IllegalArgumentException("null expiration");
        } 

        TimerPrimaryKey timerId = null;

        try {
            timerId = timerService_.createTimer(containerId_, 
                                                getTimedObjectPrimaryKey(),
                                                expiration, 0, info);
        } catch(CreateException ce) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ce);
            throw ejbEx;           
        }

        return new TimerWrapper(timerId, timerService_);
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration,
                             Serializable info) 
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();

        if( initialExpiration == null ) {
            throw new IllegalArgumentException("null expiration");
        } else if ( intervalDuration < 0 ) {
            throw new IllegalArgumentException("invalid interval duration = " +
                                               intervalDuration);
        }

        TimerPrimaryKey timerId = null;
        try {
            timerId = timerService_.createTimer(containerId_, 
                getTimedObjectPrimaryKey(), initialExpiration, 
                intervalDuration, info);
        } catch(CreateException e) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;                       
        }

        return new TimerWrapper(timerId, timerService_);
    }

    public Collection getTimers() throws IllegalStateException, EJBException {
        
        checkCallPermission();
        
        Collection timerIds = new HashSet();

        if( ejbContext_.isTimedObject() ) {        
            try {
                timerIds = timerService_.getTimerIds
                    (containerId_,  getTimedObjectPrimaryKey());
            } catch(FinderException fe) {
                EJBException ejbEx = new EJBException();
                ejbEx.initCause(fe);
                throw ejbEx;                         
            }
        } 
                                                        
        Collection timerWrappers = new HashSet();

        for(Iterator iter = timerIds.iterator(); iter.hasNext();) {
            TimerPrimaryKey next = (TimerPrimaryKey) iter.next();
            timerWrappers.add( new TimerWrapper(next, timerService_) );
        }

        return timerWrappers;
    }

    private Object getTimedObjectPrimaryKey() {
        if( !entity_ ) {
            return null;
        } else {
            synchronized(this) {
                if( timedObjectPrimaryKey_ == null ) {
                    timedObjectPrimaryKey_ = 
                        ((EntityContextImpl) ejbContext_).getPrimaryKey();
                }
            }
        }
        return timedObjectPrimaryKey_;
    }

    private void checkCreateTimerCallPermission() 
        throws IllegalStateException {
        if( ejbContext_.isTimedObject() ) {
            checkCallPermission();
        } else {
            throw new IllegalStateException("EJBTimerService.createTimer can "
                + "only be called from a timed object.  This EJB does not " 
                + "implement javax.ejb.TimedObject");                 
        }
    }

    private void checkCallPermission() 
        throws IllegalStateException {
        ejbContext_.checkTimerServiceMethodAccess();
    }

}
