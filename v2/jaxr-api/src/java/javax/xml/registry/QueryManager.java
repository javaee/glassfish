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

package javax.xml.registry;

import java.util.*;
import javax.xml.registry.infomodel.*;

/**
 * This is the common base interface for all QueryManagers in the API.
 *
 * @author Farrukh S. Najmi
 */
public interface QueryManager {

    /** 
     * Gets the RegistryObject specified by the Id and type of object.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param id is the id of the Key for a RegistryObject.
	 * @param objectType is a constant definition from LifeCycleManager that specifies the type of object desired.
     * @return the RegistryObject, returned as its concrete type (e.g. Organization, User etc.).
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public RegistryObject getRegistryObject(String id, String objectType)  throws JAXRException;

    /** 
     * Gets the RegistryObject specified by the Id. 
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param id	the id for the desired object
     * @return the RegistryObject, returned as its concrete type (e.g. Organization, User etc.).
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public RegistryObject getRegistryObject(String id)  throws JAXRException;

    /** 
	 * Gets the specified RegistryObjects. 
	 * The objects are returned as their concrete type (e.g. Organization, User etc.).
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param objectKeys	a Collection of Key objects for the desired objects
	 * @return BulkResponse containing a heterogeneous Collection of RegistryObjects (e.g. Organization, User etc.).
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public BulkResponse getRegistryObjects(Collection objectKeys)  throws JAXRException;

    /** 
     * Gets the specified RegistryObjects. 
     * The objects are returned as their concrete type (e.g. Organization, User etc.).
     * 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     *
     * @param objectKeys	a Collection of Key objects for the desired objects
     * @param objectTypes	a Collection of String objects that allow filtering desired objects by their type 
     * @return BulkResponse containing a heterogeneous Collection of RegistryObjects (e.g. Organization, User etc.).
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public BulkResponse getRegistryObjects(Collection objectKeys, String objectTypes)  throws JAXRException;

    /** 
     * Gets the RegistryObjects owned by the caller. 
     * The objects are returned as their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return BulkResponse containing a heterogeneous Collection of RegistryObjects (e.g. Organization, User etc.).
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public BulkResponse getRegistryObjects()  throws JAXRException;

    /** 
     * Gets the RegistryObjects owned by the caller, that are of the specified type. 
     * The objects are returned as their concrete type (e.g. Organization, User etc.).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param objectType Is a constant that defines the type of object sought. See LifeCycleManager for constants for object types.
	 * @see LifeCycleManager#ORGANIZATION
     * @return BulkResponse containing a heterogeneous Collection of RegistryObjects (e.g. Organization, User etc.).
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public BulkResponse getRegistryObjects(String objectType)  throws JAXRException;

	
    /**
     * Returns the parent RegistryService that created this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the RegistryService created this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{javax.xml.registry.RegistryService}>
     */
    RegistryService getRegistryService() throws JAXRException;
}
