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

package com.sun.enterprise.admin.server.core;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.admin.jmx.remote.IStringManager;
import com.sun.enterprise.admin.jmx.remote.StringManagerFactory;
/**
 * An implementation of {@link X509TrustManager} that provides support for 
 * managing certificates from an in memory trustore (javax.net.trustStore 
 * system property controls the trustore specification)
 * It checks if the server is trusted and displays the certificate chain 
 * that was received from the server. If the certificate fails the existing 
 * trust chain, communication stops
 */
public class InMemoryX509TrustManager implements X509TrustManager {
    
    private static IStringManager _strMgr = null;
    private KeyStore mTrustStore = null;    
    
    public InMemoryX509TrustManager (String certNickname) {
        try {
            // certificate alias name
            if (mTrustStore == null)
                mTrustStore = getCertTrustore(certNickname);
        } catch (Exception ex) {
            ex.printStackTrace();
            // ignore, trustStore will be null
        }
        if (_strMgr == null) 
            _strMgr = StringManagerFactory.getClientStringManager(
                           InMemoryX509TrustManager.class, null);
    }

    /**
     * Checks if client is trusted given the certificate chain and 
     * authorization type string, e.g. "RSA".
     * @throws {@link CertificateException}
     * @throws {@link UnsupportedOperationException}
     */
    public void checkClientTrusted(X509Certificate[] x509Certificate, 
        String authType) throws CertificateException {
        
        throw new UnsupportedOperationException(
            "Not Implemented for Client Trust Management");
    }
	
    /**
     * Checs if the server is trusted.
     * @param chain The server certificate to be  validated.
     * @param authType
     * @throws CertificateException
     */    
    public void checkServerTrusted(X509Certificate[] chain, String authType) 
    throws CertificateException {
        try {
            checkCertificate(chain);                
        } catch (CertificateException ex) {
            throw ex;
        } 
    }
	
    /**
     * This function validates the cert and ensures that it is trusted.
     * @param chain
     * @throws RuntimeException
     * @throws CertificateException
     */    
    protected void checkCertificate(X509Certificate[] chain) 
    throws CertificateException, IllegalArgumentException {        
        
        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException (_strMgr.getString(
                "emptyServerCertificate"));
        } 
        
        //First ensure that the certificate is valid.
        for (int i = 0 ; i < chain.length ; i ++) 
            chain[i].checkValidity();   
        
        try {
            // if the certificate does not exist then we have an issue. If 
            // the cert was not changed on the DAS post a DAS/NA sync then
            // some DAS with which this NA did not sync up earlier has been
            // conencted to from NA. Throw an exception and abort NA startup
            if (!certificateExists(chain[0]))
                throw new CertificateException(
                    _strMgr.getString("serverCertificateNotTrusted"));
            
        } catch (Exception ex) {
            // mask all exceptions as CertificateException
            // but with correct diagnostic message
            // the exception could be a KeyStoreException or ConfigException
            // while trying to fetch correct trust store
            throw new CertificateException(ex.getMessage());
        }        
    }
    
    public X509Certificate[] getAcceptedIssuers() {
        return ( new X509Certificate[0] );
    }
    
    /**
     * Returns certificate used by jmx connector.
     *
     * @param  certNickname certificate nick name used to find 
     *                      the correct trust store
     *
     * @return  KeyStore    key store containing the cert with the 
     *                      input cert nick name
     *
     * @throws  KeyStoreException  if keystore has not been initialized
     */
    private KeyStore getCertTrustore(String certNickname) 
        throws KeyStoreException {

        // available trust stores
        SecuritySupport secSupp = SecurityUtil.getSecuritySupport();
        KeyStore[] trustStore = secSupp.getTrustStores();
        int i = 0; boolean found = false;
        Certificate cert = null;
        for (; i<trustStore.length; i++) {
            cert = trustStore[i].getCertificate(certNickname);
            if (cert != null) {
                // found target
                found = true;
                break;
            }
        }
        if (found) 
            if (trustStore != null) return trustStore[i];
        return null;
    }

    private boolean certificateExists(X509Certificate x509Certificate) 
    throws KeyStoreException {
        if (mTrustStore == null) return false;
        return (mTrustStore.getCertificateAlias(x509Certificate) == null ? 
                    false : true);
    }
}
