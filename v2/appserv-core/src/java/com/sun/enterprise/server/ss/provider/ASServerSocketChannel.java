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
package com.sun.enterprise.server.ss.provider;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.server.ss.ASSocketService;
import com.sun.enterprise.server.ss.spi.ASSocketFacadeUtils;
import com.sun.logging.LogDomains;

class ASServerSocketChannel extends ServerSocketChannel implements ASChannel {
    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    private ServerSocketChannel ssc = null;
    private ServerSocket ssocket = null;
    private int port = 0;

    ASServerSocketChannel(ServerSocketChannel ssc, SelectorProvider p) {
        super(p);
        this.ssc = ssc;
        // initialize the server port. It may be -1.
        port = ssc.socket().getLocalPort();
    }

    public ServerSocket socket() { 
        try {
            if (ssocket == null) {
                ServerSocket ss = ssc.socket();
                ssocket = new ASServerSocket(ss, this);
            }
        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
        return ssocket;
    }

    public SocketChannel accept() throws IOException {

	SocketChannel sc = ssc.accept();
        if ( logger.isLoggable(Level.FINE) ) {
             Socket s = sc.socket();
             logger.fine("In ASServerSocketChannel.accept got connection, s.port=" +
             s.getPort()+" s.localPort="+s.getLocalPort());
        }

        ASSocketFacadeUtils.getASSocketService().waitOnAccept(sc);

        return sc;
    }

    public void implConfigureBlocking(boolean b) throws IOException {
        ssc.configureBlocking(b);
    }

    /**
     * This is the place where a serversocket in socket service finally
     * gets closed.
     *
     * Some services [eg. MQ] closes and recreates the serversocket 
     * while starting itself. However this can break, socket service
     * logic, since the waiting connections can get a "connection reset"
     * exception.
     */
    public void implCloseSelectableChannel() throws IOException {
        ASSocketFacadeUtils.getASSocketService().close(port, null, ssc);
    }


    public SelectableChannel getActualChannel() {
        return ssc;

    }

    void setPortNumber(int port) {
        this.port = port;
    }

    int getPortNumber() {
        return this.port;
    }

    void setServerSocketChannel(ServerSocketChannel ssc) {
        this.ssc = ssc;
    }

    // The ORB needs this for logging.
    public String toString()
    {
	return "ASServerSocketChannel[" + ssc.toString() + "]";
    }
}

// End of file.


