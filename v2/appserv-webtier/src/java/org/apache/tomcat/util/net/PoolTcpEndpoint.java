

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.tomcat.util.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.AccessControlException;

import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadPoolRunnable;

/* Similar with MPM module in Apache2.0. Handles all the details related with
   "tcp server" functionality - thread management, accept policy, etc.
   It should do nothing more - as soon as it get a socket ( and all socket options
   are set, etc), it just handle the stream to ConnectionHandler.processConnection. (costin)
*/



/**
 * Handle incoming TCP connections.
 *
 * This class implement a simple server model: one listener thread accepts on a socket and
 * creates a new worker thread for each incoming connection.
 *
 * More advanced Endpoints will reuse the threads, use queues, etc.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin@eng.sun.com
 * @author Gal Shachor [shachor@il.ibm.com]
 */
public class PoolTcpEndpoint { // implements Endpoint {

    private StringManager sm = 
        StringManager.getManager("org.apache.tomcat.util.net.res");

    private static final int BACKLOG = 100;
    private static final int TIMEOUT = 1000;

    private final Object threadSync = new Object();

    private boolean isPool = true;

    private int backlog = BACKLOG;
    private int serverTimeout = TIMEOUT;

    TcpConnectionHandler handler;

    private InetAddress inet;
    private int port;

    private ServerSocketFactory factory;
    private ServerSocket serverSocket;

    ThreadPoolRunnable listener;
    private volatile boolean running = false;
    private boolean initialized = false;
    private boolean reinitializing = false;
    static final int debug=0;

    ThreadPool tp;
    // XXX Do we need it for backward compat ?
    //protected Log _log=Log.getLog("tc/PoolTcpEndpoint", "PoolTcpEndpoint");

    static Log log=LogFactory.getLog(PoolTcpEndpoint.class );

    protected boolean tcpNoDelay=false;
    protected int linger=100;
    protected int socketTimeout=-1;
    
    public PoolTcpEndpoint() {
	//	super("tc_log");	// initialize default logger
	tp = new ThreadPool();
    }

    public PoolTcpEndpoint( ThreadPool tp ) {
        this.tp=tp;
    }

    // -------------------- Configuration --------------------

    public void setPoolOn(boolean isPool) {
        this.isPool = isPool;
    }

    public boolean isPoolOn() {
        return isPool;
    }

    public void setMaxThreads(int maxThreads) {
	if( maxThreads > 0)
	    tp.setMaxThreads(maxThreads);
    }

    public int getMaxThreads() {
        return tp.getMaxThreads();
    }

    public void setMaxSpareThreads(int maxThreads) {
	if(maxThreads > 0) 
	    tp.setMaxSpareThreads(maxThreads);
    }

    public int getMaxSpareThreads() {
        return tp.getMaxSpareThreads();
    }

    public void setMinSpareThreads(int minThreads) {
	if(minThreads > 0) 
	    tp.setMinSpareThreads(minThreads);
    }

    public int getMinSpareThreads() {
        return tp.getMinSpareThreads();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port ) {
        this.port=port;
    }

    public InetAddress getAddress() {
	    return inet;
    }

    public void setAddress(InetAddress inet) {
	    this.inet=inet;
    }

    public void setServerSocket(ServerSocket ss) {
	    serverSocket = ss;
    }

    public void setServerSocketFactory(  ServerSocketFactory factory ) {
	    this.factory=factory;
    }

   ServerSocketFactory getServerSocketFactory() {
 	    return factory;
   }

    public void setConnectionHandler( TcpConnectionHandler handler ) {
    	this.handler=handler;
    }

    public TcpConnectionHandler getConnectionHandler() {
	    return handler;
    }

    public boolean isRunning() {
	return running;
    }
    
    /**
     * Allows the server developer to specify the backlog that
     * should be used for server sockets. By default, this value
     * is 100.
     */
    public void setBacklog(int backlog) {
	if( backlog>0)
	    this.backlog = backlog;
    }

    public int getBacklog() {
        return backlog;
    }

    /**
     * Sets the timeout in ms of the server sockets created by this
     * server. This method allows the developer to make servers
     * more or less responsive to having their server sockets
     * shut down.
     *
     * <p>By default this value is 1000ms.
     */
    public void setServerTimeout(int timeout) {
	this.serverTimeout = timeout;
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }
    
    public void setTcpNoDelay( boolean b ) {
	tcpNoDelay=b;
    }

    public int getSoLinger() {
        return linger;
    }
    
    public void setSoLinger( int i ) {
	linger=i;
    }

    public int getSoTimeout() {
        return socketTimeout;
    }
    
    public void setSoTimeout( int i ) {
	socketTimeout=i;
    }
    
    public int getServerSoTimeout() {
        return serverTimeout;
    }  
    
    public void setServerSoTimeout( int i ) {
	serverTimeout=i;
    }

    // -------------------- Public methods --------------------

    public void initEndpoint() throws IOException, InstantiationException {
	try {
	    if(factory==null)
		factory=ServerSocketFactory.getDefault();
	    if(serverSocket==null) {
                try {
                    if (inet == null) {
                        serverSocket = factory.createSocket(port, backlog);
                    } else {
                        serverSocket = factory.createSocket(port, backlog, inet);
                    }
                } catch ( BindException be ) {
                    throw new BindException(be.getMessage() + ":" + port);
                }
	    }
            if( serverTimeout >= 0 )
		serverSocket.setSoTimeout( serverTimeout );
	} catch( IOException ex ) {
	    //	    log("couldn't start endpoint", ex, Logger.DEBUG);
            throw ex;
	} catch( InstantiationException ex1 ) {
	    //	    log("couldn't start endpoint", ex1, Logger.DEBUG);
            throw ex1;
	}
        initialized = true;
    }

    public void startEndpoint() throws IOException, InstantiationException {
        if (!initialized) {
            initEndpoint();
        }
	if(isPool) {
	    tp.start();
	}
	running = true;
        if(isPool) {
    	    listener = new TcpWorkerThread(this);
            tp.runIt(listener);
        } else {
	    log.error("XXX Error - need pool !");
	}
    }

    public void stopEndpoint() {
	if (running) {
	    tp.shutdown();
	    running = false;
            if (serverSocket != null) {
                closeServerSocket();
            }
	}
    }

    protected void closeServerSocket() {
        Socket s = null;
        try {
            // Need to create a connection to unlock the accept();
            if (inet == null) {
                s=new Socket("127.0.0.1", port );
            }else{
                s=new Socket(inet, port );
                    // setting soLinger to a small value will help shutdown the
                    // connection quicker
                s.setSoLinger(true, 0);
            }
        } catch(Exception e) {
            log.error("Caught exception trying to unlock accept on " + port
                    + " " + e.toString());
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        try {
            if( serverSocket!=null)
                serverSocket.close();
        } catch(Exception e) {
            log.error("Caught exception trying to close socket.", e);
        }
        serverSocket = null;
    }

    // -------------------- Private methods

    Socket acceptSocket() {
        if( !running || serverSocket==null ) return null;

        Socket accepted = null;

    	try {
            if(factory==null) {
                accepted = serverSocket.accept();
            } else {
                accepted = factory.acceptSocket(serverSocket);
            }
            if (null == accepted) {
                log.warn("Null socket returned by accept");
            } else {
                if (!running) {
                    accepted.close();  // rude, but unlikely!
                    accepted = null;
                } else if (factory != null) {
                    factory.initSocket( accepted );
                }
            }
        }
        catch(InterruptedIOException iioe) {
            // normal part -- should happen regularly so
            // that the endpoint can release if the server
            // is shutdown.
        }
        catch (AccessControlException ace) {
            // When using the Java SecurityManager this exception
            // can be thrown if you are restricting access to the
            // socket with SocketPermission's.
            // Log the unauthorized access and continue
            String msg = sm.getString("endpoint.warn.security",
                                      serverSocket,ace);
            log.warn(msg);
        }
        catch (IOException e) {

            String msg = null;

            if (running) {
                msg = sm.getString("endpoint.err.nonfatal",
                        serverSocket, e);
                log.error(msg, e);
            }

            if (accepted != null) {
                try {
                    accepted.close();
                } catch(Throwable ex) {
                    msg = sm.getString("endpoint.err.nonfatal",
                                       accepted, ex);
                    log.warn(msg, ex);
                }
                accepted = null;
            }

            if( ! running ) return null;
            reinitializing = true;
            // Restart endpoint when getting an IOException during accept
            synchronized (threadSync) {
                if (reinitializing) {
                    reinitializing = false;
                    // 1) Attempt to close server socket
                    closeServerSocket();
                    initialized = false;
                    // 2) Reinit endpoint (recreate server socket)
                    try {
                        msg = sm.getString("endpoint.warn.reinit");
                        log.warn(msg);
                        initEndpoint();
                    } catch (Throwable t) {
                        msg = sm.getString("endpoint.err.nonfatal",
                                           serverSocket, t);
                        log.error(msg, t);
                    }
                    // 3) If failed, attempt to restart endpoint
                    if (!initialized) {
                        msg = sm.getString("endpoint.warn.restart");
                        log.warn(msg);
                        try {
                            stopEndpoint();
                            initEndpoint();
                            startEndpoint();
                        } catch (Throwable t) {
                            msg = sm.getString("endpoint.err.fatal",
                                               serverSocket, t);
                            log.error(msg, t);
                        } finally {
                            // Current thread is now invalid: kill it
                            throw new ThreadDeath();
                        }
                    }
                }
            }

        }

        return accepted;
    }

    /** @deprecated
     */
    public void log(String msg)
    {
	log.info(msg);
    }

    /** @deprecated
     */
    public void log(String msg, Throwable t)
    {
	log.error( msg, t );
    }

    /** @deprecated
     */
    public void log(String msg, int level)
    {
	log.info( msg );
    }

    /** @deprecated
     */
    public void log(String msg, Throwable t, int level) {
    	log.error( msg, t );
    }

    void setSocketOptions(Socket socket)
        throws SocketException {
        if(linger >= 0 ) 
            socket.setSoLinger( true, linger);
        if( tcpNoDelay )
            socket.setTcpNoDelay(tcpNoDelay);
        if( socketTimeout > 0 )
            socket.setSoTimeout( socketTimeout );
    }

}

// -------------------- Threads --------------------

/*
 * I switched the threading model here.
 *
 * We used to have a "listener" thread and a "connection"
 * thread, this results in code simplicity but also a needless
 * thread switch.
 *
 * Instead I am now using a pool of threads, all the threads are
 * simmetric in their execution and no thread switch is needed.
 */
class TcpWorkerThread implements ThreadPoolRunnable {
    /* This is not a normal Runnable - it gets attached to an existing
       thread, runs and when run() ends - the thread keeps running.

       It's better to keep the name ThreadPoolRunnable - avoid confusion.
       We also want to use per/thread data and avoid sync wherever possible.
    */
    PoolTcpEndpoint endpoint;
    
    public TcpWorkerThread(PoolTcpEndpoint endpoint) {
	this.endpoint = endpoint;
    }

    public Object[] getInitData() {
        // no synchronization overhead, but 2 array access 
        Object obj[]=new Object[2];
        obj[1]= endpoint.getConnectionHandler().init();
        obj[0]=new TcpConnection();
        return obj;
    }
    
    public void runIt(Object perThrData[]) {

	// Create per-thread cache
	if (endpoint.isRunning()) {
	    Socket s = null;
	    try {
                s = endpoint.acceptSocket();
	    } finally {
		// Continue accepting on another thread...
                if (endpoint.isRunning()) {
                    endpoint.tp.runIt(this);
                }
            }
	    if (null != s) {

                TcpConnection con = null;
                int step = 1;
                try {

                    // 1: Set socket options: timeout, linger, etc
                    endpoint.setSocketOptions( s );

                    // 2: SSL handshake
                    step = 2;
 		    if(endpoint.getServerSocketFactory()!=null) {
                        endpoint.getServerSocketFactory().handshake(s);
 		    }

                    // 3: Process the connection
                    step = 3;
                    con = (TcpConnection) perThrData[0];
		    con.setEndpoint(endpoint);
		    con.setSocket(s);
		    endpoint.getConnectionHandler()
                        .processConnection(con, (Object []) perThrData[1]);

                } catch (SocketException se) {
                    endpoint.log.error
                        ("Remote Host " + s.getInetAddress() +
                         " SocketException: " + se.getMessage());
                    // Try to close the socket
                    try {
                        s.close();
                    } catch (IOException e) {}
                } catch (Throwable t) {
                    if (step == 2) {
                        endpoint.log.error("Handshake failed", t);
                    } else {
                        endpoint.log.error("Unexpected error", t);
                    }
                    // Try to close the socket
                    try {
                        s.close();
                    } catch (IOException e) {}
                } finally {
                    if (con != null) {
                        con.recycle();
                    }
                }
	    }
	}
    }

}
