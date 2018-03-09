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
 * This class implements the LDAP request control for proxied authorization.
 * This control is used to request that the accompanying operation be
 * performed using the supplied authorization identity, overriding any
 * existing authorization identity.
 * The control may be included in any LDAP operation except in those that
 * cause change in authentication, authorization or data confidentiality, such
 * as bind and startTLS.
 * <p>
 * The Proxied Authorization control is defined in <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-proxy-12.txt">draft-weltman-ldapv3-proxy-12</a>.
 * <p>
 * The object identifier for the Proxied Authorization control is 2.16.840.1.113730.3.4.18
 *  and the control value is the authorization identity to be used. The control
 * value is empty if anonymous identity is to be used. The control's value has
 * the following ASN.1 definition:
 * <p>
 * <pre>
 *
 *     ProxiedAuth ::= LDAPString ; containing an authzId as defined in RFC 2829
 *                                ; or an empty value
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
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     // examine the authorization identity and set the appropriate prefix
 *     String authzId = isDN(authzId) ? "dn:" + authzId : "u:" + authzId;
 *
 *     // activate the control
 *     ctx.setRequestControls(new Control[] {
 *         new ProxiedAuthorizationControl(authzId)
 *     };
 *
 *     // perform an operation using the authorization identity
 *     ctx.getAttributes("");
 *
 * </pre>
 *
 * @author Vincent Ryan
 * @see AuthorizationIDControl
 * @see com.sun.jndi.ldap.ext.WhoAmIRequest
 */

public class ProxiedAuthorizationControl extends BasicControl {

    private static final long serialVersionUID = 552016610613918389L;

    /**
     * The proxied authorization control's assigned object identifier is
     * 2.16.840.1.113730.3.4.18.
     */
    public static final String OID = "2.16.840.1.113730.3.4.18";

    /**
     * Constructs a control to perform an operation using the supplied 
     * authorization identity. The control is always marked critical.
     *
     * @param authzId A non null authorization identity to use. authzId
     *		      must be set to an empty string if anonymous identity
     *		      is to be used.
     * @exception IOException If a BER encoding error occurs.
     */
    public ProxiedAuthorizationControl(String authzId) throws IOException {
	super(OID, true, null);  
	value = setEncodedValue(authzId);
    }

    /*
     * Encodes the control's value using ASN.1 BER.
     * The result includes the BER tag and length for the control's value but 
     * does not include the control's object identifer and criticality setting.
     *
     * @param authzId The authorization identity to use.
     * @return A byte array representing the ASN.1 BER encoded value of the
     *	       LDAP control.
     * @exception IOException If a BER encoding error occurs.
     */
    private static byte[] setEncodedValue(String authzId) throws IOException {

        // build the ASN.1 BER encoding
        BerEncoder ber = new BerEncoder(2 * authzId.length() + 5);
        ber.encodeString(authzId, true);

        return ber.getTrimmedBuf();
    }
}
