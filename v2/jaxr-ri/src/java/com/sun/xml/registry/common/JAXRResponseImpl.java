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


/*
 * JAXRResponseImpl.java
 *
 * Created on May 17, 2001, 1:13 PM
 */

package com.sun.xml.registry.common;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 *
 * @author  kwalsh
 * @version
 */
public class JAXRResponseImpl implements javax.xml.registry.JAXRResponse {
    
    protected String requestId;
    protected int status;
    
    /**
     * Creates new JAXRResponseImpl
     */
    public JAXRResponseImpl() {
        status = STATUS_SUCCESS;
    }
    
    /**
     * Returns the request id of the bulk response.
     */
    public String getRequestId() throws JAXRException {
        return requestId;
    }
    
    /**
     * Sets the request id of the response. This is set before
     * returning the response to the client.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * Returns the status of the response. Once delivered to a client
     * the status may still be updated by the provider in the case
     * of an asynchronous connection.
     */
    public int getStatus() throws JAXRException {
	synchronized (this) {
	    return status;
	}
    }

    /**
     * Sets the status of the message. This is set before returning
     * the response to the client, except when asynchronous connections
     * are used. Then the status may be changed from STATUS_UNAVAILABLE
     * to the final status.
     */
    public void setStatus(int status)  {
	synchronized (this) {
	    this.status = status;
	}
    }
	
    /**
     * Returns true if a response is available, false otherwise.
     * This is a polling method and must not block.
     */
    public boolean isAvailable() throws JAXRException{
	return (getStatus() != STATUS_UNAVAILABLE);
    }
    
}


