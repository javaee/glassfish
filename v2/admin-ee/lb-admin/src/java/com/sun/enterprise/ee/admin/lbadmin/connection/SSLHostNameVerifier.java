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
/*
 * SSLHostNameVerifier.java
 *
 * Created on July 30, 2005, 12:08 AM
 */

package com.sun.enterprise.ee.admin.lbadmin.connection;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;
import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;

/**
 *
 * @author sv96363
 */
public class SSLHostNameVerifier implements HostnameVerifier {
    
    /**
     * matches the hostname of the Load balancer to CN attribute of the
     * certificate obtained.
     * @param hostname hostname of the load balancer
     * @param session  SSL session information
     * @return true - if the LB host name and CN attribute in the certificate
     * matches, false otherwise
     */
    public boolean verify(String hostname, SSLSession session) {
        if (session != null) {
            Certificate[] certs = null;
            try {
                certs = session.getPeerCertificates();
            } catch (Exception e) {
            }
            if (certs == null) {
                 String msg = _strMgr.getString("NoPeerCert",hostname);
                _logger.info(msg);
                return false;
            }
            for(int i=0; i < certs.length; i++) {
                if (certs[i] instanceof X509Certificate) {
                    X500Principal prin =
                        ((X509Certificate)certs[i]).getSubjectX500Principal();
                    String hName = null;
                    String dn = prin.getName();
                    // Look for name of the cert in the CN attribute
                    int cnIdx = dn.indexOf("CN="); 
                    if (cnIdx != -1) {
                        String cnStr = dn.substring(cnIdx, dn.length());
                        int commaIdx = cnStr.indexOf(",");
                        // if the CN is the last element in the string, then
                        // there won't be a ',' after that.
                        // The principal could be either CN=chandu.sfbay,C=US
                        // or C=US,CN=chandu.sfbay
                        if (commaIdx == -1) {
                            commaIdx = dn.length();
                        }
                        hName = dn.substring(cnIdx+3,commaIdx);
                    }
                    if (hostname.equals(hName)) {
                        return true;
                    }
                } else {
                     String msg = _strMgr.getString("NotX905Cert",hostname);
                    _logger.info(msg);
                }
            }
            // Now, try to match if it matches the hostname from the SSLSession
            if (hostname.equals(session.getPeerHost())) {
                return true;
            }
        }
        String msg = _strMgr.getString("NotCertMatch",hostname,new
            String(session.getId()));
        _logger.info(msg);
        return false;
    }

    private static final StringManager _strMgr = 
        StringManager.getManager(SSLHostNameVerifier.class);
    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
    
}
