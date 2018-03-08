/*
 * @(#)VirtualAttributesOnlyControl.java	1.2 03/04/15
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
 * This class implements a LDAP request control for notifying the server
 * only virtual attributes be returned during LDAP search operation.
 * <p>
 * A virtual attribute is not stored with the entry. It is computed and
 * returned to the client application as a normal attribute during the 
 * LDAP operation.
 * <p>
 * This control can be included with any JNDI operation that results
 * in a LDAP search. VirtualAttributeOnlyControl and {@link RealAttributesOnlyControl}
 * function in a mutually exclusive way. If both controls are included in the
 * search request, the server may send an error back as there are no results
 * to return. If neither of the controls is included in the search request
 * the server will return both virtual and real attributes.   
 * <p>
 * The object identifier for Virtual Attributes Only control is
 * 2.16.840.1.113730.3.4.19 and the control has no value.
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     ctx.setRequestControls(new Control[] {
 *         new VirtualAttributesOnlyControl()
 *     };
 *     NamingEnumeration results
 *            = ctx.search(searchBase, filter, constraints);
 *
 *       while (results != null && results.hasMore()) {
 *           SearchResult sr = (SearchResult) results.next();
 *
 *           // Gets only virtual attributes
 *           Attributes virtualAttrs = sr.getAttributes();
 *
 *
 * </pre>
 * @see RealAttributesOnlyControl
 * @author Jayalaxmi Hangal
 */

public class VirtualAttributesOnlyControl extends BasicControl {

    /**
     * The virtual attributes only control's assigned object identifier is
     * 2.16.840.1.113730.3.4.19
     */
    public static final String OID = "2.16.840.1.113730.3.4.19";

    private static final long serialVersionUID = -5373095230975089359L;

    /**
     * Constructs a control to return only virtual attributes in
     * the search result.
     * The control is always marked critical. 
     *
     */
    public VirtualAttributesOnlyControl() {
	super(OID, true, null);
    }
}
