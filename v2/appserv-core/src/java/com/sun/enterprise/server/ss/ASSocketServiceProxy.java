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

package com.sun.enterprise.server.ss;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import com.sun.enterprise.server.ss.spi.ASSocketServiceFacade;

/**
 * A Proxy that delegates to the actual ASSocketService class. This facade
 * is introduced to separate the implementation and interfaces of the 
 * Quickstartup/ASLazyKernel related classes
 * In the new classloader scheme, the actual implementation
 * of the ASLazyKernel would lie in appserv-rt.jar, and the SystemClasspath 
 * would have the interfaces classes alone. [NIO specific AS-implementation interfaces
 * would need to be in the system classpath as J2SE' NIO implmentation requires
 * them to be there]
 * Classes under the following packages are now placed under appserv-launch.jar
 * com.sun.enterprise.server.ss.provider 
 * com.sun.enterprise.server.ss.util
 * com.sun.enterprise.server.ss.spi
 *
 * @author Sivakumar Thyagarajan, Binod PG
 */
public class ASSocketServiceProxy implements ASSocketServiceFacade{

    public void clientSocketConnected(int port, int localPort) {
        ASSocketService.clientSocketConnected(port, localPort);        
    }

    public boolean isServerStartingUp(int port) {
        return ASSocketService.isServerStartingUp(port);
    }

    public boolean close(int port, ServerSocket sock, ServerSocketChannel channel) throws IOException {
        return ASSocketService.close(port, sock, channel);
    }

    public void waitOnAccept(Socket s) {
        ASSocketService.waitOnAccept(s);
    }

    public boolean isLocalClient(InetAddress ia) {
        return ASSocketService.isLocalClient(ia);
    }

    public boolean isLocalClient(Socket s) {
        return ASSocketService.isLocalClient(s);
    }

    public boolean exists(int port) {
        return ASSocketService.exists(port);
    }

    public void removeListeningSelector(int port) {
        ASSocketService.removeListeningSelector(port);
    }

    public void waitOnAccept(SocketChannel sc) {
        ASSocketService.waitOnAccept(sc);
    }

    public ServerSocketChannel getServerSocketChannel(int port) {
        return ASSocketService.getServerSocketChannel(port);
    }

    public ServerSocket getServerSocket(int port) {
        return ASSocketService.getServerSocket(port);
    }

    public boolean socketServiceNotified(int port) {
        return ASSocketService.socketServiceNotified(port);
    }

    public void waitOnClientConnection(int port) {
        ASSocketService.waitOnClientConnection(port);
    }
    
}
