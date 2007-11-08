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


package com.sun.xml.registry.uddi;

import javax.xml.registry.*;

/**
 * Class Declaration for Class1
 * @see
 * @author Farrukh S. Najmi
 */
public class SecurityManagerImpl {
    /**
     * Adds the authentication tokens (information) to the specified request
	 * as required by the spefic target registry provider. The request is a 
	 * registry provider specific object.
	 *
	 * @return Object that is the modified request with the authentication info added.
     */
    public Object addAuthenticationTokens(Object request) throws JAXRException{
        // Write your code here
        return null;
    }

    /**
     * Digitally sign the specified request
     * as required by the spefic target registry provider. The request is a 
     * registry provider specific object.
     *
     * @return Object that is the modified request with the digital signature added.
     */
    public Object signRequest(Object request) throws JAXRException{
        // Write your code here
        return null;
    }

    /**
     * Validate the digitally signature in the specified response.
     * The reponse is a registry provider specific object.
	 * Throws an InvalidObjectException??
     *
     */
    public void validateRequest(Object response) throws JAXRException{
        // Write your code here
    }

    /**
     * Encrypt the specified request
     * as required by the spefic target registry provider. The request is a 
     * registry provider specific object.
     *
     * @return Object that is the encrypted request.
     */
    public Object encryptRequest(Object request) throws JAXRException{
        // Write your code here
        return null;
    }

    /**
     * Decrypt the specified response
     * The response is a registry provider specific object.
     *
     * @return Object that is the decrypted response.
     */
    public Object decryptRequest(Object request) throws JAXRException{
        // Write your code here
        return null;
    }
}
