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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.server.core.channel;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;

/**
 * Local RMI Client Socket Factory. This class creates client sockets
 * that connect to local loopback address. This class needs to be Serializable
 * because RMI stubs contain a reference to client socket factory.
 */
public class LocalRMIClientSocketFactory implements RMIClientSocketFactory,
        Serializable {

    private InetAddress localLoopbackAddress;

    /**
     * Create a new local RMI Client Socket Factory. This method uses <code>null
     * </code> argument to <code>InetAddress.getByName()</code> to determine
     * local loopback address.
     * @throws UnknownHostException as thrown from the method <code>
     * InetAddress.getByName()</code>.
     */
    public LocalRMIClientSocketFactory() throws UnknownHostException {
        localLoopbackAddress = InetAddress.getByName(null);
    }

    /**
     * Create a new local RMI Client Socket Factory on specified local IP
     * address. Use this constructor when you know the local loopback IP
     * address and do not want to handle UnknownHostException thrown by default
     * constructor.
     * @param localAddr local loopback address.
     */
    public LocalRMIClientSocketFactory(InetAddress localAddr) {
        localLoopbackAddress = localAddr;
    }

    /**
     * Create a new socket that connects to local loopback address and
     * specified port. This method ignores host parameter. The method comes
     * from the interface <code>java.rmi.server.RMIClientSocketFactory</code>
     * @param host host to connect to, this is ignored and local loopback
     *      address is used.
     * @param port port to connect to
     * @throws IOException if an IO error occurs while creating the socket
     * @return a Socket connected to local loopback address on specified port
     */
    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(localLoopbackAddress, port);
    }

}
