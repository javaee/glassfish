
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
package javax.ejb;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * The Timer interface contains information about a timer
 * that was created through the EJB Timer Service.
 *
 */
public interface Timer {

    /**
     * Cause the timer and all its associated expiration notifications to
     * be cancelled.
     *
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public void cancel() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;
    
    /**
     * Get the number of milliseconds that will elapse before the next
     * scheduled timer expiration. 
     *
     * @return the number of milliseconds that will elapse before the next
     * scheduled timer expiration.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public long getTimeRemaining() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;

    /**
     * Get the point in time at which the next timer expiration is scheduled 
     * to occur.
     *
     * @return the point in time at which the next timer expiration is 
     * scheduled to occur.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public Date getNextTimeout() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;

    /**
     * Get the information associated with the timer at the time of creation.
     *
     * @return The Serializable object that was passed in at timer creation, or
     * null if the info argument passed in at timer creation was null.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public Serializable getInfo() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;


    /**
     * Get a serializable handle to the timer.  This handle can
     * be used at a later time to re-obtain the timer reference.
     *
     * @return a serializable handle to the timer.
     * 
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public TimerHandle getHandle() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;

    /**
     * Get the schedule expression corresponding to this timer.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access
     * to this method.  Also thrown if invoked on a timer that was created
     * with one of the non-ScheduleExpression TimerService.createTimer APIs.
     *
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     *
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public ScheduleExpression getSchedule() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;

    /**
     * Query whether this timer has persistent semantics.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access
     * to this method.
     *
     * @return true if this timer has persistent guarantees.
     * @return false if this is a non-persistent timer.
     *
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     *
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public boolean isPersistent() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;

} 
