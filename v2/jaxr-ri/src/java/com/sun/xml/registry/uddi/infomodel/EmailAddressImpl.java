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
 * Implementation of EmailAddress interface
 *
 * @see User
 * @author Farrukh S. Najmi
 */
public class EmailAddressImpl implements EmailAddress, Serializable {
    
    String address;
    String type;
    
    /**
     * Default constructor
     */
    public EmailAddressImpl() {
    }

    /**
     * Utility constructor
     */
    public EmailAddressImpl(String address) {
        this.address = address;
    }
    
    /**
     * Utility constructor
     */
    public EmailAddressImpl(String address, String type) {
        this(address);
        this.type = type;
    }
    
    /**
     * Returns the email address for this object.
     */
    public String getAddress() throws JAXRException{
        return address;
    }
    
    /**
     * Sets the email address for this object.
     */
    public void setAddress(String address) throws JAXRException {
        this.address = address;
    }
    
    /**
     * The type for this object.
     */
    public String getType() throws JAXRException {
        return type;
    }
    
    /**
     * Sets the type for this object.
     */
    public void setType(String type) throws JAXRException{
        this.type = type;
    }
}
