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
package com.sun.enterprise.container.common;

import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.file.FileRealmUser;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.SecurityLifecycle;
import com.sun.enterprise.security.SecuritySniffer;
import com.sun.enterprise.admin.util.AdminConstants;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.AdminService;
import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Habitat;

import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;
import javax.management.remote.JMXAuthenticator;
import java.util.logging.Logger;
import java.util.List;
import java.util.Enumeration;
import java.io.File;

/** Implementation of {@link AdminAccessController} that delegates to LoginContextDriver.
 *  @author Kedar Mhaswade (km@dev.java.net)
 */
@Service
public class GenericAdminAuthenticator implements AdminAccessController, JMXAuthenticator {
    @Inject
    Habitat habitat;
    
    @Inject
    SecuritySniffer snif;

    @Inject
    volatile SecurityService ss;
    private final String defaultAdminUser = SystemPropertyConstants.DEFAULT_ADMIN_USER;

    @Inject
    volatile AdminService as;

    @Inject
    LocalPassword localPassword;

    @Inject
    ClassLoaderHierarchy clh;

    private static LocalStringManagerImpl lsm = new LocalStringManagerImpl(GenericAdminAuthenticator.class);
    
    private final Logger logger = Logger.getAnonymousLogger();

    public boolean login(String user, String password, String realm) throws LoginException {
        if (as.usesFileRealm())
            return handleFileRealm(user, password, realm);
        else {
            //now, deleate to the security service
            ClassLoader pc = null;
            boolean hack = false;
            try {
                pc = Thread.currentThread().getContextClassLoader();
                if (!clh.getCommonClassLoader().equals(pc)) { //this is per Sahoo
                    Thread.currentThread().setContextClassLoader(clh.getCommonClassLoader());
                    hack = true;
                }
                Inhabitant<SecurityLifecycle> sl = habitat.getInhabitantByType(SecurityLifecycle.class);
                sl.get();
                snif.setup(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + "/modules/security", Logger.getAnonymousLogger());
                LoginContextDriver.login(user, password, realm);
                return true;
           } catch(Exception e) {
                LoginException le = new LoginException("login failed!");
                le.initCause(e);
                //thorw le //TODO need to work on this ...
                return false;
           } finally {
                if (hack)
                    Thread.currentThread().setContextClassLoader(pc);
            }
        }
    }

    private boolean handleFileRealm(String user, String password, String realm) throws LoginException {
        /* I decided to handle FileRealm  as a special case. Maybe it is not such a good idea, but
           loading the security subsystem for FileRealm is really not required.
         */
        boolean anonok = serverAllowsAnonymousFileRealmLogin();
        if (anonok) {
            return anonok;
        }

        boolean isLocal = isLocalPassword(user, password);
        if (isLocal)
            return true;

        try {
            AuthRealm ar = as.getAssociatedAuthRealm();
            if (FileRealm.class.getName().equals(ar.getClassname())) {
                String adminKeyFilePath = ar.getPropertyValue("file");
                FileRealm fr = new FileRealm(adminKeyFilePath);
                FileRealmUser fru = (FileRealmUser)fr.getUser(user);
                for (String group : fru.getGroups()) {
                    if (group.equals(AdminConstants.DOMAIN_ADMIN_GROUP_NAME))
                        return fr.authenticate(user, password) != null;
                }
                return false;
            }
        } catch(NoSuchUserException ue) {
            return false;       // if fr.getUser fails to find the user name
        } catch(Exception e) {
            LoginException le =  new LoginException (e.getMessage());
            le.initCause(e);
            throw le;
        }
        return false;
    }

    /* All bets are off when this method returns true, so implement it carefully. */
    private boolean serverAllowsAnonymousFileRealmLogin() {
        AuthRealm realm = as.getAssociatedAuthRealm();
        if (realm == null) {
            //this is really an assertion -- admin service's auth-realm-name points to a non-existent realm
            throw new RuntimeException("Warning: Configuration is bad, realm: " + as.getAuthRealmName() + " does not exist!");
        }
        if (! FileRealm.class.getName().equals(realm.getClassname())) {
            logger.fine("NOT ALLOWING ANONYMOUS ADMIN LOGIN, SINCE IT's NOT A FILE");
            return false;  //only file realm allows anonymous login
        }
        String pv = realm.getPropertyValue("file");  //the property named "file"
        File   rf = null;
        if (pv == null || !(rf=new File(pv)).exists()) {
            //an incompletely formed file property or the file property points to a non-existent file, allow access
            logger.fine("ALLOWING ANONYMOUS ADMIN LOGIN AS THE KEYFILE DOES NOT EXIST");
            return true;
        }
        try {
            FileRealm fr = new FileRealm(rf.getAbsolutePath());
            Enumeration users = fr.getUserNames();
            if (users.hasMoreElements()) {
                String au = (String) users.nextElement();
                if (defaultAdminUser.equals(au) && !users.hasMoreElements()) {
                    //there is only one user and that is anonymous
                    logger.fine("Allowing anonymous access");
                    return true;
                }
            }
        } catch(Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Check whether the password is the local password.
     * We ignore the user name but could check whether it's
     * a valid admin user name.
     */
    private boolean isLocalPassword(String user, String password) {
        if (!localPassword.isLocalPassword(password)) {
            logger.finest("Password is not the local password");
            return false;
        }
        logger.fine("Allowing access using local password");
        return true;
    }

    public Subject authenticate(Object credentials) {
        if (this.serverAllowsAnonymousFileRealmLogin()) {  //all bets are off if server allows anonymous login
            logger.fine("Allowing anonymous login for JMX Access");
            return null;
        }
        if (!(credentials instanceof String[])) {
            String msg = lsm.getLocalString("two.elem.array",
                    "The JMX Connector should access with a two-element string array containing user name and password");
            throw new SecurityException(msg);
        }
        //this is supposed to be 2-string array with user name and password
        String[] up = (String[])credentials;
        if (up.length < 2) {
            String msg = lsm.getLocalString("invalid.array",
                    "JMX Connector (client) provided an invalid array {0}, access denied", java.util.Arrays.toString(up));
            throw new SecurityException(msg);
        }
        String u = up[0], p = up[1];
        String realm = as.getSystemJmxConnector().getAuthRealmName(); //yes, for backward compatibility;
        if (realm == null)
            realm = as.getAuthRealmName();

        // XXX - allow local password for JMX?
        try {
            boolean ok = this.login(u, p, realm);
            if (!ok) {
                String msg = lsm.getLocalString("authentication.failed",
                        "User [{0}] does not have administration access", u);
                throw new SecurityException(msg);
            }
            return null; //for now;
        } catch (LoginException e) {
            throw new SecurityException(e);
        }
    }
}
