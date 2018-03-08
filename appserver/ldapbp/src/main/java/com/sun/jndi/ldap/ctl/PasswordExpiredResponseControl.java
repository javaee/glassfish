/*
 * @(#)PasswordExpiredResponseControl.java	1.2 03/04/15
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jndi.ldap.ctl;

import com.sun.jndi.ldap.BasicControl;

import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.Control;

/**
 * This class implements the LDAP response control for password expired 
 * notification. The password expired control is received if password 
 * needs to be changed when the user logs into newly created account.
 * The control is also received if the user needs to change the password
 * upon its reset. 
 * <p>
 * This control should be checked whenever a LDAP bind operation is
 * performed as a result of operations on the context such as when a new
 * initial context is created or when {@link javax.naming.ldap.InitialLdapContext#reconnect(javax.naming.ldap.Control[]) InitialLdapContext.reconnect}
 * is called.
 * <p>
 * Note that if the password is not changed when the control is received
 * during the creation of the context, or after reconnecting, the subsequent
 * LDAP operations on the context will fail and the PasswordExpired control is
 * received. 
 * <p>
 * The Password Expired control is defined in <tt>draft-vchu-ldap-pwd-policy-00.txt</tt>
 * <p>
 * The object identifier for Password Expired control is 2.16.840.1.113730.3.4.4
 * and the control has no value.
 * <p>
 *
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *     retrieveControls(ctx); 
 *    
 *     try { 
 *         // Do some operations on the context
 *         ctx.lookup("");
 *     } catch (javax.naming.OperationNotSupportedException e) { 
 *         retrieveControls(ctx); 
 *     }
 *
 *
 *    public static void printControls(DirContext ctx)
 *        Control[] respControls;
 *
 *        // retrieve response controls
 *        if ((respControls = ctx.getResponseControls()) != null) {
 *            for (int i = 0; i < respControls.length; i++) {
 *         
 *                // locate the password expired control
 *	  	if (respControls[i] instanceof PasswordExpiredResponseControl) {
 *	              System.out.println("Password has expired," +
 *				" please change the password");
 *              }
 *	  }
 *    }
 *
 * </pre>
 *
 * @see PasswordExpiringResponseControl
 * @author Vincent Ryan
 */
public class PasswordExpiredResponseControl extends BasicControl {

    private static final long serialVersionUID = -4568118365564432308L;

    /**
     * The password expired control's assigned object identifier is
     * 2.16.840.1.113730.3.4.4.
     */
    public static final String OID = "2.16.840.1.113730.3.4.4";

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
    PasswordExpiredResponseControl(String id, boolean criticality,
	byte[] value) {

	super(id, criticality, null);
    }
}
