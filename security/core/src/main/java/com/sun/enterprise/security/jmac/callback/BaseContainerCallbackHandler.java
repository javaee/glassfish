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
 * BaseContainerCallbackHandler.java
 *
 * Created on April 21, 2004, 11:56 AM
 */

package com.sun.enterprise.security.jmac.callback;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.CertStoreCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.callback.PrivateKeyCallback;
import javax.security.auth.message.callback.SecretKeyCallback;
import javax.security.auth.message.callback.TrustStoreCallback;
import javax.security.auth.x500.X500Principal;

//V3:Commented import com.sun.enterprise.Switch;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.PrincipalImpl;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.login.DistinguishedPrincipalCredential;
import com.sun.enterprise.security.auth.login.common.LoginException;
import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.jmac.config.CallbackHandlerConfig;
import com.sun.enterprise.security.jmac.config.GFServerConfigProvider;
import com.sun.enterprise.security.jmac.config.HandlerContext;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.logging.LogDomains;

import sun.security.util.DerValue;
import org.glassfish.internal.api.Globals;

/**
 * Base Callback Handler for JSR 196
 * @author  Harpreet Singh
 * @author  Shing Wai Chan
 */

abstract class BaseContainerCallbackHandler
        implements CallbackHandler, CallbackHandlerConfig {
     
    private static final String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";
    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
    private static final String CLIENT_SECRET_KEYSTORE =
        "com.sun.appserv.client.secretKeyStore";
    private static final String CLIENT_SECRET_KEYSTORE_PASSWORD =
        "com.sun.appserv.client.secretKeyStorePassword";
    private static final String DEFAULT_CLIENT_SECRET_KEYSTORE_PASSWORD =
        "changeit";

    protected static Logger _logger = LogDomains.getLogger(BaseContainerCallbackHandler.class, LogDomains.SECURITY_LOGGER);

    protected HandlerContext handlerContext = null;

    // TODO: inject them once this class becomes a component
    protected final SSLUtils sslUtils = Globals.getDefaultHabitat().getComponent(SSLUtils.class);
    protected final SecuritySupport secSup = Globals.getDefaultHabitat().getByContract(SecuritySupport.class);
    
    protected BaseContainerCallbackHandler() {
    }
    
    public void setHandlerContext(HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    /*
     * To be implemented by a sub-class. The sub class decides 
     * which callbacks it supports.
     * <i>EjbServletWSSCallbackHandler</i> supports:
     * <li>SecretKeyCallback</li>
     * <li>TrustStoreCallback</li>
     * <li>PasswordValidationCallback</li>
     * <li>CertStoreCallback</li>
     * <li>PrivateKeyCallback</li>
     * <i> AppclientWSSCallbackHandler</i> supports:
     * <li>NameCallback</li>
     * <li>PasswordCallback</li>
     * <li>ChoiceCallback</li>
     */
    protected abstract boolean isSupportedCallback(Callback callback);

    protected abstract void handleSupportedCallbacks(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException; 
    
    public void handle(Callback[] callbacks) 
            throws IOException, UnsupportedCallbackException {
        if (callbacks == null) {
            return;
        }

        for (Callback callback : callbacks) {
            if (!isSupportedCallback(callback)) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                            "JMAC: UnsupportedCallback : " +
                                    callback.getClass().getName());
                }
                throw new UnsupportedCallbackException(callback);
            }
        }       

        handleSupportedCallbacks(callbacks);
    }
    
    /**
     * gets the appropriate callback processor and hands the callback to 
     * processor to process the callback.
     */
    protected void processCallback (Callback callback) 
            throws UnsupportedCallbackException {
        if (callback instanceof CallerPrincipalCallback) {
            processCallerPrincipal((CallerPrincipalCallback)callback);
        } else if (callback instanceof GroupPrincipalCallback) {
            processGroupPrincipal((GroupPrincipalCallback)callback);
    	} else if (callback instanceof PasswordValidationCallback) {
            processPasswordValidation((PasswordValidationCallback)callback);
        } else if (callback instanceof PrivateKeyCallback) {
            processPrivateKey((PrivateKeyCallback)callback);
        } else if (callback instanceof TrustStoreCallback) {
            TrustStoreCallback tstoreCallback = (TrustStoreCallback)callback;
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, 
                    "JMAC: In TrustStoreCallback Processor");
            }
            tstoreCallback.setTrustStore (sslUtils.getMergedTrustStore());

        } else if (callback instanceof CertStoreCallback) {
            processCertStore((CertStoreCallback)callback);
        } else if (callback instanceof SecretKeyCallback) {
            processSecretKey((SecretKeyCallback)callback);
        } else {
            // sanity check =- should never come here.
            // the isSupportedCallback method already takes care of this case
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,"JMAC: UnsupportedCallback : "+
                    callback.getClass().getName());
            }
            throw new UnsupportedCallbackException(callback); 
        }
    }


    private void processCallerPrincipal(CallerPrincipalCallback cpCallback) {
        final Subject fs = cpCallback.getSubject();
        Principal principal = cpCallback.getPrincipal();

        String realmName = null;
        if (handlerContext != null) {
            realmName = handlerContext.getRealmName();
        }

        boolean isCertRealm = CertificateRealm.AUTH_TYPE.equals(realmName);
        if (principal == null) {
            if (cpCallback.getName() != null) {
                if (isCertRealm) {
                    principal = new X500Principal(cpCallback.getName());
                } else {
                    principal = new PrincipalImpl(cpCallback.getName());
                }
            } else {
                // 196 unauthenticated caller principal
                principal = SecurityContext.getDefaultCallerPrincipal();
            }
        }

        if (isCertRealm) {
            LoginContextDriver.jmacLogin(fs, (X500Principal)principal);
        }

        final Principal fprin = principal;
        final DistinguishedPrincipalCredential fdpc =
                new DistinguishedPrincipalCredential(principal);
        AppservAccessController.doPrivileged(new PrivilegedAction(){
            public java.lang.Object run() {
                fs.getPrincipals().add(fprin);
                Iterator iter = fs.getPublicCredentials().iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof DistinguishedPrincipalCredential) {
                        iter.remove();
                    }
                }
                fs.getPublicCredentials().add(fdpc);
                return fs;
            }
        });
    }

    private void processGroupPrincipal(GroupPrincipalCallback gpCallback) {
        final Subject fs = gpCallback.getSubject();
        final String[] groups = gpCallback.getGroups();
        if (groups != null && groups.length > 0) {
            AppservAccessController.doPrivileged(new PrivilegedAction(){
                public java.lang.Object run() {
                    for (String group : groups) {
                        fs.getPrincipals().add(new Group(group));
                    }
                    return fs;
                }
            });
        } else if (groups == null) {
            AppservAccessController.doPrivileged(new PrivilegedAction(){
                public java.lang.Object run() {
                    fs.getPrincipals(Group.class).clear();
                    return fs;
                }
            });
        }
    }

    private void processPasswordValidation(
            PasswordValidationCallback pwdCallback) {


        //if (Switch.getSwitch().getContainerType() == Switch.APPCLIENT_CONTAINER) {
        if (SecurityServicesUtil.getInstance().isACC()) {
            if (_logger.isLoggable(Level.FINE)){
                _logger.log(Level.FINE, "JMAC: In PasswordValidationCallback Processor for appclient - will do nothing");
            }
            pwdCallback.setResult(true);
            return;
        }
        String username = pwdCallback.getUsername();
        String password = null;
        char[] passwd = pwdCallback.getPassword();
        if(passwd != null){
            password = new String(passwd);
        }
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JMAC: In PasswordValidationCallback Processor");
        }
        try {
            String realmName = null;
            if (handlerContext != null) {
                realmName = handlerContext.getRealmName();
            }
            Subject s = LoginContextDriver.jmacLogin(pwdCallback.getSubject(),
                    username, password, realmName);
            GFServerConfigProvider.setValidateRequestSubject(s);
            if(_logger.isLoggable(Level.FINE)){
                _logger.log(Level.FINE, 
                    "JMAC: authentication succeeded for user = ", 
                    username);
            }
            // explicitly ditch the password
            password = null;
            pwdCallback.setResult(true);
        } catch(LoginException le) {
            // login failed
            if (_logger.isLoggable(Level.INFO)) {
                _logger.log(Level.INFO, "jmac.loginfail", username);
            }
            pwdCallback.setResult(false);
        }
    }

    private void processPrivateKey(PrivateKeyCallback privKeyCallback) {
        KeyStore[] kstores = secSup.getKeyStores();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, 
                "JMAC: In PrivateKeyCallback Processor");
        }
    	
        // make sure we have a keystore
        if (kstores == null || kstores.length == 0) {
            // cannot get any information
            privKeyCallback.setKey(null, null);
            return;
        }

        String[] passwords =
            secSup.getKeyStorePasswords();

        // get the request type
        PrivateKeyCallback.Request req = privKeyCallback.getRequest();
        PrivateKey privKey = null;
        Certificate[] certs = null;
        if (req == null) {
            // no request type - set default key
            PrivateKeyEntry pke = getDefaultPrivateKeyEntry(
                    kstores, passwords);
            if (pke != null) {
                privKey = pke.getPrivateKey();
                certs = pke.getCertificateChain();
            }
            privKeyCallback.setKey(privKey, certs);
            return;
        }

        // find key based on request type
        try {
            if (req instanceof PrivateKeyCallback.AliasRequest) {
                PrivateKeyCallback.AliasRequest aReq =
                        (PrivateKeyCallback.AliasRequest)req;

                String alias = aReq.getAlias();
                PrivateKeyEntry privKeyEntry;
                if (alias == null) {
                    // use default key
                    privKeyEntry = getDefaultPrivateKeyEntry(kstores, passwords);
                } else {
                    privKeyEntry = sslUtils.getPrivateKeyEntryFromTokenAlias(alias);
                }

                if (privKeyEntry != null) {
                    privKey = privKeyEntry.getPrivateKey();
                    certs = privKeyEntry.getCertificateChain();
                }
            } else if (req instanceof PrivateKeyCallback.IssuerSerialNumRequest) {
                PrivateKeyCallback.IssuerSerialNumRequest isReq =
                        (PrivateKeyCallback.IssuerSerialNumRequest)req;
                X500Principal issuer = isReq.getIssuer();
                BigInteger serialNum = isReq.getSerialNum();
                if (issuer != null && serialNum != null) {
                    boolean found = false;
                    for (int i = 0; i < kstores.length && !found; i++) {
                        Enumeration aliases = kstores[i].aliases();
                        while (aliases.hasMoreElements() && !found) {
                            String nextAlias = (String)aliases.nextElement();
                            Key key = kstores[i].getKey(nextAlias, passwords[i].toCharArray());
                            if (key != null && (key instanceof PrivateKey)) {
                                Certificate[] certificates =
                                        kstores[i].getCertificateChain(nextAlias);
                                // check issuer/serial
                                X509Certificate eeCert = (X509Certificate)certificates[0];
                                if (eeCert.getIssuerX500Principal().equals(issuer) &&
                                        eeCert.getSerialNumber().equals(serialNum)) {
                                    privKey = (PrivateKey)key;
                                    certs = certificates;
                                    found = true;
                                }
                            }
                        }
                    }
                }
            } else if (req instanceof PrivateKeyCallback.SubjectKeyIDRequest) {
                PrivateKeyCallback.SubjectKeyIDRequest skReq = 
                        (PrivateKeyCallback.SubjectKeyIDRequest)req;
                byte[] subjectKeyID = skReq.getSubjectKeyID();
                if (subjectKeyID != null) {
                    boolean found = false;
                    // In DER, subjectKeyID will be an OCTET STRING of OCTET STRING
                    DerValue derValue1 = new DerValue(
                        DerValue.tag_OctetString, subjectKeyID);
                    DerValue derValue2 = new DerValue(
                        DerValue.tag_OctetString, derValue1.toByteArray());
                    byte[] derSubjectKeyID = derValue2.toByteArray();

                    for (int i = 0; i < kstores.length && !found; i++) {
                        Enumeration aliases = kstores[i].aliases();
                        while (aliases.hasMoreElements() && !found) {
                            String nextAlias = (String)aliases.nextElement();
                            Key key = kstores[i].getKey(nextAlias, passwords[i].toCharArray());
                            if (key != null && (key instanceof PrivateKey)) {
                                Certificate[] certificates =
                                        kstores[i].getCertificateChain(nextAlias);
                                X509Certificate eeCert = (X509Certificate)certificates[0];
                                // Extension: SubjectKeyIdentifier
                                byte[] derSubKeyID = eeCert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
                                if (derSubKeyID != null &&
                                        Arrays.equals(derSubKeyID, derSubjectKeyID)) {
                                    privKey = (PrivateKey)key;
                                    certs = certificates;
                                    found = true;
                                }
                            }
                        }
                    }
                }
            } else if (req instanceof PrivateKeyCallback.DigestRequest) {
                PrivateKeyCallback.DigestRequest dReq =
                        (PrivateKeyCallback.DigestRequest)req;
                byte[] digest = dReq.getDigest();
                String algorithm = dReq.getAlgorithm();

                PrivateKeyEntry privKeyEntry = null;
                if (digest == null) {
                    // get default key
                    privKeyEntry = getDefaultPrivateKeyEntry(kstores, passwords);
                } else {
                    if (algorithm == null) {
                        algorithm = DEFAULT_DIGEST_ALGORITHM;
                    }
                    MessageDigest md = MessageDigest.getInstance(algorithm);
                    privKeyEntry = getPrivateKeyEntry(kstores, passwords, md, digest);
                }

                if (privKeyEntry != null) {
                    privKey = privKeyEntry.getPrivateKey();
                    certs = privKeyEntry.getCertificateChain();
                }
            } else {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                         "invalid request type: " + req.getClass().getName());
                }
            }
        } catch (Exception e) {
            // UnrecoverableKeyException
            // NoSuchAlgorithmException
            // KeyStoreException
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                    "JMAC: In PrivateKeyCallback Processor: " +
                    " Error reading key !", e);
            }
        } finally {
            privKeyCallback.setKey(privKey, certs);
            passwords = null;
        }
    }
    
    /**
     * Return the first key/chain that we can successfully
     * get out of the keystore
     */
    private PrivateKeyEntry getDefaultPrivateKeyEntry(
            KeyStore[] kstores, String[] passwords) {
        PrivateKey privKey = null;
        Certificate[] certs = null;
        try {
            for (int i = 0; i < kstores.length && privKey == null; i++) {
                Enumeration aliases = kstores[i].aliases();
                // loop thru aliases and try to get the key/chain
                while (aliases.hasMoreElements() && privKey == null) {
                    String nextAlias = (String)aliases.nextElement();
                    privKey = null;
                    certs = null;
                    Key key = kstores[i].getKey(nextAlias, passwords[i].toCharArray());
                    if (key != null && (key instanceof PrivateKey)) {
                        privKey = (PrivateKey)key;
                        certs = kstores[i].getCertificateChain(nextAlias);
                    }
                }
            }
        } catch (Exception e) {
            // UnrecoverableKeyException
            // NoSuchAlgorithmException
            // KeyStoreException
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                    "Exception in getDefaultPrivateKeyEntry", e);
            }
        }
    	
        return new PrivateKeyEntry(privKey, certs);
    }

    private PrivateKeyEntry getPrivateKeyEntry(
            KeyStore[] kstores, String[] passwords,
            MessageDigest md, byte[] digest) {
        PrivateKey privKey = null;
        Certificate[] certs = null;
        try {
            for (int i = 0; i < kstores.length && privKey == null; i++) {
                Enumeration aliases = kstores[i].aliases();
                // loop thru aliases and try to get the key/chain
                while (aliases.hasMoreElements() && privKey == null) {
                    String nextAlias = (String)aliases.nextElement();
                    privKey = null;
                    certs = null;
                    Key key = kstores[i].getKey(nextAlias, passwords[i].toCharArray());
                    if (key != null && (key instanceof PrivateKey)) {
                        certs = kstores[i].getCertificateChain(nextAlias);
                        md.reset();
                        byte[] cDigest = md.digest(certs[0].getEncoded());
                        if (Arrays.equals(digest, cDigest)) {
                            privKey = (PrivateKey)key;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // UnrecoverableKeyException
            // NoSuchAlgorithmException
            // KeyStoreException
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                    "Exception in getPrivateKeyEntry for Digest", e);
            }
        }
    	
        return new PrivateKeyEntry(privKey, certs);
    }

    private void processCertStore(CertStoreCallback certStoreCallback) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, 
                "JMAC: In CertStoreCallback Processor");
        }

        KeyStore certStore = sslUtils.getMergedTrustStore();
        if (certStore == null) {// should never happen
            certStoreCallback.setCertStore(null);
        }
        List list = new ArrayList();
        CollectionCertStoreParameters ccsp;
        try{
            if (certStore != null) {
                Enumeration enu = certStore.aliases();
                while (enu.hasMoreElements()) {
                    String alias = (String) enu.nextElement();
                    if (certStore.isCertificateEntry(alias)) {
                        try {
                            Certificate cert = certStore.getCertificate(alias);
                            list.add(cert);
                        } catch (KeyStoreException kse) {
                            // ignore and move to next
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, "JAMAC: Cannot retrieve" +
                                        "certificate for alias " + alias);
                            }
                        }
                    }
                }
            }
            ccsp = new CollectionCertStoreParameters(list);
            CertStore certstore = CertStore.getInstance("Collection", ccsp);
            certStoreCallback.setCertStore(certstore);
        } catch(KeyStoreException kse){
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, 
                    "JMAC:  Cannot determine truststore aliases", kse);        
            }
        } catch(InvalidAlgorithmParameterException iape){
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, 
                    "JMAC:  Cannot instantiate CertStore", iape);        
            }
        } catch(NoSuchAlgorithmException nsape){
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, 
                    "JMAC:  Cannot instantiate CertStore", nsape);        
            }
        }
    }

    private void processSecretKey(SecretKeyCallback secretKeyCallback) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, 
                "JMAC: In SecretKeyCallback Processor");
        }

        String alias = ((SecretKeyCallback.AliasRequest)secretKeyCallback.getRequest()).getAlias();
        if (alias != null) {
            try {
                PasswordAdapter passwordAdapter = null;
                // (Switch.getSwitch().getContainerType() ==
                  //    Switch.APPCLIENT_CONTAINER) {
                if (SecurityServicesUtil.getInstance().isACC()) {
                    passwordAdapter = new PasswordAdapter(
                        System.getProperty(CLIENT_SECRET_KEYSTORE),
                        System.getProperty(CLIENT_SECRET_KEYSTORE_PASSWORD,
                            DEFAULT_CLIENT_SECRET_KEYSTORE_PASSWORD).toCharArray());
                } else {
                    passwordAdapter = new PasswordAdapter(
                        IdentityManager.getMasterPassword().toCharArray());
                }

                secretKeyCallback.setKey(
                    passwordAdapter.getPasswordSecretKeyForAlias(alias));
            } catch(Exception e) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, 
                    "JMAC: In SecretKeyCallback Processor: "+
                    " Error reading key ! for alias "+alias, e);
                }
                secretKeyCallback.setKey(null);
            }
        } else {
            // Dont bother about checking for principal
            // we dont support that feature - typically 
            // used in an environment with kerberos
            //            Principal p = secretKeyCallback.getPrincipal();
            secretKeyCallback.setKey(null);
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.log(Level.WARNING, "jmac.unsupportreadprinciple");
            }
        }
    }
}
