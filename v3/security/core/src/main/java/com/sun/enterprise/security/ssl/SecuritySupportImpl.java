/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.security.ssl;

import com.sun.enterprise.security.common.Util;
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
import javax.security.auth.callback.CallbackHandler;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
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
    protected static final List<String> keyStorePasswords = new ArrayList<String>();
    protected static final List<String> tokenNames = new ArrayList<String>();

    public SecuritySupportImpl() {
        this(true);
    }

    protected SecuritySupportImpl(boolean init) {
        if (init) {
            initJKS();
        }
    }

    protected void initJKS() {
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

        if (!initialized) {
            loadStores(
                    null, 
                    null, 
                   keyStoreFileName, 
                    SSLUtils.getKeyStorePass(),
                    SSLUtils.getKeyStoreType(),
                    trustStoreFileName, 
                    SSLUtils.getTrustStorePass(),
                    SSLUtils.getTrustStoreType());
            initialized = true;
        }
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
                       String keyStorePass,
                       String keyStoreType,
                       String trustStoreFile,
                       String trustStorePass,                       
                       String trustStoreType) {

        try {
            KeyStore keyStore = loadKS(keyStoreType, provider, keyStoreFile,
                keyStorePass);
            KeyStore trustStore = loadKS(trustStoreType, provider,trustStoreFile,
                trustStorePass);
            keyStores.add(keyStore);
            trustStores.add(trustStore);
            keyStorePasswords.add(keyStorePass);
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
		    String keyStoreFile, String keyStorePass)
		    throws Exception
    {
        KeyStore ks = null;
        if (provider != null) {
            ks = KeyStore.getInstance(keyStoreType, provider);
        } else {
            ks = KeyStore.getInstance(keyStoreType);
        }
        char[] passphrase = keyStorePass.toCharArray();

        FileInputStream istream = null;
        BufferedInputStream bstream = null;
        try {
            if (keyStoreFile != null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Loading keystoreFile = " +
                        keyStoreFile + ", keystorePass = " + keyStorePass);
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
    public String[] getKeyStorePasswords() {
        return keyStorePasswords.toArray(new String[keyStorePasswords.size()]);
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
    public String getKeyStorePassword(String token) {
        int idx = getTokenIndex(token);
        if (idx < 0) {
            return null;
        }
        return keyStorePasswords.get(idx);
    }
   
    /**
     * @return returned index 
     */ 
    private int getTokenIndex(String token) {
        int idx = -1;
        if (token!=null) {
            idx = tokenNames.indexOf(token);
            if (idx < 0 && _logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "token " + token + " is not found");
            }
        }
        return idx;        
    }

    public void synchronizeKeyFile(Object configContext, String fileRealmName) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet in V3.");
    }
}
