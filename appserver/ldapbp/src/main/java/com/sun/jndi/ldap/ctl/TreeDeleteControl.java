/*
 * @(#)TreeDeleteControl.java	1.4 03/04/15
 *
 * Copyright 2000 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package com.sun.jndi.ldap.ctl;

import java.io.IOException;
import com.sun.jndi.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Request Control for Tree Delete as
 * defined in <tt>draft-armijo-ldap-treedelete-02.txt</tt>
 *
 * The control has no control value.
 *
 * @author Vincent Ryan
 */
final public class TreeDeleteControl extends BasicControl {

    /**
     * The tree delete control's assigned object identifier
     * is 1.2.840.113556.1.4.805.
     */
    public static final String OID = "1.2.840.113556.1.4.805";

    private static final long serialVersionUID = 1278332007778853814L;

    /**
     * Constructs a tree delete critical control.
     */
    public TreeDeleteControl() {
	super(OID, true, null);
    }

    /**
     * Constructs a tree delete control.
     *
     * @param	criticality The control's criticality setting.
     */
    public TreeDeleteControl(boolean criticality) {
	super(OID, criticality, null);
    }
}
