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
