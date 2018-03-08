/*
 * @(#)DirSyncControl.java	1.3 03/04/15
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
