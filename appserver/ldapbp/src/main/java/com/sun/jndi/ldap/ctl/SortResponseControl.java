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
