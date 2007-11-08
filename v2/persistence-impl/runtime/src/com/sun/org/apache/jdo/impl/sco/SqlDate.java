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

/*
 * SqlDate.java
 *
 */

package com.sun.org.apache.jdo.impl.sco;


import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCODate;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.persistence.support.JDOHelper;



/**
 * A mutable 2nd class object that represents java.sql.Date.
 * @author Marina Vatkina 
 * @version 1.0 
 * @see     java.sql.Date
 */
public class SqlDate extends java.sql.Date
    implements SCODate {

    private transient StateManagerInternal owner;

    private transient int fieldNumber;

    private final static String _SqlDate = "SqlDate"; // NOI18N

    /**
     * Creates a <code>SqlDate</code> object that represents the time at which
     * it was allocated.
     */
    public SqlDate() {
        super(0);
    }

    /**
     * Creates a <code>SqlDate</code> object that represents the given time
     * in milliseconds.
     * @param date      the number of milliseconds
     */  
    public SqlDate(long date) {
        super(date);
    }

    /**
     * Sets the <tt>SqlDate</tt> object to represent a point in time that is
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT.
     *   
     * @param   time   the number of milliseconds.
     * @see     java.sql.Date
     */  
    public void setTime(long time) {
        SCOHelper.debug(_SqlDate, "setTime"); // NOI18N

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
        SCOHelper.debug(_SqlDate, "clone"); // NOI18N

        Object obj = super.clone();
        if (obj instanceof SCO) 
            ((SCO)obj).unsetOwner(owner, fieldNumber);

        return obj;
    }

    /** -----------Depricated Methods------------------*/

    /**
     * Sets the year of this <tt>SqlDate</tt> object to be the specified
     * value plus 1900. 
     *   
     * @param   year    the year value.
     * @see     java.util.Calendar
     * @see     java.sql.Date
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.YEAR, year + 1900)</code>.
     */  
    public void setYear(int year) {
        SCOHelper.debug(_SqlDate, "setYear"); // NOI18N

        this.makeDirty(); 
        super.setYear(year);
    }  

    /**
     * Sets the month of this date to the specified value.      
     * @param   month   the month value between 0-11.
     * @see     java.util.Calendar
     * @see     java.sql.Date
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.MONTH, int month)</code>.
     */
    public void setMonth(int month) {
        SCOHelper.debug(_SqlDate, "setMonth"); // NOI18N

        this.makeDirty(); 
        super.setMonth(month);
    }    

    /**
     * Sets the day of the month of this <tt>SqlDate</tt> object to the
     * specified value. 
     *   
     * @param   date   the day of the month value between 1-31.
     * @see     java.util.Calendar
     * @see     java.sql.Date
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.DAY_OF_MONTH, int date)</code>.
     */  
    public void setDate(int date) {
        SCOHelper.debug(_SqlDate, "setDate"); // NOI18N

        this.makeDirty(); 
        super.setDate(date);
    } 

    /** ---------------- internal methods ------------------- */

    /**
     * Sets the <tt>SqlDate</tt> object without notification of the Owner
     * field. Used internaly to populate date from DB
     *   
     * @param   time   the number of milliseconds.
     * @see     java.sql.Date
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
