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

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;

public class ASJMXAuthenticator implements JMXAuthenticator {

    private static final boolean _debug = false;
    
    private static Logger _logger =
            Logger.getLogger(AdminConstants.kLoggerName);

    private static StringManager _strings =
            StringManager.getManager(ASLoginDriverImpl.class);

    private String realmName;
    private LoginDriver loginDriver;

    public ASJMXAuthenticator() {
    }

    public ASJMXAuthenticator(String realmName) {
        setRealmName(realmName);
    }

    public void setRealmName(String realm) {
        // TODO: Add permission check.
        realmName = realm;
    }

    public String getRealmName() {
        return realmName;
    }

    public LoginDriver getLoginDriver() {
        return loginDriver;
    }

    public void setLoginDriver(LoginDriver driver) {
        // TODO: Add permission check.
        loginDriver = driver;
    }

    public Subject authenticate(Object credentials) {        
        if (credentials == null) {
            if (_debug) {
                System.out.println("JMXAuthenticator: Null credentials sent from the client");
            }
            throwInvalidCredentialsException();
        }
        if (!(credentials instanceof String[])) {
            if (_debug) {
                System.out.println("JMXAuthenticator: Invalid credentials sent from the client " + credentials.getClass().getName());
            }
            throwInvalidCredentialsException();
        }
        String[] userpass = (String[])credentials;
        if (userpass.length != 2) {
            if (_debug) {
                System.out.println("JMXAuthenticator: Invalid credentials sent from client, string array of length " + userpass.length);
            }
            throwInvalidCredentialsException();
        }
        if (_debug) {
            System.out.println("JMX authentication request for user "
                + userpass[0] + " and password " + userpass[1]);
            System.out.println("Authentication realm is " + realmName);
        }

        Subject subject = null;
        if (loginDriver != null) {
            subject = loginDriver.login(userpass[0], userpass[1], realmName);
        } else {
            // TODO: WARNING message, JMX connector not protected
        }
        return subject;
    }

    private void throwInvalidCredentialsException() {
        throw new SecurityException(
                _strings.getString("admin.auth.invalid.credentials"));
    }
}
