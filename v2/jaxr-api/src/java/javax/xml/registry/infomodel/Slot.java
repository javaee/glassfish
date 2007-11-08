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
 * Slot instances provide a dynamic way to add arbitrary attributes to 
 * RegistryObject instances. This ability to add attributes dynamically 
 * to RegistryObject instances enables extensibility within the Registry 
 * Information Model. 
 * <p>
 * A RegistryObject may have 0 or more Slots. A slot is composed of a name, 
 * a slotType and a collection of values. The name of a slot is locally unique 
 * within the RegistryObject instance. Similarly, the value of a Slot is 
 * locally unique within a slot instance. Since a Slot represents an 
 * extensible attribute whose value may be a collection, a 
 * Slot is allowed to have a collection of values rather than a single value. 
 * The slotType attribute may optionally specify a type or category for the slot.
 *
 * @see ExtensibleObject
 * @author Farrukh S. Najmi 
 */
public interface Slot {

	/**
	 * Gets the name for this Slot.
	 * Default is a NULL String. 
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the name 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getName() throws JAXRException;

    /**
     * Sets the name for this Slot.
     * Default is a NULL String. 
     * 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 @param name	the name
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setName(String name) throws JAXRException;

    /**
     * Gets the slotType for this Slot.
     * Default is a NULL String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the slot type which is an arbitrary String
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getSlotType() throws JAXRException;

    /**
     * Sets the slotType for this Slot.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param slotType	the slot type which is an arbitrary String
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setSlotType(String slotType) throws JAXRException;

    /**
     * Gets the values for this Slot.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	
	 * 
     * @see java.lang.String
     * @return Collection of String instances representing the values for this Slot. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     */
    public Collection getValues() throws JAXRException;

    /**
     * Sets the values for this Slot.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param values the values for this Slot
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setValues(Collection values) throws JAXRException;
	
	/**
	 * Name for pre-defined Slot used in PostalAddress by JAXR UDDI provider.
	 */
	public static final String SORT_CODE_SLOT = "sortCode";

	/**
	 * Name for pre-defined Slot used in PostalAddress by JAXR UDDI provider.
	 */
	public static final String ADDRESS_LINES_SLOT = "addressLines";		

	/**
	 * Name for pre-defined Slot used in Organization and ClassificationScheme by JAXR UDDI provider.
	 */
	public static final String AUTHORIZED_NAME_SLOT = "authorizedName";

	/**
	 * Name for pre-defined Slot used in Organization and ClassificationScheme by JAXR UDDI provider.
	 */
	public static final String OPERATOR_SLOT = "operator";

}
