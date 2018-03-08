/*
 * @(#)EmptyExtendedResponse.java	1.1 03/04/17 
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
