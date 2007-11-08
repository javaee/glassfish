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
import java.net.*;
import javax.xml.registry.*;

/**
 * The RegistryEntry interface is a base interface for interfaces in the model that require additional metadata beyond what is provided by the RegistryObject interface.
 * A few interfaces in the model represent high level (coarse grain) objects in the registry that require additional metadata such as version information and indication of the stability or volatility of the information.
 *
 *
 * @author Farrukh S. Najmi
 */
public interface RegistryEntry extends RegistryObject, Versionable {
    /** 
	 * Gets the life cycle status of the RegistryEntry within the registry. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
	 *
	 * @See RegistryEntry#STATUS_SUBMITTED
	 *
	 * @return the life cycle status as an integer enumeration
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    int getStatus() throws JAXRException;	
                                     
    /** 
	 * Gets the stability indicator for the RegistryEntry within the Registry. 
	 * The stability indicator is provided by the submitter as an indication 
	 * of the level of stability for the content.
     *
     * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
     *
	 * @see RegistryEntry#STABILITY_DYNAMIC
	 * @return the stability indicator as an integer enumeration
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
     */
    int getStability() throws JAXRException;

	/** 
	 * Sets the stability indicator for the RegistryEntry.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
	 *
	 * @param stability the stability indicator
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */	
    void setStability(int stability) throws JAXRException;

	
	/** 
	 * Gets expirationDate attribute of the RegistryEntry within the Registry. 
	 * This attribute defines a time limit upon the stability indication 
	 * provided by the stability attribute. Once the expirationDate has been 
	 * reached the stability attribute in effect becomes STABILITY_DYNAMIC 
	 * implying that content can change at any time and in any manner. 
	 * A null value implies that there is no expiration on stability attribute. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
	 *
	 * @return the expiration Date for the stability indicator
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	Date getExpiration() throws JAXRException;
	
	/** 
	 * Sets the expirationDate. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param expiration	the expiration Date for the stability indicator
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void setExpiration(Date expiration) throws JAXRException;
	
	/** RegistryEntry has been submitted. */
	public static final int STATUS_SUBMITTED=0;
	
	/** RegistryEntry has been submitted and approved. */
	public static final int STATUS_APPROVED=1;
	
	/** RegistryEntry has been deprecated. */
	public static final int STATUS_DEPRECATED=2;
	
	/** RegistryEntry has been withdrawn by the submitter. */
	public static final int STATUS_WITHDRAWN=3;
	

	/** RegistryEntry may change at any time. */
	public static final int STABILITY_DYNAMIC=0;
	
	/** RegistryEntry may change at any time, however the changes will be backward compatible. */
	public static final int STABILITY_DYNAMIC_COMPATIBLE=1;
	
	/** RegistryEntry will not change. */
	public static final int STABILITY_STATIC=2;
	

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     */
    /*#Slot lnkSlot;*/
}
