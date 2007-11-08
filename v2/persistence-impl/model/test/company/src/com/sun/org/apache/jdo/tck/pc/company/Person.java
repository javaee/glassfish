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

import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a person.
 */
public class Person 
    implements Serializable, Comparable, DeepEquality  {

    private long    personid;
    private String  firstname;
    private String  lastname;
    private String  middlename;
    private Date    birthdate;
    private Address address;

    // maps phone number types ("home", "work", "mobile", etc.) 
    // to phone numbers specified as String
    private Map phoneNumbers = new HashMap();
    
    protected static SimpleDateFormat formatter =
        new SimpleDateFormat("d/MMM/yyyy"); // NOI18N

    /** This is the JDO-required no-args constructor. */
    protected Person() {}

    /**
     * Initialize a <code>Person</code> instance.
     * @param personid The person identifier.
     * @param firstname The person's first name.
     * @param lastname The person's last name.
     * @param middlename The person's middle name.
     * @param birthdate The person's birthdate.
     * @param address The person's address.
     */
    public Person(long personid, String firstname, String lastname, 
                  String middlename, Date birthdate, Address address) {
        this.personid = personid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.middlename = middlename;
        this.address = address;
        this.birthdate = birthdate;
    }

    /**
     * Get the person's id.
     * @return The personid.
     */
    public long getPersonid() {
        return personid;
    }

    /**
     * Set the person's id.
     * @param personid The personid.
     */
    public void setLastname(long personid) {
        this.personid = personid;
    }

    /**
     * Get the person's last name.
     * @return The last name.
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Set the person's last name.
     * @param lastname The last name.
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * Get the person's first name.
     * @return The first name.
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Set the person's first name.
     * @param firstname The first name.
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * Get the person's middle name.
     * @return The middle name.
     */
    public String getMiddlename() {
        return middlename;
    }

    /**
     * Set the person's middle name.
     * @param middlename The middle name.
     */
    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    /**
     * Get the address.
     * @return The address.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Set the address.
     * @param address The address.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Get the person's birthdate.
     * @return The person's birthdate.
     */
    public Date getBirthdate() {
        return birthdate;
    }

    /**
     * Set the person's birthdate.
     * @param birthdate The person's birthdate.
     */
    public void setBirthdate(Date birthdate) {
        this. birthdate = birthdate;
    }

    /**
     * Get the map of phone numbers as an unmodifiable map.
     * @return The map of phone numbers, as an unmodifiable map.
     */
    public Map getPhoneNumbers() {
        return Collections.unmodifiableMap(phoneNumbers);
    }

    /**
     * Get the phone number for the specified phone number type. 
     * @param type The phone number type ("home", "work", "mobile", etc.).
     * @return The phone number associated with specified type, or
     * <code>null</code> if there was no phone number for the type. 
     */
    public String getPhoneNumber(String type) {
        return (String)phoneNumbers.get(type);
    }
    
    /**
     * Associates the specified phone number with the specified type in the
     * map of phone numbers of this person. 
     * @param type The phone number type ("home", "work", "mobile", etc.).
     * @param phoneNumber The phone number 
     * @return The previous phone number associated with specified type, or
     * <code>null</code> if there was no phone number for the type. 
     */
    public String putPhoneNumber(String type, String phoneNumber) {
        return (String)phoneNumbers.put(type, phoneNumber);
    }

    /**
     * Remove a phoneNumber from the map of phone numbers.
     * @param type The phone number type ("home", "work", "mobile", etc.).
     * @return The previous phone number associated with specified type, or
     * <code>null</code> if there was no phone number for the type. 
     */
    public String removePhoneNumber(String type) {
        return (String)phoneNumbers.remove(type);
    }

    /**
     * Set the phoneNumber map to be in this person.
     * @param phoneNumbers The map of phoneNumbers for this person.
     */
    public void setPhoneNumbers(Map phoneNumbers) {
        // workaround: create a new HashMap, because fostore does not
        // support LinkedHashMap
        this.phoneNumbers = 
            (phoneNumbers != null) ? new HashMap(phoneNumbers) : null;
    }

    /**
     * Return a String representation of a <code>Person</code> object.
     */
    public String toString() {
        StringBuffer rc = new StringBuffer("Person: "); // NOI18N
        rc.append(personid);
        rc.append(", "); // NOI18N
        rc.append(lastname);
        rc.append(", " + firstname); // NOI18N
        rc.append(", born " + formatter.format(birthdate)); // NOI18N
        rc.append(", phone " + phoneNumbers); // NOI18N
        return rc.toString();
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
        Person otherPerson = (Person)other;
        return (personid == otherPerson.personid) &&
            helper.equals(firstname, otherPerson.firstname) &&
            helper.equals(lastname, otherPerson.lastname) &&
            helper.equals(middlename, otherPerson.middlename) &&
            helper.equals(birthdate, otherPerson.birthdate) &&
            helper.deepEquals(address, otherPerson.address) &&
            helper.deepEquals(phoneNumbers, otherPerson.phoneNumbers);
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
        return compareTo((Person)o);
    }

    /** 
     * Compares this object with the specified Person object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Person object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified Person 
     * object. 
     */
    public int compareTo(Person other) {
        long otherId = other.personid;
        return (personid < otherId ? -1 : (personid == otherId ? 0 : 1));
    }
    
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            return compareTo((Person)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)personid;
    }
    /**
     * This class is used to represent the application identifier
     * for the <code>Person</code> class.
     */
    public static class Oid implements Serializable, Comparable {

        /**
         * This field represents the identifier for the <code>Person</code>
         * class. It must match a field in the <code>Person</code> class in
         * both name and type. 
         */
        public long personid;

        /**
         * The required public no-arg constructor.
         */
        public Oid() { }

        /**
         * Initialize the identifier.
         * @param personid The person identifier.
         */
        public Oid(long personid) {
            this.personid = personid;
        }
        
        public Oid(String s) { personid = Long.parseLong(justTheId(s)); }
        
        /** The name of the class of the target object.
        */
        public static final String targetClassName = "org.apache.jdo.tck.pc.company.Person"; // NOI18N

        public String toString() { return getTargetClassName() + ": "  + personid;} // NOI18N

        /** */
        public boolean equals(java.lang.Object obj) {
            if( obj==null ||
                !this.getClass().equals(obj.getClass()) ) return( false );
            Oid o = (Oid) obj;
            if( this.personid != o.personid ) return( false );
            return( true );
        }

        /** */
        public int hashCode() {
            return( (int) personid );
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
            if( personid < other.personid ) return -1;
            if( personid > other.personid ) return 1;
            return 0;
        }

    }

}
