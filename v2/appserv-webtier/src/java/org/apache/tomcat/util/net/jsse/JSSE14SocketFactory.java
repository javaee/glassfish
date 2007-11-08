

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.tomcat.util.net.jsse;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Vector;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPathParameters;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.tomcat.util.res.StringManager;

/*
  1. Make the JSSE's jars available, either as an installed
     extension (copy them into jre/lib/ext) or by adding
     them to the Tomcat classpath.
  2. keytool -genkey -alias tomcat -keyalg RSA
     Use "changeit" as password ( this is the default we use )
 */

/**
 * SSL server socket factory. It _requires_ a valid RSA key and
 * JSSE. 
 *
 * @author Harish Prabandham
 * @author Costin Manolache
 * @author Stefan Freyr Stefansson
 * @author EKR -- renamed to JSSESocketFactory
 * @author Jan Luehe
 */
public class JSSE14SocketFactory  extends JSSESocketFactory {

    private static StringManager sm =
        StringManager.getManager("org.apache.tomcat.util.net.jsse.res");

    public JSSE14SocketFactory () {
    }

    
    /**
     * Reads the keystore and initializes the SSL socket factory.
     */
    /* SJSAS 6439313
    void init() throws IOException{
     */
    // START SJSAS 6439313
    public void init() throws IOException{
    // END SJSAS 6439313
        try {

            String clientAuthStr = (String) attributes.get("clientauth");
            if (clientAuthStr != null){
                clientAuth = Boolean.valueOf(clientAuthStr).booleanValue();
            }

            // SSL protocol variant (e.g., TLS, SSL v3, etc.)
            String protocol = (String) attributes.get("protocol");
            if (protocol == null) {
                protocol = defaultProtocol;
            }

            // Certificate encoding algorithm (e.g., SunX509)
            String algorithm = (String) attributes.get("algorithm");
            if (algorithm == null) {
                algorithm = defaultAlgorithm;
            }

            // Create and init SSLContext
            /* SJSAS 6439313
            SSLContext context = SSLContext.getInstance(protocol);
             */
            
            // START SJSAS 6439313
            context = SSLContext.getInstance(protocol);
            // END SJSAS 6439313 
            
            // Configure SSL session timeout and cache size
            configureSSLSessionContext(context.getServerSessionContext());
                
            String trustAlgorithm = (String)attributes.get("truststoreAlgorithm");
            if (trustAlgorithm == null) {
                trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            }

            context.init(getKeyManagers(algorithm,
                                        (String) attributes.get("keyAlias")),
                         getTrustManagers(trustAlgorithm),
                         new SecureRandom());

            // create proxy
            sslProxy = context.getServerSocketFactory();

            // Determine which cipher suites to enable
            String requestedCiphers = (String)attributes.get("ciphers");
            if (requestedCiphers != null) {
                enabledCiphers = getEnabledCiphers(requestedCiphers,
                                                   sslProxy.getSupportedCipherSuites());
            }

        } catch(Exception e) {
            if( e instanceof IOException )
                throw (IOException)e;
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Gets the initialized key managers.
     */
    protected KeyManager[] getKeyManagers(String algorithm,
                                          String keyAlias)
                throws Exception {

        KeyManager[] kms = null;

        String keystorePass = getKeystorePassword();

        KeyStore ks = getKeystore(keystorePass);
        if (keyAlias != null && !ks.isKeyEntry(keyAlias)) {
            throw new IOException(sm.getString("jsse.alias_no_key_entry", keyAlias));
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(ks, keystorePass.toCharArray());

        kms = kmf.getKeyManagers();
        if (keyAlias != null) {
            // START SJSAS 6266949
            /*
            if (JSSESocketFactory.defaultKeystoreType.equals(keystoreType)) {
                keyAlias = keyAlias.toLowerCase();
            }
            */
            //END SJSAS 6266949
            
            for(int i=0; i<kms.length; i++) {
                kms[i] = new JSSEKeyManager((X509KeyManager)kms[i], keyAlias);
            }
        }

        return kms;
    }

    /**
     * Gets the intialized trust managers.
     */
    protected TrustManager[] getTrustManagers(String algorithm)
                throws Exception {

        String crlf = (String) attributes.get("crlFile");

        TrustManager[] tms = null;

        KeyStore trustStore = getTrustStore();
        if (trustStore != null) {
            if (crlf == null) {
                TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(algorithm);
                tmf.init(trustStore);
                tms = tmf.getTrustManagers();
            } else {
                TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(algorithm);
                CertPathParameters params = getParameters(algorithm, crlf,
                                                          trustStore);
                ManagerFactoryParameters mfp = 
                    new CertPathTrustManagerParameters(params);
                tmf.init(mfp);
                tms = tmf.getTrustManagers();
            }
        }

        return tms;
    }


    /**
     * Return the initialization parameters for the TrustManager.
     * Currently, only the default <code>PKIX</code> is supported.
     * 
     * @param algorithm The algorithm to get parameters for.
     * @param crlf The path to the CRL file.
     * @param trustStore The configured TrustStore.
     * @return The parameters including the CRLs and TrustStore.
     */
    protected CertPathParameters getParameters(String algorithm, 
                                               String crlf, 
                                               KeyStore trustStore)
            throws Exception {

        CertPathParameters params = null;
        if ("PKIX".equalsIgnoreCase(algorithm)) {
            PKIXBuilderParameters xparams =
                new PKIXBuilderParameters(trustStore, 
                                          new X509CertSelector());
            Collection crls = getCRLs(crlf);
            CertStoreParameters csp = new CollectionCertStoreParameters(crls);
            CertStore store = CertStore.getInstance("Collection", csp);
            xparams.addCertStore(store);
            xparams.setRevocationEnabled(true);
            String trustLength = (String)attributes.get("trustMaxCertLength");
            if (trustLength != null) {
                try {
                    xparams.setMaxPathLength(Integer.parseInt(trustLength));
                } catch(Exception ex) {
                    log.warn("Bad maxCertLength: " + trustLength);
                }
            }
            params = xparams;
        } else {
            throw new CRLException("CRLs not supported for type: "
                                   + algorithm);
        }
        return params;
    }


    /**
     * Load the collection of CRLs.
     */
    protected Collection<? extends CRL> getCRLs(String crlf) 
            throws IOException, CRLException, CertificateException {

        File crlFile = new File(crlf);
        if (!crlFile.isAbsolute()) {
            crlFile = new File(System.getProperty("catalina.base"), crlf);
        }
        Collection<? extends CRL> crls = null;
        InputStream is = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            is = new FileInputStream(crlFile);
            crls = cf.generateCRLs(is);
        } catch(IOException iex) {
            throw iex;
        } catch(CRLException crle) {
            throw crle;
        } catch(CertificateException ce) {
            throw ce;
        } finally { 
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                }
            }
        }

        return crls;
    }


    protected void setEnabledProtocols(SSLServerSocket socket, String []protocols){
        if (protocols != null) {
            socket.setEnabledProtocols(protocols);
        }
    }

    protected String[] getEnabledProtocols(SSLServerSocket socket,
                                           String requestedProtocols){
        String[] supportedProtocols = socket.getSupportedProtocols();

        String[] enabledProtocols = null;

        if (requestedProtocols != null) {
            Vector vec = null;
            String protocol = requestedProtocols;
            int index = requestedProtocols.indexOf(',');
            if (index != -1) {
                int fromIndex = 0;
                while (index != -1) {
                    protocol = requestedProtocols.substring(fromIndex, index).trim();
                    if (protocol.length() > 0) {
                        /*
                         * Check to see if the requested protocol is among the
                         * supported protocols, i.e., may be enabled
                         */
                        for (int i=0; supportedProtocols != null
                                     && i<supportedProtocols.length; i++) {
                            if (supportedProtocols[i].equals(protocol)) {
                                if (vec == null) {
                                    vec = new Vector();
                                }
                                vec.addElement(protocol);
                                break;
                            }
                        }
                    }
                    fromIndex = index+1;
                    index = requestedProtocols.indexOf(',', fromIndex);
                } // while
                protocol = requestedProtocols.substring(fromIndex);
            }

            if (protocol != null) {
                protocol = protocol.trim();
                if (protocol.length() > 0) {
                    /*
                     * Check to see if the requested protocol is among the
                     * supported protocols, i.e., may be enabled
                     */
                    for (int i=0; supportedProtocols != null
                                 && i<supportedProtocols.length; i++) {
                        if (supportedProtocols[i].equals(protocol)) {
                            if (vec == null) {
                                vec = new Vector();
                            }
                            vec.addElement(protocol);
                            break;
                        }
                    }
                }
            }           

            if (vec != null) {
                enabledProtocols = new String[vec.size()];
                vec.copyInto(enabledProtocols);
            }
        }

        return enabledProtocols;
    }


    /*
     * Configures the given SSLSessionContext.
     *
     * @param sslSessionCtxt The SSLSessionContext to configure
     */
    private void configureSSLSessionContext(SSLSessionContext sslSessionCtxt) {

        String attrValue = (String) attributes.get("sslSessionTimeout");
        if (attrValue != null) {
            sslSessionCtxt.setSessionTimeout(
                Integer.valueOf(attrValue).intValue());
        }

        attrValue = (String) attributes.get("ssl3SessionTimeout");
        if (attrValue != null) {
            sslSessionCtxt.setSessionTimeout(
                Integer.valueOf(attrValue).intValue());
        }
     
        attrValue = (String) attributes.get("sslSessionCacheSize");
        if (attrValue != null) {
            sslSessionCtxt.setSessionCacheSize(
                Integer.valueOf(attrValue).intValue());
        }
    }

}
