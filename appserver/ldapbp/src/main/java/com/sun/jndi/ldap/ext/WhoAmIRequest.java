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
 * This class implements the LDAPv3 Extended Request for WhoAmI. The 
 * <tt>WhoAmIRequest</tt> and <tt>WhoAmIResponse</tt> are used to
 * obtain the current authorization identity of the user.   
 * WhoAmI extended operation allows users to get authorization identity
 * seperately from LDAP bind operation, unlike {@link com.sun.jndi.ldap.ctl.AuthorizationIDControl <tt>AuthorizationIDControl</tt>}
 * which has to be used with LDAP bind operation.
 * <p>
 * The WhoAmI LDAP extended operation is defined in <a href="http://www.ietf.org/internet-drafts/draft-zeilenga-ldap-authzid-08.txt">draft-zeilenga-ldap-authzid-08</a>.
 * <p>
 * The object identifier used by WhoAmI extended operation is
 * 2.16.840.1.113730.3.4.15 and the extened request has no value.
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
 *
 * @see WhoAmIResponse
 * @see com.sun.jndi.ldap.ctl.AuthorizationIDControl 
 * @author Vincent Ryan
 */

public class WhoAmIRequest implements ExtendedRequest {

    /**
     * The WhoAmI extended request's assigned object identifier
     * is 1.3.6.1.4.1.4203.1.11.3.
     */
    public static final String OID = "1.3.6.1.4.1.4203.1.11.3";

    private static final long serialVersionUID = -6522045023216094713L;

    /**
     * Constructs a WhoAmI extended request.
     */
    public WhoAmIRequest() {
    }

    /**
     * Retrieves the WhoAmI request's object identifier string.
     *
     * @return The non-null object identifier string.
     */
    public String getID() {
        return OID;
    }

    /**
     * Retrieves the WhoAmI request's ASN.1 BER encoded value.
     * Since the request has no defined value, null is always
     * returned.
     *
     * @return The null value.
     */
    public byte[] getEncodedValue() {
        return null;
    }

    /**
     * Creates an extended response object that corresponds to the 
     * LDAP WhoAmI extended request.
     *
     * @throws NamingException if cannot create extended response due
     * to an error
     * <p>
     */
    public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
        int offset, int length) throws NamingException {

        // Confirm that the object identifier is correct
        if ((id != null) && (!id.equals(OID))) {
            throw new ConfigurationException(
                "WhoAmI received the following response instead of " +
                OID + ": " + id);
        }
	try {
            return new WhoAmIResponse(id, berValue, offset, length);
	} catch (IOException e) { 

	    // Error occured in parsing the response value
	    NamingException ne = new NamingException(
			"Could not parse the response value");
	    ne.setRootCause(e);
	    throw ne;
	}
    }
}
