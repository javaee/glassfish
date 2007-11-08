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
 * This class represents a postal address.
 */
public class Address 
    implements Serializable, Comparable, DeepEquality {

    private long    addrid;
    private String  street;
    private String  city;
    private String  state;
    private String  zipcode;
    private String  country;

    /** This is the JDO-required no-args constructor */
    protected Address() {}

    /**
     * This constructor initializes the <code>Address</code> components.
     * @param addrid The address ID.
     * @param street The street address.
     * @param city The city.
     * @param state The state.
     * @param zipcode The zip code.
     * @param country The zip country.
     */
    public Address(long addrid, String street, String city, 
                   String state, String zipcode, String country)
    {
        this.addrid = addrid;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
        this.country = country;
    }

    /**
     * Get the addrid associated with this object.
     * @return the addrid.
     */
    public long getAddrid() {
        return addrid;
    }

    /** 
     * Get the street component of the address.
     * @return The street component of the address.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Set the street component of the address.
     * @param street The street component.
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Get the city.
     * @return The city component of the address.
     */
    public String getCity(String city) {
        return city;
    }

    /**
     * Set the city component of the address.
     * @param city The city.
     */
    public void setCity(String city) {
        this.city = city;
    }
    
    /**
     * Get the state component of the address.
     * @return The state.
     */
    public String getState() {
        return state;
    }

    /**
     * Set the state component of the address.
     * @param state The state.
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Get the zipcode component of the address.
     * @return The zipcode.
     */
    public String getZipcode() {
        return zipcode;
    }

    /**
     * Set the zip code component of the address.
     * @param zipcode The zipcode.
     */
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    /**
     * Get the country component of the address.
     * @return The country.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Set the country component of the address.
     * @param country The country.
     */
    public void setCountry(String country) {
        this.country = country;
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
        Address otherAddress = (Address)other;
        return (addrid == otherAddress.addrid) &&
            helper.equals(street, otherAddress.street) &&
            helper.equals(city, otherAddress.city) &&
            helper.equals(state, otherAddress.state) &&
            helper.equals(zipcode, otherAddress.zipcode) &&
            helper.equals(country, otherAddress.country);
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
        return compareTo((Address)o);
    }

    /** 
     * Compares this object with the specified Address object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Address object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified Address
     * object. 
     */
    public int compareTo(Address other) {
        long otherId = other.addrid;
        return (addrid < otherId ? -1 : (addrid == otherId ? 0 : 1));
    }
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Address) {
            return compareTo((Address)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)addrid;
    }
    
    /**
     * This class is used to represent the application identifier 
     * for the <code>Address</code> class.
     */
    public static class Oid implements Serializable, Comparable {

        /**
         * This is the identifier field for <code>Address</code> and must
         * correspond in type and name to the field in
         * <code>Address</code>. 
         */
        public long addrid;
        
        /** The name of the class of the target object.
        */
        public static final String targetClassName = "org.apache.jdo.tck.pc.company.address"; // NOI18N
        
        /** The required public, no-arg constructor. */
        public Oid()
        {
            addrid = 0;
        }

        /**
         * A constructor to initialize the identifier field.
         * @param addrid the id of the Adress.
         */
        public Oid(long addrid) {
            this.addrid = addrid;
        }
        
        public Oid(String s) { addrid = Long.parseLong(justTheId(s)); }

        public String toString() { return getTargetClassName() + ": "  + addrid;} // NOI18N


        /** */
        public boolean equals(java.lang.Object obj) {
            if( obj==null || !this.getClass().equals(obj.getClass()) )
                return( false );
            Oid o = (Oid) obj;
            if( this.addrid != o.addrid ) return( false );
            return( true );
        }

        /** */
        public int hashCode() {
            return( (int) addrid );
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
            if( addrid < other.addrid ) return -1;
            if( addrid > other.addrid ) return 1;
            return 0;
        }

    }

}
