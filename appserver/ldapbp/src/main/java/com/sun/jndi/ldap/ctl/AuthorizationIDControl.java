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
import javax.naming.ldap.InitialLdapContext;

/**
 * This class implements the LDAP request control for authorization identity
 * control. This control is used to request that the server return the
 * authorization identity (in the LDAP bind response) resulting from the
 * accompanying LDAP bind operation. It is a <em>connection request control</em>
 * as described in {@link javax.naming.ldap.InitialLdapContext InitialLdapContext}
 * <p>
 * The Authorization Identity Bind Control is defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-auth-response-08.txt">draft-weltman-ldapv3-auth-response-08</a>.
 * <p>
 * The object identifier used for Authorization Identity control is
 * 2.16.840.1.113730.3.4.16 and the control has no value.
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an authorization identity bind control
 *     Control[] reqControls = new Control[]{
 *         new AuthorizationIDControl()
 *     };
 *
 *     // create an initial context using the supplied environment properties
 *     // and the supplied control
 *     LdapContext ctx = new InitialLdapContext(env, reqControls);
 *     Control[] respControls;
 *
 *     // retrieve response controls
 *     if ((respControls = ctx.getResponseControls()) != null) {
 *         for (int i = 0; i < respControls.length; i++) {
 *
 *             // locate the authorization identity response control
 *             if (respControls[i] instanceof AuthorizationIDResponseControl) {
 *                 System.out.println("My identity is " +
 *                     ((AuthorizationIDResponseControl) respControls[i])
 *                         .getAuthorizationID());
 *             }
 *         }
 *     }
 *
 * </pre>
 *
 * @see AuthorizationIDResponseControl
 * @see com.sun.jndi.ldap.ext.WhoAmIRequest
 * @author Vincent Ryan
 */

public class AuthorizationIDControl extends BasicControl {

    /**
     * The authorization identity control's assigned object identifier is
     * 2.16.840.1.113730.3.4.16.
     */
    public static final String OID = "2.16.840.1.113730.3.4.16";

    private static final long serialVersionUID = 2851964666449637092L;

    /**
     * Constructs a control to request the authorization identity.
     *
     * @param   criticality The control's criticality setting.
     */
    public AuthorizationIDControl(boolean criticality) {
	super(OID, criticality, null);
    }
}
