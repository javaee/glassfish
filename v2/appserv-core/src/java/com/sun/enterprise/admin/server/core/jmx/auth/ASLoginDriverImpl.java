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
package com.sun.enterprise.admin.server.core.jmx.auth;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.security.auth.LoginContextDriver;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.util.i18n.StringManager;

public class ASLoginDriverImpl implements LoginDriver {

    private static final String ASADMIN_GROUP = "asadmin";

    private static Logger _logger =
            Logger.getLogger(AdminConstants.kLoggerName);

    private static StringManager _strings =
            StringManager.getManager(ASLoginDriverImpl.class);

    public ASLoginDriverImpl() {
    }

    public Subject login(String user, String password, String realm) {
        LoginContextDriver.login(user, password, realm);
        // Login succeeded, try authorization
        authorize(user, password, realm);
        /* **
        ** TODO: The subject needs to be initialized properly 
        JMXPrincipal principal = new JMXPrincipal(user);
        HashSet principalSet = new HashSet();
        principalSet.add(principal);
        Subject subj = new Subject(true, principalSet, new HashSet(),
                new HashSet());
        return subj;
        ** */
        return null;
    }

    private void authorize(String user, String password, String realmName) {
        boolean isAuthorized = false;
        try {
            boolean isValid = Realm.isValidRealm(realmName);
            if (!isValid) {
                realmName = Realm.getDefaultRealm();
            }
            Realm realm = Realm.getInstance(realmName);
            Enumeration groups = realm.getGroupNames(user);
            while (groups != null && groups.hasMoreElements()) {
                String groupName = (String)groups.nextElement();
                if (ASADMIN_GROUP.equals(groupName)) {
                    isAuthorized = true;
                    break;
                }
            }
        } catch (Exception ee) {
            _logger.log(Level.WARNING, "core.auth_failed", realmName);
            _logger.log(Level.INFO, "core.auth_fail_exception", ee);
            SecurityException se = new SecurityException(
                    _strings.getString("admin.auth.failed"));
            se.initCause(ee);
            throw se;
        }
        if (!isAuthorized) {
            throw new SecurityException(
                    _strings.getString("admin.auth.failed.nogroup"));
        }
        return;
    }

}
