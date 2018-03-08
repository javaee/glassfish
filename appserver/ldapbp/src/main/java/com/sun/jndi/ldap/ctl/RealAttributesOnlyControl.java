/*
 * @(#)RealAttributesOnlyControl.java	1.2 03/04/15
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jndi.ldap.ctl;

import java.io.IOException;
import com.sun.jndi.ldap.BasicControl;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;

/**
 * This class implements a LDAP request control for notifying the server that
 * only real attributes be returned during LDAP search operation.
 * <p>
 * A real attribute is stored with the entry. Unlike virtual attribute
 * it is physically present in the entry.
 * <p>
 * This control can be included with any JNDI operation that results
 * in LDAP search. RealAttributeOnlyControl and {@link VirtualAttributesOnlyControl}
 * function in a mutually exclusive way. If both controls are included in the
 * search request, the server may send an error back as there are no results
 * to return. If neither of the controls is included in the search request
 * the server will return both virtual and real attributes.
 * <p>
 * The object identifier for Real Attributes Only control is
 * 2.16.840.1.113730.3.4.17 and the control has no value.
 *
 * </pre>
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     ctx.setRequestControls(new Control[] {
 *         new RealAttributesOnlyControl()
 *     };
 *     NamingEnumeration results
 *            = ctx.search(searchBase, filter, constraints);
 *
 *       while (results != null && results.hasMore()) {
 *           SearchResult sr = (SearchResult) results.next();
 *
 *	     // Gets only real attributes
 *           Attributes realAttrs = sr.getAttributes();
 *
 * </pre>
 * @see VirtualAttributesOnlyControl
 * @author Jayalaxmi Hangal
 */

public class RealAttributesOnlyControl extends BasicControl {

    /**
     * The real attributes only control's assigned object identifier is
     * 2.16.840.1.113730.3.4.17
     */
    public static final String OID = "2.16.840.1.113730.3.4.17";

    private static final long serialVersionUID = 5253873444607688486L;

    /**
     * Constructs a control to return only real attributes in the
     * search result.
     * The control is always marked critical.
     */
    public RealAttributesOnlyControl() {
	super(OID, true, null);
    }
}
