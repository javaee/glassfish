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

/*
 * TelephoneNumberImpl.java
 *
 * Created on May 16, 2001, 11:11 AM
 */

package com.sun.xml.registry.uddi.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.Serializable;

/**
 *
 * @author  kwalsh
 * @author Bobby Bissett
 */
public class TelephoneNumberImpl implements TelephoneNumber, Serializable {

    String number;
    String type;
    
    /**
     * Default constructor
     */
    public TelephoneNumberImpl() {
	number = new String();
	type = new String(); // default is "any String"
    }

    /**
     * The telephone number suffix not including the country or area code. 
     */
    public String getNumber() throws JAXRException {
        return number;
    }
    
    /**
     * The telephone number suffix not including the country or area code. 
     */
    public void setNumber(String number) throws JAXRException {
        this.number = number;
    }

    /**
     * The type of telephone number (e.g. fax etc.)
     */
    public String getType() throws JAXRException{
        return type;
    }
    
    /**
     * The type of telephone number (e.g. fax etc.) as a Concept
     */
    public void setType(String type) throws JAXRException {
        this.type = type;
    }

    /**
     * Level 1 method
     */
    public String getCountryCode() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setCountryCode(String countryCode) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public String getAreaCode() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setAreaCode(String areaCode) throws JAXRException {
        //this.areaCode = areaCode;
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public String getExtension() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setExtension(String extension) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public String getUrl() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setUrl(String url) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

}
