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
 * Implementation of Versionable interface. All methods are
 * level 1 methods.
 *
 * @author Bobby Bissett
 */
public class VersionableImpl implements Versionable, Serializable {

    /**
     * Level 1 method, throws UnsupportedCapabilityException
     */
    public int getMajorVersion() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method, throws UnsupportedCapabilityException
     */
    public void setMajorVersion(int majorVersion) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method, throws UnsupportedCapabilityException
     */
    public int getMinorVersion() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method, throws UnsupportedCapabilityException
     */
    public void setMinorVersion(int minorVersion) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method, throws UnsupportedCapabilityException
     */
    public String getUserVersion() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method, throws UnsupportedCapabilityException
     */
    public void setUserVersion(String userVersion) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
}
