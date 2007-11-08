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
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents information about a company.
 */
public class Company 
    implements Serializable, Comparable, DeepEquality {

    private long        companyid;
    private String      name;
    private Date        founded;
    private Address     address;
    private transient Set departments = new HashSet(); // element type is Department

    /** This is the JDO-required no-args constructor */
    protected Company() {}

    /** 
     * Initialize the <code>Company</code> instance.
     * @param companyid The company id.
     * @param name The company name.
     * @param founded The date the company was founded.
     * @param addr The company's address.
     */
    public Company(long companyid, String name, Date founded, Address addr) {
        this.companyid = companyid;
        this.name = name;
        this.founded = founded;
        this.address = addr;
    }

    /**
     * Get the company id.
     * @return The company id.
     */
    public long getCompanyid() {
        return companyid;
    }

    /**
     * Get the name of the company.
     * @return The name of the company.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the  name of the company.
     * @param name The value to use for the name of the company.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the date that the company was founded.
     * @return The date the company was founded.
     */
    public Date getFounded() {
        return founded;
    }

    /**
     * Set the date that the company was founded.
     * @param founded The date to set that the company was founded.
     */
    public void setFounded(Date founded) {
        this.founded = founded;
    }

    /**
     * Get the address of the company.
     * @return The primary address of the company.
     */
    public Address getAddress() {
        return address;
    }
    
    /**
     * Set the primary address for the company.
     * @param address The address to set for the company.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Get the departments contained in the company.
     * @return An unmodifiable <code>Set</code> that contains all the
     * <code>Department</code>s of the company.
     */
    public Set getDepartments() {
        return Collections.unmodifiableSet(departments);
    }

    /**
     * Add a <code>Department</code> instance to the company.
     * @param dept The <code>Department</code> instance to add.
     */
    public void addDepartment(Department dept) {
        departments.add(dept);
    }

    /**
     * Remove a <code>Department</code> instance from the company.
     * @param dept The <code>Department</code> instance to remove.
     */
    public void removeDepartment(Department dept) {
        departments.remove(dept);
    }

    /**
     * Initialize the set of <code>Department</code>s in the company to the 
     * parameter. 
     * @param departments The set of <code>Department</code>s for the
     * company. 
     */
    public void setDepartments(Set departments) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.departments = 
            (departments != null) ? new HashSet(departments) : null;
    }
    
    /** Serialization support: initialize transient fields. */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        departments = new HashSet();
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
        Company otherCompany = (Company)other;
        return (companyid == otherCompany.companyid) &&
            helper.equals(name, otherCompany.name) &&
            helper.equals(founded, otherCompany.founded) &&
            helper.deepEquals(address, otherCompany.address) &&
            helper.deepEquals(departments, otherCompany.departments);
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
        return compareTo((Company)o);
    }

    /** 
     * Compares this object with the specified Company object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Company object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified Company
     * object. 
     */
    public int compareTo(Company other) {
        long otherId = other.companyid;
        return (companyid < otherId ? -1 : (companyid == otherId ? 0 : 1));
    }
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Company) {
            return compareTo((Company)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)companyid;
    }
    
    /**
     * The class to be used as the application identifier
     * for the <code>Company</code> class. It consists of both the company 
     * name and the date that the company was founded.
     */
    public static class Oid implements Serializable, Comparable {

        /**
         * This field is part of the identifier and should match in name
         * and type with a field in the <code>Company</code> class.
         */
        public long companyid;
        
        /** The name of the class of the target object.
        */
        public static final String targetClassName = "org.apache.jdo.tck.pc.company.Company"; // NOI18N

        /** The required public no-arg constructor. */
        public Oid() { }

        /**
         * Initialize the identifier.
         * @param companyid The id of the company.
         */
        public Oid(long companyid) {
            this.companyid = companyid;
        }
        
        public Oid(String s) { companyid = Long.parseLong(justTheId(s)); }

        public String toString() { return getTargetClassName() + ": "  + companyid;} // NOI18N

        
        /** */
        public boolean equals(Object obj) {
            if (obj==null || !this.getClass().equals(obj.getClass())) 
                return false;
            Oid o = (Oid) obj;
            if (this.companyid != o.companyid) 
                return false;
            return true;
        }

        /** */
        public int hashCode() {
            return (int)companyid;
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
            if( companyid < other.companyid ) return -1;
            if( companyid > other.companyid ) return 1;
            return 0;
        }
        
    }

}

