/*
 * @(#)SortKey.java	1.2 00/09/01
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
