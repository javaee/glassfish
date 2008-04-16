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

package org.apache.taglibs.standard.tlv;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluator;
import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.apache.taglibs.standard.resources.Resources;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A base class to support SAX-based validation in JSTL.</p>
 * 
 * @author Shawn Bayern
 */
public abstract class JstlBaseTLV extends TagLibraryValidator {

    //*********************************************************************
    // Implementation Overview

    /*
     * We essentially just run the page through a SAX parser, handling
     * the callbacks that interest us.  The SAX parser is supplied by
     * subclasses using the protected getHandler() method.
     */

    protected abstract DefaultHandler getHandler();


    //*********************************************************************
    // Constants

    // parameter names
    private final String EXP_ATT_PARAM = "expressionAttributes";

    // attributes
    protected static final String VAR = "var";
    protected static final String SCOPE = "scope";  

    //scopes
    protected static final String PAGE_SCOPE = "page";        
    protected static final String REQUEST_SCOPE = "request";  
    protected static final String SESSION_SCOPE = "session";  
    protected static final String APPLICATION_SCOPE = "application";

    // Relevant URIs
    protected final String JSP = "http://java.sun.com/JSP/Page"; 
    
    // types of sub-classes - used on method validate()
    private   static final int TYPE_UNDEFINED = 0;
    protected static final int TYPE_CORE = 1;
    protected static final int TYPE_FMT = 2;
    protected static final int TYPE_SQL = 3;
    protected static final int TYPE_XML = 4;

    // which tlv is being validated
    private int tlvType = TYPE_UNDEFINED;

    //*********************************************************************
    // Validation and configuration state (protected)

    protected String uri;			// our taglib's uri (as passed by JSP container on XML View)
    protected String prefix;			// our taglib's prefix
    protected Vector messageVector;		// temporary error messages
    protected Map config;			// configuration (Map of Sets)
    protected boolean failed;			// have we failed >0 times?
    protected String lastElementId;		// the last element we've seen

    //*********************************************************************
    // Constructor and lifecycle management

    public JstlBaseTLV() {
	super();
	init();
    }

    private void init() {
	messageVector = null;
	prefix = null;
	config = null;
    }

    public void release() {
	super.release();
	init();
    }
    

    //*********************************************************************
    // Validation entry point - this method is called by the sub-classes to 
    // do the validation.

    public synchronized ValidationMessage[] validate(
	    int type, String prefix, String uri, PageData page) {
	try {
	    this.tlvType = type;
	    this.uri = uri;
	    // initialize
	    messageVector = new Vector();

	    // save the prefix
	    this.prefix = prefix;

	    // parse parameters if necessary
	    try {
		if (config == null)
		    configure((String) getInitParameters().get(EXP_ATT_PARAM));
	    } catch (NoSuchElementException ex) {
		// parsing error
	        return vmFromString(
		    Resources.getMessage("TLV_PARAMETER_ERROR",
			EXP_ATT_PARAM));
	    }

	    // get a handler
	    DefaultHandler h = getHandler();

	    // parse the page
	    SAXParserFactory f = SAXParserFactory.newInstance();
	    f.setValidating(false);
	    f.setNamespaceAware(true);
	    SAXParser p = f.newSAXParser();
	    p.parse(page.getInputStream(), h);

	    if (messageVector.size() == 0)
		return null;
	    else
		return vmFromVector(messageVector);

	} catch (SAXException ex) {
	    return vmFromString(ex.toString());
	} catch (ParserConfigurationException ex) {
	    return vmFromString(ex.toString());
	} catch (IOException ex) {
	    return vmFromString(ex.toString());
	}
    }

    //*********************************************************************
    // Protected utility functions

    // delegate validation to the appropriate expression language
    protected String validateExpression(
	    String elem, String att, String expr) {

	// let's just use the cache kept by the ExpressionEvaluatorManager
	ExpressionEvaluator current;
	try {
	    current =
	        ExpressionEvaluatorManager.getEvaluatorByName(
                  ExpressionEvaluatorManager.EVALUATOR_CLASS);
	} catch (JspException ex) {
	    // (using JspException here feels ugly, but it's what EEM uses)
	    return ex.getMessage();
	}
	
	String response = current.validate(att, expr);
	if (response == null)
	    return response;
	else
	    return "tag = '" + elem + "' / attribute = '" + att + "': "
		+ response;
    }

    // utility methods to help us match elements in our tagset
    protected boolean isTag(String tagUri,
			    String tagLn,
			    String matchUri,
			    String matchLn) {
	if (tagUri == null
                || tagUri.length() == 0
	        || tagLn == null
		|| matchUri == null
		|| matchLn == null)
	    return false;
        // match beginning of URI since some suffix *_rt tags can
        // be nested in EL enabled tags as defined by the spec
        if (tagUri.length() > matchUri.length()) {
            return (tagUri.startsWith(matchUri) && tagLn.equals(matchLn));
        } else {
            return (matchUri.startsWith(tagUri) && tagLn.equals(matchLn));
        }
    }

    protected boolean isJspTag(String tagUri, String tagLn, String target) {
        return isTag(tagUri, tagLn, JSP, target);
    }

    private boolean isTag( int type, String tagUri, String tagLn, String target) {
        return ( this.tlvType == type && isTag(tagUri, tagLn, this.uri, target) );
    }

    protected boolean isCoreTag(String tagUri, String tagLn, String target) {
	return isTag( TYPE_CORE, tagUri, tagLn, target );
    }

    protected boolean isFmtTag(String tagUri, String tagLn, String target) {
	return isTag( TYPE_FMT, tagUri, tagLn, target );
    }

    protected boolean isSqlTag(String tagUri, String tagLn, String target) {
	return isTag( TYPE_SQL, tagUri, tagLn, target );
    }

    protected boolean isXmlTag(String tagUri, String tagLn, String target) {
	return isTag( TYPE_XML, tagUri, tagLn, target );
    }

    // utility method to determine if an attribute exists
    protected boolean hasAttribute(Attributes a, String att) {
        return (a.getValue(att) != null);
    }

    /*
     * method to assist with failure [ as if it's not easy enough
     * already :-) ]
     */
    protected void fail(String message) {
        failed = true;
        messageVector.add(new ValidationMessage(lastElementId, message));
    }

    // returns true if the given attribute name is specified, false otherwise
    protected boolean isSpecified(TagData data, String attributeName) {
        return (data.getAttribute(attributeName) != null);
    }

    // returns true if the 'scope' attribute is valid
    protected boolean hasNoInvalidScope(Attributes a) {
        String scope = a.getValue(SCOPE);

	if ((scope != null)
	    && !scope.equals(PAGE_SCOPE)
	    && !scope.equals(REQUEST_SCOPE)
	    && !scope.equals(SESSION_SCOPE)
	    && !scope.equals(APPLICATION_SCOPE))
	    return false;

        return true;
    }

    // returns true if the 'var' attribute is empty
    protected boolean hasEmptyVar(Attributes a) {
	if ("".equals(a.getValue(VAR)))
	    return true;
	return false;
    }

    // returns true if the 'scope' attribute is present without 'var'
    protected boolean hasDanglingScope(Attributes a) {
	return (a.getValue(SCOPE) != null && a.getValue(VAR) == null);
    }

    // retrieves the local part of a QName
    protected String getLocalPart(String qname) {
	int colon = qname.indexOf(":");
	if (colon == -1)
	    return qname;
	else
	    return qname.substring(colon + 1);
    }

    //*********************************************************************
    // Miscellaneous utility functions

    // parses our configuration parameter for element:attribute pairs
    private void configure(String info) {
        // construct our configuration map
	config = new HashMap();

	// leave the map empty if we have nothing to configure
	if (info == null)
	    return;

	// separate parameter into space-separated tokens and store them
	StringTokenizer st = new StringTokenizer(info);
	while (st.hasMoreTokens()) {
	    String pair = st.nextToken();
	    StringTokenizer pairTokens = new StringTokenizer(pair, ":");
	    String element = pairTokens.nextToken();
	    String attribute = pairTokens.nextToken();
	    Object atts = config.get(element);
	    if (atts == null) {
	        atts = new HashSet();
	        config.put(element, atts);
	    }
	    ((Set) atts).add(attribute);
	}
    }

    // constructs a ValidationMessage[] from a single String and no ID
    static ValidationMessage[] vmFromString(String message) {
	return new ValidationMessage[] {
	    new ValidationMessage(null, message)
	};
    }

    // constructs a ValidationMessage[] from a ValidationMessage Vector
    static ValidationMessage[] vmFromVector(Vector v) {
	ValidationMessage[] vm = new ValidationMessage[v.size()];
	for (int i = 0; i < vm.length; i++)
	   vm[i] = (ValidationMessage) v.get(i);
	return vm;
    }
}
