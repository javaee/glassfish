/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1999-2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.jndi.ldap.BerDecoder;

/**
 * This class implements the LDAPv3 Response Control for
 * paged-results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2696">RFC-2696</a>.
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
 * @see PagedResultsControl
 * @author Vincent Ryan
 */
final public class PagedResultsResponseControl extends BasicControl {

    /**
     * The paged-results response control's assigned object identifier
     * is 1.2.840.113556.1.4.319.
     */
    public static final String OID = "1.2.840.113556.1.4.319";

    /** 
     * An estimate of the number of entries in the search result.
     *
     * @serial
     */
    private int resultSize;

    /** 
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = 4004691067488246793L;

    /**
     * Constructs a paged-results response control.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public PagedResultsResponseControl(String id, boolean criticality,
        byte[] value) throws IOException {

        super(id, criticality, value);

        // decode value
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);

            ber.parseSeq(null);
            resultSize = ber.parseInt();
	    cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
        }
    }

    /**
     * Retrieves (an estimate of) the number of entries in the search result.
     *
     * @return The number of entries in the search result, or zero if unknown.
     */
    public int getResultSize() {
        return resultSize;
    }

    /*
     * Retrieves the server-generated cookie. Null is returned when there are
     * no more entries for the server to return.
     *
     * @return A possibly null server-generated cookie.
     */
    public byte[] getCookie() {
	if (cookie.length == 0) {
	    return null;
	} else {
            return cookie;
	}
    }
}
