

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
package org.apache.coyote.http11;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.coyote.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadWithAttributes;
import org.apache.tomcat.util.net.*;
import com.sun.org.apache.commons.modeler.Registry;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;


/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public class Http11Protocol implements ProtocolHandler, MBeanRegistration
{
    // START SJSAS 6439313     
    protected boolean blocking = false;
    // END SJSAS 6439313     

    /**
     * The <code>SelectorThread</code> implementation class. Not used when 
     * Coyote is used.
     */
    protected String selectorThreadImpl = null; 
    
    
    public Http11Protocol() {
        // START SJSAS 6439313 
        this(false,false,null);
    }

    
    public Http11Protocol(boolean secure, boolean blocking, 
                          String selectorThreadImpl) {
        this.secure = secure;
        this.blocking = blocking; 
        this.selectorThreadImpl = selectorThreadImpl;
        // END SJSAS 6439313
        create();
    }
   
    protected void create() {
        cHandler = new Http11ConnectionHandler( this );
        setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        setServerSoTimeout(Constants.DEFAULT_SERVER_SOCKET_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }


    public int getMaxHttpHeaderSize() {
        return maxHttpHeaderSize;
    }
    

    public void setMaxHttpHeaderSize(int valueI) {
        maxHttpHeaderSize = valueI;
        setAttribute("maxHttpHeaderSize", "" + valueI);
    }
    

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /** Pass config info
     */
    public void setAttribute( String name, Object value ) {
        if( log.isTraceEnabled())
            log.trace(sm.getString("http11protocol.setattribute", name, value));
        attributes.put(name, value);
/*
        if ("maxKeepAliveRequests".equals(name)) {
            maxKeepAliveRequests = Integer.parseInt((String) value.toString());
        } else if ("port".equals(name)) {
            setPort(Integer.parseInt((String) value.toString()));
        }
*/
    }

    public Object getAttribute( String key ) {
        return attributes.get(key);
    }

    /**
     * Set a property.
     */
    public void setProperty(String name, String value) {
        setAttribute(name, value);
    }

    /**
     * Get a property
     */
    public String getProperty(String name) {
        return (String)getAttribute(name);
    }

    /** The adapter, used to call the connector 
     */
    public void setAdapter(Adapter adapter) {
        this.adapter=adapter;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    
    /** Start the protocol
     */
    public void init() throws Exception {
        ep.setConnectionHandler( cHandler );
	try {
            checkSocketFactory();
        } catch( Exception ex ) {
            log.error(sm.getString("http11protocol.socketfactory.initerror"),
                      ex);
            throw ex;
        }

        if( socketFactory!=null ) {
            Enumeration attE=attributes.keys();
            while( attE.hasMoreElements() ) {
                String key=(String)attE.nextElement();
                Object v=attributes.get( key );
                socketFactory.setAttribute( key, v );
            }
        }

        // XXX get domain from registration
        try {
            ep.initEndpoint();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.initerror"), ex);
            throw ex;
        }
        log.info(sm.getString("http11protocol.init", "" + ep.getPort(),
                System.getProperty("product.name")));

    }
    
    ObjectName tpOname;
    ObjectName rgOname;
    
    public void start() throws Exception {
        if( this.domain != null ) {
            try {
                // XXX We should be able to configure it separately
                // XXX It should be possible to use a single TP
                tpOname=new ObjectName(domain + ":" + "type=ThreadPool,name=http" + ep.getPort());
                Registry.getRegistry().registerComponent(tp, tpOname, null );
                tp.setName("http" + ep.getPort());
                tp.addThreadPoolListener(new MXPoolListener(this, tp));
            } catch (Exception e) {
                log.error("Can't register threadpool" );
            }
            rgOname=new ObjectName( domain + 
                    ":type=GlobalRequestProcessor,name=http" +
                    ep.getPort());
            Registry.getRegistry().registerComponent( cHandler.global,
                    rgOname, null );
        }

        try {
            ep.startEndpoint();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.starterror"), ex);
            throw ex;
        }
        log.info(sm.getString("http11protocol.start", "" + ep.getPort(),
                System.getProperty("product.name")));
    }

    public void destroy() throws Exception {
        log.info("Stoping http11 protocol on " + ep.getPort() + " " + tpOname);
        ep.stopEndpoint();
        if( tpOname!=null ) 
            Registry.getRegistry().unregisterComponent(tpOname);
        if( rgOname != null ) 
            Registry.getRegistry().unregisterComponent(rgOname);
    }
    
    // -------------------- Properties--------------------
    protected ThreadPool tp=ThreadPool.createThreadPool(true);
    protected PoolTcpEndpoint ep=new PoolTcpEndpoint(tp);
    protected boolean secure;
    
    protected ServerSocketFactory socketFactory;
    protected SSLImplementation sslImplementation;
    // socket factory attriubtes ( XXX replace with normal setters ) 
    protected Hashtable attributes = new Hashtable();
    protected String socketFactoryName=null;
    protected String sslImplementationName=null;

    private int maxKeepAliveRequests=100; // as in Apache HTTPD server
    protected int timeout = 300000;	// 5 minutes as in Apache HTTPD server
    protected int maxPostSize = 2 * 1024 * 1024;
    protected int maxHttpHeaderSize = 4 * 1024;
    private String reportedname;
    protected int socketCloseDelay=-1;
    protected boolean disableUploadTimeout = true;
    protected Adapter adapter;
    private Http11ConnectionHandler cHandler;
    
    // START OF SJSAS PE 8.1 6172948
    /**
     * The input request buffer size.
     */
    protected int requestBufferSize = 4096;
    // END OF SJSAS PE 8.1 6172948
    
    /**
     * Compression value.
     */
    protected String compression = "off";

    // -------------------- Pool setup --------------------

    public boolean getPools() {
        return ep.isPoolOn();
    }

    public void setPools( boolean t ) {
	ep.setPoolOn(t);
        setAttribute("pools", "" + t);
    }

    public int getMaxThreads() {
        return ep.getMaxThreads();
    }

    public void setMaxThreads( int maxThreads ) {
	ep.setMaxThreads(maxThreads);
        setAttribute("maxThreads", "" + maxThreads);
    }

    public int getMaxSpareThreads() {
        return ep.getMaxSpareThreads();
    }

    public void setMaxSpareThreads( int maxThreads ) {
	ep.setMaxSpareThreads(maxThreads);
        setAttribute("maxSpareThreads", "" + maxThreads);
    }

    public int getMinSpareThreads() {
        return ep.getMinSpareThreads();
    }

    public void setMinSpareThreads( int minSpareThreads ) {
	ep.setMinSpareThreads(minSpareThreads);
        setAttribute("minSpareThreads", "" + minSpareThreads);
    }

    // -------------------- Tcp setup --------------------

    public int getBacklog() {
        return ep.getBacklog();
    }
    public void setBacklog( int i ) {
	ep.setBacklog(i);
        setAttribute("backlog", "" + i);
    }
    
    public int getPort() {
        return ep.getPort();
    }

    public void setPort( int port ) {
	ep.setPort(port);
        setAttribute("port", "" + port);
    	//this.port=port;
    }

    public InetAddress getAddress() {
        return ep.getAddress();
    }

    public void setAddress(InetAddress ia) {
	ep.setAddress( ia );
        setAttribute("address", "" + ia);
    }

    //public void setHostName( String name ) {
	// ??? Doesn't seem to be used in existing or prev code
	// vhost=name;
    //}

    public String getSocketFactory() {
        return socketFactoryName;
    }

    public void setSocketFactory( String valueS ) {
	socketFactoryName = valueS;
        setAttribute("socketFactory", valueS);
    }

    public String getSSLImplementation() {
        return sslImplementationName;
    }

    public void setSSLImplementation( String valueS) {
 	sslImplementationName=valueS;
        setAttribute("sslImplementation", valueS);
    }
 	
    public boolean getTcpNoDelay() {
        return ep.getTcpNoDelay();
    }

    public void setTcpNoDelay( boolean b ) {
	ep.setTcpNoDelay( b );
        setAttribute("tcpNoDelay", "" + b);
    }

    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }

    public void setDisableUploadTimeout(boolean isDisabled) {
        disableUploadTimeout = isDisabled;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String valueS) {
        compression = valueS;
        setAttribute("compression", valueS);
    }

    public int getMaxPostSize() {
        return maxPostSize;
    }

    public void setMaxPostSize(int valueI) {
        maxPostSize = valueI;
        setAttribute("maxPostSize", "" + valueI);
    }
  
    public int getSoLinger() {
        return ep.getSoLinger();
    }

    public void setSoLinger( int i ) {
	ep.setSoLinger( i );
        setAttribute("soLinger", "" + i);
    }

    public int getSoTimeout() {
        return ep.getSoTimeout();
    }

    public void setSoTimeout( int i ) {
	ep.setSoTimeout(i);
        setAttribute("soTimeout", "" + i);
    }
    
    public int getServerSoTimeout() {
        return ep.getServerSoTimeout();
    }

    public void setServerSoTimeout( int i ) {
	ep.setServerSoTimeout(i);
        setAttribute("serverSoTimeout", "" + i);
    }
    
    public String getKeystore() {
        return getProperty("keystore");
    }

    public void setKeystore( String k ) {
        setAttribute("keystore", k);
    }

    public String getKeypass() {
        return getProperty("keypass");
    }

    public void setKeypass( String k ) {
        attributes.put("keypass", k);
        //setAttribute("keypass", k);
    }

    public String getKeytype() {
        return getProperty("keystoreType");
    } 

    public void setKeytype( String k ) {
        setAttribute("keystoreType", k);
    }

    // START GlassFish Issue 657
    public void setTruststore(String truststore) {
        setAttribute("truststore", truststore);
    }

    public void setTruststoreType(String truststoreType) {
        setAttribute("truststoreType", truststoreType);
    }    
    // END GlassFish Issue 657

    public String getClientauth() {
        return getProperty("clientauth");
    }

    public void setClientauth( String k ) {
        setAttribute("clientauth", k);
    }
    
    public String getProtocol() {
        return getProperty("protocol");
    }

    public void setProtocol( String k ) {
        setAttribute("protocol", k);
    }

    public String getProtocols() {
        return getProperty("protocols");
    }

    public void setProtocols(String k) {
        setAttribute("protocols", k);
    }

    public String getAlgorithm() {
        return getProperty("algorithm");
    }

    public void setAlgorithm( String k ) {
        setAttribute("algorithm", k);
    }

    public boolean getSecure() {
        return secure;
    }

    public void setSecure( boolean b ) {
    	secure=b;
        setAttribute("secure", "" + b);
    }
    
    // START SJSAS 6439313     
    public boolean getBlocking() {
        return blocking;
    }

    public void setBlocking( boolean b ) {
    	blocking=b;
        setAttribute("blocking", "" + b);
    }
    // END SJSAS 6439313     
    
    public String getCiphers() {
        return getProperty("ciphers");
    }

    public void setCiphers(String ciphers) {
        setAttribute("ciphers", ciphers);
    }

    public String getKeyAlias() {
        return getProperty("keyAlias");
    }

    public void setKeyAlias(String keyAlias) {
        setAttribute("keyAlias", keyAlias);
    }

    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }

    /** Set the maximum number of Keep-Alive requests that we will honor.
     */
    public void setMaxKeepAliveRequests(int mkar) {
	maxKeepAliveRequests = mkar;
        setAttribute("maxKeepAliveRequests", "" + mkar);
    }

    public int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    public void setSocketCloseDelay( int d ) {
        socketCloseDelay=d;
        setAttribute("socketCloseDelay", "" + d);
    }

    protected static ServerSocketFactory string2SocketFactory( String val)
	throws ClassNotFoundException, IllegalAccessException,
	InstantiationException
    {
	Class chC=Class.forName( val );
	return (ServerSocketFactory)chC.newInstance();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout( int timeouts ) {
	timeout = timeouts * 1000;
        setAttribute("timeout", "" + timeouts);
    }
 
    public String getReportedname() {
        return reportedname;
    }

    public void setReportedname( String reportedName) {
	reportedname = reportedName;
    }
    
    // --------------------  Connection handler --------------------
    public static final int THREAD_DATA_PROCESSOR=1;
    public static final int THREAD_DATA_OBJECT_NAME=2;
    
    
    static class MXPoolListener implements ThreadPool.ThreadPoolListener {
        MXPoolListener( Http11Protocol proto, ThreadPool control ) {
            
        }

        public void threadStart(ThreadPool tp, Thread t) {
        }

        public void threadEnd(ThreadPool tp, Thread t) {
            // Register our associated processor
            // TP uses only TWA
            ThreadWithAttributes ta=(ThreadWithAttributes)t;
            Object tpData[]=ta.getThreadData(tp);
            if( tpData==null ) return;
            // Weird artifact - it should be cleaned up, but that may break something
            // and it won't gain us too much
            if( tpData[1] instanceof Object[] ) {
                tpData=(Object [])tpData[1];
            }
            ObjectName oname=(ObjectName)tpData[Http11Protocol.THREAD_DATA_OBJECT_NAME];
            if( oname==null ) return;
            Registry.getRegistry().unregisterComponent(oname);
            Http11Processor processor = 
                (Http11Processor) tpData[Http11Protocol.THREAD_DATA_PROCESSOR];
            RequestInfo rp=processor.getRequest().getRequestProcessor();
            rp.setGlobalProcessor(null);
        }
    }

    static class Http11ConnectionHandler implements TcpConnectionHandler {
        Http11Protocol proto;
        static int count=0;
        RequestGroupInfo global=new RequestGroupInfo();

        Http11ConnectionHandler( Http11Protocol proto ) {
            this.proto=proto;
        }
        
        public void setAttribute( String name, Object value ) {
        }
        
        public void setServer( Object o ) {
        }
    
        public Object[] init() {
            Object thData[]=new Object[3];
            
            Http11Processor  processor = 
                new Http11Processor(proto.maxHttpHeaderSize);
            processor.setAdapter( proto.adapter );
            processor.setThreadPool( proto.tp );
            processor.setMaxKeepAliveRequests( proto.maxKeepAliveRequests );
            processor.setTimeout( proto.timeout );
            processor.setDisableUploadTimeout( proto.disableUploadTimeout );
            processor.setCompression( proto.compression );
            processor.setMaxPostSize( proto.maxPostSize );

            // START OF SJSAS PE 8.1 6172948
            processor.setBufferSize(proto.requestBufferSize);
            // END OF SJSAS PE 8.1 6172948

            thData[Http11Protocol.THREAD_DATA_PROCESSOR]=processor;
            
            if( proto.getDomain() != null ) {
                try {
                    RequestInfo rp=processor.getRequest().getRequestProcessor();
                    rp.setGlobalProcessor(global);
                    ObjectName rpName=new ObjectName(proto.getDomain() + 
                            ":type=RequestProcessor,worker=http" +
                            proto.ep.getPort() +",name=HttpRequest" + count++ );
                    Registry.getRegistry().registerComponent( rp, rpName, null);
                    thData[Http11Protocol.THREAD_DATA_OBJECT_NAME]=rpName;
                } catch( Exception ex ) {
                    log.warn("Error registering request");
                }
            }

            return  thData;
        }

        public void processConnection(TcpConnection connection,
				      Object thData[]) {
            Socket socket=null;
            Http11Processor  processor=null;
            try {
                processor=(Http11Processor)thData[Http11Protocol.THREAD_DATA_PROCESSOR];
                
                if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_START, null);
                }
                socket=connection.getSocket();
                
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                if( proto.secure ) {
                    SSLSupport sslSupport=null;
                    if(proto.sslImplementation != null)
                        sslSupport = proto.sslImplementation.getSSLSupport(socket);
                    processor.setSSLSupport(sslSupport);
                } else {
                    processor.setSSLSupport( null );
                }
                processor.setSocket( socket );
                
                processor.process(in, out);
                
                // If unread input arrives after the shutdownInput() call
                // below and before or during the socket.close(), an error
                // may be reported to the client.  To help troubleshoot this
                // type of error, provide a configurable delay to give the
                // unread input time to arrive so it can be successfully read
                // and discarded by shutdownInput().
                if( proto.socketCloseDelay >= 0 ) {
                    try {
                        Thread.sleep(proto.socketCloseDelay);
                    } catch (InterruptedException ie) { /* ignore */ }
                }
                
                TcpConnection.shutdownInput( socket );
            } catch(java.net.SocketException e) {
                // SocketExceptions are normal
                proto.log.debug
                    (sm.getString
                     ("http11protocol.proto.socketexception.debug"), e);
            } catch (java.io.IOException e) {
                // IOExceptions are normal 
                proto.log.debug
                    (sm.getString
                     ("http11protocol.proto.ioexception.debug"), e);
            }
            // Future developers: if you discover any other
            // rare-but-nonfatal exceptions, catch them here, and log as
            // above.
            catch (Throwable e) {
                // any other exception or error is odd. Here we log it
                // with "ERROR" level, so it will show up even on
                // less-than-verbose logs.
                proto.log.error(sm.getString("http11protocol.proto.error"), e);
            } finally {
                //       if(proto.adapter != null) proto.adapter.recycle();
                //                processor.recycle();
                
                if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_STOP, null);
                }
                // recycle kernel sockets ASAP
                try { if (socket != null) socket.close (); }
                catch (IOException e) { /* ignore */ }
            }
        }
    }

    protected static final com.sun.org.apache.commons.logging.Log log 
        = com.sun.org.apache.commons.logging.LogFactory.getLog(Http11Protocol.class);

    // -------------------- Various implementation classes --------------------

    /** Sanity check and socketFactory setup.
     *  IMHO it is better to stop the show on a broken connector,
     *  then leave Tomcat running and broken.
     *  @exception TomcatException Unable to resolve classes
     */
    private void checkSocketFactory() throws Exception {
	if(secure) {
 	    try {
 		// The SSL setup code has been moved into
 		// SSLImplementation since SocketFactory doesn't
 		// provide a wide enough interface
 		sslImplementation=SSLImplementation.getInstance
 		    (sslImplementationName);
                socketFactory = 
                        sslImplementation.getServerSocketFactory();
		ep.setServerSocketFactory(socketFactory);
 	    } catch (ClassNotFoundException e){
 		throw e;
  	    }
  	}
 	else {
 	    if (socketFactoryName != null) {
 		try {
 		    socketFactory = string2SocketFactory(socketFactoryName);
 		    ep.setServerSocketFactory(socketFactory);
 		} catch(Exception sfex) {
 		    throw sfex;
 		}
	    }
	}
    }

    /*

    public boolean isKeystoreSet() {
        return (attributes.get("keystore") != null);
    }

    public boolean isKeypassSet() {
        return (attributes.get("keypass") != null);
    }

    public boolean isClientauthSet() {
        return (attributes.get("clientauth") != null);
    }

    public boolean isAttributeSet( String attr ) {
        return (attributes.get(attr) != null);
    }

    public boolean isSecure() {
        return secure;
    }

   
    public PoolTcpEndpoint getEndpoint() {
	return ep;
    }
    
    */

    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
    
    // START OF SJSAS PE 8.1 6172948
    /**
     * Set the request input buffer size
     */
    public void setBufferSize(int requestBufferSize){
        this.requestBufferSize = requestBufferSize;
    }
    

    /**
     * Return the request input buffer size
     */
    public int getBufferSize(){
        return requestBufferSize;
    }    
    // END OF SJSAS PE 8.1 6172948 
}
