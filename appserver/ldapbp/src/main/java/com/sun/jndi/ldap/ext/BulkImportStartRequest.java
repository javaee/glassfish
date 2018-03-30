/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

import java.io.IOException;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;

/**
 * This class implements the LDAPv3 Extended Request for BulkImportStart. The
 * BulkImportStartRequest is used to mark the beginning of a bulk
 * import operation.
 * <p>
 * The bulk import extended operations allow importing entries
 * remotely with a series of LDAP add operations. When the bulk import
 * operation starts it must be finished by sending the
 * {@link BulkImportFinishedRequest} before the normal LDAP operations
 * can resume. Only LDAP add operations are legal between BulkImportStart and
 * BulkImportFinished operations.
 * <p>
 * Note that to add entries using JNDI, use the context methods {@link javax.naming.Context#createSubcontext(javax.naming.Name) Context.createSubcontext}
 * or {@link javax.naming.Context#bind(javax.naming.Name, java.lang.Object) Context.bind}.
 * <p>
 * <b>WARNING:</b> Users have to be extremely careful when using bulk import
 * operations. Once a bulk import has begun, the previous contents under the
 * naming context tree are erased. When a bulk import is started, if the
 * connection is aborted before the bulk import finished is sent, no entries are
 * imported and the previous contents under the naming context tree are wiped
 * out of the directory.
 * <p>
 * The object identifier for BulkImportStart is 2.16.840.1.113730.3.5.7
 * and the extended request value is the naming context to import to.
 * 
 * <p>
 * The following code sample shows how the extended operation may be used:
 * <pre>
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     // The naming context to import to
 *     String namingContext;
 *
 *     // Bulk import starts
 *     ctx.extendedOperation(new BulkImportStartRequest(
 *					 namingContext));
 *     System.out.println("Bulk import operation begins");
 *
 *     // Add entries 
 *     ctx.createSubcontext(entryName, entryAttrs);
 *           :
 *	     :
 *     // Bulk import done 
 *     ctx.extendedOperation(new BulkImportFinishedRequest());
 *     System.out.println("Bulk import operation finished");
 * </pre>
 *
 * @see BulkImportFinishedRequest
 * @author Jayalaxmi Hangal
 */

public class BulkImportStartRequest implements ExtendedRequest {

    /**
     * The BulkImportStart extended operation's assigned object identifier
     * is  2.16.840.1.113730.3.5.7 
     */
    public static final String OID = "2.16.840.1.113730.3.5.7";

    /**
     * ASN1 Ber encoded value of the extended request
     * @serial
     */
    private byte[] value; 

    private static final long serialVersionUID = 8280455967681862705L;

    /**
     * Constructs a BulkImportStart extended request.
     *
     * @param importName The naming context to import to.
     * The naming context is one of the values of the <tt>namingContexts</tt>
     * attribute contained in the servers' rootDSE entry.
     * @exception IOException If a BER encoding error occurs.
     */
    public BulkImportStartRequest(String importName) 
	throws IOException {
	value = importName.getBytes("UTF8");
    }

    /**
     * Retrieves the BulkImportStart request's object identifier string.
     *
     * @return The non-null object identifier string.
     */
    public String getID() {
        return OID;
    }

    /**
     * Retrieves the BulkImportStart request's ASN.1 BER encoded value.
     *
     * @return The ASN.1 BER encoded value of the LDAP extended request.
     */
    public byte[] getEncodedValue() {
	return value;
    }

    /**
     * Creates an extended response object that corresponds to the 
     * LDAP BulkImportStart extended request.
     * <p>
     */
    public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
        int offset, int length) throws NamingException {

        // Confirm that the object identifier is correct
        if ((id != null) && (!id.equals(OID))) {
            throw new ConfigurationException(
                "BulkImportStart received the following response instead of " +
                OID + ": " + id);
        }
        return new EmptyExtendedResponse(id);
    }
}
