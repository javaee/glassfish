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
 * InternationalStringImpl.java
 *
 */
package com.sun.xml.registry.uddi.infomodel;

import java.util.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.io.Serializable;

/**
 * Implementation of JAXR InternationalString. "default locale"
 * in comments below refers to the locale given by
 * Locale.getDefault() at runtime.
 *
 * Class contains a java.util.HashMap that maps locales to
 * LocalizedString instances. Unless specified for a given method,
 * the behavior for null locales and values is the same as for
 * null keys and objects in HashMap.
 *
 * @author Bobby Bissett
 */
public class InternationalStringImpl implements InternationalString, Serializable {

    static String DEFAULT_CHARSET = LocalizedString.DEFAULT_CHARSET_NAME;
    private HashMap strings;
    
    /**
     * Default constructor creates empty hash map for
     * holding LocalizedStrings.
     */
    public InternationalStringImpl() {
        strings = new HashMap();
    }

    /**
     * Utility constructor used with a given LocalizedString.
     *
     * @param string LocalizedString to store
     */
    public InternationalStringImpl(LocalizedString lString)
        throws JAXRException {
            this();
	    String key = makeKey(lString.getCharsetName(), lString.getLocale()); 
            strings.put(key, lString);
    }

    /**
     * Utility constructor used with given locale and string.
     *
     * @param locale
     * @param value String value for given locale
     */
    public InternationalStringImpl(Locale locale, String value) {
        this();
        
        // does not call this(LocalizedString) to avoid throws clause
	String key = makeKey(DEFAULT_CHARSET, locale);
        strings.put(key, new LocalizedStringImpl(locale, value));
    }
    
    /**
     * Utility constructor used to set a string value for the
     * default locale.
     *
     * @param value Store this value for the default locale
     */
    public InternationalStringImpl(String value) {
        this(Locale.getDefault(), value);
    }

    /**
     * Returns the string value for default locale
     */
    public String getValue() throws JAXRException {
        String value = getValue(Locale.getDefault());
        if (value == null)
            value = getValue(new Locale(Locale.getDefault().getLanguage(), ""));  
        return value;
    }
    
    /**
     * Returns the string value for the given locale. If locale
     * is null or there is no localized string for the given
     * locale, the method will return null.
     */
    public String getValue(Locale locale) throws JAXRException {

	// hash map would allow value for null key
	if (locale == null) {
	    return null;
	}

	// get the LocalizedString and return its value if any
	String key = makeKey(DEFAULT_CHARSET, locale);
        LocalizedString string = (LocalizedString) strings.get(key);
	return (string==null ? null : string.getValue());
    }
    
    /**
     * Sets string value for default locale
     */
    public void setValue(String string) throws JAXRException {
        setValue(Locale.getDefault(), string);
    }
    
    /**
     * Sets the string value for the specified locale
     */
    public void setValue(Locale locale, String string) throws JAXRException {
	String key = makeKey(DEFAULT_CHARSET, locale);
        strings.put(key, new LocalizedStringImpl(locale, string));
    }

    /**
     * Adds given localized string to collection. LocalizedString parameter
     * cannot be null.
     */
    public void addLocalizedString(LocalizedString localizedString) throws JAXRException {
        if (localizedString == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("InternationalStringImpl:LocalizedString_cannot_be_null"));
        }
	String key = makeKey(localizedString.getCharsetName(),
			     localizedString.getLocale());
        strings.put(key, localizedString);
    }

    /**
     * Adds localized strings to the collection. If there are multiple
     * localized strings for the same locale, it is unspecified which
     * one will be set for this international string.
     */
    public void addLocalizedStrings(Collection strings) throws JAXRException {
	if (strings == null) {
	    return;
	}
	Iterator iter = strings.iterator();
	try {
	    while (iter.hasNext()) {
		addLocalizedString((LocalizedString) iter.next());
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("InternationalStringImpl:Objects_in_collection_must_be_LocalizedStrings"), e);
	}
    }
    
    /**
     * Removes given localized string. If the localized string is not
     * in this international string, nothing happens. Parameter
     * localized string cannot be null.
     */
    public void removeLocalizedString(LocalizedString lString)
        throws JAXRException {
            if (lString == null) {
		return;
            }
	    String key = makeKey(lString.getCharsetName(), lString.getLocale());
            strings.remove(key);
    }

    /**
     * Removes localized strings from the international string
     * if they are present.
     */
    public void removeLocalizedStrings(Collection lStrings)
        throws JAXRException {
	    if (lStrings == null) {
		return;
	    }
            Iterator iter = lStrings.iterator();
            while (iter.hasNext()) {
                try {
                    removeLocalizedString((LocalizedString) iter.next());
                } catch (ClassCastException e) {
                    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("InternationalStringImpl:Objects_in_collection_must_be_LocalizedStrings"), e);
                }
            }
    }
    
    /**
     * Gets the localized string for the given locale, if one exists.
     */
    public LocalizedString getLocalizedString(Locale locale, String charsetName)
        throws JAXRException {
	    String key = makeKey(charsetName, locale);
            return (LocalizedString) strings.get(key);
    }
    
    /**
     * Get the localized strings contained in this international
     * string. This method creates a new collection to return
     * rather than the one that backs the hash map.
     */
    public Collection getLocalizedStrings() throws JAXRException {
        return new ArrayList(strings.values());
    }

   /*
    * Helper method. getLocalizedString() takes a locale and
    * charset, so the hash map key is a combination of the two.
    */
    private String makeKey(String charSet, Locale locale) {
	return new String(charSet + locale.toString());
    }
  
}
