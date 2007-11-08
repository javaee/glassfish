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
import java.io.InputStream;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import java.util.logging.*;
import com.sun.enterprise.server.ss.spi.ASSocketFacadeUtils;
import com.sun.enterprise.server.ss.spi.ASSocketServiceFacade;
import com.sun.logging.LogDomains;

/**
 * NIO based SocketImpl implementation used by java.net.Socket. The implementation
 * is used always in the non-blocking mode.
 */

final class ASClientSocketImpl extends java.net.SocketImpl {
    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    private SocketChannel sc;
    private Socket sock;
    private InputStream is;

    private boolean usePlainSockets = true;
    private ASPlainSocketImpl ps = null;

    public int available() throws IOException {
        if (usePlainSockets) {
            return getPlainSocket().available();
        }
        return 0;
    }

    /**
     * Apart from closing the internal socket ant it's channel,
     * we need to shutdown the output also. This is necessary because
     * 1) A normal socket's InputStream will get an EOF, when its remote
     *    socket closes.
     * 2) But in case of non blocking IO, if a selector is registered
     *    on the channel with OP_WRITE key, the output of the socket 
     *    doesnt get shutdown immediately even when the socket/channel 
     *    is closed. This could lead to its corrsponding InputStream
     *    longer to get closed.
     * We explicitely shutdown to fix this problem.
     * 
     * Note that, if a program, expects its output stream to be open
     * for a few microseconds after the socket is closed and try to do
     * some smart stuff, may not work correctly. But that is a very 
     * very very rare case.
     */
    public void close() throws IOException {
        if (usePlainSockets) {
            getPlainSocket().close();
            return;
        }
        if (sock != null) {
            try {
                sock.shutdownOutput();
            } catch (Exception e) {
                if ( logger.isLoggable(Level.FINER) ) {
                    logger.log(Level.FINER, "" + e.getMessage(), e);
                }
            }
            try {
	        sock.close();
                sc.close();
            } catch (IOException ie) {
                if ( logger.isLoggable(Level.FINER) ) {
                    logger.log(Level.FINER, "" + ie.getMessage(), ie);
                }
                //throw ie;
            } catch (Error er) {
                if ( logger.isLoggable(Level.FINE) ) {
                    logger.log(Level.FINE, "" + er.getMessage(), er);
                }
                // This ideally should not happen...
            }
            is = null;
        }
    }

    public InetAddress getInetAddress() {
        if (usePlainSockets) {
            return getPlainSocket().getInetAddress();
        }
        try {
            return getClientSocket().getInetAddress();
        } catch (IOException ie) {
            if ( logger.isLoggable(Level.FINE) ) {
                 logger.log(Level.FINE, "" + ie.getMessage(), ie);
            }
            // Typically this will never be executed.
            return super.getInetAddress();
        }
    }


    public int getLocalPort() {
        if (usePlainSockets) {
            return getPlainSocket().getLocalPort();
        }
        try {
            return getClientSocket().getLocalPort();
        } catch (IOException ie) {
            if ( logger.isLoggable(Level.FINE) ) {
                 logger.log(Level.FINE, "" + ie.getMessage(), ie);
            }
            // Typically this will never be executed.
            return super.getLocalPort();
        }
    }

    public int getPort() {

        if (usePlainSockets) {
            return getPlainSocket().getPort();
        }

        try {
            return getClientSocket().getPort();
        } catch (IOException ie) {
            if ( logger.isLoggable(Level.FINE) ) {
                 logger.log(Level.FINE, "" + ie.getMessage(), ie);
            }
            // Typically this will never be executed.
            return super.getPort();
        }
    }

    public void shutdownInput() throws IOException {
        if (usePlainSockets) {
            getPlainSocket().shutdownInput();
            return;
        }
        getClientSocket().shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        if (usePlainSockets) {
            getPlainSocket().shutdownOutput();
            return;
        }
        getClientSocket().shutdownOutput();
    }

    public boolean supportsUrgentData() {
        if (usePlainSockets) {
            return getPlainSocket().supportsUrgentData();
        }
        return true;
    }

    public void sendUrgentData(int i) throws IOException {
        if (usePlainSockets) {
            getPlainSocket().sendUrgentData(i);
            return;
        }
        getClientSocket().sendUrgentData(i);
    }

    public void listen(int i) throws IOException {
	throw new UnsupportedOperationException(
		    "listen() not supported in ASClientSocketImpl");
    }

    public void create(boolean stream) throws IOException {
        if (usePlainSockets) {
            getPlainSocket().create(stream);
        }
    }
    
    public java.io.InputStream getInputStream() throws IOException {
        if (usePlainSockets) {
            return getPlainSocket().getInputStream();
        }
        if (this.is == null) {
            this.is = new ASInputStream(getClientSocketChannel(), getClientSocket()); 
        }
        return this.is;
    }

    public java.io.OutputStream getOutputStream() throws IOException {
        if (usePlainSockets) {
            return getPlainSocket().getOutputStream();
        }
        return new ASOutputStream(getClientSocketChannel(), getClientSocket());
    }

    public void bind(java.net.InetAddress ia, int port) throws IOException {
        if (usePlainSockets) {
            getPlainSocket().bind(ia, port);
            return;
        }
        InetSocketAddress isa = new InetSocketAddress(ia, port);
        getClientSocket().bind(isa);
    }

    public void connect(java.lang.String host, int port) throws IOException {
        if (usePlainSockets) {
            getPlainSocket().connect(host, port);
            return;
        }
        InetSocketAddress isa = new InetSocketAddress(host, port);
        if ( logger.isLoggable(Level.FINE) ) {
            logger.fine("In ASClientSocketImpl.connect, host = "+host
	 					     +" port = "+port);
        }
        connect(isa);
    }

    public void connect(java.net.InetAddress ia, int port) throws IOException {
        if (usePlainSockets) {
            getPlainSocket().connect(ia, port);
            return;
        }
        InetSocketAddress isa = new InetSocketAddress(ia, port);
        if ( logger.isLoggable(Level.FINE) ) {
	    logger.fine("In ASClientSocketImpl.connect, host = "+ia.getHostName()
							       +" port = "+port);
        }
        connect(isa);
    }

    private void connect(InetSocketAddress isa) throws IOException {
        connect(isa,0);
    }

    public void connect(java.net.SocketAddress sa, int timeout) throws IOException {
        if (usePlainSockets) {
            getPlainSocket().connect(sa, timeout);
            return;
        }
        if (sa == null || !(sa instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("unsupported address type");
	}
	
        InetSocketAddress addr = (InetSocketAddress) sa;
        if (addr.isUnresolved()) {
            throw new UnknownHostException(addr.getHostName());
	}
        this.port = addr.getPort();
        this.address = addr.getAddress();

        // Check whether the socketservice is notified. If it is not, then
        // this is the first connection to the service. This will trigger
        // the startup. So, this connection should be serviced only after
        // the completion of service startup.
        boolean waitForStartupReqd = ! ASSocketFacadeUtils.getASSocketService().
                                      socketServiceNotified(this.port);

        SocketChannel ch = getClientSocketChannel();
        boolean connected = ch.connect(sa);
	if ( !connected ) {
	    waitForSelect(timeout);
	    if (ch.finishConnect() == false) {
		throw new IOException("Connection timed out");
	    }
	}

        if (ASSocketFacadeUtils.getASSocketService().
        isLocalClient(address) == false) {
            return;
        }

	// Inform the ASSocketService about this socket, so that it can
	// correctly block remote sockets while allowing local loopback sockets.
	// Note: this needs to be called when NIO sockets are created too,
	// for now we dont do it because the Windows NIO impl does not allow 
	// SocketChannel to be wrapped (it casts the SocketChannel to an
	// internal class).
        Socket connectedSocket = sc.socket();
        if (waitForStartupReqd) {
            ASSocketFacadeUtils.getASSocketService().
            waitOnClientConnection(connectedSocket.getPort());
        }
	ASSocketFacadeUtils.getASSocketService().clientSocketConnected(
             connectedSocket.getPort(), connectedSocket.getLocalPort());
    }

    public void accept(java.net.SocketImpl si) throws IOException {
	throw new UnsupportedOperationException(
		    "accept() not supported in ASClientSocketImpl");
    }

    public void setOption(int opt,java.lang.Object val) throws SocketException {
        if (usePlainSockets) {
            getPlainSocket().setOption(opt, val);
            return;
        }
	try {
	    switch (opt) {
	    case SO_LINGER:
		if (val == null || (!(val instanceof Integer) && !(val instanceof Boolean)))
		    throw new SocketException("Bad parameter for option");
		if (val instanceof Boolean) {
		    if ( ((Boolean)val).booleanValue() == true )
			throw new SocketException("Bad parameter for option");
		    getClientSocket().setSoLinger( false, 0); 
		} else {
		    getClientSocket().setSoLinger( true, ((Integer) val).intValue());
		}
		break;
	    case SO_TIMEOUT:
		if (val == null || (!(val instanceof Integer)))
		    throw new SocketException("Bad parameter for SO_TIMEOUT");
		int tmp = ((Integer) val).intValue();
		if (tmp < 0)
		    throw new IllegalArgumentException("timeout < 0");
		//timeout = tmp;
		getClientSocket().setSoTimeout( tmp );
		break;
	    case IP_TOS:
		 if (val == null || !(val instanceof Integer)) {
		     throw new SocketException("bad argument for IP_TOS");
		 }
		 int trafficClass = ((Integer)val).intValue();
		 getClientSocket().setTrafficClass( trafficClass );
		 break;
	    case SO_BINDADDR:
		throw new SocketException("Cannot re-bind socket");
	    case TCP_NODELAY:
		if (val == null || !(val instanceof Boolean))
		    throw new SocketException("bad parameter for TCP_NODELAY");
		
		getClientSocket().setTcpNoDelay( ((Boolean)val).booleanValue() );
		break;
	    case SO_SNDBUF:
		if (val == null || !(val instanceof Integer) ||
		    !(((Integer)val).intValue() > 0)) {
		    throw new SocketException("bad parameter for SO_SNDBUF " );
		}
		getClientSocket().setSendBufferSize( ((Integer) val).intValue() );
		break;
	    case SO_RCVBUF:
		if (val == null || !(val instanceof Integer) ||
		    !(((Integer)val).intValue() > 0)) {
		    throw new SocketException("bad parameter for SO_SNDBUF " +
					      "or SO_RCVBUF");
		}
		getClientSocket().setReceiveBufferSize( ((Integer) val).intValue() );
		break;
	    case SO_KEEPALIVE:
		if (val == null || !(val instanceof Boolean))
		    throw new SocketException("bad parameter for SO_KEEPALIVE");
		getClientSocket().setKeepAlive( ((Boolean)val).booleanValue() );
		break;
	    case SO_OOBINLINE:
		if (val == null || !(val instanceof Boolean))
		    throw new SocketException("bad parameter for SO_OOBINLINE");
		getClientSocket().setOOBInline( ((Boolean)val).booleanValue() );
		break;
	    case SO_REUSEADDR:
		if (val == null || !(val instanceof Boolean)) 
		    throw new SocketException("bad parameter for SO_REUSEADDR");
		getClientSocket().setReuseAddress( ((Boolean)val).booleanValue() );
		break;
	    default:
		throw new SocketException("unrecognized TCP option: " + opt);
	    }

	} catch ( IOException ioex ) {
	    if ( ioex instanceof SocketException )
		throw ((SocketException)ioex);
	    else
		throw (SocketException)(new SocketException()).initCause(ioex);
	}
    }

    public Object getOption(int opt) throws SocketException {
        if (usePlainSockets) {
            return getPlainSocket().getOption(opt);
        }
        try {
            switch (opt) {
                case SO_LINGER:
                    return new Integer(getClientSocket().getSoLinger());
                case SO_TIMEOUT:
                    return new Integer(getClientSocket().getSoTimeout());
                case IP_TOS:
                    return new Integer(getClientSocket().getTrafficClass());
                case SO_BINDADDR:
	            return getClientSocket().getInetAddress();
                case TCP_NODELAY:
                    return new Boolean(getClientSocket().getTcpNoDelay());
                case SO_SNDBUF:
                    return new Integer(getClientSocket().getSendBufferSize());
                case SO_RCVBUF:
                    return 
                    new Integer(getClientSocket().getReceiveBufferSize());
                case SO_KEEPALIVE:
                    return new Boolean(getClientSocket().getKeepAlive());
                case SO_OOBINLINE:
                    return new Boolean(getClientSocket().getOOBInline());
                case SO_REUSEADDR:
                    return new Boolean(getClientSocket().getReuseAddress());
                default:
                    throw 
                    new SocketException("unrecognized TCP option: " + opt);
            }
        } catch (SocketException se) {
            throw se;
        } catch (IOException ie) {
            throw (SocketException)
            (new SocketException(ie.getMessage())).initCause(ie);
        }
    }

    private Socket getClientSocket() throws IOException {
	if ( sock == null ) {
            createSocket();
	}
	return sock;
    }

    private SocketChannel getClientSocketChannel() throws IOException {
	if ( sc == null ) {
            createSocket();
	}
	return sc;
    }

    private void createSocket() throws IOException {
	sc = SocketChannel.open();
        sc.configureBlocking(false);
	sock = sc.socket();
    }


    private void waitForSelect(long timeout) throws IOException {
        Selector selector = Selector.open();
        this.sc.register(selector, SelectionKey.OP_CONNECT); 
        
        selectorblock:
            while (true) {
                try {
                    int n = selector.select(timeout);
                    if (n==0 && this.sc.finishConnect()) {
                        break;
                    }
                    Iterator it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey selKey = (SelectionKey)it.next();
                        if (selKey.isValid() && selKey.isConnectable()) {
                            it.remove();
                            break selectorblock;
                        }
                    }
                } catch (Exception e) {
                    throw (IOException) (new IOException()).initCause(e);
                }
            }
        try {
            selector.close();
        } catch (IOException ie) {
            if ( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, ie.getMessage(), ie);
            }
        }
    }

    // Called from ASServerSocketImpl
    void setClientSocket(Socket s) throws IOException {
        usePlainSockets = false;
	sock = s;
	sc = sock.getChannel();
        sc.configureBlocking(false);
        localport = sock.getLocalPort();
        port = sock.getPort();
        address = sock.getInetAddress();
    }

    /**
     * The plain socket implementation will be used for
     * any outbound communication will use this socketimpl.
     */
    ASPlainSocketImpl getPlainSocket() {
        if (ps == null) {
            ps = new ASPlainSocketImpl();
        }
        return ps;
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } catch (Throwable t) {}
    }
    
}

