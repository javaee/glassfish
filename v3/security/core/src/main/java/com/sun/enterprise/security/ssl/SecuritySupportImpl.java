/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.security.ssl;

import com.sun.enterprise.security.common.Util;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.KeyStore;
import java.security.Provider;

//V3:Commented import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

/**
 * This implements SecuritySupport used in PluggableFeatureFactory.
 * @author Shing Wai Chan
 */
// TODO: when we have two SecuritySupport implementations,
// we create Habitat we'll select which SecuritySupport implementation to use.
@Service
@Scoped(Singleton.class)
public class SecuritySupportImpl implements SecuritySupport {

    private static final String keyStoreProp = "javax.net.ssl.keyStore";
    private static final String trustStoreProp = "javax.net.ssl.trustStore";
    
    protected static final Logger _logger =
            LogDomains.getLogger(SecuritySupportImpl.class, LogDomains.SECURITY_LOGGER);

    protected static boolean initialized = false;
    protected static final List<KeyStore> keyStores = new ArrayList<KeyStore>();
    protected static final List<KeyStore> trustStores = new ArrayList<KeyStore>();
    protected static final List<char[]> keyStorePasswords = new ArrayList<char[]>();
    protected static final List<String> tokenNames = new ArrayList<String>();

    private MasterPasswordImpl masterPasswordHelper = null;
    private static boolean instantiated = false;

    public SecuritySupportImpl() {
        this(true);
    }

    protected SecuritySupportImpl(boolean init) {
        if (init) {
            initJKS();
        }
    }

    private void initJKS() {
        String keyStoreFileName = null;
        String trustStoreFileName = null;

        if (Util.isEmbeddedServer()) {
            try {
                keyStoreFileName = Util.writeConfigFileToTempDir("keystore.jks").getAbsolutePath();
                trustStoreFileName = Util.writeConfigFileToTempDir("cacerts.jks").getAbsolutePath();
            } catch (IOException ex) {
                _logger.log(Level.SEVERE, "Error obtaining keystore and truststore files for embedded server", ex);
            }
        } else {
            keyStoreFileName = System.getProperty(keyStoreProp);
            trustStoreFileName = System.getProperty(trustStoreProp);
        }

        char[] keyStorePass = null;
        char[] trustStorePass = null;
        if (!isInstantiated()) {
            if (masterPasswordHelper == null && Globals.getDefaultHabitat() != null) {
                masterPasswordHelper = Globals.getDefaultHabitat().getByType(MasterPasswordImpl.class);
            }
            if (masterPasswordHelper instanceof MasterPasswordImpl) {
                keyStorePass = masterPasswordHelper.getMasterPassword();
                trustStorePass = keyStorePass;
            }
        }
        if (keyStorePass == null) {
            keyStorePass = System.getProperty(SSLUtils.KEYSTORE_PASS_PROP, SSLUtils.DEFAULT_KEYSTORE_PASS).toCharArray();
            trustStorePass = System.getProperty(SSLUtils.TRUSTSTORE_PASS_PROP, SSLUtils.DEFAULT_TRUSTSTORE_PASS).toCharArray();
        }

        if (!initialized) {
            loadStores(
                    null, 
                    null, 
                   keyStoreFileName, 
                    keyStorePass,
                    SSLUtils.getKeyStoreType(),
                    trustStoreFileName, 
                    trustStorePass,
                    SSLUtils.getTrustStoreType());
            Arrays.fill(keyStorePass, ' ');
            Arrays.fill(trustStorePass, ' ');
            initialized = true;
        }
    }
    
    private static synchronized boolean isInstantiated() {
        if (!instantiated) {
            instantiated = true;
            return false;
        }
        return true;
    }

    /**
     * This method will load keystore and truststore and add into
     * corresponding list.
     * @param tokenName
     * @param provider
     * @param keyStorePass
     * @param keyStoreFile
     * @param keyStoreType
     * @param trustStorePass
     * @param trustStoreFile
     * @param trustStoreType
     */
    /*protected synchronized static void loadStores(String tokenName, 
            String storeType, Provider provider,
            String keyStoreFile, String keyStorePass, 
            String trustStoreFile, String trustStorePass) {*/
    protected synchronized static void loadStores(
                       String tokenName,
                       Provider provider,
                       String keyStoreFile,
                       char[] keyStorePass,
                       String keyStoreType,
                       String trustStoreFile,
                       char[] trustStorePass,
                       String trustStoreType) {

        try {
            KeyStore keyStore = loadKS(keyStoreType, provider, keyStoreFile,
                keyStorePass);
            KeyStore trustStore = loadKS(trustStoreType, provider,trustStoreFile,
                trustStorePass);
            keyStores.add(keyStore);
            trustStores.add(trustStore);
            keyStorePasswords.add(Arrays.copyOf(keyStorePass, keyStorePass.length));
            tokenNames.add(tokenName);
        } catch(Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * This method load keystore with given keystore file and
     * keystore password for a given keystore type and provider.
     * It always return a non-null keystore.
     * @param keyStoreType
     * @param provider
     * @param keyStoreFile
     * @param keyStorePass
     * @retun keystore loaded
     */
    private static KeyStore loadKS(String keyStoreType, Provider provider,
		    String keyStoreFile, char[] keyStorePass)
		    throws Exception
    {
        KeyStore ks = null;
        if (provider != null) {
            ks = KeyStore.getInstance(keyStoreType, provider);
        } else {
            ks = KeyStore.getInstance(keyStoreType);
        }
        char[] passphrase = keyStorePass;

        FileInputStream istream = null;
        BufferedInputStream bstream = null;
        try {
            if (keyStoreFile != null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Loading keystoreFile = {0}, keystorePass = {1}",
                            new Object[]{keyStoreFile, keyStorePass});
	        }
                istream = new FileInputStream(keyStoreFile);
                bstream = new BufferedInputStream(istream);
            }

            ks.load(bstream, passphrase);
        } finally {
            if (bstream != null) {
	        bstream.close();
            }
            if (istream != null) {
	        istream.close();
            }
        }
	return ks;
    }


    // --- implements SecuritySupport ---

    /**
     * This method returns an array of keystores containing keys and
     * certificates.
     */
    public KeyStore[] getKeyStores() {
        return keyStores.toArray(new KeyStore[keyStores.size()]);
    }

    /**
     * This method returns an array of truststores containing certificates.
     */
    public KeyStore[] getTrustStores() {
        return trustStores.toArray(new KeyStore[trustStores.size()]);
    }

    /**
     * This method returns an array of passwords in order corresponding to
     * array of keystores.
     */
    List<char[]> getKeyStorePasswords() {
        return keyStorePasswords;
    }

    /**
     * This method returns an array of token names in order corresponding to
     * array of keystores.
     */
    public String[] getTokenNames() {
        return tokenNames.toArray(new String[tokenNames.size()]);
    }

    /**
     * This method synchronize key file for given realm.
     * @param configContext the ConfigContextx
     * @param fileRealmName
     * @exception
     */
    /*V3:Commented
    public void synchronizeKeyFile(ConfigContext configContext,
            String fileRealmName) throws Exception {
        // no op
    }*/

    /**
     * @param  token 
     * @return a keystore
     */
    public KeyStore getKeyStore(String token) {
        int idx = getTokenIndex(token);
        if (idx < 0) {
            return null;
        }
        return keyStores.get(idx);
    }

    /**
     * @param  token 
     * @return a truststore
     */
    public KeyStore getTrustStore(String token) {
        int idx = getTokenIndex(token);
        if (idx < 0) {
            return null;
        }
        return trustStores.get(idx);
    }
   
    /**
     * @param  token
     * @return the password for this token
     */
//    public String getKeyStorePassword(String token) {
//        SSLUtils.checkPermission(SSLUtils.KEYSTORE_PASS_PROP);
//        int idx = getTokenIndex(token);
//        if (idx < 0) {
//            return null;
//        }
//        return keyStorePasswords.get(idx);
//    }

    /**
     * @return returned index 
     */ 
    private int getTokenIndex(String token) {
        int idx = -1;
        if (token!=null) {
            idx = tokenNames.indexOf(token);
            if (idx < 0 && _logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "token {0} is not found", token);
            }
        }
        return idx;        
    }

    public void synchronizeKeyFile(Object configContext, String fileRealmName) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet in V3.");
    }

    public PrivateKey getPrivateKeyForAlias(String alias, int keystoreIndex) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        SSLUtils.checkPermission(SSLUtils.KEYSTORE_PASS_PROP);
        Key key = keyStores.get(keystoreIndex).getKey(alias, keyStorePasswords.get(keystoreIndex));
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        } else {
            return null;
        }
    }
}
