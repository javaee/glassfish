/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.taglibs.standard.examples.beans;

import java.util.Date;
import java.text.*;

/**
 * Object that represents a Customer.
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:37 $
 */

public class Customer {
    
    //*********************************************************************
    // Instance variables
    
    /** Holds value of property key. */
    int key;
    
    /** Holds value of property lastName. */
    private String lastName;
    
    /** Holds value of property firstName. */
    private String firstName;
    
    /** Holds value of property birthDate. */
    private Date birthDate;
    
    /** Holds value of property address. */
    private Address address;
       
    /** Holds value of property phoneHome. */
    private String phoneHome;
    
    /** Holds value of property phoneCell. */
    private String phoneCell;
    
    static DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    
    //*********************************************************************
    // Constructors
    
    public Customer() {}
    
    public Customer(int key,
    String lastName,
    String firstName,
    Date birthDate,
    Address address,
    String phoneHome,
    String phoneCell) {
        init(key, lastName, firstName, birthDate, address, phoneHome, phoneCell);
    }
    
    public void init(int key,
    String lastName,
    String firstName,
    Date birthDate,
    Address address,
    String phoneHome,
    String phoneCell) {
        setKey(key);
        setLastName(lastName);
        setFirstName(firstName);
        setBirthDate(birthDate);
        setAddress(address);
        setPhoneHome(phoneHome);
        setPhoneCell(phoneCell);
    }
    
    //*********************************************************************
    // Properties
    
    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public int getKey() {
        return key;
    }
    
    /**
     * Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(int key) {
        this.key = key;
    }
    
    /**
     * Getter for property lastName.
     * @return Value of property lastName.
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Setter for property lastName.
     * @param lastName New value of property lastName.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Getter for property firstName.
     * @return Value of property firstName.
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Setter for property firstName.
     * @param firstName New value of property firstName.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Getter for property birthDate.
     * @return Value of property birthDate.
     */
    public Date getBirthDate() {
        return birthDate;
    }
    
    /**
     * Setter for property birthDate.
     * @param birthDate New value of property birthDate.
     */
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    /**
     * Getter for property address.
     * @return Value of property address.
     */
    public Address getAddress() {
        return address;
    }
    
    /**
     * Setter for property address.
     * @param address New value of property address.
     */
    public void setAddress(Address address) {
        this.address = address;
    }
    
    /**
     * Getter for property phoneHome.
     * @return Value of property phoneHome.
     */
    public String getPhoneHome() {
        return phoneHome;
    }
    
    /**
     * Setter for property phoneHome.
     * @param phoneHome New value of property phoneHome.
     */
    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }
    
    /**
     * Getter for property phoneCell.
     * @return Value of property phoneCell.
     */
    public String getPhoneCell() {
        return phoneCell;
    }
    
    /**
     * Setter for property phoneCell.
     * @param phoneCell New value of property phoneCell.
     */
    public void setPhoneCell(String phoneCell) {
        this.phoneCell = phoneCell;
    }
    
    //*********************************************************************
    // Utility Methods
    
    /**
     * Return a String representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(key).append("] ");
        sb.append(getLastName()).append(", ");
        sb.append(getFirstName()).append("  ");
        sb.append(df.format(getBirthDate())).append("  ");
        sb.append(getAddress()).append("  ");
        if(getPhoneHome() != null) sb.append(getPhoneHome()).append("  ");
        if(getPhoneCell() != null) sb.append(getPhoneCell());
        return (sb.toString());
    }
}

