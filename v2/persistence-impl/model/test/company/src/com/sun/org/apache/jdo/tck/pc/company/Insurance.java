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
 
package com.sun.org.apache.jdo.tck.pc.company;

import java.io.Serializable;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents an insurance carrier selection for a particular
 * <code>Employee</code>.
 */
public abstract class Insurance 
    implements Serializable, Comparable, DeepEquality  {

    private long     insid;
    private String   carrier;
    private Employee employee;

    /** This is the JDO-required no-args constructor. */
    protected Insurance() {}

    /**
     * Initialize an <code>Insurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     */
    protected Insurance(long insid, String carrier) {
        this.insid = insid;
        this.carrier = carrier;
    }

    /**
     * Initialize an <code>Insurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param employee The employee associated with this insurance. 
     */
    protected Insurance(long insid, String carrier, Employee employee) {
        this.insid = insid;
        this.carrier = carrier;
        this.employee = employee;
    }

    /**
     * Get the insurance ID.
     * @return the insurance ID.
     */
    public long getInsid() {
        return insid;
    }

    /**
     * Set the insurance ID.
     * @param insid The insurance ID value.
     */
    public void setInsid(long insid) {
        this.insid = insid;
    }

    /**
     * Get the insurance carrier.
     * @return The insurance carrier.
     */
    public String getCarrier() {
        return carrier;
    }

    /**
     * Set the insurance carrier.
     * @param carrier The insurance carrier.
     */
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
    
    /**
     * Get the associated employee.
     * @return The employee for this insurance.
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Set the associated employee.
     * @param employee The associated employee.
     */
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the coresponding fields of the specified Person.
     * @param other the object with which to compare.
     * @param helper EqualityHelper to keep track of instances that have
     * already been processed. 
     * @return <code>true</code> if all the fields are deep equal;
     * <code>false</code> otherwise.  
     * @throws ClassCastException if the specified instances' type prevents
     * it from being compared to this instance. 
     */
    public boolean deepCompareFields(DeepEquality other, 
                                     EqualityHelper helper) {
        Insurance otherInd = (Insurance)other;
        return (insid == otherInd.insid) && 
            helper.equals(carrier, otherInd.carrier) && 
            helper.deepEquals(employee, otherInd.employee);
    }
    
    /** 
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. 
     * @param o The Object to be compared. 
     * @return a negative integer, zero, or a positive integer as this 
     * object is less than, equal to, or greater than the specified object. 
     * @throws ClassCastException - if the specified object's type prevents
     * it from being compared to this Object. 
     */
    public int compareTo(Object o) {
        return compareTo((Insurance)o);
    }

    /** 
     * Compares this object with the specified Insurance object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Insurance object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified
     * Insurance object. 
     */
    public int compareTo(Insurance other) {
        long otherId = other.insid;
        return (insid < otherId ? -1 : (insid == otherId ? 0 : 1));
    }
    
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Insurance) {
            return compareTo((Insurance)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)insid;
    }

    /**
     * This class is used to represent the application
     * identifier for the <code>Insurance</code> class.
     */
    public static class Oid implements Serializable, Comparable 
    {
        /**
         * This field represents the application identifier for the
         * <code>Insurance</code> class. It must match the field in the
         * <code>Insurance</code> class in both name and type. 
         */
        public long insid;
        
        /** The name of the class of the target object.
        */
        public static final String targetClassName = "org.apache.jdo.tck.pc.company.Insurance"; // NOI18N

        
        /**
         * The required public no-args constructor.
         */
        public Oid() { }

        /**
         * Initialize with an insurance identifier.
         * @param insid the insurance ID.
         */
        public Oid(long insid) {
            this.insid = insid;
        }
        
        public Oid(String s) { insid = Long.parseLong(justTheId(s)); }

        public String toString() { return getTargetClassName() + ": "  + insid;} // NOI18N


        /** */
        public boolean equals(java.lang.Object obj) {
            if( obj==null || !this.getClass().equals(obj.getClass()) )
                return( false );
            Oid o=(Oid) obj;
            if( this.insid!=o.insid ) return( false );
            return( true );
        }

        /** */
        public int hashCode() {
            return( (int) insid );
        }
        
        protected static String justTheId(String str) {
            return str.substring(str.indexOf(':') + 1);
        }
        
        /** Return the target class name.
         * @return the target class name.
         */
        public String getTargetClassName() {
            return targetClassName;
        } 

        /** */
        public int compareTo(Object obj) {
            // may throw ClassCastException which the user must handle
            Oid other = (Oid) obj;
            if( insid < other.insid ) return -1;
            if( insid > other.insid ) return 1;
            return 0;
        }

    }

}

