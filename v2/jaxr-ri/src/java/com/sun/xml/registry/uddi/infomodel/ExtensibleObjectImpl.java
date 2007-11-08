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

import java.util.*;
import java.io.Serializable;

/**
 * Implementation of ExtensibleObject interface.
 *
 * Class contains a java.util.HashMap that maps names to
 * slots. Unless specified for a given method, the behavior for 
 * null names and slots is the same as for
 * null keys and objects in HashMap.
 *
 * @author Bobby Bissett
 */
public class ExtensibleObjectImpl implements ExtensibleObject, Serializable {

    private HashMap slots;
    
    /**
     * Creates new ExtensibleObjectImpl
     */
    public ExtensibleObjectImpl() {
        slots = new HashMap();
    }

    /**
     * Add a single slot to the map. The map key is slot.getName()
     * The slot cannot be null.
     */
    public void addSlot(Slot slot) throws JAXRException {
	if (slot == null) {
	    throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ExtensibleObjectImpl:Slot_cannot_be_null"));
	}
	slots.put(slot.getName(), slot);
    }

    /**
     * Add multiple slots. Null param is treated as an
     * empty collection.
     */
    public void addSlots(Collection slots) throws JAXRException {
	if (slots == null) {
	    return;
	}
	Iterator iter = slots.iterator();
	try {
	    while (iter.hasNext()) {
		addSlot((Slot) iter.next());
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ExtensibleObjectImpl:Objects_in_collection_must_be_Slots"), e);
	}
    }
    
    /**
     * Get single slot from map.
     */
    public Slot getSlot(String slotName) throws JAXRException {
	return (Slot) slots.get(slotName);
    }

    /**
     * Returns all slots.
     */
    public Collection getSlots() throws JAXRException {
	return new ArrayList(slots.values());
    }
    
    /**
     * Remove single slot from the map.
     */
    public void removeSlot(java.lang.String slotName) throws JAXRException {
	slots.remove(slotName);
    }
    
    /**
     * Remove all slots with the given names. Null param is treated
     * as an empty collection.
     */
    public void removeSlots(Collection slotNames) throws JAXRException {
	if (slotNames == null) {
	    return;
	}
	Iterator iter = slotNames.iterator();
	try {
	    while (iter.hasNext()) {
		removeSlot((String) iter.next());
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ExtensibleObjectImpl:Objects_in_collection_must_be_Strings"), e);
	}
    }
    
}
