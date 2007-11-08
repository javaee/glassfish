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

import java.util.Date;
import java.io.Serializable;

/**
 * Implementation of RegistryEntry interface
 *
 * @author Bobby Bissett
 */
public class RegistryEntryImpl extends RegistryObjectImpl implements RegistryEntry, Versionable, Serializable {

    /**
     * Default constructor
     */    
    public RegistryEntryImpl() {	
	super();        
    }

    /**
     * Constructor used by subclass to
     * initialize RegistryObject data
     */
    public RegistryEntryImpl(Key key, String description, String name) {
	super(key, description, name);
    }

    /**
     * Constructor used by subclass to
     * initialize RegistryObject data
     */
    public RegistryEntryImpl(Key key) {
	super(key);
    }

    /**
     * Level 1 method
     */
    public int getStatus() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public int getStability() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setStability(int stability) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public Date getExpiration() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setExpiration(Date date) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public int getMajorVersion() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setMajorVersion(int majorVersion) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public int getMinorVersion() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setMinorVersion(int minorVersion) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public String getUserVersion() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setUserVersion(String userVersion) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

}

