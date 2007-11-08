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
 * RegistryPackage instances are RegistryEntries that group logically related 
 * RegistryEntries together.
 * A package may contain any number of RegistryObjects. A RegistryObject may be a member of any number of Packages.
 *
 * @see RegistryObject
 * @author Farrukh S. Najmi
 */
public interface RegistryPackage extends RegistryEntry {
	/** 
	 * Adds a child RegistryObject as member. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param registryObject	the RegistryObject being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void addRegistryObject(RegistryObject registryObject) throws JAXRException;

	/** 
	 * Adds a Collection of RegistryObject children as members.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param registryObjects	the Collection of RegistryObjects being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void addRegistryObjects(Collection registryObjects) throws JAXRException;

	/** 
	 * Removes a child RegistryObject from membership. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param registryObject	the RegistryObject being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void removeRegistryObject(RegistryObject registryObject) throws JAXRException;

	/** 
	 * Removes a Collection of children RegistryObjects from membership.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param registryObjects	the Collection of RegistryObject being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void removeRegistryObjects(Collection registryObjects) throws JAXRException;

    /**
     * Gets the collection of member RegistryObjects of this RegistryPackage.
     * 	 	 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @return the Set of RegistryObjects that are members of this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Set getRegistryObjects() throws JAXRException;
}
