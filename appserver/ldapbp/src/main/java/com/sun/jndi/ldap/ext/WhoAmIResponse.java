/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.ext;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

import java.io.IOException;

/**
 * This class implements the LDAPv3 Extended Response for WhoAmI.
 * The <tt>WhoAmIRequest</tt> and <tt>WhoAmIResponse</tt> are used to
 * obtain the current authorization identity of the user.
 * WhoAmI extended operation allows users to get authorization identity
 * seperately from LDAP bind operation, unlike {@link com.sun.jndi.ldap.ctl.AuthorizationIDControl <tt>AuthorizationIDControl</tt>}
 * which has to be used with LDAP bind operation.
 *
 * <p>
 * The WhoAmI LDAP extended operation is defined in <a href="http://www.ietf.org/internet-drafts/draft-zeilenga-ldap-authzid-08.txt">draft-zeilenga-ldap-authzid-08</a>.
 * <p>
 * The object identifier used by WhoAmI extended operation is
 * 2.16.840.1.113730.3.4.15 and the extended response value is the user
 * authorization identity.
 * <p>   
 * The extended response's value has the following ASN.1 definition:
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
 * The following code sample shows how the extended operation may be used:
 * <pre>
 *
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     // perform the extended operation
 *     WhoAmIResponse whoAmI =
 *         (WhoAmIResponse) ctx.extendedOperation(new WhoAmIRequest());
 *
 *     System.out.println("I am <" + whoAmI.getAuthorizationID() + ">");
 *
 * </pre>
 * @see WhoAmIRequest
 * @see com.sun.jndi.ldap.ctl.AuthorizationIDControl 
 * @author Vincent Ryan
 */

public class WhoAmIResponse implements ExtendedResponse {

    /**
     * The WhoAmI extended response's assigned object identifier
     * is 1.3.6.1.4.1.4203.1.11.3.
     */
    public static final String OID = "1.3.6.1.4.1.4203.1.11.3";

    /**
     * Authorization identity of the bound user
     * @serial
     */
    private String authzId;

    /**
     * The ASN1 encoded value of the extended response
     * @serial
     */
    private byte[] value;

    private static final long serialVersionUID = 4095032263256625777L;

    /**
     * Constructs a WhoAmI extended response.
     */
    WhoAmIResponse(String id, byte[] value, int offset, int length)
	throws IOException {
	
	this.value = value;

	// decode value
        if ((value != null) && (value.length > 0)) {
	    authzId = new String(value, offset, length, "UTF8");
        }
    }

    /**
     * Retrieves the WhoAmI response's object identifier string.
     *
     * @return The non-null object identifier string.
     */
    public String getID() {
        return OID;
    }

    /**
     * Retrieves the WhoAmI response's ASN.1 BER encoded value.
     *
     * @return A possibly null byte array representing the ASN.1 BER
     * 	       encoded value of the LDAP extended response. 
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

    /**
     * Retrieves the authorization identity.
     *
     * @return The authorization identity. An empty string is returned
     * when anonymous authentication is used.
     */
    public String getAuthorizationID() {
	if (authzId == null) {
	   return ""; 
	}
	return authzId;
    }
}
