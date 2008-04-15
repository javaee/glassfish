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

package javax.servlet.jsp.jstl.tlv;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A TagLibraryValidator class to allow a TLD to restrict what
 * taglibs (in addition to itself) may be imported on a page where it's
 * used.</p>
 *
 * <p>This TLV supports the following initialization parameter:</p>
 * <ul>
 * <li><b>permittedTaglibs</b>: A whitespace-separated list of URIs corresponding 
 *   to tag libraries permitted to be imported on the page in addition to the tag 
 *   library that references PermittedTaglibsTLV (which is allowed implicitly).
 * </ul>
 *
 * @author Shawn Bayern
 */
public class PermittedTaglibsTLV extends TagLibraryValidator {

    //*********************************************************************
    // Constants

    // parameter names
    private final String PERMITTED_TAGLIBS_PARAM = "permittedTaglibs";

    // URI for "<jsp:root>" element
    private final String JSP_ROOT_URI = "http://java.sun.com/JSP/Page";

    // local name of "<jsp:root>" element
    private final String JSP_ROOT_NAME = "root";

    // QName for "<jsp:root>" element
    private final String JSP_ROOT_QN = "jsp:root";


    //*********************************************************************
    // Validation and configuration state (protected)

    private Set permittedTaglibs;		// what URIs are allowed?
    private boolean failed;			// did the page fail?
    private String uri;				// our taglib's URI

    //*********************************************************************
    // Constructor and lifecycle management

    public PermittedTaglibsTLV() {
	super();
	init();
    }

    private void init() {
	permittedTaglibs = null;
        failed = false;
    }

    public void release() {
	super.release();
	init();
    }
    

    //*********************************************************************
    // Validation entry point

    public synchronized ValidationMessage[] validate(
	    String prefix, String uri, PageData page) {
	try {

	    // initialize
	    this.uri = uri;
	    permittedTaglibs = readConfiguration();

	    // get a handler
	    DefaultHandler h = new PermittedTaglibsHandler();

	    // parse the page
	    SAXParserFactory f = SAXParserFactory.newInstance();
	    f.setValidating(true);
	    SAXParser p = f.newSAXParser();
	    p.parse(page.getInputStream(), h);

	    if (failed)
		return vmFromString(
		    "taglib " + prefix + " (" + uri + ") allows only the "
		    + "following taglibs to be imported: " + permittedTaglibs);
	    else
		return null;

	} catch (SAXException ex) {
	    return vmFromString(ex.toString());
	} catch (ParserConfigurationException ex) {
	    return vmFromString(ex.toString());
	} catch (IOException ex) {
	    return vmFromString(ex.toString());
	}
    }


    //*********************************************************************
    // Utility functions

    /** Returns Set of permitted taglibs, based on configuration data. */
    private Set readConfiguration() {

	// initialize the Set
	Set s = new HashSet();

	// get the space-separated list of taglibs
	String uris = (String) getInitParameters().get(PERMITTED_TAGLIBS_PARAM);

        // separate the list into individual uris and store them
        StringTokenizer st = new StringTokenizer(uris);
        while (st.hasMoreTokens())
	    s.add(st.nextToken());

	// return the new Set
	return s;

    }

    // constructs a ValidationMessage[] from a single String and no ID
    private ValidationMessage[] vmFromString(String message) {
	return new ValidationMessage[] {
	    new ValidationMessage(null, message)
	};
    }


    //*********************************************************************
    // SAX handler

    /** The handler that provides the base of our implementation. */
    private class PermittedTaglibsHandler extends DefaultHandler {

        // if the element is <jsp:root>, check its "xmlns:" attributes
        public void startElement(
                String ns, String ln, String qn, Attributes a) {

	    // ignore all but <jsp:root>
	    if (!qn.equals(JSP_ROOT_QN) &&
	            (!ns.equals(JSP_ROOT_URI) || !ln.equals(JSP_ROOT_NAME)))
		return;

	    // for <jsp:root>, check the attributes
	    for (int i = 0; i < a.getLength(); i++) {
		String name = a.getQName(i);

		// ignore non-namespace attributes, and xmlns:jsp
		if (!name.startsWith("xmlns:") || name.equals("xmlns:jsp"))
		    continue;

		String value = a.getValue(i);
		// ignore our own namespace declaration
		if (value.equals(uri))
		    continue;

		// otherwise, ensure that 'value' is in 'permittedTaglibs' set
		if (!permittedTaglibs.contains(value))
		    failed = true;
	    }
	}
    }

}
