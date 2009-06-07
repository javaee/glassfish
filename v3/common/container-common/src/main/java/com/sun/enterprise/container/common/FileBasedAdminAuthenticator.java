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

import java.io.File;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.glassfish.internal.api.AdminAuthenticator;
import com.sun.grizzly.tcp.Request;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.GFBase64Decoder;

/**
 * User: Jerome Dochez
 * Date: Apr 15, 2008
 * Time: 10:30:54 PM
 */
@Service
public class FileBasedAdminAuthenticator implements AdminAuthenticator {

    private static final String BASIC = "Basic ";
    private static final GFBase64Decoder decoder = new GFBase64Decoder();    

    @Inject
    Logger logger;
    
    /**
     * authenticate incoming request
     * @param request incoming request
     * @param fileRealm file realm location
     *
     * @return true if authentication is successful.
     */
    public boolean authenticate(Request request, File fileRealm) throws Exception {

        String authHeader = request.getHeader("Authorization");
        boolean authenticated = false;

        // the file containing userid and password
        FileRealm f =
                new FileRealm(fileRealm.getAbsolutePath());

        authenticated = authenticateAnonymous(f); // allow anonymous login regardless
        if (!authenticated && authHeader != null) { // only if anonymous login is allowed.
            String base64Coded = authHeader.substring(BASIC.length());
            String decoded = new String(decoder.decodeBuffer(base64Coded));
            String[] userNamePassword = decoded.split(":");
            if (userNamePassword == null || userNamePassword.length == 0) {
                // no username/password in header - try anonymous auth
                authenticated = authenticateAnonymous(f);
            } else {
                String userName = userNamePassword[0];
                String password = userNamePassword.length > 1 ? userNamePassword[1] : "";
                authenticated = f.authenticate(userName, password) != null;
            }
        }
        return authenticated;
    }

    private boolean authenticateAnonymous(FileRealm f) throws Exception {
        Enumeration<String> users = f.getUserNames();
        if (users.hasMoreElements()) {
            String userNameInRealm = users.nextElement();
            // allow anonymous authentication if the only user in the key file is the
            // default user, with default password
            if (!users.hasMoreElements() &&
                    userNameInRealm.equals(SystemPropertyConstants.DEFAULT_ADMIN_USER)) {
                logger.finer("Allowed anonymous access");
                return true;
            }
        }
        return false;
    }
}
