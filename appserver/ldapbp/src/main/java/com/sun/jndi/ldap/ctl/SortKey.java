/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jndi.ldap.ctl;

/**
 * This class implements a sort key which is used by the LDAPv3
 * Control for server side sorting of search results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2891.txt">RFC-2891</a>.
 *
 * @author Vincent Ryan
 */
public class SortKey {

    /* 
     * The ID of the attribute to sort by.
     */
    private String attrID;

    /* 
     * The sort order. Ascending order, by default.
     */
    private boolean reverseOrder = false;

    /* 
     * The ID of the matching rule to use for ordering attribute values.
     */
    private String matchingRuleID = null;

    /**
     * Constructs a new instance of SortKey.
     *
     * @param	attrID	The ID of the attribute to be used as a sort key.
     */
    public SortKey(String attrID) {
	this.attrID = attrID;
    }

    /**
     * Constructs a new instance of SortKey.
     *
     * @param	attrID		The ID of the attribute to be used as a sort
     *				key.
     * @param	ascendingOrder	If true then entries are arranged in ascending
     *				order. Otherwise there are in descending order.
     * @param	matchingRule	The possibly null ID of the matching rule to
     *				use to order the attribute values. If not
     *				specified then the ordering matching rule
     *				defined for the sort key attribute, is used.
     */
    public SortKey(String attrID, boolean ascendingOrder,
	String matchingRuleID) {

	this.attrID = attrID;
	reverseOrder = (! ascendingOrder);
	if (matchingRuleID != null)
	    this.matchingRuleID = matchingRuleID;
    }

    /**
     * Retrieves the attribute ID of the sort key.
     *
     * @return    Attribute ID of the sort key.
     */
    public String getAttributeID() {
	return attrID;
    }

    /**
     * Determines the sort order.
     *
     * @return    true if the sort order is ascending, false if descending.
     */
    public boolean isAscending() {
	return (! reverseOrder);
    }

    /**
     * Retrieves the matching rule ID used to order the attribute values.
     *
     * @return    The possibly null matching rule ID. If null then the
     *            ordering matching rule defined for the sort key attribute,
     *            is used.
     */
    public String getMatchingRuleID() {
	return matchingRuleID;
    }
}
