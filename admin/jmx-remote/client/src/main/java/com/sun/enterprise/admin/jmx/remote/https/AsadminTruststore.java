/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.jmx.remote.https;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import javax.crypto.spec.SecretKeySpec;

import java.util.Enumeration;

/**
 * This class implements an adapter for password manipulation a JCEKS.
 * @author Shing Wai Chan
 */
public class AsadminTruststore {
    private static final String ASADMIN_TRUSTSTORE = ".asadmintruststore";
    private KeyStore _keyStore = null;
    private File _keyFile = null;        
    private char[] _password = null;
    
    public static final String CLIENT_TRUSTSTORE_PROPERTY = 
        "javax.net.ssl.trustStore";
    public static final String CLIENT_TRUSTSTORE_PASSWORD_PROPERTY =
        "javax.net.ssl.trustStorePassword";
    
    public static File getAsadminTruststore()
    {
        String location = System.getProperty(CLIENT_TRUSTSTORE_PROPERTY);
        if (location == null) {
            return new File(System.getProperty("user.home") + File.separator + ASADMIN_TRUSTSTORE);
        } else {
            return new File(location);
        }
    }
    
    public static String getAsadminTruststorePassword()
    {
        return System.getProperty(CLIENT_TRUSTSTORE_PASSWORD_PROPERTY, 
            "changeit");
    }        
    
    public AsadminTruststore() throws CertificateException, IOException, 
        KeyStoreException, NoSuchAlgorithmException 
    {                 
        this(getAsadminTruststorePassword());
    }
            
    public AsadminTruststore(String password) throws CertificateException, IOException, 
        KeyStoreException, NoSuchAlgorithmException 
    {                 
        init(getAsadminTruststore(), password);
    }
    
    private void init(File keyfile, String password)
        throws CertificateException, IOException,
        KeyStoreException, NoSuchAlgorithmException 
    {
        _keyFile = keyfile;
        _keyStore = KeyStore.getInstance("JKS"); 
        _password = password.toCharArray();
        BufferedInputStream bInput = null;        
        if (_keyFile.exists()) {
            bInput = new BufferedInputStream(new FileInputStream(_keyFile));
        }
        try {            
            //load must be called with null to initialize an empty keystore
            _keyStore.load(bInput, _password);
            if (bInput != null) {
                bInput.close();
                bInput = null;
            } 
        } finally {
             if (bInput != null) {
                 try {
                     bInput.close();
                 } catch(Exception ex) {
                     //ignore we are cleaning up
                 }
             }
        }        
    }   
    
    public boolean certificateExists(Certificate cert) throws KeyStoreException
    {
        return (_keyStore.getCertificateAlias(cert) == null ? false : true);
    }
    
    public void addCertificate(String alias, Certificate cert) throws KeyStoreException, IOException, 
        NoSuchAlgorithmException, CertificateException
    {
        _keyStore.setCertificateEntry(alias, cert);
        writeStore();
    }
    
    public void writeStore() throws KeyStoreException, IOException, 
        NoSuchAlgorithmException, CertificateException
    {
         BufferedOutputStream boutput = null;

         try {
             boutput = new BufferedOutputStream(
                     new FileOutputStream(_keyFile));
             _keyStore.store(boutput, _password);
             boutput.close();
             boutput = null;
         } finally {
             if (boutput != null) {
                 try {
                     boutput.close();
                 } catch(Exception ex) {
                     //ignore we are cleaning up
                 }
             }
         }
    }    
}
