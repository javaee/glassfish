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

package org.apache.taglibs.standard.extra.spath;

import java.util.List;

/**
 * <p>Represents a 'step' in an SPath expression.</p>
 *
 * @author Shawn Bayern
 */
public class Step {

    private boolean depthUnlimited;
    private String name;
    private List predicates;

    // record a few things for for efficiency...
    private String uri, localPart;

    /**
     * Constructs a new Step object, given a name and a (possibly null)
     * list of predicates.  A boolean is also passed, indicating
     * whether this particular Step is relative to the 'descendent-or-self'
     * axis of the node courrently under consideration.  If true, it is;
     * if false, then this Step is rooted as a direct child of the node
     * under consideration.
     */
    public Step(boolean depthUnlimited, String name, List predicates) {
	if (name == null)
	    throw new IllegalArgumentException("non-null name required");
	this.depthUnlimited = depthUnlimited;
	this.name = name;
	this.predicates = predicates;
    }

    /**
     * Returns true if the given name matches the Step object's
     * name, taking into account the Step object's wildcards; returns
     * false otherwise.
     */
    public boolean isMatchingName(String uri, String localPart) {
	// check and normalize arguments
	if (localPart == null)
	    throw new IllegalArgumentException("need non-null localPart");
	if (uri != null && uri.equals(""))
	    uri = null;

	// split name into uri/localPart if we haven't done so already
	if (this.localPart == null && this.uri == null)
	    parseStepName();

	// generic wildcard
	if (this.uri == null && this.localPart.equals("*"))
	    return true;

	// match will null namespace
	if (uri == null && this.uri == null
		&& localPart.equals(this.localPart))
	    return true;

	if (uri != null && this.uri != null && uri.equals(this.uri)) {
	    // exact match
	    if (localPart.equals(this.localPart))
		return true;

	    // namespace-specific wildcard
	    if (this.localPart.equals("*"))
		return true;
	}

	// no match
	return false;
    }

    /** Returns true if the Step's depth is unlimited, false otherwise. */
    public boolean isDepthUnlimited() {
	return depthUnlimited;
    }

    /** Returns the Step's node name. */
    public String getName() {
	return name;
    }

    /** Returns a list of this Step object's predicates. */
    public List getPredicates() {
	return predicates;
    }

    /** Lazily computes some information about our name. */
    private void parseStepName() {
	String prefix;
	int colonIndex = name.indexOf(":");

	if (colonIndex == -1) {
	    // no colon, so localpart is simply name (even if it's "*")
	    prefix = null;
	    localPart = name;
	} else {
	    prefix = name.substring(0, colonIndex);
	    localPart = name.substring(colonIndex + 1);
	}

	uri = mapPrefix(prefix);
    }

    /** Returns a URI for the given prefix, given our mappings. */
    private String mapPrefix(String prefix) {
	// ability to specify a mapping is, as of yet, unimplemented
	if (prefix == null)
	    return null;
	else
	    throw new IllegalArgumentException(
		"unknown prefix '" + prefix + "'");
    }
}
