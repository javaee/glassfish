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
 * AdminRmiSslClientSocketFactory.java
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

import java.io.Serializable;
import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.sun.enterprise.config.serverbeans.Ssl;

/** This is the custom RMI client socket factory that uses the same keystore, truststore,
 * certificate databases all the time. This factory will be used to create
 * the client side sockets when rmi connector server is configured to use SSL. Please
 * read the package.html.
 * Since this class is not standard, it is not used.
 * @author  Kedar.Mhaswade@sun.com
 * @since Application Server 8.1
 */
public class AdminSslClientSocketFactory implements RMIClientSocketFactory, Serializable {
    private final Ssl sslc;
    public AdminSslClientSocketFactory(final Ssl sslc) {
        if (sslc == null)
            throw new IllegalArgumentException("Internal: null ssl configuration");
        this.sslc = sslc;
    }    
    /** Implementation of the only method in {@link RMIClientSocketFactory}. This
     * method is called for creating the server socket.
     * @return instance of ServerSocket
     */
    
    public Socket createSocket(final String host, final int port) throws IOException {
        try {
            final SSLSocketFactory f = (SSLSocketFactory)SSLSocketFactory.getDefault();
            final SSLSocket s = (SSLSocket) f.createSocket(host, port);
            return ( s );
        }
        catch (final Exception e) {
            throw new IOException (e.getMessage());
        }
    }
    
}
