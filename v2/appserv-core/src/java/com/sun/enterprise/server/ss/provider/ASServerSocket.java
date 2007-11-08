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
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.server.ss.spi.ASSocketFacadeUtils;
import com.sun.logging.LogDomains;

/**
 * Wrapper for the ServerSocket returned from ServerSocketChannel
 */
public final class ASServerSocket extends ServerSocket {

    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    private ServerSocket ss = null;
    private ASServerSocketChannel sschan = null;
    private boolean toBind = true;
    private LinkedList socketCache = null;
    private final LinkedList clientSocketLocalPorts = new LinkedList();

    ASServerSocket(ServerSocket ss, ASServerSocketChannel sschan) 
	    throws IOException {
        this.ss = ss;
        this.sschan = sschan;
    }

    public int getLocalPort() {
        return ss.getLocalPort();
    }

    public synchronized int getReceiveBufferSize()
       throws SocketException {
        return ss.getReceiveBufferSize();
    }

    public synchronized int getSoTimeout() throws IOException {
        return ss.getSoTimeout();
    }

    public void close() throws IOException {
        ASSocketFacadeUtils.getASSocketService().close(getLocalPort(), ss, 
        (ServerSocketChannel) sschan.getActualChannel());
    }

    public boolean getReuseAddress() throws SocketException {
        return ss.getReuseAddress();
    }

    public boolean isBound() {
        return ss.isBound();
    }

    public boolean isClosed() {
        return ss.isClosed();
    }

    public synchronized void setReceiveBufferSize(int i)
       throws SocketException {
        if ( logger.isLoggable(Level.FINE) ) {
	    logger.fine("In ASServerSocket.setReceiveBufferSize = "+i);
        }
        ss.setReceiveBufferSize(i);
    }

    public synchronized void setSoTimeout(int i)
       throws SocketException {
        if ( logger.isLoggable(Level.FINE) ) {
	    logger.fine("In ASServerSocket.setSoTimeout = "+i);
        }
        ss.setSoTimeout(i);
    }

    public void setReuseAddress(boolean b) 
       throws SocketException {
       if ( logger.isLoggable(Level.FINE) ) {
	    logger.fine("In ASServerSocket.setReuseAddress = "+b);
        }
        ss.setReuseAddress(b);
    }

    public java.lang.String toString() {
        return ss.toString();
    }

    public InetAddress getInetAddress() {
        return ss.getInetAddress();
    }

    /**
     * Logic of accept method:
     * 
     * When any appserver service do a serversocket.accept(), it will 
     * reach the following method. 
     *
     * 1. Try to get a socket from its cache, for a client in same VM.
     * 2. Get the first socket in the cache.
     * 3. If there is nothing in the socket cache, do an actual accept
     *    This is the most common case. The first ever accept will 
     *    do ss.accept()
     * 4. If clientSocketLocalPorts empty (i.e there is no request waiting
     *    in the same VM) then do ASSocketService.waitOnAccept. waitOnAccept
     *    will block until server startup or until connection request from
     *    within the same VM.
     * 5. There is a socket connection request waiting on the same VM. So
     *    we need to accept that request. Find for that socket.
     */
    public synchronized Socket accept() throws IOException {

        Socket s = getAlreadyAcceptedSocketInSameVM();


        if ( s != null) { // Comment 1.
           return s;
        } else {
           s = getFirstSocketFromCache(); // Comment 2.
        }


        if (s == null) {
            s = acceptSocket(); // Comment 3.
        }

        if ( logger.isLoggable(Level.FINE) ) {
	    logger.fine("In ASServerSocket.accept got connection, s.port="
                         +s.getPort()+" s.localPort="+s.getLocalPort());
        }

        if ( hasClientSocketLocalPorts() == false) { // Comment 4.
            ASSocketFacadeUtils.getASSocketService().waitOnAccept(s);
        }

        if (hasClientSocketLocalPorts()) { //Comment 5
            s = findSocketInSameVM(s);
        }

        return s;
    }

    private Socket acceptSocket() throws IOException {
        return  ss.accept();
    }

    public void addClientSocketLocalPort(int port) {
        synchronized (clientSocketLocalPorts) {
            clientSocketLocalPorts.addLast(new Integer(port));
        }
    }

    public boolean hasClientSocketLocalPorts() {
        return clientSocketLocalPorts.size() > 0;
    }

    /**
     * 1. If socket cache is null or empty return null.
     * 2. If there is more than one socket in the cache and 
     *    clientSocketLocalPorts is not empty then try to find the
     *    socket in the cache that has client request. Then return it.
     */
    private Socket getAlreadyAcceptedSocketInSameVM() {
        if (socketCache != null) {
            if (socketCache.size() > 0 && clientSocketLocalPorts.size() > 0) {
                Iterator it = socketCache.iterator();
                Socket result = null;
                while (it.hasNext()) {
                    result = (Socket) it.next();
                    if ( ASSocketFacadeUtils.getASSocketService().isLocalClient(result) ) {
                        Integer port = new Integer(result.getPort());
                        synchronized (clientSocketLocalPorts) {
                            if (clientSocketLocalPorts.remove(port)) {
                               it.remove();
                               return result; // Comment 2.
                            }
                        }
                    }
                }
            } 
        } 
        return null; // Comment 1.
    }

    /**
     *  1. return the first socket in the cache.
     */
    private Socket getFirstSocketFromCache() {
        if (socketCache != null && socketCache.size() > 0 ) {
            return (Socket) socketCache.removeFirst(); // Comment 3, 4
        } else {
            return null;
        }
    }

    private LinkedList getSocketCache() {
        if (socketCache == null) {
            socketCache = new LinkedList();
        } 
        return socketCache;
    }

    /**
     * If the clientSocketLocalPorts table contains the local port, 
     * of the socket, then return that. Otherwise loop until such 
     * socket is accepted.
     */
    private Socket findSocketInSameVM(Socket s) throws IOException {
        Socket result = s;
        while (true) {
            if ( ASSocketFacadeUtils.getASSocketService().isLocalClient(result) ) {
                Integer port = new Integer(result.getPort());
                synchronized (clientSocketLocalPorts) {
                    if (clientSocketLocalPorts.remove(port)) {
                        break;
                    }
                }
            } 

            LinkedList cache = getSocketCache();
            cache.addLast(result);
            result = acceptSocket();
        }
        return result;
    }

    public SocketAddress getLocalSocketAddress() {
        return ss.getLocalSocketAddress();
    }

    public void bind(SocketAddress s) throws IOException {
	// 0 for backlog results in default value being used
        this.bind(s, 0);
    }

    public void bind(SocketAddress s, int backlog) throws IOException {

	int port = ((InetSocketAddress)s).getPort();

        if ( logger.isLoggable(Level.FINER) ) {
	     logger.log(Level.FINER, "In ASServerSocket.bind for port " + port, 
		   new Exception());
        }

        sschan.setPortNumber(port);

        if (!ASSocketFacadeUtils.getASSocketService().exists(port)) { 
            ss.bind(s, backlog);
	}
	else { // this port is managed by the ASSocketService
	    ServerSocket savedss = getServerSocket(port); 
	    if (savedss != null) {
		// replace the ServerSocketChannel and ServerSocket with the
		// ones created for this port at startup time.
		ss = savedss; 
		sschan.setServerSocketChannel(ss.getChannel()); 
	    }

	    int state = getPortState(port);
	    if (state != ASSelectorProvider.PORT_BOUND) {
		// This may cause an exception if there is a port conflict
                try {
		    ss.bind(s, backlog);
                } catch (IOException ie) {
                    throw ie;
                } catch (Throwable t) {
                    if (t.getCause() instanceof SocketException) {
                        throw (SocketException) t.getCause();
                    }
                    throw new BindException(t.getMessage() + ":" + port);
                }
	    } else if (state == ASSelectorProvider.PORT_BOUND) {
                // The real listener is up. No need for the old listener.
                ASSocketFacadeUtils.getASSocketService().
                                                 removeListeningSelector(port);
            }

	    // set the ServerSocket and update the state for this port
	    setServerSocket(ss, port);
	}
        if ( logger.isLoggable(Level.FINE) ) {
            logger.fine("In ASServerSocket.bind, bound at port " + ss.getLocalPort());
        }
    }

    public ServerSocketChannel getChannel() {
         return sschan;
    }

    private ServerSocket getServerSocket(int port) {
        ASSelectorProvider provider = 
        (ASSelectorProvider) SelectorProvider.provider();
        return provider.getServerSocket(port);
    }

    private void setServerSocket(ServerSocket ss, int port) {
        ASSelectorProvider provider = 
        (ASSelectorProvider) SelectorProvider.provider();
        provider.setServerSocket(ss, port);
    }

    private int getPortState(int port) {
        ASSelectorProvider provider = 
		(ASSelectorProvider) SelectorProvider.provider();
        return provider.getPortState(port);
    }

}

