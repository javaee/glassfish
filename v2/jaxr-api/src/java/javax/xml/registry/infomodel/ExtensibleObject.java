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

package javax.xml.registry.infomodel;

import java.util.*;

import javax.xml.registry.*;

/**
 * An ExtensibleObject is one that allows itself to be extended by utilizing 
 * dynamically added Slots that add arbitrary attributes to the object on a 
 * per instance basis.
 * 
 * @see Slot
 * @author Farrukh S. Najmi  
 */
public interface ExtensibleObject {
    /** 
	 * Adds a Slot to this object. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param slot	the Slot object being added to this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addSlot(Slot slot) throws JAXRException;

    /**
     *
     * Adds more Slots to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param slots	the Collection of Slot objects being added to this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 0..*
     * @associates <{Slot}>
     * @undirected
     * @supplierRole slots
     * @link aggregationByValue
     */
    void addSlots(Collection slots) throws JAXRException;

    /** 
	 * Removes a Slot from this object. The Slot is identified by its name. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param slotName	the name for the Slot object being removed from this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeSlot(String slotName) throws JAXRException;

    /**
     * Removes specified Slots from this object. The Slots are identified by its name.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param slotNames	the Collection of names for Slot objects being removed from this object. Must be a Collection of Strings
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 0..*
     * @associates <{Slot}>
     * @undirected
     * @supplierRole slots
     * @link aggregationByValue
     */
    void removeSlots(Collection slotNames) throws JAXRException;

	/**
	 * Gets the slot specified by slotName.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param slotName the name of the desired Slot object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	Slot getSlot(String slotName) throws JAXRException;
	
    /**
     * Returns the Slots associated with this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return Collection of Slot instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 0..*
     * @associates <{Slot}>
     * @undirected
     * @supplierRole slots
     * @link aggregationByValue
     */
    Collection getSlots() throws JAXRException;
	
}