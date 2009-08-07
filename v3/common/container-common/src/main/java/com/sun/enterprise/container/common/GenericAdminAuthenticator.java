/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.container.common;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.api.admin.ServerEnvironment;

import javax.security.auth.login.LoginException;

import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.RealmConfig;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.AdminService;

import java.util.List;
import java.util.Enumeration;
import java.io.File;

/** Implementation of {@link AdminAccessController} that delegates to LoginContextDriver.
 */
@Service
public class GenericAdminAuthenticator implements AdminAccessController {
    @Inject
    volatile SecurityService ss = null;

    @Inject
    volatile ServerEnvironment env = null;

    @Inject
    volatile AdminService as = null;
    
    public void login(String user, String password, String realm) throws LoginException {
        AuthRealm fr = getFileRealm(realm);
        if (fr != null) { //this is rather ugly, but that's life
            handleFileRealm(user, password, fr); //you know for sure that the given realm is File Realm
        }
        else {
            RealmConfig.createRealms(as.getAuthRealmName(), ss.getAuthRealm());
            LoginContextDriver.login(user, password, realm);
        }
    }

    private void handleFileRealm(String user, String password, AuthRealm fr) throws LoginException {
        //finally!
        //if the realm allows anonymous login -> i.e. if there is ONE user named "anonymous" in keyfile, all bets are off
        //if the realm file is missing, then too, we allow access!
        try {
            if (anonLoginOrFileMissing(fr)) {
                return;
            }
        } catch(Exception e) {
            LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
        RealmConfig.createRealms(as.getAuthRealmName(), ss.getAuthRealm());
        LoginContextDriver.login(user, password, fr.getName());
    }

    private boolean anonLoginOrFileMissing(AuthRealm fr) throws Exception {
        String value    = fr.getPropertyValue("file"); //file -> actual keyfile!
        File f = new File(value);
        if (!f.exists())
            return true;  //all accesses allowed!
        FileRealm realm = new FileRealm(f.getAbsolutePath());
        Enumeration users = realm.getUserNames();
        int size = 0;
        while (users.hasMoreElements())
            size++;
        if (size > 1)
            return false; //there are more than one users - no anon login allowed
        //re-fetch the enum -- pain!
        users = realm.getUserNames();
        return "anonymous".equals(users.nextElement()); //we already know it has exactly one
    }

    private AuthRealm getFileRealm(String name) {
        //returns an AuthRealm only if its name is same as given name and it's a FileRealm, null otherwise
        List<AuthRealm> realms = ss.getAuthRealm();
        for (AuthRealm realm : realms) {
            if (realm.getName().equals(name) && FileRealm.class.getName().equals(realm.getClassname())) {
                return realm;
            }
        }
        return null;
    }
}
