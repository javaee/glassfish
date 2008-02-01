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
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * Support for tag handlers for &lt;formatNumber&gt;, the number
 * formatting tag in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class FormatNumberSupport extends BodyTagSupport {

    //*********************************************************************
    // Private constants

    private static final Class[] GET_INSTANCE_PARAM_TYPES =
	new Class[] { String.class };
    private static final String NUMBER = "number";    
    private static final String CURRENCY = "currency";
    private static final String PERCENT = "percent";


    //*********************************************************************
    // Protected state

    protected Object value;                    // 'value' attribute
    protected boolean valueSpecified;	       // status
    protected String type;                     // 'type' attribute
    protected String pattern;                  // 'pattern' attribute
    protected String currencyCode;             // 'currencyCode' attribute
    protected String currencySymbol;           // 'currencySymbol' attribute
    protected boolean isGroupingUsed;          // 'groupingUsed' attribute
    protected boolean groupingUsedSpecified;
    protected int maxIntegerDigits;            // 'maxIntegerDigits' attribute
    protected boolean maxIntegerDigitsSpecified;
    protected int minIntegerDigits;            // 'minIntegerDigits' attribute
    protected boolean minIntegerDigitsSpecified;
    protected int maxFractionDigits;           // 'maxFractionDigits' attribute
    protected boolean maxFractionDigitsSpecified;
    protected int minFractionDigits;           // 'minFractionDigits' attribute
    protected boolean minFractionDigitsSpecified;


    //*********************************************************************
    // Private state

    private String var;                        // 'var' attribute
    private int scope;                         // 'scope' attribute
    private static Class currencyClass;


    //*********************************************************************
    // Constructor and initialization

    static {
	try {
	    currencyClass = Class.forName("java.util.Currency");
	    // container's runtime is J2SE 1.4 or greater
	} catch (Exception cnfe) {
	}
    }

    public FormatNumberSupport() {
	super();
	init();
    }

    private void init() {
	value = type = null;
	valueSpecified = false;
	pattern = var = currencyCode = currencySymbol = null;
	groupingUsedSpecified = false;
	maxIntegerDigitsSpecified = minIntegerDigitsSpecified = false;
	maxFractionDigitsSpecified = minFractionDigitsSpecified = false;
	scope = PageContext.PAGE_SCOPE;
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
	String formatted = null;
        Object input = null;

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
	    // Spec says:
            // If value is null or empty, remove the scoped variable 
            // if it is specified (see attributes var and scope).
	    if (var != null) {
	        pageContext.removeAttribute(var, scope);
            }
	    return EVAL_PAGE;
	}

	/*
	 * If 'value' is a String, it is first parsed into an instance of
	 * java.lang.Number
	 */
	if (input instanceof String) {
	    try {
		if (((String) input).indexOf('.') != -1) {
		    input = Double.valueOf((String) input);
		} else {
		    input = Long.valueOf((String) input);
		}
	    } catch (NumberFormatException nfe) {
		throw new JspException(
                    Resources.getMessage("FORMAT_NUMBER_PARSE_ERROR", input),
		    nfe);
	    }
	}

	// Determine formatting locale
	Locale loc = SetLocaleSupport.getFormattingLocale(pageContext,
                                                          this,
                                                          false,
                                                          true);
	if (loc != null) {
	    // Create formatter 
	    NumberFormat formatter = null;
	    if ((pattern != null) && !pattern.equals("")) {
		// if 'pattern' is specified, 'type' is ignored
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
		formatter = new DecimalFormat(pattern, symbols);
	    } else {
		formatter = createFormatter(loc);
	    }
	    if (((pattern != null) && !pattern.equals(""))
		    || CURRENCY.equalsIgnoreCase(type)) {
		try {
		    setCurrency(formatter);
		} catch (Exception e) {
		    throw new JspException(
                        Resources.getMessage("FORMAT_NUMBER_CURRENCY_ERROR"),
			e);
		}
	    }
	    configureFormatter(formatter);
	    formatted = formatter.format(input);
	} else {
	    // no formatting locale available, use toString()
	    formatted = input.toString();
	}

	if (var != null) {
	    pageContext.setAttribute(var, formatted, scope);	
	} else {
	    try {
		pageContext.getOut().print(formatted);
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

    private NumberFormat createFormatter(Locale loc) throws JspException {
	NumberFormat formatter = null;
	
	if ((type == null) || NUMBER.equalsIgnoreCase(type)) {
	    formatter = NumberFormat.getNumberInstance(loc);
	} else if (CURRENCY.equalsIgnoreCase(type)) {
	    formatter = NumberFormat.getCurrencyInstance(loc);
	} else if (PERCENT.equalsIgnoreCase(type)) {
	    formatter = NumberFormat.getPercentInstance(loc);
	} else {
	    throw new JspException(
	        Resources.getMessage("FORMAT_NUMBER_INVALID_TYPE", type));
	}
	
	return formatter;
    }

    /*
     * Applies the 'groupingUsed', 'maxIntegerDigits', 'minIntegerDigits',
     * 'maxFractionDigits', and 'minFractionDigits' attributes to the given
     * formatter.
     */
    private void configureFormatter(NumberFormat formatter) {
	if (groupingUsedSpecified)
	    formatter.setGroupingUsed(isGroupingUsed);
	if (maxIntegerDigitsSpecified)
	    formatter.setMaximumIntegerDigits(maxIntegerDigits);
	if (minIntegerDigitsSpecified)
	    formatter.setMinimumIntegerDigits(minIntegerDigits);
	if (maxFractionDigitsSpecified)
	    formatter.setMaximumFractionDigits(maxFractionDigits);
	if (minFractionDigitsSpecified)
	    formatter.setMinimumFractionDigits(minFractionDigits);
    }

    /*
     * Override the formatting locale's default currency symbol with the
     * specified currency code (specified via the "currencyCode" attribute) or
     * currency symbol (specified via the "currencySymbol" attribute).
     *
     * If both "currencyCode" and "currencySymbol" are present,
     * "currencyCode" takes precedence over "currencySymbol" if the
     * java.util.Currency class is defined in the container's runtime (that
     * is, if the container's runtime is J2SE 1.4 or greater), and
     * "currencySymbol" takes precendence over "currencyCode" otherwise.
     *
     * If only "currencyCode" is given, it is used as a currency symbol if
     * java.util.Currency is not defined.
     *
     * Example:
     *
     * JDK    "currencyCode" "currencySymbol" Currency symbol being displayed
     * -----------------------------------------------------------------------
     * all         ---            ---         Locale's default currency symbol
     *
     * <1.4        EUR            ---         EUR
     * >=1.4       EUR            ---         Locale's currency symbol for Euro
     *
     * all         ---           \u20AC       \u20AC
     * 
     * <1.4        EUR           \u20AC       \u20AC
     * >=1.4       EUR           \u20AC       Locale's currency symbol for Euro
     */
    private void setCurrency(NumberFormat formatter) throws Exception {
	String code = null;
	String symbol = null;

	if ((currencyCode == null) && (currencySymbol == null)) {
	    return;
	}

	if ((currencyCode != null) && (currencySymbol != null)) {
	    if (currencyClass != null)
		code = currencyCode;
	    else
		symbol = currencySymbol;
	} else if (currencyCode == null) {
	    symbol = currencySymbol;
	} else {
	    if (currencyClass != null)
		code = currencyCode;
	    else
		symbol = currencyCode;
	}

	if (code != null) {
	    Object[] methodArgs = new Object[1];

	    /*
	     * java.util.Currency.getInstance()
	     */
	    Method m = currencyClass.getMethod("getInstance",
					       GET_INSTANCE_PARAM_TYPES);
	    methodArgs[0] = code;
	    Object currency = m.invoke(null, methodArgs);

	    /*
	     * java.text.NumberFormat.setCurrency()
	     */
	    Class[] paramTypes = new Class[1];
	    paramTypes[0] = currencyClass;
	    Class numberFormatClass = Class.forName("java.text.NumberFormat");
	    m = numberFormatClass.getMethod("setCurrency", paramTypes);
	    methodArgs[0] = currency;
	    m.invoke(formatter, methodArgs);
	} else {
	    /*
	     * Let potential ClassCastException propagate up (will almost
	     * never happen)
	     */
	    DecimalFormat df = (DecimalFormat) formatter;
	    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
	    dfs.setCurrencySymbol(symbol);
	    df.setDecimalFormatSymbols(dfs);
	}
    }
}
