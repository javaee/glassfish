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
