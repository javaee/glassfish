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
 * Contains the response of a method in the API that performs a bulk
 * operation and returns a bulk response. Partial commits are allowed on
 * a bulk operation.
 * <p>
 * In the event of a partial success where only a subset of objects were processed successfully, the getStatus method of the BulkResponse must return JAXRResponse.STATUS_WARNING. In this case, a Collection of JAXRException instances is included in the BulkResponse instance. The JAXRExceptions provide information on each error that prevented some objects in the request to not be processed successfully.
 *
 * @see QueryManager
 * @see LifeCycleManager
 * @author Farrukh S. Najmi
 */
public interface BulkResponse extends JAXRResponse {

    /**
     * Get the Collection of objects returned as a response of a 
	 * bulk operation.
	 * Caller thread will block here if result is not yet available. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.RegistryObject
     * @return Collection of RegistryObject instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Collection getCollection() throws JAXRException;

    /** 
	 * Get the Collection of RegistryException instances in case of partial commit. 
	 * Caller thread will block here if result is not yet available. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL>
	 *	 
	 * @see RegistryException
	 * @return Collection of RegistryException instances. Return null if result is available and there is no RegistryException.	 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    Collection getExceptions() throws JAXRException;

	/** 
	 * Determines whether the response is a partial response due to large result set.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return <code>true</code> if the response is partial; <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public boolean isPartialResponse() throws JAXRException;
	
}
