/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/

package com.sun.xml.registry.uddi.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.Serializable;

/**
 * Implementation of PersonName interface
 *
 * @author Bobby Bissett
 */
public class PersonNameImpl implements PersonName, Serializable {
	
    private String fullName;

    /**
     * Default constructor
     */    
    public PersonNameImpl() {   
	fullName = new String();
    }
	
    /**
     * Utility constructor given the person's name
     */
    public PersonNameImpl(String fullName){
	this.fullName = fullName;
    }

    /**
     * The fully formatted name for this Person.
     */
    public String getFullName() throws JAXRException {
        return fullName;
    }
    
    /**
     * Sets the fully formatted name for this Person.
     */
    public void setFullName(String fullName) throws JAXRException {
        this.fullName = fullName;
    }
    
    /**
     * Level 1 method
     */
    public String getFirstName() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public String getLastName() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public String getMiddleName() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setFirstName(String firstName) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /** 
     * Level 1 method
     */
    public void setLastName(String lastName) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setMiddleName(String middleName) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
}
