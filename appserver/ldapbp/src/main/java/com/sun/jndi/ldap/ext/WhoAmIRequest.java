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
