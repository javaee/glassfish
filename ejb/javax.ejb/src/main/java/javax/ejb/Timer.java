
/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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
     * @exception javax.ejb.NoMoreTimeoutsExceptions Indicates that the 
     * timer has no future timeouts
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public long getTimeRemaining() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.NoMoreTimeoutsException, javax.ejb.EJBException;

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
     * @exception javax.ejb.NoMoreTimeoutsExceptions Indicates that the 
     * timer has no future timeouts
     *
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public Date getNextTimeout() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.NoMoreTimeoutsException, javax.ejb.EJBException;

    /**
     * Get the schedule expression corresponding to this timer.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.  Also thrown if invoked on a timer that is not a
     * calendar-based timer.
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

    /**
     * Query whether this timer is a calendar-based timer.  
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method. 
     *
     * @return true if this timer is a calendar-based timer.  
     * @return false if this is not a calendar-based timer. 
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public boolean isCalendarTimer() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;    


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
     * to this method.  Also thrown if invoked on a non-persistent timer.
     * 
     * @exception javax.ejb.NoSuchObjectLocalException If invoked on a timer
     * that has expired or has been cancelled.
     * 
     * @exception javax.ejb.EJBException If this method could not complete due
     * to a system-level failure.
     */
    public TimerHandle getHandle() throws java.lang.IllegalStateException, javax.ejb.NoSuchObjectLocalException, javax.ejb.EJBException;



} 
