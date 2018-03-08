/*
 * @(#)PasswordExpiringResponseControl.java	1.2 03/04/15
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jndi.ldap.ctl;

import com.sun.jndi.ldap.BasicControl;
import java.io.IOException;

/**
 * This class implements the LDAP response control notifying
 * password expiration. The Password Expiring control is a notification
 * to the client when password is about to expiring according to the
 * server's password policy.
 * This control should be checked whenever a LDAP bind operation is
 * performed as a result of operations on the context such as when a new
 * initial context is created or when {@link javax.naming.ldap.InitialLdapContext#reconnect(javax.naming.ldap.Control[]) InitialLdapContext.reconnect}
 * is called.
 * <p>
 * The Password Expiring control is defined in <tt>draft-vchu-ldap-pwd-policy-00.txt</tt>
 * <p>
 * The object identifier for Password Expiry is 2.16.840.1.113730.3.4.5 and
 * the value returned indicates the time left until the password expires.
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     PasswordExpiring ::= OCTET STRING  ; time in seconds until the 
 *                                        ; password expires
 *
 * </pre>
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *     Control[] respControls;
 *
 *     // retrieve response controls
 *     if ((respControls = ctx.getResponseControls()) != null) {
 *         for (int i = 0; i < respControls.length; i++) {
 *
 *             // locate the password expiring control
 *             if (respControls[i] instanceof PasswordExpiringResponseControl) {
 *                 System.out.println("Password expires in " +
 *                     ((PasswordExpiringResponseControl) respControls[i])
 *                         .timeRemaining() + " seconds");
 *             }
 *         }
 *     }
 *
 * </pre>
 *
 * @see PasswordExpiredResponseControl
 * @author Vincent Ryan
 */
public class PasswordExpiringResponseControl extends BasicControl {

    /**
     * The password expiring control's assigned object identifier is
     * 2.16.840.1.113730.3.4.5.
     */
    public static final String OID = "2.16.840.1.113730.3.4.5";

    /**
     * The time remaining until the password expires
     * @serial
     */
    private long timeLeft; 

    private static final long serialVersionUID = -7968094990572151704L;

    /**
     * Constructs a control to notify of password expiration.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    PasswordExpiringResponseControl(String id, boolean criticality,
	byte[] value) throws IOException  {

	super(id, criticality, value);
	if ((value != null) && (value.length > 0)) {
	    timeLeft = Long.parseLong(new String(value));
	}
    }

    /**
     * The time remaining until the password expires.
     *
     * @return The number of seconds until the password expires.
     */
    public long timeRemaining() {
	return timeLeft;
    }

    /**
     * Retrieves the PasswordExpiring control response's ASN.1 BER
     * encoded value.
     *
     * @return The ASN.1 BER encoded value of the LDAP control. 
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
