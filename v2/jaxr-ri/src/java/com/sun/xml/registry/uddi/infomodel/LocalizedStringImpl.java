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

import java.util.Locale;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.io.Serializable;

/**
 * Implementation of JAXR LocalizedString.
 *
 * @author Bobby Bissett
 */
public class LocalizedStringImpl implements LocalizedString, Serializable {

    private String charsetName;
    private Locale locale;
    private String value;
    
    /**
     * Default constructor
     */
    public LocalizedStringImpl() {
	charsetName = LocalizedString.DEFAULT_CHARSET_NAME;
	locale = Locale.getDefault();
    }

    /**
     * Utility constructor sets locale and value
     *
     * @param locale The locale for this localized string
     * @param value The string value for this localized string
     */
    public LocalizedStringImpl(Locale locale, String value) {
	this();
        this.locale = locale;
        this.value = value;
    }

    /**
     * Get the locale
     */
    public Locale getLocale() throws JAXRException {
	return locale;
    }
    
    /**
     * Set the locale
     */
    public void setLocale(Locale locale) throws JAXRException {
        this.locale = locale;
    }
    
    /**
     * Get the value
     */
    public String getValue() throws JAXRException {
        return value;
    }
    
    /**
     * Set the value
     */
    public void setValue(String value) throws JAXRException {
        this.value = value;
    }
    
    /**
     * Get the charset name
     */
    public String getCharsetName() throws JAXRException {
	return charsetName;
    }
    
    /**
     * Set the charset name
     */
    public void setCharsetName(String charset) throws JAXRException {
        charsetName = charset;
    }
}
