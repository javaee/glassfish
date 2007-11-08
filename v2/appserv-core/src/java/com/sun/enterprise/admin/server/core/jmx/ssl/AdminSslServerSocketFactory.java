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
 * AdminRmiSslServerSocketFactory.java
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. No tabs are used, all spaces.
 * 2. In vi/vim -
 *      :set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *      1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *      2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = True.
 *      3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 * Unit Testing Information:
 * 0. Is Standard Unit Test Written (y/n):
 * 1. Unit Test Location: (The instructions should be in the Unit Test Class itself).
 */

package com.sun.enterprise.admin.server.core.jmx.ssl;

import java.net.ServerSocket;
import java.net.InetAddress;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.rmi.server.RMIServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import com.sun.enterprise.config.serverbeans.Ssl;

/* for PE/SE/EE pluggable security infrastructure */
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.security.ssl.J2EEKeyManager;
import java.security.SecureRandom;


/* for PE/SE/EE plugaable security infrastructure */

/** This is the custom RMI server socket factory that uses the same keystore, truststore,
 * certificate databases all the time. This factory will be used to create
 * the server side sockets when rmi connector server is configured to use SSL. Please
 * read the package.html. Note that this code depends upon the pluggability of the proper
 * security infrastructure.
 * @author  Kedar.Mhaswade@sun.com
 * @since Sun Java System Application Server 8.1
 */
public class AdminSslServerSocketFactory implements RMIServerSocketFactory {
    private final Ssl sslc;

    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    private String address = DEFAULT_ADDRESS;
    
    public AdminSslServerSocketFactory(final Ssl sslc, String address) {
        if (sslc == null)
            throw new IllegalArgumentException("Internal: null ssl configuration");
        this.sslc = sslc;
        this.address = address;
    }
    
    /** Implementation of the only method in {@link RMIServerSocketFactory}. This
     * method is called for creating the server socket.
     * @return instance of ServerSocket
     */
    public ServerSocket createServerSocket(final int port) throws IOException {
        try {
            /* My belief is that one of the bootstrap classes for
            * initializing the SSL Context and the proper (pluggable)
            * Key and Trust Managers are in place. We just need to leverage that.
            */
            // first get the SSLContext - returned as a new one - http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html#AppA
            final SSLContext ctx = SSLContext.getInstance("TLSv1");
            // get the key and trust managers
            final KeyManager[] kms = SSLUtils.getKeyManagers();
            J2EEKeyManager[] jkms = new J2EEKeyManager[kms.length];
            for (int i = 0; i < kms.length; i++) {
                jkms[i] = new J2EEKeyManager((X509KeyManager)kms[i], sslc.getCertNickname());
            }
            final TrustManager[] tms = null; //not needed really untill we support client auth
            final SecureRandom sr = null; // need to handle better?
            // now initialize the SSLContext gotten above and return
            ctx.init(jkms, tms, sr);
            final SSLServerSocketFactory sf = ctx.getServerSocketFactory();
            
            InetAddress bindAddress = null;
            ServerSocket sss = null;
            if (address.equals(DEFAULT_ADDRESS))             
                sss = sf.createServerSocket(port);
            else {            
                bindAddress = InetAddress.getByName(address);             
                sss = sf.createServerSocket(port, 0, bindAddress);        
            }
            debug(sss);
            return ( sss );
        }
        catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    private void debug (final ServerSocket sss) {
        // prints the debug information - suppress after beta
        final String prefix = "RMI/TLS Server Debug Message: " ;
        final boolean DEBUG = Boolean.getBoolean("Debug");
        if (sss != null) {
            if (DEBUG) {
                System.out.println(prefix + "ServerSocket local port = " + sss.getLocalPort());
                System.out.println(prefix + "ServerSocket host address = " + sss.getInetAddress().getHostAddress());
                System.out.println(prefix + "ServerSocket bound status = " + sss.isBound());
            }
        }
        else {
            System.out.println(prefix + " Catastrophe: no server socket");
        }
    }
}
