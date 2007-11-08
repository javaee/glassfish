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
 * RMIMultiHomedServerSocketFactory.java
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

package com.sun.enterprise.admin.server.core.jmx.nonssl;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.JmxConnector;

import java.rmi.server.RMIServerSocketFactory;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.InetAddress;

/** This is the custom RMI server socket factory that helps bind RMI servers to a specific
 * IP interface as has been specified in the domain->config->admin-service->jmx-connector->address
 * in domain.xml. In the absence of a specific entry, the default RMI behavior of binding to all
 * IP interfaces on the host is observed.   
 * @author  Nandini.Ektare@sun.com
 * @since Sun Java System Application Server 8.1 UR 2
 */
public class RMIMultiHomedServerSocketFactory implements RMIServerSocketFactory {

    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    
    private final String address;

    public RMIMultiHomedServerSocketFactory(String host) { 
	address = host;
        /*ServerContext serverCtx = ApplicationServer.getServerContext();
	if (serverCtx != null) {
	    try {
                ConfigContext configCtx = serverCtx.getConfigContext();
                Config config = ServerBeansFactory.getConfigBean(configCtx);	
                AdminService as = config.getAdminService();
                JmxConnector[] jc = as.getJmxConnector();
                address = jc[0].getAddress(); 
            } catch (ConfigException ex) {
                return null;
            }
	}*/
    }
    
    /** Implementation of the only method in {@link RMIServerSocketFactory}. This
     * method is called for creating the server socket.
     * @return instance of ServerSocket
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        try {
            InetAddress bindAddress = null;
            ServerSocket ss = null;        
            if (address.equals(DEFAULT_ADDRESS))             
                ss = new ServerSocket(port);        
            else {            
                bindAddress = InetAddress.getByName(address);             
                ss = new ServerSocket(port, 0, bindAddress);        
            }
            debug(ss);
            return (ss);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
        public int
    hashCode() {
        return super.hashCode() ^ address.hashCode();
    }
    
    /**
     * Overriding the base class method here to ensure that when a functionally
     * equivalent instance of server socket factory is passed, the same factory 
     * is reused instead of creating new objects
     */
    public boolean equals(Object anotherFactory) {
        
        if (anotherFactory != null && 
       	    anotherFactory.getClass().equals(this.getClass())) {
    
            RMIMultiHomedServerSocketFactory rmhssf = 
                (RMIMultiHomedServerSocketFactory) anotherFactory;
    
            if (this.address == null && rmhssf.address == null) return true;
            if (this.address == null ^ rmhssf.address == null) return false;
	    return this.address.equals(rmhssf.address);
        }
        return false;
    }
    
    private void debug (ServerSocket sss) {
        // prints the debug information - suppress after beta
        String prefix = "RMI/TLS Server Debug Message: " ;
        boolean DEBUG = Boolean.getBoolean("Debug");
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
