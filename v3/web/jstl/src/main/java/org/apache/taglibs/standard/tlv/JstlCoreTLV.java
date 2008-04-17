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

package org.apache.taglibs.standard.tlv;

import java.util.Set;
import java.util.Stack;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.ValidationMessage;

import org.apache.taglibs.standard.resources.Resources;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A SAX-based TagLibraryValidator for the core JSTL tag library.
 * Currently implements the following checks:</p>
 * 
 * <ul>
 *   <li>Expression syntax validation.
 *   <li>Choose / when / otherwise constraints</li>
 *   <li>Tag bodies that must either be empty or non-empty given
 *      particular attributes.  (E.g., <set> cannot have a body when
 *      'value' is specified; it *must* have a body otherwise.)  For
 *      these purposes, "having a body" refers to non-whitespace
 *      content inside the tag.</li>
 *   <li>Other minor constraints.</li>
 * </ul>
 * 
 * @author Shawn Bayern
 */
public class JstlCoreTLV extends JstlBaseTLV {

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
     */


    //*********************************************************************
    // Constants

    // tag names
    private final String CHOOSE = "choose";
    private final String WHEN = "when";
    private final String OTHERWISE = "otherwise";
    private final String EXPR = "out";
    private final String SET = "set";
    private final String IMPORT = "import";
    private final String URL = "url";
    private final String REDIRECT = "redirect";
    private final String PARAM = "param";
    // private final String EXPLANG = "expressionLanguage";
    private final String TEXT = "text";

    // attribute names
    private final String VALUE = "value";
    private final String DEFAULT = "default";
    private final String VAR_READER = "varReader";

    // alternative identifiers for tags
    private final String IMPORT_WITH_READER = "import varReader=''";
    private final String IMPORT_WITHOUT_READER = "import var=''";


    //*********************************************************************
    // set its type and delegate validation to super-class
    public  ValidationMessage[] validate(
	    String prefix, String uri, PageData page) {
	return super.validate( TYPE_CORE, prefix, uri, page );
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
        private Stack urlTags = new Stack();
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
	    if (isJspTag(ns, ln, TEXT))
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
		if (isCoreTag(ns, ln, WHEN)) {
		    chooseHasWhen.pop();
		    chooseHasWhen.push(Boolean.TRUE);
		}

		// ensure <choose> has the right children
		if(!isCoreTag(ns, ln, WHEN) && !isCoreTag(ns, ln, OTHERWISE)) {
		    fail(Resources.getMessage("TLV_ILLEGAL_CHILD_TAG",
			prefix, CHOOSE, qn));
		}

		// make sure <otherwise> is the last tag
		if (((Boolean) chooseHasOtherwise.peek()).booleanValue()) {
		   fail(Resources.getMessage("TLV_ILLEGAL_ORDER",
			qn, prefix, OTHERWISE, CHOOSE));
		}
		if (isCoreTag(ns, ln, OTHERWISE)) {
		    chooseHasOtherwise.pop();
		    chooseHasOtherwise.push(Boolean.TRUE);
		}

	    }

	    // check constraints for <param> vis-a-vis URL-related tags
	    if (isCoreTag(ns, ln, PARAM)) {
		// no <param> outside URL tags.
		if (urlTags.empty() || urlTags.peek().equals(PARAM))
		    fail(Resources.getMessage("TLV_ILLEGAL_ORPHAN", PARAM));

		// no <param> where the most recent <import> has a reader
		if (!urlTags.empty() &&
			urlTags.peek().equals(IMPORT_WITH_READER))
		    fail(Resources.getMessage("TLV_ILLEGAL_PARAM",
			prefix, PARAM, IMPORT, VAR_READER));
	    } else {
		// tag ISN'T <param>, so it's illegal under non-reader <import>
		if (!urlTags.empty()
			&& urlTags.peek().equals(IMPORT_WITHOUT_READER))
		    fail(Resources.getMessage("TLV_ILLEGAL_CHILD_TAG",
			prefix, IMPORT, qn));
	    }

	    // now, modify state

	    // we're a choose, so record new choose-specific state
	    if (isCoreTag(ns, ln, CHOOSE)) {
                chooseDepths.push(Integer.valueOf(depth));
                chooseHasWhen.push(Boolean.FALSE);
                chooseHasOtherwise.push(Boolean.FALSE);
            }

	    // if we're introducing a URL-related tag, record it
	    if (isCoreTag(ns, ln, IMPORT)) {
		if (hasAttribute(a, VAR_READER))
		    urlTags.push(IMPORT_WITH_READER);
		else
		    urlTags.push(IMPORT_WITHOUT_READER);
	    } else if (isCoreTag(ns, ln, PARAM))
		urlTags.push(PARAM);
	    else if (isCoreTag(ns, ln, REDIRECT))
		urlTags.push(REDIRECT);
	    else if (isCoreTag(ns, ln, URL))
		urlTags.push(URL);

	    // set up a check against illegal attribute/body combinations
	    bodyIllegal = false;
	    bodyNecessary = false;
	    if (isCoreTag(ns, ln, EXPR)) {
		if (hasAttribute(a, DEFAULT))
		    bodyIllegal = true;
	    } else if (isCoreTag(ns, ln, SET)) {
		if (hasAttribute(a, VALUE))
		    bodyIllegal = true;
		// else
		//    bodyNecessary = true;
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
		fail(Resources.getMessage("TLV_ILLEGAL_BODY", lastElementName));
	    if (!urlTags.empty()
		    && urlTags.peek().equals(IMPORT_WITHOUT_READER)) {
		// we're in an <import> without a Reader; nothing but
		// <param> is allowed
		fail(Resources.getMessage("TLV_ILLEGAL_BODY",
		    prefix + ":" + IMPORT));
	    }

	    // make sure <choose> has no non-whitespace text
	    if (chooseChild()) {
		String msg = 
		    Resources.getMessage("TLV_ILLEGAL_TEXT_BODY",
			prefix, CHOOSE,
			(s.length() < 7 ? s : s.substring(0,7)));
		fail(msg);
	    }
	}

	public void endElement(String ns, String ln, String qn) {

	    // consistently, we ignore JSP_TEXT
	    if (isJspTag(ns, ln, TEXT))
		return;

	    // handle body-related invariant
	    if (bodyNecessary)
		fail(Resources.getMessage("TLV_MISSING_BODY",
		    lastElementName));
	    bodyIllegal = false;	// reset: we've left the tag

	    // update <choose>-related state
	    if (isCoreTag(ns, ln, CHOOSE)) {
		Boolean b = (Boolean) chooseHasWhen.pop();
		if (!b.booleanValue())
		    fail(Resources.getMessage("TLV_PARENT_WITHOUT_SUBTAG",
			CHOOSE, WHEN));
		chooseDepths.pop();
		chooseHasOtherwise.pop();
	    }

	    // update state related to URL tags
	    if (isCoreTag(ns, ln, IMPORT)
                    || isCoreTag(ns, ln, PARAM)
		    || isCoreTag(ns, ln, REDIRECT)
		    || isCoreTag(ns, ln, URL))
		urlTags.pop();

	    // update our depth
	    depth--;
	}

	// are we directly under a <choose>?
	private boolean chooseChild() {
	    return (!chooseDepths.empty()
		&& (depth - 1) == ((Integer) chooseDepths.peek()).intValue());
	}

    }
}
