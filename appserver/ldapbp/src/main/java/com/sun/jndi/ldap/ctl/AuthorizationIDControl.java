/*
 * @(#)AuthorizationIDControl.java	1.2 03/04/15
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
