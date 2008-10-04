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
    public Collection getTimers() throws java.lang.IllegalStateException,
        javax.ejb.EJBException;

} 
