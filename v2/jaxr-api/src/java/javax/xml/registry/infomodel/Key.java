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

import javax.xml.registry.*;

/**
 * Represents a unique key that identifies a RegistryObject. Must be a DCE 128 UUID.
 *
 * @see RegistryObject
 * @author Farrukh S. Najmi
 */
public interface Key {

	/**
	 * Returns the unique Id of this key.
	 * Default is a NULL String. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the id for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getId() throws JAXRException;
	
	/**
	 * Sets the unique id associated with this key.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param id	the id being defined for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setId(String id) throws JAXRException;
	
}
