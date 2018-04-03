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

/**
 * This class implements the LDAPv3 Extended Request for BulkImportFinished.
 * The BulkImportFinishedRequest mark the end of bulk import operation. 
 * <p>
 * The bulk import extended operations allow importing entries
 * remotely with a series of LDAP add operations. When a
 * {@link BulkImportStartRequest} is sent the import must end by
 * sending the <tt>BulkImportFinishedRequest</tt> before
 * the normal LDAP operations can resume. Only LDAP add operations are legal
 * between BulkImportStart and BulkImportFinished operations.
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
 * The object identifier for BulkImportFinished is 2.16.840.1.113730.3.5.8
 * and there is no extended request value.
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
 *                                       namingContext));
 *     System.out.println("Bulk import operation begins");
 *
 *     // Add entries
 *     ctx.createSubcontext(entryName, entryAttrs);
 *           :
 *           :
 *     // Bulk import done
 *     ctx.extendedOperation(new BulkImportFinishedRequest());
 *     System.out.println("Bulk import operation finished");
 * </pre>
 *
 * @see BulkImportStartRequest
 * @author Jayalaxmi Hangal
 */

public class BulkImportFinishedRequest implements ExtendedRequest {

    /**
     * The BulkImportFinished extended request's assigned object identifier
     * is  2.16.840.1.113730.3.5.8 
     */
    public static final String OID = "2.16.840.1.113730.3.5.8";

    private static final long serialVersionUID = 4555688155005767980L;

    /**
     * Constructs a BulkImportFinished extended request.
     */
    public BulkImportFinishedRequest() {
    }

    /**
     * Retrieves the BulkImportFinished request's object identifier string.
     *
     * @return The non-null object identifier string.
     */
    public String getID() {
        return OID;
    }

    /**
     * Retrieves the BulkImportFinished request's ASN.1 BER encoded value.
     * Since the request has no defined value, null is always
     * returned.
     *
     * @return The null value.
     */
    public byte[] getEncodedValue() {
        return null;
    }

    /**
     * Creates an extended response object that corresponds to the 
     * LDAP BulkImportFinished extended request.
     */
    public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
        int offset, int length) throws NamingException {

        // Confirm that the object identifier is correct
        if ((id != null) && (!id.equals(OID))) {
            throw new ConfigurationException(
                "BulkImportFinished received the following response instead of " +
                OID + ": " + id);
        }
        return new EmptyExtendedResponse(id);
    }
}
