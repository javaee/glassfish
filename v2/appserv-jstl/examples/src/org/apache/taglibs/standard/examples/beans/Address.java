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

/**
 * Object that represents a Customer.
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:36 $
 */

public class Address {
    
    //*********************************************************************
    // Instance variables
    
    /** Holds value of property line1. */
    private String line1;
    
    /** Holds value of property line2. */
    private String line2;
    
    /** Holds value of property city. */
    private String city;
    
    /** Holds value of property zip. */
    private String zip;

    /** Holds value of property state. */
    private String state;
    
    /** Holds value of property country. */
    private String country;
    
    //*********************************************************************
    // Constructor
    
    public Address(String line1, String line2, String city,
    String state, String zip, String country) {
        setLine1(line1);
        setLine2(line2);
        setCity(city);
        setState(state);
        setZip(zip);
        setCountry(country);
    }
    
    //*********************************************************************
    // Accessors
    
    /** Getter for property line1.
     * @return Value of property line1.
     */
    public String getLine1() {
        return line1;
    }
    
    /** Setter for property line1.
     * @param line1 New value of property line1.
     */
    public void setLine1(String line1) {
        this.line1 = line1;
    }
    
    /** Getter for property line2.
     * @return Value of property line2.
     */
    public String getLine2() {
        return line2;
    }
    
    /** Setter for property line2.
     * @param line2 New value of property line2.
     */
    public void setLine2(String line2) {
        this.line2 = line2;
    }
    
    /** Getter for property city.
     * @return Value of property city.
     */
    public String getCity() {
        return city;
    }
    
    /** Setter for property city.
     * @param city New value of property city.
     */
    public void setCity(String city) {
        this.city = city;
    }
    
    /** Getter for property zip.
     * @return Value of property zip.
     */
    public String getZip() {
        return zip;
    }
    
    /** Setter for property zip.
     * @param zip New value of property zip.
     */
    public void setZip(String zip) {
        this.zip = zip;
    }
    
    /** Getter for property country.
     * @return Value of property country.
     */
    public String getCountry() {
        return country;
    }
    
    /** Setter for property country.
     * @param country New value of property country.
     */
    public void setCountry(String country) {
        this.country = country;
    }
    
    /** Getter for property state.
     * @return Value of property state.
     */
    public String getState() {
        return state;
    }
    
    /** Setter for property state.
     * @param state New value of property state.
     */
    public void setState(String state) {
        this.state = state;
    }
    
    //*********************************************************************
    // Utility Methods
    
    /**
     * Return a String representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(line1).append(" ");
        sb.append(city).append(" ");
        sb.append(country);
        return (sb.toString());
    } 
}
