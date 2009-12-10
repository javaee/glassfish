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
import com.sun.enterprise.security.*;
import org.glassfish.internal.api.LocalPassword;
import com.sun.enterprise.admin.util.AdminConstants;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.AdminService;
import org.glassfish.internal.api.*;
import org.glassfish.security.common.Group;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Habitat;

import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;
import javax.management.remote.JMXAuthenticator;
import java.util.logging.Logger;
import java.util.Enumeration;
import java.util.Set;
import java.io.File;

/** Implementation of {@link AdminAccessController} that delegates to LoginContextDriver.
 *  @author Kedar Mhaswade (km@dev.java.net)
 *  This is still being developed. This particular implementation both authenticates and authorizes
 *  the users directly or indirectly. <p>
 *  <ul>
 *    <li> Authentication works by either calling FileRealm.authenticate() or by calling LoginContextDriver.login </li>
 *    <li> The admin users in case of administration file realm are always in a fixed group called "asadmin". In case
 *         of LDAP, the specific group relationships are enforced. </li>
 *  </ul>
 *  Note that admin security is tested only with FileRealm and LDAPRealm.
 *  @see com.sun.enterprise.security.cli.LDAPAdminAccessConfigurator
 *  @see com.sun.enterprise.security.cli.CreateFileUser
 *  @since GlassFish v3
 */
@Service
@ContractProvided(JMXAuthenticator.class)
public class GenericAdminAuthenticator implements AdminAccessController, JMXAuthenticator {
    @Inject
    Habitat habitat;
    
    @Inject
    SecuritySniffer snif;

    @Inject
    volatile SecurityService ss;

    @Inject
    volatile AdminService as;

    @Inject
    LocalPassword localPassword;

    @Inject
    ServerContext sc;

    private static LocalStringManagerImpl lsm = new LocalStringManagerImpl(GenericAdminAuthenticator.class);
    
    private final Logger logger = Logger.getAnonymousLogger();

    /** Ensures that authentication and authorization works as specified in class documentation.
     *
     * @param user String representing the user name of the user doing an admin opearation
     * @param password String representing clear-text password of the user doing an admin operation
     * @param realm String representing the name of the admin realm for given server
     * @return boolean representing successful authentication and group membership
     * @throws LoginException
     */
    public boolean loginAsAdmin(String user, String password, String realm) throws LoginException {
        boolean isLocal = isLocalPassword(user, password); //local password gets preference
        if (isLocal)
            return true;
        if (as.usesFileRealm())
            return handleFileRealm(user, password);
        else {
            //now, deleate to the security service
            ClassLoader pc = null;
            boolean hack = false;
            boolean authenticated = false;
            try {
                pc = Thread.currentThread().getContextClassLoader();
                if (!sc.getCommonClassLoader().equals(pc)) { //this is per Sahoo
                    Thread.currentThread().setContextClassLoader(sc.getCommonClassLoader());
                    hack = true;
                }
                Inhabitant<SecurityLifecycle> sl = habitat.getInhabitantByType(SecurityLifecycle.class);
                sl.get();
                snif.setup(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + "/modules/security", Logger.getAnonymousLogger());
                LoginContextDriver.login(user, password, realm);
                authenticated = true;
                if (as.getAssociatedAuthRealm().getGroupMapping() != null)
                    return ensureGroupMembership(user, realm);
                else
                    return true;
           } catch(Exception e) {
//              LoginException le = new LoginException("login failed!");
//              le.initCause(e);
//              thorw le //TODO need to work on this, this is rather too ugly
                return false;
           } finally {
                if (hack)
                    Thread.currentThread().setContextClassLoader(pc);
            }
        }
    }

    private boolean ensureGroupMembership(String user, String realm) {
        try {
            SecurityContext sc = SecurityContext.getCurrent();
            Set ps = sc.getPrincipalSet(); //before generics
            for (Object principal : ps) {
                if (principal instanceof Group) {
                    Group group = (Group) principal;
                    if (group.getName().equals(AdminConstants.DOMAIN_ADMIN_GROUP_NAME))
                        return true;
                }
            }
            logger.fine("User is not the member of the special admin group");
            return false;
        } catch(Exception e) {
            logger.fine("User is not the member of the special admin group: " + e.getMessage());
            return false;
        }

    }

    private boolean handleFileRealm(String user, String password) throws LoginException {
        /* I decided to handle FileRealm  as a special case. Maybe it is not such a good idea, but
           loading the security subsystem for FileRealm is really not required.
         * If no user name was supplied, assume the default admin user name,
         * if there is one.
         */
        if (user == null || user.length() == 0) {
            String defuser = getDefaultAdminUser();
            if (defuser != null) {
                user = defuser;
                logger.fine("Using default user: " + defuser);
            } else
                logger.fine("No default user");
        }

        try {
            AuthRealm ar = as.getAssociatedAuthRealm();
            if (FileRealm.class.getName().equals(ar.getClassname())) {
                String adminKeyFilePath = ar.getPropertyValue("file");
                FileRealm fr = new FileRealm(adminKeyFilePath);
                FileRealmUser fru = (FileRealmUser)fr.getUser(user);
                for (String group : fru.getGroups()) {
                    if (group.equals(AdminConstants.DOMAIN_ADMIN_GROUP_NAME))
                        return fr.authenticate(user, password) != null; //this is indirect as all admin-keyfile users are in group "asadmin"
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

    /**
     * Return the default admin user.  A default admin user only
     * exists if the admin realm is a file realm and the file
     * realm contains exactly one user.  If so, that's the default
     * admin user.
     */
    private String getDefaultAdminUser() {
        AuthRealm realm = as.getAssociatedAuthRealm();
        if (realm == null) {
            //this is really an assertion -- admin service's auth-realm-name points to a non-existent realm
            throw new RuntimeException("Warning: Configuration is bad, realm: " + as.getAuthRealmName() + " does not exist!");
        }
        if (! FileRealm.class.getName().equals(realm.getClassname())) {
            logger.fine("CAN'T FIND DEFAULT ADMIN USER: IT'S NOT A FILE REALM");
            return null;  // can only find default admin user in file realm
        }
        String pv = realm.getPropertyValue("file");  //the property named "file"
        File   rf = null;
        if (pv == null || !(rf=new File(pv)).exists()) {
            //an incompletely formed file property or the file property points to a non-existent file, can't allow access
            logger.fine("CAN'T FIND DEFAULT ADMIN USER: THE KEYFILE DOES NOT EXIST");
            return null;
        }
        try {
            FileRealm fr = new FileRealm(rf.getAbsolutePath());
            Enumeration users = fr.getUserNames();
            if (users.hasMoreElements()) {
                String au = (String) users.nextElement();
                if (!users.hasMoreElements()) {
                    FileRealmUser fru = (FileRealmUser)fr.getUser(au);
                    for (String group : fru.getGroups()) {
                        if (group.equals(AdminConstants.DOMAIN_ADMIN_GROUP_NAME))
                            // there is only one admin user, in the right group, default to it
                            logger.fine("Attempting access using default admin user: " + au);
                            return au;
                    }
                }
            }
        } catch(Exception e) {
            return null;
        }
        return null;
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

    /**
     * The JMXAUthenticator's authenticate method.
     */
    @Override
    public Subject authenticate(Object credentials) {
        String user = "", password = "";
        if (credentials instanceof String[]) {
            // this is supposed to be 2-string array with user name and password
            String[] up = (String[])credentials;
            if (up.length == 1) {
                user = up[0];
            } else if (up.length >= 2) {
                user = up[0];
                password = up[1];
                if (password == null)
                    password = "";
            }
        }

        String realm = as.getSystemJmxConnector().getAuthRealmName(); //yes, for backward compatibility;
        if (realm == null)
            realm = as.getAuthRealmName();

        try {
            boolean ok = this.loginAsAdmin(user, password, realm);
            if (!ok) {
                String msg = lsm.getLocalString("authentication.failed",
                        "User [{0}] does not have administration access", user);
                throw new SecurityException(msg);
            }
            return null; //for now;
        } catch (LoginException e) {
            throw new SecurityException(e);
        }
    }
}
