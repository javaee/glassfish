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
package com.sun.enterprise.ee.security;

import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.server.pluggable.SecuritySupport;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.apache.tomcat.util.net.jsse.JSSE14SocketFactory;
import org.apache.tomcat.util.net.jsse.JSSEKeyManager;

import com.sun.enterprise.security.SSLUtils;

/**
 * NSS Socket Factory.
 * 
 * @author Jean-Francois Arcand
 */
public class NssSocketFactory  extends JSSE14SocketFactory {

    NssSocketFactory() {
        super();        
    }
       
    
    /*
     * Gets the SSL server's keystore.
     */
    protected KeyStore getKeystore(String pass) throws IOException {
        String keyAlias = (String)attributes.get("keyAlias");
        String token = getTokenFromKeyAlias(keyAlias);        
        SecuritySupport secSupp = SecurityUtil.getSecuritySupport();
        KeyStore ks = secSupp.getKeyStore(token);
        if (ks==null) {
            throw new IOException("keystore not found for token " + token);
        }
        return ks;
    }
    
    
    /*
     * Gets the SSL server's truststore. JDK 1.5 provider has 
     * issues in loading some of the NSStrust certs. In is case, we have our native 
     * code to load all trust certs and put it into a keystore. That is why we 
     * will have more than one keyStores even in flat file NSS. In General, 
     * we even cannot assume there is only one keystores. In case of hardware 
     * accelerators, there will be multiple (one for earch HW).  
     */
    protected KeyStore getTrustStore() throws IOException {
        try {
            return SSLUtils.getMergedTrustStore();
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Gets the initialized key managers.
     */
    protected KeyManager[] getKeyManagers(String algorithm,
                                          String keyAlias)
                throws Exception {
        KeyManager[] kms = null;
        SecuritySupport secSupp = SecurityUtil.getSecuritySupport();
        String token=getTokenFromKeyAlias(keyAlias);
        String certAlias = getCertAliasFromKeyAlias(keyAlias);
        String keystorePass = secSupp.getKeyStorePassword(token);
        KeyStore ks = secSupp.getKeyStore(token);
        if (ks==null) {
            throw new IOException("keystore not found for token " + token);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(ks, keystorePass.toCharArray());
        kms = kmf.getKeyManagers();
        for(int i=0; certAlias!=null && i<kms.length; i++) {
            kms[i] = new JSSEKeyManager((X509KeyManager)kms[i], certAlias);
        }
        return kms;
    }
    
    private static String getTokenFromKeyAlias(String keyAlias) {
        String token = null;
        if (keyAlias!=null) {
            int idx = keyAlias.indexOf(':');
            if (idx != -1) {
                token = keyAlias.substring(0, idx);
            }
        }        
        if (token==null) {
            token = EESecuritySupportImpl.INTERNAL_TOKEN;
        } else {
            token = token.trim();
        }
        return token;
    }

    /**
     * @param keyAlias format is "token:certAlias" or "certAlias"
     *
     * in Appserver design, the "token" name part serves two purposes
     * (1) identify the token in NSS DB
     *     e.g. ./modutil -list -dbdir /export/sonia/appserver/domains/domain1/config/
     * (2) "token:certAlias" WHOLE string is the cert alias in NSS
     *     for example ("nobody@test" is the token name):
     *     ./certutil -L  -h nobody@test  -d /export/sonia/appserver/domains/domain1/config
     *     Enter Password or Pin for "nobody@test":
     *     nobody@test:mps                                              u,u,u
     *     nobody@test:J2EESQECA                                        u,u,u
     *     nobody@test:AppServer1                                       u,u,u
     *     nobody@test:Server-Cert                                      u,u,u
     *
     * JDK5 KeyStore of type "SunPKCS11" identifies cert by "certAlias" part of "token:certAlias"
     */
    private static String getCertAliasFromKeyAlias(String keyAlias) {
        String certAlias = null;
        if (keyAlias!=null) {
            int idx = keyAlias.indexOf(':');
            if (idx == -1) {
                certAlias = keyAlias;
            } else {
                idx++;
                if (idx < keyAlias.length()-1 ) {
                    certAlias = keyAlias.substring(idx);
                }
            } 
        }
        if (certAlias!=null)
            certAlias = certAlias.trim();
        return certAlias;
    }
}
