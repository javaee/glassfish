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
 * This class implements the LDAPv3 Request Control for server-side sorting
 * of search results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2891.txt">RFC-2891</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     SortKeyList ::= SEQUENCE OF SEQUENCE {
 *         attributeType     AttributeDescription,
 *         orderingRule  [0] MatchingRuleId OPTIONAL,
 *         reverseOrder  [1] BOOLEAN DEFAULT FALSE }
 *
 * </pre>
 *
 * @see SortResponseControl
 * @author Vincent Ryan
 */
final public class SortControl extends BasicControl {

    /**
     * The server-side sort control's assigned object identifier
     * is 1.2.840.113556.1.4.473.
     */
    public static final String OID = "1.2.840.113556.1.4.473";

    private static final long serialVersionUID = 8931633399436504556L;

    /**
     * Constructs a server-side sort control.
     *
     * @param	sortBy	    The keys to sort by.
     * @param	criticality The control's criticality setting.
     * @exception IOException If a BER encoding error occurs.
     */
    public SortControl(SortKey[] sortBy, boolean criticality)
	throws IOException {

	super(OID, criticality, null);
	super.value = setEncodedValue(sortBy);
    }

    /**
     * Constructs a server-side sort control.
     *
     * @param	sortBy	The attribute IDs to sort by.
     * @param	criticality The control's criticality setting.
     * @exception IOException If a BER encoding error occurs.
     */
    public SortControl(String[] sortBy, boolean criticality)
	throws IOException {

	super(OID, criticality, null);

	if (sortBy == null || (sortBy.length == 0)) {
	    return;
	}

	SortKey[] sortKeys = new SortKey[sortBy.length];
	for (int i = 0; i < sortBy.length; i++) {
	    sortKeys[i] = new SortKey(sortBy[i]);
	}
	super.value = setEncodedValue(sortKeys);
    }

    /*
     * Sets the ASN.1 BER encoded value of the sort control.
     * The result is the raw BER bytes including the tag and length of
     * the control's value. It does not include the controls OID or criticality.
     *
     * @param	sortKeys    The keys to sort by.
     * @return A possibly null byte array representing the ASN.1 BER encoded
     *         value of the LDAP sort control.
     * @exception IOException If a BER encoding error occurs.
     */
    private byte[] setEncodedValue(SortKey[] sortKeys) throws IOException {

	// build the ASN.1 encoding
	BerEncoder ber = new BerEncoder(32);
	String matchingRule;

	ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);

	for (int i = 0; i < sortKeys.length; i++) {
	    ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
	    ber.encodeString(sortKeys[i].getAttributeID(), true); // v3

	    if ((matchingRule = sortKeys[i].getMatchingRuleID()) != null) {
		ber.encodeString(matchingRule, (Ber.ASN_CONTEXT | 0), true);
	    }
	    if (! sortKeys[i].isAscending()) {
		ber.encodeBoolean(true, (Ber.ASN_CONTEXT | 1));
	    }
	    ber.endSeq();
	}
	ber.endSeq();

	return ber.getTrimmedBuf();
    }
}
