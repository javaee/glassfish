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
 * The Versionable interface defines the behavior common to classes that 
 * are capable of creating versions of their instances. At present all 
 * RegistryEntry classes are required to implement the Versionable interface. 
 *
 * @see RegistryEntry 
 * @author Farrukh S. Najmi 
 */
public interface Versionable {
    /**
     * Gets the major revision number for this version of the Versionable object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the major version for this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    int getMajorVersion() throws JAXRException;

    /**
     * Sets the major revision number for this version of the Versionable object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param majorVersion	the major version number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setMajorVersion(int majorVersion) throws JAXRException;

    /**
     * Gets the minor revision number for this version of the Versionable object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @return the minor version for this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    int getMinorVersion() throws JAXRException;

    /**
     * Sets the minor revision number for this version of the Versionable object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @param minorVersion	the minor version number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setMinorVersion(int minorVersion) throws JAXRException;

    /**
     * Gets the user-specified revision number for this version of the Versionable object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @return the user-defined version number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    String getUserVersion() throws JAXRException;

    /**
     * Sets the user specified revision number for this version of the Versionable object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @param userVersion	the user-defined version number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setUserVersion(String userVersion) throws JAXRException;
}
