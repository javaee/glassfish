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

package com.sun.xml.registry.uddi.infomodel;

import java.net.*;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * Implementation of URIValidator. This class is used as a
 * delegate in other classes that implement URIValidator.
 * Declared Serializable here so that it can be serialized
 * with the objects that use it.
 *
 * @author Bobby Bissett
 */
public class URIValidatorImpl implements URIValidator, Serializable {

	private boolean validateURI = false;

    /**
     * Getter for validateURI
     */
    public boolean getValidateURI() {
	return validateURI;
    }

    /**
     * Setter for validateURI
     */
    public void setValidateURI(boolean validate) {
	validateURI = validate;
    }

    /**
     * Attemptes to validate a given uri. Throws exception
     * if invalid.
     */
    void validate(String uri) throws InvalidRequestException {
	if (validateURI == false) {
	    return;
	}

	URL url = null;
	try {
	    url = new URL(uri);
	}
	catch(MalformedURLException e) {
	    throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:Malformed_URL_Exception:_") + uri, e);
	}

	// only try to resolve http urls
	if (url.getProtocol().equalsIgnoreCase("http")) {

	    try {
		HttpURLConnection connection =
		    (HttpURLConnection) url.openConnection();
		int responseCode = connection.getResponseCode();
                
                if ( responseCode == 404 ) {
                    throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:Received_response_code_") + responseCode + ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:_for_uri_") + uri);
                }
                
		if ((responseCode < 200) || (responseCode > 302) && (responseCode != 400)) {
		    throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:Received_response_code_") + responseCode + ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:_for_uri_") + uri);
		}
	    } catch (UnknownHostException uhe) {
		throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:Make_sure_your_proxies_are_set._Received_error:_") + uhe, uhe);
	    } catch (Exception e) {
		throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("URIValidatorImpl:Could_not_validate_") + uri + ":" + e, e);
	    }
	} 
    }
}
