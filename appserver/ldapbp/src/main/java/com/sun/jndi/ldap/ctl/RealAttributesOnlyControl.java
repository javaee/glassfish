/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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
