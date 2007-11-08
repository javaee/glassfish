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
package com.sun.enterprise.security;

import java.security.Principal;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;

import com.sun.enterprise.security.auth.login.PasswordCredential;
import com.sun.enterprise.deployment.PrincipalImpl;
import com.sun.enterprise.ServerConfiguration;

import java.util.logging.*;
import com.sun.logging.*;


/**
 * This class represents the security context on the client side.
 * For usage of the IIOP_CLIENT_PER_THREAD_FLAG flag, see
 * UsernamePasswordStore. When set to false, the volatile
 * field sharedCsc is used to store the context.
 *
 * @see UsernamePasswordStore
 * @author Harpreet Singh
 *
 */
public final class ClientSecurityContext extends AbstractSecurityContext {
    
    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.SECURITY_LOGGER);

    public static final String IIOP_CLIENT_PER_THREAD_FLAG =
        "com.sun.appserv.iiopclient.perthreadauth";

    // Bug Id: 4787940
    private static final boolean isPerThreadAuth = 
            Boolean.getBoolean(IIOP_CLIENT_PER_THREAD_FLAG);

    // either the thread local or shared version will be used
    private static ThreadLocal localCsc =
        isPerThreadAuth ? new ThreadLocal() : null;
    private static volatile ClientSecurityContext sharedCsc;

    /**
     * This creates a new ClientSecurityContext object.
     * @param The name of the user.
     * @param The Credentials of the user.
     */
    public ClientSecurityContext(String userName, 
				 Subject s) {

	this.initiator = new PrincipalImpl(userName);
	this.subject = s ;
    }

    /**
     * Initialize the SecurityContext & handle the unauthenticated
     * principal case
     */
    public static ClientSecurityContext init() {
	ClientSecurityContext sc = getCurrent();
	if (sc == null) { // there is no current security context
            // create a default one if
	    sc = generateDefaultSecurityContext();
        }
	return sc;
    }
    
    private static ClientSecurityContext generateDefaultSecurityContext() {
	final String PRINCIPAL_NAME = "auth.default.principal.name";
	final String PRINCIPAL_PASS = "auth.default.principal.password";
	
	ServerConfiguration config = ServerConfiguration.getConfiguration();
	String username = config.getProperty(PRINCIPAL_NAME, "guest");
	String password = config.getProperty(PRINCIPAL_PASS, "guest123");
	
        synchronized (ClientSecurityContext.class) {
            // login & all that stuff..
            try {
                final Subject subject = new Subject();
                final PasswordCredential pc = new PasswordCredential(username,
                        password, "default");
                AppservAccessController.doPrivileged(new PrivilegedAction() {
                    public java.lang.Object run() {
                        subject.getPrivateCredentials().add(pc);
                        return null;
                    }
                });
                // we do not need to generate any credential as authorization
                // decisions are not being done on the appclient side.
                ClientSecurityContext defaultCSC =
                    new ClientSecurityContext(username, subject);
                setCurrent(defaultCSC);
                return defaultCSC;
            } catch(Exception e) {
                _logger.log(Level.SEVERE,
                            "java_security.gen_security_context", e);
                return null;
            }
        }
    }

    /**
     * This method gets the SecurityContext stored here.  If using a
     * per-thread authentication model, it gets the context from
     * Thread Local Store (TLS) of the current thread. If not using a
     * per-thread authentication model, it gets the singleton context.
     *
     * @return The current Security Context stored here. It returns
     *      null if SecurityContext could not be found.
     */
    public static ClientSecurityContext getCurrent() {
        if (isPerThreadAuth) {
            return (ClientSecurityContext) localCsc.get();
        } else {
            return sharedCsc;
        }
    }

    /**
     * This method sets the SecurityContext to be stored here.
     * 
     * @param The Security Context that should be stored.
     */
    public static void setCurrent(ClientSecurityContext sc) {
        if (isPerThreadAuth) {
            localCsc.set(sc);
        } else {
            sharedCsc = sc;
        }
    } 

    /**
     * This method returns the caller principal. 
     * This information may be redundant since the same information 
     * can be inferred by inspecting the Credentials of the caller.
     * 
     * @return The caller Principal. 
     */
    public Principal getCallerPrincipal() {
	return initiator;
    }

    
    public Subject getSubject() {
	return subject;
    }

    public String toString() {
	return "ClientSecurityContext[ " + "Initiator: " + initiator +
	    "Subject " + subject + " ]";
    }

}







