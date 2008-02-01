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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * Support for tag handlers for &lt;parseDate&gt;, the date and time
 * parsing tag in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class ParseDateSupport extends BodyTagSupport {

    //*********************************************************************
    // Private constants

    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String DATETIME = "both";


    //*********************************************************************
    // Protected state

    protected String value;                      // 'value' attribute
    protected boolean valueSpecified;	         // status
    protected String type;                       // 'type' attribute
    protected String pattern;                    // 'pattern' attribute
    protected Object timeZone;                   // 'timeZone' attribute
    protected Locale parseLocale;                // 'parseLocale' attribute
    protected String dateStyle;                  // 'dateStyle' attribute
    protected String timeStyle;                  // 'timeStyle' attribute


    //*********************************************************************
    // Private state

    private String var;                          // 'var' attribute
    private int scope;                           // 'scope' attribute


    //*********************************************************************
    // Constructor and initialization

    public ParseDateSupport() {
	super();
	init();
    }

    private void init() {
	type = dateStyle = timeStyle = null;
	value = pattern = var = null;
	valueSpecified = false;
	timeZone = null;
	scope = PageContext.PAGE_SCOPE;
	parseLocale = null;
    }


   //*********************************************************************
    // Tag attributes known at translation time

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
	this.scope = Util.getScope(scope);
    }


    //*********************************************************************
    // Tag logic

    public int doEndTag() throws JspException {

        String input = null;

        // determine the input by...
        if (valueSpecified) {
	    // ... reading 'value' attribute
	    input = value;
	} else {
	    // ... retrieving and trimming our body
	    if (bodyContent != null && bodyContent.getString() != null)
	        input = bodyContent.getString().trim();
	}

	if ((input == null) || input.equals("")) {
	    if (var != null) {
		pageContext.removeAttribute(var, scope);
	    }
	    return EVAL_PAGE;
	}

	/*
	 * Set up parsing locale: Use locale specified via the 'parseLocale'
	 * attribute (if present), or else determine page's locale.
	 */
	Locale locale = parseLocale;
	if (locale == null)
	    locale = SetLocaleSupport.getFormattingLocale(pageContext,
                                                          this,
                                                          true,
                                                          false);
	if (locale == null) {
	    throw new JspException(
                    Resources.getMessage("PARSE_DATE_NO_PARSE_LOCALE"));
	}

	// Create parser
	DateFormat parser = createParser(locale);

	// Apply pattern, if present
	if (pattern != null) {
	    try {
		((SimpleDateFormat) parser).applyPattern(pattern);
	    } catch (ClassCastException cce) {
		parser = new SimpleDateFormat(pattern, locale);
	    }
	}

	// Set time zone
	TimeZone tz = null;
	if ((timeZone instanceof String) && ((String) timeZone).equals("")) {
	    timeZone = null;
	}
	if (timeZone != null) {
	    if (timeZone instanceof String) {
		tz = TimeZone.getTimeZone((String) timeZone);
	    } else if (timeZone instanceof TimeZone) {
		tz = (TimeZone) timeZone;
	    } else {
		throw new JspException(
                    Resources.getMessage("PARSE_DATE_BAD_TIMEZONE"));
	    }
	} else {
	    tz = TimeZoneSupport.getTimeZone(pageContext, this);
	}
	if (tz != null) {
	    parser.setTimeZone(tz);
	}

	// Parse date
	Date parsed = null;
	try {
	    parsed = parser.parse(input);
	} catch (ParseException pe) {
	    throw new JspException(
	            Resources.getMessage("PARSE_DATE_PARSE_ERROR", input),
		    pe);
	}

	if (var != null) {
	    pageContext.setAttribute(var, parsed, scope);	
	} else {
	    try {
		pageContext.getOut().print(parsed);
	    } catch (IOException ioe) {
		throw new JspTagException(ioe.toString(), ioe);
	    }
	}

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }


    //*********************************************************************
    // Private utility methods

    private DateFormat createParser(Locale loc) throws JspException {
	DateFormat parser = null;

	if ((type == null) || DATE.equalsIgnoreCase(type)) {
	    parser = DateFormat.getDateInstance(
	        Util.getStyle(dateStyle, "PARSE_DATE_INVALID_DATE_STYLE"),
		loc);
	} else if (TIME.equalsIgnoreCase(type)) {
	    parser = DateFormat.getTimeInstance(
	        Util.getStyle(timeStyle, "PARSE_DATE_INVALID_TIME_STYLE"),
		loc);
	} else if (DATETIME.equalsIgnoreCase(type)) {
	    parser = DateFormat.getDateTimeInstance(
	        Util.getStyle(dateStyle, "PARSE_DATE_INVALID_DATE_STYLE"),
		Util.getStyle(timeStyle, "PARSE_DATE_INVALID_TIME_STYLE"),
		loc);
	} else {
	    throw new JspException(
                    Resources.getMessage("PARSE_DATE_INVALID_TYPE", type));
	}

	parser.setLenient(false);

	return parser;
    }
}
