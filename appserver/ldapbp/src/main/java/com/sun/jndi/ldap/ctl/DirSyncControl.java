/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2018 Oracle and/or its affiliates. All rights reserved.
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
 * This class implements the LDAPv3 Request Control for directory
 * synchronization as defined in <tt>draft-armijo-ldap-dirsync-01.txt</tt>
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     realReplControlValue ::= SEQUENCE {
 *         parentsFirst     INTEGER,
 *         maxReturnlength  INTEGER,
 *         cookie           OCTET STRING }
 *
 * </pre>
 *
 * @see DirSyncResponseControl
 * @author Vincent Ryan
 */
final public class DirSyncControl extends BasicControl {

    /**
     * The dir-sync control's assigned object identifier
     * is 1.2.840.113556.1.4.841.
     */
    public static final String OID = "1.2.840.113556.1.4.841";

    /** 
     * Parent entries are returned before their children when value is set to 1.
     *
     * @serial
     */
    private int parentsFirst = 1;

    /** 
     * The maximum length (in bytes) to be returned in a control response.
     * Must be greater than zero for any data to be returned.
     *
     * @serial
     */
    private int maxReturnLength = Integer.MAX_VALUE;

    /** 
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = 564423657960860475L;

    /**
     * Constructs a dir-sync control.
     *
     * @exception IOException If a BER encoding error occurs.
     */
    public DirSyncControl() throws IOException {

	super(OID, true, null);
	super.value = setEncodedValue();
    }

    /**
     * Constructs a dir-sync control.
     *
     * @param	criticality  The control's criticality setting.
     * @exception IOException If a BER encoding error occurs.
     */
    public DirSyncControl(boolean criticality) throws IOException {

	super(OID, criticality, null);
	super.value = setEncodedValue();
    }

    /**
     * Constructs a dir-sync control.
     *
     * @param	parentsFirst	  Parent entries are returned before their
     *				  children when value is set to 1.
     * @param	maxReturnLength   The maximum length (in bytes) to be returned
     *				  in a control response. Must be greater than
     *				  zero for any data to be returned.
     * @param	cookie		  A server-generated cookie.
     * @param	criticality	  The control's criticality setting.
     * @exception IOException	  If a BER encoding error occurs.
     */
    public DirSyncControl(int parentsFirst, int maxReturnLength,
	byte[] cookie, boolean criticality) throws IOException {

	super(OID, criticality, null);

	this.parentsFirst = parentsFirst;
	this.maxReturnLength = maxReturnLength;
	this.cookie = cookie;
	super.value = setEncodedValue();
    }

    /*
     * Sets the ASN.1 BER encoded value of the dir-sync control.
     * The result is the raw BER bytes including the tag and length of
     * the control's value. It does not include the controls OID or criticality.
     *
     * @return A possibly null byte array representing the ASN.1 BER encoded
     *         value of the LDAP dir-sync control.
     * @exception IOException If a BER encoding error occurs.
     */
    private byte[] setEncodedValue() throws IOException {

	// build the ASN.1 encoding
	BerEncoder ber = new BerEncoder(64);

	ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
	    ber.encodeInt(parentsFirst);
	    ber.encodeInt(maxReturnLength);
	    ber.encodeOctetString(cookie, ber.ASN_OCTET_STR);
	ber.endSeq();

	return ber.getTrimmedBuf();
    }
}
