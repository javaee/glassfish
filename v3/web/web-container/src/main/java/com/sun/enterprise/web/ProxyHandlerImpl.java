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

package com.sun.enterprise.web;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.servlet.http.HttpServletRequest;
import com.sun.appserv.ProxyHandler;

/**
 * Default ProxyHandler implementation.
 */
public class ProxyHandlerImpl extends ProxyHandler {

    /**
     * Gets the SSL client certificate chain with which the client
     * had authenticated itself to the SSL offloader, and which the
     * SSL offloader has added as a custom request header on the
     * given request.
     *
     * @param request The request from which to retrieve the SSL client
     * certificate chain
     *
     * @return Array of java.security.cert.X509Certificate instances
     * representing the SSL client certificate chain, or null if this
     * information is not available from the given request
     *
     * @throws CertificateException if the certificate chain retrieved
     * from the request header cannot be parsed
     */
    public X509Certificate[] getSSLClientCertificateChain(
                        HttpServletRequest request)
            throws CertificateException {

        X509Certificate[] certs = null;

        String clientCert = request.getHeader("Proxy-auth-cert");
        if (clientCert != null) {
            clientCert = clientCert.replaceAll("% d% a", "\n");
            clientCert = "-----BEGIN CERTIFICATE-----\n" + clientCert
                         + "\n-----END CERTIFICATE-----";
            byte[] certBytes = new byte[clientCert.length()];
            clientCert.getBytes(0, clientCert.length(), certBytes, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certs = new X509Certificate[1];
            certs[0] = (X509Certificate) cf.generateCertificate(bais);
        }

        return certs;
    }

    /**
     * Returns the SSL keysize with which the original client request that
     * was intercepted by the SSL offloader has been protected, and which
     * the SSL offloader has added as a custom request header on the
     * given request.
     *
     * @param request The request from which to retrieve the SSL key
     * size
     *
     * @return SSL keysize, or -1 if this information is not available from
     * the given request
     */
    public int getSSLKeysize(HttpServletRequest request) {

        int keySize = -1;

        String header = request.getHeader("Proxy-keysize");
        if (header != null) {
            keySize = Integer.parseInt(header);
        }

        return keySize;   
    }

    /**
     * Gets the Internet Protocol (IP) source port of the client request that
     * was intercepted by the proxy server.
     *
     * @param request The request from which to retrieve the IP source port
     * of the original client request
     *
     * @return IP source port of the original client request, or null if this
     * information is not available from the given request
     */
    public String getRemoteAddress(HttpServletRequest request) {
        return request.getHeader("Proxy-ip");
    }
}
