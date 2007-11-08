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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.AsynchronousCloseException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.server.ss.spi.ASSocketFacadeUtils;
import com.sun.logging.LogDomains;

/**
 * NIO based SocketImpl implementation used by java.net.ServerSocket.
 */
public final class ASServerSocketImpl extends java.net.SocketImpl {
    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private java.net.InetAddress hostToBind;
    private int portToBind;

    private ServerSocketChannel ssc;
    private ServerSocket ss;
    private Selector selector;
    private Hashtable options = new Hashtable();
    
    public int available() throws IOException {
	throw new UnsupportedOperationException(
		    "available() not supported in ASServerSocketImpl");
    }

    public void connect(java.lang.String s,int i) throws IOException {
	throw new UnsupportedOperationException(
		    "connect() not supported in ASServerSocketImpl");
    }

    public void connect(java.net.InetAddress ia,int i) 
	    throws IOException {
	throw new UnsupportedOperationException(
		    "connect() not supported in ASServerSocketImpl");
    }

    public void connect(java.net.SocketAddress sa,int i) 
	    throws IOException {
	throw new UnsupportedOperationException(
		    "connect() not supported in ASServerSocketImpl");
    }

    public java.io.InputStream getInputStream() throws IOException {
	throw new UnsupportedOperationException(
		    "getInputStream() not supported in ASServerSocketImpl");
    }

    public java.io.OutputStream getOutputStream() throws IOException {
	throw new UnsupportedOperationException(
		    "getOutputStream() not supported in ASServerSocketImpl");
    }

    public void shutdownInput() throws IOException {
	throw new UnsupportedOperationException(
		    "shutdownInput() not supported in ASServerSocketImpl");
    }

    public void shutdownOutput() throws IOException {
	throw new UnsupportedOperationException(
		    "shutdownOutput() not supported in ASServerSocketImpl");
    }

    public boolean supportsUrgentData() {
	throw new UnsupportedOperationException(
		    "supportsUrgentData() not supported in ASServerSocketImpl");
    }

    public void sendUrgentData(int i) throws IOException {
	throw new UnsupportedOperationException(
		    "sendUrgentData() not supported in ASServerSocketImpl");
    }

    public void close() throws IOException {
        if (ss != null && !ss.isClosed()) {
            try { 
                ServerSocketChannel channelToClose = ssc;
                ServerSocket socketToClose = ss;
                if (ssc instanceof ASServerSocketChannel) {
                    ASServerSocketChannel assc = 
                    (ASServerSocketChannel) ssc;
                    channelToClose = (ServerSocketChannel) assc.getActualChannel();
                    socketToClose = ssc.socket();
                }
                ASSocketFacadeUtils.getASSocketService().close(portToBind, 
                                            socketToClose, channelToClose);
            } catch (IOException e) {
                if ( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, ""+ e.getMessage(),e);
                }
            }
        }
    }

    public void create(boolean stream) throws IOException {
	// No-op: stream is always true when called from ServerSocket
    }


    public void bind(java.net.InetAddress host, int port) throws IOException {
	hostToBind = host;
	portToBind = port;

	// actual binding happens in listen() below, because listen() is
	// called after bind() by java.net.ServerSocket.java.
    }

    public void listen(int backlog) throws IOException {

	// Check for services that are not lazily initialized
        if (!ASSocketFacadeUtils.getASSocketService().exists(portToBind)) { 
	    ssc = ServerSocketChannel.open();
	    ss = ssc.socket();
        } else {
            ssc = ASSocketFacadeUtils.getASSocketService().
                                             getServerSocketChannel(portToBind);
            ss = ASSocketFacadeUtils.getASSocketService().
                                             getServerSocket(portToBind);
	}

        InetSocketAddress isa = new InetSocketAddress(hostToBind, portToBind);
	ss.bind(isa, backlog);

        localport = ss.getLocalPort();
        address = ss.getInetAddress();
    }

    public void accept(java.net.SocketImpl si) throws IOException {
        try {
	    Socket sock = ss.accept();
	    ((ASClientSocketImpl)si).setClientSocket(sock);
        } catch (AsynchronousCloseException ase) {
            SocketException se = new SocketException(ase.getMessage());
            se.initCause(ase);
            throw se;
        }
    }

    public void setOption(int opt,java.lang.Object val ) throws SocketException{
        if ( logger.isLoggable(Level.FINE) ) {
             logger.log(java.util.logging.Level.FINE, "In ASServerSocketImpl.setOption, opt = "
                        +opt+" val = "+val, new Exception());
        }

	//Consider only those options that are settable in a ServerSocket
     	switch (opt) {

	case SO_TIMEOUT:
	    if (val == null || (!(val instanceof Integer)))
		throw new SocketException("Bad parameter for SO_TIMEOUT");
	    int tmp = ((Integer) val).intValue();
	    if (tmp < 0)
		throw new IllegalArgumentException("timeout < 0");
	    ss.setSoTimeout( tmp );
	    break;
	
	case SO_RCVBUF:
	    if (val == null || !(val instanceof Integer) ||
		!(((Integer)val).intValue() > 0)) {
		throw new SocketException("bad parameter for SO_SNDBUF " +
					  "or SO_RCVBUF");
	    }
	    ss.setReceiveBufferSize( ((Integer) val).intValue() );
	    break;
	
	case SO_REUSEADDR:
	    if (val == null || !(val instanceof Boolean)) 
	        throw new SocketException("bad parameter for SO_REUSEADDR");
            if (ss != null) 
	        ss.setReuseAddress( ((Boolean)val).booleanValue() );
	    break;
	default:
	    throw new SocketException("unrecognized TCP option: " + opt);
	}
    }

    public Object getOption(int opt) throws SocketException {
       switch (opt) {
        case SO_TIMEOUT:
            try {
                return new Integer(ss.getSoTimeout());
            } catch( IOException ioe ) {
                throw new SocketException(ioe.getMessage()) ;
            }
        case SO_RCVBUF:
            return new Integer(ss.getReceiveBufferSize());
        case SO_REUSEADDR:
            return new Boolean(ss.getReuseAddress());
        default:
            throw new SocketException("unrecognized TCP option: " + opt);
        }

    }
}

