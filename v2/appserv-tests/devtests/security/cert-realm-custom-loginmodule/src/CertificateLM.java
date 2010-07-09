/*
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.certrealm.lm;

import com.sun.appserv.security.AppservCertificateLoginModule;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.security.auth.login.LoginException;

/**
 *
 * @author nasradu8
 */
public class CertificateLM extends AppservCertificateLoginModule {

    @Override
    protected void authenticateUser() throws LoginException {
        // Get the distinguished name from the X500Principal.
        String dname = getX500Principal().getName();
        StringTokenizer st = new StringTokenizer(dname, " \t\n\r\f,");
        _logger.log(Level.INFO, "Appname: " + getAppName() + " accessed by " + getX500Principal().getName());
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            // At this point, one has the application name and the DN of
            // the certificate. A suitable login decision can be made here.
            if (next.startsWith("CN=")) {
		String cname = next.substring(3);
		if (cname.equals("SSLTest")){
			commitUserAuthentication(new String[]{getAppName() + ":alice-group"});
			return;
		}
            }
        }
        throw new LoginException("No OU found.");
    }
}
