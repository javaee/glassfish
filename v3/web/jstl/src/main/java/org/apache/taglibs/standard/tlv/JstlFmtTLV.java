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

import java.util.Set;
import java.util.Stack;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.ValidationMessage;

import org.apache.taglibs.standard.resources.Resources;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A SAX-based TagLibraryValidator for the JSTL i18n-capable formatting
 * library. Currently implements the following checks:</p>
 * 
 * <ul>
 *   <li>Expression syntax validation.
 *   <li>Tag bodies that must either be empty or non-empty given
 *      particular attributes.</li>
 * </ul>
 * 
 * @author Shawn Bayern
 * @author Jan Luehe
 */
public class JstlFmtTLV extends JstlBaseTLV {

    //*********************************************************************
    // Implementation Overview

    /*
     * We essentially just run the page through a SAX parser, handling
     * the callbacks that interest us.  We collapse <jsp:text> elements
     * into the text they contain, since this simplifies processing
     * somewhat.  Even a quick glance at the implementation shows its
     * necessary, tree-oriented nature:  multiple Stacks, an understanding
     * of 'depth', and so on all are important as we recover necessary
     * state upon each callback.  This TLV demonstrates various techniques,
     * from the general "how do I use a SAX parser for a TLV?" to
     * "how do I read my init parameters and then validate?"  But also,
     * the specific SAX methodology was kept as general as possible to
     * allow for experimentation and flexibility.
     *
     * Much of the code and structure is duplicated from JstlCoreTLV.
     * An effort has been made to re-use code where unambiguously useful.
     * However, splitting logic among parent/child classes isn't
     * necessarily the cleanest approach when writing a parser like the
     * one we need.  I'd like to reorganize this somewhat, but it's not
     * a priority.
     */


    //*********************************************************************
    // Constants

    // tag names
    private final String SETLOCALE = "setLocale";
    private final String SETBUNDLE = "setBundle";
    private final String SETTIMEZONE = "setTimeZone";
    private final String BUNDLE = "bundle";
    private final String MESSAGE = "message";
    private final String MESSAGE_PARAM = "param";
    private final String FORMAT_NUMBER = "formatNumber";
    private final String PARSE_NUMBER = "parseNumber";
    private final String PARSE_DATE = "parseDate";
    // private final String EXPLANG = "expressionLanguage";
    private final String JSP_TEXT = "jsp:text";

    // attribute names
    private final String EVAL = "evaluator";
    private final String MESSAGE_KEY = "key";
    private final String BUNDLE_PREFIX = "prefix";
    private final String VALUE = "value";


    //*********************************************************************
    // set its type and delegate validation to super-class
    public  ValidationMessage[] validate(
	    String prefix, String uri, PageData page) {
	return super.validate( TYPE_FMT, prefix, uri, page );
    }


    //*********************************************************************
    // Contract fulfillment

    protected DefaultHandler getHandler() {
	return new Handler();
    }


    //*********************************************************************
    // SAX event handler

    /** The handler that provides the base of our implementation. */
    private class Handler extends DefaultHandler {

	// parser state
	private int depth = 0;
	private Stack messageDepths = new Stack();
	private String lastElementName = null;
	private boolean bodyNecessary = false;
	private boolean bodyIllegal = false;

	// process under the existing context (state), then modify it
	public void startElement(
	        String ns, String ln, String qn, Attributes a) {

            // substitute our own parsed 'ln' if it's not provided
            if (ln == null)
                ln = getLocalPart(qn);

	    // for simplicity, we can ignore <jsp:text> for our purposes
	    // (don't bother distinguishing between it and its characters)
	    if (qn.equals(JSP_TEXT))
		return;

	    // check body-related constraint
	    if (bodyIllegal)
		fail(Resources.getMessage("TLV_ILLEGAL_BODY",
					  lastElementName));

            // validate expression syntax if we need to
            Set expAtts;
            if (qn.startsWith(prefix + ":")
                    && (expAtts = (Set) config.get(ln)) != null) {
                for (int i = 0; i < a.getLength(); i++) {
                    String attName = a.getLocalName(i);
                    if (expAtts.contains(attName)) {
                        String vMsg =
                            validateExpression(
                                ln,
                                attName,
                                a.getValue(i));
                        if (vMsg != null)
                            fail(vMsg);
                    }
                }
            }

            // validate attributes
            if (qn.startsWith(prefix + ":") && !hasNoInvalidScope(a))
                fail(Resources.getMessage("TLV_INVALID_ATTRIBUTE",
                    SCOPE, qn, a.getValue(SCOPE)));
	    if (qn.startsWith(prefix + ":") && hasEmptyVar(a))
		fail(Resources.getMessage("TLV_EMPTY_VAR", qn));
            if (qn.startsWith(prefix + ":")
                && !isFmtTag(ns, ln, SETLOCALE) 
		&& !isFmtTag(ns, ln, SETBUNDLE)
		&& !isFmtTag(ns, ln, SETTIMEZONE)
                && hasDanglingScope(a))
                fail(Resources.getMessage("TLV_DANGLING_SCOPE", qn));

	    /*
	     * Make sure <fmt:param> is nested inside <fmt:message>. Note that
	     * <fmt:param> does not need to be a direct child of <fmt:message>.
	     * Otherwise, the following would not work:
	     *
	     *  <fmt:message key="..." bundle="...">
	     *   <c:forEach var="arg" items="...">
	     *    <fmt:param value="${arg}"/>
	     *   </c:forEach>
	     *  </fmt:message>
	     */
	    if (isFmtTag(ns, ln, MESSAGE_PARAM) && messageDepths.empty()) {
		fail(Resources.getMessage("PARAM_OUTSIDE_MESSAGE"));
	    }

	    // now, modify state

	    // If we're in a <message>, record relevant state
	    if (isFmtTag(ns, ln, MESSAGE)) {
                messageDepths.push(Integer.valueOf(depth));
            }

	    // set up a check against illegal attribute/body combinations
	    bodyIllegal = false;
	    bodyNecessary = false;
	    if (isFmtTag(ns, ln, MESSAGE_PARAM)
		    || isFmtTag(ns, ln, FORMAT_NUMBER)
		    || isFmtTag(ns, ln, PARSE_NUMBER)
		    || isFmtTag(ns, ln,  PARSE_DATE)) {
		if (hasAttribute(a, VALUE))
		    bodyIllegal = true;
		else
		    bodyNecessary = true;
	    } else if (isFmtTag(ns, ln, MESSAGE)
		    && !hasAttribute(a, MESSAGE_KEY)) {
		bodyNecessary = true;
	    } else if (isFmtTag(ns, ln, BUNDLE)
		    && hasAttribute(a, BUNDLE_PREFIX)) {
		bodyNecessary = true;
	    }

	    // record the most recent tag (for error reporting)
	    lastElementName = qn;
            lastElementId = a.getValue(JSP, "id");

	    // we're a new element, so increase depth
	    depth++;
	}

	public void characters(char[] ch, int start, int length) {

	    bodyNecessary = false;		// body is no longer necessary!

	    // ignore strings that are just whitespace
	    String s = new String(ch, start, length).trim();
	    if (s.equals(""))
		return;

	    // check and update body-related constraints
	    if (bodyIllegal)
		fail(Resources.getMessage("TLV_ILLEGAL_BODY",
					  lastElementName));
	}

	public void endElement(String ns, String ln, String qn) {

	    // consistently, we ignore JSP_TEXT
	    if (qn.equals(JSP_TEXT))
		return;

	    // handle body-related invariant
	    if (bodyNecessary)
		fail(Resources.getMessage("TLV_MISSING_BODY",
		    lastElementName));
	    bodyIllegal = false;	// reset: we've left the tag

	    // update <message>-related state
	    if (isFmtTag(ns, ln, MESSAGE)) {
		messageDepths.pop();
	    }

	    // update our depth
	    depth--;
	}
    }
}
