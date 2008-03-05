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
package com.sun.enterprise.security.ssl;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.Subject;

//import com.sun.enterprise.Switch;
import com.sun.enterprise.security.ClientSecurityContext;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.security.auth.login.X509CertificateCredential;
import com.sun.enterprise.security.auth.LoginContextDriver;
import java.util.logging.*;
import com.sun.logging.*;

/**
 * This a J2EE specific Key Manager class that is used to select 
 * user certificates for SSL client authentication. It delegates most
 * of the functionality to the provider specific KeyManager class.
 * @author Vivek Nagar
 * @author Harpreet Singh
 */
public final class J2EEKeyManager implements X509KeyManager {

    private static Logger _logger=null;  
    static {
        _logger=LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    }

    private X509KeyManager mgr = null; // delegate
    
    private String alias = null;

    private Map tokenName2MgrMap = null;
    private boolean supportTokenAlias = false;

    public J2EEKeyManager(X509KeyManager mgr, String alias) {
	this.mgr = mgr;
	this.alias = alias;

        if (mgr instanceof UnifiedX509KeyManager) {
            UnifiedX509KeyManager umgr = (UnifiedX509KeyManager)mgr;
            X509KeyManager[] mgrs = umgr.getX509KeyManagers();
            String[] tokenNames = umgr.getTokenNames();

            tokenName2MgrMap = new HashMap();
            for (int i = 0; i < mgrs.length; i++) {
                if (tokenNames[i] != null) {
                    tokenName2MgrMap.put(tokenNames[i], mgrs[i]);
                }
            }
            supportTokenAlias = (tokenName2MgrMap.size() > 0);
        }
    }

    /**
     * Choose the client alias that will be used to select the client
     * certificate for SSL client auth.
     * @param the keytype
     * @param the certificate issuers.
     * @param the socket used for this connection. This parameter can be null,
     *        in which case the method will return the most generic alias to use.
     * @return the alias.
     */
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
    Socket socket) {
        
        String alias = null;
        
        if(this.alias == null){
            //InvocationManager im = Switch.getSwitch().getInvocationManager();
            //if (im == null) {
            if(!isAppClientContainer()) {
                // standalone client
                alias = mgr.chooseClientAlias(keyType, issuers, socket);
            } else {
                //ComponentInvocation ci = im.getCurrentInvocation();
                
                //if (ci == null) {       // 4646060
                //    throw new InvocationException();
                //}
                
                //Object containerContext = ci.getContainerContext();
                //if(containerContext != null &&
                //(containerContext instanceof AppContainer)) {
                    
                    ClientSecurityContext ctx = ClientSecurityContext.getCurrent();
                    Subject s = ctx.getSubject();
                    if(s == null) {
                        // pass the handler and do the login
                        //TODO V3: LoginContextDriver.doClientLogin(AppContainer.CERTIFICATE,
                        //AppContainer.getCallbackHandler());
                        LoginContextDriver.doClientLogin(SecurityUtil.APPCONTAINER_CERTIFICATE,
                        SecurityUtil.getAppContainerCallbackHandler());
                        s = ctx.getSubject();
                    }
                    Iterator itr = s.getPrivateCredentials().iterator();
                    while(itr.hasNext()) {
                        Object o = itr.next();
                        if(o instanceof X509CertificateCredential) {
                            X509CertificateCredential crt =
                            (X509CertificateCredential) o;
                            alias = crt.getAlias();
                            break;
                        }
                    }
                //}
            }
        }else{
            alias = this.alias;
        }
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE,
            "Choose client Alias :" + alias);
        }
        return alias;
    }

    /**
     * Choose the server alias that will be used to select the server
     * certificate for SSL server auth.
     * @param the keytype
     * @param the certificate issuers.
     * @param the socket used for this connection. This parameter can be null,
     *        in which case the method will return the most generic alias to use.
     * @return the alias
     */
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {

        String alias = null;
        if(this.alias != null){
            alias = this.alias;
        }else{
            alias =  mgr.chooseServerAlias(keyType, issuers, socket);
	}
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE,"Choosing server alias :"+ alias);
        }         
        return alias;
    }

    /**
     * Return the certificate chain for the specified alias.
     * @param the alias.
     * @return the chain of X509 Certificates.
     */
    public X509Certificate[] getCertificateChain(String alias) {
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE,"Getting certificate chain");
        }
        X509KeyManager keyMgr = getManagerFromToken(alias);
        if (keyMgr != null) {
            String aliasName = alias.substring(alias.indexOf(':') + 1);
            return keyMgr.getCertificateChain(aliasName);
        } else {
            return mgr.getCertificateChain(alias);
        }
    }

    /**
     * Return all the available client aliases for the specified key type.
     * @param the keytype
     * @param the certificate issuers.
     * @return the array of aliases.
     */
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        if(_logger.isLoggable(Level.FINE)){
	    _logger.log(Level.FINE,"Getting client aliases");
        }
	return mgr.getClientAliases(keyType, issuers);
    }

    /**
     * Return all the available server aliases for the specified key type.
     * @param the keytype
     * @param the certificate issuers.
     * @return the array of aliases.
     */
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE,"Getting server aliases");
        }
        return mgr.getServerAliases(keyType, issuers);
    }

    /**
     * Return the private key for the specified alias.
     * @param the alias.
     * @return the private key.
     */
    public PrivateKey getPrivateKey(String alias) {
        if(_logger.isLoggable(Level.FINE)){
	    _logger.log(Level.FINE,"Getting private key for alias:" + alias);
	}
        X509KeyManager keyMgr = getManagerFromToken(alias);
        if (keyMgr != null) {
            String aliasName = alias.substring(alias.indexOf(':') + 1);
            return keyMgr.getPrivateKey(aliasName);
        } else {
            return mgr.getPrivateKey(alias);
        }
    }    

    
    /**
     * Find the corresponding X509KeyManager associated to token in alias.
     * It returns null if there is n
     * @param tokenAlias of the form &lt;tokenName&gt;:&lt;aliasName&gt;
     */
    private X509KeyManager getManagerFromToken(String tokenAlias) {
        X509KeyManager keyMgr = null;
        int ind = -1;
        if (supportTokenAlias && tokenAlias != null && (ind = tokenAlias.indexOf(':')) != -1) {
            String tokenName = alias.substring(0, ind);
            keyMgr = (X509KeyManager)tokenName2MgrMap.get(tokenName);
        }
        return keyMgr;
    }

    //TODO: Code this correctly
    private boolean isAppClientContainer() {
        return true;
    }
}

