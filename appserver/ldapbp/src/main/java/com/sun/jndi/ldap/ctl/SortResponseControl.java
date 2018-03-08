/*
 * @(#)SortResponseControl.java	1.4 02/01/23
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.jndi.ldap.ctl;

import java.io.IOException;
import javax.naming.*;
import javax.naming.directory.*;
import com.sun.jndi.ldap.BasicControl;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.LdapCtx;

/**
 * This class implements the LDAPv3 Response Control for server-side sorting
 * of search results as defined in 
 * <a href="http://www.ietf.org/rfc/rfc2891.txt">RFC-2891</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     SortResult ::= SEQUENCE {
 *        sortResult  ENUMERATED {
 *            success                   (0), -- results are sorted
 *            operationsError           (1), -- server internal failure
 *            timeLimitExceeded         (3), -- timelimit reached before
 *                                           -- sorting was completed
 *            strongAuthRequired        (8), -- refused to return sorted
 *                                           -- results via insecure
 *                                           -- protocol
 *            adminLimitExceeded       (11), -- too many matching entries
 *                                           -- for the server to sort
 *            noSuchAttribute          (16), -- unrecognized attribute
 *                                           -- type in sort key
 *            inappropriateMatching    (18), -- unrecognized or inappro-
 *                                           -- priate matching rule in
 *                                           -- sort key
 *            insufficientAccessRights (50), -- refused to return sorted
 *                                           -- results to this client
 *            busy                     (51), -- too busy to process
 *            unwillingToPerform       (53), -- unable to sort
 *            other                    (80)
 *            },
 *      attributeType [0] AttributeType OPTIONAL }
 *
 * </pre>
 *
 * @see SortControl
 * @author Vincent Ryan
 */
final public class SortResponseControl extends BasicControl {

    /**
     * The server-side sort response control's assigned object identifier
     * is 1.2.840.113556.1.4.474.
     */
    public static final String OID = "1.2.840.113556.1.4.474";

    /** 
     * The sort result code.
     *
     * @serial
     */
    private int resultCode = 0;

    /** 
     * The ID of the attribute that caused the sort to fail.
     *
     * @serial
     */
    private String badAttrId = null;

    private static final long serialVersionUID = -3732673799687266442L;

    /**
     * Constructs a new instance of SortResponseControl.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public SortResponseControl(String id, boolean criticality, byte[] value)
	throws IOException {

	super(id, criticality, value);

	// decode value
	if ((value != null) && (value.length > 0)) {
	    BerDecoder ber = new BerDecoder(value, 0, value.length);

	    ber.parseSeq(null);
	    resultCode = ber.parseEnumeration();
	    if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_CONTEXT)) {
		badAttrId = ber.parseStringWithTag(Ber.ASN_CONTEXT, true, null);
	    }
	}
    }

    /**
     * Determines if the search results have been successfully sorted.
     * If an error occurred during sorting a NamingException is thrown.
     *
     * @return    true if the search results have been sorted.
     */
    public boolean isSorted() {
	return (resultCode == 0); // a result code of zero indicates success
    }

    /**
     * Retrieves the LDAP result code of the sort operation.
     *
     * @return    The result code. A zero value indicates success.
     */
    public int getResultCode() {
	return resultCode;
    }

    /**
     * Retrieves the ID of the attribute that caused the sort to fail.
     * Returns null if no ID was returned by the server.
     *
     * @return The possibly null ID of the bad attribute.
     */
    public String getAttributeID() {
	return badAttrId;
    }

    /**
     * Retrieves the NamingException appropriate for the result code.
     *
     * @return A NamingException or null if the result code indicates
     *         success.
     */
    public NamingException getException() {

	return LdapCtx.mapErrorCode(resultCode, null);
    }
}
