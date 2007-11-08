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

import java.util.*;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.Serializable;

/**
 * Implementation of JAXR Slot.
 *
 * @author Bobby Bissett
 */
public class SlotImpl implements Slot, Serializable {

    // defaults are null for the strings
    private String name;
    private String slotType;
    private ArrayList values;

    /**
     * Default constructor
     */
    public SlotImpl() {
	values = new ArrayList();
    }
	
    /**
     * Utility constructor when name, type, and one value
     * are known.
     */
    public SlotImpl(String name, String value, String type) {
	this();
	this.name = name;
	slotType = type;
	values.add(value);
    }
	
    /**
     * Utility constructor when name, type, and values
     * are known.
     */
    public SlotImpl(String name, Collection values, String type) {
	this();
	this.name = name;
	slotType = type;
	this.values.addAll(values);
    }
    
    /**
     * Getter for Slot name. Default is a null string.
     */
    public String getName() throws JAXRException {
        return name;
    }
    
    /**
     * Setter for Slot name
     */
    public void setName(String name) throws JAXRException {
        this.name = name;
    }
    
    /**
     * Getter for Slot type. Default is a null string.
     */
    public String getSlotType() throws JAXRException {
        return slotType;
    }
    
    /**
     * Setter for Slot type
     */
    public void setSlotType(String slotType) throws JAXRException {
        this.slotType = slotType;
    }
    
    /**
     * Getter for the Slot values
     */
    public Collection getValues() throws JAXRException {
	return (Collection) values.clone();
    }

    /**
     * Setter for values. Null param will be treated as
     * an empty collection.
     */    
    public void setValues(Collection values) throws JAXRException {
	this.values = new ArrayList();
	if (values != null) {
	    this.values.addAll(values);
	}
    }  
    
}
