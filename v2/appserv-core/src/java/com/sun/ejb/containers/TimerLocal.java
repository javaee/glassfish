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

import javax.ejb.EJBLocalObject;

/**
 * Local view of the persistent representation of an EJB timer.
 *
 * @author Kenneth Saks
 */
public interface TimerLocal extends EJBLocalObject {

    /**
     * Accessors for read-only state.
     */ 

    /**
     * When timer was created.  Informational only.
     */
    Date getCreationTime();

    /**
     * Time of first timer expiration.
     */
    Date getInitialExpiration();


    /**
     * For periodic timers, number of milli-seconds between
     * timeouts.  0 for single-action timers.
     */
    long getIntervalDuration();

    /**
     * ejb container corresponding to timed object that created timer.
     */
    long getContainerId();

    /**
     * Id of server instance that owns timer.
     */
    String getOwnerId();

    /**
     * Application info associated with timer.  Can be null.
     * 
     */
    Serializable getInfo();    

    /**
     * Holds primary key for entity timed object. Null otherwise.
     */ 
    Object getTimedObjectPrimaryKey();

    /**
     * Mutable fields.
     */ 

    /**
     * This is the last time that a timer expiration completed
     * successfully or null if this field has not been set.
     */
    Date getLastExpiration();

    /**
     * This is the last time that a timer expiration completed
     * successfully or null to clear this value.
     */
    void setLastExpiration(Date lastExpiration);

    /**
     * Changes timer owner.  Typically done during timer migration.
     */ 
    void setOwnerId(String ownerId);

    /**
     * Operations.
     */ 

    /**
     * True if interval timer.  False if single-action timer.
     */
    boolean repeats();

    /**
     * Check if timer is in an active state.
     */
    boolean isActive();

    /**
     * Check if timer is in a cancelled state.
     */
    boolean isCancelled();
    
    /**
     * Cancel timer.
     */
    void cancel() throws Exception;

}
