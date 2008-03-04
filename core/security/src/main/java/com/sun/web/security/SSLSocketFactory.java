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
package com.sun.web.security;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.security.SSLUtils;
//V3:Commented import com.sun.enterprise.ServerConfiguration;
//V3:Commented import com.sun.web.server.*;
//V3:Commented import com.sun.enterprise.server.J2EEServer;
import com.sun.enterprise.security.SecurityServicesUtil;
import java.util.logging.*;
import com.sun.logging.*;


/**
 * SSL server socket factory. 
 *
 * @author Harish Prabandham
 * @author Vivek Nagar
 * @author Harpreet Singh
 */

public class SSLSocketFactory implements org.apache.catalina.net.ServerSocketFactory {

    static Logger _logger=LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private static final boolean clientAuth = false;

    private static LocalStringManagerImpl localStrings = 
	new LocalStringManagerImpl(SSLSocketFactory.class);

    private SSLContext context = null;
    private javax.net.ssl.SSLServerSocketFactory factory = null;
    private String cipherSuites[];
    
    public static final SecureRandom secureRandom = SecurityServicesUtil.secureRandom;
    
    //V3:Commented private static SecureRandom sr = J2EEServer.secureRandom;
    private static KeyManager[] keyManagers = null;
    private static TrustManager[] trustManagers = null;


    /**
     * Create the SSL socket factory. Initialize the key managers and
     * trust managers which are passed to the SSL context.
     */
    public SSLSocketFactory () {
	try {
	    if(keyManagers == null || trustManagers == null) {
		SSLUtils.initStoresAtStartup();
	    }
	    context = SSLContext.getInstance("TLS");
	    context.init(keyManagers, trustManagers, secureRandom);

	    factory = context.getServerSocketFactory();
	    cipherSuites = factory.getSupportedCipherSuites();
	    
            for(int i=0; i < cipherSuites.length; ++i) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,"Suite: " + cipherSuites[i]);
                }
	    }
            
	} catch(Exception e) {
	  _logger.log(Level.SEVERE,
                      "web_security.excep_sslsockfact", e.getMessage());
	}
    }

    /**
     * Create the socket at the specified port.
     * @param the port number.
     * @return the SSL server socket.
     */
    public ServerSocket createSocket (int port)
    throws IOException
    {
	SSLServerSocket socket = 
	    (SSLServerSocket) factory.createServerSocket(port);
	init(socket);
	return socket;
    }

    /**
     * Specify whether the server will require client authentication.
     * @param the SSL server socket.
     */
    private void init(SSLServerSocket socket) {
	// Some initialization goes here.....
	// socket.setEnabledCipherSuites(cipherSuites);
	socket.setNeedClientAuth(clientAuth);
    }

    /**
     * Create the socket at the specified port.
     * @param the port number.
     * @return the SSL server socket.
     */
    public ServerSocket createSocket (int port, int backlog)
    throws IOException
    {
	SSLServerSocket socket = (SSLServerSocket)
	    factory.createServerSocket(port, backlog);
	init(socket);
	return socket;
    }

    /**
     * Create the socket at the specified port.
     * @param the port number.
     * @return the SSL server socket.
     */
    public ServerSocket createSocket (int port, int backlog, InetAddress ifAddress)
    throws IOException
    {
	SSLServerSocket socket = (SSLServerSocket)
	    factory.createServerSocket(port, backlog, ifAddress);
	init(socket);
	return socket;
    }

    public static void setManagers(KeyManager[] kmgrs, TrustManager[] tmgrs) {
        keyManagers = kmgrs;
        trustManagers = tmgrs;
    }
}
