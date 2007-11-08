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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.server.PEMain;
import com.sun.enterprise.server.ondemand.entry.EntryPoint;
import com.sun.enterprise.server.ondemand.entry.ServerEntryHelper;
import com.sun.enterprise.server.ss.provider.ASSelectorProvider;
import com.sun.enterprise.server.ss.provider.ASServerSocket;
import com.sun.enterprise.server.ss.provider.ASSelector;
import com.sun.enterprise.server.ss.provider.PortConflictException;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.Switch;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

/**
 * One each ASSocketService will be used for each port opened in domain.xml. 
 * It binds a port so that the client will not get exception when it tries to 
 * access the port.
 */
public final class ASSocketService implements EntryPoint {

    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private static final StringManager localStrings =
        StringManager.getManager( ASSocketService.class);

    private static final HashMap<Integer, ASSocketService> managedPortsTable = 
            new HashMap<Integer, ASSocketService>();
    private static PEMain peMain = null;
    private static InetAddress localHost = null;

    private static final String SO_REUSEADDR_PROPERTY =
            "com.sun.enterprise.server.ss.setReuseAddress";
    private static final boolean setReuseAddress =
             new Boolean(System.getProperty(SO_REUSEADDR_PROPERTY));

    private ServerSocket ss;
    private ServerSocketChannel sschan;
    private Selector sel;

    private static final int TIMEOUT = 30 * 1000; // 30 seconds.
    private static boolean triggered = false;

    static final int NOTSTARTED = 0;
    static final int SERVICENOTIFIED = 1;
    static final int STARTING = 2;
    static final int STARTED = 3;
    private int state = NOTSTARTED;

    private static final Object lock = new Object();
    private final Object acceptLock = new Object();
    private ASSocketServiceConfig config = null;

    ASSocketService(ASSocketServiceConfig config) {
        this.config = config;
	if ( localHost == null ) {
	    try {
		localHost = InetAddress.getLocalHost();
	    } catch ( Exception ex ) {}
	}
    }

    void start() throws PortConflictException {
        try {
            config.init();
            _initializeService();
            if (config.getStartSelector() == true) {
                Selector select = createListeningSelector();
                (new EntryPointThread(select)).start();
            } else {
                // Socket service doesnt need to handle the
                // lifecycle.
                this.state = STARTED;
            }

            if ( logger.isLoggable(Level.FINE) ) {
                logger.fine("ASSocketService Successfully started for " + config);
            }
        } catch(IOException ie){
            String i18nMsg = localStrings.getString(
                "socketservice.port_conflict", new Object[]{String.valueOf(config.getPort())});
            throw new PortConflictException(config.getPort(),i18nMsg, ie);
        }
    }

    void _reInitializeService() throws IOException {
        ASSelectorProvider provider = 
        (ASSelectorProvider) SelectorProvider.provider();
        provider.clear(config.getPort());
        synchronized (managedPortsTable) {
	    // put the port in stable so that exists(port) returns true
            managedPortsTable.remove(config.getPort());
        }

        try {
            if (this.sel != null) {
                this.sel.close();
            }
        } catch (Exception e) {
            logger.log(Level.FINEST, e.getMessage(), e);
        }

        int oldPort = config.getPort();
        config.init();
        /** In retrospect, the reInitialization is not that necessary.
            Commenting this out for now. Keeping this code within
            the comment so that it can be turned on later.

            Reinitialization will be required, when we automatically 
            stop containers that are idle.
        if (config.getPort() == oldPort) {
            _initializeService();
        }
        **/
    }

    void _initializeService() throws IOException {
         synchronized (managedPortsTable) {
	     // put the port in stable so that exists(port) returns true
             managedPortsTable.put(config.getPort(), this);
         }
	 // open channel and bind its ServerSocket
         sschan = ServerSocketChannel.open();
         ss = sschan.socket();
         if (setReuseAddress) {
             ss.setReuseAddress(true);
         }
         if (config.getBacklog() > 0) {
             ss.bind(config.getSocketAddress(), config.getBacklog());
         } else {
             ss.bind(config.getSocketAddress());
         }
    }

    static boolean close(int port, ServerSocket sock, ServerSocketChannel channel) 
    throws IOException {

        ASSocketService savedSS = null;
        if (port > 0) {
            savedSS = ASSocketService.get(port);
            if (savedSS != null) { //If managed by socket service
                if (savedSS.entryBeingProcessed()) {
                    // Return and dont close
                    ASSelectorProvider pr = (ASSelectorProvider)
                    SelectorProvider.provider();
                    pr.setPortState(port, ASSelectorProvider.PORT_BOUND);
                    return false;
                }
            } 
        }

        boolean closed = false;
        try {
            if (channel != null) {
                if (channel.isOpen()) {
                    closed = true;
                    channel.close();
                }
            }
            if (sock != null) {
                if (sock.isClosed() == false) {
                    closed = true;
                    sock.close();
                }
            }
        } finally {
            if (savedSS != null && closed) {
                ASSocketService.reInitializeService(port);
            }
        }

        return true;
        
    }

    static void reInitializeService(int port) throws IOException {
        ASSocketService savedSS = ASSocketService.get(port);
        if (savedSS != null) {
            savedSS._reInitializeService();
        }
    }

    Selector createListeningSelector() {
        try {
            sschan.configureBlocking(false);
            this.sel = Selector.open();
            sschan.register(sel, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return this.sel;
    }

    static void removeListeningSelector(int port) {
        try {
            ASSocketService savedSS = get(port);
            if (savedSS.sel != null) {
                savedSS.sel.close();
                savedSS.sschan.configureBlocking(true);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    static ServerSocket getServerSocket(int port) {
        ASSocketService a = managedPortsTable.get(port);
        return a.ss;
    }


    static ServerSocketChannel getServerSocketChannel(int port) {
        ASSocketService a = managedPortsTable.get(port);
        return a.sschan;
    }

    static boolean exists(int port) {
        return managedPortsTable.containsKey(port);
    }

    static ASSocketService get(int port) {
        return managedPortsTable.get(port);
    }

    static void initialize() {
	if ( peMain == null ) {
	    peMain = com.sun.enterprise.server.PEMain.getInstance();
	}
    }

    boolean entryBeingProcessed() {
        return state != STARTED;
    }

    public static boolean socketServiceNotified(int port) {

        ASSocketService ss = ASSocketService.get(port);
        if (ss != null) {
            InvocationManager im = Switch.getSwitch().getInvocationManager();
            //System.out.println("Startup invocation :" + im.isStartupInvocation());
            ss._socketServiceNotified(port);
            return im.isStartupInvocation();
        } else {
            return true;
        }
    }

    void _socketServiceNotified(int port) {
        if (state == NOTSTARTED) {
            // No one has started it so far.
            if (!ServerEntryHelper.isNotifiedByPortEntryContext(port)) {
                state = SERVICENOTIFIED;
            }
        }
    }

    public void generateEntryContext(Object context) {
        if ( logger.isLoggable(Level.FINE) ) {
	     logger.fine("In ASSocketService.generateEntryContext for context " + context);
        }
        ServerEntryHelper.generatePortEntryContext((Integer) context);
    }

    static void waitOnAccept(SocketChannel sc) {
        // Redundant peMain.isStartingUp() is required since sc.socket() 
        // has to pass thru a synchronization.
	if ( peMain.isStartingUp() ) { 
            waitOnAccept(sc.socket());
        } else {
            waitForServiceStartUp(sc.socket());
        }

    }

    // If server is starting up & socket is connected to a client outside this JVM
    // & socket is bound to a "managed port", then wait till startup completes.
    // This is necessary to prevent incoming requests from being processed before
    // all modules in the appserver have been initialized.
    static void waitOnAccept(Socket s) {
	if ( peMain.isStartingUp() ) {
	    int localPort = s.getLocalPort();
	    if ( !exists(localPort) ) { // Check if port is managed
		return;
	    }

	    // If client is in this JVM, dont wait
	    synchronized ( peMain ) {
	        while ( peMain.isStartingUp() && ! hasClientSocketLocalPorts(s) ) {
                    if ( logger.isLoggable(Level.FINE) ) {
		        logger.fine("In ASSocketService.waitOnAccept for localport " + localPort);
                    }
		    try {
			peMain.wait();
		    } catch ( Exception ex ) {}
	        }
	    }
	} 

        waitForServiceStartUp(s);
    }

    static void waitForServiceStartUp(Socket s) {
        ASSocketService savedService = get(s.getLocalPort());
        if ( savedService != null ) {
            savedService._waitForServiceStartUp(s);
        }
    }

    // When a service is being started and 
    // if a client request come for that service, wait 
    // until the processing the entry has been processed
    // completely.
    void _waitForServiceStartUp(Socket s) {
	if ( entryBeingProcessed() ) {
	    int localPort = s.getLocalPort();

	    // If client is in this JVM, dont wait
	    synchronized ( acceptLock ) {
	        while ( entryBeingProcessed() && 
                        ! ASSocketService.hasClientSocketLocalPorts(s) ) {
                    if ( logger.isLoggable(Level.FINE) ) {
		        logger.fine("In ASSocketService.waitForServiceStartUp for localport " + localPort);
                    }
		    try {
			acceptLock.wait();
		    } catch ( Exception ex ) {}
	        }
	    }
	} 

    }

    static void clientSocketConnected(int port, int localPort) {
        boolean toAdd = true;
	if ( peMain.isStartingUp() ) {
            if ( logger.isLoggable(Level.FINE) ) { 
	        logger.fine("In ASSocketService.clientSocketConnected, adding port " 
			+ localPort);
            }

	    synchronized ( peMain ) {
                putClientSocketLocalPort(port, localPort);
                toAdd = false;
		peMain.notifyAll();
	    }
	}

        ASSocketService service = ASSocketService.get(port); 
        if (service != null && service.entryBeingProcessed()) {
	    synchronized ( service.acceptLock ) {
                if (toAdd) {
                    putClientSocketLocalPort(port, localPort);
                }
	        service.acceptLock.notifyAll();
	   }
        }

    }

    static void putClientSocketLocalPort(int serverPort, int localPort) {
        if (exists(serverPort)) {
            ASServerSocket ss = (ASServerSocket) getServerSocket(serverPort);
            ss.addClientSocketLocalPort(localPort);
        } 
    }

    static boolean hasClientSocketLocalPorts(Socket s) {
        ASServerSocket ss = (ASServerSocket) getServerSocket(s.getLocalPort());
        return ss.hasClientSocketLocalPorts();
    }

    static boolean isLocalClient(Socket s) {
        return isLocalClient(s.getInetAddress());
    }

    static boolean isLocalClient(InetAddress remoteAddr) {
	    // Check if s is connected to a remote client (on a different machine)
	    return   (  remoteAddr.equals(localHost)
          	     || remoteAddr.isSiteLocalAddress() 
		     || remoteAddr.isLinkLocalAddress()
		     || remoteAddr.isLoopbackAddress()
		     || remoteAddr.isAnyLocalAddress());
    }

    // Retrieves the key representing the channel's registration 
    // with the actual selector.
    public static SelectionKey keyFor(SelectableChannel channel, Selector sel) {
        return channel.keyFor(((ASSelector) sel).getSelector());
    }

    static boolean isServerStartingUp() {
        return peMain.isStartingUp();
    }

    static boolean isServerStartingUp(int port) {
        if (isServerStartingUp() == true) {
            return true;
        }
        if (port > 0 ) {
            ASSocketService savedSS = ASSocketService.get(port);
            if (savedSS != null) {
                return savedSS.entryBeingProcessed();
            }
        }
        return false;
    }

    public static void triggerServerExit(){
        synchronized (lock) {
            triggered = true;
            lock.notifyAll();
        }
    }

    public static void waitForClientNotification(){
        synchronized (lock) {
            if (triggered == false) {
                try {
                    lock.wait(TIMEOUT);
                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * If a client connects from within appserver and 
     * that triggers the service startup, that 
     * client should wait until service startup 
     * completes before being accepted by server.
     * 
     * This method should be called, if and only if the
     * connection is first ever connection to the server.
     *
     * @param s A connected socket from client.
     */
    public static void waitOnClientConnection(int port) {
        ASSocketService ss = ASSocketService.get(port);
        if (ss != null) {
            ss._waitOnClientConnection();
        }
    }

    void _waitOnClientConnection() {
        if (entryBeingProcessed()) {
            synchronized (acceptLock) {
                try {
                    while (entryBeingProcessed()) {
                        acceptLock.wait();
                    }
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    class EntryPointThread extends Thread {

        private Selector selector = null;

        EntryPointThread(Selector selector) {
            this.selector = selector;
        }

        public void run() {

            selectorblock: 
            while (true) {
                try {
                    selector.select();
                    Iterator it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey selKey = (SelectionKey)it.next();
                        if (selKey.isValid() && selKey.isAcceptable()) {
                            it.remove();
                            // Service notified state will be set only by clients within
                            // JVM. If the service is notified by any client in  the same
                            // JVM, then the selector is returned by a client from outside
                            // JVM. In this case, the service should start only after
                            // every startup operation completes.
                            if (state != SERVICENOTIFIED) {
                                if (peMain.isStartingUp()) {
                                    synchronized (peMain) {
                                        try {
                                            while (peMain.isStartingUp()) {
                                                peMain.wait();
                                            }
                                        } catch (Exception e) {}
                                    }
                                }
                            }
                            break selectorblock;
                        }
                    }

                } catch (Exception e) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, e.getMessage(), e);
                    }
                    break;
                    // The selector will be closed whenever actual server
                    // comes up. See removeListeningSelector() method.
                    // That can cause this exception. So, just return.
                    //return;
                }
            }


            try {
                state = STARTING;
                generateEntryContext(new Integer(config.getPort()));
                state = STARTED;
                synchronized (acceptLock) {
                    acceptLock.notifyAll();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}
