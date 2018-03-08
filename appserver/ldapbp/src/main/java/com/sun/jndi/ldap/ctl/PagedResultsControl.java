/*
 * @(#)PagedResultsControl.java	1.4 02/01/23
 *
 * Copyright 1999 by Sun Microsystems, Inc.,
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
 * This class implements the LDAPv3 Control for paged-results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2696.txt">RFC-2696</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     realSearchControlValue ::= SEQUENCE {
 *         size      INTEGER (0..maxInt),
 *                           -- requested page size from client
 *                           -- result set size estimate from server
 *         cookie    OCTET STRING
 *     }
 *
 * </pre>
 *
 * @see PagedResultsResponseControl
 * @author Vincent Ryan
 */
final public class PagedResultsControl extends BasicControl {

    /**
     * The paged-results control's assigned object identifier
     * is 1.2.840.113556.1.4.319.
     */
    public static final String OID = "1.2.840.113556.1.4.319";

    /** 
     * The number of entries to return in a page.
     *
     * @serial
     */
    private int pageSize;

    /** 
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = -8771840635877430549L;

    /**
     * Constructs a paged-results critical control.
     *
     * @param	pageSize	The number of entries to return in a page.
     * @exception IOException If a BER encoding error occurs.
     *
     */
    public PagedResultsControl(int pageSize) throws IOException {
	super(OID, true, null);
	this.pageSize = pageSize;
	super.value = setEncodedValue();
    }

    /**
     * Constructs a paged-results control.
     * <p>
     * A sequence of paged-results can be abandoned by setting the pageSize
     * to zero and setting the cookie to the last cookie received from the
     * server.
     *
     * @param	pageSize	The number of entries to return in a page.
     * @param	cookie		A server-generated cookie.
     * @param	criticality	The control's criticality setting.
     * @exception IOException If a BER encoding error occurs.
     */
    public PagedResultsControl(int pageSize, byte[] cookie,
	boolean criticality) throws IOException {

	super(OID, criticality, null);
	this.pageSize = pageSize;
	this.cookie = cookie;
	super.value = setEncodedValue();
    }

    /*
     * Sets the ASN.1 BER encoded value of the paged-results control.
     * The result is the raw BER bytes including the tag and length of
     * the control's value. It does not include the controls OID or criticality.
     *
     * @return A possibly null byte array representing the ASN.1 BER encoded
     *         value of the LDAP sort control.
     * @exception IOException If a BER encoding error occurs.
     */
    private byte[] setEncodedValue() throws IOException {

	// build the ASN.1 encoding
	BerEncoder ber = new BerEncoder(32);

	ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
	    ber.encodeInt(pageSize);
            ber.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
	ber.endSeq();

	return ber.getTrimmedBuf();
    }
}
