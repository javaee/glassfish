/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.security.store.AsadminSecurityUtil;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Encapsulates the implementation of secure admin.
 * <p>
 * Some CLI commands need to connect to the DAS securely but will have neither
 * a user-provided master password nor a human who we could prompt for the
 * master password.  Such commands need to connect using SSL and a client certificate.
 * Such CLI command classes should @Inject SecureAdminClientManager and then
 * invoke its init method.
 *
 * @author Tim Quinn
 */
@Service
@Scoped(PerLookup.class)
public class SecureAdminClientManager implements PostConstruct {

    private static final Logger logger = LogDomains.getLogger(SecureAdminClientManager.class,
            LogDomains.ADMIN_LOGGER);

    private boolean isEnabled;

    private static KeyManager[] keyManagers = null;
    
    @Inject
    private SecureAdmin secureAdmin;

    private String instanceAlias = null;

    public static KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    @Override
    public void postConstruct() {
        isEnabled = Boolean.parseBoolean(secureAdmin.getEnabled());
        if (isEnabled) {
            instanceAlias = secureAdmin.getInstanceAlias();
            logger.fine("SecureAdminClientManager: secure admin is enabled");
        } else {
            logger.fine("SecureAdminClientManager: secure admin is disabled");
        }
    }

    /**
     * Prepares the manager for later use, typically when making a connection to
     * a remote admin port.  The main result of invoking this method is to
     * build an array of KeyManagers which can be passed to SSLContext.init
     * so SSL can use the managers to find certs that meet the requirements of
     * the partner on the other end of the connection.
     * <p>
     * This method opens the keystore, so it will need the master password.  The calling
     * command should pass the master password which the user specified via the
     * --passwordfile option (if any).  Because the user-provided password might be
     * wrong or missing, the caller also indicates whether a human user is present to
     * respond to a prompt for the password.  This will not be the case, for
     * example, during an unattended start-up of an instance.
     *
     * @param commandMasterPassword master password provided by the user on the command line; null if none
     * @param isPromptable whether the caller is in a context where a human could be prompted to enter a password
     */
    public void init(final char[] commandMasterPassword, final boolean isPromptable) {
        if (isEnabled) {
            try {
                /*
                 * The keystore should contain certs for both the
                 * admin (DAS) and the instances (or clients).
                 * If we point SSL at that keystore then it could choose any
                 * public cert that matches what the server is asking for, which
                 * means it might choose to return the DAS cert - or some other
                 * cert the user might have added to the keystore.  Because
                 * the admin code receiving the admin request is expecting us
                 * to use the instance cert, we need to make sure that happens.
                 * So, we'll create a temporary internal keystore containing
                 * the cert for the configured instance alias and we'll use that
                 * keystore for SSL.
                 */
                keyManagers = prepareKeyManagers(commandMasterPassword, isPromptable);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Reports whether the secure admin is enabled, according to the current
     * configuration.
     *
     * @return if secure admin is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    private KeyManager[] prepareKeyManagers(final char[] commandMasterPassword,
            final boolean isPromptable) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {

        /*
         * The configuration specifies what alias we should use for SSL client
         * authentication.  Because the keystore on disk contains multiple certs,
         * extract the required cert from the on-disk keystore and add it to a
         * temporary key store so it's the only cert there.
         */
        Certificate instanceCert = getCertForConfiguredAlias(
                commandMasterPassword, isPromptable);

        final KeyStore ks = instanceCertOnlyKS(instanceCert);

        /*
         * The caller will eventually need an array of KeyManagers to pass to
         * SSLContext.init.  Create that array now from the internal, single-cert
         * keystore so it's available later.
         */

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

        kmf.init(ks, new char[] {});
        return kmf.getKeyManagers();
    }

    private KeyStore instanceCertOnlyKS(final Certificate instanceCert) throws KeyStoreException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.setCertificateEntry(instanceAlias, instanceCert);
        return ks;
    }

    private Certificate getCertForConfiguredAlias(
            final char[] commandMasterPassword,
            final boolean isPromptable) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        final KeyStore permanentKS = AsadminSecurityUtil
                .getInstance(commandMasterPassword, isPromptable)
                .getAsadminKeystore();
        Certificate cert = permanentKS.getCertificate(instanceAlias);
        if (cert != null) {
            logger.log(Level.FINER, "Found matching cert in keystore for instance alias {0}", instanceAlias);
        } else {
            logger.log(Level.FINER, "Could not find matching cert in keystore for instance alias {0}", instanceAlias);
        }
        return cert;
    }
}
