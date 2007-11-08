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

import javax.activation.DataHandler;
import java.io.Serializable;

/**
 * Implementation of ExtrinsicObject. All levels are level 1 methods
 */
public class ExtrinsicObjectImpl extends RegistryEntryImpl implements ExtrinsicObject, Serializable {

    /**
     * Default constructor
     */
    public ExtrinsicObjectImpl() {
        super();
    }

    /**
     * Level 1 method
     */
    public String getMimeType() throws JAXRException {
		throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void setMimeType(String mimeType) throws JAXRException {
		throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public boolean isOpaque() throws javax.xml.registry.JAXRException {
		throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setOpaque(boolean isOpaque) throws JAXRException {
		throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public DataHandler getRepositoryItem() throws JAXRException {
        throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setRepositoryItem(javax.activation.DataHandler repositoryItem) throws UnsupportedCapabilityException, JAXRException {
        throw new UnsupportedCapabilityException();
    }
    
}
