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
import com.sun.jndi.ldap.BerDecoder;

/**
 * This class implements the LDAPv3 Response Control for directory
 * synchronization as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-armijo-ldap-dirsync-01.txt">draft-armijo-ldap-dirsync-01.txt</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     realReplControlValue ::= SEQUENCE {
 *              flag                    INTEGER
 *              maxReturnlength         INTEGER
 *              cookie                  OCTET STRING
 *     }
 *
 * </pre>
 *
 * @see DirSyncControl
 * @author Vincent Ryan
 */
final public class DirSyncResponseControl extends BasicControl {

    /**
     * The dir-sync response control's assigned object identifier
     * is 1.2.840.113556.1.4.841.
     */
    public static final String OID = "1.2.840.113556.1.4.841";

    /** 
     * If flag is set to a non-zero value, it implies that there is more
     * data to retrieve.
     *
     * @serial
     */
    private int flag;

    /** 
     * The maximum length (in bytes) returned in a control response.
     *
     * @serial
     */
    private int maxReturnLength;

    /** 
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = -4497924817230713114L;

    /**
     * Constructs a paged-results response control.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public DirSyncResponseControl(String id, boolean criticality,
        byte[] value) throws IOException {

        super(id, criticality, value);

        // decode value
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);

            ber.parseSeq(null);
            flag = ber.parseInt();
            maxReturnLength = ber.parseInt();
	    cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
        }
    }

    /**
     * Retrieves the more-data flag.
     *
     * @return The more-data flag.
     */
    public int getFlag() {
        return flag;
    }

    /**
     * Determines if more data is available or not.
     *
     * @return true if more data is available.
     */
    public boolean hasMoreData() {
	return (flag != 0);
    }

    /**
     * Retrieves the maximum length (in bytes) returned in a control response.
     *
     * @return The length.
     */
    public int getMaxReturnLength() {
        return maxReturnLength;
    }

    /*
     * Retrieves the server-generated cookie. It is used by the client in
     * subsequent searches.
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
