/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/**
 * SqlTime.java
 *
 */

package com.sun.org.apache.jdo.impl.sco;


import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCODate;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.persistence.support.JDOHelper;



/**
 * A mutable 2nd class object that represents java.sql.Time.
 * @author Marina Vatkina 
 * @version 1.0 
 * @see     java.sql.Time
 */
public class SqlTime extends java.sql.Time
    implements SCODate {

    private transient StateManagerInternal owner;

    private transient int fieldNumber = -1;

    private final static String _SqlTime = "SqlTime"; // NOI18N

    /**
     * Creates a <code>SqlTime</code> object that represents the time at which
     * it was allocated.
     */
    public SqlTime() {
        super(0);
    }

    /**
     * Creates a <code>SqlTime</code> object that represents the given time
     * in milliseconds.
     * @param date      the number of milliseconds
     */  
    public SqlTime(long date) {
        super(date);
    }

    /**
     * Sets the <tt>SqlTime</tt> object to represent a point in time that is
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT.
     *   
     * @param   time   the number of milliseconds.
     * @see     java.sql.Time
     */  
    public void setTime(long time) {
        SCOHelper.debug(_SqlTime, "setTime"); // NOI18N

        this.makeDirty();
        super.setTime(time);
    }

    /**
     * Creates and returns a copy of this object.
     *
     * <P>Mutable Second Class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     */
    public Object clone() {
        SCOHelper.debug(_SqlTime, "clone"); // NOI18N

        Object obj = super.clone();
        if (obj instanceof SCO) {
            ((SCO)obj).unsetOwner(owner, fieldNumber);
        }

        return obj;
    }

    /** -----------Depricated Methods------------------*/

    /**
     * Sets the hour of this <tt>SqlTime</tt> object to the specified value.
     *   
     * @param   hours   the hour value.
     * @see     java.util.Calendar
     * @see     java.sql.Time
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.HOUR_OF_DAY, int hours)</code>.
     */  
    public void setHours(int hours) {
        SCOHelper.debug(_SqlTime, "setHours"); // NOI18N

        this.makeDirty();
        super.setHours(hours);
    }  

    /**
     * Sets the minutes of this <tt>SqlTime</tt> object to the specified value.
     *   
     * @param   minutes   the value of the minutes.
     * @see     java.util.Calendar
     * @see     java.sql.Time
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.MINUTE, int minutes)</code>.
     */
    public void setMinutes(int minutes) {
        SCOHelper.debug(_SqlTime, "setMinutes"); // NOI18N

        this.makeDirty();
        super.setMinutes(minutes);
    }   
 
    /**
     * Sets the seconds of this <tt>SqlTime</tt> to the specified value.
     *   
     * @param   seconds   the seconds value.
     * @see     java.util.Calendar
     * @see     java.sql.Time
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.SECOND, int seconds)</code>.
     */  
    public void setSeconds(int seconds) {
        SCOHelper.debug(_SqlTime, "setMinutes"); // NOI18N

        this.makeDirty();
        super.setSeconds(seconds);
    } 

    /** ---------------- internal methods ------------------- */

    /**
     * Sets the <tt>SqlTime</tt> object without notification of the Owner
     * field. Used internaly to populate date from DB
     *   
     * @param   time   the number of milliseconds.
     * @see     java.sql.Time
     */  
    public void setTimeInternal(long time) {
        super.setTime(time);
    }

    /**
     * @see SCO#unsetOwner(Object owner, int fieldNumber)
     */
    public void unsetOwner(Object owner, int fieldNumber) { 
        // Unset only if owner and fieldNumber match.
        if (this.owner == owner && this.fieldNumber == fieldNumber) {
            this.owner = null; 
            this.fieldNumber = -1;
        }
    }

    /**
     * @see SCO#setOwner (Object owner, int fieldNumber)
     */
    public void setOwner (Object owner, int fieldNumber) {
        // Set only if it was not set before.
        if (this.owner == null && owner instanceof StateManagerInternal) {
            this.owner = (StateManagerInternal)owner;    
            this.fieldNumber = fieldNumber;
        }
    }

    /** 
     * @see SCO#getOwner()
     */   
    public Object getOwner() {   
        return SCOHelper.getOwner(owner);
    } 
 
    /**  
     * @see SCO#getFieldName()
     */   
    public String getFieldName() {
        return SCOHelper.getFieldName(owner, fieldNumber);
    }

    /**
     * Marks object dirty
     */
    private void makeDirty() {
        if (owner != null) {
            owner.makeDirty(fieldNumber); //
        }
    }   
}
