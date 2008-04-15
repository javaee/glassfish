/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.jsp.jstl.fmt;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;

/**
 * Class which exposes the locale-determination logic for resource bundles
 * through convenience methods.
 *
 * <p> This class may be useful to any tag handler implementation that needs
 * to produce localized messages. For example, this might be useful for 
 * exception messages that are intended directly for user consumption on an 
 * error page.
 *
 * @author Jan Luehe
 */

public class LocaleSupport {

    private static final String UNDEFINED_KEY = "???";
    private static final char HYPHEN = '-';
    private static final char UNDERSCORE = '_';
    private static final String REQUEST_CHAR_SET =
        "javax.servlet.jsp.jstl.fmt.request.charset";
    private static final Locale EMPTY_LOCALE = new Locale("", "");

    /** 
     * Retrieves the localized message corresponding to the given key.
     *
     * <p> The given key is looked up in the resource bundle of the default
     * I18N localization context, which is retrieved from the
     * <tt>javax.servlet.jsp.jstl.fmt.localizationContext</tt> configuration
     * setting.
     *
     * <p> If the configuration setting is empty, or the default I18N
     * localization context does not contain any resource bundle, or the given
     * key is undefined in its resource bundle, the string "???&lt;key&gt;???" is
     * returned, where "&lt;key&gt;" is replaced with the given key.
     * 
     * @param pageContext the page in which to get the localized message
     * corresponding to the given key  
     * @param key the message key
     * 
     * @return the localized message corresponding to the given key 
     */ 
    public static String getLocalizedMessage(PageContext pageContext, 
                                             String key) {
	return getLocalizedMessage(pageContext, key, null, null);
    }

    /** 
     * Retrieves the localized message corresponding to the given key.
     *
     * <p> The given key is looked up in the resource bundle with the given
     * base name.
     *
     * <p> If no resource bundle with the given base name exists, or the given
     * key is undefined in the resource bundle, the string "???&lt;key&gt;???" is
     * returned, where "&lt;key&gt;" is replaced with the given key.
     * 
     * @param pageContext the page in which to get the localized message
     * corresponding to the given key  
     * @param key the message key
     * @param basename the resource bundle base name 
     * 
     * @return the localized message corresponding to the given key 
     */ 
    public static String getLocalizedMessage(PageContext pageContext, 
                                             String key, 
                                             String basename) {
	return getLocalizedMessage(pageContext, key, null, basename);
    }

    /**
     * Retrieves the localized message corresponding to the given key, and
     * performs parametric replacement using the arguments specified via
     * <tt>args</tt>.
     *
     * <p> See the specification of the &lt;fmt:message&gt; action for a description
     * of how parametric replacement is implemented.
     *
     * <p> The localized message is retrieved as in
     * {@link #getLocalizedMessage(javax.servlet.jsp.PageContext,java.lang.String) getLocalizedMessage(pageContext, key)}.
     *
     * @param pageContext the page in which to get the localized message
     * corresponding to the given key  
     * @param key the message key
     * @param args the arguments for parametric replacement 
     * 
     * @return the localized message corresponding to the given key 
     */ 
    public static String getLocalizedMessage(PageContext pageContext, 
                                             String key, 
                                             Object[] args) {
	return getLocalizedMessage(pageContext, key, args, null);
    }

    /**
     * Retrieves the localized message corresponding to the given key, and
     * performs parametric replacement using the arguments specified via
     * <tt>args</tt>.
     *
     * <p> See the specification of the &lt;fmt:message&gt; action for a description
     * of how parametric replacement is implemented.
     *
     * <p> The localized message is retrieved as in
     * {@link #getLocalizedMessage(javax.servlet.jsp.PageContext,java.lang.String, java.lang.String) getLocalizedMessage(pageContext, key, basename)}.
     * 
     * @param pageContext the page in which to get the localized message
     * corresponding to the given key  
     * @param key the message key
     * @param args the arguments for parametric replacement 
     * @param basename the resource bundle base name 
     * 
     * @return the localized message corresponding to the given key 
     */ 
    public static String getLocalizedMessage(PageContext pageContext, 
                                             String key, 
                                             Object[] args, 
                                             String basename) {
	LocalizationContext locCtxt = null;
	String message = UNDEFINED_KEY + key + UNDEFINED_KEY;

	if (basename != null) {
	    locCtxt = getLocalizationContext(pageContext, basename);
	} else {
	    locCtxt = getLocalizationContext(pageContext);
	}

	if (locCtxt != null) {
	    ResourceBundle bundle = locCtxt.getResourceBundle();
	    if (bundle != null) {
		try {
		    message = bundle.getString(key);
		    if (args != null) {
			MessageFormat formatter = new MessageFormat("");
			if (locCtxt.getLocale() != null) {
			    formatter.setLocale(locCtxt.getLocale());
			}
			formatter.applyPattern(message);
			message = formatter.format(args);
		    }
		} catch (MissingResourceException mre) {
		}
	    }
	}

	return message;
    }


    /**
     * Gets the default I18N localization context.
     *
     * @param pc Page in which to look up the default I18N localization context
     */    
    private static LocalizationContext getLocalizationContext(PageContext pc) {
	LocalizationContext locCtxt = null;

	Object obj = Config.find(pc, Config.FMT_LOCALIZATION_CONTEXT);
	if (obj == null) {
	    return null;
	}

	if (obj instanceof LocalizationContext) {
	    locCtxt = (LocalizationContext) obj;
	} else {
	    // localization context is a bundle basename
	    locCtxt = getLocalizationContext(pc, (String) obj);
	}

	return locCtxt;
    }

    /**
     * Gets the resource bundle with the given base name, whose locale is
     * determined as follows:
     *
     * Check if a match exists between the ordered set of preferred
     * locales and the available locales, for the given base name.
     * The set of preferred locales consists of a single locale
     * (if the <tt>javax.servlet.jsp.jstl.fmt.locale</tt> configuration
     * setting is present) or is equal to the client's preferred locales
     * determined from the client's browser settings.
     *
     * <p> If no match was found in the previous step, check if a match
     * exists between the fallback locale (given by the
     * <tt>javax.servlet.jsp.jstl.fmt.fallbackLocale</tt> configuration
     * setting) and the available locales, for the given base name.
     *
     * @param pageContext Page in which the resource bundle with the
     * given base name is requested
     * @param basename Resource bundle base name
     *
     * @return Localization context containing the resource bundle with the
     * given base name and the locale that led to the resource bundle match,
     * or the empty localization context if no resource bundle match was found
     */
    private static LocalizationContext getLocalizationContext(PageContext pc,
                                                              String basename) {
	LocalizationContext locCtxt = null;
	ResourceBundle bundle = null;

	if ((basename == null) || basename.equals("")) {
	    return new LocalizationContext();
	}

	// Try preferred locales
	Locale pref = getLocale(pc, Config.FMT_LOCALE);
	if (pref != null) {
	    // Preferred locale is application-based
	    bundle = findMatch(basename, pref);
	    if (bundle != null) {
		locCtxt = new LocalizationContext(bundle, pref);
	    }
	} else {
	    // Preferred locales are browser-based
	    locCtxt = findMatch(pc, basename);
	}
	
	if (locCtxt == null) {
	    // No match found with preferred locales, try using fallback locale
	    pref = getLocale(pc, Config.FMT_FALLBACK_LOCALE);
	    if (pref != null) {
		bundle = findMatch(basename, pref);
		if (bundle != null) {
		    locCtxt = new LocalizationContext(bundle, pref);
		}
	    }
	}

	if (locCtxt == null) {
	    // try using the root resource bundle with the given basename
	    try {
		bundle = ResourceBundle.getBundle(basename, EMPTY_LOCALE,
						  Thread.currentThread().getContextClassLoader());
		if (bundle != null) {
		    locCtxt = new LocalizationContext(bundle, null);
		}
	    } catch (MissingResourceException mre) {
		// do nothing
	    }
	}
		 
	if (locCtxt != null) {
	    // set response locale
	    if (locCtxt.getLocale() != null) {
		setResponseLocale(pc, locCtxt.getLocale());
	    }
	} else {
	    // create empty localization context
	    locCtxt = new LocalizationContext();
	}

	return locCtxt;
    }

    /*
     * Determines the client's preferred locales from the request, and compares
     * each of the locales (in order of preference) against the available
     * locales in order to determine the best matching locale.
     *
     * @param pageContext the page in which the resource bundle with the
     * given base name is requested
     * @param basename the resource bundle's base name
     *
     * @return the localization context containing the resource bundle with
     * the given base name and best matching locale, or <tt>null</tt> if no
     * resource bundle match was found
     */
    private static LocalizationContext findMatch(PageContext pageContext,
						 String basename) {
	LocalizationContext locCtxt = null;
	
	// Determine locale from client's browser settings.
        
	for (Enumeration enum_ = getRequestLocales(
                            (HttpServletRequest)pageContext.getRequest());
	     enum_.hasMoreElements(); ) {
	    Locale pref = (Locale) enum_.nextElement();
	    ResourceBundle match = findMatch(basename, pref);
	    if (match != null) {
		locCtxt = new LocalizationContext(match, pref);
		break;
	    }
	}
        	
	return locCtxt;
    }

    /*
     * Gets the resource bundle with the given base name and preferred locale.
     * 
     * This method calls java.util.ResourceBundle.getBundle(), but ignores
     * its return value unless its locale represents an exact or language match
     * with the given preferred locale.
     *
     * @param basename the resource bundle base name
     * @param pref the preferred locale
     *
     * @return the requested resource bundle, or <tt>null</tt> if no resource
     * bundle with the given base name exists or if there is no exact- or
     * language-match between the preferred locale and the locale of
     * the bundle returned by java.util.ResourceBundle.getBundle().
     */
    private static ResourceBundle findMatch(String basename, Locale pref) {
	ResourceBundle match = null;

	try {
	    ResourceBundle bundle =
		ResourceBundle.getBundle(basename, pref,
					 Thread.currentThread().getContextClassLoader());
	    Locale avail = bundle.getLocale();
	    if (pref.equals(avail)) {
		// Exact match
		match = bundle;
	    } else {
                /*
                 * We have to make sure that the match we got is for
                 * the specified locale. The way ResourceBundle.getBundle()
                 * works, if a match is not found with (1) the specified locale,
                 * it tries to match with (2) the current default locale as 
                 * returned by Locale.getDefault() or (3) the root resource 
                 * bundle (basename).
                 * We must ignore any match that could have worked with (2) or (3).
                 * So if an exact match is not found, we make the following extra
                 * tests:
                 *     - avail locale must be equal to preferred locale
                 *     - avail country must be empty or equal to preferred country
                 *       (the equality match might have failed on the variant)
		 */
                if (pref.getLanguage().equals(avail.getLanguage())
		    && ("".equals(avail.getCountry()) || pref.getCountry().equals(avail.getCountry()))) {
		    /*
		     * Language match.
		     * By making sure the available locale does not have a 
		     * country and matches the preferred locale's language, we
		     * rule out "matches" based on the container's default
		     * locale. For example, if the preferred locale is 
		     * "en-US", the container's default locale is "en-UK", and
		     * there is a resource bundle (with the requested base
		     * name) available for "en-UK", ResourceBundle.getBundle()
		     * will return it, but even though its language matches
		     * that of the preferred locale, we must ignore it,
		     * because matches based on the container's default locale
		     * are not portable across different containers with
		     * different default locales.
		     */
		    match = bundle;
		}
	    }
	} catch (MissingResourceException mre) {
	}

	return match;
    }

    /*
     * Returns the locale specified by the named scoped attribute or context
     * configuration parameter.
     *
     * <p> The named scoped attribute is searched in the page, request,
     * session (if valid), and application scope(s) (in this order). If no such
     * attribute exists in any of the scopes, the locale is taken from the
     * named context configuration parameter.
     *
     * @param pageContext the page in which to search for the named scoped
     * attribute or context configuration parameter
     * @param name the name of the scoped attribute or context configuration
     * parameter
     *
     * @return the locale specified by the named scoped attribute or context
     * configuration parameter, or <tt>null</tt> if no scoped attribute or
     * configuration parameter with the given name exists
     */
    private static Locale getLocale(PageContext pageContext, String name) {
	Locale loc = null;

	Object obj = Config.find(pageContext, name);
	if (obj != null) {
	    if (obj instanceof Locale) {
		loc = (Locale) obj;
	    } else {
		loc = parseLocale((String) obj);
	    }
	}

	return loc;
    }

    /*
     * Stores the given locale in the response object of the given page
     * context, and stores the locale's associated charset in the
     * javax.servlet.jsp.jstl.fmt.request.charset session attribute, which
     * may be used by the <requestEncoding> action in a page invoked by a
     * form included in the response to set the request charset to the same as
     * the response charset (this makes it possible for the container to
     * decode the form parameter values properly, since browsers typically
     * encode form field values using the response's charset).
     *
     * @param pageContext the page context whose response object is assigned
     * the given locale
     * @param locale the response locale
     */
    private static void setResponseLocale(PageContext pc, Locale locale) {
	// set response locale
	ServletResponse response = pc.getResponse();
	response.setLocale(locale);
	
	// get response character encoding and store it in session attribute
	if (pc.getSession() != null) {
            try {
	        pc.setAttribute(REQUEST_CHAR_SET,
			        response.getCharacterEncoding(),
			        PageContext.SESSION_SCOPE);
            } catch (IllegalStateException ex) {} // invalidated session ignored
	}
    }

    /**
     * See parseLocale(String, String) for details.
     */
    private static Locale parseLocale(String locale) {
	return parseLocale(locale, null);
    }

    /**
     * Parses the given locale string into its language and (optionally)
     * country components, and returns the corresponding
     * <tt>java.util.Locale</tt> object.
     *
     * If the given locale string is null or empty, the runtime's default
     * locale is returned.
     *
     * @param locale the locale string to parse
     * @param variant the variant
     *
     * @return <tt>java.util.Locale</tt> object corresponding to the given
     * locale string, or the runtime's default locale if the locale string is
     * null or empty
     *
     * @throws IllegalArgumentException if the given locale does not have a
     * language component or has an empty country component
     */
    private static Locale parseLocale(String locale, String variant) {

	Locale ret = null;
	String language = locale;
	String country = null;
	int index = -1;

	if (((index = locale.indexOf(HYPHEN)) > -1)
	        || ((index = locale.indexOf(UNDERSCORE)) > -1)) {
	    language = locale.substring(0, index);
	    country = locale.substring(index+1);
	}

	if ((language == null) || (language.length() == 0)) {
	    throw new IllegalArgumentException(
		"Missing language component in 'value' attribute in setLocale");
	}

	if (country == null) {
	    if (variant != null)
		ret = new Locale(language, "", variant);
	    else
		ret = new Locale(language, "");
	} else if (country.length() > 0) {
	    if (variant != null)
		ret = new Locale(language, country, variant);
	    else
		ret = new Locale(language, country);
	} else {
	    throw new IllegalArgumentException(
		"Empty country component in 'value' attribute in setLocale");
	}

	return ret;
    }

    /**
     * HttpServletRequest.getLocales() returns the server's default locale 
     * if the request did not specify a preferred language.
     * We do not want this behavior, because it prevents us from using
     * the fallback locale. 
     * We therefore need to return an empty Enumeration if no preferred 
     * locale has been specified. This way, the logic for the fallback 
     * locale will be able to kick in.
     */
    private static Enumeration getRequestLocales(HttpServletRequest request) {        
        Enumeration values = request.getHeaders("accept-language");
        if (values.hasMoreElements()) {
            // At least one "accept-language". Simply return
            // the enumeration returned by request.getLocales().
            // System.out.println("At least one accept-language");
            return request.getLocales();
        } else {
            // No header for "accept-language". Simply return
            // the empty enumeration.
            // System.out.println("No accept-language");
            return values;
        }
    }

}

