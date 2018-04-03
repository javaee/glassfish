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

import com.sun.jndi.ldap.BasicControl;

import java.io.IOException;

/**
 * This class implements the LDAP response control for Authorization Identity
 * Response control. This control retrieves the current authorization identity
 * resulting from an LDAP bind operation..
 * When {@link AuthorizationIDControl} is included in the LDAP bind request,
 * the server bind response includes the Authorization Identity Response
 * Control.
 * <p>
 * The Authorization Identity Response Control is defined in <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-auth-response-08.txt">draft-weltman-ldapv3-auth-response-08</a>.
 * <p>
 * The object identifier used by Authorization identity response  control is
 * 2.16.840.1.113730.3.4.15 and the control value returned is the authorization
 * identity. The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     AuthzId ::= LDAPString ; containing an authzId as defined in RFC 2829
 *                            ; or an empty value
 *
 *     authzId    = dnAuthzId / uAuthzId
 *
 *     ; distinguished-name-based authz id.
 *     dnAuthzId  = "dn:" dn
 *     dn         = utf8string    ; with syntax defined in RFC 2253
 *
 *     ; unspecified userid, UTF-8 encoded.
 *     uAuthzId   = "u:" userid
 *     userid     = utf8string    ; syntax unspecified
 *
 * </pre>
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an authorization identity response control
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
 * @see AuthorizationIDControl
 * @see com.sun.jndi.ldap.ext.WhoAmIRequest
 * @author Vincent Ryan
 */
public class AuthorizationIDResponseControl extends BasicControl {

    /**
     * The authorization identity response control's assigned object identifier is
     * 2.16.840.1.113730.3.4.15.
     */
    public static final String OID = "2.16.840.1.113730.3.4.15";

    /**
     * Authorization Identity of the bound user
     * @serial
     */
    private String authzId;

    private static final long serialVersionUID = -7740841453439127143L;

    /**
     * Constructs a control to indicate the authorization identity.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    AuthorizationIDResponseControl(String id, boolean criticality,
	byte[] value) throws IOException {

	super(id, criticality, value);
	if ((value == null) || (value.length == 0)){
	    authzId = "";
	} else {
	    authzId = new String(value, "UTF8");
	}
    }

    /**
     * Retrieves the authorization identity.
     * An empty string is returned when anonymous authentication is used.
     *
     * @return The authorization identity.
     */
    public String getAuthorizationID() {
	return authzId;
    }

    /**
     * Retrieves the authorization identity control response's ASN.1 BER
     * encoded value.
     *
     * @return A possibly null byte array representing the ASN.1 BER
     * 	       encoded value of the LDAP response control. 
     */
    public byte[] getEncodedValue() {

	if (value == null) {
	    return null;
	}

        // return a copy of value
        byte[] retval = new byte[value.length];
        System.arraycopy(value, 0, retval, 0, value.length);
        return retval;
    }
}
