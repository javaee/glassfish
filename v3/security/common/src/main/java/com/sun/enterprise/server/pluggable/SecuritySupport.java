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
package com.sun.enterprise.server.pluggable;

import java.security.KeyStore;
//V3:Commented import com.sun.enterprise.config.ConfigContext;
import javax.security.auth.callback.CallbackHandler;
import org.jvnet.hk2.annotations.Contract;

/**
 * SecuritySupport is part of PluggableFeature that provides access to
 * internal services managed by application server.
 * @author Shing Wai Chan
 */
@Contract
public interface SecuritySupport {

    /**
     * This method returns an array of keystores containing keys and
     * certificates.
     */
    public KeyStore[] getKeyStores();

    /**
     * This method returns an array of truststores containing certificates.
     */
    public KeyStore[] getTrustStores();

    /**
     * This method returns an array of passwords in order corresponding to
     * array of keystores.
     */
    public String[] getKeyStorePasswords();

    /**
     * @param  token 
     * @return a keystore. If token is null, return the the first keystore.
     */
    public KeyStore getKeyStore(String token);

    /**
     * @param  token 
     * @return a truststore. If token is null, return the first truststore.
     */
    public KeyStore getTrustStore(String token);

    /**
     * @param  token 
     * @return the password for this token. 
     */
    public String getKeyStorePassword(String token);
    
    /**
     * This method returns an array of token names in order corresponding to
     * array of keystores.
     */
    public String[] getTokenNames();

    /**
     * This method synchronize key file for given realm.
     * @param config the ConfigContextx
     * @param fileRealmName
     * @exception if fail to synchronize, a known exception is
     *            com.sun.enterprise.ee.synchronization.SynchronizationException
     */
    /** TODO:V3:Cluster ConfigContext is no longer present so find out what this needs to be */
    //public void synchronizeKeyFile(ConfigContext config, String fileRealmName)
    public void synchronizeKeyFile(Object configContext, String fileRealmName)
        throws Exception;
    
}
