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
import java.util.*;

/**
 * This interface is used as a simple wrapper interface that associates a String with its Locale. The interface is needed in the InternationalString interface where a Collection of LocalizedString instances are kept. Each LocalizedString instance has a Locale and a String instance. 
 *
 * @see InternationalString
 * @author Farrukh S. Najmi
 */
public interface LocalizedString {

	/**
	 * The default name returned by getCharsetName if no other 
	 * name has explicitly been set.
	 */
	public static final String DEFAULT_CHARSET_NAME = "UTF-8";

	/**
	 * Gets the canonical name for the charset for this object.
	 * Must return the default charset when there is no charset name defined.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @see LocalizedString#DEFAULT_CHARSET_NAME
	 * @return the character set name for the character set used by this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    String getCharsetName() throws JAXRException;

	/**
	 * Get the Locale for this object.
	 * Must return the default Locale when no Locale has been defined.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @see java.util.Locale#getDefault() 
	 * @return the Locale used by this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    Locale getLocale() throws JAXRException;
	
    /**
     * Get the String value for this object.
	 *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return the value defined by this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    String getValue() throws JAXRException;

    /**
     * Set the canonical name for the charset for this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param charsetName	the character set name for the character set used by this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setCharsetName(String charsetName) throws JAXRException;

    /**
     * Set the Locale for this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param locale 	the Locale used by this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setLocale(Locale locale) throws JAXRException;

    /**
     * Set the String value for the specified object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param value	the value defined by this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setValue(String value) throws JAXRException;		
	
}