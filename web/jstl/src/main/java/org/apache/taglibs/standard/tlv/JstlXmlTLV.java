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
 * <p>A SAX-based TagLibraryValidator for the JSTL XML library.
 * Currently implements the following checks:</p>
 * 
 * <ul>
 *   <li>Expression syntax validation.
 *   <li>Choose / when / otherwise constraints</li>
 *   <li>Tag bodies that must either be empty or non-empty given
 *      particular attributes.</li>
 *   <li>Other minor constraints.</li>
 * </ul>
 * 
 * @author Shawn Bayern
 */
public class JstlXmlTLV extends JstlBaseTLV {

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
    private final String CHOOSE = "choose";
    private final String WHEN = "when";
    private final String OTHERWISE = "otherwise";
    private final String PARSE = "parse";
    private final String PARAM = "param";
    private final String TRANSFORM = "transform";
    private final String JSP_TEXT = "jsp:text";

    // attribute names
    private final String VALUE = "value";
    private final String SOURCE = "xml";


    //*********************************************************************
    // set its type and delegate validation to super-class
    public  ValidationMessage[] validate(
	    String prefix, String uri, PageData page) {
	return super.validate( TYPE_XML, prefix, uri, page );
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
	private Stack chooseDepths = new Stack();
	private Stack chooseHasOtherwise = new Stack();
        private Stack chooseHasWhen = new Stack();
	private String lastElementName = null;
	private boolean bodyNecessary = false;
	private boolean bodyIllegal = false;
	private Stack transformWithSource = new Stack();

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
		fail(Resources.getMessage("TLV_ILLEGAL_BODY", lastElementName));

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
            if (qn.startsWith(prefix + ":") && hasDanglingScope(a))
                fail(Resources.getMessage("TLV_DANGLING_SCOPE", qn));

	    // check invariants for <choose>
	    if (chooseChild()) {
                // mark <choose> for the first the first <when>
                if (isXmlTag(ns, ln, WHEN)) {
                    chooseHasWhen.pop();
                    chooseHasWhen.push(Boolean.TRUE);
                }

		// ensure <choose> has the right children
		if(!isXmlTag(ns, ln, WHEN) && !isXmlTag(ns, ln, OTHERWISE)) {
		    fail(Resources.getMessage("TLV_ILLEGAL_CHILD_TAG",
			prefix, CHOOSE, qn));
		}

		// make sure <otherwise> is the last tag
		if (((Boolean) chooseHasOtherwise.peek()).booleanValue()) {
		   fail(Resources.getMessage("TLV_ILLEGAL_ORDER",
			qn, prefix, OTHERWISE, CHOOSE));
		}
		if (isXmlTag(ns, ln, OTHERWISE)) {
		    chooseHasOtherwise.pop();
		    chooseHasOtherwise.push(Boolean.TRUE);
		}

	    }

	    // Specific check, directly inside <transform source="...">
	    if (!transformWithSource.empty() &&
		    topDepth(transformWithSource) == (depth - 1)) {
		// only allow <param>
		if (!isXmlTag(ns, ln, PARAM))
		    fail(Resources.getMessage("TLV_ILLEGAL_BODY",
			prefix + ":" + TRANSFORM));

		// thus, if we get the opportunity to hit depth++,
		// we know we've got a <param> subtag
	    }

	    // now, modify state

	    // we're a choose, so record new choose-specific state
	    if (isXmlTag(ns, ln, CHOOSE)) {
		chooseDepths.push(Integer.valueOf(depth));
                chooseHasWhen.push(Boolean.FALSE);
		chooseHasOtherwise.push(Boolean.FALSE);
	    }

	    // set up a check against illegal attribute/body combinations
	    bodyIllegal = false;
	    bodyNecessary = false;
	    if (isXmlTag(ns, ln, PARSE)) {
		if (hasAttribute(a, SOURCE))
		    bodyIllegal = true;
	    } else if (isXmlTag(ns, ln, PARAM)) {
		if (hasAttribute(a, VALUE))
		    bodyIllegal = true;
		else
		    bodyNecessary = true;
	    } else if (isXmlTag(ns, ln, TRANSFORM)) {
		if (hasAttribute(a, SOURCE))
		    transformWithSource.push(Integer.valueOf(depth));
	    }

	    // record the most recent tag (for error reporting)
	    lastElementName = qn;
            lastElementId = a.getValue("http://java.sun.com/JSP/Page", "id");

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
		fail(Resources.getMessage("TLV_ILLEGAL_BODY", lastElementName));

	    // make sure <choose> has no non-whitespace text
	    if (chooseChild()) {
		String msg = 
		    Resources.getMessage("TLV_ILLEGAL_TEXT_BODY",
			prefix, CHOOSE,
			(s.length() < 7 ? s : s.substring(0,7)));
		fail(msg);
	    }

            // Specific check, directly inside <transform source="...">
            if (!transformWithSource.empty()
		    && topDepth(transformWithSource) == (depth - 1)) {
                fail(Resources.getMessage("TLV_ILLEGAL_BODY",
                    prefix + ":" + TRANSFORM));
            }
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

	    // update <choose>-related state
	    if (isXmlTag(ns, ln, CHOOSE)) {
                Boolean b = (Boolean) chooseHasWhen.pop();
                if (!b.booleanValue())
                    fail(Resources.getMessage("TLV_PARENT_WITHOUT_SUBTAG",
                        CHOOSE, WHEN));
		chooseDepths.pop();
		chooseHasOtherwise.pop();
	    }

	    // update <transform source="...">-related state
	    if (!transformWithSource.empty()
		    && topDepth(transformWithSource) == (depth - 1))
		transformWithSource.pop();

	    // update our depth
	    depth--;
	}

	// are we directly under a <choose>?
	private boolean chooseChild() {
	    return (!chooseDepths.empty()
		&& (depth - 1) == ((Integer) chooseDepths.peek()).intValue());
	}

        // returns the top int depth (peeked at) from a Stack of Integer
        private int topDepth(Stack s) {
            return ((Integer) s.peek()).intValue();
        }
    }
}
