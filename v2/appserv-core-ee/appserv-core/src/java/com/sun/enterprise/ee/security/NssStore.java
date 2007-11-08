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

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.logging.LogDomains;


/**
 *  This class implements the invocation with NSS.
 *  @author Shing Wai Chan
 */
public class NssStore {
    private static Logger _logger = null;
    private static NssStore _instance = null;
    private static ArrayList keyStores = new ArrayList();
    private static ArrayList trustStores = new ArrayList();
    private static ArrayList tokenNames = null;
    private static ArrayList tokenInfoList = null;
    private static String _password = null;
    private static boolean useCaseExact = true;

    static {
        _logger=LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
        //nspr4, plc4, nss3, smime3 are need for conversion

        boolean isWin = OS.isWindows();
        if (!isWin) {
            System.loadLibrary("nspr4");
        } else {
            System.loadLibrary("libnspr4");
        }

        if (!isWin) {
            System.loadLibrary("plc4");
            //XXX suggested by Mark Basler to workaround crash issues in linux
            //    when changing AS_NSS, AS_NSS_BIN with native launcher on
            try {
                System.loadLibrary("softokn3");
            } catch(Throwable ex) {
            }
        } else {
            System.loadLibrary("libplc4");
        }

        System.loadLibrary("nss3");
        System.loadLibrary("smime3");

        System.loadLibrary("asnss");
    }

    public static String getNssDbPassword() 
    {
        if (_password == null) {
            _password = System.getProperty(SystemPropertyConstants.NSS_DB_PASSWORD_PROPERTY);
        }
        return _password;
    }       
    
    private NssStore(String dbDir, boolean initJKS, String password) throws Exception {
        _password = password;
        if (dbDir == null) {
            dbDir = System.getProperty(SystemPropertyConstants.NSS_DB_PROPERTY);
        }
        if (dbDir == null) {
            String msg = "NSS database location is not defined";
            _logger.log(Level.SEVERE,msg); 
            throw new Exception(msg);
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.initNSSNative ...");
        }
        initNSSNative(dbDir);
       
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.initSlotNative");
        }    

        // init internal slot
        initSlotNative(null, getNssDbPassword());
        String[] tokNames = getTokenNamesAsArray();
        Map name2PwdMap = IdentityManager.getMap();
        if (tokNames != null && tokNames.length > 0) {
            for (int i = 0; i < tokNames.length; i++) {
                String tokenName = tokNames[i];
                initSlotNative(tokenName, (String)name2PwdMap.get(tokenName));
            }
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.initSlotNative Done");
        }    

        if (initJKS) {
            initStores();
        }
    }

    public static synchronized NssStore getInstance() throws Exception {
        return getInstance(null, true);
    }
    
    public static synchronized NssStore getInstance(String dbDir, boolean initJKS) 
        throws Exception 
    {
        return getInstance(dbDir, initJKS, null);
    }
    
    public static synchronized NssStore getInstance(String dbDir, boolean initJKS, 
        String password) throws Exception 
    {
        if (_instance == null) {
            _instance = new NssStore(dbDir, initJKS, password);
        }
        return _instance;
    }

    public static synchronized void closeInstance() {
        if (_instance != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "NssStore.close");
            }    
            _instance.close();
            _instance = null;
            _password = null;
        }
    }
    
    // JNI native methods
    private native void initNSSNative(String dir) throws Exception;

    /**
     * Initialize named slot with given password.
     * @param tokenName
     * @param password
     */
    private native void initSlotNative(String tokenName, String password)
            throws Exception;

    /**
     * Get Keys and Certs bytes for a given tokenName.
     * @param keyMap
     * @param certMap
     * @param tokenName
     * @param password
     * @exception Exception
     */
    private native void getKeysAndCertificatesNative(
            HashMap keyMap, HashMap certMap, String tokenName, String password)
            throws Exception;

    /**
     * Get CA Certs bytes.
     * @param certMap
     * @param password
     * @exception Exception
     */
    private native void getCACertificatesNative(HashMap certMap,
            String password) throws Exception;
    
    /**
     * This returns an ArrayList of tokenName other than internal slot
     * that requires login.
     * It is used for password prompting in startup time.
     * @return an ArrrayList of tokenName other than internal slot.
     */
    private native ArrayList getTokenNamesNative() throws Exception;
    
    /**
     * This returns an ArrayList of NssTokenInfo containing token name, 
     * library location and slot id (slotListIndex).
     * @return a ArrayList of NssTokenInfo.
     */
    private native ArrayList getTokenInfoListNative() throws Exception;

    /**
     * Changes the NSS database password for the internal slot
     * @param password
     * @param newPassword
     * @throws Exception
     */    
    public native void changePassword(String password, String newPassword) 
        throws Exception;
    
    /**
     * Close the nss database     
     */    
    private native void close();
    
    // -------------------------------------------------------------

    List getKeyStores() throws Exception {
        return (ArrayList)keyStores.clone();
    }

    List getTrustStores() throws Exception {
        return (ArrayList)trustStores.clone();
    }

    List getTokenNames() throws Exception {
        if (tokenNames == null) {
            tokenNames = getTokenNamesNative();
        }
        if (tokenNames == null) {
            tokenNames = new ArrayList();
        }    

        return (List)tokenNames.clone();
    }

    public String[] getTokenNamesAsArray() throws Exception {
        if (tokenNames == null) {
            tokenNames = (ArrayList)getTokenNames();
        }
        
        return (String[])tokenNames.toArray(new String[tokenNames.size()]);
    }

    /**
     * This method returns a List of tokenInfo object.
     * It should only be called within this package.
     */
    List getTokenInfoList() throws Exception {
        if (tokenInfoList == null) {
            tokenInfoList = getTokenInfoListNative();
            if (tokenInfoList == null) {
                tokenInfoList = new ArrayList();
            }
        }
        return (List)tokenInfoList.clone();
    }
    
    private void initStores() throws Exception {
        String[] tokNames = getTokenNamesAsArray();
        Map name2PwdMap = IdentityManager.getMap();
        if (tokNames != null && tokNames.length > 0) {
            for (int i = 0; i < tokNames.length; i++) {
                String tName = tokNames[i];
                initStore(tName, (String)name2PwdMap.get(tName));
            }
        }
        //init internal store
        initStore(null, getNssDbPassword());
        //init CA certs
        initCAStore(getNssDbPassword());
    }

    /**
     * This is an internal API getting a keystore for keys and certs storage.
     * First, it tries to get a CaseExactJKS keystore which is new to
     * JDK 1.4.2_07 that will work better with PKCS11 keystore.
     * If it fails, then it will get JKS keystore.
     * @throws KeyStoreException
     */
    private KeyStore getJavaKeyStore() throws KeyStoreException {
        if (useCaseExact) {
            try {
                return KeyStore.getInstance("CaseExactJKS");
            } catch(KeyStoreException kex) {
                // CaseExactJKS does not exists in this JDK
                useCaseExact = false;
            }
        }

        return KeyStore.getInstance("JKS");
    }

    private void initStore(String tokenName, String password) throws Exception {

        KeyStore keyStore = getJavaKeyStore();
        KeyStore trustStore = getJavaKeyStore();
        keyStore.load(null, password.toCharArray());
        trustStore.load(null, password.toCharArray());

        KeyStore ks = KeyStore.getInstance("PKCS12");

        HashMap keyMap = new HashMap();
        HashMap certMap = new HashMap();

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.getKeysAndCertificatesNative ...");
        }
        getKeysAndCertificatesNative(keyMap, certMap, tokenName, password);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.getKeysAndCertificatesNative Done");
        }
        Iterator keyAliasIter = keyMap.keySet().iterator();
        while (keyAliasIter.hasNext()) {
            String alias = (String)keyAliasIter.next();
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "NssStore.keyAlias = " + alias);
            }
            byte[] pkcs12bs = (byte[])keyMap.get(alias);

            BufferedInputStream bufInput = null;
            try {
                if (pkcs12bs != null && pkcs12bs.length > 0) {
                    bufInput = new BufferedInputStream(new ByteArrayInputStream(pkcs12bs));
                }
                ks.load(bufInput, password.toCharArray());
            } finally {
                if (bufInput != null) {
                    try {
                        bufInput.close();
                    } catch(Exception ex) {
                    }
                }
            }

            Key key = ks.getKey(alias, password.toCharArray());
            Certificate[] chain = ks.getCertificateChain(alias);
            keyStore.setKeyEntry(alias, key, password.toCharArray(), chain);
        }

        processCertificates(certMap, trustStore);

        // trust private key cert
        Enumeration keyAlias = keyStore.aliases();
        while (keyAlias.hasMoreElements()) {
            String alias = (String)keyAlias.nextElement();
            trustStore.setCertificateEntry(alias, keyStore.getCertificate(alias));
        }

        keyStores.add(keyStore);
        trustStores.add(trustStore);
    }

    void initCAStore(String password) throws Exception {

        KeyStore trustStore = getJavaKeyStore();
        trustStore.load(null, password.toCharArray());

        HashMap certMap = new HashMap();

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.getCACertificatesNative ...");
        }
        getCACertificatesNative(certMap, password);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "NssStore.getCACertificatesNative Done");
        }

        processCertificates(certMap, trustStore);
        trustStores.add(trustStore);
    }

    private void processCertificates(HashMap certMap, KeyStore trustStore)
            throws Exception {

        Iterator certAliasIter = certMap.keySet().iterator();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        while (certAliasIter.hasNext()) {
            String alias = (String)certAliasIter.next();
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "NssStore.certAlias = " + alias);
            }
            byte[] derBytes = (byte[])certMap.get(alias);

            if (derBytes != null && derBytes.length > 0) {
                BufferedInputStream bufInput = null;
                Collection certColl = null;
                try {
                    bufInput = new BufferedInputStream(new ByteArrayInputStream(derBytes));
                    certColl = certFactory.generateCertificates(bufInput);
                } catch(Exception ex) {
                    //XXX we do this because JDK 1.4 cannot load certs with large key
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, "Can't load cert: " + alias, ex);
                    } else if (_logger.isLoggable(Level.INFO)) {
                        _logger.log(Level.INFO, "nss.cantloadcert", alias);
                    }
                } finally {
                    if (bufInput != null) {
                        try {
                            bufInput.close();
                        } catch(Exception ex) {
                        }
                    }
                }

                if (certColl != null) {
                    Iterator certIter = certColl.iterator();
                    if (certIter.hasNext()) {
                        X509Certificate cert = (X509Certificate)certIter.next();
                        trustStore.setCertificateEntry(alias, cert);
                    }
                } else if (_logger.isLoggable(Level.INFO)) {
                    _logger.log(Level.INFO, "nss.cantloadcert", alias);
                }
            }
        }
    }

    // -------------------------------------------------------------
    // for testing only
    // LD_LIBRARY_PATH must contains NSS libraries, libjvm.so, libverifier.so
    // CLASSPATH must contains appserv-se.jar, appserv-rt.jar, appserv-admin.jar, j2ee.jar
    // java -Dcom.sun.appserv.nss.db=$AS_ROOT/domains/domain1/config -Dcom.sun.appserv.nss.db.password=changeit com.sun.enterprise.ee.security.NssStore
    public static void main(String args[]) throws Exception {
        String dbDir = System.getProperty(SystemPropertyConstants.NSS_DB_PROPERTY);
        String pwd = getNssDbPassword();
        System.out.println("dbDir = " + dbDir + ", password = " + pwd);
     
        NssStore nssStore = NssStore.getInstance();
        KeyStore ks = (KeyStore)nssStore.getKeyStores().get(0);
        KeyStore ts = (KeyStore)nssStore.getTrustStores().get(0);
        
        dumpKeyStore(ks, pwd);
        dumpKeyStore(ts, pwd);
    }

    private static void dumpKeyStore(KeyStore ks, String pwd) throws Exception {
        System.out.println("Dump keystore: " + ks);
        Enumeration aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = (String)aliases.nextElement();
            if (ks.isKeyEntry(alias)) {
                System.out.println("\tkey = " + alias + ": " +
                        ks.getKey(alias, pwd.toCharArray()));
            } else {
                System.out.println("\tcert = " + alias + ": " +
                        ks.getCertificate(alias));
            }
        }
    }
}
