/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
 */

package org.apache.taglibs.standard.tag.common.fmt;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * Support for tag handlers for &lt;setLocale&gt;, the locale setting tag in
 * JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class SetLocaleSupport extends TagSupport {

    
    //*********************************************************************
    // Private constants

    private static final char HYPHEN = '-';
    private static final char UNDERSCORE = '_';
    private static Locale[] availableFormattingLocales;
    private static final Locale[] dateLocales =
        DateFormat.getAvailableLocales();
    private static final Locale[] numberLocales =
        NumberFormat.getAvailableLocales();


    //*********************************************************************
    // Protected state

    protected Object value;                      // 'value' attribute
    protected String variant;                    // 'variant' attribute


    //*********************************************************************
    // Private state

    private int scope;                           // 'scope' attribute


    //*********************************************************************
    // Constructor and initialization

    /**
     * Sets up the available formatting locales that will be used
     * by getFormattingLocale(PageContext).
     */
    static {
        Vector vec = new Vector(dateLocales.length);
        for (int i=0; i<dateLocales.length; i++) {
            for (int j=0; j<numberLocales.length; j++) {
                if (dateLocales[i].equals(numberLocales[j])) {
                    vec.add(dateLocales[i]);
                    break;
                }
            }
        }
        availableFormattingLocales = new Locale[vec.size()];
        availableFormattingLocales = (Locale[])vec.toArray(availableFormattingLocales);
    }

    public SetLocaleSupport() {
	super();
	init();
    }

    private void init() {
	value = variant = null;
	scope = PageContext.PAGE_SCOPE;
    }


   //*********************************************************************
    // Tag attributes known at translation time

    public void setScope(String scope) {
	this.scope = Util.getScope(scope);
    }


    //*********************************************************************
    // Tag logic

    public int doEndTag() throws JspException {
	Locale locale = null;

	if (value == null) {
	    locale = Locale.getDefault();
	} else if (value instanceof String) {
	    if (((String) value).trim().equals("")) {
		locale = Locale.getDefault();
	    } else {
		locale = parseLocale((String) value, variant);
	    }
	} else {
	    locale = (Locale) value;
	}

	Config.set(pageContext, Config.FMT_LOCALE, locale, scope);
	setResponseLocale(pageContext, locale);

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }


    //*********************************************************************
    // Public utility methods

    /**
     * See parseLocale(String, String) for details.
     */
    public static Locale parseLocale(String locale) {
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
    public static Locale parseLocale(String locale, String variant) {

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
		Resources.getMessage("LOCALE_NO_LANGUAGE"));
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
		Resources.getMessage("LOCALE_EMPTY_COUNTRY"));
	}

	return ret;
    }


    //*********************************************************************
    // Package-scoped utility methods

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
    static void setResponseLocale(PageContext pc, Locale locale) {
	// set response locale
	ServletResponse response = pc.getResponse();
	response.setLocale(locale);
	
	// get response character encoding and store it in session attribute
	if (pc.getSession() != null) {
            try {
	        pc.setAttribute(RequestEncodingSupport.REQUEST_CHAR_SET,
			    response.getCharacterEncoding(),
			    PageContext.SESSION_SCOPE);
            } catch (IllegalStateException ex) {} // invalidated session ignored
	}
    }
 
    /*
     * Returns the formatting locale to use with the given formatting action
     * in the given page.
     *
     * @param pc The page context containing the formatting action
     * @param fromTag The formatting action
     * @param isDate true if the locale is needed for date formatting, false
     * otherwise (i.e., the locale is needed for number formatting)
     * @param format <tt>true</tt> if the formatting action is of type
     * <formatXXX> (as opposed to <parseXXX>), and <tt>false</tt> otherwise
     * (if set to <tt>true</tt>, the formatting locale that is returned by
     * this method is used to set the response locale).
     *
     * @return the formatting locale to use
     */
    static Locale getFormattingLocale(PageContext pc,
				      Tag fromTag,
                                      boolean isDate,
				      boolean format) {

	LocalizationContext locCtxt = null;
	
	// Get formatting locale from enclosing <fmt:bundle>
	Tag parent = findAncestorWithClass(fromTag, BundleSupport.class);
	if (parent != null) {
	    /*
	     * use locale from localization context established by parent
	     * <fmt:bundle> action, unless that locale is null
	     */
	    locCtxt = ((BundleSupport) parent).getLocalizationContext();
	    if (locCtxt.getLocale() != null) {
		if (format) {
		    setResponseLocale(pc, locCtxt.getLocale());
		}
		return locCtxt.getLocale();
	    }
	}

	// Use locale from default I18N localization context, unless it is null
	if ((locCtxt = BundleSupport.getLocalizationContext(pc)) != null) {
	    if (locCtxt.getLocale() != null) {
		if (format) {
		    setResponseLocale(pc, locCtxt.getLocale());
		}
		return locCtxt.getLocale();
	    }
	}

	/*
	 * Establish formatting locale by comparing the preferred locales
	 * (in order of preference) against the available formatting
	 * locales, and determining the best matching locale.
	 */
	Locale match = null;
	Locale pref = getLocale(pc, Config.FMT_LOCALE);
        Locale[] avail = null;
        if (isDate) {
            avail = dateLocales;
        } else {
            avail = numberLocales;
        }
	if (pref != null) {
	    // Preferred locale is application-based
	    match = findFormattingMatch(pref, avail);
	} else {
	    // Preferred locales are browser-based 
	    match = findFormattingMatch(pc, avail);
	}
	if (match == null) {
	    //Use fallback locale.
	    pref = getLocale(pc, Config.FMT_FALLBACK_LOCALE);
	    if (pref != null) {
		match = findFormattingMatch(pref, avail);
	    }
	}
 	if (format && (match != null)) {
	    setResponseLocale(pc, match);
	}

	return match;
    }
    
    /*
     * Returns the formatting locale to use when <fmt:message> is used
     * with a locale-less localization context.
     *
     * @param pc The page context containing the formatting action
     * @return the formatting locale to use
     */
    static Locale getFormattingLocale(PageContext pc) {
	/*
	 * Establish formatting locale by comparing the preferred locales
	 * (in order of preference) against the available formatting
	 * locales, and determining the best matching locale.
	 */
	Locale match = null;
	Locale pref = getLocale(pc, Config.FMT_LOCALE);
	if (pref != null) {
	    // Preferred locale is application-based
	    match = findFormattingMatch(pref, availableFormattingLocales);
	} else {
	    // Preferred locales are browser-based 
	    match = findFormattingMatch(pc, availableFormattingLocales);
	}
	if (match == null) {
	    //Use fallback locale.
	    pref = getLocale(pc, Config.FMT_FALLBACK_LOCALE);
	    if (pref != null) {
		match = findFormattingMatch(pref, availableFormattingLocales);
	    }
	}
 	if (match != null) {
	    setResponseLocale(pc, match);
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
    static Locale getLocale(PageContext pageContext, String name) {
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


    //*********************************************************************
    // Private utility methods

    /*
     * Determines the client's preferred locales from the request, and compares
     * each of the locales (in order of preference) against the available
     * locales in order to determine the best matching locale.
     *
     * @param pageContext Page containing the formatting action
     * @param avail Available formatting locales
     *
     * @return Best matching locale, or <tt>null</tt> if no match was found
     */
    private static Locale findFormattingMatch(PageContext pageContext,
					      Locale[] avail) {
	Locale match = null;
	for (Enumeration enum_ = Util.getRequestLocales((HttpServletRequest)pageContext.getRequest());
	     enum_.hasMoreElements(); ) {
            Locale locale = (Locale)enum_.nextElement();
	    match = findFormattingMatch(locale, avail);
	    if (match != null) {
		break;
	    }
	}
	
	return match;
    }

    /*
     * Returns the best match between the given preferred locale and the
     * given available locales.
     *
     * The best match is given as the first available locale that exactly
     * matches the given preferred locale ("exact match"). If no exact match
     * exists, the best match is given to an available locale that meets
     * the following criteria (in order of priority):
     *  - available locale's variant is empty and exact match for both
     *    language and country
     *  - available locale's variant and country are empty, and exact match 
     *    for language.
     *
     * @param pref the preferred locale
     * @param avail the available formatting locales
     *
     * @return Available locale that best matches the given preferred locale,
     * or <tt>null</tt> if no match exists
     */
    private static Locale findFormattingMatch(Locale pref, Locale[] avail) {
	Locale match = null;
        boolean langAndCountryMatch = false;
        for (int i=0; i<avail.length; i++) {
            if (pref.equals(avail[i])) {
                // Exact match
                match = avail[i];
                break;
            } else if (
                    !"".equals(pref.getVariant()) &&
                    "".equals(avail[i].getVariant()) &&
                    pref.getLanguage().equals(avail[i].getLanguage()) &&
                    pref.getCountry().equals(avail[i].getCountry())) {
                // Language and country match; different variant
                match = avail[i];
                langAndCountryMatch = true;
            } else if (
                    !langAndCountryMatch &&
                    pref.getLanguage().equals(avail[i].getLanguage()) &&
                    ("".equals(avail[i].getCountry()))) {
                // Language match
                if (match == null) {
                    match = avail[i];
                }
            }
        }
	return match;
    }
}
