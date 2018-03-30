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
import javax.naming.*;
import javax.naming.directory.*;
import com.sun.jndi.ldap.BasicControl;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.LdapCtx;

/**
 * This class implements the LDAPv3 Response Control for virtual-list-view
 * as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-ietf-ldapext-ldapv3-vlv-04.txt">draft-ietf-ldapext-ldapv3-vlv-04.txt</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     VirtualListViewResponse ::= SEQUENCE {
 *         targetPosition   INTEGER (0 .. maxInt),
 *         contentCount     INTEGER (0 .. maxInt),
 *         virtualListViewResult ENUMERATED {
 *             success                   (0),
 *             operatonsError            (1),
 *             unwillingToPerform       (53),
 *             insufficientAccessRights (50),
 *             busy                     (51),
 *             timeLimitExceeded         (3),
 *             adminLimitExceeded       (11),
 *             sortControlMissing       (60),
 *             offsetRangeError         (61),
 *             other                    (80)
 *         },
 *         contextID     OCTET STRING OPTIONAL
 *     }
 *
 * </pre>
 *
 * @see VirtualListViewControl
 * @see ResponseControlFactory
 * @author Vincent Ryan
 */
final public class VirtualListViewResponseControl extends BasicControl {

    /**
     * The virtual-list-view control's assigned object identifier
     * is 2.16.840.1.113730.3.4.10.
     */
    public static final String OID = "2.16.840.1.113730.3.4.10";

    /**
     * The position of the target entry in the list.
     * 
     * @serial
     */
    private int targetOffset;

    /**
     * The current number of entries in the list.
     *
     * @serial
     */
    private int listSize;

    /**
     * The result code of the view operation.
     *
     * @serial
     */
    private int resultCode;

    /**
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = null;

    private static final long serialVersionUID = 7199084985976520165L;

    /**
     * Constructs a new instance of VirtualListViewResponseControl.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public VirtualListViewResponseControl(String id, boolean criticality,
	byte[] value) throws IOException {

	super(id, criticality, value);

	// decode value
	if ((value != null) && (value.length > 0)) {
	    BerDecoder ber = new BerDecoder(value, 0, value.length);

	    ber.parseSeq(null);
	    targetOffset = ber.parseInt();
	    listSize = ber.parseInt();
	    resultCode = ber.parseEnumeration();

	    if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_OCTET_STR)){
		cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
	    }
	}
    }

    /**
     * Retrieves the LDAP result code of the view operation.
     *
     * @return    The result code. A zero value indicates success.
     */
    public int getResultCode() {
	return resultCode;
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

    /**
     * Retrieves the server's estimate of the current position of the
     * target entry in the list.
     *
     * @return The position of the target entry in the list.
     */
    public int getTargetOffset() {
	return targetOffset;
    }

    /**
     * Retrieves the server's estimate of the current number of entries
     * in the list.
     *
     * @return The current number of entries in the list.
     */
    public int getListSize() {
	return listSize;
    }

    /**
     * Retrieves the server-generated cookie (if supplied).
     *
     * @return A server-generated cookie or null if unavailable.
     */
    public byte[] getContextID() {
        return cookie;
    }
}
