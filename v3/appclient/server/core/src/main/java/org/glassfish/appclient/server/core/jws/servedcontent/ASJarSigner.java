/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.server.core.jws.servedcontent;

import com.sun.enterprise.security.ssl.SecuritySupportImpl;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import java.io.File;
import java.security.AccessControlException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.Permission;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;
import sun.security.tools.JarSigner;

/**
 * Signs a specified JAR file.
 *<p>
 *This implementation searches the available keystores for the signing alias
 *indicated in the domain.xml config or, if not specified, the default alias,
 *the first time it is invoked to sign a JAR file.  After the first requested
 *signing it uses the same alias and provider to sign all JARs.
 *<p>
 *The public interface to this class is the static signJar method.  
 *
 * @author tjquinn
 */
@Service
@Scoped(Singleton.class)
public class ASJarSigner implements PostConstruct {
    
    /** property name optionally set by the admin in domain.xml to select an alias for signing */
    public static final String USER_SPECIFIED_ALIAS_PROPERTYNAME = "com.sun.aas.jws.signing.alias";

    /** keystore type for JKS keystores */
    private static final String JKS_KEYSTORE_TYPE_VALUE = "jks";
    
    /** default alias for signing if the admin does not specify one */
    private static final String DEFAULT_ALIAS_VALUE = "s1as";

//    /** user-specified signing alias */
//    private final String userAlias; // = System.getProperty(USER_SPECIFIED_ALIAS_PROPERTYNAME);
    
    private static final StringManager localStrings = StringManager.getManager(ASJarSigner.class);

    // TODO: SecuritySupportImpl does not properly init as a @Service; just create one for now
//    @Inject
    private static SecuritySupport securitySupport;



    private Logger logger;

    public void postConstruct() {
        logger =
            LogDomains.getLogger(ASJarSigner.class,
            LogDomains.CORE_LOGGER);
        securitySupport  = new SecuritySupportImpl();
    }

    /**
     *Creates a signed jar from the specified unsigned jar.
     *@param unsignedJar the unsigned JAR file
     *@param signedJar the signed JAR to be created
     *@return the elapsed time to sign the JAR (in milliseconds)
     *@throws Exception getting the keystores from SSLUtils fails
     */
    public long signJar(final File unsignedJar, final File signedJar,
        String alias) throws Exception {

        if (alias == null) {
            alias = DEFAULT_ALIAS_VALUE;
        }
        final SigningInfo signingInfo = createSigningInfo(alias);
        long startTime = System.currentTimeMillis();
        
        /*
         *Obtain the command-line arguments suitable for signing this JAR
         *based on the signing information already established, which will depend
         *on the keystore type in which the alias was found, etc.
         */
        String[] args = signingInfo.getSigningArgs(unsignedJar, signedJar);

        /*
         *In response to errors, the JarSigner class writes errors directly to 
         *System.out (rather than throw exceptions) and invokes System.exit.  
         *To prevent JarSigner's error handling from forcing the app server to
         *exit establish a security manager that prohibits the use of System.exit,
         *temporarily while JarSigner runs.  
         *
         *Make sure to change the security manager and use the JarSigner
         *class only one thread at a time.
         */
        synchronized(this) {
            
            /*
             *Save the current security manager; restored later.
             */
            SecurityManager mgr = System.getSecurityManager();

            try {
                NoExitSecurityManager noExitMgr = new NoExitSecurityManager(mgr);
                System.setSecurityManager(noExitMgr);

                /*
                 *Run the jar signer.
                 */
                JarSigner.main(args);
            } catch (Throwable t) {
                /*
                 *In case of any problems, make sure there is no ill-formed signed
                 *jar file left behind.
                 */
                signedJar.delete();

                /*
                 *The jar signer will have written some information to System.out
                 *and/or System.err.  Refer the user to those earlier messages.
                 */
                throw new Exception(localStrings.getString("jws.sign.errorSigning", 
                        signedJar.getAbsolutePath(), signingInfo.getAlias()), t);
            } finally {
                /*
                 *Restore the saved security manager.
                 */
                System.setSecurityManager(mgr);

                /*
                 *Clear out the args array to hide the password.
                 */
                for (int i = 0; i < args.length; i++) {
                    args[i] = null;
                }
                long duration = System.currentTimeMillis() - startTime;
                logger.fine("Signing " + unsignedJar.getAbsolutePath() + " took " + duration + " ms");
            }
        } 
        
        return System.currentTimeMillis() - startTime;
    }

    /**
     *Wraps any underlying exception.
     *<p>
     *This is primarily used to insulate calling logic from
     *the large variety of exceptions that can occur during signing
     *from which the caller cannot really recover.
     */
    public static class ASJarSignerException extends Exception {
        public ASJarSignerException(String msg, Throwable t) {
            super(msg, t);
        }
    }
    
//    /**
//     *Returns the signing info to use, creating it if it is not already
//     *created.
//     *@return the signing information to use in signing JARs
//     */
//    private static synchronized SigningInfo getSigningInfo() throws Exception {
//        if (signingInfo == null) {
//            signingInfo = createSigningInfo();
//        }
//        return signingInfo;
//    }
    
    /**
     *Creates an object containing the signing information provided by the
     *user-specified or default alias from the keystore in which it appears.
     *@return the SigningInfo object containing the information to be used for signing
     */
    private SigningInfo createSigningInfo(final String userAlias) throws Exception {
        
        String[] keystorePWs = securitySupport.getKeyStorePasswords();
        String[] tokenNames = securitySupport.getTokenNames();

        /*
         *Assemble lists of signing info objects, one list for matches on the 
         *user-specified alias (if specified at all), the other for matches on 
         *the default alias.
         */
        ArrayList<SigningInfo> signingInfoForDefaultAlias = new ArrayList<SigningInfo>();
        ArrayList<SigningInfo> signingInfoForUserAlias = new ArrayList<SigningInfo>();
        
        int keystoreSlot = 0;
        
        for (KeyStore ks : securitySupport.getKeyStores()) {
            if (userAlias != null && ks.containsAlias(userAlias)) {
                /*
                 *The user specified an alias and the current keystore contains
                 *it.  Use this keystore and the user's alias.
                 */
                signingInfoForUserAlias.add(SigningInfo.newInstance(
                        userAlias, 
                        keystorePWs[keystoreSlot], 
                        ks,
                        tokenNames[keystoreSlot],
                        logger));
            }
            
            if (ks.containsAlias(DEFAULT_ALIAS_VALUE)) {
                signingInfoForDefaultAlias.add(SigningInfo.newInstance(
                        DEFAULT_ALIAS_VALUE, 
                        keystorePWs[keystoreSlot], 
                        ks, 
                        tokenNames[keystoreSlot],
                        logger));
            }
            keystoreSlot++;
        }
        
        /*
         *Choose which signing information object to use based on whether the user 
         *specified an alias, if so whether it was found among the known keystores,
         *etc.
         */
        SigningInfo result = selectSigningInfo(
                userAlias,
                signingInfoForUserAlias,
                signingInfoForDefaultAlias);
        logger.fine("Selected signing info " + result.toString());
        
        return result;
    }
    
    /**
     *Selects the signing info instance to be used for signing JAR files.
     *This method may issue warnings if the user-specified alias does not
     *appear in any keystore or if the user-specified or default alias appears
     *in more than one keystore.
     *@param signingInfoForUserAlias signing info for the user-specified alias
     *@param signingInfoForDefaultAlias signing info for the default alias
     *@return the SigningInfo instance to be used for signing JARs
     */
    private SigningInfo selectSigningInfo(
            final String userAlias,
            ArrayList<SigningInfo> signingInfoForUserAlias,
            ArrayList<SigningInfo> signingInfoForDefaultAlias) {
        /*
         *Use the user-specified info if requested and available. Otherwise use
         *the default info.
         */
        ArrayList<SigningInfo> signingInfoOfInterest;
        String aliasOfInterest;
        
        if (userAlias != null) {
            if (signingInfoForUserAlias.size() == 0) {
                logger.log(Level.WARNING, "jws.sign.userAliasAbsent", userAlias);
                signingInfoOfInterest = signingInfoForDefaultAlias;
                aliasOfInterest = DEFAULT_ALIAS_VALUE;
            } else {
                signingInfoOfInterest = signingInfoForUserAlias;
                aliasOfInterest = userAlias;
            }
        } else {
            signingInfoOfInterest = signingInfoForDefaultAlias;
            aliasOfInterest = DEFAULT_ALIAS_VALUE;
        }
            
        /*
         *Make sure whichever list of signing info is now of interest has
         *exactly one entry for the alias of interest.
         *
         *If there is no entry for the alias of interest, then we cannot proceed.
         */
        if (signingInfoOfInterest.size() == 0) {
            throw new IllegalArgumentException(
                    localStrings.getString("jws.sign.aliasNotFound", aliasOfInterest));
        }
        
        if (signingInfoOfInterest.size() > 1) {
            /*
             *Prepare a warning identifying all the keystore providers for
             *which the keystore contains the alias of interest.  
             */
            StringBuilder sb = new StringBuilder();
            for (SigningInfo si : signingInfoOfInterest) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(si);
            }
            logger.log(Level.WARNING, "jws.sign.aliasFoundMult", 
                    new Object[] {aliasOfInterest, sb.toString()});
        }

        /*
         *Return the first signing info that matched the alias.
         */
        return signingInfoOfInterest.get(0);
    }
    
    /**
     *Represents the information needed to actually sign a JAR file: the alias,
     *the keystore that contains the certificates associated with that alias,
     *and so forth.
     */
    private abstract static class SigningInfo {

        /** JarSigner command line argument options */
        private static final String SIGNEDJAR_OPTION = "-signedjar";
        private static final String KEYSTORE_OPTION = "-keystore";
        private static final String STOREPASS_OPTION = "-storepass";
        private static final String STORETYPE_OPTION = "-storetype";

        private KeyStore keystore;
        private String alias;
        private String password;
        private PrivateKey key;
        private String token;
        private Logger logger;
        
        /**
         *Factory method that returns a new instance of the correct subtype 
         *of SigningInfo, depending on the type of keystore.
         *@param alias the alias to be used during signing
         *@param password the password for this alias
         *@param keystore the keystore in which this alias was found
         *@param token the token (possibly null) for this keystore
         *@return new instance of a SigningInfo subclass
         */
        static SigningInfo newInstance(String alias, String password,
                KeyStore keystore, String token, Logger logger)
                    throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
            if (keystore.getType().equalsIgnoreCase(JKS_KEYSTORE_TYPE_VALUE)) {
                return new JKSSigningInfo(alias, password, keystore, token, logger);
            } else {
                return new PKCS11SigningInfo(alias, password, keystore, token, logger);
            }
        }

        public SigningInfo(String alias, String password, 
                KeyStore keystore, String token, Logger logger)
                    throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
            this.keystore = keystore;
            this.alias = alias;
            this.password = password;
            this.token = token;
            this.logger = logger;
            key = validateKey();
        }
        
        public String getAlias() {
            return alias;
        }
        
        private PrivateKey validateKey() throws KeyStoreException, NoSuchAlgorithmException, 
                UnrecoverableKeyException {
            Key tempKey = keystore.getKey(alias, password.toCharArray());
            if (tempKey instanceof PrivateKey) {
                return (PrivateKey) tempKey;
            } else {
                final String msg = MessageFormat.format(
                        logger.getResourceBundle().getString("jws.sign.keyNotPrivate"), alias);
                throw new IllegalArgumentException(msg);
            }
        }
        
        public String getProviderName() {
            return keystore.getProvider().getName();
        }
        
        public String getToken() {
            return token;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getStoreType() {
            return keystore.getType();
        }
        public X509Certificate[] getCertificateChain() throws KeyStoreException {
            Certificate[] certs = keystore.getCertificateChain(alias);
            X509Certificate[] X509certs = new X509Certificate[certs.length];
            int slot = 0;
            for (Certificate c : certs) {
                if (c instanceof X509Certificate) {
                    X509certs[slot++] = (X509Certificate) c;
                } else {
                    throw new IllegalArgumentException(localStrings.getString("jws.sign.notX509Cert", alias));
                }
            }
            return X509certs;
        }
        
        public String toString() {
            return new StringBuilder().
                    append(getClass().getName()).
                    append(": alias=").append(alias).
                    append("; keystore type=").append(keystore.getType()).
                    append("; provider=").append(keystore.getProvider().getName()).
                    
                    toString();
        }
        
        public KeyStore getKeyStore() {
            return keystore;
        }
        
        public String[] getSigningArgs(File unsignedJar, File signedJar) {
            /*Compose the arguments to pass to the JarSigner class equivalent to
             *this command:
             *jarsigner 
             *  -keystore <jks-file-spec, if type is jks; NONE if pkcs11>
             *  -providerName <pkcs11 provider name, if pkcs11>
             *  -storetype <jks or pkcs11, depending on the keystore>
             *  -storepass <keystore password>
             *  -signedjar <signed JAR file spec>
             *  <unsigned JAR file spec>
             *  <alias>
             *
             *Note that techniques for importing certs into the AS keystore are
             *documented as requiring that the key password and the keystore
             *password be the same.  We rely on that here because there is no
             *provision for extracting the password for an alias from the keystore.
             */
            ArrayList<String> args = new ArrayList<String>();
            
            addKeyStoreTypeSpecificArgs(args);
            
            args.add(STORETYPE_OPTION);
            args.add(getKeyStore().getType());

            args.add(STOREPASS_OPTION);
            int passwordSlot = args.size();
            args.add(getPassword());

            args.add(SIGNEDJAR_OPTION);
            args.add(signedJar.getAbsolutePath());
            
            args.add(unsignedJar.getAbsolutePath());

            args.add(getAlias());
            
            String[] result = args.toArray(new String[args.size()]);
            args.set(passwordSlot,"");
            return result;
        }
        
        /**
         *Appends command-line arguments that depend on the specific type of
         *keystore to the collection of arguments.
         *@param args Collection of command-line argument Strings to be added to
         */
        protected abstract void addKeyStoreTypeSpecificArgs(Collection<String> args);
        
        /**
         *Implements the JKS-specific type of signing information.
         */
        private static class JKSSigningInfo extends SigningInfo {

            /** property name the value of which points to the JKS keystore, if present */
            private static final String KEYSTORE_PATH_PROPERTYNAME = "javax.net.ssl.keyStore";

            /**
             *Returns the absolute path to the keystore.
             *@return path to the keystore
             */
            private static String getJKSKeystoreAbsolutePath() {
                return System.getProperty(KEYSTORE_PATH_PROPERTYNAME);
            }
            
            public JKSSigningInfo(String alias, String password, 
                    KeyStore keystore, String token, Logger logger) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
                super(alias, password, keystore, token, logger);
            }
            
            protected void addKeyStoreTypeSpecificArgs(Collection<String> args) {
                /*
                 *  -keystore <filespec>
                 */
                args.add(KEYSTORE_OPTION);
                args.add(getJKSKeystoreAbsolutePath());
            }
            
        }
        
        /**
         *Implements the PKCS11-specific type of signing information.
         */
        private static class PKCS11SigningInfo extends SigningInfo {

            private static final String PKCS11_PROVIDERNAME_OPTION = "-providerName";
            private static final String PKCS11_KEYSTORE_OPTION_VALUE = "NONE";

            public PKCS11SigningInfo(String alias, String password, 
                    KeyStore keystore, String token, Logger logger) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
                super(alias, password, keystore, token, logger);
            }
            
            protected void addKeyStoreTypeSpecificArgs(Collection<String> args) {
                /*For PKCS11 keystores, use:
                 *
                 *  -keystore NONE
                 *  -providerName <provider name for the keystore>
                 *
                 *The provider name includes the token, so any configuration
                 *information the provider needs to sign the JAR will be 
                 *available to the provider instance.
                 */
                args.add(KEYSTORE_OPTION);
                args.add(PKCS11_KEYSTORE_OPTION_VALUE);
                
                args.add(PKCS11_PROVIDERNAME_OPTION);
                args.add(getKeyStore().getProvider().getName());
           }
        }        
    }
    
    /**
     *A security manager that rejects any attempt to exit the VM.
     */
    private class NoExitSecurityManager extends SecurityManager {
        
        private SecurityManager originalManager;
        
        public NoExitSecurityManager(SecurityManager originalManager) {
            this.originalManager = originalManager;
        }
        
        public void checkExit(int status) {
            /*
             *Always reject attempts to exit the VM.
             */
            throw new AccessControlException("System.exit");
        }
        
        public void checkPermission(Permission p) {
            /*
             *Delegate to the other manager, if any.
             */
            if (originalManager != null) {
                originalManager.checkPermission(p);
            }
        }
    }
}
