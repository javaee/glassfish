/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.connection;

import java.util.Map;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateException;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

/**
 * Validates server certificate chain by comparing the certificate with 
 * in-memory cert database. 
 */
class X509TrustManagerImpl implements X509TrustManager {

    /**
     * Constructor.
     *
     * @param  serverName  application server name
     * @param  cert  application server certificate
     */
    public X509TrustManagerImpl(String serverName, X509Certificate cert) {
        
        _certDB.put(serverName, cert);
    }

    /**
     * Removes the certificate entry for the given server instance name.
     *
     * @param  serverName  application server name
     *
     * @return   certificate of the removed server
     */
    static X509Certificate removeCertificate(String serverName) {
        return (X509Certificate)_certDB.remove(serverName);
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) 
            throws IllegalArgumentException, CertificateException {

        boolean trusted = false;

        if (chain == null) {
            throw new IllegalArgumentException();
        }
        for (int i=0; i<chain.length; i++) {
            if ( isCertTrusted(chain[i]) ) {
                trusted = true;
                break;
            }
        }

        // did not find any trusted certificate
        if (!trusted) {

            LogDomains.getLogger().warning(
                "Server certificate chain is unknown; can not be trusted.");

            throw new CertificateException();
        }

    } 

    /**
     * Returns true if given certificate is found in the 
     * certificate database. 
     *
     * @param  cert  certificate to be verified
     */
    private boolean isCertTrusted(X509Certificate cert) {

        boolean trusted = false;
        try {
            Collection collection = _certDB.values();
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                X509Certificate c2 = (X509Certificate) iter.next();

                // found certificate in the database
                if ( isCertEqual(cert, c2) ) {
                    trusted = true;
                    break;
                }
            }
        } catch (Exception e) {
            trusted = false;
        }

        return trusted;
    }

    /**
     * Returns true if given two certificates are equal. This method 
     * compares the subject and the signature of the two certificates.
     */
    private boolean isCertEqual(X509Certificate cert1, X509Certificate cert2) {

        boolean equal = false;

        try {
            BigInteger serial1 = cert1.getSerialNumber();
            BigInteger serial2 = cert2.getSerialNumber();

            // both serial numbers match
            if ( (serial1 != null) && (serial1.equals(serial2)) ) {

                X500Principal p1 = cert1.getSubjectX500Principal();
                X500Principal p2 = cert2.getSubjectX500Principal();

                // found matching principal
                if ((p1 != null) && (p1.equals(p2))) {

                    // found matching signature
                    if ( isSignatureEqual(cert1, cert2) ) {
                        equal = true;
                    }
                }
            }
        } catch (Exception e) {
            equal = false;
        }

        return equal;
    }

    /**
     * Returns true if signature of given two certificates are equal.
     *
     * @param  c1  first X509Certificate to be compared
     * @param  c2  second X509Certificate to be compared
     *
     * @return  true if signature matches 
     */
    private boolean isSignatureEqual(X509Certificate c1, X509Certificate c2) {

        boolean equal = false;

        try {
            byte[] s1  = c1.getSignature();
            byte[] s2  = c2.getSignature();
        
            // both signature length is same
            if (s1.length == s2.length) {
                boolean  allBytesEqual = true;
                for (int j=0; j<s1.length; j++) {
                    if (s1[j] != s2[j]) {
                        allBytesEqual = false;
                        break;
                    }
                }
                // both signature are same
                if (allBytesEqual) {
                    equal = true;
                }
            }
        } catch (Exception e) {
            equal = false;
        }

        return equal;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        return;
    }

    // ---- VARIABLES - PRIVATE --------------------------------
    private static Map _certDB = new Hashtable();
}
