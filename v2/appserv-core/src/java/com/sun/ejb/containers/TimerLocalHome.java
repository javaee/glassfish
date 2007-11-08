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

import java.io.Serializable;
import java.util.Date;
import java.util.Collection;
import java.util.Set;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

/**
 *
 * @author Kenneth Saks
 */
public interface TimerLocalHome extends EJBLocalHome {

    TimerLocal create(String timerId,  
                      long containerId, String ownerId,
                      Object timedObjectPrimaryKey, 
                      Date initialExpiration, long intervalDuration, 
                      Serializable info) throws CreateException;

    TimerLocal findByPrimaryKey(TimerPrimaryKey timerId) throws FinderException;

    // 
    // Queries returning Timer Ids (TimerPrimaryKey)
    //

    Set selectTimerIdsByContainer(long containerId)
        throws FinderException;
    Set selectActiveTimerIdsByContainer(long containerId)
        throws FinderException;
    Set selectCancelledTimerIdsByContainer(long containerId)
        throws FinderException;    

    Set selectTimerIdsOwnedByThisServerByContainer(long containerId) 
        throws FinderException;
    Set selectActiveTimerIdsOwnedByThisServerByContainer(long containerId) 
        throws FinderException;
    Set selectCancelledTimerIdsOwnedByThisServerByContainer(long containerId)
        throws FinderException;

    Set selectAllTimerIdsOwnedByThisServer() throws FinderException; 
    Set selectAllActiveTimerIdsOwnedByThisServer() throws FinderException; 
    Set selectAllCancelledTimerIdsOwnedByThisServer() throws FinderException;

    Set selectAllTimerIdsOwnedBy(String owner) throws FinderException;
    Set selectAllActiveTimerIdsOwnedBy(String owner) throws FinderException;
    Set selectAllCancelledTimerIdsOwnedBy(String owner) throws FinderException;


    //
    // Queries returning Timer local objects
    //

    Set selectTimersByContainer(long containerId)
        throws FinderException;
    Set selectActiveTimersByContainer(long containerId)
        throws FinderException;
    Set selectCancelledTimersByContainer(long containerId)
        throws FinderException;    

    Set selectTimersOwnedByThisServerByContainer(long containerId) 
        throws FinderException;
    Set selectActiveTimersOwnedByThisServerByContainer(long containerId) 
        throws FinderException;
    Set selectCancelledTimersOwnedByThisServerByContainer(long containerId)
        throws FinderException;

    Set selectAllTimersOwnedByThisServer() throws FinderException; 
    Set selectAllActiveTimersOwnedByThisServer() throws FinderException; 
    Set selectAllCancelledTimersOwnedByThisServer() throws FinderException;

    Set selectAllTimersOwnedBy(String owner) throws FinderException;
    Set selectAllActiveTimersOwnedBy(String owner) throws FinderException;
    Set selectAllCancelledTimersOwnedBy(String owner) throws FinderException;


    //
    // Queries returning counts
    //

    int selectCountTimersByContainer(long containerId)
        throws FinderException;
    int selectCountActiveTimersByContainer(long containerId)
        throws FinderException;
    int selectCountCancelledTimersByContainer(long containerId)
        throws FinderException;    

    int selectCountTimersOwnedByThisServerByContainer(long containerId) 
        throws FinderException;
    int selectCountActiveTimersOwnedByThisServerByContainer(long containerId) 
        throws FinderException;
    int selectCountCancelledTimersOwnedByThisServerByContainer(long containerId)
        throws FinderException;

    int selectCountAllTimersOwnedByThisServer() 
        throws FinderException; 
    int selectCountAllActiveTimersOwnedByThisServer() 
        throws FinderException; 
    int selectCountAllCancelledTimersOwnedByThisServer() 
        throws FinderException;

    int selectCountAllTimersOwnedBy(String owner) 
        throws FinderException;
    int selectCountAllActiveTimersOwnedBy(String owner) 
        throws FinderException;
    int selectCountAllCancelledTimersOwnedBy(String owner) 
        throws FinderException;



    // Perform health check on timer database
    boolean checkStatus(String resourceJndiName, boolean checkDatabase);

}
