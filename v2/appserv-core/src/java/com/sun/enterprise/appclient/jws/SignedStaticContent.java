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

package com.sun.enterprise.appclient.jws;

import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.File;
import java.net.URI;
import java.security.AccessControlException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a jar file that is signed before being served in 
 * response to a Java Web Start request.
 *
 * Signing occurs when the jar is first requested.  
 *
 * @author tjquinn
 */
public class SignedStaticContent extends StaticContent {
    
    /** the unsigned jar to be served in signed form */
    private final File unsignedJar;
    
    /** the signed jar */
    private final File signedJar;
    
    /** URI for the app server installation root */
    private URI installRootURI;

    private final Logger logger = DeploymentLogger.get();

    private final StringManager localStrings;
    
    
    /**
     * Creates a new instance of SignedStaticContent
     * 
     * @param origin the origin from which the jar to be signed comes
     * @param contentKey key by which this jar will be retrievable when requested
     * @param path the relative path within the app server to the jar
     * @param signedJar specifies what the resulting signed jar file should be
     * @param unsignedjar the existing unsigned jar to be signed just-in-time
     * @param installRootURI the app server's installation directory
     * @param isMain indicates if the jar contains the mail class that Java Web Start should launch
     */
    
    public SignedStaticContent(
            ContentOrigin origin, 
            String contentKey, 
            String path, 
            File signedJar,
            File unsignedJar, 
            URI installRootURI,
            StringManager localStrings,
            boolean isMain) throws Exception {
        super(origin, contentKey, path, signedJar, installRootURI, isMain);
        
        /*
         *Find out as much as we can in order to sign the jar, but do not sign it yet.
         */
        this.installRootURI = installRootURI;
        this.unsignedJar = unsignedJar;
        this.signedJar = signedJar;
        this.localStrings = localStrings;
    }
    
    /**
     *Returns the URI, relative to the app server installation directory, of
     *the signed jar file to be published, signing the unsigned jar if needed
     *to create the signed one to serve.
     *@return relative URI to the jar
     */
    public URI getRelativeURI() {
        try {
            ensureSignedFileUpToDate();
            return installRootURI.relativize(signedJar.toURI());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     *Makes sure that the signed jar exists and is up-to-date compared to the
     *corresponding unsigned jar.  If not, create a new signed jar using
     *the alias provided on this object's constructor.
     *@throws KeyStoreException in case of errors reading the key store
     *@throws IllegalArgumentException if the unsigned jar does not exists
     */
    private synchronized void ensureSignedFileUpToDate() throws KeyStoreException, IllegalArgumentException, Exception {
        /*
         *Check to see if the signed version of this jar is present.
         */
        if ( ! unsignedJar.exists()) {
            throw new IllegalArgumentException(
                    localStrings.getString("jws.sign.noUnsignedJar", unsignedJar.getAbsolutePath()));
        }

        if ( ! signedJar.exists() || (signedJar.lastModified() < unsignedJar.lastModified())) {
            signJar();
        }
    }
    
    /**
     *Signs the jar file.
     *@throws Exception when signing the JAR or obtaining keystore information
     */
    private void signJar() throws Exception {
        /*
         *In EE environments synchronization does not include empty directories.
         *So the java-web-start/<app-name> directory may not yet exist if this
         *is a non-DAS instance.  So just make sure the required directories
         *are created.
         */
        File signedJarParent = signedJar.getParentFile();
        if ( ! signedJarParent.exists() && ! signedJarParent.mkdirs() ) {
            throw new Exception(localStrings.getString("jws.sign.errorCreatingDir", signedJarParent.getAbsolutePath()));
        }
        
        ASJarSigner.signJar(unsignedJar, signedJar);
    }
    
    /**
     *Returns the password for the keystore.
     *@return the keystore password
     */
    private String getKeystorePassword() {
        return SSLUtils.getKeyStorePass();
    }

    /**
     *Returns whether the specified alias is present in the keystore or not.  Also
     *logs a warning if the user-specified alias is missing.
     *@param keystore the keystore to use in checking the user alias
     *@param candidateAlias the alias to look for
     *@return true if the alias is present in the keystore; false otherwise
     *@throws KeyStoreException in case of error accessing the keystore
     */
    private boolean checkUserAlias(KeyStore keystore, String candidateAlias) throws KeyStoreException {
        boolean result;
        if ( ! (result = keystore.containsAlias(candidateAlias)) ) {
            logger.warning(localStrings.getString("jws.sign.userAliasAbsent", candidateAlias));
        }
        return result;
    }
}
