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

package com.sun.enterprise.security.auth.login.common;

import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.common.AppservAccessController;
import java.io.*;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import org.glassfish.security.common.Group;


/**
 * This is the default callback handler provided by the application
 * client container. The container tries to use the application specified 
 * callback handler (if provided). If there is no callback handler or if
 * the handler cannot be instantiated then this default handler is used.
 */
public class ServerLoginCallbackHandler implements CallbackHandler 
{
    private String username = null;
    private String password = null;
    private String contextRoot = null;

    public ServerLoginCallbackHandler(String username, String password) {
	this.username = username;
	this.password = password;
    }

    public ServerLoginCallbackHandler(String username, String password, String contextRoot) {
	this.username = username;
	this.password = password;
        this.contextRoot = contextRoot;
    }
    
    public ServerLoginCallbackHandler(){
    }
    
    public void setUsername(String user){
	username = user;
    }
    
    public void setPassword(String pass){
	password = pass;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    
    /**
     * This is the callback method called when authentication data is
     * required. It either pops up a dialog box to request authentication
     * data or use text input.
     * @param the callback object instances supported by the login module.
     */
    public void handle(Callback[] callbacks) throws IOException,
					UnsupportedCallbackException
    {
	for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback){
		NameCallback nme = (NameCallback)callbacks[i];
		nme.setName(username);
	    } else if (callbacks[i] instanceof PasswordCallback){
		PasswordCallback pswd = (PasswordCallback)callbacks[i];
		pswd.setPassword(password.toCharArray());
	    } else if (callbacks[i] instanceof CertificateRealm.AppContextCallback){
                ((CertificateRealm.AppContextCallback) callbacks[i]).setAppName(contextRoot);
            } else if (callbacks[i] instanceof GroupPrincipalCallback){
                processGroupPricipal((GroupPrincipalCallback) callbacks[i]);
            }
	}
    }

    private void processGroupPricipal(GroupPrincipalCallback gpCallback) {
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
                    Set<Principal> principalSet = fs.getPrincipals();
                    principalSet.removeAll(fs.getPrincipals(Group.class));
                    return fs;
                }
            });
        }
    }
}

