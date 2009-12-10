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
import java.util.Collection;

/**
 * The TimerService interface provides enterprise bean components 
 * with access to the container-provided Timer Service.  The EJB
 * Timer Service allows entity beans, stateless session beans,
 * and message-driven beans to be registered for timer callback 
 * events at a specified time, after a specified elapsed time, or 
 * after a specified interval.
 *
 */
public interface TimerService {

    /**
     * Create a single-action timer that expires after a specified duration.
     *
     * @param duration The number of milliseconds that must elapse before
     * the timer expires.
     *
     * @param info Application information to be delivered along
     * with the timer expiration notification. This can be null.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If duration is negative
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method fails due to a 
     * system-level failure.
     * 
     */
    public Timer createTimer(long duration, Serializable info) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException,
        javax.ejb.EJBException;

    /**
     * Create a single-action timer that expires after a specified duration.
     *
     * @param duration The number of milliseconds that must elapse before
     * the timer expires.
     *
     * @param timerConfig Timer configuration.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If duration is negative
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method fails due to a 
     * system-level failure.
     * 
     */
    public Timer createSingleActionTimer(long duration, TimerConfig timerConfig) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException,
        javax.ejb.EJBException;

    /**
     * Create an interval timer whose first expiration occurs after a specified
     * duration, and whose subsequent expirations occur after a specified
     * interval.
     *
     * @param initialDuration The number of milliseconds that must elapse 
     * before the first timer expiration notification.
     *
     * @param intervalDuration The number of milliseconds that must elapse
     * between timer expiration notifications.  Expiration notifications are
     * scheduled relative to the time of the first expiration.  If expiration
     * is delayed(e.g. due to the interleaving of other method calls on the
     * bean) two or more expiration notifications may occur in close 
     * succession to "catch up".
     * 
     * @param info Application information to be delivered along
     * with the timer expiration. This can be null.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If initialDuration is
     * negative, or intervalDuration is negative.
     * 
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createTimer(long initialDuration, long intervalDuration, 
                             Serializable info) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException, 
        javax.ejb.EJBException;

    /**
     * Create an interval timer whose first expiration occurs after a specified
     * duration, and whose subsequent expirations occur after a specified
     * interval.
     *
     * @param initialDuration The number of milliseconds that must elapse 
     * before the first timer expiration notification.
     *
     * @param intervalDuration The number of milliseconds that must elapse
     * between timer expiration notifications.  Expiration notifications are
     * scheduled relative to the time of the first expiration.  If expiration
     * is delayed(e.g. due to the interleaving of other method calls on the
     * bean) two or more expiration notifications may occur in close 
     * succession to "catch up".
     * 
     * @param timerConfig Timer configuration
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If initialDuration is
     * negative, or intervalDuration is negative.
     * 
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createIntervalTimer(long initialDuration, long intervalDuration, 
                             TimerConfig timerConfig) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException, 
        javax.ejb.EJBException;
        

    /**
     * Create a single-action timer that expires at a given point in time.
     *
     * @param expiration The point in time at which the timer must expire.
     *
     * @param info Application information to be delivered along
     * with the timer expiration notification. This can be null.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If expiration is null, or
     * expiration.getTime() is negative.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createTimer(Date expiration, Serializable info) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException, 
        javax.ejb.EJBException;

    /**
     * Create a single-action timer that expires at a given point in time.
     *
     * @param expiration The point in time at which the timer must expire.
     *
     * @param timerConfig Timer configuration.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If expiration is null, or
     * expiration.getTime() is negative.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createSingleActionTimer(Date expiration, TimerConfig timerConfig) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException, 
        javax.ejb.EJBException;
        
        

    /**
     * Create an interval timer whose first expiration occurs at a given
     * point in time and whose subsequent expirations occur after a specified
     * interval.
     *
     * @param initialExpiration The point in time at which the first timer
     * expiration must occur.
     *
     * @param intervalDuration The number of milliseconds that must elapse
     * between timer expiration notifications.  Expiration notifications are
     * scheduled relative to the time of the first expiration.  If expiration
     * is delayed(e.g. due to the interleaving of other method calls on the
     * bean) two or more expiration notifications may occur in close 
     * succession to "catch up".
     * 
     * @param info Application information to be delivered along
     * with the timer expiration. This can be null.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If initialExpiration is
     * null, or initialExpiration.getTime() is negative, or intervalDuration 
     * is negative.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createTimer(Date initialExpiration, long intervalDuration, 
                             Serializable info) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException,
        javax.ejb.EJBException;

/**
     * Create an interval timer whose first expiration occurs at a given
     * point in time and whose subsequent expirations occur after a specified
     * interval.
     *
     * @param initialExpiration The point in time at which the first timer
     * expiration must occur.
     *
     * @param intervalDuration The number of milliseconds that must elapse
     * between timer expiration notifications.  Expiration notifications are
     * scheduled relative to the time of the first expiration.  If expiration
     * is delayed(e.g. due to the interleaving of other method calls on the
     * bean) two or more expiration notifications may occur in close 
     * succession to "catch up".
     * 
     * @param timerConfig Timer configuration.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If initialExpiration is
     * null, or initialExpiration.getTime() is negative, or intervalDuration 
     * is negative.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createIntervalTimer(Date initialExpiration, long intervalDuration, 
                             TimerConfig timerConfig) throws
        java.lang.IllegalArgumentException, java.lang.IllegalStateException,
        javax.ejb.EJBException;

    /**
     * Create a calendar-based timer based on the input schedule expression.
     *
     * @param schedule A schedule expression describing the timeouts for this timer.
     *
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If Schedule represents an
     * invalid schedule expression.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createCalendarTimer(ScheduleExpression schedule) 
        throws java.lang.IllegalArgumentException, 
               java.lang.IllegalStateException, javax.ejb.EJBException;

    /**
     * Create a calendar-based timer based on the input schedule expression.
     *
     * @param schedule A schedule expression describing the timeouts for this timer.
     *
     * @param timerConfig Timer configuration.
     *                    
     * @return The newly created Timer.
     *
     * @exception java.lang.IllegalArgumentException If Schedule represents an
     * invalid schedule expression.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Timer createCalendarTimer(ScheduleExpression schedule, TimerConfig timerConfig) 
        throws java.lang.IllegalArgumentException, 
               java.lang.IllegalStateException, javax.ejb.EJBException;

    /**
     * Get all the active timers associated with this bean.
     *
     * @return A collection of javax.ejb.Timer objects.
     *
     * @exception java.lang.IllegalStateException If this method is
     * invoked while the instance is in a state that does not allow access 
     * to this method.
     * 
     * @exception javax.ejb.EJBException If this method could not complete
     * due to a system-level failure.
     * 
     */
    public Collection<Timer> getTimers() throws java.lang.IllegalStateException,
        javax.ejb.EJBException;



} 
