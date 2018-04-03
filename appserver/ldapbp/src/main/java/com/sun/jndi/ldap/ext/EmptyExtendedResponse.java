/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.ext;

import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;

/**
  * This class represents an LDAP extended operation response with
  * an OID and empty response value. The response comprises an optional
  * object identifier and an optional ASN.1 BER encoded value. 
  * For extended responses which do not have value to return,
  * this class can be used.
  *<p>
  * @see javax.naming.ldap.ExtendedResponse
  * @see javax.naming.ldap.ExtendedRequest
  */

class EmptyExtendedResponse implements ExtendedResponse {

     /**
      * OID of the extended response
      * @serial
      */
     private String oid;

     private static final long serialVersionUID = -6096832546823615936L;

     EmptyExtendedResponse(String oid) {
	this.oid = oid;
     }

    /**
      * Retrieves the object identifier of the response.
      * The LDAP protocol specifies that the response object identifier is
      * optional.
      * If the server does not send it, the response will contain no ID
      * (i.e. null).
      *
      * @return	A possibly null object identifier string representing the LDAP
      *         <tt>ExtendedResponse.responseName</tt> component.
      */
    public String getID() {
	return oid;
    }

    /**
     * Since the response has no defined value, null is always
     * returned.
     *
     * @return The null value.
     */
    public byte[] getEncodedValue() {
	return null;
    }

}
