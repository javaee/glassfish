/*
 * @(#)GetEffectiveRightsControl.java	1.2 03/04/15
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jndi.ldap.ctl;

import java.io.IOException;
import com.sun.jndi.ldap.BasicControl;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;
import javax.naming.directory.DirContext;

/**
 * This class implements the getEffectiveRights control to obtain the
 * access control rights in effect for a given user. This control may be
 * included in a LDAP search operation. In response to this control the
 * server sends effective rights for the entries and the attributes returned
 * in the search result response.
 * <p>
 * The JNDI context methods {@link javax.naming.directory.DirContext#getAttributes(String name, String[] attrIds) DirContext.getAttributes}
 * and {@link javax.naming.directory.DirContext#search(Name name, Attributes matchingAttributes, String[] attributesToReturn) DirContext.search}
 * can be used to retrieve the effective rights.
 * 
 * <p>
 * The object identifier for the GetEffectiveRights control is
 * 1.3.6.1.4.1.42.2.27.9.5.2 and the control value consists of the
 * authorization identity of the user for whom the effective rights are being
 * requested and the additional attributes for which the user effective rights
 * are to be known.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     GetRightsControl ::= SEQUENCE {
 *          authzId  = authzId ; as defined in RFC 2829
 *                          ; NULL or empty string means get bound user's rights.
 *                          ; "dn:" means get anonymous user's rights.
 *          attributes  SEQUENCE OF AttributeType
 *                          ; additional attribute type for which rights
 *			    information is requested.
 *                          ; NULL means just the ones returned with the
 *			    search operation.
 *     }
 *
 * </pre>
 * The following code sample shows how the control may be used:
 * <pre>
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     // Get the effective rights for authzId
 *     String dn = "dn:" + authzId;
 *
 *    // create a GetEffectiveRights control to return effective
 *    // rights for authzId on the search result entries and attributes 
 *    Control[] reqControls = new Control[] {
 *               new GetEffectiveRightsControl(dn, null, true)
 *    };
 *
 *    // activate the control
 *    ctx.setRequestControls(reqControls);
 *
 *    // The effective rights are returned in the aclRights operational
 *    // attribute.
 *    String[] attrsToReturn = new String[] {"aclRights"};
 *
 *    // Get the entry level effective rights for all the
 *    // entries in the search result
 *    NamingEnumeration results =
 * 			ctx.search(entryName, null, attrsToReturn);
 *
 *    printEffectiveRights(results);
 *
 *
 * </pre>
 * @author Vincent Ryan
 */

public class GetEffectiveRightsControl extends BasicControl {

    /**
     * The GetEffectiveRights control's assigned object identifier
     * is 1.3.6.1.4.1.42.2.27.9.5.2.
     */
    public static final String OID = "1.3.6.1.4.1.42.2.27.9.5.2";

    private static final long serialVersionUID = -6292851668254246648L;

    /**
     * Constructs a control to request the rights which are in effect
     * for the given user.
     *
     * @param	authzId The authorization identity. 
     * @param	attributes Additional attributes for which rights information
     * 		is requested.
     * @param	criticality The control's criticality setting.
     * @exception IOException If a BER encoding error occurs.
     */
    public GetEffectiveRightsControl(String authzId, String[] attributes,
	    boolean criticality) throws IOException {

	super(OID, criticality, null);
	value = setEncodedValue(authzId, attributes);
    }


    private static byte[] setEncodedValue(String authzId, String[] attrs)
	    throws IOException {

	// build the ASN.1 encoding
	BerEncoder ber = new BerEncoder(256);

	ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
	    ber.encodeString(authzId, true);
	    ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		ber.encodeStringArray(attrs, true);
	    ber.endSeq();
	ber.endSeq();

	return ber.getTrimmedBuf();
    }
}
